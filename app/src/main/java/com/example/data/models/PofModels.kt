package com.example.data.models

import androidx.compose.ui.graphics.Color

enum class SeedCategory(val displayName: String, val icon: String) {
    VEGETABLE("Vegetable", "🥦"),
    HERB("Herb", "🌿"),
    TREE("Trees & Barks", "🪵"),
    FRUIT_TREE("Fruit Trees", "🍎"),
    ALL("All Seeds", "🌾")
}

enum class ContractDifficulty(
    val displayName: String,
    val reqLevel: Int,
    val icon: String,
    val colorHex: Long
) {
    EASY("Easy Contract", 65, "🟢", 0xFF81C784),
    MEDIUM("Medium Contract", 75, "🟡", 0xFFFFD54F),
    HARD("Hard Contract", 85, "🔴", 0xFFE57373)
}

data class FarmingContract(
    val difficulty: ContractDifficulty,
    val targetCrop: FarmCropType,
    val isCompleted: Boolean = false,
    val isRewardClaimed: Boolean = false
)

enum class FarmCropType(
    val displayName: String,
    val seedId: String,
    val seedName: String,
    val seedEmoji: String,
    val reqFarmingLevel: Int,
    val growthTimeSeconds: Int,
    val farmingXp: Long,
    val produceItemId: String,
    val produceName: String,
    val produceEmoji: String,
    val produceQty: Int,
    val produceHealHp: Int,
    val produceHunger: Int,
    val produceGpVal: Long,
    val category: SeedCategory = SeedCategory.VEGETABLE
) {
    // VEGETABLE SEEDS
    POTATO("Potato Patch", "item_potato_seed", "Potato Seed", "🌱", 1, 10, 20L, "item_potato", "Fresh Potato", "🥔", 3, 5, 12, 15L, SeedCategory.VEGETABLE),
    ONION("Onion Patch", "item_onion_seed", "Onion Seed", "🌱", 5, 15, 35L, "item_onion", "Fresh Onion", "🧅", 3, 6, 15, 25L, SeedCategory.VEGETABLE),
    CABBAGE("Cabbage Patch", "item_cabbage_seed", "Cabbage Seed", "🥬", 10, 20, 50L, "item_cabbage", "Fresh Cabbage", "🥬", 3, 8, 18, 40L, SeedCategory.VEGETABLE),
    CARROT("Carrot Patch", "item_carrot_seed", "Carrot Seed", "🥕", 15, 25, 65L, "item_carrot", "Crunchy Carrot", "🥕", 4, 9, 20, 50L, SeedCategory.VEGETABLE),
    TOMATO("Tomato Patch", "item_tomato_seed", "Tomato Seed", "🍅", 20, 30, 80L, "item_tomato", "Juicy Tomato", "🍅", 3, 10, 22, 65L, SeedCategory.VEGETABLE),
    SWEETCORN("Sweetcorn Field", "item_sweetcorn_seed", "Sweetcorn Seed", "🌽", 35, 45, 140L, "item_sweetcorn", "Golden Sweetcorn", "🌽", 4, 15, 30, 120L, SeedCategory.VEGETABLE),
    STRAWBERRY("Strawberry Garden", "item_strawberry_seed", "Strawberry Seed", "🍓", 45, 60, 220L, "item_strawberry", "Ripe Strawberry", "🍓", 4, 20, 35, 220L, SeedCategory.VEGETABLE),
    PUMPKIN("Pumpkin Patch", "item_pumpkin_seed", "Pumpkin Seed", "🎃", 52, 75, 310L, "item_pumpkin", "Giant Pumpkin", "🎃", 4, 24, 42, 350L, SeedCategory.VEGETABLE),
    WATERMELON("Watermelon Patch", "item_watermelon_seed", "Watermelon Seed", "🍉", 60, 90, 400L, "item_watermelon", "Juicy Watermelon", "🍉", 5, 28, 50, 450L, SeedCategory.VEGETABLE),

    // HERB SEEDS
    GREENLEAF("Greenleaf Patch", "item_greenleaf_seed", "Greenleaf Seed", "🌿", 1, 30, 40L, "item_clean_greenleaf", "Clean Greenleaf", "🌿", 5, 0, 0, 100L, SeedCategory.HERB),
    MEADOW_MINT("Meadow Mint Patch", "item_meadow_mint_seed", "Meadow Mint Seed", "🌱", 8, 45, 65L, "item_clean_meadow_mint", "Clean Meadow Mint", "🌿", 5, 0, 0, 180L, SeedCategory.HERB),
    WILD_THYME("Wild Thyme Patch", "item_wild_thyme_seed", "Wild Thyme Seed", "🍃", 18, 60, 110L, "item_clean_wild_thyme", "Clean Wild Thyme", "🌿", 5, 0, 0, 280L, SeedCategory.HERB),
    LAVENDER("Lavender Patch", "item_lavender_seed", "Lavender Seed", "🪻", 28, 90, 190L, "item_clean_lavender", "Clean Lavender", "🌱", 5, 0, 0, 450L, SeedCategory.HERB),
    SUNLEAF("Sunleaf Herb Patch", "item_sunleaf_seed", "Sunleaf Seed", "🌿", 40, 120, 320L, "item_clean_sunleaf", "Clean Sunleaf", "🌿", 6, 0, 0, 800L, SeedCategory.HERB),
    IRONLEAF("Ironleaf Patch", "item_ironleaf_seed", "Ironleaf Seed", "🍃", 55, 150, 480L, "item_clean_ironleaf", "Clean Ironleaf", "🍃", 6, 0, 0, 1200L, SeedCategory.HERB),
    WINTERGREEN("Wintergreen Patch", "item_wintergreen_seed", "Wintergreen Seed", "🌿", 68, 180, 680L, "item_clean_wintergreen", "Clean Wintergreen", "🌿", 6, 0, 0, 1800L, SeedCategory.HERB),
    SILVERLEAF("Silverleaf Patch", "item_silverleaf_seed", "Silverleaf Seed", "🌱", 78, 240, 950L, "item_clean_silverleaf", "Clean Silverleaf", "🌿", 6, 0, 0, 2500L, SeedCategory.HERB),
    MYSTIC_SAGE("Mystic Sage Patch", "item_mystic_sage_seed", "Mystic Sage Seed", "✨", 85, 300, 1300L, "item_clean_mystic_sage", "Clean Mystic Sage", "🌿", 6, 0, 0, 3800L, SeedCategory.HERB),
    MOONFLOWER("Moonflower Patch", "item_moonflower_seed", "Moonflower Seed", "🪻", 89, 330, 1500L, "item_clean_moonflower", "Clean Moonflower", "🪻", 6, 0, 0, 4400L, SeedCategory.HERB),
    VERVAIN("Vervain Patch", "item_vervain_seed", "Vervain Seed", "🌸", 92, 360, 1800L, "item_clean_vervain", "Clean Vervain", "🌸", 6, 0, 0, 5000L, SeedCategory.HERB),

    // REGULAR TREE SEEDS (Take hours to grow, massive Agriculture XP)
    OAK_TREE("Oak Tree Patch", "item_oak_seed", "Oak Tree Seed", "🌳", 25, 7200, 14000L, "item_oak_bark", "Oak Bark", "🪵", 8, 0, 0, 1200L, SeedCategory.TREE), // 2 hours
    BIRCH_TREE("Birch Tree Patch", "item_birch_seed", "Birch Tree Seed", "🌳", 25, 9000, 20000L, "item_birch_bark", "Birch Bark", "🪵", 8, 0, 0, 1600L, SeedCategory.TREE), // 2.5 hours
    WILLOW_TREE("Willow Tree Patch", "item_willow_seed", "Willow Tree Seed", "🌳", 30, 12600, 32000L, "item_willow_bark", "Willow Bark", "🪵", 8, 0, 0, 2400L, SeedCategory.TREE), // 3.5 hours
    PINE_TREE("Pine Tree Patch", "item_pine_seed", "Pine Tree Seed", "🌲", 38, 16200, 48000L, "item_pine_bark", "Pine Bark", "🪵", 8, 0, 0, 3500L, SeedCategory.TREE), // 4.5 hours
    MAPLE_TREE("Maple Tree Patch", "item_maple_seed", "Maple Tree Seed", "🍁", 45, 19800, 70000L, "item_maple_bark", "Maple Bark", "🪵", 8, 0, 0, 5000L, SeedCategory.TREE), // 5.5 hours
    CEDAR_TREE("Cedar Tree Patch", "item_cedar_seed", "Cedar Tree Seed", "🌲", 52, 23400, 100000L, "item_cedar_bark", "Cedar Bark", "🪵", 8, 0, 0, 7000L, SeedCategory.TREE), // 6.5 hours
    YEW_TREE("Yew Tree Patch", "item_yew_seed", "Yew Tree Seed", "🌳", 60, 28800, 145000L, "item_yew_bark", "Yew Bark", "🪵", 8, 0, 0, 10000L, SeedCategory.TREE), // 8 hours
    IRONWOOD_TREE("Ironwood Tree Patch", "item_ironwood_seed", "Ironwood Tree Seed", "🪵", 68, 36000, 210000L, "item_ironwood_bark", "Ironwood Bark", "🪵", 8, 0, 0, 15000L, SeedCategory.TREE), // 10 hours
    MAGIC_TREE("Magic Tree Patch", "item_magic_seed", "Magic Tree Seed", "✨", 75, 43200, 320000L, "item_magic_bark", "Magic Bark", "🪵", 8, 0, 0, 25000L, SeedCategory.TREE), // 12 hours
    REDWOOD_TREE("Redwood Tree Patch", "item_redwood_seed", "Redwood Tree Seed", "🪵", 85, 57600, 500000L, "item_redwood_bark", "Redwood Bark", "🪵", 8, 0, 0, 40000L, SeedCategory.TREE), // 16 hours

    // FRUIT TREE SEEDS (Take hours to grow, massive Agriculture XP)
    APPLE_TREE("Apple Tree Orchard", "item_apple_tree_seed", "Apple Tree Seed", "🍎", 25, 9000, 18000L, "item_cooking_apple", "Cooking Apple", "🍎", 6, 12, 20, 800L, SeedCategory.FRUIT_TREE), // 2.5 hours
    CHERRY_TREE("Cherry Tree Orchard", "item_cherry_tree_seed", "Cherry Tree Seed", "🍒", 33, 12600, 30000L, "item_sweet_cherry", "Sweet Cherry", "🍒", 6, 15, 25, 1200L, SeedCategory.FRUIT_TREE), // 3.5 hours
    APRICOT_TREE("Apricot Tree Orchard", "item_apricot_tree_seed", "Apricot Tree Seed", "🍑", 39, 16200, 45000L, "item_golden_apricot", "Golden Apricot", "🍑", 6, 18, 30, 1800L, SeedCategory.FRUIT_TREE), // 4.5 hours
    PEACH_TREE("Peach Tree Orchard", "item_peach_tree_seed", "Peach Tree Seed", "🍑", 48, 19800, 68000L, "item_ripe_peach", "Ripe Peach", "🍑", 6, 22, 35, 2500L, SeedCategory.FRUIT_TREE), // 5.5 hours
    PALM_TREE("Palm Tree Orchard", "item_palm_tree_seed", "Palm Tree Seed", "🌴", 57, 25200, 105000L, "item_papaya_fruit", "Papaya Fruit", "🥥", 6, 26, 40, 4000L, SeedCategory.FRUIT_TREE), // 7 hours
    SAKURA_TREE("Sakura Tree Orchard", "item_sakura_tree_seed", "Sakura Tree Seed", "🌸", 64, 30600, 150000L, "item_sakura_blossom", "Sakura Blossom", "🌸", 6, 30, 45, 6000L, SeedCategory.FRUIT_TREE), // 8.5 hours
    COCONUT_TREE("Coconut Palm Orchard", "item_coconut_tree_seed", "Coconut Tree Seed", "🥥", 72, 36000, 210000L, "item_coconut", "Fresh Coconut", "🥥", 6, 35, 50, 9000L, SeedCategory.FRUIT_TREE), // 10 hours
    DRAGONFRUIT_TREE("Dragonfruit Orchard", "item_dragonfruit_seed", "Dragonfruit Seed", "🐉", 81, 43200, 310000L, "item_dragonfruit", "Dragonfruit", "🐉", 6, 42, 60, 15000L, SeedCategory.FRUIT_TREE), // 12 hours
    SPIRIT_TREE("Spirit Tree Orchard", "item_spirit_seed", "Spirit Tree Seed", "🌀", 88, 54000, 480000L, "item_spirit_fruit", "Spirit Essence Fruit", "🌀", 6, 50, 75, 25000L, SeedCategory.FRUIT_TREE) // 15 hours
}

fun formatGrowthDuration(seconds: Int): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
        hrs > 0 -> "${hrs}h"
        mins > 0 && secs > 0 -> "${mins}m ${secs}s"
        mins > 0 -> "${mins}m"
        else -> "${secs}s"
    }
}

enum class CropGrowthStage(
    val stageName: String,
    val description: String,
    val stepIndex: Int,
    val stepLabel: String
) {
    PLANTING("1. Sowing & Composting", "Seed planted in enriched soil (-1 Seed, -1 Compost)", 1, "1. Sown"),
    SPROUTING("2. Seedling Sprout", "Shoots breaking through soil, auto-watered 💦", 2, "2. Sprout"),
    VEGETATIVE("2. Vegetative Growth", "Healthy leafy stem expanding rapidly", 2, "2. Growing"),
    FLOWERING("2. Budding & Flowering", "Flowering buds and initial fruit forming", 2, "2. Blooming"),
    MATURING("2. Ripening Crop", "Full color and size development under sunlight", 2, "2. Ripening"),
    READY_TO_HARVEST("3. Harvest Ready", "Crop mature! Automated reaping in progress 🌾", 3, "3. Harvest"),
    RESEEDING("4. Re-seeding Cycle", "Patch cleared, searching Bag/Bank for next seed", 4, "4. Re-plant")
}

data class FarmPlotState(
    val plotIndex: Int,
    val cropType: FarmCropType? = null,
    val plantedTimestampMs: Long = 0L,
    val isWatered: Boolean = false,
    val isComposted: Boolean = false,
    val isDiseased: Boolean = false
) {
    fun isReadyToHarvest(currentTimeMs: Long): Boolean {
        if (cropType == null) return false
        val elapsedSec = (currentTimeMs - plantedTimestampMs) / 1000
        val requiredSec = if (isWatered) (cropType.growthTimeSeconds * 0.8f).toInt() else cropType.growthTimeSeconds
        return elapsedSec >= requiredSec
    }

    fun remainingSeconds(currentTimeMs: Long): Int {
        if (cropType == null) return 0
        val elapsedSec = ((currentTimeMs - plantedTimestampMs) / 1000).toInt()
        val requiredSec = if (isWatered) (cropType.growthTimeSeconds * 0.8f).toInt() else cropType.growthTimeSeconds
        return (requiredSec - elapsedSec).coerceAtLeast(0)
    }

    fun progressFraction(currentTimeMs: Long): Float {
        if (cropType == null) return 0f
        val elapsedSec = (currentTimeMs - plantedTimestampMs) / 1000f
        val requiredSec = if (isWatered) (cropType.growthTimeSeconds * 0.8f) else cropType.growthTimeSeconds.toFloat()
        return (elapsedSec / requiredSec).coerceIn(0f, 1f)
    }

    fun getGrowthStage(currentTimeMs: Long): CropGrowthStage {
        if (cropType == null) return CropGrowthStage.RESEEDING
        if (isReadyToHarvest(currentTimeMs)) return CropGrowthStage.READY_TO_HARVEST
        val progress = progressFraction(currentTimeMs)
        return when {
            progress < 0.15f -> CropGrowthStage.PLANTING
            progress < 0.35f -> CropGrowthStage.SPROUTING
            progress < 0.65f -> CropGrowthStage.VEGETATIVE
            progress < 0.85f -> CropGrowthStage.FLOWERING
            else -> CropGrowthStage.MATURING
        }
    }

    fun getStageVisualEmoji(currentTimeMs: Long): String {
        if (cropType == null) return "🪴"
        if (isReadyToHarvest(currentTimeMs)) return cropType.produceEmoji
        val progress = progressFraction(currentTimeMs)
        return when {
            progress < 0.15f -> cropType.seedEmoji
            progress < 0.35f -> "🌱"
            progress < 0.65f -> if (cropType.category == SeedCategory.HERB) "🍃" else "🌿"
            progress < 0.85f -> if (cropType.category == SeedCategory.TREE || cropType.category == SeedCategory.FRUIT_TREE) "🌲" else "🌸"
            else -> cropType.produceEmoji
        }
    }

    fun growthChancePercent(farmingLevel: Int, scarecrowBuilt: Boolean = false): Int {
        if (cropType == null) return calculatePatchBaseFertility(farmingLevel)
        return calculateCropGrowthChance(
            crop = cropType,
            farmingLevel = farmingLevel,
            isWatered = isWatered,
            isComposted = isComposted,
            scarecrowBuilt = scarecrowBuilt
        )
    }
}

fun calculateCropGrowthChance(
    crop: FarmCropType,
    farmingLevel: Int,
    isWatered: Boolean = false,
    isComposted: Boolean = false,
    scarecrowBuilt: Boolean = false
): Int {
    val levelDiff = (farmingLevel - crop.reqFarmingLevel).coerceAtLeast(0)
    val base = when (crop.category) {
        SeedCategory.VEGETABLE -> 78
        SeedCategory.HERB -> 72
        SeedCategory.TREE -> 68
        SeedCategory.FRUIT_TREE -> 70
        else -> 75
    }
    val levelBonus = (levelDiff * 0.4f).toInt().coerceAtMost(16)
    val waterBonus = if (isWatered) 8 else 0
    val compostBonus = if (isComposted) 10 else 0
    val scarecrowBonus = if (scarecrowBuilt) 4 else 0
    return (base + levelBonus + waterBonus + compostBonus + scarecrowBonus).coerceIn(40, 100)
}

fun calculatePatchBaseFertility(farmingLevel: Int): Int {
    return (75 + ((farmingLevel - 1) * 20 / 98)).coerceIn(75, 95)
}

data class PlayerOwnedFarmState(
    val plots: List<FarmPlotState> = (0..11).map { FarmPlotState(plotIndex = it) },
    val scarecrowBuilt: Boolean = false,
    val compostBinLevel: Int = 1,
    val compostBucketsCount: Int = 10,
    val totalCropsHarvested: Int = 0,
    val activeContract: FarmingContract? = null,
    val totalContractsCompleted: Int = 0,
    val husbandryState: AnimalHusbandryState = AnimalHusbandryState()
)

fun isFarmPlotUnlocked(plotIndex: Int, farmingLevel: Int, constructionLevel: Int): Boolean {
    return when (plotIndex) {
        0, 1, 2, 3 -> true // Active Main Farm Patches #1 - #4 (Herbs & Vegetables)
        8, 9, 10, 11 -> farmingLevel >= 25 // Tree Orchard Patches #9 - #12 (Trees & Fruit Trees, Lv 25)
        4, 5 -> farmingLevel >= 65 // Farming Guild Patches #5 & #6 (Unlocked at Level 65 Agriculture)
        6 -> constructionLevel >= 50 // Construction Patch #1 (Unlocked at Level 50 Construction)
        7 -> constructionLevel >= 75 // Construction Patch #2 (Unlocked at Level 75 Construction)
        else -> false
    }
}

fun getAllowedSeedCategoriesForPlot(plotIndex: Int): List<SeedCategory> {
    return when (plotIndex) {
        0, 1, 2, 3 -> listOf(SeedCategory.HERB, SeedCategory.VEGETABLE)
        8, 9, 10, 11 -> listOf(SeedCategory.TREE, SeedCategory.FRUIT_TREE)
        else -> listOf(SeedCategory.VEGETABLE, SeedCategory.HERB, SeedCategory.TREE, SeedCategory.FRUIT_TREE)
    }
}

fun isCropAllowedInPlot(plotIndex: Int, cropType: FarmCropType): Boolean {
    return when (plotIndex) {
        0, 1, 2, 3 -> cropType.category == SeedCategory.HERB || cropType.category == SeedCategory.VEGETABLE
        8, 9, 10, 11 -> cropType.category == SeedCategory.TREE || cropType.category == SeedCategory.FRUIT_TREE
        else -> true
    }
}

fun isCompostableItem(item: InventoryItem): Boolean {
    val lowerId = item.id.lowercase()
    val lowerName = item.name.lowercase()

    // 1. Must NOT be a seed
    val isSeed = lowerName.contains("seed") || lowerId.contains("seed") ||
            FarmCropType.entries.any { it.seedId == item.id || it.seedName.equals(item.name, ignoreCase = true) }
    if (isSeed) return false

    // 2. Must NOT be animal meat, fish, poultry, bones, or ashes
    val isMeatOrFish = lowerName.contains("meat") || lowerName.contains("beef") ||
            lowerName.contains("chicken") || lowerName.contains("pork") ||
            lowerName.contains("trout") || lowerName.contains("salmon") ||
            lowerName.contains("lobster") || lowerName.contains("shark") ||
            lowerName.contains("shrimp") || lowerName.contains("swordfish") ||
            lowerName.contains("manta") || lowerName.contains("anchov") ||
            lowerName.contains("crab") || lowerName.contains("tuna") ||
            lowerName.contains("fish") || lowerName.contains("bone") ||
            lowerName.contains("ash") || lowerId.contains("raw_") ||
            lowerId.contains("meat") || lowerId.contains("fish") ||
            lowerId.contains("bone")

    if (isMeatOrFish) return false

    // 3. Must NOT be equipment, metal, runes, tools, potions, coins, scrolls
    val isNonOrganic = item.category == ItemCategory.EQUIPMENT ||
            item.category == ItemCategory.SKILL_TOOL ||
            item.category == ItemCategory.POTION ||
            item.category == ItemCategory.RUNES_MAGIC ||
            item.category == ItemCategory.BARS_ORES ||
            item.category == ItemCategory.BONES ||
            lowerId.contains("ore") || lowerId.contains("bar") ||
            lowerId.contains("pickaxe") || lowerId.contains("axe") ||
            lowerId.contains("sword") || lowerId.contains("shield") ||
            lowerId.contains("potion") || lowerId.contains("coin") ||
            lowerId.contains("scroll") || lowerId.contains("rune_") ||
            lowerId.contains("helm") || lowerId.contains("plate") ||
            lowerId.contains("legs") || lowerId.contains("boots")

    if (isNonOrganic) return false

    // 4. Any Farm produce (Vegetables, Herbs, Fruits, Barks)
    if (FarmCropType.entries.any { it.produceItemId == item.id }) return true

    // 5. Check keywords for all vegetables, fruits, crops, herbs, barks, organic produce
    val isVegetableOrFruit = lowerName.contains("potato") || lowerName.contains("onion") ||
            lowerName.contains("cabbage") || lowerName.contains("carrot") ||
            lowerName.contains("tomato") || lowerName.contains("sweetcorn") ||
            lowerName.contains("corn") || lowerName.contains("strawberry") ||
            lowerName.contains("pumpkin") || lowerName.contains("watermelon") ||
            lowerName.contains("melon") || lowerName.contains("apple") ||
            lowerName.contains("cherry") || lowerName.contains("apricot") ||
            lowerName.contains("peach") || lowerName.contains("papaya") ||
            lowerName.contains("sakura") || lowerName.contains("coconut") ||
            lowerName.contains("dragonfruit") || lowerName.contains("spirit fruit") ||
            lowerName.contains("fruit") || lowerName.contains("vegetable") ||
            lowerName.contains("banana") || lowerName.contains("orange") ||
            lowerName.contains("lemon") || lowerName.contains("lime") ||
            lowerName.contains("grape") || lowerName.contains("berry") ||
            lowerName.contains("berries") || lowerName.contains("mushroom") ||
            lowerName.contains("lettuce") || lowerName.contains("spinach") ||
            lowerName.contains("kale") || lowerName.contains("beet") ||
            lowerName.contains("radish") || lowerName.contains("turnip") ||
            lowerName.contains("cucumber") || lowerName.contains("pepper") ||
            lowerName.contains("chili") || lowerName.contains("squash") ||
            lowerName.contains("gourd") || lowerName.contains("eggplant") ||
            lowerName.contains("herb") || lowerName.contains("leaf") ||
            lowerName.contains("clean_") || lowerName.contains("grimy_") ||
            lowerName.contains("bark") || lowerName.contains("logs") ||
            lowerName.contains("weed") || lowerName.contains("grass") ||
            lowerName.contains("seaweed") || lowerName.contains("kelp") ||
            lowerName.contains("flower") || lowerName.contains("blossom") ||
            lowerName.contains("bread") || lowerName.contains("dough") ||
            lowerName.contains("grain") || lowerName.contains("wheat") ||
            lowerName.contains("flour") ||
            lowerId.contains("potato") || lowerId.contains("onion") ||
            lowerId.contains("cabbage") || lowerId.contains("carrot") ||
            lowerId.contains("tomato") || lowerId.contains("sweetcorn") ||
            lowerId.contains("strawberry") || lowerId.contains("pumpkin") ||
            lowerId.contains("watermelon") || lowerId.contains("apple") ||
            lowerId.contains("cherry") || lowerId.contains("apricot") ||
            lowerId.contains("peach") || lowerId.contains("papaya") ||
            lowerId.contains("sakura") || lowerId.contains("coconut") ||
            lowerId.contains("dragonfruit") || lowerId.contains("fruit") ||
            lowerId.contains("herb") || lowerId.contains("leaf") ||
            lowerId.contains("bark") || lowerId.contains("logs")

    if (isVegetableOrFruit) return true

    // 6. Any other Food item that is not meat/fish
    if (item.category == ItemCategory.FOOD) return true

    return false
}
