package com.example.data.models

data class RecipeMaterial(
    val itemId: String,
    val itemName: String,
    val quantity: Int,
    val emoji: String = "📦"
)

data class FletchRecipe(
    val id: String,
    val name: String,
    val reqLevel: Int,
    val xpReward: Long,
    val iconEmoji: String,
    val inputMaterials: List<RecipeMaterial>,
    val outputItemId: String,
    val outputItemName: String,
    val outputQuantity: Int,
    val rangedPowerBonus: Int = 0,
    val description: String
)

object FletchingData {
    val ARROW_RECIPES = listOf(
        FletchRecipe(
            id = "fletch_headless_arrows",
            name = "Headless Arrows (15x)",
            reqLevel = 1,
            xpReward = 15L,
            iconEmoji = "🪶",
            inputMaterials = listOf(
                RecipeMaterial("item_arrow_shaft", "Wooden Shafts", 15, "🪵"),
                RecipeMaterial("item_feather", "Feathers", 15, "🪶")
            ),
            outputItemId = "item_headless_arrow",
            outputItemName = "Headless Arrow",
            outputQuantity = 15,
            rangedPowerBonus = 0,
            description = "Combine wooden shafts and feathers into headless arrow shafts."
        ),
        FletchRecipe(
            id = "fletch_bronze_arrows",
            name = "Bronze Arrows (15x)",
            reqLevel = 1,
            xpReward = 20L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_headless_arrow", "Headless Arrows", 15, "🪶"),
                RecipeMaterial("item_bronze_arrowtip", "Bronze Arrowtips", 15, "🗡️")
            ),
            outputItemId = "item_bronze_arrows",
            outputItemName = "Bronze Arrows",
            outputQuantity = 15,
            rangedPowerBonus = 2,
            description = "Attach bronze arrowtips to headless arrows (+2 Ranged Damage)."
        ),
        FletchRecipe(
            id = "fletch_iron_arrows",
            name = "Iron Arrows (15x)",
            reqLevel = 15,
            xpReward = 38L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_headless_arrow", "Headless Arrows", 15, "🪶"),
                RecipeMaterial("item_iron_arrowtip", "Iron Arrowtips", 15, "🗡️")
            ),
            outputItemId = "item_iron_arrows",
            outputItemName = "Iron Arrows",
            outputQuantity = 15,
            rangedPowerBonus = 5,
            description = "Attach iron arrowtips to headless arrows (+5 Ranged Damage)."
        ),
        FletchRecipe(
            id = "fletch_steel_arrows",
            name = "Steel Arrows (15x)",
            reqLevel = 30,
            xpReward = 75L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_headless_arrow", "Headless Arrows", 15, "🪶"),
                RecipeMaterial("item_steel_arrowtip", "Steel Arrowtips", 15, "🗡️")
            ),
            outputItemId = "item_steel_arrows",
            outputItemName = "Steel Arrows",
            outputQuantity = 15,
            rangedPowerBonus = 9,
            description = "Attach sharp steel arrowtips for piercing accuracy (+9 Ranged Damage)."
        ),
        FletchRecipe(
            id = "fletch_mithril_arrows",
            name = "Opalite Arrows (15x)",
            reqLevel = 50,
            xpReward = 125L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_headless_arrow", "Headless Arrows", 15, "🪶"),
                RecipeMaterial("item_mithril_arrowtip", "Opalite Arrowtips", 15, "🗡️")
            ),
            outputItemId = "item_mithril_arrows",
            outputItemName = "Opalite Arrows",
            outputQuantity = 15,
            rangedPowerBonus = 16,
            description = "Attach gleaming opalite arrowtips for fast aerodynamic shots (+16 Ranged Damage)."
        ),
        FletchRecipe(
            id = "fletch_adamant_arrows",
            name = "Amethyst Arrows (15x)",
            reqLevel = 70,
            xpReward = 175L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_headless_arrow", "Headless Arrows", 15, "🪶"),
                RecipeMaterial("item_adamant_arrowtip", "Amethyst Arrowtips", 15, "🗡️")
            ),
            outputItemId = "item_adamant_arrows",
            outputItemName = "Amethyst Arrows",
            outputQuantity = 15,
            rangedPowerBonus = 24,
            description = "Attach dense crystalline amethyst arrowtips to pierce monster hides (+24 Ranged Damage)."
        ),
        FletchRecipe(
            id = "fletch_rune_arrows",
            name = "Aetherite Arrows (15x)",
            reqLevel = 85,
            xpReward = 235L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_headless_arrow", "Headless Arrows", 15, "🪶"),
                RecipeMaterial("item_rune_arrowtip", "Aetherite Arrowtips", 15, "🗡️")
            ),
            outputItemId = "item_rune_arrows",
            outputItemName = "Aetherite Arrows",
            outputQuantity = 15,
            rangedPowerBonus = 35,
            description = "Attach masterwork cosmic aetherite arrowtips for ultimate piercing power (+35 Ranged Damage)."
        ),
        FletchRecipe(
            id = "fletch_dragon_arrows",
            name = "Dragon Arrows (15x)",
            reqLevel = 90,
            xpReward = 300L,
            iconEmoji = "🐉",
            inputMaterials = listOf(
                RecipeMaterial("item_headless_arrow", "Headless Arrows", 15, "🪶"),
                RecipeMaterial("item_dragon_arrowtip", "Dragon Arrowtips", 15, "🐉")
            ),
            outputItemId = "item_dragon_arrows",
            outputItemName = "Dragon Arrows",
            outputQuantity = 15,
            rangedPowerBonus = 48,
            description = "Legendary dragon arrows. Devastating armor-shredding damage (+48 Ranged Damage)."
        )
    )

    val BOW_RECIPES = listOf(
        FletchRecipe(
            id = "fletch_shortbow",
            name = "Shortbow",
            reqLevel = 1,
            xpReward = 30L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_logs", "Logs", 1, "🪵"),
                RecipeMaterial("item_bowstring", "Bowstring", 1, "🧵")
            ),
            outputItemId = "item_shortbow",
            outputItemName = "Shortbow",
            outputQuantity = 1,
            rangedPowerBonus = 8,
            description = "Basic wooden shortbow carved from timber logs."
        ),
        FletchRecipe(
            id = "fletch_birch_shortbow",
            name = "Birch Shortbow",
            reqLevel = 10,
            xpReward = 45L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_birch_logs", "Birch Logs", 1, "🪵"),
                RecipeMaterial("item_bowstring", "Bowstring", 1, "🧵")
            ),
            outputItemId = "item_birch_shortbow",
            outputItemName = "Birch Shortbow",
            outputQuantity = 1,
            rangedPowerBonus = 11,
            description = "Lightweight flexible shortbow carved from birch wood."
        ),
        FletchRecipe(
            id = "fletch_oak_shortbow",
            name = "Oak Shortbow",
            reqLevel = 20,
            xpReward = 65L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_oak_logs", "Oak Logs", 1, "🪵"),
                RecipeMaterial("item_bowstring", "Bowstring", 1, "🧵")
            ),
            outputItemId = "item_oak_shortbow",
            outputItemName = "Oak Shortbow",
            outputQuantity = 1,
            rangedPowerBonus = 15,
            description = "Sturdy shortbow carved from dense oak wood."
        ),
        FletchRecipe(
            id = "fletch_pine_shortbow",
            name = "Pine Shortbow",
            reqLevel = 28,
            xpReward = 90L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_pine_logs", "Pine Logs", 1, "🪵"),
                RecipeMaterial("item_bowstring", "Bowstring", 1, "🧵")
            ),
            outputItemId = "item_pine_shortbow",
            outputItemName = "Pine Shortbow",
            outputQuantity = 1,
            rangedPowerBonus = 19,
            description = "Resilient aromatic pine wood shortbow."
        ),
        FletchRecipe(
            id = "fletch_willow_shortbow",
            name = "Willow Shortbow",
            reqLevel = 35,
            xpReward = 115L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_willow_logs", "Willow Logs", 1, "🪵"),
                RecipeMaterial("item_bowstring", "Bowstring", 1, "🧵")
            ),
            outputItemId = "item_willow_shortbow",
            outputItemName = "Willow Shortbow",
            outputQuantity = 1,
            rangedPowerBonus = 23,
            description = "Fast, springy shortbow carved from pliable willow wood."
        ),
        FletchRecipe(
            id = "fletch_cedar_shortbow",
            name = "Cedar Shortbow",
            reqLevel = 42,
            xpReward = 140L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_cedar_logs", "Cedar Logs", 1, "🪵"),
                RecipeMaterial("item_bowstring", "Bowstring", 1, "🧵")
            ),
            outputItemId = "item_cedar_shortbow",
            outputItemName = "Cedar Shortbow",
            outputQuantity = 1,
            rangedPowerBonus = 28,
            description = "Balanced mountain cedar wood shortbow."
        ),
        FletchRecipe(
            id = "fletch_maple_shortbow",
            name = "Maple Shortbow",
            reqLevel = 50,
            xpReward = 175L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_maple_logs", "Maple Logs", 1, "🪵"),
                RecipeMaterial("item_bowstring", "Bowstring", 1, "🧵")
            ),
            outputItemId = "item_maple_shortbow",
            outputItemName = "Maple Shortbow",
            outputQuantity = 1,
            rangedPowerBonus = 34,
            description = "Polished dense autumn maple shortbow."
        ),
        FletchRecipe(
            id = "fletch_yew_shortbow",
            name = "Yew Shortbow",
            reqLevel = 65,
            xpReward = 220L,
            iconEmoji = "🏹",
            inputMaterials = listOf(
                RecipeMaterial("item_yew_logs", "Yew Logs", 1, "🪵"),
                RecipeMaterial("item_bowstring", "Bowstring", 1, "🧵")
            ),
            outputItemId = "item_yew_shortbow",
            outputItemName = "Yew Shortbow",
            outputQuantity = 1,
            rangedPowerBonus = 42,
            description = "Sacred yew shortbow renowned for exceptional range and power."
        ),
        FletchRecipe(
            id = "fletch_ironwood_shortbow",
            name = "Ironwood Shortbow",
            reqLevel = 75,
            xpReward = 280L,
            iconEmoji = "🪵",
            inputMaterials = listOf(
                RecipeMaterial("item_ironwood_logs", "Ironwood Logs", 1, "🪵"),
                RecipeMaterial("item_bowstring", "Bowstring", 1, "🧵")
            ),
            outputItemId = "item_ironwood_shortbow",
            outputItemName = "Ironwood Shortbow",
            outputQuantity = 1,
            rangedPowerBonus = 52,
            description = "Unbreakable heavy shortbow carved from dense metallic ironwood."
        ),
        FletchRecipe(
            id = "fletch_magic_shortbow",
            name = "Magic Shortbow",
            reqLevel = 85,
            xpReward = 360L,
            iconEmoji = "🔮",
            inputMaterials = listOf(
                RecipeMaterial("item_magic_logs", "Magic Logs", 1, "🪵"),
                RecipeMaterial("item_bowstring", "Bowstring", 1, "🧵")
            ),
            outputItemId = "item_magic_shortbow",
            outputItemName = "Magic Shortbow",
            outputQuantity = 1,
            rangedPowerBonus = 65,
            description = "Enchanted shortbow carved from mystic magic logs with supernatural draw."
        ),
        FletchRecipe(
            id = "fletch_redwood_shortbow",
            name = "Redwood Shortbow",
            reqLevel = 90,
            xpReward = 450L,
            iconEmoji = "🔴",
            inputMaterials = listOf(
                RecipeMaterial("item_redwood_logs", "Redwood Logs", 1, "🪵"),
                RecipeMaterial("item_bowstring", "Bowstring", 1, "🧵")
            ),
            outputItemId = "item_redwood_shortbow",
            outputItemName = "Redwood Shortbow",
            outputQuantity = 1,
            rangedPowerBonus = 78,
            description = "Colossal ancient redwood bow delivering devastating kinetic force."
        )
    )

    val ALL_RECIPES = ARROW_RECIPES + BOW_RECIPES
}
