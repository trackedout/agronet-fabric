package org.trackedout

import net.minecraft.server.MinecraftServer
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
            handleTask(it, server)
        }
    }

    private fun handleTask(task: Task, server: MinecraftServer) {
        when (task.type) {
            "shutdown-server-if-empty" -> {
                if (server.playerManager.playerList.isEmpty()) {
                    logger.warn("Shutting down empty server as per dunga-dunga request")
                    if (!server.isStopping) {
                        server.shutdown()
                    }
                } else {
                    logger.warn("Server shutdown request ignored as ${server.playerManager.playerList.size} are online")
                }
            }

            else -> throw Exception("Unknown command type '${task.type}'")
        }
    }
}
