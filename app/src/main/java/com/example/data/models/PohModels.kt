package com.example.data.models

enum class PohRoomType(
    val displayName: String,
    val reqLevel: Int,
    val buildCostGp: Long,
    val iconEmoji: String,
    val description: String
) {
    PARLOUR("Parlour", 1, 1000L, "🚪", "Basic living area for relaxing with your pet"),
    GRASS_PATCH("Grass Patch", 1, 100L, "🌿", "Natural grassy lawn with lush green turf floor to connect your house rooms and gardens"),
    GARDEN("Garden", 1, 1000L, "🌱", "Outdoor garden space with campfire & fishing pond"),
    WORKSHOP("Workshop", 1, 1000L, "🛠️", "Sawmill area to convert logs to planks, craft furniture & tools"),
    KITCHEN("Kitchen", 5, 5000L, "🍳", "Cook meals and food for your companion"),
    DINING_ROOM("Dining Room", 10, 10000L, "🪑", "Feast with friends and pets at dining tables"),
    BEDROOM("Bedroom", 20, 10000L, "🛏️", "Restful sanctuary for your companion pet"),
    SKILL_HALL("Skill Hall", 25, 15000L, "🏆", "Display skill trophies and achievements"),
    GAMES_ROOM("Games Room", 30, 25000L, "🎲", "Fun minigames and activity tables"),
    COMBAT_ROOM("Combat Room", 32, 25000L, "⚔️", "Train combat and dueling ring"),
    MENAGERIE("Menagerie", 37, 30000L, "🐾", "A habitat where all unlocked pets freely roam!"),
    CHAPEL("Chapel", 45, 50000L, "🏛️", "Pray at altars for XP bonuses"),
    PORTAL_CHAMBER("Portal Chamber", 50, 100000L, "🔮", "Teleport portals to capital cities"),
    SUPERIOR_GARDEN("Superior Garden", 65, 120000L, "🌸", "Restoration pools and spirit trees"),
    ACHIEVEMENT_GALLERY("Achievement Gallery", 80, 200000L, "👑", "Display the Occult Altar and Cape Stand")
}

enum class GeMaterial(
    val displayName: String,
    val defaultPriceGp: Long,
    val iconEmoji: String,
    val itemId: String
) {
    OAK_PLANK("Oak Plank", 460L, "🪚", "item_oak_plank"),
    BIRCH_PLANK("Birch Plank", 300L, "🪚", "item_birch_plank"),
    WILLOW_PLANK("Willow Plank", 550L, "🪚", "item_willow_plank"),
    PINE_PLANK("Pine Plank", 420L, "🪚", "item_pine_plank"),
    CEDAR_PLANK("Cedar Plank", 600L, "🪚", "item_cedar_plank"),
    MAPLE_PLANK("Maple Plank", 750L, "🪚", "item_maple_plank"),
    YEW_PLANK("Yew Plank", 950L, "🪚", "item_yew_plank"),
    MAGIC_PLANK("Magic Plank", 1100L, "🪚", "item_magic_plank"),
    REDWOOD_PLANK("Redwood Plank", 1400L, "🪚", "item_redwood_plank"),
    SPIRIT_PLANK("Spirit Redwood Plank", 1600L, "🪚", "item_spirit_plank"),
    ASTRAL_PLANK("Astral Oak Plank", 1900L, "🪚", "item_astral_plank"),
    SUNFIRE_PLANK("Sunfire Plank", 2300L, "🪚", "item_sunfire_plank"),
    EMBERWOOD_PLANK("Emberwood Plank", 2700L, "🪚", "item_emberwood_plank"),
    OBSIDIAN_PLANK("Obsidian Plank", 3200L, "🪚", "item_obsidian_plank"),
    CELESTIAL_PLANK("Celestial Yew Plank", 4000L, "🪚", "item_celestial_plank"),
    COSMIC_PLANK("Cosmic Redwood Plank", 5000L, "🪚", "item_cosmic_plank"),
    GOLDEN_SPIRIT_PLANK("Golden Spirit Plank", 8000L, "🪚", "item_golden_spirit_plank"),
    IRONWOOD_PLANK("Ironwood Plank", 1200L, "🪚", "item_ironwood_plank"),
    TEAK_PLANK("Teak Plank", 850L, "🪚", "item_teak_plank"),
    MAHOGANY_PLANK("Mahogany Plank", 2100L, "🪚", "item_mahogany_plank"),
    GOLD_LEAF("Gold Leaf", 130000L, "🍃", "item_gold_leaf"),
    MARBLE_BLOCK("Marble Block", 325000L, "🏛️", "item_marble_block"),
    CLOTH("Cloth", 650L, "🧵", "item_cloth"),
    NAILS("Nails (100x)", 120L, "🔩", "item_bronze_nails")
}

data class PohFurnitureItem(
    val id: String,
    val name: String,
    val reqLevel: Int,
    val xpGained: Long,
    val roomType: PohRoomType,
    val requiredMaterials: Map<GeMaterial, Int>,
    val iconEmoji: String,
    val effectDescription: String = ""
)

enum class PohWallType(
    val displayName: String,
    val iconEmoji: String,
    val colorHex: Long,
    val reqLevel: Int = 1,
    val costGp: Long = 0L,
    val materialReq: String = "No Material"
) {
    NONE("Open (No Wall)", "⬜", 0x00000000, 1, 0L, "Free"),
    WOOD_PLANK("Wood Plank Wall", "🪵", 0xFF8D6E63, 1, 100L, "1x Oak Plank"),
    STONE_WALL("Stone Wall", "🪨", 0xFF90A4AE, 5, 250L, "1x Stone Block"),
    BRICK_WALL("Red Brick Wall", "🧱", 0xFFC62828, 15, 500L, "1x Brick Block"),
    MARBLE_WALL("White Marble Wall", "🏛️", 0xFFECEFF1, 30, 1500L, "1x Marble Block"),
    OBSIDIAN_WALL("Obsidian Runed Wall", "🔮", 0xFF4A148C, 50, 3500L, "1x Obsidian Block");

    companion object {
        fun fromName(name: String?): PohWallType {
            return values().firstOrNull { it.name.equals(name, ignoreCase = true) } ?: NONE
        }
    }
}

enum class PohFloorType(
    val displayName: String,
    val iconEmoji: String,
    val colorHex: Long,
    val reqLevel: Int = 1,
    val costGp: Long = 0L,
    val description: String = ""
) {
    DEFAULT_WOOD("Oak Floorboards", "🪵", 0xFF3E2723, 1, 50L, "Warm rustic wooden floorboards"),
    COBBLESTONE("Cobblestone Pavers", "🪨", 0xFF37474F, 5, 150L, "Solid chiseled stone walkway tiles"),
    HERRINGBONE_BRICK("Herringbone Brick", "🧱", 0xFF4E342E, 15, 300L, "Interlocking terracotta clay bricks"),
    MARBLE_TILES("Polished Marble", "🏛️", 0xFF546E7A, 25, 800L, "Lustrous white-veined marble slabs"),
    ROYAL_CARPET("Royal Crimson Carpet", "🧶", 0xFF880E4F, 35, 1200L, "Soft woven royal crimson velvet rug"),
    VERDANT_TURF("Lush Garden Turf", "🌿", 0xFF1B5E20, 1, 50L, "Fresh emerald lawn grass"),
    CELESTIAL_MOSAIC("Celestial Mosaic", "✨", 0xFF1A237E, 50, 2500L, "Luminescent astral crystal tiling");

    companion object {
        fun fromName(name: String?): PohFloorType {
            return values().firstOrNull { it.name.equals(name, ignoreCase = true) } ?: DEFAULT_WOOD
        }
    }
}

data class BuiltRoom(
    val id: String,
    val roomType: PohRoomType,
    val builtFurnitureIds: List<String> = emptyList(),
    val gridPosition: Int = 4,
    val wallNorth: PohWallType = PohWallType.WOOD_PLANK,
    val wallEast: PohWallType = PohWallType.WOOD_PLANK,
    val wallSouth: PohWallType = PohWallType.WOOD_PLANK,
    val wallWest: PohWallType = PohWallType.WOOD_PLANK,
    val floorType: PohFloorType = PohFloorType.DEFAULT_WOOD
)

data class PohHouseState(
    val builtRooms: List<BuiltRoom> = listOf(
        BuiltRoom(
            id = "default_parlour",
            roomType = PohRoomType.PARLOUR,
            builtFurnitureIds = listOf("wooden_chair_1"),
            gridPosition = 4
        )
    ),
    val materialInventory: Map<GeMaterial, Int> = emptyMap(),
    val extraGridSize: Int = 0
)

data class TaskXpItem(
    val taskId: String,
    val title: String,
    val category: String,
    val defaultSkill: OsrsSkill,
    val currentXp: Long
)

/**
 * Calculates POH blueprint grid dimension based on Construction level and purchased GP expansions:
 * - Level 1-20: 3x3 base grid (9 rooms)
 * - Level 21-40: 4x4 base grid (16 rooms)
 * - Level 41-60: 5x5 base grid (25 rooms)
 * - Level 61-80: 6x6 base grid (36 rooms)
 * - Level 81-99: 7x7 base grid (49 rooms)
 * + extraGridSize purchased rows/columns with GP
 */
fun getPohGridDimension(constructionLevel: Int, extraGridSize: Int = 0): Int {
    val base = ((constructionLevel.coerceIn(1, 99) - 1) / 20) + 3
    return base + extraGridSize.coerceAtLeast(0)
}
