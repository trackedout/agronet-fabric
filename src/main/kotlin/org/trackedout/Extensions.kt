package org.trackedout

import net.minecraft.entity.player.PlayerEntity
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

fun ServerCommandSource.sendMessage(message: String, format: Formatting) {
    this.sendMessage(Text.literal(message).formatted(format))
}
