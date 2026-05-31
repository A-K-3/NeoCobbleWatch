package ak.neocobblewatch.api.dto

import ak.neocobblewatch.economy.EconomySnapshot
import kotlinx.serialization.Serializable

@Serializable
internal data class EconomyDto(
    val cobbleDollars: Long,
    val snapshotAt: Long,
) {
    companion object {
        val EMPTY = EconomyDto(cobbleDollars = 0L, snapshotAt = 0L)
    }
}

@Serializable
internal data class EconomyResponse(val data: EconomyDto)

internal fun EconomySnapshot.toDto() = EconomyDto(cobbleDollars = cobbleDollars, snapshotAt = snapshotAt)
