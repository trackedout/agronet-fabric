package org.trackedout.actions

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Formatting
import org.slf4j.LoggerFactory
import org.trackedout.RECEIVED_SHULKER
import org.trackedout.debug
import org.trackedout.isDeckedOutShulker
import org.trackedout.sendMessage

class RemoveDeckFromPlayerInventoryAction {
    private val logger = LoggerFactory.getLogger("Agronet")

    fun execute(player: PlayerEntity) {

        val removedItems = player.inventory.remove(
            { item ->
                if (item.isDeckedOutShulker()) {
                    player.debug("Removing Decked Out 2 shulker from your inventory")
                    logger.info("Removing ${player.name.string}'s shulker from their inventory")
                    val nbt = item.nbt
                    if (nbt != null) {
                        logger.info("NBT data: ${nbt.asString()}")
                    } else {
                        logger.warn("NBT data not present!")
                    }

                    true
                } else {
                    false
                }
            },
            -1,
            player.playerScreenHandler.craftingInput
        )

        if (removedItems > 0) {
            player.commandTags.remove(RECEIVED_SHULKER)
            player.sendMessage("Your Decked Out shulker has been removed from your inventory (it's stored in Dunga Dunga)", Formatting.GREEN)
        } else {
            logger.info("${player.name}'s inventory does not contain a Decked Out Shulker")
            player.sendMessage("Your inventory does not contain a Decked Out Shulker", Formatting.RED)
        }

        player.inventory.updateItems()
        player.commandTags.remove(RECEIVED_SHULKER)
    }
}
