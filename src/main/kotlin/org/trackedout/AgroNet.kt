package org.trackedout


import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.util.Formatting
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import org.trackedout.actions.AddDeckToPlayerInventoryAction
import org.trackedout.actions.RemoveDeckFromPlayerInventoryAction
import org.trackedout.client.apis.EventsApi
import org.trackedout.client.apis.InventoryApi
import org.trackedout.client.models.EventsPostRequest
import org.trackedout.commands.CardPurchasedCommand
import org.trackedout.commands.LogEventCommand
import org.trackedout.listeners.AgroNetServerPlayConnectionListener
import redis.clients.jedis.Jedis
import java.net.InetAddress
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import java.net.Socket
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource


const val RECEIVED_SHULKER = "do2.received_shulker"

object AgroNet : ModInitializer {
    private val logger = LoggerFactory.getLogger("Agro-net")

    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        val serverName = getEnvOrDefault("SERVER_NAME", InetAddress.getLocalHost().hostName)
        val dungaAPIPath = getEnvOrDefault("DUNGA_API", "http://localhost:3000/v1")

        val eventsApi = EventsApi(
            basePath = dungaAPIPath,
            client = OkHttpClient.Builder()
                .connectTimeout(5.seconds.toJavaDuration())
                .callTimeout(30.seconds.toJavaDuration())
                .build()
        )

        val inventoryApi = InventoryApi(
            basePath = dungaAPIPath,
            client = OkHttpClient.Builder()
                .connectTimeout(5.seconds.toJavaDuration())
                .callTimeout(30.seconds.toJavaDuration())
                .build()
        )

        val addDeckToPlayerInventoryAction = AddDeckToPlayerInventoryAction(inventoryApi)
        val removeDeckFromPlayerInventoryAction = RemoveDeckFromPlayerInventoryAction()

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("take-shulker")
                .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                .executes { context ->
                    val player = context.source.player
                    if (player != null) {
                        removeDeckFromPlayerInventoryAction.execute(player)
                    } else {
                        logger.warn("Attempting to take shulker but command is not run as a player, ignoring...")
                    }

                    1
                }
            )
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("gief-shulker")
                .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                .executes { context ->
                    val player = context.source.player
                    if (player != null) {
                        addDeckToPlayerInventoryAction.execute(context.source, player)
                    } else {
                        logger.warn("Attempting to give shulker but command is not run as a player, ignoring...")
                    }

                    1
                })
        }

        val logEventCommand = LogEventCommand(eventsApi, serverName)

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("log-event")
                .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                .then(
                    argument("event", StringArgumentType.word()) // words_with_underscores
                        .executes(logEventCommand::run)
                        .then(
                            argument(
                                "count", // Number of units for this event
                                IntegerArgumentType.integer(1)
                            )
                                .executes(logEventCommand::run)
                        )
                )
            )
        }

        val cardPurchasedCommand = CardPurchasedCommand(inventoryApi, eventsApi, serverName)

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("card-bought")
                .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                .then(
                    argument("card", StringArgumentType.word()) // words_with_underscores
                        .executes(cardPurchasedCommand::run)
                )
            )
        }

        // todo: could add syntax like `/scale-worker <machine> <num_instances>
//        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
//            dispatcher.register(literal("scale-worker")
//                .requires(Permissions.require("trackedout.serveradmin.scale-worker", 4))
//                .executes { context ->
//                    sendRedisMessage(context.source,"server-hosts", "scale-worker")
//                    1
//                })
//        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("update-workers")
                .requires(Permissions.require("trackedout.serveradmin.update-workers", 4))
                .executes { context ->
                    sendRedisMessage(context.source,"server-hosts", "update-workers")
                    1
                })
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("update-datapack")
                .requires(Permissions.require("trackedout.update-datapack", 2))
                .executes { context ->
                    sendRedisMessage(context.source, "datapack-updates", "request-update")
                    1
                })
        }

        ServerPlayConnectionEvents.JOIN.register(AgroNetServerPlayConnectionListener(addDeckToPlayerInventoryAction))

        eventsApi.eventsPost(
            EventsPostRequest(
                name = "server-online",
                player = "server",
                server = serverName,
                x = 0.0,
                y = 0.0,
                z = 0.0,
                count = 1,
            )
        )

        logger.info("Agro-net online. Flee with extra flee!")
    }

    private fun sendRedisMessage(commandSource: ServerCommandSource, redisChannel: String, redisMessage: String): Long {
        val redisIp = System.getenv("REDIS_IP") ?: "10.150.0.6"
        val redisPort = System.getenv("REDIS_PORT")?.toIntOrNull() ?: 6379
        val redisPassword = System.getenv("REDIS_PASSWORD") ?: "freehand-cleft-barbados-brooch"

        // TEST 1
        try {
            val socket = Socket(redisIp, redisPort)
            socket.close()
            logger.info("Successfully established a socket connection to Redis.")
        } catch (e: Exception) {
            val message = "Failed to establish a socket connection to Redis: ${e.message}"
            logger.error(message)
            e.printStackTrace()
            commandSource.sendMessage(message, Formatting.GRAY)
            return 0
        }

        // TEST 2
        try {
            val address = InetAddress.getByName(redisIp)
            if (address.isReachable(5000)) { // Timeout in milliseconds
                logger.info("Host $redisIp is reachable.")
            } else {
                logger.error("Host $redisIp is not reachable.")
            }
        } catch (e: Exception) {
            val message = "Error checking host reachability: ${e.message}"
            logger.error(message)
            e.printStackTrace()
            commandSource.sendMessage(message, Formatting.GRAY)
            return 0
        }

        // Send message over redis
        val jedis = Jedis(redisIp, redisPort)
        jedis.auth(redisPassword)
        val numRecipients = jedis.publish(redisChannel, redisMessage)
        val message = if (numRecipients > 0L) "Message delivered" else "Message sent, but not received"
        logger.info("[AGRONET]: [redis-message]: $message")
        commandSource.sendMessage(message, Formatting.GRAY)
        return numRecipients
    }

    private fun getEnvOrDefault(key: String, default: String): String {
        var value = System.getenv(key)
        if (value == null || value.isEmpty()) {
            value = default
        }
        return value
    }
}
