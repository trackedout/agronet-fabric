package org.trackedout.data

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

val runTypes = listOf(
    RunType('p', "practice", "Practice", 1, "LIME_SHULKER_BOX", 0x55ff55), // Green
    RunType('c', "competitive", "Competitive", 2, "CYAN_SHULKER_BOX", 0x55ffff), // Aqua
    RunType('h', "hardcore", "Hardcore", 3, "RED_SHULKER_BOX", 0xff5555), // Red
)

val unknownRunType = RunType('u', "unknown", "Unknown", 99, "GRAY_SHULKER_BOX", 0x555555) // Grey

fun getRunTypeById(id: String): RunType {
    return findRunTypeById(id) ?: unknownRunType
}

fun findRunTypeById(id: String): RunType? {
    if (id.isEmpty()) {
        return null
    }
    return runTypes.find { it.shortId == id[0] || it.longId == id || it.runTypeId == id.toIntOrNull() }
}
