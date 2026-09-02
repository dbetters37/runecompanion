package com.example.data.models

enum class QuestDifficulty(
    val displayName: String,
    val colorHex: Long,
    val approxNormalMinutes: Float
) {
    NOVICE("Novice", 0xFF4CAF50, 5f),
    INTERMEDIATE("Intermediate", 0xFF2196F3, 15f),
    EXPERIENCED("Experienced", 0xFFFF9800, 35f),
    MASTER("Master", 0xFF9C27B0, 60f),
    GRANDMASTER("Grandmaster", 0xFFE91E63, 120f);

    val baseDurationSeconds: Int
        get() = (approxNormalMinutes * 60f * 1.8f).toInt()

    val rarityLabel: String
        get() = when (this) {
            NOVICE -> "⭐ Common"
            INTERMEDIATE -> "⭐⭐ Uncommon"
            EXPERIENCED -> "⭐⭐⭐ Rare"
            MASTER -> "🔮 Epic"
            GRANDMASTER -> "👑 Mythic"
        }

    val badgeIcon: String
        get() = when (this) {
            NOVICE -> "🌱"
            INTERMEDIATE -> "⚔️"
            EXPERIENCED -> "⚡"
            MASTER -> "🔮"
            GRANDMASTER -> "👑"
        }
}

data class QuestRequirementItem(
    val itemId: String,
    val itemName: String,
    val itemEmoji: String,
    val requiredQty: Int
)

data class OsrsQuest(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val difficulty: QuestDifficulty,
    val recCombatLevel: Int,
    val reqSkill: OsrsSkill? = null,
    val reqSkillLevel: Int = 1,
    val reqQuestIds: List<String> = emptyList(),
    val description: String,
    val requiredItems: List<QuestRequirementItem> = emptyList(),
    val questPoints: Int,
    val rewardXpMap: Map<OsrsSkill, Long>,
    val rewardGp: Long,
    val rewardItemName: String? = null,
    val rewardItemEmoji: String? = null,
    val rewardItemId: String? = null,
    val unlockedFeatures: List<String> = emptyList(),
    val isMembers: Boolean = true,
    val isMiniquest: Boolean = false,
    val approxNormalMinutes: Float? = null,
    val chapterId: String? = null
) {
    fun calculateDurationSeconds(petCombatLevel: Int): Int {
        val mins = approxNormalMinutes ?: difficulty.approxNormalMinutes
        val baseSeconds = (mins * 60f * 1.8f).toInt()
        val ratio = (recCombatLevel.toFloat() / petCombatLevel.coerceAtLeast(1).toFloat()).coerceIn(0.6f, 1.4f)
        return (baseSeconds.toFloat() * ratio).toInt().coerceAtLeast(10)
    }

    val isHighValueReward: Boolean
        get() = rewardGp >= 5000L || questPoints >= 3 || unlockedFeatures.isNotEmpty() || difficulty == QuestDifficulty.MASTER || difficulty == QuestDifficulty.GRANDMASTER

    val rewardValueLabel: String?
        get() = when {
            difficulty == QuestDifficulty.GRANDMASTER -> "👑 MYTHIC REWARD"
            difficulty == QuestDifficulty.MASTER -> "🔮 LEGENDARY REWARD"
            unlockedFeatures.isNotEmpty() -> "🔑 UNLOCKS FEATURE"
            rewardGp >= 10000L || questPoints >= 4 -> "✨ HIGH VALUE LOOT"
            rewardItemName != null -> "🎁 RARE ITEM REWARD"
            else -> null
        }
}

fun formatQuestDuration(seconds: Int): String {
    if (seconds < 60) return "${seconds}s"
    val mins = seconds / 60
    val secs = seconds % 60
    if (mins < 60) {
        return if (secs == 0) "${mins}m" else "${mins}m ${secs}s"
    }
    val hours = mins / 60
    val remMins = mins % 60
    return if (remMins == 0) "${hours}h" else "${hours}h ${remMins}m"
}

object OsrsQuestData {
    val QUESTS = listOf(
        // Core Shaman Expeditions & Trials
        OsrsQuest(
            id = "quest_cooks_assistant",
            name = "The Grand Shaman Feast",
            iconEmoji = "🍲",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 3,
            reqSkill = OsrsSkill.COOKING,
            reqSkillLevel = 1,
            description = "The Spirit Valley Hearth Master needs sacred ingredients to bake a ceremonial spirit cake for the High Elder!",
            requiredItems = listOf(
                QuestRequirementItem("item_bread", "Ancestral Bread", "🍞", 2),
                QuestRequirementItem("item_raw_trout", "Sacred Stream Trout", "🐟", 1)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(OsrsSkill.COOKING to 300L),
            rewardGp = 500L,
            rewardItemName = "Master Shaman's Feathered Headdress & Spirit Cake",
            rewardItemEmoji = "🎂",
            rewardItemId = "item_chef_hat"
        ),
        OsrsQuest(
            id = "quest_demon_slayer",
            name = "Shadow Fiend Banishment",
            iconEmoji = "😈",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 15,
            reqSkill = OsrsSkill.ATTACK,
            reqSkillLevel = 10,
            description = "Infiltrate the dark subterranean catacombs and banish the Corrupted Shadow Fiend using a consecrated Sunblade!",
            requiredItems = listOf(
                QuestRequirementItem("item_bones", "Consecrated Bones", "🦴", 3),
                QuestRequirementItem("item_ore_iron", "Star Iron Ore", "🪨", 2)
            ),
            questPoints = 3,
            rewardXpMap = mapOf(OsrsSkill.ATTACK to 1000L),
            rewardGp = 1000L,
            rewardItemName = "Consecrated Sunblade Katana",
            rewardItemEmoji = "🗡️",
            rewardItemId = "item_silverlight"
        ),
        OsrsQuest(
            id = "quest_restless_ghost",
            name = "Wandering Ancestor's Solace",
            iconEmoji = "👻",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 5,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 5,
            description = "Assist the High Shaman Priest in laying a restless ancestor spirit to peace by recovering its stolen totem relic!",
            requiredItems = listOf(
                QuestRequirementItem("item_bones", "Purification Bones", "🦴", 2)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(OsrsSkill.MAGIC to 1125L),
            rewardGp = 500L,
            rewardItemName = "Amulet of Spirit Whispers",
            rewardItemEmoji = "📿",
            rewardItemId = "item_ghostspeak_amulet"
        ),
        OsrsQuest(
            id = "quest_romeo_juliet",
            name = "Star-Crossed Spirit Elixir",
            iconEmoji = "💌",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 3,
            reqSkill = OsrsSkill.HERBLORE,
            reqSkillLevel = 5,
            description = "Brew a sacred harmony elixir to unite two young spirit acolytes separated by rival clan chieftains!",
            requiredItems = listOf(
                QuestRequirementItem("item_bread", "Offering Bread", "🍞", 1),
                QuestRequirementItem("item_raw_trout", "Herb Offering", "🐟", 1)
            ),
            questPoints = 5,
            rewardXpMap = mapOf(OsrsSkill.HERBLORE to 250L),
            rewardGp = 1000L,
            rewardItemName = "Spirit Harmony Elixir",
            rewardItemEmoji = "🧪",
            rewardItemId = "item_cadava_potion"
        ),
        OsrsQuest(
            id = "quest_shield_of_arrav",
            name = "Shield of Ancestral Totems",
            iconEmoji = "🛡️",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 10,
            reqSkill = OsrsSkill.THIEVING,
            reqSkillLevel = 10,
            description = "Work alongside rogue scout guilds to recover the twin sacred halves of the ancient Ancestral Totem Shield!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_bronze", "Bronze Bar", "🧱", 2),
                QuestRequirementItem("item_ore_iron", "Iron Ore", "🪨", 2)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(OsrsSkill.THIEVING to 600L),
            rewardGp = 1200L,
            rewardItemName = "Ancestral Totem Shield Crest",
            rewardItemEmoji = "🛡️",
            rewardItemId = "item_shield_half"
        ),
        OsrsQuest(
            id = "quest_ernest_the_chicken",
            name = "Curse of the Feathered Familiar",
            iconEmoji = "🐔",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 5,
            reqSkill = OsrsSkill.FLETCHING,
            reqSkillLevel = 8,
            description = "Rescue an apprentice transformed into a feathered spirit fowl by an erratic alchemist at Midnight Manor!",
            requiredItems = listOf(
                QuestRequirementItem("item_logs", "Logs", "🪵", 2),
                QuestRequirementItem("item_ore_copper", "Copper Ore", "🪨", 2)
            ),
            questPoints = 4,
            rewardXpMap = mapOf(OsrsSkill.FLETCHING to 500L),
            rewardGp = 1500L,
            rewardItemName = "Midnight Manor Spirit Portal Access",
            rewardItemEmoji = "🪶",
            rewardItemId = null,
            unlockedFeatures = listOf("🗺️ Lore Unlock: Midnight Manor Shortcut & Oil Access")
        ),
        OsrsQuest(
            id = "quest_vampyre_slayer",
            name = "Shadow Lord's Downfall",
            iconEmoji = "🧛",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 20,
            reqSkill = OsrsSkill.ATTACK,
            reqSkillLevel = 15,
            description = "Slay the Corrupted Shadow Lord terrorizing the valley catacombs using a consecrated oak stake and garlic wreath!",
            requiredItems = listOf(
                QuestRequirementItem("item_oak_logs", "Consecrated Stake (Oak Logs)", "🪵", 2),
                QuestRequirementItem("item_trout", "Food Ration (Cooked Trout)", "🐟", 2)
            ),
            questPoints = 3,
            rewardXpMap = mapOf(OsrsSkill.ATTACK to 4825L, OsrsSkill.HITPOINTS to 1500L),
            rewardGp = 3000L,
            rewardItemName = "Valley Shade Cleansing Blessing",
            rewardItemEmoji = "✨",
            rewardItemId = null,
            unlockedFeatures = listOf("🗺️ Lore Unlock: Valley Shade Vanquished & Peace Restored")
        ),
        OsrsQuest(
            id = "quest_imp_catcher",
            name = "Sprite Essence Recovery",
            iconEmoji = "👿",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 10,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 10,
            description = "Recover four elemental spirit spheres stolen from the High Archmage by mischievous woodland sprites!",
            requiredItems = listOf(
                QuestRequirementItem("item_ore_tin", "Tin Ore", "🪨", 2),
                QuestRequirementItem("item_ore_copper", "Copper Ore", "🪨", 2)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(OsrsSkill.MAGIC to 875L),
            rewardGp = 800L,
            rewardItemName = "Amulet of Spirit Focus",
            rewardItemEmoji = "📿",
            rewardItemId = "item_amulet_of_accuracy"
        ),
        OsrsQuest(
            id = "quest_prince_ali_rescue",
            name = "Sunken Dunes Emissary Rescue",
            iconEmoji = "🕌",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 15,
            reqSkill = OsrsSkill.THIEVING,
            reqSkillLevel = 15,
            description = "Infiltrate the desert fortress and liberate the captured High Shaman emissary!",
            requiredItems = listOf(
                QuestRequirementItem("item_bread", "Bribe Bread", "🍞", 2),
                QuestRequirementItem("item_bar_bronze", "Key Material (Bronze Bar)", "🧱", 1)
            ),
            questPoints = 3,
            rewardXpMap = mapOf(OsrsSkill.THIEVING to 1000L),
            rewardGp = 2000L,
            rewardItemName = "Sunken Dunes Passage Seal",
            rewardItemEmoji = "🎟️",
            rewardItemId = null,
            unlockedFeatures = listOf("🗺️ Region Unlock: Sunken Dunes Free Passage (Lore / Map Access)")
        ),
        OsrsQuest(
            id = "quest_dorics_quest",
            name = "Granite Blacksmith's Ore Supply",
            iconEmoji = "⛏️",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 3,
            reqSkill = OsrsSkill.SMITHING,
            reqSkillLevel = 15,
            description = "Mine copper, tin, and iron ore for the master artisan blacksmith in Granite Citadel!",
            requiredItems = listOf(
                QuestRequirementItem("item_ore_copper", "Copper Ore", "🪨", 2),
                QuestRequirementItem("item_ore_iron", "Iron Ore", "🪨", 2)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(OsrsSkill.SMITHING to 1300L),
            rewardGp = 1000L,
            rewardItemName = "Granite Forge Attunement",
            rewardItemEmoji = "🔨",
            rewardItemId = null,
            unlockedFeatures = listOf("🔓 In-Game Feature: Granite Citadel Sacred Forge Access")
        ),
        OsrsQuest(
            id = "quest_black_knights_fortress",
            name = "Shadow Citadel Sabotage",
            iconEmoji = "🏰",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 15,
            reqSkill = OsrsSkill.DEFENCE,
            reqSkillLevel = 12,
            description = "Infiltrate the Shadow Citadel and sabotage their dark siege altar!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2),
                QuestRequirementItem("item_bread", "Ration Bread", "🍞", 2)
            ),
            questPoints = 3,
            rewardXpMap = mapOf(OsrsSkill.DEFENCE to 2500L),
            rewardGp = 2500L,
            rewardItemName = "Radiant Light Templar Sigil",
            rewardItemEmoji = "🛡️",
            rewardItemId = null,
            unlockedFeatures = listOf("🗺️ Lore Unlock: Order of Light Commendation")
        ),
        OsrsQuest(
            id = "quest_witchs_potion",
            name = "High Witch's Cauldron",
            iconEmoji = "🧹",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 5,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 5,
            description = "Gather rare herbs and essence to assist the High Witch in brewing a spirit draught!",
            requiredItems = listOf(
                QuestRequirementItem("item_bones", "Bones", "🦴", 1),
                QuestRequirementItem("item_raw_shrimps", "Raw Shrimps", "🦐", 1)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(OsrsSkill.MAGIC to 325L),
            rewardGp = 500L,
            rewardItemName = "High Shaman's Essence Draught",
            rewardItemEmoji = "🧪",
            rewardItemId = "item_rat_tail_brew"
        ),
        OsrsQuest(
            id = "quest_knights_sword",
            name = "Forging the Sentinel Blade",
            iconEmoji = "🗡️",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 10,
            reqSkill = OsrsSkill.SMITHING,
            reqSkillLevel = 15,
            description = "Assist the master artisan in forging a sacred ceremonial blade from rare Star-Iron ore!",
            requiredItems = listOf(
                QuestRequirementItem("item_ore_iron", "Iron Ore", "🪨", 2),
                QuestRequirementItem("item_bread", "Artisan Offering (Fresh Bread)", "🍞", 1)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(OsrsSkill.SMITHING to 12725L),
            rewardGp = 1500L,
            rewardItemName = "Star-Iron Spirit Blade",
            rewardItemEmoji = "⚔️",
            rewardItemId = "item_blurite_sword"
        ),
        OsrsQuest(
            id = "quest_goblin_diplomacy",
            name = "Wildland Chieftain Reconciliation",
            iconEmoji = "👺",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 5,
            reqSkill = OsrsSkill.FLETCHING,
            reqSkillLevel = 10,
            description = "Settle the long-standing totem tunic color dispute between rival wildland chieftain brothers! Unlocks the Trough Slosh recipe for Animal Husbandry.",
            requiredItems = listOf(
                QuestRequirementItem("item_logs", "Logs", "🪵", 2),
                QuestRequirementItem("item_ore_copper", "Copper Ore", "🪨", 1)
            ),
            questPoints = 5,
            rewardXpMap = mapOf(OsrsSkill.FLETCHING to 200L),
            rewardGp = 1000L,
            rewardItemName = "Wildland Totem Tunic & Trough Slosh Recipe",
            rewardItemEmoji = "🍲",
            rewardItemId = "item_goblin_armor"
        ),
        OsrsQuest(
            id = "quest_pirates_treasure",
            name = "Tide Rover's Hidden Stash",
            iconEmoji = "🏴‍☠️",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 10,
            reqSkill = OsrsSkill.SAILING,
            reqSkillLevel = 10,
            description = "Smuggle rare island nectar past harbor guards to uncover Captain Redbeard's buried spirit chest!",
            requiredItems = listOf(
                QuestRequirementItem("item_bread", "Sailor Ration", "🍞", 2),
                QuestRequirementItem("item_raw_trout", "Fresh Trout", "🐟", 1)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(OsrsSkill.SAILING to 1000L, OsrsSkill.THIEVING to 500L),
            rewardGp = 2000L,
            rewardItemName = "Tide Raider's Treasure Key",
            rewardItemEmoji = "🧰",
            rewardItemId = "item_casket_key"
        ),
        OsrsQuest(
            id = "quest_rune_mysteries",
            name = "Arcane Essence Discovery",
            iconEmoji = "🔮",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 3,
            reqSkill = OsrsSkill.RUNECRAFT,
            reqSkillLevel = 5,
            description = "Assist the High Archmage in uncovering the ancient secret of Arcane Essence for Spirit Runecrafting!",
            requiredItems = listOf(
                QuestRequirementItem("item_ore_tin", "Tin Ore (Research)", "🪨", 1),
                QuestRequirementItem("item_logs", "Logs (Altar Catalyst)", "🪵", 1)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(OsrsSkill.RUNECRAFT to 500L, OsrsSkill.MAGIC to 500L),
            rewardGp = 1000L,
            rewardItemName = "Essence of Zephyr Runes",
            rewardItemEmoji = "💨",
            rewardItemId = "item_rune_air"
        ),
        OsrsQuest(
            id = "quest_below_ice_mountain",
            name = "Glacial Ruins Expedition",
            iconEmoji = "🧊",
            difficulty = QuestDifficulty.INTERMEDIATE,
            recCombatLevel = 30,
            reqSkill = OsrsSkill.SMITHING,
            reqSkillLevel = 25,
            description = "Explore the subterranean ruins below Glacial Mountain with fellow spirit scholars!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2),
                QuestRequirementItem("item_trout", "Cooked Trout", "🐟", 2)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(OsrsSkill.SMITHING to 2000L),
            rewardGp = 5000L,
            rewardItemName = "Glacial Barronite Ward",
            rewardItemEmoji = "🛡️",
            rewardItemId = "item_barronite_guard"
        ),
        OsrsQuest(
            id = "quest_dragon_slayer_1",
            name = "Apex Wyvern Spirit Trial",
            iconEmoji = "🐲",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 45,
            reqSkill = OsrsSkill.DEFENCE,
            reqSkillLevel = 32,
            reqQuestIds = listOf("quest_cooks_assistant", "quest_knights_sword"),
            description = "Sail to Isle of Ashes and conquer the Apex Green Wyvern Spirit to wear Titan Armor!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 3),
                QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 2),
                QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 1)
            ),
            questPoints = 5,
            rewardXpMap = mapOf(
                OsrsSkill.ATTACK to 18650L,
                OsrsSkill.DEFENCE to 18650L,
                OsrsSkill.SLAYER to 5000L
            ),
            rewardGp = 12000L,
            rewardItemName = "Apex Wyvern Horn Crest",
            rewardItemEmoji = "🐉",
            rewardItemId = "item_elvarg_head",
            unlockedFeatures = listOf("🔓 In-Game Feature: Wield Attuned Titan Spirit Armor")
        ),

        // P2P / Members Quests
        OsrsQuest(
            id = "quest_druidic_ritual",
            name = "Grove Purification Ritual",
            iconEmoji = "🌿",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 10,
            reqSkill = OsrsSkill.HERBLORE,
            reqSkillLevel = 5,
            description = "Purify the ancient stone circle in Whispering Grove and unlock the art of Herblore!",
            requiredItems = listOf(
                QuestRequirementItem("item_raw_trout", "Raw Fish Offering", "🐟", 1),
                QuestRequirementItem("item_bones", "Purification Bones", "🦴", 1)
            ),
            questPoints = 4,
            rewardXpMap = mapOf(OsrsSkill.HERBLORE to 250L),
            rewardGp = 1000L,
            rewardItemName = "Herblore Skill Access",
            rewardItemEmoji = "🌿",
            rewardItemId = null,
            unlockedFeatures = listOf("🔓 In-Game Feature: Herblore Skill Unlocked")
        ),
        OsrsQuest(
            id = "quest_waterfall_quest",
            name = "Waterfall Cavern Spirit Shrine",
            iconEmoji = "🌊",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 25,
            reqSkill = OsrsSkill.ATTACK,
            reqSkillLevel = 15,
            description = "Explore the ancient cascading waterfall caves to claim the long-lost sacred spirit totems of ancient elders!",
            requiredItems = listOf(
                QuestRequirementItem("item_bread", "Offering Bread", "🍞", 2),
                QuestRequirementItem("item_raw_trout", "Sacred Stream Trout", "🐟", 1)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(
                OsrsSkill.ATTACK to 13750L
            ),
            rewardGp = 5000L,
            rewardItemName = "Amulet of Cascading Waters",
            rewardItemEmoji = "📿",
            rewardItemId = "item_glarials_amulet"
        ),
        OsrsQuest(
            id = "quest_witchs_house",
            name = "Enchanted Grove Manor Orb",
            iconEmoji = "🧙‍♀️",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 20,
            reqSkill = OsrsSkill.HITPOINTS,
            reqSkillLevel = 15,
            description = "Retrieve an acolyte's lost enchanted spirit orb from the courtyard of a dangerous haunted manor!",
            requiredItems = listOf(
                QuestRequirementItem("item_bread", "Distraction Treat", "🍞", 2),
                QuestRequirementItem("item_bones", "Bones", "🦴", 1)
            ),
            questPoints = 4,
            rewardXpMap = mapOf(OsrsSkill.HITPOINTS to 6325L),
            rewardGp = 2000L,
            rewardItemName = "Mystic Grove Manor Key",
            rewardItemEmoji = "⚽",
            rewardItemId = null,
            unlockedFeatures = listOf("🗺️ Lore Unlock: Mystic Grove Manor Access")
        ),
        OsrsQuest(
            id = "quest_tree_gnome_village",
            name = "Woodland Protection Orbs",
            iconEmoji = "🌳",
            difficulty = QuestDifficulty.INTERMEDIATE,
            recCombatLevel = 30,
            reqSkill = OsrsSkill.WOODCUTTING,
            reqSkillLevel = 25,
            description = "Help the Woodland Spirit Chieftain recover stolen Protection Orbs from corrupted invaders!",
            requiredItems = listOf(
                QuestRequirementItem("item_oak_logs", "Oak Logs", "🪵", 3),
                QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(OsrsSkill.ATTACK to 11400L),
            rewardGp = 3000L,
            rewardItemName = "Sacred Spirit Tree Network Attunement",
            rewardItemEmoji = "🌳",
            rewardItemId = null,
            unlockedFeatures = listOf("🗺️ Travel Unlock: Spirit Tree Fast-Travel Network (Lore / Map Access)")
        ),
        OsrsQuest(
            id = "quest_the_grand_tree",
            name = "The Elder Worldtree Conspiracy",
            iconEmoji = "🌴",
            difficulty = QuestDifficulty.INTERMEDIATE,
            recCombatLevel = 35,
            reqSkill = OsrsSkill.AGILITY,
            reqSkillLevel = 30,
            reqQuestIds = listOf("quest_tree_gnome_village"),
            description = "Uncover and foil a dark sabotage plot against the High Elder Council at the Elder Worldtree!",
            requiredItems = listOf(
                QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2),
                QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)
            ),
            questPoints = 5,
            rewardXpMap = mapOf(
                OsrsSkill.ATTACK to 18400L,
                OsrsSkill.AGILITY to 7900L,
                OsrsSkill.MAGIC to 2150L
            ),
            rewardGp = 8000L,
            rewardItemName = "Zephyr Glider Wingflight Pass",
            rewardItemEmoji = "🛩️",
            rewardItemId = null,
            unlockedFeatures = listOf("🗺️ Travel Unlock: Zephyr Gliders Aerial Network (Lore / Map Access)")
        ),
        OsrsQuest(
            id = "quest_priest_in_peril",
            name = "Sanctuary Border Defense",
            iconEmoji = "⛪",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 25,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 15,
            description = "Protect the Sanctuary Guardian at the eastern border temple and unlock passage to Mistwood Marsh!",
            requiredItems = listOf(
                QuestRequirementItem("item_bones", "Consecrated Bones", "🦴", 3),
                QuestRequirementItem("item_bread", "Monk Bread", "🍞", 2)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(OsrsSkill.MAGIC to 1406L),
            rewardGp = 2000L,
            rewardItemName = "Wolfbane Spirit Dagger",
            rewardItemEmoji = "🗡️",
            rewardItemId = "item_wolfbane_dagger",
            unlockedFeatures = listOf("🗺️ Region Unlock: Mistwood Border Gate Access Pass")
        ),
        OsrsQuest(
            id = "quest_nature_spirit",
            name = "Swamp Guardian Consecration",
            iconEmoji = "🍃",
            difficulty = QuestDifficulty.NOVICE,
            recCombatLevel = 30,
            reqSkill = OsrsSkill.FLETCHING,
            reqSkillLevel = 18,
            reqQuestIds = listOf("quest_priest_in_peril"),
            description = "Assist an ancient ascetic in becoming a Nature Guardian Spirit to cleanse the decaying swamp!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_iron", "Iron Bar (Sickle)", "🧱", 2),
                QuestRequirementItem("item_trout", "Cooked Trout", "🐟", 2)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.FLETCHING to 2000L,
                OsrsSkill.HITPOINTS to 2000L,
                OsrsSkill.DEFENCE to 2000L
            ),
            rewardGp = 3000L,
            rewardItemName = "Consecrated Silver Sickle (b)",
            rewardItemEmoji = "🌙",
            rewardItemId = "item_silver_sickle_b",
            unlockedFeatures = listOf("💰 Permanent 2x Multiplier on Gold earned through Trickery (Thieving)")
        ),
        OsrsQuest(
            id = "quest_holy_grail",
            name = "Quest for the Sacred Chalice",
            iconEmoji = "🏆",
            difficulty = QuestDifficulty.INTERMEDIATE,
            recCombatLevel = 45,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 30,
            description = "Embark on an epic holy pilgrimage alongside Light Templars to locate the long-lost Sacred Chalice of Purity!",
            requiredItems = listOf(
                QuestRequirementItem("item_big_bones", "Ancient Bones", "🦴", 2),
                QuestRequirementItem("item_bar_gold", "Gold Bar", "🪙", 2)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.MAGIC to 11000L,
                OsrsSkill.DEFENCE to 15300L
            ),
            rewardGp = 10000L,
            rewardItemName = "Sacred Chalice Blessing & Kingdom Consecration",
            rewardItemEmoji = "🔔",
            rewardItemId = null,
            unlockedFeatures = listOf("🗺️ Lore Unlock: Sacred Kingdom Consecration")
        ),
        OsrsQuest(
            id = "quest_temple_of_ikov",
            name = "Subterranean Sun Temple",
            iconEmoji = "🏹",
            difficulty = QuestDifficulty.INTERMEDIATE,
            recCombatLevel = 40,
            reqSkill = OsrsSkill.RANGED,
            reqSkillLevel = 40,
            description = "Infiltrate the subterranean sun temple and retrieve the legendary Staff of Sunfire!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2),
                QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(
                OsrsSkill.RANGED to 10500L,
                OsrsSkill.FLETCHING to 8000L
            ),
            rewardGp = 5000L,
            rewardItemName = "Zephyr Feather Boots",
            rewardItemEmoji = "🥾",
            rewardItemId = "item_boots_of_lightness"
        ),
        OsrsQuest(
            id = "quest_fight_arena",
            name = "Colosseum of the Corrupted",
            iconEmoji = "🏟️",
            difficulty = QuestDifficulty.INTERMEDIATE,
            recCombatLevel = 35,
            reqSkill = OsrsSkill.ATTACK,
            reqSkillLevel = 30,
            description = "Infiltrate the warlord's gladiatorial arena to free captive villagers and master beast tamers!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 3),
                QuestRequirementItem("item_trout", "Cooked Trout", "🐟", 3)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.ATTACK to 12175L,
                OsrsSkill.THIEVING to 2175L
            ),
            rewardGp = 4000L,
            rewardItemName = "Champion Gladiator Battle Harness",
            rewardItemEmoji = "🛡️",
            rewardItemId = "item_khazard_armor"
        ),
        OsrsQuest(
            id = "quest_horror_from_the_deep",
            name = "Tide Sentinel Terror",
            iconEmoji = "🦑",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 50,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 40,
            description = "Investigate the stormy coastal beacon and banish the colossal Deep Tide Leviathan!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2),
                QuestRequirementItem("item_swordfish", "Cooked Swordfish", "🐟", 2),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.RANGED to 4662L,
                OsrsSkill.MAGIC to 4662L,
                OsrsSkill.ATTACK to 4662L
            ),
            rewardGp = 10000L,
            rewardItemName = "Tome of Elemental Spirits",
            rewardItemEmoji = "📖",
            rewardItemId = "item_god_book"
        ),
        OsrsQuest(
            id = "quest_animal_magnetism",
            name = "Spectral Magnet Device",
            iconEmoji = "🎒",
            difficulty = QuestDifficulty.INTERMEDIATE,
            recCombatLevel = 30,
            reqSkill = OsrsSkill.SLAYER,
            reqSkillLevel = 25,
            reqQuestIds = listOf("quest_restless_ghost", "quest_ernest_the_chicken"),
            description = "Help the arcane scholar construct an enchanted spirit magnet to magnetically retrieve projectile arrows!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2),
                QuestRequirementItem("item_oak_logs", "Oak Logs", "🪵", 2),
                QuestRequirementItem("item_bones", "Bones", "🦴", 2)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(
                OsrsSkill.FLETCHING to 1000L,
                OsrsSkill.FLETCHING to 1000L,
                OsrsSkill.SLAYER to 1000L,
                OsrsSkill.WOODCUTTING to 2500L
            ),
            rewardGp = 5000L,
            rewardItemName = "Spirit Magnetic Quiver",
            rewardItemEmoji = "🎒",
            rewardItemId = "item_avas_accumulator"
        ),
        OsrsQuest(
            id = "quest_lost_city",
            name = "Gateway to the Celestial City",
            iconEmoji = "💎",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 50,
            reqSkill = OsrsSkill.FLETCHING,
            reqSkillLevel = 35,
            description = "Defeat the ancient tree spirit guardian to unlock the hidden gateway to the Celestial City!",
            requiredItems = listOf(
                QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2),
                QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2),
                QuestRequirementItem("item_big_bones", "Big Bones", "🦴", 1)
            ),
            questPoints = 3,
            rewardXpMap = mapOf(
                OsrsSkill.FLETCHING to 5000L,
                OsrsSkill.WOODCUTTING to 5000L
            ),
            rewardGp = 10000L,
            rewardItemName = "Celestial Spirit Branch Staff",
            rewardItemEmoji = "🪄",
            rewardItemId = "item_dramen_staff",
            unlockedFeatures = listOf("🔓 In-Game Feature: Wield Dragon Blades")
        ),
        OsrsQuest(
            id = "quest_fremennik_trials",
            name = "Trials of the Northland Chieftains",
            iconEmoji = "🛡️",
            difficulty = QuestDifficulty.INTERMEDIATE,
            recCombatLevel = 40,
            reqSkill = OsrsSkill.FLETCHING,
            reqSkillLevel = 40,
            description = "Pass seven rigorous trials set by the Northern Elders to become an honored Frost Spirit Champion!",
            requiredItems = listOf(
                QuestRequirementItem("item_raw_salmon", "Raw Salmon", "🐟", 2),
                QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2),
                QuestRequirementItem("item_oak_logs", "Oak Logs", "🪵", 2)
            ),
            questPoints = 3,
            rewardXpMap = mapOf(
                OsrsSkill.ATTACK to 2812L,
                OsrsSkill.DEFENCE to 2812L,
                OsrsSkill.FLETCHING to 2812L,
                OsrsSkill.WOODCUTTING to 2812L
            ),
            rewardGp = 8000L,
            rewardItemName = "Frost Spirit Chieftain Seal",
            rewardItemEmoji = "🪖",
            rewardItemId = null,
            unlockedFeatures = listOf("🗺️ Lore Unlock: Frost Realm Chieftain Honor")
        ),
        OsrsQuest(
            id = "quest_fremennik_isles",
            name = "Isles of the Glacial Frostguard",
            iconEmoji = "👑",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 60,
            reqSkill = OsrsSkill.FLETCHING,
            reqSkillLevel = 56,
            reqQuestIds = listOf("quest_fremennik_trials"),
            description = "Unite the twin northern island spirit clans and slay the Frost Behemoth King in his glacial peak throne!",
            requiredItems = listOf(
                QuestRequirementItem("item_oak_plank", "Oak Planks", "🪵", 3),
                QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2),
                QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 2)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(
                OsrsSkill.FLETCHING to 10000L,
                OsrsSkill.WOODCUTTING to 10000L
            ),
            rewardGp = 15000L,
            rewardItemName = "Crown of Sovereign Valour",
            rewardItemEmoji = "👑",
            rewardItemId = "item_helm_of_neitiznot"
        ),
        OsrsQuest(
            id = "quest_lunar_diplomacy",
            name = "Lunar Spirit Dream Attunement",
            iconEmoji = "🌙",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 70,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 65,
            reqQuestIds = listOf("quest_fremennik_isles", "quest_lost_city"),
            description = "Sail to Lunar Isle and achieve inner dream harmony to master the sacred art of Lunar Spirit Sorcery!",
            requiredItems = listOf(
                QuestRequirementItem("item_maple_logs", "Maple Logs", "🪵", 3),
                QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.MAGIC to 5000L,
                OsrsSkill.RUNECRAFT to 5000L
            ),
            rewardGp = 25000L,
            rewardItemName = "Moonclan Spirit Staff & Celestial Robes",
            rewardItemEmoji = "🌙",
            rewardItemId = "item_lunar_staff",
            unlockedFeatures = listOf("📜 Spellbook Unlock: Lunar Spirit Incantations (Lore / World Feature)")
        ),
        OsrsQuest(
            id = "quest_underground_pass",
            name = "Treacherous Abyss Passage",
            iconEmoji = "🕳️",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 55,
            reqSkill = OsrsSkill.AGILITY,
            reqSkillLevel = 35,
            description = "Traverse a dark subterranean abyss pass and defeat the Corrupted Archmage lurking in the shadows!",
            requiredItems = listOf(
                QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2),
                QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2),
                QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 3)
            ),
            questPoints = 5,
            rewardXpMap = mapOf(OsrsSkill.AGILITY to 3000L),
            rewardGp = 10000L,
            rewardItemName = "Staff of the Dark Abyss",
            rewardItemEmoji = "🪄",
            rewardItemId = "item_ibans_staff"
        ),
        OsrsQuest(
            id = "quest_regicide",
            name = "Forest Realm Tyrant Downfall",
            iconEmoji = "🪓",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 65,
            reqSkill = OsrsSkill.AGILITY,
            reqSkillLevel = 56,
            reqQuestIds = listOf("quest_underground_pass"),
            description = "Journey deep into the overgrown mist forest to overthrow the tyrant king and restore peace!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2),
                QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 3),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)
            ),
            questPoints = 3,
            rewardXpMap = mapOf(
                OsrsSkill.AGILITY to 13750L,
                OsrsSkill.FLETCHING to 13750L
            ),
            rewardGp = 20000L,
            rewardItemName = "Dragonkin Halberd",
            rewardItemEmoji = "🪓",
            rewardItemId = "item_dragon_halberd",
            unlockedFeatures = listOf("🗺️ Region Unlock: Mist Forest Realm Access")
        ),
        OsrsQuest(
            id = "quest_roving_elves",
            name = "Tomb of the Ancient Spirit Guardians",
            iconEmoji = "🏹",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 70,
            reqSkill = OsrsSkill.AGILITY,
            reqSkillLevel = 56,
            reqQuestIds = listOf("quest_regicide"),
            description = "Assist woodland wanderers in consecrating the sacred shrine of ancient elven spirit guardians!",
            requiredItems = listOf(
                QuestRequirementItem("item_yew_logs", "Yew Logs", "🪵", 2),
                QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)
            ),
            questPoints = 1,
            rewardXpMap = mapOf(OsrsSkill.AGILITY to 10000L),
            rewardGp = 25000L,
            rewardItemName = "Prismatic Crystal Bow",
            rewardItemEmoji = "🏹",
            rewardItemId = "item_crystal_bow"
        ),
        OsrsQuest(
            id = "quest_monkey_madness",
            name = "Wild Isle Infiltration",
            iconEmoji = "🐒",
            difficulty = QuestDifficulty.MASTER,
            recCombatLevel = 70,
            reqSkill = OsrsSkill.AGILITY,
            reqSkillLevel = 50,
            reqQuestIds = listOf("quest_the_grand_tree", "quest_tree_gnome_village"),
            description = "Infiltrate Primate Isle using a spirit disguise amulet to stop the invading armada!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_gold", "Gold Bar", "🪙", 2),
                QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 3),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1),
                QuestRequirementItem("item_badge_boulder", "Boulder Totem", "🪨", 1)
            ),
            questPoints = 5,
            rewardXpMap = mapOf(
                OsrsSkill.ATTACK to 35000L,
                OsrsSkill.AGILITY to 10000L
            ),
            rewardGp = 25000L,
            rewardItemName = "Ape Spirit Shaman Totem",
            rewardItemEmoji = "🪬",
            rewardItemId = "item_monkey_greegree",
            unlockedFeatures = listOf("🔓 In-Game Feature: Equip Dragon Scimitar")
        ),
        OsrsQuest(
            id = "quest_desert_treasure",
            name = "Pyramid of the Four Elements",
            iconEmoji = "🏛️",
            difficulty = QuestDifficulty.MASTER,
            recCombatLevel = 80,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 60,
            reqQuestIds = listOf("quest_temple_of_ikov", "quest_priest_in_peril"),
            description = "Uncover ancient elemental crystals inside the Sunken Desert Pyramid to unlock Ancient Elemental Sorcery!",
            requiredItems = listOf(
                QuestRequirementItem("item_magic_logs", "Magic Catalyst", "🪵", 2),
                QuestRequirementItem("item_bar_gold", "Gold Offering", "🪙", 2),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2),
                QuestRequirementItem("item_badge_cascade", "Cascade Totem", "💧", 1)
            ),
            questPoints = 3,
            rewardXpMap = mapOf(
                OsrsSkill.MAGIC to 20000L,
                OsrsSkill.RUNECRAFT to 15000L,
                OsrsSkill.MAGIC to 10000L
            ),
            rewardGp = 50000L,
            rewardItemName = "Ancient Elemental Staff",
            rewardItemEmoji = "📖",
            rewardItemId = "item_ancient_staff",
            unlockedFeatures = listOf("📜 Spellbook Unlock: Ancient Magicks (Lore / World Feature)")
        ),
        OsrsQuest(
            id = "quest_sins_of_the_father",
            name = "Rebellion in the Shadow Realm",
            iconEmoji = "🦇",
            difficulty = QuestDifficulty.MASTER,
            recCombatLevel = 85,
            reqSkill = OsrsSkill.SLAYER,
            reqSkillLevel = 65,
            reqQuestIds = listOf("quest_nature_spirit"),
            description = "Join the underground resistance in Darkmeyer Citadel and slay the Shadow High Sovereign!",
            requiredItems = listOf(
                QuestRequirementItem("item_yew_logs", "Yew Logs", "🪵", 3),
                QuestRequirementItem("item_bar_adamant", "Adamant Bar", "🛡️", 2),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2),
                QuestRequirementItem("item_badge_soul", "Soul Totem", "🟣", 1)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.WOODCUTTING to 15000L,
                OsrsSkill.FLETCHING to 15000L,
                OsrsSkill.FLETCHING to 15000L,
                OsrsSkill.AGILITY to 15000L
            ),
            rewardGp = 60000L,
            rewardItemName = "Blisterwood Morningstar",
            rewardItemEmoji = "🏏",
            rewardItemId = "item_blisterwood_flail",
            unlockedFeatures = listOf("🗺️ Region Unlock: Darkmeyer Citadel Free Passage")
        ),
        OsrsQuest(
            id = "quest_a_kingdom_divided",
            name = "Uniting the Realm Houses",
            iconEmoji = "👑",
            difficulty = QuestDifficulty.MASTER,
            recCombatLevel = 85,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 65,
            description = "Expose corruption among council lords and unite the rival houses of the realm under one banner!",
            requiredItems = listOf(
                QuestRequirementItem("item_magic_logs", "Magic Logs", "🪵", 2),
                QuestRequirementItem("item_bar_adamant", "Adamant Bar", "🛡️", 2),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2),
                QuestRequirementItem("item_badge_marsh", "Marsh Totem", "🟡", 1)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.AGILITY to 10000L,
                OsrsSkill.FLETCHING to 10000L,
                OsrsSkill.SMITHING to 10000L,
                OsrsSkill.HERBLORE to 10000L
            ),
            rewardGp = 75000L,
            rewardItemName = "Arceuus Soul Sorcery Expansion",
            rewardItemEmoji = "🔮",
            rewardItemId = null,
            unlockedFeatures = listOf("📜 Spellbook Unlock: Arceuus Magicks (Lore / World Feature)")
        ),
        OsrsQuest(
            id = "quest_monkey_madness_2",
            name = "Siege of the Primate Fleet",
            iconEmoji = "🦍",
            difficulty = QuestDifficulty.GRANDMASTER,
            recCombatLevel = 95,
            reqSkill = OsrsSkill.SLAYER,
            reqSkillLevel = 75,
            reqQuestIds = listOf("quest_monkey_madness"),
            description = "Thwart the invading airship fleet and defeat mutated primal behemoths at Lithkren Fortress!",
            requiredItems = listOf(
                QuestRequirementItem("item_shark", "Cooked Shark", "🦈", 3),
                QuestRequirementItem("item_bar_rune", "Rune Bar", "⚔️", 2),
                QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 2),
                QuestRequirementItem("item_badge_thunder", "Thunder Totem", "⚡", 1)
            ),
            questPoints = 4,
            rewardXpMap = mapOf(
                OsrsSkill.AGILITY to 25000L,
                OsrsSkill.HUNTER to 25000L,
                OsrsSkill.SLAYER to 25000L,
                OsrsSkill.THIEVING to 25000L
            ),
            rewardGp = 100000L,
            rewardItemName = "Heavy Ballista Siege Engine",
            rewardItemEmoji = "🏹",
            rewardItemId = null,
            unlockedFeatures = listOf(
                "🔓 In-Game Feature: Equip Heavy Ballista Ranged Weapon",
                "🌾 Permanent +25% Chance for Double Crop Harvest & Barn Animal Drops"
            )
        ),
        OsrsQuest(
            id = "quest_dragon_slayer_2",
            name = "Apex Wyvern Sovereign Trial",
            iconEmoji = "🐉",
            difficulty = QuestDifficulty.GRANDMASTER,
            recCombatLevel = 100,
            reqSkill = OsrsSkill.SMITHING,
            reqSkillLevel = 75,
            reqQuestIds = listOf("quest_dragon_slayer_1", "quest_fremennik_trials"),
            description = "Reclaim the Ancient Dragon Fortress and defeat the colossal Apex Wyvern Sovereign!",
            requiredItems = listOf(
                QuestRequirementItem("item_dragon_bones", "Dragon Bones", "🦴", 3),
                QuestRequirementItem("item_bar_rune", "Rune Bar", "⚔️", 2),
                QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 2),
                QuestRequirementItem("item_badge_volcano", "Volcano Totem", "🔥", 1)
            ),
            questPoints = 5,
            rewardXpMap = mapOf(
                OsrsSkill.SMITHING to 25000L,
                OsrsSkill.AGILITY to 25000L,
                OsrsSkill.THIEVING to 25000L,
                OsrsSkill.SLAYER to 25000L
            ),
            rewardGp = 15000L,
            rewardItemName = "Myths' High Sanctuary Access",
            rewardItemEmoji = "🧥",
            rewardItemId = null,
            unlockedFeatures = listOf("🗺️ Region Unlock: Myths' High Sanctuary Access")
        ),
        OsrsQuest(
            id = "quest_song_of_the_elves",
            name = "Reclaiming the Crystal City",
            iconEmoji = "🧝",
            difficulty = QuestDifficulty.GRANDMASTER,
            recCombatLevel = 100,
            reqSkill = OsrsSkill.WOODCUTTING,
            reqSkillLevel = 75,
            reqQuestIds = listOf("quest_roving_elves"),
            description = "Reclaim the glorious Crystal City from dark corruption and awaken the Crystal Worldtree!",
            requiredItems = listOf(
                QuestRequirementItem("item_magic_logs", "Elven Magic Wood", "🪵", 4),
                QuestRequirementItem("item_bar_rune", "Rune Bar", "⚔️", 2),
                QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 2),
                QuestRequirementItem("item_badge_rainbow", "Rainbow Totem", "🌈", 1)
            ),
            questPoints = 4,
            rewardXpMap = mapOf(
                OsrsSkill.WOODCUTTING to 80000L,
                OsrsSkill.AGILITY to 80000L,
                OsrsSkill.CONSTRUCTION to 40000L
            ),
            rewardGp = 200000L,
            rewardItemName = "Prifddinas Crystal Sanctuary Seal",
            rewardItemEmoji = "💎",
            rewardItemId = null,
            unlockedFeatures = listOf("🗺️ Region Unlock: Prifddinas Crystal City Access (Lore / Map Region)")
        ),
        OsrsQuest(
            id = "quest_desert_treasure_2",
            name = "Vaults of the Fallen Empire",
            iconEmoji = "🏜️",
            difficulty = QuestDifficulty.GRANDMASTER,
            recCombatLevel = 105,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 80,
            reqQuestIds = listOf("quest_desert_treasure"),
            description = "Uncover ancient fallen empire vaults and defeat the four elemental spirit titans!",
            requiredItems = listOf(
                QuestRequirementItem("item_magic_logs", "Magic Wood", "🪵", 3),
                QuestRequirementItem("item_bar_rune", "Rune Bar", "⚔️", 2),
                QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 2),
                QuestRequirementItem("item_badge_earth", "Earth Totem", "🌍", 1)
            ),
            questPoints = 5,
            rewardXpMap = mapOf(
                OsrsSkill.MAGIC to 100000L,
                OsrsSkill.THIEVING to 100000L,
                OsrsSkill.AGILITY to 100000L,
                OsrsSkill.MAGIC to 100000L
            ),
            rewardGp = 250000L,
            rewardItemName = "Ancient Elemental Sovereign Relic",
            rewardItemEmoji = "👑",
            rewardItemId = "item_ancient_relic"
        ),
        OsrsQuest(
            id = "quest_recipe_for_disaster",
            name = "Grand Banquet of the High Realms",
            iconEmoji = "👑",
            difficulty = QuestDifficulty.GRANDMASTER,
            recCombatLevel = 90,
            reqSkill = OsrsSkill.COOKING,
            reqSkillLevel = 70,
            reqQuestIds = listOf("quest_cooks_assistant"),
            description = "Defeat the Corrupted Culinaromancer and save the High Realm Council in the ultimate culinary battle!",
            requiredItems = listOf(
                QuestRequirementItem("item_shark", "Feast Shark", "🦈", 4),
                QuestRequirementItem("item_bar_adamant", "Adamant Platter", "🛡️", 2),
                QuestRequirementItem("item_bar_gold", "Royal Gold", "🪙", 2),
                QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 1)
            ),
            questPoints = 10,
            rewardXpMap = mapOf(
                OsrsSkill.COOKING to 25000L,
                OsrsSkill.ATTACK to 20000L,
                OsrsSkill.DEFENCE to 20000L
            ),
            rewardGp = 100000L,
            rewardItemName = "Sovereign Champion Barrows Gauntlets",
            rewardItemEmoji = "🥊",
            rewardItemId = "item_barrows_gloves"
        ),

        // Kanto Shaman Trials, High Spirits & Spirit God
        OsrsQuest(
            id = "quest_kanto_gym_1_brock",
            name = "Forest Realm Trial #1: Earth Bear Spirit",
            iconEmoji = "🪨",
            difficulty = QuestDifficulty.INTERMEDIATE,
            recCombatLevel = 15,
            reqSkill = OsrsSkill.ATTACK,
            reqSkillLevel = 15,
            description = "Challenge Earth Bear Spirit and its stone guardians at Granite Peak Shrine to earn the Boulder Totem!",
            requiredItems = listOf(
                QuestRequirementItem("item_ore_iron", "Iron Ore", "🪨", 2),
                QuestRequirementItem("item_trout", "Cooked Trout", "🐟", 2)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.ATTACK to 1500L,
                OsrsSkill.DEFENCE to 2000L
            ),
            rewardGp = 3000L,
            rewardItemName = "Boulder Totem",
            rewardItemEmoji = "🪨",
            rewardItemId = "item_badge_boulder"
        ),
        OsrsQuest(
            id = "quest_kanto_gym_2_misty",
            name = "Forest Realm Trial #2: River Serpent Spirit",
            iconEmoji = "💧",
            difficulty = QuestDifficulty.INTERMEDIATE,
            recCombatLevel = 25,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 25,
            reqQuestIds = listOf("quest_kanto_gym_1_brock"),
            description = "Defeat River Serpent Spirit at Cascade Waters Shrine to earn the Cascade Totem!",
            requiredItems = listOf(
                QuestRequirementItem("item_raw_trout", "Raw Trout", "🐟", 2),
                QuestRequirementItem("item_bar_bronze", "Bronze Bar", "🧱", 2)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.MAGIC to 2500L,
                OsrsSkill.HITPOINTS to 2000L
            ),
            rewardGp = 5000L,
            rewardItemName = "Cascade Totem",
            rewardItemEmoji = "💧",
            rewardItemId = "item_badge_cascade"
        ),
        OsrsQuest(
            id = "quest_kanto_gym_3_lt_surge",
            name = "Forest Realm Trial #3: Storm Hawk Spirit",
            iconEmoji = "⚡",
            difficulty = QuestDifficulty.INTERMEDIATE,
            recCombatLevel = 35,
            reqSkill = OsrsSkill.RANGED,
            reqSkillLevel = 35,
            reqQuestIds = listOf("quest_kanto_gym_2_misty"),
            description = "Infiltrate Storm Harbor Shrine and defeat Storm Hawk Spirit to claim the Thunder Totem!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2),
                QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.RANGED to 3500L,
                OsrsSkill.ATTACK to 3000L
            ),
            rewardGp = 8000L,
            rewardItemName = "Thunder Totem",
            rewardItemEmoji = "⚡",
            rewardItemId = "item_badge_thunder"
        ),
        OsrsQuest(
            id = "quest_kanto_gym_4_erika",
            name = "Forest Realm Trial #4: Floral Vine Spirit",
            iconEmoji = "🌈",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 45,
            reqSkill = OsrsSkill.HERBLORE,
            reqSkillLevel = 45,
            reqQuestIds = listOf("quest_kanto_gym_3_lt_surge"),
            description = "Battle Floral Vine Spirit at Emerald Grove Shrine to earn the Rainbow Totem!",
            requiredItems = listOf(
                QuestRequirementItem("item_maple_logs", "Maple Logs", "🪵", 2),
                QuestRequirementItem("item_salmon", "Cooked Salmon", "🐟", 2),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.HERBLORE to 5000L,
                OsrsSkill.FARMING to 5000L
            ),
            rewardGp = 12000L,
            rewardItemName = "Rainbow Totem",
            rewardItemEmoji = "🌈",
            rewardItemId = "item_badge_rainbow"
        ),
        OsrsQuest(
            id = "quest_kanto_gym_5_koga",
            name = "Forest Realm Trial #5: Shadow Wolf Spirit",
            iconEmoji = "🟣",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 55,
            reqSkill = OsrsSkill.THIEVING,
            reqSkillLevel = 55,
            reqQuestIds = listOf("quest_kanto_gym_4_erika"),
            description = "Navigate Venom Marsh Shrine's maze and overcome Shadow Wolf Spirit to earn the Soul Totem!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2),
                QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 2),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.THIEVING to 6000L,
                OsrsSkill.SLAYER to 6000L
            ),
            rewardGp = 16000L,
            rewardItemName = "Soul Totem",
            rewardItemEmoji = "🟣",
            rewardItemId = "item_badge_soul"
        ),
        OsrsQuest(
            id = "quest_kanto_gym_6_sabrina",
            name = "Forest Realm Trial #6: Mind Owl Spirit",
            iconEmoji = "🟡",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 65,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 65,
            reqQuestIds = listOf("quest_kanto_gym_5_koga"),
            description = "Challenge Mind Owl Spirit at Astral Spire Shrine to earn the Marsh Totem!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2),
                QuestRequirementItem("item_yew_logs", "Yew Logs", "🪵", 2),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.MAGIC to 10000L,
                OsrsSkill.MAGIC to 8000L
            ),
            rewardGp = 20000L,
            rewardItemName = "Marsh Totem",
            rewardItemEmoji = "🟡",
            rewardItemId = "item_badge_marsh"
        ),
        OsrsQuest(
            id = "quest_kanto_gym_7_blaine",
            name = "Forest Realm Trial #7: Magma Drake Spirit",
            iconEmoji = "🔥",
            difficulty = QuestDifficulty.EXPERIENCED,
            recCombatLevel = 75,
            reqSkill = OsrsSkill.FIREMAKING,
            reqSkillLevel = 70,
            reqQuestIds = listOf("quest_kanto_gym_6_sabrina"),
            description = "Withstand the fiery heat of Magma Drake Spirit at Ember Isle Shrine to earn the Volcano Totem!",
            requiredItems = listOf(
                QuestRequirementItem("item_yew_logs", "Yew Logs", "🪵", 3),
                QuestRequirementItem("item_swordfish", "Cooked Swordfish", "🐟", 2),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.FIREMAKING to 15000L,
                OsrsSkill.ATTACK to 12000L
            ),
            rewardGp = 25000L,
            rewardItemName = "Volcano Totem",
            rewardItemEmoji = "🔥",
            rewardItemId = "item_badge_volcano"
        ),
        OsrsQuest(
            id = "quest_kanto_gym_8_giovanni",
            name = "Forest Realm Trial #8: Earth Titan Spirit",
            iconEmoji = "🌍",
            difficulty = QuestDifficulty.MASTER,
            recCombatLevel = 85,
            reqSkill = OsrsSkill.ATTACK,
            reqSkillLevel = 75,
            reqQuestIds = listOf("quest_kanto_gym_7_blaine"),
            description = "Confront Earth Titan Spirit at Verdant Shrine to earn the final Forest Realm totem: Earth Totem!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_adamant", "Adamant Bar", "🛡️", 2),
                QuestRequirementItem("item_shark", "Cooked Shark", "🦈", 2),
                QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1),
                QuestRequirementItem("item_badge_boulder", "Boulder Totem", "🪨", 1)
            ),
            questPoints = 3,
            rewardXpMap = mapOf(
                OsrsSkill.ATTACK to 20000L,
                OsrsSkill.DEFENCE to 20000L
            ),
            rewardGp = 40000L,
            rewardItemName = "Earth Totem",
            rewardItemEmoji = "🌍",
            rewardItemId = "item_badge_earth"
        ),
        OsrsQuest(
            id = "quest_kanto_e4_1_lorelei",
            name = "High Spirits Trial #1: Frostbite Phoenix",
            iconEmoji = "🧊",
            difficulty = QuestDifficulty.MASTER,
            recCombatLevel = 90,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 80,
            reqQuestIds = listOf("quest_kanto_gym_8_giovanni"),
            description = "Enter Celestial Summit and overcome the freezing ice blizzards of the Frostbite Phoenix!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_adamant", "Adamant Bar", "🛡️", 2),
                QuestRequirementItem("item_shark", "Cooked Shark", "🦈", 2),
                QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 1),
                QuestRequirementItem("item_badge_cascade", "Cascade Totem", "💧", 1)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.MAGIC to 25000L,
                OsrsSkill.HITPOINTS to 20000L
            ),
            rewardGp = 50000L,
            rewardItemName = "Glacial Crest",
            rewardItemEmoji = "🧊",
            rewardItemId = "item_dragon_bones"
        ),
        OsrsQuest(
            id = "quest_kanto_e4_2_bruno",
            name = "High Spirits Trial #2: Ironheart Behemoth",
            iconEmoji = "🥋",
            difficulty = QuestDifficulty.MASTER,
            recCombatLevel = 92,
            reqSkill = OsrsSkill.ATTACK,
            reqSkillLevel = 82,
            reqQuestIds = listOf("quest_kanto_e4_1_lorelei"),
            description = "Withstand martial strikes and earth shatterings from the High Spirits Ironheart Behemoth!",
            requiredItems = listOf(
                QuestRequirementItem("item_bar_adamant", "Adamant Bar", "🛡️", 2),
                QuestRequirementItem("item_shark", "Cooked Shark", "🦈", 2),
                QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 1),
                QuestRequirementItem("item_badge_thunder", "Thunder Totem", "⚡", 1)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.ATTACK to 30000L
            ),
            rewardGp = 60000L,
            rewardItemName = "Black Belt Token",
            rewardItemEmoji = "🥋",
            rewardItemId = "item_bar_adamant"
        ),
        OsrsQuest(
            id = "quest_kanto_e4_3_agatha",
            name = "High Spirits Trial #3: Nether Specter Queen",
            iconEmoji = "👻",
            difficulty = QuestDifficulty.MASTER,
            recCombatLevel = 95,
            reqSkill = OsrsSkill.MAGIC,
            reqSkillLevel = 85,
            reqQuestIds = listOf("quest_kanto_e4_2_bruno"),
            description = "Confront the ghostly specters and shadow magic of Nether Specter Queen!",
            requiredItems = listOf(
                QuestRequirementItem("item_dragon_bones", "Dragon Bones", "🦴", 2),
                QuestRequirementItem("item_magic_logs", "Magic Logs", "🪵", 2),
                QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 1),
                QuestRequirementItem("item_badge_soul", "Soul Totem", "🟣", 1)
            ),
            questPoints = 2,
            rewardXpMap = mapOf(
                OsrsSkill.MAGIC to 35000L,
                OsrsSkill.THIEVING to 25000L
            ),
            rewardGp = 70000L,
            rewardItemName = "Phantom Essence",
            rewardItemEmoji = "👻",
            rewardItemId = "item_magic_logs"
        ),
        OsrsQuest(
            id = "quest_kanto_e4_4_lance",
            name = "High Spirits Trial #4: Celestial Wyvern",
            iconEmoji = "🐉",
            difficulty = QuestDifficulty.GRANDMASTER,
            recCombatLevel = 100,
            reqSkill = OsrsSkill.SLAYER,
            reqSkillLevel = 88,
            reqQuestIds = listOf("quest_kanto_e4_3_agatha"),
            description = "Confront High Spirits Celestial Wyvern and his hyper-beam dragon spirits!",
            requiredItems = listOf(
                QuestRequirementItem("item_dragon_bones", "Dragon Bones", "🦴", 3),
                QuestRequirementItem("item_bar_rune", "Rune Bar", "⚔️", 2),
                QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 2),
                QuestRequirementItem("item_badge_rainbow", "Rainbow Totem", "🌈", 1)
            ),
            questPoints = 3,
            rewardXpMap = mapOf(
                OsrsSkill.SLAYER to 50000L,
                OsrsSkill.ATTACK to 40000L,
                OsrsSkill.DEFENCE to 40000L
            ),
            rewardGp = 100000L,
            rewardItemName = "Dragon Master Crest",
            rewardItemEmoji = "🐉",
            rewardItemId = "item_rune_bar"
        ),
        OsrsQuest(
            id = "quest_kanto_champion_blue",
            name = "Forest Realm Champion Battle",
            iconEmoji = "🏆",
            difficulty = QuestDifficulty.GRANDMASTER,
            recCombatLevel = 105,
            reqSkill = OsrsSkill.ATTACK,
            reqSkillLevel = 90,
            reqQuestIds = listOf("quest_kanto_e4_4_lance"),
            description = "Defeat the Forest Realm Champion in the ultimate showdown to claim the League Totem!",
            requiredItems = listOf(
                QuestRequirementItem("item_manta_ray", "Manta Ray", "🐟", 3),
                QuestRequirementItem("item_bar_rune", "Rune Bar", "⚔️", 2),
                QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 2),
                QuestRequirementItem("item_badge_earth", "Earth Totem", "🌍", 1)
            ),
            questPoints = 5,
            rewardXpMap = mapOf(
                OsrsSkill.ATTACK to 80000L,
                OsrsSkill.DEFENCE to 80000L,
                OsrsSkill.MAGIC to 80000L,
                OsrsSkill.RANGED to 80000L
            ),
            rewardGp = 250000L,
            rewardItemName = "League Totem",
            rewardItemEmoji = "👑",
            rewardItemId = "item_badge_league"
        ),

        // === 100+ OFFICIAL HIGH REALM QUESTS ===
        OsrsQuest("quest_black_knights_fortress_part2", "Shadow Citadel Sabotage - Part 2", "🏰", QuestDifficulty.NOVICE, 15, OsrsSkill.ATTACK, 10, emptyList(), "Infiltrate the Shadow Citadel and ruin their secret invincibility elixir!", emptyList(), 3, mapOf(OsrsSkill.ATTACK to 2500L), 2500L, "Shadow Knight Armor", "🛡️", "item_iron_bar"),
        OsrsQuest("quest_dorics_quest_part2", "Granite Smith's Favor - Part 2", "⛏️", QuestDifficulty.NOVICE, 1, OsrsSkill.SMITHING, 15, emptyList(), "Mine copper, tin, and iron ore for High Dwarf Thurgo the blacksmith!", emptyList(), 1, mapOf(OsrsSkill.SMITHING to 1300L), 1000L, "Dwarf Smith Amulet", "📿", "item_copper_ore"),
        OsrsQuest("quest_goblin_diplomacy_part2", "Wildland Chieftain Reconciliation - Part 2", "👺", QuestDifficulty.NOVICE, 5, OsrsSkill.FLETCHING, 5, emptyList(), "Resolve the feud between rival Wildland Chiefs over ritual armor color!", emptyList(), 5, mapOf(OsrsSkill.FLETCHING to 800L), 800L, "Chieftain Armor", "🥋", "item_gold_bar"),
        OsrsQuest("quest_sheep_shearer", "Highland Fleece Harvest", "🐑", QuestDifficulty.NOVICE, 1, OsrsSkill.FLETCHING, 1, emptyList(), "Gather 20 balls of sacred wool for Highland Herder in Whispering Glade!", listOf(QuestRequirementItem("item_logs", "Logs", "🪵", 2)), 1, mapOf(OsrsSkill.FLETCHING to 500L), 500L, "Ball of Wool", "🧶", "item_coins_100"),
        OsrsQuest("quest_witchs_potion_part2", "High Witch's Cauldron - Part 2", "🧙‍♀️", QuestDifficulty.NOVICE, 5, OsrsSkill.MAGIC, 1, emptyList(), "Help High Witch Hetty brew a mystical potion to increase your spiritual power!", listOf(QuestRequirementItem("item_bones", "Bones", "🦴", 2)), 1, mapOf(OsrsSkill.MAGIC to 325L), 500L, "Magic Elixir Brew", "🧪", "item_bread"),
        OsrsQuest("quest_knights_sword_part2", "Forging the Sentinel Blade - Part 2", "⚔️", QuestDifficulty.INTERMEDIATE, 10, OsrsSkill.SMITHING, 15, emptyList(), "Assist Sentinel Smith in Royal Citadel to forge a duplicate Crystal Blade with High Dwarf Thurgo!", listOf(QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2), QuestRequirementItem("item_ore_iron", "Iron Ore", "🪨", 2)), 1, mapOf(OsrsSkill.SMITHING to 12725L), 3000L, "Crystal Blade", "🗡️", "item_iron_bar"),
        OsrsQuest("quest_pirates_treasure_part2", "Sea Rover's Hidden Stash - Part 2", "🏴‍☠️", QuestDifficulty.NOVICE, 10, OsrsSkill.THIEVING, 5, emptyList(), "Smuggle Sea Rover Brew past customs to find Captain Frank's buried treasure!", listOf(QuestRequirementItem("item_raw_shrimps", "Raw Shrimps", "🦐", 3)), 2, mapOf(OsrsSkill.THIEVING to 1000L), 2000L, "Sea Rover Treasure Chest", "🪙", "item_coins_100"),
        OsrsQuest("quest_below_ice_mountain_part2", "Glacial Ruins Expedition - Part 2", "🏔️", QuestDifficulty.NOVICE, 15, OsrsSkill.SMITHING, 16, emptyList(), "Investigate the mysterious ruins under Glacial Ridge with Scholar Willow!", listOf(QuestRequirementItem("item_ore_copper", "Copper Ore", "🪨", 2), QuestRequirementItem("item_ore_tin", "Tin Ore", "🪨", 2)), 1, mapOf(OsrsSkill.SMITHING to 2000L), 1500L, "Barronite Shard", "💎", "item_iron_ore"),
        OsrsQuest("quest_corsair_curse", "The Smuggler's Curse", "⛵", QuestDifficulty.NOVICE, 20, OsrsSkill.FISHING, 10, emptyList(), "Unravel the curse plaguing Captain Titch's crew in Smuggler's Cove!", listOf(QuestRequirementItem("item_raw_trout", "Raw Trout", "🐟", 2)), 2, mapOf(OsrsSkill.FISHING to 1500L), 2000L, "Sea Rover Medallion", "🏅", "item_raw_trout"),
        OsrsQuest("quest_x_marks_the_spot", "Secret Treasure Scroll", "🗺️", QuestDifficulty.NOVICE, 1, OsrsSkill.WOODCUTTING, 1, emptyList(), "Follow Navigator Veos's map across Whispering Valley!", listOf(QuestRequirementItem("item_logs", "Logs", "🪵", 2)), 1, mapOf(OsrsSkill.WOODCUTTING to 300L), 500L, "Antique Essence Lamp", "🪔", "item_oak_logs"),
        OsrsQuest("quest_misthalin_mystery", "Mystic Isle Mystery", "🕯️", QuestDifficulty.NOVICE, 1, OsrsSkill.THIEVING, 1, emptyList(), "Solve the ancient mystery on the isolated mansion island!", listOf(QuestRequirementItem("item_bones", "Bones", "🦴", 2)), 1, mapOf(OsrsSkill.FLETCHING to 600L), 1000L, "Cultist Robes", "🥋", "item_bread"),
        
        // Members Quests
        OsrsQuest("quest_animal_magnetism_part2", "Spectral Device Engineering - Part 2", "🧲", QuestDifficulty.INTERMEDIATE, 20, OsrsSkill.SLAYER, 18, listOf("quest_restless_ghost"), "Construct a spirit magnet accumulator device to automatically retrieve fired arrows!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2), QuestRequirementItem("item_bones", "Bones", "🦴", 3)), 2, mapOf(OsrsSkill.SLAYER to 2500L, OsrsSkill.FLETCHING to 2500L), 3000L, "Spirit Magnet Accumulator", "🎒", "item_arrowtip"),
        OsrsQuest("quest_big_chompy", "Giant Beast Hunt", "🍗", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.HUNTER, 15, emptyList(), "Hunt Giant Chompies in Wilderness Ridge with Rantz the Ogre Shaman!", listOf(QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2), QuestRequirementItem("item_feathers", "Feathers", "🪶", 5)), 2, mapOf(OsrsSkill.HUNTER to 2000L, OsrsSkill.COOKING to 2000L), 2500L, "Ogre Bow & Bellows", "🏹", "item_raw_bird_meat"),
        OsrsQuest("quest_biohazard", "Shadow Outpost Infiltration", "☣️", QuestDifficulty.NOVICE, 15, OsrsSkill.THIEVING, 10, listOf("quest_plague_city"), "Infiltrate Shadow Cult headquarters in Valley Outpost to expose the plague lie!", listOf(QuestRequirementItem("item_raw_trout", "Raw Trout", "🐟", 2)), 3, mapOf(OsrsSkill.THIEVING to 1250L), 2000L, "Primsat Vial", "🧪", "item_bread"),
        OsrsQuest("quest_plague_city", "Quarantine Zone Rescue", "🏙️", QuestDifficulty.NOVICE, 1, OsrsSkill.SMITHING, 1, emptyList(), "Enter quarantine zone Valley Outpost to rescue Elena!", listOf(QuestRequirementItem("item_logs", "Logs", "🪵", 2)), 1, mapOf(OsrsSkill.SMITHING to 2425L), 1000L, "Gas Mask", "🎭", "item_bread"),
        OsrsQuest("quest_cabin_fever", "High Seas Hijack", "🏴‍☠️", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.SAILING, 20, emptyList(), "Pillage enemy ships on the high seas with Cap'n Izzy Sea Rover!", listOf(QuestRequirementItem("item_oak_plank", "Oak Planks", "🪵", 3), QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)), 2, mapOf(OsrsSkill.SAILING to 7000L, OsrsSkill.FLETCHING to 5000L), 5000L, "Pirate Tricorn Hat", "🎩", "item_magic_logs"),
        OsrsQuest("quest_clock_tower", "Highland Clock Tower", "🕰️", QuestDifficulty.NOVICE, 10, OsrsSkill.FLETCHING, 5, emptyList(), "Retrieve missing cogs to repair the ancient Highland Clock Tower!", listOf(QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2)), 1, mapOf(OsrsSkill.FLETCHING to 500L), 1500L, "Clock Cog", "⚙️", "item_steel_bar"),
        OsrsQuest("quest_cold_war", "Glacial Infiltration", "🐧", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.CONSTRUCTION, 34, emptyList(), "Infiltrate the Frost Agent Base disguised in a spirit disguise suit!", listOf(QuestRequirementItem("item_oak_plank", "Oak Planks", "🪵", 3), QuestRequirementItem("item_raw_salmon", "Raw Salmon", "🐟", 2)), 1, mapOf(OsrsSkill.CONSTRUCTION to 5000L, OsrsSkill.AGILITY to 5000L), 4000L, "Frost Agent Suit", "🐧", "item_oak_plank"),
        OsrsQuest("quest_contact", "Sunken Citadel Alliance", "🏜️", QuestDifficulty.MASTER, 60, OsrsSkill.SLAYER, 50, emptyList(), "Delve beneath Sunken Citadel into the scarab dungeon to contact Oasis City!", listOf(QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2), QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 3), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)), 1, mapOf(OsrsSkill.SLAYER to 7000L, OsrsSkill.ATTACK to 7000L), 10000L, "Keris Dagger", "🗡️", "item_dragon_bones"),
        OsrsQuest("quest_creature_fenkenstrain", "Alchemist's Experiment", "🧟", QuestDifficulty.INTERMEDIATE, 25, OsrsSkill.THIEVING, 25, emptyList(), "Help Lord Fenkenstrain reanimate a spirit monster at Spire Citadel!", listOf(QuestRequirementItem("item_bar_silver", "Silver Bar", "🥈", 2), QuestRequirementItem("item_big_bones", "Big Bones", "🦴", 2)), 2, mapOf(OsrsSkill.THIEVING to 1000L), 3000L, "Ring of Charos", "💍", "item_gold_bar"),
        OsrsQuest("quest_darkness_hallowvale", "Shadow Realm Resistance", "🧛", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.CONSTRUCTION, 25, emptyList(), "Infiltrate Shadow Realm Stronghold and aid the Spirit Resistance!", listOf(QuestRequirementItem("item_oak_plank", "Oak Planks", "🪵", 3), QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)), 2, mapOf(OsrsSkill.CONSTRUCTION to 7000L, OsrsSkill.AGILITY to 6000L), 6000L, "Tome of Experience", "📚", "item_oak_logs"),
        OsrsQuest("quest_death_plateau", "Scouting Glacial Plateau", "⛰️", QuestDifficulty.NOVICE, 20, OsrsSkill.AGILITY, 15, emptyList(), "Scout the secret route up Glacial Plateau for High Sentinel Guard!", listOf(QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2), QuestRequirementItem("item_trout", "Cooked Trout", "🐟", 2)), 1, mapOf(OsrsSkill.ATTACK to 3000L), 3000L, "Climbing Boots", "👢", "item_bread"),
        OsrsQuest("quest_desert_treasure_1_part2", "Pyramid of Elements - Part 2", "💎", QuestDifficulty.MASTER, 75, OsrsSkill.MAGIC, 55, emptyList(), "Uncover ancient Elemental Magicks by gathering four elemental crystals across High Realms!", listOf(QuestRequirementItem("item_magic_logs", "Magic Logs", "🪵", 2), QuestRequirementItem("item_bar_gold", "Gold Bar", "🪙", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)), 3, mapOf(OsrsSkill.MAGIC to 20000L), 20000L, "Ancient Staff", "🪄", "item_rune_bar"),
        OsrsQuest("quest_desert_treasure_2_part2", "Vaults of Fallen Sovereign - Part 2", "👑", QuestDifficulty.GRANDMASTER, 100, OsrsSkill.SLAYER, 75, listOf("quest_desert_treasure_1"), "Infiltrate Elemental Sovereign vaults and battle Tide Leviathan, Whisperer, Shadow Titan, and Flame Monarch!", listOf(QuestRequirementItem("item_magic_logs", "Magic Logs", "🪵", 3), QuestRequirementItem("item_bar_rune", "Rune Bar", "⚔️", 2), QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 2)), 5, mapOf(OsrsSkill.SLAYER to 50000L, OsrsSkill.MAGIC to 50000L), 100000L, "Arcane Sovereign Robe Top", "👘", "item_dragon_bones"),
        OsrsQuest("quest_dig_site", "Highland Archaeological Excavation", "🏛️", QuestDifficulty.INTERMEDIATE, 25, OsrsSkill.HERBLORE, 20, emptyList(), "Earn your graduate digger certificate at the Highland Archaeological Digsite!", listOf(QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2), QuestRequirementItem("item_big_bones", "Big Bones", "🦴", 1)), 2, mapOf(OsrsSkill.SMITHING to 15300L, OsrsSkill.HERBLORE to 2000L), 4000L, "Unidentified Liquid", "🧪", "item_iron_ore"),
        OsrsQuest("quest_dragon_slayer_2_part2", "Apex Wyvern Sovereign Trial - Part 2", "🐲", QuestDifficulty.GRANDMASTER, 100, OsrsSkill.SMITHING, 75, listOf("quest_dragon_slayer_1"), "Forge the mythical Dragonkin key and slay Apex Wyvern atop the Volcanic Peak!", listOf(QuestRequirementItem("item_dragon_bones", "Dragon Bones", "🦴", 3), QuestRequirementItem("item_bar_rune", "Rune Bar", "⚔️", 2), QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 2)), 5, mapOf(OsrsSkill.SMITHING to 25000L, OsrsSkill.SLAYER to 25000L, OsrsSkill.SMITHING to 25000L), 150000L, "Mythical Cape", "🧥", "item_dragon_bones"),
        OsrsQuest("quest_druidic_ritual_part2", "Grove Purification Ritual - Part 2", "🌿", QuestDifficulty.NOVICE, 1, OsrsSkill.HERBLORE, 1, emptyList(), "Purify the Sacred Stone Circle to unlock the Herblore skill!", listOf(QuestRequirementItem("item_raw_beef", "Raw Beef", "🥩", 2)), 4, mapOf(OsrsSkill.HERBLORE to 250L), 1000L, "Herblore Starter Kit", "🌿", "item_bread"),
        OsrsQuest("quest_dwarf_cannon", "Dwarven Multicannon Repair", "💣", QuestDifficulty.NOVICE, 20, OsrsSkill.FLETCHING, 10, emptyList(), "Repair Captain Lawgof's dwarven multicannon at the High Guard Camp!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)), 2, mapOf(OsrsSkill.FLETCHING to 750L), 3000L, "Multicannon Mold", "💣", "item_steel_bar"),
        OsrsQuest("quest_eadgars_ruse", "Glacial Titan Infiltration", "🐸", QuestDifficulty.INTERMEDIATE, 35, OsrsSkill.HERBLORE, 31, emptyList(), "Disguise yourself as a spirit animal to infiltrate Glacial Titan Stronghold!", listOf(QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)), 1, mapOf(OsrsSkill.HERBLORE to 11000L), 5000L, "Trollheim Teleport", "🔮", "item_magic_logs"),
        OsrsQuest("quest_eagles_peak", "Celestial Hawk Peak", "🦅", QuestDifficulty.NOVICE, 15, OsrsSkill.HUNTER, 27, emptyList(), "Rescue Nickolaus on Celestial Hawk Peak using giant hawk flight!", listOf(QuestRequirementItem("item_feathers", "Feathers", "🪶", 10)), 2, mapOf(OsrsSkill.HUNTER to 2500L), 2000L, "Eagle Feather Wand", "🪄", "item_feather"),
        OsrsQuest("quest_elemental_workshop_1", "Elemental Workshop I", "⚙️", QuestDifficulty.NOVICE, 20, OsrsSkill.SMITHING, 20, emptyList(), "Discover the secret underground furnace and forge Elemental Metal!", listOf(QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2), QuestRequirementItem("item_coal", "Coal", "🪨", 2)), 1, mapOf(OsrsSkill.SMITHING to 5000L, OsrsSkill.FLETCHING to 5000L), 3000L, "Elemental Shield", "🛡️", "item_steel_bar"),
        OsrsQuest("quest_elemental_workshop_2", "Elemental Workshop II", "⚙️", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.SMITHING, 30, listOf("quest_elemental_workshop_1"), "Operate the mind forge to power Mind Bar equipment!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2), QuestRequirementItem("item_coal", "Coal", "🪨", 4)), 1, mapOf(OsrsSkill.SMITHING to 7500L, OsrsSkill.FLETCHING to 7500L), 5000L, "Mind Shield", "🛡️", "item_mithril_bar"),
        OsrsQuest("quest_fairy_tale_1", "Spirit Grove Rescue", "🧚", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.FARMING, 20, emptyList(), "Help Spirit Grove Fairies save the Fairy Queen from the Root Fiend!", listOf(QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2), QuestRequirementItem("item_potato_seed", "Potato Seed", "🌱", 3)), 2, mapOf(OsrsSkill.FARMING to 3500L, OsrsSkill.HERBLORE to 2000L), 4000L, "Magic Secateurs", "✂️", "item_potato_seed"),
        OsrsQuest("quest_fairy_tale_2", "Fairy Ring Network Expansion", "🌀", QuestDifficulty.INTERMEDIATE, 45, OsrsSkill.HERBLORE, 40, listOf("quest_fairy_tale_1"), "Unlock Fairy Ring teleport network across High Realms!", listOf(QuestRequirementItem("item_maple_logs", "Maple Logs", "🪵", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)), 2, mapOf(OsrsSkill.HERBLORE to 3500L, OsrsSkill.HERBLORE to 2500L), 6000L, "Fairy Ring Access", "🌀", "item_sunleaf_seed"),
        OsrsQuest("quest_family_crest", "High Sovereign Crest", "🛡️", QuestDifficulty.EXPERIENCED, 50, OsrsSkill.SMITHING, 40, emptyList(), "Reunite High Sovereign's three sons to restore the royal crest!", listOf(QuestRequirementItem("item_bar_gold", "Gold Bar", "🪙", 3), QuestRequirementItem("item_bar_silver", "Silver Bar", "🥈", 2)), 1, mapOf(OsrsSkill.SMITHING to 5000L, OsrsSkill.FLETCHING to 5000L), 8000L, "Goldsmith Gauntlets", "🧤", "item_gold_bar"),
        OsrsQuest("quest_the_feud", "Oasis City Reconciliation", "🌵", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.THIEVING, 30, emptyList(), "Resolve the feud between Desert Nomad Bandits in Oasis City!", listOf(QuestRequirementItem("item_raw_trout", "Raw Trout", "🐟", 2), QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2)), 1, mapOf(OsrsSkill.THIEVING to 15000L), 5000L, "Black Blackjack", "🦯", "item_bread"),
        OsrsQuest("quest_fight_arena_part2", "Colosseum Liberation - Part 2", "⚔️", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.ATTACK, 25, emptyList(), "Battle Shadow Warlord's champion monsters in the Colosseum!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2), QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 2)), 2, mapOf(OsrsSkill.ATTACK to 12175L, OsrsSkill.THIEVING to 2175L), 5000L, "Shadow Armor Set", "🛡️", "item_iron_bar"),
        OsrsQuest("quest_fishing_contest", "Highland Fishing Contest", "🎣", QuestDifficulty.NOVICE, 1, OsrsSkill.FISHING, 10, emptyList(), "Win the Highland Fishing Contest using garlic and red worms!", listOf(QuestRequirementItem("item_raw_shrimps", "Raw Shrimps", "🦐", 3)), 1, mapOf(OsrsSkill.FISHING to 2437L), 1500L, "Fishing Trophy", "🏆", "item_raw_trout"),
        OsrsQuest("quest_fremennik_trials_part2", "Trials of the Northland Council - Part 2", "🪓", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.FLETCHING, 40, emptyList(), "Pass tests of seven Northland council members to become a Frost Warrior!", listOf(QuestRequirementItem("item_raw_salmon", "Raw Salmon", "🐟", 2), QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)), 3, mapOf(OsrsSkill.FLETCHING to 2812L, OsrsSkill.FISHING to 2812L, OsrsSkill.WOODCUTTING to 2812L), 8000L, "Frost Warrior Helm", "🪖", "item_raw_salmon"),
        OsrsQuest("quest_fremennik_isles_part2", "Isles of the Frost Trolls - Part 2", "⚔️", QuestDifficulty.EXPERIENCED, 55, OsrsSkill.CONSTRUCTION, 30, listOf("quest_fremennik_trials"), "Unite Twin Northern Clans against the Frost Titan King!", listOf(QuestRequirementItem("item_oak_plank", "Oak Planks", "🪵", 3), QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2)), 1, mapOf(OsrsSkill.CONSTRUCTION to 10000L, OsrsSkill.FLETCHING to 10000L), 12000L, "Glacial Horned Helm", "🪖", "item_oak_plank"),
        OsrsQuest("quest_ghosts_ahoy", "Liberating Ghostly Citadel", "👻", QuestDifficulty.INTERMEDIATE, 35, OsrsSkill.MAGIC, 25, listOf("quest_restless_ghost"), "Free the ghosts of Ghostly Citadel and obtain the Spirit Portal Vessel!", listOf(QuestRequirementItem("item_big_bones", "Big Bones", "🦴", 3), QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)), 2, mapOf(OsrsSkill.MAGIC to 2400L), 5000L, "Spirit Portal Vessel", "🏺", "item_dragon_bones"),
        OsrsQuest("quest_giant_dwarf", "Subterranean Dwarven City", "⛏️", QuestDifficulty.INTERMEDIATE, 20, OsrsSkill.SMITHING, 20, emptyList(), "Enter the Underground Dwarf Citadel!", listOf(QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2), QuestRequirementItem("item_ore_iron", "Iron Ore", "🪨", 2)), 2, mapOf(OsrsSkill.SMITHING to 2500L, OsrsSkill.SMITHING to 2500L, OsrsSkill.FLETCHING to 2500L), 4000L, "Dwarf Citadel Ticket", "🎫", "item_iron_ore"),
        OsrsQuest("quest_grand_tree_part2", "The Elder Worldtree Conspiracy - Part 2", "🌳", QuestDifficulty.EXPERIENCED, 45, OsrsSkill.AGILITY, 35, emptyList(), "Expose Woodland King Narnode's treachery and defeat the Shadow Demon!", listOf(QuestRequirementItem("item_maple_logs", "Maple Logs", "🪵", 2), QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2)), 5, mapOf(OsrsSkill.AGILITY to 18400L, OsrsSkill.ATTACK to 18400L, OsrsSkill.MAGIC to 2150L), 10000L, "Glider Flight Pass", "✈️", "item_magic_logs"),
        OsrsQuest("quest_great_brain_robbery", "Harmony Isle Exorcism", "🧠", QuestDifficulty.EXPERIENCED, 50, OsrsSkill.MAGIC, 50, listOf("quest_creature_fenkenstrain"), "Stop Alchemist Fenkenstrain from transplanting spirit essences on Harmony Isle!", listOf(QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2), QuestRequirementItem("item_dragon_bones", "Dragon Bones", "🦴", 2)), 2, mapOf(OsrsSkill.MAGIC to 6000L, OsrsSkill.FLETCHING to 3000L), 8000L, "Anchor Weapon", "⚓", "item_dragon_bones"),
        OsrsQuest("quest_grim_tales", "Grim Tales of the Giant", "📖", QuestDifficulty.MASTER, 70, OsrsSkill.HERBLORE, 55, emptyList(), "Climb the beanstalk and trick the cloud giant to recover Grimm's items!", listOf(QuestRequirementItem("item_magic_logs", "Magic Logs", "🪵", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)), 1, mapOf(OsrsSkill.HERBLORE to 14000L, OsrsSkill.FARMING to 4000L), 15000L, "Giant's Feather", "🪶", "item_sunleaf_seed"),
        OsrsQuest("quest_haunted_mine", "Haunted Mine Purification", "⛏️", QuestDifficulty.EXPERIENCED, 50, OsrsSkill.FLETCHING, 40, emptyList(), "Defeat Shadow Phantom inside the Haunted Abyss Mine for Salve Amulet!", listOf(QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2), QuestRequirementItem("item_big_bones", "Big Bones", "🦴", 3)), 2, mapOf(OsrsSkill.ATTACK to 22000L), 8000L, "Salve Amulet", "📿", "item_mithril_ore"),
        OsrsQuest("quest_heroes_quest", "Trial of Sovereign Heroes", "🦸", QuestDifficulty.EXPERIENCED, 55, OsrsSkill.SMITHING, 50, listOf("quest_shield_of_arrav"), "Gain entry to the Sovereign Heroes' Guild by completing trials of valor!", listOf(QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2), QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 3)), 1, mapOf(OsrsSkill.ATTACK to 3075L, OsrsSkill.DEFENCE to 3075L, OsrsSkill.ATTACK to 3075L, OsrsSkill.HITPOINTS to 3075L), 15000L, "Heroes' Guild Crest", "🛡️", "item_adamant_ore"),
        OsrsQuest("quest_holy_grail_part2", "Quest for the Sacred Chalice - Part 2", "🏆", QuestDifficulty.INTERMEDIATE, 45, OsrsSkill.MAGIC, 35, listOf("quest_merlins_crystal"), "Seek the Sacred Chalice for High King Arthur and defeat the Shadow Titan!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)), 2, mapOf(OsrsSkill.MAGIC to 11000L, OsrsSkill.DEFENCE to 15300L), 10000L, "Sacred Chalice Vessel", "🏆", "item_dragon_bones"),
        OsrsQuest("quest_horror_from_deep_part2", "Tide Sentinel Terror - Part 2", "🐙", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.MAGIC, 25, emptyList(), "Investigate Lighthouse basement and slay the Deep Tide Sovereign!", listOf(QuestRequirementItem("item_swordfish", "Cooked Swordfish", "🐟", 2), QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)), 2, mapOf(OsrsSkill.MAGIC to 4662L, OsrsSkill.RANGED to 4662L, OsrsSkill.ATTACK to 4662L), 7000L, "God Book Page", "📖", "item_raw_swordfish"),
        OsrsQuest("quest_icthlarins_helper", "Spirit Guardian's Blessing", "🐈", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.AGILITY, 25, emptyList(), "Aid Spirit Guardian Icthlarin in saving Sunken Citadel from Shadow Queen!", listOf(QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2), QuestRequirementItem("item_trout", "Cooked Trout", "🐟", 2)), 2, mapOf(OsrsSkill.AGILITY to 4500L, OsrsSkill.THIEVING to 4500L, OsrsSkill.WOODCUTTING to 4500L), 5000L, "Sunken Citadel Wand", "🪄", "item_oak_logs"),
        OsrsQuest("quest_in_search_myreque", "Path to Spirit Resistance", "🦇", QuestDifficulty.NOVICE, 25, OsrsSkill.AGILITY, 25, emptyList(), "Navigate Misty Abyss Swamp to locate secret Resistance hideout!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2), QuestRequirementItem("item_trout", "Cooked Trout", "🐟", 2)), 2, mapOf(OsrsSkill.ATTACK to 600L, OsrsSkill.DEFENCE to 600L, OsrsSkill.ATTACK to 600L, OsrsSkill.HITPOINTS to 600L, OsrsSkill.FLETCHING to 600L), 3000L, "Swamp Boots", "👢", "item_bread"),
        OsrsQuest("quest_jungle_potion", "Jungle Spirit Elixirs", "🌴", QuestDifficulty.NOVICE, 10, OsrsSkill.HERBLORE, 5, emptyList(), "Gather five tropical herbs for High Druid in Jungle Spirit Village!", listOf(QuestRequirementItem("item_logs", "Logs", "🪵", 2)), 1, mapOf(OsrsSkill.HERBLORE to 3750L), 2000L, "Tropical Herb Pouch", "🌿", "item_bread"),
        OsrsQuest("quest_kings_ransom", "High Sovereign Rescue", "👑", QuestDifficulty.EXPERIENCED, 65, OsrsSkill.DEFENCE, 65, listOf("quest_holy_grail"), "Rescue High King Arthur from Shadow Witch's Citadel prison!", listOf(QuestRequirementItem("item_bar_adamant", "Adamant Bar", "🛡️", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)), 1, mapOf(OsrsSkill.DEFENCE to 33000L, OsrsSkill.MAGIC to 5000L), 15000L, "Knight Waves Scroll", "📜", "item_rune_bar"),
        OsrsQuest("quest_legends_quest", "Legends' Realm Trial", "🏰", QuestDifficulty.MASTER, 75, OsrsSkill.HERBLORE, 55, listOf("quest_heroes_quest"), "Prove your worth to join Legends' Guild by mapping Mystic Jungle!", listOf(QuestRequirementItem("item_bar_adamant", "Adamant Bar", "🛡️", 2), QuestRequirementItem("item_yew_logs", "Yew Logs", "🪵", 3), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)), 4, mapOf(OsrsSkill.ATTACK to 7650L, OsrsSkill.DEFENCE to 7650L, OsrsSkill.ATTACK to 7650L, OsrsSkill.HITPOINTS to 7650L, OsrsSkill.MAGIC to 7650L, OsrsSkill.MAGIC to 7650L, OsrsSkill.WOODCUTTING to 7650L, OsrsSkill.FLETCHING to 7650L, OsrsSkill.SMITHING to 7650L, OsrsSkill.SMITHING to 7650L, OsrsSkill.HERBLORE to 7650L, OsrsSkill.AGILITY to 7650L), 25000L, "Dragon Square Shield", "🛡️", "item_dragon_bones"),
        OsrsQuest("quest_lost_city_part2", "Gateway to Celestial City - Part 2", "🧚", QuestDifficulty.INTERMEDIATE, 35, OsrsSkill.WOODCUTTING, 36, emptyList(), "Harvest Dramen Branch on Holy Isle to unlock Spirit Realm Portal!", listOf(QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2), QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)), 3, mapOf(OsrsSkill.WOODCUTTING to 5000L, OsrsSkill.FLETCHING to 5000L), 8000L, "Fairy Portal Staff", "🪄", "item_magic_logs"),
        OsrsQuest("quest_lunar_diplomacy_part2", "Lunar Dream Harmony - Part 2", "🌙", QuestDifficulty.MASTER, 70, OsrsSkill.HERBLORE, 65, listOf("quest_lost_city"), "Craft Moonclan Robes and master Lunar Magicks on Moon Isle!", listOf(QuestRequirementItem("item_maple_logs", "Maple Logs", "🪵", 3), QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)), 2, mapOf(OsrsSkill.MAGIC to 5000L, OsrsSkill.RUNECRAFT to 5000L, OsrsSkill.FLETCHING to 5000L, OsrsSkill.SMITHING to 5000L, OsrsSkill.HERBLORE to 5000L, OsrsSkill.AGILITY to 5000L), 20000L, "+1 Active Incantation Slot & Lunar Staff", "🌙", "item_rune_bar"),
        OsrsQuest("quest_making_history", "Chronicles of the Realm", "📜", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.FLETCHING, 35, emptyList(), "Research ancient artifacts across Western Outpost and Ghostly Citadel!", listOf(QuestRequirementItem("item_bar_silver", "Silver Bar", "🥈", 2), QuestRequirementItem("item_oak_logs", "Oak Logs", "🪵", 2)), 3, mapOf(OsrsSkill.FLETCHING to 1000L, OsrsSkill.MAGIC to 1000L), 5000L, "Enchanted Key", "🔑", "item_steel_bar"),
        OsrsQuest("quest_monkey_madness_1_part2", "Wild Isle Infiltration - Part 2", "🐒", QuestDifficulty.MASTER, 70, OsrsSkill.AGILITY, 50, listOf("quest_grand_tree"), "Transform using Spirit Totem and slay Jungle Fiend on Primate Isle!", listOf(QuestRequirementItem("item_bar_gold", "Gold Bar", "🪙", 2), QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 3), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)), 3, mapOf(OsrsSkill.ATTACK to 35000L, OsrsSkill.DEFENCE to 35000L, OsrsSkill.ATTACK to 35000L, OsrsSkill.HITPOINTS to 35000L), 30000L, "Dragon Scimitar Access", "🗡️", "item_dragon_scimitar"),
        OsrsQuest("quest_monkey_madness_2_part2", "Siege of the Primate Fleet - Part 2", "🦍", QuestDifficulty.GRANDMASTER, 90, OsrsSkill.SLAYER, 75, listOf("quest_monkey_madness_1"), "Infiltrate Shadow Warlord's Airship and slay Primal Gorillas in Volcanic Cavern!", listOf(QuestRequirementItem("item_shark", "Cooked Shark", "🦈", 3), QuestRequirementItem("item_bar_rune", "Rune Bar", "⚔️", 2), QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 2)), 4, mapOf(OsrsSkill.SLAYER to 25000L, OsrsSkill.AGILITY to 20000L, OsrsSkill.THIEVING to 15000L, OsrsSkill.HUNTER to 15000L), 75000L, "Heavy Ballista", "🏹", "item_dragon_bones", listOf("🔓 In-Game Feature: Equip Heavy Ballista Ranged Weapon", "🌾 Permanent +25% Chance for Double Crop Harvest & Barn Animal Drops")),
        OsrsQuest("quest_mountain_daughter", "Sacred Bear Guardian", "🐻", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.AGILITY, 25, emptyList(), "Resolve Highland Camp leader's grief by slaying the Sacred Mountain Bear!", listOf(QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2), QuestRequirementItem("item_trout", "Cooked Trout", "🐟", 2)), 2, mapOf(OsrsSkill.ATTACK to 2000L, OsrsSkill.MAGIC to 2000L), 5000L, "Bearhead Mask", "🐻", "item_bread"),
        OsrsQuest("quest_murder_mystery", "Highland Manor Mystery", "🔍", QuestDifficulty.NOVICE, 1, OsrsSkill.THIEVING, 5, emptyList(), "Analyze fingerprints and poison evidence at Highland Manor to catch Lord Sinclair's killer!", listOf(QuestRequirementItem("item_bones", "Bones", "🦴", 1)), 3, mapOf(OsrsSkill.FLETCHING to 1400L), 2000L, "Magnifying Glass", "🔍", "item_bread"),
        OsrsQuest("quest_my_arms_big_adventure", "Glacial Farming Peak", "💪", QuestDifficulty.EXPERIENCED, 50, OsrsSkill.FARMING, 40, listOf("quest_eadgars_ruse"), "Teach Glacial Titan My Arm how to cultivate rare Herbs on Glacial Peak!", listOf(QuestRequirementItem("item_sunleaf_seed", "Sunleaf Seed", "🌿", 2), QuestRequirementItem("item_maple_logs", "Maple Logs", "🪵", 2)), 1, mapOf(OsrsSkill.FARMING to 10000L, OsrsSkill.HERBLORE to 10000L), 10000L, "Disease-Free Herb Patch", "🌱", "item_sunleaf_seed"),
        OsrsQuest("quest_nature_spirit_part2", "Swamp Guardian Consecration - Part 2", "🍄", QuestDifficulty.NOVICE, 20, OsrsSkill.MAGIC, 20, listOf("quest_restless_ghost"), "Purify Ancient Hermit's Grotto to become Swamp Nature Guardian!", listOf(QuestRequirementItem("item_bar_silver", "Silver Bar", "🥈", 2), QuestRequirementItem("item_bones", "Bones", "🦴", 2)), 2, mapOf(OsrsSkill.FLETCHING to 3000L, OsrsSkill.HITPOINTS to 2000L, OsrsSkill.DEFENCE to 2000L), 3000L, "Silver Sickle (b)", "🌙", "item_dragon_bones", listOf("💰 Permanent 2x Multiplier on Gold earned through Trickery (Thieving)")),
        OsrsQuest("quest_observatory_quest", "Stargazer Observatory", "🔭", QuestDifficulty.NOVICE, 10, OsrsSkill.FLETCHING, 10, emptyList(), "Help Observatory Professor craft a telescope lens to view constellation stars!", listOf(QuestRequirementItem("item_bar_bronze", "Bronze Bar", "🧱", 2)), 2, mapOf(OsrsSkill.FLETCHING to 2250L), 2500L, "Supercompost", "🪴", "item_potato_seed"),
        OsrsQuest("quest_one_small_favour", "Cascade of Favours", "📜", QuestDifficulty.EXPERIENCED, 45, OsrsSkill.HERBLORE, 35, emptyList(), "Embark on an epic cascade of favours around High Realms starting in Jungle Village!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2), QuestRequirementItem("item_maple_logs", "Maple Logs", "🪵", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)), 2, mapOf(OsrsSkill.HERBLORE to 10000L, OsrsSkill.FLETCHING to 10000L, OsrsSkill.SMITHING to 10000L, OsrsSkill.SMITHING to 10000L), 12000L, "Key Ring", "🔑", "item_mithril_bar"),
        OsrsQuest("quest_priest_in_peril_part2", "Sanctuary Border Defense - Part 2", "🦇", QuestDifficulty.NOVICE, 25, OsrsSkill.MAGIC, 10, emptyList(), "Save Guardian Drezel in Border Temple to unlock entrance into Shadow Realm!", listOf(QuestRequirementItem("item_bones", "Bones", "🦴", 3), QuestRequirementItem("item_raw_beef", "Raw Beef", "🥩", 1)), 1, mapOf(OsrsSkill.MAGIC to 1406L), 3000L, "Wolfbane Dagger", "🗡️", "item_dragon_bones"),
        OsrsQuest("quest_rag_and_bone_man_1", "Relic Bone Collector I", "🦴", QuestDifficulty.NOVICE, 15, OsrsSkill.SLAYER, 5, emptyList(), "Collect odd monster bones and boil them in vinegar for Old Man Oddbone!", listOf(QuestRequirementItem("item_bones", "Bones", "🦴", 4)), 1, mapOf(OsrsSkill.COOKING to 500L), 1500L, "Bonesack", "🎒", "item_bones"),
        OsrsQuest("quest_rag_and_bone_man_2", "Relic Bone Collector II", "🦴", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.SLAYER, 35, listOf("quest_rag_and_bone_man_1"), "Collect 27 additional exotic creature bones across High Realms!", listOf(QuestRequirementItem("item_big_bones", "Big Bones", "🦴", 4), QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)), 1, mapOf(OsrsSkill.COOKING to 5000L, OsrsSkill.MAGIC to 5000L), 8000L, "Ram Skull Helm", "💀", "item_big_bones"),
        OsrsQuest("quest_ratcatchers", "Citadel Pest Exorcism", "🐀", QuestDifficulty.INTERMEDIATE, 35, OsrsSkill.THIEVING, 30, emptyList(), "Clear rat infestations in Highland Citadel, Western Outpost, Dwarven City, and Harbor Town!", listOf(QuestRequirementItem("item_raw_trout", "Raw Trout", "🐟", 2), QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2)), 2, mapOf(OsrsSkill.THIEVING to 4500L), 6000L, "Wily Cat Training", "🐈", "item_bread"),
        OsrsQuest("quest_recruitment_drive", "Order of Light Initiation", "🛡️", QuestDifficulty.NOVICE, 15, OsrsSkill.MAGIC, 10, emptyList(), "Pass Order of Light initiation tests to become an Initiate Knight!", listOf(QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2)), 1, mapOf(OsrsSkill.MAGIC to 1000L, OsrsSkill.HERBLORE to 1000L, OsrsSkill.AGILITY to 1000L), 2500L, "Initiate Armor Access", "🛡️", "item_steel_bar"),
        OsrsQuest("quest_regicide_part2", "Forest Realm Tyrant Downfall - Part 2", "👑", QuestDifficulty.MASTER, 70, OsrsSkill.AGILITY, 56, listOf("quest_underground_pass"), "Cross Mist Forest traps to execute High Sovereign's order on Tyrant King!", listOf(QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2), QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 3), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)), 3, mapOf(OsrsSkill.AGILITY to 13750L, OsrsSkill.FLETCHING to 13750L), 25000L, "Dragon Halberd Access", "🗡️", "item_magic_logs"),
        OsrsQuest("quest_roving_elves_part2", "Tomb of Ancient Guardians - Part 2", "🧝", QuestDifficulty.EXPERIENCED, 60, OsrsSkill.AGILITY, 56, listOf("quest_regicide"), "Aid Forest Wanderers in restoring the consecration of the Crystal Shrine!", listOf(QuestRequirementItem("item_yew_logs", "Yew Logs", "🪵", 2), QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)), 1, mapOf(OsrsSkill.ATTACK to 10000L), 15000L, "Crystal Shield / Bow", "🛡️", "item_crystal_bow"),
        OsrsQuest("quest_royal_trouble", "Island Realm Uprising", "🏰", QuestDifficulty.EXPERIENCED, 55, OsrsSkill.SLAYER, 40, listOf("quest_throne_miscellania"), "Resolve Island Realm rebellion and slay Giant Sea Hydra!", listOf(QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2), QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 2)), 1, mapOf(OsrsSkill.AGILITY to 5000L, OsrsSkill.SLAYER to 5000L), 15000L, "Island Kingdom Management", "🏰", "item_adamant_bar"),
        OsrsQuest("quest_rum_deal", "Spirited Brew Mastery", "🍾", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.FARMING, 40, emptyList(), "Brew Spirited Elixir on Smuggler's Isle with Cap'n Braindeath!", listOf(QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)), 2, mapOf(OsrsSkill.FARMING to 7000L, OsrsSkill.HERBLORE to 7000L, OsrsSkill.FISHING to 7000L), 8000L, "Holy Wrench", "🔧", "item_holy_wrench"),
        OsrsQuest("quest_scorpion_catcher", "Elemental Scorpion Hunt", "🦂", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.MAGIC, 31, emptyList(), "Catch High Sorcerer's three escaping elemental scorpions!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2), QuestRequirementItem("item_trout", "Cooked Trout", "🐟", 2)), 1, mapOf(OsrsSkill.ATTACK to 6625L), 5000L, "Battlestaff Enchanting", "🪄", "item_gold_bar"),
        OsrsQuest("quest_sea_slug", "Coastal Tide Defense", "🐌", QuestDifficulty.NOVICE, 1, OsrsSkill.FIREMAKING, 30, emptyList(), "Rescue trapped villagers on Coastal Fishing Platform from mind-controlling Tide Parasites!", listOf(QuestRequirementItem("item_logs", "Logs", "🪵", 2)), 1, mapOf(OsrsSkill.FISHING to 7175L), 2500L, "Oyster Pearls", "🦪", "item_raw_shrimps"),
        OsrsQuest("quest_shades_mortton", "Shadow Temple Purification", "🕯️", QuestDifficulty.INTERMEDIATE, 35, OsrsSkill.HERBLORE, 20, emptyList(), "Rebuild the Shadow Temple and burn shade remains with sacred oil!", listOf(QuestRequirementItem("item_oak_logs", "Oak Logs", "🪵", 3), QuestRequirementItem("item_big_bones", "Big Bones", "🦴", 2)), 2, mapOf(OsrsSkill.HERBLORE to 2000L, OsrsSkill.FLETCHING to 2000L), 4000L, "Pyre Logs & Oils", "🪵", "item_oak_logs"),
        OsrsQuest("quest_shadow_of_storm", "Shadow Fiend Banishing - Part 2", "😈", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.FLETCHING, 30, listOf("quest_demon_slayer"), "Infiltrate Desert Ruins dark cult and banish Fiend Lord with Sacred Blade!", listOf(QuestRequirementItem("item_bar_silver", "Silver Bar", "🥈", 2), QuestRequirementItem("item_big_bones", "Big Bones", "🦴", 2)), 1, mapOf(OsrsSkill.ATTACK to 10000L), 8000L, "Arclite Demonic Slayer Sword", "🗡️", "item_arclite"),
        OsrsQuest("quest_sheep_herder", "Highland Herd Quarantine", "🐑", QuestDifficulty.NOVICE, 10, OsrsSkill.HERBLORE, 1, emptyList(), "Herd infected sheep into Western Outpost incinerator!", listOf(QuestRequirementItem("item_raw_beef", "Raw Beef", "🥩", 1)), 4, mapOf(OsrsSkill.HERBLORE to 3100L), 2000L, "Cattleprod", "⚡", "item_bread"),
        OsrsQuest("quest_shilo_village", "Jungle Spirit Liberation", "🌴", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.FLETCHING, 25, listOf("quest_jungle_potion"), "Exorcise Ancient Zombie Plague and liberate Jungle Village!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2), QuestRequirementItem("item_big_bones", "Big Bones", "🦴", 2)), 2, mapOf(OsrsSkill.FLETCHING to 3875L), 8000L, "Jungle Village Access", "🌴", "item_gold_bar"),
        OsrsQuest("quest_song_of_elves_part2", "Reclaiming the Crystal City - Part 2", "🧝‍♀️", QuestDifficulty.GRANDMASTER, 100, OsrsSkill.CONSTRUCTION, 75, listOf("quest_roving_elves"), "Reclaim Crystal City and defeat Shadow Fragment!", listOf(QuestRequirementItem("item_magic_logs", "Magic Logs", "🪵", 4), QuestRequirementItem("item_bar_rune", "Rune Bar", "⚔️", 2), QuestRequirementItem("item_saradomin_brew", "Restoration Elixir", "🧪", 2)), 4, mapOf(OsrsSkill.CONSTRUCTION to 40000L, OsrsSkill.AGILITY to 40000L, OsrsSkill.WOODCUTTING to 40000L), 150000L, "Crystal City", "🏰", "item_rune_bar"),
        OsrsQuest("quest_souls_bane", "Abyssal Rift Trial", "🕳️", QuestDifficulty.NOVICE, 20, OsrsSkill.MAGIC, 15, emptyList(), "Enter Abyssal Rift Dungeon and defeat manifestations of Confusion, Anger, and Fear!", listOf(QuestRequirementItem("item_bones", "Bones", "🦴", 2)), 1, mapOf(OsrsSkill.HITPOINTS to 5000L, OsrsSkill.DEFENCE to 5000L), 3000L, "Dark Core Ring", "💍", "item_bread"),
        OsrsQuest("quest_swan_song", "High Sage's Sanctuary", "🦢", QuestDifficulty.MASTER, 70, OsrsSkill.FISHING, 62, emptyList(), "Save High Sage's Fishing Colony from Deep Sea Leviathan Queen!", listOf(QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 4), QuestRequirementItem("item_bar_adamant", "Adamant Bar", "🛡️", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)), 2, mapOf(OsrsSkill.FISHING to 15000L, OsrsSkill.MAGIC to 10000L, OsrsSkill.HERBLORE to 10000L), 20000L, "Monkfish Fishing Access", "🐟", "item_raw_swordfish"),
        OsrsQuest("quest_tai_bwo_wannai", "Jungle Fishermen Trio", "🐟", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.FISHING, 20, emptyList(), "Help Island Chieftain's three sons catch rare Deep Sea Fish!", listOf(QuestRequirementItem("item_raw_trout", "Raw Trout", "🐟", 2), QuestRequirementItem("item_raw_salmon", "Raw Salmon", "🐟", 2)), 2, mapOf(OsrsSkill.FISHING to 5000L, OsrsSkill.COOKING to 5000L, OsrsSkill.AGILITY to 5000L), 6000L, "Deep Sea Fishing Vessel", "🚤", "item_raw_lobster"),
        OsrsQuest("quest_tail_of_two_cats", "Sacred Companion Heritage", "🐈‍⬛", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.HUNTER, 20, emptyList(), "Uncover Bob the Sacred Companion Cat's ancient tiger lineage with High Druid Neite!", listOf(QuestRequirementItem("item_raw_salmon", "Raw Salmon", "🐟", 2), QuestRequirementItem("item_bar_silver", "Silver Bar", "🥈", 1)), 2, mapOf(OsrsSkill.HUNTER to 2500L, OsrsSkill.FLETCHING to 2500L), 5000L, "Mouse Toy & Camulet", "🐭", "item_toy_mouse"),
        OsrsQuest("quest_tears_of_guthix", "Elemental Tears Cave", "💧", QuestDifficulty.NOVICE, 20, OsrsSkill.SMITHING, 20, emptyList(), "Mine Spirit Serpent Cave to gain weekly Elemental Tears XP boost!", listOf(QuestRequirementItem("item_ore_iron", "Iron Ore", "🪨", 2), QuestRequirementItem("item_trout", "Cooked Trout", "🐟", 2)), 1, mapOf(OsrsSkill.FLETCHING to 1000L), 3000L, "Elemental Tears Access", "💧", "item_iron_ore"),
        OsrsQuest("quest_temple_of_ikov_part2", "Subterranean Sun Temple - Part 2", "🏹", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.RANGED, 40, emptyList(), "Infiltrate Subterranean Sun Temple and decide whether to side with Shadow Warlord or Guardians of Air!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2), QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2)), 1, mapOf(OsrsSkill.RANGED to 10500L, OsrsSkill.FLETCHING to 8000L), 8000L, "Staff of Air Guardians", "🪄", "item_arrowtip"),
        OsrsQuest("quest_throne_miscellania", "Throne of Island Realm", "👑", QuestDifficulty.EXPERIENCED, 50, OsrsSkill.WOODCUTTING, 45, emptyList(), "Marry Island Princess or Prince to rule Island Realm!", listOf(QuestRequirementItem("item_maple_logs", "Maple Logs", "🪵", 3), QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2), QuestRequirementItem("item_bar_gold", "Gold Bar", "🪙", 1)), 1, mapOf(OsrsSkill.WOODCUTTING to 5000L, OsrsSkill.FISHING to 5000L, OsrsSkill.SMITHING to 5000L), 12000L, "Kingdom Revenue Tax", "🪙", "item_oak_logs"),
        OsrsQuest("quest_tourist_trap", "Sunken Desert Rescue", "🌵", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.FLETCHING, 20, emptyList(), "Infiltrate Sunken Desert mining camp to rescue Ana from Desert Bandits!", listOf(QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2), QuestRequirementItem("item_feathers", "Feathers", "🪶", 10)), 2, mapOf(OsrsSkill.FLETCHING to 9300L, OsrsSkill.SMITHING to 9300L), 6000L, "Desert Mining Pass", "🌵", "item_iron_ore"),
        OsrsQuest("quest_tower_of_life", "Alchemist Tower Spawners", "🏗️", QuestDifficulty.NOVICE, 25, OsrsSkill.CONSTRUCTION, 15, emptyList(), "Help Alchemist Guild build monster combination spawners!", listOf(QuestRequirementItem("item_oak_plank", "Oak Planks", "🪵", 3), QuestRequirementItem("item_bar_iron", "Iron Bar", "🧱", 2)), 2, mapOf(OsrsSkill.CONSTRUCTION to 1000L, OsrsSkill.FLETCHING to 500L), 3000L, "Creature Creation Access", "🧪", "item_plank"),
        OsrsQuest("quest_tree_gnome_village_part2", "Woodland Protection Orbs - Part 2", "🌲", QuestDifficulty.INTERMEDIATE, 30, OsrsSkill.ATTACK, 25, emptyList(), "Infiltrate Shadow Warlord maze to retrieve the Orbs of Protection for Woodland King!", listOf(QuestRequirementItem("item_willow_logs", "Willow Logs", "🪵", 2), QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)), 2, mapOf(OsrsSkill.ATTACK to 11450L), 6000L, "Woodland Spirit Tree Access", "🌳", "item_iron_bar"),
        OsrsQuest("quest_tribal_totem", "Ancestral Tribal Totem", "🗿", QuestDifficulty.NOVICE, 15, OsrsSkill.THIEVING, 21, emptyList(), "Steal the Ancestral Tribal Totem back from High Lord's mansion vault!", listOf(QuestRequirementItem("item_bar_silver", "Silver Bar", "🥈", 2)), 1, mapOf(OsrsSkill.THIEVING to 1775L), 2500L, "Tribal Totem Crest", "🗿", "item_gold_bar"),
        OsrsQuest("quest_troll_stronghold", "Titan Stronghold Rescue", "🏔️", QuestDifficulty.EXPERIENCED, 50, OsrsSkill.AGILITY, 30, listOf("quest_death_plateau"), "Infiltrate Glacial Titan Stronghold prison and rescue Captured Scholars Godric and Eadgar!", listOf(QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 2), QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2)), 1, mapOf(OsrsSkill.AGILITY to 10000L), 10000L, "Law Rune Altar Access", "🔮", "item_adamant_bar"),
        OsrsQuest("quest_troll_romance", "Titan Mountain Romance", "❤️", QuestDifficulty.INTERMEDIATE, 45, OsrsSkill.AGILITY, 30, listOf("quest_troll_stronghold"), "Help Glacial Titan Ug win flower for his love Aga by slaying Mountain Giant Arrg!", listOf(QuestRequirementItem("item_salmon", "Cooked Salmon", "🐟", 2), QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)), 2, mapOf(OsrsSkill.AGILITY to 8000L, OsrsSkill.ATTACK to 4000L), 8000L, "Titan Mountain Shortcut", "🏔️", "item_bread"),
        OsrsQuest("quest_underground_pass_part2", "Treacherous Abyss Passage - Part 2", "🕳️", QuestDifficulty.EXPERIENCED, 60, OsrsSkill.AGILITY, 50, emptyList(), "Delve deep into Corrupted Nether Pass beneath Western Mountain Ranges!", listOf(QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2), QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 3), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)), 5, mapOf(OsrsSkill.AGILITY to 15000L, OsrsSkill.ATTACK to 3000L), 20000L, "Archmage Nether Staff", "🪄", "item_magic_logs"),
        OsrsQuest("quest_wanted", "Shadow Outlaw Pursuit", "🛡️", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.SLAYER, 25, listOf("quest_recruitment_drive"), "Track Shadow Outlaw across High Realms to become Order of Light Captain!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2), QuestRequirementItem("item_trout", "Cooked Trout", "🐟", 2)), 1, mapOf(OsrsSkill.SLAYER to 5000L), 8000L, "Order of Light Armor Access", "🛡️", "item_steel_bar"),
        OsrsQuest("quest_watchtower", "Southern Citadel Defense", "🔮", QuestDifficulty.INTERMEDIATE, 45, OsrsSkill.MAGIC, 25, emptyList(), "Build Southern Citadel watchtower defenses and defeat Highland Giants!", listOf(QuestRequirementItem("item_bar_gold", "Gold Bar", "🪙", 2), QuestRequirementItem("item_big_bones", "Big Bones", "🦴", 2)), 4, mapOf(OsrsSkill.MAGIC to 15250L), 10000L, "Watchtower Teleport", "🔮", "item_gold_bar"),
        OsrsQuest("quest_waterfall_quest_part2", "Waterfall Cavern Treasures - Part 2", "🌊", QuestDifficulty.NOVICE, 20, OsrsSkill.ATTACK, 10, emptyList(), "Enter Cascading Falls tomb to uncover Ancient Guardian treasures!", listOf(QuestRequirementItem("item_raw_trout", "Raw Trout", "🐟", 2)), 1, mapOf(OsrsSkill.ATTACK to 13750L, OsrsSkill.ATTACK to 13750L), 5000L, "Guardian's Amulet & Urn", "🏺", "item_bread"),
        OsrsQuest("quest_what_lies_below", "Shadow Outlaw Mind Control", "📜", QuestDifficulty.INTERMEDIATE, 35, OsrsSkill.RUNECRAFT, 35, emptyList(), "Stop High Sovereign's mind control by Shadow Outlaws!", listOf(QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2), QuestRequirementItem("item_chaos_rune", "Chaos Rune", "🌀", 5)), 1, mapOf(OsrsSkill.RUNECRAFT to 8000L, OsrsSkill.DEFENCE to 2000L), 6000L, "Beacon Ring", "💍", "item_steel_bar"),
        OsrsQuest("quest_witchs_house_part2", "Haunted Manor Artifact - Part 2", "🧙‍♀️", QuestDifficulty.NOVICE, 15, OsrsSkill.HITPOINTS, 5, emptyList(), "Infiltrate High Witch Garden to retrieve a young boy's lost orb!", listOf(QuestRequirementItem("item_bones", "Bones", "🦴", 2)), 4, mapOf(OsrsSkill.HITPOINTS to 6325L), 3000L, "Boy's Enchanted Orb", "⚽", "item_bread"),
        OsrsQuest("quest_zogre_flesh_eaters", "Wilderness Disease Outbreak", "🧟", QuestDifficulty.INTERMEDIATE, 40, OsrsSkill.SMITHING, 20, emptyList(), "Investigate Wilderness Undead Outbreak and cure Rot Disease with High Sage Reldo!", listOf(QuestRequirementItem("item_big_bones", "Big Bones", "🦴", 3), QuestRequirementItem("item_bar_steel", "Steel Bar", "🧱", 2)), 1, mapOf(OsrsSkill.FLETCHING to 2000L, OsrsSkill.HERBLORE to 2000L), 7000L, "Ogre Comp Bow Access", "🏹", "item_big_bones"),
        OsrsQuest("quest_sins_of_father_part2", "Rebellion in Shadow Realm - Part 2", "🦇", QuestDifficulty.MASTER, 85, OsrsSkill.SLAYER, 65, listOf("quest_darkness_hallowvale"), "Battle Shadow Sovereign Drakan in Dark Citadel to liberate Shadow Realm!", listOf(QuestRequirementItem("item_yew_logs", "Yew Logs", "🪵", 3), QuestRequirementItem("item_bar_adamant", "Adamant Bar", "🛡️", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)), 2, mapOf(OsrsSkill.SLAYER to 15000L, OsrsSkill.WOODCUTTING to 15000L), 50000L, "Vampire Slayer Flail", "🪓", "item_dragon_bones"),
        OsrsQuest("quest_kingdom_divided_part2", "Uniting the Realm Houses - Part 2", "👑", QuestDifficulty.EXPERIENCED, 75, OsrsSkill.AGILITY, 60, emptyList(), "Uncover High Council Conspiracy and reinstate Shadow Grimoire!", listOf(QuestRequirementItem("item_magic_logs", "Magic Logs", "🪵", 2), QuestRequirementItem("item_bar_adamant", "Adamant Bar", "🛡️", 2), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 2)), 2, mapOf(OsrsSkill.AGILITY to 10000L, OsrsSkill.HERBLORE to 10000L, OsrsSkill.SMITHING to 10000L, OsrsSkill.FLETCHING to 10000L, OsrsSkill.MAGIC to 10000L), 40000L, "Shadow Grimoire", "📜", "item_magic_logs"),
        OsrsQuest("quest_perilous_moons", "Ancient Spirit Tombs", "🌙", QuestDifficulty.INTERMEDIATE, 65, OsrsSkill.HUNTER, 48, emptyList(), "Explore Ancient Nagua Tombs and slay Eclipse, Blood, and Blue Moon Spirit Lords!", listOf(QuestRequirementItem("item_bar_mithril", "Mithril Bar", "🛡️", 2), QuestRequirementItem("item_lobster", "Cooked Lobster", "🦞", 3), QuestRequirementItem("item_prayer_potion", "Divinity Nectar", "🧪", 1)), 2, mapOf(OsrsSkill.HUNTER to 10000L, OsrsSkill.FISHING to 10000L, OsrsSkill.SLAYER to 10000L), 30000L, "Dual Macuahuitl", "🪵", "item_dragon_bones"),
        OsrsQuest("quest_defenders_varrock", "Defenders of Royal Citadel", "🛡️", QuestDifficulty.INTERMEDIATE, 55, OsrsSkill.SMITHING, 55, emptyList(), "Defend Royal Citadel against Zombie Army Invasion!", listOf(QuestRequirementItem("item_bar_adamant", "Adamant Bar", "🛡️", 2), QuestRequirementItem("item_swordfish", "Cooked Swordfish", "🐟", 2)), 2, mapOf(OsrsSkill.SMITHING to 15000L, OsrsSkill.HUNTER to 10000L), 25000L, "Zombie Axe", "🪓", "item_adamant_bar")
    ).sortedBy { it.name }

    val ALL_QUESTS: List<OsrsQuest> by lazy {
        (QUESTS + TrainerLeagueData.ALL_TRAINER_QUESTS).sortedBy { it.name }
    }

    fun findQuestById(id: String): OsrsQuest? {
        return ALL_QUESTS.find { it.id == id }
    }
}
