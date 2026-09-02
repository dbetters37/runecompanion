package com.example.data.models

data class CookingFireRecipe(
    val rawId: String,
    val rawName: String,
    val emoji: String,
    val cookedId: String,
    val cookedName: String,
    val cookedEmoji: String,
    val reqLevel: Int,
    val stopBurnLevel: Int,
    val xpEarned: Long,
    val healHp: Int,
    val restoreHunger: Int,
    val happinessGain: Int,
    val description: String,
    val category: CookingCategory = CookingCategory.FISH
)

enum class CookingCategory(val label: String, val emoji: String) {
    ALL("All Foods", "🍱"),
    READY_TO_COOK("Ready to Cook", "🍳"),
    RECIPES("Recipes", "🍲"),
    FISH("Fish & Seafood", "🐟"),
    MEAT("Meats & Poultry", "🍗"),
    BAKERY("Bakery & Pies", "🥧"),
    SPIRIT("Spirit & Cosmic", "✨")
}

object CookingRecipes {
    const val BASE_QUEUE_LIMIT = 3
    const val MID_QUEUE_LIMIT = 4
    const val MAX_QUEUE_LIMIT = 5

    fun getMaxQueueSlots(cookingLevel: Int): Int {
        return when {
            cookingLevel >= 80 -> MAX_QUEUE_LIMIT
            cookingLevel >= 50 -> MID_QUEUE_LIMIT
            else -> BASE_QUEUE_LIMIT
        }
    }

    fun getNextUnlockDescription(cookingLevel: Int): String {
        return when {
            cookingLevel >= 80 -> "🎉 Max Capacity Unlocked (5 Queue Slots)"
            cookingLevel >= 50 -> "🔓 Lv.80 Cooking unlocks 5th Queue Slot!"
            else -> "🔓 Lv.50 unlocks 4th Slot • Lv.80 unlocks 5th Slot!"
        }
    }

    val ALL_RECIPES = listOf(
        // NOVICE / SHALLOWS
        CookingFireRecipe(
            rawId = "item_raw_shrimps",
            rawName = "Raw Shrimps",
            emoji = "🦐",
            cookedId = "item_shrimps",
            cookedName = "Cooked Shrimps",
            cookedEmoji = "🦐",
            reqLevel = 1,
            stopBurnLevel = 34,
            xpEarned = 30L,
            healHp = 10,
            restoreHunger = 15,
            happinessGain = 8,
            description = "Small cooked freshwater shrimps.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_anchovies",
            rawName = "Raw Anchovies",
            emoji = "🐟",
            cookedId = "item_anchovies",
            cookedName = "Cooked Anchovies",
            cookedEmoji = "🐟",
            reqLevel = 1,
            stopBurnLevel = 34,
            xpEarned = 35L,
            healHp = 12,
            restoreHunger = 16,
            happinessGain = 8,
            description = "Salty roasted anchovies packed with flavor.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_chicken",
            rawName = "Raw Chicken",
            emoji = "🍗",
            cookedId = "item_cooked_chicken",
            cookedName = "Cooked Chicken",
            cookedEmoji = "🍗",
            reqLevel = 1,
            stopBurnLevel = 34,
            xpEarned = 30L,
            healHp = 12,
            restoreHunger = 18,
            happinessGain = 9,
            description = "Tender flame roasted farm chicken.",
            category = CookingCategory.MEAT
        ),
        CookingFireRecipe(
            rawId = "item_raw_sardine",
            rawName = "Raw Sardine",
            emoji = "🐟",
            cookedId = "item_sardine",
            cookedName = "Cooked Sardine",
            cookedEmoji = "🐟",
            reqLevel = 1,
            stopBurnLevel = 38,
            xpEarned = 45L,
            healHp = 15,
            restoreHunger = 20,
            happinessGain = 10,
            description = "Crispy grilled coastal sardine.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_herring",
            rawName = "Raw Herring",
            emoji = "🐟",
            cookedId = "item_herring",
            cookedName = "Cooked Herring",
            cookedEmoji = "🐟",
            reqLevel = 5,
            stopBurnLevel = 41,
            xpEarned = 50L,
            healHp = 16,
            restoreHunger = 22,
            happinessGain = 10,
            description = "Smoky grilled coastal herring.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_mackerel",
            rawName = "Raw Mackerel",
            emoji = "🐟",
            cookedId = "item_mackerel",
            cookedName = "Cooked Mackerel",
            cookedEmoji = "🐟",
            reqLevel = 10,
            stopBurnLevel = 45,
            xpEarned = 60L,
            healHp = 18,
            restoreHunger = 24,
            happinessGain = 12,
            description = "Flavorful flame-cooked sea mackerel.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_bird_meat",
            rawName = "Raw Bird Meat",
            emoji = "🍗",
            cookedId = "item_cooked_meat",
            cookedName = "Roast Bird Meat",
            cookedEmoji = "🍗",
            reqLevel = 1,
            stopBurnLevel = 30,
            xpEarned = 35L,
            healHp = 15,
            restoreHunger = 20,
            happinessGain = 10,
            description = "Succulent roasted bird meat cooked over open fire.",
            category = CookingCategory.MEAT
        ),
        CookingFireRecipe(
            rawId = "item_raw_meat",
            rawName = "Raw Beef",
            emoji = "🥩",
            cookedId = "item_cooked_meat",
            cookedName = "Cooked Meat",
            cookedEmoji = "🥩",
            reqLevel = 1,
            stopBurnLevel = 30,
            xpEarned = 40L,
            healHp = 18,
            restoreHunger = 24,
            happinessGain = 12,
            description = "Juicy flame-grilled steak.",
            category = CookingCategory.MEAT
        ),
        CookingFireRecipe(
            rawId = "item_raw_bear_meat",
            rawName = "Raw Bear Meat",
            emoji = "🥩",
            cookedId = "item_cooked_meat",
            cookedName = "Cooked Bear Meat",
            cookedEmoji = "🥩",
            reqLevel = 1,
            stopBurnLevel = 30,
            xpEarned = 40L,
            healHp = 18,
            restoreHunger = 25,
            happinessGain = 12,
            description = "Hearty wild game bear steak.",
            category = CookingCategory.MEAT
        ),
        CookingFireRecipe(
            rawId = "item_bread_dough",
            rawName = "Bread Dough",
            emoji = "🌾",
            cookedId = "item_bread",
            cookedName = "Fresh Baked Bread",
            cookedEmoji = "🍞",
            reqLevel = 1,
            stopBurnLevel = 35,
            xpEarned = 40L,
            healHp = 12,
            restoreHunger = 22,
            happinessGain = 10,
            description = "Warm golden loaf of artisan bread.",
            category = CookingCategory.BAKERY
        ),

        // RIVER / MID-TIER
        CookingFireRecipe(
            rawId = "item_raw_trout",
            rawName = "Raw Trout",
            emoji = "🐟",
            cookedId = "item_trout",
            cookedName = "Cooked Trout",
            cookedEmoji = "🐟",
            reqLevel = 15,
            stopBurnLevel = 50,
            xpEarned = 70L,
            healHp = 20,
            restoreHunger = 28,
            happinessGain = 15,
            description = "Delicious flame-cooked river trout.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_cod",
            rawName = "Raw Cod",
            emoji = "🐟",
            cookedId = "item_cod",
            cookedName = "Cooked Cod",
            cookedEmoji = "🐟",
            reqLevel = 18,
            stopBurnLevel = 52,
            xpEarned = 75L,
            healHp = 22,
            restoreHunger = 30,
            happinessGain = 15,
            description = "Fresh white sea cod seared over hot embers.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_pike",
            rawName = "Raw Pike",
            emoji = "🐟",
            cookedId = "item_pike",
            cookedName = "Cooked Pike",
            cookedEmoji = "🐟",
            reqLevel = 20,
            stopBurnLevel = 64,
            xpEarned = 80L,
            healHp = 25,
            restoreHunger = 32,
            happinessGain = 16,
            description = "Firm and tasty flame-grilled river pike.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_yak_meat",
            rawName = "Raw Yak Meat",
            emoji = "🥩",
            cookedId = "item_cooked_meat",
            cookedName = "Roast Yak Meat",
            cookedEmoji = "🥩",
            reqLevel = 20,
            stopBurnLevel = 55,
            xpEarned = 75L,
            healHp = 26,
            restoreHunger = 34,
            happinessGain = 16,
            description = "Tough mountain yak roasted to savory tenderness.",
            category = CookingCategory.MEAT
        ),
        CookingFireRecipe(
            rawId = "item_raw_salmon",
            rawName = "Raw Salmon",
            emoji = "🐟",
            cookedId = "item_salmon",
            cookedName = "Cooked Salmon",
            cookedEmoji = "🐟",
            reqLevel = 25,
            stopBurnLevel = 58,
            xpEarned = 90L,
            healHp = 30,
            restoreHunger = 36,
            happinessGain = 18,
            description = "Savory pink salmon rich in healthy omega oils.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_uncooked_stew",
            rawName = "Uncooked Stew",
            emoji = "🍲",
            cookedId = "item_stew",
            cookedName = "Hearty Meat Stew",
            cookedEmoji = "🍲",
            reqLevel = 25,
            stopBurnLevel = 58,
            xpEarned = 117L,
            healHp = 35,
            restoreHunger = 40,
            happinessGain = 20,
            description = "Rich broth stew packed with tender meat and potatoes.",
            category = CookingCategory.BAKERY
        ),
        CookingFireRecipe(
            rawId = "item_raw_tuna",
            rawName = "Raw Tuna",
            emoji = "🐟",
            cookedId = "item_tuna",
            cookedName = "Cooked Tuna",
            cookedEmoji = "🐟",
            reqLevel = 30,
            stopBurnLevel = 64,
            xpEarned = 100L,
            healHp = 45,
            restoreHunger = 42,
            happinessGain = 20,
            description = "Hearty cooked tuna steak.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_karambwan",
            rawName = "Raw Karambwan",
            emoji = "🐙",
            cookedId = "item_karambwan",
            cookedName = "Cooked Karambwan",
            cookedEmoji = "🐙",
            reqLevel = 30,
            stopBurnLevel = 70,
            xpEarned = 190L,
            healHp = 50,
            restoreHunger = 45,
            happinessGain = 22,
            description = "Exotic Karamjan sea creature with instant healing powers.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_uncooked_pie",
            rawName = "Uncooked Apple Pie",
            emoji = "🥧",
            cookedId = "item_apple_pie",
            cookedName = "Baked Apple Pie",
            cookedEmoji = "🥧",
            reqLevel = 30,
            stopBurnLevel = 60,
            xpEarned = 130L,
            healHp = 40,
            restoreHunger = 45,
            happinessGain = 25,
            description = "Sweet spiced apple pie with flaky crust.",
            category = CookingCategory.BAKERY
        ),
        CookingFireRecipe(
            rawId = "item_uncooked_pizza",
            rawName = "Uncooked Pizza",
            emoji = "🍕",
            cookedId = "item_plain_pizza",
            cookedName = "Plain Pizza",
            cookedEmoji = "🍕",
            reqLevel = 35,
            stopBurnLevel = 68,
            xpEarned = 145L,
            healHp = 48,
            restoreHunger = 48,
            happinessGain = 25,
            description = "Cheesy fire-baked pizza.",
            category = CookingCategory.BAKERY
        ),
        CookingFireRecipe(
            rawId = "item_raw_lobster",
            rawName = "Raw Lobster",
            emoji = "🦞",
            cookedId = "item_lobster",
            cookedName = "Cooked Lobster",
            cookedEmoji = "🦞",
            reqLevel = 40,
            stopBurnLevel = 74,
            xpEarned = 120L,
            healHp = 60,
            restoreHunger = 50,
            happinessGain = 25,
            description = "Boiled reef lobster served with melted butter aroma.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_bass",
            rawName = "Raw Bass",
            emoji = "🐟",
            cookedId = "item_bass",
            cookedName = "Cooked Bass",
            cookedEmoji = "🐟",
            reqLevel = 43,
            stopBurnLevel = 80,
            xpEarned = 130L,
            healHp = 65,
            restoreHunger = 52,
            happinessGain = 26,
            description = "Rich succulent deep sea bass.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_swordfish",
            rawName = "Raw Swordfish",
            emoji = "🗡️",
            cookedId = "item_swordfish",
            cookedName = "Cooked Swordfish",
            cookedEmoji = "🗡️",
            reqLevel = 45,
            stopBurnLevel = 81,
            xpEarned = 140L,
            healHp = 90,
            restoreHunger = 60,
            happinessGain = 30,
            description = "A grand champion's meal of fire-seared swordfish.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_uncooked_curry",
            rawName = "Uncooked Curry",
            emoji = "🍛",
            cookedId = "item_curry",
            cookedName = "Spiced Curry",
            cookedEmoji = "🍛",
            reqLevel = 60,
            stopBurnLevel = 85,
            xpEarned = 280L,
            healHp = 110,
            restoreHunger = 70,
            happinessGain = 35,
            description = "Fiery aromatic curry simmered over charcoal fire.",
            category = CookingCategory.BAKERY
        ),

        // HIGH TIER / ABYSS
        CookingFireRecipe(
            rawId = "item_raw_monkfish",
            rawName = "Raw Monkfish",
            emoji = "🐟",
            cookedId = "item_monkfish",
            cookedName = "Cooked Monkfish",
            cookedEmoji = "🐟",
            reqLevel = 62,
            stopBurnLevel = 90,
            xpEarned = 150L,
            healHp = 100,
            restoreHunger = 65,
            happinessGain = 32,
            description = "Delicacy monkfish seasoned with sea herbs.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_shark",
            rawName = "Raw Shark",
            emoji = "🦈",
            cookedId = "item_shark",
            cookedName = "Cooked Shark",
            cookedEmoji = "🦈",
            reqLevel = 80,
            stopBurnLevel = 99,
            xpEarned = 210L,
            healHp = 120,
            restoreHunger = 75,
            happinessGain = 35,
            description = "Mighty cooked apex shark providing tremendous vitality.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_sea_turtle",
            rawName = "Raw Sea Turtle",
            emoji = "🐢",
            cookedId = "item_sea_turtle",
            cookedName = "Cooked Sea Turtle",
            cookedEmoji = "🐢",
            reqLevel = 82,
            stopBurnLevel = 99,
            xpEarned = 240L,
            healHp = 150,
            restoreHunger = 80,
            happinessGain = 40,
            description = "Ancient oceanic delicacy prized by high adventurers.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_anglerfish",
            rawName = "Raw Anglerfish",
            emoji = "🐡",
            cookedId = "item_anglerfish",
            cookedName = "Cooked Anglerfish",
            cookedEmoji = "🐡",
            reqLevel = 84,
            stopBurnLevel = 99,
            xpEarned = 230L,
            healHp = 160,
            restoreHunger = 85,
            happinessGain = 42,
            description = "Deep sea delicacy that overheals max health!",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_dark_crab",
            rawName = "Raw Dark Crab",
            emoji = "🦀",
            cookedId = "item_dark_crab",
            cookedName = "Cooked Dark Crab",
            cookedEmoji = "🦀",
            reqLevel = 90,
            stopBurnLevel = 99,
            xpEarned = 260L,
            healHp = 175,
            restoreHunger = 88,
            happinessGain = 44,
            description = "Wilderness delicacy offering exceptional sustenance.",
            category = CookingCategory.FISH
        ),
        CookingFireRecipe(
            rawId = "item_raw_manta_ray",
            rawName = "Raw Manta Ray",
            emoji = "🐋",
            cookedId = "item_manta_ray",
            cookedName = "Cooked Manta Ray",
            cookedEmoji = "🐋",
            reqLevel = 91,
            stopBurnLevel = 99,
            xpEarned = 300L,
            healHp = 180,
            restoreHunger = 90,
            happinessGain = 45,
            description = "Supreme banquet meal restoring colossal health and spirit.",
            category = CookingCategory.FISH
        ),

        // SPIRIT OASIS & COSMIC
        CookingFireRecipe(
            rawId = "item_spirit_koi",
            rawName = "Spirit Koi",
            emoji = "🎏",
            cookedId = "item_cooked_spirit_koi",
            cookedName = "Seared Spirit Koi",
            cookedEmoji = "🎏",
            reqLevel = 85,
            stopBurnLevel = 99,
            xpEarned = 380L,
            healHp = 250,
            restoreHunger = 95,
            happinessGain = 55,
            description = "Golden spirit koi seared in sacred flame.",
            category = CookingCategory.SPIRIT
        ),
        CookingFireRecipe(
            rawId = "item_astral_angler",
            rawName = "Astral Anglerfish",
            emoji = "🐡",
            cookedId = "item_cooked_astral_angler",
            cookedName = "Starlight Angler",
            cookedEmoji = "🐡",
            reqLevel = 90,
            stopBurnLevel = 99,
            xpEarned = 450L,
            healHp = 300,
            restoreHunger = 98,
            happinessGain = 60,
            description = "Bioluminescent anglerfish crackling with cosmic stardust.",
            category = CookingCategory.SPIRIT
        ),
        CookingFireRecipe(
            rawId = "item_ethereal_ray",
            rawName = "Ethereal Ray",
            emoji = "🌌",
            cookedId = "item_cooked_ethereal_ray",
            cookedName = "Celestial Ethereal Ray",
            cookedEmoji = "🌌",
            reqLevel = 92,
            stopBurnLevel = 99,
            xpEarned = 520L,
            healHp = 350,
            restoreHunger = 100,
            happinessGain = 65,
            description = "Shimmering ethereal ray blessing the companion with transcendence.",
            category = CookingCategory.SPIRIT
        ),
        CookingFireRecipe(
            rawId = "item_magma_eel",
            rawName = "Magma Eel",
            emoji = "🐍",
            cookedId = "item_cooked_magma_eel",
            cookedName = "Volcanic Smoked Eel",
            cookedEmoji = "🐍",
            reqLevel = 90,
            stopBurnLevel = 99,
            xpEarned = 600L,
            healHp = 420,
            restoreHunger = 100,
            happinessGain = 70,
            description = "Lava-infused eel smoked over intense volcanic embers.",
            category = CookingCategory.SPIRIT
        ),
        CookingFireRecipe(
            rawId = "item_ember_trout",
            rawName = "Ember Trout",
            emoji = "🔥",
            cookedId = "item_cooked_ember_trout",
            cookedName = "Flame-Glazed Ember Trout",
            cookedEmoji = "🔥",
            reqLevel = 93,
            stopBurnLevel = 99,
            xpEarned = 700L,
            healHp = 500,
            restoreHunger = 100,
            happinessGain = 75,
            description = "Radiant molten trout providing immense vitality.",
            category = CookingCategory.SPIRIT
        ),
        CookingFireRecipe(
            rawId = "item_obsidian_crab",
            rawName = "Obsidian Crab",
            emoji = "🦀",
            cookedId = "item_cooked_obsidian_crab",
            cookedName = "Roasted Obsidian Crab",
            cookedEmoji = "🦀",
            reqLevel = 95,
            stopBurnLevel = 99,
            xpEarned = 850L,
            healHp = 600,
            restoreHunger = 100,
            happinessGain = 80,
            description = "Armored crab roasted to perfection in volcanic heart.",
            category = CookingCategory.SPIRIT
        ),
        CookingFireRecipe(
            rawId = "item_sacred_shaman_fish",
            rawName = "Sacred Shaman Fish",
            emoji = "🌟",
            cookedId = "item_cooked_sacred_shaman_fish",
            cookedName = "Divine Shaman Feast",
            cookedEmoji = "🌟",
            reqLevel = 99,
            stopBurnLevel = 99,
            xpEarned = 1200L,
            healHp = 750,
            restoreHunger = 100,
            happinessGain = 90,
            description = "Supreme holy feast blessing the soul with divine enlightenment.",
            category = CookingCategory.SPIRIT
        ),
        CookingFireRecipe(
            rawId = "item_cosmic_whale",
            rawName = "Cosmic Whale",
            emoji = "🐳",
            cookedId = "item_cooked_cosmic_whale",
            cookedName = "Cosmic Leviathan Roast",
            cookedEmoji = "🐳",
            reqLevel = 99,
            stopBurnLevel = 99,
            xpEarned = 1600L,
            healHp = 950,
            restoreHunger = 100,
            happinessGain = 95,
            description = "Colossal cosmic feast pulsing with starlight energy.",
            category = CookingCategory.SPIRIT
        ),
        CookingFireRecipe(
            rawId = "item_golden_dragonfish",
            rawName = "Golden Dragonfish",
            emoji = "🐉",
            cookedId = "item_cooked_golden_dragonfish",
            cookedName = "Mythical Dragonfish Feast",
            cookedEmoji = "🐉",
            reqLevel = 99,
            stopBurnLevel = 99,
            xpEarned = 2200L,
            healHp = 1200,
            restoreHunger = 100,
            happinessGain = 100,
            description = "Pinnacle legend food of supreme spirit mastery.",
            category = CookingCategory.SPIRIT
        )
    )

    fun findRecipe(rawId: String): CookingFireRecipe? {
        val norm = rawId.lowercase()
        val exact = ALL_RECIPES.find { it.rawId.equals(norm, ignoreCase = true) }
        if (exact != null) return exact
        if (norm.startsWith("item_cooked_") || norm.startsWith("cooked_")) return null
        val clean = norm.removePrefix("item_raw_").removePrefix("raw_").removePrefix("item_")
        return ALL_RECIPES.find {
            val recipeClean = it.rawId.removePrefix("item_raw_").removePrefix("raw_").removePrefix("item_")
            recipeClean.equals(clean, ignoreCase = true)
        }
    }

    fun isRawFoodId(rawId: String): Boolean {
        if (findRecipe(rawId) != null) return true
        val lowerId = rawId.lowercase()
        return lowerId.startsWith("item_raw_") ||
                lowerId.contains("raw_") ||
                lowerId.contains("uncooked") ||
                lowerId == "item_bread_dough" ||
                lowerId == "item_uncooked_pie" ||
                lowerId == "item_uncooked_pizza" ||
                lowerId == "item_uncooked_stew" ||
                lowerId == "item_uncooked_curry"
    }

    fun isRawFoodItem(item: InventoryItem): Boolean {
        if (item.isRawUncookedFood) return true
        if (findRecipe(item.id) != null) return true
        val lowerId = item.id.lowercase()
        val lowerName = item.name.lowercase()
        return lowerId.startsWith("item_raw_") ||
                lowerId.contains("raw_") ||
                lowerId.contains("uncooked") ||
                lowerName.startsWith("raw ") ||
                lowerName.contains("raw") ||
                lowerName.startsWith("uncooked ") ||
                lowerName.contains("uncooked") ||
                lowerName.contains("dough") ||
                lowerId == "item_bread_dough" ||
                lowerId == "item_uncooked_pie"
    }

    fun getOrCreateRecipeForItem(item: InventoryItem): CookingFireRecipe {
        val existing = findRecipe(item.id)
        if (existing != null) return existing

        val cleanName = item.name.replace("Raw ", "", ignoreCase = true)
            .replace("Uncooked ", "", ignoreCase = true)
            .trim()
        val cleanId = item.id.replace("item_raw_", "").replace("item_uncooked_", "").replace("item_", "")

        val category = when {
            item.name.contains("fish", true) || item.name.contains("shrimp", true) ||
            item.name.contains("trout", true) || item.name.contains("crab", true) ||
            item.name.contains("ray", true) || item.name.contains("eel", true) ||
            item.name.contains("angler", true) || item.name.contains("koi", true) ||
            item.name.contains("lobster", true) || item.name.contains("shark", true) ||
            item.name.contains("turtle", true) || item.name.contains("whale", true) -> CookingCategory.FISH

            item.name.contains("pie", true) || item.name.contains("bread", true) ||
            item.name.contains("cake", true) || item.name.contains("pizza", true) ||
            item.name.contains("dough", true) || item.name.contains("stew", true) ||
            item.name.contains("curry", true) -> CookingCategory.BAKERY

            item.name.contains("spirit", true) || item.name.contains("astral", true) ||
            item.name.contains("ethereal", true) || item.name.contains("cosmic", true) ||
            item.name.contains("divine", true) || item.name.contains("sacred", true) -> CookingCategory.SPIRIT

            else -> CookingCategory.MEAT
        }

        return CookingFireRecipe(
            rawId = item.id,
            rawName = item.name,
            emoji = if (item.iconEmoji.isNotBlank()) item.iconEmoji else "🥩",
            cookedId = "item_cooked_$cleanId",
            cookedName = "Cooked $cleanName",
            cookedEmoji = if (item.iconEmoji.isNotBlank()) item.iconEmoji else "🍲",
            reqLevel = 1,
            stopBurnLevel = 35,
            xpEarned = 45L,
            healHp = 20,
            restoreHunger = 25,
            happinessGain = 12,
            description = "Freshly cooked $cleanName prepared over an open fire.",
            category = category
        )
    }

    val ALL_COOKED_FOOD_ITEMS: List<InventoryItem> by lazy {
        ALL_RECIPES.map { recipe ->
            InventoryItem(
                id = recipe.cookedId,
                name = recipe.cookedName,
                category = ItemCategory.FOOD,
                iconEmoji = recipe.cookedEmoji,
                description = recipe.description,
                costGp = (recipe.healHp * 15L).coerceAtLeast(10L),
                healHp = recipe.healHp,
                restoreHunger = recipe.restoreHunger,
                addHappiness = recipe.happinessGain
            )
        }.distinctBy { it.id }
    }
}
