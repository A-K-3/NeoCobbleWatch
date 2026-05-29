package ak.neocobblewatch

import ak.neocobblewatch.core.ModLifecycle
import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(Neocobblewatch.ID)
object Neocobblewatch {
    const val ID = "neocobblewatch"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        ModLifecycle.register()
    }
}
