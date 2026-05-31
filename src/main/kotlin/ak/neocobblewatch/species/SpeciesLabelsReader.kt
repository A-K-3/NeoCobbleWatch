package ak.neocobblewatch.species

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies

internal fun generationOf(dex: Int): Int = when (dex) {
    in 1..151 -> 1
    in 152..251 -> 2
    in 252..386 -> 3
    in 387..493 -> 4
    in 494..649 -> 5
    in 650..721 -> 6
    in 722..809 -> 7
    in 810..905 -> 8
    in 906..1025 -> 9
    else -> 0
}

internal object SpeciesLabelsReader {
    fun readAll(): SpeciesLabelsSnapshot {
        val map = PokemonSpecies.species.mapNotNull { species ->
            val rid = species.resourceIdentifier ?: return@mapNotNull null
            val id = rid.toString()
            id to SpeciesInfo(
                id = id,
                nationalDex = species.nationalPokedexNumber,
                generation = generationOf(species.nationalPokedexNumber),
                labels = species.labels.toSet(),
                fullyEvolved = species.evolutions.isEmpty(),
                primaryType = species.primaryType.name,
                secondaryType = species.secondaryType?.name,
            )
        }.toMap()
        return SpeciesLabelsSnapshot(species = map, snapshotAt = System.currentTimeMillis())
    }
}
