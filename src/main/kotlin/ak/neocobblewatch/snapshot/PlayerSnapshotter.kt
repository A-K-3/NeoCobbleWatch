package ak.neocobblewatch.snapshot

import ak.neocobblewatch.Neocobblewatch
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
) {
    /**
     * Reads all five domains on the server thread (Cobblemon API requires it), then commits them
     * to SQLite in a single transaction — one fsync instead of five.
     * Per-player errors are swallowed and logged so the scheduler's outer loop can move on.
     */
    suspend fun snapshot(player: ServerPlayer, online: Boolean = true) =
        snapshotSafe(player.name.string, player.uuid) {
            withContext(serverThreadDispatcher) { readOnline(player, online) }
        }

    /** Offline path: caller resolves identity/timestamps from disk before invoking. */
    suspend fun snapshot(server: MinecraftServer, ctx: OfflinePlayerContext) =
        snapshotSafe(ctx.name, ctx.uuid) {
            withContext(serverThreadDispatcher) { readOffline(server, ctx) }
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

    private fun readOnline(player: ServerPlayer, online: Boolean): Bundle = Bundle(
        player = PlayerReader.readFor(player, online),
        stats = StatsReader.readFor(player),
        pokedex = PokedexReader.readFor(player),
        party = PartyReader.readFor(player),
        pc = PcReader.readFor(player),
    )

    private fun readOffline(server: MinecraftServer, ctx: OfflinePlayerContext): Bundle = Bundle(
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
    }

    private data class Bundle(
        val player: PlayerSnapshot,
        val stats: StatsSnapshot,
        val pokedex: PokedexSnapshot,
        val party: PartySnapshot,
        val pc: PcSnapshot,
    )
}
