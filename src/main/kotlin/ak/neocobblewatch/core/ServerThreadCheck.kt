package ak.neocobblewatch.core

import net.minecraft.server.MinecraftServer

internal fun assertServerThread(server: MinecraftServer) {
    check(server.isSameThread) {
        "Must be called on the server thread, but was on ${Thread.currentThread().name}"
    }
}
