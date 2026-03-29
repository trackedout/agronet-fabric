package org.trackedout.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrillianceArtifact(
    val id: String,
    val emberValue: Int,
    val tag: ArtifactTag,
)

@Serializable
data class ArtifactTag(
    @SerialName("CustomModelData")
    val customModelData: Int,
    val display: ArtifactDisplay,
)

@Serializable
data class ArtifactDisplay(
    @SerialName("Lore")
    val lore: List<String>, // List of JSON strings representing the lore lines

    @SerialName("Name")
    val name: String,
)
