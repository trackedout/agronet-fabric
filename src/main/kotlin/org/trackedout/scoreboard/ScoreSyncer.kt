package org.trackedout.scoreboard

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.minecraft.resource.ResourceManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import org.trackedout.RunContext
import org.trackedout.RunContext.serverName
import org.trackedout.client.apis.ScoreApi
import org.trackedout.client.apis.TasksApi
import org.trackedout.client.models.Score
import org.trackedout.client.models.Task
import org.trackedout.data.BrillianceCard
import org.trackedout.data.BrillianceScoreboardDescription
import org.trackedout.fullRunType
import org.trackedout.runType
import java.nio.charset.StandardCharsets

val json = Json { ignoreUnknownKeys = true }

class ScoreSyncer(
    private val scoreApi: ScoreApi,
    private val tasksApi: TasksApi,
) {
    private val logger = LoggerFactory.getLogger("ServerPlayConnectionJoin")

    private var objectivesToStore = listOf<String>()

    // Use data from Brilliance to determine which Objectives to store, and Card limits
    @Synchronized
    fun reload(resourceManager: ResourceManager) {
        parseBrillianceData(resourceManager, "scoreboards.json") { jsonString ->
            val map = json.decodeFromString<Map<String, BrillianceScoreboardDescription>>(jsonString)
            objectivesToStore = map.filter { it.value.category == "totals" }.keys.toList()
            println("Updated objectives to store to: $objectivesToStore")
        }

        parseBrillianceData(resourceManager, "cards.json") { jsonString ->
            val jsonElement = json.parseToJsonElement(jsonString).jsonObject
            RunContext.brillianceCards = jsonElement.mapValues { (_, value) ->
                val rawTag = value.jsonObject["tag"]?.toString() ?: error("Missing tag field")
                val brillianceCard = json.decodeFromJsonElement(BrillianceCard.serializer(), value)
                brillianceCard.copy(tagRaw = rawTag)
            }

            println("Card data from Brilliance: ${RunContext.brillianceCards}")
        }
    }

    private inline fun parseBrillianceData(resourceManager: ResourceManager, fileName: String, unit: (String) -> Unit) {
        val resourceId = Identifier("brilliance-data", fileName)

        try {
            // Collect the resource as an InputStream
            val resource = resourceManager.getResource(resourceId).orElseThrow {
                throw IllegalStateException("Resource $fileName not found: $resourceId")
            }

            // Read and parse the JSON file using Gson
            resource.inputStream.use { inputStream ->
                val jsonData = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                unit(jsonData)
            }
        } catch (e: Exception) {
            println("Failed to load $fileName: ${e.message}")
        }
    }

    @Synchronized
    fun syncPlayerScoreboard(server: MinecraftServer, player: ServerPlayerEntity) {
        if (serverName.startsWith("builders", ignoreCase = true)) {
            return
        }

        val playerName = player.entityName
        if (playerName.equals("TangoCam", ignoreCase = true)) {
            return
        }

        logger.info("Starting to sync scoreboard values for $playerName")
        try {
            val batchMap = server.scoreboard.getPlayerObjectives(playerName)
                // Filter for objectives in the "totals" category: https://github.com/trackedout/Brilliance/blob/main/JSON/scoreboards.json
                .filter { objective -> (objective.key?.name)?.let { objectivesToStore.contains(it) } ?: false }.map { objective ->
                    objective.key.name to objective.value.score
                }.toMap().toMutableMap()

            batchMap += server.advancementLoader.advancements.asSequence()
                .filter { !it.id.path.startsWith("visible/") }
                .filter { !it.id.path.startsWith("utility/") }
                .filter { player.advancementTracker.getProgress(it).isAnyObtained }.flatMap {
                    val progress = player.advancementTracker.getProgress(it)
                    it.criteria.entries.map { entry ->
                        val obtained: Boolean? = progress.getCriterionProgress(entry.key)?.isObtained
                        val value = if (obtained != null && obtained) 1 else 0

                        "advancement-${it.id.namespace}#${it.id.path}#${entry.key}" to value
                    }
                }.filter { it.second > 0 }.toList()

            player.advancementTracker.save()

            if (batchMap.isEmpty()) {
                logger.info("$playerName does not have any applicable objectives, skipping store call")
                return
            }

            logger.info("Storing ${batchMap.size} objectives for player $playerName")
            logger.info("BatchMap: ${Json.encodeToString(batchMap)}")

            val metadata = mapOf(
                "run-id" to RunContext.runId,
                "run-type" to RunContext.playerContext(playerName).runType(),
            )

            // Chunk batchMap into batches of 100 entries
            batchMap.entries.chunked(100).forEach { chunk ->
                logger.info("Storing chunk of ${chunk.size} objectives for player $playerName")
                logger.info("Chunk: ${Json.encodeToString(chunk.associate { it.key to it.value })}")

                scoreApi.scoresPost(
                    chunk.map {
                        Score(
                            player = playerName,
                            key = "${getFullRunType(playerName)}-${it.key}",
                            value = it.value.toBigDecimal(),
                            metadata = metadata,
                        )
                    })
            }

            logger.info("Successfully stored ${batchMap.size} objectives for player $playerName with metadata: ${Json.encodeToString(metadata)}")

            tasksApi.tasksPost(
                Task(
                    server = "lobby",
                    type = "update-inventory",
                    targetPlayer = playerName,
                    arguments = listOf(),
                )
            )

            logger.info("Created update-inventory task for player $playerName")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFullRunType(playerName: String): String = RunContext.playerContext(playerName).fullRunType().lowercase()
}
