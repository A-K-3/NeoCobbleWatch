package ak.neocobblewatch.party

import ak.neocobblewatch.pokemon.PokemonSnapshot
import java.util.UUID

internal data class PartySlotSnapshot(
    val slotIndex: Int,
    val pokemon: PokemonSnapshot,
)

internal data class PartySnapshot(
    val playerUuid: UUID,
    val slots: List<PartySlotSnapshot>,
    val snapshotAt: Long,
)
