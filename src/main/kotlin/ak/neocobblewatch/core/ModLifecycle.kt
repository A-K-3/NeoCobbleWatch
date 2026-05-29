package ak.neocobblewatch.core

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.persistence.Database
import net.minecraft.world.level.storage.LevelResource
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent

internal object ModLifecycle {
    val modScope: ModScope = ModScope()
    val serverThreadDispatcher: ServerThreadDispatcher = ServerThreadDispatcher()
    val database: Database = Database()

    fun register() {
        NeoForge.EVENT_BUS.addListener(::onServerStarting)
        NeoForge.EVENT_BUS.addListener(::onServerStopping)
    }

    private fun onServerStarting(event: ServerStartingEvent) {
        if (modScope.isStarted || serverThreadDispatcher.isBound || database.isOpen) {
            Neocobblewatch.LOGGER.warn("Server starting while previous cycle was not torn down — recycling")
            modScope.cancel()
            database.close()
            serverThreadDispatcher.unbind()
        }
        serverThreadDispatcher.bind(event.server)
        modScope.start()
        val dbPath = event.server.getWorldPath(LevelResource.ROOT).resolve(Config.database().path)
        database.open(dbPath)

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

    private fun onServerStopping(event: ServerStoppingEvent) {
        Neocobblewatch.LOGGER.info("NeoCobbleWatch stopping")
        modScope.cancel()
        database.close()
        serverThreadDispatcher.unbind()
        Neocobblewatch.LOGGER.info("NeoCobbleWatch stopped")
    }
}
