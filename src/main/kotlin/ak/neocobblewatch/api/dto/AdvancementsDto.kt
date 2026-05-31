package ak.neocobblewatch.api.dto

import ak.neocobblewatch.advancements.AdvancementsSnapshot
import kotlinx.serialization.Serializable

@Serializable
internal data class AdvancementsDto(
    val completed: List<String>,
    val snapshotAt: Long,
) {
    companion object {
        val EMPTY = AdvancementsDto(completed = emptyList(), snapshotAt = 0L)
    }
}

@Serializable
internal data class AdvancementsResponse(val data: AdvancementsDto)

internal fun AdvancementsSnapshot.toDto() = AdvancementsDto(completed = completed, snapshotAt = snapshotAt)
