package ak.neocobblewatch.economy

import java.util.UUID

internal data class EconomySnapshot(
    val playerUuid: UUID,
    val cobbleDollars: Long,
    val snapshotAt: Long,
) {
    companion object {
        fun empty(uuid: UUID) = EconomySnapshot(uuid, 0L, System.currentTimeMillis())
    }
}
