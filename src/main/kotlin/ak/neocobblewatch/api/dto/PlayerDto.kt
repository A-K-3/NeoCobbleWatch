package ak.neocobblewatch.api.dto

import ak.neocobblewatch.player.PlayerSnapshot
import kotlinx.serialization.Serializable

@Serializable
internal data class PlayerSummaryDto(
    val uuid: String,
    val name: String,
    val lastSeen: Long,
    val online: Boolean,
)

@Serializable
internal data class PlayerDto(
    val uuid: String,
    val name: String,
    val firstSeen: Long,
    val lastSeen: Long,
    val online: Boolean,
    val starterPrompted: Boolean,
    val starterLocked: Boolean,
    val starterSelected: Boolean,
    val starterUuid: String?,
    val keyItems: Set<String>,
    val snapshotAt: Long,
)

@Serializable
internal data class PlayerProfileDto(
    val player: PlayerDto,
    val stats: StatsDto,
)

@Serializable
internal data class PlayerListResponse(val data: List<PlayerSummaryDto>, val page: PageInfo)

@Serializable
internal data class PlayerProfileResponse(val data: PlayerProfileDto)

internal fun PlayerSnapshot.toSummaryDto(): PlayerSummaryDto = PlayerSummaryDto(
    uuid = uuid.toString(),
    name = name,
    lastSeen = lastSeen,
    online = online,
)

internal fun PlayerSnapshot.toDto(): PlayerDto = PlayerDto(
    uuid = uuid.toString(),
    name = name,
    firstSeen = firstSeen,
    lastSeen = lastSeen,
    online = online,
    starterPrompted = starterPrompted,
    starterLocked = starterLocked,
    starterSelected = starterSelected,
    starterUuid = starterUuid?.toString(),
    keyItems = keyItems,
    snapshotAt = snapshotAt,
)
