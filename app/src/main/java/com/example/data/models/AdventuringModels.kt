package com.example.data.models

import androidx.compose.ui.graphics.Color

data class EnemyAttack(
    val id: String,
    val name: String,
    val emoji: String,
    val damagePower: Int,
    val shieldPierce: Boolean = false,
    val description: String = "",
    val specialEffect: String? = null
)

data class AdventuringMonster(
    val id: String,
    val name: String,
    val emoji: String,
    val floorLevel: Int,
    val hp: Int,
    val maxHp: Int,
    val attackPower: Int,
    val defence: Int,
    val xpReward: Long,
    val gpReward: Long,
    val storyLore: String,
    val combatLevel: Int = 3,
    val attackCards: List<EnemyAttack> = emptyList()
) {
    val effectiveAttackCards: List<EnemyAttack>
        get() {
            if (attackCards.isNotEmpty()) return attackCards
            val baseDmg = attackPower.coerceAtLeast(4)
            return listOf(
                EnemyAttack(
                    id = "${id}_atk_1",
                    name = "$name Strike",
                    emoji = emoji,
                    damagePower = baseDmg,
                    description = "Standard melee slash dealing $baseDmg physical damage."
                ),
                EnemyAttack(
                    id = "${id}_atk_2",
                    name = "$name Heavy Smash",
                    emoji = "💥",
                    damagePower = (baseDmg * 1.25).toInt(),
                    description = "Heavy power attack dealing extra damage."
                ),
                EnemyAttack(
                    id = "${id}_atk_3",
                    name = "$name Piercing Strike",
                    emoji = "⚡",
                    damagePower = (baseDmg * 0.9).toInt(),
                    shieldPierce = true,
                    description = "Swift strike that pierces player's defense shield."
                )
            )
        }
}

data class CauldronRecipe(
    val id: String,
    val name: String,
    val emoji: String,
    val reqLevel: Int,
    val buffEffect: String,
    val healthRestored: Int,
    val hungerRestored: Int,
    val description: String,
    val requiredRawItemId: String,
    val rawItemName: String,
    val requiredItem2Id: String,
    val item2Name: String,
    val cookedItemName: String,
    val cookingXp: Long,
    val boostedSkill: OsrsSkill? = null,
    val xpBoostPercent: Int = 15,
    val skillBoostDescription: String = "+15% Skill XP Boost (8 hrs)"
)

data class ActiveCookingBuff(
    val recipeId: String,
    val recipeName: String,
    val emoji: String,
    val buffEffect: String,
    val expiryTimeMs: Long,
    val boostedSkill: OsrsSkill? = null,
    val xpBoostPercent: Int = 15,
    val durationHours: Int = 8
)

object CauldronRecipes {
    val ALL_RECIPES = listOf(
        CauldronRecipe(
            id = "rec_melee_brawler_stew",
            name = "Brawler's Melee Brew",
            emoji = "🥩",
            reqLevel = 1,
            buffEffect = "+10% Melee Attack & Card Damage",
            healthRestored = 45,
            hungerRestored = 45,
            description = "Robust meat & potato stew that sharpens melee focus, boosting melee attack card damage by +10%!",
            requiredRawItemId = "item_raw_bird_meat",
            rawItemName = "Raw Meat",
            requiredItem2Id = "item_potato",
            item2Name = "Fresh Potato",
            cookedItemName = "Brawler's Melee Brew",
            cookingXp = 80L,
            boostedSkill = OsrsSkill.ATTACK,
            xpBoostPercent = 10,
            skillBoostDescription = "+10% Melee Combat Damage"
        ),
        CauldronRecipe(
            id = "rec_blowdart_hunter_salad",
            name = "Blow Dart Hunter Salad",
            emoji = "🎯",
            reqLevel = 1,
            buffEffect = "+10% Blow Darts & Ranged Boost",
            healthRestored = 45,
            hungerRestored = 45,
            description = "Zesty shrimp & cabbage salad that enhances dexterity and blow dart venom damage by +10%!",
            requiredRawItemId = "item_raw_shrimps",
            rawItemName = "Raw Shrimps",
            requiredItem2Id = "item_cabbage",
            item2Name = "Fresh Cabbage",
            cookedItemName = "Blow Dart Hunter Salad",
            cookingXp = 80L,
            boostedSkill = OsrsSkill.RANGED,
            xpBoostPercent = 10,
            skillBoostDescription = "+10% Blow Darts & Ranged Damage"
        ),
        CauldronRecipe(
            id = "rec_magic_mystic_broth",
            name = "Mystic Arcane Broth",
            emoji = "🪄",
            reqLevel = 1,
            buffEffect = "+10% Magic Spell & Incantation Boost",
            healthRestored = 45,
            hungerRestored = 45,
            description = "Herb-infused warm broth that channels magical energy, boosting magic spell card power by +10%!",
            requiredRawItemId = "item_bread",
            rawItemName = "Fresh Bread",
            requiredItem2Id = "item_clean_guam",
            item2Name = "Greenleaf Herb",
            cookedItemName = "Mystic Arcane Broth",
            cookingXp = 80L,
            boostedSkill = OsrsSkill.MAGIC,
            xpBoostPercent = 10,
            skillBoostDescription = "+10% Magic Card Power"
        ),
        // VEGETABLE & LOG RECIPES (A unique log recipe for each vegetable)
        CauldronRecipe(
            id = "rec_potato_logs",
            name = "Hearth-Roasted Potato",
            emoji = "🥔",
            reqLevel = 1,
            buffEffect = "+15% Forging XP Boost (8 hrs)",
            healthRestored = 50,
            hungerRestored = 50,
            description = "Fresh farm potato roasted over natural logs. Grants +15% Forging XP Boost for 8 Hours!",
            requiredRawItemId = "item_potato",
            rawItemName = "Fresh Potato",
            requiredItem2Id = "item_logs",
            item2Name = "Normal Logs",
            cookedItemName = "Hearth-Roasted Potato",
            cookingXp = 100L,
            boostedSkill = OsrsSkill.SMITHING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Forging XP Boost"
        ),
        CauldronRecipe(
            id = "rec_onion_oak",
            name = "Oak-Charred Sweet Onion",
            emoji = "🧅",
            reqLevel = 5,
            buffEffect = "+15% Fishing XP Boost (8 hrs)",
            healthRestored = 55,
            hungerRestored = 55,
            description = "Fresh garden onion slow-charred over sturdy oak logs. Grants +15% Fishing XP Boost for 8 Hours!",
            requiredRawItemId = "item_onion",
            rawItemName = "Fresh Onion",
            requiredItem2Id = "item_oak_logs",
            item2Name = "Oak Logs",
            cookedItemName = "Oak-Charred Sweet Onion",
            cookingXp = 140L,
            boostedSkill = OsrsSkill.FISHING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Fishing XP Boost"
        ),
        CauldronRecipe(
            id = "rec_cabbage_willow",
            name = "Willow-Smoked Cabbage Bowl",
            emoji = "🥬",
            reqLevel = 10,
            buffEffect = "+15% Defence XP Boost (8 hrs)",
            healthRestored = 60,
            hungerRestored = 60,
            description = "Fresh green cabbage smoked over aromatic willow logs. Grants +15% Warding XP Boost for 8 Hours!",
            requiredRawItemId = "item_cabbage",
            rawItemName = "Fresh Cabbage",
            requiredItem2Id = "item_willow_logs",
            item2Name = "Willow Logs",
            cookedItemName = "Willow-Smoked Cabbage Bowl",
            cookingXp = 180L,
            boostedSkill = OsrsSkill.DEFENCE,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Warding XP Boost"
        ),
        CauldronRecipe(
            id = "rec_carrot_teak",
            name = "Teak-Braised Glazed Carrots",
            emoji = "🥕",
            reqLevel = 20,
            buffEffect = "+15% Hunter XP Boost (8 hrs)",
            healthRestored = 75,
            hungerRestored = 75,
            description = "Crunchy farm carrots braised gently with tropical teak logs. Grants +15% Hunter XP Boost for 8 Hours!",
            requiredRawItemId = "item_carrot",
            rawItemName = "Crunchy Carrot",
            requiredItem2Id = "item_teak_logs",
            item2Name = "Teak Logs",
            cookedItemName = "Teak-Braised Glazed Carrots",
            cookingXp = 240L,
            boostedSkill = OsrsSkill.HUNTER,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Hunter XP Boost"
        ),
        CauldronRecipe(
            id = "rec_tomato_maple",
            name = "Maple Ember Roasted Tomato",
            emoji = "🍅",
            reqLevel = 30,
            buffEffect = "+15% Cooking XP Boost (8 hrs)",
            healthRestored = 90,
            hungerRestored = 85,
            description = "Juicy vine tomatoes roasted over sweet maple embers. Grants +15% Cooking XP Boost for 8 Hours!",
            requiredRawItemId = "item_tomato",
            rawItemName = "Juicy Tomato",
            requiredItem2Id = "item_maple_logs",
            item2Name = "Maple Logs",
            cookedItemName = "Maple Ember Roasted Tomato",
            cookingXp = 320L,
            boostedSkill = OsrsSkill.COOKING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Cooking XP Boost"
        ),
        CauldronRecipe(
            id = "rec_sweetcorn_mahogany",
            name = "Mahogany Fire Sweetcorn",
            emoji = "🌽",
            reqLevel = 40,
            buffEffect = "+15% Construction XP Boost (8 hrs)",
            healthRestored = 110,
            hungerRestored = 95,
            description = "Golden sweetcorn charred to perfection over exotic mahogany logs. Grants +15% Construction XP Boost for 8 Hours!",
            requiredRawItemId = "item_sweetcorn",
            rawItemName = "Golden Sweetcorn",
            requiredItem2Id = "item_mahogany_logs",
            item2Name = "Mahogany Logs",
            cookedItemName = "Mahogany Fire Sweetcorn",
            cookingXp = 420L,
            boostedSkill = OsrsSkill.CONSTRUCTION,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Construction XP Boost"
        ),
        CauldronRecipe(
            id = "rec_strawberry_yew",
            name = "Yew Ember Berry Medley",
            emoji = "🍓",
            reqLevel = 50,
            buffEffect = "+15% Fletching XP Boost (8 hrs)",
            healthRestored = 130,
            hungerRestored = 95,
            description = "Ripe sweet strawberries gently warm-roasted over ancient yew logs. Grants +15% Fletching XP Boost for 8 Hours!",
            requiredRawItemId = "item_strawberry",
            rawItemName = "Ripe Strawberry",
            requiredItem2Id = "item_yew_logs",
            item2Name = "Yew Logs",
            cookedItemName = "Yew Ember Berry Medley",
            cookingXp = 520L,
            boostedSkill = OsrsSkill.FLETCHING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Fletching XP Boost"
        ),
        CauldronRecipe(
            id = "rec_pumpkin_magic",
            name = "Magic Flame Pumpkin Feast",
            emoji = "🎃",
            reqLevel = 60,
            buffEffect = "+20% Magic XP Boost (8 hrs)",
            healthRestored = 160,
            hungerRestored = 100,
            description = "Giant harvest pumpkin roasted over enchanted magic logs. Grants +20% Magic XP Boost for 8 Hours!",
            requiredRawItemId = "item_pumpkin",
            rawItemName = "Giant Pumpkin",
            requiredItem2Id = "item_magic_logs",
            item2Name = "Magic Logs",
            cookedItemName = "Magic Flame Pumpkin Feast",
            cookingXp = 650L,
            boostedSkill = OsrsSkill.MAGIC,
            xpBoostPercent = 20,
            skillBoostDescription = "+20% Magic XP Boost"
        ),
        CauldronRecipe(
            id = "rec_watermelon_redwood",
            name = "Redwood Smoked Watermelon Slice",
            emoji = "🍉",
            reqLevel = 70,
            buffEffect = "+20% Woodcutting XP Boost (8 hrs)",
            healthRestored = 190,
            hungerRestored = 100,
            description = "Juicy watermelon slices smoked over towering redwood logs. Grants +20% Woodcutting XP Boost for 8 Hours!",
            requiredRawItemId = "item_watermelon",
            rawItemName = "Juicy Watermelon",
            requiredItem2Id = "item_redwood_logs",
            item2Name = "Redwood Logs",
            cookedItemName = "Redwood Smoked Watermelon Slice",
            cookingXp = 750L,
            boostedSkill = OsrsSkill.WOODCUTTING,
            xpBoostPercent = 20,
            skillBoostDescription = "+20% Woodcutting XP Boost"
        ),

        CauldronRecipe(
            id = "rec_potato_oak",
            name = "Oak Potato Stew",
            emoji = "🥔",
            reqLevel = 1,
            buffEffect = "+15% Woodcutting XP Boost (8 hrs)",
            healthRestored = 50,
            hungerRestored = 50,
            description = "Fresh farm potato stewed with oak logs. Grants +15% Woodcutting XP Boost for 8 Hours!",
            requiredRawItemId = "item_potato",
            rawItemName = "Fresh Potato",
            requiredItem2Id = "item_oak_logs",
            item2Name = "Oak Logs",
            cookedItemName = "Oak Potato Stew",
            cookingXp = 100L,
            boostedSkill = OsrsSkill.WOODCUTTING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Woodcutting XP Boost"
        ),
        CauldronRecipe(
            id = "rec_cabbage_guam",
            name = "Greenleaf Cabbage Salad",
            emoji = "🥬",
            reqLevel = 1,
            buffEffect = "+15% Herblore XP Boost (8 hrs)",
            healthRestored = 40,
            hungerRestored = 40,
            description = "Crisp cabbage salad tossed with clean Greenleaf herb. Grants +15% Herblore XP Boost for 8 Hours!",
            requiredRawItemId = "item_cabbage",
            rawItemName = "Fresh Cabbage",
            requiredItem2Id = "item_clean_greenleaf",
            item2Name = "Greenleaf Herb",
            cookedItemName = "Greenleaf Cabbage Salad",
            cookingXp = 120L,
            boostedSkill = OsrsSkill.HERBLORE,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Herblore XP Boost"
        ),
        CauldronRecipe(
            id = "rec_meat_teak",
            name = "Teak-Smoked Roast Meat",
            emoji = "🥩",
            reqLevel = 5,
            buffEffect = "+15% Attack XP Boost (8 hrs)",
            healthRestored = 75,
            hungerRestored = 70,
            description = "Choice raw meat smoked over teak logs. Grants +15% Attack XP Boost for 8 Hours!",
            requiredRawItemId = "item_raw_bird_meat",
            rawItemName = "Raw Meat",
            requiredItem2Id = "item_teak_plank",
            item2Name = "Teak Logs",
            cookedItemName = "Teak-Smoked Roast Meat",
            cookingXp = 150L,
            boostedSkill = OsrsSkill.ATTACK,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Attack XP Boost"
        ),
        CauldronRecipe(
            id = "rec_shrimp_pot",
            name = "Spirit Shrimp Stew",
            emoji = "🍲",
            reqLevel = 5,
            buffEffect = "+15% Construction XP Boost (8 hrs)",
            healthRestored = 45,
            hungerRestored = 45,
            description = "Coastal spirit shrimps stewed with fresh potato. Grants +15% Construction XP Boost for 8 Hours!",
            requiredRawItemId = "item_raw_shrimps",
            rawItemName = "Raw Shrimps",
            requiredItem2Id = "item_potato",
            item2Name = "Fresh Potato",
            cookedItemName = "Spirit Shrimp Stew",
            cookingXp = 160L,
            boostedSkill = OsrsSkill.CONSTRUCTION,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Construction XP Boost"
        ),
        CauldronRecipe(
            id = "rec_trout_herb",
            name = "Trout Herb Broth",
            emoji = "🍵",
            reqLevel = 10,
            buffEffect = "+15% Fishing XP Boost (8 hrs)",
            healthRestored = 60,
            hungerRestored = 50,
            description = "Stream trout simmered with fresh onion. Grants +15% Fishing XP Boost for 8 Hours!",
            requiredRawItemId = "item_raw_trout",
            rawItemName = "Raw Trout",
            requiredItem2Id = "item_onion",
            item2Name = "Fresh Onion",
            cookedItemName = "Trout Herb Broth",
            cookingXp = 180L,
            boostedSkill = OsrsSkill.FISHING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Fishing XP Boost"
        ),
        CauldronRecipe(
            id = "rec_salmon_cabbage",
            name = "Salmon Shaman Tonic",
            emoji = "🧪",
            reqLevel = 15,
            buffEffect = "+15% Forging XP Boost (8 hrs)",
            healthRestored = 70,
            hungerRestored = 60,
            description = "River salmon stewed with fresh cabbage. Grants +15% Forging XP Boost for 8 Hours!",
            requiredRawItemId = "item_raw_salmon",
            rawItemName = "Raw Salmon",
            requiredItem2Id = "item_cabbage",
            item2Name = "Fresh Cabbage",
            cookedItemName = "Salmon Shaman Tonic",
            cookingXp = 200L,
            boostedSkill = OsrsSkill.SMITHING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Forging XP Boost"
        ),
        CauldronRecipe(
            id = "rec_lobster_tomato",
            name = "Flame Lobster Chowder",
            emoji = "🫕",
            reqLevel = 20,
            buffEffect = "+15% Cooking XP Boost (8 hrs)",
            healthRestored = 85,
            hungerRestored = 75,
            description = "Lobster chowder simmered with juicy tomato. Grants +15% Cooking XP Boost for 8 Hours!",
            requiredRawItemId = "item_raw_lobster",
            rawItemName = "Raw Lobster",
            requiredItem2Id = "item_tomato",
            item2Name = "Juicy Tomato",
            cookedItemName = "Flame Lobster Chowder",
            cookingXp = 250L,
            boostedSkill = OsrsSkill.COOKING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Cooking XP Boost"
        ),
        CauldronRecipe(
            id = "rec_swordfish_corn",
            name = "Totem Swordfish Brew",
            emoji = "🥣",
            reqLevel = 25,
            buffEffect = "+15% Crafting XP Boost (8 hrs)",
            healthRestored = 100,
            hungerRestored = 85,
            description = "Golden swordfish stewed with sweetcorn. Grants +15% Fletching XP Boost for 8 Hours!",
            requiredRawItemId = "item_raw_swordfish",
            rawItemName = "Raw Swordfish",
            requiredItem2Id = "item_sweetcorn",
            item2Name = "Golden Sweetcorn",
            cookedItemName = "Totem Swordfish Brew",
            cookingXp = 300L,
            boostedSkill = OsrsSkill.FLETCHING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Fletching XP Boost"
        ),
        CauldronRecipe(
            id = "rec_shark_melon",
            name = "Astral Shark Stew",
            emoji = "🦈",
            reqLevel = 30,
            buffEffect = "+15% Hitpoints XP Boost (8 hrs)",
            healthRestored = 130,
            hungerRestored = 90,
            description = "Deep-sea shark cooked with juicy watermelon. Grants +15% Hitpoints XP Boost for 8 Hours!",
            requiredRawItemId = "item_raw_shark",
            rawItemName = "Raw Shark",
            requiredItem2Id = "item_watermelon",
            item2Name = "Juicy Watermelon",
            cookedItemName = "Astral Shark Stew",
            cookingXp = 350L,
            boostedSkill = OsrsSkill.HITPOINTS,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Hitpoints XP Boost"
        ),
        CauldronRecipe(
            id = "rec_banana_meadow_mint",
            name = "Meadow Mint Banana Mash",
            emoji = "🍌",
            reqLevel = 35,
            buffEffect = "+15% Agility XP Boost (8 hrs)",
            healthRestored = 80,
            hungerRestored = 65,
            description = "Sweet banana mashed with Meadow Mint herb. Grants +15% Agility XP Boost for 8 Hours!",
            requiredRawItemId = "item_banana",
            rawItemName = "Sweet Banana",
            requiredItem2Id = "item_clean_meadow_mint",
            item2Name = "Meadow Mint Herb",
            cookedItemName = "Meadow Mint Banana Mash",
            cookingXp = 380L,
            boostedSkill = OsrsSkill.AGILITY,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Agility XP Boost"
        ),
        CauldronRecipe(
            id = "rec_apple_wild_thyme",
            name = "Wild Thyme Apple Crisp",
            emoji = "🍎",
            reqLevel = 40,
            buffEffect = "+15% Farming XP Boost (8 hrs)",
            healthRestored = 90,
            hungerRestored = 70,
            description = "Crisp apple baked with Wild Thyme herb. Grants +15% Farming XP Boost for 8 Hours!",
            requiredRawItemId = "item_apple",
            rawItemName = "Crisp Apple",
            requiredItem2Id = "item_clean_wild_thyme",
            item2Name = "Wild Thyme Herb",
            cookedItemName = "Wild Thyme Apple Crisp",
            cookingXp = 400L,
            boostedSkill = OsrsSkill.FARMING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Farming XP Boost"
        ),
        CauldronRecipe(
            id = "rec_bread_lavender",
            name = "Lavender Bread Porridge",
            emoji = "🍞",
            reqLevel = 45,
            buffEffect = "+15% Thieving XP Boost (8 hrs)",
            healthRestored = 95,
            hungerRestored = 75,
            description = "Fresh bread porridge simmered with Lavender herb. Grants +15% Thieving XP Boost for 8 Hours!",
            requiredRawItemId = "item_bread",
            rawItemName = "Fresh Bread",
            requiredItem2Id = "item_clean_lavender",
            item2Name = "Lavender Herb",
            cookedItemName = "Lavender Bread Porridge",
            cookingXp = 420L,
            boostedSkill = OsrsSkill.THIEVING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Thieving XP Boost"
        ),
        CauldronRecipe(
            id = "rec_sweetcorn_sunleaf",
            name = "Sunleaf Sweetcorn Chowder",
            emoji = "🌽",
            reqLevel = 50,
            buffEffect = "+15% Runecrafting XP Boost (8 hrs)",
            healthRestored = 110,
            hungerRestored = 80,
            description = "Golden sweetcorn chowder infused with Sunleaf herb. Grants +15% Runecrafting XP Boost for 8 Hours!",
            requiredRawItemId = "item_sweetcorn",
            rawItemName = "Golden Sweetcorn",
            requiredItem2Id = "item_clean_sunleaf",
            item2Name = "Sunleaf Herb",
            cookedItemName = "Sunleaf Sweetcorn Chowder",
            cookingXp = 450L,
            boostedSkill = OsrsSkill.RUNECRAFT,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Runecrafting XP Boost"
        ),
        CauldronRecipe(
            id = "rec_carrot_ironleaf",
            name = "Ironleaf Carrot Puree",
            emoji = "🥕",
            reqLevel = 55,
            buffEffect = "+15% Hunter XP Boost (8 hrs)",
            healthRestored = 105,
            hungerRestored = 80,
            description = "Crunchy carrot puree blended with Ironleaf herb. Grants +15% Hunter XP Boost for 8 Hours!",
            requiredRawItemId = "item_carrot",
            rawItemName = "Crunchy Carrot",
            requiredItem2Id = "item_clean_ironleaf",
            item2Name = "Ironleaf Herb",
            cookedItemName = "Ironleaf Carrot Puree",
            cookingXp = 480L,
            boostedSkill = OsrsSkill.HUNTER,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Hunter XP Boost"
        ),
        CauldronRecipe(
            id = "rec_strawberry_wintergreen",
            name = "Wintergreen Berry Compote",
            emoji = "🍓",
            reqLevel = 60,
            buffEffect = "+15% Fletching XP Boost (8 hrs)",
            healthRestored = 120,
            hungerRestored = 85,
            description = "Ripe strawberry compote stewed with Wintergreen herb. Grants +15% Fletching XP Boost for 8 Hours!",
            requiredRawItemId = "item_strawberry",
            rawItemName = "Ripe Strawberry",
            requiredItem2Id = "item_clean_wintergreen",
            item2Name = "Wintergreen Herb",
            cookedItemName = "Wintergreen Berry Compote",
            cookingXp = 500L,
            boostedSkill = OsrsSkill.FLETCHING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Fletching XP Boost"
        ),
        CauldronRecipe(
            id = "rec_pumpkin_silverleaf",
            name = "Silverleaf Pumpkin Soup",
            emoji = "🎃",
            reqLevel = 65,
            buffEffect = "+15% Smithing XP Boost (8 hrs)",
            healthRestored = 140,
            hungerRestored = 90,
            description = "Giant pumpkin soup simmered with Silverleaf herb. Grants +15% Smithing XP Boost for 8 Hours!",
            requiredRawItemId = "item_pumpkin",
            rawItemName = "Giant Pumpkin",
            requiredItem2Id = "item_clean_silverleaf",
            item2Name = "Silverleaf Herb",
            cookedItemName = "Silverleaf Pumpkin Soup",
            cookingXp = 550L,
            boostedSkill = OsrsSkill.SMITHING,
            xpBoostPercent = 15,
            skillBoostDescription = "+15% Smithing XP Boost"
        ),
        CauldronRecipe(
            id = "rec_meat_vervain",
            name = "Vervain Pepper Steak",
            emoji = "🥩",
            reqLevel = 70,
            buffEffect = "+20% Slayer XP Boost (8 hrs)",
            healthRestored = 160,
            hungerRestored = 95,
            description = "Raw meat spiced with Vervain herb. Grants +20% Slayer XP Boost for 8 Hours!",
            requiredRawItemId = "item_raw_bird_meat",
            rawItemName = "Raw Meat",
            requiredItem2Id = "item_clean_vervain",
            item2Name = "Vervain Herb",
            cookedItemName = "Vervain Pepper Steak",
            cookingXp = 600L,
            boostedSkill = OsrsSkill.SLAYER,
            xpBoostPercent = 20,
            skillBoostDescription = "+20% Slayer XP Boost"
        ),
        CauldronRecipe(
            id = "rec_manta_mystic_sage",
            name = "Mystic Sage Manta Ray Feast",
            emoji = "🐙",
            reqLevel = 75,
            buffEffect = "+20% Firemaking XP Boost (8 hrs)",
            healthRestored = 180,
            hungerRestored = 100,
            description = "Manta ray sea feast simmered with Mystic Sage herb. Grants +20% Firemaking XP Boost for 8 Hours!",
            requiredRawItemId = "item_manta_ray",
            rawItemName = "Raw Manta Ray",
            requiredItem2Id = "item_clean_mystic_sage",
            item2Name = "Mystic Sage Herb",
            cookedItemName = "Mystic Sage Manta Ray Feast",
            cookingXp = 650L,
            boostedSkill = OsrsSkill.FIREMAKING,
            xpBoostPercent = 20,
            skillBoostDescription = "+20% Firemaking XP Boost"
        ),
        CauldronRecipe(
            id = "rec_lobster_sunleaf",
            name = "Sunleaf Lobster Gumbo",
            emoji = "🦞",
            reqLevel = 80,
            buffEffect = "+20% Adventuring XP Boost (8 hrs)",
            healthRestored = 200,
            hungerRestored = 100,
            description = "Fresh lobster gumbo simmered with Sunleaf herb. Grants +20% Adventuring XP Boost for 8 Hours!",
            requiredRawItemId = "item_raw_lobster",
            rawItemName = "Raw Lobster",
            requiredItem2Id = "item_clean_sunleaf",
            item2Name = "Sunleaf Herb",
            cookedItemName = "Sunleaf Lobster Gumbo",
            cookingXp = 700L,
            boostedSkill = OsrsSkill.ADVENTURING,
            xpBoostPercent = 20,
            skillBoostDescription = "+20% Adventuring XP Boost"
        ),
        CauldronRecipe(
            id = "rec_sovereign_nectar",
            name = "Sovereign Food Feast",
            emoji = "🍷",
            reqLevel = 85,
            buffEffect = "+25% Global XP Boost Across ALL Skills (8 hrs)",
            healthRestored = 220,
            hungerRestored = 100,
            description = "Grand royal feast cooked with raw shark and ripe strawberries. Grants +25% Global XP Boost across ALL skills for 8 Hours!",
            requiredRawItemId = "item_raw_shark",
            rawItemName = "Raw Shark",
            requiredItem2Id = "item_strawberry",
            item2Name = "Ripe Strawberry",
            cookedItemName = "Sovereign Food Feast",
            cookingXp = 1000L,
            boostedSkill = null,
            xpBoostPercent = 25,
            skillBoostDescription = "+25% Global All Skills XP Boost"
        )
    )
}

data class AdventuringFloor(
    val floorLevel: Int,
    val title: String,
    val description: String,
    val bgStartColor: Long,
    val bgEndColor: Long,
    val monsters: List<AdventuringMonster>,
    val boss: AdventuringMonster
)

data class SpiritFish(
    val id: String,
    val name: String,
    val emoji: String,
    val reqLevel: Int,
    val xp: Long,
    val description: String,
    val dropChancePercent: Int = 33,
    val bonusSecondItemId: String? = null,
    val bonusSecondItemName: String? = null,
    val bonusSecondItemEmoji: String? = null,
    val bonusSecondItemQty: Int = 1
)

data class GroveTree(
    val id: String,
    val name: String,
    val emoji: String,
    val reqLevel: Int,
    val xp: Long,
    val description: String,
    val dropChancePercent: Int = 33,
    val bonusSecondItemId: String? = null,
    val bonusSecondItemName: String? = null,
    val bonusSecondItemEmoji: String? = null,
    val bonusSecondItemQty: Int = 1
)

data class GemologyMineral(
    val id: String,
    val name: String,
    val emoji: String,
    val reqLevel: Int,
    val xp: Long,
    val dropChancePercent: Int,
    val description: String,
    val isGem: Boolean = false,
    val bonusSecondItemId: String? = null,
    val bonusSecondItemName: String? = null,
    val bonusSecondItemEmoji: String? = null,
    val bonusSecondItemQty: Int = 1
)

data class GemologyArea(
    val id: String,
    val name: String,
    val emoji: String,
    val reqLevel: Int,
    val description: String,
    val posXRatio: Float = 0.5f,
    val posYRatio: Float = 0.5f,
    val minerals: List<GemologyMineral>,
    val reqQuestId: String? = null,
    val reqQuestName: String? = null,
    val reqAdventuringFloor: Int? = null,
    val reqTotemId: String? = null,
    val reqTotemName: String? = null,
    val reqTotemEmoji: String? = null,
    val specialPerkDesc: String? = null
)

data class SpiritPoolArea(
    val id: String,
    val name: String,
    val emoji: String,
    val reqLevel: Int,
    val description: String,
    val posXRatio: Float,
    val posYRatio: Float,
    val catchableFish: List<SpiritFish>,
    val reqQuestId: String? = null,
    val reqQuestName: String? = null,
    val reqAdventuringFloor: Int? = null,
    val reqTotemId: String? = null,
    val reqTotemName: String? = null,
    val reqTotemEmoji: String? = null,
    val specialPerkDesc: String? = null
)

data class GroveForestArea(
    val id: String,
    val name: String,
    val emoji: String,
    val reqLevel: Int,
    val description: String,
    val posXRatio: Float,
    val posYRatio: Float,
    val choppableTrees: List<GroveTree>,
    val reqQuestId: String? = null,
    val reqQuestName: String? = null,
    val reqAdventuringFloor: Int? = null,
    val reqTotemId: String? = null,
    val reqTotemName: String? = null,
    val reqTotemEmoji: String? = null,
    val specialPerkDesc: String? = null
)

object AdventuringStoryData {

    val CHAPTER_TITLES = mapOf(
        1 to "Chapter I: Outer Bastion Gates",
        2 to "Chapter II: Dread Catacombs of Echoes",
        3 to "Chapter III: Infernal Magma Forge",
        4 to "Chapter IV: Frozen Abyss Sanctum",
        5 to "Chapter V: Serpent Hydra Sanctum",
        6 to "Chapter VI: Tempest Vanguard Citadel",
        7 to "Chapter VII: Clockwork Obsidian Armory",
        8 to "Chapter VIII: Blood Moon Cloister",
        9 to "Chapter IX: Astral Void Spire",
        10 to "Chapter X: Apex Throne of the Shaman Sovereign"
    )

    val CHAPTER_COLORS = mapOf(
        1 to Pair(0xFF1B3B22L, 0xFF2D5A37L),
        2 to Pair(0xFF2B3A4AL, 0xFF43586CL),
        3 to Pair(0xFF4A1C1CL, 0xFF6E2828L),
        4 to Pair(0xFF1B3A4BL, 0xFF2A5B75L),
        5 to Pair(0xFF1D3B2AL, 0xFF2B5B40L),
        6 to Pair(0xFF3A2D4BL, 0xFF5B4075L),
        7 to Pair(0xFF3B2E1DL, 0xFF5B482BL),
        8 to Pair(0xFF4B1D2AL, 0xFF752B40L),
        9 to Pair(0xFF2A1B3DL, 0xFF442D63L),
        10 to Pair(0xFF3D1B32L, 0xFF632D52L)
    )

    val FLOORS: List<AdventuringFloor> by lazy {
        (1..99).map { level -> buildFloor(level) }
    }

    fun getFloor(floorLevel: Int): AdventuringFloor {
        val lvl = floorLevel.coerceIn(1, 99)
        return buildFloor(lvl)
    }

    private fun buildFloor(level: Int): AdventuringFloor {
        val chapterNum = ((level - 1) / 10 + 1).coerceIn(1, 10)
        val chapterTitle = CHAPTER_TITLES[chapterNum] ?: "Fortress Realm"
        val colors = CHAPTER_COLORS[chapterNum] ?: Pair(0xFF1B3B22L, 0xFF2D5A37L)

        val scale = level
        val hpBase = 35 + (scale * 16)
        val atkBase = 4 + (scale * 2)
        val defBase = 1 + (scale / 3)
        val xpBase = scale * 75L
        val gpBase = scale * 20L
        val cLvl = (scale * 2) + 1

        val (m1Name, m1Emoji, m2Name, m2Emoji, bossName, bossEmoji, bossLore) = getThemeEntities(chapterNum, level)

        val bossHp = (hpBase * 2.5).toInt()
        val bossAtk = (atkBase * 1.5).toInt()
        val bossDef = defBase + 3

        val bossCards = getBossAttackCards(level, bossName, bossEmoji, bossAtk)

        val m1 = AdventuringMonster(
            id = "m1_f$level",
            name = m1Name,
            emoji = m1Emoji,
            floorLevel = level,
            hp = hpBase,
            maxHp = hpBase,
            attackPower = atkBase,
            defence = defBase,
            xpReward = xpBase,
            gpReward = gpBase,
            storyLore = "Lurks in $chapterTitle on Floor $level.",
            combatLevel = cLvl
        )

        val m2 = AdventuringMonster(
            id = "m2_f$level",
            name = m2Name,
            emoji = m2Emoji,
            floorLevel = level,
            hp = hpBase + 15,
            maxHp = hpBase + 15,
            attackPower = atkBase + 2,
            defence = defBase + 1,
            xpReward = (xpBase * 1.25).toLong(),
            gpReward = (gpBase * 1.3).toLong(),
            storyLore = "Elite guardian patrolling Floor $level.",
            combatLevel = cLvl + 2
        )

        val boss = AdventuringMonster(
            id = "b_f$level",
            name = bossName,
            emoji = bossEmoji,
            floorLevel = level,
            hp = bossHp,
            maxHp = bossHp,
            attackPower = bossAtk,
            defence = bossDef,
            xpReward = xpBase * 4,
            gpReward = gpBase * 4,
            storyLore = bossLore,
            combatLevel = cLvl + 6,
            attackCards = bossCards
        )

        val floorTitle = "Floor $level: $chapterTitle"
        val floorDesc = "Level $level / 99 in the Fortress. Conquer the guardian $bossName!"

        return AdventuringFloor(
            floorLevel = level,
            title = floorTitle,
            description = floorDesc,
            bgStartColor = colors.first,
            bgEndColor = colors.second,
            monsters = listOf(m1, m2),
            boss = boss
        )
    }

    private fun getBossAttackCards(level: Int, bossName: String, bossEmoji: String, bossAtk: Int): List<EnemyAttack> {
        val bDmg = bossAtk.coerceAtLeast(6)
        return when (level) {
            1 -> listOf(
                EnemyAttack("b1_1", "Nature's Slam", "🪵", bDmg, description = "Crushing branch strike."),
                EnemyAttack("b1_2", "Bark Shield Heal", "🛡️", (bDmg * 0.8).toInt(), specialEffect = "HEAL", description = "Hardens oak skin and restores vitality."),
                EnemyAttack("b1_3", "Root Vine Piercer", "🌿", (bDmg * 1.2).toInt(), shieldPierce = true, description = "Thorny root vines piercing player shield.")
            )
            10 -> listOf(
                EnemyAttack("b10_1", "Bastion Mortar Shell", "💣", (bDmg * 1.2).toInt(), description = "Launches explosive stone projectile."),
                EnemyAttack("b10_2", "Iron Fortress Wall", "🛡️", bDmg, specialEffect = "HEAL", description = "Raises impenetrable defenses and repairs structure."),
                EnemyAttack("b10_3", "Ground Breaker Slam", "💥", (bDmg * 1.5).toInt(), shieldPierce = true, description = "Shatters earth bypassing defense shields.")
            )
            20 -> listOf(
                EnemyAttack("b20_1", "Death Ray Bolt", "☠️", (bDmg * 1.3).toInt(), description = "Fires necrotic beam of dark soul magic."),
                EnemyAttack("b20_2", "Soul Drain Harvest", "🔮", bDmg, specialEffect = "LIFESTEAL", description = "Siphons soul essence from player."),
                EnemyAttack("b20_3", "Frost Nova Cataclysm", "❄️", (bDmg * 1.6).toInt(), shieldPierce = true, description = "Freezing blast bypassing player shield.")
            )
            30 -> listOf(
                EnemyAttack("b30_1", "Infernal Hellfire Wave", "🔥", (bDmg * 1.3).toInt(), description = "Wave of searing lava flames."),
                EnemyAttack("b30_2", "Magma Eruption Critical", "🌋", (bDmg * 1.5).toInt(), specialEffect = "CRITICAL", description = "Erupts magma for critical fire damage."),
                EnemyAttack("b30_3", "Solar Supernova Blast", "☀️", (bDmg * 1.7).toInt(), shieldPierce = true, description = "Solar flare piercing all defenses.")
            )
            50 -> listOf(
                EnemyAttack("b50_1", "Venom Acid Shower", "🧪", (bDmg * 1.4).toInt(), description = "Rains corrosive venom acid."),
                EnemyAttack("b50_2", "Seven-Head Hydra Frenzy", "🐍", (bDmg * 1.3).toInt(), specialEffect = "LIFESTEAL", description = "Rapid multi-strike draining life force."),
                EnemyAttack("b50_3", "Toxic Annihilation", "☣️", (bDmg * 1.8).toInt(), shieldPierce = true, description = "Lethal toxin bypassing player shield.")
            )
            99 -> listOf(
                EnemyAttack("b99_1", "Void Realm Collapse", "🌌", (bDmg * 1.5).toInt(), description = "Crushes space-time with pure void energy."),
                EnemyAttack("b99_2", "Singularity Life Drain", "💫", (bDmg * 1.4).toInt(), specialEffect = "LIFESTEAL", description = "Drains life through singularity distortion."),
                EnemyAttack("b99_3", "Genesis Realm Annihilation", "👑", (bDmg * 2.0).toInt(), shieldPierce = true, description = "Ultimate sovereign spell bypassing all shields.")
            )
            else -> listOf(
                EnemyAttack("b_${level}_1", "$bossName Strike", bossEmoji, bDmg, description = "Heavy boss attack dealing $bDmg damage."),
                EnemyAttack("b_${level}_2", "$bossName Power Burst", "💥", (bDmg * 1.3).toInt(), specialEffect = if (level % 2 == 0) "CRITICAL" else "LIFESTEAL", description = "Devastating boss power blast."),
                EnemyAttack("b_${level}_3", "$bossName Shield Breaker", "⚡", (bDmg * 1.1).toInt(), shieldPierce = true, description = "Pierces shield with dark aura.")
            )
        }
    }

    private fun getThemeEntities(chapter: Int, level: Int): ThemeTuple {
        return when (chapter) {
            1 -> ThemeTuple("Corrupted Slime", "🦠", "Shadow Bramble Spider", "🕷️", "Boss Treant $level", "🪵", "Corrupted oak tree guarding Chapter 1 floor $level.")
            2 -> ThemeTuple("Mist Phantom", "👻", "Dread Shadow Wolf", "🐺", "Boss Spectre $level", "💀", "Restless spirit guarding Chapter 2 catacombs floor $level.")
            3 -> ThemeTuple("Infernal Ember Sprite", "🔥", "Magma Wyrmling", "🐉", "Boss Fire Archon $level", "🌋", "Fire titan guarding Chapter 3 magma forge floor $level.")
            4 -> ThemeTuple("Frost Bite Wolf", "❄️", "Ice Crag Golem", "🧊", "Boss Glacier Empress $level", "❄️", "Frozen queen ruling Chapter 4 glacier abyss floor $level.")
            5 -> ThemeTuple("Venom Naga Sentry", "🐍", "Toxic Hydralisk", "🧪", "Boss Hydra Sovereign $level", "🐉", "Seven-headed beast guarding Chapter 5 serpent sanctum floor $level.")
            6 -> ThemeTuple("Storm Eagle", "🦅", "Plasma Elemental", "⚡", "Boss Raijin Lord $level", "🌩️", "Thunder overlord commanding Chapter 6 tempest citadel floor $level.")
            7 -> ThemeTuple("Clockwork Sentry", "🤖", "Iron Gargoyle", "👺", "Boss Dreadnought Colossus $level", "🛡️", "Iron titan ruling Chapter 7 clockwork armory floor $level.")
            8 -> ThemeTuple("Vampire Blood Stalker", "🧛", "Crimson Blood Bat", "🦇", "Boss Count Malakor $level", "🍷", "Ancient vampire lord guarding Chapter 8 blood moon cloister floor $level.")
            9 -> ThemeTuple("Astral Void Beast", "🌌", "Cosmic Shaman Acolyte", "🔮", "Boss Void Emperor $level", "💫", "Ethereal void master commanding Chapter 9 astral spire floor $level.")
            else -> ThemeTuple("Celestial Shaman Drake", "🌟", "Apex Void Guardian", "👑", "Grand Void Sovereign Moros $level", "👑", "The ultimate master shaman ruling Chapter 10 apex throne floor $level!")
        }
    }

    private data class ThemeTuple(
        val m1Name: String, val m1Emoji: String,
        val m2Name: String, val m2Emoji: String,
        val bossName: String, val bossEmoji: String, val bossLore: String
    )

    val SPIRIT_POOL_AREAS = listOf(
        SpiritPoolArea(
            id = "area_coastal",
            name = "Coastal Shallows",
            emoji = "🏖️",
            reqLevel = 1,
            description = "Calm shoreline waters perfect for novice anglers.",
            posXRatio = 0.2f,
            posYRatio = 0.7f,
            catchableFish = listOf(
                SpiritFish("item_raw_shrimps", "Raw Shrimps", "🦐", 1, 25L, "Small freshwater shrimps.", dropChancePercent = 55),
                SpiritFish("item_raw_anchovies", "Raw Anchovies", "🐟", 5, 35L, "Tiny silvery fish.", dropChancePercent = 30),
                SpiritFish("item_raw_sardine", "Raw Sardine", "🐟", 10, 45L, "Oily coastal fish.", dropChancePercent = 15)
            )
        ),
        SpiritPoolArea(
            id = "pool_totem_vampyre",
            name = "Vampyre Cavern Shallows",
            emoji = "🦇",
            reqLevel = 10,
            description = "Subterranean tidal grotto unlocked by the Woodland Obelisk from Pewter Gym. Rich in Shrimps and Trout.",
            posXRatio = 0.14f,
            posYRatio = 0.82f,
            catchableFish = listOf(
                SpiritFish("item_raw_shrimps", "Cavern Shrimps", "🦐", 10, 55L, "Grotto shrimps dropping Raw Shrimps + Raw Trout!", dropChancePercent = 60, bonusSecondItemId = "item_raw_trout", bonusSecondItemName = "Raw Trout", bonusSecondItemEmoji = "🐟", bonusSecondItemQty = 1),
                SpiritFish("item_raw_trout", "Shadow Trout", "🐟", 15, 80L, "Cave trout dropping Raw Trout + Raw Anchovies!", dropChancePercent = 40, bonusSecondItemId = "item_raw_anchovies", bonusSecondItemName = "Raw Anchovies", bonusSecondItemEmoji = "🐟", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_totem_woodland",
            reqTotemName = "Woodland Obelisk",
            reqTotemEmoji = "🪵",
            specialPerkDesc = "Dual Harvest: Cavern Shrimps yields Raw Shrimps + Raw Trout!"
        ),
        SpiritPoolArea(
            id = "pool_totem_parasite",
            name = "Abyssal Reef Trench",
            emoji = "🦠",
            reqLevel = 13,
            description = "Swirling void-infused reef unlocked by the Mist Fen Obelisk from Cerulean Gym. Abyssal currents surge with bonus fish.",
            posXRatio = 0.28f,
            posYRatio = 0.62f,
            catchableFish = listOf(
                SpiritFish("item_raw_trout", "Runic Trout", "🐟", 13, 75L, "Void trout dropping Raw Trout + Raw Salmon!", dropChancePercent = 60, bonusSecondItemId = "item_raw_salmon", bonusSecondItemName = "Raw Salmon", bonusSecondItemEmoji = "🐟", bonusSecondItemQty = 1),
                SpiritFish("item_raw_salmon", "Abyssal Salmon", "🐟", 20, 110L, "Abyssal salmon dropping Raw Salmon + Raw Pike!", dropChancePercent = 40, bonusSecondItemId = "item_raw_pike", bonusSecondItemName = "Raw Pike", bonusSecondItemEmoji = "🐟", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_totem_mist_fen",
            reqTotemName = "Mist Fen Obelisk",
            reqTotemEmoji = "🐺",
            specialPerkDesc = "Dual Harvest: Runic Trout yields Raw Trout + Raw Salmon!"
        ),
        SpiritPoolArea(
            id = "area_river",
            name = "River Whispers",
            emoji = "🌊",
            reqLevel = 15,
            description = "Flowing river streams with rapid currents.",
            posXRatio = 0.4f,
            posYRatio = 0.5f,
            catchableFish = listOf(
                SpiritFish("item_raw_trout", "Raw Trout", "🐟", 15, 50L, "Speckled freshwater trout.", dropChancePercent = 55),
                SpiritFish("item_raw_pike", "Raw Pike", "🐟", 20, 65L, "Predatory river fish.", dropChancePercent = 30),
                SpiritFish("item_raw_salmon", "Raw Salmon", "🐟", 25, 80L, "Vibrant pink salmon.", dropChancePercent = 15)
            )
        ),
        SpiritPoolArea(
            id = "pool_totem_mosquito",
            name = "Mosquito Swarm Cove",
            emoji = "🦟",
            reqLevel = 22,
            description = "Humming tropical marsh cove unlocked by the Fen Obelisk from Pastoria Gym. Teeming with Trout, Salmon, and Lobsters.",
            posXRatio = 0.46f,
            posYRatio = 0.68f,
            catchableFish = listOf(
                SpiritFish("item_raw_salmon", "Marsh Salmon", "🐟", 22, 120L, "Marsh salmon dropping Raw Salmon + Raw Lobster!", dropChancePercent = 55, bonusSecondItemId = "item_raw_lobster", bonusSecondItemName = "Raw Lobster", bonusSecondItemEmoji = "🦞", bonusSecondItemQty = 1),
                SpiritFish("item_raw_lobster", "Swarm Lobster", "🦞", 35, 180L, "Cove lobster dropping Raw Lobster + Raw Tuna!", dropChancePercent = 45, bonusSecondItemId = "item_raw_tuna", bonusSecondItemName = "Raw Tuna", bonusSecondItemEmoji = "🐟", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_fen",
            reqTotemName = "Fen Obelisk",
            reqTotemEmoji = "🌊",
            specialPerkDesc = "Dual Harvest: Marsh Salmon yields Raw Salmon + Raw Lobster!"
        ),
        SpiritPoolArea(
            id = "area_coral",
            name = "Coral Reef Lagoon",
            emoji = "🪸",
            reqLevel = 40,
            description = "Tropical coral reef teeming with crustaceans and sport fish.",
            posXRatio = 0.7f,
            posYRatio = 0.75f,
            catchableFish = listOf(
                SpiritFish("item_raw_lobster", "Raw Lobster", "🦞", 40, 150L, "Red shelled lobster.", dropChancePercent = 50),
                SpiritFish("item_raw_tuna", "Raw Tuna", "🐟", 45, 180L, "Sleek fast swimming tuna.", dropChancePercent = 30),
                SpiritFish("item_raw_swordfish", "Raw Swordfish", "🗡️", 50, 220L, "Majestic billfish with sharp sword bill.", dropChancePercent = 20)
            )
        ),
        SpiritPoolArea(
            id = "pool_totem_ibis",
            name = "Sacred Ibis Lagoon",
            emoji = "🦩",
            reqLevel = 40,
            description = "Sanctified wading lagoon unlocked by the Rain Obelisk from Sootopolis Gym. Rich in Lobster & Swordfish.",
            posXRatio = 0.62f,
            posYRatio = 0.85f,
            catchableFish = listOf(
                SpiritFish("item_raw_lobster", "Sacred Lagoon Lobster", "🦞", 40, 210L, "Lagoon lobster dropping Raw Lobster + Raw Swordfish!", dropChancePercent = 50, bonusSecondItemId = "item_raw_swordfish", bonusSecondItemName = "Raw Swordfish", bonusSecondItemEmoji = "🗡️", bonusSecondItemQty = 1),
                SpiritFish("item_raw_swordfish", "Ibis Crest Swordfish", "🗡️", 48, 280L, "Swift swordfish dropping Raw Swordfish + Raw Tuna!", dropChancePercent = 50, bonusSecondItemId = "item_raw_tuna", bonusSecondItemName = "Raw Tuna", bonusSecondItemEmoji = "🐟", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_rain",
            reqTotemName = "Rain Obelisk",
            reqTotemEmoji = "💧",
            specialPerkDesc = "Dual Harvest: Lagoon Lobster yields Raw Lobster + Raw Swordfish!"
        ),
        SpiritPoolArea(
            id = "pool_totem_jelly",
            name = "Bioluminescent Grotto",
            emoji = "🪼",
            reqLevel = 43,
            description = "Glowing undersea cavern unlocked by the Sacred Grove Obelisk from Celadon Gym. Translucent jellies illuminate massive Swordfish.",
            posXRatio = 0.76f,
            posYRatio = 0.60f,
            catchableFish = listOf(
                SpiritFish("item_raw_swordfish", "Luminous Swordfish", "🗡️", 43, 290L, "Glowing swordfish dropping Raw Swordfish + Raw Shark!", dropChancePercent = 50, bonusSecondItemId = "item_raw_shark", bonusSecondItemName = "Raw Shark", bonusSecondItemEmoji = "🦈", bonusSecondItemQty = 1),
                SpiritFish("item_raw_shark", "Grotto Shark", "🦈", 55, 420L, "Deep shark dropping Raw Shark + Raw Sea Turtle!", dropChancePercent = 50, bonusSecondItemId = "item_raw_sea_turtle", bonusSecondItemName = "Raw Sea Turtle", bonusSecondItemEmoji = "🐢", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_totem_sacred_grove",
            reqTotemName = "Sacred Grove Obelisk",
            reqTotemEmoji = "🌳",
            specialPerkDesc = "Dual Harvest: Luminous Swordfish yields Raw Swordfish + Raw Shark!"
        ),
        SpiritPoolArea(
            id = "pool_totem_hydra",
            name = "Nine-Headed Hydra Abyss",
            emoji = "🐉",
            reqLevel = 56,
            description = "Swirling oceanic vortex unlocked by the Rising Obelisk from Blackthorn Gym. Hunts huge Sharks and Manta Rays.",
            posXRatio = 0.88f,
            posYRatio = 0.50f,
            catchableFish = listOf(
                SpiritFish("item_raw_shark", "Hydra Apex Shark", "🦈", 56, 440L, "Apex shark dropping Raw Shark + Raw Manta Ray!", dropChancePercent = 50, bonusSecondItemId = "item_raw_manta_ray", bonusSecondItemName = "Raw Manta Ray", bonusSecondItemEmoji = "🐋", bonusSecondItemQty = 1),
                SpiritFish("item_raw_manta_ray", "Nine-Fin Manta Ray", "🐋", 68, 560L, "Colossal ray dropping Raw Manta Ray + Spirit Koi!", dropChancePercent = 50, bonusSecondItemId = "item_spirit_koi", bonusSecondItemName = "Spirit Koi", bonusSecondItemEmoji = "🎏", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_rising",
            reqTotemName = "Rising Obelisk",
            reqTotemEmoji = "🐲",
            specialPerkDesc = "Dual Harvest: Hydra Apex Shark yields Raw Shark + Raw Manta Ray!"
        ),
        SpiritPoolArea(
            id = "summon_bunyip_area",
            name = "Bunyip Dreamtime Billabong",
            emoji = "🐊",
            reqLevel = 68,
            description = "Enchanted mystical billabong unlocked by the Relic Obelisk from Hearthome Gym. Teeming with Manta Rays and Spirit Koi.",
            posXRatio = 0.40f,
            posYRatio = 0.35f,
            catchableFish = listOf(
                SpiritFish("item_raw_manta_ray", "Dreamtime Manta Ray", "🐋", 68, 580L, "Blessed ray dropping Raw Manta Ray + Spirit Koi!", dropChancePercent = 50, bonusSecondItemId = "item_spirit_koi", bonusSecondItemName = "Spirit Koi", bonusSecondItemEmoji = "🎏", bonusSecondItemQty = 1),
                SpiritFish("item_spirit_koi", "Bunyip Golden Koi", "🎏", 75, 720L, "Golden koi dropping Spirit Koi + Astral Anglerfish!", dropChancePercent = 50, bonusSecondItemId = "item_astral_angler", bonusSecondItemName = "Astral Anglerfish", bonusSecondItemEmoji = "🐡", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_relic",
            reqTotemName = "Relic Obelisk",
            reqTotemEmoji = "👻",
            specialPerkDesc = "Dual Harvest: Dreamtime Manta Ray yields Manta Ray + Spirit Koi!"
        ),
        SpiritPoolArea(
            id = "area_abyss",
            name = "Abyssal Trench",
            emoji = "🦈",
            reqLevel = 76,
            description = "Deep dark ocean trench home to colossal ocean predators.",
            posXRatio = 0.85f,
            posYRatio = 0.35f,
            catchableFish = listOf(
                SpiritFish("item_raw_shark", "Raw Shark", "🦈", 76, 320L, "Apex ocean predator.", dropChancePercent = 50),
                SpiritFish("item_raw_sea_turtle", "Raw Sea Turtle", "🐢", 79, 380L, "Ancient hard-shelled sea turtle.", dropChancePercent = 30),
                SpiritFish("item_raw_manta_ray", "Raw Manta Ray", "🐋", 81, 450L, "Giant ocean ray swimming gracefully.", dropChancePercent = 20)
            )
        ),
        SpiritPoolArea(
            id = "pool_totem_locust",
            name = "Plague Swarm Estuary",
            emoji = "🦗",
            reqLevel = 76,
            description = "Rushing tidal estuary unlocked by the Beacon Obelisk from Sunyshore Gym. Massive catches of Spirit Koi & Astral Anglerfish.",
            posXRatio = 0.60f,
            posYRatio = 0.32f,
            catchableFish = listOf(
                SpiritFish("item_spirit_koi", "Swarm Spirit Koi", "🎏", 76, 750L, "Swarm koi dropping Spirit Koi + Astral Anglerfish!", dropChancePercent = 50, bonusSecondItemId = "item_astral_angler", bonusSecondItemName = "Astral Anglerfish", bonusSecondItemEmoji = "🐡", bonusSecondItemQty = 1),
                SpiritFish("item_astral_angler", "Estuary Anglerfish", "🐡", 82, 920L, "Luminescent angler dropping Astral Anglerfish + Ethereal Ray!", dropChancePercent = 50, bonusSecondItemId = "item_ethereal_ray", bonusSecondItemName = "Ethereal Ray", bonusSecondItemEmoji = "🌌", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_beacon",
            reqTotemName = "Beacon Obelisk",
            reqTotemEmoji = "⚡",
            specialPerkDesc = "Dual Harvest: Swarm Spirit Koi yields Spirit Koi + Astral Anglerfish!"
        ),
        SpiritPoolArea(
            id = "area_mystic",
            name = "Mystic Spirit Oasis",
            emoji = "✨",
            reqLevel = 85,
            description = "Enchanted glowing oasis infused with shamanic spirit essence.",
            posXRatio = 0.5f,
            posYRatio = 0.25f,
            catchableFish = listOf(
                SpiritFish("item_spirit_koi", "Spirit Koi", "🎏", 85, 550L, "Golden glowing spirit fish.", dropChancePercent = 50),
                SpiritFish("item_astral_angler", "Astral Anglerfish", "🐡", 90, 680L, "Deep sea fish with bioluminescent lure.", dropChancePercent = 30),
                SpiritFish("item_ethereal_ray", "Ethereal Ray", "🌌", 92, 800L, "Transparent ray woven from starlight.", dropChancePercent = 20)
            )
        ),
        SpiritPoolArea(
            id = "pool_totem_unicorn",
            name = "Pure Spring of the Stallion",
            emoji = "🦄",
            reqLevel = 88,
            description = "Pristine crystal-clear spring unlocked by the Astral Bloom Obelisk from Cinnabar Gym. Brimming with Ethereal Rays and Magma Eels.",
            posXRatio = 0.74f,
            posYRatio = 0.20f,
            catchableFish = listOf(
                SpiritFish("item_ethereal_ray", "Pure Spring Ray", "🌌", 88, 1100L, "Celestial ray dropping Ethereal Ray + Magma Eel!", dropChancePercent = 50, bonusSecondItemId = "item_magma_eel", bonusSecondItemName = "Magma Eel", bonusSecondItemEmoji = "🐍", bonusSecondItemQty = 1),
                SpiritFish("item_magma_eel", "Purified Magma Eel", "🐍", 92, 1350L, "Fiery eel dropping Magma Eel + Sacred Shaman Fish!", dropChancePercent = 50, bonusSecondItemId = "item_sacred_shaman_fish", bonusSecondItemName = "Sacred Shaman Fish", bonusSecondItemEmoji = "🌟", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_totem_astral_bloom",
            reqTotemName = "Astral Bloom Obelisk",
            reqTotemEmoji = "🌺",
            specialPerkDesc = "Dual Harvest: Pure Spring Ray yields Ethereal Ray + Magma Eel!"
        ),
        SpiritPoolArea(
            id = "area_volcanic",
            name = "Volcanic Lava Lake",
            emoji = "🌋",
            reqLevel = 90,
            description = "Subterranean magma pool where fiery elemental fish thrive.",
            posXRatio = 0.15f,
            posYRatio = 0.3f,
            catchableFish = listOf(
                SpiritFish("item_magma_eel", "Magma Eel", "🐍", 90, 950L, "Fiery eel swimming in liquid rock.", dropChancePercent = 50),
                SpiritFish("item_ember_trout", "Ember Trout", "🔥", 93, 1150L, "Glowing ember-coated fish.", dropChancePercent = 30),
                SpiritFish("item_obsidian_crab", "Obsidian Crab", "🦀", 95, 1350L, "Armored lava crab with obsidian shell.", dropChancePercent = 20)
            )
        ),
        SpiritPoolArea(
            id = "pool_totem_dragon",
            name = "Ancient Dragon Leviathan Chasm",
            emoji = "🐲",
            reqLevel = 97,
            description = "Deepest abyssal oceanic trench unlocked by the Sovereign Wild Obelisk from Viridian Gym. Guarded by ancient dragon sea leviathans.",
            posXRatio = 0.86f,
            posYRatio = 0.10f,
            catchableFish = listOf(
                SpiritFish("item_sacred_shaman_fish", "Dragon-Blessed Shaman Fish", "🌟", 97, 2500L, "Divine fish dropping Sacred Shaman Fish + Cosmic Whale!", dropChancePercent = 50, bonusSecondItemId = "item_cosmic_whale", bonusSecondItemName = "Cosmic Whale", bonusSecondItemEmoji = "🐳", bonusSecondItemQty = 1),
                SpiritFish("item_golden_dragonfish", "Apex Golden Dragonfish", "🐉", 99, 3800L, "Supreme dragonfish dropping Golden Dragonfish + Sacred Shaman Fish!", dropChancePercent = 50, bonusSecondItemId = "item_sacred_shaman_fish", bonusSecondItemName = "Sacred Shaman Fish", bonusSecondItemEmoji = "🌟", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_totem_sovereign_wild",
            reqTotemName = "Sovereign Wild Obelisk",
            reqTotemEmoji = "🦌",
            specialPerkDesc = "Dual Harvest: Dragon Shaman Fish yields Sacred Fish + Cosmic Whale!"
        ),
        SpiritPoolArea(
            id = "area_starlight",
            name = "Void Starlight Pool",
            emoji = "🌌",
            reqLevel = 99,
            description = "Sacred cosmic pool where celestial shaman leviathans reside.",
            posXRatio = 0.65f,
            posYRatio = 0.12f,
            catchableFish = listOf(
                SpiritFish("item_sacred_shaman_fish", "Sacred Shaman Fish", "🌟", 99, 1800L, "Divine fish blessing shaman masters with massive XP.", dropChancePercent = 50),
                SpiritFish("item_cosmic_whale", "Cosmic Whale", "🐳", 99, 2200L, "Legendary cosmic leviathan.", dropChancePercent = 30),
                SpiritFish("item_golden_dragonfish", "Golden Dragonfish", "🐉", 99, 3000L, "Mythical gold scaled dragonfish of supreme spirit power.", dropChancePercent = 20)
            )
        )
    )

    val GROVE_FOREST_AREAS = listOf(
        GroveForestArea(
            id = "forest_sylvan",
            name = "Sylvan Canopy",
            emoji = "🌳",
            reqLevel = 1,
            description = "Peaceful ancient woodland filled with normal trees, lush oaks, and silvery birches.",
            posXRatio = 0.2f,
            posYRatio = 0.72f,
            choppableTrees = listOf(
                GroveTree("item_logs", "Tree", "🌲", 1, 25L, "Standard woodland tree that produces regular timber logs.", dropChancePercent = 45),
                GroveTree("item_oak_logs", "Oak Tree", "🌳", 15, 38L, "Sturdy woodland oak tree with dense foliage.", dropChancePercent = 35),
                GroveTree("item_birch_timber", "Birch Grove", "🪵", 25, 65L, "Silvery birch tree producing flexible timber logs.", dropChancePercent = 20)
            )
        ),
        GroveForestArea(
            id = "forest_totem_dreadfowl",
            name = "Dreadfowl Briar Perch",
            emoji = "🐓",
            reqLevel = 1,
            description = "Dense briar woodland unlocked by the Woodland Obelisk from Pewter Gym. Yields rapid bundles of Oak, Birch, and Willow.",
            posXRatio = 0.10f,
            posYRatio = 0.84f,
            choppableTrees = listOf(
                GroveTree("item_oak_logs", "Briar Oak Tree", "🌳", 1, 48L, "Lush briar oak dropping Oak & Birch timber together!", dropChancePercent = 40, bonusSecondItemId = "item_birch_timber", bonusSecondItemName = "Birch Timber", bonusSecondItemEmoji = "🪵", bonusSecondItemQty = 1),
                GroveTree("item_birch_timber", "Roosting Birch", "🪵", 10, 75L, "Roost birch dropping Birch & Willow logs!", dropChancePercent = 35, bonusSecondItemId = "item_oak_logs", bonusSecondItemName = "Oak Logs", bonusSecondItemEmoji = "🌳", bonusSecondItemQty = 1),
                GroveTree("item_willow_logs", "Willow Riverbank", "🌿", 30, 95L, "Flexible weeping willow growing near clear streams.", dropChancePercent = 25, bonusSecondItemId = "item_birch_timber", bonusSecondItemName = "Birch Timber", bonusSecondItemEmoji = "🪵", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_totem_woodland",
            reqTotemName = "Woodland Obelisk",
            reqTotemEmoji = "🪵",
            specialPerkDesc = "Dual Harvest: Briar woodland trees drop dual timber logs!"
        ),
        GroveForestArea(
            id = "forest_totem_spirit_wolf",
            name = "Wolfspirit Timber Den",
            emoji = "🐺",
            reqLevel = 4,
            description = "Shaded lupine den unlocked by the Zephyr Obelisk from Violet Gym. Bountiful Birch & Willow roots.",
            posXRatio = 0.26f,
            posYRatio = 0.84f,
            choppableTrees = listOf(
                GroveTree("item_birch_timber", "Wolf Den Birch", "🪵", 4, 80L, "Birch trunk dropping Birch Timber + Willow Logs!", dropChancePercent = 60, bonusSecondItemId = "item_willow_logs", bonusSecondItemName = "Willow Logs", bonusSecondItemEmoji = "🌿", bonusSecondItemQty = 1),
                GroveTree("item_willow_logs", "Prowler Willow", "🌿", 15, 110L, "Willow branch dropping Willow Logs + Oak Logs!", dropChancePercent = 40, bonusSecondItemId = "item_birch_timber", bonusSecondItemName = "Birch Timber", bonusSecondItemEmoji = "🪵", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_zephyr",
            reqTotemName = "Zephyr Obelisk",
            reqTotemEmoji = "🪶",
            specialPerkDesc = "Dual Harvest: Wolf Den Birch yields Birch Timber + Willow Logs!"
        ),
        GroveForestArea(
            id = "forest_totem_spirit_spider",
            name = "Arachnid Silk Canopy",
            emoji = "🕷️",
            reqLevel = 16,
            description = "Misty web-wrapped forest canopy unlocked by the Hive Obelisk from Azalea Gym. Rich in Alpine Pine and Willow timber.",
            posXRatio = 0.38f,
            posYRatio = 0.68f,
            choppableTrees = listOf(
                GroveTree("item_willow_logs", "Silk-Spun Willow", "🌿", 16, 115L, "Silken willow yielding Willow + Pine logs!", dropChancePercent = 55, bonusSecondItemId = "item_pine_logs", bonusSecondItemName = "Alpine Pine", bonusSecondItemEmoji = "🌲", bonusSecondItemQty = 1),
                GroveTree("item_pine_logs", "Spiderweb Alpine Pine", "🌲", 25, 135L, "Alpine pine yielding Pine + Cedar timber!", dropChancePercent = 45, bonusSecondItemId = "item_cedar_timber", bonusSecondItemName = "Mountain Cedar", bonusSecondItemEmoji = "🪵", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_hive",
            reqTotemName = "Hive Obelisk",
            reqTotemEmoji = "🪲",
            specialPerkDesc = "Dual Harvest: Silk-Spun Willow yields Willow + Alpine Pine!"
        ),
        GroveForestArea(
            id = "forest_totem_spirit_owl",
            name = "Wise Owl Moonwood",
            emoji = "🦉",
            reqLevel = 20,
            description = "Nocturnal sacred hollow unlocked by the Forest Obelisk from Eterna Gym. Sap-rich Pine & Cedar trees.",
            posXRatio = 0.52f,
            posYRatio = 0.74f,
            choppableTrees = listOf(
                GroveTree("item_pine_logs", "Moonlit Pine", "🌲", 20, 140L, "Pine tree yielding Alpine Pine + Mountain Cedar!", dropChancePercent = 50, bonusSecondItemId = "item_cedar_timber", bonusSecondItemName = "Mountain Cedar", bonusSecondItemEmoji = "🪵", bonusSecondItemQty = 1),
                GroveTree("item_cedar_timber", "Nocturnal Cedar", "🪵", 28, 175L, "Fragrant cedar yielding Cedar + Maple logs!", dropChancePercent = 50, bonusSecondItemId = "item_maple_logs", bonusSecondItemName = "Autumn Maple", bonusSecondItemEmoji = "🍁", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_forest",
            reqTotemName = "Forest Obelisk",
            reqTotemEmoji = "🌿",
            specialPerkDesc = "Dual Harvest: Moonlit Pine yields Alpine Pine + Mountain Cedar!"
        ),
        GroveForestArea(
            id = "forest_totem_compy",
            name = "Compy Raptor Woods",
            emoji = "🦖",
            reqLevel = 28,
            description = "Ancient primitive woods unlocked by the Stone Obelisk from Rustboro Gym. Fragrant Cedar & Maple trees.",
            posXRatio = 0.35f,
            posYRatio = 0.48f,
            choppableTrees = listOf(
                GroveTree("item_cedar_timber", "Raptor Cedar", "🪵", 28, 185L, "Cedar tree yielding Mountain Cedar + Autumn Maple!", dropChancePercent = 50, bonusSecondItemId = "item_maple_logs", bonusSecondItemName = "Autumn Maple", bonusSecondItemEmoji = "🍁", bonusSecondItemQty = 1),
                GroveTree("item_maple_logs", "Primeval Maple", "🍁", 35, 220L, "Maple tree yielding Autumn Maple + Pine logs!", dropChancePercent = 50, bonusSecondItemId = "item_pine_logs", bonusSecondItemName = "Alpine Pine", bonusSecondItemEmoji = "🌲", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_stone",
            reqTotemName = "Stone Obelisk",
            reqTotemEmoji = "🪨",
            specialPerkDesc = "Dual Harvest: Raptor Cedar yields Mountain Cedar + Autumn Maple!"
        ),
        GroveForestArea(
            id = "forest_pine_ridge",
            name = "Whispering Pine Ridge",
            emoji = "🌲",
            reqLevel = 35,
            description = "High elevation alpine pine & fragrant cedar mountain forest.",
            posXRatio = 0.45f,
            posYRatio = 0.55f,
            choppableTrees = listOf(
                GroveTree("item_pine_logs", "Alpine Pine", "🌲", 25, 80L, "Aromatic alpine pine tree growing in crisp mountain air.", dropChancePercent = 55),
                GroveTree("item_cedar_timber", "Mountain Cedar", "🪵", 40, 125L, "Dense fragrant cedar tree prized by master whittlers.", dropChancePercent = 30),
                GroveTree("item_maple_logs", "Autumn Maple", "🍁", 45, 160L, "Vibrant golden maple tree with sap-rich logs.", dropChancePercent = 15)
            )
        ),
        GroveForestArea(
            id = "forest_totem_spirit_terrorbird",
            name = "Terrorbird Savanna Copse",
            emoji = "🦤",
            reqLevel = 37,
            description = "Fast-growing savanna copse unlocked by the Plain Obelisk from Goldenrod Gym. Rich Maple & Yew.",
            posXRatio = 0.58f,
            posYRatio = 0.48f,
            choppableTrees = listOf(
                GroveTree("item_maple_logs", "Savanna Maple", "🍁", 37, 240L, "Savanna maple yielding Autumn Maple + Ancient Yew!", dropChancePercent = 55, bonusSecondItemId = "item_yew_logs", bonusSecondItemName = "Ancient Yew", bonusSecondItemEmoji = "🌳", bonusSecondItemQty = 1),
                GroveTree("item_yew_logs", "Avian Yew Trunk", "🌳", 45, 310L, "Yew trunk yielding Ancient Yew + Cedar timber!", dropChancePercent = 45, bonusSecondItemId = "item_cedar_timber", bonusSecondItemName = "Mountain Cedar", bonusSecondItemEmoji = "🪵", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_plain",
            reqTotemName = "Plain Obelisk",
            reqTotemEmoji = "🐮",
            specialPerkDesc = "Dual Harvest: Savanna Maple yields Autumn Maple + Ancient Yew!"
        ),
        GroveForestArea(
            id = "forest_totem_spirit_larupia",
            name = "Larupia Prowl Thicket",
            emoji = "🐆",
            reqLevel = 47,
            description = "Dense jungle thicket unlocked by the Sacred Grove Obelisk from Celadon Gym. Ancient Yew and Ironwood trees.",
            posXRatio = 0.70f,
            posYRatio = 0.52f,
            choppableTrees = listOf(
                GroveTree("item_yew_logs", "Predator Yew", "🌳", 47, 330L, "Ancient yew yielding Yew Logs + Metallic Ironwood!", dropChancePercent = 55, bonusSecondItemId = "item_ironwood_timber", bonusSecondItemName = "Metallic Ironwood", bonusSecondItemEmoji = "🗡️", bonusSecondItemQty = 1),
                GroveTree("item_ironwood_timber", "Stalker Ironwood", "🗡️", 55, 420L, "Metallic ironwood yielding Ironwood + Magic logs!", dropChancePercent = 45, bonusSecondItemId = "item_magic_logs", bonusSecondItemName = "Mystic Magic Tree", bonusSecondItemEmoji = "🔮", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_totem_sacred_grove",
            reqTotemName = "Sacred Grove Obelisk",
            reqTotemEmoji = "🌳",
            specialPerkDesc = "Dual Harvest: Predator Yew yields Yew Logs + Metallic Ironwood!"
        ),
        GroveForestArea(
            id = "forest_ironwood",
            name = "Ironwood Woodlands",
            emoji = "⚔️",
            reqLevel = 60,
            description = "Ancient dense ironwood grove where trees are hard as forged steel.",
            posXRatio = 0.75f,
            posYRatio = 0.62f,
            choppableTrees = listOf(
                GroveTree("item_yew_logs", "Ancient Yew", "🌳", 60, 220L, "Dense ancient yew tree used for master longbows.", dropChancePercent = 50),
                GroveTree("item_ironwood_timber", "Metallic Ironwood", "🗡️", 75, 300L, "Iron-hard metallic timber tree requiring sharp axes.", dropChancePercent = 30),
                GroveTree("item_magic_logs", "Mystic Magic Tree", "🔮", 75, 360L, "Glows with blue arcane mana along its bark.", dropChancePercent = 20)
            )
        ),
        GroveForestArea(
            id = "forest_totem_spirit_graahk",
            name = "Horned Graahk Highlands",
            emoji = "🦏",
            reqLevel = 61,
            description = "Rugged highland ridge unlocked by the Feather Obelisk from Fortree Gym. Metallic Ironwood and Mystic Magic trees.",
            posXRatio = 0.82f,
            posYRatio = 0.58f,
            choppableTrees = listOf(
                GroveTree("item_ironwood_timber", "Highland Ironwood", "🗡️", 61, 460L, "Tough ironwood yielding Metallic Ironwood + Mystic Magic Logs!", dropChancePercent = 50, bonusSecondItemId = "item_magic_logs", bonusSecondItemName = "Mystic Magic Tree", bonusSecondItemEmoji = "🔮", bonusSecondItemQty = 1),
                GroveTree("item_magic_logs", "Horned Magic Tree", "🔮", 68, 540L, "Arcane tree yielding Mystic Magic Logs + Yew logs!", dropChancePercent = 50, bonusSecondItemId = "item_yew_logs", bonusSecondItemName = "Ancient Yew", bonusSecondItemEmoji = "🌳", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_feather",
            reqTotemName = "Feather Obelisk",
            reqTotemEmoji = "🪶",
            specialPerkDesc = "Dual Harvest: Highland Ironwood yields Ironwood + Mystic Magic Logs!"
        ),
        GroveForestArea(
            id = "forest_totem_fruit_bat",
            name = "Bountiful Orchard Hollow",
            emoji = "🦇",
            reqLevel = 72,
            description = "Canopy hollow unlocked by the Fog Obelisk from Ecruteak Gym. Massive drops of Mystic Magic & Redwood.",
            posXRatio = 0.76f,
            posYRatio = 0.28f,
            choppableTrees = listOf(
                GroveTree("item_magic_logs", "Orchard Magic Tree", "🔮", 72, 580L, "Mystic tree yielding Magic Logs + Giant Redwood Timber!", dropChancePercent = 50, bonusSecondItemId = "item_redwood_timber", bonusSecondItemName = "Giant Redwood", bonusSecondItemEmoji = "🔴", bonusSecondItemQty = 1),
                GroveTree("item_redwood_timber", "Bountiful Redwood", "🔴", 78, 720L, "Giant redwood yielding Redwood + Spirit Redwood!", dropChancePercent = 50, bonusSecondItemId = "item_spirit_redwood", bonusSecondItemName = "Spirit Redwood", bonusSecondItemEmoji = "🌌", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_badge_fog",
            reqTotemName = "Fog Obelisk",
            reqTotemEmoji = "👻",
            specialPerkDesc = "Dual Harvest: Orchard Magic Tree yields Magic Logs + Giant Redwood!"
        ),
        GroveForestArea(
            id = "forest_redwood",
            name = "Enchanted Redwood Hollow",
            emoji = "🔴",
            reqLevel = 80,
            description = "Massive towering redwoods infused with ancient elemental earth energy.",
            posXRatio = 0.85f,
            posYRatio = 0.35f,
            choppableTrees = listOf(
                GroveTree("item_redwood_timber", "Giant Redwood", "🔴", 80, 500L, "Towering giant redwood reaching into the clouds.", dropChancePercent = 50),
                GroveTree("item_spirit_redwood", "Spirit Redwood", "🌌", 85, 600L, "Infused with deep shamanic spirit roots.", dropChancePercent = 30),
                GroveTree("item_astral_bark", "Astral Bark Oak", "💫", 88, 720L, "Glints with shimmering cosmic stardust bark.", dropChancePercent = 20)
            )
        ),
        GroveForestArea(
            id = "forest_totem_swamp_titan",
            name = "Ancient Bogwood Mire",
            emoji = "🐸",
            reqLevel = 85,
            description = "Primordial wetland unlocked by the Ember Spirit Obelisk from Fuchsia Gym. Spirit Redwood and Sunfire Baobabs.",
            posXRatio = 0.22f,
            posYRatio = 0.42f,
            choppableTrees = listOf(
                GroveTree("item_spirit_redwood", "Bogwood Spirit Redwood", "🌌", 85, 850L, "Ancient redwood yielding Spirit Redwood + Sunfire Baobab!", dropChancePercent = 50, bonusSecondItemId = "item_sunfire_log", bonusSecondItemName = "Sunfire Baobab", bonusSecondItemEmoji = "🔥", bonusSecondItemQty = 1),
                GroveTree("item_sunfire_log", "Titan Sunfire Baobab", "🔥", 88, 1150L, "Fiery tree yielding Sunfire Baobab + Emberwood Trunk!", dropChancePercent = 50, bonusSecondItemId = "item_emberwood_timber", bonusSecondItemName = "Emberwood Trunk", bonusSecondItemEmoji = "🪵", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_totem_ember_spirit",
            reqTotemName = "Ember Spirit Obelisk",
            reqTotemEmoji = "🔥",
            specialPerkDesc = "Dual Harvest: Spirit Redwood yields Spirit Redwood + Sunfire Baobab!"
        ),
        GroveForestArea(
            id = "forest_baobab",
            name = "Sunfire Baobab Oasis",
            emoji = "🔥",
            reqLevel = 90,
            description = "Sacred volcanic oasis growing fiery baobabs and obsidian embers.",
            posXRatio = 0.15f,
            posYRatio = 0.3f,
            choppableTrees = listOf(
                GroveTree("item_sunfire_log", "Sunfire Baobab", "🔥", 90, 880L, "Radiates searing desert heat from its core.", dropChancePercent = 50),
                GroveTree("item_emberwood_timber", "Emberwood Trunk", "🪵", 93, 1100L, "Smoldering wood with internal magma veins.", dropChancePercent = 30),
                GroveTree("item_obsidian_bark", "Obsidian Baobab", "🖤", 95, 1300L, "Extremely tough fireproof baobab tree.", dropChancePercent = 20)
            )
        ),
        GroveForestArea(
            id = "forest_totem_pack_yak",
            name = "Pack Yak Runic Timberlands",
            emoji = "🦬",
            reqLevel = 92,
            description = "High tundra grove unlocked by the Celestial Canopy Obelisk from Saffron Gym. Huge hauls of Emberwood and Astral Bark.",
            posXRatio = 0.48f,
            posYRatio = 0.22f,
            choppableTrees = listOf(
                GroveTree("item_emberwood_timber", "Yak Tundra Emberwood", "🪵", 92, 1300L, "Smoldering trunk yielding Emberwood + Astral Bark!", dropChancePercent = 50, bonusSecondItemId = "item_astral_bark", bonusSecondItemName = "Astral Bark Oak", bonusSecondItemEmoji = "💫", bonusSecondItemQty = 1),
                GroveTree("item_astral_bark", "Burden Astral Oak", "💫", 94, 1500L, "Stardust tree yielding Astral Bark + Obsidian Baobab!", dropChancePercent = 50, bonusSecondItemId = "item_obsidian_bark", bonusSecondItemName = "Obsidian Baobab", bonusSecondItemEmoji = "🖤", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_totem_celestial_canopy",
            reqTotemName = "Celestial Canopy Obelisk",
            reqTotemEmoji = "🌌",
            specialPerkDesc = "Dual Harvest: Yak Emberwood yields Emberwood + Astral Bark!"
        ),
        GroveForestArea(
            id = "forest_totem_phoenix",
            name = "Phoenix Sunspire Canopy",
            emoji = "🦅",
            reqLevel = 99,
            description = "Radiant crown of the celestial forest unlocked by the Sovereign Wild Obelisk from Viridian Gym. Bathed in solar flame.",
            posXRatio = 0.88f,
            posYRatio = 0.15f,
            choppableTrees = listOf(
                GroveTree("item_celestial_yew_log", "Phoenix Celestial Yew", "🌟", 99, 2400L, "Divine yew yielding Celestial Yew + Cosmic Redwood!", dropChancePercent = 50, bonusSecondItemId = "item_cosmic_redwood", bonusSecondItemName = "Cosmic Redwood", bonusSecondItemEmoji = "🐳", bonusSecondItemQty = 1),
                GroveTree("item_golden_spirit_trunk", "Solar World Tree", "🐉", 99, 3600L, "Pinnacle tree yielding Golden Spirit World Tree + Celestial Yew!", dropChancePercent = 50, bonusSecondItemId = "item_celestial_yew_log", bonusSecondItemName = "Celestial Yew", bonusSecondItemEmoji = "🌟", bonusSecondItemQty = 1)
            ),
            reqTotemId = "item_totem_sovereign_wild",
            reqTotemName = "Sovereign Wild Obelisk",
            reqTotemEmoji = "🦌",
            specialPerkDesc = "Dual Harvest: Phoenix Yew yields Celestial Yew + Cosmic Redwood!"
        ),
        GroveForestArea(
            id = "forest_celestial",
            name = "Celestial Yew Sanctuary",
            emoji = "🌟",
            reqLevel = 99,
            description = "Floating cosmic grove where divine trees blossom with infinite spirit essence.",
            posXRatio = 0.65f,
            posYRatio = 0.12f,
            choppableTrees = listOf(
                GroveTree("item_celestial_yew_log", "Celestial Yew", "🌟", 99, 1700L, "Divine floating yew tree blessed by spirit lords.", dropChancePercent = 50),
                GroveTree("item_cosmic_redwood", "Cosmic Redwood", "🐳", 99, 2100L, "Ancient cosmic tree rooted in void dust.", dropChancePercent = 30),
                GroveTree("item_golden_spirit_trunk", "Golden Spirit World Tree", "🐉", 99, 2900L, "Pinnacle World Tree radiating supreme Woodcutting & Firemaking power.", dropChancePercent = 20)
            )
        )
    )

    val GEMOLOGY_AREAS = listOf(
        GemologyArea(
            id = "quarry_surface",
            name = "Surface Ore Quarry",
            emoji = "🪨",
            reqLevel = 1,
            description = "Shallow sedimentary quarry rich in basic smelting ores and occasional blue gems.",
            posXRatio = 0.18f,
            posYRatio = 0.70f,
            minerals = listOf(
                GemologyMineral("item_copper_ore", "Copper Ore", "🪨", 1, 25L, 45, "Soft conductive reddish metal ore."),
                GemologyMineral("item_tin_ore", "Tin Ore", "🪨", 1, 25L, 35, "Silvery soft ore smelted into bronze."),
                GemologyMineral("item_rune_essence", "Rune Essence", "✨", 1, 20L, 12, "Porous crystalline stone used in Runecrafting."),
                GemologyMineral("item_uncut_sapphire", "Sapphire", "🟦", 20, 50L, 8, "Deep blue gem found within stone fissures.", isGem = true)
            )
        ),
        GemologyArea(
            id = "quarry_rune_essence_vault",
            name = "Rune Essence Vault",
            emoji = "✨",
            reqLevel = 1,
            description = "Ancient crystalline vault unlocked by the Woodland Obelisk from Pewter Gym. Brimming with pure rune essence (99% yield).",
            posXRatio = 0.28f,
            posYRatio = 0.62f,
            minerals = listOf(
                GemologyMineral("item_rune_essence", "Rune Essence", "✨", 1, 20L, 99, "Porous crystalline stone used in Runecrafting."),
                GemologyMineral("item_uncut_sapphire", "Sapphire", "🟦", 20, 50L, 1, "Deep blue gemstone unlocked at Lv. 20 Forging.", isGem = true),
                GemologyMineral("item_uncut_emerald", "Emerald", "🟩", 27, 75L, 1, "Vibrant green gemstone unlocked at Lv. 27 Forging.", isGem = true),
                GemologyMineral("item_uncut_ruby", "Ruby", "🟥", 34, 100L, 1, "Fiery crimson gemstone unlocked at Lv. 34 Forging.", isGem = true),
                GemologyMineral("item_uncut_diamond", "Diamond", "💎", 40, 150L, 1, "Flawless sparkling diamond unlocked at Lv. 40 Forging.", isGem = true),
                GemologyMineral("item_uncut_dragonstone", "Dragonstone", "🔮", 55, 250L, 1, "Precious purple dragonstone unlocked at Lv. 55 Forging.", isGem = true),
                GemologyMineral("item_uncut_onyx", "Onyx", "🖤", 67, 400L, 1, "Ultra-rare pitch black gemstone unlocked at Lv. 67 Forging.", isGem = true)
            ),
            reqTotemId = "item_totem_woodland",
            reqTotemName = "Woodland Obelisk",
            reqTotemEmoji = "🪵",
            specialPerkDesc = "Essence Siphon: Unlocks the Ancient Rune Essence Vault brimming with 99% pure Rune Essence!"
        ),
        GemologyArea(
            id = "quarry_totem_desert_wyrm",
            name = "Wyrm Tunnel Digsite",
            emoji = "🪱",
            reqLevel = 7,
            description = "Subterranean tunnel unlocked by the Coal Obelisk from Oreburgh Gym. Exposing mixed Copper & Tin deposits.",
            posXRatio = 0.22f,
            posYRatio = 0.82f,
            minerals = listOf(
                GemologyMineral("item_copper_ore", "Bronze Composite Vein", "🪨", 7, 45L, 60, "Rich copper-tin rock yielding 1x Copper Ore AND 1x Tin Ore!", bonusSecondItemId = "item_tin_ore", bonusSecondItemName = "Tin Ore", bonusSecondItemEmoji = "🪨", bonusSecondItemQty = 1),
                GemologyMineral("item_tin_ore", "Tin Vein", "🪨", 7, 40L, 30, "Soft tin vein yielding 1x Tin Ore + 1x Copper Ore!", bonusSecondItemId = "item_copper_ore", bonusSecondItemName = "Copper Ore", bonusSecondItemEmoji = "🪨", bonusSecondItemQty = 1),
                GemologyMineral("item_uncut_sapphire", "Wyrm Sapphire", "🟦", 20, 75L, 10, "Glinting sapphire buried in sand.", isGem = true)
            ),
            reqTotemId = "item_badge_coal",
            reqTotemName = "Coal Obelisk",
            reqTotemEmoji = "🪨",
            specialPerkDesc = "Dual Smelting Vein: Mining Composite Vein yields 1x Copper Ore + 1x Tin Ore simultaneously!"
        ),
        GemologyArea(
            id = "quarry_iron_ridge",
            name = "Iron & Coal Ridge",
            emoji = "⛏️",
            reqLevel = 15,
            description = "Mountain ridge layered with rich veins of iron, fuel coal, and raw emeralds.",
            posXRatio = 0.38f,
            posYRatio = 0.50f,
            minerals = listOf(
                GemologyMineral("item_iron_ore", "Iron Ore", "🪨", 15, 40L, 50, "Heavy magnetic iron ore used for steel and iron bars."),
                GemologyMineral("item_coal_ore", "Coal", "⬛", 30, 65L, 30, "Combustible black mineral fuel essential for steel and higher metals."),
                GemologyMineral("item_silver_ore", "Silver Ore", "⚪", 20, 45L, 12, "Precious lustrous ore smelted into holy holy symbols."),
                GemologyMineral("item_uncut_emerald", "Emerald", "🟩", 27, 75L, 8, "Vibrant green mineral gemstone.", isGem = true)
            )
        ),
        GemologyArea(
            id = "quarry_totem_granite_crab",
            name = "Granite Crab Seam",
            emoji = "🦀",
            reqLevel = 19,
            description = "Dense rocky seam unlocked by the Stone Obelisk from Rustboro Gym. Heavy dual deposits of Iron Ore & Coal.",
            posXRatio = 0.35f,
            posYRatio = 0.65f,
            minerals = listOf(
                GemologyMineral("item_iron_ore", "Steelmaker Iron Vein", "🪨", 19, 65L, 60, "Dense iron vein yielding 1x Iron Ore AND 1x Coal!", bonusSecondItemId = "item_coal_ore", bonusSecondItemName = "Coal", bonusSecondItemEmoji = "⬛", bonusSecondItemQty = 1),
                GemologyMineral("item_coal_ore", "Crab Coal Pocket", "⬛", 25, 75L, 30, "Crab-burrowed coal yielding 1x Coal + 1x Iron Ore!", bonusSecondItemId = "item_iron_ore", bonusSecondItemName = "Iron Ore", bonusSecondItemEmoji = "🪨", bonusSecondItemQty = 1),
                GemologyMineral("item_uncut_emerald", "Granite Emerald", "🟩", 27, 100L, 10, "Deep emerald embedded in granite.", isGem = true)
            ),
            reqTotemId = "item_badge_stone",
            reqTotemName = "Stone Obelisk",
            reqTotemEmoji = "🪨",
            specialPerkDesc = "Dual Smelting Vein: Mining Iron Vein yields 1x Iron Ore + 1x Coal simultaneously!"
        ),
        GemologyArea(
            id = "quarry_totem_spirit_bull",
            name = "Minotaur Vein Quarry",
            emoji = "🐂",
            reqLevel = 25,
            description = "Deep rocky cavern unlocked by the Ancient Crag Obelisk from Vermilion Gym. Rich in Silver Ore and Coal.",
            posXRatio = 0.48f,
            posYRatio = 0.60f,
            minerals = listOf(
                GemologyMineral("item_silver_ore", "Bull Silver Seam", "⚪", 25, 80L, 55, "Lustrous silver seam yielding 1x Silver Ore AND 1x Coal!", bonusSecondItemId = "item_coal_ore", bonusSecondItemName = "Coal", bonusSecondItemEmoji = "⬛", bonusSecondItemQty = 1),
                GemologyMineral("item_coal_ore", "Heavy Bull Coal", "⬛", 30, 85L, 35, "Dense fuel coal yielding 1x Coal + 1x Silver Ore!", bonusSecondItemId = "item_silver_ore", bonusSecondItemName = "Silver Ore", bonusSecondItemEmoji = "⚪", bonusSecondItemQty = 1),
                GemologyMineral("item_uncut_ruby", "Bull Ruby", "🟥", 34, 125L, 10, "Glowing red ruby.", isGem = true)
            ),
            reqTotemId = "item_totem_ancient_crag",
            reqTotemName = "Ancient Crag Obelisk",
            reqTotemEmoji = "🗿",
            specialPerkDesc = "Dual Smelting Vein: Mining Silver Seam yields 1x Silver Ore + 1x Coal simultaneously!"
        ),
        GemologyArea(
            id = "quarry_totem_pyrelord",
            name = "Pyrelord Smoldering Crater",
            emoji = "🔥",
            reqLevel = 31,
            description = "Molten caldera unlocked by the Heat Obelisk from Lavaridge Gym. Fuses raw Gold Ore with High-Heat Coal.",
            posXRatio = 0.30f,
            posYRatio = 0.40f,
            minerals = listOf(
                GemologyMineral("item_gold_ore", "Smoldering Gold Vein", "🟡", 31, 110L, 55, "Hot gold seam yielding 1x Gold Ore AND 1x Coal!", bonusSecondItemId = "item_coal_ore", bonusSecondItemName = "Coal", bonusSecondItemEmoji = "⬛", bonusSecondItemQty = 1),
                GemologyMineral("item_coal_ore", "Pyre Magma Coal", "⬛", 35, 95L, 35, "Blazing coal yielding 1x Coal + 1x Gold Ore!", bonusSecondItemId = "item_gold_ore", bonusSecondItemName = "Gold Ore", bonusSecondItemEmoji = "🟡", bonusSecondItemQty = 1),
                GemologyMineral("item_uncut_ruby", "Pyre Ruby", "🟥", 34, 140L, 10, "Fire-forged ruby.", isGem = true)
            ),
            reqTotemId = "item_badge_heat",
            reqTotemName = "Heat Obelisk",
            reqTotemEmoji = "🔥",
            specialPerkDesc = "Dual Smelting Vein: Mining Gold Seam yields 1x Gold Ore + 1x Coal simultaneously!"
        ),
        GemologyArea(
            id = "quarry_totem_magpie",
            name = "Magpie Glittering Pocket",
            emoji = "🐦",
            reqLevel = 34,
            description = "Shiny cliffside pocket unlocked by the Dynamo Obelisk from Mauville Gym. Stockpiled with glittering Gold Ore, Diamonds, and Rubies.",
            posXRatio = 0.60f,
            posYRatio = 0.52f,
            minerals = listOf(
                GemologyMineral("item_gold_ore", "Glittering Gold Pocket", "🟡", 34, 120L, 50, "Shiny gold pocket yielding 1x Gold Ore AND 1x Diamond!", bonusSecondItemId = "item_uncut_diamond", bonusSecondItemName = "Diamond", bonusSecondItemEmoji = "💎", bonusSecondItemQty = 1),
                GemologyMineral("item_uncut_ruby", "Magpie Ruby", "🟥", 34, 130L, 30, "Ruby pocket yielding 1x Ruby + 1x Sapphire!", bonusSecondItemId = "item_uncut_sapphire", bonusSecondItemName = "Sapphire", bonusSecondItemEmoji = "🟦", bonusSecondItemQty = 1, isGem = true),
                GemologyMineral("item_uncut_diamond", "Sparkling Star Diamond", "💎", 40, 180L, 20, "Sparkling diamond.", isGem = true)
            ),
            reqTotemId = "item_badge_dynamo",
            reqTotemName = "Dynamo Obelisk",
            reqTotemEmoji = "⚡",
            specialPerkDesc = "Dual Jewel Vein: Mining Gold Pocket yields 1x Gold Ore + 1x Diamond simultaneously!"
        ),
        GemologyArea(
            id = "quarry_totem_war_tortoise",
            name = "War Tortoise Bastion Quarry",
            emoji = "🐢",
            reqLevel = 52,
            description = "Heavily fortified cavern unlocked by the Mineral Obelisk from Olivine Gym. Pure Opalite Ore and Coal deposits.",
            posXRatio = 0.72f,
            posYRatio = 0.45f,
            minerals = listOf(
                GemologyMineral("item_mithril_ore", "Bastion Opalite Seam", "🔷", 52, 150L, 55, "Resilient opalite rock yielding 1x Opalite Ore AND 1x Coal!", bonusSecondItemId = "item_coal_ore", bonusSecondItemName = "Coal", bonusSecondItemEmoji = "⬛", bonusSecondItemQty = 1),
                GemologyMineral("item_gold_ore", "Fortress Gold", "🟡", 52, 130L, 30, "Heavy gold seam yielding 1x Gold Ore + 1x Opalite Ore!", bonusSecondItemId = "item_mithril_ore", bonusSecondItemName = "Opalite Ore", bonusSecondItemEmoji = "🔷", bonusSecondItemQty = 1),
                GemologyMineral("item_uncut_diamond", "Bastion Diamond", "💎", 52, 200L, 15, "Unbreakable diamond crystal.", isGem = true)
            ),
            reqTotemId = "item_badge_mineral",
            reqTotemName = "Mineral Obelisk",
            reqTotemEmoji = "⚙️",
            specialPerkDesc = "Dual Smelting Vein: Mining Opalite Seam yields 1x Opalite Ore + 1x Coal simultaneously!"
        ),
        GemologyArea(
            id = "quarry_mithril_veins",
            name = "Deep Opalite Veins",
            emoji = "🔷",
            reqLevel = 55,
            description = "Subterranean azure caverns containing dense opalite, gold, and blood-red rubies.",
            posXRatio = 0.65f,
            posYRatio = 0.68f,
            minerals = listOf(
                GemologyMineral("item_mithril_ore", "Opalite Ore", "🔷", 55, 100L, 50, "Resilient lightweight opalite ore treasured by elven smiths."),
                GemologyMineral("item_gold_ore", "Gold Ore", "🟡", 40, 75L, 25, "Pure heavy gold ore crafted into jewellery."),
                GemologyMineral("item_uncut_ruby", "Ruby", "🟥", 34, 100L, 15, "Fiery crimson gemstone radiating heat.", isGem = true),
                GemologyMineral("item_uncut_diamond", "Diamond", "💎", 40, 150L, 10, "Flawless sparkling gem of extreme hardness.", isGem = true)
            )
        ),
        GemologyArea(
            id = "quarry_totem_spirit_kyatt",
            name = "Kyatt Jagged Peak",
            emoji = "🐅",
            reqLevel = 65,
            description = "High mountain crags unlocked by the Mine Obelisk from Canalave Gym. Sharp Amethyst Ore and glowing Dragonstones.",
            posXRatio = 0.85f,
            posYRatio = 0.32f,
            minerals = listOf(
                GemologyMineral("item_adamant_ore", "Jagged Amethyst Vein", "🟢", 65, 210L, 55, "Tough amethyst seam yielding 1x Amethyst Ore AND 1x Coal!", bonusSecondItemId = "item_coal_ore", bonusSecondItemName = "Coal", bonusSecondItemEmoji = "⬛", bonusSecondItemQty = 1),
                GemologyMineral("item_mithril_ore", "Kyatt Opalite Crag", "🔷", 65, 175L, 30, "Opalite crag yielding 1x Opalite Ore + 1x Amethyst Ore!", bonusSecondItemId = "item_adamant_ore", bonusSecondItemName = "Amethyst Ore", bonusSecondItemEmoji = "🟢", bonusSecondItemQty = 1),
                GemologyMineral("item_uncut_dragonstone", "Peak Dragonstone", "🔮", 65, 320L, 15, "Rare purple dragonstone.", isGem = true)
            ),
            reqTotemId = "item_badge_mine",
            reqTotemName = "Mine Obelisk",
            reqTotemEmoji = "🛡️",
            specialPerkDesc = "Dual Smelting Vein: Mining Amethyst Vein yields 1x Amethyst Ore + 1x Coal simultaneously!"
        ),
        GemologyArea(
            id = "quarry_adamant_chasm",
            name = "Amethyst & Aetherite Chasm",
            emoji = "🟢",
            reqLevel = 70,
            description = "Deep chasm holding unbreakable amethyst ore, legendary aetherite, and purple dragonstones.",
            posXRatio = 0.82f,
            posYRatio = 0.40f,
            minerals = listOf(
                GemologyMineral("item_adamant_ore", "Amethyst Ore", "🟢", 70, 150L, 50, "Hard amethyst ore used to forge impenetrable amethyst armor."),
                GemologyMineral("item_runite_ore", "Aetherite Ore", "💙", 85, 250L, 25, "Legendary azure metallic ore imbued with ancient smithing power."),
                GemologyMineral("item_uncut_diamond", "Diamond", "💎", 40, 150L, 15, "Flawless brilliant crystal diamond.", isGem = true),
                GemologyMineral("item_uncut_dragonstone", "Dragonstone", "🔮", 55, 250L, 10, "Precious royal purple dragonstone gem.", isGem = true)
            )
        ),
        GemologyArea(
            id = "quarry_totem_earth_titan",
            name = "Earth Titan Megalith",
            emoji = "🗿",
            reqLevel = 80,
            description = "Monolithic cavern unlocked by the Ancient Crag Obelisk from Vermilion Gym. Pure Aetherite Ore and deep Onyx crystals.",
            posXRatio = 0.45f,
            posYRatio = 0.18f,
            minerals = listOf(
                GemologyMineral("item_runite_ore", "Titan Aetherite Seam", "💙", 80, 320L, 55, "Legendary azure ore yielding 1x Aetherite Ore AND 1x High-Heat Coal!", bonusSecondItemId = "item_coal_ore", bonusSecondItemName = "Coal", bonusSecondItemEmoji = "⬛", bonusSecondItemQty = 1),
                GemologyMineral("item_adamant_ore", "Titan Amethyst Vein", "🟢", 80, 240L, 30, "Heavy amethyst ore yielding 1x Amethyst Ore + 1x Aetherite Ore!", bonusSecondItemId = "item_runite_ore", bonusSecondItemName = "Aetherite Ore", bonusSecondItemEmoji = "💙", bonusSecondItemQty = 1),
                GemologyMineral("item_uncut_onyx", "Megalith Onyx", "🖤", 80, 480L, 15, "Pure dark obsidian onyx crystal.", isGem = true)
            ),
            reqTotemId = "item_totem_ancient_crag",
            reqTotemName = "Ancient Crag Obelisk",
            reqTotemEmoji = "🗿",
            specialPerkDesc = "Dual Smelting Vein: Mining Aetherite Seam yields 1x Aetherite Ore + 1x Coal simultaneously!"
        ),
        GemologyArea(
            id = "quarry_volcanic_magma",
            name = "Volcanic Magma Quarry",
            emoji = "🌋",
            reqLevel = 85,
            description = "Fiery obsidian crag surging with volcanic heat and pitch-black onyx crystals.",
            posXRatio = 0.22f,
            posYRatio = 0.24f,
            minerals = listOf(
                GemologyMineral("item_runite_ore", "Aetherite Ore", "💙", 85, 250L, 45, "Pure aetherite ore baked in magma heat."),
                GemologyMineral("item_coal_ore", "High-Heat Coal", "⬛", 30, 80L, 30, "Intense volcanic coal giving immense smelting heat."),
                GemologyMineral("item_uncut_dragonstone", "Dragonstone", "🔮", 55, 250L, 15, "Dragonstone forged in dragon magma.", isGem = true),
                GemologyMineral("item_uncut_onyx", "Onyx", "🖤", 67, 400L, 10, "Ultra-rare pitch black gemstone of supreme power.", isGem = true)
            )
        ),
        GemologyArea(
            id = "quarry_totem_steel_titan",
            name = "Colossal Steel Foundry Crater",
            emoji = "🤖",
            reqLevel = 95,
            description = "Apex volcanic foundry quarry unlocked by the Sovereign Wild Obelisk from Viridian Gym. Celestial Aetherite and pure Onyx gems.",
            posXRatio = 0.75f,
            posYRatio = 0.16f,
            minerals = listOf(
                GemologyMineral("item_runite_ore", "Colossal Aetherite Vein", "💙", 95, 420L, 50, "Pure legendary ore yielding 1x Aetherite Ore AND 1x Onyx Gem!", bonusSecondItemId = "item_uncut_onyx", bonusSecondItemName = "Onyx", bonusSecondItemEmoji = "🖤", bonusSecondItemQty = 1),
                GemologyMineral("item_uncut_onyx", "Foundry Onyx Core", "🖤", 95, 550L, 30, "Flawless onyx yielding 1x Onyx + 1x Dragonstone!", bonusSecondItemId = "item_uncut_dragonstone", bonusSecondItemName = "Dragonstone", bonusSecondItemEmoji = "🔮", bonusSecondItemQty = 1, isGem = true),
                GemologyMineral("item_uncut_dragonstone", "Astral Dragonstone Core", "🔮", 95, 450L, 20, "Cosmic dragonstone.", isGem = true)
            ),
            reqTotemId = "item_totem_sovereign_wild",
            reqTotemName = "Sovereign Wild Obelisk",
            reqTotemEmoji = "🦌",
            specialPerkDesc = "Dual Treasure Vein: Mining Colossal Aetherite yields 1x Aetherite Ore + 1x Onyx simultaneously!"
        ),
        GemologyArea(
            id = "quarry_celestial_nexus",
            name = "Celestial Gem Nexus",
            emoji = "🌟",
            reqLevel = 95,
            description = "Cosmic astral quarry where pure celestial ores and legendary gemstones crystallize.",
            posXRatio = 0.68f,
            posYRatio = 0.14f,
            minerals = listOf(
                GemologyMineral("item_runite_ore", "Celestial Aetherite", "💙", 85, 300L, 40, "Aetherite infused with starlight."),
                GemologyMineral("item_uncut_onyx", "Onyx", "🖤", 67, 400L, 30, "Flawless dark obsidian onyx gem.", isGem = true),
                GemologyMineral("item_uncut_dragonstone", "Astral Dragonstone", "🔮", 55, 300L, 20, "Cosmic dragonstone radiating celestial light.", isGem = true),
                GemologyMineral("item_uncut_diamond", "Divine Star Diamond", "💎", 40, 250L, 10, "Pure star diamond glowing with infinite clarity.", isGem = true)
            )
        )
    )
}
