package ak.neocobblewatch.snapshot

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.core.ServerThreadDispatcher
import ak.neocobblewatch.core.SnapshotConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

internal class SnapshotScheduler(
    private val scope: CoroutineScope,
    private val server: MinecraftServer,
    private val serverThreadDispatcher: ServerThreadDispatcher,
    private val config: SnapshotConfig,
    private val snapshotter: PlayerSnapshotter,
) {
    @Volatile
    var lastFullCycleAt: Long = 0L
        private set

    private var loopJob: Job? = null

    fun start() {
        check(loopJob == null) { "SnapshotScheduler already started" }
        loopJob = scope.launch(CoroutineName("snapshot-cycle")) { runLoop() }
        Neocobblewatch.LOGGER.info("SnapshotScheduler started — interval {}s, parallel {}", config.intervalSeconds, config.parallelPlayerLimit)
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
    }

    /** Fire-and-forget snapshot of a single player, used by login/logout triggers. */
    fun snapshotPlayer(player: ServerPlayer, online: Boolean = true) {
        scope.launch(CoroutineName("snapshot-${player.uuid}")) {
            snapshotter.snapshot(player, online)
        }
    }

    private suspend fun runLoop() {
        val intervalMs = config.intervalSeconds * 1000L
        while (scope.isActive) {
            val cycleStart = System.currentTimeMillis()
            runCycleSafely()
            lastFullCycleAt = System.currentTimeMillis()
            val elapsed = System.currentTimeMillis() - cycleStart
            val toSleep = (intervalMs - elapsed).coerceAtLeast(0L)
            delay(toSleep)
        }
    }

    private suspend fun runCycleSafely() {
        try {
            snapshotAllOnline()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Neocobblewatch.LOGGER.warn("Snapshot cycle failed: {}", e.message, e)
        }
    }

    private suspend fun snapshotAllOnline() {
        val players = withContext(serverThreadDispatcher) {
            server.playerList.players.toList()
        }
        if (players.isEmpty()) return
        val semaphore = Semaphore(config.parallelPlayerLimit)
        coroutineScope {
            players.map { player ->
                async { semaphore.withPermit { snapshotter.snapshot(player) } }
            }.awaitAll()
        }
    }
}
