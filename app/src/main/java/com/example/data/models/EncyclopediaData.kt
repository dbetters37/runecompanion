package com.example.data.models

enum class EncyclopediaCategory(val displayName: String, val iconEmoji: String) {
    ALL("All Items", "🌟"),
    EQUIPMENT("Weapons & Armor", "⚔️"),
    FOOD("Food & Cooking", "🍲"),
    HERBLORE("Potions & Herbs", "🧪"),
    WOOD_FLETCHING("Wood & Fletching", "🪵"),
    MINING_SMITHING("Mining & Smithing", "⛏️"),
    FARMING("Farming & Crops", "🌾"),
    MAGIC_RUNES("Runes & Magic", "💨"),
    SLAYER_HUNTER("Slayer & Hunter", "💀"),
    DIVINATION_SUMMONING("Divination & Summoning", "🔮"),
    WORKSHOP_POH("Workshop & POH", "🛠️"),
    QUEST_LEAGUE("Quest & League", "📜")
}

data class EncyclopediaItem(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val category: EncyclopediaCategory,
    val description: String,
    val primaryObtainMethod: String,
    val allObtainMethods: List<String>,
    val costGp: Long = 0L,
    val highAlchGp: Long = 0L,
    val healHp: Int = 0,
    val restoreHunger: Int = 0,
    val combatPower: Int = 0,
    val defPower: Int = 0,
    val equipmentSlot: EquipmentSlot? = null,
    val reqSkill: OsrsSkill? = null,
    val reqLevel: Int = 1
) {
    val effectiveSkill: OsrsSkill
        get() = reqSkill ?: SkillItemRegistry.getItemSkill(
            InventoryItem(
                id = id,
                name = name,
                category = when (category) {
                    EncyclopediaCategory.FOOD -> ItemCategory.FOOD
                    EncyclopediaCategory.HERBLORE -> ItemCategory.POTION
                    EncyclopediaCategory.WOOD_FLETCHING -> ItemCategory.LOGS_WOOD
                    EncyclopediaCategory.MINING_SMITHING -> ItemCategory.BARS_ORES
                    EncyclopediaCategory.FARMING -> ItemCategory.SEEDS
                    EncyclopediaCategory.MAGIC_RUNES -> ItemCategory.RUNES_MAGIC
                    else -> ItemCategory.MISC
                },
                iconEmoji = iconEmoji,
                description = description,
                costGp = costGp,
                healHp = healHp,
                restoreHunger = restoreHunger,
                combatPowerBonus = combatPower,
                defPowerBonus = defPower,
                equipmentSlot = equipmentSlot
            )
        )
}

object EncyclopediaDatabase {

    private val CURATED_OBTAIN_MAP: Map<String, Pair<EncyclopediaCategory, List<String>>> = mapOf(
        // === BASIC & COOKED FOODS ===
        "item_bread" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cooking: Baked at range from Flour & Water (Lv. 1 Cooking)",
            "🛒 Shop: Purchased from General Store for 10 GP",
            "🥷 Thieving: Pickpocketed from Men & Women in Lumbridge"
        )),
        "item_raw_shrimps" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Net fishing in Coastal Shallows (Lv. 1 Fishing)",
            "🎣 Shaman Pool: Caught at Coastal Shallows with small net",
            "🛒 Shop: Purchased for 5 GP"
        )),
        "item_raw_anchovies" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Net fishing in Coastal Shallows (Lv. 5 Fishing)",
            "🎣 Shaman Pool: Caught in Coastal Shallows"
        )),
        "item_raw_sardine" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Bait fishing at Coastal Shallows (Lv. 10 Fishing)",
            "🛒 Shop: Purchased for 15 GP"
        )),
        "item_raw_trout" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Fly fishing in River Whispers (Lv. 15 Fishing)",
            "🎣 POH Pond: Fished at Player Owned House pond",
            "💀 Slayer: Dropped by Cave Crawlers"
        )),
        "item_trout" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cooking: Cooked Raw Trout on a Fire or Range (Lv. 15 Cooking)",
            "🛒 Shop: Purchased from Food Vendor for 25 GP"
        )),
        "item_raw_pike" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Bait fishing in River Whispers (Lv. 20 Fishing)",
            "🎣 Shaman Pool: Caught along River Whispers"
        )),
        "item_raw_salmon" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Fly fishing in River Whispers (Lv. 25 Fishing)",
            "🎣 POH Pond: Fished at Player Owned House pond",
            "🥷 Thieving: Pickpocketed from Fremennik Traders"
        )),
        "item_salmon" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cooking: Cooked Raw Salmon on a Fire or Range (Lv. 25 Cooking)",
            "🛒 Shop: Purchased from Food Vendor for 35 GP"
        )),
        "item_raw_lobster" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Cage fishing at Coral Reef Lagoon (Lv. 40 Fishing)",
            "🎣 POH Pond: Fished at Coral Pond",
            "🌊 Adventuring: Dropped from Ocean Leviathans"
        )),
        "item_lobster" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cooking: Cooked Raw Lobster on a Range (Lv. 40 Cooking)",
            "🛒 Shop: Purchased from Grand Exchange for 50 GP"
        )),
        "item_raw_tuna" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Harpoon fishing at Coral Reef Lagoon (Lv. 45 Fishing)",
            "🎣 Shaman Pool: Caught at Coral Reef Lagoon"
        )),
        "item_raw_swordfish" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Harpoon fishing at Coral Reef Lagoon (Lv. 50 Fishing)",
            "🎣 POH Pond: Fished at high level POH pond",
            "🌊 Adventuring: Deep sea dungeon reward"
        )),
        "item_swordfish" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cooking: Cooked Raw Swordfish on a Range (Lv. 50 Cooking)",
            "🛒 Shop: Purchased for 90 GP"
        )),
        "item_raw_shark" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Harpoon fishing at Abyssal Trench (Lv. 76 Fishing)",
            "🎣 Shaman Pool: Caught in Abyssal Trench with Big Net/Harpoon",
            "💀 Slayer: Dropped by Abyssal Sire & Kraken"
        )),
        "item_shark" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cooking: Cooked Raw Shark on a Range (Lv. 80 Cooking)",
            "🛒 Shop: Purchased from Grand Exchange for 150 GP"
        )),
        "item_raw_sea_turtle" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Deep sea fishing at Abyssal Trench (Lv. 79 Fishing)",
            "🌊 Adventuring: High floor dungeon reward"
        )),
        "item_raw_manta_ray" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Big net fishing at Abyssal Trench (Lv. 81 Fishing)",
            "🎣 Shaman Pool: Caught in deep Abyssal waters"
        )),
        "item_manta_ray" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cooking: Cooked Raw Manta Ray on a Range (Lv. 91 Cooking)",
            "🛒 Shop: Purchased for 250 GP"
        )),
        "item_spirit_koi" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Caught at Mystic Spirit Oasis (Lv. 85 Fishing)",
            "✨ Shaman Pool: Caught with Shaman Rod"
        )),
        "item_astral_angler" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Caught at Mystic Spirit Oasis (Lv. 90 Fishing)",
            "🌌 Adventuring: Dropped from Astral Void Dungeons"
        )),
        "item_ethereal_ray" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Caught at Mystic Spirit Oasis (Lv. 92 Fishing)",
            "✨ Shaman Pool: Mystic Spirit Oasis deep pool"
        )),
        "item_magma_eel" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Lava fishing at Volcanic Lava Lake with Oily Rod (Lv. 90 Fishing)",
            "🌋 Shaman Pool: Volcanic Lava Lake"
        )),
        "item_ember_trout" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Caught at Volcanic Lava Lake (Lv. 93 Fishing)",
            "🔥 Adventuring: Magma dungeon floor drop"
        )),
        "item_obsidian_crab" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Caught at Volcanic Lava Lake (Lv. 95 Fishing)",
            "🌋 Shaman Pool: Volcanic Lava Lake"
        )),
        "item_sacred_shaman_fish" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Caught at Void Starlight Pool (Lv. 99 Fishing)",
            "🌟 Shaman Pool: Apex fishing spot",
            "👑 Adventuring: Chapter 10 Apex Throne drop"
        )),
        "item_cosmic_whale" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Caught at Void Starlight Pool (Lv. 99 Fishing)",
            "🌌 Shaman Pool: Void Starlight Pool"
        )),
        "item_golden_dragonfish" to (EncyclopediaCategory.FOOD to listOf(
            "🎣 Fishing: Caught at Void Starlight Pool (Lv. 99 Fishing)",
            "🐉 Adventuring: Moros Apex Boss Drop"
        )),
        "item_purple_sweets" to (EncyclopediaCategory.FOOD to listOf(
            "📜 Clue Scrolls & Treasure Trails: Rewarded from completing Clue Scrolls",
            "🛒 Shop: Purchased for 80 GP"
        )),

        // === CAULDRON MEALS ===
        "item_shrimp_stew" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cauldron Cooking: Cooked from Raw Shrimps + Clean Greenleaf (Lv. 1 Cooking)",
            "🍲 Cauldron Buff: Grants +10% Attack & +5% Defence"
        )),
        "item_trout_elixir" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cauldron Cooking: Cooked from Raw Trout + Clean Meadow Mint (Lv. 20 Cooking)",
            "🍲 Cauldron Buff: Grants +15% Attack & +10% GP Drops"
        )),
        "item_salmon_tonic" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cauldron Cooking: Cooked from Raw Salmon + Clean Wild Thyme (Lv. 35 Cooking)",
            "🍲 Cauldron Buff: Grants +20% Damage & +15% XP"
        )),
        "item_lobster_chowder" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cauldron Cooking: Cooked from Raw Lobster + Clean Lavender (Lv. 50 Cooking)",
            "🍲 Cauldron Buff: Grants +25% Crit & +20% Defence"
        )),
        "item_swordfish_brew" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cauldron Cooking: Cooked from Raw Swordfish + Clean Sunleaf (Lv. 65 Cooking)",
            "🍲 Cauldron Buff: Grants +30% GP Drops & +20% Attack"
        )),
        "item_shark_stew" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cauldron Cooking: Cooked from Raw Shark + Clean Ironleaf (Lv. 80 Cooking)",
            "🍲 Cauldron Buff: Grants +35% Combat & +25% XP"
        )),
        "item_void_nectar" to (EncyclopediaCategory.FOOD to listOf(
            "🍲 Cauldron Cooking: Cooked from Raw Manta Ray + Clean Mystic Sage (Lv. 95 Cooking)",
            "🍲 Cauldron Buff: Grants +50% Combat Power & +50% GP Drops"
        )),

        // === ORES, BARS & SMITHING ===
        "item_copper_ore" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Mined in Novice Quarry (Lv. 1 Mining)",
            "⛏️ POH Quarry: Mined at Player Owned House quarry",
            "🛒 Shop: Purchased for 5 GP"
        )),
        "item_tin_ore" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Mined in Novice Quarry (Lv. 1 Mining)",
            "⛏️ POH Quarry: Mined at Player Owned House quarry",
            "🛒 Shop: Purchased for 5 GP"
        )),
        "item_bronze_bar" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⚒️ Smelting: Smelted at Furnace from 1 Copper + 1 Tin Ore (Lv. 1 Smithing)",
            "🛒 Shop: Purchased from Smithing Store for 15 GP",
            "🥷 Thieving: Pickpocketed from Warrior Women"
        )),
        "item_iron_ore" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Mined in Iron Quarry (Lv. 15 Mining)",
            "⛏️ POH Quarry: Mined at Player Owned House quarry",
            "💀 Slayer: Dropped by Cave Bugs & Harpie Swarms"
        )),
        "item_iron_bar" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⚒️ Smelting: Smelted at Furnace from 1 Iron Ore (Lv. 15 Smithing)",
            "🛒 Shop: Purchased for 30 GP",
            "🥷 Thieving: Pickpocketed from Wilderness Rogues"
        )),
        "item_coal_ore" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Mined in Coal Veins (Lv. 30 Mining)",
            "⛏️ POH Quarry: Mined at Player Owned House quarry",
            "💀 Slayer: Dropped by Wall Beasts & Turoths"
        )),
        "item_steel_bar" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⚒️ Smelting: Smelted at Furnace from 1 Iron Ore + 2 Coal (Lv. 30 Smithing)",
            "🛒 Shop: Purchased for 75 GP",
            "🥷 Thieving: Pickpocketed from Ardougne Knights"
        )),
        "item_silver_ore" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Mined in Silver Quarry (Lv. 20 Mining)",
            "⛏️ POH Quarry: Mined at Player Owned House quarry"
        )),
        "item_silver_bar" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⚒️ Smelting: Smelted at Furnace from 1 Silver Ore (Lv. 20 Smithing)",
            "🛒 Shop: Purchased for 50 GP"
        )),
        "item_gold_ore" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Mined in Gold Quarry (Lv. 40 Mining)",
            "⛏️ POH Quarry: Mined at Player Owned House quarry"
        )),
        "item_gold_bar" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⚒️ Smelting: Smelted at Furnace from 1 Gold Ore (Lv. 40 Smithing)",
            "🥷 Thieving: Pickpocketed from Wealthy Citizens & TzHaar-Hur",
            "🛒 Shop: Purchased for 120 GP"
        )),
        "item_mithril_ore" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Mined in Opalite Quarry (Lv. 55 Mining)",
            "⛏️ POH Quarry: Mined at Player Owned House quarry"
        )),
        "item_mithril_bar" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⚒️ Smelting: Smelted at Furnace from 1 Opalite Ore + 2 Coal (Lv. 50 Smithing)",
            "🛒 Shop: Purchased for 180 GP",
            "🥷 Thieving: Pickpocketed from Desert Bandits"
        )),
        "item_adamant_ore" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Mined in Amethyst Quarry (Lv. 70 Mining)",
            "⛏️ POH Quarry: Mined at Player Owned House quarry"
        )),
        "item_adamant_bar" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⚒️ Smelting: Smelted at Furnace from 1 Amethyst Ore + 3 Coal (Lv. 70 Smithing)",
            "🛒 Shop: Purchased for 350 GP",
            "🥷 Thieving: Pickpocketed from Vyre Nobles"
        )),
        "item_runite_ore" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Mined in Aetherite Quarry (Lv. 85 Mining)",
            "⛏️ POH Quarry: Mined at Player Owned House quarry",
            "💀 Slayer: Dropped by Dragons & Abyssal Demons"
        )),
        "item_rune_bar" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⚒️ Smelting: Smelted at Furnace from 1 Aetherite Ore + 4 Coal (Lv. 85 Smithing)",
            "🛒 Shop: Purchased for 900 GP",
            "🥷 Thieving: Pickpocketed from TzHaar-Ket Guards & Grand Alchemists"
        )),

        // === GEMS & JEWELRY ===
        "item_uncut_sapphire" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Rolled when mining in quarries or gem areas (Lv. 20 Mining)",
            "🥷 Thieving: Pickpocketed from Desert Urchins & Wealthy Citizens"
        )),
        "item_sapphire" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "💎 Crafting: Cut from Sapphire using Chisel (Lv. 20 Crafting)",
            "🛒 Shop: Purchased for 200 GP"
        )),
        "item_uncut_emerald" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Rolled from gem quarries and mining nodes (Lv. 27 Mining)",
            "💀 Slayer: Dropped by Cockatrices & Pyrefiends"
        )),
        "item_emerald" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "💎 Crafting: Cut from Emerald using Chisel (Lv. 27 Crafting)",
            "🛒 Shop: Purchased for 350 GP"
        )),
        "item_uncut_ruby" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Rolled from gem quarries (Lv. 34 Mining)",
            "🥷 Thieving: Pickpocketed from TzHaar Artisans & Vyre Nobles"
        )),
        "item_ruby" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "💎 Crafting: Cut from Ruby using Chisel (Lv. 34 Crafting)",
            "🛒 Shop: Purchased for 600 GP"
        )),
        "item_uncut_diamond" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Rare drop from high level gem quarries (Lv. 40 Mining)",
            "🥷 Thieving: Pickpocketed from Heroes, Elves & Crystal Artisans"
        )),
        "item_diamond" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "💎 Crafting: Cut from Diamond using Chisel (Lv. 40 Crafting)",
            "🛒 Shop: Purchased for 1,200 GP"
        )),
        "item_uncut_dragonstone" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "⛏️ Mining: Rare find in Gemology Quarries (Lv. 55 Mining)",
            "💀 Slayer: Dropped by Gargoyles & Skeletal Wyverns",
            "📜 Quests: Rewarded from Legends' Quest"
        )),
        "item_dragonstone" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "💎 Crafting: Cut from Dragonstone using Chisel (Lv. 55 Crafting)",
            "🛒 Shop: Purchased for 3,000 GP"
        )),
        "item_uncut_onyx" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "🌋 Volcanic Shaman Guild: Purchased for 260,000 Lava Embers in Magma Sanctum",
            "💀 Slayer: Dropped by Serpent Spirits & Volcanic Bosses"
        )),
        "item_onyx" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "💎 Crafting: Cut from Onyx using Chisel (Lv. 67 Crafting)",
            "🛒 Shop: Purchased for 15,000 GP"
        )),
        "item_zenyte_shard" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "💀 Slayer: Dropped by Corrupted Primate Spirits",
            "⛏️ Mining: Rare deep fissure roll"
        )),
        "item_uncut_zenyte" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "💎 Crafting: Fused from Zenyte Shard + Onyx (Lv. 89 Crafting)"
        )),

        // === WOODCUTTING & FLETCHING ===
        "item_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Normal Trees (Lv. 1 Woodcutting)",
            "🌲 The Grove: Chopped in Sylvan Canopy",
            "🛒 Shop: Purchased for 4 GP"
        )),
        "item_oak_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Oak Trees (Lv. 15 Woodcutting)",
            "🌲 The Grove: Chopped in Sylvan Canopy",
            "🛒 Shop: Purchased for 15 GP"
        )),
        "item_birch_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Birch Groves (Lv. 10 Woodcutting)",
            "🌲 The Grove: Sylvan Canopy Birch Grove"
        )),
        "item_willow_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Willow Trees (Lv. 30 Woodcutting)",
            "🌲 The Grove: Chopped along Willow Riverbank",
            "🛒 Shop: Purchased for 30 GP"
        )),
        "item_pine_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped in Whispering Pine Ridge (Lv. 25 Woodcutting)",
            "🌲 The Grove: Alpine Pine trees"
        )),
        "item_cedar_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped in Whispering Pine Ridge (Lv. 40 Woodcutting)",
            "🌲 The Grove: Mountain Cedar trees"
        )),
        "item_maple_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Maple Trees (Lv. 45 Woodcutting)",
            "🌲 The Grove: Autumn Maple trees",
            "🛒 Shop: Purchased for 65 GP"
        )),
        "item_teak_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Teak Trees (Lv. 35 Woodcutting)",
            "🌲 The Grove: Hardwood Teak grove",
            "🛒 Shop: Purchased for 80 GP"
        )),
        "item_yew_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Yew Trees (Lv. 60 Woodcutting)",
            "🌲 The Grove: Sacred Yew Grove",
            "🛒 Shop: Purchased for 140 GP"
        )),
        "item_mahogany_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Mahogany Trees (Lv. 50 Woodcutting)",
            "🌲 The Grove: Mahogany Rainforest",
            "🛒 Shop: Purchased for 180 GP"
        )),
        "item_ironwood_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Ironwood Woodlands (Lv. 60 Woodcutting)",
            "🌲 The Grove: Ironwood Woodlands"
        )),
        "item_magic_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Magic Trees (Lv. 75 Woodcutting)",
            "🌲 The Grove: Enchanted Magic Grove",
            "🛒 Shop: Purchased for 320 GP"
        )),
        "item_redwood_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Redwood Trees (Lv. 90 Woodcutting)",
            "🌲 The Grove: Giant Redwood Canopy",
            "🛒 Shop: Purchased for 500 GP"
        )),
        "item_elderwood_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Elderwood Sanctuary (Lv. 95 Woodcutting)",
            "🌲 The Grove: Elderwood Trees"
        )),
        "item_spirit_wood_logs" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪓 Woodcutting: Chopped from Spirit Tree Hollow (Lv. 99 Woodcutting)",
            "🌲 The Grove: Apex woodcutting spot"
        )),
        "item_arrow_shaft" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🪵 Fletching Tab: Whittled from Normal, Oak, Willow, Maple, or Yew Logs at Whittling Bench (1 Log ➔ 15 Shafts)",
            "🎯 Fletching Tab: Carved directly from any wood logs",
            "🛒 Archery Shop / General Store: Purchased for 2 GP"
        )),
        "item_wooden_stick" to (EncyclopediaCategory.WORKSHOP_POH to listOf(
            "🛠️ Crafting: Carved at Wooden Sticks Bench (1 Log ➔ 4 Sticks, +25 Crafting XP)",
            "🌲 The Grove: Foraged from ground and underbrush",
            "🛒 General Store: Purchased for 5 GP"
        )),
        "item_feather" to (EncyclopediaCategory.SLAYER_HUNTER to listOf(
            "🪤 Hunter Tab: Caught from Crimson Swifts, Cerulean Twitch, Copper Longtail, Tropical Wagtail using Bird Snares (10-45 Feathers per catch)",
            "🛠️ Workshop: Plucked / Prepared at Workshop from fibers (1 Log / 1 Fiber ➔ 15 Feathers)",
            "🛒 General Store & Archery Shop: Purchased in Feather Packs for 3 GP each",
            "💀 Slayer: Dropped by Giant Birds & Desert Avians"
        )),
        "item_bowstring" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🧵 Crafting Tab: Spun at Spinning Wheel from Flax / Plant Fiber (1 Flax / 1 Fiber ➔ 1 Bowstring, Lv. 10 Crafting)",
            "🌾 Farming Tab: Harvested flax spun into tough string",
            "🛒 Crafting Shop: Purchased for 35 GP",
            "💀 Slayer: Dropped by Temple Guardians & Spiders"
        )),
        "item_headless_arrow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching Tab: Combined 15x Arrow Shafts + 15x Feathers (Lv. 1 Fletching)",
            "🛠️ Fletching Station: Assembled at Fletching Station",
            "🛒 Archery Shop: Purchased for 8 GP"
        )),
        "item_arrowtip" to (EncyclopediaCategory.WORKSHOP_POH to listOf(
            "🔨 Smithing: Forged at Anvil from any Metal Bar (1 Bar ➔ 10 Tips)",
            "🛒 Archery Shop: Purchased for 15 GP"
        )),
        "item_bronze_arrowtip" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "🔨 Smithing Tab: Forged at Metal Arrowtips Anvil (1 Bronze Bar ➔ 15 Bronze Arrowtips, Lv. 1 Smithing)",
            "⚒️ Smithing Tab: Smelted and hammered at Anvils",
            "🛒 Archery Shop: Purchased for 5 GP"
        )),
        "item_iron_arrowtip" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "🔨 Smithing Tab: Forged at Metal Arrowtips Anvil (1 Iron Bar ➔ 15 Iron Arrowtips, Lv. 15 Smithing)",
            "⚒️ Smithing Tab: Hammered at Anvils",
            "🛒 Archery Shop: Purchased for 10 GP"
        )),
        "item_steel_arrowtip" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "🔨 Smithing Tab: Forged at Metal Arrowtips Anvil (1 Steel Bar ➔ 15 Steel Arrowtips, Lv. 30 Smithing)",
            "⚒️ Smithing Tab: Hammered at Anvils",
            "🛒 Archery Shop: Purchased for 20 GP"
        )),
        "item_mithril_arrowtip" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "🔨 Smithing Tab: Forged at Metal Arrowtips Anvil (1 Mithril Bar ➔ 15 Mithril Arrowtips, Lv. 45 Smithing)",
            "⚒️ Smithing Tab: Hammered at Anvils",
            "🛒 Archery Shop: Purchased for 40 GP"
        )),
        "item_adamant_arrowtip" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "🔨 Smithing Tab: Forged at Metal Arrowtips Anvil (1 Adamant Bar ➔ 15 Adamant Arrowtips, Lv. 60 Smithing)",
            "⚒️ Smithing Tab: Hammered at Anvils",
            "🛒 Archery Shop: Purchased for 80 GP"
        )),
        "item_rune_arrowtip" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "🔨 Smithing Tab: Forged at Metal Arrowtips Anvil (1 Rune Bar ➔ 15 Rune Arrowtips, Lv. 75 Smithing)",
            "⚒️ Smithing Tab: Hammered at Anvils",
            "🛒 Archery Shop: Purchased for 200 GP"
        )),
        "item_dragon_arrowtip" to (EncyclopediaCategory.MINING_SMITHING to listOf(
            "🔨 Smithing Tab: Forged at Metal Arrowtips Anvil (1 Dragon Metal ➔ 15 Dragon Arrowtips, Lv. 90 Smithing)",
            "💀 Slayer & Bosses: Dropped by Dragon Wyverns & King Black Dragon",
            "🛒 Rare Token Shop: Purchased for 600 GP"
        )),
        "item_bronze_arrows" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Attached Bronze Arrowtips to Headless Arrows (Lv. 1 Fletching)",
            "🛒 Shop: Purchased for 5 GP"
        )),
        "item_iron_arrows" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Attached Iron Arrowtips to Headless Arrows (Lv. 15 Fletching)",
            "🛒 Shop: Purchased for 10 GP"
        )),
        "item_steel_arrows" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Attached Steel Arrowtips to Headless Arrows (Lv. 30 Fletching)",
            "🛒 Shop: Purchased for 20 GP"
        )),
        "item_mithril_arrows" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Attached Opalite Arrowtips to Headless Arrows (Lv. 50 Whittling)",
            "🛒 Shop: Purchased for 40 GP"
        )),
        "item_adamant_arrows" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Attached Amethyst Arrowtips to Headless Arrows (Lv. 70 Whittling)",
            "🛒 Shop: Purchased for 80 GP"
        )),
        "item_rune_arrows" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Attached Aetherite Arrowtips to Headless Arrows (Lv. 85 Whittling)",
            "🛒 Shop: Purchased for 200 GP"
        )),
        "item_dragon_arrows" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Attached Dragon Arrowtips to Headless Arrows (Lv. 90 Whittling)",
            "💀 Slayer: Dropped by Dragon Wyverns & Hydra",
            "🛒 Shop: Purchased for 600 GP"
        )),
        "item_shortbow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Carved from Normal Logs & Strung with Bowstring (Lv. 1 Whittling)",
            "🛒 Shop: Purchased for 25 GP"
        )),
        "item_birch_shortbow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Carved from Birch Logs & Strung (Lv. 10 Whittling)",
            "🛒 Shop: Purchased for 50 GP"
        )),
        "item_oak_shortbow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Carved from Oak Logs & Strung (Lv. 20 Whittling)",
            "🛒 Shop: Purchased for 60 GP"
        )),
        "item_pine_shortbow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Carved from Pine Logs & Strung (Lv. 28 Whittling)",
            "🛒 Shop: Purchased for 110 GP"
        )),
        "item_willow_shortbow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Carved from Willow Logs & Strung (Lv. 35 Whittling)",
            "🛒 Shop: Purchased for 120 GP"
        )),
        "item_cedar_shortbow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Carved from Cedar Logs & Strung (Lv. 42 Whittling)",
            "🛒 Shop: Purchased for 220 GP"
        )),
        "item_maple_shortbow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Carved from Maple Logs & Strung (Lv. 50 Whittling)",
            "🛒 Shop: Purchased for 250 GP"
        )),
        "item_yew_shortbow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Carved from Yew Logs & Strung (Lv. 65 Whittling)",
            "🛒 Shop: Purchased for 500 GP"
        )),
        "item_ironwood_shortbow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Carved from Ironwood Logs & Strung (Lv. 75 Whittling)",
            "🛒 Shop: Purchased for 1,200 GP"
        )),
        "item_magic_shortbow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Carved from Magic Logs & Strung (Lv. 85 Whittling)",
            "🛒 Shop: Purchased for 2,500 GP"
        )),
        "item_redwood_shortbow" to (EncyclopediaCategory.WOOD_FLETCHING to listOf(
            "🎯 Fletching: Carved from Redwood Logs & Strung (Lv. 90 Whittling)",
            "🛒 Shop: Purchased for 6,000 GP"
        )),

        // === HERBLORE: CLEAN HERBS & CRUSHED HERBS ===
        "item_clean_greenleaf" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Greenleaf Patch (Lv. 1 Farming)",
            "💀 Slayer: Dropped by Crawling Hands & Cave Slimes",
            "🛒 Shop: Purchased from Herbalist for 100 GP"
        )),
        "item_crushed_greenleaf" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Greenleaf using Pestle & Mortar (Lv. 1 Herblore)",
            "🧪 Used for: Brewing Warrior Elixir (+ Eye of Newt)"
        )),
        "item_clean_meadow_mint" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Meadow Mint Patch (Lv. 8 Farming)",
            "💀 Slayer: Dropped by Cave Crawlers",
            "🛒 Shop: Purchased for 180 GP"
        )),
        "item_crushed_meadow_mint" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Meadow Mint using Pestle & Mortar (Lv. 5 Herblore)",
            "🧪 Used for: Brewing Antidote Draught (+ Unicorn Horn Dust)"
        )),
        "item_clean_wild_thyme" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Wild Thyme Patch (Lv. 18 Farming)",
            "💀 Slayer: Dropped by Cave Bugs",
            "🛒 Shop: Purchased for 280 GP"
        )),
        "item_crushed_wild_thyme" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Wild Thyme using Pestle & Mortar (Lv. 11 Herblore)",
            "🧪 Used for: Brewing Might Potion (+ Limpwurt Root)"
        )),
        "item_clean_lavender" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Lavender Patch (Lv. 28 Farming)",
            "💀 Slayer: Dropped by Desert Bandits",
            "🛒 Shop: Purchased for 450 GP"
        )),
        "item_crushed_lavender" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Lavender using Pestle & Mortar (Lv. 20 Herblore)",
            "🧪 Used for: Brewing Ironhide Tonic (+ White Berries)"
        )),
        "item_clean_sunleaf" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Sunleaf Patch (Lv. 40 Farming)",
            "💀 Slayer: Dropped by Aberrant Spectres & Turoths",
            "🥷 Thieving: Master Farmer high-level pickpocket"
        )),
        "item_crushed_sunleaf" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Sunleaf using Pestle & Mortar (Lv. 25 Herblore)",
            "🧪 Used for: Brewing Divinity Nectar (+ Snape Grass)"
        )),
        "item_clean_ironleaf" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Ironleaf Patch (Lv. 55 Farming)",
            "💀 Slayer: Dropped by Kurasks & Gargoyles"
        )),
        "item_crushed_ironleaf" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Ironleaf using Pestle & Mortar (Lv. 40 Herblore)",
            "🧪 Used for: Brewing High Warrior Elixir (+ Eye of Newt)"
        )),
        "item_clean_wintergreen" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Wintergreen Patch (Lv. 68 Farming)",
            "💀 Slayer: Dropped by Nechryael & Bloodvelds"
        )),
        "item_crushed_wintergreen" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Wintergreen using Pestle & Mortar (Lv. 48 Herblore)",
            "🧪 Used for: Brewing Restoration Elixir (+ Red Spiders' Eggs)"
        )),
        "item_clean_elderberry" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Elderberry Patch (Lv. 74 Farming)",
            "💀 Slayer: Dropped by Abyssal Demons & Wyverns"
        )),
        "item_crushed_elderberry" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Elderberry using Pestle & Mortar (Lv. 55 Herblore)",
            "🧪 Used for: Brewing Titan Might Potion (+ Limpwurt Root)"
        )),
        "item_clean_silverleaf" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Silverleaf Patch (Lv. 78 Farming)",
            "💀 Slayer: Dropped by Dark Beasts & Rune Dragons"
        )),
        "item_crushed_silverleaf" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Silverleaf using Pestle & Mortar (Lv. 65 Herblore)",
            "🧪 Used for: Brewing Aegis Shield Tonic & Sustaining Feast Brew"
        )),
        "item_clean_moonflower" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Moonflower Patch (Lv. 89 Farming)",
            "💀 Slayer: Dropped by Hydra & Cerberus"
        )),
        "item_crushed_moonflower" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Moonflower using Pestle & Mortar (Lv. 73 Herblore)",
            "🧪 Used for: Brewing Swift Expedition Elixir (+ Gold Ore)"
        )),
        "item_clean_chamomile" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Chamomile Patch (Lv. 82 Farming)",
            "💀 Slayer: Dropped by Hydra Spirits & Frost Drakes"
        )),
        "item_crushed_chamomile" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Chamomile using Pestle & Mortar (Lv. 79 Herblore)",
            "🧪 Used for: Brewing Hawkeye Elixir (+ Wine of Shadow)"
        )),
        "item_clean_vervain" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Vervain Patch (Lv. 92 Farming)",
            "💀 Slayer: Dropped by High Wyrms and Ancient Titans"
        )),
        "item_crushed_vervain" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Vervain using Pestle & Mortar (Lv. 81 Herblore)",
            "🧪 Used for: Brewing Celestial Nectar / Shaman Spirit Brew (+ Crushed Bird's Nest)"
        )),
        "item_clean_mystic_sage" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested directly from Mystic Sage Patch (Lv. 85 Farming)",
            "💀 Slayer: Dropped by Moros Apex Sovereign & Spirit Lords",
            "🥷 Thieving: Shaman Elder master pickpocket"
        )),
        "item_crushed_mystic_sage" to (EncyclopediaCategory.HERBLORE to listOf(
            "🥣 Herblore: Ground from Mystic Sage using Pestle & Mortar (Lv. 85 Herblore)",
            "🧪 Used for: Brewing Grand Master Flask & Eternal Vitality Infusion"
        )),
        "item_eye_of_newt" to (EncyclopediaCategory.HERBLORE to listOf(
            "🛒 Herblore Shop: Purchased from Apothecary Mystic Vendors",
            "💀 Slayer: Dropped by Crawling Shades & Cave Creepers"
        )),
        "item_unicorn_horn" to (EncyclopediaCategory.HERBLORE to listOf(
            "🦄 Hunting & Combat: Obtained from Celestial Unicorns",
            "💀 Slayer: Dropped by Cave Creepers"
        )),
        "item_limpwurt_root" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested from Limpwurt Flower Seed (Lv. 26 Farming)",
            "💀 Slayer: Dropped by Mountain Giants & Earth Goblins"
        )),
        "item_white_berries" to (EncyclopediaCategory.HERBLORE to listOf(
            "🌾 Farming: Harvested from Whiteberry Bushes (Lv. 59 Farming)",
            "🐾 Hunter: Foraged in Sylvan Woodlands"
        )),
        "item_attack_potion" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Greenleaf + Eye of Newt in Water Vial (Lv. 3 Herblore)",
            "🛒 Shop: Purchased for 50 GP"
        )),
        "item_antipoison_potion" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Meadow Mint + Unicorn Horn Dust (Lv. 5 Herblore)",
            "🛒 Shop: Purchased for 80 GP"
        )),
        "item_strength_potion" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Wild Thyme + Limpwurt Root (Lv. 12 Herblore)",
            "🛒 Shop: Purchased for 100 GP"
        )),
        "item_defence_potion" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Lavender + White Berries (Lv. 18 Herblore)",
            "🛒 Shop: Purchased for 120 GP"
        )),
        "item_prayer_potion" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Sunleaf + Snape Grass (Lv. 38 Herblore)",
            "🛒 Shop: Purchased for 250 GP"
        )),
        "item_super_attack" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Ironleaf + Eye of Newt (Lv. 45 Herblore)",
            "🛒 Shop: Purchased for 300 GP"
        )),
        "item_super_strength" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Elderberry + Limpwurt Root (Lv. 55 Herblore)",
            "🛒 Shop: Purchased for 400 GP"
        )),
        "item_super_defence" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Silverleaf + White Berries (Lv. 66 Herblore)",
            "🛒 Shop: Purchased for 500 GP"
        )),
        "item_ranging_potion" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Chamomile + Wine of Shadow (Lv. 72 Herblore)",
            "🛒 Shop: Purchased for 600 GP"
        )),
        "item_magic_potion" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Moonflower + Gold Ore (Lv. 76 Herblore)",
            "🛒 Shop: Purchased for 700 GP"
        )),
        "item_saradomin_brew" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Vervain + Crushed Bird's Nest (Lv. 81 Herblore)",
            "🛒 Shop: Purchased for 1,000 GP"
        )),
        "item_super_restore" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Brewed from Crushed Wintergreen + Red Spiders' Eggs (Lv. 63 Herblore)",
            "🛒 Shop: Purchased for 800 GP"
        )),
        "item_overload_potion" to (EncyclopediaCategory.HERBLORE to listOf(
            "🧪 Herblore: Combined Super Sets + Crushed Mystic Sage at Herblore Lab (Lv. 96 Herblore)",
            "🛒 Grand Exchange Shop: Purchased for 5,000 GP"
        )),

        // === RUNES & MAGIC ===
        "item_rune_air" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Air Altar from Rune Essence (Lv. 1 Runecrafting)",
            "🛒 Magic Shop: Purchased from Mystic Magic Shop for 4 GP",
            "🥷 Thieving: Pickpocketed from Citizens"
        )),
        "item_rune_mind" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Mind Altar (Lv. 2 Runecrafting)",
            "🛒 Magic Shop: Purchased for 5 GP",
            "🥷 Thieving: Pickpocketed from Silk Merchants"
        )),
        "item_rune_water" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Water Altar (Lv. 5 Runecrafting)",
            "🛒 Magic Shop: Purchased for 4 GP"
        )),
        "item_rune_earth" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Earth Altar (Lv. 9 Runecrafting)",
            "🛒 Magic Shop: Purchased for 4 GP"
        )),
        "item_rune_fire" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Fire Altar (Lv. 14 Runecrafting)",
            "🛒 Magic Shop: Purchased for 4 GP",
            "💀 Slayer: Dropped by Pyrefiends"
        )),
        "item_rune_body" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Body Altar (Lv. 20 Runecrafting)",
            "🛒 Magic Shop: Purchased for 8 GP"
        )),
        "item_rune_cosmic" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Cosmic Altar in Celestial Realm (Lv. 27 Runecrafting)",
            "🛒 Magic Shop: Purchased for 25 GP",
            "🥷 Thieving: Pickpocketed from Cave Spirits & Wealthy Citizens"
        )),
        "item_rune_chaos" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Chaos Altar (Lv. 35 Runecrafting)",
            "🛒 Magic Shop: Purchased for 50 GP",
            "💀 Slayer: Dropped by Lesser Fiends"
        )),
        "item_rune_astral" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Lunar Astral Altar (Lv. 40 Runecrafting)",
            "🛒 Magic Shop: Purchased for 65 GP",
            "🥷 Thieving: Pickpocketed from Shaman Traders"
        )),
        "item_rune_nature" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Nature Altar in Spirit Grove (Lv. 44 Runecrafting)",
            "🛒 Magic Shop: Purchased for 90 GP",
            "🥷 Thieving: Pickpocketed from Shaman Elders & Spirit Paladins"
        )),
        "item_rune_law" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Law Altar on Sacred Isle (Lv. 54 Runecrafting)",
            "🛒 Magic Shop: Purchased for 120 GP",
            "🥷 Thieving: Pickpocketed from Temple Guards & Royal Knights"
        )),
        "item_rune_death" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Death Altar in Shadow Pass (Lv. 65 Runecrafting)",
            "🛒 Magic Shop: Purchased for 200 GP",
            "💀 Slayer: Dropped by Wraith Spectres & Nether Fiends"
        )),
        "item_rune_blood" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Blood Altar in Ancient Crypts (Lv. 77 Runecrafting)",
            "🛒 Magic Shop: Purchased for 350 GP",
            "🥷 Thieving: Pickpocketed from Shadow Nobles & Dark Mages"
        )),
        "item_rune_soul" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Soul Altar in Spirit Haven (Lv. 90 Runecrafting)",
            "🛒 Magic Shop: Purchased for 500 GP",
            "🥷 Thieving: Pickpocketed from High Adepts & Mystic Mages"
        )),
        "item_rune_wrath" to (EncyclopediaCategory.MAGIC_RUNES to listOf(
            "✨ Runecrafting: Crafted at Wrath Altar in Apex Sanctum (Lv. 95 Runecrafting)",
            "🛒 Magic Shop: Purchased for 800 GP",
            "🥷 Thieving: Pickpocketed from Flame Shaman Guards & Grand Alchemists"
        )),

        // === SLAYER & HUNTER ===
        "item_bones" to (EncyclopediaCategory.SLAYER_HUNTER to listOf(
            "💀 Combat: Dropped by Goblins, Skeletons, and wandering spirits",
            "💀 Slayer: Universal basic bone drop"
        )),
        "item_big_bones" to (EncyclopediaCategory.SLAYER_HUNTER to listOf(
            "💀 Combat: Dropped by Giants, Ogres, and Forest Trolls",
            "💀 Slayer: Dropped by Woodland Behemoths"
        )),
        "item_dragon_bones" to (EncyclopediaCategory.SLAYER_HUNTER to listOf(
            "💀 Combat: Dropped by Forest, Sea, Fire, and Spirit Drakes",
            "💀 Slayer: Dropped by Ancient Frost Wyverns & Cerberus"
        )),
        "item_bird_snare" to (EncyclopediaCategory.SLAYER_HUNTER to listOf(
            "🛠️ Crafting & POH: Crafted from 1 Oak Plank + 1 Iron Nails (Lv. 1 Hunter/Crafting)",
            "🛒 Shop: Purchased for 20 GP"
        )),
        "item_box_trap" to (EncyclopediaCategory.SLAYER_HUNTER to listOf(
            "🛠️ Crafting: Crafted from Willow Planks + Steel Nails (Lv. 27 Hunter)",
            "🛒 Shop: Purchased for 50 GP"
        )),
        "item_noose_wand" to (EncyclopediaCategory.SLAYER_HUNTER to listOf(
            "🛠️ Crafting: Crafted from Teak Logs + String (Lv. 3 Hunter)",
            "🛒 Shop: Purchased for 30 GP"
        )),
        "item_abyssal_whip" to (EncyclopediaCategory.EQUIPMENT to listOf(
            "💀 Slayer: 1/512 Drop from Nether Demons (Lv. 85 Slayer)",
            "🛒 Grand Exchange: Purchased for 1,500,000 GP"
        )),
        "item_dragon_scimitar" to (EncyclopediaCategory.EQUIPMENT to listOf(
            "📜 Quests: Primal Spirits Quest Reward & Tribal vendors unlock",
            "🛒 Grand Exchange: Purchased for 100,000 GP"
        )),
        "item_godsword_blade" to (EncyclopediaCategory.EQUIPMENT to listOf(
            "💀 Boss Dungeons: Combined from 3 Primordial Shards (Spiritual Dungeons)",
            "🛒 Grand Exchange: Purchased for 5,000,000 GP"
        )),
        "item_twisted_bow" to (EncyclopediaCategory.EQUIPMENT to listOf(
            "🏆 Raids: Chambers of Ancients Mega-Rare Unique Drop",
            "👑 Grand Master: Top tier ranged weapon in the game"
        )),
        "item_scythe_of_vitur" to (EncyclopediaCategory.EQUIPMENT to listOf(
            "🏆 Raids: Sanctuary of Shadows Mega-Rare Unique Drop",
            "👑 Grand Master: Top tier 3-target melee weapon"
        )),
        "item_shadow_of_tumeken" to (EncyclopediaCategory.EQUIPMENT to listOf(
            "🏆 Raids: Sunken Pyramid Mega-Rare Unique Drop",
            "👑 Grand Master: Quadruples magic accuracy and magic damage"
        ))
    )

    // Cached base static items map for instant encyclopedia lookups
    private val BASE_ITEMS_MAP: Map<String, EncyclopediaItem> by lazy {
        val resultMap = LinkedHashMap<String, EncyclopediaItem>()

        // 1. Process all standard shop / default items
        DefaultItems.ALL_SHOP_ITEMS.forEach { item ->
            resultMap[item.id] = createEncyclopediaItem(item)
        }

        // 2. Process all Smithing Anvil recipes
        SmithingData.ALL_ANVIL_RECIPES.forEach { recipe ->
            if (!resultMap.containsKey(recipe.outputItemId)) {
                val cat = EncyclopediaCategory.EQUIPMENT
                val obtainList = listOf(
                    "⚒️ Smithing: Forged at Anvil with ${recipe.barsRequired}x ${recipe.metalTier} Bars (Req. Smithing Lv. ${recipe.reqLevel})",
                    "🛒 Grand Exchange: Buyable from other players / shops"
                )
                resultMap[recipe.outputItemId] = EncyclopediaItem(
                    id = recipe.outputItemId,
                    name = recipe.outputItemName,
                    iconEmoji = recipe.iconEmoji,
                    category = cat,
                    description = recipe.description,
                    primaryObtainMethod = obtainList.first(),
                    allObtainMethods = obtainList,
                    costGp = (recipe.reqLevel * 25L).coerceAtLeast(50L),
                    highAlchGp = (recipe.reqLevel * 15L).coerceAtLeast(30L),
                    combatPower = recipe.combatPower,
                    defPower = recipe.defPower,
                    equipmentSlot = recipe.equipSlot,
                    reqSkill = OsrsSkill.SMITHING,
                    reqLevel = recipe.reqLevel
                )
            }
        }

        // 3. Process Fletching Recipes
        FletchingData.ARROW_RECIPES.forEach { recipe ->
            if (!resultMap.containsKey(recipe.outputItemId)) {
                val obtainList = listOf(
                    "🎯 Fletching: Crafted with ${recipe.inputMaterials.joinToString { "${it.quantity}x ${it.itemName}" }} (Req. Lv. ${recipe.reqLevel})",
                    "🛒 Shop: Purchased in Archery Stores"
                )
                resultMap[recipe.outputItemId] = EncyclopediaItem(
                    id = recipe.outputItemId,
                    name = recipe.outputItemName,
                    iconEmoji = recipe.iconEmoji,
                    category = EncyclopediaCategory.WOOD_FLETCHING,
                    description = recipe.description,
                    primaryObtainMethod = obtainList.first(),
                    allObtainMethods = obtainList,
                    costGp = (recipe.reqLevel * 10L).coerceAtLeast(20L),
                    reqSkill = OsrsSkill.FLETCHING,
                    reqLevel = recipe.reqLevel
                )
            }
        }

        // 4. Process Herblore Crushing Recipes & Potions
        HerbloreData.CRUSH_HERB_RECIPES.forEach { recipe ->
            if (!resultMap.containsKey(recipe.herbId)) {
                val obtainList = listOf(
                    "🌾 Farming: Grown from ${recipe.seedName} in herb patch (Req. Lv. ${recipe.reqLevel})",
                    "💀 Slayer: Dropped by cave creatures and dungeon monsters",
                    "🛒 Grand Exchange Shop: Purchased from Master Herbalist"
                )
                resultMap[recipe.herbId] = EncyclopediaItem(
                    id = recipe.herbId,
                    name = recipe.herbName,
                    iconEmoji = recipe.iconEmoji,
                    category = EncyclopediaCategory.HERBLORE,
                    description = "Freshly harvested botanical herb. Grind with a Pestle & Mortar into ${recipe.crushedHerbName}.",
                    primaryObtainMethod = obtainList.first(),
                    allObtainMethods = obtainList,
                    costGp = (recipe.reqLevel * 20L).coerceAtLeast(40L),
                    reqSkill = OsrsSkill.HERBLORE,
                    reqLevel = recipe.reqLevel
                )
            }
            if (!resultMap.containsKey(recipe.crushedHerbId)) {
                val obtainList = listOf(
                    "🥣 Herblore: Ground from ${recipe.herbName} using Pestle & Mortar (Req. Lv. ${recipe.reqLevel})",
                    "🧪 Brewing: Required primary ingredient for potent potion brewing"
                )
                resultMap[recipe.crushedHerbId] = EncyclopediaItem(
                    id = recipe.crushedHerbId,
                    name = recipe.crushedHerbName,
                    iconEmoji = "🥣",
                    category = EncyclopediaCategory.HERBLORE,
                    description = "Finely crushed ${recipe.herbName}. Ready to combine with secondary reagents into elixirs.",
                    primaryObtainMethod = obtainList.first(),
                    allObtainMethods = obtainList,
                    costGp = (recipe.reqLevel * 25L).coerceAtLeast(50L),
                    reqSkill = OsrsSkill.HERBLORE,
                    reqLevel = recipe.reqLevel
                )
            }
        }

        HerbloreData.POTION_RECIPES.forEach { recipe ->
            if (!resultMap.containsKey(recipe.outputPotionId)) {
                val obtainList = listOf(
                    "🧪 Herblore: Brewed from ${recipe.crushedHerbName} + ${recipe.secondaryItemName} in Vial of Water (Req. Herblore Lv. ${recipe.reqLevel})",
                    "🛒 Grand Exchange Shop: Purchased from Apothecary"
                )
                resultMap[recipe.outputPotionId] = EncyclopediaItem(
                    id = recipe.outputPotionId,
                    name = recipe.outputPotionName,
                    iconEmoji = recipe.iconEmoji,
                    category = EncyclopediaCategory.HERBLORE,
                    description = recipe.effectDescription,
                    primaryObtainMethod = obtainList.first(),
                    allObtainMethods = obtainList,
                    costGp = (recipe.reqLevel * 35L).coerceAtLeast(50L),
                    reqSkill = OsrsSkill.HERBLORE,
                    reqLevel = recipe.reqLevel
                )
            }
        }

        // 5. Process Runecrafting Runes
        RunecraftData.CRAFTABLE_RUNES.forEach { rune ->
            if (!resultMap.containsKey(rune.runeItemId)) {
                val obtainList = listOf(
                    "✨ Runecrafting: Infused from Pure Essence at ${rune.runeName.removeSuffix(" Rune")} Altar (Req. Lv. ${rune.reqLevel})",
                    "🛒 Magic Stores: Purchased from Magic Guild",
                    "💀 Slayer: Dropped by wizards and magical beasts"
                )
                resultMap[rune.runeItemId] = EncyclopediaItem(
                    id = rune.runeItemId,
                    name = rune.runeName,
                    iconEmoji = rune.iconEmoji,
                    category = EncyclopediaCategory.MAGIC_RUNES,
                    description = "Concentrated magical catalyst used to cast spells.",
                    primaryObtainMethod = obtainList.first(),
                    allObtainMethods = obtainList,
                    costGp = (rune.reqLevel * 5L).coerceAtLeast(10L),
                    reqSkill = OsrsSkill.RUNECRAFT,
                    reqLevel = rune.reqLevel
                )
            }
        }

        // 6. Process Cauldron Meals
        CauldronRecipes.ALL_RECIPES.forEach { recipe ->
            if (!resultMap.containsKey(recipe.id)) {
                val obtainList = listOf(
                    "🍲 Cauldron Cooking: Cooked in Cauldron using ${recipe.rawItemName} + ${recipe.item2Name} (Req. Cooking Lv. ${recipe.reqLevel})",
                    "🍲 Buff: ${recipe.buffEffect}"
                )
                resultMap[recipe.id] = EncyclopediaItem(
                    id = recipe.id,
                    name = recipe.cookedItemName,
                    iconEmoji = recipe.emoji,
                    category = EncyclopediaCategory.FOOD,
                    description = recipe.description,
                    primaryObtainMethod = obtainList.first(),
                    allObtainMethods = obtainList,
                    healHp = recipe.healthRestored,
                    restoreHunger = recipe.hungerRestored,
                    costGp = (recipe.reqLevel * 15L).coerceAtLeast(60L),
                    reqSkill = OsrsSkill.COOKING,
                    reqLevel = recipe.reqLevel
                )
            }
        }

        // 7. Process Farming Crops & Seeds
        FarmCropType.values().forEach { crop ->
            if (!resultMap.containsKey(crop.seedId)) {
                val obtainList = listOf(
                    "🥷 Thieving: Pickpocketed from Master Farmer or Farmers (Req. Thieving Lv. ${crop.reqFarmingLevel / 2 + 1})",
                    "💀 Slayer: Dropped by low & high tier Slayer monsters",
                    "🛒 Seed Shop: Purchased at Farming Guild"
                )
                resultMap[crop.seedId] = EncyclopediaItem(
                    id = crop.seedId,
                    name = crop.seedName,
                    iconEmoji = crop.seedEmoji,
                    category = EncyclopediaCategory.FARMING,
                    description = "Plantable seed that produces ${crop.produceName} in ${formatGrowthDuration(crop.growthTimeSeconds)}.",
                    primaryObtainMethod = obtainList.first(),
                    allObtainMethods = obtainList,
                    costGp = crop.produceGpVal,
                    reqSkill = OsrsSkill.FARMING,
                    reqLevel = crop.reqFarmingLevel
                )
            }
            if (!resultMap.containsKey(crop.produceItemId)) {
                val obtainList = listOf(
                    "🌾 Farming: Harvested from ${crop.displayName} (Req. Farming Lv. ${crop.reqFarmingLevel})",
                    "🛒 Shop: Sold in Grand Exchange and Food Markets"
                )
                resultMap[crop.produceItemId] = EncyclopediaItem(
                    id = crop.produceItemId,
                    name = crop.produceName,
                    iconEmoji = crop.produceEmoji,
                    category = if (crop.category == SeedCategory.HERB) EncyclopediaCategory.HERBLORE else EncyclopediaCategory.FARMING,
                    description = "Freshly harvested farm produce. Restores ${crop.produceHealHp} HP and ${crop.produceHunger} Hunger.",
                    primaryObtainMethod = obtainList.first(),
                    allObtainMethods = obtainList,
                    healHp = crop.produceHealHp,
                    restoreHunger = crop.produceHunger,
                    costGp = crop.produceGpVal * 2,
                    reqSkill = OsrsSkill.FARMING,
                    reqLevel = crop.reqFarmingLevel
                )
            }
        }

        // 8. Process Skilling Sets & Adventuring Outfit Pieces (Featured Adventuring Rewards & Favor Unlocks)
        SkillOutfitData.ALL_PIECES.forEach { piece ->
            if (!resultMap.containsKey(piece.id)) {
                val skillName = piece.skill.displayName
                val obtainList = listOf(
                    "🎁 Favor Boxes: 10% chance to unlock when opening ${piece.skill.displayName} Favor Boxes",
                    "🗡️ Adventuring: Rewarded upon clearing Dungeon Floors in the Shaman Catacombs (Floor 1-99)",
                    "💰 Favor Dialog: Direct unlock with GP from the Craftsman dialog",
                    "✨ Permanent Buff: Grants permanent +5% $skillName XP passive buff (takes 0 storage slots)"
                )
                resultMap[piece.id] = EncyclopediaItem(
                    id = piece.id,
                    name = piece.name,
                    iconEmoji = piece.iconEmoji,
                    category = EncyclopediaCategory.QUEST_LEAGUE,
                    description = piece.description,
                    primaryObtainMethod = obtainList.first(),
                    allObtainMethods = obtainList,
                    costGp = piece.costGp,
                    highAlchGp = (piece.costGp * 0.6).toLong(),
                    combatPower = 0,
                    defPower = 0,
                    equipmentSlot = null,
                    reqSkill = piece.skill,
                    reqLevel = 1
                )
            }
        }

        resultMap
    }

    private val BASE_ITEMS_SORTED_LIST: List<EncyclopediaItem> by lazy {
        BASE_ITEMS_MAP.values.sortedBy { it.name }
    }

    /**
     * Generates a comprehensive list of all game items with accurate obtain instructions,
     * aggregating predefined entries and runtime database items.
     */
    fun getAllEncyclopediaItems(
        inventoryItems: List<InventoryItem> = emptyList(),
        bankItems: List<InventoryItem> = emptyList(),
        equippedItems: Map<EquipmentSlot, InventoryItem> = emptyMap()
    ): List<EncyclopediaItem> {
        val baseMap = BASE_ITEMS_MAP

        // Fast path: check if any dynamic live items are not already in baseMap
        var hasUnseenLiveItems = false
        val unseenLiveItems = mutableListOf<InventoryItem>()

        for (item in inventoryItems) {
            if (!baseMap.containsKey(item.id)) {
                hasUnseenLiveItems = true
                unseenLiveItems.add(item)
            }
        }
        for (item in bankItems) {
            if (!baseMap.containsKey(item.id)) {
                hasUnseenLiveItems = true
                unseenLiveItems.add(item)
            }
        }
        for (item in equippedItems.values) {
            if (!baseMap.containsKey(item.id)) {
                hasUnseenLiveItems = true
                unseenLiveItems.add(item)
            }
        }

        if (!hasUnseenLiveItems) {
            return BASE_ITEMS_SORTED_LIST
        }

        val resultMap = LinkedHashMap(baseMap)
        unseenLiveItems.forEach { liveItem ->
            if (!resultMap.containsKey(liveItem.id)) {
                resultMap[liveItem.id] = createEncyclopediaItem(liveItem)
            }
        }

        return resultMap.values.sortedBy { it.name }
    }

    private fun createEncyclopediaItem(item: InventoryItem): EncyclopediaItem {
        val curated = CURATED_OBTAIN_MAP[item.id]
        val cat = curated?.first ?: mapCategory(item)
        val obtainMethods = curated?.second ?: generateDynamicObtainMethods(item, cat)

        return EncyclopediaItem(
            id = item.id,
            name = item.name,
            iconEmoji = item.iconEmoji,
            category = cat,
            description = item.description.ifBlank { "Valuable item used in adventures and skilling." },
            primaryObtainMethod = obtainMethods.firstOrNull() ?: "🛒 Grand Exchange & Monster Drops",
            allObtainMethods = obtainMethods,
            costGp = item.costGp,
            highAlchGp = (item.costGp * 0.6).toLong(),
            healHp = item.healHp,
            restoreHunger = item.restoreHunger,
            combatPower = item.combatPowerBonus,
            defPower = item.defPowerBonus,
            equipmentSlot = item.equipmentSlot,
            reqSkill = item.bonusXpSkill ?: SkillItemRegistry.getItemSkill(item),
            reqLevel = 1
        )
    }

    private fun mapCategory(item: InventoryItem): EncyclopediaCategory {
        val id = item.id.lowercase()
        val name = item.name.lowercase()

        return when {
            item.category == ItemCategory.EQUIPMENT || item.equipmentSlot != null ||
                    id.contains("sword") || id.contains("bow") || id.contains("armor") || id.contains("shield") ||
                    id.contains("helm") || id.contains("plate") || id.contains("legs") || id.contains("boots") ||
                    id.contains("gloves") || id.contains("ring") || id.contains("amulet") || id.contains("cape") ||
                    id.contains("staff") || id.contains("whip") || id.contains("dagger") -> EncyclopediaCategory.EQUIPMENT

            item.category == ItemCategory.FOOD || id.contains("raw_") || id.contains("cooked") ||
                    id.contains("stew") || id.contains("pie") || id.contains("cake") || id.contains("fish") ||
                    id.contains("lobster") || id.contains("shark") || id.contains("bread") -> EncyclopediaCategory.FOOD

            item.category == ItemCategory.POTION || id.contains("potion") || id.contains("brew") ||
                    id.contains("herb") || id.contains("crushed") || id.contains("clean_") ||
                    id.contains("vial") || id.contains("elixir") -> EncyclopediaCategory.HERBLORE

            item.category == ItemCategory.LOGS_WOOD || id.contains("log") || id.contains("wood") ||
                    id.contains("arrow") || id.contains("shaft") || id.contains("fletch") ||
                    id.contains("timber") || id.contains("plank") -> EncyclopediaCategory.WOOD_FLETCHING

            item.category == ItemCategory.BARS_ORES || id.contains("ore") || id.contains("bar") ||
                    id.contains("gem") || id.contains("uncut") || id.contains("sapphire") ||
                    id.contains("ruby") || id.contains("diamond") || id.contains("emerald") ||
                    id.contains("coal") -> EncyclopediaCategory.MINING_SMITHING

            item.category == ItemCategory.SEEDS || item.category == ItemCategory.HERBS_FARMING ||
                    id.contains("seed") || id.contains("sapling") || id.contains("patch") ||
                    id.contains("crop") -> EncyclopediaCategory.FARMING

            item.category == ItemCategory.RUNES_MAGIC || id.contains("rune") || id.contains("talisman") ||
                    id.contains("tiara") || id.contains("spell") || id.contains("tome") -> EncyclopediaCategory.MAGIC_RUNES

            item.category == ItemCategory.BONES || id.contains("bone") || id.contains("slayer") ||
                    id.contains("trap") || id.contains("snare") || id.contains("kebbit") ||
                    id.contains("chinchompa") || id.contains("pelt") -> EncyclopediaCategory.SLAYER_HUNTER

            id.contains("effigy") || id.contains("energy") || id.contains("totem") ||
                    id.contains("pouch") || id.contains("shard") || id.contains("memory") -> EncyclopediaCategory.DIVINATION_SUMMONING

            item.category == ItemCategory.CONSTRUCTION || id.contains("nails") || id.contains("gizmo") ||
                    id.contains("craft") || id.contains("poh") || id.contains("brick") ||
                    id.contains("saw") -> EncyclopediaCategory.WORKSHOP_POH

            id.contains("badge") || id.contains("trophy") || id.contains("quest") ||
                    id.contains("scroll") || id.contains("card") || id.contains("relic") -> EncyclopediaCategory.QUEST_LEAGUE

            else -> EncyclopediaCategory.ALL
        }
    }

    fun getEncyclopediaItem(
        itemId: String,
        inventoryItems: List<InventoryItem> = emptyList(),
        bankItems: List<InventoryItem> = emptyList(),
        equippedItems: Map<EquipmentSlot, InventoryItem> = emptyMap()
    ): EncyclopediaItem {
        val baseMap = BASE_ITEMS_MAP
        val normalizedId = DefaultItems.normalizeItemId(itemId)
        baseMap[itemId]?.let { return it }
        baseMap[normalizedId]?.let { return it }

        val database = getAllEncyclopediaItems(inventoryItems, bankItems, equippedItems)
        val exact = database.find { 
            it.id == itemId || it.id == normalizedId || 
            it.name.equals(itemId, ignoreCase = true) || 
            it.id.equals("item_" + itemId.lowercase().replace(" ", "_"), ignoreCase = true) 
        }
        if (exact != null) return exact

        // Fallback: search by name in BASE_ITEMS_MAP
        val nameMatch = baseMap.values.find { 
            it.name.equals(itemId, ignoreCase = true) || 
            it.name.replace(" ", "_").equals(itemId, ignoreCase = true) 
        }
        if (nameMatch != null) return nameMatch

        // Fallback: build from InventoryItem directly
        val rawItem = DefaultItems.getItemById(itemId)
        return createEncyclopediaItem(rawItem)
    }

    private fun generateDynamicObtainMethods(item: InventoryItem, category: EncyclopediaCategory): List<String> {
        val id = item.id.lowercase()
        val name = item.name.lowercase()

        val list = mutableListOf<String>()

        if (id.contains("shaft") || name.contains("shaft")) {
            list.add("🪵 Fletching Tab: Whittled from wood logs at Whittling Bench (1 Log ➔ 15 Shafts)")
            list.add("🎯 Fletching Tab: Carved with knife from Normal, Oak, Willow, or Maple Logs")
            list.add("🛒 Archery Shop: Purchased for 2 GP each")
            return list
        }
        if (id.contains("stick") || name.contains("stick")) {
            list.add("🛠️ Crafting: Carved at Wooden Sticks Bench (1 Log ➔ 4 Sticks, +25 Crafting XP)")
            list.add("🌲 The Grove: Foraged from forest ground and underbrush")
            list.add("🛒 General Store: Purchased for 5 GP")
            return list
        }
        if (id.contains("feather") || name.contains("feather")) {
            list.add("🪤 Hunter Tab: Trapped from birds using Bird Snares (10-45 Feathers per catch)")
            list.add("🛠️ Workshop: Plucked / Prepared at Workshop from fibers (1 Log / 1 Fiber ➔ 15 Feathers)")
            list.add("🛒 General Store & Archery Shop: Purchased in Feather Packs")
            return list
        }
        if (id.contains("bowstring") || name.contains("bowstring")) {
            list.add("🧵 Crafting Tab: Spun at Spinning Wheel from Flax / Plant Fiber (1 Flax ➔ 1 Bowstring)")
            list.add("🌾 Farming Tab: Harvested flax spun into bowstrings")
            list.add("🛒 Crafting Shop: Purchased for 35 GP")
            return list
        }
        if (id.contains("arrowtip") || name.contains("arrowtip") || name.contains("arrow tip")) {
            list.add("🔨 Smithing Tab: Forged at Metal Arrowtips Anvil from metal bars (1 Bar ➔ 15 Arrowtips)")
            list.add("⚒️ Smithing Tab: Smelted and forged at Anvils")
            list.add("🛒 Archery Shop: Available in bulk packs")
            return list
        }
        if (id.contains("plank") || name.contains("plank")) {
            list.add("🪚 Workshop Tab: Milled from wood logs at Sawmill (+Crafting XP)")
            list.add("🌲 The Grove: Processed wood supplies")
            list.add("🛒 Lumberyard: Purchased from Sawmill Operator")
            return list
        }
        if (id.contains("nail") || name.contains("nail")) {
            list.add("🔨 Smithing Tab: Forged from metal bars at Nails Anvil (1 Bar ➔ 15 Nails)")
            list.add("⚒️ Smithing Tab: Smelted and hammered at Anvil")
            return list
        }

        when (category) {
            EncyclopediaCategory.EQUIPMENT -> {
                list.add("⚒️ Smithing / Crafting: Forged at Anvil or Crafted in Workshop")
                list.add("💀 Slayer & Bosses: Dropped by dangerous dungeon monsters")
                list.add("🛒 Grand Exchange: Purchased in Armor & Weapon shops")
            }
            EncyclopediaCategory.FOOD -> {
                list.add("🍲 Cooking: Prepared at range or campfire from raw ingredients")
                list.add("🎣 Fishing: Caught with rod, net, or harpoon at fishing spots")
                list.add("🛒 Food Stores: Purchased from chefs and food merchants")
            }
            EncyclopediaCategory.HERBLORE -> {
                list.add("🧪 Herblore: Brewed using clean herbs and secondary ingredients")
                list.add("🌾 Farming: Grown from herb seeds in farm patches")
                list.add("💀 Slayer: Dropped by aberrant spectres and cave monsters")
            }
            EncyclopediaCategory.WOOD_FLETCHING -> {
                list.add("🪓 Woodcutting: Chopped from trees across the spirit realms")
                list.add("🎯 Fletching: Whittled and strung with knife and bowstrings")
            }
            EncyclopediaCategory.MINING_SMITHING -> {
                list.add("⛏️ Mining: Extracted from rock veins and quarries")
                list.add("⚒️ Smelting: Smelted at furnace using high heat")
            }
            EncyclopediaCategory.FARMING -> {
                list.add("🌾 Farming: Planted and harvested in Player Owned Farm patches")
                list.add("🥷 Thieving: Pickpocketed from Master Farmers")
            }
            EncyclopediaCategory.MAGIC_RUNES -> {
                list.add("✨ Runecrafting: Crafted from Rune Essence at elemental altars")
                list.add("🛒 Magic Shops: Purchased from wizards and rune vendors")
            }
            EncyclopediaCategory.SLAYER_HUNTER -> {
                list.add("💀 Slayer Tasks: Dropped by assigned Slayer creatures")
                list.add("🐾 Hunter: Trapped using box traps, bird snares, or tracking")
            }
            EncyclopediaCategory.DIVINATION_SUMMONING -> {
                list.add("🔮 Divination: Harvested from divination energy rifts")
                list.add("✨ Summoning: Infused with spirit charms and pouches")
            }
            EncyclopediaCategory.WORKSHOP_POH -> {
                list.add("🛠️ Workshop: Assembled at Workstations, Sawmill & Anvils")
                list.add("🏡 POH: Crafted for Player-Owned House rooms")
            }
            EncyclopediaCategory.QUEST_LEAGUE -> {
                list.add("📜 Quests: Rewarded upon completing spiritual quests")
                list.add("🏆 League: Earned through Trainer League achievements")
            }
            else -> {
                list.add("🛒 Shops & Markets: Purchased from merchant vendors")
                list.add("💀 Combat Drops: Dropped by dungeon creatures across the realm")
            }
        }
        return list
    }
}
