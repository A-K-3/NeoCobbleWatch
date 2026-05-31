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
                starter_prompted INTEGER NOT NULL DEFAULT 0,
                starter_locked INTEGER NOT NULL DEFAULT 0,
                starter_selected INTEGER NOT NULL DEFAULT 0,
                starter_uuid TEXT,
                key_items TEXT NOT NULL DEFAULT '[]',
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
                aspects_collected TEXT NOT NULL DEFAULT '{}',
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

    private val V2 = Migration(
        version = 2,
        statements = listOf(
            """
            CREATE TABLE mod_state (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent(),
        ),
    )

    private val V3 = Migration(
        version = 3,
        statements = listOf(
            """
            CREATE TABLE player_activity (
                player_uuid TEXT PRIMARY KEY,
                play_time_ticks INTEGER NOT NULL DEFAULT 0,
                deaths INTEGER NOT NULL DEFAULT 0,
                mob_kills INTEGER NOT NULL DEFAULT 0,
                fish_caught INTEGER NOT NULL DEFAULT 0,
                damage_dealt INTEGER NOT NULL DEFAULT 0,
                damage_taken INTEGER NOT NULL DEFAULT 0,
                damage_blocked INTEGER NOT NULL DEFAULT 0,
                walk_cm INTEGER NOT NULL DEFAULT 0,
                sprint_cm INTEGER NOT NULL DEFAULT 0,
                fly_cm INTEGER NOT NULL DEFAULT 0,
                swim_cm INTEGER NOT NULL DEFAULT 0,
                jumps INTEGER NOT NULL DEFAULT 0,
                sleep_in_bed INTEGER NOT NULL DEFAULT 0,
                traded_with_villager INTEGER NOT NULL DEFAULT 0,
                level_ups INTEGER NOT NULL DEFAULT 0,
                evolutions INTEGER NOT NULL DEFAULT 0,
                times_ridden INTEGER NOT NULL DEFAULT 0,
                riding_land_cm INTEGER NOT NULL DEFAULT 0,
                riding_air_cm INTEGER NOT NULL DEFAULT 0,
                riding_liquid_cm INTEGER NOT NULL DEFAULT 0,
                eggs_collected INTEGER NOT NULL DEFAULT 0,
                eggs_hatched INTEGER NOT NULL DEFAULT 0,
                rod_casts INTEGER NOT NULL DEFAULT 0,
                reel_ins INTEGER NOT NULL DEFAULT 0,
                released INTEGER NOT NULL DEFAULT 0,
                raids_hosted INTEGER NOT NULL DEFAULT 0,
                raids_joined INTEGER NOT NULL DEFAULT 0,
                raids_completed INTEGER NOT NULL DEFAULT 0,
                raid_tier1 INTEGER NOT NULL DEFAULT 0,
                raid_tier2 INTEGER NOT NULL DEFAULT 0,
                raid_tier3 INTEGER NOT NULL DEFAULT 0,
                raid_tier4 INTEGER NOT NULL DEFAULT 0,
                raid_tier5 INTEGER NOT NULL DEFAULT 0,
                badges TEXT NOT NULL DEFAULT '[]',
                snapshot_at INTEGER NOT NULL,
                FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE
            )
            """.trimIndent(),
        ),
    )

    private val V4 = Migration(
        version = 4,
        statements = listOf(
            "ALTER TABLE player_activity ADD COLUMN blocks_mined INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE player_activity ADD COLUMN items_used INTEGER NOT NULL DEFAULT 0",
        ),
    )

    private val V5 = Migration(
        version = 5,
        statements = listOf(
            "ALTER TABLE player_activity ADD COLUMN items_crafted INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE player_activity ADD COLUMN items_broken INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE player_activity ADD COLUMN kills_map TEXT NOT NULL DEFAULT '{}'",
            "ALTER TABLE player_activity ADD COLUMN killed_by_map TEXT NOT NULL DEFAULT '{}'",
            """
            CREATE TABLE player_advancements (
                player_uuid TEXT PRIMARY KEY,
                completed TEXT NOT NULL DEFAULT '[]',
                snapshot_at INTEGER NOT NULL,
                FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE
            )
            """.trimIndent(),
        ),
    )

    private val V6 = Migration(
        version = 6,
        statements = listOf(
            """
            CREATE TABLE player_economy (
                player_uuid TEXT PRIMARY KEY,
                cobble_dollars INTEGER NOT NULL DEFAULT 0,
                snapshot_at INTEGER NOT NULL,
                FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE
            )
            """.trimIndent(),
        ),
    )

    private val all: List<Migration> = listOf(V1, V2, V3, V4, V5, V6)

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
