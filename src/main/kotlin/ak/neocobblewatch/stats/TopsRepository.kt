package ak.neocobblewatch.stats

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.getUuid
import ak.neocobblewatch.persistence.queryLong
import java.sql.Connection
import java.util.UUID

internal enum class TopMetric(val slug: String, val column: String?) {
    CAPTURES("captures", "total_capture_count"),
    SHINIES("shinies", "total_shiny_capture_count"),
    PVP_WINS("pvp_wins", "total_pvp_battle_victory_count"),
    TRADES("trades", "total_traded_count"),
    EGGS_HATCHED("eggs_hatched", "total_eggs_hatched"),

    /** Derived from `pokedex_entries`; SQL handled specially in [TopsRepository.list]. */
    POKEDEX_CAUGHT("pokedex_caught", null),
    ;

    companion object {
        fun fromSlug(slug: String): TopMetric? = entries.firstOrNull { it.slug.equals(slug, ignoreCase = true) }

        val SLUGS: List<String> = entries.map { it.slug }
    }
}

internal data class TopEntry(val uuid: UUID, val name: String, val value: Long)

internal class TopsRepository(private val db: Database) {
    suspend fun list(metric: TopMetric, limit: Int, offset: Int): List<TopEntry> =
        db.withConnection { conn -> list(conn, metric, limit, offset) }

    suspend fun count(): Long = db.withConnection { conn -> count(conn) }

    fun list(conn: Connection, metric: TopMetric, limit: Int, offset: Int): List<TopEntry> {
        val sql = if (metric == TopMetric.POKEDEX_CAUGHT) {
            """
            SELECT p.uuid AS uuid, p.name AS name,
                   COUNT(CASE WHEN pe.knowledge = 'CAUGHT' THEN 1 END) AS value
            FROM players p
            LEFT JOIN pokedex_entries pe ON pe.player_uuid = p.uuid
            GROUP BY p.uuid, p.name
            ORDER BY value DESC, p.name COLLATE NOCASE ASC
            LIMIT ? OFFSET ?
            """.trimIndent()
        } else {
            // metric.column comes from a closed enum, no SQL injection risk.
            """
            SELECT p.uuid AS uuid, p.name AS name, s.${metric.column} AS value
            FROM players p
            JOIN player_stats s ON s.player_uuid = p.uuid
            ORDER BY s.${metric.column} DESC, p.name COLLATE NOCASE ASC
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
