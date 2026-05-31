package ak.neocobblewatch.snapshot

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.persistence.ModStateRepository
import ak.neocobblewatch.persistence.SqlJson
import ak.neocobblewatch.player.OfflinePlayerContext
import ak.neocobblewatch.player.PlayerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

/**
 * One-shot enumeration of all player data directories to seed the DB with offline players
 * the first time the mod runs on an existing world. After completion the flag in `mod_state`
 * prevents re-runs; from then on, online players are kept fresh by [SnapshotScheduler] and the
 * JOIN/DISCONNECT hooks in `ModLifecycle`.
 *
 * Scans every directory in [dataDirs] so players who have Minecraft stats or economy data but
 * no Cobblemon data are also included.
 *
 * Idempotent at the per-player level (all repository writes are upserts), so a crash mid-run
 * just means the next startup retries the whole set.
 */
internal class BackfillRunner(
    private val server: MinecraftServer,
    private val dataDirs: List<Path>,
    private val snapshotter: PlayerSnapshotter,
    private val modState: ModStateRepository,
    private val playerRepository: PlayerRepository,
    private val parallelLimit: Int,
) {
    suspend fun runIfNeeded() {
        // Runs every startup: fixes players whose name was stored as a UUID.
        refreshNamesFromFiles()
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

    suspend fun runForced() {
        Neocobblewatch.LOGGER.info("Forced resync requested — re-scanning all offline players.")
        refreshNamesFromFiles()
        try {
            run()
        } catch (e: CancellationException) {
            Neocobblewatch.LOGGER.info("Forced resync cancelled before completion.")
            throw e
        } catch (e: Exception) {
            Neocobblewatch.LOGGER.error("Forced resync failed: {}", e.message, e)
        }
    }

    private suspend fun refreshNamesFromFiles() {
        val names = loadNameSources()
        if (names.isEmpty()) {
            Neocobblewatch.LOGGER.debug("No valid player names found in server files — skipping name fix.")
            return
        }
        playerRepository.fixUuidLikeNames(names)
        Neocobblewatch.LOGGER.debug("Name fix: checked {} players from server files.", names.size)
    }

    private suspend fun run() {
        val contexts = enumerateOfflinePlayers()
        if (contexts.isEmpty()) {
            Neocobblewatch.LOGGER.info("Backfill: no player data found in any directory, nothing to do.")
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
        val nameCache = loadNameSources()
        val seen = mutableSetOf<UUID>()
        val results = mutableListOf<OfflinePlayerContext>()
        for (dir in dataDirs) {
            if (!dir.exists()) continue
            // Walk handles both flat and sharded layouts (Cobblemon may shard under subdirs).
            Files.walk(dir).use { stream ->
                stream.forEach { path ->
                    if (!path.isRegularFile()) return@forEach
                    val uuid = uuidFromFilename(path) ?: return@forEach
                    if (!seen.add(uuid)) return@forEach
                    results += buildContext(uuid, path, nameCache)
                }
            }
        }
        return results
    }

    private fun buildContext(uuid: UUID, dataFile: Path, nameCache: Map<UUID, String>): OfflinePlayerContext {
        val mtime = runCatching { Files.getLastModifiedTime(dataFile).toMillis() }.getOrDefault(0L)
        val name = server.profileCache?.get(uuid)?.orElse(null)?.name
            ?: nameCache[uuid]
            ?: uuid.toString()
        return OfflinePlayerContext(uuid = uuid, name = name, firstSeen = mtime, lastSeen = mtime)
    }

    // Reads player UUID→name from multiple server files. whitelist.json is the most reliable
    // source on servers with custom auth systems that corrupt usercache.json.
    // Entries where "name" contains a dash are skipped — they're UUID-like, not real names.
    private fun loadNameSources(): Map<UUID, String> {
        val result = mutableMapOf<UUID, String>()
        for (filename in listOf("usercache.json", "ops.json", "whitelist.json")) {
            val file = FabricLoader.getInstance().gameDir.resolve(filename)
            if (!file.exists()) continue
            try {
                SqlJson.parseToJsonElement(file.readText()).jsonArray
                    .mapNotNull { el ->
                        val obj = el as? JsonObject ?: return@mapNotNull null
                        val uuidStr = obj["uuid"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                        val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                        if (name.contains('-')) return@mapNotNull null
                        runCatching { UUID.fromString(uuidStr) }.getOrNull()?.let { it to name }
                    }
                    .forEach { (uuid, name) -> result[uuid] = name }
            } catch (e: Exception) {
                Neocobblewatch.LOGGER.warn("Failed to read {}: {}", filename, e.message)
            }
        }
        return result
    }

    private fun uuidFromFilename(path: Path): UUID? {
        val name = path.fileName.toString()
        if (!name.endsWith(".json", ignoreCase = true)) return null
        val raw = name.substring(0, name.length - ".json".length)
        return runCatching { UUID.fromString(raw) }.getOrNull()
    }
}
