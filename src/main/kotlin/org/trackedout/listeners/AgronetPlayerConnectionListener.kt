package org.trackedout.listeners

import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.GameRules
import org.slf4j.LoggerFactory
import org.trackedout.RunContext
import org.trackedout.RunContext.serverName
import org.trackedout.actions.AddDeckToPlayerInventoryAction
import org.trackedout.client.apis.ClaimApi
import org.trackedout.client.apis.ScoreApi
import org.trackedout.client.models.Score
import org.trackedout.data.getRunTypeById
import org.trackedout.fullRunType
import org.trackedout.scoreboard.ScoreSyncer
import org.trackedout.sendMessage
import org.trackedout.shortRunType

val json = Json { ignoreUnknownKeys = true }

class AgronetPlayerConnectionListener(
    private val scoreApi: ScoreApi,
    private val claimApi: ClaimApi,
    private val addDeckToPlayerInventoryAction: AddDeckToPlayerInventoryAction,
    private val scoreSyncer: ScoreSyncer,
) : ServerPlayConnectionEvents.Join, ServerPlayConnectionEvents.Disconnect, SimpleSynchronousResourceReloadListener {
    private val logger = LoggerFactory.getLogger("ServerPlayConnectionJoin")

    override fun getFabricId(): Identifier {
        return Identifier("agronet", "scoreboard")
    }

    // Use data from Brilliance to determine which objectives to store, and Card limits
    override fun reload(resourceManager: ResourceManager) {
        scoreSyncer.reload(resourceManager)
    }

    override fun onPlayReady(handler: ServerPlayNetworkHandler, sender: PacketSender, server: MinecraftServer) {
        val playerName = handler.player.entityName
        if (playerName.equals("TangoCam", ignoreCase = true)) {
            return
        }

        val joinEventStartTime = System.currentTimeMillis()

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

                    claim.metadata?.shortRunType()?.let { runType ->
                        val objective = server.scoreboard.getObjective("do2.utility.runType")
                        val playerScore = server.scoreboard.getPlayerScore(playerName, objective)
                        playerScore.score = getRunTypeById(runType).runTypeId
                        logger.info("Set do2.utility.runType to ${playerScore.score} (${runType}) for $playerName")
                    } ?: run { logger.error("No run-type found for ${playerName}! Unable to set do2.utility.runType") }

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
            var startTime = System.currentTimeMillis()
            val scores = scoreApi.scoresGet(
                player = playerName,
                prefixFilter = filter,
                limit = 10000,
            )
            logger.info("Fetched ${scores.results?.size} scoreboards for $playerName in ${System.currentTimeMillis() - startTime}ms")

            val scoreboards = scores.results!!
                .filter { it.key!!.startsWith(filter) }
                .filter { !it.key!!.startsWith(advancementFilter) }
                .map { it.copy(key = it.key?.substring(filter.length)) }
                .filter { it.key!!.isNotBlank() }

            logger.info("Applying scoreboards: ${scoreboards.map { it.key to it.value }}")
            startTime = System.currentTimeMillis()
            scoreboards.forEach {
                val objective = server.scoreboard.getObjective(it.key)

                val playerScore = server.scoreboard.getPlayerScore(playerName, objective)
                playerScore.score = it.value!!.toInt()
            }
            logger.info("Finished processing ${scoreboards.size} scoreboards for $playerName in ${System.currentTimeMillis() - startTime}ms")

            applyAdvancements(handler.player, scores.results, advancementFilter)

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

        logger.info("Finished handling join event for $playerName in ${System.currentTimeMillis() - joinEventStartTime}ms")
    }

    private fun applyAdvancements(
        player: ServerPlayerEntity,
        results: List<Score>,
        advancementFilter: String,
    ) {
        val startTime = System.currentTimeMillis()
        val tracker = player.advancementTracker

        val playerName = player.entityName
        val server = player.server

        val advancements = results
            .filter { it.key!!.startsWith(advancementFilter) }
            .map { it.copy(key = it.key?.substring(advancementFilter.length)) }
            .filter { it.key!!.isNotBlank() && it.key.contains("#") && !it.key.contains("visible") && !it.key.contains("utility") }
            .filter { it.value!!.toInt() > 0 }

        logger.info("Applying ${advancements.size} advancements for $playerName")
        server.gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server)

        val advancementProgressMap = mutableMapOf<String, List<String>>()
        advancements.forEach { score ->
            val split = score.key!!.split("#")
            var namespace = "do2"
            var key = split[0]
            var criterion = split[1]

            if (split.size == 3) {
                namespace = split[0]
                key = split[1]
                criterion = split[2]
            }

            val nsKey = "$namespace#$key"
            advancementProgressMap[nsKey] = advancementProgressMap.getOrDefault(nsKey, listOf()) + criterion
        }

        advancements.forEach { score ->
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
                if (obtained == null || !obtained) {
                    tracker.grantCriterion(advancement, criterion)
//                    logger.info("Granted Advancement progress $key (criterion: $criterion) to $playerName")
//                    logger.info("Advancement: ${tracker.getProgress(advancement)}")
                } else {
//                    logger.info("$playerName already has advancement progress $key (criterion: $criterion)")
                }
            }
        }

        server.gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, server)
        logger.info("Finished processing ${advancements.size} advancements for $playerName in ${System.currentTimeMillis() - startTime}ms")
    }

    private fun getFullRunType(playerName: String): String = RunContext.playerContext(playerName).fullRunType().lowercase()

    override fun onPlayDisconnect(handler: ServerPlayNetworkHandler, server: MinecraftServer) {
        if (serverName.startsWith("builders", ignoreCase = true)) {
            return
        }

        val playerName = handler.player.entityName
        if (playerName.equals("TangoCam", ignoreCase = true)) {
            return
        }

        scoreSyncer.syncPlayerScoreboard(server, handler.player)
    }
}
