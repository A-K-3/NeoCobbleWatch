package ak.neocobblewatch.pokemon

import kotlinx.serialization.Serializable

@Serializable
internal data class PokemonSnapshot(
    val pokemonUuid: String,
    val speciesId: String,
    val formName: String,
    val nickname: String?,
    val level: Int,
    val shiny: Boolean,
    val gender: String,
    val nature: String,
    val ability: String,
    val ball: String,
    val teraType: String?,
    val heldItem: String?,
    val otName: String?,
    val friendship: Int,
    val ivs: Map<String, Int>,
    val evs: Map<String, Int>,
    val moves: List<MoveSnapshot>,
    val aspects: Set<String>,
)

@Serializable
internal data class MoveSnapshot(
    val name: String,
    val pp: Int,
    val ppMax: Int,
)
