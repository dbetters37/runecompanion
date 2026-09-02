package com.example.data.models

/**
 * Pure unlockable Skill Outfits data model.
 * Skill outfits are permanent passive buffs (+5% XP per piece, up to +30% for a 6-piece set)
 * that never take up inventory or bank storage space and do not require equipping.
 */
data class SkillOutfitPiece(
    val id: String,
    val name: String,
    val slotName: String,
    val skill: OsrsSkill,
    val iconEmoji: String,
    val costGp: Long,
    val bonusXpPercent: Int = 5,
    val description: String
)

data class SkillOutfitSet(
    val skill: OsrsSkill,
    val setName: String,
    val prefix: String,
    val iconEmoji: String,
    val pieces: List<SkillOutfitPiece>
) {
    val totalBonusXp: Int get() = pieces.sumOf { it.bonusXpPercent }
}

object SkillOutfitData {

    private fun createSet(
        skill: OsrsSkill,
        setName: String,
        prefix: String,
        emojis: List<String>,
        headName: String,
        bodyName: String,
        legsName: String,
        bootsName: String,
        glovesName: String,
        capeName: String
    ): SkillOutfitSet {
        val skillName = skill.displayName
        val pieces = listOf(
            SkillOutfitPiece(
                id = "item_${prefix}_head",
                name = headName,
                slotName = "Headgear",
                skill = skill,
                iconEmoji = emojis[0],
                costGp = 50000L,
                bonusXpPercent = 5,
                description = "Part of the $setName skilling set. Grants permanent +5% $skillName XP passive buff once unlocked (takes 0 storage)."
            ),
            SkillOutfitPiece(
                id = "item_${prefix}_body",
                name = bodyName,
                slotName = "Chestpiece",
                skill = skill,
                iconEmoji = emojis[1],
                costGp = 80000L,
                bonusXpPercent = 5,
                description = "Part of the $setName skilling set. Grants permanent +5% $skillName XP passive buff once unlocked (takes 0 storage)."
            ),
            SkillOutfitPiece(
                id = "item_${prefix}_legs",
                name = legsName,
                slotName = "Legwear",
                skill = skill,
                iconEmoji = emojis[2],
                costGp = 70000L,
                bonusXpPercent = 5,
                description = "Part of the $setName skilling set. Grants permanent +5% $skillName XP passive buff once unlocked (takes 0 storage)."
            ),
            SkillOutfitPiece(
                id = "item_${prefix}_boots",
                name = bootsName,
                slotName = "Footwear",
                skill = skill,
                iconEmoji = emojis[3],
                costGp = 40000L,
                bonusXpPercent = 5,
                description = "Part of the $setName skilling set. Grants permanent +5% $skillName XP passive buff once unlocked (takes 0 storage)."
            ),
            SkillOutfitPiece(
                id = "item_${prefix}_gloves",
                name = glovesName,
                slotName = "Handwear",
                skill = skill,
                iconEmoji = emojis[4],
                costGp = 40000L,
                bonusXpPercent = 5,
                description = "Part of the $setName skilling set. Grants permanent +5% $skillName XP passive buff once unlocked (takes 0 storage)."
            ),
            SkillOutfitPiece(
                id = "item_${prefix}_cape",
                name = capeName,
                slotName = "Back Cloak",
                skill = skill,
                iconEmoji = emojis[5],
                costGp = 60000L,
                bonusXpPercent = 5,
                description = "Part of the $setName skilling set. Grants permanent +5% $skillName XP passive buff once unlocked (takes 0 storage)."
            )
        )
        return SkillOutfitSet(
            skill = skill,
            setName = setName,
            prefix = prefix,
            iconEmoji = emojis[0],
            pieces = pieces
        )
    }

    val ALL_SETS: List<SkillOutfitSet> = listOf(
        // 1. ATTACK
        createSet(
            skill = OsrsSkill.ATTACK,
            setName = "Striker",
            prefix = "striker",
            emojis = listOf("🪖", "🥋", "👖", "👢", "🧤", "🧥"),
            headName = "Striker Helm",
            bodyName = "Striker Top",
            legsName = "Striker Legs",
            bootsName = "Striker Boots",
            glovesName = "Striker Gloves",
            capeName = "Striker Cape"
        ),
        // 2. DEFENCE
        createSet(
            skill = OsrsSkill.DEFENCE,
            setName = "Guardian",
            prefix = "guardian",
            emojis = listOf("🪖", "🛡️", "🦵", "👢", "🧤", "🧥"),
            headName = "Guardian Helm",
            bodyName = "Guardian Plate",
            legsName = "Guardian Legs",
            bootsName = "Guardian Boots",
            glovesName = "Guardian Gauntlets",
            capeName = "Guardian Cape"
        ),
        // 3. RANGED
        createSet(
            skill = OsrsSkill.RANGED,
            setName = "Marksman",
            prefix = "marksman",
            emojis = listOf("🪖", "🥋", "👖", "👢", "🧤", "🎒"),
            headName = "Marksman Cowl",
            bodyName = "Marksman Tunic",
            legsName = "Marksman Chaps",
            bootsName = "Marksman Boots",
            glovesName = "Marksman Vambraces",
            capeName = "Marksman Quiver Cape"
        ),
        // 4. DIVINATION
        createSet(
            skill = OsrsSkill.DIVINATION,
            setName = "Diviner",
            prefix = "diviner",
            emojis = listOf("🔮", "👘", "👖", "🥿", "🧤", "🧣"),
            headName = "Diviner Circlet",
            bodyName = "Diviner Robe Top",
            legsName = "Diviner Robe Bottom",
            bootsName = "Diviner Sandals",
            glovesName = "Diviner Wraps",
            capeName = "Diviner Sacred Stole"
        ),
        // 5. MAGIC
        createSet(
            skill = OsrsSkill.MAGIC,
            setName = "Mystic",
            prefix = "mystic",
            emojis = listOf("🧙‍♂️", "👘", "👖", "👢", "🧤", "🧥"),
            headName = "Mystic Hat",
            bodyName = "Mystic Robe Top",
            legsName = "Mystic Robe Bottom",
            bootsName = "Mystic Boots",
            glovesName = "Mystic Gloves",
            capeName = "Mystic Cape"
        ),
        // 6. RUNECRAFT
        createSet(
            skill = OsrsSkill.RUNECRAFT,
            setName = "Riftwalker",
            prefix = "riftwalker",
            emojis = listOf("🌀", "👘", "👖", "👢", "🧤", "🧥"),
            headName = "Riftwalker Hood",
            bodyName = "Riftwalker Top",
            legsName = "Riftwalker Legs",
            bootsName = "Riftwalker Boots",
            glovesName = "Riftwalker Gloves",
            capeName = "Riftwalker Cape"
        ),
        // 7. CONSTRUCTION
        createSet(
            skill = OsrsSkill.CONSTRUCTION,
            setName = "Carpenter",
            prefix = "carpenter",
            emojis = listOf("⛑️", "🦺", "👖", "👢", "🧤", "🧥"),
            headName = "Carpenter Helmet",
            bodyName = "Carpenter Shirt",
            legsName = "Carpenter Trousers",
            bootsName = "Carpenter Boots",
            glovesName = "Carpenter Gloves",
            capeName = "Carpenter Cape"
        ),
        // 8. HITPOINTS
        createSet(
            skill = OsrsSkill.HITPOINTS,
            setName = "Vitality",
            prefix = "vitality",
            emojis = listOf("❤️", "🦺", "👖", "👢", "🧤", "🧥"),
            headName = "Vitality Helm",
            bodyName = "Vitality Plate",
            legsName = "Vitality Greaves",
            bootsName = "Vitality Treads",
            glovesName = "Vitality Gauntlets",
            capeName = "Vitality Cape"
        ),
        // 9. AGILITY
        createSet(
            skill = OsrsSkill.AGILITY,
            setName = "Graceful",
            prefix = "graceful",
            emojis = listOf("🥷", "🥋", "👖", "👟", "🧤", "🧥"),
            headName = "Graceful Hood",
            bodyName = "Graceful Top",
            legsName = "Graceful Legs",
            bootsName = "Graceful Boots",
            glovesName = "Graceful Gloves",
            capeName = "Graceful Cape"
        ),
        // 10. HERBLORE
        createSet(
            skill = OsrsSkill.HERBLORE,
            setName = "Botanist",
            prefix = "botanist",
            emojis = listOf("😷", "🥼", "👖", "👢", "🧤", "🧥"),
            headName = "Botanist Mask",
            bodyName = "Botanist Tunic",
            legsName = "Botanist Trousers",
            bootsName = "Botanist Boots",
            glovesName = "Botanist Gloves",
            capeName = "Botanist Cape"
        ),
        // 11. THIEVING
        createSet(
            skill = OsrsSkill.THIEVING,
            setName = "Rogue",
            prefix = "rogue",
            emojis = listOf("🥷", "🥋", "👖", "👢", "🧤", "🧥"),
            headName = "Rogue Mask",
            bodyName = "Rogue Top",
            legsName = "Rogue Trousers",
            bootsName = "Rogue Boots",
            glovesName = "Rogue Gloves",
            capeName = "Rogue Cloak"
        ),
        // 12. FLETCHING
        createSet(
            skill = OsrsSkill.FLETCHING,
            setName = "Fletcher",
            prefix = "fletcher",
            emojis = listOf("🤠", "🥋", "👖", "👢", "🧤", "🧥"),
            headName = "Fletcher Hat",
            bodyName = "Fletcher Top",
            legsName = "Fletcher Legs",
            bootsName = "Fletcher Boots",
            glovesName = "Fletcher Gloves",
            capeName = "Fletcher Cape"
        ),
        // 13. SLAYER
        createSet(
            skill = OsrsSkill.SLAYER,
            setName = "Bounty Hunter",
            prefix = "bounty_hunter",
            emojis = listOf("💀", "🥋", "👖", "👢", "🧤", "🧥"),
            headName = "Bounty Hunter Helm",
            bodyName = "Bounty Hunter Top",
            legsName = "Bounty Hunter Legs",
            bootsName = "Bounty Hunter Boots",
            glovesName = "Bounty Hunter Gloves",
            capeName = "Bounty Hunter Cape"
        ),
        // 14. HUNTER
        createSet(
            skill = OsrsSkill.HUNTER,
            setName = "Huntsman",
            prefix = "huntsman",
            emojis = listOf("🤠", "🥋", "👖", "👢", "🧤", "🧥"),
            headName = "Huntsman Hat",
            bodyName = "Huntsman Top",
            legsName = "Huntsman Trousers",
            bootsName = "Huntsman Boots",
            glovesName = "Huntsman Gloves",
            capeName = "Huntsman Cloak"
        ),
        // 15. SMITHING / MINING
        createSet(
            skill = OsrsSkill.SMITHING,
            setName = "Forgemaster",
            prefix = "forgemaster",
            emojis = listOf("⛑️", "🦺", "🦵", "👢", "🧤", "🧥"),
            headName = "Forgemaster Helmet",
            bodyName = "Forgemaster Apron",
            legsName = "Forgemaster Greaves",
            bootsName = "Forgemaster Boots",
            glovesName = "Forgemaster Gauntlets",
            capeName = "Forgemaster Cape"
        ),
        // 16. FISHING
        createSet(
            skill = OsrsSkill.FISHING,
            setName = "Angler",
            prefix = "angler",
            emojis = listOf("👒", "🥋", "👖", "👢", "🧤", "🧥"),
            headName = "Angler Hat",
            bodyName = "Angler Top",
            legsName = "Angler Waders",
            bootsName = "Angler Boots",
            glovesName = "Angler Gloves",
            capeName = "Angler Cape"
        ),
        // 17. COOKING
        createSet(
            skill = OsrsSkill.COOKING,
            setName = "Chef",
            prefix = "chef",
            emojis = listOf("👨‍🍳", "🥼", "👖", "👞", "🧤", "🧥"),
            headName = "Chef Toque",
            bodyName = "Chef Apron",
            legsName = "Chef Trousers",
            bootsName = "Chef Clogs",
            glovesName = "Chef Oven Mitts",
            capeName = "Chef Cape"
        ),
        // 18. FIREMAKING / SUMMONING
        createSet(
            skill = OsrsSkill.FIREMAKING,
            setName = "Summoner",
            prefix = "summoner",
            emojis = listOf("🐺", "👘", "👖", "👢", "🧤", "🧥"),
            headName = "Summoner Hood",
            bodyName = "Summoner Robe Top",
            legsName = "Summoner Robe Legs",
            bootsName = "Summoner Boots",
            glovesName = "Summoner Grips",
            capeName = "Summoner Cloak"
        ),
        // 19. WOODCUTTING
        createSet(
            skill = OsrsSkill.WOODCUTTING,
            setName = "Lumberjack",
            prefix = "lumberjack",
            emojis = listOf("🧢", "🥋", "👖", "👢", "🧤", "🧥"),
            headName = "Lumberjack Hat",
            bodyName = "Lumberjack Top",
            legsName = "Lumberjack Legs",
            bootsName = "Lumberjack Boots",
            glovesName = "Lumberjack Gloves",
            capeName = "Lumberjack Cape"
        ),
        // 20. FARMING
        createSet(
            skill = OsrsSkill.FARMING,
            setName = "Farmer",
            prefix = "farmer",
            emojis = listOf("👒", "🧥", "👖", "👢", "🧤", "🧥"),
            headName = "Farmer Strawhat",
            bodyName = "Farmer Jacket",
            legsName = "Farmer Boro Trousers",
            bootsName = "Farmer Boots",
            glovesName = "Farmer Gloves",
            capeName = "Farmer Cape"
        ),
        // 21. SAILING
        createSet(
            skill = OsrsSkill.SAILING,
            setName = "Captain",
            prefix = "captain",
            emojis = listOf("🎩", "🧥", "👖", "👢", "🧤", "🧥"),
            headName = "Captain Tricorne",
            bodyName = "Captain Coat",
            legsName = "Captain Trousers",
            bootsName = "Captain Deck Boots",
            glovesName = "Captain Gloves",
            capeName = "Captain Cloak"
        ),
        // 22. ADVENTURING
        createSet(
            skill = OsrsSkill.ADVENTURING,
            setName = "Explorer",
            prefix = "explorer",
            emojis = listOf("🤠", "🥋", "👖", "👢", "🧤", "🎒"),
            headName = "Explorer Hat",
            bodyName = "Explorer Tunic",
            legsName = "Explorer Khakis",
            bootsName = "Explorer Boots",
            glovesName = "Explorer Gloves",
            capeName = "Explorer Pack"
        )
    )

    val ALL_PIECES: List<SkillOutfitPiece> = ALL_SETS.flatMap { it.pieces }

    val ALL_PIECE_IDS: Set<String> = ALL_PIECES.map { it.id }.toSet()

    val SETS_BY_SKILL: Map<OsrsSkill, SkillOutfitSet> = ALL_SETS.associateBy { it.skill }

    val DUNGEON_99_PRIZES: List<SkillOutfitPiece> = ALL_PIECES.take(99)

    fun getPieceById(id: String): SkillOutfitPiece? {
        val normId = id.trim().lowercase()
        return ALL_PIECES.find { it.id.lowercase() == normId || it.id.lowercase() == "item_$normId" }
    }

    fun getSetForSkill(skill: OsrsSkill): SkillOutfitSet? {
        return SETS_BY_SKILL[skill]
    }
}
