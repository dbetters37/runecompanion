package com.example.data.models

enum class SpiritQuestTaskType(val displayName: String, val emoji: String) {
    CHOP_TREES("Harvest Sacred Timber", "🪓"),
    FISH_POOL("Catch Shamanic Fish", "🎣"),
    CHANNEL_ENERGY("Channel Life Energy", "⚡"),
    COMMUNE_PET("Commune with Spirit Companion", "🐾"),
    OFFER_RUNES("Craft Nature Runes", "🔮"),
    FORAGE_HERBS("Forage Sacred Herbs", "🌿"),
    SMITH_TOTEM("Forge Shaman Totems", "🔨")
}

data class DailySpiritQuest(
    val id: String,
    val title: String,
    val description: String,
    val taskType: SpiritQuestTaskType,
    val requiredAmount: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val rewardLifeEnergy: Int,
    val rewardItemName: String,
    val rewardItemEmoji: String,
    val rewardItemId: String,
    val rewardItemQty: Int = 1
)

object DailySpiritQuestGenerator {
    val TEMPLATES = listOf(
        DailySpiritQuest(
            id = "sq_chop_willow",
            title = "Whispering Willow Timber",
            description = "Chop Willow or Elder trees to harvest 10 Spirit Timber logs.",
            taskType = SpiritQuestTaskType.CHOP_TREES,
            requiredAmount = 10,
            rewardLifeEnergy = 500,
            rewardItemName = "Magic Logs",
            rewardItemEmoji = "🪵",
            rewardItemId = "item_magic_logs",
            rewardItemQty = 5
        ),
        DailySpiritQuest(
            id = "sq_fish_salmon",
            title = "Sacred Pool Harvest",
            description = "Catch 5 Shamanic Fish or Salmon in the Shaman Pool.",
            taskType = SpiritQuestTaskType.FISH_POOL,
            requiredAmount = 5,
            rewardLifeEnergy = 600,
            rewardItemName = "Raw Lobster",
            rewardItemEmoji = "🦞",
            rewardItemId = "item_raw_lobster",
            rewardItemQty = 8
        ),
        DailySpiritQuest(
            id = "sq_channel_energy",
            title = "Life Energy Resonance",
            description = "Channel 300 Life Energy into your Shamanic Companion.",
            taskType = SpiritQuestTaskType.CHANNEL_ENERGY,
            requiredAmount = 300,
            rewardLifeEnergy = 750,
            rewardItemName = "Spirit Crystal",
            rewardItemEmoji = "💎",
            rewardItemId = "item_spirit_crystal",
            rewardItemQty = 2
        ),
        DailySpiritQuest(
            id = "sq_commune_pet",
            title = "Spirit Animal Communion",
            description = "Pet, feed, or play with your Companion 3 times.",
            taskType = SpiritQuestTaskType.COMMUNE_PET,
            requiredAmount = 3,
            rewardLifeEnergy = 400,
            rewardItemName = "Gold Leaf",
            rewardItemEmoji = "🍂",
            rewardItemId = "item_gold_leaf",
            rewardItemQty = 2
        ),
        DailySpiritQuest(
            id = "sq_forage_herbs",
            title = "Sacred Herb Gathering",
            description = "Harvest or gather 4 Sunleaf / Mystic Sage herbs for the shrine.",
            taskType = SpiritQuestTaskType.FORAGE_HERBS,
            requiredAmount = 4,
            rewardLifeEnergy = 650,
            rewardItemName = "Clean Sunleaf",
            rewardItemEmoji = "🌿",
            rewardItemId = "item_clean_ranarr",
            rewardItemQty = 4
        ),
        DailySpiritQuest(
            id = "sq_offer_runes",
            title = "Elemental Rune Consecration",
            description = "Craft or channel 20 Nature or Cosmic Runes at the Altar.",
            taskType = SpiritQuestTaskType.OFFER_RUNES,
            requiredAmount = 20,
            rewardLifeEnergy = 800,
            rewardItemName = "Elder Bark",
            rewardItemEmoji = "🪵",
            rewardItemId = "item_elder_bark",
            rewardItemQty = 3
        ),
        DailySpiritQuest(
            id = "sq_smith_totem",
            title = "Ancestral Totem Forging",
            description = "Forge or smith 3 metal bars or spirit totems at the anvil.",
            taskType = SpiritQuestTaskType.SMITH_TOTEM,
            requiredAmount = 3,
            rewardLifeEnergy = 700,
            rewardItemName = "Runite Ore",
            rewardItemEmoji = "🪨",
            rewardItemId = "item_runite_ore",
            rewardItemQty = 2
        )
    )

    fun generateDailyQuests(count: Int = 3): List<DailySpiritQuest> {
        return TEMPLATES.shuffled().take(count)
    }
}
