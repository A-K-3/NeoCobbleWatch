package ak.neocobblewatch.snapshot

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.persistence.ModStateRepository
import ak.neocobblewatch.player.OfflinePlayerContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import net.minecraft.server.MinecraftServer
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

/**
 * One-shot enumeration of `<world>/cobblemonplayerdata/` to seed the DB with offline players
 * the first time the mod runs on an existing world. After completion the flag in `mod_state`
 * prevents re-runs; from then on, online players are kept fresh by [SnapshotScheduler] and the
 * JOIN/DISCONNECT hooks in `ModLifecycle`.
 *
 * Idempotent at the per-player level (all repository writes are upserts), so a crash mid-run
 * just means the next startup retries the whole set.
 */
internal class BackfillRunner(
    private val server: MinecraftServer,
    private val cobblemonDataDir: Path,
    private val snapshotter: PlayerSnapshotter,
    private val modState: ModStateRepository,
    private val parallelLimit: Int,
) {
    suspend fun runIfNeeded() {
        if (modState.get(ModStateRepository.Keys.BACKFILL_COMPLETED_AT) != null) {
            Neocobblewatch.LOGGER.debug("Backfill already completed, skipping.")
            return
        }
        try {
            run()
        } catch (e: CancellationException) {
            Neocobblewatch.LOGGER.info("Backfill cancelled before completion — will retry on next start.")
            throw e
        } catch (e: Exception) {
            Neocobblewatch.LOGGER.error("Backfill failed — will retry on next start: {}", e.message, e)
        }
    }

    private suspend fun run() {
        if (!cobblemonDataDir.exists()) {
            Neocobblewatch.LOGGER.info(
                "No cobblemonplayerdata/ at {} — treating as empty world, marking backfill complete.",
                cobblemonDataDir,
            )
            modState.set(ModStateRepository.Keys.BACKFILL_COMPLETED_AT, System.currentTimeMillis().toString())
            return
        }

        val contexts = enumerateOfflinePlayers()
        if (contexts.isEmpty()) {
            Neocobblewatch.LOGGER.info("Backfill: cobblemonplayerdata/ is empty, nothing to do.")
            modState.set(ModStateRepository.Keys.BACKFILL_COMPLETED_AT, System.currentTimeMillis().toString())
            return
        }

        Neocobblewatch.LOGGER.info("Backfill: discovered {} offline player(s); starting…", contexts.size)
        val started = System.currentTimeMillis()
        val semaphore = Semaphore(parallelLimit)
        val done = AtomicInteger(0)
        coroutineScope {
            contexts.map { ctx ->
                async {
                    semaphore.withPermit { snapshotter.snapshot(server, ctx) }
                    val n = done.incrementAndGet()
                    if (n % 50 == 0 || n == contexts.size) {
                        Neocobblewatch.LOGGER.info("Backfill progress: {}/{}", n, contexts.size)
                    }
                }
            }.awaitAll()
        }
        val elapsed = System.currentTimeMillis() - started
        modState.set(ModStateRepository.Keys.BACKFILL_COMPLETED_AT, System.currentTimeMillis().toString())
        Neocobblewatch.LOGGER.info("Backfill complete: {} player(s) in {} ms.", contexts.size, elapsed)
    }

    private fun enumerateOfflinePlayers(): List<OfflinePlayerContext> {
        // Cobblemon may shard player files (e.g. `<prefix>/<uuid>.json`); walking the tree handles
        // both flat and sharded layouts. We dedupe by UUID because the same player may appear under
        // multiple files if Cobblemon stores per-domain data separately within this dir.
        val seen = mutableSetOf<UUID>()
        val results = mutableListOf<OfflinePlayerContext>()
        Files.walk(cobblemonDataDir).use { stream ->
            stream.forEach { path ->
                if (!path.isRegularFile()) return@forEach
                val uuid = uuidFromFilename(path) ?: return@forEach
                if (!seen.add(uuid)) return@forEach
                results += buildContext(uuid, path)
            }
        }
        return results
    }

    private fun buildContext(uuid: UUID, dataFile: Path): OfflinePlayerContext {
        val mtime = runCatching { Files.getLastModifiedTime(dataFile).toMillis() }.getOrDefault(0L)
        val name = server.profileCache?.get(uuid)?.orElse(null)?.name
            ?: uuid.toString().substring(0, 8)
        return OfflinePlayerContext(uuid = uuid, name = name, firstSeen = mtime, lastSeen = mtime)
    }

    private fun uuidFromFilename(path: Path): UUID? {
        val name = path.fileName.toString()
        if (!name.endsWith(".json", ignoreCase = true)) return null
        val raw = name.substring(0, name.length - ".json".length)
        return runCatching { UUID.fromString(raw) }.getOrNull()
    }
}
