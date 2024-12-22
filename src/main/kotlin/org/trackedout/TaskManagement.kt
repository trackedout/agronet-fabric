package org.trackedout

import io.netty.buffer.Unpooled
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import org.trackedout.client.apis.TasksApi
import org.trackedout.client.models.Task


class TaskManagement(
    private val tasksApi: TasksApi,
    private val serverName: String,
) {
    private val logger = LoggerFactory.getLogger("Agronet")

    fun fetchAndExecuteTasks(server: MinecraftServer) {
        logger.debug("Fetching Tasks from Dunga Dunga")
        val tasks = tasksApi.tasksGet(
            server = serverName,
            limit = 10,
            state = "SCHEDULED",
        ).results!!

        tasks.forEach {
            logger.info("Handling task: $it")
            it.updateState(tasksApi, "IN_PROGRESS")
            try {
                handleTask(it, server)
            } catch (e: Exception) {
                logger.error("Failed to execute task $it")
                e.printStackTrace()
            }
        }
    }

    private fun handleTask(task: Task, server: MinecraftServer) {
        when (task.type) {
            "shutdown-server-if-empty" -> {
                if (server.playerManager.playerList.isEmpty()) {
                    logger.warn("Shutting down empty server as per dunga-dunga request")
                    if (server.isRunning) {
                        server.stop(false)
                    }
                } else {
                    logger.warn("Server shutdown request ignored as ${server.playerManager.playerList.size} are online")
                }
            }

            "broadcast-message" -> {
                task.arguments?.forEach { message ->
                    server.overworld.players.forEach { player ->
                        player.sendMessage(Text.literal(message).formatted(Formatting.DARK_AQUA))
                    }
                }
            }

            "bungee-message" -> {
                if (task.targetPlayer != null) {
                    val targetPlayer = server.overworld.players.find { it.gameProfile.name == task.targetPlayer }
                    if (targetPlayer != null) {
                        val buf = PacketByteBuf(Unpooled.buffer())
                        task.arguments?.forEach(buf::writeString)

                        targetPlayer.networkHandler.sendPacket(
                            CustomPayloadS2CPacket(
                                Identifier(
                                    "bungeecord", "main"
                                ),
                                buf
                            )
                        )
                    } else {
                        val message = "Task type is '${task.type}' and targets a player, but the player was not found"
                        logger.warn(message)
                        throw Exception(message)
                    }
                }
            }

            else -> throw Exception("Unknown command type '${task.type}'")
        }
    }
}
