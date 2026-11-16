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
import org.trackedout.Agronet.sendServerOnlineEvent
import org.trackedout.EventsApiWithContext
import org.trackedout.RunContext
import org.trackedout.RunContext.serverName
import org.trackedout.actions.AddDeckToPlayerInventoryAction
import org.trackedout.actions.ReturnDungeonItemsToDeckAction
import org.trackedout.client.apis.ClaimApi
import org.trackedout.client.apis.ScoreApi
import org.trackedout.client.models.Claim
import org.trackedout.client.models.Score
import org.trackedout.scoreboard.ScoreSyncer
import org.trackedout.sendMessage
import org.trackedout.runType
import org.trackedout.data.LeagueRegistry

val json = Json { ignoreUnknownKeys = true }

private const val ADVANCEMENT_PREFIX = "advancement-"
private const val LEADERBOARD_PREFIX = "leaderboard-"
private const val INVENTORY_PREFIX = "do2.inventory."
private const val PARTY_PLAYERS_KEY = "party-players"

class AgronetPlayerConnectionListener(
    private val scoreApi: ScoreApi,
    private val claimApi: ClaimApi,
    private val eventsApi: EventsApiWithContext,
    private val addDeckToPlayerInventoryAction: AddDeckToPlayerInventoryAction,
    private val returnDungeonItemsToDeckAction: ReturnDungeonItemsToDeckAction,
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
                initializeRunFromClaim(playerName)
            } else {
                logger.info("Run context is already initialized, skipping claim lookup for ${playerName}'s join event")
            }

            if (RunContext.isRunPlayer(playerName)) {
                setRunTypeScore(server, playerName)
                givePlayerTheirShulker = RunContext.takeDeckHandout(playerName)
            } else if (!RunContext.initialized) {
                handler.player.sendMessage("No matching claim found for your run, contact a moderator (unless you are spectating)", Formatting.RED)
            }

            val slug = RunContext.playerContext(playerName).runType()

            logger.info("Loading scores for $playerName from namespace '$slug'")
            var startTime = System.currentTimeMillis()
            val scores = scoreApi.scoresGet(
                player = playerName,
                slug = slug,
                limit = 10000,
            )
            logger.info("Fetched ${scores.results?.size} scoreboards for $playerName in ${System.currentTimeMillis() - startTime}ms")

            val scoreboards = scores.results!!
                .filter { isDungeonScoreboard(it.key!!) }

            logger.info("Applying scoreboards: ${scoreboards.map { it.key to it.value }}")
            startTime = System.currentTimeMillis()
            scoreboards.forEach {
                val objective = server.scoreboard.getObjective(it.key)
                if (objective == null) {
                    logger.warn("Skipping score '${it.key}' for $playerName: this server has no such objective")
                    return@forEach
                }

                val playerScore = server.scoreboard.getPlayerScore(playerName, objective)
                playerScore.score = it.value!!.toInt()
            }
            logger.info("Finished processing ${scoreboards.size} scoreboards for $playerName in ${System.currentTimeMillis() - startTime}ms")

            applyAdvancements(handler.player, scores.results)

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

    // The run's claim belongs to the player who queued. Party members share it, so
    // whoever joins first (owner or member) loads it for everyone in the party.
    private fun initializeRunFromClaim(playerName: String) {
        val claim = claimApi.claimsGet(
            state = "acquired",
            type = "dungeon",
            claimant = serverName,
        ).results?.firstOrNull { playerName in runPlayersOf(it) }

        if (claim == null) {
            logger.error("No matching claim found for $playerName")
            return
        }

        val runPlayers = runPlayersOf(claim)
        claim.metadata?.let { metadata -> runPlayers.forEach { RunContext.addPlayerContext(it, metadata) } }
        claim.metadata?.get("run-id")?.let { RunContext.runId = it }
        RunContext.claimOwner = claim.player
        RunContext.runPlayers = runPlayers
        // TODO: Store deck-id - https://github.com/trackedout/agronet-fabric/issues/31
        logger.info("Setting state of Claim ${claim.id} to 'in-use' (players: $runPlayers)")
        claimApi.claimsIdPatch(claim.id!!, claim.copy(id = null, state = "in-use", claimant = serverName))
        RunContext.initialized = true
    }

    private fun runPlayersOf(claim: Claim): Set<String> {
        val partyPlayers = claim.metadata?.get(PARTY_PLAYERS_KEY).orEmpty().split(",").filter { it.isNotBlank() }
        return setOfNotNull(claim.player) + partyPlayers
    }

    private fun setRunTypeScore(server: MinecraftServer, playerName: String) {
        val runType = RunContext.playerContext(playerName).runType()
        val objective = server.scoreboard.getObjective("do2.utility.runType")
        val playerScore = server.scoreboard.getPlayerScore(playerName, objective)
        playerScore.score = LeagueRegistry.getRunTypeIdForSlug(runType)
        logger.info("Set do2.utility.runType to ${playerScore.score} (${runType}) for $playerName")
    }

    // A namespace also holds scores that are not dungeon scoreboards: advancements
    // (applied separately), the shard balance and leaderboard copies (lobby side).
    private fun isDungeonScoreboard(key: String): Boolean =
        key.isNotBlank() &&
            !key.startsWith(ADVANCEMENT_PREFIX) &&
            !key.startsWith(LEADERBOARD_PREFIX) &&
            !key.startsWith(INVENTORY_PREFIX)

    private fun applyAdvancements(
        player: ServerPlayerEntity,
        results: List<Score>,
    ) {
        val startTime = System.currentTimeMillis()
        val tracker = player.advancementTracker

        val playerName = player.entityName
        val server = player.server

        val advancements = results
            .filter { it.key!!.startsWith(ADVANCEMENT_PREFIX) }
            .map { it.copy(key = it.key?.substring(ADVANCEMENT_PREFIX.length)) }
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

    override fun onPlayDisconnect(handler: ServerPlayNetworkHandler, server: MinecraftServer) {
        if (serverName.startsWith("builders", ignoreCase = true)) {
            return
        }

        val playerName = handler.player.entityName
        if (playerName.equals("TangoCam", ignoreCase = true)) {
            return
        }

        if (RunContext.gameWon) {
            returnDungeonItemsToDeckAction.execute(handler.player)
        }

        val playerListWithoutLeavingPlayer = server.playerManager.playerList
            .filter { player -> !player.commandTags.contains("do2.spectating") }
            .filter { player -> !player.entityName.equals(playerName, ignoreCase = true) }
            .map { player -> player.gameProfile.name }
            .toList()

        // We want dunga-dunga to shut down the server if no players are left after this player leaves
        sendServerOnlineEvent(eventsApi, playerListWithoutLeavingPlayer)

        scoreSyncer.syncPlayerScoreboard(server, handler.player)
    }
}
