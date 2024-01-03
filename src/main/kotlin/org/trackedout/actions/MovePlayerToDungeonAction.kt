package org.trackedout.actions

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandOutput
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.Vec2f
import org.slf4j.LoggerFactory
import org.trackedout.*

class MovePlayerToDungeonAction {
    private val logger = LoggerFactory.getLogger("Agro-net")
    fun execute(player: PlayerEntity): ActionResult {

        if (player.isSpectator) {
            player.sendMessage("Sorry, spectators may not enter the dungeon this way", Formatting.RED)
            logger.warn("Spectator attempting to enter dungeon, rejecting!")
            return ActionResult.FAIL
        }

        if (player.isCreative) {
            return ActionResult.PASS
        }

        if (!player.isReadyToStartDungeonRun()) {
            if (!player.hasKeyInHand()) {
                player.sendMessage("You need to be holding a key to enter the Dungeon!", Formatting.RED)
                logger.info("${player.name.string} smacked the door without holding a key, silly player.")
            } else if (!player.hasShulkerInInventory()) {
                player.sendMessage("You need your deck (Shulker Box) in your inventory to enter the Dungeon!", Formatting.RED)
                logger.info("${player.name.string} smacked the door without their Shulker Box, silly player.")
            }
            return ActionResult.FAIL
        }

        // The player is ready to enter the dungeon!
        player.sendMessage("Entering dungeon, good luck!", Formatting.BLUE)
        logger.info("${player.name.string} is entering the dungeon!")

        val removeDeckFromPlayerInventoryAction = RemoveDeckFromPlayerInventoryAction()
        removeDeckFromPlayerInventoryAction.execute(player)

        val commandSource = ServerCommandSource(
            CommandOutput.DUMMY,
            player.pos,
            Vec2f.ZERO,
            player.world as ServerWorld,
            2,
            player.name.string,
            player.displayName,
            player.world.server,
            player
        )
        player.server!!.commandManager.executeWithPrefix(commandSource, "/tp 14 137 136")
        player.server!!.commandManager.executeWithPrefix(commandSource, "/proxycommand \"send ${player.name.string} lobby\"")
        player.playSound(SoundEvents.ENTITY_WARDEN_EMERGE, player.soundCategory, 1.0f, 1.0f)

        return ActionResult.SUCCESS
    }
}