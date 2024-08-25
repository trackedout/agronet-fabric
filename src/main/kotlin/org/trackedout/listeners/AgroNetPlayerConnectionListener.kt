package org.trackedout.listeners

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.util.Formatting
import net.minecraft.world.GameRules
import org.slf4j.LoggerFactory
import org.trackedout.RunContext
import org.trackedout.RunContext.serverName
import org.trackedout.actions.AddDeckToPlayerInventoryAction
import org.trackedout.client.apis.ClaimApi
import org.trackedout.client.apis.ScoreApi
import org.trackedout.client.models.Score
import org.trackedout.fullRunType
import org.trackedout.sendMessage

class AgroNetPlayerConnectionListener(
    private val scoreApi: ScoreApi,
    private val claimApi: ClaimApi,
    private val runContext: RunContext,
    private val addDeckToPlayerInventoryAction: AddDeckToPlayerInventoryAction,
) :
    ServerPlayConnectionEvents.Join,
    ServerPlayConnectionEvents.Disconnect {
    private val logger = LoggerFactory.getLogger("ServerPlayConnectionJoin")

    private val objectivesToStore =
        "do2.artifacts.ASV,do2.artifacts.BAP,do2.artifacts.BED,do2.artifacts.CF1,do2.artifacts.CUS,do2.artifacts.DLP,do2.artifacts.GDE,do2.artifacts.GGR,do2.artifacts.GGS,do2" +
            ".artifacts.HAY,do2.artifacts.HGT,do2.artifacts.HST,do2.artifacts.HYB,do2.artifacts.JSS,do2.artifacts.KNH,do2.artifacts.MDM,do2.artifacts.MGW,do2.artifacts.MKY,do2" +
            ".artifacts.OFP,do2.artifacts.PCL,do2.artifacts.PPS,do2.artifacts.PWS,do2.artifacts.SHD,do2.artifacts.SKA,do2.artifacts.SPS,do2.artifacts.THL,do2.artifacts.TSL," +
            "do2.artifacts.WGG,do2.artifakes.ASV,do2.artifakes.BAP,do2.artifakes.BED,do2.artifakes.CF1,do2.artifakes.CUS,do2.artifakes.DLP,do2.artifakes.GDE,do2.artifakes" +
            ".GGR,do2.artifakes.GGS,do2.artifakes.HAY,do2.artifakes.HGT,do2.artifakes.HST,do2.artifakes.HYB,do2.artifakes.JSS,do2.artifakes.KNH,do2.artifakes.MDM,do2" +
            ".artifakes.MGW,do2.artifakes.MKY,do2.artifakes.OFP,do2.artifakes.PCL,do2.artifakes.PPS,do2.artifakes.PWS,do2.artifakes.SHD,do2.artifakes.SKA,do2.artifakes.SPS,do2.artifakes.THL,do2.artifakes.TSL,do2.artifakes.WGG,do2.cards.available.ADR,do2.cards.available.BES,do2.cards.available.BRI,do2.cards.available.BST,do2.cards.available.CHS,do2.cards.available.COS,do2.cards.available.DEF,do2.cards.available.DUR,do2.cards.available.EES,do2.cards.available.EOP,do2.cards.available.EVA,do2.cards.available.FBS,do2.cards.available.FRF,do2.cards.available.LAS,do2.cards.available.NIL,do2.cards.available.PIB,do2.cards.available.QUI,do2.cards.available.REC,do2.cards.available.SAG,do2.cards.available.SEW,do2.cards.available.SIR,do2.cards.available.SPR,do2.cards.available.SPT,do2.cards.available.SUU,do2.cards.available.SWA,do2.cards.available.TRL,do2.cards.bought.ADR,do2.cards.bought.BES,do2.cards.bought.BRI,do2.cards.bought.BST,do2.cards.bought.CHS,do2.cards.bought.COS,do2.cards.bought.DEF,do2.cards.bought.DUL,do2.cards.bought.DUR,do2.cards.bought.EES,do2.cards.bought.EMS,do2.cards.bought.EOP,do2.cards.bought.EVA,do2.cards.bought.FBS,do2.cards.bought.FRF,do2.cards.bought.LAS,do2.cards.bought.MOC,do2.cards.bought.NIL,do2.cards.bought.P2W,do2.cards.bought.PCP,do2.cards.bought.PIB,do2.cards.bought.QUI,do2.cards.bought.REC,do2.cards.bought.SAG,do2.cards.bought.SEW,do2.cards.bought.SIR,do2.cards.bought.SNE,do2.cards.bought.SPR,do2.cards.bought.SPT,do2.cards.bought.STA,do2.cards.bought.STU,do2.cards.bought.SUU,do2.cards.bought.SWA,do2.cards.bought.TAA,do2.cards.bought.TRH,do2.cards.bought.TRL,do2.cards.played.ADR,do2.cards.played.BES,do2.cards.played.BRI,do2.cards.played.BST,do2.cards.played.CHS,do2.cards.played.COS,do2.cards.played.DEF,do2.cards.played.DUL,do2.cards.played.DUR,do2.cards.played.EES,do2.cards.played.EMS,do2.cards.played.EOP,do2.cards.played.EVA,do2.cards.played.FBS,do2.cards.played.FRF,do2.cards.played.LAS,do2.cards.played.MOC,do2.cards.played.NIL,do2.cards.played.P2W,do2.cards.played.PCP,do2.cards.played.PIB,do2.cards.played.QUI,do2.cards.played.REC,do2.cards.played.SAG,do2.cards.played.SEW,do2.cards.played.SIR,do2.cards.played.SNE,do2.cards.played.SPR,do2.cards.played.SPT,do2.cards.played.STA,do2.cards.played.STU,do2.cards.played.SUU,do2.cards.played.SWA,do2.cards.played.TAA,do2.cards.played.TRH,do2.cards.played.TRL,do2.depth_charge_success,do2.eggs.bdubs,do2.eggs.beef,do2.eggs.cleo,do2.eggs.cub,do2.eggs.docm,do2.eggs.false,do2.eggs.gold,do2.eggs.grian,do2.eggs.hypno,do2.eggs.impulse,do2.eggs.iskall,do2.eggs.jevin,do2.eggs.joe,do2.eggs.pearl,do2.eggs.scar,do2.eggs.stress,do2.eggs.total,do2.eggs.wels,do2.eggs.xb,do2.eggs.xisuma,do2.eggs.zedaph,do2.highest_loss_streak,do2.highest_win_streak,do2.lifetime.coinsconverted,do2.lifetime.escaped.crowns,do2.lifetime.escaped.embers,do2.lifetime.pickedup.coins,do2.lifetime.pickedup.crowns,do2.lifetime.pickedup.embers,do2.lifetime.spent.crowns,do2.lifetime.spent.embers,do2.loss_streak,do2.losses,do2.runs,do2.systems.clank.blocked,do2.systems.clank.generated,do2.systems.embers.released,do2.systems.hazard.activated,do2.systems.hazard.blocked,do2.systems.hazard.generated,do2.systems.maxclank.released,do2.systems.treasure.released,do2.win_streak,do2.wins"
                .split(",")

    override fun onPlayReady(handler: ServerPlayNetworkHandler, sender: PacketSender, server: MinecraftServer) {
        val playerName = handler.player.entityName
        if (playerName.equals("TangoCam", ignoreCase = true)) {
            return
        }

        try {
            claimApi.claimsGet(
                player = playerName,
                state = "acquired",
                type = "dungeon",
            ).results?.firstOrNull()?.let { claim ->
                claim.metadata?.let { runContext.addPlayerContext(playerName, it) }

//                claim.metadata?.get("run-type")?.let { runContext.gameTags[playerName] = it }
//                // TODO: Don't do this if player is spectating
//                claim.metadata?.get("run-id")?.let { runContext.runId = it }

//                // TODO: Store deck-id - https://github.com/trackedout/agronet-fabric/issues/31

                logger.info("Setting state of Claim ${claim.id} to 'in-use'")
                claimApi.claimsIdPatch(claim.id!!, claim.copy(id = null, state = "in-use", claimant = runContext.serverName))
            } ?: run {
                logger.error("No matching claim found for $playerName")
                handler.player.sendMessage("No matching claim found for your run, contact a moderator (unless you are spectating)", Formatting.RED)
            }

            val runType = getFullRunType(playerName)
            val filter = "$runType-"
            val advancementFilter = "$runType-advancement-"

            logger.info("Scoreboard filter: $filter")
            val scores = scoreApi.scoresGet(
                player = playerName,
                limit = 10000,
            )

            scores.results!!
                .filter { it.key!!.startsWith(filter) }
                .filter { !it.key!!.startsWith(advancementFilter) }
                .map { it.copy(key = it.key?.substring(filter.length)) }
                .filter { it.key!!.isNotBlank() }
                .forEach {
                    val objective = server.scoreboard.getObjective(it.key)

                    val playerScore = server.scoreboard.getPlayerScore(playerName, objective)
                    playerScore.score = it.value!!.toInt()
                    logger.info("Set ${it.key} to ${it.value} for $playerName")
                }

            val tracker = handler.player.advancementTracker
            server.gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server)

            scores.results
                .filter { it.key!!.startsWith(advancementFilter) }
                .map { it.copy(key = it.key?.substring(advancementFilter.length)) }
                .filter { it.key!!.isNotBlank() && it.key.contains("#") }
                .filter { it.value!!.toInt() > 0 }
                .forEach { score ->
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
        }

        handler.player?.let { player ->
            server.commandSource?.let { commandSource ->
                try {
                    addDeckToPlayerInventoryAction.execute(commandSource, player)
                } catch (e: Exception) {
                    e.printStackTrace()
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
                .filter { objective -> (objective.key?.name)?.let { objectivesToStore.contains(it) } ?: false }
                .map { objective ->
                    objective.key.name to objective.value.score
                }
                .toMap().toMutableMap()

            batchMap += server.advancementLoader.advancements.asSequence()
                .filter { !it.id.path.startsWith("visible/credits/") }
                .filter { handler.player.advancementTracker.getProgress(it).isAnyObtained }
                .flatMap {
                    val progress = handler.player.advancementTracker.getProgress(it)
                    it.criteria.entries.map { entry ->
                        val obtained: Boolean? = progress.getCriterionProgress(entry.key)?.isObtained
                        val value = if (obtained != null && obtained == true) 1 else 0

                        "advancement-${it.id.namespace}#${it.id.path}#${entry.key}" to value
                    }
                }
                .filter { it.second > 0 }.toList()

            handler.player.advancementTracker.save()

            if (batchMap.isEmpty()) {
                logger.info("$playerName does not have any applicable objectives, skipping store call")
                return
            }

            logger.info("Storing ${batchMap.size} objectives for player $playerName")
            logger.info("BatchMap: ${Json.encodeToString(batchMap)}")

            scoreApi.scoresPost(
                batchMap.map {
                    Score(
                        player = playerName,
                        key = "${getFullRunType(playerName)}-${it.key}",
                        value = it.value.toBigDecimal(),
                    )
                }
            )

            logger.info("Successfully stored ${batchMap.size} objectives for player $playerName")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFullRunType(playerName: String): String = runContext.playerContext(playerName).fullRunType().lowercase()
}
