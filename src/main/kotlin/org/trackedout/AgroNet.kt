package org.trackedout

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.netty.buffer.Unpooled
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket
import net.minecraft.resource.ResourceType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import org.trackedout.RunContext.serverName
import org.trackedout.actions.AddDeckToPlayerInventoryAction
import org.trackedout.actions.RemoveDeckFromPlayerInventoryAction
import org.trackedout.client.apis.ClaimApi
import org.trackedout.client.apis.EventsApi
import org.trackedout.client.apis.InventoryApi
import org.trackedout.client.apis.ScoreApi
import org.trackedout.client.apis.TasksApi
import org.trackedout.client.models.Event
import org.trackedout.commands.CardInteractionCommand
import org.trackedout.commands.LogEventCommand
import org.trackedout.listeners.AgroNetPlayerConnectionListener
import redis.clients.jedis.Jedis
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

const val RECEIVED_SHULKER = "do2.received_shulker"

object AgroNet : ModInitializer {
    private val logger = LoggerFactory.getLogger("Agronet")
    private val threadPool = Executors.newScheduledThreadPool(2)
    private var activePlayers: List<String> = emptyList()
    private var allPlayers: List<String> = emptyList()

    private val runContext = RunContext


    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        serverName = getEnvOrDefault("SERVER_NAME", InetAddress.getLocalHost().hostName)
        if (serverName.contains("dungeon-")) {
            // dungeon-1  -> d801
            // dungeon-14 -> d814
            val dungeonId = serverName.replace("dungeon-", "")
            serverName = "d8${dungeonId.padStart(2, '0')}"
        }

        val dungaAPIPath = getEnvOrDefault("DUNGA_API", "http://localhost:3000/v1")

        logger.info("Agronet server name: $serverName (run ID: ${runContext.runId})")
        logger.info("Dunga-dunga API path: $dungaAPIPath")

        val eventsApi = EventsApiWithContext(
            EventsApi(
                basePath = dungaAPIPath,
                client = OkHttpClient.Builder()
                    .connectTimeout(5.seconds.toJavaDuration())
                    .callTimeout(30.seconds.toJavaDuration())
                    .build()
            ), serverName, runContext
        )

        val inventoryApi = InventoryApi(
            basePath = dungaAPIPath,
            client = OkHttpClient.Builder()
                .connectTimeout(5.seconds.toJavaDuration())
                .callTimeout(30.seconds.toJavaDuration())
                .build()
        )

        val tasksApi = TasksApi(
            basePath = dungaAPIPath,
            client = OkHttpClient.Builder()
                .connectTimeout(5.seconds.toJavaDuration())
                .callTimeout(30.seconds.toJavaDuration())
                .build()
        )

        val scoreApi = ScoreApi(
            basePath = dungaAPIPath,
            client = OkHttpClient.Builder()
                .connectTimeout(5.seconds.toJavaDuration())
                .callTimeout(30.seconds.toJavaDuration())
                .build()
        )

        val claimApi = ClaimApi(
            basePath = dungaAPIPath,
            client = OkHttpClient.Builder()
                .connectTimeout(5.seconds.toJavaDuration())
                .callTimeout(30.seconds.toJavaDuration())
                .build()
        )

        val addDeckToPlayerInventoryAction = AddDeckToPlayerInventoryAction(eventsApi, inventoryApi)
        val removeDeckFromPlayerInventoryAction = RemoveDeckFromPlayerInventoryAction()

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal("take-shulker")
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
            dispatcher.register(
                literal("gief-shulker")
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

        val logEventCommand = LogEventCommand(eventsApi, tasksApi)

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal("log-event")
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

        val cardInteractionCommand = CardInteractionCommand(inventoryApi, eventsApi, serverName)

        listOf("card-bought", "card-played", "card-available").forEach { action ->
            CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
                dispatcher.register(
                    literal(action)
                        .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                        .then(
                            argument("card", StringArgumentType.word()) // words_with_underscores
                                .executes { context ->
                                    cardInteractionCommand.run(context, action)
                                }
                        )
                )
            }
        }

        val itemInteractionCommand = CardInteractionCommand(inventoryApi, eventsApi, serverName)
        listOf("add-item").forEach { action ->
            CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
                dispatcher.register(
                    literal(action)
                        .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                        .then(
                            argument("card", StringArgumentType.word())
                                .then(
                                    argument("count", IntegerArgumentType.integer(1))
                                        .executes { context ->
                                            itemInteractionCommand.run(context, action)
                                        }
                                )
                        )
                )
            }
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
            dispatcher.register(
                literal("update-workers")
                    .requires(Permissions.require("trackedout.serveradmin.update-workers", 4))
                    .executes { context ->
                        sendRedisMessage(context.source, "server-hosts", "update-workers")
                        1
                    })
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal("update-datapack")
                    .requires(Permissions.require("trackedout.update-datapack", 2))
                    .executes { context ->
                        sendRedisMessage(context.source, "datapack-updates", "request-update")
                        1
                    })
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            val sendPlayerToLobby: (context: CommandContext<ServerCommandSource>) -> Int = { context ->
                tasksApi.sendPlayerToLobby(context.source.name)

                context.source.player?.let { sendPlayerToLobby(it) }
                1
            }

            dispatcher.register(literal("leave").executes(sendPlayerToLobby))
            dispatcher.register(literal("lobby").executes(sendPlayerToLobby))
        }

        if (!serverName.equals("builders", ignoreCase = true)) {
            CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
                dispatcher.register(
                    literal("is-dungeon-instance")
                        .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                        .executes { _ -> 1 })
            }
        }

        ServerPlayConnectionEvents.JOIN.register { _: ServerPlayNetworkHandler, _: PacketSender, server: MinecraftServer ->
            val playerListAfterJoin = server.playerManager.playerList
                .filter { player -> !player.commandTags.contains("do2.spectating") }
                .map { player -> player.gameProfile.name }
                .toList()

            sendServerOnlineEvent(eventsApi, playerListAfterJoin)
            allPlayers.forEach {
                sendPlayerSeenEvent(eventsApi, serverName, it)
            }
        }

        if (!serverName.equals("builders", ignoreCase = true)) {
            val scoreListener = AgroNetPlayerConnectionListener(scoreApi, claimApi, tasksApi, addDeckToPlayerInventoryAction)
            ServerPlayConnectionEvents.JOIN.register(scoreListener)
            ServerPlayConnectionEvents.DISCONNECT.register(scoreListener)
            ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(scoreListener)
        }

        ServerTickEvents.START_SERVER_TICK.register {
            allPlayers = it.playerManager.playerList
                .map { player -> player.gameProfile.name }
                .toList()

            activePlayers = it.playerManager.playerList
                .filter { player -> !player.commandTags.contains("do2.spectating") }
                .map { player -> player.gameProfile.name }
                .toList()
        }

        sendServerOnlineEvent(eventsApi, activePlayers)
        threadPool.scheduleAtFixedRate({
            logger.info("Sending server-online event (with player count)")
            sendServerOnlineEvent(eventsApi, activePlayers)

            activePlayers.forEach {
                sendPlayerSeenEvent(eventsApi, serverName, it)
            }
        }, 0, 15, TimeUnit.SECONDS)

        ServerLivingEntityEvents.AFTER_DEATH.register { entity, source ->
            if (entity is ServerPlayerEntity) {
                val killerName = source.attacker?.displayName?.string ?: "unknown"
                val killerType = source.attacker?.type?.name?.string ?: "unknown"
                logger.info("Player ${entity.gameProfile.name} died at ${entity.pos}, killer: $killerName (${killerType})")

                eventsApi.eventsPost(
                    Event(
                        name = "player-died",
                        player = entity.gameProfile.name,
                        x = entity.pos.x,
                        y = entity.pos.y,
                        z = entity.pos.z,
                        count = 1,
                        metadata = mapOf(
                            "killer" to killerName,
                            "killer-type" to killerType,
                        )
                    )
                )
            }
        }

        ServerLifecycleEvents.SERVER_STARTED.register { server: MinecraftServer ->
            val taskManager = TaskManagement(tasksApi, serverName)
            threadPool.scheduleAtFixedRate({
                try {
                    taskManager.fetchAndExecuteTasks(server)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 0, 3, TimeUnit.SECONDS)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            logger.warn("Server shutting down! Sending server-closing event")

            threadPool.shutdown()

            eventsApi.eventsPost(
                Event(
                    name = "server-closing",
                    player = "server",
                    x = 0.0,
                    y = 0.0,
                    z = 0.0,
                    count = 1,
                )
            )
        }

        logger.info("Agronet online. Flee with extra flee!")
    }

    fun sendPlayerToLobby(player: ServerPlayerEntity) {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeString("ConnectOther")
        buf.writeString(player.gameProfile.name)
        buf.writeString("lobby")

        player.networkHandler.sendPacket(
            CustomPayloadS2CPacket(
                Identifier(
                    "bungeecord", "main"
                ),
                buf
            )
        )
    }

    private fun sendServerOnlineEvent(eventsApi: EventsApiWithContext, playerList: List<String>) {
        try {
            eventsApi.eventsPost(
                Event(
                    name = "server-online",
                    player = "server",
                    x = 0.0,
                    y = 0.0,
                    z = 0.0,
                    count = playerList.size,
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to send 'server-online' event: ${e.message}")
        }
    }

    private fun sendPlayerSeenEvent(eventsApi: EventsApiWithContext, serverName: String, playerName: String) {
        try {
            eventsApi.eventsPost(
                Event(
                    name = "player-seen",
                    player = playerName,
                    server = serverName,
                    x = 0.0,
                    y = 0.0,
                    z = 0.0,
                    count = 1,
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to send 'player-seen' event: ${e.message}")
        }
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
