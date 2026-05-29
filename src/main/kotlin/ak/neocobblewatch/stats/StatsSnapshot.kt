package ak.neocobblewatch.stats

import java.util.UUID

internal data class StatsSnapshot(
    val playerUuid: UUID,
    val totalCaptureCount: Int,
    val totalShinyCaptureCount: Int,
    val totalEggsCollected: Int,
    val totalEggsHatched: Int,
    val totalEvolvedCount: Int,
    val totalBattleVictoryCount: Int,
    val totalPvpBattleVictoryCount: Int,
    val totalPvwBattleVictoryCount: Int,
    val totalPvnBattleVictoryCount: Int,
    val totalTradedCount: Int,
    val typeCaptureCounts: Map<String, Int>,
    val defeatedCounts: Map<String, Int>,
    val aspectsCollected: Map<String, Set<String>>,
    val snapshotAt: Long,
)
