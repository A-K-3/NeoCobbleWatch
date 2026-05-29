package ak.neocobblewatch.stats

internal data class GlobalStatsSnapshot(
    val totalPlayers: Long,
    val onlinePlayers: Long,
    val totalCaptureCount: Long,
    val totalShinyCaptureCount: Long,
    val totalEggsHatched: Long,
    val totalEvolvedCount: Long,
    val totalBattleVictoryCount: Long,
    val totalPvpBattleVictoryCount: Long,
    val totalTradedCount: Long,
)
