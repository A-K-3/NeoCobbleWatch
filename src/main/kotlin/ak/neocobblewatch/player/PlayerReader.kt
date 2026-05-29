package ak.neocobblewatch.player

import ak.neocobblewatch.core.assertServerThread
import net.minecraft.server.level.ServerPlayer

internal class PlayerReader {
    /**
     * `firstSeen` is set to now; [PlayerRepository.upsert] keeps the existing value via
     * `ON CONFLICT` so this only materializes on the very first snapshot for a player.
     */
    fun readFor(player: ServerPlayer): PlayerSnapshot {
        assertServerThread(player.server)
        val now = System.currentTimeMillis()
        return PlayerSnapshot(
            uuid = player.uuid,
            name = player.name.string,
            firstSeen = now,
            lastSeen = now,
            online = true,
            snapshotAt = now,
        )
    }
}
