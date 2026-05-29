package ak.neocobblewatch.api.routes

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.api.dto.HealthDto
import ak.neocobblewatch.api.dto.HealthResponse
import ak.neocobblewatch.snapshot.SnapshotScheduler
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.healthRoutes(scheduler: SnapshotScheduler) {
    get("/health") {
        val now = System.currentTimeMillis()
        val lastCycle = scheduler.lastFullCycleAt
        call.respond(
            HealthResponse(
                data = HealthDto(
                    status = "ok",
                    version = Neocobblewatch.VERSION,
                    lastFullCycleAt = lastCycle.takeIf { it > 0 },
                    lastFullCycleAgeSeconds = lastCycle.takeIf { it > 0 }?.let { (now - it) / 1000 },
                ),
            ),
        )
    }
}
