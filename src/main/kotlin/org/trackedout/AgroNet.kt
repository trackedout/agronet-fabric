package org.trackedout

import ServerTickListener
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.text.Text
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import org.trackedout.client.apis.UsersApi
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


object AgroNet : ModInitializer {
    private val logger = LoggerFactory.getLogger("agronet")

    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        val usersApi = UsersApi(
            client = OkHttpClient.Builder()
                .connectTimeout(5.seconds.toJavaDuration())
                .callTimeout(30.seconds.toJavaDuration())
                .build()
        )

        ServerTickEvents.END_SERVER_TICK.register(ServerTickListener())

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("log-event")
                .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                .then(
                    argument("event", StringArgumentType.word()) // words_with_underscores
                        .then(
                            argument("player", EntityArgumentType.player()) // Player that's in the game
                                .then(argument(
                                    "count", // Number of units for this event
                                    IntegerArgumentType.integer(1)
                                )
                                    .executes { context ->
                                        val event = StringArgumentType.getString(context, "event")
                                        val player = EntityArgumentType.getPlayer(context, "player")
                                        val count = IntegerArgumentType.getInteger(context, "count")

                                        context.source.sendFeedback(
                                            { Text.literal("Processing /log-event { event=${event}, player=${player.name}, count=${count} }") },
                                            true
                                        )

                                        try {
                                            val someUser = usersApi.usersGet().results?.get(0)?.name
                                            context.source.sendFeedback(
                                                { Text.literal("Some user: $someUser") },
                                                true
                                            )
                                        } catch (e: Exception) {
                                            context.source.sendFeedback(
                                                { Text.literal("Failed to call Users API: ${e.message}") },
                                                true
                                            )
                                        }

                                        1
                                    })
                        )
                )
            )
        }

        logger.info("Hello Fabric world!")
    }
}
