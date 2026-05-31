package ak.neocobblewatch.species

import ak.neocobblewatch.api.dto.DropEntryDto
import ak.neocobblewatch.api.dto.EvolutionDto
import ak.neocobblewatch.api.dto.EvolutionRequirementDto
import ak.neocobblewatch.api.dto.PreEvolutionDto
import ak.neocobblewatch.api.dto.SpawnDto
import ak.neocobblewatch.api.dto.SpecialFormDto
import ak.neocobblewatch.api.dto.SpeciesDetailDto
import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.pokemon.evolution.variants.ItemInteractionEvolution
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.resources.ResourceLocation

internal object SpeciesDetailReader {
    fun read(id: ResourceLocation, cache: SpeciesLabelsCache): SpeciesDetailDto? {
        val species = PokemonSpecies.getByIdentifier(id) ?: return null

        val evolutions = species.evolutions.map { evo ->
            val resultSpecies = evo.result.species?.let { PokemonSpecies.getByName(it) }
            val resultAspects = evo.result.aspects.toSortedSet()
            val baseName = resultSpecies?.name?.let(::prettify)
            val resultName = if (baseName != null && resultAspects.isNotEmpty())
                "$baseName (${resultAspects.joinToString(", ") { prettify(it) }})"
            else baseName
            val triggerReq = (evo as? ItemInteractionEvolution)
                ?.requiredContext
                ?.let { extractItemId(it) }
                ?.let { EvolutionRequirementDto(type = "item_trigger", item = it) }
            EvolutionDto(
                resultId = resultSpecies?.resourceIdentifier?.toString(),
                resultName = resultName,
                resultAspects = resultAspects.toList(),
                requirements = listOfNotNull(triggerReq) + evo.requirements.map(RequirementMapper::toDto),
                optional = evo.optional,
                consumeHeldItem = evo.consumeHeldItem,
            )
        }

        val preEvo = species.preEvolution?.species?.let { pre ->
            val rid = pre.resourceIdentifier ?: return@let null
            PreEvolutionDto(speciesId = rid.toString(), speciesName = prettify(pre.name))
        }

        val drops = species.drops.entries.mapNotNull { entry ->
            if (entry !is ItemDropEntry) return@mapNotNull null
            val itemId = entry.item?.toString() ?: return@mapNotNull null
            val range = entry.quantityRange
            DropEntryDto(
                item = itemId,
                percentage = entry.percentage,
                quantityMin = range?.first ?: entry.quantity.coerceAtLeast(1),
                quantityMax = range?.last ?: entry.quantity.coerceAtLeast(1),
            )
        }

        val specialForms = species.forms
            .filter { form -> form.labels.any { it.lowercase() in setOf("mega", "gmax") } }
            .map { form ->
                SpecialFormDto(
                    name = prettify(form.name),
                    aspects = form.aspects.toList().sorted(),
                    labels = form.labels.toList().sorted(),
                    primaryType = form.primaryType?.name ?: species.primaryType.name,
                    secondaryType = (form.secondaryType ?: species.secondaryType)?.name,
                    dynamaxBlocked = form.dynamaxBlocked,
                )
            }

        return SpeciesDetailDto(
            id = id.toString(),
            name = prettify(species.name),
            nationalDex = species.nationalPokedexNumber,
            generation = generationOf(species.nationalPokedexNumber),
            primaryType = species.primaryType.name,
            secondaryType = species.secondaryType?.name,
            labels = species.labels.toList().sorted(),
            fullyEvolved = species.evolutions.isEmpty(),
            catchRate = species.catchRate,
            preEvolution = preEvo,
            evolutions = evolutions,
            drops = drops,
            spawns = cache.spawnsFor(species.name),
            forms = specialForms,
        )
    }

    /** Walks the global spawn pool once and groups PokemonSpawnDetails by lowercase species name. */
    fun collectAllSpawns(): Map<String, List<SpawnDto>> {
        val pool = CobblemonSpawnPools.WORLD_SPAWN_POOL ?: return emptyMap()
        val out = mutableMapOf<String, MutableList<SpawnDto>>()
        for (detail in pool) {
            if (detail !is PokemonSpawnDetail) continue
            val target = detail.pokemon.species?.lowercase() ?: continue
            val range = detail.levelRange
            val dto = SpawnDto(
                bucket = detail.bucket.name,
                weight = detail.weight,
                levelMin = range?.first ?: 1,
                levelMax = range?.last ?: 100,
                biomes = detail.validBiomes.map(ResourceLocation::toString).sorted(),
                labels = detail.labels.toList(),
            )
            out.getOrPut(target) { mutableListOf() } += dto
        }
        return out.mapValues { it.value.sortedByDescending { dto -> dto.weight } }
    }

    private fun extractItemId(predicate: ItemPredicate): String? = runCatching {
        val holderSet = predicate.items.orElse(null) ?: return@runCatching null
        val either = holderSet.unwrap()
        val tag = either.left().orElse(null)
        if (tag != null) return@runCatching "#${tag.location}"
        val holders = either.right().orElse(emptyList())
        holders.firstOrNull()?.unwrapKey()?.orElse(null)?.location()?.toString()
    }.getOrNull()

    private fun prettify(raw: String): String =
        raw.split('_', '-', ' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
}
