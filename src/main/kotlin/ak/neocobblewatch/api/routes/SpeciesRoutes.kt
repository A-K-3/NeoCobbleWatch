package ak.neocobblewatch.api.routes

import ak.neocobblewatch.api.ApiContext
import ak.neocobblewatch.api.dto.ApiError
import ak.neocobblewatch.api.dto.ApiErrorDetail
import ak.neocobblewatch.api.dto.SpeciesDetailResponse
import ak.neocobblewatch.species.SpeciesDetailReader
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import net.minecraft.resources.ResourceLocation

internal fun Route.speciesRoutes(ctx: ApiContext) {
    route("/species") {
        get("/labels") {
            call.respond(ctx.speciesLabelsCache.getResponse())
        }
        get("/{namespace}/{path}") {
            val ns = call.parameters["namespace"].orEmpty()
            val path = call.parameters["path"].orEmpty()
            val rl = runCatching { ResourceLocation.fromNamespaceAndPath(ns, path) }.getOrNull()
            if (rl == null) {
                call.respond(HttpStatusCode.BadRequest, ApiError(ApiErrorDetail("INVALID_ID", "Invalid species id")))
                return@get
            }
            val detail = SpeciesDetailReader.read(rl, ctx.speciesLabelsCache)
            if (detail == null) {
                call.respond(HttpStatusCode.NotFound, ApiError(ApiErrorDetail("NOT_FOUND", "Species not found")))
                return@get
            }
            call.respond(SpeciesDetailResponse(data = detail))
        }
    }
}
