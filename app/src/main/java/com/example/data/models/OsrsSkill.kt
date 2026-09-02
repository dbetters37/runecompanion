package com.example.data.models

import androidx.compose.ui.graphics.Color

enum class OsrsSkill(
    val displayName: String,
    val description: String,
    val realLifeAction: String,
    val accentColor: Color,
    val iconSymbol: String
) {
    ATTACK("Hand Combat", "Accuracy in combat & physical strikes", "Do punching drills or boxing workouts", Color(0xFF9E2A2B), "⚔️"),
    DEFENCE("Warding", "Resilience & spiritual protection", "Hold a plank, do core exercises, maintain good posture", Color(0xFF3A86FF), "🛡️"),
    RANGED("Blowdarts", "Precision & target focus", "Dart throwing, archery, or target practice", Color(0xFF38B000), "🏹"),
    MAGIC("Incantations", "Sacred nature chants, spirit blessings & shamanic rites", "Meditation chants, focused breathing, or reciting positive affirmations", Color(0xFF4361EE), "🪄"),
    RUNECRAFT("Runemaking", "Carving ancient runes & spirit energy focus", "Solving logic riddles or memory games", Color(0xFF7209B7), "🔮"),
    CONSTRUCTION("Hut-Keeping", "Building sacred shrines & domestic sanctuary", "Clean your room, assemble furniture, or organize desk", Color(0xFFB5179E), "🛠️"),
    HITPOINTS("Life Energy", "Overall spirit health & vitality", "Eating balanced meals & getting 8 hours of sleep", Color(0xFFE63946), "❤️"),
    AGILITY("Dexterity", "Stamina, speed & physical mobility", "Walking steps, running, jogging, taking stairs", Color(0xFF2A9D8F), "🏃"),
    HERBLORE("Herbalism", "Potion brewing & sacred herb knowledge", "Drinking water, taking vitamins, brewing tea", Color(0xFF52B788), "🧪"),
    THIEVING("Trickery", "Stealth & quick hand dexterity", "Quick finger tapping, card tricks, typing speed test", Color(0xFF6D597A), "🥷"),
    FLETCHING("Whittling", "Crafting spirit charms & feather totems", "Fine motor tasks, origami, motor precision", Color(0xFF264653), "🎯"),
    SLAYER("Bounty Hunter", "Purifying corrupt beasts & tough challenges", "Conquering a hard task on your to-do list", Color(0xFF1D3557), "💀"),
    HUNTER("Beast Tracking", "Tracking wildlife & nature awareness", "Nature walks, bird watching, observing wildlife", Color(0xFFE07A5F), "🐾"),
    SMITHING("Forging", "Forging metal totems, mining ores & crafting sacred armors", "Repairing items, fixing household objects, mining ores", Color(0xFF495057), "⚒️"),
    FISHING("Fishing", "Patience & river harvesting", "Spear fishing or sitting calmly by water", Color(0xFF0077B6), "🎣"),
    COOKING("Cooking", "Culinary arts & campfire meal prep", "Preparing a meal, baking, or cooking healthy recipes", Color(0xFF81B29A), "🍳"),
    FIREMAKING("Summoning", "Igniting sacred fires & spirit summoning", "Safely lighting a candle, bonfire, or cozy hearth", Color(0xFFF3722C), "🐺"),
    WOODCUTTING("Harvesting", "Harvesting sacred trees & clearing paths", "Swiping clear notifications or clearing paperwork", Color(0xFF31572C), "🪓"),
    FARMING("Agriculture", "Cultivating herb gardens & spirit crops", "Watering house plants, gardening, caring for nature", Color(0xFF70E000), "🌱"),
    DIVINATION("Divination", "Telepathic spirit energy & messaging", "Sending or receiving text messages & SMS", Color(0xFF00B4D8), "📱"),
    SAILING("Navigation", "Seafaring navigation & island exploration", "Boating, canoeing or water navigation", Color(0xFF0096C7), "⛵"),
    ADVENTURING("Adventuring", "Active dungeon exploration & spirit trials", "Active 2D exploration & floor encounters", Color(0xFFFF9800), "🗺️");

    companion object {
        fun fromName(name: String): OsrsSkill {
            return entries.firstOrNull { 
                it.name.equals(name, ignoreCase = true) || 
                it.displayName.equals(name, ignoreCase = true) ||
                (name.equals("Attack", true) && it == ATTACK) ||
                (name.equals("Spirit Strike", true) && it == ATTACK) ||
                (name.equals("Hand Combat", true) && it == ATTACK) ||
                (name.equals("Strength", true) && it == ATTACK) ||
                (name.equals("Defence", true) && it == DEFENCE) ||
                (name.equals("Ranged", true) && it == RANGED) ||
                (name.equals("Magic", true) && it == MAGIC) ||
                (name.equals("Runecraft", true) && it == RUNECRAFT) ||
                (name.equals("Construction", true) && it == CONSTRUCTION) ||
                (name.equals("Hut-Keeping", true) && it == CONSTRUCTION) ||
                (name.equals("Shrine Building", true) && it == CONSTRUCTION) ||
                (name.equals("Hitpoints", true) && it == HITPOINTS) ||
                (name.equals("Life Energy", true) && it == HITPOINTS) ||
                (name.equals("Soul Essence", true) && it == HITPOINTS) ||
                (name.equals("Agility", true) && it == AGILITY) ||
                (name.equals("Herblore", true) && it == HERBLORE) ||
                (name.equals("Thieving", true) && it == THIEVING) ||
                (name.equals("Crafting", true) && it == FLETCHING) ||
                (name.equals("Fletching", true) && it == FLETCHING) ||
                (name.equals("Slayer", true) && it == SLAYER) ||
                (name.equals("Hunter", true) && it == HUNTER) ||
                (name.equals("Mining", true) && it == SMITHING) ||
                (name.equals("Gemology", true) && it == SMITHING) ||
                (name.equals("Smithing", true) && it == SMITHING) ||
                (name.equals("Forging", true) && it == SMITHING) ||
                (name.equals("Fishing", true) && it == FISHING) ||
                (name.equals("Cooking", true) && it == COOKING) ||
                (name.equals("Firemaking", true) && it == FIREMAKING) ||
                (name.equals("Woodcutting", true) && it == WOODCUTTING) ||
                (name.equals("Farming", true) && it == FARMING) ||
                (name.equals("Divination", true) && it == DIVINATION) ||
                (name.equals("Sailing", true) && it == SAILING) ||
                (name.equals("Adventuring", true) && it == ADVENTURING)
            } ?: WOODCUTTING
        }
    }
}

object OsrsXpCalculator {
    /**
     * Standard OSRS XP table calculator up to level 99 (13,034,431 XP).
     */
    fun getXpForLevel(level: Int): Long {
        if (level <= 1) return 0L
        var points = 0.0
        for (lvl in 1 until level) {
            points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0))
        }
        return Math.floor(points / 4.0).toLong()
    }

    fun getLevelForXp(xp: Long): Int {
        for (lvl in 1..99) {
            if (xp < getXpForLevel(lvl + 1)) {
                return lvl
            }
        }
        return 99
    }

    fun getXpProgressToNextLevel(xp: Long): Float {
        val currentLevel = getLevelForXp(xp)
        if (currentLevel >= 99) return 1.0f
        val currentLevelXp = getXpForLevel(currentLevel)
        val nextLevelXp = getXpForLevel(currentLevel + 1)
        val xpInLevel = xp - currentLevelXp
        val totalXpNeededInLevel = nextLevelXp - currentLevelXp
        if (totalXpNeededInLevel <= 0) return 1.0f
        return (xpInLevel.toFloat() / totalXpNeededInLevel.toFloat()).coerceIn(0.0f, 1.0f)
    }

    fun getXpRemainingForNextLevel(xp: Long): Long {
        val currentLevel = getLevelForXp(xp)
        if (currentLevel >= 99) return 0L
        val nextLevelXp = getXpForLevel(currentLevel + 1)
        return (nextLevelXp - xp).coerceAtLeast(0L)
    }

    /**
     * Standard OSRS Combat Level calculation formula delegated to CombatManager.
     */
    fun calculateCombatLevel(skillXpMap: Map<OsrsSkill, Long>): Int {
        return CombatManager.calculateCombatLevel(skillXpMap)
    }

    fun calculateCombatLevel(att: Int, def: Int, hp: Int, rng: Int, mag: Int): Int {
        return CombatManager.calculateCombatLevel(att, def, hp, rng, mag)
    }
}
