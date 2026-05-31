package ak.neocobblewatch.stats

import ak.neocobblewatch.core.assertServerThread
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.types.ElementalTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

internal object StatsReader {
    fun readFor(player: ServerPlayer): StatsSnapshot = readFor(player.server, player.uuid)

    fun readFor(server: MinecraftServer, uuid: UUID): StatsSnapshot {
        assertServerThread(server)
        val data = Cobblemon.playerDataManager.getGenericData(uuid).advancementData
        return StatsSnapshot(
            playerUuid = uuid,
            totalCaptureCount = data.totalCaptureCount,
            totalShinyCaptureCount = data.totalShinyCaptureCount,
            totalEggsCollected = data.totalEggsCollected,
            totalEggsHatched = data.totalEggsHatched,
            totalEvolvedCount = data.totalEvolvedCount,
            totalBattleVictoryCount = data.totalBattleVictoryCount,
            totalPvpBattleVictoryCount = data.totalPvPBattleVictoryCount,
            totalPvwBattleVictoryCount = data.totalPvWBattleVictoryCount,
            totalPvnBattleVictoryCount = data.totalPvNBattleVictoryCount,
            totalTradedCount = data.totalTradedCount,
            typeCaptureCounts = ElementalTypes.all()
                .associate { it.showdownId to data.getTotalTypeCaptureCount(it) }
                .filterValues { it > 0 },
            // Cobblemon does not expose `totalDefeatedCounts` publicly; populated in v2 via BATTLE_FAINTED.
            defeatedCounts = emptyMap(),
            aspectsCollected = data.aspectsCollected.entries.associate { (species, aspects) ->
                species.toString() to aspects.toSet()
            },
            snapshotAt = System.currentTimeMillis(),
        )
    }
}
