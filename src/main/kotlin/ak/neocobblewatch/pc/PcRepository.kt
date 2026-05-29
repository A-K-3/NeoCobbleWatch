package ak.neocobblewatch.pc

import ak.neocobblewatch.persistence.Database
import ak.neocobblewatch.persistence.queryLong
import ak.neocobblewatch.persistence.setUuid
import ak.neocobblewatch.pokemon.PokemonRow
import java.sql.Connection
import java.sql.ResultSet
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
        val slots = queryRows(
            conn,
            "SELECT * FROM pc_slots WHERE player_uuid = ? ORDER BY box_index, slot_index",
        ) { it.setUuid(1, playerUuid) }
        if (slots.isEmpty()) return null
        return PcSnapshot(
            playerUuid = playerUuid,
            slots = slots,
            snapshotAt = readLatestSnapshotAt(conn, playerUuid),
        )
    }

    fun listByBox(conn: Connection, playerUuid: UUID, boxIndex: Int): List<PcSlotSnapshot> = queryRows(
        conn,
        "SELECT * FROM pc_slots WHERE player_uuid = ? AND box_index = ? ORDER BY slot_index",
    ) {
        it.setUuid(1, playerUuid)
        it.setInt(2, boxIndex)
    }

    fun listPaginated(conn: Connection, playerUuid: UUID, limit: Int, offset: Int): List<PcSlotSnapshot> = queryRows(
        conn,
        "SELECT * FROM pc_slots WHERE player_uuid = ? ORDER BY box_index, slot_index LIMIT ? OFFSET ?",
    ) {
        it.setUuid(1, playerUuid)
        it.setInt(2, limit)
        it.setInt(3, offset)
    }

    fun count(conn: Connection, playerUuid: UUID): Long =
        conn.queryLong("SELECT COUNT(*) FROM pc_slots WHERE player_uuid = ?") { it.setUuid(1, playerUuid) }

    private fun readLatestSnapshotAt(conn: Connection, playerUuid: UUID): Long =
        conn.queryLong("SELECT MAX(snapshot_at) FROM pc_slots WHERE player_uuid = ?") { it.setUuid(1, playerUuid) }

    private inline fun queryRows(
        conn: Connection,
        sql: String,
        bind: (java.sql.PreparedStatement) -> Unit,
    ): List<PcSlotSnapshot> {
        val results = mutableListOf<PcSlotSnapshot>()
        conn.prepareStatement(sql).use { stmt ->
            bind(stmt)
            stmt.executeQuery().use { rs: ResultSet ->
                while (rs.next()) {
                    results += PcSlotSnapshot(
                        boxIndex = rs.getInt("box_index"),
                        slotIndex = rs.getInt("slot_index"),
                        pokemon = PokemonRow.read(rs),
                    )
                }
            }
        }
        return results
    }
}
