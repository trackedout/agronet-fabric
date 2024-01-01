package org.trackedout.listeners

import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import org.slf4j.LoggerFactory
import org.trackedout.actions.AddDeckToPlayerInventoryAction

class AgroNetServerPlayConnectionListener(private val addDeckToPlayerInventoryAction: AddDeckToPlayerInventoryAction) : ServerPlayConnectionEvents.Join {
    private val logger = LoggerFactory.getLogger("ServerPlayConnectionJoin")

    override fun onPlayReady(handler: ServerPlayNetworkHandler?, sender: PacketSender?, server: MinecraftServer?) {
        logger.debug("onPlayReady", handler, sender, server);
        handler?.player?.let {player ->
            server?.commandSource?.let {commandSource ->
                addDeckToPlayerInventoryAction.execute(commandSource, player)
            }
        }
    }
}