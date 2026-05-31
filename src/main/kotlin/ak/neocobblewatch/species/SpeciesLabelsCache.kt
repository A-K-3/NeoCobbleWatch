package ak.neocobblewatch.species

import ak.neocobblewatch.api.dto.SpawnDto
import ak.neocobblewatch.api.dto.SpeciesInfoDto
import ak.neocobblewatch.api.dto.SpeciesLabelsResponse
import java.util.concurrent.atomic.AtomicReference

internal class SpeciesLabelsCache {
    private val lock = Any()
    private val snapshot = AtomicReference<SpeciesLabelsSnapshot?>(null)
    private val response = AtomicReference<SpeciesLabelsResponse?>(null)
    private val spawnsByName = AtomicReference<Map<String, List<SpawnDto>>?>(null)

    fun get(): SpeciesLabelsSnapshot {
        snapshot.get()?.let { return it }
        synchronized(lock) {
            snapshot.get()?.let { return it }
            return SpeciesLabelsReader.readAll().also { snapshot.set(it) }
        }
    }

    /** Cached spawn map keyed by lowercase species name — built once by walking WORLD_SPAWN_POOL. */
    fun spawnsFor(speciesName: String): List<SpawnDto> {
        val cached = spawnsByName.get()
        if (cached != null) return cached[speciesName.lowercase()] ?: emptyList()
        synchronized(lock) {
            spawnsByName.get()?.let { return it[speciesName.lowercase()] ?: emptyList() }
            val map = SpeciesDetailReader.collectAllSpawns()
            spawnsByName.set(map)
            return map[speciesName.lowercase()] ?: emptyList()
        }
    }

    /** Cached DTO-shaped response — converted once per snapshot, served from cache after. */
    fun getResponse(): SpeciesLabelsResponse {
        response.get()?.let { return it }
        synchronized(lock) {
            response.get()?.let { return it }
            val s = get()
            val data = s.species.mapValues { (_, info) ->
                SpeciesInfoDto(
                    nationalDex = info.nationalDex,
                    generation = info.generation,
                    labels = info.labels.toList().sorted(),
                    fullyEvolved = info.fullyEvolved,
                    primaryType = info.primaryType,
                    secondaryType = info.secondaryType,
                )
            }
            return SpeciesLabelsResponse(data = data, snapshotAt = s.snapshotAt).also { response.set(it) }
        }
    }

    fun invalidate() {
        snapshot.set(null)
        response.set(null)
        spawnsByName.set(null)
    }
}
