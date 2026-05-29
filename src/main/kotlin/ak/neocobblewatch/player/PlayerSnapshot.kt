package ak.neocobblewatch.player

import java.util.UUID

internal data class PlayerSnapshot(
    val uuid: UUID,
    val name: String,
    val firstSeen: Long,
    val lastSeen: Long,
    val online: Boolean,
    val snapshotAt: Long,
)
