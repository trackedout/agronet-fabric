package org.trackedout.data

/**
 * DEPRECATED / LEGACY — This file contains hardcoded run-type definitions that are being replaced
 * by dynamic league data fetched from dunga-dunga (via LeagueCache in citadel).
 *
 * In agronet-fabric, the claim metadata already carries run-type information via PlayerContext,
 * so usages here should be migrated to use claim-provided data instead of this static lookup.
 *
 * Do NOT add new usages of [runTypes], [getRunTypeById], [findRunTypeById], or [unknownRunType].
 * This file will be removed once all consumers have been migrated.
 *
 * See: citadel's LeagueCache and effectiveRunTypes() for the dynamic replacement.
 */

data class RunType(
    val shortId: Char,
    val longId: String,        // e.g. 'c' for competitive, 'p' for practice
    val displayName: String,   // e.g. "competitive" for competitive, "practice" for practice
    val runTypeId: Int,        // e.g. "Competitive" for competitive, "Practice" for practice
    val deckMaterial: String,  // e.g. "CYAN_SHULKER_BOX" for competitive, "LIME_SHULKER_BOX" for practice
    val displayColour: Int,    // e.g. 0x55ffff = AQUA for competitive, 0x55ff55 = GREEN for practice
) {
    fun deckType(): String {
        return shortId.toString()
    }
}

@Deprecated("Use dynamic league data from dunga-dunga API instead. Run-type is available via claim metadata / PlayerContext.")
val runTypes = listOf(
    RunType('p', "practice", "Practice", 1, "LIME_SHULKER_BOX", 0x55ff55), // Green
    RunType('c', "competitive", "Competitive", 2, "CYAN_SHULKER_BOX", 0x55ffff), // Aqua
    RunType('h', "hardcore", "Hardcore", 3, "RED_SHULKER_BOX", 0xff5555), // Red
)

@Deprecated("Use dynamic league data from dunga-dunga API instead. Run-type is available via claim metadata / PlayerContext.")
val unknownRunType = RunType('u', "unknown", "Unknown", 99, "GRAY_SHULKER_BOX", 0x555555) // Grey

@Deprecated("Use dynamic league data from dunga-dunga API instead. Run-type is available via claim metadata / PlayerContext.")
fun getRunTypeById(id: String): RunType {
    return findRunTypeById(id) ?: unknownRunType
}

@Deprecated("Use dynamic league data from dunga-dunga API instead. Run-type is available via claim metadata / PlayerContext.")
fun findRunTypeById(id: String): RunType? {
    if (id.isEmpty()) {
        return null
    }
    return runTypes.find { it.shortId == id[0] || it.longId == id || it.runTypeId == id.toIntOrNull() }
}
