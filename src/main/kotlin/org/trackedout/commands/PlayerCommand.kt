package org.trackedout.commands

import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface PlayerCommand {
    val logger: Logger
        get() = LoggerFactory.getLogger("Agro-net")

    fun run(context: CommandContext<ServerCommandSource>): Int
}
