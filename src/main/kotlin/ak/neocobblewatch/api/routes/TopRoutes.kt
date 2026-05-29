package ak.neocobblewatch.api.routes

import ak.neocobblewatch.api.ApiContext
import ak.neocobblewatch.api.dto.PageInfo
import ak.neocobblewatch.api.dto.TopEntryDto
import ak.neocobblewatch.api.dto.TopResponse
import ak.neocobblewatch.api.parsePage
import ak.neocobblewatch.api.respondBadRequest
import ak.neocobblewatch.stats.TopMetric
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

internal fun Route.topRoutes(ctx: ApiContext) {
    route("/tops") {
        get("/{metric}") {
            val slug = call.parameters["metric"] ?: return@get call.respondBadRequest("BAD_METRIC", "Missing metric")
            val metric = TopMetric.fromSlug(slug)
                ?: return@get call.respondBadRequest(
                    "BAD_METRIC",
                    "Unknown metric '$slug'. Valid: ${TopMetric.SLUGS.joinToString()}",
                )
            val page = call.parsePage()
            val (rows, total) = ctx.database.withConnection { conn ->
                ctx.topsRepository.list(conn, metric, page.limit, page.offset) to ctx.topsRepository.count(conn)
            }
            call.respond(
                TopResponse(
                    metric = metric.slug,
                    data = rows.mapIndexed { idx, entry ->
                        TopEntryDto(
                            rank = page.offset + idx + 1,
                            uuid = entry.uuid.toString(),
                            name = entry.name,
                            value = entry.value,
                        )
                    },
                    page = PageInfo(page.limit, page.offset, total),
                ),
            )
        }
    }
}
