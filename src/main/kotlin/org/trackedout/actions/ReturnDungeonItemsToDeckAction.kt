package org.trackedout.actions

import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import org.slf4j.LoggerFactory
import org.trackedout.RunContext
import org.trackedout.client.apis.InventoryApi
import org.trackedout.client.models.Card
import org.trackedout.runType
import org.trackedout.sendMessage

// Items that are returned to the player's deck on game win
val returnableItems = setOf(
    "SWIFTNESS_POTION", "SLOWNESS_POTION", "WEAKNESS_POTION", "HEALTH_POTION",
    "COPPER_BLOCK", "EXPOSED_COPPER", "WEATHERED_COPPER", "OXIDIZED_COPPER",
    "ICE", "PACKED_ICE", "BLUE_ICE",
    "RED_DYE",
)

class ReturnDungeonItemsToDeckAction(
    private val inventoryApi: InventoryApi,
    private val serverName: String,
) {
    private val logger = LoggerFactory.getLogger("Agronet")

    fun execute(player: ServerPlayerEntity) {
        val playerName = player.name.string
        val runType = RunContext.playerContext(playerName).runType()
        val itemsGiven = RunContext.getItemsGivenOnJoin(playerName)
        logger.info("Returning dungeon items from ${playerName}'s inventory back to their deck (given on join: $itemsGiven)")

        for ((itemName, expectedStack) in dungeonItemsForGiveCommand) {
            if (!returnableItems.contains(itemName)) {
                logger.info("$itemName is consumed on use, not returning to deck")
                continue
            }

            val maxReturn = itemsGiven.getOrDefault(itemName, 0)
            if (maxReturn == 0) {
                logger.info("$itemName was not given to $playerName on join, skipping")
                continue
            }

            var removedCount = 0
            for (slot in 0 until player.inventory.size()) {
                val stack = player.inventory.getStack(slot)
                if (stack.isEmpty) continue
                if (!ItemStack.canCombine(stack, expectedStack)) continue

                val toRemove = minOf(stack.count, maxReturn - removedCount)
                if (toRemove <= 0) break

                if (toRemove >= stack.count) {
                    removedCount += stack.count
                    player.inventory.setStack(slot, ItemStack.EMPTY)
                } else {
                    removedCount += toRemove
                    stack.decrement(toRemove)
                }
            }

            if (removedCount > 0) {
                logger.info("Removed ${removedCount}x $itemName from ${playerName}'s inventory, adding back to deck (was given ${maxReturn}x on join)")
                player.sendMessage("Returned ${removedCount}x $itemName to your deck", Formatting.GREEN)
                for (i in 1..removedCount) {
                    inventoryApi.inventoryAddCardPost(
                        Card(
                            name = itemName,
                            player = playerName,
                            server = serverName,
                            deckType = runType,
                        )
                    )
                }
            } else {
                logger.info("No $itemName found in ${playerName}'s inventory, skipping")
            }
        }

        player.inventory.updateItems()
    }
}
