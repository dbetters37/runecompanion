package com.example.data.models

data class SummonableAnimal(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val levelRequired: Int,
    val description: String,
    val requiredEffigies: Map<String, Int>, // map of effigy item ID to count
    val xpReward: Long,
    val durationSeconds: Int = 1200, // 20 minutes default
    val benefitText: String,
    val runesMultiplier: Float = 1.0f,
    val expeditionTimeReductionPercent: Int = 0,
    val questTimeReductionPercent: Int = 0,
    val skillingXpBonusPercent: Int = 0,
    val petMoodBonus: Int = 0,
    val extraIncantationSlots: Int = 0
)

data class ActiveSummoningCompanion(
    val animalId: String,
    val animalName: String,
    val iconEmoji: String,
    val benefitText: String,
    val startTimeMillis: Long,
    val durationSeconds: Int = 1200,
    val remainingSeconds: Int,
    val runesMultiplier: Float = 1.0f,
    val expeditionTimeReductionPercent: Int = 0,
    val questTimeReductionPercent: Int = 0,
    val skillingXpBonusPercent: Int = 0,
    val extraIncantationSlots: Int = 0
)

data class EffigyRequirement(
    val effigyItemId: String,
    val quantity: Int
)

data class GolemTier(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val levelRequired: Int,
    val description: String,
    val requiredEffigiesMap: Map<String, Int>,
    val xpReward: Long,
    val workDurationSeconds: Int = 1200,
    val tierNumber: Int,
    val totemItemId: String = "item_totem_$id"
) {
    val reqLevel: Int get() = levelRequired
    val durationMinutes: Int get() = workDurationSeconds / 60
    val requiredEffigies: List<EffigyRequirement> get() = requiredEffigiesMap.map { EffigyRequirement(it.key, it.value) }
}

data class GolemHarvestedReward(
    val itemId: String,
    val itemName: String,
    val itemEmoji: String,
    val quantity: Int
) {
    val name: String get() = itemName
    val iconEmoji: String get() = itemEmoji
}

data class GolemTaskOption(
    val id: String,
    val skill: OsrsSkill,
    val name: String,
    val emoji: String,
    val description: String,
    val levelReq: Int = 1,
    val subOptionName: String = "",
    val subOptionId: String = ""
)

data class ActiveGolemState(
    val golemId: String,
    val golemName: String,
    val iconEmoji: String,
    val totalDurationSeconds: Int,
    val remainingSeconds: Int,
    val isWorking: Boolean = false, // Timer stays paused until assigned to an activity!
    val assignedActivityId: String? = null,
    val assignedActivityName: String? = null,
    val assignedActivityEmoji: String? = null,
    val assignedActivitySubOption: String? = null,
    val assignedSkill: OsrsSkill? = null,
    val startTimeMs: Long = 0L,
    val lastTickTimeMs: Long = 0L,
    val completedActions: Int = 0,
    val accumulatedXp: Long = 0L,
    val accumulatedGp: Long = 0L,
    val accumulatedLoot: List<GolemHarvestedReward> = emptyList(),
    val isCompleted: Boolean = false
) {
    val isAwaitingActivity: Boolean get() = !isWorking && !isCompleted
    val tierId: String get() = golemId
    val durationSeconds: Int get() = totalDurationSeconds
}

data class DruidEffigyRecipe(
    val effigyId: String,
    val effigyName: String,
    val emoji: String,
    val runeId: String,
    val runeName: String,
    val runeCount: Int = 1,
    val logId: String,
    val logName: String,
    val logCount: Int = 1,
    val nailId: String,
    val nailName: String,
    val nailCount: Int = 5,
    val levelReq: Int = 1,
    val xpReward: Long = 120L,
    val gpReward: Long = 35L,
    val description: String = ""
)

object SummoningData {
    val ALL_DRUID_EFFIGY_RECIPES = listOf(
        DruidEffigyRecipe(
            effigyId = "item_effigy_air",
            effigyName = "Air Effigy",
            emoji = "💨",
            runeId = "item_rune_air",
            runeName = "Air Rune",
            runeCount = 1,
            logId = "item_logs",
            logName = "Normal Logs",
            logCount = 1,
            nailId = "item_nails",
            nailName = "Bronze Nails",
            nailCount = 5,
            levelReq = 1,
            xpReward = 120L,
            gpReward = 30L,
            description = "Fuses an Air Rune with Normal Logs and Bronze Nails into a brisk Air Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_mind",
            effigyName = "Mind Effigy",
            emoji = "🧠",
            runeId = "item_rune_mind",
            runeName = "Mind Rune",
            runeCount = 1,
            logId = "item_oak_logs",
            logName = "Oak Logs",
            logCount = 1,
            nailId = "item_iron_nails",
            nailName = "Iron Nails",
            nailCount = 5,
            levelReq = 5,
            xpReward = 150L,
            gpReward = 40L,
            description = "Fuses a Mind Rune with Oak Logs and Iron Nails into a contemplative Mind Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_water",
            effigyName = "Water Effigy",
            emoji = "💧",
            runeId = "item_rune_water",
            runeName = "Water Rune",
            runeCount = 1,
            logId = "item_willow_logs",
            logName = "Willow Logs",
            logCount = 1,
            nailId = "item_steel_nails",
            nailName = "Steel Nails",
            nailCount = 5,
            levelReq = 10,
            xpReward = 180L,
            gpReward = 50L,
            description = "Fuses a Water Rune with Willow Logs and Steel Nails into a flowing Water Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_earth",
            effigyName = "Earth Effigy",
            emoji = "🪨",
            runeId = "item_rune_earth",
            runeName = "Earth Rune",
            runeCount = 1,
            logId = "item_oak_logs",
            logName = "Oak Logs",
            logCount = 1,
            nailId = "item_iron_nails",
            nailName = "Iron Nails",
            nailCount = 5,
            levelReq = 15,
            xpReward = 210L,
            gpReward = 60L,
            description = "Fuses an Earth Rune with Oak Logs and Iron Nails into a grounded Earth Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_fire",
            effigyName = "Fire Effigy",
            emoji = "🔥",
            runeId = "item_rune_fire",
            runeName = "Fire Rune",
            runeCount = 1,
            logId = "item_willow_logs",
            logName = "Willow Logs",
            logCount = 1,
            nailId = "item_steel_nails",
            nailName = "Steel Nails",
            nailCount = 5,
            levelReq = 20,
            xpReward = 250L,
            gpReward = 70L,
            description = "Fuses a Fire Rune with Willow Logs and Steel Nails into a radiant Fire Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_body",
            effigyName = "Body Effigy",
            emoji = "🛡️",
            runeId = "item_rune_body",
            runeName = "Body Rune",
            runeCount = 1,
            logId = "item_willow_logs",
            logName = "Willow Logs",
            logCount = 1,
            nailId = "item_steel_nails",
            nailName = "Steel Nails",
            nailCount = 5,
            levelReq = 25,
            xpReward = 290L,
            gpReward = 80L,
            description = "Fuses a Body Rune with Willow Logs and Steel Nails into a resilient Body Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_cosmic",
            effigyName = "Cosmic Effigy",
            emoji = "🌌",
            runeId = "item_rune_cosmic",
            runeName = "Cosmic Rune",
            runeCount = 1,
            logId = "item_maple_logs",
            logName = "Maple Logs",
            logCount = 1,
            nailId = "item_mithril_nails",
            nailName = "Mithril Nails",
            nailCount = 5,
            levelReq = 32,
            xpReward = 340L,
            gpReward = 100L,
            description = "Fuses a Cosmic Rune with Maple Logs and Mithril Nails into a celestial Cosmic Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_chaos",
            effigyName = "Chaos Effigy",
            emoji = "💥",
            runeId = "item_rune_chaos",
            runeName = "Chaos Rune",
            runeCount = 1,
            logId = "item_maple_logs",
            logName = "Maple Logs",
            logCount = 1,
            nailId = "item_mithril_nails",
            nailName = "Mithril Nails",
            nailCount = 5,
            levelReq = 40,
            xpReward = 400L,
            gpReward = 120L,
            description = "Fuses a Chaos Rune with Maple Logs and Mithril Nails into a volatile Chaos Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_nature",
            effigyName = "Nature Effigy",
            emoji = "🌿",
            runeId = "item_rune_nature",
            runeName = "Nature Rune",
            runeCount = 1,
            logId = "item_yew_logs",
            logName = "Yew Logs",
            logCount = 1,
            nailId = "item_mithril_nails",
            nailName = "Mithril Nails",
            nailCount = 5,
            levelReq = 48,
            xpReward = 470L,
            gpReward = 150L,
            description = "Fuses a Nature Rune with Yew Logs and Mithril Nails into an organic Nature Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_law",
            effigyName = "Law Effigy",
            emoji = "⚖️",
            runeId = "item_rune_law",
            runeName = "Law Rune",
            runeCount = 1,
            logId = "item_yew_logs",
            logName = "Yew Logs",
            logCount = 1,
            nailId = "item_adamant_nails",
            nailName = "Adamant Nails",
            nailCount = 5,
            levelReq = 56,
            xpReward = 550L,
            gpReward = 180L,
            description = "Fuses a Law Rune with Yew Logs and Adamant Nails into an orderly Law Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_death",
            effigyName = "Death Effigy",
            emoji = "💀",
            runeId = "item_rune_death",
            runeName = "Death Rune",
            runeCount = 1,
            logId = "item_yew_logs",
            logName = "Yew Logs",
            logCount = 1,
            nailId = "item_adamant_nails",
            nailName = "Adamant Nails",
            nailCount = 5,
            levelReq = 65,
            xpReward = 640L,
            gpReward = 220L,
            description = "Fuses a Death Rune with Yew Logs and Adamant Nails into a dark Death Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_astral",
            effigyName = "Astral Effigy",
            emoji = "✨",
            runeId = "item_rune_astral",
            runeName = "Astral Rune",
            runeCount = 1,
            logId = "item_yew_logs",
            logName = "Yew Logs",
            logCount = 1,
            nailId = "item_adamant_nails",
            nailName = "Adamant Nails",
            nailCount = 5,
            levelReq = 72,
            xpReward = 740L,
            gpReward = 260L,
            description = "Fuses an Astral Rune with Yew Logs and Adamant Nails into an ethereal Astral Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_blood",
            effigyName = "Blood Effigy",
            emoji = "🩸",
            runeId = "item_rune_blood",
            runeName = "Blood Rune",
            runeCount = 1,
            logId = "item_redwood_logs",
            logName = "Redwood Logs",
            logCount = 1,
            nailId = "item_rune_nails",
            nailName = "Rune Nails",
            nailCount = 5,
            levelReq = 80,
            xpReward = 850L,
            gpReward = 320L,
            description = "Fuses a Blood Rune with Redwood Logs and Rune Nails into a vital Blood Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_soul",
            effigyName = "Soul Effigy",
            emoji = "👻",
            runeId = "item_rune_soul",
            runeName = "Soul Rune",
            runeCount = 1,
            logId = "item_redwood_logs",
            logName = "Redwood Logs",
            logCount = 1,
            nailId = "item_rune_nails",
            nailName = "Rune Nails",
            nailCount = 5,
            levelReq = 88,
            xpReward = 980L,
            gpReward = 400L,
            description = "Fuses a Soul Rune with Redwood Logs and Rune Nails into an eternal Soul Effigy."
        ),
        DruidEffigyRecipe(
            effigyId = "item_effigy_wrath",
            effigyName = "Wrath Effigy",
            emoji = "⚡",
            runeId = "item_rune_wrath",
            runeName = "Wrath Rune",
            runeCount = 1,
            logId = "item_redwood_logs",
            logName = "Redwood Logs",
            logCount = 1,
            nailId = "item_rune_nails",
            nailName = "Rune Nails",
            nailCount = 5,
            levelReq = 95,
            xpReward = 1150L,
            gpReward = 500L,
            description = "Fuses a Wrath Rune with Redwood Logs and Rune Nails into a devastating Wrath Effigy."
        )
    )

    fun getEffigyRecipe(effigyId: String): DruidEffigyRecipe {
        return ALL_DRUID_EFFIGY_RECIPES.find { it.effigyId == effigyId } ?: ALL_DRUID_EFFIGY_RECIPES.first()
    }

    fun getEffigyRecipeByRune(runeId: String): DruidEffigyRecipe {
        return ALL_DRUID_EFFIGY_RECIPES.find { it.runeId == runeId } ?: ALL_DRUID_EFFIGY_RECIPES.first()
    }

    val RUNE_TO_EFFIGY_MAP = mapOf(
        "item_rune_air" to "item_effigy_air",
        "item_rune_mind" to "item_effigy_mind",
        "item_rune_water" to "item_effigy_water",
        "item_rune_earth" to "item_effigy_earth",
        "item_rune_fire" to "item_effigy_fire",
        "item_rune_body" to "item_effigy_body",
        "item_rune_cosmic" to "item_effigy_cosmic",
        "item_rune_chaos" to "item_effigy_chaos",
        "item_rune_nature" to "item_effigy_nature",
        "item_rune_law" to "item_effigy_law",
        "item_rune_death" to "item_effigy_death",
        "item_rune_astral" to "item_effigy_astral",
        "item_rune_blood" to "item_effigy_blood",
        "item_rune_soul" to "item_effigy_soul",
        "item_rune_wrath" to "item_effigy_wrath"
    )

    val EFFIGY_ITEMS = listOf(
        InventoryItem("item_effigy_air", "Air Effigy", ItemCategory.MISC, "💨", "Infused Air Effigy used to invoke spirit companions.", 50L),
        InventoryItem("item_effigy_mind", "Mind Effigy", ItemCategory.MISC, "🧠", "Infused Mind Effigy used to invoke spirit companions.", 60L),
        InventoryItem("item_effigy_water", "Water Effigy", ItemCategory.MISC, "💧", "Infused Water Effigy used to invoke spirit companions.", 50L),
        InventoryItem("item_effigy_earth", "Earth Effigy", ItemCategory.MISC, "🪨", "Infused Earth Effigy used to invoke spirit companions.", 50L),
        InventoryItem("item_effigy_fire", "Fire Effigy", ItemCategory.MISC, "🔥", "Infused Fire Effigy used to invoke spirit companions.", 60L),
        InventoryItem("item_effigy_body", "Body Effigy", ItemCategory.MISC, "🛡️", "Infused Body Effigy used to invoke spirit companions.", 80L),
        InventoryItem("item_effigy_cosmic", "Cosmic Effigy", ItemCategory.MISC, "🌌", "Infused Cosmic Effigy used to invoke spirit companions.", 150L),
        InventoryItem("item_effigy_chaos", "Chaos Effigy", ItemCategory.MISC, "💥", "Infused Chaos Effigy used to invoke spirit companions.", 200L),
        InventoryItem("item_effigy_nature", "Nature Effigy", ItemCategory.MISC, "🌿", "Infused Nature Effigy used to invoke spirit companions.", 300L),
        InventoryItem("item_effigy_law", "Law Effigy", ItemCategory.MISC, "⚖️", "Infused Law Effigy used to invoke spirit companions.", 350L),
        InventoryItem("item_effigy_death", "Death Effigy", ItemCategory.MISC, "💀", "Infused Death Effigy used to invoke spirit companions.", 400L),
        InventoryItem("item_effigy_astral", "Astral Effigy", ItemCategory.MISC, "✨", "Infused Astral Effigy used to invoke spirit companions.", 300L),
        InventoryItem("item_effigy_blood", "Blood Effigy", ItemCategory.MISC, "🩸", "Infused Blood Effigy used to invoke spirit companions.", 500L),
        InventoryItem("item_effigy_soul", "Soul Effigy", ItemCategory.MISC, "👻", "Infused Soul Effigy used to invoke spirit companions.", 500L),
        InventoryItem("item_effigy_wrath", "Wrath Effigy", ItemCategory.MISC, "⚡", "Infused Wrath Effigy used to invoke spirit companions.", 600L)
    )

    val TOTEM_ITEMS by lazy {
        ALL_ANIMALS.map { animal ->
            val durationMin = animal.durationSeconds / 60
            InventoryItem(
                id = "item_totem_${animal.id}",
                name = "${animal.name} Totem",
                category = ItemCategory.MISC,
                iconEmoji = "🗿",
                description = "Crafted spirit totem. Activate this totem from Inventory or Summoning Tab to invoke ${animal.name}'s ${durationMin}-min spirit effects: ${animal.benefitText}.",
                costGp = animal.xpReward * 2
            )
        }
    }

    val ALL_ANIMALS = listOf(
        SummonableAnimal(
            id = "summon_dreadfowl",
            name = "Dreadfowl",
            iconEmoji = "🐓",
            levelRequired = 1,
            description = "Feathered fowl familiar that boosts elemental rune gathering.",
            requiredEffigies = mapOf("item_effigy_air" to 1),
            xpReward = 150L,
            benefitText = "1.2x Runes Gathered",
            runesMultiplier = 1.2f
        ),
        SummonableAnimal(
            id = "summon_spirit_wolf",
            name = "Spirit Wolf",
            iconEmoji = "🐺",
            levelRequired = 4,
            description = "Loyal wolf spirit that speeds up quest completions.",
            requiredEffigies = mapOf("item_effigy_mind" to 1, "item_effigy_air" to 1),
            xpReward = 300L,
            benefitText = "-5% Quest Time",
            questTimeReductionPercent = 5
        ),
        SummonableAnimal(
            id = "summon_desert_wyrm",
            name = "Desert Wyrm",
            iconEmoji = "🪱",
            levelRequired = 7,
            description = "Subterranean wyrm that tunnels through expedition routes.",
            requiredEffigies = mapOf("item_effigy_earth" to 1),
            xpReward = 450L,
            benefitText = "-5% Expedition Time",
            expeditionTimeReductionPercent = 5
        ),
        SummonableAnimal(
            id = "summon_vampyre_bat",
            name = "Vampyre Bat",
            iconEmoji = "🦇",
            levelRequired = 10,
            description = "Night creature that increases trickery and combat focus.",
            requiredEffigies = mapOf("item_effigy_water" to 1),
            xpReward = 600L,
            benefitText = "+5% Skilling XP",
            skillingXpBonusPercent = 5
        ),
        SummonableAnimal(
            id = "summon_abyssal_parasite",
            name = "Abyssal Parasite",
            iconEmoji = "🦠",
            levelRequired = 13,
            description = "Strange void organism that multiplies harvested runes.",
            requiredEffigies = mapOf("item_effigy_body" to 1, "item_effigy_water" to 1),
            xpReward = 800L,
            benefitText = "1.3x Runes Gathered",
            runesMultiplier = 1.3f
        ),
        SummonableAnimal(
            id = "summon_spirit_spider",
            name = "Spirit Spider",
            iconEmoji = "🕷️",
            levelRequired = 16,
            description = "Eight-legged spirit weaver that hastens quests.",
            requiredEffigies = mapOf("item_effigy_fire" to 1),
            xpReward = 1000L,
            benefitText = "-10% Quest Time",
            questTimeReductionPercent = 10
        ),
        SummonableAnimal(
            id = "summon_granite_crab",
            name = "Granite Crab",
            iconEmoji = "🦀",
            levelRequired = 19,
            description = "Heavy stone crab that enhances mining & forging prowess.",
            requiredEffigies = mapOf("item_effigy_cosmic" to 1, "item_effigy_earth" to 1),
            xpReward = 1300L,
            benefitText = "+10% Skilling XP",
            skillingXpBonusPercent = 10
        ),
        SummonableAnimal(
            id = "summon_spirit_owl",
            name = "Spirit Owl",
            iconEmoji = "🦉",
            levelRequired = 20,
            description = "Wise celestial spirit owl that grants an extra Incantation Slot when invoked.",
            requiredEffigies = mapOf("item_effigy_mind" to 1, "item_effigy_cosmic" to 1),
            xpReward = 1450L,
            durationSeconds = 1800, // 30 minutes
            benefitText = "+1 Incantation Slot",
            extraIncantationSlots = 1
        ),
        SummonableAnimal(
            id = "summon_spirit_mosquito",
            name = "Spirit Mosquito",
            iconEmoji = "🦟",
            levelRequired = 22,
            description = "Swift flying pest that cuts down travel time on expeditions.",
            requiredEffigies = mapOf("item_effigy_chaos" to 1),
            xpReward = 1600L,
            benefitText = "-10% Expedition Time",
            expeditionTimeReductionPercent = 10
        ),
        SummonableAnimal(
            id = "summon_spirit_bull",
            name = "Spirit Bull",
            iconEmoji = "🐂",
            levelRequired = 25,
            description = "Mighty bovine beast that multiplies runic essences.",
            requiredEffigies = mapOf("item_effigy_nature" to 1),
            xpReward = 2000L,
            benefitText = "1.5x Runes Gathered",
            runesMultiplier = 1.5f
        ),
        SummonableAnimal(
            id = "summon_compy_familiar",
            name = "Compy Familiar",
            iconEmoji = "🦖",
            levelRequired = 28,
            description = "Tiny agile dinosaur that aids in quick quest execution.",
            requiredEffigies = mapOf("item_effigy_law" to 1),
            xpReward = 2500L,
            benefitText = "-15% Quest Time",
            questTimeReductionPercent = 15
        ),
        SummonableAnimal(
            id = "summon_pyrelord",
            name = "Pyrelord",
            iconEmoji = "🔥",
            levelRequired = 31,
            description = "Fiery elemental lord that warms companion mood and grants skilling XP.",
            requiredEffigies = mapOf("item_effigy_fire" to 1, "item_effigy_air" to 1),
            xpReward = 3000L,
            benefitText = "+15% Skilling XP",
            skillingXpBonusPercent = 15
        ),
        SummonableAnimal(
            id = "summon_magpie_familiar",
            name = "Magpie Familiar",
            iconEmoji = "🐦",
            levelRequired = 34,
            description = "Shining bird familiar with a knack for discovering bonus runes.",
            requiredEffigies = mapOf("item_effigy_cosmic" to 1, "item_effigy_nature" to 1),
            xpReward = 3600L,
            benefitText = "1.6x Runes Gathered",
            runesMultiplier = 1.6f
        ),
        SummonableAnimal(
            id = "summon_spirit_terrorbird",
            name = "Spirit Terrorbird",
            iconEmoji = "🦤",
            levelRequired = 37,
            description = "High-speed sprinting avian that slashes expedition durations.",
            requiredEffigies = mapOf("item_effigy_body" to 1, "item_effigy_mind" to 1),
            xpReward = 4200L,
            benefitText = "-15% Expedition Time",
            expeditionTimeReductionPercent = 15
        ),
        SummonableAnimal(
            id = "summon_ibis_familiar",
            name = "Ibis Familiar",
            iconEmoji = "🦩",
            levelRequired = 40,
            description = "Sacred river wading bird that improves overall skilling rewards.",
            requiredEffigies = mapOf("item_effigy_water" to 1, "item_effigy_astral" to 1),
            xpReward = 5000L,
            benefitText = "+15% Skilling XP",
            skillingXpBonusPercent = 15
        ),
        SummonableAnimal(
            id = "summon_spirit_jelly",
            name = "Spirit Jelly",
            iconEmoji = "🪼",
            levelRequired = 43,
            description = "Translucent gelatinous entity that keeps pet companion mood at 100%.",
            requiredEffigies = mapOf("item_effigy_death" to 1),
            xpReward = 6000L,
            benefitText = "+25% Pet Mood Boost",
            petMoodBonus = 25
        ),
        SummonableAnimal(
            id = "summon_spirit_larupia",
            name = "Spirit Larupia",
            iconEmoji = "🐆",
            levelRequired = 47,
            description = "Fierce feline predator that cuts down quest travel time.",
            requiredEffigies = mapOf("item_effigy_chaos" to 1, "item_effigy_nature" to 1),
            xpReward = 7500L,
            benefitText = "-20% Quest Time",
            questTimeReductionPercent = 20
        ),
        SummonableAnimal(
            id = "summon_war_tortoise",
            name = "War Tortoise",
            iconEmoji = "🐢",
            levelRequired = 52,
            description = "Armored battle tortoise that provides heavy expedition protection.",
            requiredEffigies = mapOf("item_effigy_law" to 1, "item_effigy_body" to 1),
            xpReward = 9500L,
            benefitText = "-20% Expedition Time",
            expeditionTimeReductionPercent = 20
        ),
        SummonableAnimal(
            id = "summon_hydra_familiar",
            name = "Hydra Familiar",
            iconEmoji = "🐉",
            levelRequired = 56,
            description = "Multi-headed mythical dragon beast that dramatically boosts runes gathered.",
            requiredEffigies = mapOf("item_effigy_blood" to 1),
            xpReward = 12000L,
            benefitText = "1.8x Runes Gathered",
            runesMultiplier = 1.8f
        ),
        SummonableAnimal(
            id = "summon_spirit_graahk",
            name = "Spirit Graahk",
            iconEmoji = "🦏",
            levelRequired = 61,
            description = "Wild horned graahk familiar that charges through quest paths.",
            requiredEffigies = mapOf("item_effigy_nature" to 1, "item_effigy_astral" to 1),
            xpReward = 15000L,
            benefitText = "-25% Quest Time",
            questTimeReductionPercent = 25
        ),
        SummonableAnimal(
            id = "summon_spirit_kyatt",
            name = "Spirit Kyatt",
            iconEmoji = "🐅",
            levelRequired = 65,
            description = "Saber-toothed hunting beast that speeds up expedition timers.",
            requiredEffigies = mapOf("item_effigy_cosmic" to 1, "item_effigy_law" to 1),
            xpReward = 18000L,
            benefitText = "-25% Expedition Time",
            expeditionTimeReductionPercent = 25
        ),
        SummonableAnimal(
            id = "summon_bunyip",
            name = "Bunyip",
            iconEmoji = "🐊",
            levelRequired = 68,
            description = "Mystical water spirit that offers constant healing energy and 2.0x runes.",
            requiredEffigies = mapOf("item_effigy_water" to 1, "item_effigy_soul" to 1),
            xpReward = 22000L,
            benefitText = "2.0x Runes Gathered",
            runesMultiplier = 2.0f
        ),
        SummonableAnimal(
            id = "summon_fruit_bat",
            name = "Fruit Bat",
            iconEmoji = "🦇",
            levelRequired = 72,
            description = "Generous bat companion that grants +25% skilling XP bonus.",
            requiredEffigies = mapOf("item_effigy_nature" to 1, "item_effigy_mind" to 1),
            xpReward = 26000L,
            benefitText = "+25% Skilling XP",
            skillingXpBonusPercent = 25
        ),
        SummonableAnimal(
            id = "summon_ravenous_locust",
            name = "Ravenous Locust",
            iconEmoji = "🦗",
            levelRequired = 76,
            description = "Swarm entity that slashes quest durations by 25%.",
            requiredEffigies = mapOf("item_effigy_death" to 1, "item_effigy_chaos" to 1),
            xpReward = 31000L,
            benefitText = "-25% Quest Time",
            questTimeReductionPercent = 25
        ),
        SummonableAnimal(
            id = "summon_titan_of_earth",
            name = "Titan of Earth",
            iconEmoji = "🗿",
            levelRequired = 80,
            description = "Monolithic stone giant that crushes expedition delay timers.",
            requiredEffigies = mapOf("item_effigy_earth" to 1, "item_effigy_soul" to 1),
            xpReward = 38000L,
            benefitText = "-25% Expedition Time",
            expeditionTimeReductionPercent = 25
        ),
        SummonableAnimal(
            id = "summon_swamp_titan",
            name = "Swamp Titan",
            iconEmoji = "🐸",
            levelRequired = 85,
            description = "Ancient bog behemoth that yields double (2.2x) runes gathered.",
            requiredEffigies = mapOf("item_effigy_nature" to 1, "item_effigy_wrath" to 1),
            xpReward = 45000L,
            benefitText = "2.2x Runes Gathered",
            runesMultiplier = 2.2f
        ),
        SummonableAnimal(
            id = "summon_unicorn_stallion",
            name = "Unicorn Stallion",
            iconEmoji = "🦄",
            levelRequired = 88,
            description = "Pure divine unicorn that restores complete pet mood & +30% skilling XP.",
            requiredEffigies = mapOf("item_effigy_astral" to 1, "item_effigy_soul" to 1),
            xpReward = 52000L,
            benefitText = "+30% Skilling XP & Full Mood",
            skillingXpBonusPercent = 30,
            petMoodBonus = 100
        ),
        SummonableAnimal(
            id = "summon_pack_yak",
            name = "Pack Yak",
            iconEmoji = "🦬",
            levelRequired = 92,
            description = "Ultimate beast of burden! Slashes both expedition and quest times by 30%.",
            requiredEffigies = mapOf("item_effigy_law" to 2, "item_effigy_body" to 2),
            xpReward = 65000L,
            benefitText = "-30% Expedition & Quest Time",
            expeditionTimeReductionPercent = 30,
            questTimeReductionPercent = 30
        ),
        SummonableAnimal(
            id = "summon_steel_titan",
            name = "Steel Titan",
            iconEmoji = "🤖",
            levelRequired = 95,
            description = "Colossal metal juggernaut granting 2.5x runes gathered & heavy skilling power.",
            requiredEffigies = mapOf("item_effigy_death" to 2, "item_effigy_wrath" to 2),
            xpReward = 85000L,
            benefitText = "2.5x Runes Gathered",
            runesMultiplier = 2.5f
        ),
        SummonableAnimal(
            id = "summon_dragon_familiar",
            name = "Dragon Familiar",
            iconEmoji = "🐲",
            levelRequired = 97,
            description = "Legendary ancient dragon that reduces quest & expedition times by 35%.",
            requiredEffigies = mapOf("item_effigy_blood" to 2, "item_effigy_soul" to 2),
            xpReward = 100000L,
            benefitText = "-35% Expedition & Quest Time",
            expeditionTimeReductionPercent = 35,
            questTimeReductionPercent = 35
        ),
        SummonableAnimal(
            id = "summon_phoenix_spirit",
            name = "Phoenix Spirit",
            iconEmoji = "🦅",
            levelRequired = 99,
            description = "Immortal fiery spirit bird! Grants 3.0x Runes Gathered & ultimate spirit aura.",
            requiredEffigies = mapOf("item_effigy_wrath" to 2, "item_effigy_soul" to 2),
            xpReward = 150000L,
            benefitText = "3.0x Runes Gathered",
            runesMultiplier = 3.0f
        )
    )

    val GOLEM_TIERS = listOf(
        GolemTier(
            id = "golem_clay",
            name = "Clay Golem",
            iconEmoji = "🗿",
            levelRequired = 30,
            description = "Awakened earthen clay construct. Assigned to perform a secondary AFK gathering or crafting activity in your place for 20 minutes.",
            requiredEffigiesMap = mapOf("item_effigy_earth" to 2, "item_effigy_body" to 1),
            xpReward = 3500L,
            workDurationSeconds = 20 * 60, // 20 min
            tierNumber = 1
        ),
        GolemTier(
            id = "golem_stone",
            name = "Stone Golem",
            iconEmoji = "🪨",
            levelRequired = 60,
            description = "Reinforced stone golem chiseled with cosmic and law runes. Works secondary AFK tasks with increased stamina for 35 minutes.",
            requiredEffigiesMap = mapOf("item_effigy_earth" to 3, "item_effigy_cosmic" to 2, "item_effigy_law" to 1),
            xpReward = 16000L,
            workDurationSeconds = 35 * 60, // 35 min
            tierNumber = 2
        ),
        GolemTier(
            id = "golem_crystal",
            name = "Crystal Golem",
            iconEmoji = "💎",
            levelRequired = 80,
            description = "Luminescent crystalline titan infused with astral and soul forces. Gathers and crafts secondary AFK tasks tirelessly for 50 minutes.",
            requiredEffigiesMap = mapOf("item_effigy_earth" to 4, "item_effigy_soul" to 2, "item_effigy_astral" to 2),
            xpReward = 45000L,
            workDurationSeconds = 50 * 60, // 50 min
            tierNumber = 3
        ),
        GolemTier(
            id = "golem_ancient",
            name = "Ancient Titan Golem",
            iconEmoji = "🤖",
            levelRequired = 95,
            description = "Epoch-forged primordial colossus fueled by wrath and soul effigies. Executes secondary AFK tasks with unmatched mastery for 75 minutes.",
            requiredEffigiesMap = mapOf("item_effigy_earth" to 5, "item_effigy_wrath" to 3, "item_effigy_soul" to 3),
            xpReward = 95000L,
            workDurationSeconds = 75 * 60, // 75 min
            tierNumber = 4
        )
    )

    val GOLEM_TOTEM_ITEMS by lazy {
        GOLEM_TIERS.map { golem ->
            val durationMin = golem.workDurationSeconds / 60
            InventoryItem(
                id = "item_totem_${golem.id}",
                name = "${golem.name} Totem",
                category = ItemCategory.MISC,
                iconEmoji = golem.iconEmoji,
                description = "Totem to invoke a ${golem.name} (Lvl ${golem.levelRequired} Summoning). When activated, the ${durationMin}-min timer remains paused until assigned to an AFK activity to work in your place.",
                costGp = golem.xpReward * 2
            )
        }
    }

    val GOLEM_TASKS = listOf(
        GolemTaskOption(
            id = "woodcutting_sylvan",
            skill = OsrsSkill.WOODCUTTING,
            name = "Woodcutting: Sylvan Woods",
            emoji = "🪓",
            description = "Chops Regular & Oak Logs with bonus bird nests.",
            levelReq = 1,
            subOptionName = "Sylvan Woods",
            subOptionId = "forest_sylvan"
        ),
        GolemTaskOption(
            id = "woodcutting_pines",
            skill = OsrsSkill.WOODCUTTING,
            name = "Woodcutting: Whispering Pines",
            emoji = "🪓",
            description = "Harvests Willow & Maple Logs.",
            levelReq = 30,
            subOptionName = "Whispering Pines",
            subOptionId = "forest_pines"
        ),
        GolemTaskOption(
            id = "woodcutting_canopy",
            skill = OsrsSkill.WOODCUTTING,
            name = "Woodcutting: Ancient Canopy",
            emoji = "🪓",
            description = "Harvests Yew, Magic & Redwood Logs.",
            levelReq = 60,
            subOptionName = "Ancient Canopy",
            subOptionId = "forest_canopy"
        ),
        GolemTaskOption(
            id = "mining_surface",
            skill = OsrsSkill.SMITHING,
            name = "Mining: Surface Quarry",
            emoji = "⛏️",
            description = "Extracts Copper, Tin, Iron & Coal Ores.",
            levelReq = 1,
            subOptionName = "Surface Quarry",
            subOptionId = "quarry_surface"
        ),
        GolemTaskOption(
            id = "mining_deep",
            skill = OsrsSkill.SMITHING,
            name = "Mining: Deep Quarry",
            emoji = "⛏️",
            description = "Extracts Mithril, Adamant & Runite Ores.",
            levelReq = 55,
            subOptionName = "Deep Quarry",
            subOptionId = "quarry_deep"
        ),
        GolemTaskOption(
            id = "mining_gems",
            skill = OsrsSkill.SMITHING,
            name = "Mining: Gemstone Vein",
            emoji = "💎",
            description = "Unearths Sapphires, Emeralds, Rubies & Diamonds.",
            levelReq = 40,
            subOptionName = "Gemstone Vein",
            subOptionId = "quarry_gem"
        ),
        GolemTaskOption(
            id = "fishing_coastal",
            skill = OsrsSkill.FISHING,
            name = "Fishing: Coastal Waters",
            emoji = "🎣",
            description = "Catches Raw Shrimps, Trout & Salmon.",
            levelReq = 1,
            subOptionName = "Coastal Waters",
            subOptionId = "area_coastal"
        ),
        GolemTaskOption(
            id = "fishing_deep",
            skill = OsrsSkill.FISHING,
            name = "Fishing: Deep Ocean",
            emoji = "🎣",
            description = "Nets Raw Lobsters, Swordfish & Sharks.",
            levelReq = 40,
            subOptionName = "Deep Ocean",
            subOptionId = "area_deep"
        ),
        GolemTaskOption(
            id = "thieving_pickpocket",
            skill = OsrsSkill.THIEVING,
            name = "Thieving: Master Pickpocket",
            emoji = "💰",
            description = "Steals GP, lockpicks, gems, seeds and pouches.",
            levelReq = 1,
            subOptionName = "Town Citizens",
            subOptionId = "pickpocket"
        ),
        GolemTaskOption(
            id = "cooking_fish",
            skill = OsrsSkill.COOKING,
            name = "Cooking: Master Hearth",
            emoji = "🍳",
            description = "Cooks savory fish and seafood delicacies.",
            levelReq = 1,
            subOptionName = "Master Hearth",
            subOptionId = "cooking_fish"
        ),
        GolemTaskOption(
            id = "campfire_logs",
            skill = OsrsSkill.FIREMAKING,
            name = "Firemaking: Campfire Ritual",
            emoji = "🔥",
            description = "Tends glowing pyres and generates Firemaking XP + Ashes.",
            levelReq = 1,
            subOptionName = "Campfire Pyre",
            subOptionId = "campfire_logs"
        ),
        GolemTaskOption(
            id = "smelting_bars",
            skill = OsrsSkill.SMITHING,
            name = "Smelting: Foundry Furnace",
            emoji = "🔥",
            description = "Smelts refined Bronze, Iron, Steel, Mithril and Adamant bars.",
            levelReq = 1,
            subOptionName = "Foundry Furnace",
            subOptionId = "smelting_bars"
        ),
        GolemTaskOption(
            id = "fletching_arrows",
            skill = OsrsSkill.FLETCHING,
            name = "Fletching: Fletcher Bench",
            emoji = "🏹",
            description = "Carves Arrow Shafts, Bows and Flighted Arrows.",
            levelReq = 1,
            subOptionName = "Fletcher Bench",
            subOptionId = "fletching_arrows"
        ),
        GolemTaskOption(
            id = "sawmill_planks",
            skill = OsrsSkill.WOODCUTTING,
            name = "Sawmill: Timber Milling",
            emoji = "🪵",
            description = "Processes logs into construction planks.",
            levelReq = 1,
            subOptionName = "Timber Mill",
            subOptionId = "sawmill_planks"
        ),
        GolemTaskOption(
            id = "runecrafting_altar",
            skill = OsrsSkill.RUNECRAFT,
            name = "Runecrafting: Runic Altar",
            emoji = "🔮",
            description = "Channels pure magical essence into elemental & high-tier Runes.",
            levelReq = 1,
            subOptionName = "Runic Altar",
            subOptionId = "runecrafting_altar"
        ),
        GolemTaskOption(
            id = "druid_altar",
            skill = OsrsSkill.FIREMAKING,
            name = "Summoning: Druid Altar Ritual",
            emoji = "🌿",
            description = "Forges magical Effigies from runes, timber and nails.",
            levelReq = 1,
            subOptionName = "Druid Altar",
            subOptionId = "druid_altar"
        ),
        GolemTaskOption(
            id = "catacombs_agility",
            skill = OsrsSkill.AGILITY,
            name = "Agility: Catacombs Vaults",
            emoji = "🏃",
            description = "Runs trap courses and retrieves dungeon relics and GP.",
            levelReq = 1,
            subOptionName = "Catacombs Vaults",
            subOptionId = "catacombs_agility"
        ),
        GolemTaskOption(
            id = "prayer_sanctification",
            skill = OsrsSkill.MAGIC,
            name = "Magic: Bone Sanctification",
            emoji = "🦴",
            description = "Blesses holy bones and communes with the divine.",
            levelReq = 1,
            subOptionName = "Holy Sanctum",
            subOptionId = "prayer_sanctification"
        )
    )

    fun getGolemTier(id: String): GolemTier {
        val cleanId = id.removePrefix("item_totem_")
        return GOLEM_TIERS.find { it.id == cleanId || it.id == id || it.totemItemId == id }
            ?: GOLEM_TIERS.first()
    }

    fun getGolemTask(id: String): GolemTaskOption? {
        return GOLEM_TASKS.find { it.id == id }
    }
}
