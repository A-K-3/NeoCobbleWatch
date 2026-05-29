package ak.neocobblewatch.api.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ApiError(val error: ApiErrorDetail)

@Serializable
internal data class ApiErrorDetail(val code: String, val message: String)
