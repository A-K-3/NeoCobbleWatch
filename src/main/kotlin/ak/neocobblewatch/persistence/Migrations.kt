package ak.neocobblewatch.persistence

import ak.neocobblewatch.Neocobblewatch
import java.sql.Connection

internal object Migrations {
    private data class Migration(val version: Int, val statements: List<String>)

    private val V1 = Migration(
        version = 1,
        statements = listOf(
            """
            CREATE TABLE players (
                uuid TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                first_seen INTEGER NOT NULL,
                last_seen INTEGER NOT NULL,
                online INTEGER NOT NULL DEFAULT 0,
                snapshot_at INTEGER NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE player_stats (
                player_uuid TEXT PRIMARY KEY,
                total_capture_count INTEGER NOT NULL DEFAULT 0,
                total_shiny_capture_count INTEGER NOT NULL DEFAULT 0,
                total_eggs_collected INTEGER NOT NULL DEFAULT 0,
                total_eggs_hatched INTEGER NOT NULL DEFAULT 0,
                total_evolved_count INTEGER NOT NULL DEFAULT 0,
                total_battle_victory_count INTEGER NOT NULL DEFAULT 0,
                total_pvp_battle_victory_count INTEGER NOT NULL DEFAULT 0,
                total_pvw_battle_victory_count INTEGER NOT NULL DEFAULT 0,
                total_pvn_battle_victory_count INTEGER NOT NULL DEFAULT 0,
                total_traded_count INTEGER NOT NULL DEFAULT 0,
                type_capture_counts TEXT NOT NULL DEFAULT '{}',
                defeated_counts TEXT NOT NULL DEFAULT '{}',
                snapshot_at INTEGER NOT NULL,
                FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE
            )
            """.trimIndent(),
            """
            CREATE TABLE pokedex_entries (
                player_uuid TEXT NOT NULL,
                species_id TEXT NOT NULL,
                knowledge TEXT NOT NULL CHECK (knowledge IN ('NONE','ENCOUNTERED','CAUGHT')),
                forms_encountered TEXT NOT NULL DEFAULT '[]',
                forms_caught TEXT NOT NULL DEFAULT '[]',
                shiny_states TEXT NOT NULL DEFAULT '[]',
                genders_seen TEXT NOT NULL DEFAULT '[]',
                aspects_seen TEXT NOT NULL DEFAULT '[]',
                snapshot_at INTEGER NOT NULL,
                PRIMARY KEY (player_uuid, species_id),
                FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE
            )
            """.trimIndent(),
            "CREATE INDEX idx_pokedex_player ON pokedex_entries(player_uuid)",
            "CREATE INDEX idx_pokedex_species ON pokedex_entries(species_id)",
            """
            CREATE TABLE party_slots (
                player_uuid TEXT NOT NULL,
                slot_index INTEGER NOT NULL,
                pokemon_uuid TEXT NOT NULL,
                species_id TEXT NOT NULL,
                form_name TEXT NOT NULL,
                nickname TEXT,
                level INTEGER NOT NULL,
                shiny INTEGER NOT NULL,
                gender TEXT NOT NULL,
                nature TEXT NOT NULL,
                ability TEXT NOT NULL,
                ball TEXT NOT NULL,
                tera_type TEXT,
                held_item TEXT,
                ot_name TEXT,
                friendship INTEGER NOT NULL,
                ivs TEXT NOT NULL,
                evs TEXT NOT NULL,
                moves TEXT NOT NULL,
                aspects TEXT NOT NULL,
                snapshot_at INTEGER NOT NULL,
                PRIMARY KEY (player_uuid, slot_index),
                FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE
            )
            """.trimIndent(),
            """
            CREATE TABLE pc_slots (
                player_uuid TEXT NOT NULL,
                box_index INTEGER NOT NULL,
                slot_index INTEGER NOT NULL,
                pokemon_uuid TEXT NOT NULL,
                species_id TEXT NOT NULL,
                form_name TEXT NOT NULL,
                nickname TEXT,
                level INTEGER NOT NULL,
                shiny INTEGER NOT NULL,
                gender TEXT NOT NULL,
                nature TEXT NOT NULL,
                ability TEXT NOT NULL,
                ball TEXT NOT NULL,
                tera_type TEXT,
                held_item TEXT,
                ot_name TEXT,
                friendship INTEGER NOT NULL,
                ivs TEXT NOT NULL,
                evs TEXT NOT NULL,
                moves TEXT NOT NULL,
                aspects TEXT NOT NULL,
                snapshot_at INTEGER NOT NULL,
                PRIMARY KEY (player_uuid, box_index, slot_index),
                FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE
            )
            """.trimIndent(),
            "CREATE INDEX idx_pc_player ON pc_slots(player_uuid)",
        ),
    )

    private val all: List<Migration> = listOf(V1)

    fun applyAll(conn: Connection) {
        ensureSchemaVersionTable(conn)
        val current = currentVersion(conn)
        val pending = all.filter { it.version > current }
        if (pending.isEmpty()) {
            Neocobblewatch.LOGGER.info("Schema up to date at v{}", current)
            return
        }
        pending.forEach { apply(conn, it) }
    }

    private fun ensureSchemaVersionTable(conn: Connection) {
        conn.createStatement().use { stmt ->
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS schema_version (" +
                    "version INTEGER PRIMARY KEY," +
                    "applied_at INTEGER NOT NULL)",
            )
        }
    }

    private fun currentVersion(conn: Connection): Int {
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT COALESCE(MAX(version), 0) FROM schema_version").use { rs ->
                return if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    private fun apply(conn: Connection, migration: Migration) {
        Neocobblewatch.LOGGER.info("Applying migration v{}", migration.version)
        conn.autoCommit = false
        try {
            conn.createStatement().use { stmt -> migration.statements.forEach { stmt.execute(it) } }
            conn.prepareStatement("INSERT INTO schema_version (version, applied_at) VALUES (?, ?)").use {
                it.setInt(1, migration.version)
                it.setLong(2, System.currentTimeMillis())
                it.executeUpdate()
            }
            conn.commit()
            Neocobblewatch.LOGGER.info("Migration v{} applied", migration.version)
        } catch (e: Exception) {
            conn.rollback()
            throw e
        } finally {
            conn.autoCommit = true
        }
    }
}
