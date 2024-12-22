package org.trackedout.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.trackedout.AgroNet.sendPlayerToLobby
import org.trackedout.EventsApiWithContext
import org.trackedout.client.apis.TasksApi
import org.trackedout.client.models.Event

class LogEventCommand(
    private val eventsApi: EventsApiWithContext,
    private val tasksApi: TasksApi,
) : PlayerCommand {
    override fun run(context: CommandContext<ServerCommandSource>): Int {
        val event = StringArgumentType.getString(context, "event")
        val count = try {
            IntegerArgumentType.getInteger(context, "count")
        } catch (e: Exception) {
            1
        }

        val position = if (context.source.player != null) {
            context.source.player!!.pos
        } else {
            context.source.position
        }

        val x = position.x
        val y = position.y
        val z = position.z

        val sourceName = context.source.name // Either "@" or the player name
        context.source.sendFeedback(
            {
                Text.literal(
                    "Processing /log-event { event=${event}, count=${count} } for $sourceName " +
                        "at location [$x, $y, $z]"
                )
            },
            true
        )

        try {
            val result = eventsApi.eventsPost(
                Event(
                    name = event,
                    player = sourceName,
                    x = x,
                    y = y,
                    z = z,
                    count = count,
                )
            )

            if (event == "game-ended") {
                val spectators = context.source.server.playerManager.playerList
                    .filter { player -> player.commandTags.contains("do2.spectating") }
                    .filter { player -> !player.commandTags.contains("do2.staff") }

                logger.info("game-ended event detected, sending ${spectators.size} spectators back to the lobby")
                spectators.forEach { spectator ->
                    sendPlayerToLobby(spectator)
                }
            }

            context.source.sendFeedback(
                {
                    Text.literal(
                        "Successfully sent event { event=${event}, count=${count} } for $sourceName " +
                            "for location [$x, $y, $z] to Dunga Dunga"
                    )
                },
                true
            )
            context.source.sendFeedback({ Text.literal("Result: $result") }, true)

        } catch (e: Exception) {
            logger.error("Failed to call Events API: ${e.message}")
            e.printStackTrace()
            context.source.sendFeedback(
                { Text.literal("Failed to call Events API: ${e.message}") },
                true
            )
        }

        return 1
    }
}
