package com.example.data.models

data class SawmillPlankRecipe(
    val plankId: String,
    val plankName: String,
    val emoji: String,
    val primaryLogId: String,
    val acceptedLogIds: List<String>,
    val logDisplayName: String,
    val reqWoodcuttingLevel: Int,
    val reqConstructionLevel: Int,
    val constructionXp: Long,
    val geMaterial: GeMaterial,
    val description: String
)

object SawmillRecipes {
    val ALL_RECIPES = listOf(
        SawmillPlankRecipe(
            plankId = "item_plank",
            plankName = "Standard Plank",
            emoji = "🪚",
            primaryLogId = "item_logs",
            acceptedLogIds = listOf("item_logs", "item_regular_logs"),
            logDisplayName = "Normal Logs",
            reqWoodcuttingLevel = 1,
            reqConstructionLevel = 1,
            constructionXp = 120L,
            geMaterial = GeMaterial.BIRCH_PLANK,
            description = "Basic building plank milled from regular woodland logs."
        ),
        SawmillPlankRecipe(
            plankId = "item_oak_plank",
            plankName = "Oak Plank",
            emoji = "🪵",
            primaryLogId = "item_oak_logs",
            acceptedLogIds = listOf("item_oak_logs", "item_oak_bark"),
            logDisplayName = "Oak Logs",
            reqWoodcuttingLevel = 1,
            reqConstructionLevel = 15,
            constructionXp = 250L,
            geMaterial = GeMaterial.OAK_PLANK,
            description = "Sturdy oak timber plank used for dependable furniture."
        ),
        SawmillPlankRecipe(
            plankId = "item_birch_plank",
            plankName = "Birch Plank",
            emoji = "🪵",
            primaryLogId = "item_birch_timber",
            acceptedLogIds = listOf("item_birch_timber", "item_birch_logs", "item_birch_bark"),
            logDisplayName = "Birch Timber",
            reqWoodcuttingLevel = 10,
            reqConstructionLevel = 10,
            constructionXp = 180L,
            geMaterial = GeMaterial.BIRCH_PLANK,
            description = "Smooth silvery birch plank milled from Birch Grove timber."
        ),
        SawmillPlankRecipe(
            plankId = "item_pine_plank",
            plankName = "Pine Plank",
            emoji = "🌲",
            primaryLogId = "item_pine_logs",
            acceptedLogIds = listOf("item_pine_logs", "item_pine_bark"),
            logDisplayName = "Alpine Pine Logs",
            reqWoodcuttingLevel = 25,
            reqConstructionLevel = 25,
            constructionXp = 260L,
            geMaterial = GeMaterial.PINE_PLANK,
            description = "Aromatic alpine pine plank with natural weather resistance."
        ),
        SawmillPlankRecipe(
            plankId = "item_willow_plank",
            plankName = "Willow Plank",
            emoji = "🌿",
            primaryLogId = "item_willow_logs",
            acceptedLogIds = listOf("item_willow_logs", "item_willow_bark"),
            logDisplayName = "Willow Logs",
            reqWoodcuttingLevel = 30,
            reqConstructionLevel = 30,
            constructionXp = 300L,
            geMaterial = GeMaterial.WILLOW_PLANK,
            description = "Flexible willow plank milled from weeping riverbank logs."
        ),
        SawmillPlankRecipe(
            plankId = "item_teak_plank",
            plankName = "Teak Spirit Plank",
            emoji = "🪵",
            primaryLogId = "item_teak_logs",
            acceptedLogIds = listOf("item_teak_logs", "item_teak_bark"),
            logDisplayName = "Teak Logs",
            reqWoodcuttingLevel = 35,
            reqConstructionLevel = 35,
            constructionXp = 350L,
            geMaterial = GeMaterial.TEAK_PLANK,
            description = "Polished tropical hardwood plank for sanctuary construction."
        ),
        SawmillPlankRecipe(
            plankId = "item_cedar_plank",
            plankName = "Cedar Plank",
            emoji = "🪵",
            primaryLogId = "item_cedar_timber",
            acceptedLogIds = listOf("item_cedar_timber", "item_cedar_logs", "item_cedar_bark"),
            logDisplayName = "Mountain Cedar Timber",
            reqWoodcuttingLevel = 40,
            reqConstructionLevel = 40,
            constructionXp = 380L,
            geMaterial = GeMaterial.CEDAR_PLANK,
            description = "Dense fragrant cedar plank prized by master artisans."
        ),
        SawmillPlankRecipe(
            plankId = "item_maple_plank",
            plankName = "Maple Plank",
            emoji = "🍁",
            primaryLogId = "item_maple_logs",
            acceptedLogIds = listOf("item_maple_logs", "item_maple_bark"),
            logDisplayName = "Autumn Maple Logs",
            reqWoodcuttingLevel = 45,
            reqConstructionLevel = 45,
            constructionXp = 440L,
            geMaterial = GeMaterial.MAPLE_PLANK,
            description = "Rich amber maple plank with gorgeous wood grain."
        ),
        SawmillPlankRecipe(
            plankId = "item_mahogany_plank",
            plankName = "Mahogany Plank",
            emoji = "🪵",
            primaryLogId = "item_mahogany_logs",
            acceptedLogIds = listOf("item_mahogany_logs", "item_mahogany_bark"),
            logDisplayName = "Mahogany Logs",
            reqWoodcuttingLevel = 50,
            reqConstructionLevel = 50,
            constructionXp = 500L,
            geMaterial = GeMaterial.MAHOGANY_PLANK,
            description = "Exquisite hardwood plank for masterwork POH furniture."
        ),
        SawmillPlankRecipe(
            plankId = "item_yew_plank",
            plankName = "Yew Plank",
            emoji = "🌳",
            primaryLogId = "item_yew_logs",
            acceptedLogIds = listOf("item_yew_logs", "item_yew_bark"),
            logDisplayName = "Ancient Yew Logs",
            reqWoodcuttingLevel = 60,
            reqConstructionLevel = 60,
            constructionXp = 580L,
            geMaterial = GeMaterial.YEW_PLANK,
            description = "Dense ancient yew plank with supreme tensile strength."
        ),
        SawmillPlankRecipe(
            plankId = "item_ironwood_plank",
            plankName = "Ironwood Plank",
            emoji = "🗡️",
            primaryLogId = "item_ironwood_timber",
            acceptedLogIds = listOf("item_ironwood_timber", "item_ironwood_logs", "item_ironwood_bark"),
            logDisplayName = "Metallic Ironwood",
            reqWoodcuttingLevel = 75,
            reqConstructionLevel = 70,
            constructionXp = 720L,
            geMaterial = GeMaterial.IRONWOOD_PLANK,
            description = "Metallic ironwood plank impervious to rot and physical stress."
        ),
        SawmillPlankRecipe(
            plankId = "item_magic_plank",
            plankName = "Magic Plank",
            emoji = "🔮",
            primaryLogId = "item_magic_logs",
            acceptedLogIds = listOf("item_magic_logs", "item_magic_bark"),
            logDisplayName = "Mystic Magic Logs",
            reqWoodcuttingLevel = 75,
            reqConstructionLevel = 75,
            constructionXp = 850L,
            geMaterial = GeMaterial.MAGIC_PLANK,
            description = "Arcane-infused glowing magic plank humming with mana."
        ),
        SawmillPlankRecipe(
            plankId = "item_redwood_plank",
            plankName = "Redwood Plank",
            emoji = "🔴",
            primaryLogId = "item_redwood_timber",
            acceptedLogIds = listOf("item_redwood_timber", "item_redwood_logs", "item_redwood_bark"),
            logDisplayName = "Giant Redwood Timber",
            reqWoodcuttingLevel = 80,
            reqConstructionLevel = 80,
            constructionXp = 1000L,
            geMaterial = GeMaterial.REDWOOD_PLANK,
            description = "Massive giant redwood plank harvested from colossal canopy trees."
        ),
        SawmillPlankRecipe(
            plankId = "item_spirit_plank",
            plankName = "Spirit Redwood Plank",
            emoji = "🌌",
            primaryLogId = "item_spirit_redwood",
            acceptedLogIds = listOf("item_spirit_redwood", "item_spirit_logs", "item_spirit_bark"),
            logDisplayName = "Spirit Redwood",
            reqWoodcuttingLevel = 85,
            reqConstructionLevel = 85,
            constructionXp = 1200L,
            geMaterial = GeMaterial.SPIRIT_PLANK,
            description = "Spiritual timber plank deeply attuned to shamanic energy."
        ),
        SawmillPlankRecipe(
            plankId = "item_astral_plank",
            plankName = "Astral Oak Plank",
            emoji = "💫",
            primaryLogId = "item_astral_bark",
            acceptedLogIds = listOf("item_astral_bark", "item_astral_logs"),
            logDisplayName = "Astral Bark Oak",
            reqWoodcuttingLevel = 88,
            reqConstructionLevel = 88,
            constructionXp = 1400L,
            geMaterial = GeMaterial.ASTRAL_PLANK,
            description = "Shimmering cosmic oak plank glistening with starry stardust."
        ),
        SawmillPlankRecipe(
            plankId = "item_sunfire_plank",
            plankName = "Sunfire Plank",
            emoji = "🔥",
            primaryLogId = "item_sunfire_log",
            acceptedLogIds = listOf("item_sunfire_log", "item_sunfire_logs", "item_sunfire_bark"),
            logDisplayName = "Sunfire Baobab",
            reqWoodcuttingLevel = 90,
            reqConstructionLevel = 90,
            constructionXp = 1600L,
            geMaterial = GeMaterial.SUNFIRE_PLANK,
            description = "Searing sunfire plank radiating intense solar heat."
        ),
        SawmillPlankRecipe(
            plankId = "item_emberwood_plank",
            plankName = "Emberwood Plank",
            emoji = "🪵",
            primaryLogId = "item_emberwood_timber",
            acceptedLogIds = listOf("item_emberwood_timber", "item_emberwood_logs", "item_emberwood_bark"),
            logDisplayName = "Emberwood Trunk",
            reqWoodcuttingLevel = 93,
            reqConstructionLevel = 93,
            constructionXp = 1800L,
            geMaterial = GeMaterial.EMBERWOOD_PLANK,
            description = "Smoldering timber plank coursing with molten volcanic veins."
        ),
        SawmillPlankRecipe(
            plankId = "item_obsidian_plank",
            plankName = "Obsidian Plank",
            emoji = "🖤",
            primaryLogId = "item_obsidian_bark",
            acceptedLogIds = listOf("item_obsidian_bark", "item_obsidian_logs"),
            logDisplayName = "Obsidian Baobab",
            reqWoodcuttingLevel = 95,
            reqConstructionLevel = 95,
            constructionXp = 2100L,
            geMaterial = GeMaterial.OBSIDIAN_PLANK,
            description = "Extremely dense, heatproof obsidian plank from baobab bark."
        ),
        SawmillPlankRecipe(
            plankId = "item_celestial_plank",
            plankName = "Celestial Yew Plank",
            emoji = "🌟",
            primaryLogId = "item_celestial_yew_log",
            acceptedLogIds = listOf("item_celestial_yew_log", "item_celestial_logs"),
            logDisplayName = "Celestial Yew",
            reqWoodcuttingLevel = 99,
            reqConstructionLevel = 99,
            constructionXp = 2500L,
            geMaterial = GeMaterial.CELESTIAL_PLANK,
            description = "Divine floating yew plank blessed by celestial spirits."
        ),
        SawmillPlankRecipe(
            plankId = "item_cosmic_plank",
            plankName = "Cosmic Redwood Plank",
            emoji = "🐳",
            primaryLogId = "item_cosmic_redwood",
            acceptedLogIds = listOf("item_cosmic_redwood", "item_cosmic_logs"),
            logDisplayName = "Cosmic Redwood",
            reqWoodcuttingLevel = 99,
            reqConstructionLevel = 99,
            constructionXp = 2800L,
            geMaterial = GeMaterial.COSMIC_PLANK,
            description = "Ancient cosmic redwood plank woven with deep space dust."
        ),
        SawmillPlankRecipe(
            plankId = "item_golden_spirit_plank",
            plankName = "Golden Spirit Plank",
            emoji = "🐉",
            primaryLogId = "item_golden_spirit_trunk",
            acceptedLogIds = listOf("item_golden_spirit_trunk", "item_golden_spirit_logs"),
            logDisplayName = "Golden Spirit World Tree",
            reqWoodcuttingLevel = 99,
            reqConstructionLevel = 99,
            constructionXp = 3500L,
            geMaterial = GeMaterial.GOLDEN_SPIRIT_PLANK,
            description = "Supreme pinnacle World Tree plank radiating transcendent power."
        )
    )

    fun findRecipeForPlank(plankId: String): SawmillPlankRecipe? {
        return ALL_RECIPES.find { it.plankId == plankId }
    }

    fun findRecipeForLog(logId: String): SawmillPlankRecipe? {
        return ALL_RECIPES.find { recipe ->
            recipe.primaryLogId == logId || recipe.acceptedLogIds.contains(logId)
        }
    }
}
