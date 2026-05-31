package ak.neocobblewatch.core

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.api.ApiContext
import ak.neocobblewatch.api.HttpServer
import ak.neocobblewatch.party.PartyRepository
import ak.neocobblewatch.pc.PcRepository
import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.ModStateRepository
import ak.neocobblewatch.player.PlayerRepository
import ak.neocobblewatch.pokedex.PokedexRepository
import ak.neocobblewatch.snapshot.BackfillRunner
import ak.neocobblewatch.snapshot.PlayerSnapshotter
import ak.neocobblewatch.snapshot.SnapshotScheduler
import ak.neocobblewatch.species.SpeciesLabelsCache
import ak.neocobblewatch.stats.StatsRepository
import ak.neocobblewatch.stats.TopsRepository
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.LevelResource

internal object ModLifecycle {
    val modScope: ModScope = ModScope()
    val serverThreadDispatcher: ServerThreadDispatcher = ServerThreadDispatcher()
    val database: Database = Database()

    private var scheduler: SnapshotScheduler? = null
    private var httpServer: HttpServer? = null

    fun register() {
        ServerLifecycleEvents.SERVER_STARTING.register(::onServerStarting)
        ServerLifecycleEvents.SERVER_STOPPING.register(::onServerStopping)
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ -> onPlayerJoin(handler.player) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> onPlayerLeave(handler.player) }
    }

    private fun onServerStarting(server: MinecraftServer) {
        if (modScope.isStarted || serverThreadDispatcher.isBound || database.isOpen) {
            Neocobblewatch.LOGGER.warn("Server starting while previous cycle was not torn down — recycling")
            httpServer?.stop()
            httpServer = null
            scheduler?.stop()
            scheduler = null
            modScope.cancel()
            database.close()
            serverThreadDispatcher.unbind()
        }
        serverThreadDispatcher.bind(server)
        modScope.start()
        val worldRoot = server.getWorldPath(LevelResource.ROOT)
        val dbPath = worldRoot.resolve(Config.database().path)
        database.open(dbPath)

        val (activeScheduler, apiCtx, backfill) = buildRuntime(server, worldRoot)
        scheduler = activeScheduler.also { it.start() }
        httpServer = HttpServer(apiCtx).also { it.start() }

        modScope.scope.launch(CoroutineName("backfill")) { backfill.runIfNeeded() }

        val http = Config.http()
        val snapshot = Config.snapshot()
        Neocobblewatch.LOGGER.info(
            "NeoCobbleWatch started — HTTP {}:{}; snapshot every {}s (parallel {}); db {}",
            http.bind,
            http.port,
            snapshot.intervalSeconds,
            snapshot.parallelPlayerLimit,
            dbPath,
        )
    }

    private fun onServerStopping(server: MinecraftServer) {
        Neocobblewatch.LOGGER.info("NeoCobbleWatch stopping")
        httpServer?.stop()
        httpServer = null
        scheduler?.stop()
        scheduler = null
        modScope.cancel()
        database.close()
        serverThreadDispatcher.unbind()
        Neocobblewatch.LOGGER.info("NeoCobbleWatch stopped")
    }

    private fun onPlayerJoin(player: ServerPlayer) {
        if (!Config.snapshot().snapshotOnLogin) return
        scheduler?.snapshotPlayer(player, online = true)
    }

    private fun onPlayerLeave(player: ServerPlayer) {
        if (!Config.snapshot().snapshotOnLogout) return
        scheduler?.snapshotPlayer(player, online = false)
    }

    private data class Runtime(
        val scheduler: SnapshotScheduler,
        val apiContext: ApiContext,
        val backfill: BackfillRunner,
    )

    private fun buildRuntime(server: MinecraftServer, worldRoot: java.nio.file.Path): Runtime {
        val playerRepo = PlayerRepository(database)
        val statsRepo = StatsRepository(database)
        val pokedexRepo = PokedexRepository(database)
        val partyRepo = PartyRepository(database)
        val pcRepo = PcRepository(database)
        val topsRepo = TopsRepository(database)
        val modStateRepo = ModStateRepository(database)
        val snapshotter = PlayerSnapshotter(
            serverThreadDispatcher = serverThreadDispatcher,
            database = database,
            playerRepository = playerRepo,
            statsRepository = statsRepo,
            pokedexRepository = pokedexRepo,
            partyRepository = partyRepo,
            pcRepository = pcRepo,
        )
        val scheduler = SnapshotScheduler(
            scope = modScope.scope,
            server = server,
            serverThreadDispatcher = serverThreadDispatcher,
            config = Config.snapshot(),
            snapshotter = snapshotter,
        )
        val backfill = BackfillRunner(
            server = server,
            cobblemonDataDir = worldRoot.resolve("cobblemonplayerdata"),
            snapshotter = snapshotter,
            modState = modStateRepo,
            parallelLimit = Config.snapshot().parallelPlayerLimit,
        )
        val apiCtx = ApiContext(
            httpConfig = Config.http(),
            scheduler = scheduler,
            database = database,
            playerRepository = playerRepo,
            statsRepository = statsRepo,
            pokedexRepository = pokedexRepo,
            partyRepository = partyRepo,
            pcRepository = pcRepo,
            topsRepository = topsRepo,
            speciesLabelsCache = SpeciesLabelsCache(),
        )
        return Runtime(scheduler, apiCtx, backfill)
    }
}
