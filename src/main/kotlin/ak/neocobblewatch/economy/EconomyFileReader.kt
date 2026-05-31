package ak.neocobblewatch.economy

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.persistence.SqlJson
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.readText

internal class EconomyFileReader(private val dataDir: Path) {

    fun readFor(uuid: UUID): EconomySnapshot {
        val file = dataDir.resolve("$uuid.json")
        return try {
            val root = SqlJson.parseToJsonElement(file.readText()).jsonObject
            val balance = root["cobbledollars"]?.jsonPrimitive?.longOrNull ?: 0L
            EconomySnapshot(uuid, balance, System.currentTimeMillis())
        } catch (_: NoSuchFileException) {
            EconomySnapshot.empty(uuid)
        } catch (e: Exception) {
            Neocobblewatch.LOGGER.warn("Failed to parse economy data for {}: {}", uuid, e.message)
            EconomySnapshot.empty(uuid)
        }
    }
}
