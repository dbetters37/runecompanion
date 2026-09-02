package com.example.data.models

data class DropTableItem(
    val itemId: String,
    val itemName: String,
    val iconEmoji: String,
    val minQty: Int = 1,
    val maxQty: Int = 1,
    val chancePercent: Int = 100, // 1 to 100
    val costGpValue: Long = 0L
)

data class SlayerMonster(
    val id: String,
    val name: String,
    val reqSlayerLevel: Int,
    val maxHp: Int,
    val xpReward: Long,
    val iconSymbol: String,
    val description: String,
    val drops: List<DropTableItem>,
    val reqQuestId: String? = null,
    val reqQuestName: String? = null
)

data class HunterCreature(
    val id: String,
    val name: String,
    val reqHunterLevel: Int,
    val trapType: String,
    val requiredTrapItemId: String,
    val requiredTrapName: String,
    val xpReward: Long,
    val iconSymbol: String,
    val description: String,
    val drops: List<DropTableItem>,
    val reqQuestId: String? = null,
    val reqQuestName: String? = null
)

object SlayerData {
    val MONSTERS = listOf(
        SlayerMonster(
            id = "crawling_hand",
            name = "Crawling Hand",
            reqSlayerLevel = 1,
            maxHp = 20,
            xpReward = 25L,
            iconSymbol = "🖐️",
            description = "A severed hand crawling around the Bounty Hunter Tower.",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_potato_seed", "Potato Seed", "🌱", 1, 2, 60),
                DropTableItem("item_rune_air", "Air Runes", "💨", 15, 35, 60),
                DropTableItem("item_bread", "Fresh Bread", "🍞", 1, 2, 70),
                DropTableItem("item_eye_of_newt", "Eye of Newt", "👁️", 2, 5, 80),
                DropTableItem("item_coins_100", "Coins", "🪙", 50, 150, 100)
            )
        ),
        SlayerMonster(
            id = "cave_crawler",
            name = "Cave Crawler",
            reqSlayerLevel = 3,
            maxHp = 25,
            xpReward = 35L,
            iconSymbol = "🐛",
            description = "Poisonous multi-legged bug in Fremennik Caves.",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_onion_seed", "Onion Seed", "🌱", 1, 2, 60),
                DropTableItem("item_unicorn_horn", "Unicorn Horn Dust", "🦄", 1, 3, 75),
                DropTableItem("item_eye_of_newt", "Eye of Newt", "👁️", 1, 3, 70),
                DropTableItem("item_raw_trout", "Raw Trout", "🐟", 1, 2, 80)
            )
        ),
        SlayerMonster(
            id = "cave_slime",
            name = "Cave Slime",
            reqSlayerLevel = 5,
            maxHp = 28,
            xpReward = 40L,
            iconSymbol = "🟢",
            description = "Oozing green blob in Lumbridge Swamps.",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_cabbage_seed", "Cabbage Seed", "🥬", 1, 2, 60),
                DropTableItem("item_limpwurt_root", "Limpwurt Root", "🪵", 1, 3, 75),
                DropTableItem("item_tin_ore", "Tin Ore", "🪨", 1, 3, 70)
            )
        ),
        SlayerMonster(
            id = "cave_bug",
            name = "Cave Bug",
            reqSlayerLevel = 7,
            maxHp = 30,
            xpReward = 45L,
            iconSymbol = "🐞",
            description = "Giant cave pest found in Lumbridge Swamp Caves.",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_meadow_mint_seed", "Meadow Mint Seed", "🌱", 1, 2, 60),
                DropTableItem("item_rune_mind", "Mind Runes", "🧠", 10, 25, 60),
                DropTableItem("item_trout", "Cooked Trout", "🐟", 1, 2, 60),
                DropTableItem("item_copper_ore", "Copper Ore", "🪨", 1, 3, 50)
            )
        ),
        SlayerMonster(
            id = "ghostly_fiend",
            name = "Ghostly Fiend",
            reqSlayerLevel = 10,
            maxHp = 40,
            xpReward = 60L,
            iconSymbol = "👻",
            description = "Haunted specter haunting the haunted woods.",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_rune_water", "Water Runes", "💧", 10, 20, 60),
                DropTableItem("item_iron_ore", "Iron Ore", "🪨", 1, 3, 60),
                DropTableItem("item_onion_seed", "Onion Seed", "🌱", 1, 2, 50)
            )
        ),
        SlayerMonster(
            id = "rockslug",
            name = "Rockslug",
            reqSlayerLevel = 12,
            maxHp = 50,
            xpReward = 85L,
            iconSymbol = "🐌",
            description = "A tough slug made of solid rock. Finish with salt!",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_wild_thyme_seed", "Wild Thyme Seed", "🌱", 1, 2, 60),
                DropTableItem("item_rune_earth", "Earth Runes", "🪨", 15, 30, 60),
                DropTableItem("item_coal_ore", "Coal", "🪨", 2, 5, 70),
                DropTableItem("item_bronze_bar", "Bronze Bar", "🧈", 1, 2, 50)
            )
        ),
        SlayerMonster(
            id = "banshee",
            name = "Banshee",
            reqSlayerLevel = 15,
            maxHp = 55,
            xpReward = 95L,
            iconSymbol = "🧕",
            description = "Wailing spirit requiring earmuffs to fight.",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_rune_earth", "Earth Runes", "🪨", 15, 30, 60),
                DropTableItem("item_iron_bar", "Iron Bar", "🧈", 1, 3, 60),
                DropTableItem("item_cabbage_seed", "Cabbage Seed", "🥬", 1, 3, 50)
            )
        ),
        SlayerMonster(
            id = "pyrefiend",
            name = "Pyrefiend",
            reqSlayerLevel = 18,
            maxHp = 70,
            xpReward = 120L,
            iconSymbol = "🔥",
            description = "Fiery fiend roaming dungeon depths.",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_rune_fire", "Fire Runes", "🔥", 15, 30, 60),
                DropTableItem("item_steel_bar", "Steel Bar", "🧈", 1, 2, 60),
                DropTableItem("item_tomato_seed", "Tomato Seed", "🍅", 1, 2, 45),
                DropTableItem("item_obsidian_cape", "Obsidian Cloak", "🌋", 1, 1, 5)
            )
        ),
        SlayerMonster(
            id = "harpie_swarm",
            name = "Harpie Bug Swarm",
            reqSlayerLevel = 20,
            maxHp = 75,
            xpReward = 135L,
            iconSymbol = "🐝",
            description = "Deadly swarm of buzzing bugs requiring a Lit Bug Lantern.",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_rune_fire", "Fire Runes", "🔥", 15, 30, 60),
                DropTableItem("item_iron_ore", "Iron Ore", "🪨", 2, 4, 70),
                DropTableItem("item_cabbage_seed", "Cabbage Seed", "🥬", 1, 3, 50)
            )
        ),
        SlayerMonster(
            id = "wall_beast",
            name = "Wall Beast",
            reqSlayerLevel = 22,
            maxHp = 80,
            xpReward = 145L,
            iconSymbol = "🧱",
            description = "Monster ambushing adventurers from cave walls. Wear a Spiny Helmet!",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_sweetcorn_seed", "Sweetcorn Seed", "🌽", 1, 2, 60),
                DropTableItem("item_rune_body", "Body Runes", "🛡️", 15, 30, 60),
                DropTableItem("item_coal_ore", "Coal", "🪨", 2, 5, 60),
                DropTableItem("item_mithril_ore", "Mithril Ore", "🪨", 1, 2, 40)
            )
        ),
        SlayerMonster(
            id = "cockatrice",
            name = "Cockatrice",
            reqSlayerLevel = 25,
            maxHp = 90,
            xpReward = 160L,
            iconSymbol = "🐓",
            description = "Feathered beast with a petrifying gaze.",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_iron_bar", "Iron Bar", "🧈", 1, 3, 60),
                DropTableItem("item_sweetcorn_seed", "Sweetcorn Seed", "🌽", 1, 3, 50)
            )
        ),
        SlayerMonster(
            id = "fever_spider",
            name = "Fever Spider",
            reqSlayerLevel = 28,
            maxHp = 100,
            xpReward = 185L,
            iconSymbol = "🕷️",
            description = "Diseased arachnid on Braindeath Island. Requires Bounty Hunter Gloves!",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_mithril_bar", "Mithril Bar", "🧈", 1, 2, 50),
                DropTableItem("item_sweetcorn_seed", "Sweetcorn Seed", "🌽", 1, 3, 50)
            )
        ),
        SlayerMonster(
            id = "infernal_mage",
            name = "Infernal Mage",
            reqSlayerLevel = 30,
            maxHp = 110,
            xpReward = 210L,
            iconSymbol = "🧙",
            description = "Dark wizard practicing forbidden flame spells.",
            drops = listOf(
                DropTableItem("item_bones", "Bones", "🦴", 1, 1, 100),
                DropTableItem("item_steel_bar", "Steel Bar", "🧈", 2, 4, 65),
                DropTableItem("item_tomato_seed", "Tomato Seed", "🍅", 2, 4, 50)
            )
        ),
        SlayerMonster(
            id = "brine_rat",
            name = "Brine Rat",
            reqSlayerLevel = 33,
            maxHp = 120,
            xpReward = 230L,
            iconSymbol = "🐀",
            description = "Salty rodent dwelling in Brine Sabre Caverns.",
            drops = listOf(
                DropTableItem("item_big_bones", "Big Bones", "🦴", 1, 1, 100),
                DropTableItem("item_raw_salmon", "Raw Salmon", "🐟", 2, 4, 80),
                DropTableItem("item_coal_ore", "Coal", "🪨", 3, 6, 60)
            )
        ),
        SlayerMonster(
            id = "ankou",
            name = "Ankou",
            reqSlayerLevel = 35,
            maxHp = 130,
            xpReward = 260L,
            iconSymbol = "💀",
            description = "Skeletal nightmare roaming the Stronghold of Security.",
            drops = listOf(
                DropTableItem("item_big_bones", "Big Bones", "🦴", 1, 1, 100),
                DropTableItem("item_adamant_ore", "Adamantite Ore", "🪨", 1, 3, 50),
                DropTableItem("item_sunleaf_seed", "Sunleaf Seed", "🌿", 1, 2, 40)
            )
        ),
        SlayerMonster(
            id = "molanisk",
            name = "Molanisk",
            reqSlayerLevel = 38,
            maxHp = 140,
            xpReward = 290L,
            iconSymbol = "🦡",
            description = "Subterranean creature in Dorgesh-Kaan caves.",
            drops = listOf(
                DropTableItem("item_big_bones", "Big Bones", "🦴", 1, 1, 100),
                DropTableItem("item_mithril_bar", "Mithril Bar", "🧈", 1, 3, 60),
                DropTableItem("item_coal_ore", "Coal", "🪨", 4, 8, 70)
            )
        ),
        SlayerMonster(
            id = "basilisk",
            name = "Basilisk",
            reqSlayerLevel = 40,
            maxHp = 150,
            xpReward = 330L,
            iconSymbol = "🐍",
            description = "Fierce serpent requiring a Mirror Shield.",
            drops = listOf(
                DropTableItem("item_big_bones", "Big Bones", "🦴", 1, 1, 100),
                DropTableItem("item_mithril_ore", "Mithril Ore", "🪨", 2, 4, 65),
                DropTableItem("item_rune_scimitar", "Rune Scimitar", "🗡️", 1, 1, 15)
            )
        ),
        SlayerMonster(
            id = "sea_snake",
            name = "Sea Snake",
            reqSlayerLevel = 42,
            maxHp = 160,
            xpReward = 360L,
            iconSymbol = "🐍",
            description = "Aquatic serpent in Miscellania dungeons.",
            drops = listOf(
                DropTableItem("item_big_bones", "Big Bones", "🦴", 1, 1, 100),
                DropTableItem("item_raw_lobster", "Raw Lobster", "🦞", 2, 4, 80),
                DropTableItem("item_adamant_ore", "Adamantite Ore", "🪨", 1, 3, 50)
            )
        ),
        SlayerMonster(
            id = "jelly",
            name = "Jelly",
            reqSlayerLevel = 45,
            maxHp = 180,
            xpReward = 400L,
            iconSymbol = "🍧",
            description = "Translucent gelatinous monster in Fremennik Caves.",
            drops = listOf(
                DropTableItem("item_big_bones", "Big Bones", "🦴", 1, 1, 100),
                DropTableItem("item_steel_bar", "Steel Bar", "🧈", 3, 6, 70),
                DropTableItem("item_strawberry_seed", "Strawberry Seed", "🍓", 1, 3, 50)
            )
        ),
        SlayerMonster(
            id = "cave_horror",
            name = "Cave Horror",
            reqSlayerLevel = 48,
            maxHp = 190,
            xpReward = 450L,
            iconSymbol = "👹",
            description = "Terrifying beast on Mos Le'Harmless. Drops Black Mask!",
            drops = listOf(
                DropTableItem("item_big_bones", "Big Bones", "🦴", 1, 1, 100),
                DropTableItem("item_adamant_bar", "Adamant Bar", "🧈", 1, 2, 50),
                DropTableItem("item_slayer_helmet", "Black Mask", "🎭", 1, 1, 12)
            ),
            reqQuestId = "quest_nature_spirit",
            reqQuestName = "Nature Spirit"
        ),
        SlayerMonster(
            id = "bloodveld",
            name = "Bloodveld",
            reqSlayerLevel = 50,
            maxHp = 210,
            xpReward = 520L,
            iconSymbol = "🩸",
            description = "Grotesque vampyric beast hungering for blood.",
            drops = listOf(
                DropTableItem("item_big_bones", "Big Bones", "🦴", 1, 1, 100),
                DropTableItem("item_lobster", "Cooked Lobster", "🦞", 2, 4, 70),
                DropTableItem("item_rune_kiteshield", "Runite Vanguard Shield", "🛡️", 1, 1, 20)
            )
        ),
        SlayerMonster(
            id = "catablepon",
            name = "Catablepon",
            reqSlayerLevel = 52,
            maxHp = 220,
            xpReward = 560L,
            iconSymbol = "🐂",
            description = "Bull-like creature draining adventurer stats.",
            drops = listOf(
                DropTableItem("item_big_bones", "Big Bones", "🦴", 1, 1, 100),
                DropTableItem("item_rune_law", "Law Runes", "⚖️", 10, 25, 60),
                DropTableItem("item_adamant_ore", "Adamantite Ore", "🪨", 2, 4, 60),
                DropTableItem("item_coal_ore", "Coal", "🪨", 5, 10, 75)
            )
        ),
        SlayerMonster(
            id = "turoth",
            name = "Turoth",
            reqSlayerLevel = 55,
            maxHp = 240,
            xpReward = 620L,
            iconSymbol = "🦏",
            description = "Heavy armored beast requiring Leaf-bladed weapons or Broad bolts.",
            drops = listOf(
                DropTableItem("item_big_bones", "Big Bones", "🦴", 1, 1, 100),
                DropTableItem("item_rune_law", "Law Runes", "⚖️", 10, 25, 60),
                DropTableItem("item_adamant_bar", "Adamant Bar", "🧈", 1, 3, 60),
                DropTableItem("item_sunleaf_seed", "Sunleaf Seed", "🌿", 1, 2, 45)
            )
        ),
        SlayerMonster(
            id = "mutated_zygomite",
            name = "Mutated Zygomite",
            reqSlayerLevel = 58,
            maxHp = 250,
            xpReward = 680L,
            iconSymbol = "🍄",
            description = "Sentient fungal monster. Finish with Fungicide Spray!",
            drops = listOf(
                DropTableItem("item_big_bones", "Big Bones", "🦴", 1, 1, 100),
                DropTableItem("item_rune_law", "Law Runes", "⚖️", 10, 25, 60),
                DropTableItem("item_watermelon_seed", "Watermelon Seed", "🍉", 1, 3, 60),
                DropTableItem("item_sunleaf_seed", "Sunleaf Seed", "🌿", 1, 2, 50)
            )
        ),
        SlayerMonster(
            id = "aberrant_spectre",
            name = "Aberrant Spectre",
            reqSlayerLevel = 60,
            maxHp = 270,
            xpReward = 780L,
            iconSymbol = "🧟",
            description = "Noxious ghost dropping valuable seeds and herbs.",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 1, 1, 100),
                DropTableItem("item_rune_death", "Death Runes", "💀", 10, 25, 50),
                DropTableItem("item_adamant_ore", "Adamantite Ore", "🪨", 2, 5, 60),
                DropTableItem("item_sunleaf_seed", "Sunleaf Seed", "🌿", 1, 3, 55)
            )
        ),
        SlayerMonster(
            id = "spiritual_ranger",
            name = "Spiritual Ranger",
            reqSlayerLevel = 63,
            maxHp = 290,
            xpReward = 850L,
            iconSymbol = "🏹",
            description = "Ghostly archer in God Wars Dungeon.",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 1, 1, 100),
                DropTableItem("item_rune_death", "Death Runes", "💀", 10, 25, 50),
                DropTableItem("item_adamant_bar", "Adamant Bar", "🟢", 2, 4, 60),
                DropTableItem("item_dragon_darts", "Dragon Darts", "🎯", 10, 25, 40)
            )
        ),
        SlayerMonster(
            id = "dust_devil",
            name = "Dust Devil",
            reqSlayerLevel = 65,
            maxHp = 330,
            xpReward = 980L,
            iconSymbol = "🌪️",
            description = "Swirling sand monster found in Smoke Dungeon.",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 1, 1, 100),
                DropTableItem("item_rune_death", "Death Runes", "💀", 10, 25, 50),
                DropTableItem("item_adamant_bar", "Adamant Bar", "🟢", 2, 4, 65),
                DropTableItem("item_dragon_boots", "Dragon Boots", "👢", 1, 1, 15),
                DropTableItem("item_rune_chaos", "Chaos Runes", "💥", 20, 60, 85),
                DropTableItem("item_rune_nature", "Nature Runes", "🌿", 15, 35, 80)
            )
        ),
        SlayerMonster(
            id = "wyrm",
            name = "Wyrm",
            reqSlayerLevel = 68,
            maxHp = 360,
            xpReward = 1100L,
            iconSymbol = "🐉",
            description = "Draconic reptile in Karuulm Bounty Hunter Dungeon.",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 1, 2, 100),
                DropTableItem("item_rune_death", "Death Runes", "💀", 10, 25, 50),
                DropTableItem("item_runite_ore", "Runite Ore", "💙", 1, 3, 50),
                DropTableItem("item_dragon_scimitar", "Dragon Scimitar", "🗡️", 1, 1, 12)
            )
        ),
        SlayerMonster(
            id = "kurask",
            name = "Kurask",
            reqSlayerLevel = 70,
            maxHp = 390,
            xpReward = 1250L,
            iconSymbol = "🦍",
            description = "Hulking beast requiring Leaf-bladed weapons.",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 1, 2, 100),
                DropTableItem("item_rune_blood", "Blood Runes", "🩸", 10, 25, 50),
                DropTableItem("item_adamant_bar", "Adamant Bar", "🟢", 2, 5, 70),
                DropTableItem("item_mystic_sage_seed", "Mystic Sage Seed", "🌱", 1, 2, 40)
            )
        ),
        SlayerMonster(
            id = "skeletal_wyvern",
            name = "Skeletal Wyvern",
            reqSlayerLevel = 72,
            maxHp = 410,
            xpReward = 1350L,
            iconSymbol = "🐲",
            description = "Icy dragon skeleton requiring Mind Shield.",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 2, 3, 100),
                DropTableItem("item_rune_blood", "Blood Runes", "🩸", 15, 30, 50),
                DropTableItem("item_runite_ore", "Runite Ore", "💙", 2, 4, 60),
                DropTableItem("item_granite_maul", "Granite Longsword", "⚔️", 1, 1, 15)
            )
        ),
        SlayerMonster(
            id = "gargoyle",
            name = "Gargoyle",
            reqSlayerLevel = 75,
            maxHp = 440,
            xpReward = 1500L,
            iconSymbol = "🗿",
            description = "Stone guardian. Smash with Rock Hammer!",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 1, 2, 100),
                DropTableItem("item_rune_blood", "Blood Runes", "🩸", 15, 30, 50),
                DropTableItem("item_granite_maul", "Granite Maul", "🔨", 1, 1, 15),
                DropTableItem("item_rune_bar", "Rune Bar", "💙", 2, 4, 50)
            ),
            reqQuestId = "quest_priest_in_peril",
            reqQuestName = "Priest in Peril"
        ),
        SlayerMonster(
            id = "drake",
            name = "Drake",
            reqSlayerLevel = 78,
            maxHp = 470,
            xpReward = 1700L,
            iconSymbol = "🐊",
            description = "Wingless draconic terror in Mt Karuulm.",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 2, 3, 100),
                DropTableItem("item_rune_blood", "Blood Runes", "🩸", 15, 30, 50),
                DropTableItem("item_runite_ore", "Runite Ore", "💙", 2, 5, 55),
                DropTableItem("item_dragon_boots", "Drake Tooth", "🦴", 1, 1, 15)
            )
        ),
        SlayerMonster(
            id = "nechryael",
            name = "Nechryael",
            reqSlayerLevel = 80,
            maxHp = 500,
            xpReward = 1900L,
            iconSymbol = "👿",
            description = "Demonic entity summoning death spawns.",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 2, 3, 100),
                DropTableItem("item_rune_blood", "Blood Runes", "🩸", 15, 30, 50),
                DropTableItem("item_rune_bar", "Rune Bar", "💙", 2, 4, 60),
                DropTableItem("item_rune_platelegs", "Rune Boots", "👢", 1, 1, 20)
            ),
            reqQuestId = "quest_priest_in_peril",
            reqQuestName = "Priest in Peril"
        ),
        SlayerMonster(
            id = "spiritual_mage",
            name = "Spiritual Mage",
            reqSlayerLevel = 83,
            maxHp = 530,
            xpReward = 2100L,
            iconSymbol = "🔮",
            description = "Spellcasting ghost in God Wars Dungeon. Drops Dragon Boots!",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 2, 3, 100),
                DropTableItem("item_rune_blood", "Blood Runes", "🩸", 15, 30, 50),
                DropTableItem("item_dragon_boots", "Dragon Boots", "👢", 1, 1, 25),
                DropTableItem("item_rune_platebody", "Runite Vanguard Platebody", "🛡️", 1, 1, 20)
            )
        ),
        SlayerMonster(
            id = "abyssal_demon",
            name = "Abyssal Demon",
            reqSlayerLevel = 85,
            maxHp = 570,
            xpReward = 2400L,
            iconSymbol = "😈",
            description = "Teleporting demon from Abyss. Drops Abyssal Whip!",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 2, 4, 100),
                DropTableItem("item_rune_blood", "Blood Runes", "🩸", 20, 40, 50),
                DropTableItem("item_abyssal_whip", "Abyssal Whip", "🪢", 1, 1, 12),
                DropTableItem("item_shark", "Cooked Shark", "🦈", 3, 6, 70),
                DropTableItem("item_rune_law", "Law Runes", "⚖️", 15, 45, 80),
                DropTableItem("item_rune_death", "Death Runes", "💀", 20, 50, 85)
            ),
            reqQuestId = "quest_priest_in_peril",
            reqQuestName = "Priest in Peril"
        ),
        SlayerMonster(
            id = "cave_kraken",
            name = "Cave Kraken",
            reqSlayerLevel = 88,
            maxHp = 620,
            xpReward = 2800L,
            iconSymbol = "🐙",
            description = "Aquatic leviathan in Kraken Cove.",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 2, 4, 100),
                DropTableItem("item_runite_ore", "Runite Ore", "💙", 3, 6, 60),
                DropTableItem("item_abyssal_whip", "Kraken Tentacle", "🐙", 1, 1, 15)
            )
        ),
        SlayerMonster(
            id = "dark_beast",
            name = "Dark Beast",
            reqSlayerLevel = 90,
            maxHp = 720,
            xpReward = 3400L,
            iconSymbol = "🐗",
            description = "Horned beast from Mourner Tunnels. Drops Dark Bow!",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 2, 5, 100),
                DropTableItem("item_dark_bow", "Dark Bow", "🏹", 1, 1, 15),
                DropTableItem("item_runite_ore", "Runite Ore", "💙", 3, 6, 60)
            )
        ),
        SlayerMonster(
            id = "smoke_devil",
            name = "Smoke Devil",
            reqSlayerLevel = 93,
            maxHp = 850,
            xpReward = 4200L,
            iconSymbol = "💨",
            description = "Fiery vaporous demon. Drops Occult Necklace!",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 3, 6, 100),
                DropTableItem("item_amulet_of_fury", "Occult Necklace", "📿", 1, 1, 15),
                DropTableItem("item_rune_bar", "Rune Bar", "💙", 3, 6, 70)
            )
        ),
        SlayerMonster(
            id = "alchemical_hydra",
            name = "Alchemical Hydra",
            reqSlayerLevel = 95,
            maxHp = 1100,
            xpReward = 5200L,
            iconSymbol = "🐲",
            description = "Multi-headed ancient dragon boss in Karuulm Dungeon!",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 5, 10, 100),
                DropTableItem("item_dragon_hunter_lance", "Dragon Hunter Lance", "🗡️", 1, 1, 12),
                DropTableItem("item_slayer_helmet", "Monster Hunter Helmet", "💀", 1, 1, 20),
                DropTableItem("item_manta_ray", "Cooked Manta Ray", "🪸", 5, 10, 100),
                DropTableItem("item_rune_blood", "Blood Runes", "🩸", 30, 90, 90),
                DropTableItem("item_rune_wrath", "Wrath Runes", "⚡", 20, 50, 75)
            )
        ),
        SlayerMonster(
            id = "cerberus",
            name = "Cerberus",
            reqSlayerLevel = 99,
            maxHp = 1500,
            xpReward = 7500L,
            iconSymbol = "🐕",
            description = "Three-headed hellhound guarding the River Styx! Drops Primordial Crystals!",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 8, 15, 100),
                DropTableItem("item_dragon_boots", "Primordial Crystal", "💎", 1, 1, 15),
                DropTableItem("item_abyssal_whip", "Infernal Axe", "🪓", 1, 1, 15)
            )
        )
    )
}

object HunterData {
    val CREATURES = listOf(
        HunterCreature(
            id = "crimson_swift",
            name = "Crimson Swift",
            reqHunterLevel = 1,
            trapType = "Bird Snare 🪤",
            requiredTrapItemId = "item_bird_snare",
            requiredTrapName = "Bird Snare",
            xpReward = 34L,
            iconSymbol = "🐦",
            description = "Red feathered swift found on Feldip Hills beaches.",
            drops = listOf(
                DropTableItem("item_feather", "Feather", "🪶", 10, 25, 100),
                DropTableItem("item_potato_seed", "Potato Seed", "🌱", 1, 3, 60),
                DropTableItem("item_eye_of_newt", "Eye of Newt", "👁️", 1, 3, 75),
                DropTableItem("item_raw_bird_meat", "Raw Bird Meat", "🥩", 1, 2, 80)
            )
        ),
        HunterCreature(
            id = "polar_kebbit",
            name = "Polar Kebbit",
            reqHunterLevel = 3,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 42L,
            iconSymbol = "🐻‍❄️",
            description = "White furred kebbit living in the Rellekka snow fields.",
            drops = listOf(
                DropTableItem("item_polar_kebbit_fur", "Polar Kebbit Fur", "🧊", 1, 2, 100),
                DropTableItem("item_onion_seed", "Onion Seed", "🌱", 1, 3, 60),
                DropTableItem("item_unicorn_horn", "Unicorn Horn Dust", "🦄", 1, 2, 75),
                DropTableItem("item_raw_bird_meat", "Raw Meat", "🥩", 1, 2, 70)
            )
        ),
        HunterCreature(
            id = "golden_warbler",
            name = "Golden Warbler",
            reqHunterLevel = 5,
            trapType = "Bird Snare 🪤",
            requiredTrapItemId = "item_bird_snare",
            requiredTrapName = "Bird Snare",
            xpReward = 48L,
            iconSymbol = "🐤",
            description = "Bright yellow songbird native to Uzer desert oases.",
            drops = listOf(
                DropTableItem("item_feather", "Golden Feathers", "🪶", 12, 30, 100),
                DropTableItem("item_meadow_mint_seed", "Meadow Mint Seed", "🌱", 1, 2, 60),
                DropTableItem("item_limpwurt_root", "Limpwurt Root", "🪵", 1, 3, 70)
            )
        ),
        HunterCreature(
            id = "feldip_weasel",
            name = "Feldip Weasel",
            reqHunterLevel = 7,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 56L,
            iconSymbol = "🦡",
            description = "Fast woodland rodent tracked through burrows.",
            drops = listOf(
                DropTableItem("item_feldip_fur", "Weasel Fur", "🪵", 1, 2, 100),
                DropTableItem("item_cabbage_seed", "Cabbage Seed", "🥬", 1, 2, 60),
                DropTableItem("item_white_berries", "White Berries", "🍒", 1, 3, 70)
            )
        ),
        HunterCreature(
            id = "copper_longtail",
            name = "Copper Longtail",
            reqHunterLevel = 9,
            trapType = "Bird Snare 🪤",
            requiredTrapItemId = "item_bird_snare",
            requiredTrapName = "Bird Snare",
            xpReward = 64L,
            iconSymbol = "🪶",
            description = "Copper-colored long-tailed bird in Piscatoris.",
            drops = listOf(
                DropTableItem("item_feather", "Copper Feathers", "🪶", 15, 32, 100),
                DropTableItem("item_tomato_seed", "Tomato Seed", "🍅", 1, 2, 60),
                DropTableItem("item_snape_grass", "Snape Grass", "🌾", 1, 3, 75)
            )
        ),
        HunterCreature(
            id = "desert_devil",
            name = "Desert Devil",
            reqHunterLevel = 11,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 72L,
            iconSymbol = "🌵",
            description = "Fiesty desert creature hiding beneath arid sand dunes.",
            drops = listOf(
                DropTableItem("item_desert_fur", "Desert Fur", "🏜️", 1, 2, 100),
                DropTableItem("item_wild_thyme_seed", "Wild Thyme Seed", "🌱", 1, 2, 60),
                DropTableItem("item_wine_of_zamorak", "Wine of Zamorak", "🍷", 1, 2, 65)
            )
        ),
        HunterCreature(
            id = "cerulean_twitch",
            name = "Cerulean Twitch",
            reqHunterLevel = 14,
            trapType = "Bird Snare 🪤",
            requiredTrapItemId = "item_bird_snare",
            requiredTrapName = "Bird Snare",
            xpReward = 84L,
            iconSymbol = "🔵",
            description = "Azure blue bird nesting in Rellekka mountains.",
            drops = listOf(
                DropTableItem("item_feather", "Cerulean Feathers", "🪶", 20, 40, 100),
                DropTableItem("item_lavender_seed", "Lavender Seed", "🌱", 1, 2, 60),
                DropTableItem("item_crushed_nest", "Crushed Bird's Nest", "🪹", 1, 2, 70)
            )
        ),
        HunterCreature(
            id = "ruby_harvest_impling",
            name = "Ruby Harvest Impling",
            reqHunterLevel = 15,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 90L,
            iconSymbol = "🦋",
            description = "Agile ruby impling fluttering near crop fields.",
            drops = listOf(
                DropTableItem("item_strawberry_seed", "Strawberry Seed", "🍓", 1, 3, 80),
                DropTableItem("item_clean_greenleaf", "Clean Greenleaf", "🌿", 1, 3, 70)
            )
        ),
        HunterCreature(
            id = "white_warbler",
            name = "White Warbler",
            reqHunterLevel = 17,
            trapType = "Bird Snare 🪤",
            requiredTrapItemId = "item_bird_snare",
            requiredTrapName = "Bird Snare",
            xpReward = 92L,
            iconSymbol = "🕊️",
            description = "Pure white feathered songbird near snow peaks.",
            drops = listOf(
                DropTableItem("item_feather", "White Feathers", "🪶", 25, 45, 100),
                DropTableItem("item_meadow_mint_seed", "Meadow Mint Seed", "🌱", 1, 2, 50)
            )
        ),
        HunterCreature(
            id = "tropical_wagtail",
            name = "Tropical Wagtail",
            reqHunterLevel = 19,
            trapType = "Bird Snare 🪤",
            requiredTrapItemId = "item_bird_snare",
            requiredTrapName = "Bird Snare",
            xpReward = 95L,
            iconSymbol = "🦜",
            description = "Brightly colored jungle bird with vibrant feathers.",
            drops = listOf(
                DropTableItem("item_feather", "Tropical Feathers", "🪶", 15, 35, 100),
                DropTableItem("item_cabbage_seed", "Cabbage Seed", "🥬", 1, 2, 50)
            )
        ),
        HunterCreature(
            id = "wild_kebbit",
            name = "Wild Kebbit",
            reqHunterLevel = 21,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 110L,
            iconSymbol = "🦝",
            description = "Cunning woodland kebbit hiding in hollow logs.",
            drops = listOf(
                DropTableItem("item_wild_fur", "Wild Fur", "🌲", 1, 2, 100),
                DropTableItem("item_wild_thyme_seed", "Wild Thyme Seed", "🌱", 1, 2, 50)
            )
        ),
        HunterCreature(
            id = "barb_tailed_kebbit",
            name = "Barb-tailed Kebbit",
            reqHunterLevel = 23,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 125L,
            iconSymbol = "🦔",
            description = "Spiky kebbit whose barbed tail makes fine harpoons.",
            drops = listOf(
                DropTableItem("item_barb_tail", "Barb Tail", "🪡", 1, 1, 100),
                DropTableItem("item_lavender_seed", "Lavender Seed", "🌱", 1, 2, 50)
            )
        ),
        HunterCreature(
            id = "baby_impling",
            name = "Baby Impling",
            reqHunterLevel = 25,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 135L,
            iconSymbol = "🧚",
            description = "Tiny cheeky impling carrying whimsical toys & seeds.",
            drops = listOf(
                DropTableItem("item_toy_mouse", "Toy Mouse", "🐭", 1, 1, 80),
                DropTableItem("item_sunleaf_seed", "Sunleaf Seed", "🌱", 1, 2, 40)
            )
        ),
        HunterCreature(
            id = "black_warlock_butterfly",
            name = "Black Warlock",
            reqHunterLevel = 27,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 145L,
            iconSymbol = "🦋",
            description = "Mystic dark butterfly radiating magical energy.",
            drops = listOf(
                DropTableItem("item_magic_essence", "Magic Essence", "✨", 2, 5, 100),
                DropTableItem("item_sunleaf_seed", "Sunleaf Seed", "🌱", 1, 2, 45)
            )
        ),
        HunterCreature(
            id = "swamp_lizard",
            name = "Swamp Lizard",
            reqHunterLevel = 29,
            trapType = "Net Trap 🕸️",
            requiredTrapItemId = "item_net_trap",
            requiredTrapName = "Net Trap Gear",
            xpReward = 152L,
            iconSymbol = "🦎",
            description = "Amphibious reptile trapped in the Canifis Swamps.",
            drops = listOf(
                DropTableItem("item_swamp_lizard_item", "Swamp Lizard", "🦎", 1, 1, 100),
                DropTableItem("item_tomato_seed", "Tomato Seed", "🍅", 1, 2, 45)
            )
        ),
        HunterCreature(
            id = "prickly_kebbit",
            name = "Prickly Kebbit",
            reqHunterLevel = 31,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 165L,
            iconSymbol = "🦔",
            description = "Covered in sharp spines used to craft prickly bolts.",
            drops = listOf(
                DropTableItem("item_prickly_fur", "Prickly Kebbit Fur", "🪵", 1, 2, 100),
                DropTableItem("item_ironleaf_seed", "Ironleaf Seed", "🌱", 1, 2, 40)
            )
        ),
        HunterCreature(
            id = "razor_backed_kebbit",
            name = "Razor-backed Kebbit",
            reqHunterLevel = 33,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 178L,
            iconSymbol = "🐗",
            description = "Aggressive boar-like rodent with razor ridges.",
            drops = listOf(
                DropTableItem("item_razor_fur", "Razor Fur", "🐗", 1, 2, 100),
                DropTableItem("item_wintergreen_seed", "Wintergreen Seed", "🌱", 1, 2, 40)
            )
        ),
        HunterCreature(
            id = "young_impling",
            name = "Young Impling",
            reqHunterLevel = 35,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 188L,
            iconSymbol = "🧚",
            description = "Playful impling holding herbs & crafting supplies.",
            drops = listOf(
                DropTableItem("item_sunleaf_seed", "Sunleaf Seed", "🌱", 1, 2, 50),
                DropTableItem("item_oak_plank", "Oak Plank", "🪵", 2, 5, 80)
            )
        ),
        HunterCreature(
            id = "snowy_knight_butterfly",
            name = "Snowy Knight",
            reqHunterLevel = 37,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 196L,
            iconSymbol = "🦋",
            description = "Frosty white butterfly Fluttering near icy slopes.",
            drops = listOf(
                DropTableItem("item_silverleaf_seed", "Silverleaf Seed", "🌱", 1, 2, 50),
                DropTableItem("item_pure_essence", "Pure Essence", "🔮", 10, 25, 90)
            )
        ),
        HunterCreature(
            id = "orange_salamander",
            name = "Orange Salamander",
            reqHunterLevel = 39,
            trapType = "Net Trap 🕸️",
            requiredTrapItemId = "item_net_trap",
            requiredTrapName = "Net Trap Gear",
            xpReward = 205L,
            iconSymbol = "🦎",
            description = "Heat-loving salamander trapped in Uzer Desert.",
            drops = listOf(
                DropTableItem("item_salamander_orange", "Orange Salamander", "🦎", 1, 1, 100),
                DropTableItem("item_silverleaf_seed", "Silverleaf Seed", "🌱", 1, 2, 40)
            )
        ),
        HunterCreature(
            id = "gourmet_impling",
            name = "Gourmet Impling",
            reqHunterLevel = 41,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 215L,
            iconSymbol = "🥧",
            description = "Foodie impling carrying delicious pies & cooked fish.",
            drops = listOf(
                DropTableItem("item_shark", "Cooked Shark", "🦈", 1, 3, 70),
                DropTableItem("item_lobster", "Cooked Lobster", "🦞", 2, 5, 100)
            )
        ),
        HunterCreature(
            id = "sabre_toothed_kebbit",
            name = "Sabre-toothed Kebbit",
            reqHunterLevel = 43,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 225L,
            iconSymbol = "🐅",
            description = "Dangerous predator with long curved fangs.",
            drops = listOf(
                DropTableItem("item_sabre_fur", "Sabre-tooth Fur", "🐅", 1, 2, 100),
                DropTableItem("item_mystic_sage_seed", "Mystic Sage Seed", "🌱", 1, 2, 40)
            )
        ),
        HunterCreature(
            id = "earth_impling",
            name = "Earth Impling",
            reqHunterLevel = 45,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 235L,
            iconSymbol = "🌍",
            description = "Impling carrying gems, ores & mining treasures.",
            drops = listOf(
                DropTableItem("item_uncut_ruby", "Uncut Ruby", "💎", 1, 2, 60),
                DropTableItem("item_coal_ore", "Coal Ore", "🪨", 5, 15, 90)
            )
        ),
        HunterCreature(
            id = "red_salamander",
            name = "Red Salamander",
            reqHunterLevel = 47,
            trapType = "Net Trap 🕸️",
            requiredTrapItemId = "item_net_trap",
            requiredTrapName = "Net Trap Gear",
            xpReward = 248L,
            iconSymbol = "🦎",
            description = "Volcanic red salamander trapped near Ourania altar.",
            drops = listOf(
                DropTableItem("item_salamander_red", "Red Salamander", "🦎", 1, 1, 100),
                DropTableItem("item_vervain_seed", "Vervain Seed", "🌱", 1, 2, 40)
            )
        ),
        HunterCreature(
            id = "essence_impling",
            name = "Essence Impling",
            reqHunterLevel = 50,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 260L,
            iconSymbol = "🔮",
            description = "Impling imbued with Runemaking magic & essence.",
            drops = listOf(
                DropTableItem("item_pure_essence", "Pure Essence", "🔮", 25, 50, 100),
                DropTableItem("item_mystic_sage_seed", "Mystic Sage Seed", "🌱", 1, 2, 35)
            )
        ),
        HunterCreature(
            id = "grey_chinchompa",
            name = "Grey Chinchompa",
            reqHunterLevel = 53,
            trapType = "Box Trap 📦",
            requiredTrapItemId = "item_box_trap",
            requiredTrapName = "Box Trap",
            xpReward = 198L,
            iconSymbol = "🐭",
            description = "Explosive furry rodent found in the Woodlands.",
            drops = listOf(
                DropTableItem("item_chinchompas", "Grey Chinchompa", "💣", 1, 3, 100),
                DropTableItem("item_sweetcorn_seed", "Sweetcorn Seed", "🌽", 1, 2, 50)
            )
        ),
        HunterCreature(
            id = "spotted_kebbit",
            name = "Spotted Kebbit",
            reqHunterLevel = 55,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 280L,
            iconSymbol = "🐆",
            description = "Swift spotted kebbit whose fur crafts Spotted Capes.",
            drops = listOf(
                DropTableItem("item_spotted_fur", "Spotted Kebbit Fur", "🐆", 1, 2, 100),
                DropTableItem("item_mystic_sage_seed", "Mystic Sage Seed", "🌱", 1, 2, 40)
            )
        ),
        HunterCreature(
            id = "eclectic_impling",
            name = "Eclectic Impling",
            reqHunterLevel = 58,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 300L,
            iconSymbol = "⚡",
            description = "Energetic impling containing Medium Clue scrolls & loot.",
            drops = listOf(
                DropTableItem("item_adamant_bar", "Adamant Bar", "🧱", 1, 3, 70),
                DropTableItem("item_sunleaf_seed", "Sunleaf Seed", "🌱", 1, 3, 50)
            )
        ),
        HunterCreature(
            id = "black_salamander",
            name = "Black Salamander",
            reqHunterLevel = 60,
            trapType = "Net Trap 🕸️",
            requiredTrapItemId = "item_net_trap",
            requiredTrapName = "Net Trap Gear",
            xpReward = 319L,
            iconSymbol = "🐊",
            description = "Wilderness black salamander capable of firing flame.",
            drops = listOf(
                DropTableItem("item_salamander_black", "Black Salamander", "🐊", 1, 1, 100),
                DropTableItem("item_mystic_sage_seed", "Mystic Sage Seed", "🌱", 1, 2, 45)
            )
        ),
        HunterCreature(
            id = "red_chinchompa",
            name = "Red Chinchompa",
            reqHunterLevel = 63,
            trapType = "Box Trap 📦",
            requiredTrapItemId = "item_box_trap",
            requiredTrapName = "Box Trap",
            xpReward = 265L,
            iconSymbol = "🐿️",
            description = "Fiery red explosive chinchompa trapped in Feldip Hills.",
            drops = listOf(
                DropTableItem("item_chinchompas", "Red Chinchompa", "💣", 2, 5, 100),
                DropTableItem("item_strawberry_seed", "Strawberry Seed", "🍓", 1, 2, 45)
            )
        ),
        HunterCreature(
            id = "nature_impling",
            name = "Nature Impling",
            reqHunterLevel = 65,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 340L,
            iconSymbol = "🌿",
            description = "Agriculture-obsessed impling bearing rare tree seeds.",
            drops = listOf(
                DropTableItem("item_yew_seed", "Yew Seed", "🌱", 1, 2, 50),
                DropTableItem("item_vervain_seed", "Vervain Seed", "🌱", 1, 1, 30)
            )
        ),
        HunterCreature(
            id = "dark_kebbit",
            name = "Dark Kebbit",
            reqHunterLevel = 67,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 360L,
            iconSymbol = "🦇",
            description = "Nocturnal kebbit roaming dark forests.",
            drops = listOf(
                DropTableItem("item_dark_fur", "Dark Kebbit Fur", "🦇", 1, 2, 100),
                DropTableItem("item_mystic_sage_seed", "Mystic Sage Seed", "🌱", 1, 2, 45)
            )
        ),
        HunterCreature(
            id = "dashed_kebbit",
            name = "Dashed Kebbit",
            reqHunterLevel = 70,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 380L,
            iconSymbol = "🐇",
            description = "Lightning-fast kebbit requiring expert tracking.",
            drops = listOf(
                DropTableItem("item_dashed_fur", "Dashed Fur", "🐇", 1, 2, 100),
                DropTableItem("item_vervain_seed", "Vervain Seed", "🌱", 1, 2, 40)
            )
        ),
        HunterCreature(
            id = "black_chinchompa",
            name = "Black Chinchompa",
            reqHunterLevel = 73,
            trapType = "Box Trap 📦",
            requiredTrapItemId = "item_box_trap",
            requiredTrapName = "Box Trap",
            xpReward = 315L,
            iconSymbol = "🦝",
            description = "High-potency black chinchompa trapped in the dangerous Wilderness!",
            drops = listOf(
                DropTableItem("item_chinchompas", "Black Chinchompa", "💣", 3, 6, 100),
                DropTableItem("item_watermelon_seed", "Watermelon Seed", "🍉", 1, 2, 40)
            )
        ),
        HunterCreature(
            id = "magpie_impling",
            name = "Magpie Impling",
            reqHunterLevel = 76,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 450L,
            iconSymbol = "💎",
            description = "Shiny impling obsessed with jewelry & dragon equipment.",
            drops = listOf(
                DropTableItem("item_amulet_of_fury", "Amulet of Glory", "🔮", 1, 1, 40),
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 2, 5, 80)
            )
        ),
        HunterCreature(
            id = "moonlight_antelope",
            name = "Moonlight Antelope",
            reqHunterLevel = 78,
            trapType = "Box Trap 📦",
            requiredTrapItemId = "item_box_trap",
            requiredTrapName = "Box Trap",
            xpReward = 520L,
            iconSymbol = "🦌",
            description = "Majestic antelope grazing in Varlamore moonlight.",
            drops = listOf(
                DropTableItem("item_antelope_fur", "Moonlight Antelope Fur", "🦌", 1, 2, 100),
                DropTableItem("item_dragon_bones", "Superior Dragon Bones", "🦴", 1, 3, 80)
            )
        ),
        HunterCreature(
            id = "herbiboar",
            name = "Herbiboar",
            reqHunterLevel = 80,
            trapType = "Tracking 🐾",
            requiredTrapItemId = "item_noose_wand",
            requiredTrapName = "Noose Wand",
            xpReward = 650L,
            iconSymbol = "🐗",
            description = "Ancient creature living on Fossil Island that grows rare herbs on its back!",
            drops = listOf(
                DropTableItem("item_dragon_bones", "Herbiboar Herbs", "🌿", 2, 5, 100),
                DropTableItem("item_sunleaf_seed", "Sunleaf Seed", "🌿", 1, 3, 60),
                DropTableItem("item_mystic_sage_seed", "Mystic Sage Seed", "🌱", 1, 2, 40)
            ),
            reqQuestId = "quest_bone_voyage",
            reqQuestName = "Bone Voyage"
        ),
        HunterCreature(
            id = "dragon_impling",
            name = "Dragon Impling",
            reqHunterLevel = 83,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 1200L,
            iconSymbol = "🐲",
            description = "Extremely rare impling flying across Gielinor with dragon treasures!",
            drops = listOf(
                DropTableItem("item_dragon_darts", "Dragon Darts", "🎯", 10, 30, 100),
                DropTableItem("item_mystic_sage_seed", "Mystic Sage Seed", "🌱", 2, 5, 50),
                DropTableItem("item_amulet_of_fury", "Amulet of Fury", "🔮", 1, 1, 20),
                DropTableItem("item_barrows_gloves", "Crypt Champion Gloves", "🧤", 1, 1, 15)
            )
        ),
        HunterCreature(
            id = "lucky_impling",
            name = "Lucky Impling",
            reqHunterLevel = 87,
            trapType = "Impling Net 🕸️",
            requiredTrapItemId = "item_impling_net",
            requiredTrapName = "Impling Net & Jar",
            xpReward = 1600L,
            iconSymbol = "🍀",
            description = "Legendary four-leaf clover impling granting Master Clue rewards!",
            drops = listOf(
                DropTableItem("item_3rd_age_platebody", "3rd Age Armour", "🛡️", 1, 1, 10),
                DropTableItem("item_dragon_scimitar", "Dragon Scimitar", "🗡️", 1, 1, 50),
                DropTableItem("item_vervain_seed", "Vervain Seed", "🌱", 2, 5, 80)
            )
        ),
        HunterCreature(
            id = "sunfire_fanatic_antelope",
            name = "Sunfire Antelope",
            reqHunterLevel = 91,
            trapType = "Box Trap 📦",
            requiredTrapItemId = "item_box_trap",
            requiredTrapName = "Box Trap",
            xpReward = 2100L,
            iconSymbol = "🦒",
            description = "Golden radiant beast of Varlamore sacred plains.",
            drops = listOf(
                DropTableItem("item_sunfire_splinter", "Sunfire Splinters", "☀️", 50, 150, 100),
                DropTableItem("item_mystic_sage_seed", "Mystic Sage Seed", "🌱", 3, 8, 70)
            )
        )
    )
}

data class BossMonster(
    val id: String,
    val name: String,
    val iconSymbol: String,
    val description: String,
    val reqCombatLevel: Int = 1,
    val reqSlayerLevel: Int = 0,
    val reqQuestId: String? = null,
    val reqQuestName: String? = null,
    val reqSkill: OsrsSkill? = null,
    val reqSkillLevel: Int = 0,
    val baseKillTimeSeconds: Int,
    val maxHp: Int,
    val combatXpReward: Long,
    val drops: List<DropTableItem>
)

object BossData {
    val BOSSES = listOf(
        BossMonster(
            id = "giant_mole",
            name = "Burrowing Void Titan",
            iconSymbol = "🗿",
            description = "Colossal earth-dwelling behemoth that tunnels beneath the valley.",
            reqCombatLevel = 50,
            baseKillTimeSeconds = 45,
            maxHp = 200,
            combatXpReward = 300L,
            drops = listOf(
                DropTableItem("item_mole_skin", "Titan Skin", "📜", 1, 3, 100),
                DropTableItem("item_mole_claw", "Titan Claw", "🐾", 1, 2, 100),
                DropTableItem("item_yew_logs", "Birch Timber", "🪵", 10, 25, 70),
                DropTableItem("item_adamant_bar", "Mythic Ore Bar", "🧱", 2, 5, 50),
                DropTableItem("item_baby_mole_pet", "Baby Titan Companion", "🗿", 1, 1, 5)
            )
        ),
        BossMonster(
            id = "king_black_dragon",
            name = "Astral Flame Drake",
            iconSymbol = "🐉",
            description = "Ancient three-headed cosmic wyrm dwelling in the Shattered Peaks.",
            reqCombatLevel = 75,
            baseKillTimeSeconds = 65,
            maxHp = 240,
            combatXpReward = 450L,
            drops = listOf(
                DropTableItem("item_dragon_bones", "Astral Dragon Scale", "🦴", 2, 2, 100),
                DropTableItem("item_dragon_pickaxe", "Mythic Pickaxe", "⛏️", 1, 1, 12),
                DropTableItem("item_draconic_visage", "Astral Core Visage", "🛡️", 1, 1, 5),
                DropTableItem("item_rune_scimitar", "Spirit Scimitar", "🗡️", 1, 1, 40),
                DropTableItem("item_rune_fire", "Fire Runes", "🔥", 100, 300, 90),
                DropTableItem("item_rune_air", "Air Runes", "💨", 100, 300, 90),
                DropTableItem("item_kbd_pet", "Astral Drake Companion", "🐉", 1, 1, 4)
            )
        ),
        BossMonster(
            id = "barrows_brothers",
            name = "Crypt Sentinel Warlord",
            iconSymbol = "🪦",
            description = "Undead warlord guarding ancient ancestral crypts.",
            reqQuestId = "quest_ancient_tomb",
            reqQuestName = "Ancient Crypts",
            reqCombatLevel = 60,
            baseKillTimeSeconds = 90,
            maxHp = 600,
            combatXpReward = 500L,
            drops = listOf(
                DropTableItem("item_dharoks_greataxe", "Nether Greataxe", "🪓", 1, 1, 15),
                DropTableItem("item_ahrims_robetop", "Archmage Robetop", "🧙‍♂️", 1, 1, 15),
                DropTableItem("item_karils_leathertop", "Shadow Ranger Leathertop", "🏹", 1, 1, 15),
                DropTableItem("item_guthans_warspear", "Crypt Sentinel Spear", "🗡️", 1, 1, 15),
                DropTableItem("item_death_rune", "Death Runes", "💀", 50, 150, 80),
                DropTableItem("item_coins_100", "Coins", "🪙", 10000, 30000, 100)
            )
        ),
        BossMonster(
            id = "dagannoth_kings",
            name = "Abyssal Leviathan Kings",
            iconSymbol = "👑",
            description = "Trio of ancient deep-sea overlords ruling the ocean trenches.",
            reqQuestId = "quest_ocean_abyss",
            reqQuestName = "Ocean Abyss",
            reqCombatLevel = 80,
            baseKillTimeSeconds = 75,
            maxHp = 300,
            combatXpReward = 550L,
            drops = listOf(
                DropTableItem("item_berserker_ring", "Berserker Ring", "💍", 1, 1, 18),
                DropTableItem("item_dragon_axe", "Mythic Woodcutter Axe", "🪓", 1, 1, 15),
                DropTableItem("item_archer_ring", "Archers Ring", "💍", 1, 1, 18),
                DropTableItem("item_seers_ring", "Seers Ring", "💍", 1, 1, 18),
                DropTableItem("item_dagannoth_pet", "Leviathan Companion", "👑", 1, 1, 4)
            )
        ),
        BossMonster(
            id = "zulrah",
            name = "Emerald Viper Monarch",
            iconSymbol = "🐍",
            description = "Venomous serpent guardian of the Spirit Mire.",
            reqQuestId = "quest_spirit_realm",
            reqQuestName = "Spirit Realm",
            reqCombatLevel = 85,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 75,
            baseKillTimeSeconds = 110,
            maxHp = 500,
            combatXpReward = 750L,
            drops = listOf(
                DropTableItem("item_zulrah_scales", "Emerald Venom Scales", "🧪", 100, 300, 100),
                DropTableItem("item_tanzanite_fang", "Venom Fang", "🐍", 1, 1, 12),
                DropTableItem("item_magic_fang", "Spirit Staff Tip", "🪄", 1, 1, 12),
                DropTableItem("item_serpentine_visage", "Serpentine Visage", "🛡️", 1, 1, 10),
                DropTableItem("item_dragon_bones", "Dragon Bones", "🦴", 5, 10, 100),
                DropTableItem("item_rune_nature", "Nature Runes", "🌿", 50, 150, 90),
                DropTableItem("item_rune_death", "Death Runes", "💀", 50, 150, 90),
                DropTableItem("item_snakeling_pet", "Viper Companion", "🐍", 1, 1, 3)
            )
        ),
        BossMonster(
            id = "vorkath",
            name = "Frostbite Revenant Wyrm",
            iconSymbol = "🐲",
            description = "Undead ice dragon lord guarding Glacial Ridge.",
            reqQuestId = "quest_dragon_slayer",
            reqQuestName = "Wyrm Slayer",
            reqCombatLevel = 90,
            reqSlayerLevel = 68,
            baseKillTimeSeconds = 120,
            maxHp = 750,
            combatXpReward = 900L,
            drops = listOf(
                DropTableItem("item_dragon_bones", "Glacial Dragon Bones", "🦴", 2, 2, 100),
                DropTableItem("item_vorkaths_head", "Frostbite Wyrm Horn", "🐉", 1, 1, 25),
                DropTableItem("item_draconic_visage", "Skeletal Visage", "🛡️", 1, 1, 8),
                DropTableItem("item_dragon_scimitar", "Mythic Scimitar", "🗡️", 1, 1, 30),
                DropTableItem("item_rune_soul", "Soul Runes", "👻", 40, 120, 85),
                DropTableItem("item_rune_blood", "Blood Runes", "🩸", 50, 150, 90),
                DropTableItem("item_vorki_pet", "Wyrmling Companion", "🐉", 1, 1, 3)
            )
        ),
        BossMonster(
            id = "general_graardor",
            name = "Iron Titan Warlord",
            iconSymbol = "🛡️",
            description = "Towering armored monolith presiding over the Iron Citadel.",
            reqQuestId = "quest_mountain_stronghold",
            reqQuestName = "Titan Stronghold",
            reqCombatLevel = 90,
            reqSkill = OsrsSkill.ATTACK,
            reqSkillLevel = 70,
            baseKillTimeSeconds = 90,
            maxHp = 350,
            combatXpReward = 700L,
            drops = listOf(
                DropTableItem("item_bandos_chestplate", "Titan Warlord Chestplate", "🛡️", 1, 1, 12),
                DropTableItem("item_bandos_tassets", "Titan Warlord Greaves", "🦵", 1, 1, 12),
                DropTableItem("item_rune_scimitar", "Aether Scimitar", "🗡️", 1, 1, 40),
                DropTableItem("item_graardor_pet", "Titan Warlord Companion", "🛡️", 1, 1, 3)
            )
        ),
        BossMonster(
            id = "commander_zilyana",
            name = "Celestial Light Sentinel",
            iconSymbol = "⚔️",
            description = "Archangel of divine light wielding the Celestial Eagle Bow.",
            reqQuestId = "quest_mountain_stronghold",
            reqQuestName = "Titan Stronghold",
            reqCombatLevel = 95,
            reqSkill = OsrsSkill.AGILITY,
            reqSkillLevel = 70,
            baseKillTimeSeconds = 100,
            maxHp = 350,
            combatXpReward = 700L,
            drops = listOf(
                DropTableItem("item_armadyl_crossbow", "Celestial Eagle Bow", "🏹", 1, 1, 12),
                DropTableItem("item_saradomin_sword", "Holy Dawn Greatsword", "⚔️", 1, 1, 18),
                DropTableItem("item_zilyana_pet", "Light Sentinel Companion", "⚔️", 1, 1, 3)
            )
        ),
        BossMonster(
            id = "kraken",
            name = "Kraken of the Abyss",
            iconSymbol = "🐙",
            description = "Massive tentacled horror sleeping in the ocean floor.",
            reqSlayerLevel = 87,
            reqCombatLevel = 80,
            baseKillTimeSeconds = 50,
            maxHp = 255,
            combatXpReward = 450L,
            drops = listOf(
                DropTableItem("item_trident_of_seas", "Trident of the Abyss", "🪄", 1, 1, 18),
                DropTableItem("item_kraken_tentacle", "Kraken Tentacle", "🐙", 1, 1, 15),
                DropTableItem("item_kraken_pet", "Abyssal Kraken Companion", "🐙", 1, 1, 5)
            )
        ),
        BossMonster(
            id = "cerberus",
            name = "Inferno Dreadhound",
            iconSymbol = "🐕",
            description = "Three-headed hellhound guarding the River of Souls.",
            reqSlayerLevel = 91,
            reqCombatLevel = 90,
            baseKillTimeSeconds = 80,
            maxHp = 600,
            combatXpReward = 800L,
            drops = listOf(
                DropTableItem("item_primordial_crystal", "Primordial Crystal", "💎", 1, 1, 12),
                DropTableItem("item_pegasian_crystal", "Pegasian Crystal", "💎", 1, 1, 12),
                DropTableItem("item_eternal_crystal", "Eternal Crystal", "💎", 1, 1, 12),
                DropTableItem("item_hellpuppy_pet", "Dreadhound Companion", "🐕", 1, 1, 3)
            )
        ),
        BossMonster(
            id = "phantom_muspah",
            name = "Void Shift Revenant",
            iconSymbol = "👻",
            description = "Shifting shadow anomaly pulsing with cosmic energy.",
            reqQuestId = "quest_northern_secrets",
            reqQuestName = "Secrets of the North",
            reqCombatLevel = 90,
            baseKillTimeSeconds = 110,
            maxHp = 850,
            combatXpReward = 850L,
            drops = listOf(
                DropTableItem("item_ancient_icon", "Ancient Void Icon", "📜", 1, 1, 15),
                DropTableItem("item_dragon_bones", "Glacial Dragon Bones", "🦴", 5, 10, 100),
                DropTableItem("item_muphon_pet", "Voidling Companion", "👻", 1, 1, 3)
            )
        ),
        BossMonster(
            id = "vardorvis",
            name = "Bloodwood Executioner",
            iconSymbol = "🩸",
            description = "Fierce warrior wielding broadswords in the Stranglewood.",
            reqQuestId = "quest_desert_treasure",
            reqQuestName = "Ancient Artifacts",
            reqCombatLevel = 95,
            baseKillTimeSeconds = 105,
            maxHp = 700,
            combatXpReward = 900L,
            drops = listOf(
                DropTableItem("item_ultor_vestige", "Bloodwood Vestige", "💍", 1, 1, 10),
                DropTableItem("item_dragon_scimitar", "Mythic Scimitar", "🗡️", 1, 1, 30),
                DropTableItem("item_butch_pet", "Executioner Hound Companion", "🐶", 1, 1, 3)
            )
        ),
        BossMonster(
            id = "corporeal_beast",
            name = "Ethereal Calamity Behemoth",
            iconSymbol = "🦬",
            description = "Mythical spirit beast roaming the high mountain peaks.",
            reqCombatLevel = 100,
            baseKillTimeSeconds = 180,
            maxHp = 2000,
            combatXpReward = 1600L,
            drops = listOf(
                DropTableItem("item_elysian_spirit_shield", "Elysian Spirit Shield", "🛡️", 1, 1, 5),
                DropTableItem("item_holy_elixir", "Holy Elixir", "🧪", 1, 1, 15),
                DropTableItem("item_raw_shark", "Raw Shark", "🦈", 20, 50, 100),
                DropTableItem("item_corp_pet", "Spirit Behemoth Companion", "🦬", 1, 1, 2)
            )
        )
    )
}
