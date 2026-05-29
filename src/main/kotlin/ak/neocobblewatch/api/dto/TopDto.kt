package ak.neocobblewatch.api.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class TopEntryDto(
    val rank: Int,
    val uuid: String,
    val name: String,
    val value: Long,
)

@Serializable
internal data class TopResponse(
    val metric: String,
    val data: List<TopEntryDto>,
    val page: PageInfo,
)
