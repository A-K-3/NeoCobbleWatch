package ak.neocobblewatch.core

import ak.neocobblewatch.Neocobblewatch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal data class HttpConfig(
    val port: Int,
    val bind: String,
)

internal data class SnapshotConfig(
    val intervalSeconds: Int,
    val snapshotOnLogin: Boolean,
    val snapshotOnLogout: Boolean,
    val parallelPlayerLimit: Int,
)

internal data class DatabaseConfig(
    val path: String,
)

@Serializable
private data class ServerSection(
    val httpPort: Int = 8080,
    val httpBind: String = "127.0.0.1",
)

@Serializable
private data class SnapshotSection(
    val intervalSeconds: Int = 300,
    val snapshotOnLogin: Boolean = true,
    val snapshotOnLogout: Boolean = true,
    val parallelPlayerLimit: Int = 4,
)

@Serializable
private data class DatabaseSection(
    val path: String = "neocobblewatch.db",
)

@Serializable
private data class ModConfigFile(
    val server: ServerSection = ServerSection(),
    val snapshot: SnapshotSection = SnapshotSection(),
    val database: DatabaseSection = DatabaseSection(),
)

/**
 * JSON config loaded once at mod init. Path: `config/neocobblewatch.json` (FabricLoader's config dir).
 * If the file is missing, a default one is written. Unknown keys are ignored to ease forward-compat.
 */
internal object Config {
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Volatile
    private var current: ModConfigFile = ModConfigFile()

    fun load() {
        val configPath = configPath()
        if (!configPath.exists()) {
            Files.createDirectories(configPath.parent)
            configPath.writeText(json.encodeToString(ModConfigFile()))
            Neocobblewatch.LOGGER.info("Generated default config at {}", configPath)
        }
        current = try {
            json.decodeFromString<ModConfigFile>(configPath.readText())
        } catch (e: Exception) {
            Neocobblewatch.LOGGER.error("Failed to read config at {}: {}. Falling back to defaults.", configPath, e.message)
            ModConfigFile()
        }
    }

    fun http(): HttpConfig = HttpConfig(
        port = current.server.httpPort,
        bind = current.server.httpBind,
    )

    fun snapshot(): SnapshotConfig = SnapshotConfig(
        intervalSeconds = current.snapshot.intervalSeconds,
        snapshotOnLogin = current.snapshot.snapshotOnLogin,
        snapshotOnLogout = current.snapshot.snapshotOnLogout,
        parallelPlayerLimit = current.snapshot.parallelPlayerLimit,
    )

    fun database(): DatabaseConfig = DatabaseConfig(path = current.database.path)

    private fun configPath(): Path =
        FabricLoader.getInstance().configDir.resolve("${Neocobblewatch.ID}.json")
}
