package ak.neocobblewatch.api.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PageInfo(
    val limit: Int,
    val offset: Int,
    val total: Long,
)
