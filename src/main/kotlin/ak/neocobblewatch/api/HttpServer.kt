package ak.neocobblewatch.api

import ak.neocobblewatch.Neocobblewatch
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer

internal class HttpServer(private val ctx: ApiContext) {
    private var server: EmbeddedServer<*, *>? = null

    fun start() {
        check(server == null) { "HttpServer already started" }
        val instance = embeddedServer(CIO, port = ctx.httpConfig.port, host = ctx.httpConfig.bind) {
            installRouting(ctx)
        }
        try {
            instance.start(wait = false)
            server = instance
            Neocobblewatch.LOGGER.info("HTTP server listening on {}:{}", ctx.httpConfig.bind, ctx.httpConfig.port)
        } catch (e: Exception) {
            Neocobblewatch.LOGGER.error("Failed to start HTTP server on {}:{}", ctx.httpConfig.bind, ctx.httpConfig.port, e)
            throw e
        }
    }

    fun stop() {
        server?.stop(gracePeriodMillis = 1_000, timeoutMillis = 3_000)
        server = null
        Neocobblewatch.LOGGER.info("HTTP server stopped")
    }
}
