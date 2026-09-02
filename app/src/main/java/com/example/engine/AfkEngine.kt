package com.example.engine

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single-object AFK Engine that centralizes, coordinates, and optimizes
 * all Idle/AFK gathering, processing, crafting, exploration, and combat activities.
 */
enum class AfkActivityType(
    val key: String,
    val displayName: String,
    val category: String,
    val emoji: String,
    val associatedSkill: OsrsSkill
) {
    WOODCUTTING("woodcutting", "Woodcutting", "Gathering", "🌳", OsrsSkill.WOODCUTTING),
    MINING("mining", "Quarry Mining", "Gathering", "⛏️", OsrsSkill.SMITHING),
    FISHING("fishing", "Fishing Pond", "Gathering", "🎣", OsrsSkill.FISHING),
    COOKING("cooking", "Cooking Range", "Processing", "🍳", OsrsSkill.COOKING),
    CAMPFIRE("campfire", "Campfire Firemaking", "Processing", "🔥", OsrsSkill.FIREMAKING),
    SMELTING("smelting", "Furnace Smelting", "Artisan", "🔥", OsrsSkill.SMITHING),
    SAWMILL("sawmill", "Sawmill Plank Making", "Artisan", "🪚", OsrsSkill.WOODCUTTING),
    NAIL_CRAFTING("nail_crafting", "Nail Crafting", "Artisan", "🔨", OsrsSkill.SMITHING),
    STICK_CRAFTING("stick_crafting", "Stick Crafting", "Crafting", "🪵", OsrsSkill.FLETCHING),
    SHAFT_CRAFTING("shaft_crafting", "Arrow Shaft Crafting", "Crafting", "🪵", OsrsSkill.FLETCHING),
    FEATHER_CRAFTING("feather_crafting", "Feather Gathering", "Gathering", "🪶", OsrsSkill.HUNTER),
    BOWSTRING_CRAFTING("bowstring_crafting", "Bowstring Crafting", "Crafting", "🧶", OsrsSkill.FLETCHING),
    ARROWTIP_CRAFTING("arrowtip_crafting", "Arrowtip Crafting", "Artisan", "🏹", OsrsSkill.SMITHING),
    TRAP_CRAFTING("trap_crafting", "Hunter Trap Crafting", "Crafting", "🪤", OsrsSkill.HUNTER),
    FLETCHING("fletching", "Fletching Bench", "Crafting", "🏹", OsrsSkill.FLETCHING),
    SMITHING_ANVIL("smithing_anvil", "Anvil Smithing", "Artisan", "⚒️", OsrsSkill.SMITHING),
    HERB_CRUSHING("herb_crushing", "Herb Crushing", "Herblore", "🌿", OsrsSkill.HERBLORE),
    POTION_BREWING("potion_brewing", "Potion Brewing", "Herblore", "🧪", OsrsSkill.HERBLORE),
    DRUID_ALTAR("druid_altar", "Druid Altar Effigies", "Summoning", "🗿", OsrsSkill.FIREMAKING),
    SLAYER("slayer", "Slayer Combat", "Combat", "⚔️", OsrsSkill.SLAYER),
    HUNTER("hunter", "Hunter Trapping", "Gathering", "🐾", OsrsSkill.HUNTER),
    BOSS("boss", "Boss Combat", "Combat", "👑", OsrsSkill.ATTACK),
    FARMING("farming", "Farming Allotment", "Gathering", "🌱", OsrsSkill.FARMING),
    BONE_BURYING("bone_burying", "Bone Burying", "Magic", "🦴", OsrsSkill.MAGIC),
    SAILING("sailing", "Sailing Expedition", "Exploration", "⛵", OsrsSkill.AGILITY),
    RUNECRAFTING("runecrafting", "Runecrafting Altar", "Magic", "🔮", OsrsSkill.RUNECRAFT),
    THIEVING("thieving", "Pickpocketing Thieving", "Rogue", "🕵️", OsrsSkill.THIEVING),
    CATACOMBS("catacombs", "Shamanic Catacombs", "Dungeon", "🗿", OsrsSkill.THIEVING);

    companion object {
        fun fromKey(key: String?): AfkActivityType? {
            if (key.isNullOrBlank()) return null
            val clean = key.removePrefix("afk_")
            if (clean == "sepulchre") return CATACOMBS
            if (clean == "herb_cleaning") return HERB_CRUSHING
            return entries.find { it.key == clean || it.name.equals(clean, ignoreCase = true) }
        }
    }
}

object AfkEngine {

    // Single source of truth for active AFK activity
    private val _currentActivity = MutableStateFlow<AfkActivityType?>(null)
    val currentActivity: StateFlow<AfkActivityType?> = _currentActivity.asStateFlow()

    private val _sessionTicks = MutableStateFlow(0)
    val sessionTicks: StateFlow<Int> = _sessionTicks.asStateFlow()

    private val _activityStartTimeMs = MutableStateFlow(0L)
    val activityStartTimeMs: StateFlow<Long> = _activityStartTimeMs.asStateFlow()

    private val _lastProcessTimeMs = MutableStateFlow(System.currentTimeMillis())
    val lastProcessTimeMs: StateFlow<Long> = _lastProcessTimeMs.asStateFlow()

    private val _recentHistory = MutableStateFlow<List<String>>(emptyList())
    val recentHistory: StateFlow<List<String>> = _recentHistory.asStateFlow()

    // Backward-compatible state flow bridges
    private val _isAfkCampfireActive = MutableStateFlow(false)
    val isAfkCampfireActive: StateFlow<Boolean> = _isAfkCampfireActive.asStateFlow()

    private val _isAfkCookingActive = MutableStateFlow(false)
    val isAfkCookingActive: StateFlow<Boolean> = _isAfkCookingActive.asStateFlow()

    private val _isAfkFishingActive = MutableStateFlow(false)
    val isAfkFishingActive: StateFlow<Boolean> = _isAfkFishingActive.asStateFlow()

    private val _isAfkMiningActive = MutableStateFlow(false)
    val isAfkMiningActive: StateFlow<Boolean> = _isAfkMiningActive.asStateFlow()

    private val _isAfkSmeltingActive = MutableStateFlow(false)
    val isAfkSmeltingActive: StateFlow<Boolean> = _isAfkSmeltingActive.asStateFlow()

    private val _isAfkSawmillActive = MutableStateFlow(false)
    val isAfkSawmillActive: StateFlow<Boolean> = _isAfkSawmillActive.asStateFlow()

    private val _isAfkWoodcuttingActive = MutableStateFlow(false)
    val isAfkWoodcuttingActive: StateFlow<Boolean> = _isAfkWoodcuttingActive.asStateFlow()

    private val _isAfkNailCraftingActive = MutableStateFlow(false)
    val isAfkNailCraftingActive: StateFlow<Boolean> = _isAfkNailCraftingActive.asStateFlow()

    private val _isAfkStickCraftingActive = MutableStateFlow(false)
    val isAfkStickCraftingActive: StateFlow<Boolean> = _isAfkStickCraftingActive.asStateFlow()

    private val _isAfkShaftCraftingActive = MutableStateFlow(false)
    val isAfkShaftCraftingActive: StateFlow<Boolean> = _isAfkShaftCraftingActive.asStateFlow()

    private val _isAfkFeatherCraftingActive = MutableStateFlow(false)
    val isAfkFeatherCraftingActive: StateFlow<Boolean> = _isAfkFeatherCraftingActive.asStateFlow()

    private val _isAfkBowstringCraftingActive = MutableStateFlow(false)
    val isAfkBowstringCraftingActive: StateFlow<Boolean> = _isAfkBowstringCraftingActive.asStateFlow()

    private val _isAfkArrowtipCraftingActive = MutableStateFlow(false)
    val isAfkArrowtipCraftingActive: StateFlow<Boolean> = _isAfkArrowtipCraftingActive.asStateFlow()

    private val _isAfkTrapCraftingActive = MutableStateFlow(false)
    val isAfkTrapCraftingActive: StateFlow<Boolean> = _isAfkTrapCraftingActive.asStateFlow()

    private val _isAfkFletchingActive = MutableStateFlow(false)
    val isAfkFletchingActive: StateFlow<Boolean> = _isAfkFletchingActive.asStateFlow()

    private val _isAfkSmithingAnvilActive = MutableStateFlow(false)
    val isAfkSmithingAnvilActive: StateFlow<Boolean> = _isAfkSmithingAnvilActive.asStateFlow()

    private val _isAfkHerbCrushingActive = MutableStateFlow(false)
    val isAfkHerbCrushingActive: StateFlow<Boolean> = _isAfkHerbCrushingActive.asStateFlow()
    val isAfkHerbCleaningActive: StateFlow<Boolean> get() = _isAfkHerbCrushingActive

    private val _isAfkPotionBrewingActive = MutableStateFlow(false)
    val isAfkPotionBrewingActive: StateFlow<Boolean> = _isAfkPotionBrewingActive.asStateFlow()

    private val _isAfkDruidAltarActive = MutableStateFlow(false)
    val isAfkDruidAltarActive: StateFlow<Boolean> = _isAfkDruidAltarActive.asStateFlow()

    private val _isAfkSlayerActive = MutableStateFlow(false)
    val isAfkSlayerActive: StateFlow<Boolean> = _isAfkSlayerActive.asStateFlow()

    private val _isAfkHunterActive = MutableStateFlow(false)
    val isAfkHunterActive: StateFlow<Boolean> = _isAfkHunterActive.asStateFlow()

    private val _isAfkBossActive = MutableStateFlow(false)
    val isAfkBossActive: StateFlow<Boolean> = _isAfkBossActive.asStateFlow()

    private val _isAfkFarmingActive = MutableStateFlow(false)
    val isAfkFarmingActive: StateFlow<Boolean> = _isAfkFarmingActive.asStateFlow()

    private val _isAfkBoneBuryingActive = MutableStateFlow(false)
    val isAfkBoneBuryingActive: StateFlow<Boolean> = _isAfkBoneBuryingActive.asStateFlow()

    private val _isAfkSailingActive = MutableStateFlow(false)
    val isAfkSailingActive: StateFlow<Boolean> = _isAfkSailingActive.asStateFlow()

    private val _isAfkRunecraftingActive = MutableStateFlow(false)
    val isAfkRunecraftingActive: StateFlow<Boolean> = _isAfkRunecraftingActive.asStateFlow()

    private val _isAfkThievingActive = MutableStateFlow(false)
    val isAfkThievingActive: StateFlow<Boolean> = _isAfkThievingActive.asStateFlow()

    private val _isAfkCatacombsActive = MutableStateFlow(false)
    val isAfkCatacombsActive: StateFlow<Boolean> = _isAfkCatacombsActive.asStateFlow()
    val isAfkSepulchreActive: StateFlow<Boolean> get() = _isAfkCatacombsActive

    val isAnyActive: Boolean
        get() = _currentActivity.value != null

    fun isActivityActive(type: AfkActivityType): Boolean = _currentActivity.value == type

    /**
     * Atomically switches to a new AFK activity, updating state and all bridges in O(1).
     */
    fun startActivity(type: AfkActivityType, prefs: SharedPreferences? = null) {
        if (_currentActivity.value == type) return
        _currentActivity.value = type
        _sessionTicks.value = 0
        _activityStartTimeMs.value = System.currentTimeMillis()
        syncStateBridges(type)
        recordActivity(type.key, prefs)
        saveState(prefs)
    }

    /**
     * Atomically stops all AFK activities.
     */
    fun stopAll(prefs: SharedPreferences? = null, reason: String? = null) {
        if (_currentActivity.value == null) return
        _currentActivity.value = null
        _sessionTicks.value = 0
        syncStateBridges(null)
        saveState(prefs)
    }

    /**
     * Toggles an activity on/off. Returns true if now active, false if stopped.
     */
    fun toggleActivity(type: AfkActivityType, prefs: SharedPreferences? = null): Boolean {
        return if (_currentActivity.value == type) {
            stopAll(prefs)
            false
        } else {
            startActivity(type, prefs)
            true
        }
    }

    fun incrementTick() {
        _sessionTicks.value += 1
        _lastProcessTimeMs.value = System.currentTimeMillis()
    }

    private fun syncStateBridges(active: AfkActivityType?) {
        _isAfkCampfireActive.value = (active == AfkActivityType.CAMPFIRE)
        _isAfkCookingActive.value = (active == AfkActivityType.COOKING)
        _isAfkFishingActive.value = (active == AfkActivityType.FISHING)
        _isAfkMiningActive.value = (active == AfkActivityType.MINING)
        _isAfkSmeltingActive.value = (active == AfkActivityType.SMELTING)
        _isAfkSawmillActive.value = (active == AfkActivityType.SAWMILL)
        _isAfkWoodcuttingActive.value = (active == AfkActivityType.WOODCUTTING)
        _isAfkNailCraftingActive.value = (active == AfkActivityType.NAIL_CRAFTING)
        _isAfkStickCraftingActive.value = (active == AfkActivityType.STICK_CRAFTING)
        _isAfkShaftCraftingActive.value = (active == AfkActivityType.SHAFT_CRAFTING)
        _isAfkFeatherCraftingActive.value = (active == AfkActivityType.FEATHER_CRAFTING)
        _isAfkBowstringCraftingActive.value = (active == AfkActivityType.BOWSTRING_CRAFTING)
        _isAfkArrowtipCraftingActive.value = (active == AfkActivityType.ARROWTIP_CRAFTING)
        _isAfkTrapCraftingActive.value = (active == AfkActivityType.TRAP_CRAFTING)
        _isAfkFletchingActive.value = (active == AfkActivityType.FLETCHING)
        _isAfkSmithingAnvilActive.value = (active == AfkActivityType.SMITHING_ANVIL)
        _isAfkHerbCrushingActive.value = (active == AfkActivityType.HERB_CRUSHING)
        _isAfkPotionBrewingActive.value = (active == AfkActivityType.POTION_BREWING)
        _isAfkDruidAltarActive.value = (active == AfkActivityType.DRUID_ALTAR)
        _isAfkSlayerActive.value = (active == AfkActivityType.SLAYER)
        _isAfkHunterActive.value = (active == AfkActivityType.HUNTER)
        _isAfkBossActive.value = (active == AfkActivityType.BOSS)
        _isAfkFarmingActive.value = (active == AfkActivityType.FARMING)
        _isAfkBoneBuryingActive.value = (active == AfkActivityType.BONE_BURYING)
        _isAfkSailingActive.value = (active == AfkActivityType.SAILING)
        _isAfkRunecraftingActive.value = (active == AfkActivityType.RUNECRAFTING)
        _isAfkThievingActive.value = (active == AfkActivityType.THIEVING)
        _isAfkCatacombsActive.value = (active == AfkActivityType.CATACOMBS)
    }

    fun recordActivity(activityKey: String, prefs: SharedPreferences? = null) {
        val cleanKey = activityKey.removePrefix("afk_")
        val current = _recentHistory.value.toMutableList()
        current.remove(cleanKey)
        current.add(0, cleanKey)
        val trimmed = current.take(6)
        _recentHistory.value = trimmed
        if (prefs != null) {
            try {
                prefs.edit().putString("afk_recent_history", trimmed.joinToString(",")).apply()
            } catch (_: Exception) {}
        }
    }

    /**
     * Fast O(1) material & fuel checker for current AFK station.
     */
    fun hasMaterials(
        activity: AfkActivityType,
        itemQuantityMap: Map<String, Int>,
        selectedTrapId: String? = null,
        cookingQueue: List<String> = emptyList(),
        selectedFoodId: String? = null,
        activeSmeltRecipe: SmeltRecipe? = null,
        activeSmithAnvilRecipe: SmithAnvilRecipe? = null,
        activeFletchRecipe: FletchRecipe? = null,
        activeCrushHerbRecipe: HerbCrushingRecipe? = null,
        activePotionRecipe: PotionBrewRecipe? = null,
        hasFarmingPlotsActive: Boolean = false
    ): Boolean {
        fun getQty(id: String): Int {
            val direct = itemQuantityMap[id]
            if (direct != null && direct > 0) return direct
            val norm = DefaultItems.normalizeItemId(id)
            val byNorm = itemQuantityMap[norm]
            if (byNorm != null && byNorm > 0) return byNorm
            return itemQuantityMap.entries.firstOrNull {
                it.key.equals(id, true) || DefaultItems.normalizeItemId(it.key).equals(norm, true)
            }?.value ?: 0
        }

        return when (activity) {
            AfkActivityType.CAMPFIRE, AfkActivityType.SAWMILL, AfkActivityType.STICK_CRAFTING, AfkActivityType.SHAFT_CRAFTING -> {
                itemQuantityMap.any { (id, qty) -> qty > 0 && (id.contains("logs") || id == "item_logs") }
            }
            AfkActivityType.FEATHER_CRAFTING -> true
            AfkActivityType.BOWSTRING_CRAFTING -> {
                itemQuantityMap.any { (id, qty) -> qty > 0 && (id.contains("flax") || id.contains("fiber") || id.contains("wool") || id.contains("logs") || id == "item_logs") } || getQty("item_logs") > 0
            }
            AfkActivityType.COOKING -> {
                if (cookingQueue.isNotEmpty()) {
                    cookingQueue.any { getQty(it) > 0 }
                } else if (selectedFoodId != null) {
                    getQty(selectedFoodId) > 0
                } else {
                    false
                }
            }
            AfkActivityType.SMELTING -> {
                if (activeSmeltRecipe != null) {
                    activeSmeltRecipe.inputOres.all { ore -> getQty(ore.itemId) >= ore.quantity }
                } else {
                    itemQuantityMap.any { (id, qty) -> qty > 0 && id.endsWith("_ore") }
                }
            }
            AfkActivityType.SMITHING_ANVIL -> {
                if (activeSmithAnvilRecipe != null) {
                    getQty(activeSmithAnvilRecipe.barItemId) >= activeSmithAnvilRecipe.barsRequired
                } else true
            }
            AfkActivityType.NAIL_CRAFTING, AfkActivityType.ARROWTIP_CRAFTING -> {
                itemQuantityMap.any { (id, qty) -> qty > 0 && id.contains("bar") }
            }
            AfkActivityType.FLETCHING -> {
                if (activeFletchRecipe != null) {
                    activeFletchRecipe.inputMaterials.all { mat -> getQty(mat.itemId) >= mat.quantity }
                } else true
            }
            AfkActivityType.HERB_CRUSHING -> {
                if (activeCrushHerbRecipe != null) {
                    getQty(activeCrushHerbRecipe.herbId) > 0
                } else true
            }
            AfkActivityType.POTION_BREWING -> {
                if (activePotionRecipe != null) {
                    (getQty(activePotionRecipe.crushedHerbId) > 0 || getQty(activePotionRecipe.cleanHerbId) > 0) &&
                            getQty(activePotionRecipe.secondaryItemId) > 0
                } else true
            }
            AfkActivityType.TRAP_CRAFTING -> {
                val trap = selectedTrapId ?: "item_bird_snare"
                when (trap) {
                    "item_bird_snare", "item_noose_wand" -> itemQuantityMap.any { (id, qty) -> qty > 0 && (id.contains("logs") || id == "item_logs") }
                    "item_box_trap" -> itemQuantityMap.any { (id, qty) -> qty >= 2 && (id.contains("logs") || id == "item_logs") }
                    "item_net_trap" -> itemQuantityMap.any { (id, qty) -> qty > 0 && (id.contains("logs") || id == "item_logs") } && getQty("item_wooden_stick") > 0
                    "item_impling_net" -> getQty("item_wooden_stick") >= 2
                    else -> true
                }
            }
            AfkActivityType.BONE_BURYING -> {
                itemQuantityMap.any { (id, qty) -> qty > 0 && (id.contains("bones") || id == "item_bones") }
            }
            AfkActivityType.RUNECRAFTING -> {
                getQty("item_rune_essence") > 0 || getQty("item_pure_essence") > 0
            }
            AfkActivityType.FARMING -> {
                hasFarmingPlotsActive || itemQuantityMap.any { (id, qty) -> qty > 0 && (id.contains("seed") || id.contains("sapling")) }
            }
            else -> true
        }
    }

    /**
     * Saves consolidated AFK engine state to SharedPreferences in a single write.
     */
    fun saveState(prefs: SharedPreferences?) {
        if (prefs == null) return
        val current = _currentActivity.value
        val editor = prefs.edit()
        editor.putLong("afk_last_timestamp", System.currentTimeMillis())
        editor.putLong("afk_activity_start_time_ms", _activityStartTimeMs.value)
        editor.putString("afk_active_activity_key", current?.key)
        editor.putString("afk_active_activity_name", current?.displayName)

        // Sync legacy boolean keys for backwards-compatibility
        AfkActivityType.entries.forEach { activity ->
            editor.putBoolean("afk_${activity.key}", current == activity)
        }
        editor.apply()
    }

    /**
     * Loads AFK state from SharedPreferences, gracefully handling legacy format and modern format.
     */
    fun loadState(prefs: SharedPreferences?) {
        if (prefs == null) return
        val activeKey = prefs.getString("afk_active_activity_key", null)
        var resolvedActivity: AfkActivityType? = AfkActivityType.fromKey(activeKey)

        // Fallback: check legacy boolean keys if modern key was absent
        if (resolvedActivity == null) {
            for (activity in AfkActivityType.entries) {
                if (prefs.getBoolean("afk_${activity.key}", false)) {
                    resolvedActivity = activity
                    break
                }
            }
        }

        _currentActivity.value = resolvedActivity
        _activityStartTimeMs.value = prefs.getLong("afk_activity_start_time_ms", System.currentTimeMillis())
        _lastProcessTimeMs.value = prefs.getLong("afk_last_timestamp", System.currentTimeMillis())
        syncStateBridges(resolvedActivity)

        val savedHistoryStr = prefs.getString("afk_recent_history", null)
        if (!savedHistoryStr.isNullOrEmpty()) {
            val list = savedHistoryStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (list.isNotEmpty()) {
                _recentHistory.value = list
            }
        }
    }

    fun getDisplayName(): String? = _currentActivity.value?.displayName
    fun getEmoji(): String = _currentActivity.value?.emoji ?: "⚔️"
}
