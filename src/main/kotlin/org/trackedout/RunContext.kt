package org.trackedout

import com.google.common.collect.Maps
import java.util.UUID
import java.util.concurrent.ConcurrentMap

data object RunContext {
    var serverName = "unknown"
    var runId = UUID.randomUUID().toString()

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
    return when (this.runType()) {
        "p" -> "Practice"
        "c" -> "Competitive"
        else -> "unknown"
    }
}

fun PlayerContext.fullDeckId(): String {
    // TODO: https://github.com/trackedout/agronet-fabric/issues/31
    return this.getOrDefault("deck-id", "p1")
}

fun PlayerContext.shortDeckId(): String {
    // TODO: https://github.com/trackedout/agronet-fabric/issues/31
    return this.getOrDefault("deck-id", "p1").substring(1)
}
