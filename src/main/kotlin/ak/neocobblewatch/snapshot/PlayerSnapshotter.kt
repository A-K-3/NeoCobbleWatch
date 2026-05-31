package ak.neocobblewatch.snapshot

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.activity.ActivityFileReader
import ak.neocobblewatch.activity.ActivityRepository
import ak.neocobblewatch.activity.ActivitySnapshot
import ak.neocobblewatch.advancements.AdvancementsFileReader
import ak.neocobblewatch.advancements.AdvancementsRepository
import ak.neocobblewatch.advancements.AdvancementsSnapshot
import ak.neocobblewatch.economy.EconomyFileReader
import ak.neocobblewatch.economy.EconomyRepository
import ak.neocobblewatch.economy.EconomySnapshot
import ak.neocobblewatch.core.ServerThreadDispatcher
import ak.neocobblewatch.party.PartyReader
import ak.neocobblewatch.party.PartyRepository
import ak.neocobblewatch.party.PartySnapshot
import ak.neocobblewatch.pc.PcReader
import ak.neocobblewatch.pc.PcRepository
import ak.neocobblewatch.pc.PcSnapshot
import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.player.OfflinePlayerContext
import ak.neocobblewatch.player.PlayerReader
import ak.neocobblewatch.player.PlayerRepository
import ak.neocobblewatch.player.PlayerSnapshot
import ak.neocobblewatch.pokedex.PokedexReader
import ak.neocobblewatch.pokedex.PokedexRepository
import ak.neocobblewatch.pokedex.PokedexSnapshot
import ak.neocobblewatch.stats.StatsReader
import ak.neocobblewatch.stats.StatsRepository
import ak.neocobblewatch.stats.StatsSnapshot
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

internal class PlayerSnapshotter(
    private val serverThreadDispatcher: ServerThreadDispatcher,
    private val database: Database,
    private val playerRepository: PlayerRepository,
    private val statsRepository: StatsRepository,
    private val pokedexRepository: PokedexRepository,
    private val partyRepository: PartyRepository,
    private val pcRepository: PcRepository,
    private val activityRepository: ActivityRepository,
    private val activityFileReader: ActivityFileReader,
    private val advancementsRepository: AdvancementsRepository,
    private val advancementsFileReader: AdvancementsFileReader,
    private val economyRepository: EconomyRepository,
    private val economyFileReader: EconomyFileReader,
) {
    /**
     * Cobblemon storage (party, PC, Pokédex, stats) must only be accessed from the server
     * thread — reads happen there and produce immutable snapshots so file IO can safely
     * proceed off-thread in parallel.
     */
    suspend fun snapshot(player: ServerPlayer, online: Boolean = true) =
        snapshotSafe(player.name.string, player.uuid) {
            val cobblemon = withContext(serverThreadDispatcher) { readCobblemonOnline(player, online) }
            val (activity, advancements, economy) = readFileData(player.uuid)
            cobblemon.toBundle(activity, advancements, economy)
        }

    suspend fun snapshot(server: MinecraftServer, ctx: OfflinePlayerContext) =
        snapshotSafe(ctx.name, ctx.uuid) {
            val cobblemon = withContext(serverThreadDispatcher) { readCobblemonOffline(server, ctx) }
            val (activity, advancements, economy) = readFileData(ctx.uuid)
            cobblemon.toBundle(activity, advancements, economy)
        }

    private suspend fun readFileData(uuid: UUID) = withContext(Dispatchers.IO) {
        coroutineScope {
            val a = async { activityFileReader.readFor(uuid) }
            val b = async { advancementsFileReader.readFor(uuid) }
            val e = async { economyFileReader.readFor(uuid) }
            Triple(a.await(), b.await(), e.await())
        }
    }

    private suspend fun snapshotSafe(name: String, uuid: UUID, read: suspend () -> Bundle) {
        try {
            persist(read())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Neocobblewatch.LOGGER.warn("Snapshot failed for {} ({}): {}", name, uuid, e.message, e)
        }
    }

    private fun readCobblemonOnline(player: ServerPlayer, online: Boolean) = CobblemonBundle(
        player = PlayerReader.readFor(player, online),
        stats = StatsReader.readFor(player),
        pokedex = PokedexReader.readFor(player),
        party = PartyReader.readFor(player),
        pc = PcReader.readFor(player),
    )

    private fun readCobblemonOffline(server: MinecraftServer, ctx: OfflinePlayerContext) = CobblemonBundle(
        player = PlayerReader.readFor(server, ctx, online = false),
        stats = StatsReader.readFor(server, ctx.uuid),
        pokedex = PokedexReader.readFor(server, ctx.uuid),
        party = PartyReader.readFor(server, ctx.uuid),
        pc = PcReader.readFor(server, ctx.uuid),
    )

    private suspend fun persist(bundle: Bundle) = database.transaction { conn ->
        playerRepository.upsert(conn, bundle.player)
        statsRepository.upsert(conn, bundle.stats)
        pokedexRepository.replace(conn, bundle.pokedex)
        partyRepository.replace(conn, bundle.party)
        pcRepository.replace(conn, bundle.pc)
        activityRepository.upsert(conn, bundle.activity)
        advancementsRepository.upsert(conn, bundle.advancements)
        economyRepository.upsert(conn, bundle.economy)
    }

    private data class CobblemonBundle(
        val player: PlayerSnapshot,
        val stats: StatsSnapshot,
        val pokedex: PokedexSnapshot,
        val party: PartySnapshot,
        val pc: PcSnapshot,
    ) {
        fun toBundle(activity: ActivitySnapshot, advancements: AdvancementsSnapshot, economy: EconomySnapshot) =
            Bundle(player, stats, pokedex, party, pc, activity, advancements, economy)
    }

    private data class Bundle(
        val player: PlayerSnapshot,
        val stats: StatsSnapshot,
        val pokedex: PokedexSnapshot,
        val party: PartySnapshot,
        val pc: PcSnapshot,
        val activity: ActivitySnapshot,
        val advancements: AdvancementsSnapshot,
        val economy: EconomySnapshot,
    )
}
