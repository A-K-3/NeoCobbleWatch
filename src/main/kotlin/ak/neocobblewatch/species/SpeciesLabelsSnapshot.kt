package ak.neocobblewatch.species

internal data class SpeciesInfo(
    val id: String,
    val nationalDex: Int,
    val generation: Int,
    val labels: Set<String>,
    val fullyEvolved: Boolean,
    val primaryType: String,
    val secondaryType: String?,
)

internal data class SpeciesLabelsSnapshot(
    val species: Map<String, SpeciesInfo>,
    val snapshotAt: Long,
)
