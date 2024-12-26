package org.trackedout.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
  Example:
  {
    "shorthand": "MOC",
    "id": "minecraft:iron_nugget",
    "maxCopies": 5,
    "tag": {
      "NameFormat": {
        "color": "gray",
        "OriginalName": "'{\"color\":\"gray\",\"text\":\"✲ Moment of Clarity ✲\"}'",
        "ModifiedName": "'{\"color\":\"gray\",\"text\":\"✲ Moment of Clarity ✲\"}'"
      },
      "CustomRoleplayData": "1b",
      "CustomModelData": 106,
      "display": {
        "Name": "'{\"color\":\"gray\",\"text\":\"✲ Moment of Clarity ✲\"}'"
      },
      "tracked": "0b"
    }
  }
 */

@Serializable
data class BrillianceCard(
    val shorthand: String,
    val id: String,
    val tag: Tag,
    val maxCopies: Int? = null,
)

@Serializable
data class Tag(
    @SerialName("NameFormat")
    val nameFormat: NameFormat? = null,

    @SerialName("CustomRoleplayData")
    val customRoleplayData: String? = null,

    @SerialName("CustomModelData")
    val customModelData: Long? = null,

    val display: Display,
    val tracked: String? = null,
)

@Serializable
data class Display(
    @SerialName("Name")
    val name: String? = null,
)

@Serializable
data class NameFormat(
    val color: String? = null,

    @SerialName("OriginalName")
    val originalName: String? = null,

    @SerialName("ModifiedName")
    val modifiedName: String? = null,
)
