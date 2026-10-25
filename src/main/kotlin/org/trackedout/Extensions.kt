package org.trackedout

import io.netty.buffer.Unpooled
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.trackedout.client.apis.TasksApi
import org.trackedout.client.models.Task
import org.trackedout.client.models.TasksIdPatchRequest
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/**
 * Builds a BungeeCord/Velocity plugin-message payload for the "bungeecord:main" channel.
 *
 * The proxy reads these messages with DataInputStream.readUTF, which expects Java
 * Modified-UTF-8 (a 2-byte unsigned-short length prefix). PacketByteBuf.writeString uses a
 * VarInt length prefix instead, so writing the arguments with writeString produces a payload
 * the proxy misreads, corrupting the length and crashing its BungeeCordMessageResponder. Each
 * argument must therefore be encoded with DataOutputStream.writeUTF and the raw bytes written
 * into the buffer verbatim.
 */
fun bungeeCordMessage(vararg arguments: String): PacketByteBuf {
    val bytes = ByteArrayOutputStream()
    DataOutputStream(bytes).use { out ->
        arguments.forEach(out::writeUTF)
    }
    return PacketByteBuf(Unpooled.wrappedBuffer(bytes.toByteArray()))
}

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
