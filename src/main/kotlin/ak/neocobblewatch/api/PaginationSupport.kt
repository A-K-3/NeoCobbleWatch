package ak.neocobblewatch.api

import ak.neocobblewatch.api.dto.ApiError
import ak.neocobblewatch.api.dto.ApiErrorDetail
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import java.util.UUID

internal data class PageRequest(val limit: Int, val offset: Int)

internal fun ApplicationCall.parsePage(defaultLimit: Int = 50, maxLimit: Int = 200): PageRequest {
    val limit = request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, maxLimit) ?: defaultLimit
    val offset = request.queryParameters["offset"]?.toIntOrNull()?.coerceAtLeast(0) ?: 0
    return PageRequest(limit, offset)
}

internal fun ApplicationCall.uuidParam(name: String = "uuid"): UUID? {
    val raw = parameters[name] ?: return null
    return try {
        UUID.fromString(raw)
    } catch (e: IllegalArgumentException) {
        null
    }
}

internal suspend fun ApplicationCall.respondNotFound(code: String, message: String) {
    respond(HttpStatusCode.NotFound, ApiError(ApiErrorDetail(code, message)))
}

internal suspend fun ApplicationCall.respondBadRequest(code: String, message: String) {
    respond(HttpStatusCode.BadRequest, ApiError(ApiErrorDetail(code, message)))
}
