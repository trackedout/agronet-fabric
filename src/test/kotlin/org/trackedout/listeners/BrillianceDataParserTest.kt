package org.trackedout.listeners

import org.junit.jupiter.api.Test
import org.trackedout.data.BrillianceCard
import org.trackedout.data.BrillianceScoreboardDescription

class BrillianceDataParserTest {
    val scoreboardsJsonPath = "data/brilliance-data/scoreboards.json"
    val cardsJsonPath = "data/brilliance-data/cards.json"

    @Test
    fun `scoreboards data parsing`() {
        val scoreboardData = this::class.java.classLoader.getResourceAsStream(scoreboardsJsonPath)!!.bufferedReader().use { it.readText() }
        json.decodeFromString<Map<String, BrillianceScoreboardDescription>>(scoreboardData)
        println("Successfully parsed scoreboards.json")
    }

    @Test
    fun `cards data parsing`() {
        val cardsData = this::class.java.classLoader.getResourceAsStream(cardsJsonPath)!!.bufferedReader().use { it.readText() }
        json.decodeFromString<Map<String, BrillianceCard>>(cardsData)
        println("Successfully parsed cards.json")
    }
}
