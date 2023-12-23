package org.trackedout.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.trackedout.client.apis.InventoryApi
import org.trackedout.client.models.Card

class CardPurchasedCommand(private val inventoryApi: InventoryApi, private val serverName: String) : PlayerCommand {
    override fun run(context: CommandContext<ServerCommandSource>): Int {
        val cardName = StringArgumentType.getString(context, "card")

        val player = context.source.player
        if (player == null) {
            logger.warn("Attempting to run /card-bought { cardName=${cardName} }, but command is not run as a player, ignoring...")
            context.source.sendFeedback(
                { Text.literal("Attempting to run card-bought command, but command is not run as a player, ignoring...") },
                true
            )

            return -1
        }

        val x = player.x
        val y = player.y
        val z = player.z

        context.source.sendFeedback(
            {
                Text.literal(
                    "Processing /card-bought { cardName=${cardName} } for player ${player.name.string} " +
                            "at location [$x, $y, $z]"
                )
            },
            true
        )

        try {
            val result = inventoryApi.inventoryAddCardPost(
                card = Card(
                    name = cardName,
                    player = player.name.string,
                    server = serverName,
                    deckId = "1",
                )
            )

            context.source.sendFeedback(
                {
                    Text.literal(
                        "Successfully sent '${cardName}' card purchase event for player ${player.name.string} to Dunga Dunga"
                    )
                },
                true
            )
            context.source.sendFeedback({ Text.literal("Result: $result") }, true)

        } catch (e: Exception) {
            logger.error("Failed to call Inventory API: ${e.message}")
            e.printStackTrace()
            context.source.sendFeedback(
                { Text.literal("Failed to call Inventory API: ${e.message}") },
                true
            )
        }

        return 1
    }
}