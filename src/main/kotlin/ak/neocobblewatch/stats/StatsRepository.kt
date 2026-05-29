package ak.neocobblewatch.stats

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.getJson
import ak.neocobblewatch.persistence.setJson
import ak.neocobblewatch.persistence.setUuid
import java.sql.Connection
import java.util.UUID

internal class StatsRepository(private val db: Database) {
    suspend fun upsert(snapshot: StatsSnapshot) = db.withConnection { conn -> upsert(conn, snapshot) }
    suspend fun get(playerUuid: UUID): StatsSnapshot? = db.withConnection { conn -> get(conn, playerUuid) }

    fun upsert(conn: Connection, snapshot: StatsSnapshot) {
        conn.prepareStatement(
            """
            INSERT INTO player_stats (
                player_uuid, total_capture_count, total_shiny_capture_count,
                total_eggs_collected, total_eggs_hatched, total_evolved_count,
                total_battle_victory_count, total_pvp_battle_victory_count,
                total_pvw_battle_victory_count, total_pvn_battle_victory_count,
                total_traded_count, type_capture_counts, defeated_counts, snapshot_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(player_uuid) DO UPDATE SET
                total_capture_count = excluded.total_capture_count,
                total_shiny_capture_count = excluded.total_shiny_capture_count,
                total_eggs_collected = excluded.total_eggs_collected,
                total_eggs_hatched = excluded.total_eggs_hatched,
                total_evolved_count = excluded.total_evolved_count,
                total_battle_victory_count = excluded.total_battle_victory_count,
                total_pvp_battle_victory_count = excluded.total_pvp_battle_victory_count,
                total_pvw_battle_victory_count = excluded.total_pvw_battle_victory_count,
                total_pvn_battle_victory_count = excluded.total_pvn_battle_victory_count,
                total_traded_count = excluded.total_traded_count,
                type_capture_counts = excluded.type_capture_counts,
                defeated_counts = excluded.defeated_counts,
                snapshot_at = excluded.snapshot_at
            """.trimIndent(),
        ).use { stmt ->
            stmt.setUuid(1, snapshot.playerUuid)
            stmt.setInt(2, snapshot.totalCaptureCount)
            stmt.setInt(3, snapshot.totalShinyCaptureCount)
            stmt.setInt(4, snapshot.totalEggsCollected)
            stmt.setInt(5, snapshot.totalEggsHatched)
            stmt.setInt(6, snapshot.totalEvolvedCount)
            stmt.setInt(7, snapshot.totalBattleVictoryCount)
            stmt.setInt(8, snapshot.totalPvpBattleVictoryCount)
            stmt.setInt(9, snapshot.totalPvwBattleVictoryCount)
            stmt.setInt(10, snapshot.totalPvnBattleVictoryCount)
            stmt.setInt(11, snapshot.totalTradedCount)
            stmt.setJson(12, snapshot.typeCaptureCounts)
            stmt.setJson(13, snapshot.defeatedCounts)
            stmt.setLong(14, snapshot.snapshotAt)
            stmt.executeUpdate()
        }
    }

    fun get(conn: Connection, playerUuid: UUID): StatsSnapshot? {
        conn.prepareStatement("SELECT * FROM player_stats WHERE player_uuid = ?").use { stmt ->
            stmt.setUuid(1, playerUuid)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) return null
                return StatsSnapshot(
                    playerUuid = playerUuid,
                    totalCaptureCount = rs.getInt("total_capture_count"),
                    totalShinyCaptureCount = rs.getInt("total_shiny_capture_count"),
                    totalEggsCollected = rs.getInt("total_eggs_collected"),
                    totalEggsHatched = rs.getInt("total_eggs_hatched"),
                    totalEvolvedCount = rs.getInt("total_evolved_count"),
                    totalBattleVictoryCount = rs.getInt("total_battle_victory_count"),
                    totalPvpBattleVictoryCount = rs.getInt("total_pvp_battle_victory_count"),
                    totalPvwBattleVictoryCount = rs.getInt("total_pvw_battle_victory_count"),
                    totalPvnBattleVictoryCount = rs.getInt("total_pvn_battle_victory_count"),
                    totalTradedCount = rs.getInt("total_traded_count"),
                    typeCaptureCounts = rs.getJson("type_capture_counts"),
                    defeatedCounts = rs.getJson("defeated_counts"),
                    snapshotAt = rs.getLong("snapshot_at"),
                )
            }
        }
    }
}
