package ak.neocobblewatch.party

import ak.neocobblewatch.core.assertServerThread
import ak.neocobblewatch.pokemon.PokemonMapper
import com.cobblemon.mod.common.Cobblemon
import net.minecraft.server.level.ServerPlayer

internal class PartyReader {
    fun readFor(player: ServerPlayer): PartySnapshot {
        assertServerThread(player.server)
        val party = Cobblemon.storage.getParty(player.uuid, player.server.registryAccess())
        val slots = (0 until PARTY_SIZE).mapNotNull { slotIndex ->
            val pokemon = party.get(slotIndex) ?: return@mapNotNull null
            PartySlotSnapshot(slotIndex = slotIndex, pokemon = PokemonMapper.map(pokemon))
        }
        return PartySnapshot(
            playerUuid = player.uuid,
            slots = slots,
            snapshotAt = System.currentTimeMillis(),
        )
    }

    private companion object {
        const val PARTY_SIZE = 6
    }
}
