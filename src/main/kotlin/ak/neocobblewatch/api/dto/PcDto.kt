package ak.neocobblewatch.api.dto

import ak.neocobblewatch.pc.PcSlotSnapshot
import kotlinx.serialization.Serializable

@Serializable
internal data class PcSlotDto(
    val boxIndex: Int,
    val slotIndex: Int,
    val pokemon: PokemonDto,
)

@Serializable
internal data class PcResponse(val data: List<PcSlotDto>, val page: PageInfo)

internal fun PcSlotSnapshot.toDto(): PcSlotDto = PcSlotDto(
    boxIndex = boxIndex,
    slotIndex = slotIndex,
    pokemon = pokemon.toDto(),
)
