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
            logger.info("Refreshed league registry: ${leagues.mapNotNull { it.slug }}")
        } catch (e: Exception) {
            logger.error("Failed to refresh leagues from API: ${e.message}")
        }
    }

    fun getBySlug(slug: String): League? {
        return leagues.find { it.slug == slug }
    }

    // The datapack mode id (1=practice, 2=competitive, 3=hardcore, ...) is stored
    // on the league's display.runTypeId. Resolve it from the canonical slug.
    fun getRunTypeIdForSlug(slug: String): Int {
        return getBySlug(slug)?.display?.runTypeId ?: 0
    }
}
