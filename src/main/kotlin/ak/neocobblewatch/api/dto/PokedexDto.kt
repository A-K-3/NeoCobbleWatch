package ak.neocobblewatch.api.dto

import ak.neocobblewatch.pokedex.PokedexEntrySnapshot
import ak.neocobblewatch.pokedex.PokedexSnapshot
import kotlinx.serialization.Serializable

@Serializable
internal data class PokedexEntryDto(
    val speciesId: String,
    val knowledge: String,
    val formsEncountered: Set<String>,
    val formsCaught: Set<String>,
    val shinyStates: Set<String>,
    val gendersSeen: Set<String>,
    val aspectsSeen: Set<String>,
)

@Serializable
internal data class PokedexDto(
    val entries: List<PokedexEntryDto>,
    val snapshotAt: Long,
)

@Serializable
internal data class PokedexResponse(val data: PokedexDto)

internal fun PokedexEntrySnapshot.toDto(): PokedexEntryDto = PokedexEntryDto(
    speciesId = speciesId,
    knowledge = knowledge.name,
    formsEncountered = formsEncountered,
    formsCaught = formsCaught,
    shinyStates = shinyStates,
    gendersSeen = gendersSeen,
    aspectsSeen = aspectsSeen,
)

internal fun PokedexSnapshot.toDto(): PokedexDto = PokedexDto(
    entries = entries.map { it.toDto() },
    snapshotAt = snapshotAt,
)
