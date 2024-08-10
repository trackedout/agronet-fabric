package org.trackedout.actions

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
import org.trackedout.EventsApiWithContext
import org.trackedout.RECEIVED_SHULKER
import org.trackedout.RunContext
import org.trackedout.client.apis.InventoryApi
import org.trackedout.client.models.Event
import org.trackedout.data.Cards
import org.trackedout.data.Cards.Companion.Card
import org.trackedout.debug
import org.trackedout.fullDeckId
import org.trackedout.fullRunType
import org.trackedout.runType
import org.trackedout.sendMessage
import org.trackedout.shortDeckId

class AddDeckToPlayerInventoryAction(
    private val eventsApi: EventsApiWithContext,
    private val inventoryApi: InventoryApi,
) {
    private val logger = LoggerFactory.getLogger("Agronet")

    fun execute(source: ServerCommandSource, player: ServerPlayerEntity) {
        val playerName = player.name.string
        logger.debug("Fetch player deck command")

        player.debug("Player tags: ${player.commandTags}")
        logger.debug("Player tags: {}", player.commandTags)
        if (player.commandTags.contains(RECEIVED_SHULKER)) {
            source.sendMessage("Player $playerName already has their shulker box, refusing to give them another one", Formatting.RED)
            player.debug("You already have your shulker box, refusing to give you another one")
            logger.warn("Player $playerName already has their shulker box, refusing to give them another one")
            return
        }

        val context = RunContext.playerContext(playerName)
        player.sendMessage("Fetching ${context.fullRunType()} mode Decked Out shulker #${context.shortDeckId()} from Dunga Dunga...", Formatting.GRAY)
        val cards = inventoryApi.inventoryCardsGet(player = playerName, limit = 200, deckType = context.runType(), deckId = context.fullDeckId()).results!!

        eventsApi.eventsPost(
            Event(
                name = "card-count-on-join",
                player = playerName,
                x = 0.0,
                y = 0.0,
                z = 0.0,
                count = cards.size,
            )
        )

        val canPlaceOn = "CanPlaceOn: [\"redstone_lamp\"]"
        val shulkerNbt =
            StringNbtReader.parse("{${BlockItem.BLOCK_ENTITY_TAG_KEY}:{Items:[],id:\"minecraft:cyan_shulker_box\"}, $canPlaceOn }")
        val blockCompound = shulkerNbt.getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY)
        val shulkerItems = blockCompound["Items"] as NbtList

        val nameJson = "{\"text\":\"❄☠ Frozen Assets ☠❄\"}"
        val display = NbtCompound()
        display.putString("Name", nameJson)
        shulkerNbt.put("display", display)
        shulkerNbt.putString("owner", playerName)
        shulkerNbt.putUuid("owner-id", player.uuid)

        val cardCount = cards.groupingBy { it.name!! }.eachCount()
        var cardIndex = 0
        player.debug("Your shulker should contain ${cards.size} cards:")
        cardCount.forEach { (cardName, count) ->
            player.debug("- ${count}x $cardName")
            logger.info("$playerName's shulker should contain ${count}x $cardName")

            val card = Cards.findCard(cardName)
            if (card == null) {
                player.sendMessage("Unknown card '${cardName}', Agronet will not add it to your deck", Formatting.RED)
                logger.error("Unknown card '${cardName}', Agronet cannot add it to $playerName's deck")
            } else {
                val cardData = createCard(cardIndex++, card, count)
                shulkerItems.add(cardData)

                eventsApi.eventsPost(
                    Event(
                        name = "card-exists-on-join-${cardName.replace("_", "-")}",
                        player = playerName,
                        x = 0.0,
                        y = 0.0,
                        z = 0.0,
                        count = count,
                    )
                )
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

        player.addCommandTag(RECEIVED_SHULKER)
        player.sendMessage("Your Decked Out shulker has been placed in your inventory", Formatting.GREEN)
    }

    private fun createCard(index: Int, card: Card, count: Int): NbtCompound {
        val nbt = NbtCompound()
        ItemStack(Items.IRON_NUGGET, count).writeNbt(nbt)

        /*
        Expected result:
        {
          "CustomRoleplayData": 1,
          "tracked": 0,
          "NameFormat": {
            "OriginalName": "{\"color\":\"gray\",\"text\":\"✲ Moment of Clarity ✲\"}",
            "color": "gray",
            "ModifiedName": "{\"color\":\"gray\",\"text\":\"✲ Moment of Clarity ✲\"}"
          },
          "CustomModelData": 106,
          "display": {
            "Name": "{\"color\":\"gray\",\"text\":\"✲ Moment of Clarity ✲\"}"
          }
        }
         */

        val tag = NbtCompound()

        tag.putByte("CustomRoleplayData", 1)
        tag.putByte("tracked", 0)

        val nameJson = "{\"color\":\"${card.colour}\",\"text\":\"${card.displayName}\"}"
        var originalName = nameJson
        if (Card.PORK_CHOP_POWER.key == card.key) {
            originalName = "{\"color\":\"gray\",\"text\":\"${card.displayName}\"}"
        } else if (listOf(Card.PAY_TO_WIN, Card.PIRATES_BOOTY, Card.DUNGEON_LACKEY).map(Card::key).contains(card.key)) {
            originalName = "{\"text\":\"${card.displayName}\"}"
        }

        val nameFormat = NbtCompound()
        nameFormat.putString("OriginalName", originalName)
        nameFormat.putString("color", card.colour.lowercase())
        nameFormat.putString("ModifiedName", nameJson)
        tag.put("NameFormat", nameFormat)

        tag.putInt("CustomModelData", card.modelData)

        val display = NbtCompound()
        display.putString("Name", nameJson)
        tag.put("display", display)

        nbt.put("tag", tag)
        nbt.putByte("Slot", index.toByte())

        return nbt
    }
}
