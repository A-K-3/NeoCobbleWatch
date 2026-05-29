package ak.neocobblewatch.pokemon

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.PokemonStats
import net.minecraft.core.registries.BuiltInRegistries

/**
 * Cobblemon [Pokemon] state is mutable and server-thread-only. The resulting [PokemonSnapshot]
 * is immutable and safe to pass to other dispatchers.
 */
internal object PokemonMapper {
    fun map(pokemon: Pokemon): PokemonSnapshot = PokemonSnapshot(
        pokemonUuid = pokemon.uuid.toString(),
        speciesId = pokemon.species.resourceIdentifier.toString(),
        formName = pokemon.form.name,
        nickname = pokemon.nickname?.string,
        level = pokemon.level,
        shiny = pokemon.shiny,
        gender = pokemon.gender.name,
        nature = pokemon.nature.name.toString(),
        ability = pokemon.ability.name,
        ball = pokemon.caughtBall.name.toString(),
        teraType = pokemon.teraType.id.toString(),
        heldItem = mapHeldItem(pokemon),
        otName = pokemon.originalTrainerName,
        friendship = pokemon.friendship,
        ivs = readStats(pokemon.ivs),
        evs = readStats(pokemon.evs),
        moves = pokemon.moveSet.getMoves().map { MoveSnapshot(it.name, it.currentPp, it.maxPp) },
        aspects = pokemon.aspects.toSet(),
    )

    private fun mapHeldItem(pokemon: Pokemon): String? {
        val stack = pokemon.heldItem()
        if (stack.isEmpty) return null
        return BuiltInRegistries.ITEM.getKey(stack.item).toString()
    }

    private fun readStats(stats: PokemonStats): Map<String, Int> = buildMap {
        for ((stat, value) in stats) {
            put(stat.showdownId, value)
        }
    }
}
