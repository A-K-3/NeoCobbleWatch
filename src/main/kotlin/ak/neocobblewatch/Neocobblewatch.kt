package ak.neocobblewatch

import ak.neocobblewatch.core.Config
import ak.neocobblewatch.core.ModLifecycle
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Neocobblewatch : ModInitializer {
    const val ID = "neocobblewatch"

    val LOGGER: Logger = LoggerFactory.getLogger(ID)

    val VERSION: String by lazy {
        FabricLoader.getInstance().getModContainer(ID)
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")
    }

    override fun onInitialize() {
        Config.load()
        ModLifecycle.register()
    }
}
