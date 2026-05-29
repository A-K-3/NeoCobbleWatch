package ak.neocobblewatch.core

internal data class HttpConfig(
    val port: Int,
    val bind: String,
    val corsAllowedOrigins: List<String>,
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

/**
 * Read-only typed facade over [ConfigSpec]. Each call returns an immutable snapshot
 * of the current config; consumers should grab one at startup and not re-read mid-cycle.
 */
internal object Config {
    fun http(): HttpConfig = HttpConfig(
        port = ConfigSpec.httpPort.get(),
        bind = ConfigSpec.httpBind.get(),
        corsAllowedOrigins = ConfigSpec.corsAllowedOrigins.get().toList(),
    )

    fun snapshot(): SnapshotConfig = SnapshotConfig(
        intervalSeconds = ConfigSpec.snapshotIntervalSeconds.get(),
        snapshotOnLogin = ConfigSpec.snapshotOnLogin.get(),
        snapshotOnLogout = ConfigSpec.snapshotOnLogout.get(),
        parallelPlayerLimit = ConfigSpec.parallelPlayerLimit.get(),
    )

    fun database(): DatabaseConfig = DatabaseConfig(
        path = ConfigSpec.databasePath.get(),
    )
}
