package ak.neocobblewatch.api.routes

import ak.neocobblewatch.api.ApiContext
import ak.neocobblewatch.api.dto.OnlinePlayerDto
import ak.neocobblewatch.api.dto.OnlinePlayersResponse
import ak.neocobblewatch.api.dto.ServerStatsResponse
import ak.neocobblewatch.api.dto.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

internal fun Route.serverRoutes(ctx: ApiContext) {
    route("/server") {
        get("/online") {
            val players = ctx.database.withConnection { conn -> ctx.playerRepository.listOnline(conn) }
            call.respond(
                OnlinePlayersResponse(
                    data = players.map { OnlinePlayerDto(uuid = it.uuid.toString(), name = it.name) },
                ),
            )
        }
        get("/stats") {
            val global = ctx.database.withConnection { conn -> ctx.statsRepository.aggregateGlobal(conn) }
            call.respond(ServerStatsResponse(global.toDto()))
        }
    }
}
