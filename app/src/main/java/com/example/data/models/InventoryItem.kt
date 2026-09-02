package com.example.data.models

data class EquipmentLoadout(
    val id: String,
    val name: String,
    val items: Map<EquipmentSlot, String>
)

enum class ItemCategory {
    FOOD,
    TOY,
    POTION,
    SKILL_TOOL,
    EQUIPMENT,
    BONES,
    CONSTRUCTION,
    SEEDS,
    RUNES_MAGIC,
    LOGS_WOOD,
    BARS_ORES,
    HERBS_FARMING,
    MISC
}

data class InventoryItem(
    val id: String,
    val name: String,
    val category: ItemCategory,
    val iconEmoji: String,
    val description: String,
    val costGp: Long,
    val healHp: Int = 0,
    val restoreHunger: Int = 0,
    val addHappiness: Int = 0,
    val bonusXpSkill: OsrsSkill? = null,
    val bonusXpAmount: Long = 0L,
    val quantity: Int = 1,
    val equipmentSlot: EquipmentSlot? = null,
    val combatPowerBonus: Int = 0,
    val defPowerBonus: Int = 0
) {
    val isRawUncookedFood: Boolean
        get() = id.startsWith("item_raw_") ||
                id.contains("raw_") ||
                name.startsWith("Raw ", ignoreCase = true) ||
                name.contains("Raw", ignoreCase = true) ||
                id == "item_raw_shrimps" ||
                id == "item_raw_trout" ||
                id == "item_raw_salmon" ||
                id == "item_raw_lobster" ||
                id == "item_raw_swordfish" ||
                id == "item_raw_shark" ||
                id == "item_raw_bird_meat"

    val isCookedReadyToEatFood: Boolean
        get() {
            if (id == "item_trough_slosh" || name.contains("Trough Slosh", ignoreCase = true)) return false
            val isFood = category == ItemCategory.FOOD || restoreHunger > 0 || healHp > 0
            val isBurnt = id.startsWith("item_burnt_") || name.startsWith("Burnt ", ignoreCase = true) || name.contains("Burnt", ignoreCase = true)
            return isFood && !isRawUncookedFood && !isBurnt
        }
}

object DefaultItems {
    val ALL get() = ALL_SHOP_ITEMS
    val ALL_SHOP_ITEMS = listOf(
        InventoryItem(
            id = "item_bread",
            name = "Fresh Bread",
            category = ItemCategory.FOOD,
            iconEmoji = "🍞",
            description = "Basic baked food. Restores 5 HP & 15 Hunger.",
            costGp = 10L,
            healHp = 5,
            restoreHunger = 15,
            addHappiness = 5,
            bonusXpSkill = OsrsSkill.COOKING,
            bonusXpAmount = 50L
        ),
        InventoryItem(
            id = "item_trout",
            name = "Cooked Trout",
            category = ItemCategory.FOOD,
            iconEmoji = "🐟",
            description = "Restores 7 HP & 20 Hunger. River fishing delicacy!",
            costGp = 25L,
            healHp = 7,
            restoreHunger = 20,
            addHappiness = 8,
            bonusXpSkill = OsrsSkill.COOKING,
            bonusXpAmount = 80L
        ),
        InventoryItem(
            id = "item_salmon",
            name = "Cooked Salmon",
            category = ItemCategory.FOOD,
            iconEmoji = "🐟",
            description = "Restores 9 HP & 22 Hunger. Fresh river cooked salmon!",
            costGp = 35L,
            healHp = 9,
            restoreHunger = 22,
            addHappiness = 10,
            bonusXpSkill = OsrsSkill.COOKING,
            bonusXpAmount = 90L
        ),
        InventoryItem(
            id = "item_lobster",
            name = "Cooked Lobster",
            category = ItemCategory.FOOD,
            iconEmoji = "🦞",
            description = "Restores 12 HP & 25 Hunger. Classic OSRS food!",
            costGp = 50L,
            healHp = 12,
            restoreHunger = 25,
            addHappiness = 12,
            bonusXpSkill = OsrsSkill.COOKING,
            bonusXpAmount = 120L
        ),
        InventoryItem(
            id = "item_swordfish",
            name = "Cooked Swordfish",
            category = ItemCategory.FOOD,
            iconEmoji = "🗡️",
            description = "Restores 14 HP & 30 Hunger. High tier warrior food!",
            costGp = 90L,
            healHp = 14,
            restoreHunger = 30,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.COOKING,
            bonusXpAmount = 160L
        ),
        InventoryItem(
            id = "item_shark",
            name = "Cooked Shark",
            category = ItemCategory.FOOD,
            iconEmoji = "🦈",
            description = "Restores 20 HP & 40 Hunger. High-tier skilling food!",
            costGp = 150L,
            healHp = 20,
            restoreHunger = 40,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.COOKING,
            bonusXpAmount = 210L
        ),
        InventoryItem(
            id = "item_manta_ray",
            name = "Cooked Manta Ray",
            category = ItemCategory.FOOD,
            iconEmoji = "🪸",
            description = "Restores 22 HP & 50 Hunger. Rare deep ocean feast!",
            costGp = 250L,
            healHp = 22,
            restoreHunger = 50,
            addHappiness = 25,
            bonusXpSkill = OsrsSkill.COOKING,
            bonusXpAmount = 300L
        ),
        // CAULDRON RECIPE MEALS
        InventoryItem(
            id = "item_shrimp_stew",
            name = "Spirit Shrimp Stew",
            category = ItemCategory.FOOD,
            iconEmoji = "🍲",
            description = "Cauldron recipe meal. Restores 15 HP & 20 Hunger + Adventuring Buff (+10% Atk, +5% Def)!",
            costGp = 40L,
            healHp = 15,
            restoreHunger = 20,
            addHappiness = 15
        ),
        InventoryItem(
            id = "item_trout_elixir",
            name = "Trout Herb Elixir",
            category = ItemCategory.FOOD,
            iconEmoji = "🍵",
            description = "Cauldron recipe meal. Restores 30 HP & 35 Hunger + Adventuring Buff (+15% Atk, +10% Drop GP)!",
            costGp = 80L,
            healHp = 30,
            restoreHunger = 35,
            addHappiness = 20
        ),
        InventoryItem(
            id = "item_salmon_tonic",
            name = "Salmon Shaman Tonic",
            category = ItemCategory.FOOD,
            iconEmoji = "🧪",
            description = "Cauldron recipe meal. Restores 50 HP & 50 Hunger + Adventuring Buff (+20% Dmg, +15% XP)!",
            costGp = 120L,
            healHp = 50,
            restoreHunger = 50,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_lobster_chowder",
            name = "Flame Lobster Chowder",
            category = ItemCategory.FOOD,
            iconEmoji = "🫕",
            description = "Cauldron recipe meal. Restores 75 HP & 70 Hunger + Adventuring Buff (+25% Crit, +20% Def)!",
            costGp = 180L,
            healHp = 75,
            restoreHunger = 70,
            addHappiness = 30
        ),
        InventoryItem(
            id = "item_swordfish_brew",
            name = "Totem Swordfish Brew",
            category = ItemCategory.FOOD,
            iconEmoji = "🍵",
            description = "Cauldron recipe meal. Restores 100 HP & 85 Hunger + Adventuring Buff (+30% Drop GP, +20% Atk)!",
            costGp = 280L,
            healHp = 100,
            restoreHunger = 85,
            addHappiness = 35
        ),
        InventoryItem(
            id = "item_shark_stew",
            name = "Astral Shark Stew",
            category = ItemCategory.FOOD,
            iconEmoji = "🥣",
            description = "Cauldron recipe meal. Restores 140 HP & 100 Hunger + Adventuring Buff (+35% Combat, +25% XP)!",
            costGp = 450L,
            healHp = 140,
            restoreHunger = 100,
            addHappiness = 40
        ),
        InventoryItem(
            id = "item_void_nectar",
            name = "Sovereign Void Nectar",
            category = ItemCategory.FOOD,
            iconEmoji = "🍷",
            description = "Cauldron recipe meal. Restores 200 HP & 100 Hunger + Adventuring Buff (+50% Combat, +50% Drop GP)!",
            costGp = 750L,
            healHp = 200,
            restoreHunger = 100,
            addHappiness = 50
        ),
        InventoryItem(
            id = "item_purple_sweets",
            name = "Purple Sweets",
            category = ItemCategory.FOOD,
            iconEmoji = "🍬",
            description = "Restores 10 Energy & 10 Hunger. Stackable treat!",
            costGp = 80L,
            healHp = 5,
            restoreHunger = 10,
            addHappiness = 25,
            bonusXpSkill = OsrsSkill.AGILITY,
            bonusXpAmount = 150L
        ),
        InventoryItem(
            id = "item_saradomin_brew",
            name = "Celestial Nectar",
            category = ItemCategory.POTION,
            iconEmoji = "🧪",
            description = "Boosts Warding and restores massive HP!",
            costGp = 300L,
            healHp = 30,
            restoreHunger = 15,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 300L
        ),
        InventoryItem(
            id = "item_prayer_potion",
            name = "Divinity Nectar",
            category = ItemCategory.POTION,
            iconEmoji = "🧪",
            description = "Restores divine energy and mindfulness!",
            costGp = 200L,
            healHp = 10,
            restoreHunger = 10,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.MAGIC,
            bonusXpAmount = 200L
        ),
        InventoryItem(
            id = "item_sustaining_feast_brew",
            name = "Sustaining Feast Brew",
            category = ItemCategory.POTION,
            iconEmoji = "🍲",
            description = "High-level herbal elixir! Restores hunger over time (+60 Hunger & halts hunger decay for 2 hours)!",
            costGp = 450L,
            healHp = 20,
            restoreHunger = 60,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 400L
        ),
        InventoryItem(
            id = "item_swift_expedition_elixir",
            name = "Swift Expedition Elixir",
            category = ItemCategory.POTION,
            iconEmoji = "⏱️",
            description = "High-level herbal elixir! Reduces duration of all missions and quests by -20% for 8 hours!",
            costGp = 600L,
            healHp = 15,
            restoreHunger = 15,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 500L
        ),
        InventoryItem(
            id = "item_vitality_infusion_potion",
            name = "Eternal Vitality Infusion",
            category = ItemCategory.POTION,
            iconEmoji = "💖",
            description = "Pinnacle Herbalism infusion! Restores +50 HP & +50 Hunger with continuous health regeneration!",
            costGp = 800L,
            healHp = 50,
            restoreHunger = 50,
            addHappiness = 30,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 700L
        ),
        InventoryItem(
            id = "item_toy_mouse",
            name = "Clockwork Mouse",
            category = ItemCategory.TOY,
            iconEmoji = "🐭",
            description = "Wind it up and watch your pet chase it around!",
            costGp = 100L,
            addHappiness = 35,
            bonusXpSkill = OsrsSkill.AGILITY,
            bonusXpAmount = 250L
        ),
        InventoryItem(
            id = "item_rubber_chicken",
            name = "Rubber Chicken",
            category = ItemCategory.TOY,
            iconEmoji = "🐔",
            description = "Squeak squeak! Endless fun for your pet companion.",
            costGp = 200L,
            addHappiness = 50,
            bonusXpSkill = OsrsSkill.ATTACK,
            bonusXpAmount = 300L
        ),
        InventoryItem(
            id = "item_logs",
            name = "Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Standard timber logs chopped from normal trees. Burn at Campfire or convert to Planks at Sawmill!",
            costGp = 20L,
            addHappiness = 10,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 100L
        ),
        InventoryItem(
            id = "item_oak_logs",
            name = "Oak Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Sturdy oak timber logs. Burn at Campfire or convert to Oak Planks at Sawmill!",
            costGp = 60L,
            addHappiness = 12,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 200L
        ),
        InventoryItem(
            id = "item_plank",
            name = "Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Sawn wooden plank used for Construction building in POH!",
            costGp = 180L,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 120L
        ),
        InventoryItem(
            id = "item_oak_plank",
            name = "Oak Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "High quality oak plank used for building advanced POH furniture!",
            costGp = 460L,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 250L
        ),
        InventoryItem(
            id = "item_raw_trout",
            name = "Raw Trout",
            category = ItemCategory.FOOD,
            iconEmoji = "🐟",
            description = "Freshly caught raw trout! Cook at a range or campfire before eating.",
            costGp = 15L,
            healHp = 0,
            restoreHunger = 0,
            addHappiness = 0,
            bonusXpSkill = OsrsSkill.FISHING,
            bonusXpAmount = 50L
        ),
        InventoryItem(
            id = "item_raw_lobster",
            name = "Raw Lobster",
            category = ItemCategory.FOOD,
            iconEmoji = "🦞",
            description = "Freshly caught raw lobster! Cook at a range or campfire before eating.",
            costGp = 40L,
            healHp = 0,
            restoreHunger = 0,
            addHappiness = 0,
            bonusXpSkill = OsrsSkill.FISHING,
            bonusXpAmount = 120L
        ),
        InventoryItem(
            id = "item_raw_shark",
            name = "Raw Shark",
            category = ItemCategory.FOOD,
            iconEmoji = "🦈",
            description = "Massive raw shark! Cook at a range or campfire before eating.",
            costGp = 120L,
            healHp = 0,
            restoreHunger = 0,
            addHappiness = 0,
            bonusXpSkill = OsrsSkill.FISHING,
            bonusXpAmount = 220L
        ),
        InventoryItem(
            id = "item_birch_timber",
            name = "Birch Timber",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Silvery birch timber logs chopped from Birch groves in the Sylvan Canopy.",
            costGp = 45L,
            addHappiness = 11,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 160L
        ),
        InventoryItem(
            id = "item_birch_logs",
            name = "Birch Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Silvery birch timber logs chopped at Level 10 Woodcutting.",
            costGp = 40L,
            addHappiness = 11,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 150L
        ),
        InventoryItem(
            id = "item_teak_logs",
            name = "Teak Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Dense tropical teak logs prized for high-tier carpentry and sawmill planks.",
            costGp = 110L,
            addHappiness = 13,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 240L
        ),
        InventoryItem(
            id = "item_pine_logs",
            name = "Pine Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🌲",
            description = "Aromatic pine logs chopped at Level 25 Woodcutting.",
            costGp = 90L,
            addHappiness = 12,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 220L
        ),
        InventoryItem(
            id = "item_cedar_timber",
            name = "Mountain Cedar",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Fragrant mountain cedar timber prized by master whittlers and carpenters.",
            costGp = 165L,
            addHappiness = 14,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 310L
        ),
        InventoryItem(
            id = "item_cedar_logs",
            name = "Cedar Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Sturdy mountain cedar logs chopped at Level 40 Woodcutting.",
            costGp = 160L,
            addHappiness = 14,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 300L
        ),
        InventoryItem(
            id = "item_ironwood_timber",
            name = "Metallic Ironwood",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🗡️",
            description = "Dense, metallic ironwood timber hard as forged steel, chopped from ancient ironwood trees.",
            costGp = 360L,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 420L
        ),
        InventoryItem(
            id = "item_ironwood_logs",
            name = "Ironwood Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Dense, metallic ironwood timber chopped from Ironwood Trees at Level 75 Woodcutting.",
            costGp = 350L,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 400L
        ),
        InventoryItem(
            id = "item_magic_logs",
            name = "Magic Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🔮",
            description = "Arcane logs glowing with blue mystic mana along their bark.",
            costGp = 480L,
            addHappiness = 18,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 550L
        ),
        InventoryItem(
            id = "item_elder_bark",
            name = "Elder Bark",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "✨",
            description = "Legendary ancient bark harvested from colossal Redwood and Elder trees.",
            costGp = 600L,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 700L
        ),
        InventoryItem(
            id = "item_willow_logs",
            name = "Willow Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Pliable willow logs chopped with a Steel Axe.",
            costGp = 120L,
            addHappiness = 13,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 250L
        ),
        InventoryItem(
            id = "item_maple_logs",
            name = "Maple Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Rich maple timber chopped with a Mithril Axe.",
            costGp = 200L,
            addHappiness = 14,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 350L
        ),
        InventoryItem(
            id = "item_yew_logs",
            name = "Yew Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Dense yew logs chopped with an Adamant Axe.",
            costGp = 280L,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 500L
        ),
        InventoryItem(
            id = "item_redwood_logs",
            name = "Redwood Logs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪵",
            description = "Massive ancient redwood logs chopped with a Dragon Axe.",
            costGp = 500L,
            addHappiness = 18,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 750L
        ),
        InventoryItem(
            id = "item_birch_plank",
            name = "Birch Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Smooth birch plank milled at the Sawmill for POH Hut-Keeping.",
            costGp = 300L,
            addHappiness = 16,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 180L
        ),
        InventoryItem(
            id = "item_pine_plank",
            name = "Pine Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Durable pine plank milled at the Sawmill.",
            costGp = 420L,
            addHappiness = 18,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 220L
        ),
        InventoryItem(
            id = "item_cedar_plank",
            name = "Cedar Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "High grade cedar plank used for advanced crafting.",
            costGp = 600L,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 300L
        ),
        InventoryItem(
            id = "item_ironwood_plank",
            name = "Ironwood Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Unbreakable ironwood plank milled from Ironwood Logs.",
            costGp = 1200L,
            addHappiness = 25,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 500L
        ),
        InventoryItem(
            id = "item_willow_plank",
            name = "Willow Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Flexible willow plank milled from Willow Logs.",
            costGp = 550L,
            addHappiness = 19,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 260L
        ),
        InventoryItem(
            id = "item_maple_plank",
            name = "Maple Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Rich amber maple plank milled from Maple Logs.",
            costGp = 750L,
            addHappiness = 22,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 350L
        ),
        InventoryItem(
            id = "item_yew_plank",
            name = "Yew Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Dense ancient yew plank milled from Yew Logs.",
            costGp = 950L,
            addHappiness = 24,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 450L
        ),
        InventoryItem(
            id = "item_magic_plank",
            name = "Magic Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Arcane-infused magic wood plank milled from Magic Logs.",
            costGp = 1500L,
            addHappiness = 28,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 600L
        ),
        InventoryItem(
            id = "item_redwood_plank",
            name = "Redwood Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Massive giant redwood plank milled from Redwood Timber.",
            costGp = 2000L,
            addHappiness = 30,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 750L
        ),
        InventoryItem(
            id = "item_spirit_plank",
            name = "Spirit Redwood Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Spiritual redwood plank milled from Spirit Redwood.",
            costGp = 2500L,
            addHappiness = 32,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 900L
        ),
        InventoryItem(
            id = "item_astral_plank",
            name = "Astral Oak Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Cosmic astral wood plank milled from Astral Bark Oak.",
            costGp = 3000L,
            addHappiness = 35,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 1100L
        ),
        InventoryItem(
            id = "item_sunfire_plank",
            name = "Sunfire Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Searing sunfire plank milled from Sunfire Baobab Logs.",
            costGp = 3600L,
            addHappiness = 38,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 1300L
        ),
        InventoryItem(
            id = "item_emberwood_plank",
            name = "Emberwood Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Smoldering emberwood plank milled from Emberwood Trunks.",
            costGp = 4200L,
            addHappiness = 40,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 1500L
        ),
        InventoryItem(
            id = "item_obsidian_plank",
            name = "Obsidian Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Fireproof obsidian wood plank milled from Obsidian Baobab Bark.",
            costGp = 5000L,
            addHappiness = 44,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 1800L
        ),
        InventoryItem(
            id = "item_celestial_plank",
            name = "Celestial Yew Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Divine celestial plank milled from Celestial Yew Logs.",
            costGp = 6000L,
            addHappiness = 48,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 2200L
        ),
        InventoryItem(
            id = "item_cosmic_plank",
            name = "Cosmic Redwood Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Deep cosmic redwood plank milled from Cosmic Redwood.",
            costGp = 7500L,
            addHappiness = 52,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 2600L
        ),
        InventoryItem(
            id = "item_golden_spirit_plank",
            name = "Golden Spirit Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Supreme World Tree plank milled from Golden Spirit Trunks.",
            costGp = 10000L,
            addHappiness = 60,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 3200L
        ),
        InventoryItem(
            id = "item_nails",
            name = "Bronze Nails",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🔩",
            description = "Essential hardware for POH Construction and carpentry! Crafted from Bronze Bars.",
            costGp = 5L
        ),
        InventoryItem(
            id = "item_wooden_stick",
            name = "Wooden Stick",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥢",
            description = "Wooden sticks crafted from logs. Used as arrow shafts for fletching!",
            costGp = 5L,
            bonusXpSkill = OsrsSkill.FLETCHING,
            bonusXpAmount = 15L
        ),
        InventoryItem(
            id = "item_arrowtip",
            name = "Arrowtip",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🗡️",
            description = "Forged metal arrowtip. Combined with wooden sticks to fletch arrows!",
            costGp = 12L,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 25L
        ),
        InventoryItem(
            id = "item_shortbow",
            name = "Shortbow",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            iconEmoji = "🏹",
            description = "A reliable wooden shortbow required for Ranged combat attacks!",
            costGp = 120L,
            combatPowerBonus = 12
        ),
        InventoryItem(
            id = "item_bronze_arrows",
            name = "Bronze Arrows",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMMO,
            iconEmoji = "➹",
            description = "Ammunition required for firing bows in Ranged combat attacks!",
            costGp = 8L,
            combatPowerBonus = 8
        ),
        InventoryItem(
            id = "item_iron_arrows",
            name = "Iron Arrows",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMMO,
            iconEmoji = "➹",
            description = "Iron tipped arrows for higher damage Ranged attacks!",
            costGp = 16L,
            combatPowerBonus = 12
        ),
        InventoryItem(
            id = "item_steel_arrows",
            name = "Steel Arrows",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMMO,
            iconEmoji = "➹",
            description = "Steel tipped arrows crafted for sharp precision!",
            costGp = 32L,
            combatPowerBonus = 18
        ),
        InventoryItem(
            id = "item_mithril_arrows",
            name = "Opalite Arrows",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMMO,
            iconEmoji = "➹",
            description = "Opalite arrows gleaming with lightweight metal!",
            costGp = 64L,
            combatPowerBonus = 26
        ),
        InventoryItem(
            id = "item_adamant_arrows",
            name = "Amethyst Arrows",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMMO,
            iconEmoji = "➹",
            description = "Heavy amethyst arrows that pierce thick armor!",
            costGp = 128L,
            combatPowerBonus = 36
        ),
        InventoryItem(
            id = "item_rune_arrows",
            name = "Aetherite Arrows",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMMO,
            iconEmoji = "➹",
            description = "Deadly aetherite arrows forged for ultimate Ranged power!",
            costGp = 280L,
            combatPowerBonus = 50
        ),
        InventoryItem(
            id = "item_iron_nails",
            name = "Iron Nails",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🔩",
            description = "Sturdy iron nails for building furniture and houses. Crafted from Iron Bars.",
            costGp = 10L
        ),
        InventoryItem(
            id = "item_steel_nails",
            name = "Steel Nails",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🔩",
            description = "Durable steel nails for high quality furniture. Crafted from Steel Bars.",
            costGp = 20L
        ),
        InventoryItem(
            id = "item_mithril_nails",
            name = "Opalite Nails",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🔩",
            description = "High tier opalite nails for specialized construction. Crafted from Opalite Bars.",
            costGp = 40L
        ),
        InventoryItem(
            id = "item_adamant_nails",
            name = "Amethyst Nails",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🔩",
            description = "Heavy duty amethyst nails. Crafted from Amethyst Bars.",
            costGp = 80L
        ),
        InventoryItem(
            id = "item_rune_nails",
            name = "Aetherite Nails",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🔩",
            description = "Masterwork aetherite nails forged with spirit runes. Strongest construction nails in the realm!",
            costGp = 150L
        ),
        // Hunter Traps
        InventoryItem(
            id = "item_bird_snare",
            name = "Spirit Bird Snare",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪤",
            description = "Essential Hunter trap required to capture Crimson Swifts & Songbirds.",
            costGp = 20L
        ),
        InventoryItem(
            id = "item_net_trap",
            name = "Spirit Net Snare",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪤",
            description = "Rope & Woven Net required to trap salamanders and fen creatures.",
            costGp = 50L
        ),
        InventoryItem(
            id = "item_box_trap",
            name = "Primal Box Trap",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪤",
            description = "Heavy mesh trap required to capture swift spirit chinchompas!",
            costGp = 80L
        ),
        InventoryItem(
            id = "item_noose_wand",
            name = "Shamanic Tracking Wand",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪤",
            description = "Tracking wand required to track and commune with sacred forest beasts.",
            costGp = 120L
        ),
        InventoryItem(
            id = "item_impling_net",
            name = "Spirit Wisp Net & Vessel",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪤",
            description = "Ethereal net and enchanted spirit vessels to capture celestial wisps.",
            costGp = 250L
        ),
        // Hunter Drops
        InventoryItem(
            id = "item_feather",
            name = "Feather",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🪶",
            description = "Colorful bird feathers used in fly fishing and fletching arrows.",
            costGp = 5L
        ),
        // Fletching & Archery Crafting Materials
        InventoryItem(
            id = "item_arrow_shaft",
            name = "Wooden Shafts",
            category = ItemCategory.LOGS_WOOD,
            iconEmoji = "🪵",
            description = "Smooth whittled wooden shafts ready to be fletched with feathers into headless arrows.",
            costGp = 2L
        ),
        InventoryItem(
            id = "item_headless_arrow",
            name = "Headless Arrows",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🪶",
            description = "Fletched arrow shafts with feathers, ready to attach sharp metal arrowtips.",
            costGp = 8L
        ),
        InventoryItem(
            id = "item_bowstring",
            name = "Bowstring",
            category = ItemCategory.MISC,
            iconEmoji = "🧵",
            description = "Durable spun flax bowstring used to string unstrung wooden bows.",
            costGp = 35L
        ),
        InventoryItem(
            id = "item_bronze_arrowtip",
            name = "Bronze Arrowtips",
            category = ItemCategory.BARS_ORES,
            iconEmoji = "🗡️",
            description = "Sharpened bronze arrowtips forged from bronze bars at a smithing anvil.",
            costGp = 5L
        ),
        InventoryItem(
            id = "item_iron_arrowtip",
            name = "Iron Arrowtips",
            category = ItemCategory.BARS_ORES,
            iconEmoji = "🗡️",
            description = "Pointed iron arrowtips forged from iron bars.",
            costGp = 10L
        ),
        InventoryItem(
            id = "item_steel_arrowtip",
            name = "Steel Arrowtips",
            category = ItemCategory.BARS_ORES,
            iconEmoji = "🗡️",
            description = "Forged steel arrowtips for crafting durable steel arrows.",
            costGp = 20L
        ),
        InventoryItem(
            id = "item_mithril_arrowtip",
            name = "Opalite Arrowtips",
            category = ItemCategory.BARS_ORES,
            iconEmoji = "🗡️",
            description = "Lightweight opalite arrowtips for crafting opalite arrows.",
            costGp = 40L
        ),
        InventoryItem(
            id = "item_adamant_arrowtip",
            name = "Amethyst Arrowtips",
            category = ItemCategory.BARS_ORES,
            iconEmoji = "🗡️",
            description = "Heavy amethyst arrowtips for crafting amethyst arrows.",
            costGp = 80L
        ),
        InventoryItem(
            id = "item_rune_arrowtip",
            name = "Aetherite Arrowtips",
            category = ItemCategory.BARS_ORES,
            iconEmoji = "🗡️",
            description = "Masterwork aetherite arrowtips for crafting aetherite arrows.",
            costGp = 200L
        ),
        InventoryItem(
            id = "item_dragon_arrowtip",
            name = "Dragon Arrowtips",
            category = ItemCategory.BARS_ORES,
            iconEmoji = "🐉",
            description = "Rare dragon metal arrowtips for crafting lethal dragon arrows.",
            costGp = 600L
        ),
        InventoryItem(
            id = "item_bronze_arrows",
            name = "Bronze Arrows",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "Basic bronze arrows for beginner archers.",
            costGp = 5L,
            equipmentSlot = EquipmentSlot.AMMO,
            combatPowerBonus = 3
        ),
        InventoryItem(
            id = "item_iron_arrows",
            name = "Iron Arrows",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "Sharpened iron arrows.",
            costGp = 10L,
            equipmentSlot = EquipmentSlot.AMMO,
            combatPowerBonus = 6
        ),
        InventoryItem(
            id = "item_steel_arrows",
            name = "Steel Arrows",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "Tempered steel arrows.",
            costGp = 20L,
            equipmentSlot = EquipmentSlot.AMMO,
            combatPowerBonus = 10
        ),
        InventoryItem(
            id = "item_mithril_arrows",
            name = "Opalite Arrows",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "Opalite arrows with glowing aerodynamic flight speed.",
            costGp = 40L,
            equipmentSlot = EquipmentSlot.AMMO,
            combatPowerBonus = 16
        ),
        InventoryItem(
            id = "item_adamant_arrows",
            name = "Amethyst Arrows",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "Heavy crystalline amethyst arrows that strike with immense force.",
            costGp = 80L,
            equipmentSlot = EquipmentSlot.AMMO,
            combatPowerBonus = 24
        ),
        InventoryItem(
            id = "item_rune_arrows",
            name = "Aetherite Arrows",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "High-tier aetherite arrows for deadly cosmic precision.",
            costGp = 200L,
            equipmentSlot = EquipmentSlot.AMMO,
            combatPowerBonus = 35
        ),
        InventoryItem(
            id = "item_dragon_arrows",
            name = "Dragon Arrows",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🐉",
            description = "Legendary dragon arrows crafted from primeval dragon metal.",
            costGp = 600L,
            equipmentSlot = EquipmentSlot.AMMO,
            combatPowerBonus = 48
        ),
        InventoryItem(
            id = "item_shortbow",
            name = "Shortbow",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "A standard wooden shortbow crafted from normal logs.",
            costGp = 25L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 8
        ),
        InventoryItem(
            id = "item_birch_shortbow",
            name = "Birch Shortbow",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "A flexible shortbow carved from pale birch wood.",
            costGp = 50L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 11
        ),
        InventoryItem(
            id = "item_oak_shortbow",
            name = "Oak Shortbow",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "A sturdy shortbow carved from oak logs.",
            costGp = 75L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 14
        ),
        InventoryItem(
            id = "item_pine_shortbow",
            name = "Pine Shortbow",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "A resilient shortbow crafted from aromatic pine wood.",
            costGp = 110L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 17
        ),
        InventoryItem(
            id = "item_willow_shortbow",
            name = "Willow Shortbow",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "A swift, springy shortbow carved from willow wood.",
            costGp = 150L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 20
        ),
        InventoryItem(
            id = "item_cedar_shortbow",
            name = "Cedar Shortbow",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "A balanced shortbow carved from durable cedar wood.",
            costGp = 220L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 24
        ),
        InventoryItem(
            id = "item_maple_shortbow",
            name = "Maple Shortbow",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "A fine shortbow carved from dense maple wood.",
            costGp = 300L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 28
        ),
        InventoryItem(
            id = "item_yew_shortbow",
            name = "Yew Shortbow",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🏹",
            description = "A powerful long-range bow crafted from sacred yew wood.",
            costGp = 600L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 36
        ),
        InventoryItem(
            id = "item_ironwood_shortbow",
            name = "Ironwood Shortbow",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🪵",
            description = "An ultra-durable heavy recurve bow crafted from ironwood.",
            costGp = 1200L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 48
        ),
        InventoryItem(
            id = "item_magic_shortbow",
            name = "Magic Shortbow",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🔮",
            description = "An enchanted shortbow carved from mystic magic logs with lethal rate of fire.",
            costGp = 2500L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 62
        ),
        InventoryItem(
            id = "item_redwood_shortbow",
            name = "Redwood Shortbow",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🔴",
            description = "A massive masterwork longbow carved from ancient redwood timber.",
            costGp = 6000L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 78
        ),
        InventoryItem(
            id = "item_raw_bird_meat",
            name = "Raw Bird Meat",
            category = ItemCategory.FOOD,
            iconEmoji = "🥩",
            description = "Raw bird meat from Hunter captures. Cook before eating.",
            costGp = 15L,
            restoreHunger = 0,
            healHp = 0
        ),
        InventoryItem(
            id = "item_swamp_lizard_item",
            name = "Fen Spirit Salamander",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🦎",
            description = "Live fen spirit salamander used as an elemental ranged breath weapon!",
            costGp = 200L
        ),
        InventoryItem(
            id = "item_chinchompas",
            name = "Spirit Chinchompa",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "💣",
            description = "Explosive spirit creature released in combat for multi-target ranged devastation!",
            costGp = 450L
        ),
        InventoryItem(
            id = "item_dragon_darts",
            name = "Dragon Darts",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🎯",
            description = "Deadly thrown darts forged from dragon metal.",
            costGp = 800L
        ),
        InventoryItem(
            id = "item_amulet_of_fury",
            name = "Spirit Fury Amulet",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🔮",
            description = "Ancient onyx amulet imbued with immense spiritual power.",
            costGp = 250000L,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 18,
            defPowerBonus = 18
        ),
        InventoryItem(
            id = "item_barrows_gloves",
            name = "Crypt Champion Gauntlets",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🧤",
            description = "Legendary spirit champion gauntlets imbued with ancestral blessings.",
            costGp = 100000L,
            equipmentSlot = EquipmentSlot.GLOVES,
            combatPowerBonus = 12,
            defPowerBonus = 12
        ),
        InventoryItem(
            id = "item_bones",
            name = "Bones",
            category = ItemCategory.BONES,
            iconEmoji = "🦴",
            description = "Bones dropped by slain creatures! Bury or offer at POH altar for Magic XP.",
            costGp = 50L,
            addHappiness = 2,
            bonusXpSkill = OsrsSkill.MAGIC,
            bonusXpAmount = 150L
        ),
        InventoryItem(
            id = "item_big_bones",
            name = "Big Bones",
            category = ItemCategory.BONES,
            iconEmoji = "🦴",
            description = "Large bones from giants & heavy beasts! Bury or offer at POH altar for Magic XP.",
            costGp = 200L,
            addHappiness = 5,
            bonusXpSkill = OsrsSkill.MAGIC,
            bonusXpAmount = 300L
        ),
        InventoryItem(
            id = "item_dragon_bones",
            name = "Dragon Bones",
            category = ItemCategory.BONES,
            iconEmoji = "🦴",
            description = "Bones of a slain dragon! Bury or offer at POH altar for Magic XP.",
            costGp = 500L,
            addHappiness = 10,
            bonusXpSkill = OsrsSkill.MAGIC,
            bonusXpAmount = 600L
        ),
        InventoryItem(
            id = "item_obsidian_spirit_aegis",
            name = "Obsidian Spirit Aegis",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🛡️",
            description = "Sacred volcanic shield forged in magma chambers! Imbued with primal flame.",
            costGp = 60000L,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 15,
            defPowerBonus = 25
        ),
        InventoryItem(
            id = "item_vorkaths_head",
            name = "Frost Wyvern Sovereign Trophy",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🐉",
            description = "Prized trophy from the ancient Frost Wyvern Sovereign! Used for soul quivers or POH trophies.",
            costGp = 50000L
        ),
        InventoryItem(
            id = "item_draconic_visage",
            name = "Draconic Spirit Visage",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🛡️",
            description = "Ultra-rare dragon spirit shield component! Attach to anti-dragon shield.",
            costGp = 500000L,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 25,
            defPowerBonus = 60
        ),
        InventoryItem(
            id = "item_elysian_spirit_shield",
            name = "Elysian Celestial Aegis",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🛡️",
            description = "Pinnacle divine spirit shield that absorbs incoming damage.",
            costGp = 1000000L,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 30,
            defPowerBonus = 80
        ),
        InventoryItem(
            id = "item_dragon_pickaxe",
            name = "Dragonflame Pickaxe",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "⛏️",
            description = "Top-tier mining pickaxe imbued with dragon spirit heat! +15% Mining XP.",
            costGp = 150000L,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 1500L
        ),
        InventoryItem(
            id = "item_mole_skin",
            name = "Earth Beast Pelt",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "📜",
            description = "Thick pelt of the Giant Earth Beast exchangeable for nests and herbs.",
            costGp = 3000L
        ),
        InventoryItem(
            id = "item_mole_claw",
            name = "Earth Beast Talons",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🐾",
            description = "Razor-sharp claw of the Giant Earth Beast exchangeable for bird nests.",
            costGp = 4000L
        ),
        InventoryItem(
            id = "item_zulrah_scales",
            name = "Serpent Spirit Scales",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🧪",
            description = "Venomous scales harvested from the Great Serpent Spirit to charge toxic weapons.",
            costGp = 250L
        ),
        InventoryItem(
            id = "item_primordial_crystal",
            name = "Primordial Spirit Crystal",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "💎",
            description = "Fiery red crystal from primal underworld spirits! Attaches to Dragon Boots for Primordial Boots.",
            costGp = 350000L,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 30,
            defPowerBonus = 15
        ),
        InventoryItem(
            id = "item_ultor_vestige",
            name = "Primal Berserker Vestige",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "💍",
            description = "Ancient ring remnant from primal spirits boosting Melee Strength!",
            costGp = 400000L,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 40,
            defPowerBonus = 5
        ),
        InventoryItem(
            id = "item_abyssal_whip",
            name = "Abyssal Spirit Lash",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "⚡",
            description = "Ultra rare drop from Abyssal Spirits! Strikes with fierce speed and ethereal force.",
            costGp = 50000L,
            addHappiness = 80,
            bonusXpSkill = OsrsSkill.ATTACK,
            bonusXpAmount = 2500L
        ),
        InventoryItem(
            id = "item_rune_pickaxe",
            name = "Aetherite Pickaxe",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "⛏️",
            description = "Masterwork aetherite pickaxe providing +20% bonus Mining XP!",
            costGp = 500L,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 500L
        ),
        InventoryItem(
            id = "item_dragon_axe",
            name = "Primal Dragonwood Axe",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🪓",
            description = "Top-tier woodcutting axe! Yields Redwood Logs & massive bonus XP when equipped.",
            costGp = 800L,
            addHappiness = 30,
            equipmentSlot = EquipmentSlot.AXE,
            bonusXpSkill = OsrsSkill.WOODCUTTING,
            bonusXpAmount = 750L
        ),
        InventoryItem(
            id = "item_burnt_food",
            name = "Burnt Food",
            category = ItemCategory.FOOD,
            iconEmoji = "🤢",
            description = "Burnt to a crisp! Oops... lower cooking levels burn food more often.",
            costGp = 1L,
            healHp = -1,
            restoreHunger = 0,
            addHappiness = -5,
            bonusXpSkill = OsrsSkill.COOKING,
            bonusXpAmount = 5L
        ),
        InventoryItem(
            id = "item_raw_shrimps",
            name = "Raw Shrimps",
            category = ItemCategory.FOOD,
            iconEmoji = "🦐",
            description = "Freshly netted raw shrimps. Cook at a range or campfire before eating!",
            costGp = 5L,
            healHp = 0,
            restoreHunger = 0,
            addHappiness = 0,
            bonusXpSkill = OsrsSkill.FISHING,
            bonusXpAmount = 25L
        ),
        InventoryItem(
            id = "item_raw_salmon",
            name = "Raw Salmon",
            category = ItemCategory.FOOD,
            iconEmoji = "🐟",
            description = "Freshly caught raw salmon. Cook at a range or campfire before eating!",
            costGp = 25L,
            healHp = 0,
            restoreHunger = 0,
            addHappiness = 0,
            bonusXpSkill = OsrsSkill.FISHING,
            bonusXpAmount = 80L
        ),
        InventoryItem(
            id = "item_copper_ore",
            name = "Copper Ore",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪨",
            description = "Reddish copper ore. Smelt with Tin Ore at Furnace for Bronze Bar!",
            costGp = 10L,
            addHappiness = 5,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 25L
        ),
        InventoryItem(
            id = "item_tin_ore",
            name = "Tin Ore",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪨",
            description = "Silvery tin ore. Smelt with Copper Ore at Furnace for Bronze Bar!",
            costGp = 10L,
            addHappiness = 5,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 25L
        ),
        InventoryItem(
            id = "item_iron_ore",
            name = "Iron Ore",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪨",
            description = "Dense iron ore. Smelt at Furnace for Iron or Steel Bars!",
            costGp = 25L,
            addHappiness = 10,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 40L
        ),
        InventoryItem(
            id = "item_coal_ore",
            name = "Coal",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪨",
            description = "Black coal ore. Essential fuel for smelting Steel, Opalite, Amethyst & Aetherite Bars!",
            costGp = 45L,
            addHappiness = 10,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 65L
        ),
        InventoryItem(
            id = "item_mithril_ore",
            name = "Opalite Ore",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪨",
            description = "Luminous opalite ore. Smelt with Coal at Furnace for Opalite Bar!",
            costGp = 120L,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 100L
        ),
        InventoryItem(
            id = "item_adamant_ore",
            name = "Amethyst Ore",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪨",
            description = "Hard amethyst ore. Smelt with Coal for Amethyst Bar!",
            costGp = 250L,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 150L
        ),
        InventoryItem(
            id = "item_runite_ore",
            name = "Aetherite Ore",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪨",
            description = "Ultra rare aetherite ore! Smelt with Coal at Furnace for Aetherite Bar!",
            costGp = 1200L,
            addHappiness = 40,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 250L
        ),
        InventoryItem(
            id = "item_rune_essence",
            name = "Rune Essence",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "✨",
            description = "Blank magical stone mined from the Rune Essence Mine. Used to craft magical runestones!",
            costGp = 15L,
            addHappiness = 5,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 20L
        ),
        InventoryItem(
            id = "item_pure_essence",
            name = "Pure Essence",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🔮",
            description = "High-density crystallized essence used to craft all tier Runes including high-level catalytic runes!",
            costGp = 30L,
            addHappiness = 10,
            bonusXpSkill = OsrsSkill.RUNECRAFT,
            bonusXpAmount = 25L
        ),
        // MAGICAL RUNES
        InventoryItem(
            id = "item_rune_air",
            name = "Air Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "💨",
            description = "Elemental air rune used for wind magic and basic teleports.",
            costGp = 4L,
            addHappiness = 2
        ),
        InventoryItem(
            id = "item_rune_mind",
            name = "Mind Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "🧠",
            description = "Catalytic mind rune required for strike combat spells.",
            costGp = 5L,
            addHappiness = 2
        ),
        InventoryItem(
            id = "item_rune_water",
            name = "Water Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "💧",
            description = "Elemental water rune used for water combat spells and humidify.",
            costGp = 4L,
            addHappiness = 2
        ),
        InventoryItem(
            id = "item_rune_earth",
            name = "Earth Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "🪨",
            description = "Elemental earth rune used for earth combat spells and fertile soil.",
            costGp = 4L,
            addHappiness = 2
        ),
        InventoryItem(
            id = "item_rune_fire",
            name = "Fire Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "🔥",
            description = "Elemental fire rune used for fire strike, superheat, and alchemy.",
            costGp = 5L,
            addHappiness = 2
        ),
        InventoryItem(
            id = "item_rune_body",
            name = "Body Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "🛡️",
            description = "Body rune used for defensive spells and reanimation.",
            costGp = 10L,
            addHappiness = 3
        ),
        InventoryItem(
            id = "item_rune_cosmic",
            name = "Cosmic Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "🌌",
            description = "Mystical cosmic rune used for enchanting jewelry and shadow veil.",
            costGp = 50L,
            addHappiness = 5
        ),
        InventoryItem(
            id = "item_rune_chaos",
            name = "Chaos Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "💥",
            description = "Volatile chaos rune used for bolt combat spells and ancient bursts.",
            costGp = 90L,
            addHappiness = 8
        ),
        InventoryItem(
            id = "item_rune_nature",
            name = "Nature Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "🌿",
            description = "Essential nature rune used for alchemy, superheating, and conversion.",
            costGp = 180L,
            addHappiness = 10
        ),
        InventoryItem(
            id = "item_rune_law",
            name = "Law Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "⚖️",
            description = "Mystic law rune essential for teleports and spatial distortion.",
            costGp = 240L,
            addHappiness = 12
        ),
        InventoryItem(
            id = "item_rune_death",
            name = "Death Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "💀",
            description = "Potent death rune used for blast/wave combat and ice barrage.",
            costGp = 220L,
            addHappiness = 12
        ),
        InventoryItem(
            id = "item_rune_astral",
            name = "Astral Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "✨",
            description = "Lunar astral rune used for baking, curing plants, and spellbook swaps.",
            costGp = 150L,
            addHappiness = 10
        ),
        InventoryItem(
            id = "item_rune_blood",
            name = "Blood Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "🩸",
            description = "Vampiric blood rune used for blood barrage and high level ancient magicks.",
            costGp = 400L,
            addHappiness = 15
        ),
        InventoryItem(
            id = "item_rune_soul",
            name = "Soul Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "👻",
            description = "Exalted soul rune used for grand ancient magicks and high reanimation.",
            costGp = 350L,
            addHappiness = 15
        ),
        InventoryItem(
            id = "item_rune_wrath",
            name = "Wrath Rune",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "⚡",
            description = "Ultimate wrath rune representing pure unbridled destruction.",
            costGp = 450L,
            addHappiness = 20
        ),
        // GEMSTONES
        InventoryItem(
            id = "item_uncut_sapphire",
            name = "Sapphire",
            category = ItemCategory.BARS_ORES,
            iconEmoji = "🟦",
            description = "Glittering blue gemstone found while mining!",
            costGp = 250L,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 50L
        ),
        InventoryItem(
            id = "item_uncut_emerald",
            name = "Emerald",
            category = ItemCategory.BARS_ORES,
            iconEmoji = "🟩",
            description = "Vibrant green gemstone found while mining!",
            costGp = 500L,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 100L
        ),
        InventoryItem(
            id = "item_uncut_ruby",
            name = "Ruby",
            category = ItemCategory.BARS_ORES,
            iconEmoji = "🟥",
            description = "Deep red gemstone found while mining!",
            costGp = 1000L,
            addHappiness = 30,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 200L
        ),
        InventoryItem(
            id = "item_uncut_diamond",
            name = "Diamond",
            category = ItemCategory.BARS_ORES,
            iconEmoji = "💎",
            description = "Precious brilliant diamond found while mining!",
            costGp = 2500L,
            addHappiness = 50,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 400L
        ),

        InventoryItem(
            id = "item_bronze_bar",
            name = "Bronze Bar",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🧈",
            description = "Smelted metal bar. Used for basic Smithing smithcraft!",
            costGp = 35L,
            addHappiness = 10,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 15L
        ),
        InventoryItem(
            id = "item_iron_bar",
            name = "Iron Bar",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🧈",
            description = "Smelted iron bar for Smithing weaponry and armour!",
            costGp = 75L,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 25L
        ),
        InventoryItem(
            id = "item_steel_bar",
            name = "Steel Bar",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🧈",
            description = "Forged steel bar crafted from Iron and Coal!",
            costGp = 150L,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 35L
        ),
        InventoryItem(
            id = "item_mithril_bar",
            name = "Opalite Bar",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🧈",
            description = "Luminous opalite bar forged from Opalite Ore and Coal!",
            costGp = 300L,
            addHappiness = 25,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 60L
        ),
        InventoryItem(
            id = "item_adamant_bar",
            name = "Amethyst Bar",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🧈",
            description = "Heavy amethyst bar forged from Amethyst Ore and Coal!",
            costGp = 600L,
            addHappiness = 35,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 100L
        ),
        InventoryItem(
            id = "item_rune_bar",
            name = "Aetherite Bar",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🧈",
            description = "Masterpiece aetherite bar forged from Aetherite Ore and Coal!",
            costGp = 2800L,
            addHappiness = 60,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 150L
        ),
        // ==========================================
        // BASE EQUIPMENT ITEMS (Full sets in EquipmentData)
        // ==========================================
        InventoryItem(
            id = "item_potato_seed",
            name = "Potato Seed",
            category = ItemCategory.SEEDS,
            iconEmoji = "🌱",
            description = "Farming seed. Plant in Player Owned Farm patch to grow fresh potatoes!",
            costGp = 10L,
            addHappiness = 5,
            bonusXpSkill = OsrsSkill.FARMING,
            bonusXpAmount = 15L
        ),
        InventoryItem(
            id = "item_onion_seed",
            name = "Onion Seed",
            category = ItemCategory.SEEDS,
            iconEmoji = "🌱",
            description = "Farming seed. Plant in Player Owned Farm patch to grow fresh onions!",
            costGp = 25L,
            addHappiness = 5,
            bonusXpSkill = OsrsSkill.FARMING,
            bonusXpAmount = 25L
        ),
        InventoryItem(
            id = "item_cabbage_seed",
            name = "Cabbage Seed",
            category = ItemCategory.SEEDS,
            iconEmoji = "🌱",
            description = "Farming seed. Plant in Player Owned Farm patch to grow leafy cabbage!",
            costGp = 50L,
            addHappiness = 8,
            bonusXpSkill = OsrsSkill.FARMING,
            bonusXpAmount = 40L
        ),
        InventoryItem(
            id = "item_tomato_seed",
            name = "Tomato Seed",
            category = ItemCategory.SEEDS,
            iconEmoji = "🌱",
            description = "Farming seed. Plant in Player Owned Farm patch to grow juicy tomatoes!",
            costGp = 100L,
            addHappiness = 10,
            bonusXpSkill = OsrsSkill.FARMING,
            bonusXpAmount = 70L
        ),
        InventoryItem(
            id = "item_sweetcorn_seed",
            name = "Sweetcorn Seed",
            category = ItemCategory.SEEDS,
            iconEmoji = "🌱",
            description = "Farming seed. Plant in Player Owned Farm field to grow golden sweetcorn!",
            costGp = 250L,
            addHappiness = 12,
            bonusXpSkill = OsrsSkill.FARMING,
            bonusXpAmount = 120L
        ),
        InventoryItem(
            id = "item_strawberry_seed",
            name = "Strawberry Seed",
            category = ItemCategory.SEEDS,
            iconEmoji = "🌱",
            description = "Farming seed. Plant in Player Owned Farm garden to grow ripe strawberries!",
            costGp = 500L,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.FARMING,
            bonusXpAmount = 180L
        ),
        InventoryItem(
            id = "item_watermelon_seed",
            name = "Watermelon Seed",
            category = ItemCategory.SEEDS,
            iconEmoji = "🌱",
            description = "Farming seed. Plant in Player Owned Farm patch to grow giant watermelons!",
            costGp = 1000L,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.FARMING,
            bonusXpAmount = 300L
        ),
        // FANTASY HERB SEEDS
        InventoryItem("item_greenleaf_seed", "Greenleaf Seed", ItemCategory.SEEDS, "🌿", "Herb seed. Plant in farm patch to grow Clean Greenleaf Herbs!", 150L, addHappiness = 10, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 40L),
        InventoryItem("item_meadow_mint_seed", "Meadow Mint Seed", ItemCategory.SEEDS, "🌱", "Herb seed. Plant in farm patch to grow Clean Meadow Mint!", 300L, addHappiness = 12, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 65L),
        InventoryItem("item_wild_thyme_seed", "Wild Thyme Seed", ItemCategory.SEEDS, "🍃", "Herb seed. Plant in farm patch to grow Clean Wild Thyme!", 500L, addHappiness = 15, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 110L),
        InventoryItem("item_lavender_seed", "Lavender Seed", ItemCategory.SEEDS, "🪻", "Herb seed. Plant in farm patch to grow Clean Lavender!", 800L, addHappiness = 18, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 190L),
        InventoryItem("item_sunleaf_seed", "Sunleaf Seed", ItemCategory.SEEDS, "🌿", "Valuable herb seed! Plant in farm patch to grow Clean Sunleaf Herbs!", 1500L, addHappiness = 22, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 320L),
        InventoryItem("item_ironleaf_seed", "Ironleaf Seed", ItemCategory.SEEDS, "🍃", "Herb seed. Plant in farm patch to grow Clean Ironleaf Herbs!", 2200L, addHappiness = 25, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 480L),
        InventoryItem("item_wintergreen_seed", "Wintergreen Seed", ItemCategory.SEEDS, "🌿", "Herb seed. Plant in farm patch to grow Clean Wintergreen Herbs!", 3200L, addHappiness = 28, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 680L),
        InventoryItem("item_silverleaf_seed", "Silverleaf Seed", ItemCategory.SEEDS, "🌱", "Herb seed. Plant in farm patch to grow Clean Silverleaf Herbs!", 4500L, addHappiness = 30, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 950L),
        InventoryItem("item_mystic_sage_seed", "Mystic Sage Seed", ItemCategory.SEEDS, "✨", "Master herb seed! Plant in farm patch to grow Clean Mystic Sage Herbs!", 6500L, addHappiness = 35, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 1300L),
        InventoryItem("item_moonflower_seed", "Moonflower Seed", ItemCategory.SEEDS, "🪻", "Rare herb seed! Plant in farm patch to grow Clean Moonflower Herbs!", 8000L, addHappiness = 38, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 1500L),
        InventoryItem("item_vervain_seed", "Vervain Seed", ItemCategory.SEEDS, "🌸", "Legendary herb seed! Plant in farm patch to grow Clean Vervain Herbs!", 10000L, addHappiness = 40, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 1800L),

        // TREE SEEDS
        InventoryItem("item_oak_seed", "Oak Tree Seed", ItemCategory.SEEDS, "🌳", "Tree seed. Plant in Level 25 Tree Orchard to grow Oak Trees and harvest Oak Bark!", 300L, addHappiness = 10, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 150L),
        InventoryItem("item_birch_seed", "Birch Tree Seed", ItemCategory.SEEDS, "🌳", "Tree seed. Plant in Level 25 Tree Orchard to grow Birch Trees and harvest Birch Bark!", 500L, addHappiness = 12, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 220L),
        InventoryItem("item_willow_seed", "Willow Tree Seed", ItemCategory.SEEDS, "🌳", "Tree seed. Plant in Level 25 Tree Orchard to grow Willow Trees and harvest Willow Bark!", 800L, addHappiness = 15, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 300L),
        InventoryItem("item_pine_seed", "Pine Tree Seed", ItemCategory.SEEDS, "🌲", "Tree seed. Plant in Level 25 Tree Orchard to grow Pine Trees and harvest Pine Bark!", 1200L, addHappiness = 18, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 420L),
        InventoryItem("item_maple_seed", "Maple Tree Seed", ItemCategory.SEEDS, "🍁", "Tree seed. Plant in Level 25 Tree Orchard to grow Maple Trees and harvest Maple Bark!", 1800L, addHappiness = 20, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 550L),
        InventoryItem("item_cedar_seed", "Cedar Tree Seed", ItemCategory.SEEDS, "🌲", "Tree seed. Plant in Level 25 Tree Orchard to grow Cedar Trees and harvest Cedar Bark!", 2500L, addHappiness = 25, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 700L),
        InventoryItem("item_yew_seed", "Yew Tree Seed", ItemCategory.SEEDS, "🌳", "Tree seed. Plant in Level 25 Tree Orchard to grow Yew Trees and harvest Yew Bark!", 4000L, addHappiness = 30, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 950L),
        InventoryItem("item_ironwood_seed", "Ironwood Tree Seed", ItemCategory.SEEDS, "🪵", "Tree seed. Plant in Level 25 Tree Orchard to grow Ironwood Trees and harvest Ironwood Bark!", 6500L, addHappiness = 35, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 1300L),
        InventoryItem("item_magic_seed", "Magic Tree Seed", ItemCategory.SEEDS, "✨", "Tree seed. Plant in Level 25 Tree Orchard to grow Magic Trees and harvest Magic Bark!", 10000L, addHappiness = 40, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 1800L),
        InventoryItem("item_redwood_seed", "Redwood Tree Seed", ItemCategory.SEEDS, "🪵", "High-tier tree seed. Plant in Level 25 Tree Orchard to grow Redwood Trees and harvest Redwood Bark!", 18000L, addHappiness = 50, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 2500L),

        // FRUIT TREE SEEDS
        InventoryItem("item_apple_tree_seed", "Apple Tree Seed", ItemCategory.SEEDS, "🍎", "Fruit tree seed. Plant in Tree Orchard to harvest Cooking Apples every 5 minutes!", 600L, addHappiness = 12, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 250L),
        InventoryItem("item_cherry_tree_seed", "Cherry Tree Seed", ItemCategory.SEEDS, "🍒", "Fruit tree seed. Plant in Tree Orchard to harvest Sweet Cherries every 5 minutes!", 1000L, addHappiness = 15, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 350L),
        InventoryItem("item_apricot_tree_seed", "Apricot Tree Seed", ItemCategory.SEEDS, "🍑", "Fruit tree seed. Plant in Tree Orchard to harvest Golden Apricots every 5 minutes!", 1600L, addHappiness = 18, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 480L),
        InventoryItem("item_peach_tree_seed", "Peach Tree Seed", ItemCategory.SEEDS, "🍑", "Fruit tree seed. Plant in Tree Orchard to harvest Ripe Peaches every 5 minutes!", 2500L, addHappiness = 22, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 650L),
        InventoryItem("item_palm_tree_seed", "Palm Tree Seed", ItemCategory.SEEDS, "🌴", "Fruit tree seed. Plant in Tree Orchard to harvest Papaya Fruits every 5 minutes!", 4000L, addHappiness = 28, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 850L),
        InventoryItem("item_sakura_tree_seed", "Sakura Tree Seed", ItemCategory.SEEDS, "🌸", "Fruit tree seed. Plant in Tree Orchard to harvest Sakura Blossoms every 5 minutes!", 6000L, addHappiness = 35, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 1100L),
        InventoryItem("item_coconut_tree_seed", "Coconut Tree Seed", ItemCategory.SEEDS, "🥥", "Fruit tree seed. Plant in Tree Orchard to harvest Fresh Coconuts every 5 minutes!", 9000L, addHappiness = 42, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 1450L),
        InventoryItem("item_dragonfruit_seed", "Dragonfruit Seed", ItemCategory.SEEDS, "🐉", "Exotic fruit tree seed. Plant in Tree Orchard to harvest Dragonfruits every 5 minutes!", 15000L, addHappiness = 50, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 2000L),
        InventoryItem("item_spirit_seed", "Spirit Tree Seed", ItemCategory.SEEDS, "🌀", "Ancient tree seed. Plant in Tree Orchard to harvest Spirit Essence Fruits every 5 minutes!", 25000L, addHappiness = 60, bonusXpSkill = OsrsSkill.FARMING, bonusXpAmount = 2800L),

        // TREE BARKS (Log Substitutes harvested from regular trees)
        InventoryItem("item_oak_bark", "Oak Bark", ItemCategory.SKILL_TOOL, "🪵", "Sturdy oak tree bark. Acts as a substitute for Oak Logs in fletching, sawmill plank crafting, and building!", 120L),
        InventoryItem("item_birch_bark", "Birch Bark", ItemCategory.SKILL_TOOL, "🪵", "Smooth birch tree bark. Acts as a substitute for Birch Logs!", 180L),
        InventoryItem("item_willow_bark", "Willow Bark", ItemCategory.SKILL_TOOL, "🪵", "Flexible willow tree bark. Acts as a substitute for Willow Logs!", 250L),
        InventoryItem("item_pine_bark", "Pine Bark", ItemCategory.SKILL_TOOL, "🪵", "Resinous pine tree bark. Acts as a substitute for Pine Logs!", 350L),
        InventoryItem("item_maple_bark", "Maple Bark", ItemCategory.SKILL_TOOL, "🪵", "Rich maple tree bark. Acts as a substitute for Maple Logs!", 500L),
        InventoryItem("item_cedar_bark", "Cedar Bark", ItemCategory.SKILL_TOOL, "🪵", "Aromatic cedar tree bark. Acts as a substitute for Cedar Logs!", 700L),
        InventoryItem("item_yew_bark", "Yew Bark", ItemCategory.SKILL_TOOL, "🪵", "Dense yew tree bark. Acts as a substitute for Yew Logs!", 1000L),
        InventoryItem("item_ironwood_bark", "Ironwood Bark", ItemCategory.SKILL_TOOL, "🪵", "Hardened ironwood tree bark. Acts as a substitute for Ironwood Logs!", 1400L),
        InventoryItem("item_magic_bark", "Magic Bark", ItemCategory.SKILL_TOOL, "✨", "Glowing magic tree bark. Acts as a substitute for Magic Logs!", 2000L),
        InventoryItem("item_redwood_bark", "Redwood Bark", ItemCategory.SKILL_TOOL, "🪵", "Ancient redwood tree bark. Acts as a substitute for Redwood Logs!", 3000L),

        // FRUITS (Harvested from fruit trees)
        InventoryItem("item_cooking_apple", "Cooking Apple", ItemCategory.FOOD, "🍎", "Freshly picked crisp cooking apple.", 30L, addHappiness = 5),
        InventoryItem("item_sweet_cherry", "Sweet Cherry", ItemCategory.FOOD, "🍒", "Juicy sweet cherry harvested from orchard.", 50L, addHappiness = 8),
        InventoryItem("item_golden_apricot", "Golden Apricot", ItemCategory.FOOD, "🍑", "Delicious golden apricot.", 80L, addHappiness = 10),
        InventoryItem("item_ripe_peach", "Ripe Peach", ItemCategory.FOOD, "🍑", "Sweet ripe peach.", 120L, addHappiness = 14),
        InventoryItem("item_papaya_fruit", "Papaya Fruit", ItemCategory.FOOD, "🥥", "Exotic tropical papaya fruit.", 200L, addHappiness = 18),
        InventoryItem("item_sakura_blossom", "Sakura Blossom", ItemCategory.FOOD, "🌸", "Fragrant sakura blossom petal.", 300L, addHappiness = 22),
        InventoryItem("item_coconut", "Fresh Coconut", ItemCategory.FOOD, "🥥", "Refreshing fresh coconut.", 450L, addHappiness = 28),
        InventoryItem("item_dragonfruit", "Dragonfruit", ItemCategory.FOOD, "🐉", "Exotic dragonfruit packed with vitality.", 800L, addHappiness = 35),
        InventoryItem("item_spirit_fruit", "Spirit Essence Fruit", ItemCategory.FOOD, "🌀", "Mystical spirit fruit infused with magical energy.", 1200L, addHappiness = 50),
        InventoryItem(
            id = "item_gold_ore",
            name = "Gold Ore",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪙",
            description = "Gleaming gold ore. Smelt at Furnace for Gold Bar!",
            costGp = 150L,
            addHappiness = 15,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 80L
        ),
        InventoryItem(
            id = "item_silver_ore",
            name = "Silver Ore",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥈",
            description = "Pure silver ore. Smelt at Furnace for Silver Bar!",
            costGp = 60L,
            addHappiness = 10,
            bonusXpSkill = OsrsSkill.SMITHING,
            bonusXpAmount = 50L
        ),
        InventoryItem(
            id = "item_stolen_silk",
            name = "Al Kharid Silk",
            category = ItemCategory.MISC,
            iconEmoji = "🧵",
            description = "Fine Al Kharid silk stolen from market stalls.",
            costGp = 120L
        ),
        InventoryItem(
            id = "item_lockpick",
            name = "Lockpick",
            category = ItemCategory.MISC,
            iconEmoji = "🗝️",
            description = "Essential thief tool used for cracking desert safes and doors.",
            costGp = 100L
        ),
        InventoryItem(
            id = "item_uncut_dragonstone",
            name = "Dragonstone",
            category = ItemCategory.MISC,
            iconEmoji = "🔮",
            description = "Precious purple dragonstone gem cut into Glory amulets.",
            costGp = 6000L
        ),
        InventoryItem(
            id = "item_uncut_onyx",
            name = "Onyx",
            category = ItemCategory.MISC,
            iconEmoji = "🖤",
            description = "Ultra-rare pitch-black gemstone mined from volcanic depths.",
            costGp = 60000L
        ),
        InventoryItem(
            id = "item_stolen_gemstones",
            name = "Stolen Gemstone Pouch",
            category = ItemCategory.MISC,
            iconEmoji = "👛",
            description = "Pouch containing valuable stolen gemstones.",
            costGp = 2000L
        ),
        InventoryItem(
            id = "item_green_dhide_body",
            name = "Forest Wyrm Cuirass",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🦎",
            description = "Protective green dragonhide chest armor crafted by master leatherworkers.",
            costGp = 3200L,
            addHappiness = 15
        ),
        InventoryItem(
            id = "item_blue_dhide_body",
            name = "Sky Wyrm Cuirass",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🦚",
            description = "Supple blue dragonhide armor offering high magic resistance.",
            costGp = 5600L,
            addHappiness = 20
        ),
        InventoryItem(
            id = "item_red_dhide_body",
            name = "Crimson Dragonhide Cuirass",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🐉",
            description = "High-tier red dragonhide armor harvested from fiery red dragons.",
            costGp = 8800L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_black_dhide_body",
            name = "Shadow Dragonhide Cuirass",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🖤",
            description = "Elite black dragonhide body delivering maximum ranged protection.",
            costGp = 12500L,
            addHappiness = 30
        ),
        InventoryItem(
            id = "item_mystic_robe_top",
            name = "Arcane Mystic Robe Top",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "👘",
            description = "Enchanted blue and golden silk robe top infused with arcane magic.",
            costGp = 15000L,
            addHappiness = 35
        ),
        InventoryItem(
            id = "item_mystic_robe_bottom",
            name = "Arcane Mystic Robe Bottom",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "👖",
            description = "Enchanted mystic robe skirt woven by ancient wizard guilds.",
            costGp = 12000L,
            addHappiness = 30
        ),
        InventoryItem(
            id = "item_shaman_talisman",
            name = "Ancestral Shaman Talisman",
            category = ItemCategory.MISC,
            iconEmoji = "🗿",
            description = "Sacred spirit talisman channelled by high shamans to invoke ancestral blessings.",
            costGp = 18000L,
            addHappiness = 40
        ),
        InventoryItem(
            id = "item_spirit_totem",
            name = "Sacred Spirit Totem",
            category = ItemCategory.MISC,
            iconEmoji = "🪵",
            description = "Hand-carved redwood totem infused with elemental forest spirits.",
            costGp = 25000L,
            addHappiness = 50
        ),
        InventoryItem(
            id = "item_amulet_of_glory",
            name = "Amulet of Glory",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "📿",
            description = "Enchanted dragonstone amulet granting immense luck and teleportation powers.",
            costGp = 30000L,
            addHappiness = 60
        ),
        // FARM PRODUCE
        InventoryItem(
            id = "item_potato",
            name = "Fresh Potato",
            category = ItemCategory.FOOD,
            iconEmoji = "🥔",
            description = "Freshly harvested potato from Player Owned Farm!",
            costGp = 15L,
            healHp = 5,
            restoreHunger = 12,
            addHappiness = 5
        ),
        InventoryItem(
            id = "item_onion",
            name = "Fresh Onion",
            category = ItemCategory.FOOD,
            iconEmoji = "🧅",
            description = "Freshly harvested onion from Player Owned Farm!",
            costGp = 25L,
            healHp = 6,
            restoreHunger = 15,
            addHappiness = 5
        ),
        InventoryItem(
            id = "item_cabbage",
            name = "Fresh Cabbage",
            category = ItemCategory.FOOD,
            iconEmoji = "🥬",
            description = "Freshly harvested cabbage from Player Owned Farm!",
            costGp = 40L,
            healHp = 8,
            restoreHunger = 18,
            addHappiness = 8
        ),
        InventoryItem(
            id = "item_carrot",
            name = "Crunchy Carrot",
            category = ItemCategory.FOOD,
            iconEmoji = "🥕",
            description = "Freshly harvested crunchy carrot from Player Owned Farm!",
            costGp = 50L,
            healHp = 9,
            restoreHunger = 20,
            addHappiness = 9
        ),
        InventoryItem(
            id = "item_tomato",
            name = "Juicy Tomato",
            category = ItemCategory.FOOD,
            iconEmoji = "🍅",
            description = "Freshly harvested tomato from Player Owned Farm!",
            costGp = 65L,
            healHp = 10,
            restoreHunger = 22,
            addHappiness = 10
        ),
        InventoryItem(
            id = "item_sweetcorn",
            name = "Golden Sweetcorn",
            category = ItemCategory.FOOD,
            iconEmoji = "🌽",
            description = "Freshly harvested sweetcorn from Player Owned Farm!",
            costGp = 120L,
            healHp = 15,
            restoreHunger = 30,
            addHappiness = 12
        ),
        InventoryItem(
            id = "item_strawberry",
            name = "Ripe Strawberry",
            category = ItemCategory.FOOD,
            iconEmoji = "🍓",
            description = "Freshly harvested sweet strawberry from Player Owned Farm!",
            costGp = 220L,
            healHp = 20,
            restoreHunger = 35,
            addHappiness = 18
        ),
        InventoryItem(
            id = "item_pumpkin",
            name = "Giant Pumpkin",
            category = ItemCategory.FOOD,
            iconEmoji = "🎃",
            description = "Freshly harvested giant pumpkin from Player Owned Farm!",
            costGp = 350L,
            healHp = 24,
            restoreHunger = 42,
            addHappiness = 20
        ),
        InventoryItem(
            id = "item_watermelon",
            name = "Juicy Watermelon",
            category = ItemCategory.FOOD,
            iconEmoji = "🍉",
            description = "Giant refreshing watermelon harvested from Player Owned Farm!",
            costGp = 450L,
            healHp = 28,
            restoreHunger = 50,
            addHappiness = 25
        ),
        // ALL NORMAL HERBS
        InventoryItem(
            id = "item_clean_greenleaf",
            name = "Clean Greenleaf",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🌿",
            description = "Fresh botanical greenleaf herb. Grind with a Pestle & Mortar for Herblore brewing.",
            costGp = 100L,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 50L
        ),
        InventoryItem(
            id = "item_clean_meadow_mint",
            name = "Clean Meadow Mint",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🌱",
            description = "Fresh aromatic meadow mint herb. Grind with a Pestle & Mortar for Herblore brewing.",
            costGp = 180L,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 80L
        ),
        InventoryItem(
            id = "item_clean_wild_thyme",
            name = "Clean Wild Thyme",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🌿",
            description = "Fresh earthy wild thyme herb. Grind with a Pestle & Mortar for Herblore brewing.",
            costGp = 280L,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 120L
        ),
        InventoryItem(
            id = "item_clean_lavender",
            name = "Clean Lavender",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪻",
            description = "Fresh fragrant lavender herb. Grind with a Pestle & Mortar for Herblore brewing.",
            costGp = 450L,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 180L
        ),
        InventoryItem(
            id = "item_clean_sunleaf",
            name = "Clean Sunleaf",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🌿",
            description = "Rare radiant sunleaf herb. Grind with a Pestle & Mortar for Herblore brewing.",
            costGp = 800L,
            addHappiness = 20,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 250L
        ),
        InventoryItem(
            id = "item_clean_ironleaf",
            name = "Clean Ironleaf",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🍃",
            description = "Tough metallic ironleaf herb. Grind with a Pestle & Mortar for Herblore brewing.",
            costGp = 1200L,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 350L
        ),
        InventoryItem(
            id = "item_clean_wintergreen",
            name = "Clean Wintergreen",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🌿",
            description = "Crisp frosted wintergreen herb. Grind with a Pestle & Mortar for Herblore brewing.",
            costGp = 1800L,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 450L
        ),
        InventoryItem(
            id = "item_clean_silverleaf",
            name = "Clean Silverleaf",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🌱",
            description = "Shimmering silverleaf herb. Grind with a Pestle & Mortar for Herblore brewing.",
            costGp = 2500L,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 650L
        ),
        InventoryItem(
            id = "item_clean_moonflower",
            name = "Clean Moonflower",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🪻",
            description = "Luminous celestial moonflower herb. Grind with a Pestle & Mortar for Herblore brewing.",
            costGp = 3200L,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 750L
        ),
        InventoryItem(
            id = "item_clean_vervain",
            name = "Clean Vervain",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🌸",
            description = "Delicate medicinal vervain herb. Grind with a Pestle & Mortar for Herblore brewing.",
            costGp = 3800L,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 900L
        ),
        InventoryItem(
            id = "item_clean_mystic_sage",
            name = "Clean Mystic Sage",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "✨",
            description = "Legendary mystic sage herb. Grind with a Pestle & Mortar for Herblore brewing.",
            costGp = 5000L,
            addHappiness = 30,
            bonusXpSkill = OsrsSkill.HERBLORE,
            bonusXpAmount = 1200L
        ),

        // ALL CRUSHED HERBS (From Herblore Pestle & Mortar)
        InventoryItem(
            id = "item_crushed_greenleaf",
            name = "Crushed Greenleaf",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥣",
            description = "Finely ground Greenleaf herb. Ready to brew into Warrior Elixir.",
            costGp = 120L
        ),
        InventoryItem(
            id = "item_crushed_meadow_mint",
            name = "Crushed Meadow Mint",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥣",
            description = "Finely ground Meadow Mint herb. Ready to brew into Antidote Draught.",
            costGp = 210L
        ),
        InventoryItem(
            id = "item_crushed_wild_thyme",
            name = "Crushed Wild Thyme",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥣",
            description = "Finely ground Wild Thyme herb. Ready to brew into Might Potion.",
            costGp = 320L
        ),
        InventoryItem(
            id = "item_crushed_lavender",
            name = "Crushed Lavender",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥣",
            description = "Finely ground Lavender herb. Ready to brew into Ironhide Tonic.",
            costGp = 500L
        ),
        InventoryItem(
            id = "item_crushed_sunleaf",
            name = "Crushed Sunleaf",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥣",
            description = "Finely ground Sunleaf herb. Ready to brew into Divinity Nectar.",
            costGp = 950L
        ),
        InventoryItem(
            id = "item_crushed_ironleaf",
            name = "Crushed Ironleaf",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥣",
            description = "Finely ground Ironleaf herb. Ready to brew into High Warrior Elixir.",
            costGp = 1400L
        ),
        InventoryItem(
            id = "item_crushed_wintergreen",
            name = "Crushed Wintergreen",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥣",
            description = "Finely ground Wintergreen herb. Ready to brew into Titan Might Potion.",
            costGp = 2000L
        ),
        InventoryItem(
            id = "item_crushed_silverleaf",
            name = "Crushed Silverleaf",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥣",
            description = "Finely ground Silverleaf herb. Ready to brew into Aegis Shield Tonic & Sustaining Feast.",
            costGp = 2900L
        ),
        InventoryItem(
            id = "item_crushed_moonflower",
            name = "Crushed Moonflower",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥣",
            description = "Finely ground Moonflower herb. Ready to brew into Swift Expedition Elixir.",
            costGp = 3600L
        ),
        InventoryItem(
            id = "item_crushed_vervain",
            name = "Crushed Vervain",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥣",
            description = "Finely ground Vervain herb. Ready to brew into Celestial Nectar & Hawkeye Elixir.",
            costGp = 4400L
        ),
        InventoryItem(
            id = "item_crushed_mystic_sage",
            name = "Crushed Mystic Sage",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🥣",
            description = "Finely ground Mystic Sage herb. Ready to brew into Grand Master Flask & Eternal Vitality Infusion.",
            costGp = 5800L
        ),
        // FOREST REALM OBELISKS (SHAMAN PATH)
        InventoryItem(
            id = "item_totem_woodland",
            name = "Woodland Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🪵",
            description = "Forest Realm Obelisk awarded by the Woodland Shaman!",
            costGp = 1000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_totem_mist_fen",
            name = "Mist Fen Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🐺",
            description = "Forest Realm Obelisk awarded by the Mist Fenrir Shaman!",
            costGp = 2000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_totem_ancient_crag",
            name = "Ancient Crag Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🗿",
            description = "Forest Realm Obelisk awarded by the Stone Crag Shaman!",
            costGp = 3000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_totem_sacred_grove",
            name = "Sacred Grove Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🌳",
            description = "Forest Realm Obelisk awarded by the Grove Shaman!",
            costGp = 4000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_totem_ember_spirit",
            name = "Ember Spirit Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🔥",
            description = "Forest Realm Obelisk awarded by the Flame Shaman!",
            costGp = 5000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_totem_celestial_canopy",
            name = "Celestial Canopy Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🌌",
            description = "Forest Realm Obelisk awarded by the Sky Canopy Shaman!",
            costGp = 6000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_totem_astral_bloom",
            name = "Astral Bloom Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🌺",
            description = "Forest Realm Obelisk awarded by the Astral Shaman!",
            costGp = 7000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_totem_sovereign_wild",
            name = "Sovereign Wild Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🦌",
            description = "Forest Realm Obelisk awarded by the Sovereign Forest Shaman!",
            costGp = 8000L,
            addHappiness = 25
        ),
        // REGIONAL SHAMAN PATH OBELISKS
        InventoryItem(
            id = "item_badge_boulder",
            name = "Boulder Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🪨",
            description = "Shaman Path Obelisk awarded from the Granite Peak Trial!",
            costGp = 1000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_cascade",
            name = "Cascade Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "💧",
            description = "Shaman Path Obelisk awarded from the Cascade Waters Trial!",
            costGp = 2000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_thunder",
            name = "Thunder Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "⚡",
            description = "Shaman Path Obelisk awarded from the Thunder Spark Trial!",
            costGp = 3000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_rainbow",
            name = "Rainbow Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🌈",
            description = "Shaman Path Obelisk awarded from the Floral Vine Trial!",
            costGp = 4000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_soul",
            name = "Soul Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "💜",
            description = "Shaman Path Obelisk awarded from the Poison Fang Trial!",
            costGp = 5000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_marsh",
            name = "Marsh Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🔮",
            description = "Shaman Path Obelisk awarded from the Psychic Spiral Trial!",
            costGp = 6000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_volcano",
            name = "Volcano Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🌋",
            description = "Shaman Path Obelisk awarded from the Magma Crest Trial!",
            costGp = 7000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_earth",
            name = "Earth Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🌍",
            description = "Shaman Path Obelisk awarded from the Earth Titan Trial!",
            costGp = 8000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_zephyr",
            name = "Zephyr Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🪶",
            description = "Shaman Path Obelisk awarded from the Gale Falcon Shrine!",
            costGp = 3000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_hive",
            name = "Hive Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🪲",
            description = "Shaman Path Obelisk awarded from the Hive Mantis Shrine!",
            costGp = 4000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_plain",
            name = "Plain Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🐮",
            description = "Shaman Path Obelisk awarded from the Ironhide Boar Shrine!",
            costGp = 5000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_fog",
            name = "Fog Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "👻",
            description = "Shaman Path Obelisk awarded from the Spectral Fog Shrine!",
            costGp = 6000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_mineral",
            name = "Mineral Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "⚙️",
            description = "Shaman Path Obelisk awarded from the Steel Lighthouse Shrine!",
            costGp = 7000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_rising",
            name = "Rising Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🐲",
            description = "Shaman Path Obelisk awarded from the Dragon Den Shrine!",
            costGp = 8000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_stone",
            name = "Stone Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🪨",
            description = "Shaman Path Obelisk awarded from the Rustboro Shrine!",
            costGp = 3000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_knuckle",
            name = "Knuckle Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🥊",
            description = "Shaman Path Obelisk awarded from the Dewford Shrine!",
            costGp = 4000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_dynamo",
            name = "Dynamo Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "⚡",
            description = "Shaman Path Obelisk awarded from the Mauville Shrine!",
            costGp = 5000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_heat",
            name = "Heat Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🔥",
            description = "Shaman Path Obelisk awarded from the Lavaridge Shrine!",
            costGp = 6000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_feather",
            name = "Feather Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🪶",
            description = "Shaman Path Obelisk awarded from the Fortree Shrine!",
            costGp = 7000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_mind",
            name = "Mind Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🔮",
            description = "Shaman Path Obelisk awarded from the Mossdeep Shrine!",
            costGp = 8000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_rain",
            name = "Rain Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "💧",
            description = "Shaman Path Obelisk awarded from the Sootopolis Shrine!",
            costGp = 9000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_coal",
            name = "Coal Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🪨",
            description = "Shaman Path Obelisk awarded from the Oreburgh Shrine!",
            costGp = 3000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_forest",
            name = "Forest Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🌿",
            description = "Shaman Path Obelisk awarded from the Eterna Shrine!",
            costGp = 4000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_cobble",
            name = "Cobble Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🥊",
            description = "Shaman Path Obelisk awarded from the Veilstone Shrine!",
            costGp = 5000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_fen",
            name = "Fen Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🌊",
            description = "Shaman Path Obelisk awarded from the Pastoria Shrine!",
            costGp = 6000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_relic",
            name = "Relic Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "👻",
            description = "Shaman Path Obelisk awarded from the Hearthome Shrine!",
            costGp = 7000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_mine",
            name = "Mine Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "🛡️",
            description = "Shaman Path Obelisk awarded from the Canalave Shrine!",
            costGp = 8000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_icicle",
            name = "Icicle Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "❄️",
            description = "Shaman Path Obelisk awarded from the Snowpoint Shrine!",
            costGp = 9000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_beacon",
            name = "Beacon Obelisk",
            category = ItemCategory.TOY,
            iconEmoji = "⚡",
            description = "Shaman Path Obelisk awarded from the Sunyshore Shrine!",
            costGp = 10000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_badge_league",
            name = "League Badge",
            category = ItemCategory.TOY,
            iconEmoji = "👑",
            description = "The prestigious Indigo Plateau League Badge awarded for conquering the Kanto Elite Four & Champion!",
            costGp = 50000L,
            addHappiness = 50
        ),
        // FARMING SEED POUCHES
        InventoryItem(
            id = "item_seed_pouch_easy",
            name = "Easy Seed Pouch",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🎁",
            description = "Awarded from Easy Farming Guild Contracts. Open to receive random farming seeds including guaranteed rare seeds!",
            costGp = 500L,
            addHappiness = 15
        ),
        InventoryItem(
            id = "item_seed_pouch_medium",
            name = "Medium Seed Pouch",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🎁",
            description = "Awarded from Medium Farming Guild Contracts. Open to receive random farming seeds including guaranteed rare herb or tree seeds!",
            costGp = 1500L,
            addHappiness = 20
        ),
        InventoryItem(
            id = "item_seed_pouch_hard",
            name = "Hard Seed Pouch",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🎁",
            description = "Awarded from Hard Farming Guild Contracts. Open to receive high-tier farming seeds including rare tree and master herb seeds!",
            costGp = 4000L,
            addHappiness = 30
        ),
        InventoryItem(
            id = "item_seed_pouch",
            name = "Seed Pouch",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🎁",
            description = "Awarded from Farming Guild Contracts. Open to receive random farming seeds!",
            costGp = 1000L,
            addHappiness = 20
        ),
        // --- ANIMAL HUSBANDRY LIVESTOCK PRODUCE & FEED ---
        InventoryItem(
            id = "item_trough_slosh",
            name = "Trough Slosh",
            category = ItemCategory.FOOD,
            iconEmoji = "🍲",
            description = "Nutritious farm mash crafted by combining any two ingredients. Fills +150% trough hunger in Animal Husbandry! (Livestock only - pet cannot eat this)",
            costGp = 50L,
            restoreHunger = 0,
            healHp = 0
        ),
        InventoryItem(
            id = "item_egg",
            name = "Fresh Egg",
            category = ItemCategory.FOOD,
            iconEmoji = "🥚",
            description = "A freshly laid organic farm egg produced by coop chickens in Animal Husbandry.",
            costGp = 30L,
            restoreHunger = 10,
            healHp = 5
        ),
        InventoryItem(
            id = "item_wool",
            name = "Soft Wool",
            category = ItemCategory.MISC,
            iconEmoji = "🧶",
            description = "Clean sheared wool harvested from pen pasture sheep.",
            costGp = 80L
        ),
        InventoryItem(
            id = "item_bucket_of_milk",
            name = "Bucket of Milk",
            category = ItemCategory.FOOD,
            iconEmoji = "🥛",
            description = "Rich creamy farm milk collected daily from dairy cows in Animal Husbandry.",
            costGp = 60L,
            restoreHunger = 15,
            healHp = 8
        ),
        InventoryItem(
            id = "item_truffle",
            name = "Earthy Truffle",
            category = ItemCategory.FOOD,
            iconEmoji = "🍄",
            description = "A prized gourmet truffle foraged by spotted farm pigs in the pen soil.",
            costGp = 250L,
            restoreHunger = 25,
            healHp = 15
        ),
        InventoryItem(
            id = "item_alpaca_fleece",
            name = "Alpaca Fleece",
            category = ItemCategory.MISC,
            iconEmoji = "🦙",
            description = "Luxuriously soft and warm alpaca fleece gathered from the farm herd.",
            costGp = 400L
        ),
        InventoryItem(
            id = "item_yak_hair",
            name = "Yak Hair",
            category = ItemCategory.MISC,
            iconEmoji = "🐂",
            description = "Tough, weather-resistant Fremennik yak hair prized for sturdy ropes and garments.",
            costGp = 650L
        ),
        InventoryItem(
            id = "item_chinchilla_fur",
            name = "Chinchilla Fur",
            category = ItemCategory.MISC,
            iconEmoji = "🦔",
            description = "Silky, insulating chinchilla down shed by pen chinchillas in Animal Husbandry.",
            costGp = 1200L
        ),
        InventoryItem(
            id = "item_dragon_scale",
            name = "Dragon Scale",
            category = ItemCategory.MISC,
            iconEmoji = "🐉",
            description = "Gleaming flame-tempered scale naturally shed by farm drakes.",
            costGp = 2500L
        ),
        InventoryItem(
            id = "item_spirit_dust",
            name = "Spirit Horn Dust",
            category = ItemCategory.RUNES_MAGIC,
            iconEmoji = "✨",
            description = "Glistening magical dust collected from the shed horn tips of mystical spirit beasts.",
            costGp = 5000L
        ),
        // QUEST REWARD UNIQUE ITEMS & SPECIAL GEAR
        InventoryItem(
            id = "item_arclite",
            name = "Spirit Arclite Blade",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 95,
            defPowerBonus = 20,
            iconEmoji = "🗡️",
            description = "Legendary demonbane sword forged from ancient spirit shards! Deals massive bonus damage to demonic entities.",
            costGp = 50000L,
            addHappiness = 50
        ),
        InventoryItem(
            id = "item_darklight",
            name = "Shadowbane Spirit Sword",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 60,
            defPowerBonus = 10,
            iconEmoji = "🗡️",
            description = "Enchanted spirit sword infused with shadowy light magic to weaken and slay demons.",
            costGp = 15000L,
            addHappiness = 30
        ),
        InventoryItem(
            id = "item_silverlight",
            name = "Silver Spirit Blade",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 35,
            iconEmoji = "🗡️",
            description = "Ancient silver blade empowered by ancestral blessings to purge demonic spirits.",
            costGp = 5000L,
            addHappiness = 20
        ),
        InventoryItem(
            id = "item_blurite_sword",
            name = "Frostshard Spirit Blade",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 30,
            iconEmoji = "🗡️",
            description = "Rare sword forged from icy crystalline ore deep within frozen caverns.",
            costGp = 2500L,
            addHappiness = 15
        ),
        InventoryItem(
            id = "item_ectophial",
            name = "Spirit Realm Phial",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🏺",
            description = "Mystical vessel containing ethereal spirit essence for instant sanctum teleports!",
            costGp = 3000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_avas_accumulator",
            name = "Soul Hunter Quiver",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 30,
            defPowerBonus = 15,
            iconEmoji = "🎒",
            description = "Enchanted magnetic quiver attuned to spirits that automatically retrieves launched arrows and darts!",
            costGp = 10000L,
            addHappiness = 30
        ),
        InventoryItem(
            id = "item_helm_of_neitiznot",
            name = "Crown of Sovereign Valour",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 25,
            defPowerBonus = 45,
            iconEmoji = "🪖",
            description = "Ancestral honor helm awarded to valorous island warriors. Imbued with strength & spirit!",
            costGp = 25000L,
            addHappiness = 40
        ),
        InventoryItem(
            id = "item_salve_amulet",
            name = "Ancestral Spirit Salve",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 40,
            defPowerBonus = 20,
            iconEmoji = "📿",
            description = "Blessed pendant granting +20% accuracy and damage against undead & corrupted spirits!",
            costGp = 12000L,
            addHappiness = 30
        ),
        InventoryItem(
            id = "item_ibans_staff",
            name = "Shadowflame Spirit Staff",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 65,
            defPowerBonus = 15,
            iconEmoji = "🪄",
            description = "Ancient staff channeling dark spirit flames into devastating spell blasts!",
            costGp = 20000L,
            addHappiness = 35
        ),
        InventoryItem(
            id = "item_ancient_staff",
            name = "Ancient Spirit Staff",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 70,
            defPowerBonus = 20,
            iconEmoji = "🪄",
            description = "Mage staff attuned to Ancient Spirit Magicks.",
            costGp = 35000L,
            addHappiness = 40
        ),
        InventoryItem(
            id = "item_crystal_bow",
            name = "Celestial Crystal Bow",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 85,
            iconEmoji = "🏹",
            description = "Elven bow carved from pure spiritual crystal seed. Fires shimmering spirit arrows!",
            costGp = 50000L,
            addHappiness = 45
        ),
        InventoryItem(
            id = "item_heavy_ballista",
            name = "Colossal Siege Ballista",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 110,
            iconEmoji = "🏹",
            description = "Devastating siege engine weapon crafted from ancient hardwood and spirit springs!",
            costGp = 80000L,
            addHappiness = 50
        ),
        InventoryItem(
            id = "item_mythical_cape",
            name = "Mythic Shaman Cape",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 40,
            defPowerBonus = 50,
            iconEmoji = "🧥",
            description = "Cape of the Grand Shamans Guild awarded for legendary triumphs!",
            costGp = 40000L,
            addHappiness = 45
        ),
        InventoryItem(
            id = "item_blisterwood_flail",
            name = "Bloodwood Spirit Flail",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 75,
            iconEmoji = "🏏",
            description = "Sacred arboreal flail carved from elder bloodwood to ward off vampiric shadows!",
            costGp = 30000L,
            addHappiness = 35
        ),
        InventoryItem(
            id = "item_virtus_robe_top",
            name = "Arcane Sovereign Robe Top",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 90,
            defPowerBonus = 65,
            iconEmoji = "👘",
            description = "Ancient spirit battle robes channeling profound celestial arcana!",
            costGp = 100000L,
            addHappiness = 60
        ),
        InventoryItem(
            id = "item_keris_dagger",
            name = "Desert Stinger Dagger",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 55,
            iconEmoji = "🗡️",
            description = "Curved desert dagger with poison-tipped sting. Deals massive piercing damage to chitinous beasts!",
            costGp = 8000L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_wolfbane_dagger",
            name = "Wolf Spirit Dagger",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 35,
            iconEmoji = "🗡️",
            description = "Ancestral silver dagger attuned to wolf spirits, soothing feral transformations!",
            costGp = 4000L,
            addHappiness = 20
        ),
        InventoryItem(
            id = "item_silver_sickle_b",
            name = "Blessed Druidic Sickle",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🌙",
            description = "Blessed silver sickle used for forest bloom harvests and nature rituals!",
            costGp = 3500L,
            addHappiness = 20
        ),
        InventoryItem(
            id = "item_magic_secateurs",
            name = "Enchanted Spirit Secateurs",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "✂️",
            description = "Enchanted botanical secateurs from the Fey. Boosts Farming crop yield by +10%!",
            costGp = 12000L,
            bonusXpSkill = OsrsSkill.FARMING,
            bonusXpAmount = 100L,
            addHappiness = 30
        ),
        InventoryItem(
            id = "item_holy_wrench",
            name = "Sacred Spirit Wrench",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🔧",
            description = "Sacred blessed tool that boosts magic and spiritual energy restoration!",
            costGp = 8000L,
            bonusXpSkill = OsrsSkill.MAGIC,
            bonusXpAmount = 100L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_ghostspeak_amulet",
            name = "Spirit Communion Amulet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMULET,
            defPowerBonus = 10,
            iconEmoji = "📿",
            description = "Spectral amulet allowing direct communication with wandering spirits across the realm!",
            costGp = 2000L,
            addHappiness = 20
        ),
        InventoryItem(
            id = "item_teak_plank",
            name = "Teak Spirit Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Polished hardwood plank used for high tier sanctuary construction!",
            costGp = 900L,
            addHappiness = 25,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 450L
        ),
        InventoryItem(
            id = "item_mahogany_plank",
            name = "Mahogany Sanctuary Plank",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪚",
            description = "Exquisite hardwood plank used for master sanctuary furniture and spirit altars!",
            costGp = 2200L,
            addHappiness = 35,
            bonusXpSkill = OsrsSkill.CONSTRUCTION,
            bonusXpAmount = 900L
        ),
        InventoryItem(
            id = "item_gold_leaf",
            name = "Gold Leaf",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🪙",
            description = "Pure gold leaf foil for gilding POH furniture and altars!",
            costGp = 130000L,
            addHappiness = 50
        ),
        InventoryItem(
            id = "item_marble_block",
            name = "Marble Block",
            category = ItemCategory.CONSTRUCTION,
            iconEmoji = "🧱",
            description = "Solid marble block for constructing grand statues and portals!",
            costGp = 325000L,
            addHappiness = 60
        ),
        // Additional Quest Reward Items
        InventoryItem(
            id = "item_chef_hat",
            name = "Grand Feast Hat & Cake",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🎂",
            description = "Royal festival chef hat and delicious celebratory cake reward!",
            costGp = 1000L,
            addHappiness = 20,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 5,
            defPowerBonus = 5
        ),
        InventoryItem(
            id = "item_cadava_potion",
            name = "Somnolent Shaman Brew",
            category = ItemCategory.POTION,
            iconEmoji = "🧪",
            description = "Mysterious trance-inducing herbal draught brewed for ancient rites.",
            costGp = 1200L,
            addHappiness = 15
        ),
        InventoryItem(
            id = "item_shield_half",
            name = "Heroic Sovereign Shield Crest",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🛡️",
            description = "Historic royal crest piece recovered from ancient ruins.",
            costGp = 2500L,
            addHappiness = 25
        ),
        InventoryItem(
            id = "item_amulet_of_accuracy",
            name = "Spirit Sight Amulet",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "📿",
            description = "Enchanted amulet awarded by the Grand Astrologer (+15 Ranged & Combat accuracy).",
            costGp = 4000L,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 15,
            defPowerBonus = 5
        ),
        InventoryItem(
            id = "item_rat_tail_brew",
            name = "Purification Herbal Brew",
            category = ItemCategory.POTION,
            iconEmoji = "🧪",
            description = "Wise herbalist's magical cauldron brew for cleansing spirits.",
            costGp = 800L,
            addHappiness = 10
        ),
        InventoryItem(
            id = "item_barronite_guard",
            name = "Ancient Earth Guard",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🛡️",
            description = "Ancient mountain shield guard unearthed below subterranean spires.",
            costGp = 6000L,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 10,
            defPowerBonus = 25
        ),
        InventoryItem(
            id = "item_glarials_amulet",
            name = "Ancestral Elven Amulet",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "📿",
            description = "Sacred elven spirit amulet echoing with ancient melodies from the sacred falls.",
            costGp = 8000L,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 10,
            defPowerBonus = 20
        ),
        InventoryItem(
            id = "item_boots_of_lightness",
            name = "Featherfoot Spirit Treads",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "👢",
            description = "Weight-reducing enchanted boots blessed with wind spirit lightness.",
            costGp = 5000L,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 5,
            defPowerBonus = 10
        ),
        InventoryItem(
            id = "item_khazard_armor",
            name = "Arena Gladiator Cuirass",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🛡️",
            description = "Honor armor set worn by legendary colosseum champions.",
            costGp = 7000L,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 25,
            defPowerBonus = 30
        ),
        InventoryItem(
            id = "item_god_book",
            name = "Ancient Spirit Grimoire",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "📖",
            description = "Ancient sacred tome inscribed with invocations to elemental spirits.",
            costGp = 15000L,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 20,
            defPowerBonus = 20
        ),
        InventoryItem(
            id = "item_dramen_staff",
            name = "Grove Spirit Staff",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🪄",
            description = "Enchanted branch carved from the Sacred Grove tree to access hidden fairy spirit rings.",
            costGp = 10000L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 35,
            defPowerBonus = 10
        ),
        InventoryItem(
            id = "item_lunar_staff",
            name = "Astral Starlight Robes & Staff",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🌙",
            description = "Sacred moon-forged staff and robes attuned to astral spirits.",
            costGp = 30000L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 60,
            defPowerBonus = 40
        ),
        InventoryItem(
            id = "item_dragon_halberd",
            name = "Dragonflame Spirit Halberd",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🗡️",
            description = "Formidable two-handed dragon spirit halberd with exceptional reach.",
            costGp = 45000L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 80,
            defPowerBonus = 15
        ),
        InventoryItem(
            id = "item_monkey_greegree",
            name = "Spirit Primate Totem",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🐒",
            description = "Ancient magical totem that communes with primate spirits.",
            costGp = 12000L,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 20,
            defPowerBonus = 10
        ),
        InventoryItem(
            id = "item_ancient_relic",
            name = "Ancient Spirit Relic",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🏺",
            description = "Mysterious relic of ancient civilizations humming with dormant power.",
            costGp = 50000L,
            addHappiness = 50
        ),
        InventoryItem(
            id = "item_beacon_ring",
            name = "Luminescent Spirit Ring",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "💍",
            description = "Enchanted ring awarded by the High Sovereign (+15 Stats).",
            costGp = 10000L,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 15,
            defPowerBonus = 15
        ),
        InventoryItem(
            id = "item_ram_skull_helm",
            name = "Ancestral Bone Helm",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "💀",
            description = "Imposing horned skull helm carved from beast bones for fierce protection.",
            costGp = 8000L,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 10,
            defPowerBonus = 25
        ),
        InventoryItem(
            id = "item_bearhead",
            name = "Ursa Spirit Mask",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🐻",
            description = "Sacred bear spirit mask granting primal ferocity and resilience.",
            costGp = 6000L,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 5,
            defPowerBonus = 20
        ),
        InventoryItem(
            id = "item_key_ring",
            name = "Enchanted Key Ring",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🔑",
            description = "Handy magical key organizer awarded for diplomatic achievements.",
            costGp = 5000L
        ),
        InventoryItem(
            id = "item_bonesack",
            name = "Ancestral Bone Pack",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🎒",
            description = "Spirit bone container worn on back to carry osseous offerings.",
            costGp = 10000L,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 10,
            defPowerBonus = 20
        ),
        InventoryItem(
            id = "item_dual_macuahuitl",
            name = "Dual Obsidian Macuahuitl",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🪵",
            description = "Sun-temple obsidian studded dual clubs channeling solar spirit energy (+85 Melee).",
            costGp = 60000L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 85,
            defPowerBonus = 15
        ),
        InventoryItem(
            id = "item_zombie_axe",
            name = "Ancestral Cleaver Axe",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🪓",
            description = "Heavy double-bladed ancestral axe with brutal cleaving power (+90 Melee).",
            costGp = 75000L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 90,
            defPowerBonus = 20
        ),
        InventoryItem(
            id = "item_staff_of_armadyl",
            name = "Celestial Staff of Air",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🪄",
            description = "Ancient Staff from Subterranean Sun Temple (+95 Magic)",
            costGp = 80000L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 95,
            defPowerBonus = 20
        ),
        InventoryItem(
            id = "item_camulet",
            name = "Desert Spirit Talisman",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🐫",
            description = "Enchanted desert talisman that grants speech with oasis beasts.",
            costGp = 15000L,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 10,
            defPowerBonus = 15
        ),
        InventoryItem(
            id = "item_elvarg_head",
            name = "Apex Dragon Trophy",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🐉",
            description = "Horn and scale trophy of the ancient apex dragon from the volcano.",
            costGp = 25000L
        ),
        InventoryItem(
            id = "item_casket_key",
            name = "Sea Rover Casket Key",
            category = ItemCategory.SKILL_TOOL,
            iconEmoji = "🔑",
            description = "Ornate bronze key to the sunken sea rover's treasure chest.",
            costGp = 3000L
        ),
        InventoryItem(
            id = "item_goblin_armor",
            name = "Tribal Chieftain Cuirass",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "👕",
            description = "Durable tribal cuirass dyed with ceremonial markings.",
            costGp = 2000L,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 5,
            defPowerBonus = 15
        ),
        InventoryItem(
            id = "item_goldsmith_gauntlets",
            name = "Goldsmith Spirit Gauntlets",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "🧤",
            description = "Enchanted metallurgical gauntlets (+2.5x Smithing XP when smelting Gold).",
            costGp = 20000L,
            equipmentSlot = EquipmentSlot.GLOVES,
            combatPowerBonus = 15,
            defPowerBonus = 15
        ),
        InventoryItem(
            id = "item_climbing_boots",
            name = "Mountain Stride Boots",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "👢",
            description = "Sturdy spiked boots crafted for traversing perilous mountain ridges (+5 Strength).",
            costGp = 3000L,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 15,
            defPowerBonus = 10
        ),
        InventoryItem(
            id = "item_ring_of_charos",
            name = "Silver Tongue Spirit Ring",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "💍",
            description = "Mystic enchanted ring radiating persuasive charm and influence.",
            costGp = 8000L,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 10,
            defPowerBonus = 10
        ),
        InventoryItem(
            id = "item_anchor_weapon",
            name = "Sunken Titan Anchor",
            category = ItemCategory.EQUIPMENT,
            iconEmoji = "⚓",
            description = "Massive iron ship anchor wielded with titan strength as a colossal crushing weapon.",
            costGp = 50000L,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 85,
            defPowerBonus = 20
        )
    ) + SummoningData.EFFIGY_ITEMS + SummoningData.TOTEM_ITEMS + SummoningData.GOLEM_TOTEM_ITEMS + EquipmentData.ORE_EQUIPMENT_ITEMS + SkillContractData.CONTRACT_REWARD_ITEMS

    fun normalizeItemId(id: String): String {
        val directMapped = when (id) {
            "raw_shrimp", "raw_shrimps", "item_raw_shrimp" -> "item_raw_shrimps"
            "item_cooked_shrimps", "cooked_shrimps", "shrimps" -> "item_shrimps"

            "raw_anchovy", "raw_anchovies", "item_raw_anchovy" -> "item_raw_anchovies"
            "item_cooked_anchovies", "cooked_anchovies", "anchovies" -> "item_anchovies"

            "raw_sardine", "raw_sardines", "item_raw_sardines" -> "item_raw_sardine"
            "item_cooked_sardine", "cooked_sardine", "sardine" -> "item_sardine"

            "raw_herring", "raw_herrings", "item_raw_herrings" -> "item_raw_herring"
            "item_cooked_herring", "cooked_herring", "herring" -> "item_herring"

            "raw_mackerel", "raw_mackerels", "item_raw_mackerels" -> "item_raw_mackerel"
            "item_cooked_mackerel", "cooked_mackerel", "mackerel" -> "item_mackerel"

            "raw_trout", "raw_trouts", "item_raw_trouts" -> "item_raw_trout"
            "item_cooked_trout", "cooked_trout", "trout" -> "item_trout"

            "raw_pike", "raw_pikes", "item_raw_pikes" -> "item_raw_pike"
            "item_cooked_pike", "cooked_pike", "pike" -> "item_pike"

            "raw_salmon", "raw_salmons", "item_raw_salmons" -> "item_raw_salmon"
            "item_cooked_salmon", "cooked_salmon", "salmon" -> "item_salmon"

            "raw_tuna", "raw_tunas", "item_raw_tunas" -> "item_raw_tuna"
            "item_cooked_tuna", "cooked_tuna", "tuna" -> "item_tuna"

            "raw_lobster", "raw_lobsters", "item_raw_lobsters" -> "item_raw_lobster"
            "item_cooked_lobster", "cooked_lobster", "lobster" -> "item_lobster"

            "raw_swordfish", "raw_swordfishes", "item_raw_swordfishes" -> "item_raw_swordfish"
            "item_cooked_swordfish", "cooked_swordfish", "swordfish" -> "item_swordfish"

            "raw_cave_eel", "raw_cave_eels", "item_raw_cave_eels" -> "item_raw_cave_eel"
            "item_cooked_cave_eel", "cooked_cave_eel", "cave_eel" -> "item_cave_eel"

            "raw_lava_eel", "raw_lava_eels", "item_raw_lava_eels" -> "item_raw_lava_eel"
            "item_cooked_lava_eel", "cooked_lava_eel", "lava_eel" -> "item_lava_eel"

            "raw_shark", "raw_sharks", "item_raw_sharks" -> "item_raw_shark"
            "item_cooked_shark", "cooked_shark", "shark" -> "item_shark"

            "raw_sea_turtle", "raw_sea_turtles", "item_raw_sea_turtles" -> "item_raw_sea_turtle"
            "item_cooked_sea_turtle", "cooked_sea_turtle", "sea_turtle" -> "item_sea_turtle"

            "raw_manta_ray", "raw_manta_rays", "item_raw_manta_rays" -> "item_raw_manta_ray"
            "item_cooked_manta_ray", "cooked_manta_ray", "manta_ray" -> "item_manta_ray"

            "raw_anglerfish", "raw_anglerfishes", "item_raw_anglerfishes" -> "item_raw_anglerfish"
            "item_cooked_anglerfish", "cooked_anglerfish", "anglerfish" -> "item_anglerfish"

            "raw_karambwan", "raw_karambwans", "item_raw_karambwans" -> "item_raw_karambwan"
            "item_cooked_karambwan", "cooked_karambwan", "karambwan" -> "item_karambwan"

            "raw_dark_crab", "raw_dark_crabs", "item_raw_dark_crabs" -> "item_raw_dark_crab"
            "item_cooked_dark_crab", "cooked_dark_crab", "dark_crab" -> "item_dark_crab"

            "bread_dough", "dough", "item_dough" -> "item_bread_dough"
            "item_cooked_bread", "cooked_bread" -> "item_bread"

            "raw_chicken", "item_raw_chickens" -> "item_raw_chicken"
            "item_cooked_chicken", "cooked_chicken" -> "item_cooked_chicken"

            "raw_meat", "raw_beef", "item_raw_beef" -> "item_raw_meat"
            "item_cooked_meat", "cooked_meat" -> "item_cooked_meat"

            "item_pyromancer_head", "pyromancer_head" -> "item_summoner_head"
            "item_pyromancer_body", "pyromancer_body" -> "item_summoner_body"
            "item_pyromancer_legs", "pyromancer_legs" -> "item_summoner_legs"
            "item_pyromancer_boots", "pyromancer_boots" -> "item_summoner_boots"
            "item_pyromancer_gloves", "pyromancer_gloves" -> "item_summoner_gloves"
            "item_pyromancer_cape", "pyromancer_cape" -> "item_summoner_cape"

            "birch_timber" -> "item_birch_timber"
            "cedar_timber" -> "item_cedar_timber"
            "ironwood_timber" -> "item_ironwood_timber"
            "magic_logs", "magic_log" -> "item_magic_logs"
            "elder_bark" -> "item_elder_bark"
            else -> null
        }
        if (directMapped != null) return directMapped
        if (id.startsWith("raw_")) {
            return "item_raw_" + id.removePrefix("raw_")
        }
        if (id.startsWith("item_cooked_")) {
            val base = id.replace("item_cooked_", "item_")
            if (ALL_SHOP_ITEMS.any { it.id == base }) return base
        }
        if (id.startsWith("cooked_")) {
            val base = "item_" + id.removePrefix("cooked_")
            if (ALL_SHOP_ITEMS.any { it.id == base }) return base
        }
        return id
    }

    fun getItemById(id: String): InventoryItem {
        val normalizedId = normalizeItemId(id)
        val exact = ALL_SHOP_ITEMS.find { it.id == normalizedId }
        if (exact != null) return exact

        val matchingRecipe = CauldronRecipes.ALL_RECIPES.find { 
            it.id == id || it.id == normalizedId || ("rec_" + id.removePrefix("item_")) == it.id || it.cookedItemName.equals(id, ignoreCase = true)
        }
        if (matchingRecipe != null) {
            return InventoryItem(
                id = matchingRecipe.id,
                name = matchingRecipe.cookedItemName,
                costGp = 600,
                category = ItemCategory.FOOD,
                healHp = matchingRecipe.healthRestored,
                restoreHunger = matchingRecipe.hungerRestored,
                addHappiness = 15,
                iconEmoji = matchingRecipe.emoji,
                description = matchingRecipe.description
            )
        }

        val lowerId = id.lowercase()
        val cleanName = id.replace("item_", "").replace("_", " ").split(" ")
            .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }

        var category = ItemCategory.MISC
        var equipmentSlot: EquipmentSlot? = null
        var combatBonus = 0
        var defBonus = 0
        var healHp = 0
        var restoreHunger = 0
        var addHappiness = 5
        var iconEmoji = "📦"

        if (lowerId.contains("trout") || lowerId.contains("salmon") || lowerId.contains("lobster") ||
            lowerId.contains("swordfish") || lowerId.contains("shark") || lowerId.contains("manta") ||
            lowerId.contains("bread") || lowerId.contains("cooked") || lowerId.contains("food") ||
            lowerId.contains("stew") || lowerId.contains("pie") || lowerId.contains("cake") || lowerId.contains("meat")) {
            category = ItemCategory.FOOD
            healHp = 10
            restoreHunger = 25
            addHappiness = 10
            iconEmoji = if (lowerId.contains("shark")) "🦈" else if (lowerId.contains("lobster")) "🦞" else "🐟"
        } else if (lowerId.contains("potion") || lowerId.contains("brew") || lowerId.contains("tea")) {
            category = ItemCategory.POTION
            healHp = 15
            restoreHunger = 10
            iconEmoji = "🧪"
        } else if (lowerId.contains("badge") || lowerId.contains("trophy")) {
            category = ItemCategory.MISC
            iconEmoji = if (lowerId.contains("league")) "👑" else "🏆"
        } else if (lowerId.contains("scimitar") || lowerId.contains("sword") || lowerId.contains("dagger") ||
            lowerId.contains("whip") || lowerId.contains("halberd") || lowerId.contains("flail") ||
            lowerId.contains("bow") || lowerId.contains("staff") || lowerId.contains("rod") || lowerId.contains("wand") ||
            lowerId.contains("mace") || lowerId.contains("blade") || lowerId.contains("spear") || lowerId.contains("scepter") ||
            lowerId.contains("warhammer") || lowerId.contains("glaive") || lowerId.contains("lance") || lowerId.contains("saber")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.WEAPON
            val mult = when {
                lowerId.contains("sovereign") || lowerId.contains("ancestral") || lowerId.contains("dragonflame") || lowerId.contains("celestial") -> 80
                lowerId.contains("cobalt") || lowerId.contains("radiant") || lowerId.contains("elderwood") || lowerId.contains("titan") -> 50
                lowerId.contains("steel") || lowerId.contains("silver") || lowerId.contains("mystic") || lowerId.contains("granite") -> 30
                else -> 15
            }
            combatBonus = mult
            iconEmoji = if (lowerId.contains("bow")) "🏹" else if (lowerId.contains("rod")) "🎣" else if (lowerId.contains("staff") || lowerId.contains("scepter")) "🪄" else "⚔️"
        } else if (lowerId.contains("axe") || lowerId.contains("hatchet")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.AXE
            combatBonus = 10
            iconEmoji = "🪓"
        } else if (lowerId.contains("pickaxe")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.WEAPON
            combatBonus = 10
            iconEmoji = "⛏️"
        } else if (lowerId.contains("helm") || lowerId.contains("hat") || lowerId.contains("hood") || lowerId.contains("crown") || lowerId.contains("cap") || lowerId.contains("mask") || lowerId.contains("circlet")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.HEAD
            val mult = when {
                lowerId.contains("sovereign") || lowerId.contains("ancestral") || lowerId.contains("dragonflame") || lowerId.contains("celestial") -> 50
                lowerId.contains("cobalt") || lowerId.contains("radiant") || lowerId.contains("titan") -> 35
                lowerId.contains("steel") || lowerId.contains("silver") || lowerId.contains("mystic") -> 20
                else -> 10
            }
            defBonus = mult
            iconEmoji = if (lowerId.contains("crown")) "👑" else "🪖"
        } else if (lowerId.contains("platebody") || lowerId.contains("torso") || lowerId.contains("robe") || lowerId.contains("body") || lowerId.contains("cuirass") || lowerId.contains("tunic") || lowerId.contains("hauberk") || lowerId.contains("mail") || lowerId.contains("armor") || lowerId.contains("armour") || lowerId.contains("vest")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.BODY
            val mult = when {
                lowerId.contains("sovereign") || lowerId.contains("ancestral") || lowerId.contains("dragonflame") || lowerId.contains("celestial") -> 90
                lowerId.contains("cobalt") || lowerId.contains("radiant") || lowerId.contains("titan") -> 60
                lowerId.contains("steel") || lowerId.contains("silver") || lowerId.contains("mystic") -> 35
                else -> 18
            }
            defBonus = mult
            iconEmoji = "🛡️"
        } else if (lowerId.contains("platelegs") || lowerId.contains("legs") || lowerId.contains("skirt") || lowerId.contains("greaves") || lowerId.contains("breeches")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.LEGS
            val mult = when {
                lowerId.contains("sovereign") || lowerId.contains("ancestral") || lowerId.contains("dragonflame") || lowerId.contains("celestial") -> 70
                lowerId.contains("cobalt") || lowerId.contains("radiant") || lowerId.contains("titan") -> 45
                lowerId.contains("steel") || lowerId.contains("silver") || lowerId.contains("mystic") -> 25
                else -> 12
            }
            defBonus = mult
            iconEmoji = "👖"
        } else if (lowerId.contains("shield") || lowerId.contains("kiteshield") || lowerId.contains("defender") || lowerId.contains("aegis") || lowerId.contains("buckler") || lowerId.contains("guard")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.SHIELD
            val mult = when {
                lowerId.contains("sovereign") || lowerId.contains("ancestral") || lowerId.contains("dragonflame") || lowerId.contains("celestial") -> 75
                lowerId.contains("cobalt") || lowerId.contains("radiant") || lowerId.contains("titan") -> 45
                lowerId.contains("steel") || lowerId.contains("silver") || lowerId.contains("granite") -> 25
                else -> 12
            }
            defBonus = mult
            iconEmoji = "🛡️"
        } else if (lowerId.contains("amulet") || lowerId.contains("necklace") || lowerId.contains("pendant") || lowerId.contains("talisman")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.AMULET
            combatBonus = 15
            iconEmoji = "📿"
        } else if (lowerId.contains("ring")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.RING
            combatBonus = 10
            iconEmoji = "💍"
        } else if (lowerId.contains("gloves") || lowerId.contains("gauntlets") || lowerId.contains("bracers")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.GLOVES
            defBonus = 10
            iconEmoji = "🧤"
        } else if (lowerId.contains("boots") || lowerId.contains("shoes") || lowerId.contains("treads")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.BOOTS
            defBonus = 10
            iconEmoji = "🥾"
        } else if (lowerId.contains("cape") || lowerId.contains("cloak") || lowerId.contains("mantle")) {
            category = ItemCategory.EQUIPMENT
            equipmentSlot = EquipmentSlot.CAPE
            defBonus = 10
            iconEmoji = "🧥"
        } else if (lowerId.contains("bones")) {
            category = ItemCategory.BONES
            iconEmoji = "🦴"
        } else if (lowerId.contains("seed")) {
            category = ItemCategory.SEEDS
            iconEmoji = "🌱"
        } else if (lowerId.contains("logs") || lowerId.contains("wood")) {
            category = ItemCategory.LOGS_WOOD
            iconEmoji = "🪵"
        } else if (lowerId.contains("bar") || lowerId.contains("ore")) {
            category = ItemCategory.BARS_ORES
            iconEmoji = "🧈"
        } else if (lowerId.contains("effigy")) {
            category = ItemCategory.MISC
            iconEmoji = when {
                lowerId.contains("air") -> "💨"
                lowerId.contains("mind") -> "🧠"
                lowerId.contains("water") -> "💧"
                lowerId.contains("earth") -> "🪨"
                lowerId.contains("fire") -> "🔥"
                lowerId.contains("body") -> "🛡️"
                lowerId.contains("cosmic") -> "🌌"
                lowerId.contains("chaos") -> "💥"
                lowerId.contains("nature") -> "🌿"
                lowerId.contains("law") -> "⚖️"
                lowerId.contains("death") -> "💀"
                lowerId.contains("astral") -> "✨"
                lowerId.contains("blood") -> "🩸"
                lowerId.contains("soul") -> "👻"
                lowerId.contains("wrath") -> "⚡"
                else -> "🗿"
            }
        }

        return InventoryItem(
            id = id,
            name = cleanName,
            category = category,
            iconEmoji = iconEmoji,
            description = "Valuable $cleanName",
            costGp = 100L,
            healHp = healHp,
            restoreHunger = restoreHunger,
            addHappiness = addHappiness,
            equipmentSlot = equipmentSlot,
            combatPowerBonus = combatBonus,
            defPowerBonus = defBonus
        )
    }
}
