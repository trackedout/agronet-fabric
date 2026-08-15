package org.trackedout.data

import org.slf4j.LoggerFactory
import org.trackedout.client.apis.LeaguesApi
import org.trackedout.client.models.League

object LeagueRegistry {
    private val logger = LoggerFactory.getLogger("LeagueRegistry")

    lateinit var leaguesApi: LeaguesApi
    private var leagues: List<League> = emptyList()

    fun refresh() {
        try {
            leagues = leaguesApi.leaguesGet().results ?: emptyList()
            logger.info("Refreshed league registry: ${leagues.map { it.display?.shortId }}")
        } catch (e: Exception) {
            logger.error("Failed to refresh leagues from API: ${e.message}")
        }
    }

    fun getByShortId(shortId: String): League? {
        return leagues.find { it.display?.shortId == shortId }
    }

    fun getSlugForShortId(shortId: String): String {
        return getByShortId(shortId)?.slug ?: shortId
    }

    fun getRunTypeIdForShortId(shortId: String): Int {
        val index = leagues.indexOfFirst { it.display?.shortId == shortId }
        return if (index >= 0) index + 1 else 0
    }
}
