package org.trackedout.listeners

import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.Vec3d
import org.slf4j.LoggerFactory

object CommandBlockOutputListener {
    private val logger = LoggerFactory.getLogger("CommandBlockOutput")
    var server: MinecraftServer? = null

    fun onOutput(command: String, message: Text, pos: Vec3d) {
        logger.info("[CmdBlock] pos=[{}, {}, {}] cmd=`{}` output=`{}`", pos.x.toInt(), pos.y.toInt(), pos.z.toInt(), command, message.string)

        val formattedMessage = Text.literal("[CmdBlock] pos=[${pos.x.toInt()}, ${pos.y.toInt()}, ${pos.z.toInt()}] cmd=`$command` output=`${message.string}`")
            .formatted(Formatting.GRAY)

        server?.playerManager?.playerList
            ?.filter { it.commandTags.contains("do2.cmd-debug") }
            ?.forEach { it.sendMessage(formattedMessage, false) }
    }
}
