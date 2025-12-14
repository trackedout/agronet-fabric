package org.trackedout.fs

import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandOutput
import net.minecraft.text.Text

fun MinecraftServer.reloadServer() {
    this.executeConsoleCommand("reload").forEach {
        logger.info("[output from 'reload'] $it")
    }
}

fun MinecraftServer.executeConsoleCommand(cmd: String): List<String> {
    val lines = mutableListOf<String>()

    val out = object : CommandOutput {
        override fun sendMessage(message: Text) {
            lines.add(message.string)
        }

        override fun shouldReceiveFeedback() = true
        override fun shouldTrackOutput() = true
        override fun shouldBroadcastConsoleToOps() = false
    }

    val source = this.commandSource.withOutput(out)

    this.commandManager.dispatcher.execute(cmd, source)

    return lines
}
