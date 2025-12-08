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
import org.trackedout.Agronet.runAsyncTask
import org.trackedout.EventsApiWithContext
import org.trackedout.RECEIVED_SHULKER
import org.trackedout.RunContext
import org.trackedout.client.apis.InventoryApi
import org.trackedout.client.models.Event
import org.trackedout.data.BrillianceCard
import org.trackedout.data.Cards
import org.trackedout.data.JsonToNbtConverter
import org.trackedout.debug
import org.trackedout.fullDeckId
import org.trackedout.fullRunType
import org.trackedout.runType
import org.trackedout.sendMessage
import org.trackedout.shortDeckId
import kotlin.math.max
import kotlin.math.min

class AddDeckToPlayerInventoryAction(
    private val eventsApi: EventsApiWithContext,
    private val inventoryApi: InventoryApi,
) {
    private val logger = LoggerFactory.getLogger("Agronet")

    fun execute(source: ServerCommandSource, player: ServerPlayerEntity) = runAsyncTask {
        val playerName = player.name.string
        logger.debug("Fetch player deck command")

        player.debug("Player tags: ${player.commandTags}")
        logger.debug("Player tags: {}", player.commandTags)
        if (player.commandTags.contains(RECEIVED_SHULKER)) {
            source.sendMessage("Player $playerName already has their shulker box, refusing to give them another one", Formatting.RED)
            player.debug("You already have your shulker box, refusing to give you another one")
            logger.warn("Player $playerName already has their shulker box, refusing to give them another one")
            return@runAsyncTask
        }

        if (!RunContext.initialized) {
            player.sendMessage("Run data is not initialized, unable to determine which deck to give to $playerName", Formatting.RED)
            return@runAsyncTask
        }

        val context = RunContext.playerContext(playerName)
        player.sendMessage("Fetching ${context.fullRunType()} mode Decked Out shulker #${context.shortDeckId()} from Dunga Dunga...", Formatting.GRAY)
        val cards = inventoryApi.inventoryCardsGet(player = playerName, limit = 200, deckType = context.runType(), deckId = context.fullDeckId()).results!!

        runAsyncTask {
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
        }

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

        val cardCount = cards
            .filter { RunContext.findCard(it.name!!) != null }
            .groupingBy { it.name!! }.eachCount()
        player.debug("Your shulker should contain ${cards.size} cards:")
        var cardIndex = 0
        var totalCards = 0
        var sentTruncationWarning = false
        val modificationLog = mutableMapOf<String, String>()

        cardCount.forEach { (cardName, countInDeck) ->
            player.debug("- ${countInDeck}x $cardName")
            val card = RunContext.findCard(cardName)!!
            val maxCopies = card.maxCopies
            var count = min(countInDeck, maxCopies ?: countInDeck)
            logger.info("$playerName's shulker should contain ${count}x $cardName (deck has $countInDeck, max copies is $maxCopies, deck contains $totalCards cards)")

            if (countInDeck > (maxCopies ?: 0)) {
                // If the player has more copies of a card than they should, log the new count that we're giving them
                modificationLog["new-card-count-${cardName.replace("_", "-")}"] = "$count"
                logger.warn("$playerName has too many copies of $cardName in their deck, truncating to $count")
                val cardDisplayName = Cards.findCard(cardName)?.displayName ?: cardName
                player.sendMessage(
                    "You have too many copies of $cardDisplayName in your deck " +
                        "(max is $maxCopies and you have ${countInDeck}), truncating to $count", Formatting.RED
                )
            }

            // If the new cards would take the total over 40, truncate the count to only fill up to 40
            if (totalCards + count > 40) {
                count = max(0, 40 - totalCards)
                modificationLog["new-card-count-${cardName.replace("_", "-")}"] = "$count"
                if (!sentTruncationWarning) {
                    logger.warn("$playerName has too many cards in their deck, truncating to 40")
                    player.sendMessage("You have too many cards in your deck, truncating to 40", Formatting.RED)
                    sentTruncationWarning = true
                }
                logger.info("Deck limit exceeded. New count for $cardName is $count")
            }

            if (count <= 0) {
                runAsyncTask {
                    eventsApi.eventsPost(
                        Event(
                            name = "card-skipped-on-join-${cardName.replace("_", "-")}",
                            player = playerName,
                            x = 0.0,
                            y = 0.0,
                            z = 0.0,
                            count = count,
                        )
                    )
                }
                return@forEach
            }

            val cardData = createCard(cardIndex++, card, count)
            totalCards += count
            shulkerItems.add(cardData)

            runAsyncTask {
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

        if (modificationLog.isNotEmpty()) {
            runAsyncTask {
                eventsApi.eventsPost(
                    Event(
                        name = "deck-modified-on-join",
                        player = playerName,
                        x = 0.0,
                        y = 0.0,
                        z = 0.0,
                        count = 1,
                        metadata = modificationLog
                    )
                )
            }
        }

        cards
            .filter { RunContext.findCard(it.name!!) == null }
            .forEach { item ->
                val itemName = item.name!!
                if (dungeonItemsForGiveCommand.containsKey(itemName)) {
                    logger.info("Giving $playerName 1x$itemName (and deleting it from item storage)")
                    inventoryApi.inventoryDeleteCardPost(item)
                    eventsApi.eventsPost(
                        Event(
                            name = "item-deleted-$itemName",
                            player = playerName,
                            x = 0.0,
                            y = 0.0,
                            z = 0.0,
                            count = 1,
                        )
                    )

                    val item = dungeonItemsForGiveCommand[itemName]!!
                    player.giveItemStack(item.copy())
                } else if (!ignoredItems.contains(itemName)) {
                    player.sendMessage("Unknown item '${itemName}', Agronet will not add it to your deck", Formatting.RED)
                    logger.error("Unknown item '${itemName}', Agronet cannot add it to $playerName's deck")
                }
            }

        shulkerNbt.put(BlockItem.BLOCK_ENTITY_TAG_KEY, blockCompound)

        val shulkerBox = ItemStack(Items.CYAN_SHULKER_BOX)
        shulkerBox.nbt = shulkerNbt

        val inventory = player.inventory
        if (!inventory.insertStack(shulkerBox)) {
            logger.warn("Failed to give ${player.name} a Decked Out Shulker as their inventory is full")
            player.sendMessage("Failed to give you your Decked Out Shulker as your inventory is full", Formatting.RED)
            return@runAsyncTask
        }
        inventory.updateItems()

        player.addCommandTag(RECEIVED_SHULKER)
        player.sendMessage("Your Decked Out shulker has been placed in your inventory", Formatting.GREEN)
    }

    private fun createCard(index: Int, card: BrillianceCard, count: Int): NbtCompound {
        val nbt = NbtCompound()
        ItemStack(Items.IRON_NUGGET, count).writeNbt(nbt)

        /*
        Card data:
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
          },
         */

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

        val tag = JsonToNbtConverter.fromJson(card.tagRaw!!)
        nbt.put("tag", tag)
        nbt.putByte("Slot", index.toByte())

        logger.info("NBT for ${card.shorthand}: $nbt")

        return nbt
    }
}
