package ak.neocobblewatch.activity

import java.util.UUID

internal data class ActivitySnapshot(
    val playerUuid: UUID,
    // Vanilla MC
    val playTimeTicks: Long,
    val deaths: Int,
    val mobKills: Int,
    val fishCaught: Int,
    val damageDealt: Int,
    val damageTaken: Int,
    val damageBlocked: Int,
    val walkCm: Long,
    val sprintCm: Long,
    val flyCm: Long,
    val swimCm: Long,
    val jumps: Int,
    val sleepInBed: Int,
    val tradedWithVillager: Int,
    val blocksMined: Long,
    val itemsUsed: Long,
    val itemsCrafted: Long,
    val itemsBroken: Long,
    val killsMap: Map<String, Long>,
    val killedByMap: Map<String, Long>,
    // Cobblemon extras (not in advancementData)
    val levelUps: Int,
    val evolutions: Int,
    val timesRidden: Int,
    val ridingLandCm: Long,
    val ridingAirCm: Long,
    val ridingLiquidCm: Long,
    val eggsCollected: Int,
    val eggsHatched: Int,
    val rodCasts: Int,
    val reelIns: Int,
    val released: Int,
    // CobblemonRaidDens
    val raidsHosted: Int,
    val raidsJoined: Int,
    val raidsCompleted: Int,
    val raidTier1: Int,
    val raidTier2: Int,
    val raidTier3: Int,
    val raidTier4: Int,
    val raidTier5: Int,
    // Badges (cobbleversebadges:* items from minecraft:picked_up, excludes _badge_box)
    val badges: List<String>,
    val snapshotAt: Long,
) {
    companion object {
        fun empty(uuid: UUID) = ActivitySnapshot(
            playerUuid = uuid,
            playTimeTicks = 0, deaths = 0, mobKills = 0, fishCaught = 0,
            damageDealt = 0, damageTaken = 0, damageBlocked = 0,
            walkCm = 0, sprintCm = 0, flyCm = 0, swimCm = 0,
            jumps = 0, sleepInBed = 0, tradedWithVillager = 0,
            blocksMined = 0, itemsUsed = 0, itemsCrafted = 0, itemsBroken = 0,
            killsMap = emptyMap(), killedByMap = emptyMap(),
            levelUps = 0, evolutions = 0, timesRidden = 0,
            ridingLandCm = 0, ridingAirCm = 0, ridingLiquidCm = 0,
            eggsCollected = 0, eggsHatched = 0, rodCasts = 0, reelIns = 0, released = 0,
            raidsHosted = 0, raidsJoined = 0, raidsCompleted = 0,
            raidTier1 = 0, raidTier2 = 0, raidTier3 = 0, raidTier4 = 0, raidTier5 = 0,
            badges = emptyList(),
            snapshotAt = System.currentTimeMillis(),
        )
    }
}
