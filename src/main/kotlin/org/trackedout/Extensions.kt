package org.trackedout

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun PlayerEntity.sendMessage(message: String, format: Formatting) {
    this.sendMessage(Text.literal(message).formatted(format))
}

fun PlayerEntity.debug(message: String) {
    if (this.commandTags.contains("debug")) {
        this.sendMessage(Text.literal(message).formatted(Formatting.GRAY))
    }
}

fun PlayerEntity.hasShulkerInInventory(): Boolean {
    return this.inventory.containsAny { item -> item.isDeckedOutShulker() }
}

fun PlayerEntity.hasKeyInHand(): Boolean {
    return this.handItems.any { item -> item.isDeckedOutKey() }
}

fun PlayerEntity.isReadyToStartDungeonRun(): Boolean {
    return this.hasShulkerInInventory() && this.hasKeyInHand()
}

fun ServerCommandSource.sendMessage(message: String, format: Formatting) {
    this.sendMessage(Text.literal(message).formatted(format))
}

fun ItemStack.isDeckedOutShulker(): Boolean {
    return this.item.name == Items.CYAN_SHULKER_BOX.name
}

fun ItemStack.isDeckedOutKey(): Boolean {
    return this.item.name == Items.ECHO_SHARD.name
}

fun BlockState.isDeckedOutDoor(): Boolean {
    return this.isOf(Block.getBlockFromItem(Items.BLACKSTONE))
}