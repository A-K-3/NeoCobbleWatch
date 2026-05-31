package ak.neocobblewatch.api

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.api.dto.ApiError
import ak.neocobblewatch.api.dto.ApiErrorDetail
import ak.neocobblewatch.api.routes.healthRoutes
import ak.neocobblewatch.api.routes.playerRoutes
import ak.neocobblewatch.api.routes.serverRoutes
import ak.neocobblewatch.api.routes.speciesRoutes
import ak.neocobblewatch.api.routes.topRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
internal val ApiJson: Json = Json {
    namingStrategy = JsonNamingStrategy.SnakeCase
    encodeDefaults = true
    ignoreUnknownKeys = true
}

internal fun Application.installRouting(ctx: ApiContext) {
    install(ContentNegotiation) { json(ApiJson) }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            Neocobblewatch.LOGGER.error("Unhandled exception during HTTP request", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(ApiErrorDetail("INTERNAL", "Internal server error")),
            )
        }
    }
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
    }
    routing {
        route("/api/v1") {
            healthRoutes(ctx.scheduler)
            playerRoutes(ctx)
            serverRoutes(ctx)
            speciesRoutes(ctx)
            topRoutes(ctx)
        }
    }
}
