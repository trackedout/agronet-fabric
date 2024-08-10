package org.trackedout.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.trackedout.EventsApiWithContext
import org.trackedout.RunContext
import org.trackedout.client.apis.InventoryApi
import org.trackedout.client.models.Card
import org.trackedout.client.models.Event
import org.trackedout.client.models.Item
import org.trackedout.data.Cards
import org.trackedout.runType
import org.trackedout.sendMessage

class CardInteractionCommand(
    private val inventoryApi: InventoryApi,
    private val eventsApi: EventsApiWithContext,
    private val serverName: String,
) {
    private val logger: Logger
        get() = LoggerFactory.getLogger("Agronet")

    fun run(context: CommandContext<ServerCommandSource>, operation: String): Int {
        val cardName = StringArgumentType.getString(context, "card").replace("-", "_")
        val count = try {
            IntegerArgumentType.getInteger(context, "count")
        } catch (e: Exception) {
            1
        }

        val player = context.source.player
        if (player == null) {
            logger.warn("Attempting to run /$operation { cardName=${cardName} }, but command is not run as a player, ignoring...")
            context.source.sendFeedback(
                { Text.literal("Attempting to run $operation command, but command is not run as a player, ignoring...") },
                true
            )

            return -1
        }

        val playerName = player.name.string
        val x = player.x
        val y = player.y
        val z = player.z

        context.source.sendFeedback(
            {
                Text.literal(
                    "Processing /$operation { cardName=${cardName} } for player $playerName " +
                        "at location [$x, $y, $z]"
                )
            },
            true
        )

        try {
            eventsApi.eventsPost(
                Event(
                    name = "$operation-${cardName.replace("_", "-")}",
                    player = playerName,
                    x = x,
                    y = y,
                    z = z,
                    count = count,
                )
            )

            when (operation) {
                "card-bought" -> {
                    context.source.sendMessage(
                        "Adding $cardName to $playerName's deck as a result of a purchasing event",
                        Formatting.GRAY
                    )
                    inventoryApi.inventoryAddCardPost(
                        card = Card(
                            name = cardName,
                            player = playerName,
                            server = serverName,
                            deckType = RunContext.playerContext(playerName).runType(),
                        )
                    )
                }

                "card-played" -> {
                    if (Cards.etherealCards().find { it.key == cardName } != null) {
                        context.source.sendMessage(
                            "Deleting $cardName from $playerName's deck as it is an ethereal card",
                            Formatting.GRAY
                        )
                        inventoryApi.inventoryDeleteCardPost(
                            card = Card(
                                name = cardName,
                                player = playerName,
                                server = serverName,
                                deckType = RunContext.playerContext(playerName).runType(),
                            )
                        )
                    } else {
                        context.source.sendMessage(
                            "$cardName is NOT an ethereal card, keeping it in $playerName's deck",
                            Formatting.GRAY
                        )
                    }
                }

                "add-item" -> {
                    context.source.sendMessage(
                        "Adding $count copies of $cardName to $playerName's deck",
                        Formatting.GRAY
                    )
                    for (i in 1 until count + 1) {
                        inventoryApi.storageAddItemPost(
                            item = Item(
                                name = cardName,
                                player = playerName,
                                server = serverName,
                            )
                        )
                    }
                }

                else -> {
                    logger.info("Not performing any additional action for card event type: $operation")
                }
            }

            context.source.sendFeedback(
                {
                    Text.literal(
                        "Successfully sent $cardName $operation event for player $playerName to Dunga Dunga"
                    )
                },
                true
            )

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
