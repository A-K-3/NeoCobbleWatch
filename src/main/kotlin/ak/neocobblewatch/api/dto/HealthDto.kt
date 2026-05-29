package ak.neocobblewatch.api.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class HealthDto(
    val status: String,
    val version: String,
    val lastFullCycleAt: Long?,
    val lastFullCycleAgeSeconds: Long?,
)

@Serializable
internal data class HealthResponse(val data: HealthDto)
