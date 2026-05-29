package ak.neocobblewatch.api.dto

import ak.neocobblewatch.stats.StatsSnapshot
import kotlinx.serialization.Serializable

@Serializable
internal data class StatsDto(
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
) {
    companion object {
        val EMPTY = StatsDto(
            totalCaptureCount = 0,
            totalShinyCaptureCount = 0,
            totalEggsCollected = 0,
            totalEggsHatched = 0,
            totalEvolvedCount = 0,
            totalBattleVictoryCount = 0,
            totalPvpBattleVictoryCount = 0,
            totalPvwBattleVictoryCount = 0,
            totalPvnBattleVictoryCount = 0,
            totalTradedCount = 0,
            typeCaptureCounts = emptyMap(),
            defeatedCounts = emptyMap(),
            aspectsCollected = emptyMap(),
            snapshotAt = 0L,
        )
    }
}

internal fun StatsSnapshot.toDto(): StatsDto = StatsDto(
    totalCaptureCount = totalCaptureCount,
    totalShinyCaptureCount = totalShinyCaptureCount,
    totalEggsCollected = totalEggsCollected,
    totalEggsHatched = totalEggsHatched,
    totalEvolvedCount = totalEvolvedCount,
    totalBattleVictoryCount = totalBattleVictoryCount,
    totalPvpBattleVictoryCount = totalPvpBattleVictoryCount,
    totalPvwBattleVictoryCount = totalPvwBattleVictoryCount,
    totalPvnBattleVictoryCount = totalPvnBattleVictoryCount,
    totalTradedCount = totalTradedCount,
    typeCaptureCounts = typeCaptureCounts,
    defeatedCounts = defeatedCounts,
    aspectsCollected = aspectsCollected,
    snapshotAt = snapshotAt,
)
