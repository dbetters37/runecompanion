package com.example.data.models

import java.util.UUID

enum class LivestockType(
    val id: String,
    val speciesName: String,
    val emoji: String,
    val reqFarmingLevel: Int,
    val buyCostGp: Long,
    val produceItemId: String,
    val produceItemName: String,
    val produceEmoji: String,
    val produceCycleSeconds: Int, // e.g. 180s = 3 mins
    val description: String,
    val defaultName: String
) {
    CHICKEN(
        id = "livestock_chicken",
        speciesName = "Coop Chicken",
        emoji = "🐔",
        reqFarmingLevel = 1,
        buyCostGp = 500L,
        produceItemId = "item_egg",
        produceItemName = "Fresh Egg",
        produceEmoji = "🥚",
        produceCycleSeconds = 180, // 3 minutes
        description = "Lays fresh organic eggs and churns rich compost bedding.",
        defaultName = "Clucky"
    ),
    SHEEP(
        id = "livestock_sheep",
        speciesName = "Pasture Sheep",
        emoji = "🐑",
        reqFarmingLevel = 68,
        buyCostGp = 2000L,
        produceItemId = "item_wool",
        produceItemName = "Soft Wool",
        produceEmoji = "🧶",
        produceCycleSeconds = 210, // 3.5 minutes
        description = "Grows soft harvestable wool coats while grazing on farm pasture.",
        defaultName = "Woolly"
    ),
    DAIRY_COW(
        id = "livestock_cow",
        speciesName = "Dairy Cow",
        emoji = "🐄",
        reqFarmingLevel = 72,
        buyCostGp = 5000L,
        produceItemId = "item_bucket_of_milk",
        produceItemName = "Bucket of Milk",
        produceEmoji = "🥛",
        produceCycleSeconds = 240, // 4 minutes
        description = "Produces pails of rich creamy milk and abundant compost.",
        defaultName = "Bessie"
    ),
    SPOTTED_PIG(
        id = "livestock_pig",
        speciesName = "Spotted Pig",
        emoji = "🐖",
        reqFarmingLevel = 76,
        buyCostGp = 8000L,
        produceItemId = "item_truffle",
        produceItemName = "Earthy Truffle",
        produceEmoji = "🍄",
        produceCycleSeconds = 270, // 4.5 minutes
        description = "Roots through the pen soil foraging for prized gourmet truffles.",
        defaultName = "Barnaby"
    ),
    FLUFFY_ALPACA(
        id = "livestock_alpaca",
        speciesName = "Fluffy Alpaca",
        emoji = "🦙",
        reqFarmingLevel = 80,
        buyCostGp = 15000L,
        produceItemId = "item_alpaca_fleece",
        produceItemName = "Alpaca Fleece",
        produceEmoji = "🦙",
        produceCycleSeconds = 300, // 5 minutes
        description = "Yields ultra-fine silky fleece and enriched organic compost.",
        defaultName = "Paco"
    ),
    FREMENNIK_YAK(
        id = "livestock_yak",
        speciesName = "Fremennik Yak",
        emoji = "🐂",
        reqFarmingLevel = 84,
        buyCostGp = 25000L,
        produceItemId = "item_yak_hair",
        produceItemName = "Yak Hair",
        produceEmoji = "🐂",
        produceCycleSeconds = 330, // 5.5 minutes
        description = "Hardy northern yak shedding dense strands of industrial-grade hair.",
        defaultName = "Ragnar"
    ),
    CHINCHILLA(
        id = "livestock_chinchilla",
        speciesName = "Red Chinchilla",
        emoji = "🦔",
        reqFarmingLevel = 88,
        buyCostGp = 40000L,
        produceItemId = "item_chinchilla_fur",
        produceItemName = "Chinchilla Fur",
        produceEmoji = "🦔",
        produceCycleSeconds = 360, // 6 minutes
        description = "Quick and curious critter that sheds soft downy fur coats.",
        defaultName = "Pip"
    ),
    LAVA_DRAKE(
        id = "livestock_drake",
        speciesName = "Lava Drake",
        emoji = "🐉",
        reqFarmingLevel = 93,
        buyCostGp = 80000L,
        produceItemId = "item_dragon_scale",
        produceItemName = "Dragon Scale",
        produceEmoji = "🐉",
        produceCycleSeconds = 420, // 7 minutes
        description = "A warm draconic hatchling that periodically sheds fire-tempered scales.",
        defaultName = "Ignis"
    ),
    SPIRIT_UNICORN(
        id = "livestock_unicorn",
        speciesName = "Spirit Unicorn",
        emoji = "🦄",
        reqFarmingLevel = 97,
        buyCostGp = 150000L,
        produceItemId = "item_spirit_dust",
        produceItemName = "Spirit Horn Dust",
        produceEmoji = "✨",
        produceCycleSeconds = 480, // 8 minutes
        description = "A celestial horned beast emitting radiant spiritual dust.",
        defaultName = "Celeste"
    );

    companion object {
        fun fromId(id: String): LivestockType {
            return entries.find { it.id == id } ?: CHICKEN
        }
    }
}

data class FarmAnimalInstance(
    val instanceId: String = UUID.randomUUID().toString(),
    val typeId: String = LivestockType.CHICKEN.id,
    val customName: String = "Clucky",
    val purchasedTimestampMs: Long = System.currentTimeMillis(),
    val lastProducedTimestampMs: Long = System.currentTimeMillis()
) {
    val type: LivestockType get() = LivestockType.fromId(typeId)
}

data class AnimalHusbandryState(
    val animals: List<FarmAnimalInstance> = emptyList(),
    val troughFoodPct: Int = 100, // 0 to 4000 %
    val storedCompost: Int = 0,
    val storedProduce: Map<String, Int> = emptyMap() // itemId to quantity
) {
    companion object {
        const val MAX_TROUGH_CAPACITY = 4000 // 4000% hunger capacity (doubled capacity)
    }
}

fun calculateMaxLivestockCapacity(farmingLevel: Int): Int {
    return when {
        farmingLevel >= 99 -> 10
        farmingLevel >= 90 -> 9
        farmingLevel >= 80 -> 8
        farmingLevel >= 70 -> 7
        farmingLevel >= 60 -> 6
        else -> 5
    }
}
