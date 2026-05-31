package ak.neocobblewatch.activity

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.getJson
import ak.neocobblewatch.persistence.getUuid
import ak.neocobblewatch.persistence.setJson
import ak.neocobblewatch.persistence.setUuid
import java.sql.Connection
import java.util.UUID

internal class ActivityRepository(private val db: Database) {
    suspend fun upsert(snapshot: ActivitySnapshot) = db.withConnection { conn -> upsert(conn, snapshot) }
    suspend fun get(playerUuid: UUID): ActivitySnapshot? = db.withConnection { conn -> get(conn, playerUuid) }

    fun upsert(conn: Connection, s: ActivitySnapshot) {
        conn.prepareStatement(
            """
            INSERT INTO player_activity (
                player_uuid,
                play_time_ticks, deaths, mob_kills, fish_caught,
                damage_dealt, damage_taken, damage_blocked,
                walk_cm, sprint_cm, fly_cm, swim_cm,
                jumps, sleep_in_bed, traded_with_villager,
                blocks_mined, items_used, items_crafted, items_broken,
                kills_map, killed_by_map,
                level_ups, evolutions, times_ridden,
                riding_land_cm, riding_air_cm, riding_liquid_cm,
                eggs_collected, eggs_hatched, rod_casts, reel_ins, released,
                raids_hosted, raids_joined, raids_completed,
                raid_tier1, raid_tier2, raid_tier3, raid_tier4, raid_tier5,
                badges, snapshot_at
            ) VALUES (
                ?,
                ?, ?, ?, ?,
                ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?, ?,
                ?, ?, ?,
                ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?
            )
            ON CONFLICT(player_uuid) DO UPDATE SET
                play_time_ticks = excluded.play_time_ticks,
                deaths = excluded.deaths, mob_kills = excluded.mob_kills,
                fish_caught = excluded.fish_caught,
                damage_dealt = excluded.damage_dealt, damage_taken = excluded.damage_taken,
                damage_blocked = excluded.damage_blocked,
                walk_cm = excluded.walk_cm, sprint_cm = excluded.sprint_cm,
                fly_cm = excluded.fly_cm, swim_cm = excluded.swim_cm,
                jumps = excluded.jumps, sleep_in_bed = excluded.sleep_in_bed,
                traded_with_villager = excluded.traded_with_villager,
                blocks_mined = excluded.blocks_mined, items_used = excluded.items_used,
                items_crafted = excluded.items_crafted, items_broken = excluded.items_broken,
                kills_map = excluded.kills_map, killed_by_map = excluded.killed_by_map,
                level_ups = excluded.level_ups, evolutions = excluded.evolutions,
                times_ridden = excluded.times_ridden,
                riding_land_cm = excluded.riding_land_cm, riding_air_cm = excluded.riding_air_cm,
                riding_liquid_cm = excluded.riding_liquid_cm,
                eggs_collected = excluded.eggs_collected, eggs_hatched = excluded.eggs_hatched,
                rod_casts = excluded.rod_casts, reel_ins = excluded.reel_ins,
                released = excluded.released,
                raids_hosted = excluded.raids_hosted, raids_joined = excluded.raids_joined,
                raids_completed = excluded.raids_completed,
                raid_tier1 = excluded.raid_tier1, raid_tier2 = excluded.raid_tier2,
                raid_tier3 = excluded.raid_tier3, raid_tier4 = excluded.raid_tier4,
                raid_tier5 = excluded.raid_tier5,
                badges = excluded.badges, snapshot_at = excluded.snapshot_at
            """.trimIndent(),
        ).use { stmt ->
            stmt.setUuid(1, s.playerUuid)
            stmt.setLong(2, s.playTimeTicks)
            stmt.setInt(3, s.deaths)
            stmt.setInt(4, s.mobKills)
            stmt.setInt(5, s.fishCaught)
            stmt.setInt(6, s.damageDealt)
            stmt.setInt(7, s.damageTaken)
            stmt.setInt(8, s.damageBlocked)
            stmt.setLong(9, s.walkCm)
            stmt.setLong(10, s.sprintCm)
            stmt.setLong(11, s.flyCm)
            stmt.setLong(12, s.swimCm)
            stmt.setInt(13, s.jumps)
            stmt.setInt(14, s.sleepInBed)
            stmt.setInt(15, s.tradedWithVillager)
            stmt.setLong(16, s.blocksMined)
            stmt.setLong(17, s.itemsUsed)
            stmt.setLong(18, s.itemsCrafted)
            stmt.setLong(19, s.itemsBroken)
            stmt.setJson(20, s.killsMap)
            stmt.setJson(21, s.killedByMap)
            stmt.setInt(22, s.levelUps)
            stmt.setInt(23, s.evolutions)
            stmt.setInt(24, s.timesRidden)
            stmt.setLong(25, s.ridingLandCm)
            stmt.setLong(26, s.ridingAirCm)
            stmt.setLong(27, s.ridingLiquidCm)
            stmt.setInt(28, s.eggsCollected)
            stmt.setInt(29, s.eggsHatched)
            stmt.setInt(30, s.rodCasts)
            stmt.setInt(31, s.reelIns)
            stmt.setInt(32, s.released)
            stmt.setInt(33, s.raidsHosted)
            stmt.setInt(34, s.raidsJoined)
            stmt.setInt(35, s.raidsCompleted)
            stmt.setInt(36, s.raidTier1)
            stmt.setInt(37, s.raidTier2)
            stmt.setInt(38, s.raidTier3)
            stmt.setInt(39, s.raidTier4)
            stmt.setInt(40, s.raidTier5)
            stmt.setJson(41, s.badges)
            stmt.setLong(42, s.snapshotAt)
            stmt.executeUpdate()
        }
    }

    fun get(conn: Connection, playerUuid: UUID): ActivitySnapshot? {
        conn.prepareStatement("SELECT * FROM player_activity WHERE player_uuid = ?").use { stmt ->
            stmt.setUuid(1, playerUuid)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) return null
                return ActivitySnapshot(
                    playerUuid = rs.getUuid("player_uuid"),
                    playTimeTicks = rs.getLong("play_time_ticks"),
                    deaths = rs.getInt("deaths"),
                    mobKills = rs.getInt("mob_kills"),
                    fishCaught = rs.getInt("fish_caught"),
                    damageDealt = rs.getInt("damage_dealt"),
                    damageTaken = rs.getInt("damage_taken"),
                    damageBlocked = rs.getInt("damage_blocked"),
                    walkCm = rs.getLong("walk_cm"),
                    sprintCm = rs.getLong("sprint_cm"),
                    flyCm = rs.getLong("fly_cm"),
                    swimCm = rs.getLong("swim_cm"),
                    jumps = rs.getInt("jumps"),
                    sleepInBed = rs.getInt("sleep_in_bed"),
                    tradedWithVillager = rs.getInt("traded_with_villager"),
                    blocksMined = rs.getLong("blocks_mined"),
                    itemsUsed = rs.getLong("items_used"),
                    itemsCrafted = rs.getLong("items_crafted"),
                    itemsBroken = rs.getLong("items_broken"),
                    killsMap = rs.getJson("kills_map"),
                    killedByMap = rs.getJson("killed_by_map"),
                    levelUps = rs.getInt("level_ups"),
                    evolutions = rs.getInt("evolutions"),
                    timesRidden = rs.getInt("times_ridden"),
                    ridingLandCm = rs.getLong("riding_land_cm"),
                    ridingAirCm = rs.getLong("riding_air_cm"),
                    ridingLiquidCm = rs.getLong("riding_liquid_cm"),
                    eggsCollected = rs.getInt("eggs_collected"),
                    eggsHatched = rs.getInt("eggs_hatched"),
                    rodCasts = rs.getInt("rod_casts"),
                    reelIns = rs.getInt("reel_ins"),
                    released = rs.getInt("released"),
                    raidsHosted = rs.getInt("raids_hosted"),
                    raidsJoined = rs.getInt("raids_joined"),
                    raidsCompleted = rs.getInt("raids_completed"),
                    raidTier1 = rs.getInt("raid_tier1"),
                    raidTier2 = rs.getInt("raid_tier2"),
                    raidTier3 = rs.getInt("raid_tier3"),
                    raidTier4 = rs.getInt("raid_tier4"),
                    raidTier5 = rs.getInt("raid_tier5"),
                    badges = rs.getJson("badges"),
                    snapshotAt = rs.getLong("snapshot_at"),
                )
            }
        }
    }
}
