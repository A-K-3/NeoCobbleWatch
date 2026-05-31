package ak.neocobblewatch.species

import ak.neocobblewatch.api.dto.EvolutionRequirementDto
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.conditional.RegistryLikeIdentifierCondition
import com.cobblemon.mod.common.api.conditional.RegistryLikeTagCondition
import com.cobblemon.mod.common.api.pokemon.requirement.Requirement
import com.cobblemon.mod.common.pokemon.requirements.AnyRequirement
import com.cobblemon.mod.common.pokemon.requirements.AttackDefenceRatioRequirement
import com.cobblemon.mod.common.pokemon.requirements.BiomeRequirement
import com.cobblemon.mod.common.pokemon.requirements.ChanceRequirement
import com.cobblemon.mod.common.pokemon.requirements.FriendshipRequirement
import com.cobblemon.mod.common.pokemon.requirements.HeldItemRequirement
import com.cobblemon.mod.common.pokemon.requirements.LevelRequirement
import com.cobblemon.mod.common.pokemon.requirements.MoonPhaseRequirement
import com.cobblemon.mod.common.pokemon.requirements.MoveSetRequirement
import com.cobblemon.mod.common.pokemon.requirements.MoveTypeRequirement
import com.cobblemon.mod.common.pokemon.requirements.OwnerHoldsItemRequirement
import com.cobblemon.mod.common.pokemon.requirements.StructureRequirement
import com.cobblemon.mod.common.pokemon.requirements.TimeRangeRequirement
import com.cobblemon.mod.common.pokemon.requirements.UseMoveRequirement
import com.cobblemon.mod.common.pokemon.requirements.WeatherRequirement
import com.cobblemon.mod.common.pokemon.requirements.WorldRequirement
import net.minecraft.advancements.critereon.ItemPredicate

internal object RequirementMapper {
    fun toDto(req: Requirement): EvolutionRequirementDto = when (req) {
        is LevelRequirement -> EvolutionRequirementDto(
            type = "level",
            minLevel = req.minLevel.takeIf { it > 0 },
            maxLevel = req.maxLevel.takeIf { it in 1 until Int.MAX_VALUE },
        )

        is FriendshipRequirement -> EvolutionRequirementDto(type = "friendship", amount = req.amount)

        is HeldItemRequirement -> itemDto("held_item", req.itemCondition)
        is OwnerHoldsItemRequirement -> itemDto("owner_holds_item", req.itemCondition)

        is BiomeRequirement -> req.biomeCondition?.let { conditionDto("biome", it) }
            ?: EvolutionRequirementDto(type = "biome")
        is StructureRequirement -> req.structureCondition?.let { conditionDto("structure", it) }
            ?: EvolutionRequirementDto(type = "structure")

        is TimeRangeRequirement -> EvolutionRequirementDto(type = "time_range")

        is WeatherRequirement -> EvolutionRequirementDto(
            type = "weather",
            raining = req.isRaining,
            thundering = req.isThundering,
        )

        is MoonPhaseRequirement -> EvolutionRequirementDto(
            type = "moon_phase",
            moonPhase = req.moonPhase.name,
        )

        is WorldRequirement -> EvolutionRequirementDto(
            type = "world",
            identifier = req.identifier.toString(),
        )

        is MoveSetRequirement -> EvolutionRequirementDto(type = "move_set", move = req.move.name)
        is MoveTypeRequirement -> EvolutionRequirementDto(type = "move_type", moveType = req.type.name)
        is UseMoveRequirement -> EvolutionRequirementDto(
            type = "use_move",
            move = req.move.name,
            amount = req.amount,
        )

        is AttackDefenceRatioRequirement -> EvolutionRequirementDto(
            type = "attack_defence_ratio",
            ratio = req.ratio.name,
        )

        is ChanceRequirement -> EvolutionRequirementDto(
            type = "chance",
            chance = runCatching { req.chance.toString() }.getOrNull(),
        )

        is AnyRequirement -> EvolutionRequirementDto(
            type = "any",
            children = req.possibilities.map { toDto(it) },
        )

        else -> EvolutionRequirementDto(
            type = req::class.simpleName?.removeSuffix("Requirement")?.lowercase() ?: "unknown",
        )
    }

    private fun itemDto(type: String, predicate: ItemPredicate): EvolutionRequirementDto =
        runCatching {
            val holderSet = predicate.items.orElse(null) ?: return@runCatching EvolutionRequirementDto(type = type)
            val either = holderSet.unwrap()
            val tag = either.left().orElse(null)
            if (tag != null) {
                EvolutionRequirementDto(type = type, itemTag = "#${tag.location}")
            } else {
                val holders = either.right().orElse(emptyList())
                val firstId = holders.firstOrNull()?.unwrapKey()?.orElse(null)?.location()?.toString()
                EvolutionRequirementDto(type = type, item = firstId)
            }
        }.getOrElse { EvolutionRequirementDto(type = type) }

    private fun conditionDto(type: String, condition: RegistryLikeCondition<*>): EvolutionRequirementDto = when (condition) {
        is RegistryLikeIdentifierCondition<*> -> EvolutionRequirementDto(
            type = type,
            identifier = condition.identifier.toString(),
        )
        is RegistryLikeTagCondition<*> -> EvolutionRequirementDto(
            type = type,
            tag = "#" + condition.tag.location.toString(),
        )
        else -> EvolutionRequirementDto(type = type)
    }
}
