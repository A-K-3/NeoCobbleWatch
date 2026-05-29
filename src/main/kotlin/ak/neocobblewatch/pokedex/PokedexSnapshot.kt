package ak.neocobblewatch.pokedex

import java.util.UUID

internal enum class PokedexKnowledge { NONE, ENCOUNTERED, CAUGHT }

internal data class PokedexEntrySnapshot(
    val speciesId: String,
    val knowledge: PokedexKnowledge,
    val formsEncountered: Set<String>,
    val formsCaught: Set<String>,
    val shinyStates: Set<String>,
    val gendersSeen: Set<String>,
    val aspectsSeen: Set<String>,
)

internal data class PokedexSnapshot(
    val playerUuid: UUID,
    val entries: List<PokedexEntrySnapshot>,
    val snapshotAt: Long,
)
