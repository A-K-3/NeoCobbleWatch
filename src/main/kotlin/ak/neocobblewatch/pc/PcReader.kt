package ak.neocobblewatch.pc

import ak.neocobblewatch.core.assertServerThread
import ak.neocobblewatch.pokemon.PokemonMapper
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.pc.POKEMON_PER_BOX
import net.minecraft.server.level.ServerPlayer

internal class PcReader {
    fun readFor(player: ServerPlayer): PcSnapshot {
        assertServerThread(player.server)
        val pc = Cobblemon.storage.getPC(player.uuid, player.server.registryAccess())
        val slots = buildList {
            pc.boxes.forEachIndexed { boxIndex, box ->
                for (slotIndex in 0 until POKEMON_PER_BOX) {
                    val pokemon = box[slotIndex] ?: continue
                    add(PcSlotSnapshot(boxIndex = boxIndex, slotIndex = slotIndex, pokemon = PokemonMapper.map(pokemon)))
                }
            }
        }
        return PcSnapshot(
            playerUuid = player.uuid,
            slots = slots,
            snapshotAt = System.currentTimeMillis(),
        )
    }
}
