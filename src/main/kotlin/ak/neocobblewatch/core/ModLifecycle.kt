package ak.neocobblewatch.core

import ak.neocobblewatch.Neocobblewatch
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent

internal object ModLifecycle {
    val modScope: ModScope = ModScope()
    val serverThreadDispatcher: ServerThreadDispatcher = ServerThreadDispatcher()

    fun register() {
        NeoForge.EVENT_BUS.addListener(::onServerStarting)
        NeoForge.EVENT_BUS.addListener(::onServerStopping)
    }

    private fun onServerStarting(event: ServerStartingEvent) {
        if (modScope.isStarted || serverThreadDispatcher.isBound) {
            Neocobblewatch.LOGGER.warn("Server starting while previous cycle was not torn down — recycling")
            modScope.cancel()
            serverThreadDispatcher.unbind()
        }
        serverThreadDispatcher.bind(event.server)
        modScope.start()

        val http = Config.http()
        val snapshot = Config.snapshot()
        val database = Config.database()
        Neocobblewatch.LOGGER.info(
            "NeoCobbleWatch started — HTTP {}:{} (CORS origins: {}); snapshot every {}s (parallel {}); db {}",
            http.bind,
            http.port,
            if (http.corsAllowedOrigins.isEmpty()) "none" else http.corsAllowedOrigins.joinToString(),
            snapshot.intervalSeconds,
            snapshot.parallelPlayerLimit,
            database.path,
        )
    }

    private fun onServerStopping(event: ServerStoppingEvent) {
        Neocobblewatch.LOGGER.info("NeoCobbleWatch stopping")
        modScope.cancel()
        serverThreadDispatcher.unbind()
        Neocobblewatch.LOGGER.info("NeoCobbleWatch stopped")
    }
}
