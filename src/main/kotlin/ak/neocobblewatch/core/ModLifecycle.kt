package ak.neocobblewatch.core

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.api.ApiContext
import ak.neocobblewatch.api.HttpServer
import ak.neocobblewatch.party.PartyRepository
import ak.neocobblewatch.pc.PcRepository
import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.player.PlayerRepository
import ak.neocobblewatch.pokedex.PokedexRepository
import ak.neocobblewatch.snapshot.PlayerSnapshotter
import ak.neocobblewatch.snapshot.SnapshotScheduler
import ak.neocobblewatch.stats.StatsRepository
import ak.neocobblewatch.stats.TopsRepository
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
        val dbPath = server.getWorldPath(LevelResource.ROOT).resolve(Config.database().path)
        database.open(dbPath)

        val (activeScheduler, apiCtx) = buildRuntime(server)
        scheduler = activeScheduler.also { it.start() }
        httpServer = HttpServer(apiCtx).also { it.start() }

        val http = Config.http()
        val snapshot = Config.snapshot()
        Neocobblewatch.LOGGER.info(
            "NeoCobbleWatch started — HTTP {}:{} (CORS origins: {}); snapshot every {}s (parallel {}); db {}",
            http.bind,
            http.port,
            if (http.corsAllowedOrigins.isEmpty()) "none" else http.corsAllowedOrigins.joinToString(),
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

    private fun buildRuntime(server: MinecraftServer): Pair<SnapshotScheduler, ApiContext> {
        val playerRepo = PlayerRepository(database)
        val statsRepo = StatsRepository(database)
        val pokedexRepo = PokedexRepository(database)
        val partyRepo = PartyRepository(database)
        val pcRepo = PcRepository(database)
        val topsRepo = TopsRepository(database)
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
        )
        return scheduler to apiCtx
    }
}
