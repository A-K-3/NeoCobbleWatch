package ak.neocobblewatch.activity

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.persistence.SqlJson
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.readText

internal class ActivityFileReader(private val statsDir: Path) {

    fun readFor(uuid: UUID): ActivitySnapshot {
        val file = statsDir.resolve("$uuid.json")
        return try {
            parse(uuid, file.readText())
        } catch (_: NoSuchFileException) {
            ActivitySnapshot.empty(uuid)
        } catch (e: Exception) {
            Neocobblewatch.LOGGER.warn("Failed to parse activity stats for {}: {}", uuid, e.message)
            ActivitySnapshot.empty(uuid)
        }
    }

    private fun parse(uuid: UUID, text: String): ActivitySnapshot {
        val root = SqlJson.parseToJsonElement(text).jsonObject
        val stats = root["stats"]?.jsonObject ?: return ActivitySnapshot.empty(uuid)
        val custom = stats["minecraft:custom"]?.jsonObject?.mapToLongMap() ?: emptyMap()
        val pickedUpKeys = stats["minecraft:picked_up"]?.jsonObject?.keys ?: emptySet()
        val blocksMined = stats["minecraft:mined"]?.jsonObject?.mapToLongMap()?.values?.sum() ?: 0L
        val itemsUsed = stats["minecraft:used"]?.jsonObject?.mapToLongMap()?.values?.sum() ?: 0L
        val itemsCrafted = stats["minecraft:crafted"]?.jsonObject?.mapToLongMap()?.values?.sum() ?: 0L
        val itemsBroken = stats["minecraft:broken"]?.jsonObject?.mapToLongMap()?.values?.sum() ?: 0L
        val killsMap = stats["minecraft:killed"]?.jsonObject?.mapToLongMap() ?: emptyMap()
        val killedByMap = stats["minecraft:killed_by"]?.jsonObject?.mapToLongMap() ?: emptyMap()
        val badges = pickedUpKeys
            .filter { it.startsWith("cobbleversebadges:") && !it.endsWith("_badge_box") }
            .sorted()

        return ActivitySnapshot(
            playerUuid = uuid,
            playTimeTicks = custom["minecraft:play_time"] ?: 0L,
            deaths = (custom["minecraft:deaths"] ?: 0L).toInt(),
            mobKills = (custom["minecraft:mob_kills"] ?: 0L).toInt(),
            fishCaught = (custom["minecraft:fish_caught"] ?: 0L).toInt(),
            damageDealt = (custom["minecraft:damage_dealt"] ?: 0L).toInt(),
            damageTaken = (custom["minecraft:damage_taken"] ?: 0L).toInt(),
            damageBlocked = (custom["minecraft:damage_blocked_by_shield"] ?: 0L).toInt(),
            walkCm = custom["minecraft:walk_one_cm"] ?: 0L,
            sprintCm = custom["minecraft:sprint_one_cm"] ?: 0L,
            flyCm = custom["minecraft:fly_one_cm"] ?: 0L,
            swimCm = custom["minecraft:swim_one_cm"] ?: 0L,
            jumps = (custom["minecraft:jump"] ?: 0L).toInt(),
            sleepInBed = (custom["minecraft:sleep_in_bed"] ?: 0L).toInt(),
            tradedWithVillager = (custom["minecraft:traded_with_villager"] ?: 0L).toInt(),
            blocksMined = blocksMined,
            itemsUsed = itemsUsed,
            itemsCrafted = itemsCrafted,
            itemsBroken = itemsBroken,
            killsMap = killsMap,
            killedByMap = killedByMap,
            levelUps = (custom["cobblemon:level_up"] ?: 0L).toInt(),
            evolutions = (custom["cobblemon:evolved"] ?: 0L).toInt(),
            timesRidden = (custom["cobblemon:times_ridden"] ?: 0L).toInt(),
            ridingLandCm = custom["cobblemon:riding_land"] ?: 0L,
            ridingAirCm = custom["cobblemon:riding_air"] ?: 0L,
            ridingLiquidCm = custom["cobblemon:riding_liquid"] ?: 0L,
            eggsCollected = (custom["cobblemon:eggs_collected"] ?: 0L).toInt(),
            eggsHatched = (custom["cobblemon:eggs_hatched"] ?: 0L).toInt(),
            rodCasts = (custom["cobblemon:rod_casts"] ?: 0L).toInt(),
            reelIns = (custom["cobblemon:reel_ins"] ?: 0L).toInt(),
            released = (custom["cobblemon:released"] ?: 0L).toInt(),
            raidsHosted = (custom["cobblemonraiddens:raids_hosted"] ?: 0L).toInt(),
            raidsJoined = (custom["cobblemonraiddens:raids_joined"] ?: 0L).toInt(),
            raidsCompleted = (custom["cobblemonraiddens:raids_completed"] ?: 0L).toInt(),
            raidTier1 = (custom["cobblemonraiddens:tier_one_completed"] ?: 0L).toInt(),
            raidTier2 = (custom["cobblemonraiddens:tier_two_completed"] ?: 0L).toInt(),
            raidTier3 = (custom["cobblemonraiddens:tier_three_completed"] ?: 0L).toInt(),
            raidTier4 = (custom["cobblemonraiddens:tier_four_completed"] ?: 0L).toInt(),
            raidTier5 = (custom["cobblemonraiddens:tier_five_completed"] ?: 0L).toInt(),
            badges = badges,
            snapshotAt = System.currentTimeMillis(),
        )
    }
}

private fun JsonObject.mapToLongMap(): Map<String, Long> =
    entries.mapNotNull { (k, v) -> v.jsonPrimitive.longOrNull?.let { k to it } }.toMap()
