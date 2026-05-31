package ak.neocobblewatch.advancements

import ak.neocobblewatch.Neocobblewatch
import ak.neocobblewatch.persistence.SqlJson
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.readText

internal class AdvancementsFileReader(private val advancementsDir: Path) {

    fun readFor(uuid: UUID): AdvancementsSnapshot {
        val file = advancementsDir.resolve("$uuid.json")
        return try {
            parse(uuid, file.readText())
        } catch (_: NoSuchFileException) {
            AdvancementsSnapshot.empty(uuid)
        } catch (e: Exception) {
            Neocobblewatch.LOGGER.warn("Failed to parse advancements for {}: {}", uuid, e.message)
            AdvancementsSnapshot.empty(uuid)
        }
    }

    private fun parse(uuid: UUID, text: String): AdvancementsSnapshot {
        val root = SqlJson.parseToJsonElement(text).jsonObject
        val completed = root.entries
            .filter { (key, v) ->
                v is JsonObject &&
                v["done"]?.jsonPrimitive?.booleanOrNull == true &&
                !key.substringAfter(':').startsWith("recipes/")
            }
            .map { it.key }
            .sorted()
        return AdvancementsSnapshot(uuid, completed, System.currentTimeMillis())
    }
}
