package ak.neocobblewatch.persistence

import kotlinx.serialization.json.Json
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID

internal val SqlJson: Json = Json { ignoreUnknownKeys = true }

internal fun PreparedStatement.setUuid(index: Int, uuid: UUID) = setString(index, uuid.toString())

internal fun PreparedStatement.setBool(index: Int, value: Boolean) = setInt(index, if (value) 1 else 0)

internal inline fun <reified T> PreparedStatement.setJson(index: Int, value: T) =
    setString(index, SqlJson.encodeToString(value))

internal fun ResultSet.getUuid(column: String): UUID = UUID.fromString(getString(column))

internal fun ResultSet.getBool(column: String): Boolean = getInt(column) != 0

internal inline fun <reified T> ResultSet.getJson(column: String): T =
    SqlJson.decodeFromString(getString(column))
