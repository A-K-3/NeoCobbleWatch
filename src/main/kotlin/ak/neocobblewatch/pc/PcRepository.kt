package ak.neocobblewatch.pc

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.setUuid
import ak.neocobblewatch.pokemon.PokemonRow
import java.sql.Connection
import java.util.UUID

internal class PcRepository(private val db: Database) {
    suspend fun replace(snapshot: PcSnapshot) = db.transaction { conn -> replace(conn, snapshot) }
    suspend fun get(playerUuid: UUID): PcSnapshot? = db.withConnection { conn -> get(conn, playerUuid) }

    fun replace(conn: Connection, snapshot: PcSnapshot) {
        conn.prepareStatement("DELETE FROM pc_slots WHERE player_uuid = ?").use {
            it.setUuid(1, snapshot.playerUuid)
            it.executeUpdate()
        }
        if (snapshot.slots.isEmpty()) return
        conn.prepareStatement(
            """
            INSERT INTO pc_slots (player_uuid, box_index, slot_index, ${PokemonRow.COLUMNS}, snapshot_at)
            VALUES (?, ?, ?, ${PokemonRow.PLACEHOLDERS}, ?)
            """.trimIndent(),
        ).use { stmt ->
            for (slot in snapshot.slots) {
                stmt.setUuid(1, snapshot.playerUuid)
                stmt.setInt(2, slot.boxIndex)
                stmt.setInt(3, slot.slotIndex)
                val next = PokemonRow.bind(stmt, startIndex = 4, p = slot.pokemon)
                stmt.setLong(next, snapshot.snapshotAt)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }
    }

    fun get(conn: Connection, playerUuid: UUID): PcSnapshot? {
        val slots = mutableListOf<PcSlotSnapshot>()
        var latestSnapshotAt = 0L
        conn.prepareStatement(
            "SELECT * FROM pc_slots WHERE player_uuid = ? ORDER BY box_index, slot_index",
        ).use { stmt ->
            stmt.setUuid(1, playerUuid)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    slots += PcSlotSnapshot(
                        boxIndex = rs.getInt("box_index"),
                        slotIndex = rs.getInt("slot_index"),
                        pokemon = PokemonRow.read(rs),
                    )
                    latestSnapshotAt = rs.getLong("snapshot_at")
                }
            }
        }
        if (slots.isEmpty()) return null
        return PcSnapshot(playerUuid = playerUuid, slots = slots, snapshotAt = latestSnapshotAt)
    }
}
