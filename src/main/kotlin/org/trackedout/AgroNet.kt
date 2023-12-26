package org.trackedout

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
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
import org.trackedout.client.apis.EventsApi
import org.trackedout.client.apis.InventoryApi
import org.trackedout.client.models.Card
import org.trackedout.client.models.EventsPostRequest
import org.trackedout.commands.CardPurchasedCommand
import org.trackedout.commands.LogEventCommand
import java.net.InetAddress
import kotlin.random.Random
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

    private val takeShulkerCommand = { context: CommandContext<ServerCommandSource> ->
        val player = context.source.player
        if (player != null) {
            takeShulkerFromPlayer(player)
        } else {
            logger.warn("Attempting to take shulker but command is not run as a player, ignoring...")
        }

        1
    }

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
                .executes(takeShulkerCommand)
            )
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("gief-shulker")
                .requires { it.hasPermissionLevel(2) } // Command Blocks have permission level of 2
                .executes { context ->
                    val player = context.source.player
                    if (player != null) {
                        val playerCards = inventoryApi.inventoryCardsGet(player = player.name.string, limit = 200, deckId = "1")
                        giveShulkerToPlayer(player, playerCards.results!!)
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
                logger.info("${player.name.string} smacked the door without holding a key, silly player.")
            } else if (!player.hasShulkerInInventory()) {
                player.sendMessage(
                    Text.literal("You need your deck (Shulker Box) in your inventory to enter the Dungeon!")
                        .copy().formatted(Formatting.RED)
                )
                logger.info("${player.name.string} smacked the door without their Shulker Box, silly player.")
            }
            return ActionResult.FAIL
        }

        // The player is ready to enter the dungeon!
        player.sendMessage(Text.literal("Entering dungeon, good luck!").copy().formatted(Formatting.BLUE))
        logger.info("${player.name.string} is entering the dungeon!")
        takeShulkerFromPlayer(player)

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

    private const val RECEIVED_SHULKER = "do2.received_shulker"

    private fun giveShulkerToPlayer(player: PlayerEntity, cards: List<Card>) {
        logger.debug("Player tags: {}", player.commandTags)
        if (player.commandTags.contains(RECEIVED_SHULKER)) {
            logger.info("Player ${player.name.string} already has their shulker box, refusing to give them another one")
            return
        }

        val shulkerNbt =
            StringNbtReader.parse("{${BlockItem.BLOCK_ENTITY_TAG_KEY}:{Items:[],id:\"minecraft:shulker_box\"}}")
        val blockCompound = shulkerNbt.getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY)
        val shulkerItems = blockCompound["Items"] as NbtList

        val cardCount = cards.groupingBy { it.name!! }.eachCount()
        var cardIndex = 0
        cardCount.forEach { (cardName, count) ->
            logger.info("${player.name.string}'s shulker should contain ${count}x $cardName")
            shulkerItems.add(createCard(cardIndex++, cardName, count))
        }
        shulkerNbt.put(BlockItem.BLOCK_ENTITY_TAG_KEY, blockCompound)

        val shulkerBox = ItemStack(Items.SHULKER_BOX)
        shulkerBox.nbt = shulkerNbt

        val inventory = player.inventory
        inventory.insertStack(shulkerBox)
        inventory.updateItems()

        player.addCommandTag(RECEIVED_SHULKER)
    }

    private fun createCard(index: Int, cardName: String, count: Int): NbtCompound {
        val nbt = NbtCompound()
        ItemStack(Items.IRON_NUGGET, count).writeNbt(nbt)
        val tag = NbtCompound()
        // TODO: Use cardName to determine model data ID - https://pastebin.com/DxUsy5rb
        tag.putInt("CustomModelData", Random.nextInt(0, 12345))
        nbt.put("tag", tag)
        nbt.putByte("Slot", index.toByte())

        return nbt
    }
}
