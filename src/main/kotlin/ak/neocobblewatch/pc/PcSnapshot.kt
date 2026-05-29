package ak.neocobblewatch.pc

import ak.neocobblewatch.pokemon.PokemonSnapshot
import java.util.UUID

internal data class PcSlotSnapshot(
    val boxIndex: Int,
    val slotIndex: Int,
    val pokemon: PokemonSnapshot,
)

internal data class PcSnapshot(
    val playerUuid: UUID,
    val slots: List<PcSlotSnapshot>,
    val snapshotAt: Long,
)
