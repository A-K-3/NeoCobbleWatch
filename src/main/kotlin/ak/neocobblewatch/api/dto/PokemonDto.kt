package ak.neocobblewatch.api.dto

import ak.neocobblewatch.pokemon.MoveSnapshot
import ak.neocobblewatch.pokemon.PokemonSnapshot
import kotlinx.serialization.Serializable

@Serializable
internal data class PokemonDto(
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
    val moves: List<MoveDto>,
    val aspects: Set<String>,
)

@Serializable
internal data class MoveDto(
    val name: String,
    val pp: Int,
    val ppMax: Int,
)

internal fun PokemonSnapshot.toDto(): PokemonDto = PokemonDto(
    pokemonUuid = pokemonUuid,
    speciesId = speciesId,
    formName = formName,
    nickname = nickname,
    level = level,
    shiny = shiny,
    gender = gender,
    nature = nature,
    ability = ability,
    ball = ball,
    teraType = teraType,
    heldItem = heldItem,
    otName = otName,
    friendship = friendship,
    ivs = ivs,
    evs = evs,
    moves = moves.map { it.toDto() },
    aspects = aspects,
)

internal fun MoveSnapshot.toDto(): MoveDto = MoveDto(name = name, pp = pp, ppMax = ppMax)
