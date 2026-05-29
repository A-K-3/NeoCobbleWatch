package ak.neocobblewatch.pokedex

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.getJson
import ak.neocobblewatch.persistence.setJson
import ak.neocobblewatch.persistence.setUuid
import java.sql.Connection
import java.util.UUID

internal class PokedexRepository(private val db: Database) {
    suspend fun replace(snapshot: PokedexSnapshot) = db.transaction { conn -> replace(conn, snapshot) }
    suspend fun get(playerUuid: UUID): PokedexSnapshot? = db.withConnection { conn -> get(conn, playerUuid) }

    fun replace(conn: Connection, snapshot: PokedexSnapshot) {
        conn.prepareStatement("DELETE FROM pokedex_entries WHERE player_uuid = ?").use {
            it.setUuid(1, snapshot.playerUuid)
            it.executeUpdate()
        }
        if (snapshot.entries.isEmpty()) return
        conn.prepareStatement(
            """
            INSERT INTO pokedex_entries (
                player_uuid, species_id, knowledge,
                forms_encountered, forms_caught, shiny_states, genders_seen, aspects_seen,
                snapshot_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { stmt ->
            for (entry in snapshot.entries) {
                stmt.setUuid(1, snapshot.playerUuid)
                stmt.setString(2, entry.speciesId)
                stmt.setString(3, entry.knowledge.name)
                stmt.setJson(4, entry.formsEncountered)
                stmt.setJson(5, entry.formsCaught)
                stmt.setJson(6, entry.shinyStates)
                stmt.setJson(7, entry.gendersSeen)
                stmt.setJson(8, entry.aspectsSeen)
                stmt.setLong(9, snapshot.snapshotAt)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }
    }

    fun get(conn: Connection, playerUuid: UUID): PokedexSnapshot? {
        val entries = mutableListOf<PokedexEntrySnapshot>()
        var latestSnapshotAt = 0L
        conn.prepareStatement(
            "SELECT * FROM pokedex_entries WHERE player_uuid = ? ORDER BY species_id",
        ).use { stmt ->
            stmt.setUuid(1, playerUuid)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    entries += PokedexEntrySnapshot(
                        speciesId = rs.getString("species_id"),
                        knowledge = PokedexKnowledge.valueOf(rs.getString("knowledge")),
                        formsEncountered = rs.getJson("forms_encountered"),
                        formsCaught = rs.getJson("forms_caught"),
                        shinyStates = rs.getJson("shiny_states"),
                        gendersSeen = rs.getJson("genders_seen"),
                        aspectsSeen = rs.getJson("aspects_seen"),
                    )
                    latestSnapshotAt = rs.getLong("snapshot_at")
                }
            }
        }
        if (entries.isEmpty()) return null
        return PokedexSnapshot(playerUuid = playerUuid, entries = entries, snapshotAt = latestSnapshotAt)
    }
}
