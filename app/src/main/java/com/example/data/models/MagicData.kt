package com.example.data.models

enum class Spellbook(val displayName: String, val iconEmoji: String, val colorHex: Long) {
    STANDARD("Standard Spellbook", "🪄", 0xFF2196F3),
    ANCIENT("Ancient Magicks", "🔮", 0xFF9C27B0),
    LUNAR("Lunar Spellbook", "🌙", 0xFF00BCD4),
    ARCEUUS("Arceuus Spellbook", "📜", 0xFF4CAF50)
}

enum class SpellType {
    COMBAT,
    TELEPORT,
    ALCHEMY,
    SUPERHEAT,
    ENCHANTMENT,
    CONVERSION,
    REANIMATE
}

data class RuneRequirement(
    val runeItemId: String,
    val runeName: String,
    val runeEmoji: String,
    val quantity: Int
)

data class MagicSpell(
    val id: String,
    val name: String,
    val spellbook: Spellbook,
    val reqMagicLevel: Int,
    val iconEmoji: String,
    val description: String,
    val runes: List<RuneRequirement>,
    val xpReward: Long,
    val spellType: SpellType
)

data class RunecraftRuneInfo(
    val runeItemId: String,
    val runeName: String,
    val iconEmoji: String,
    val reqLevel: Int,
    val xpPerEssence: Double,
    val multiThresholds: List<Pair<Int, Int>> // List of Pair(LevelRequired, Multiplier)
)

object RunecraftData {
    val CRAFTABLE_RUNES = listOf(
        RunecraftRuneInfo(
            runeItemId = "item_rune_air",
            runeName = "Air Rune",
            iconEmoji = "💨",
            reqLevel = 1,
            xpPerEssence = 5.0,
            multiThresholds = listOf(1 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_mind",
            runeName = "Mind Rune",
            iconEmoji = "🧠",
            reqLevel = 2,
            xpPerEssence = 5.5,
            multiThresholds = listOf(2 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_water",
            runeName = "Water Rune",
            iconEmoji = "💧",
            reqLevel = 5,
            xpPerEssence = 6.0,
            multiThresholds = listOf(5 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_earth",
            runeName = "Earth Rune",
            iconEmoji = "🪨",
            reqLevel = 9,
            xpPerEssence = 6.5,
            multiThresholds = listOf(9 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_fire",
            runeName = "Fire Rune",
            iconEmoji = "🔥",
            reqLevel = 14,
            xpPerEssence = 7.0,
            multiThresholds = listOf(14 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_body",
            runeName = "Body Rune",
            iconEmoji = "🛡️",
            reqLevel = 20,
            xpPerEssence = 7.5,
            multiThresholds = listOf(20 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_cosmic",
            runeName = "Cosmic Rune",
            iconEmoji = "🌌",
            reqLevel = 27,
            xpPerEssence = 8.0,
            multiThresholds = listOf(27 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_chaos",
            runeName = "Chaos Rune",
            iconEmoji = "💥",
            reqLevel = 35,
            xpPerEssence = 8.5,
            multiThresholds = listOf(35 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_astral",
            runeName = "Astral Rune",
            iconEmoji = "✨",
            reqLevel = 40,
            xpPerEssence = 8.7,
            multiThresholds = listOf(40 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_nature",
            runeName = "Nature Rune",
            iconEmoji = "🌿",
            reqLevel = 44,
            xpPerEssence = 9.0,
            multiThresholds = listOf(44 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_law",
            runeName = "Law Rune",
            iconEmoji = "⚖️",
            reqLevel = 54,
            xpPerEssence = 9.5,
            multiThresholds = listOf(54 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_death",
            runeName = "Death Rune",
            iconEmoji = "💀",
            reqLevel = 65,
            xpPerEssence = 10.0,
            multiThresholds = listOf(65 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_blood",
            runeName = "Blood Rune",
            iconEmoji = "🩸",
            reqLevel = 77,
            xpPerEssence = 10.5,
            multiThresholds = listOf(77 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_soul",
            runeName = "Soul Rune",
            iconEmoji = "👻",
            reqLevel = 90,
            xpPerEssence = 12.0,
            multiThresholds = listOf(90 to 1)
        ),
        RunecraftRuneInfo(
            runeItemId = "item_rune_wrath",
            runeName = "Wrath Rune",
            iconEmoji = "⚡",
            reqLevel = 95,
            xpPerEssence = 14.0,
            multiThresholds = listOf(95 to 1)
        )
    )

    fun getMultiplier(rcLevel: Int, runeItemId: String): Int = 1
}

object MagicData {
    val SPELLS = listOf(
        // === STANDARD SPELLBOOK ===
        MagicSpell(
            id = "spell_wind_strike",
            name = "Wind Strike",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 1,
            iconEmoji = "💨",
            description = "A basic gust of wind dealing minor magic combat damage.",
            runes = listOf(
                RuneRequirement("item_rune_air", "Air Rune", "💨", 1),
                RuneRequirement("item_rune_mind", "Mind Rune", "🧠", 1)
            ),
            xpReward = 15L,
            spellType = SpellType.COMBAT
        ),
        MagicSpell(
            id = "spell_water_strike",
            name = "Water Strike",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 5,
            iconEmoji = "💧",
            description = "A splash of pressurized water dealing water magic damage.",
            runes = listOf(
                RuneRequirement("item_rune_water", "Water Rune", "💧", 1),
                RuneRequirement("item_rune_air", "Air Rune", "💨", 1),
                RuneRequirement("item_rune_mind", "Mind Rune", "🧠", 1)
            ),
            xpReward = 20L,
            spellType = SpellType.COMBAT
        ),
        MagicSpell(
            id = "spell_earth_strike",
            name = "Earth Strike",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 9,
            iconEmoji = "🪨",
            description = "Hurls razor rocks dealing earth magic damage.",
            runes = listOf(
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 2),
                RuneRequirement("item_rune_air", "Air Rune", "💨", 1),
                RuneRequirement("item_rune_mind", "Mind Rune", "🧠", 1)
            ),
            xpReward = 25L,
            spellType = SpellType.COMBAT
        ),
        MagicSpell(
            id = "spell_fire_strike",
            name = "Fire Strike",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 13,
            iconEmoji = "🔥",
            description = "A burst of flame dealing potent low-level magic damage.",
            runes = listOf(
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 3),
                RuneRequirement("item_rune_air", "Air Rune", "💨", 2),
                RuneRequirement("item_rune_mind", "Mind Rune", "🧠", 1)
            ),
            xpReward = 30L,
            spellType = SpellType.COMBAT
        ),
        MagicSpell(
            id = "spell_bones_to_bananas",
            name = "Bones to Bananas",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 15,
            iconEmoji = "🍌",
            description = "Converts all bones in your inventory into fresh bananas!",
            runes = listOf(
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 1),
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 2),
                RuneRequirement("item_rune_water", "Water Rune", "💧", 2)
            ),
            xpReward = 40L,
            spellType = SpellType.CONVERSION
        ),
        MagicSpell(
            id = "spell_low_alchemy",
            name = "Low Level Alchemy",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 21,
            iconEmoji = "🪙",
            description = "Converts an inventory item into its base gold value.",
            runes = listOf(
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 1),
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 3)
            ),
            xpReward = 50L,
            spellType = SpellType.ALCHEMY
        ),
        MagicSpell(
            id = "spell_varrock_teleport",
            name = "Varrock Teleport",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 25,
            iconEmoji = "🏰",
            description = "Teleports you instantly, reducing current expedition time by 3 minutes!",
            runes = listOf(
                RuneRequirement("item_rune_law", "Law Rune", "⚖️", 1),
                RuneRequirement("item_rune_air", "Air Rune", "💨", 3),
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 1)
            ),
            xpReward = 55L,
            spellType = SpellType.TELEPORT
        ),
        MagicSpell(
            id = "spell_lumbridge_teleport",
            name = "Lumbridge Teleport",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 31,
            iconEmoji = "🌳",
            description = "Teleports to Lumbridge Courtyard, granting instant companion happiness + XP!",
            runes = listOf(
                RuneRequirement("item_rune_law", "Law Rune", "⚖️", 1),
                RuneRequirement("item_rune_air", "Air Rune", "💨", 3),
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 1)
            ),
            xpReward = 65L,
            spellType = SpellType.TELEPORT
        ),
        MagicSpell(
            id = "spell_superheat_item",
            name = "Superheat Item",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 43,
            iconEmoji = "⚡",
            description = "Instantly smelts an ore into a bar without needing a furnace (+Forging XP)!",
            runes = listOf(
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 1),
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 4)
            ),
            xpReward = 80L,
            spellType = SpellType.SUPERHEAT
        ),
        MagicSpell(
            id = "spell_enchant_jewelry",
            name = "Enchant Jewelry",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 27,
            iconEmoji = "💎",
            description = "Enchants sapphire, emerald, ruby, or diamond jewelry into magical rings/amulets!",
            runes = listOf(
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 1),
                RuneRequirement("item_rune_air", "Air Rune", "💨", 3)
            ),
            xpReward = 75L,
            spellType = SpellType.ENCHANTMENT
        ),
        MagicSpell(
            id = "spell_high_alchemy",
            name = "High Level Alchemy",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 55,
            iconEmoji = "💰",
            description = "Converts an inventory item into 1.5x its gold value!",
            runes = listOf(
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 1),
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 5)
            ),
            xpReward = 120L,
            spellType = SpellType.ALCHEMY
        ),
        MagicSpell(
            id = "spell_bones_to_peaches",
            name = "Bones to Peaches",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 60,
            iconEmoji = "🍑",
            description = "Converts all bones in your inventory into juicy peaches (restores 12 HP each)!",
            runes = listOf(
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 2),
                RuneRequirement("item_rune_water", "Water Rune", "💧", 4),
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 4)
            ),
            xpReward = 140L,
            spellType = SpellType.CONVERSION
        ),
        MagicSpell(
            id = "spell_fire_wave",
            name = "Fire Wave",
            spellbook = Spellbook.STANDARD,
            reqMagicLevel = 75,
            iconEmoji = "🌋",
            description = "A massive wall of fiery destruction dealing high magic damage.",
            runes = listOf(
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 7),
                RuneRequirement("item_rune_air", "Air Rune", "💨", 5),
                RuneRequirement("item_rune_blood", "Blood Rune", "🩸", 1)
            ),
            xpReward = 180L,
            spellType = SpellType.COMBAT
        ),

        // === ANCIENT MAGICKS ===
        MagicSpell(
            id = "spell_blood_rush",
            name = "Blood Rush",
            spellbook = Spellbook.ANCIENT,
            reqMagicLevel = 56,
            iconEmoji = "🩸",
            description = "Siphons enemy lifeforce, dealing damage and healing your pet by 25 HP!",
            runes = listOf(
                RuneRequirement("item_rune_chaos", "Chaos Rune", "💥", 2),
                RuneRequirement("item_rune_death", "Death Rune", "💀", 2),
                RuneRequirement("item_rune_blood", "Blood Rune", "🩸", 1)
            ),
            xpReward = 95L,
            spellType = SpellType.COMBAT
        ),
        MagicSpell(
            id = "spell_ice_burst",
            name = "Ice Burst",
            spellbook = Spellbook.ANCIENT,
            reqMagicLevel = 70,
            iconEmoji = "❄️",
            description = "Freezing blast of sub-zero ice dealing area damage.",
            runes = listOf(
                RuneRequirement("item_rune_chaos", "Chaos Rune", "💥", 4),
                RuneRequirement("item_rune_death", "Death Rune", "💀", 2),
                RuneRequirement("item_rune_water", "Water Rune", "💧", 4)
            ),
            xpReward = 160L,
            spellType = SpellType.COMBAT
        ),
        MagicSpell(
            id = "spell_blood_barrage",
            name = "Blood Barrage",
            spellbook = Spellbook.ANCIENT,
            reqMagicLevel = 92,
            iconEmoji = "🩸",
            description = "Devastating vampiric blood storm, restoring pet HP by 50% of damage dealt!",
            runes = listOf(
                RuneRequirement("item_rune_death", "Death Rune", "💀", 4),
                RuneRequirement("item_rune_blood", "Blood Rune", "🩸", 4),
                RuneRequirement("item_rune_soul", "Soul Rune", "👻", 1)
            ),
            xpReward = 280L,
            spellType = SpellType.COMBAT
        ),
        MagicSpell(
            id = "spell_ice_barrage",
            name = "Ice Barrage",
            spellbook = Spellbook.ANCIENT,
            reqMagicLevel = 94,
            iconEmoji = "🧊",
            description = "Pinnacle ancient spell freezing targets in absolute zero ice for huge damage!",
            runes = listOf(
                RuneRequirement("item_rune_death", "Death Rune", "💀", 4),
                RuneRequirement("item_rune_blood", "Blood Rune", "🩸", 2),
                RuneRequirement("item_rune_water", "Water Rune", "💧", 6)
            ),
            xpReward = 320L,
            spellType = SpellType.COMBAT
        ),

        // === LUNAR SPELLBOOK ===
        MagicSpell(
            id = "spell_bake_pie",
            name = "Bake Pie",
            spellbook = Spellbook.LUNAR,
            reqMagicLevel = 65,
            iconEmoji = "🥧",
            description = "Instantly bakes all uncooked pies/fish in inventory without burning!",
            runes = listOf(
                RuneRequirement("item_rune_astral", "Astral Rune", "✨", 1),
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 5),
                RuneRequirement("item_rune_water", "Water Rune", "💧", 4)
            ),
            xpReward = 150L,
            spellType = SpellType.CONVERSION
        ),
        MagicSpell(
            id = "spell_cure_plant",
            name = "Cure Plant",
            spellbook = Spellbook.LUNAR,
            reqMagicLevel = 66,
            iconEmoji = "🌱",
            description = "Cures any diseased crops in your Player Owned Farm patches!",
            runes = listOf(
                RuneRequirement("item_rune_astral", "Astral Rune", "✨", 1),
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 8)
            ),
            xpReward = 160L,
            spellType = SpellType.CONVERSION
        ),
        MagicSpell(
            id = "spell_humidify",
            name = "Humidify",
            spellbook = Spellbook.LUNAR,
            reqMagicLevel = 68,
            iconEmoji = "💦",
            description = "Fills all empty vials, buckets, and watering cans in your inventory instantly!",
            runes = listOf(
                RuneRequirement("item_rune_astral", "Astral Rune", "✨", 1),
                RuneRequirement("item_rune_water", "Water Rune", "💧", 3),
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 1)
            ),
            xpReward = 175L,
            spellType = SpellType.CONVERSION
        ),
        MagicSpell(
            id = "spell_fertile_soil",
            name = "Fertile Soil",
            spellbook = Spellbook.LUNAR,
            reqMagicLevel = 83,
            iconEmoji = "🟫",
            description = "Super-composts all farm patches for maximum crop yields!",
            runes = listOf(
                RuneRequirement("item_rune_astral", "Astral Rune", "✨", 3),
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 2),
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 15)
            ),
            xpReward = 220L,
            spellType = SpellType.CONVERSION
        ),

        // === ARCEUUS SPELLBOOK ===
        MagicSpell(
            id = "spell_reanimate_corpse",
            name = "Reanimate Corpse",
            spellbook = Spellbook.ARCEUUS,
            reqMagicLevel = 16,
            iconEmoji = "💀",
            description = "Reanimates bones into dark energy for massive Spirit & Magic XP!",
            runes = listOf(
                RuneRequirement("item_rune_body", "Body Rune", "🛡️", 1),
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 1)
            ),
            xpReward = 100L,
            spellType = SpellType.REANIMATE
        ),
        MagicSpell(
            id = "spell_shadow_veil",
            name = "Shadow Veil",
            spellbook = Spellbook.ARCEUUS,
            reqMagicLevel = 47,
            iconEmoji = "👤",
            description = "Envelops you in shadow, granting 100% Trickery success for 5 minutes!",
            runes = listOf(
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 5),
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 5),
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 5)
            ),
            xpReward = 130L,
            spellType = SpellType.CONVERSION
        )
    )
}
