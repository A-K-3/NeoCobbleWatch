package ak.neocobblewatch.party

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.setUuid
import ak.neocobblewatch.pokemon.PokemonRow
import java.sql.Connection
import java.util.UUID

internal class PartyRepository(private val db: Database) {
    suspend fun replace(snapshot: PartySnapshot) = db.transaction { conn -> replace(conn, snapshot) }
    suspend fun get(playerUuid: UUID): PartySnapshot? = db.withConnection { conn -> get(conn, playerUuid) }

    fun replace(conn: Connection, snapshot: PartySnapshot) {
        conn.prepareStatement("DELETE FROM party_slots WHERE player_uuid = ?").use {
            it.setUuid(1, snapshot.playerUuid)
            it.executeUpdate()
        }
        if (snapshot.slots.isEmpty()) return
        conn.prepareStatement(
            """
            INSERT INTO party_slots (player_uuid, slot_index, ${PokemonRow.COLUMNS}, snapshot_at)
            VALUES (?, ?, ${PokemonRow.PLACEHOLDERS}, ?)
            """.trimIndent(),
        ).use { stmt ->
            for (slot in snapshot.slots) {
                stmt.setUuid(1, snapshot.playerUuid)
                stmt.setInt(2, slot.slotIndex)
                val next = PokemonRow.bind(stmt, startIndex = 3, p = slot.pokemon)
                stmt.setLong(next, snapshot.snapshotAt)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }
    }

    fun get(conn: Connection, playerUuid: UUID): PartySnapshot? {
        val slots = mutableListOf<PartySlotSnapshot>()
        var latestSnapshotAt = 0L
        conn.prepareStatement(
            "SELECT * FROM party_slots WHERE player_uuid = ? ORDER BY slot_index",
        ).use { stmt ->
            stmt.setUuid(1, playerUuid)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    slots += PartySlotSnapshot(
                        slotIndex = rs.getInt("slot_index"),
                        pokemon = PokemonRow.read(rs),
                    )
                    latestSnapshotAt = rs.getLong("snapshot_at")
                }
            }
        }
        if (slots.isEmpty()) return null
        return PartySnapshot(playerUuid = playerUuid, slots = slots, snapshotAt = latestSnapshotAt)
    }
}
