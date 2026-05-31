package ak.neocobblewatch.api

import ak.neocobblewatch.activity.ActivityRepository
import ak.neocobblewatch.advancements.AdvancementsRepository
import ak.neocobblewatch.economy.EconomyRepository
import ak.neocobblewatch.core.HttpConfig
import ak.neocobblewatch.party.PartyRepository
import ak.neocobblewatch.pc.PcRepository
import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.player.PlayerRepository
import ak.neocobblewatch.pokedex.PokedexRepository
import ak.neocobblewatch.snapshot.SnapshotScheduler
import ak.neocobblewatch.species.SpeciesLabelsCache
import ak.neocobblewatch.stats.StatsRepository
import ak.neocobblewatch.stats.TopsRepository

internal class ApiContext(
    val httpConfig: HttpConfig,
    val scheduler: SnapshotScheduler,
    val database: Database,
    val playerRepository: PlayerRepository,
    val statsRepository: StatsRepository,
    val pokedexRepository: PokedexRepository,
    val partyRepository: PartyRepository,
    val pcRepository: PcRepository,
    val topsRepository: TopsRepository,
    val speciesLabelsCache: SpeciesLabelsCache,
    val activityRepository: ActivityRepository,
    val advancementsRepository: AdvancementsRepository,
    val economyRepository: EconomyRepository,
)
