package ak.neocobblewatch.advancements

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.getJson
import ak.neocobblewatch.persistence.setJson
import ak.neocobblewatch.persistence.setUuid
import ak.neocobblewatch.persistence.getUuid
import java.sql.Connection
import java.util.UUID

internal class AdvancementsRepository(private val db: Database) {
    suspend fun upsert(snapshot: AdvancementsSnapshot) = db.withConnection { conn -> upsert(conn, snapshot) }
    suspend fun get(playerUuid: UUID): AdvancementsSnapshot? = db.withConnection { conn -> get(conn, playerUuid) }

    fun upsert(conn: Connection, s: AdvancementsSnapshot) {
        conn.prepareStatement(
            """
            INSERT INTO player_advancements (player_uuid, completed, snapshot_at)
            VALUES (?, ?, ?)
            ON CONFLICT(player_uuid) DO UPDATE SET
                completed = excluded.completed,
                snapshot_at = excluded.snapshot_at
            """.trimIndent(),
        ).use { stmt ->
            stmt.setUuid(1, s.playerUuid)
            stmt.setJson(2, s.completed)
            stmt.setLong(3, s.snapshotAt)
            stmt.executeUpdate()
        }
    }

    fun get(conn: Connection, playerUuid: UUID): AdvancementsSnapshot? {
        conn.prepareStatement("SELECT * FROM player_advancements WHERE player_uuid = ?").use { stmt ->
            stmt.setUuid(1, playerUuid)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) return null
                return AdvancementsSnapshot(
                    playerUuid = rs.getUuid("player_uuid"),
                    completed = rs.getJson("completed"),
                    snapshotAt = rs.getLong("snapshot_at"),
                )
            }
        }
    }
}
