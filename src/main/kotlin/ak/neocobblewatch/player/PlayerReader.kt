package ak.neocobblewatch.player

import ak.neocobblewatch.core.assertServerThread
import com.cobblemon.mod.common.Cobblemon
import net.minecraft.server.level.ServerPlayer

internal object PlayerReader {
    /**
     * `firstSeen` is set to now; [PlayerRepository.upsert] keeps the existing value via
     * `ON CONFLICT` so this only materializes on the very first snapshot for a player.
     */
    fun readFor(player: ServerPlayer, online: Boolean = true): PlayerSnapshot {
        assertServerThread(player.server)
        val data = Cobblemon.playerDataManager.getGenericData(player.uuid)
        val now = System.currentTimeMillis()
        return PlayerSnapshot(
            uuid = player.uuid,
            name = player.name.string,
            firstSeen = now,
            lastSeen = now,
            online = online,
            starterPrompted = data.starterPrompted,
            starterLocked = data.starterLocked,
            starterSelected = data.starterSelected,
            starterUuid = data.starterUUID,
            keyItems = data.keyItems.map { it.toString() }.toSet(),
            snapshotAt = now,
        )
    }
}
