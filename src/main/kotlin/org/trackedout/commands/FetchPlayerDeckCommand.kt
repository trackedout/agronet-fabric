package org.trackedout.commands

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.StringNbtReader
import net.minecraft.server.network.ServerPlayerEntity
import org.slf4j.LoggerFactory
import org.trackedout.AgroNet
import org.trackedout.client.apis.InventoryApi
import kotlin.random.Random

class FetchPlayerDeckCommand (
    private val inventoryApi: InventoryApi
) {
    private val logger = LoggerFactory.getLogger("Agro-net")

    private fun isDeckedOutShulker(it: ItemStack) = it.item.name == Items.SHULKER_BOX.name
    private fun PlayerEntity.hasShulkerInInventory() = this.inventory.containsAny(::isDeckedOutShulker)
    fun execute(player: ServerPlayerEntity) {
        logger.debug("Fetch player deck command")

        if (player.hasShulkerInInventory()) {
            logger.info("player already has their deck");
            return
        }

        // TODO: handle if cards come back empty (Default deck?)
        val cards = inventoryApi.inventoryCardsGet(player = player.name.string, limit = 200, deckId = "1").results!!
        logger.debug("Player tags: {}", player.commandTags)

        val shulkerNbt =
            StringNbtReader.parse("{${BlockItem.BLOCK_ENTITY_TAG_KEY}:{Items:[],id:\"minecraft:shulker_box\"}}")
        val blockCompound = shulkerNbt.getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY)
        val shulkerItems = blockCompound["Items"] as NbtList

        val cardCount = cards.groupingBy { it.name!! }.eachCount()
        var cardIndex = 0
        cardCount.forEach { (cardName, count) ->
            logger.info("${player.name.string}'s shulker should contain ${count}x $cardName")

            val nbt = NbtCompound()
            ItemStack(Items.IRON_NUGGET, count).writeNbt(nbt)

            val tag = NbtCompound()
            // TODO: Use cardName to determine model data ID - https://pastebin.com/DxUsy5rb
            tag.putInt("CustomModelData", Random.nextInt(0, 12345))
            nbt.put("tag", tag)
            nbt.putByte("Slot", count.toByte())

            shulkerItems.add(nbt)
        }
        shulkerNbt.put(BlockItem.BLOCK_ENTITY_TAG_KEY, blockCompound)

        val shulkerBox = ItemStack(Items.SHULKER_BOX)
        shulkerBox.nbt = shulkerNbt

        val inventory = player.inventory
        inventory.insertStack(shulkerBox)
        inventory.updateItems()
    }
}