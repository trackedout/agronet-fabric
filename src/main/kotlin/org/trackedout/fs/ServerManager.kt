package org.trackedout.fs

import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandOutput
import net.minecraft.text.Text
import org.trackedout.RunContext.serverName

private val SANDBOX_SLOT = Regex("^s4\\d\\d$")

/**
 * True for edit environments (builders and sandbox slots s400–s409) where
 * players run commands directly and expect to see their output.
 */
private fun isEditServer(): Boolean =
    serverName.startsWith("builders", ignoreCase = true) || SANDBOX_SLOT.matches(serverName)

/**
 * Re-assert gamerules that the Brilliance datapack disables (reducedDebugInfo=true
 * and sendCommandFeedback=false). On builder/sandbox servers we want command
 * feedback and full debug info, so this must run at startup, after the datapack
 * finishes reloading (the prepare-for-player datapack-load event handler), and
 * before commands that rely on feedback. No-op on other servers.
 */
fun MinecraftServer.enforceEditGamerules() {
    if (!isEditServer()) return
    executeConsoleCommand("gamerule sendCommandFeedback true")
    executeConsoleCommand("gamerule reducedDebugInfo false")
    logger.info("Enforced edit-server gamerules (sendCommandFeedback=true, reducedDebugInfo=false)")
}

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
