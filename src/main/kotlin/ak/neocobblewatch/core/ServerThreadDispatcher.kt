package ak.neocobblewatch.core

import kotlinx.coroutines.CoroutineDispatcher
import net.minecraft.server.MinecraftServer
import kotlin.coroutines.CoroutineContext

/** The only safe dispatcher to touch Cobblemon's API. */
internal class ServerThreadDispatcher : CoroutineDispatcher() {
    @Volatile
    private var server: MinecraftServer? = null

    val isBound: Boolean get() = server != null

    fun bind(server: MinecraftServer) {
        check(this.server == null) { "ServerThreadDispatcher already bound" }
        this.server = server
    }

    fun unbind() {
        server = null
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val s = server ?: error("ServerThreadDispatcher used without a bound server")
        s.execute(block)
    }
}
