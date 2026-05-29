package ak.neocobblewatch.core

import net.neoforged.neoforge.common.ModConfigSpec
import java.util.function.Predicate
import java.util.function.Supplier

internal object ConfigSpec {
    val httpPort: ModConfigSpec.IntValue
    val httpBind: ModConfigSpec.ConfigValue<String>
    val corsAllowedOrigins: ModConfigSpec.ConfigValue<List<String>>

    val snapshotIntervalSeconds: ModConfigSpec.IntValue
    val snapshotOnLogin: ModConfigSpec.BooleanValue
    val snapshotOnLogout: ModConfigSpec.BooleanValue
    val parallelPlayerLimit: ModConfigSpec.IntValue

    val databasePath: ModConfigSpec.ConfigValue<String>

    val spec: ModConfigSpec

    init {
        val builder = ModConfigSpec.Builder()

        builder.comment("HTTP server settings.").push("server")
        httpPort = builder
            .comment("Port to bind the HTTP server.")
            .defineInRange("http_port", 8080, 1, 65535)
        httpBind = builder
            .comment(
                "Interface to bind. 127.0.0.1 = local only (default).",
                "Change to 0.0.0.0 only if you intend to expose the API publicly — usually behind a reverse proxy.",
            )
            .define("http_bind", "127.0.0.1")
        corsAllowedOrigins = builder
            .comment(
                "Allowed origins for CORS. Empty list = same-origin only (no Access-Control-Allow-Origin header sent).",
                "Add the origin where the web frontend is served, e.g. \"https://cobbleverse.example\".",
            )
            .defineListAllowEmpty(
                "cors_allowed_origins",
                Supplier { emptyList() },
                Supplier { "" },
                Predicate { it is String },
            )
        builder.pop()

        builder.comment("Snapshot scheduler settings.").push("snapshot")
        snapshotIntervalSeconds = builder
            .comment(
                "Seconds between snapshot cycles.",
                "Next cycle starts at least this long after the previous one began; cycles never overlap.",
            )
            .defineInRange("interval_seconds", 300, 10, 86_400)
        snapshotOnLogin = builder
            .comment("Trigger an immediate snapshot when a player logs in.")
            .define("snapshot_on_login", true)
        snapshotOnLogout = builder
            .comment("Trigger an immediate snapshot when a player logs out.")
            .define("snapshot_on_logout", true)
        parallelPlayerLimit = builder
            .comment("How many players to snapshot in parallel within a single cycle.")
            .defineInRange("parallel_player_limit", 4, 1, 32)
        builder.pop()

        builder.comment("Database settings.").push("database")
        databasePath = builder
            .comment("Path to the SQLite database, relative to the world directory.")
            .define("path", "neocobblewatch.db")
        builder.pop()

        spec = builder.build()
    }
}
