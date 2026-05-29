package ak.neocobblewatch.player

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.getBool
import ak.neocobblewatch.persistence.getJson
import ak.neocobblewatch.persistence.getUuid
import ak.neocobblewatch.persistence.getUuidOrNull
import ak.neocobblewatch.persistence.queryLong
import ak.neocobblewatch.persistence.setBool
import ak.neocobblewatch.persistence.setJson
import ak.neocobblewatch.persistence.setUuid
import java.sql.Connection
import java.sql.ResultSet
import java.util.UUID

internal class PlayerRepository(private val db: Database) {
    suspend fun upsert(snapshot: PlayerSnapshot) = db.withConnection { conn -> upsert(conn, snapshot) }
    suspend fun get(uuid: UUID): PlayerSnapshot? = db.withConnection { conn -> get(conn, uuid) }

    fun upsert(conn: Connection, snapshot: PlayerSnapshot) {
        conn.prepareStatement(
            """
            INSERT INTO players (
                uuid, name, first_seen, last_seen, online,
                starter_prompted, starter_locked, starter_selected, starter_uuid,
                key_items, snapshot_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                name = excluded.name,
                last_seen = excluded.last_seen,
                online = excluded.online,
                starter_prompted = excluded.starter_prompted,
                starter_locked = excluded.starter_locked,
                starter_selected = excluded.starter_selected,
                starter_uuid = excluded.starter_uuid,
                key_items = excluded.key_items,
                snapshot_at = excluded.snapshot_at
            """.trimIndent(),
        ).use { stmt ->
            stmt.setUuid(1, snapshot.uuid)
            stmt.setString(2, snapshot.name)
            stmt.setLong(3, snapshot.firstSeen)
            stmt.setLong(4, snapshot.lastSeen)
            stmt.setBool(5, snapshot.online)
            stmt.setBool(6, snapshot.starterPrompted)
            stmt.setBool(7, snapshot.starterLocked)
            stmt.setBool(8, snapshot.starterSelected)
            stmt.setString(9, snapshot.starterUuid?.toString())
            stmt.setJson(10, snapshot.keyItems)
            stmt.setLong(11, snapshot.snapshotAt)
            stmt.executeUpdate()
        }
    }

    fun get(conn: Connection, uuid: UUID): PlayerSnapshot? {
        conn.prepareStatement("SELECT * FROM players WHERE uuid = ?").use { stmt ->
            stmt.setUuid(1, uuid)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) readRow(rs) else null
            }
        }
    }

    fun list(conn: Connection, limit: Int, offset: Int): List<PlayerSnapshot> {
        val results = mutableListOf<PlayerSnapshot>()
        conn.prepareStatement(
            "SELECT * FROM players ORDER BY name COLLATE NOCASE ASC LIMIT ? OFFSET ?",
        ).use { stmt ->
            stmt.setInt(1, limit)
            stmt.setInt(2, offset)
            stmt.executeQuery().use { rs ->
                while (rs.next()) results += readRow(rs)
            }
        }
        return results
    }

    fun count(conn: Connection): Long = conn.queryLong("SELECT COUNT(*) FROM players")

    fun listOnline(conn: Connection): List<PlayerSnapshot> {
        val results = mutableListOf<PlayerSnapshot>()
        conn.prepareStatement(
            "SELECT * FROM players WHERE online = 1 ORDER BY name COLLATE NOCASE ASC",
        ).use { stmt ->
            stmt.executeQuery().use { rs ->
                while (rs.next()) results += readRow(rs)
            }
        }
        return results
    }

    private fun readRow(rs: ResultSet): PlayerSnapshot = PlayerSnapshot(
        uuid = rs.getUuid("uuid"),
        name = rs.getString("name"),
        firstSeen = rs.getLong("first_seen"),
        lastSeen = rs.getLong("last_seen"),
        online = rs.getBool("online"),
        starterPrompted = rs.getBool("starter_prompted"),
        starterLocked = rs.getBool("starter_locked"),
        starterSelected = rs.getBool("starter_selected"),
        starterUuid = rs.getUuidOrNull("starter_uuid"),
        keyItems = rs.getJson("key_items"),
        snapshotAt = rs.getLong("snapshot_at"),
    )
}
