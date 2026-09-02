package com.example.data.models

data class PickpocketNpc(
    val id: String,
    val name: String,
    val levelReq: Int,
    val thievingXp: Float,
    val iconEmoji: String,
    val description: String,
    val lootSummary: String
)

val PICKPOCKET_NPCS = listOf(
    PickpocketNpc("man", "Man / Woman", 1, 8.0f, "🧔", "Innocent citizens walking around Lumbridge & Varrock.", "3-15 Coins, Fresh Bread & Air Runes"),
    PickpocketNpc("silk_merchant", "Silk Merchant", 5, 11.5f, "🧵", "Silk traders selling fine fabrics in Al Kharid bazaar.", "10-25 Coins, Al Kharid Silk & Mind Runes"),
    PickpocketNpc("farmer", "Farmer", 10, 14.5f, "🧑‍🌾", "Hardworking field farmers tending to crop patches.", "15-30 Coins, Potato/Cabbage Seeds & Oak Seeds"),
    PickpocketNpc("ham_member", "H.A.M. Cultist", 15, 18.5f, "🥷", "H.A.M. cultists lurking in Lumbridge dungeon.", "20-40 Coins, Earth Runes & Willow Seeds"),
    PickpocketNpc("zephyr_scout", "Zephyr Sky Scout", 18, 21.0f, "🪶", "Nimble sky scouts carrying wind talismans and feathers.", "25-55 Coins, Air Runes, Feathers & Fresh Bread"),
    PickpocketNpc("street_urchin", "Desert Urchin", 20, 22.0f, "👦", "Nimble urchins picking pockets in desert alleys.", "25-45 Coins, Water Runes & Uncut Sapphires"),
    PickpocketNpc("shadow_miner", "Shadow Shaft Miner", 20, 23.5f, "⛏️", "Miners pocketing rare gemstones before overseers notice.", "25-60 Coins, Coal Ore, Fire Runes & Uncut Sapphires"),
    PickpocketNpc("crag_smuggler", "Crag Ore Smuggler", 22, 25.0f, "⛏️", "Miners smuggling illicit gems and fossil ores.", "30-65 Coins, Iron Ore, Uncut Sapphires & Earth Runes"),
    PickpocketNpc("warrior", "Warrior Woman", 25, 26.0f, "👩‍⚔️", "Guards guarding Al Kharid palace gates.", "18-40 Coins, Iron Bars, Body Runes & Daggers"),
    PickpocketNpc("mist_smuggler", "Mist Fen Smuggler", 25, 28.0f, "🐺", "Shady smugglers navigating misty Cerulean bayous in rowboats.", "35-80 Coins, Nature Runes, Clean Sunleaf & Water Runes"),
    PickpocketNpc("hive_scavenger", "Hive Swarm Scavenger", 26, 29.5f, "🪲", "Hardy scavengers harvesting honeycombs and insect stingers.", "35-75 Coins, Earth Runes, Honeycomb & Coal"),
    PickpocketNpc("market_trader", "Market Trader", 28, 31.0f, "🏪", "Traders selling spices, garlic and market goods.", "25-50 Coins, Fire Runes & Willow Seeds"),
    PickpocketNpc("cliff_falconer", "Sky Ridge Falconer", 28, 33.0f, "🦅", "Falconers training predatory birds to snatch purses.", "45-90 Coins, Cosmic Runes, Oak Seeds & Uncut Sapphires"),
    PickpocketNpc("stone_bandit", "Granite Bandit", 32, 38.0f, "🪨", "Tough highwaymen taking shelter in granite caves.", "50-95 Coins, Coal, Steel Bars & Chaos Runes"),
    PickpocketNpc("rogue", "Wilderness Rogue", 32, 35.5f, "🥷", "Shadowy thieves operating in the Wilderness.", "35-60 Coins, Iron Bars, Coal Ore & Chaos Runes"),
    PickpocketNpc("ore_lockpicker", "Deep Vault Lockpicker", 34, 41.0f, "🗝️", "Specialists opening blast-door safe boxes in coal mines.", "55-105 Coins, Mithril Ore, Chaos Runes & Steel Bars"),
    PickpocketNpc("fen_bandit", "Fen Bandit Outlaw", 35, 42.0f, "🗡️", "Outlaws ambushing travelers across Cerulean wetlands.", "50-110 Coins, Iron Bars, Chaos Runes & Willow Seeds"),
    PickpocketNpc("goldenrod_gambler", "High-Stakes Gambler", 35, 45.0f, "🎲", "Reckless high-rollers with deep pockets and loose coin purses.", "100-250 Coins, Gold Ore, Cosmic Runes & Uncut Rubies"),
    PickpocketNpc("cave_goblin", "Cave Goblin", 36, 40.0f, "👺", "Dorgesh-Kaan underground miners.", "25-50 Coins, Coal/Iron Ores & Cosmic Runes"),
    PickpocketNpc("charcoal_smuggler", "Charcoal Smuggler", 36, 44.0f, "🪵", "Smugglers trading hardwood charcoal and bug nets.", "55-115 Coins, Willow Seeds, Nature Runes & Fire Runes"),
    PickpocketNpc("master_farmer", "Master Farmer", 38, 43.0f, "🧑‍🌾", "Draynor Village seed master with rare herbs and tree seeds.", "Sunleaf, Watermelon, Willow, Maple & Yew Seeds"),
    PickpocketNpc("guard", "Falador Guard", 40, 46.8f, "🛡️", "City guards patrolling Falador square.", "30-65 Coins, Iron/Steel Arrows & Law Runes"),
    PickpocketNpc("syndicate_bruiser", "Syndicate Enforcer", 42, 58.0f, "🥊", "Heavily armored bruisers carrying fight club purses.", "90-200 Coins, Steel Bars, Body Runes & Chaos Runes"),
    PickpocketNpc("bayou_witch", "Bayou Spirit Witch", 45, 68.0f, "🧙‍♀️", "Shadowy witches brewing potent concoctions in the mist.", "90-180 Coins, Astral Runes, Sunleaf Seeds & Uncut Sapphires"),
    PickpocketNpc("grove_poacher", "Grove Poacher", 45, 70.0f, "🏹", "Sneaky hunters poaching enchanted beasts in sacred canopy.", "70-150 Coins, Oak/Willow Seeds, Law Runes & Bowstrings"),
    PickpocketNpc("trader", "Fremennik Trader", 45, 65.0f, "⛵", "Rellekka seafaring merchant.", "45-90 Coins, Oak Planks, Salmon & Astral Runes"),
    PickpocketNpc("watchman", "Yanille Watchman", 48, 72.0f, "🔦", "Night watchmen guarding Yanille wizard walls.", "50-100 Coins, Steel Bars & Nature Runes"),
    PickpocketNpc("underground_fence", "Black Market Fence", 48, 76.0f, "💰", "Shrewd fences dealing in hot jewelry and smuggled goods.", "150-320 Coins, Gold Bars, Law Runes & Uncut Emeralds"),
    PickpocketNpc("wealthy_citizen", "Wealthy Citizen", 50, 75.0f, "🏛️", "Affluent citizens of Varlamore square.", "80-160 Coins, Gold Bars, Sapphires & Cosmic Runes"),
    PickpocketNpc("spectral_medium", "Spectral Medium", 52, 85.0f, "🔮", "Channellers conversing with spirits of the ancient towers.", "120-240 Coins, Death Runes, Astral Runes & Magic Potions"),
    PickpocketNpc("desert_bandit", "Desert Bandit", 53, 79.5f, "🏜️", "Outlaws hiding out in Pollnivneach.", "60-120 Coins, Gold Ore, Mithril Bars & Chaos Runes"),
    PickpocketNpc("meteor_smuggler", "Meteorite Smuggler", 54, 88.0f, "☄️", "Smugglers trading cosmic stones and rare meteor fragments.", "130-270 Coins, Cosmic Runes, Mithril Bars & Uncut Diamonds"),
    PickpocketNpc("ardougne_knight", "Ardougne Knight", 55, 84.3f, "⚔️", "Famous East Ardougne market knights.", "50-120 Coins, Steel Bars & Law Runes"),
    PickpocketNpc("sylvan_infiltrator", "Sylvan Shadow Rogue", 55, 92.0f, "🌿", "Stealthy elven rogues blending seamlessly into tree branches.", "110-220 Coins, Clean Vervain, Nature Runes & Teak Planks"),
    PickpocketNpc("marsh_poacher", "Great Marsh Poacher", 58, 98.0f, "🐊", "Poachers hunting exotic marsh reptiles and rare flora.", "140-280 Coins, Raw Lobster, Nature Runes & Watermelon Seeds"),
    PickpocketNpc("lumber_overseer", "Sawmill Overseer", 58, 91.0f, "🪵", "Foremen running the Varrock lumber mill.", "70-140 Coins, Teak Planks, Oak & Maple Seeds"),
    PickpocketNpc("menaphite_thug", "Menaphite Thug", 60, 96.0f, "🗡️", "Sullen thugs lurking in Pollnivneach alleys.", "80-150 Coins, Lockpicks, Death Runes & Gold Bars"),
    PickpocketNpc("tzhaar", "TzHaar-Hur Artisan", 63, 103.4f, "🌋", "Volcanic obsidian artisans of Mor Ul Rek.", "120-220 Coins, Gold Bars & Rubies"),
    PickpocketNpc("phantom_thief", "Phantom Master Thief", 64, 122.0f, "👻", "Ethereal thieves capable of walking through solid walls.", "220-450 Coins, Blood Runes, Uncut Diamonds & Ghostly Essences"),
    PickpocketNpc("elementalist", "Sorceress Garden Spirit", 65, 118.0f, "🌸", "Elemental spirits tending magical herb gardens.", "Elemental Runes, Sunleaf, Vervain & Yew Seeds"),
    PickpocketNpc("rum_smuggler", "Rum Smuggler", 68, 132.0f, "🏴‍☠️", "Pirates smuggling rum on Brimhaven docks.", "100-200 Coins, Mahogany Planks, Gems & Nature Runes"),
    PickpocketNpc("sacred_druid_renegade", "Renegade Arch-Druid", 68, 140.0f, "🧝‍♂️", "Outcast arch-druids guarding stolen celestial seeds.", "180-350 Coins, Spirit Weed, Yew Seeds, Law/Blood Runes"),
    PickpocketNpc("paladin", "Paladin", 70, 151.8f, "🏰", "High paladins of West Ardougne castle.", "80-200 Coins, Chaos, Death & Law Runes"),
    PickpocketNpc("bog_pirate", "Tidal Bog Pirate Captain", 70, 155.0f, "🏴‍☠️", "Ruthless captain hiding contraband in tidal marshes.", "250-500 Coins, Raw Swordfish, Death Runes, Gold Bars & Rubies"),
    PickpocketNpc("druid_elder", "Highland Druid Elder", 72, 165.0f, "🌿", "Ancient druids guarding sacred stone circles.", "Sunleaf, Mystic Sage, Yew & Ironwood Seeds, Nature Runes"),
    PickpocketNpc("gnome", "Gnome Citizen", 75, 198.5f, "🧝", "Gnome Stronghold tree inhabitants.", "300-500 Coins, Uncut Gems, Maple/Yew Seeds & Law Runes"),
    PickpocketNpc("dark_mage", "Wilderness Dark Mage", 78, 235.0f, "🔮", "Corrupted mages practicing dark arts at Mage Arena.", "Death, Blood & Soul Runes, Ironwood Seeds"),
    PickpocketNpc("hero", "Hero", 80, 273.3f, "👑", "Legendary hero roaming Heroes' Guild.", "500-800 Coins, Death/Blood/Soul Runes & Uncut Diamonds"),
    PickpocketNpc("vyrewatch", "Vyre Noble", 82, 306.5f, "🦇", "Aristocratic vampire nobles of Darkmeyer.", "400-700 Coins, Blood Runes, Adamant Bars & Rubies"),
    PickpocketNpc("crystal_artisan", "Crystal Artisan", 84, 330.0f, "💎", "Elven jewelers carving crystal harmonic resonance.", "Crystal Shards, Uncut Diamonds, Ironwood Seeds & Law Runes"),
    PickpocketNpc("elf", "Prifddinas Elf", 85, 353.0f, "🧝‍♀️", "Crystal elves of Prifddinas.", "280-600 Coins, Crystal Shards, Diamonds & Yew/Ironwood Seeds"),
    PickpocketNpc("tzhaar_ket", "TzHaar-Ket Guard", 88, 380.0f, "🛡️", "Elite obsidian volcanic warriors.", "600-1000 Coins, Rune Bars, Death/Wrath Runes & Diamonds"),
    PickpocketNpc("grand_alchemist", "Grand Master Alchemist", 90, 410.0f, "⚗️", "Master alchemists transmuting rare elements.", "Gold/Rune Bars, Wrath Runes, Ironwood & Redwood Seeds"),
    PickpocketNpc("shadow_warlord", "Shadow Warlord", 93, 450.0f, "👺", "Elite warlord of the Shadow Realm citadel.", "Rune Bars, Blood/Wrath Runes, Dragonfruit Seeds, Effigies (Lvl 70 Summoning)"),
    PickpocketNpc("shaman_ancestor", "High Shaman Ancestor", 96, 500.0f, "🧙‍♂️", "Ethereal spirit shaman residing in the Spirit Realm.", "Ironwood/Redwood/Spirit Seeds, Wrath Runes, Effigies (Lvl 70 Summoning)")
)

data class TrickeryDistrict(
    val id: String,
    val name: String,
    val emoji: String,
    val reqLevel: Int,
    val description: String,
    val posXRatio: Float,
    val posYRatio: Float,
    val npcs: List<PickpocketNpc>,
    val reqTotemId: String? = null,
    val reqTotemName: String? = null,
    val reqTotemEmoji: String? = null,
    val specialPerkDesc: String? = null
)

val TRICKERY_DISTRICTS = listOf(
    TrickeryDistrict(
        id = "dist_town",
        name = "Town Alleys & Bazaar",
        emoji = "🏙️",
        reqLevel = 1,
        description = "Bustling streets and market stalls in Lumbridge & Al Kharid.",
        posXRatio = 0.12f,
        posYRatio = 0.72f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("man", "silk_merchant", "farmer", "ham_member", "street_urchin", "warrior", "market_trader") }
    ),
    TrickeryDistrict(
        id = "dist_zephyr_spire",
        name = "Zephyr Spire & Roost",
        emoji = "🪶",
        reqLevel = 18,
        description = "Gusty cliffside rookery and wind-swept roost unlocked by the Zephyr Obelisk. Sky scouts and falconers store high-altitude aerial loot.",
        posXRatio = 0.20f,
        posYRatio = 0.35f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("zephyr_scout", "cliff_falconer") },
        reqTotemId = "item_badge_zephyr",
        reqTotemName = "Zephyr Obelisk",
        reqTotemEmoji = "🪶",
        specialPerkDesc = "Zephyr Tailwind: +15% rapid pickpocket speed & swift evasion of detection!"
    ),
    TrickeryDistrict(
        id = "dist_oreburgh_shadow",
        name = "Oreburgh Shadow Guild",
        emoji = "⛏️",
        reqLevel = 20,
        description = "Deep unmapped coal shafts and subterranean guild unlocked by the Coal Obelisk. Shadow miners and underground lockpickers.",
        posXRatio = 0.26f,
        posYRatio = 0.22f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("shadow_miner", "ore_lockpicker") },
        reqTotemId = "item_badge_coal",
        reqTotemName = "Coal Obelisk",
        reqTotemEmoji = "🪨",
        specialPerkDesc = "Carbon Vein: Rich Coal & Fire Rune bounty on pickpocket!"
    ),
    TrickeryDistrict(
        id = "dist_rustboro_crags",
        name = "Rustboro Outlaw Crags",
        emoji = "🪨",
        reqLevel = 22,
        description = "Rocky cliffside outcroppings and fossil tunnels unlocked by the Stone Obelisk. Rock quarry outlaws and mineral smugglers.",
        posXRatio = 0.24f,
        posYRatio = 0.54f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("crag_smuggler", "stone_bandit") },
        reqTotemId = "item_badge_stone",
        reqTotemName = "Stone Obelisk",
        reqTotemEmoji = "🪨",
        specialPerkDesc = "Stone Quarrying: Drops bonus Uncut Gems & Mining Ores on pickpocket!"
    ),
    TrickeryDistrict(
        id = "dist_mist_fen",
        name = "Mist Fen Bayou & Outpost",
        emoji = "🐺",
        reqLevel = 25,
        description = "Shrouded marsh hideout unlocked by the Mist Fen Obelisk from the Cerulean trial. Smugglers and water bandits trade stolen azure pearls and marsh herbs.",
        posXRatio = 0.32f,
        posYRatio = 0.82f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("mist_smuggler", "fen_bandit", "bayou_witch") },
        reqTotemId = "item_totem_mist_fen",
        reqTotemName = "Mist Fen Obelisk",
        reqTotemEmoji = "🐺",
        specialPerkDesc = "Mist Fen Resonance: +20% extra thieving loot & chance to pickpocket rare Water Runes and Clean Sunleaf!"
    ),
    TrickeryDistrict(
        id = "dist_azalea_hive",
        name = "Azalea Hive Thicket",
        emoji = "🪲",
        reqLevel = 26,
        description = "Buzzing underground hives and charcoal kilns unlocked by the Hive Obelisk. Bug catchers and forest scavengers trade enchanted honey and chitin.",
        posXRatio = 0.36f,
        posYRatio = 0.65f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("hive_scavenger", "charcoal_smuggler") },
        reqTotemId = "item_badge_hive",
        reqTotemName = "Hive Obelisk",
        reqTotemEmoji = "🪲",
        specialPerkDesc = "Hive Pheromones: +20% chance for bonus raw materials & honeycombs!"
    ),
    TrickeryDistrict(
        id = "dist_rogue",
        name = "Rogue Guild & Mines",
        emoji = "🥷",
        reqLevel = 30,
        description = "Shadowy rogue dens, underground mines, and city squares.",
        posXRatio = 0.42f,
        posYRatio = 0.48f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("rogue", "cave_goblin", "master_farmer", "guard", "trader", "watchman") }
    ),
    TrickeryDistrict(
        id = "dist_goldenrod_vaults",
        name = "Goldenrod Underground Vaults",
        emoji = "🐮",
        reqLevel = 35,
        description = "Sprawling subterranean casino corridors and black-market vaults unlocked by the Plain Totem. High-roller gamblers and fences.",
        posXRatio = 0.46f,
        posYRatio = 0.88f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("goldenrod_gambler", "underground_fence") },
        reqTotemId = "item_badge_plain",
        reqTotemName = "Plain Obelisk",
        reqTotemEmoji = "🐮",
        specialPerkDesc = "Jackpot Resonance: Doubles maximum coin loot payout on successful steals!"
    ),
    TrickeryDistrict(
        id = "dist_veilstone_syndicate",
        name = "Veilstone Syndicate Hideout",
        emoji = "🥊",
        reqLevel = 42,
        description = "Underground prize-fighting rings and meteor warehouse hideouts unlocked by the Cobble Obelisk. Syndicate bruisers and enforcers.",
        posXRatio = 0.54f,
        posYRatio = 0.52f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("syndicate_bruiser", "meteor_smuggler") },
        reqTotemId = "item_badge_cobble",
        reqTotemName = "Cobble Obelisk",
        reqTotemEmoji = "🥊",
        specialPerkDesc = "Syndicate Stash: Massive Coin payouts & bonus Blood Runes!"
    ),
    TrickeryDistrict(
        id = "dist_sacred_grove",
        name = "Sacred Grove Secret Canopy",
        emoji = "🌳",
        reqLevel = 45,
        description = "Secret treetop walkways and hidden fairy groves unlocked by the Sacred Grove Obelisk from Celadon Gym. Poachers and druidic shadow thieves hoard rare herbal cuttings.",
        posXRatio = 0.50f,
        posYRatio = 0.30f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("grove_poacher", "sylvan_infiltrator", "sacred_druid_renegade") },
        reqTotemId = "item_totem_sacred_grove",
        reqTotemName = "Sacred Grove Obelisk",
        reqTotemEmoji = "🌳",
        specialPerkDesc = "Sacred Sylvan Blessing: +25% Thieving XP & guaranteed Herb / Seed drops from grove pickpocketing!"
    ),
    TrickeryDistrict(
        id = "dist_varlamore",
        name = "Varlamore & Outlaw Dens",
        emoji = "🏛️",
        reqLevel = 50,
        description = "Affluent squares, desert outlaws, and sawmill foremen.",
        posXRatio = 0.60f,
        posYRatio = 0.72f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("wealthy_citizen", "desert_bandit", "ardougne_knight", "lumber_overseer", "menaphite_thug", "tzhaar") }
    ),
    TrickeryDistrict(
        id = "dist_ecruteak_crypts",
        name = "Ecruteak Phantom Crypts",
        emoji = "👻",
        reqLevel = 52,
        description = "Ancient misty crypts and burned tower ruins unlocked by the Fog Obelisk. Phantom thieves and spectral mediums hoard haunted relics.",
        posXRatio = 0.58f,
        posYRatio = 0.16f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("spectral_medium", "phantom_thief") },
        reqTotemId = "item_badge_fog",
        reqTotemName = "Fog Obelisk",
        reqTotemEmoji = "👻",
        specialPerkDesc = "Phantom Cloak: +30% Thieving XP & rare Death/Blood rune drops!"
    ),
    TrickeryDistrict(
        id = "dist_pastoria_marsh",
        name = "Pastoria Great Marsh Outlaws",
        emoji = "🌊",
        reqLevel = 58,
        description = "Quicksand islands and tropical marsh waterways unlocked by the Fen Obelisk. Marsh pirates and aquatic poachers.",
        posXRatio = 0.68f,
        posYRatio = 0.82f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("marsh_poacher", "bog_pirate") },
        reqTotemId = "item_badge_fen",
        reqTotemName = "Fen Obelisk",
        reqTotemEmoji = "🌊",
        specialPerkDesc = "Torrential Surge: Double loot drops & Water/Nature rune surge!"
    ),
    TrickeryDistrict(
        id = "dist_royal",
        name = "Royal Docks & Paladins",
        emoji = "🏰",
        reqLevel = 65,
        description = "Elemental spirits, pirate harbors, castle guards, and druid circles.",
        posXRatio = 0.76f,
        posYRatio = 0.38f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("elementalist", "rum_smuggler", "paladin", "druid_elder", "gnome", "dark_mage") }
    ),
    TrickeryDistrict(
        id = "dist_crystal",
        name = "Crystal Elven Citadel",
        emoji = "💎",
        reqLevel = 80,
        description = "Prifddinas crystal artisans, vampire nobles, and heroes' guild.",
        posXRatio = 0.86f,
        posYRatio = 0.62f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("hero", "vyrewatch", "crystal_artisan", "elf", "tzhaar_ket") }
    ),
    TrickeryDistrict(
        id = "dist_shadow",
        name = "Shadow Realm & Ancestors",
        emoji = "🗿",
        reqLevel = 90,
        description = "Master alchemists, shadow warlords, and ethereal shaman ancestors.",
        posXRatio = 0.92f,
        posYRatio = 0.22f,
        npcs = PICKPOCKET_NPCS.filter { it.id in listOf("grand_alchemist", "shadow_warlord", "shaman_ancestor") }
    )
)

data class TrickeryLootEntry(
    val item: InventoryItem,
    val totalQty: Int
)

data class PickpocketDrop(
    val itemId: String,
    val quantity: Int = 1,
    val dropWeight: Int = 10
)

fun getPickpocketLootPool(npcId: String): List<PickpocketDrop> {
    return when (npcId) {
        "man" -> listOf(
            PickpocketDrop("item_bread", 1, 30),
            PickpocketDrop("item_rune_air", 3, 40)
        )
        "silk_merchant" -> listOf(
            PickpocketDrop("item_stolen_silk", 1, 40),
            PickpocketDrop("item_rune_mind", 3, 40)
        )
        "farmer" -> listOf(
            PickpocketDrop("item_potato_seed", 2, 35),
            PickpocketDrop("item_cabbage_seed", 2, 35),
            PickpocketDrop("item_onion_seed", 2, 30)
        )
        "ham_member" -> listOf(
            PickpocketDrop("item_rune_earth", 3, 40),
            PickpocketDrop("item_willow_seed", 1, 20),
            PickpocketDrop("item_lockpick", 1, 20)
        )
        "zephyr_scout" -> listOf(
            PickpocketDrop("item_rune_air", 5, 35),
            PickpocketDrop("item_feather", 4, 35),
            PickpocketDrop("item_bread", 1, 30)
        )
        "street_urchin" -> listOf(
            PickpocketDrop("item_rune_water", 4, 35),
            PickpocketDrop("item_uncut_sapphire", 1, 25),
            PickpocketDrop("item_lockpick", 1, 30)
        )
        "shadow_miner" -> listOf(
            PickpocketDrop("item_coal_ore", 1, 40),
            PickpocketDrop("item_rune_fire", 4, 35),
            PickpocketDrop("item_uncut_sapphire", 1, 25)
        )
        "crag_smuggler" -> listOf(
            PickpocketDrop("item_iron_ore", 1, 40),
            PickpocketDrop("item_uncut_sapphire", 1, 30),
            PickpocketDrop("item_rune_earth", 4, 30)
        )
        "warrior" -> listOf(
            PickpocketDrop("item_iron_bar", 1, 35),
            PickpocketDrop("item_rune_body", 4, 35),
            PickpocketDrop("item_bronze_arrows", 5, 30)
        )
        "mist_smuggler" -> listOf(
            PickpocketDrop("item_rune_nature", 2, 35),
            PickpocketDrop("item_clean_sunleaf", 1, 35),
            PickpocketDrop("item_rune_water", 4, 30)
        )
        "hive_scavenger" -> listOf(
            PickpocketDrop("item_rune_earth", 4, 35),
            PickpocketDrop("item_coal_ore", 1, 35),
            PickpocketDrop("item_feather", 3, 30)
        )
        "market_trader" -> listOf(
            PickpocketDrop("item_rune_fire", 4, 35),
            PickpocketDrop("item_willow_seed", 1, 30),
            PickpocketDrop("item_bread", 2, 35)
        )
        "cliff_falconer" -> listOf(
            PickpocketDrop("item_rune_cosmic", 2, 35),
            PickpocketDrop("item_uncut_sapphire", 1, 30),
            PickpocketDrop("item_feather", 5, 35)
        )
        "stone_bandit" -> listOf(
            PickpocketDrop("item_coal_ore", 1, 35),
            PickpocketDrop("item_steel_bar", 1, 30),
            PickpocketDrop("item_rune_chaos", 2, 35)
        )
        "rogue" -> listOf(
            PickpocketDrop("item_iron_bar", 1, 30),
            PickpocketDrop("item_coal_ore", 1, 30),
            PickpocketDrop("item_rune_chaos", 3, 25),
            PickpocketDrop("item_lockpick", 1, 25)
        )
        "ore_lockpicker" -> listOf(
            PickpocketDrop("item_mithril_ore", 1, 35),
            PickpocketDrop("item_steel_bar", 1, 30),
            PickpocketDrop("item_lockpick", 1, 35)
        )
        "fen_bandit" -> listOf(
            PickpocketDrop("item_iron_bar", 1, 35),
            PickpocketDrop("item_rune_chaos", 3, 35),
            PickpocketDrop("item_willow_seed", 1, 30)
        )
        "goldenrod_gambler" -> listOf(
            PickpocketDrop("item_gold_ore", 1, 40),
            PickpocketDrop("item_rune_cosmic", 3, 35),
            PickpocketDrop("item_uncut_ruby", 1, 25)
        )
        "cave_goblin" -> listOf(
            PickpocketDrop("item_coal_ore", 1, 40),
            PickpocketDrop("item_iron_ore", 1, 35),
            PickpocketDrop("item_rune_cosmic", 2, 25)
        )
        "charcoal_smuggler" -> listOf(
            PickpocketDrop("item_willow_seed", 1, 35),
            PickpocketDrop("item_rune_nature", 2, 35),
            PickpocketDrop("item_rune_fire", 4, 30)
        )
        "master_farmer" -> listOf(
            PickpocketDrop("item_potato_seed", 3, 25),
            PickpocketDrop("item_watermelon_seed", 1, 20),
            PickpocketDrop("item_willow_seed", 1, 20),
            PickpocketDrop("item_tomato_seed", 2, 20),
            PickpocketDrop("item_sweetcorn_seed", 2, 15)
        )
        "guard" -> listOf(
            PickpocketDrop("item_iron_arrows", 5, 40),
            PickpocketDrop("item_steel_arrows", 3, 30),
            PickpocketDrop("item_rune_law", 2, 30)
        )
        "syndicate_bruiser" -> listOf(
            PickpocketDrop("item_steel_bar", 1, 35),
            PickpocketDrop("item_rune_body", 5, 35),
            PickpocketDrop("item_rune_chaos", 3, 30)
        )
        "bayou_witch" -> listOf(
            PickpocketDrop("item_rune_astral", 3, 35),
            PickpocketDrop("item_clean_sunleaf", 1, 35),
            PickpocketDrop("item_uncut_sapphire", 1, 30)
        )
        "grove_poacher" -> listOf(
            PickpocketDrop("item_willow_seed", 1, 30),
            PickpocketDrop("item_rune_law", 2, 35),
            PickpocketDrop("item_bowstring", 2, 35)
        )
        "trader" -> listOf(
            PickpocketDrop("item_oak_plank", 1, 35),
            PickpocketDrop("item_raw_salmon", 1, 35),
            PickpocketDrop("item_rune_astral", 2, 30)
        )
        "watchman" -> listOf(
            PickpocketDrop("item_steel_bar", 1, 35),
            PickpocketDrop("item_rune_nature", 2, 35),
            PickpocketDrop("item_bread", 2, 30)
        )
        "underground_fence" -> listOf(
            PickpocketDrop("item_bronze_bar", 2, 30),
            PickpocketDrop("item_rune_law", 3, 35),
            PickpocketDrop("item_uncut_emerald", 1, 35)
        )
        "wealthy_citizen" -> listOf(
            PickpocketDrop("item_uncut_sapphire", 1, 35),
            PickpocketDrop("item_rune_cosmic", 3, 35),
            PickpocketDrop("item_stolen_silk", 1, 30)
        )
        "spectral_medium" -> listOf(
            PickpocketDrop("item_rune_death", 3, 40),
            PickpocketDrop("item_rune_astral", 3, 35),
            PickpocketDrop("item_trout_elixir", 1, 25)
        )
        "desert_bandit" -> listOf(
            PickpocketDrop("item_gold_ore", 1, 35),
            PickpocketDrop("item_mithril_bar", 1, 30),
            PickpocketDrop("item_rune_chaos", 4, 35)
        )
        "meteor_smuggler" -> listOf(
            PickpocketDrop("item_rune_cosmic", 4, 35),
            PickpocketDrop("item_mithril_bar", 1, 35),
            PickpocketDrop("item_uncut_diamond", 1, 30)
        )
        "ardougne_knight" -> listOf(
            PickpocketDrop("item_steel_bar", 1, 40),
            PickpocketDrop("item_rune_law", 3, 40),
            PickpocketDrop("item_bread", 2, 20)
        )
        "sylvan_infiltrator" -> listOf(
            PickpocketDrop("item_clean_vervain", 1, 35),
            PickpocketDrop("item_rune_nature", 3, 35),
            PickpocketDrop("item_teak_plank", 1, 30)
        )
        "marsh_poacher" -> listOf(
            PickpocketDrop("item_raw_lobster", 1, 35),
            PickpocketDrop("item_rune_nature", 3, 35),
            PickpocketDrop("item_watermelon_seed", 1, 30)
        )
        "lumber_overseer" -> listOf(
            PickpocketDrop("item_teak_plank", 1, 40),
            PickpocketDrop("item_oak_logs", 1, 30),
            PickpocketDrop("item_willow_logs", 1, 30)
        )
        "menaphite_thug" -> listOf(
            PickpocketDrop("item_lockpick", 1, 35),
            PickpocketDrop("item_rune_death", 3, 35),
            PickpocketDrop("item_gold_ore", 1, 30)
        )
        "tzhaar" -> listOf(
            PickpocketDrop("item_coal_ore", 2, 35),
            PickpocketDrop("item_uncut_ruby", 1, 35),
            PickpocketDrop("item_rune_fire", 5, 30)
        )
        "phantom_thief" -> listOf(
            PickpocketDrop("item_rune_blood", 3, 35),
            PickpocketDrop("item_uncut_diamond", 1, 35),
            PickpocketDrop("item_rune_death", 3, 30)
        )
        "elementalist" -> listOf(
            PickpocketDrop("item_rune_fire", 4, 25),
            PickpocketDrop("item_rune_water", 4, 25),
            PickpocketDrop("item_rune_air", 4, 25),
            PickpocketDrop("item_clean_sunleaf", 1, 25)
        )
        "rum_smuggler" -> listOf(
            PickpocketDrop("item_mahogany_plank", 1, 35),
            PickpocketDrop("item_uncut_emerald", 1, 35),
            PickpocketDrop("item_rune_nature", 3, 30)
        )
        "sacred_druid_renegade" -> listOf(
            PickpocketDrop("item_clean_mystic_sage", 1, 35),
            PickpocketDrop("item_rune_law", 3, 35),
            PickpocketDrop("item_rune_blood", 2, 30)
        )
        "paladin" -> listOf(
            PickpocketDrop("item_rune_chaos", 4, 35),
            PickpocketDrop("item_rune_death", 3, 35),
            PickpocketDrop("item_rune_law", 3, 30)
        )
        "bog_pirate" -> listOf(
            PickpocketDrop("item_raw_lobster", 1, 35),
            PickpocketDrop("item_rune_death", 4, 35),
            PickpocketDrop("item_uncut_ruby", 1, 30)
        )
        "druid_elder" -> listOf(
            PickpocketDrop("item_clean_sunleaf", 1, 30),
            PickpocketDrop("item_clean_mystic_sage", 1, 35),
            PickpocketDrop("item_rune_nature", 4, 35)
        )
        "gnome" -> listOf(
            PickpocketDrop("item_uncut_ruby", 1, 35),
            PickpocketDrop("item_uncut_diamond", 1, 30),
            PickpocketDrop("item_rune_law", 4, 35)
        )
        "dark_mage" -> listOf(
            PickpocketDrop("item_rune_death", 4, 35),
            PickpocketDrop("item_rune_blood", 3, 35),
            PickpocketDrop("item_rune_soul", 2, 30)
        )
        "hero" -> listOf(
            PickpocketDrop("item_rune_death", 5, 30),
            PickpocketDrop("item_rune_blood", 4, 35),
            PickpocketDrop("item_uncut_diamond", 1, 35)
        )
        "vyrewatch" -> listOf(
            PickpocketDrop("item_rune_blood", 5, 40),
            PickpocketDrop("item_adamant_bar", 1, 30),
            PickpocketDrop("item_uncut_ruby", 1, 30)
        )
        "crystal_artisan" -> listOf(
            PickpocketDrop("item_uncut_diamond", 1, 35),
            PickpocketDrop("item_rune_law", 5, 35),
            PickpocketDrop("item_rune_soul", 3, 30)
        )
        "elf" -> listOf(
            PickpocketDrop("item_uncut_diamond", 1, 35),
            PickpocketDrop("item_rune_nature", 5, 35),
            PickpocketDrop("item_clean_vervain", 1, 30)
        )
        "tzhaar_ket" -> listOf(
            PickpocketDrop("item_rune_bar", 1, 35),
            PickpocketDrop("item_rune_death", 5, 35),
            PickpocketDrop("item_rune_wrath", 2, 30)
        )
        "grand_alchemist" -> listOf(
            PickpocketDrop("item_rune_bar", 1, 35),
            PickpocketDrop("item_rune_wrath", 3, 35),
            PickpocketDrop("item_uncut_diamond", 1, 30)
        )
        "shadow_warlord" -> listOf(
            PickpocketDrop("item_rune_bar", 1, 35),
            PickpocketDrop("item_rune_blood", 5, 35),
            PickpocketDrop("item_rune_wrath", 3, 30)
        )
        "shaman_ancestor" -> listOf(
            PickpocketDrop("item_rune_wrath", 4, 35),
            PickpocketDrop("item_rune_soul", 4, 35),
            PickpocketDrop("item_clean_mystic_sage", 2, 30)
        )
        else -> listOf(
            PickpocketDrop("item_bread", 1, 50),
            PickpocketDrop("item_rune_air", 2, 50)
        )
    }
}

fun rollPickpocketDrop(npcId: String): PickpocketDrop? {
    val pool = getPickpocketLootPool(npcId)
    if (pool.isEmpty()) return null
    // 70% chance to drop an item alongside coins
    if ((1..100).random() > 70) return null
    val totalWeight = pool.sumOf { it.dropWeight }
    val roll = (1..totalWeight).random()
    var cumulative = 0
    for (drop in pool) {
        cumulative += drop.dropWeight
        if (roll <= cumulative) {
            return drop
        }
    }
    return pool.first()
}
