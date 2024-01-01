package org.trackedout.actions

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.StringNbtReader
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import org.slf4j.LoggerFactory
import org.trackedout.AgroNet
import org.trackedout.client.apis.InventoryApi
import org.trackedout.data.Cards
import org.trackedout.debug
import org.trackedout.sendMessage
import kotlin.random.Random

class AddDeckToPlayerInventoryAction (
    private val inventoryApi: InventoryApi
) {
    private val logger = LoggerFactory.getLogger("Agro-net")

    fun execute(source: ServerCommandSource, player: ServerPlayerEntity) {
        logger.debug("Fetch player deck command")

        player.debug("Player tags: {}".format(player.commandTags))
        logger.debug("Player tags: {}", player.commandTags)
        if (player.commandTags.contains(AgroNet.RECEIVED_SHULKER)) {
            source.sendMessage("Player ${player.name.string} already has their shulker box, refusing to give them another one", Formatting.RED)
            player.debug("You already have your shulker box, refusing to give you another one")
            logger.warn("Player ${player.name.string} already has their shulker box, refusing to give them another one")
            return
        }

        player.sendMessage("Fetching your Decked Out shulker from Dunga Dunga...", Formatting.GRAY)
        val cards = inventoryApi.inventoryCardsGet(player = player.name.string, limit = 200, deckId = "1").results!!

        val shulkerNbt =
            StringNbtReader.parse("{${BlockItem.BLOCK_ENTITY_TAG_KEY}:{Items:[],id:\"minecraft:cyan_shulker_box\"}}")
        val blockCompound = shulkerNbt.getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY)
        val shulkerItems = blockCompound["Items"] as NbtList

        val nameJson = "{\"text\":\"❄☠ Frozen Assets ☠❄\"}"
        val display = NbtCompound()
        display.putString("Name", nameJson)
        shulkerNbt.put("display", display)
        shulkerNbt.putString("owner", player.name.string)
        shulkerNbt.putUuid("owner-id", player.uuid)

        val cardCount = cards.groupingBy { it.name!! }.eachCount()
        var cardIndex = 0
        player.debug("Your shulker should contain ${cards.size} cards:")
        cardCount.forEach { (cardName, count) ->
            player.debug("- ${count}x $cardName")
            logger.info("${player.name.string}'s shulker should contain ${count}x $cardName")

            val card = Cards.findCard(cardName)
            if (card == null) {
                player.sendMessage("Unknown card '${cardName}', Agronet will not add it to your deck", Formatting.RED)
                logger.error("Unknown card '${cardName}', Agronet cannot add it to ${player.name.string}'s deck")
            } else {
                val cardData = createCard(cardIndex++, card, count)
                shulkerItems.add(cardData)
            }
        }
        shulkerNbt.put(BlockItem.BLOCK_ENTITY_TAG_KEY, blockCompound)

        val shulkerBox = ItemStack(Items.CYAN_SHULKER_BOX)
        shulkerBox.nbt = shulkerNbt

        val inventory = player.inventory
        if (!inventory.insertStack(shulkerBox)) {
            logger.warn("Failed to give ${player.name} a Decked Out Shulker as their inventory is full")
            player.sendMessage("Failed to give you your Decked Out Shulker as your inventory is full", Formatting.RED)
            return
        }
        inventory.updateItems()

        player.addCommandTag(AgroNet.RECEIVED_SHULKER)
        player.sendMessage("Your Decked Out shulker has been placed in your inventory", Formatting.GREEN)
    }

    private fun createCard(index: Int, card: Cards.Companion.Card, count: Int): NbtCompound {
        val nbt = NbtCompound()
        ItemStack(Items.IRON_NUGGET, count).writeNbt(nbt)
        val tag = NbtCompound()

        val nameJson = "{\"color\":\"${card.colour}\",\"text\":\"${card.displayName}\"}"
        val display = NbtCompound()
        display.putString("Name", nameJson)
        display.putString("NameFormat", "{\"color\":\"${card.colour}\",\"OriginalName\":\"${nameJson}\"}")
        tag.put("display", display)

        tag.putInt("CustomModelData", card.modelData)
        nbt.put("tag", tag)
        nbt.putByte("Slot", index.toByte())

        return nbt
    }
}