package ak.neocobblewatch.persistence

import java.sql.Connection

/**
 * Key/value table for mod-level state outside the player-domain schema:
 * first-run flags, last-job timestamps, etc. One row per key.
 */
internal class ModStateRepository(private val db: Database) {
    suspend fun get(key: String): String? = db.withConnection { conn -> get(conn, key) }
    suspend fun set(key: String, value: String) = db.withConnection { conn -> set(conn, key, value) }

    fun get(conn: Connection, key: String): String? {
        conn.prepareStatement("SELECT value FROM mod_state WHERE key = ?").use { stmt ->
            stmt.setString(1, key)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) rs.getString("value") else null
            }
        }
    }

    fun set(conn: Connection, key: String, value: String) {
        conn.prepareStatement(
            """
            INSERT INTO mod_state (key, value, updated_at) VALUES (?, ?, ?)
            ON CONFLICT(key) DO UPDATE SET value = excluded.value, updated_at = excluded.updated_at
            """.trimIndent(),
        ).use { stmt ->
            stmt.setString(1, key)
            stmt.setString(2, value)
            stmt.setLong(3, System.currentTimeMillis())
            stmt.executeUpdate()
        }
    }

    companion object Keys {
        const val BACKFILL_COMPLETED_AT = "backfill_completed_at"
    }
}
