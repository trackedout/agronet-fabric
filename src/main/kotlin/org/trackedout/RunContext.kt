package org.trackedout

import com.google.common.collect.Maps
import org.trackedout.data.BrillianceCard
import java.util.UUID
import java.util.concurrent.ConcurrentMap

data object RunContext {
    var serverName = "unknown"
    var runId = UUID.randomUUID().toString()
    var initialized = false

    // Note that all keys are stored without underscores or dashes, e.g. "quickstep" and "suitup"
    // This is due to a disparity between what Agronet vs Brilliance calls the card
    var brillianceCards = mapOf<String, BrillianceCard>()

    private val playerContextMap: ConcurrentMap<String, PlayerContext> = Maps.newConcurrentMap()

    fun addPlayerContext(playerName: String, context: PlayerContext) {
        val map = playerContextMap.getOrDefault(playerName, mapOf())
        playerContextMap[playerName] = map + context
    }

    fun playerContext(playerName: String): PlayerContext {
        return playerContextMap.getOrDefault(playerName, mapOf())
    }
}

typealias PlayerContext = Map<String, String>

fun PlayerContext.runType(): String {
    return this.getOrDefault("run-type", "p")
}

fun PlayerContext.fullRunType(): String {
    return this.runType().fullRunType()
}

fun String.fullRunType(): String {
    return when (this) {
        "p" -> "Practice"
        "c" -> "Competitive"
        else -> "unknown"
    }
}

fun PlayerContext.shortRunType(): String {
    return this.runType()[0].lowercase()
}

fun PlayerContext.fullDeckId(): String {
    // TODO: https://github.com/trackedout/agronet-fabric/issues/31
    return this.getOrDefault("deck-id", "p1")
}

fun PlayerContext.shortDeckId(): String {
    // TODO: https://github.com/trackedout/agronet-fabric/issues/31
    return this.getOrDefault("deck-id", "p1").substring(1)
}
