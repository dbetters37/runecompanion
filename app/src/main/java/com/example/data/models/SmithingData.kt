package com.example.data.models

data class SmeltRecipe(
    val id: String,
    val barName: String,
    val barItemId: String,
    val reqLevel: Int,
    val xpReward: Long,
    val iconEmoji: String,
    val inputOres: List<RecipeMaterial>,
    val outputQuantity: Int = 1,
    val description: String
)

data class SmithAnvilRecipe(
    val id: String,
    val name: String,
    val metalTier: String, // "Bronze", "Iron", "Steel", "Opalite", "Amethyst", "Aetherite"
    val reqLevel: Int,
    val xpReward: Long,
    val iconEmoji: String,
    val barItemId: String,
    val barsRequired: Int,
    val outputItemId: String,
    val outputItemName: String,
    val outputQuantity: Int = 1,
    val equipSlot: EquipmentSlot? = null,
    val combatPower: Int = 0,
    val defPower: Int = 0,
    val description: String
)

object SmithingData {
    val ALL_ANVIL_RECIPES: List<SmithAnvilRecipe> by lazy {
        listOf("Bronze", "Iron", "Steel", "Opalite", "Amethyst", "Aetherite").flatMap { getAnvilRecipesForTier(it) }
    }

    val SMELT_RECIPES = listOf(
        SmeltRecipe(
            id = "smelt_bronze_bar",
            barName = "Bronze Bar",
            barItemId = "item_bronze_bar",
            reqLevel = 1,
            xpReward = 15L,
            iconEmoji = "🟤",
            inputOres = listOf(
                RecipeMaterial("item_copper_ore", "Copper Ore", 1, "🪨"),
                RecipeMaterial("item_tin_ore", "Tin Ore", 1, "🪨")
            ),
            outputQuantity = 1,
            description = "Smelt Copper and Tin ores at the furnace into a Bronze Bar."
        ),
        SmeltRecipe(
            id = "smelt_bronze_nails",
            barName = "Bronze Nails (x15)",
            barItemId = "item_nails",
            reqLevel = 4,
            xpReward = 13L,
            iconEmoji = "🔩",
            inputOres = listOf(
                RecipeMaterial("item_bronze_bar", "Bronze Bar", 1, "🟤")
            ),
            outputQuantity = 15,
            description = "Smelt & cast a Bronze Bar into 15 Bronze Construction Nails."
        ),
        SmeltRecipe(
            id = "smelt_iron_bar",
            barName = "Iron Bar",
            barItemId = "item_iron_bar",
            reqLevel = 15,
            xpReward = 25L,
            iconEmoji = "⚪",
            inputOres = listOf(
                RecipeMaterial("item_iron_ore", "Iron Ore", 1, "🪨")
            ),
            outputQuantity = 1,
            description = "Smelt Iron ore at the furnace into an Iron Bar."
        ),
        SmeltRecipe(
            id = "smelt_iron_nails",
            barName = "Iron Nails (x15)",
            barItemId = "item_iron_nails",
            reqLevel = 19,
            xpReward = 22L,
            iconEmoji = "🔩",
            inputOres = listOf(
                RecipeMaterial("item_iron_bar", "Iron Bar", 1, "⚪")
            ),
            outputQuantity = 15,
            description = "Smelt & cast an Iron Bar into 15 sturdy Iron Nails."
        ),
        SmeltRecipe(
            id = "smelt_steel_bar",
            barName = "Steel Bar",
            barItemId = "item_steel_bar",
            reqLevel = 30,
            xpReward = 38L,
            iconEmoji = "🪙",
            inputOres = listOf(
                RecipeMaterial("item_iron_ore", "Iron Ore", 1, "🪨"),
                RecipeMaterial("item_coal_ore", "Coal", 2, "⬛")
            ),
            outputQuantity = 1,
            description = "Smelt Iron ore and 2 Coal into a durable Steel Bar."
        ),
        SmeltRecipe(
            id = "smelt_steel_nails",
            barName = "Steel Nails (x15)",
            barItemId = "item_steel_nails",
            reqLevel = 34,
            xpReward = 35L,
            iconEmoji = "🔩",
            inputOres = listOf(
                RecipeMaterial("item_steel_bar", "Steel Bar", 1, "🪙")
            ),
            outputQuantity = 15,
            description = "Smelt & cast a Steel Bar into 15 durable Steel Nails."
        ),
        SmeltRecipe(
            id = "smelt_mithril_bar",
            barName = "Opalite Bar",
            barItemId = "item_mithril_bar",
            reqLevel = 50,
            xpReward = 60L,
            iconEmoji = "🟦",
            inputOres = listOf(
                RecipeMaterial("item_mithril_ore", "Opalite Ore", 1, "🪨"),
                RecipeMaterial("item_coal_ore", "Coal", 2, "⬛")
            ),
            outputQuantity = 1,
            description = "Smelt Opalite ore and 2 Coal into a glowing Opalite Bar."
        ),
        SmeltRecipe(
            id = "smelt_mithril_nails",
            barName = "Opalite Nails (x15)",
            barItemId = "item_mithril_nails",
            reqLevel = 54,
            xpReward = 55L,
            iconEmoji = "🔩",
            inputOres = listOf(
                RecipeMaterial("item_mithril_bar", "Opalite Bar", 1, "🟦")
            ),
            outputQuantity = 15,
            description = "Smelt & cast an Opalite Bar into 15 high-tier Opalite Nails."
        ),
        SmeltRecipe(
            id = "smelt_adamant_bar",
            barName = "Amethyst Bar",
            barItemId = "item_adamant_bar",
            reqLevel = 70,
            xpReward = 100L,
            iconEmoji = "🟩",
            inputOres = listOf(
                RecipeMaterial("item_adamant_ore", "Amethyst Ore", 1, "🪨"),
                RecipeMaterial("item_coal_ore", "Coal", 3, "⬛")
            ),
            outputQuantity = 1,
            description = "Smelt Amethyst ore and 3 Coal into a heavy Amethyst Bar."
        ),
        SmeltRecipe(
            id = "smelt_adamant_nails",
            barName = "Amethyst Nails (x15)",
            barItemId = "item_adamant_nails",
            reqLevel = 74,
            xpReward = 80L,
            iconEmoji = "🔩",
            inputOres = listOf(
                RecipeMaterial("item_adamant_bar", "Amethyst Bar", 1, "🟩")
            ),
            outputQuantity = 15,
            description = "Smelt & cast an Amethyst Bar into 15 heavy-duty Amethyst Nails."
        ),
        SmeltRecipe(
            id = "smelt_rune_bar",
            barName = "Aetherite Bar",
            barItemId = "item_rune_bar",
            reqLevel = 85,
            xpReward = 150L,
            iconEmoji = "🔷",
            inputOres = listOf(
                RecipeMaterial("item_runite_ore", "Aetherite Ore", 1, "💙"),
                RecipeMaterial("item_coal_ore", "Coal", 4, "⬛")
            ),
            outputQuantity = 1,
            description = "Smelt Aetherite ore and 4 Coal into a prized Aetherite Bar."
        ),
        SmeltRecipe(
            id = "smelt_rune_nails",
            barName = "Aetherite Nails (x15)",
            barItemId = "item_rune_nails",
            reqLevel = 89,
            xpReward = 120L,
            iconEmoji = "🔩",
            inputOres = listOf(
                RecipeMaterial("item_rune_bar", "Aetherite Bar", 1, "🔷")
            ),
            outputQuantity = 15,
            description = "Smelt & cast an Aetherite Bar into 15 Masterwork Aetherite Nails."
        )
    )

    fun getAnvilRecipesForTier(tier: String): List<SmithAnvilRecipe> {
        val barId = when (tier) {
            "Bronze" -> "item_bronze_bar"
            "Iron" -> "item_iron_bar"
            "Steel" -> "item_steel_bar"
            "Opalite", "Mithril" -> "item_mithril_bar"
            "Amethyst", "Adamant" -> "item_adamant_bar"
            "Aetherite", "Rune" -> "item_rune_bar"
            else -> "item_bronze_bar"
        }
        val prefix = when (tier) {
            "Opalite" -> "mithril"
            "Amethyst" -> "adamant"
            "Aetherite" -> "rune"
            else -> tier.lowercase()
        }
        val baseLvl = when (tier) {
            "Bronze" -> 1
            "Iron" -> 15
            "Steel" -> 30
            "Opalite", "Mithril" -> 50
            "Amethyst", "Adamant" -> 70
            "Aetherite", "Rune" -> 85
            else -> 1
        }
        val multXp = when (tier) {
            "Bronze" -> 12L
            "Iron" -> 25L
            "Steel" -> 38L
            "Opalite", "Mithril" -> 50L
            "Amethyst", "Adamant" -> 62L
            "Aetherite", "Rune" -> 75L
            else -> 12L
        }

        return listOf(
            SmithAnvilRecipe(
                id = "smith_${prefix}_dagger",
                name = "$tier Dagger",
                metalTier = tier,
                reqLevel = baseLvl,
                xpReward = multXp * 1,
                iconEmoji = "🗡️",
                barItemId = barId,
                barsRequired = 1,
                outputItemId = "item_${prefix}_dagger",
                outputItemName = "$tier Dagger",
                equipSlot = EquipmentSlot.WEAPON,
                combatPower = when (tier) { "Bronze" -> 8; "Iron" -> 14; "Silver" -> 20; "Steel" -> 28; "Gold" -> 36; "Opalite", "Mithril" -> 48; "Amethyst", "Adamant" -> 62; "Aetherite", "Rune" -> 80; else -> 8 },
                defPower = when (tier) { "Bronze" -> 1; "Iron" -> 2; "Silver" -> 3; "Steel" -> 4; "Gold" -> 6; "Opalite", "Mithril" -> 8; "Amethyst", "Adamant" -> 10; "Aetherite", "Rune" -> 14; else -> 1 },
                description = "Lightweight thrusting dagger forged at the anvil."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_sword",
                name = "$tier Sword",
                metalTier = tier,
                reqLevel = baseLvl + 1,
                xpReward = multXp * 1,
                iconEmoji = "⚔️",
                barItemId = barId,
                barsRequired = 1,
                outputItemId = "item_${prefix}_sword",
                outputItemName = "$tier Sword",
                equipSlot = EquipmentSlot.WEAPON,
                combatPower = when (tier) { "Bronze" -> 10; "Iron" -> 18; "Silver" -> 26; "Steel" -> 35; "Gold" -> 46; "Opalite", "Mithril" -> 60; "Amethyst", "Adamant" -> 78; "Aetherite", "Rune" -> 98; else -> 10 },
                defPower = when (tier) { "Bronze" -> 2; "Iron" -> 4; "Silver" -> 6; "Steel" -> 8; "Gold" -> 11; "Opalite", "Mithril" -> 14; "Amethyst", "Adamant" -> 18; "Aetherite", "Rune" -> 24; else -> 2 },
                description = "Classic sword for melee combat forged from $tier."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_scimitar",
                name = "$tier Scimitar",
                metalTier = tier,
                reqLevel = baseLvl + 2,
                xpReward = multXp * 2,
                iconEmoji = "🗡️",
                barItemId = barId,
                barsRequired = 2,
                outputItemId = "item_${prefix}_scimitar",
                outputItemName = "$tier Scimitar",
                equipSlot = EquipmentSlot.WEAPON,
                combatPower = when (tier) { "Bronze" -> 12; "Iron" -> 22; "Silver" -> 30; "Steel" -> 42; "Gold" -> 54; "Opalite", "Mithril" -> 70; "Amethyst", "Adamant" -> 90; "Aetherite", "Rune" -> 115; else -> 12 },
                defPower = when (tier) { "Bronze" -> 1; "Iron" -> 2; "Silver" -> 3; "Steel" -> 4; "Gold" -> 6; "Opalite", "Mithril" -> 8; "Amethyst", "Adamant" -> 10; "Aetherite", "Rune" -> 14; else -> 1 },
                description = "Curved $tier blade with fast slashing speed."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_axe",
                name = "$tier Hatchet",
                metalTier = tier,
                reqLevel = baseLvl + 1,
                xpReward = multXp * 1,
                iconEmoji = "🪓",
                barItemId = barId,
                barsRequired = 1,
                outputItemId = "item_${prefix}_axe",
                outputItemName = "$tier Hatchet",
                equipSlot = EquipmentSlot.AXE,
                combatPower = when (tier) { "Bronze" -> 6; "Iron" -> 12; "Silver" -> 18; "Steel" -> 26; "Gold" -> 34; "Opalite", "Mithril" -> 44; "Amethyst", "Adamant" -> 58; "Aetherite", "Rune" -> 75; else -> 6 },
                defPower = when (tier) { "Bronze" -> 1; "Iron" -> 2; "Silver" -> 3; "Steel" -> 4; "Gold" -> 6; "Opalite", "Mithril" -> 8; "Amethyst", "Adamant" -> 10; "Aetherite", "Rune" -> 14; else -> 1 },
                description = "Sharp $tier hatchet for woodcutting and combat."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_helmet",
                name = "$tier Full Helm",
                metalTier = tier,
                reqLevel = baseLvl + 3,
                xpReward = multXp * 2,
                iconEmoji = "🪖",
                barItemId = barId,
                barsRequired = 2,
                outputItemId = "item_${prefix}_full_helm",
                outputItemName = "$tier Full Helm",
                equipSlot = EquipmentSlot.HEAD,
                combatPower = when (tier) { "Bronze" -> 1; "Iron" -> 2; "Silver" -> 3; "Steel" -> 4; "Gold" -> 5; "Opalite", "Mithril" -> 7; "Amethyst", "Adamant" -> 9; "Aetherite", "Rune" -> 12; else -> 1 },
                defPower = when (tier) { "Bronze" -> 8; "Iron" -> 15; "Silver" -> 22; "Steel" -> 30; "Gold" -> 40; "Opalite", "Mithril" -> 52; "Amethyst", "Adamant" -> 68; "Aetherite", "Rune" -> 88; else -> 8 },
                description = "Protective full helmet forged at the anvil."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_platebody",
                name = "$tier Platebody",
                metalTier = tier,
                reqLevel = baseLvl + 5,
                xpReward = multXp * 5,
                iconEmoji = "🛡️",
                barItemId = barId,
                barsRequired = 5,
                outputItemId = "item_${prefix}_platebody",
                outputItemName = "$tier Platebody",
                equipSlot = EquipmentSlot.BODY,
                combatPower = when (tier) { "Bronze" -> 2; "Iron" -> 3; "Silver" -> 4; "Steel" -> 5; "Gold" -> 7; "Opalite", "Mithril" -> 9; "Amethyst", "Adamant" -> 12; "Aetherite", "Rune" -> 16; else -> 2 },
                defPower = when (tier) { "Bronze" -> 16; "Iron" -> 28; "Silver" -> 40; "Steel" -> 54; "Gold" -> 70; "Opalite", "Mithril" -> 90; "Amethyst", "Adamant" -> 115; "Aetherite", "Rune" -> 145; else -> 16 },
                description = "Heavy chest armor forging masterwork protection."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_platelegs",
                name = "$tier Platelegs",
                metalTier = tier,
                reqLevel = baseLvl + 4,
                xpReward = multXp * 3,
                iconEmoji = "🦵",
                barItemId = barId,
                barsRequired = 3,
                outputItemId = "item_${prefix}_platelegs",
                outputItemName = "$tier Platelegs",
                equipSlot = EquipmentSlot.LEGS,
                combatPower = when (tier) { "Bronze" -> 1; "Iron" -> 2; "Silver" -> 3; "Steel" -> 4; "Gold" -> 5; "Opalite", "Mithril" -> 7; "Amethyst", "Adamant" -> 9; "Aetherite", "Rune" -> 12; else -> 1 },
                defPower = when (tier) { "Bronze" -> 12; "Iron" -> 20; "Silver" -> 28; "Steel" -> 38; "Gold" -> 50; "Opalite", "Mithril" -> 64; "Amethyst", "Adamant" -> 82; "Aetherite", "Rune" -> 105; else -> 12 },
                description = "Heavy leg armor offering solid defense."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_kiteshield",
                name = "$tier Kiteshield",
                metalTier = tier,
                reqLevel = baseLvl + 4,
                xpReward = multXp * 3,
                iconEmoji = "🛡️",
                barItemId = barId,
                barsRequired = 3,
                outputItemId = "item_${prefix}_kiteshield",
                outputItemName = "$tier Kiteshield",
                equipSlot = EquipmentSlot.SHIELD,
                combatPower = when (tier) { "Bronze" -> 1; "Iron" -> 2; "Silver" -> 3; "Steel" -> 4; "Gold" -> 5; "Opalite", "Mithril" -> 7; "Amethyst", "Adamant" -> 9; "Aetherite", "Rune" -> 12; else -> 1 },
                defPower = when (tier) { "Bronze" -> 12; "Iron" -> 20; "Silver" -> 28; "Steel" -> 38; "Gold" -> 50; "Opalite", "Mithril" -> 64; "Amethyst", "Adamant" -> 82; "Aetherite", "Rune" -> 105; else -> 12 },
                description = "Large shield to block incoming attacks."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_gauntlets",
                name = "$tier Gauntlets",
                metalTier = tier,
                reqLevel = baseLvl + 2,
                xpReward = multXp * 1,
                iconEmoji = "🧤",
                barItemId = barId,
                barsRequired = 1,
                outputItemId = "item_${prefix}_gauntlets",
                outputItemName = "$tier Gauntlets",
                equipSlot = EquipmentSlot.GLOVES,
                combatPower = when (tier) { "Bronze" -> 1; "Iron" -> 2; "Silver" -> 3; "Steel" -> 4; "Gold" -> 5; "Opalite", "Mithril" -> 7; "Amethyst", "Adamant" -> 9; "Aetherite", "Rune" -> 12; else -> 1 },
                defPower = when (tier) { "Bronze" -> 6; "Iron" -> 10; "Silver" -> 14; "Steel" -> 18; "Gold" -> 24; "Opalite", "Mithril" -> 30; "Amethyst", "Adamant" -> 38; "Aetherite", "Rune" -> 48; else -> 6 },
                description = "Protective metal gauntlets for hand defense."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_boots",
                name = "$tier Boots",
                metalTier = tier,
                reqLevel = baseLvl + 2,
                xpReward = multXp * 1,
                iconEmoji = "👢",
                barItemId = barId,
                barsRequired = 1,
                outputItemId = "item_${prefix}_boots",
                outputItemName = "$tier Boots",
                equipSlot = EquipmentSlot.BOOTS,
                combatPower = when (tier) { "Bronze" -> 1; "Iron" -> 2; "Silver" -> 3; "Steel" -> 4; "Gold" -> 5; "Opalite", "Mithril" -> 7; "Amethyst", "Adamant" -> 9; "Aetherite", "Rune" -> 12; else -> 1 },
                defPower = when (tier) { "Bronze" -> 6; "Iron" -> 10; "Silver" -> 14; "Steel" -> 18; "Gold" -> 24; "Opalite", "Mithril" -> 30; "Amethyst", "Adamant" -> 38; "Aetherite", "Rune" -> 48; else -> 6 },
                description = "Sturdy metal boots providing foot protection."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_cape",
                name = "$tier Cape",
                metalTier = tier,
                reqLevel = baseLvl + 2,
                xpReward = multXp * 2,
                iconEmoji = "🧥",
                barItemId = barId,
                barsRequired = 2,
                outputItemId = "item_${prefix}_cape",
                outputItemName = "$tier Cape",
                equipSlot = EquipmentSlot.CAPE,
                combatPower = when (tier) { "Bronze" -> 1; "Iron" -> 2; "Silver" -> 3; "Steel" -> 4; "Gold" -> 5; "Opalite", "Mithril" -> 7; "Amethyst", "Adamant" -> 9; "Aetherite", "Rune" -> 12; else -> 1 },
                defPower = when (tier) { "Bronze" -> 4; "Iron" -> 7; "Silver" -> 10; "Steel" -> 14; "Gold" -> 18; "Opalite", "Mithril" -> 24; "Amethyst", "Adamant" -> 32; "Aetherite", "Rune" -> 42; else -> 4 },
                description = "Woven heraldic cape fastened with a $tier crest."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_ring",
                name = "$tier Ring",
                metalTier = tier,
                reqLevel = baseLvl + 1,
                xpReward = multXp * 1,
                iconEmoji = "💍",
                barItemId = barId,
                barsRequired = 1,
                outputItemId = "item_${prefix}_ring",
                outputItemName = "$tier Ring",
                equipSlot = EquipmentSlot.RING,
                combatPower = when (tier) { "Bronze" -> 3; "Iron" -> 6; "Silver" -> 10; "Steel" -> 14; "Gold" -> 18; "Opalite", "Mithril" -> 24; "Amethyst", "Adamant" -> 32; "Aetherite", "Rune" -> 42; else -> 3 },
                defPower = when (tier) { "Bronze" -> 3; "Iron" -> 5; "Silver" -> 8; "Steel" -> 12; "Gold" -> 16; "Opalite", "Mithril" -> 20; "Amethyst", "Adamant" -> 26; "Aetherite", "Rune" -> 34; else -> 3 },
                description = "Finely crafted $tier band granting balanced combat and defense."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_amulet",
                name = "$tier Amulet",
                metalTier = tier,
                reqLevel = baseLvl + 2,
                xpReward = multXp * 1,
                iconEmoji = "📿",
                barItemId = barId,
                barsRequired = 1,
                outputItemId = "item_${prefix}_amulet",
                outputItemName = "$tier Amulet",
                equipSlot = EquipmentSlot.AMULET,
                combatPower = when (tier) { "Bronze" -> 4; "Iron" -> 7; "Silver" -> 11; "Steel" -> 15; "Gold" -> 20; "Opalite", "Mithril" -> 26; "Amethyst", "Adamant" -> 35; "Aetherite", "Rune" -> 46; else -> 4 },
                defPower = when (tier) { "Bronze" -> 4; "Iron" -> 6; "Silver" -> 9; "Steel" -> 13; "Gold" -> 17; "Opalite", "Mithril" -> 22; "Amethyst", "Adamant" -> 28; "Aetherite", "Rune" -> 36; else -> 4 },
                description = "Polished $tier pendant imbued with warrior strength."
            ),
            SmithAnvilRecipe(
                id = "smith_${prefix}_arrowtips",
                name = "$tier Arrowtips (15x)",
                metalTier = tier,
                reqLevel = baseLvl,
                xpReward = multXp * 1,
                iconEmoji = "🗡️",
                barItemId = barId,
                barsRequired = 1,
                outputItemId = "item_${prefix}_arrowtip",
                outputItemName = "$tier Arrowtips",
                outputQuantity = 15,
                description = "Forge 1 bar into 15 sharp $tier Arrowtips for Fletching!"
            )
        )
    }
}
