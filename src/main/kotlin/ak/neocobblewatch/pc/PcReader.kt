package ak.neocobblewatch.pc

import ak.neocobblewatch.core.assertServerThread
import ak.neocobblewatch.pokemon.PokemonMapper
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.pc.POKEMON_PER_BOX
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

internal object PcReader {
    fun readFor(player: ServerPlayer): PcSnapshot = readFor(player.server, player.uuid)

    fun readFor(server: MinecraftServer, uuid: UUID): PcSnapshot {
        assertServerThread(server)
        val pc = Cobblemon.storage.getPC(uuid, server.registryAccess())
        val slots = buildList {
            pc.boxes.forEachIndexed { boxIndex, box ->
                for (slotIndex in 0 until POKEMON_PER_BOX) {
                    val pokemon = box[slotIndex] ?: continue
                    add(PcSlotSnapshot(boxIndex = boxIndex, slotIndex = slotIndex, pokemon = PokemonMapper.map(pokemon)))
                }
            }
        }
        return PcSnapshot(
            playerUuid = uuid,
            slots = slots,
            snapshotAt = System.currentTimeMillis(),
        )
    }
}
