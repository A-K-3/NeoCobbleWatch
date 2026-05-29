package ak.neocobblewatch.core

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

internal class ModScope {
    private var current: CoroutineScope? = null

    val isStarted: Boolean get() = current != null

    val scope: CoroutineScope
        get() = current ?: error("ModScope not started")

    fun start() {
        check(current == null) { "ModScope already started" }
        current = CoroutineScope(
            SupervisorJob() + Dispatchers.Default + CoroutineName("neocobblewatch"),
        )
    }

    fun cancel() {
        current?.cancel()
        current = null
    }
}
