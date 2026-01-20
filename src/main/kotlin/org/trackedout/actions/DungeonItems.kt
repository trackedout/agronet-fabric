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
    },

    "COPPER_BLOCK" to ItemStack(Items.COPPER_BLOCK),
    "EXPOSED_COPPER" to ItemStack(Items.EXPOSED_COPPER),
    "WEATHERED_COPPER" to ItemStack(Items.WEATHERED_COPPER),
    "OXIDIZED_COPPER" to ItemStack(Items.OXIDIZED_COPPER),

    "ICE" to ItemStack(Items.ICE),
    "PACKED_ICE" to ItemStack(Items.PACKED_ICE),
    "BLUE_ICE" to ItemStack(Items.BLUE_ICE),

)

// Tracked in the DB, but we don't actually want to give them to the player in the dungeon
val ignoredItems = setOf("VICTORY_TOME", "SHARD_FRAGMENT")
