package org.trackedout.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.trackedout.client.apis.EventsApi
import org.trackedout.client.models.EventsPostRequest

class LogEventCommand(private val eventsApi: EventsApi, private val serverName: String) : PlayerCommand {
    override fun run(context: CommandContext<ServerCommandSource>): Int {
        val event = StringArgumentType.getString(context, "event")
        val count = IntegerArgumentType.getInteger(context, "count")

        val player = context.source.player
        if (player == null) {
            logger.warn("Attempting to run /log-event { event=${event}, count=${count} }, but command is not run as a player, ignoring...")
            context.source.sendFeedback(
                { Text.literal("Attempting to run log-event command, but command is not run as a player, ignoring...") },
                true
            )

            return -1
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
                EventsPostRequest(
                    name = event,
                    player = sourceName,
                    server = serverName,
                    x, y, z, count
                )
            )

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