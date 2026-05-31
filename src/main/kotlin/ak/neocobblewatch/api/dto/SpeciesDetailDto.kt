package ak.neocobblewatch.api.dto

import kotlinx.serialization.Serializable

/** Discriminated union: `type` comes from [RequirementMapper]; only the relevant fields are set. */
@Serializable
internal data class EvolutionRequirementDto(
    val type: String,
    val minLevel: Int? = null,
    val maxLevel: Int? = null,
    val amount: Int? = null,
    val item: String? = null,
    val itemTag: String? = null,
    val identifier: String? = null,
    val tag: String? = null,
    val raining: Boolean? = null,
    val thundering: Boolean? = null,
    val moonPhase: String? = null,
    val move: String? = null,
    val moveType: String? = null,
    val ratio: String? = null,
    val chance: String? = null,
    val children: List<EvolutionRequirementDto>? = null,
)

@Serializable
internal data class EvolutionDto(
    val resultId: String?,
    val resultName: String?,
    val resultAspects: List<String>,
    val requirements: List<EvolutionRequirementDto>,
    val optional: Boolean,
    val consumeHeldItem: Boolean,
)

@Serializable
internal data class SpawnDto(
    val bucket: String,
    val weight: Float,
    val levelMin: Int,
    val levelMax: Int,
    val biomes: List<String>,
    val labels: List<String>,
)

@Serializable
internal data class PreEvolutionDto(
    val speciesId: String,
    val speciesName: String,
)

@Serializable
internal data class DropEntryDto(
    val item: String,
    val percentage: Float,
    val quantityMin: Int,
    val quantityMax: Int,
)

@Serializable
internal data class SpecialFormDto(
    val name: String,
    val aspects: List<String>,
    val labels: List<String>,
    val primaryType: String,
    val secondaryType: String?,
    val dynamaxBlocked: Boolean,
)

@Serializable
internal data class SpeciesDetailDto(
    val id: String,
    val name: String,
    val nationalDex: Int,
    val generation: Int,
    val primaryType: String,
    val secondaryType: String?,
    val labels: List<String>,
    val fullyEvolved: Boolean,
    val catchRate: Int,
    val preEvolution: PreEvolutionDto?,
    val evolutions: List<EvolutionDto>,
    val drops: List<DropEntryDto>,
    val spawns: List<SpawnDto>,
    val forms: List<SpecialFormDto>,
)

@Serializable
internal data class SpeciesDetailResponse(val data: SpeciesDetailDto)
