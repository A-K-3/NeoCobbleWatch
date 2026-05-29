package ak.neocobblewatch.api.dto

import ak.neocobblewatch.party.PartySlotSnapshot
import ak.neocobblewatch.party.PartySnapshot
import kotlinx.serialization.Serializable

@Serializable
internal data class PartySlotDto(val slotIndex: Int, val pokemon: PokemonDto)

@Serializable
internal data class PartyDto(val slots: List<PartySlotDto>, val snapshotAt: Long)

@Serializable
internal data class PartyResponse(val data: PartyDto)

internal fun PartySlotSnapshot.toDto(): PartySlotDto = PartySlotDto(slotIndex = slotIndex, pokemon = pokemon.toDto())

internal fun PartySnapshot.toDto(): PartyDto = PartyDto(
    slots = slots.map { it.toDto() },
    snapshotAt = snapshotAt,
)
