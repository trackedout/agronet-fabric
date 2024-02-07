package org.trackedout.data

class Cards {
    companion object {

        /*
            Icon legend:
            ✧ - Standard
            ✲ - Ethereal
            ≡ - Permanent
        */
        enum class Card(val key: String, val displayName: String, val modelData: Int, val colour: String) {
            ADRENALINE_RUSH("adrenaline_rush", "✧ Adrenaline Rush ✧", 123, "#80C71F"),
            BEAST_SENSE("beast_sense", "✧ Beast Sense ✧", 115, "#80C71F"),
            BOUNDING_STRIDES("bounding_strides", "✧ Bounding Strides ✧", 116, "#80C71F"),
            BRILLIANCE("brilliance", "✧ Brilliance ✧", 135, "#3C44AA"),
            CHILL_STEP("chill_step", "✧ Chill Step ✧", 127, "#3C44AA"),
            COLD_SNAP("cold_snap", "✧ Cold Snap ✧", 131, "#3C44AA"),
            DEEPFROST("deepfrost", "✧ Deepfrost ✧", 134, "#3C44AA"),
            DUNGEON_REPAIRS("dungeon_repairs", "✧ Dungeon Repairs ✧", 128, "#3C44AA"),
            EERIE_SILENCE("eerie_silence", "✧ Eerie Silence ✧", 126, "#3C44AA"),
            EMBER_SEEKER("ember_seeker", "✧ Ember Seeker ✧", 104, "gray"),
            EVASION("evasion", "✧ Evasion ✧", 110, "#80C71F"),
            EYES_ON_THE_PRIZE("eyes_on_the_prize", "✧ Eyes on the Prize ✧", 129, "#3C44AA"),
            FROST_FOCUS("frost_focus", "✧ Frost Focus ✧", 112, "#80C71F"),
            FUZZY_BUNNY_SLIPPERS("fuzzy_bunny_slippers", "≡ Fuzzy Bunny Slippers ≡", 133, "#3C44AA"),
            LOOT_AND_SCOOT("loot_and_scoot", "✧ Loot and Scoot ✧", 111, "#80C71F"),
            MOMENT_OF_CLARITY("moment_of_clarity", "✲ Moment of Clarity ✲", 106, "gray"),
            NIMBLE_LOOTING("nimble_looting", "✧ Nimble Looting ✧", 120, "#80C71F"),
            PAY_TO_WIN("pay_to_win", "✲ Pay to Win ✲", 107, "#FED83D"),
            PIRATES_BOOTY("pirates_booty", "✧ Pirate's Booty ✧", 130, "#3C44AA"),
            PORK_CHOP_POWER("pork_chop_power", "✲≡ Pork Chop Power ≡✲", 109, "#FED83D"),
            QUICKSTEP("quickstep", "✧ Quickstep ✧", 121, "#80C71F"),
            RECKLESS_CHARGE("reckless_charge", "✧ Reckless Charge ✧", 119, "#80C71F"),
            SECOND_WIND("second_wind", "✧ Second Wind ✧", 114, "#80C71F"),
            SILENT_RUNNER("silent_runner", "≡ Silent Runner ≡", 132, "#3C44AA"),
            SMASH_AND_GRAB("smash_and_grab", "✧ Smash and Grab ✧", 118, "#80C71F"),
            SNEAK("sneak", "✧ Sneak ✧", 102, "gray"),
            SPEED_RUNNER("speed_runner", "≡ Speed Runner ≡", 125, "#3C44AA"),
            SPRINT("sprint", "✧ Sprint ✧", 117, "#80C71F"),
            STABILITY("stability", "✧ Stability ✧", 105, "gray"),
            SUIT_UP("suit_up", "≡ Suit Up ≡", 122, "#80C71F"),
            SWAGGER("swagger", "✧ Swagger ✧", 124, "#3C44AA"),
            TACTICAL_APPROACH("tactical_approach", "✲≡ Tactical Approach ≡✲", 108, "#FED83D"),
            TREAD_LIGHTLY("tread_lightly", "✧ Tread Lightly ✧", 113, "#80C71F"),
            TREASURE_HUNTER("treasure_hunter", "✧ Treasure Hunter ✧", 103, "gray"),
            DUNGEON_LACKEY("dungeon_lackey", "✲≡ Dungeon Lackey ≡✲", 141, "gray"),
        }

        fun findCard(cardName: String): Card? {
            return Card.entries.find { it.key == cardName || it.displayName == cardName }
        }

        fun cardModelData(cardName: String): Int {
            val data = findCard(cardName)
            if (data != null) {
                return data.modelData
            } else {
                throw Exception("Card model data for $cardName not known")
            }
        }

        fun etherealCards(): List<Card> {
            return listOf(
                Card.MOMENT_OF_CLARITY,
                Card.PAY_TO_WIN,
                Card.PORK_CHOP_POWER,
                Card.TACTICAL_APPROACH,
                Card.DUNGEON_LACKEY,
            )
        }
    }
}