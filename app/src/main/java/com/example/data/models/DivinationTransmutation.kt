package com.example.data.models

enum class EnergyType(val displayName: String, val emoji: String, val colorHex: Long) {
    AIR("Air Energy", "💨", 0xFF81D4FA),
    WATER("Water Energy", "💧", 0xFF29B6F6),
    FIRE("Fire Energy", "🔥", 0xFFFF7043),
    NATURE("Nature Energy", "🌱", 0xFF66BB6A),
    LIGHT("Light Energy", "🌟", 0xFFFFCA28),
    DARK("Dark Energy", "🌑", 0xFFAB47BC)
}

data class EnergyCost(val type: EnergyType, val amount: Int)

data class EffigyRecipe(
    val skill: OsrsSkill,
    val effigyItemId: String,
    val effigyName: String,
    val emoji: String,
    val requiredEnergies: List<EnergyCost>,
    val xpReward: Long = 500L,
    val reqDivLevel: Int = 1,
    val tierLevel: Int = 1,
    val tierName: String = "Lesser",
    val craftDivXp: Long = 75L,
    val badgeColorHex: Long = 0xFF81C784
)

data class EffigyTier(
    val level: Int,
    val name: String,
    val reqDivLevel: Int,
    val costMultiplier: Int,
    val xpReward: Long,
    val craftDivXp: Long,
    val colorHex: Long
)

object DivinationTransmutation {

    fun getEnergyForItem(itemId: String, itemName: String, itemCategory: String = "", costGp: Long = 10L): Pair<EnergyType, Long> {
        val idLower = itemId.lowercase()
        val nameLower = itemName.lowercase()
        val catLower = itemCategory.lowercase()

        val energyType = when {
            idLower.contains("bone") || idLower.contains("slayer") || idLower.contains("dark") || 
            idLower.contains("corrupt") || idLower.contains("weapon") || idLower.contains("armor") || 
            nameLower.contains("bone") || nameLower.contains("weapon") -> EnergyType.DARK

            idLower.contains("gem") || idLower.contains("jewel") || idLower.contains("gold") || 
            idLower.contains("silver") || idLower.contains("light") || idLower.contains("blessing") || 
            nameLower.contains("diamond") || nameLower.contains("ruby") || nameLower.contains("ring") -> EnergyType.LIGHT

            idLower.contains("log") || idLower.contains("plank") || idLower.contains("seed") || 
            idLower.contains("herb") || idLower.contains("tree") || idLower.contains("feather") || 
            catLower.contains("wood") || catLower.contains("farm") -> EnergyType.NATURE

            idLower.contains("fish") || idLower.contains("water") || idLower.contains("bucket") || 
            idLower.contains("potion") || catLower.contains("potion") || catLower.contains("food") -> EnergyType.WATER

            idLower.contains("bar") || idLower.contains("ore") || idLower.contains("fire") || 
            idLower.contains("coal") || idLower.contains("ash") || idLower.contains("tinder") || 
            catLower.contains("bar") -> EnergyType.FIRE

            idLower.contains("arrow") || idLower.contains("bow") || idLower.contains("string") || 
            idLower.contains("air") || idLower.contains("dart") -> EnergyType.AIR

            else -> EnergyType.NATURE
        }

        val amount = when {
            costGp >= 1000L || idLower.contains("rune_bar") || idLower.contains("magic_log") || idLower.contains("dragon") || idLower.contains("diamond") -> 15L
            costGp >= 500L || idLower.contains("adamant") || idLower.contains("yew") || idLower.contains("shark") -> 10L
            costGp >= 100L || idLower.contains("mithril") || idLower.contains("maple") || idLower.contains("lobster") -> 6L
            costGp >= 30L || idLower.contains("steel") || idLower.contains("oak") || idLower.contains("iron") -> 3L
            else -> 2L
        }

        return Pair(energyType, amount)
    }

    fun getEnergyYieldDescription(item: InventoryItem): String {
        val (type, amount) = getEnergyForItem(item.id, item.name, item.category.name, item.costGp)
        return "✨ Divines into: +$amount ${type.emoji} ${type.displayName}"
    }

    val EFFIGY_TIERS = listOf(
        EffigyTier(1, "Lesser", 1, 1, 500L, 75L, 0xFF81C784),
        EffigyTier(2, "Common", 20, 2, 2000L, 250L, 0xFF64B5F6),
        EffigyTier(3, "Greater", 40, 4, 6000L, 600L, 0xFFBA68C8),
        EffigyTier(4, "Empowered", 60, 8, 18000L, 1500L, 0xFFFFB74D),
        EffigyTier(5, "Grand", 80, 15, 50000L, 4000L, 0xFFE57373),
        EffigyTier(6, "Elder", 95, 30, 150000L, 10000L, 0xFFFFD54F)
    )

    private data class BaseEffigySpec(
        val skill: OsrsSkill,
        val baseName: String,
        val emoji: String,
        val baseCosts: List<EnergyCost>
    )

    private val BASE_SPECS = listOf(
        BaseEffigySpec(OsrsSkill.WOODCUTTING, "Harvesting Effigy", "🪓", listOf(EnergyCost(EnergyType.NATURE, 4), EnergyCost(EnergyType.LIGHT, 2))),
        BaseEffigySpec(OsrsSkill.SMITHING, "Forging Effigy", "⚒️", listOf(EnergyCost(EnergyType.FIRE, 4), EnergyCost(EnergyType.DARK, 2))),
        BaseEffigySpec(OsrsSkill.FISHING, "Fishing Effigy", "🎣", listOf(EnergyCost(EnergyType.WATER, 4), EnergyCost(EnergyType.NATURE, 2))),
        BaseEffigySpec(OsrsSkill.COOKING, "Cooking Effigy", "🍳", listOf(EnergyCost(EnergyType.FIRE, 3), EnergyCost(EnergyType.WATER, 3))),
        BaseEffigySpec(OsrsSkill.FIREMAKING, "Summoning Effigy", "🐺", listOf(EnergyCost(EnergyType.FIRE, 4), EnergyCost(EnergyType.NATURE, 2))),
        BaseEffigySpec(OsrsSkill.HERBLORE, "Herbalism Effigy", "🧪", listOf(EnergyCost(EnergyType.NATURE, 4), EnergyCost(EnergyType.WATER, 2))),
        BaseEffigySpec(OsrsSkill.FARMING, "Agriculture Effigy", "🌱", listOf(EnergyCost(EnergyType.NATURE, 5), EnergyCost(EnergyType.LIGHT, 1))),
        BaseEffigySpec(OsrsSkill.HUNTER, "Beast Tracking Effigy", "🐾", listOf(EnergyCost(EnergyType.NATURE, 3), EnergyCost(EnergyType.AIR, 3))),
        BaseEffigySpec(OsrsSkill.SLAYER, "Bounty Hunter Effigy", "💀", listOf(EnergyCost(EnergyType.DARK, 4), EnergyCost(EnergyType.FIRE, 2))),
        BaseEffigySpec(OsrsSkill.AGILITY, "Dexterity Effigy", "🏃", listOf(EnergyCost(EnergyType.AIR, 4), EnergyCost(EnergyType.LIGHT, 2))),
        BaseEffigySpec(OsrsSkill.THIEVING, "Trickery Effigy", "🥷", listOf(EnergyCost(EnergyType.DARK, 4), EnergyCost(EnergyType.AIR, 2))),
        BaseEffigySpec(OsrsSkill.RUNECRAFT, "Runemaking Effigy", "🔮", listOf(EnergyCost(EnergyType.LIGHT, 3), EnergyCost(EnergyType.DARK, 3))),
        BaseEffigySpec(OsrsSkill.FLETCHING, "Whittling Effigy", "🎯", listOf(EnergyCost(EnergyType.AIR, 3), EnergyCost(EnergyType.NATURE, 3))),
        BaseEffigySpec(OsrsSkill.CONSTRUCTION, "Hut-Keeping Effigy", "🛠️", listOf(EnergyCost(EnergyType.NATURE, 3), EnergyCost(EnergyType.DARK, 3))),
        BaseEffigySpec(OsrsSkill.SAILING, "Navigation Effigy", "⛵", listOf(EnergyCost(EnergyType.WATER, 4), EnergyCost(EnergyType.AIR, 2))),
        BaseEffigySpec(OsrsSkill.DIVINATION, "Divination Effigy", "📱", listOf(EnergyCost(EnergyType.AIR, 2), EnergyCost(EnergyType.WATER, 2), EnergyCost(EnergyType.FIRE, 2), EnergyCost(EnergyType.NATURE, 2))),
        BaseEffigySpec(OsrsSkill.ADVENTURING, "Adventuring Effigy", "🗺️", listOf(EnergyCost(EnergyType.LIGHT, 2), EnergyCost(EnergyType.DARK, 2), EnergyCost(EnergyType.NATURE, 2))),
        BaseEffigySpec(OsrsSkill.ATTACK, "Hand Combat Effigy", "⚔️", listOf(EnergyCost(EnergyType.DARK, 4), EnergyCost(EnergyType.FIRE, 2))),
        BaseEffigySpec(OsrsSkill.DEFENCE, "Warding Effigy", "🛡️", listOf(EnergyCost(EnergyType.LIGHT, 4), EnergyCost(EnergyType.WATER, 2))),
        BaseEffigySpec(OsrsSkill.RANGED, "Blowdarts Effigy", "🏹", listOf(EnergyCost(EnergyType.AIR, 4), EnergyCost(EnergyType.NATURE, 2))),
        BaseEffigySpec(OsrsSkill.MAGIC, "Incantations Effigy", "🪄", listOf(EnergyCost(EnergyType.LIGHT, 4), EnergyCost(EnergyType.DARK, 2))),
        BaseEffigySpec(OsrsSkill.MAGIC, "Incantation Effigy", "✨", listOf(EnergyCost(EnergyType.LIGHT, 5), EnergyCost(EnergyType.AIR, 1))),
        BaseEffigySpec(OsrsSkill.HITPOINTS, "Vitality Effigy", "❤️", listOf(EnergyCost(EnergyType.WATER, 3), EnergyCost(EnergyType.NATURE, 3)))
    )

    val EFFIGY_RECIPES: List<EffigyRecipe> = BASE_SPECS.flatMap { spec ->
        EFFIGY_TIERS.map { tier ->
            val itemId = if (tier.level == 1) {
                "item_effigy_${spec.skill.name.lowercase()}"
            } else {
                "item_effigy_${spec.skill.name.lowercase()}_t${tier.level}"
            }
            val fullName = "${tier.name} ${spec.baseName}"
            val scaledCosts = spec.baseCosts.map {
                EnergyCost(it.type, it.amount * tier.costMultiplier)
            }
            EffigyRecipe(
                skill = spec.skill,
                effigyItemId = itemId,
                effigyName = fullName,
                emoji = spec.emoji,
                requiredEnergies = scaledCosts,
                xpReward = tier.xpReward,
                reqDivLevel = tier.reqDivLevel,
                tierLevel = tier.level,
                tierName = tier.name,
                craftDivXp = tier.craftDivXp,
                badgeColorHex = tier.colorHex
            )
        }
    }
}
