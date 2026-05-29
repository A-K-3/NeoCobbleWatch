package ak.neocobblewatch.persistence

import kotlinx.serialization.json.Json
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID

internal val SqlJson: Json = Json { ignoreUnknownKeys = true }

internal fun PreparedStatement.setUuid(index: Int, uuid: UUID) = setString(index, uuid.toString())

internal fun PreparedStatement.setBool(index: Int, value: Boolean) = setInt(index, if (value) 1 else 0)

internal inline fun <reified T> PreparedStatement.setJson(index: Int, value: T) =
    setString(index, SqlJson.encodeToString(value))

internal fun ResultSet.getUuid(column: String): UUID = UUID.fromString(getString(column))

internal fun ResultSet.getUuidOrNull(column: String): UUID? = getString(column)?.let(UUID::fromString)

internal fun ResultSet.getBool(column: String): Boolean = getInt(column) != 0

internal inline fun <reified T> ResultSet.getJson(column: String): T =
    SqlJson.decodeFromString(getString(column))

/** Runs a parameterized query that returns a single LONG column. Returns 0 if no row. */
internal inline fun Connection.queryLong(sql: String, bind: (PreparedStatement) -> Unit = {}): Long {
    prepareStatement(sql).use { stmt ->
        bind(stmt)
        stmt.executeQuery().use { rs ->
            return if (rs.next()) rs.getLong(1) else 0L
        }
    }
}
