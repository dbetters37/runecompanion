package com.example.data.models

enum class IncantationCategory(
    val displayName: String,
    val iconEmoji: String,
    val colorHex: Long
) {
    NATURE_RITES("Rites of Nature", "🌿", 0xFF2E7D32),
    SPIRIT_HARMONY("Spirit Harmony", "✨", 0xFF7209B7),
    CELESTIAL_BLESSINGS("Celestial Blessings", "☀️", 0xFFF3722C),
    RANGED_INCANTATIONS("Blowdarts Incantations", "🏹", 0xFF2E6B38),
    MELEE_INCANTATIONS("Melee Incantations", "⚔️", 0xFF9E2A2B)
}

data class Incantation(
    val id: String,
    val name: String,
    val category: IncantationCategory,
    val reqLevel: Int,
    val runes: List<RuneRequirement>, // 2 types for Tier 1 & 2, 3 types for Tier 3
    val iconEmoji: String,
    val description: String = "",
    val benefitSummary: String,
    val xpPerMinute: Long,
    val tier: Int = 1
)

data class IncantationSlotSource(
    val id: String,
    val title: String,
    val description: String,
    val bonusSlots: Int,
    val isUnlocked: Boolean,
    val iconEmoji: String,
    val requirementHint: String
)

object IncantationsData {
    val ALL_INCANTATIONS = listOf(
        // ==========================================
        // 1. CHANT OF VERDANT GROWTH (+15% / +25% / +40% Harvesting, Fishing & Forging XP)
        // ==========================================
        Incantation(
            id = "incant_verdant_growth",
            name = "Chant of Verdant Growth",
            category = IncantationCategory.NATURE_RITES,
            reqLevel = 1,
            runes = listOf(
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 3),
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 5)
            ),
            iconEmoji = "🌿",
            benefitSummary = "+15% Harvesting, Fishing & Forging XP",
            xpPerMinute = 120L,
            tier = 1
        ),
        Incantation(
            id = "incant_verdant_growth_t2",
            name = "Hymn of Verdant Growth",
            category = IncantationCategory.NATURE_RITES,
            reqLevel = 45,
            runes = listOf(
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 8),
                RuneRequirement("item_rune_water", "Water Rune", "💧", 12)
            ),
            iconEmoji = "🌿",
            benefitSummary = "+25% Harvesting, Fishing & Forging XP",
            xpPerMinute = 600L,
            tier = 2
        ),
        Incantation(
            id = "incant_verdant_growth_t3",
            name = "Symphony of Verdant Growth",
            category = IncantationCategory.NATURE_RITES,
            reqLevel = 80,
            runes = listOf(
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 20),
                RuneRequirement("item_rune_astral", "Astral Rune", "✨", 10),
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 10)
            ),
            iconEmoji = "🌿",
            benefitSummary = "+40% Harvesting, Fishing & Forging XP",
            xpPerMinute = 2400L,
            tier = 3
        ),

        // ==========================================
        // 2. SPROUT WHISPERER'S LULLABY (+15%/+25%/+40% Crop Yield, +1%/+2%/+3% Seed Return)
        // ==========================================
        Incantation(
            id = "incant_sprout_whisper",
            name = "Sprout Whisperer's Lullaby",
            category = IncantationCategory.NATURE_RITES,
            reqLevel = 4,
            runes = listOf(
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 4),
                RuneRequirement("item_rune_water", "Water Rune", "💧", 6)
            ),
            iconEmoji = "🌱",
            benefitSummary = "+15% Crop Yield & +1% Seed Return Chance",
            xpPerMinute = 150L,
            tier = 1
        ),
        Incantation(
            id = "incant_sprout_whisper_t2",
            name = "Sprout Whisperer's Song",
            category = IncantationCategory.NATURE_RITES,
            reqLevel = 48,
            runes = listOf(
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 10),
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 15)
            ),
            iconEmoji = "🌱",
            benefitSummary = "+25% Crop Yield & +2% Seed Return Chance",
            xpPerMinute = 700L,
            tier = 2
        ),
        Incantation(
            id = "incant_sprout_whisper_t3",
            name = "Sprout Whisperer's Anthem",
            category = IncantationCategory.NATURE_RITES,
            reqLevel = 82,
            runes = listOf(
                RuneRequirement("item_rune_nature", "Nature Rune", "🌿", 25),
                RuneRequirement("item_rune_astral", "Astral Rune", "✨", 12),
                RuneRequirement("item_rune_soul", "Soul Rune", "👻", 5)
            ),
            iconEmoji = "🌱",
            benefitSummary = "+40% Crop Yield & +3% Seed Return Chance",
            xpPerMinute = 2600L,
            tier = 3
        ),

        // ==========================================
        // 3. BLESSING OF FLOWING SPRINGS (-15% / -25% / -40% Quest & Shaman Path Time)
        // ==========================================
        Incantation(
            id = "incant_flowing_springs",
            name = "Blessing of Flowing Springs",
            category = IncantationCategory.NATURE_RITES,
            reqLevel = 10,
            runes = listOf(
                RuneRequirement("item_rune_water", "Water Rune", "💧", 10),
                RuneRequirement("item_rune_mind", "Mind Rune", "🧠", 5)
            ),
            iconEmoji = "💧",
            benefitSummary = "-15% Quest & Shaman Path Time",
            xpPerMinute = 220L,
            tier = 1
        ),
        Incantation(
            id = "incant_flowing_springs_t2",
            name = "Surge of Flowing Springs",
            category = IncantationCategory.NATURE_RITES,
            reqLevel = 52,
            runes = listOf(
                RuneRequirement("item_rune_water", "Water Rune", "💧", 25),
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 8)
            ),
            iconEmoji = "💧",
            benefitSummary = "-25% Quest & Shaman Path Time",
            xpPerMinute = 800L,
            tier = 2
        ),
        Incantation(
            id = "incant_flowing_springs_t3",
            name = "Torrent of Flowing Springs",
            category = IncantationCategory.NATURE_RITES,
            reqLevel = 86,
            runes = listOf(
                RuneRequirement("item_rune_water", "Water Rune", "💧", 50),
                RuneRequirement("item_rune_astral", "Astral Rune", "✨", 15),
                RuneRequirement("item_rune_law", "Law Rune", "⚖️", 10)
            ),
            iconEmoji = "💧",
            benefitSummary = "-40% Quest & Shaman Path Time",
            xpPerMinute = 3000L,
            tier = 3
        ),

        // ==========================================
        // 4. CHANT OF WIND BOW PRECISION (+15% / +25% / +40% Blowdarts Damage)
        // ==========================================
        Incantation(
            id = "incant_wind_precision",
            name = "Chant of Wind Bow Precision",
            category = IncantationCategory.RANGED_INCANTATIONS,
            reqLevel = 1,
            runes = listOf(
                RuneRequirement("item_rune_air", "Air Rune", "💨", 10),
                RuneRequirement("item_rune_mind", "Mind Rune", "🧠", 5)
            ),
            iconEmoji = "🏹",
            benefitSummary = "+15% Blowdarts Damage",
            xpPerMinute = 120L,
            tier = 1
        ),
        Incantation(
            id = "incant_wind_precision_t2",
            name = "Hymn of Wind Bow Precision",
            category = IncantationCategory.RANGED_INCANTATIONS,
            reqLevel = 45,
            runes = listOf(
                RuneRequirement("item_rune_air", "Air Rune", "💨", 30),
                RuneRequirement("item_rune_chaos", "Chaos Rune", "💥", 10)
            ),
            iconEmoji = "🏹",
            benefitSummary = "+25% Blowdarts Damage",
            xpPerMinute = 600L,
            tier = 2
        ),
        Incantation(
            id = "incant_wind_precision_t3",
            name = "Symphony of Wind Bow Precision",
            category = IncantationCategory.RANGED_INCANTATIONS,
            reqLevel = 80,
            runes = listOf(
                RuneRequirement("item_rune_air", "Air Rune", "💨", 60),
                RuneRequirement("item_rune_death", "Death Rune", "💀", 15),
                RuneRequirement("item_rune_wrath", "Wrath Rune", "⚡", 5)
            ),
            iconEmoji = "🏹",
            benefitSummary = "+40% Blowdarts Damage",
            xpPerMinute = 2400L,
            tier = 3
        ),

        // ==========================================
        // 5. CHANT OF HEAVY BLADE (+15% / +25% / +40% Hand Combat Damage)
        // ==========================================
        Incantation(
            id = "incant_heavy_blade",
            name = "Chant of Heavy Blade",
            category = IncantationCategory.MELEE_INCANTATIONS,
            reqLevel = 1,
            runes = listOf(
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 10),
                RuneRequirement("item_rune_body", "Body Rune", "🛡️", 5)
            ),
            iconEmoji = "⚔️",
            benefitSummary = "+15% Hand Combat Damage",
            xpPerMinute = 120L,
            tier = 1
        ),
        Incantation(
            id = "incant_heavy_blade_t2",
            name = "Hymn of Heavy Blade",
            category = IncantationCategory.MELEE_INCANTATIONS,
            reqLevel = 45,
            runes = listOf(
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 25),
                RuneRequirement("item_rune_chaos", "Chaos Rune", "💥", 10)
            ),
            iconEmoji = "⚔️",
            benefitSummary = "+25% Hand Combat Damage",
            xpPerMinute = 600L,
            tier = 2
        ),
        Incantation(
            id = "incant_heavy_blade_t3",
            name = "Symphony of Heavy Blade",
            category = IncantationCategory.MELEE_INCANTATIONS,
            reqLevel = 80,
            runes = listOf(
                RuneRequirement("item_rune_earth", "Earth Rune", "🪨", 50),
                RuneRequirement("item_rune_blood", "Blood Rune", "🩸", 10),
                RuneRequirement("item_rune_wrath", "Wrath Rune", "⚡", 5)
            ),
            iconEmoji = "⚔️",
            benefitSummary = "+40% Hand Combat Damage",
            xpPerMinute = 2400L,
            tier = 3
        ),

        // ==========================================
        // 6. GLIMMER OF STARLIGHT (+15% / +25% / +40% Magic Damage)
        // ==========================================
        Incantation(
            id = "incant_starlight_glimmer",
            name = "Glimmer of Starlight",
            category = IncantationCategory.CELESTIAL_BLESSINGS,
            reqLevel = 3,
            runes = listOf(
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 5),
                RuneRequirement("item_rune_air", "Air Rune", "💨", 10)
            ),
            iconEmoji = "⭐",
            benefitSummary = "+15% Magic Damage",
            xpPerMinute = 130L,
            tier = 1
        ),
        Incantation(
            id = "incant_starlight_glimmer_t2",
            name = "Radiance of Starlight",
            category = IncantationCategory.CELESTIAL_BLESSINGS,
            reqLevel = 45,
            runes = listOf(
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 15),
                RuneRequirement("item_rune_astral", "Astral Rune", "✨", 10)
            ),
            iconEmoji = "⭐",
            benefitSummary = "+25% Magic Damage",
            xpPerMinute = 600L,
            tier = 2
        ),
        Incantation(
            id = "incant_starlight_glimmer_t3",
            name = "Supernova of Starlight",
            category = IncantationCategory.CELESTIAL_BLESSINGS,
            reqLevel = 80,
            runes = listOf(
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 30),
                RuneRequirement("item_rune_astral", "Astral Rune", "✨", 20),
                RuneRequirement("item_rune_soul", "Soul Rune", "👻", 10)
            ),
            iconEmoji = "⭐",
            benefitSummary = "+40% Magic Damage",
            xpPerMinute = 2400L,
            tier = 3
        ),

        // ==========================================
        // 7. TIDE OF SOLAR VITALITY (+5% / +10% / +15% Hitpoints Regeneration)
        // ==========================================
        Incantation(
            id = "incant_solar_vitality",
            name = "Tide of Solar Vitality",
            category = IncantationCategory.CELESTIAL_BLESSINGS,
            reqLevel = 15,
            runes = listOf(
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 15),
                RuneRequirement("item_rune_body", "Body Rune", "🛡️", 5)
            ),
            iconEmoji = "☀️",
            benefitSummary = "+5% Hitpoints Regeneration",
            xpPerMinute = 250L,
            tier = 1
        ),
        Incantation(
            id = "incant_solar_vitality_t2",
            name = "Flare of Solar Vitality",
            category = IncantationCategory.CELESTIAL_BLESSINGS,
            reqLevel = 55,
            runes = listOf(
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 35),
                RuneRequirement("item_rune_blood", "Blood Rune", "🩸", 5)
            ),
            iconEmoji = "☀️",
            benefitSummary = "+10% Hitpoints Regeneration",
            xpPerMinute = 1000L,
            tier = 2
        ),
        Incantation(
            id = "incant_solar_vitality_t3",
            name = "Zenith of Solar Vitality",
            category = IncantationCategory.CELESTIAL_BLESSINGS,
            reqLevel = 88,
            runes = listOf(
                RuneRequirement("item_rune_fire", "Fire Rune", "🔥", 60),
                RuneRequirement("item_rune_blood", "Blood Rune", "🩸", 15),
                RuneRequirement("item_rune_soul", "Soul Rune", "👻", 10)
            ),
            iconEmoji = "☀️",
            benefitSummary = "+15% Hitpoints Regeneration",
            xpPerMinute = 3200L,
            tier = 3
        ),

        // ==========================================
        // 8. COMMUNION OF ANIMAL SPIRITS (+20% / +35% / +50% Summoning Effigy XP & Animal Boosts)
        // ==========================================
        Incantation(
            id = "incant_spirit_communion",
            name = "Communion of Animal Spirits",
            category = IncantationCategory.SPIRIT_HARMONY,
            reqLevel = 5,
            runes = listOf(
                RuneRequirement("item_rune_body", "Body Rune", "🛡️", 8),
                RuneRequirement("item_rune_mind", "Mind Rune", "🧠", 5)
            ),
            iconEmoji = "🦊",
            benefitSummary = "+20% Summoning Effigy XP & Animal Boosts",
            xpPerMinute = 180L,
            tier = 1
        ),
        Incantation(
            id = "incant_spirit_communion_t2",
            name = "Greater Communion of Animal Spirits",
            category = IncantationCategory.SPIRIT_HARMONY,
            reqLevel = 50,
            runes = listOf(
                RuneRequirement("item_rune_body", "Body Rune", "🛡️", 20),
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 10)
            ),
            iconEmoji = "🦊",
            benefitSummary = "+35% Summoning Effigy XP & Animal Boosts",
            xpPerMinute = 850L,
            tier = 2
        ),
        Incantation(
            id = "incant_spirit_communion_t3",
            name = "Master Communion of Animal Spirits",
            category = IncantationCategory.SPIRIT_HARMONY,
            reqLevel = 85,
            runes = listOf(
                RuneRequirement("item_rune_body", "Body Rune", "🛡️", 40),
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 20),
                RuneRequirement("item_rune_soul", "Soul Rune", "👻", 10)
            ),
            iconEmoji = "🦊",
            benefitSummary = "+50% Summoning Effigy XP & Animal Boosts",
            xpPerMinute = 2800L,
            tier = 3
        ),

        // ==========================================
        // 9. WHISPER OF ANCESTRAL SPIRITS (+15% / +25% / +40% Bonus XP for All Skill Training)
        // ==========================================
        Incantation(
            id = "incant_whispering_ghosts",
            name = "Whisper of Ancestral Spirits",
            category = IncantationCategory.SPIRIT_HARMONY,
            reqLevel = 12,
            runes = listOf(
                RuneRequirement("item_rune_mind", "Mind Rune", "🧠", 10),
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 5)
            ),
            iconEmoji = "👻",
            benefitSummary = "+15% Bonus XP for All Skill Training",
            xpPerMinute = 280L,
            tier = 1
        ),
        Incantation(
            id = "incant_whispering_ghosts_t2",
            name = "Voice of Ancestral Spirits",
            category = IncantationCategory.SPIRIT_HARMONY,
            reqLevel = 55,
            runes = listOf(
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 20),
                RuneRequirement("item_rune_law", "Law Rune", "⚖️", 10)
            ),
            iconEmoji = "👻",
            benefitSummary = "+25% Bonus XP for All Skill Training",
            xpPerMinute = 1000L,
            tier = 2
        ),
        Incantation(
            id = "incant_whispering_ghosts_t3",
            name = "Chorus of Ancestral Spirits",
            category = IncantationCategory.SPIRIT_HARMONY,
            reqLevel = 88,
            runes = listOf(
                RuneRequirement("item_rune_cosmic", "Cosmic Rune", "🌌", 35),
                RuneRequirement("item_rune_soul", "Soul Rune", "👻", 15),
                RuneRequirement("item_rune_wrath", "Wrath Rune", "⚡", 5)
            ),
            iconEmoji = "👻",
            benefitSummary = "+40% Bonus XP for All Skill Training",
            xpPerMinute = 3200L,
            tier = 3
        ),

        // ==========================================
        // 10. CHANT OF SLEIGHT OF HAND (+15% / +25% / +40% Pickpocketing Gold & Extra Loot Yield)
        // ==========================================
        Incantation(
            id = "incant_sleight_of_hand",
            name = "Chant of Sleight of Hand",
            category = IncantationCategory.SPIRIT_HARMONY,
            reqLevel = 8,
            runes = listOf(
                RuneRequirement("item_rune_mind", "Mind Rune", "🧠", 8),
                RuneRequirement("item_rune_body", "Body Rune", "🛡️", 6)
            ),
            iconEmoji = "🪙",
            benefitSummary = "+15% Extra Loot & GP from Pickpocketing",
            xpPerMinute = 200L,
            tier = 1
        ),
        Incantation(
            id = "incant_sleight_of_hand_t2",
            name = "Hymn of Sleight of Hand",
            category = IncantationCategory.SPIRIT_HARMONY,
            reqLevel = 50,
            runes = listOf(
                RuneRequirement("item_rune_chaos", "Chaos Rune", "💥", 15),
                RuneRequirement("item_rune_law", "Law Rune", "⚖️", 8)
            ),
            iconEmoji = "🪙",
            benefitSummary = "+25% Extra Loot & GP from Pickpocketing",
            xpPerMinute = 850L,
            tier = 2
        ),
        Incantation(
            id = "incant_sleight_of_hand_t3",
            name = "Symphony of Sleight of Hand",
            category = IncantationCategory.SPIRIT_HARMONY,
            reqLevel = 85,
            runes = listOf(
                RuneRequirement("item_rune_death", "Death Rune", "💀", 20),
                RuneRequirement("item_rune_law", "Law Rune", "⚖️", 15),
                RuneRequirement("item_rune_blood", "Blood Rune", "🩸", 10)
            ),
            iconEmoji = "🪙",
            benefitSummary = "+40% Extra Loot & GP from Pickpocketing",
            xpPerMinute = 2800L,
            tier = 3
        )
    )
}
