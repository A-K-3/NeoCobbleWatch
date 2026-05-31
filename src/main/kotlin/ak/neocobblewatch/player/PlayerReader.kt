package ak.neocobblewatch.player

import ak.neocobblewatch.core.assertServerThread
import com.cobblemon.mod.common.Cobblemon
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * Caller-provided identity + timing for the offline path. For online snapshots
 * the regular [PlayerReader.readFor] derives everything from the [ServerPlayer].
 */
internal data class OfflinePlayerContext(
    val uuid: UUID,
    val name: String,
    val firstSeen: Long,
    val lastSeen: Long,
)

internal object PlayerReader {
    /**
     * Online path: identity comes from the [ServerPlayer], timestamps default to now.
     * `firstSeen` is set to now; [PlayerRepository.upsert] keeps the existing value via
     * `ON CONFLICT` so this only materializes on the very first snapshot for a player.
     */
    fun readFor(player: ServerPlayer, online: Boolean = true): PlayerSnapshot {
        val now = System.currentTimeMillis()
        val ctx = OfflinePlayerContext(
            uuid = player.uuid,
            name = player.name.string,
            firstSeen = now,
            lastSeen = now,
        )
        return readFor(player.server, ctx, online)
    }

    /**
     * Offline/backfill path: identity and timestamps are provided by the caller (resolved
     * from `usercache.json` and the on-disk mtime of `cobblemonplayerdata/<uuid>.json`).
     */
    fun readFor(server: MinecraftServer, ctx: OfflinePlayerContext, online: Boolean): PlayerSnapshot {
        assertServerThread(server)
        val data = Cobblemon.playerDataManager.getGenericData(ctx.uuid)
        return PlayerSnapshot(
            uuid = ctx.uuid,
            name = ctx.name,
            firstSeen = ctx.firstSeen,
            lastSeen = ctx.lastSeen,
            online = online,
            starterPrompted = data.starterPrompted,
            starterLocked = data.starterLocked,
            starterSelected = data.starterSelected,
            starterUuid = data.starterUUID,
            keyItems = data.keyItems.map { it.toString() }.toSet(),
            snapshotAt = System.currentTimeMillis(),
        )
    }
}
