package org.trackedout

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.trackedout.client.apis.TasksApi
import org.trackedout.client.models.Task
import org.trackedout.client.models.TasksIdPatchRequest

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

fun ItemStack.isDeckedOutShulker(): Boolean {
    return this.item.name == Items.CYAN_SHULKER_BOX.name
}

fun Task.updateState(api: TasksApi, state: String) {
    api.tasksIdPatch(this.id!!, TasksIdPatchRequest(state))
}

fun TasksApi.sendPlayerToLobby(playerName: String) {
    this.tasksPost(
        Task(
            server = "lobby",
            type = "bungee-message",
            arguments = listOf("ConnectOther", playerName, "lobby"),
        )
    )
}
