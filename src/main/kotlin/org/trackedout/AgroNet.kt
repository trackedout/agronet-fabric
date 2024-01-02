package org.trackedout

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.CommandOutput
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.Vec2f
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
import java.net.InetAddress
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

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


        AttackBlockCallback.EVENT.register { player, world, hand, pos, direction ->
            val state = world.getBlockState(pos)
            if (state.isDeckedOutDoor()) {
                return@register ActionResult.PASS
            }

            return@register attemptToEnterDungeon(player)
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("enter-dungeon")
                .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                .executes { context ->
                    val player = context.source.player
                    if (player != null) {
                        attemptToEnterDungeon(player)
                    } else {
                        logger.warn("Attempting to enter dungeon but command is not run as a player, ignoring...")
                    }

                    1
                }
            )
        }

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

    private fun getEnvOrDefault(key: String, default: String): String {
        var value = System.getenv(key)
        if (value == null || value.isEmpty()) {
            value = default
        }
        return value
    }

    //TODO Refactor to an action
    private fun attemptToEnterDungeon(player: PlayerEntity): ActionResult {
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
