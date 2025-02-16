package org.trackedout.listeners

import kotlinx.serialization.json.jsonObject
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

    @Test
    fun `cards data parsing with raw tag`() {
        val cardsData = this::class.java.classLoader.getResourceAsStream(cardsJsonPath)!!.bufferedReader().use { it.readText() }

        val jsonElement = json.parseToJsonElement(cardsData).jsonObject
        val cards = jsonElement.mapValues { (_, value) ->
            val rawTag = value.jsonObject["tag"]?.toString() ?: error("Missing tag field")
            val brillianceCard = json.decodeFromJsonElement(BrillianceCard.serializer(), value)
            brillianceCard.copy(tagRaw = rawTag)
        }

        println("Successfully parsed cards.json")

        assert(cards["moment_of_clarity"]!!.isEthereal) {
            "Expected moment_of_clarity to be ethereal. Card: ${cards["moment_of_clarity"]}"
        }
    }
}
