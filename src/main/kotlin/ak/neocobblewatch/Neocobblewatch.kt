package ak.neocobblewatch

import ak.neocobblewatch.core.ConfigSpec
import ak.neocobblewatch.core.ModLifecycle
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(Neocobblewatch.ID)
class Neocobblewatch(container: ModContainer) {
    init {
        container.registerConfig(ModConfig.Type.COMMON, ConfigSpec.spec)
        ModLifecycle.register()
    }

    companion object {
        const val ID = "neocobblewatch"
        val LOGGER: Logger = LogManager.getLogger(ID)
    }
}
