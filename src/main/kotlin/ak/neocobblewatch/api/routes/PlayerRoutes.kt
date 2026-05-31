package ak.neocobblewatch.api.routes

import ak.neocobblewatch.api.ApiContext
import ak.neocobblewatch.api.dto.ActivityDto
import ak.neocobblewatch.api.dto.ActivityResponse
import ak.neocobblewatch.api.dto.AdvancementsDto
import ak.neocobblewatch.api.dto.AdvancementsResponse
import ak.neocobblewatch.api.dto.EconomyDto
import ak.neocobblewatch.api.dto.EconomyResponse
import ak.neocobblewatch.api.dto.toDto
import ak.neocobblewatch.api.dto.PageInfo
import ak.neocobblewatch.api.dto.PartyDto
import ak.neocobblewatch.api.dto.PartyResponse
import ak.neocobblewatch.api.dto.PcResponse
import ak.neocobblewatch.api.dto.PlayerListResponse
import ak.neocobblewatch.api.dto.PlayerProfileDto
import ak.neocobblewatch.api.dto.PlayerProfileResponse
import ak.neocobblewatch.api.dto.PokedexDto
import ak.neocobblewatch.api.dto.PokedexResponse
import ak.neocobblewatch.api.dto.StatsDto
import ak.neocobblewatch.api.dto.toSummaryDto
import ak.neocobblewatch.api.parsePage
import ak.neocobblewatch.api.respondBadRequest
import ak.neocobblewatch.api.respondNotFound
import ak.neocobblewatch.api.uuidParam
import ak.neocobblewatch.pc.PcSlotSnapshot
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.route

internal fun Route.playerRoutes(ctx: ApiContext) {
    route("/players") {
        get { listPlayers(ctx) }
        route("/{uuid}") {
            get { playerProfile(ctx) }
            get("/pokedex") { playerPokedex(ctx) }
            get("/party") { playerParty(ctx) }
            get("/pc") { playerPc(ctx) }
            get("/activity") { playerActivity(ctx) }
            get("/advancements") { playerAdvancements(ctx) }
            get("/economy") { playerEconomy(ctx) }
        }
    }
}

private suspend fun RoutingContext.listPlayers(ctx: ApiContext) {
    val page = call.parsePage()
    val (rows, total) = ctx.database.withConnection { conn ->
        ctx.playerRepository.list(conn, page.limit, page.offset) to ctx.playerRepository.count(conn)
    }
    call.respond(
        PlayerListResponse(
            data = rows.map { it.toSummaryDto() },
            page = PageInfo(page.limit, page.offset, total),
        ),
    )
}

private suspend fun RoutingContext.playerProfile(ctx: ApiContext) {
    val uuid = call.uuidParam() ?: return call.respondBadRequest("BAD_UUID", "Invalid UUID")
    val pair = ctx.database.withConnection { conn ->
        ctx.playerRepository.get(conn, uuid) to ctx.statsRepository.get(conn, uuid)
    }
    val player = pair.first ?: return call.respondNotFound("PLAYER_NOT_FOUND", "No snapshot for player $uuid")
    val stats = pair.second?.toDto() ?: StatsDto.EMPTY
    call.respond(PlayerProfileResponse(PlayerProfileDto(player.toDto(), stats)))
}

private suspend fun RoutingContext.playerPokedex(ctx: ApiContext) {
    val uuid = call.uuidParam() ?: return call.respondBadRequest("BAD_UUID", "Invalid UUID")
    val pokedex = ctx.pokedexRepository.get(uuid)
        ?: return call.respond(PokedexResponse(PokedexDto(emptyList(), 0L)))
    call.respond(PokedexResponse(pokedex.toDto()))
}

private suspend fun RoutingContext.playerParty(ctx: ApiContext) {
    val uuid = call.uuidParam() ?: return call.respondBadRequest("BAD_UUID", "Invalid UUID")
    val party = ctx.partyRepository.get(uuid)
        ?: return call.respond(PartyResponse(PartyDto(emptyList(), 0L)))
    call.respond(PartyResponse(party.toDto()))
}

private suspend fun RoutingContext.playerActivity(ctx: ApiContext) {
    val uuid = call.uuidParam() ?: return call.respondBadRequest("BAD_UUID", "Invalid UUID")
    val activity = ctx.activityRepository.get(uuid)?.toDto() ?: ActivityDto.EMPTY
    call.respond(ActivityResponse(activity))
}

private suspend fun RoutingContext.playerEconomy(ctx: ApiContext) {
    val uuid = call.uuidParam() ?: return call.respondBadRequest("BAD_UUID", "Invalid UUID")
    val economy = ctx.economyRepository.get(uuid)?.toDto() ?: EconomyDto.EMPTY
    call.respond(EconomyResponse(economy))
}

private suspend fun RoutingContext.playerAdvancements(ctx: ApiContext) {
    val uuid = call.uuidParam() ?: return call.respondBadRequest("BAD_UUID", "Invalid UUID")
    val advancements = ctx.advancementsRepository.get(uuid)?.toDto() ?: AdvancementsDto.EMPTY
    call.respond(AdvancementsResponse(advancements))
}

private suspend fun RoutingContext.playerPc(ctx: ApiContext) {
    val uuid = call.uuidParam() ?: return call.respondBadRequest("BAD_UUID", "Invalid UUID")
    val box = call.request.queryParameters["box"]?.toIntOrNull()
    val (slots, pageInfo) = when {
        box != null -> {
            val all = ctx.database.withConnection { conn -> ctx.pcRepository.listByBox(conn, uuid, box) }
            all to PageInfo(limit = all.size, offset = 0, total = all.size.toLong())
        }
        else -> {
            val page = call.parsePage()
            val (rows, total) = ctx.database.withConnection { conn ->
                ctx.pcRepository.listPaginated(conn, uuid, page.limit, page.offset) to ctx.pcRepository.count(conn, uuid)
            }
            rows to PageInfo(page.limit, page.offset, total)
        }
    }
    call.respond(PcResponse(slots.map(PcSlotSnapshot::toDto), pageInfo))
}
