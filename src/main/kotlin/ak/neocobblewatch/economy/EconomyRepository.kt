package ak.neocobblewatch.economy

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.getUuid
import ak.neocobblewatch.persistence.setUuid
import java.sql.Connection
import java.util.UUID

internal class EconomyRepository(private val db: Database) {
    suspend fun upsert(snapshot: EconomySnapshot) = db.withConnection { conn -> upsert(conn, snapshot) }
    suspend fun get(playerUuid: UUID): EconomySnapshot? = db.withConnection { conn -> get(conn, playerUuid) }

    fun upsert(conn: Connection, s: EconomySnapshot) {
        conn.prepareStatement(
            """
            INSERT INTO player_economy (player_uuid, cobble_dollars, snapshot_at)
            VALUES (?, ?, ?)
            ON CONFLICT(player_uuid) DO UPDATE SET
                cobble_dollars = excluded.cobble_dollars,
                snapshot_at = excluded.snapshot_at
            """.trimIndent(),
        ).use { stmt ->
            stmt.setUuid(1, s.playerUuid)
            stmt.setLong(2, s.cobbleDollars)
            stmt.setLong(3, s.snapshotAt)
            stmt.executeUpdate()
        }
    }

    fun get(conn: Connection, playerUuid: UUID): EconomySnapshot? {
        conn.prepareStatement("SELECT * FROM player_economy WHERE player_uuid = ?").use { stmt ->
            stmt.setUuid(1, playerUuid)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) return null
                return EconomySnapshot(
                    playerUuid = rs.getUuid("player_uuid"),
                    cobbleDollars = rs.getLong("cobble_dollars"),
                    snapshotAt = rs.getLong("snapshot_at"),
                )
            }
        }
    }
}
