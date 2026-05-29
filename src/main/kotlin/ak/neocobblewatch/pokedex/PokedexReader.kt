package ak.neocobblewatch.pokedex

import ak.neocobblewatch.core.assertServerThread
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import net.minecraft.server.level.ServerPlayer

internal object PokedexReader {
    /**
     * Form-level fields (formsEncountered/formsCaught/shinyStates/gendersSeen) are left empty in v1;
     * they require enumerating each Species' forms, which is non-trivial and not worth the complexity
     * before the frontend needs them.
     */
    fun readFor(player: ServerPlayer): PokedexSnapshot {
        assertServerThread(player.server)
        val dex = Cobblemon.playerDataManager.getPokedexData(player.uuid)
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
            playerUuid = player.uuid,
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
