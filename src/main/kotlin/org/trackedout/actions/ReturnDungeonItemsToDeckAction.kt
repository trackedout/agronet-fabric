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

class ReturnDungeonItemsToDeckAction(
    private val inventoryApi: InventoryApi,
    private val serverName: String,
) {
    private val logger = LoggerFactory.getLogger("Agronet")

    fun execute(player: ServerPlayerEntity) {
        val playerName = player.name.string
        val runType = RunContext.playerContext(playerName).runType()
        logger.info("Returning dungeon items from ${playerName}'s inventory back to their deck")

        for ((itemName, expectedStack) in dungeonItemsForGiveCommand) {
            var removedCount = 0
            for (slot in 0 until player.inventory.size()) {
                val stack = player.inventory.getStack(slot)
                if (stack.isEmpty) continue
                if (!ItemStack.canCombine(stack, expectedStack)) continue

                removedCount += stack.count
                player.inventory.setStack(slot, ItemStack.EMPTY)
            }

            if (removedCount > 0) {
                logger.info("Removed ${removedCount}x $itemName from ${playerName}'s inventory, adding back to deck")
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
