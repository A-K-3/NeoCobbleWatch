package ak.neocobblewatch.player

import java.util.UUID

internal data class PlayerSnapshot(
    val uuid: UUID,
    val name: String,
    val firstSeen: Long,
    val lastSeen: Long,
    val online: Boolean,
    val starterPrompted: Boolean,
    val starterLocked: Boolean,
    val starterSelected: Boolean,
    val starterUuid: UUID?,
    val keyItems: Set<String>,
    val snapshotAt: Long,
)
