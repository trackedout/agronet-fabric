package org.trackedout.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
  Example:
  {
    "moment_of_clarity": {
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
      },
      "name": "✲ Moment of Clarity ✲",
      "lore": [
        "{\"bold\":true,\"italic\":false,\"color\":\"#F9FFFE\",\"text\":\"-----\"}",
        "{\"italic\":true,\"color\":\"gray\",\"text\":\"Common\"}",
        "{\"color\":\"light_purple\",\"text\":\"Ethereal\"}",
        "{\"extra\":[{\"color\":\"#9D9D97\",\"text\":\"Limit: \"},{\"bold\":false,\"italic\":false,\"color\":\"#F9FFFE\",\"text\":\"4\"}],\"text\":\"\"}",
        "{\"bold\":true,\"italic\":false,\"color\":\"#F9FFFE\",\"text\":\"-----\"}",
        "{\"extra\":[{\"italic\":false,\"color\":\"gray\",\"text\":\"Block 2 \"},{\"bold\":true,\"italic\":false,\"color\":\"#169C9C\",\"text\":\"💥\"}],\"text\":\"\"}",
        "{\"extra\":[{\"italic\":false,\"color\":\"gray\",\"text\":\"Block 2 \"},{\"bold\":false,\"italic\":false,\"color\":\"dark_red\",\"text\":\"⚠\"}],\"text\":\"\"}",
        "{\"extra\":[{\"italic\":false,\"color\":\"#9D9D97\",\"text\":\"+ 4 \"},{\"bold\":true,\"italic\":false,\"color\":\"#FED83D\",\"text\":\"🪙\"}],\"text\":\"\"}",
        "{\"extra\":[{\"italic\":false,\"color\":\"#9D9D97\",\"text\":\"+ 2 \"},{\"bold\":false,\"italic\":false,\"color\":\"aqua\",\"text\":\"🔥\"}],\"text\":\"\"}"
      ]
    }
  }
 */

@Serializable
data class BrillianceCard(
    val shorthand: String,
    val name: String,
    val id: String,
    val tag: Tag,
    val tagRaw: String? = null, // Raw json string of tag
    val maxCopies: Int? = null,
    val emberValue: Int? = null,

    @SerialName("ethereal")
    val isEthereal: Boolean = false,

    val lore: List<String>? = listOf(),
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
    val name: String,
)

@Serializable
data class NameFormat(
    val color: String? = null,

    @SerialName("OriginalName")
    val originalName: String? = null,

    @SerialName("ModifiedName")
    val modifiedName: String? = null,
)
