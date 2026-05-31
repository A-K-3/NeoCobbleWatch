package ak.neocobblewatch.stats

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.getUuid
import ak.neocobblewatch.persistence.queryLong
import java.sql.Connection
import java.util.UUID

internal enum class TopMetric(val slug: String, val source: Source, val column: String? = null) {
    // ── Pokémon ────────────────────────────────────────────────────────────────
    CAPTURES("captures", Source.STATS, "total_capture_count"),
    SHINIES("shinies", Source.STATS, "total_shiny_capture_count"),
    PVP_WINS("pvp_wins", Source.STATS, "total_pvp_battle_victory_count"),
    TRADES("trades", Source.STATS, "total_traded_count"),
    EGGS_HATCHED("eggs_hatched", Source.STATS, "total_eggs_hatched"),
    POKEDEX_CAUGHT("pokedex_caught", Source.POKEDEX),
    LEVEL_UPS("level_ups", Source.ACTIVITY, "level_ups"),
    EVOLUTIONS("evolutions", Source.ACTIVITY, "evolutions"),
    // ── Minecraft ─────────────────────────────────────────────────────────────
    PLAY_TIME("play_time", Source.ACTIVITY, "play_time_ticks"),
    DISTANCE("distance", Source.ACTIVITY_COMPUTED),
    MOB_KILLS("mob_kills", Source.ACTIVITY, "mob_kills"),
    DAMAGE_DEALT("damage_dealt", Source.ACTIVITY, "damage_dealt"),
    FISH_CAUGHT("fish_caught", Source.ACTIVITY, "fish_caught"),
    RAIDS_COMPLETED("raids_completed", Source.ACTIVITY, "raids_completed"),
    BADGES("badges", Source.ACTIVITY_BADGES),
    DEATHS("deaths", Source.ACTIVITY, "deaths"),
    MONEY("money", Source.ECONOMY, "cobble_dollars"),
    ;

    enum class Source { STATS, POKEDEX, ACTIVITY, ACTIVITY_COMPUTED, ACTIVITY_BADGES, ECONOMY }

    companion object {
        fun fromSlug(slug: String): TopMetric? = entries.firstOrNull { it.slug.equals(slug, ignoreCase = true) }
        val SLUGS: List<String> = entries.map { it.slug }
    }
}

internal data class TopEntry(val uuid: UUID, val name: String, val value: Long)

private const val DISTANCE_EXPR =
    "(pa.walk_cm + pa.sprint_cm + pa.fly_cm + pa.swim_cm + " +
        "pa.riding_land_cm + pa.riding_air_cm + pa.riding_liquid_cm)"

internal class TopsRepository(private val db: Database) {
    suspend fun list(metric: TopMetric, limit: Int, offset: Int): List<TopEntry> =
        db.withConnection { conn -> list(conn, metric, limit, offset) }

    suspend fun count(): Long = db.withConnection { conn -> count(conn) }

    fun list(conn: Connection, metric: TopMetric, limit: Int, offset: Int): List<TopEntry> {
        val sql = when (metric.source) {
            TopMetric.Source.POKEDEX -> """
                SELECT p.uuid AS uuid, p.name AS name,
                       COUNT(CASE WHEN pe.knowledge = 'CAUGHT' THEN 1 END) AS value
                FROM players p
                LEFT JOIN pokedex_entries pe ON pe.player_uuid = p.uuid
                GROUP BY p.uuid, p.name
                ORDER BY value DESC, p.name COLLATE NOCASE ASC
                LIMIT ? OFFSET ?
            """.trimIndent()

            TopMetric.Source.STATS ->
                // metric.column comes from a closed enum — no injection risk.
                """
                SELECT p.uuid AS uuid, p.name AS name, s.${metric.column} AS value
                FROM players p
                JOIN player_stats s ON s.player_uuid = p.uuid
                ORDER BY s.${metric.column} DESC, p.name COLLATE NOCASE ASC
                LIMIT ? OFFSET ?
                """.trimIndent()

            TopMetric.Source.ACTIVITY ->
                """
                SELECT p.uuid AS uuid, p.name AS name, pa.${metric.column} AS value
                FROM players p
                JOIN player_activity pa ON pa.player_uuid = p.uuid
                ORDER BY pa.${metric.column} DESC, p.name COLLATE NOCASE ASC
                LIMIT ? OFFSET ?
                """.trimIndent()

            TopMetric.Source.ACTIVITY_COMPUTED ->
                """
                SELECT p.uuid AS uuid, p.name AS name, $DISTANCE_EXPR AS value
                FROM players p
                JOIN player_activity pa ON pa.player_uuid = p.uuid
                ORDER BY value DESC, p.name COLLATE NOCASE ASC
                LIMIT ? OFFSET ?
                """.trimIndent()

            TopMetric.Source.ACTIVITY_BADGES ->
                """
                SELECT p.uuid AS uuid, p.name AS name,
                       json_array_length(pa.badges) AS value
                FROM players p
                JOIN player_activity pa ON pa.player_uuid = p.uuid
                ORDER BY value DESC, p.name COLLATE NOCASE ASC
                LIMIT ? OFFSET ?
                """.trimIndent()

            TopMetric.Source.ECONOMY ->
                """
                SELECT p.uuid AS uuid, p.name AS name, pe.${metric.column} AS value
                FROM players p
                JOIN player_economy pe ON pe.player_uuid = p.uuid
                ORDER BY pe.${metric.column} DESC, p.name COLLATE NOCASE ASC
                LIMIT ? OFFSET ?
                """.trimIndent()
        }

        val results = mutableListOf<TopEntry>()
        conn.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, limit)
            stmt.setInt(2, offset)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    results += TopEntry(
                        uuid = rs.getUuid("uuid"),
                        name = rs.getString("name"),
                        value = rs.getLong("value"),
                    )
                }
            }
        }
        return results
    }

    fun count(conn: Connection): Long = conn.queryLong("SELECT COUNT(*) FROM players")
}
