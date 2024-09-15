package org.trackedout.actions

import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.StringNbtReader

val dungeonItemsForGiveCommand = mapOf(

    "SLOWNESS_POTION" to ItemStack(Items.SPLASH_POTION).also {
        it.nbt = StringNbtReader.parse(
            """
                { "Potion": "minecraft:strong_slowness" }
            """.trimIndent()
        )
    },

    "CAVES_OF_CARNAGE_KEY" to ItemStack(Items.IRON_NUGGET).also {
        it.nbt = StringNbtReader.parse(
            """
                {
                  "tracked": 1b,
                  "CustomModelData": 201,
                  "display": {
                    "Name": "{\"text\":\"❄☠ The Caves of Carnage Key ☠❄\"}"
                  }
                }
            """.trimIndent()
        )
    },

    "BLACK_MINES_KEY" to ItemStack(Items.IRON_NUGGET).also {
        it.nbt = StringNbtReader.parse(
            """
                {
                  "tracked": 1b,
                  "CustomModelData": 203,
                  "display": {
                    "Name": "{\"text\":\"❄☠ The Black Mines Key ☠❄\"}"
                  }
                }
            """.trimIndent()
        )
    },

    "BURNING_DARK_KEY" to ItemStack(Items.IRON_NUGGET).also {
        it.nbt = StringNbtReader.parse(
            """
                {
                  "tracked": 1b,
                  "CustomModelData": 209,
                  "display": {
                    "Name": "{\"text\":\"❄☠ The Burning Dark Key ☠❄\"}"
                  }
                }
            """.trimIndent()
        )
    },

    "COIN" to ItemStack(Items.IRON_NUGGET).also {
        it.nbt = StringNbtReader.parse(
            """
                {
                  "tracked": 1b,
                  "CustomModelData": 1,
                  "display": {
                    "Name": "{\"text\":\"❄☠ Decked Out Coin ☠❄\"}"
                  }
                }
            """.trimIndent()
        )
    },

    "CROWN" to ItemStack(Items.IRON_NUGGET).also {
        it.nbt = StringNbtReader.parse(
            """
                {
                  "tracked": 1b,
                  "CustomModelData": 2,
                  "display": {
                    "Name": "{\"text\":\"❄☠ Decked Out Crown ☠❄\"}"
                  }
                }
            """.trimIndent()
        )
    },

    "RUSTY_REPAIR_KIT" to ItemStack(Items.IRON_INGOT).also {
        it.nbt = StringNbtReader.parse(
            """
                {
                  "display": {
                    "Name": "[{\"text\":\"❄☠ Rusty Repair Kit ☠❄\"}]"
                  },
                  "CustomModelData": 2
                }
            """.trimIndent()
        )
    }

)

/*

{ "Potion": "minecraft:strong_slowness" }

{
  "tracked": 1b,
  "CustomModelData": 201,
  "display": {
    "Name": "{\"text\":\"❄☠ The Caves of Carnage Key ☠❄\"}"
  }
}

{
  "tracked": 1b,
  "CustomModelData": 203,
  "display": {
    "Name": "{\"text\":\"❄☠ The Black Mines Key ☠❄\"}"
  }
}

{
  "tracked": 1b,
  "CustomModelData": 209,
  "display": {
    "Name": "{\"text\":\"❄☠ The Burning Dark Key ☠❄\"}"
  }
}

{
  "tracked": 1b,
  "CustomModelData": 1,
  "display": {
    "Name": "{\"text\":\"❄☠ Decked Out Coin ☠❄\"}"
  }
}

{
  "tracked": 1b,
  "CustomModelData": 2,
  "display": {
    "Name": "{\"text\":\"❄☠ Decked Out Crown ☠❄\"}"
  }
}

{
  "display": {
    "Name": "[{\"text\":\"❄☠ Rusty Repair Kit ☠❄\"}]"
  },
  "CustomModelData": 2
}

 */
