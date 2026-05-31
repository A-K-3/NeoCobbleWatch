package ak.neocobblewatch.pokedex

import ak.neocobblewatch.core.assertServerThread
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

internal object PokedexReader {
    /**
     * Form-level fields (formsEncountered/formsCaught/shinyStates/gendersSeen) are left empty in v1;
     * they require enumerating each Species' forms, which is non-trivial and not worth the complexity
     * before the frontend needs them.
     */
    fun readFor(player: ServerPlayer): PokedexSnapshot = readFor(player.server, player.uuid)

    fun readFor(server: MinecraftServer, uuid: UUID): PokedexSnapshot {
        assertServerThread(server)
        val dex = Cobblemon.playerDataManager.getPokedexData(uuid)
        val entries = dex.speciesRecords.map { (speciesId, record) ->
            PokedexEntrySnapshot(
                speciesId = speciesId.toString(),
                knowledge = mapKnowledge(record.getKnowledge()),
                formsEncountered = emptySet(),
                formsCaught = emptySet(),
                shinyStates = emptySet(),
                gendersSeen = emptySet(),
                aspectsSeen = record.getAspects().toSet(),
            )
        }
        return PokedexSnapshot(
            playerUuid = uuid,
            entries = entries,
            snapshotAt = System.currentTimeMillis(),
        )
    }

    private fun mapKnowledge(progress: PokedexEntryProgress): PokedexKnowledge = when (progress) {
        PokedexEntryProgress.NONE -> PokedexKnowledge.NONE
        PokedexEntryProgress.ENCOUNTERED -> PokedexKnowledge.ENCOUNTERED
        PokedexEntryProgress.CAUGHT -> PokedexKnowledge.CAUGHT
    }
}
