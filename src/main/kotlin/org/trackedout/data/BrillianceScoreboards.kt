package org.trackedout.data

import kotlinx.serialization.Serializable

@Serializable
data class BrillianceScoreboardDescription(
    val auto: Int,
    val target: String,
    val category: String,
    val displayText: String? = null,
    val description: String,
    val canEditForRunTypes: List<String>? = null,
    val values: Map<String, ScoreboardValue>? = mapOf(),
)

@Serializable
data class ScoreboardValue(
    val title: String? = null,
    val description: String? = null,
)
