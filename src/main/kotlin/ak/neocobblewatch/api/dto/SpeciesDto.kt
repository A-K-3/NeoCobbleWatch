package ak.neocobblewatch.api.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class SpeciesInfoDto(
    val nationalDex: Int,
    val generation: Int,
    val labels: List<String>,
    val fullyEvolved: Boolean,
    val primaryType: String,
    val secondaryType: String?,
)

@Serializable
internal data class SpeciesLabelsResponse(
    val data: Map<String, SpeciesInfoDto>,
    val snapshotAt: Long,
)
