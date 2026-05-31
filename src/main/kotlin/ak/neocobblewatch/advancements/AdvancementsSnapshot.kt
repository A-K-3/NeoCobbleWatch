package ak.neocobblewatch.advancements

import java.util.UUID

internal data class AdvancementsSnapshot(
    val playerUuid: UUID,
    val completed: List<String>,
    val snapshotAt: Long,
) {
    companion object {
        fun empty(uuid: UUID) = AdvancementsSnapshot(uuid, emptyList(), System.currentTimeMillis())
    }
}
