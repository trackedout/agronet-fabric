package org.trackedout

import ServerTickListener
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.StringNbtReader
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.CommandOutput
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.Vec2f
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import org.trackedout.client.apis.UsersApi
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


object AgroNet : ModInitializer {
    private val logger = LoggerFactory.getLogger("Agro-net")

    private fun isDeckedOutShulker(it: ItemStack) = it.item.name == Items.SHULKER_BOX.name
    private fun isDeckedOutKey(it: ItemStack) = it.item.name == Items.ECHO_SHARD.name

    private fun isDeckedOutDoor(state: BlockState) = state.isOf(Block.getBlockFromItem(Items.BLACKSTONE))

    private fun PlayerEntity.isReadyToStartDungeonRun(): Boolean {
        return hasShulkerInInventory() && this.hasKeyInHand()
    }

    private fun PlayerEntity.hasShulkerInInventory() = this.inventory.containsAny(::isDeckedOutShulker)

    private fun PlayerEntity.hasKeyInHand(): Boolean = this.handItems.any(::isDeckedOutKey)

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

        AttackBlockCallback.EVENT.register { player, world, hand, pos, direction ->
            val state = world.getBlockState(pos)
            if (!isDeckedOutDoor(state)) {
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
                })
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("take-shulker")
                .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                .executes { context ->
                    val player = context.source.player
                    if (player != null) {
                        takeShulkerFromPlayer(player)
                    } else {
                        logger.warn("Attempting to take shulker but command is not run as a player, ignoring...")
                    }

                    1
                })
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("gief-shulker")
                .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                .executes { context ->
                    val player = context.source.player
                    if (player != null) {
                        giveShulkerToPlayer(player)
                    } else {
                        logger.warn("Attempting to give shulker but command is not run as a player, ignoring...")
                    }

                    1
                })
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("log-event")
                .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                .then(
                    argument("event", StringArgumentType.word()) // words_with_underscores
                        .then(argument(
                            "count", // Number of units for this event
                            IntegerArgumentType.integer(1)
                        )
                            .executes { context ->
                                val event = StringArgumentType.getString(context, "event")
                                val count = IntegerArgumentType.getInteger(context, "count")

                                val player = context.source.player
                                if (player == null) {
                                    logger.warn("Attempting to run /log-event { event=${event}, count=${count} }, but command is not run as a player, ignoring...")
                                    context.source.sendFeedback(
                                        { Text.literal("Attempting to run log-event command, but command is not run as a player, ignoring...") },
                                        true
                                    )

                                    return@executes -1
                                }

                                val x = player.x
                                val y = player.y
                                val z = player.z

                                context.source.sendFeedback(
                                    {
                                        Text.literal(
                                            "Processing /log-event { event=${event}, count=${count} } for player ${player.name} " +
                                                    "at location [$x, $y, $z]"
                                        )
                                    },
                                    true
                                )

                                try {
//                                            val someUser = eventsApi.eventsPost(
//                                                EventsPostRequest(
//                                                    name = event,
//                                                    player = player,
//                                                )
//                                            )

//                                            context.source.sendFeedback(
//                                                { Text.literal("Some user: $someUser") },
//                                                true
//                                            )
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
        }

        logger.info("Agro-net online. Flee with extra flee!")
    }

    private fun attemptToEnterDungeon(player: PlayerEntity): ActionResult {
        if (player.isSpectator) {
            player.sendMessage(Text.literal("Sorry, spectators may not enter the dungeon this way").copy().formatted(Formatting.RED))
            logger.warn("Spectator attempting to enter dungeon, rejecting!")
            return ActionResult.FAIL
        }

        if (player.isCreative) {
            return ActionResult.PASS
        }

        if (!player.isReadyToStartDungeonRun()) {
            if (!player.hasKeyInHand()) {
                player.sendMessage(Text.literal("You need to be holding a key to enter the Dungeon!").copy().formatted(Formatting.RED))
                logger.info("${player.name} smacked the door without holding a key, silly player.")
            } else if (!player.hasShulkerInInventory()) {
                player.sendMessage(
                    Text.literal("You need your deck (Shulker Box) in your inventory to enter the Dungeon!")
                        .copy().formatted(Formatting.RED)
                )
                logger.info("${player.name} smacked the door without their Shulker Box, silly player.")
            }
            return ActionResult.FAIL
        }

        // The player is ready to enter the dungeon!
        player.sendMessage(Text.literal("Entering dungeon, good luck!").copy().formatted(Formatting.BLUE))
        logger.info("${player.name} is entering the dungeon!")
        takeShulkerFromPlayer(player)

        val commandSource = ServerCommandSource(
            CommandOutput.DUMMY,
            player.pos,
            Vec2f.ZERO,
            player.world as ServerWorld,
            2,
            player.name.toString(),
            player.displayName,
            player.world.server,
            player
        )
        player.server!!.commandManager.executeWithPrefix(commandSource, "/tp 14 137 136")
        player.server!!.commandManager.executeWithPrefix(commandSource, "/proxycommand \"send ${player.name.string} lobby\"")
        player.playSound(SoundEvents.ENTITY_WARDEN_EMERGE, player.soundCategory, 1.0f, 1.0f)

        return ActionResult.SUCCESS
    }

    private fun takeShulkerFromPlayer(player: PlayerEntity) {
        player.inventory.remove(
            { item ->
                if (isDeckedOutShulker(item)) {
                    logger.info("Removing shulker!")
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

        player.inventory.updateItems()
    }

    private fun giveShulkerToPlayer(player: PlayerEntity) {
        // TODO: Validate that the player does not already have their shulker
        val shulkerBox = ItemStack(Items.SHULKER_BOX)
        // TODO: Fetch this data from DungaDunga
        shulkerBox.nbt =
            StringNbtReader.parse("{BlockEntityTag:{Items:[{Count:1b,Slot:0b,id:\"minecraft:redstone\"},{Count:1b,Slot:1b,id:\"minecraft:chest\"},{Count:1b,Slot:2b,id:\"minecraft:slime_block\"},{Count:1b,Slot:3b,id:\"minecraft:redstone_torch\"},{Count:1b,Slot:4b,id:\"minecraft:redstone_block\"}],id:\"minecraft:shulker_box\"}}")
        //                        shulkerBox.nbt[BlockItem.BLOCK_ENTITY_TAG_KEY].toString() // FYI that's the tag for *just* the blocks inside the shulker

        val inventory = player.inventory
        inventory.insertStack(shulkerBox)
        inventory.updateItems()
    }
}
