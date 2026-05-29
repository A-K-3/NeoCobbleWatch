package ak.neocobblewatch.player

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.getBool
import ak.neocobblewatch.persistence.getUuid
import ak.neocobblewatch.persistence.setBool
import ak.neocobblewatch.persistence.setUuid
import java.sql.Connection
import java.util.UUID

internal class PlayerRepository(private val db: Database) {
    suspend fun upsert(snapshot: PlayerSnapshot) = db.withConnection { conn -> upsert(conn, snapshot) }
    suspend fun get(uuid: UUID): PlayerSnapshot? = db.withConnection { conn -> get(conn, uuid) }

    fun upsert(conn: Connection, snapshot: PlayerSnapshot) {
        conn.prepareStatement(
            """
            INSERT INTO players (uuid, name, first_seen, last_seen, online, snapshot_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                name = excluded.name,
                last_seen = excluded.last_seen,
                online = excluded.online,
                snapshot_at = excluded.snapshot_at
            """.trimIndent(),
        ).use { stmt ->
            stmt.setUuid(1, snapshot.uuid)
            stmt.setString(2, snapshot.name)
            stmt.setLong(3, snapshot.firstSeen)
            stmt.setLong(4, snapshot.lastSeen)
            stmt.setBool(5, snapshot.online)
            stmt.setLong(6, snapshot.snapshotAt)
            stmt.executeUpdate()
        }
    }

    fun get(conn: Connection, uuid: UUID): PlayerSnapshot? {
        conn.prepareStatement(
            "SELECT uuid, name, first_seen, last_seen, online, snapshot_at FROM players WHERE uuid = ?",
        ).use { stmt ->
            stmt.setUuid(1, uuid)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) return null
                return PlayerSnapshot(
                    uuid = rs.getUuid("uuid"),
                    name = rs.getString("name"),
                    firstSeen = rs.getLong("first_seen"),
                    lastSeen = rs.getLong("last_seen"),
                    online = rs.getBool("online"),
                    snapshotAt = rs.getLong("snapshot_at"),
                )
            }
        }
    }
}
