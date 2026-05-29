package ak.neocobblewatch.api.dto

import ak.neocobblewatch.stats.GlobalStatsSnapshot
import kotlinx.serialization.Serializable

@Serializable
internal data class ServerStatsDto(
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

@Serializable
internal data class ServerStatsResponse(val data: ServerStatsDto)

@Serializable
internal data class OnlinePlayerDto(val uuid: String, val name: String)

@Serializable
internal data class OnlinePlayersResponse(val data: List<OnlinePlayerDto>)

internal fun GlobalStatsSnapshot.toDto(): ServerStatsDto = ServerStatsDto(
    totalPlayers = totalPlayers,
    onlinePlayers = onlinePlayers,
    totalCaptureCount = totalCaptureCount,
    totalShinyCaptureCount = totalShinyCaptureCount,
    totalEggsHatched = totalEggsHatched,
    totalEvolvedCount = totalEvolvedCount,
    totalBattleVictoryCount = totalBattleVictoryCount,
    totalPvpBattleVictoryCount = totalPvpBattleVictoryCount,
    totalTradedCount = totalTradedCount,
)
