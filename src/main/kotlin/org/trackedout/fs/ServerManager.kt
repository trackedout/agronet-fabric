package org.trackedout.fs

import net.minecraft.server.MinecraftServer

fun MinecraftServer.reloadServer() {
    commandManager.executeWithPrefix(this.commandSource, "reload")
}
