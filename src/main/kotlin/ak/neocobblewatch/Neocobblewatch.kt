package ak.neocobblewatch

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(Neocobblewatch.ID)
@EventBusSubscriber
object Neocobblewatch {
    const val ID = "neocobblewatch"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.info("NeoCobbleWatch initialized")
    }

    @SubscribeEvent
    fun onCommonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("Common setup complete")
    }
}
