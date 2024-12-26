package org.trackedout.listeners

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.GameRules
import org.slf4j.LoggerFactory
import org.trackedout.RunContext
import org.trackedout.RunContext.serverName
import org.trackedout.actions.AddDeckToPlayerInventoryAction
import org.trackedout.client.apis.ClaimApi
import org.trackedout.client.apis.ScoreApi
import org.trackedout.client.apis.TasksApi
import org.trackedout.client.models.Score
import org.trackedout.client.models.Task
import org.trackedout.data.BrillianceCard
import org.trackedout.data.BrillianceScoreboardDescription
import org.trackedout.fullRunType
import org.trackedout.runType
import org.trackedout.sendMessage
import java.nio.charset.StandardCharsets

val json = Json { ignoreUnknownKeys = true }

class AgroNetPlayerConnectionListener(
    private val scoreApi: ScoreApi,
    private val claimApi: ClaimApi,
    private val tasksApi: TasksApi,
    private val addDeckToPlayerInventoryAction: AddDeckToPlayerInventoryAction,
) : ServerPlayConnectionEvents.Join, ServerPlayConnectionEvents.Disconnect, SimpleSynchronousResourceReloadListener {
    private val logger = LoggerFactory.getLogger("ServerPlayConnectionJoin")

    private var objectivesToStore = listOf<String>()

    override fun getFabricId(): Identifier {
        return Identifier("agronet", "scoreboard")
    }

    // Use data from Brilliance to determine which objectives to store, and Card limits
    override fun reload(resourceManager: ResourceManager) {
        parseBrillianceData<Map<String, BrillianceScoreboardDescription>>(resourceManager, "scoreboards.json") { map ->
            objectivesToStore = map.filter { it.value.category == "totals" }.keys.toList()
            println("Updated objectives to store to: $objectivesToStore")
        }

        parseBrillianceData<Map<String, BrillianceCard>>(resourceManager, "cards.json") { map ->
            RunContext.brillianceCards = map
            println("Card data from Brilliance: ${RunContext.brillianceCards}")
        }
    }

    private inline fun <reified T> parseBrillianceData(resourceManager: ResourceManager, fileName: String, unit: (T) -> Unit) {
        val resourceId = Identifier("brilliance-data", fileName)

        try {
            // Obtain the resource as an InputStream
            val resource = resourceManager.getResource(resourceId).orElseThrow {
                throw IllegalStateException("Resource $fileName not found: $resourceId")
            }

            // Read and parse the JSON file using Gson
            resource.inputStream.use { inputStream ->
                val jsonData = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                unit(json.decodeFromString<T>(jsonData))
            }
        } catch (e: Exception) {
            println("Failed to load $fileName: ${e.message}")
        }
    }

    override fun onPlayReady(handler: ServerPlayNetworkHandler, sender: PacketSender, server: MinecraftServer) {
        val playerName = handler.player.entityName
        if (playerName.equals("TangoCam", ignoreCase = true)) {
            return
        }

        var givePlayerTheirShulker = false
        try {
            if (!RunContext.initialized) {
                claimApi.claimsGet(
                    player = playerName,
                    state = "acquired",
                    type = "dungeon",
                    claimant = serverName,
                ).results?.firstOrNull()?.let { claim ->
                    claim.metadata?.let { RunContext.addPlayerContext(playerName, it) }
                    claim.metadata?.get("run-id")?.let { RunContext.runId = it }
                    // TODO: Store deck-id - https://github.com/trackedout/agronet-fabric/issues/31
                    logger.info("Setting state of Claim ${claim.id} to 'in-use'")
                    claimApi.claimsIdPatch(claim.id!!, claim.copy(id = null, state = "in-use", claimant = serverName))
                    givePlayerTheirShulker = true
                    RunContext.initialized = true
                } ?: run {
                    logger.error("No matching claim found for $playerName")
                    handler.player.sendMessage("No matching claim found for your run, contact a moderator (unless you are spectating)", Formatting.RED)
                }
            } else {
                logger.info("Run context is already initialized, skipping processing from ${playerName}'s join event")
            }

            val runType = getFullRunType(playerName)
            val filter = "$runType-"
            val advancementFilter = "$runType-advancement-"

            logger.info("Scoreboard filter: $filter")
            val scores = scoreApi.scoresGet(
                player = playerName,
                limit = 10000,
            )

            scores.results!!.filter { it.key!!.startsWith(filter) }.filter { !it.key!!.startsWith(advancementFilter) }.map { it.copy(key = it.key?.substring(filter.length)) }
                .filter { it.key!!.isNotBlank() }.forEach {
                    val objective = server.scoreboard.getObjective(it.key)

                    val playerScore = server.scoreboard.getPlayerScore(playerName, objective)
                    playerScore.score = it.value!!.toInt()
                    logger.info("Set ${it.key} to ${it.value} for $playerName")
                }

            val tracker = handler.player.advancementTracker
            server.gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server)

            scores.results.filter { it.key!!.startsWith(advancementFilter) }.map { it.copy(key = it.key?.substring(advancementFilter.length)) }
                .filter { it.key!!.isNotBlank() && it.key.contains("#") }.filter { it.value!!.toInt() > 0 }.forEach { score ->
                    val split = score.key!!.split("#")
                    var namespace = "do2"
                    var key = split[0]
                    var criterion = split[1]

                    if (split.size == 3) {
                        namespace = split[0]
                        key = split[1]
                        criterion = split[2]
                    }

                    server.advancementLoader.advancements.find { it.id.namespace == namespace && it.id.path == key }?.let { advancement ->
                        val obtained: Boolean? = tracker.getProgress(advancement).getCriterionProgress(criterion)?.isObtained
                        if (obtained == null || obtained == false) {
                            tracker.grantCriterion(advancement, criterion)
                            logger.info("Granted Advancement progress $key (criterion: $criterion) to $playerName")
                            logger.info("Advancement: ${tracker.getProgress(advancement)}")
                        } else {
                            logger.info("$playerName already has advancement progress $key (criterion: $criterion)")
                        }
                    }
                }

        } catch (e: Exception) {
            e.printStackTrace()
            handler.player.sendMessage(
                "A critical error occurred when attempting to fetch your data from dunga-dunga, " +
                    "and your data could not be imported. Contact a moderator.", Formatting.RED
            )
        }

        if (givePlayerTheirShulker) {
            handler.player?.let { player ->
                server.commandSource?.let { commandSource ->
                    try {
                        addDeckToPlayerInventoryAction.execute(commandSource, player)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        server.gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, server)
    }

    override fun onPlayDisconnect(handler: ServerPlayNetworkHandler, server: MinecraftServer) {
        logger.info("onPlayDisconnect")
        if (serverName.equals("builders", ignoreCase = true)) {
            return
        }

        val playerName = handler.player.entityName
        if (playerName.equals("TangoCam", ignoreCase = true)) {
            return
        }

        try {
            val batchMap = server.scoreboard.getPlayerObjectives(playerName)
                // Filter for objectives in the "totals" category: https://github.com/trackedout/Brilliance/blob/main/JSON/scoreboards.json
                .filter { objective -> (objective.key?.name)?.let { objectivesToStore.contains(it) } ?: false }.map { objective ->
                    objective.key.name to objective.value.score
                }.toMap().toMutableMap()

            batchMap += server.advancementLoader.advancements.asSequence().filter { !it.id.path.startsWith("visible/credits/") }
                .filter { handler.player.advancementTracker.getProgress(it).isAnyObtained }.flatMap {
                    val progress = handler.player.advancementTracker.getProgress(it)
                    it.criteria.entries.map { entry ->
                        val obtained: Boolean? = progress.getCriterionProgress(entry.key)?.isObtained
                        val value = if (obtained != null && obtained == true) 1 else 0

                        "advancement-${it.id.namespace}#${it.id.path}#${entry.key}" to value
                    }
                }.filter { it.second > 0 }.toList()

            handler.player.advancementTracker.save()

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
            scoreApi.scoresPost(
                batchMap.map {
                    Score(
                        player = playerName,
                        key = "${getFullRunType(playerName)}-${it.key}",
                        value = it.value.toBigDecimal(),
                        metadata = metadata,
                    )
                })

            logger.info("Successfully stored ${batchMap.size} objectives for player $playerName")

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
