package org.trackedout.data

import kotlinx.serialization.Serializable

@Serializable
data class BrillianceScoreboardDescription(
    val auto: Int,
    val target: String,
    val category: String,
    val description: String,
    val values: Map<String, String>? = mapOf(),
)
