package org.trackedout

import com.google.common.collect.Maps
import org.trackedout.client.models.Event
import org.trackedout.data.BrillianceCard
import org.trackedout.data.LeagueRegistry
import org.trackedout.data.find

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

data object RunContext {
    var serverStartTimestamp: Instant = Instant.now()
    var serverName = "unknown"
    var dungeonType = "default" // e.g. "default" or "do2-s2-comp"
    var runId = UUID.randomUUID().toString()
    var initialized = false
    var gameStarted = false
    var gameEnded = false
    var gameWon = false
    var suppressCardDeletion = false

    // The player who queued; the run plays their deck. Party members share the claim.
    var claimOwner: String? = null
    var runPlayers: Set<String> = emptySet()
    private var deckHandedOut = false

    fun isRunPlayer(playerName: String) = playerName in runPlayers

    // True exactly once, when the claim owner first joins, so a rejoin does not get a second deck
    @Synchronized
    fun takeDeckHandout(playerName: String): Boolean {
        if (deckHandedOut || playerName != claimOwner) {
            return false
        }
        deckHandedOut = true
        return true
    }

    private val eventHandlers = ConcurrentHashMap<String, MutableSet<(Event) -> Unit>>()

    fun addEventHandler(eventName: String, handler: (Event) -> Unit) {
        eventHandlers
            .computeIfAbsent(eventName) { ConcurrentHashMap.newKeySet() }
            .add(handler)
    }

    fun drainEventHandlers(key: String): Set<(Event) -> Unit> = eventHandlers.remove(key) ?: emptySet()

    // Note that all keys are stored without underscores or dashes, e.g. "quickstep" and "suitup"
    // This is due to a disparity between what Agronet vs Brilliance calls the card
    var brillianceCards = mapOf<String, BrillianceCard>()

    private val playerContextMap: ConcurrentMap<String, PlayerContext> = Maps.newConcurrentMap()

    // Tracks how many of each dungeon item was given to a player on join
    private val itemsGivenOnJoin = ConcurrentHashMap<String, MutableMap<String, Int>>()

    fun recordItemGiven(playerName: String, itemName: String) {
        itemsGivenOnJoin
            .computeIfAbsent(playerName) { ConcurrentHashMap() }
            .merge(itemName, 1, Int::plus)
    }

    fun getItemsGivenOnJoin(playerName: String): Map<String, Int> {
        return itemsGivenOnJoin.getOrDefault(playerName, emptyMap())
    }

    fun addPlayerContext(playerName: String, context: PlayerContext) {
        val map = playerContextMap.getOrDefault(playerName, mapOf())
        playerContextMap[playerName] = map + context
    }

    fun playerContext(playerName: String): PlayerContext {
        return playerContextMap.getOrDefault(playerName, mapOf())
    }

    fun findCard(cardKey: String): BrillianceCard? {
        return brillianceCards.find(cardKey)
    }
}

typealias PlayerContext = Map<String, String>

// The league `slug` is the canonical run identifier network-wide. Claim metadata
// carries it under the "slug" key; default to the practice league.
fun PlayerContext.runType(): String {
    return this.getOrDefault("slug", "do2-s2-practice")
}

fun PlayerContext.fullRunType(): String {
    return this.runType()
}
