package ak.neocobblewatch.party

import ak.neocobblewatch.core.assertServerThread
import ak.neocobblewatch.pokemon.PokemonMapper
import com.cobblemon.mod.common.Cobblemon
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

internal object PartyReader {
    private const val PARTY_SIZE = 6

    fun readFor(player: ServerPlayer): PartySnapshot = readFor(player.server, player.uuid)

    fun readFor(server: MinecraftServer, uuid: UUID): PartySnapshot {
        assertServerThread(server)
        val party = Cobblemon.storage.getParty(uuid, server.registryAccess())
        val slots = (0 until PARTY_SIZE).mapNotNull { slotIndex ->
            val pokemon = party.get(slotIndex) ?: return@mapNotNull null
            PartySlotSnapshot(slotIndex = slotIndex, pokemon = PokemonMapper.map(pokemon))
        }
        return PartySnapshot(
            playerUuid = uuid,
            slots = slots,
            snapshotAt = System.currentTimeMillis(),
        )
    }
}
