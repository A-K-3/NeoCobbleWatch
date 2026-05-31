package ak.neocobblewatch.core

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

internal object Commands {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> register(dispatcher) }
    }

    private fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            LiteralArgumentBuilder.literal<CommandSourceStack>("neocobblewatch")
                .requires { it.hasPermission(2) }
                .then(
                    LiteralArgumentBuilder.literal<CommandSourceStack>("resync")
                        .executes { ctx -> executeResync(ctx.source) }
                )
        )
    }

    private fun executeResync(source: CommandSourceStack): Int {
        val runner = ModLifecycle.backfill
        if (runner == null) {
            source.sendFailure(Component.literal("[NeoCobbleWatch] El servidor no está listo todavía."))
            return 0
        }
        source.sendSuccess(
            { Component.literal("[NeoCobbleWatch] Resync iniciado — revisa los logs del servidor para ver el progreso.") },
            true,
        )
        ModLifecycle.modScope.scope.launch(CoroutineName("resync-forced")) {
            runner.runForced()
        }
        return 1
    }
}
