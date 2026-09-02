package com.example.viewmodel

import com.example.engine.AfkEngine
import com.example.engine.AfkActivityType

import android.app.Application
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiAiService
import com.example.data.db.AppDatabase
import com.example.data.models.*
import com.example.data.repository.PetRepository
import com.example.sensors.StepCounterManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class LevelUpEvent(
    val skill: OsrsSkill,
    val newLevel: Int
)

data class FarmingContractState(
    val cropName: String = "Snape Grass",
    val targetQty: Int = 10,
    val currentQty: Int = 0,
    val rewardXp: Long = 5000L,
    val rewardGp: Long = 25000L,
    val rewardItemName: String = "Seed Pack (Tier 2)"
)

data class SlayerContractState(
    val monsterId: String = "crawling_hand",
    val monsterName: String = "Crawling Hand",
    val iconSymbol: String = "🖐️",
    val reqSlayerLevel: Int = 1,
    val targetKills: Int = 15,
    val currentKills: Int = 0,
    val rewardSlayerPoints: Int = 20,
    val rewardXp: Long = 2500L,
    val rewardGp: Long = 15000L
)

data class HunterContractState(
    val creatureId: String = "crimson_swift",
    val creatureName: String = "Crimson Swift",
    val iconSymbol: String = "🐦",
    val reqHunterLevel: Int = 1,
    val targetCatches: Int = 12,
    val currentCatches: Int = 0,
    val rewardXp: Long = 2000L,
    val rewardGp: Long = 12000L,
    val rewardItemName: String = "Hunter Guild Sack (Tier 1)"
)

data class ContractRewardOpenResult(
    val skill: OsrsSkill,
    val openedCount: Int,
    val totalGp: Long,
    val totalXp: Long = 0L,
    val itemsGained: List<Pair<InventoryItem, Int>>,
    val outfitPiecesUnlocked: List<com.example.data.models.SkillOutfitPiece> = emptyList()
)


class PetViewModel(application: Application) : AndroidViewModel(application) {
    val afkEngine: AfkEngine = AfkEngine


    private val db = AppDatabase.getDatabase(application)
    private val repository = PetRepository(db.petDao(), application)
    private val geminiService = GeminiAiService()
    val stepCounterManager = StepCounterManager(application)
    private val googleTasksRepository = com.example.data.repository.GoogleTasksRepository(application)

    // POH & POF Companion Preferences
    private val pohPrefs = application.getSharedPreferences("osrs_poh_house_prefs", Context.MODE_PRIVATE)
    private val pofPrefs = application.getSharedPreferences("osrs_pof_farm_prefs", Context.MODE_PRIVATE)
    private val favPrefs = application.getSharedPreferences("osrs_favorite_items_prefs", Context.MODE_PRIVATE)
    private val npcPrefs = application.getSharedPreferences("osrs_npc_settings_prefs", Context.MODE_PRIVATE)
    private val appSettingsPrefs = application.getSharedPreferences("osrs_app_settings_prefs", Context.MODE_PRIVATE)
    private val audioPlayer = com.example.audio.ForestAmbientAudioPlayer()

    @Volatile
    private var isAppInForeground: Boolean = true
    @Volatile
    private var isProcessingOfflineAfk: Boolean = false

    private val _favoriteItemIds = MutableStateFlow<Set<String>>(
        favPrefs.getStringSet("fav_item_ids", emptySet()) ?: emptySet()
    )
    val favoriteItemIds: StateFlow<Set<String>> = _favoriteItemIds.asStateFlow()

    fun toggleFavoriteItem(itemId: String) {
        val normalized = com.example.data.models.DefaultItems.normalizeItemId(itemId)
        val current = _favoriteItemIds.value.toMutableSet()
        val isFav = current.contains(normalized) || current.contains(itemId)
        if (isFav) {
            current.remove(normalized)
            current.remove(itemId)
            addChatMessage("⭐ Removed item from favorites.")
        } else {
            current.add(normalized)
            current.add(itemId)
            addChatMessage("⭐ Favorited item! Favorited items will appear first in lists.")
        }
        _favoriteItemIds.value = current
        favPrefs.edit().putStringSet("fav_item_ids", current).apply()
    }

    fun isItemFavorite(itemId: String): Boolean {
        val normalized = com.example.data.models.DefaultItems.normalizeItemId(itemId)
        val set = _favoriteItemIds.value
        return set.contains(normalized) || set.contains(itemId)
    }

    private val _googleTasks = MutableStateFlow<List<com.example.data.models.GoogleTaskItem>>(emptyList())
    val googleTasks: StateFlow<List<com.example.data.models.GoogleTaskItem>> = _googleTasks.asStateFlow()

    // UI States
    val petState: StateFlow<PetState> = repository.petState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PetState()
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val skillXpMap: StateFlow<Map<OsrsSkill, Long>> = petState.flatMapLatest { state ->
        repository.getSkillXpForPet(state.petType.name)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OsrsSkill.entries.associateWith { 0L }
    )

    val inventoryItems: StateFlow<List<InventoryItem>> = petState.flatMapLatest { state ->
        repository.getBankItems(state.petType.name).map { list ->
            list.filter { !it.id.contains("marrentil") && !it.name.contains("marrentil", ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bankItems: StateFlow<List<InventoryItem>> = petState.flatMapLatest { state ->
        repository.getBankItems(state.petType.name).map { list ->
            list.filter { !it.id.contains("marrentil") && !it.name.contains("marrentil", ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val quests: StateFlow<List<AiQuest>> = repository.quests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activityLogs: StateFlow<List<ActivityLog>> = repository.activityLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Floor Clear 99 Prizes Event
    private val _floorClearRewardEvent = MutableStateFlow<com.example.data.models.FloorClearReward?>(null)
    val floorClearRewardEvent: StateFlow<com.example.data.models.FloorClearReward?> = _floorClearRewardEvent.asStateFlow()

    fun dismissFloorClearReward() {
        _floorClearRewardEvent.value = null
    }

    // Game Chatbox Messages
    private val _chatMessages = MutableStateFlow<List<String>>(
        listOf(
            "System: Welcome to RuneCompanion!",
            "System: Complete real-life actions, walk steps, and log habits to gain OSRS XP!"
        )
    )
    val chatMessages: StateFlow<List<String>> = _chatMessages.asStateFlow()

    // Recent AFK Activities History
    private val _recentAfkHistory = MutableStateFlow<List<String>>(
        listOf("woodcutting", "fishing", "mining", "campfire", "slayer")
    )
    val recentAfkHistory: StateFlow<List<String>> = _recentAfkHistory.asStateFlow()

    fun recordAfkActivity(id: String) {
        if (id.isBlank()) return
        val current = _recentAfkHistory.value.toMutableList()
        current.remove(id)
        current.add(0, id)
        val trimmed = current.take(6)
        _recentAfkHistory.value = trimmed
        try {
            pohPrefs.edit().putString("afk_recent_history", trimmed.joinToString(",")).apply()
        } catch (_: Exception) {}
    }

    /**
     * Checks if the companion has sufficient Health and Hunger to perform an AFK or intensive action.
     * If the companion is down (0 HP) or starving (0% Hunger), immediately posts a notification and chat warning.
     */
    fun canStartAfkOrHungerAction(
        actionName: String,
        requiresHealth: Boolean = true,
        requiresHunger: Boolean = true
    ): Boolean {
        val pet = petState.value
        if (requiresHealth && pet.health <= 0) {
            val title = "💔 Companion Has No Health (0 HP)"
            val msg = "Cannot perform $actionName: ${pet.customName} has fainted with 0 HP! Feed food from your backpack or Storage to heal your companion."
            addChatMessage("💔 $msg")
            com.example.util.NotificationHelper.sendAfkNotification(getApplication(), title, msg)
            return false
        }
        if (requiresHunger && pet.hunger <= 0) {
            val title = "🍗 Companion Is Starving (0% Hunger)"
            val msg = "Cannot perform $actionName: ${pet.customName} is out of energy (0% Hunger)! Feed food from your backpack or Storage to restore energy."
            addChatMessage("⚠️ $msg")
            com.example.util.NotificationHelper.sendAfkNotification(getApplication(), title, msg)
            return false
        }
        return true
    }

    fun startAfkActivityById(id: String) {
        val actType = com.example.engine.AfkActivityType.fromKey(id)
        val displayName = actType?.displayName ?: id.replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
        if (!canStartAfkOrHungerAction(displayName)) {
            return
        }
        recordAfkActivity(id)
        when (id) {
            "woodcutting" -> toggleAfkWoodcutting()
            "fishing" -> toggleAfkFishing()
            "mining" -> toggleAfkMining()
            "campfire" -> toggleAfkCampfire()
            "cooking" -> toggleAfkCooking()
            "smelting" -> toggleAfkSmelting()
            "sawmill" -> toggleAfkSawmill()
            "fletching" -> toggleAfkFletching()
            "thieving" -> {
                val npcId = _selectedThievingNpcId.value ?: "man"
                startPickpocketingNpc(npcId)
            }
            "slayer" -> toggleAfkSlayer(_selectedSlayerMonster.value ?: com.example.data.models.SlayerData.MONSTERS.first())
            "hunter" -> toggleAfkHunter(_selectedHunterCreature.value ?: com.example.data.models.HunterData.CREATURES.first())
            "boss" -> toggleAfkBoss(_selectedBossMonster.value ?: com.example.data.models.BossData.BOSSES.first())
            "druid_altar" -> toggleAfkDruidAltar()
            "catacombs", "sepulchre" -> toggleAfkCatacombs()
            "sailing" -> toggleAfkSailing()
            "bone_burying" -> toggleAfkBoneBurying()
            "runecrafting" -> toggleAfkRunecrafting()
            "farming" -> toggleAfkFarming()
            else -> {
                addChatMessage("⚡ Unrecognized AFK activity: $id")
            }
        }
    }

    // Internal delegates propagating petTypeName to repository
    private var isAutoEquippingOrEquipping: Boolean = false

    fun calculateEquipmentScore(item: com.example.data.models.InventoryItem): Int {
        val slot = item.equipmentSlot ?: com.example.data.models.DefaultItems.getItemById(item.id).equipmentSlot ?: return 0
        var statPower = item.combatPowerBonus * 100 + item.defPowerBonus * 100
        
        if (statPower == 0) {
            statPower = (item.costGp / 10).toInt()
        }
        
        val tierBonus = when {
            item.id.contains("dragon") || item.name.contains("Dragon") -> 90000
            item.id.contains("aetherite") || item.name.contains("Aetherite") || item.id.contains("rune") || item.name.contains("Rune") -> 80000
            item.id.contains("amethyst") || item.name.contains("Amethyst") || item.id.contains("adamant") || item.name.contains("Adamant") -> 70000
            item.id.contains("opalite") || item.name.contains("Opalite") || item.id.contains("mithril") || item.name.contains("Mithril") -> 50000
            item.id.contains("gold") || item.name.contains("Gold") -> 40000
            item.id.contains("steel") || item.name.contains("Steel") -> 30000
            item.id.contains("silver") || item.name.contains("Silver") -> 20000
            item.id.contains("iron") || item.name.contains("Iron") -> 10000
            item.id.contains("bronze") || item.name.contains("Bronze") -> 5000
            else -> 0
        }
        
        val hashTieBreaker = (item.id.hashCode() and 0x7F)
        return statPower + tierBonus + hashTieBreaker
    }

    private suspend fun saveInventoryItem(itemId: String, quantity: Int) {
        saveBankItem(itemId, quantity)
    }

    suspend fun saveBankItem(itemId: String, quantity: Int) {
        val normalizedId = com.example.data.models.DefaultItems.normalizeItemId(itemId)
        val existingInBank = bankItems.value.find { it.id == normalizedId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normalizedId }
        val oldQty = existingInBank?.quantity ?: 0

        repository.saveBankItem(petState.value.petType.name, normalizedId, quantity)

        if (quantity > oldQty && !isAutoEquippingOrEquipping) {
            val catalogItem = com.example.data.models.DefaultItems.getItemById(normalizedId)
            val slot = catalogItem.equipmentSlot ?: com.example.data.models.DefaultItems.getItemById(catalogItem.id).equipmentSlot

            if (slot != null && (catalogItem.category == com.example.data.models.ItemCategory.EQUIPMENT || catalogItem.combatPowerBonus > 0 || catalogItem.defPowerBonus > 0 || catalogItem.equipmentSlot != null)) {
                val currentEquipped = equippedItems.value[slot]

                if (currentEquipped == null) {
                    isAutoEquippingOrEquipping = true
                    try {
                        if (quantity <= 1) {
                            repository.saveBankItem(petState.value.petType.name, normalizedId, 0)
                        } else {
                            repository.saveBankItem(petState.value.petType.name, normalizedId, quantity - 1)
                        }
                        repository.equipItem(petState.value.petType.name, slot, catalogItem)
                        addChatMessage("🛡️ AUTO-EQUIPPED: ${catalogItem.name} into empty ${slot.displayName} slot!")
                    } finally {
                        isAutoEquippingOrEquipping = false
                    }
                } else {
                    val currentScore = calculateEquipmentScore(currentEquipped)
                    val newScore = calculateEquipmentScore(catalogItem)

                    if (newScore > currentScore) {
                        isAutoEquippingOrEquipping = true
                        try {
                            val oldInBank = bankItems.value.find { it.id == currentEquipped.id || com.example.data.models.DefaultItems.normalizeItemId(it.id) == currentEquipped.id }?.quantity ?: 0
                            repository.saveBankItem(petState.value.petType.name, currentEquipped.id, oldInBank + 1)

                            if (quantity <= 1) {
                                repository.saveBankItem(petState.value.petType.name, normalizedId, 0)
                            } else {
                                repository.saveBankItem(petState.value.petType.name, normalizedId, quantity - 1)
                            }

                            repository.equipItem(petState.value.petType.name, slot, catalogItem)
                            addChatMessage("⚡ GEAR UPGRADE: Auto-swapped ${currentEquipped.name} for superior ${catalogItem.name} (+${newScore - currentScore} rating)!")
                        } finally {
                            isAutoEquippingOrEquipping = false
                        }
                    }
                }
            }
        }
    }
    private fun isItemMatch(itemIdInStorage: String, targetId: String, normTarget: String): Boolean {
        if (itemIdInStorage == targetId) return true
        if (itemIdInStorage == normTarget) return true
        val normStorage = com.example.data.models.DefaultItems.normalizeItemId(itemIdInStorage)
        if (normStorage == normTarget) return true
        return false
    }

    fun getInventoryQuantity(itemId: String): Int {
        return getBankQuantity(itemId)
    }

    fun getBankQuantity(itemId: String): Int {
        val normTarget = com.example.data.models.DefaultItems.normalizeItemId(itemId)
        val match = bankItems.value.find { isItemMatch(it.id, itemId, normTarget) }
        return match?.quantity ?: 0
    }

    fun getItemQuantityCombined(itemId: String): Int {
        return getBankQuantity(itemId)
    }

    fun getItemQuantity(itemId: String): Int {
        return getBankQuantity(itemId)
    }

    suspend fun deductItemCombined(itemId: String, amount: Int, itemName: String? = null): Boolean {
        if (amount <= 0) return true
        val normTarget = com.example.data.models.DefaultItems.normalizeItemId(itemId)
        val currentTotal = getBankQuantity(normTarget)
        if (currentTotal < amount) return false

        val success = repository.deductItem(petState.value.petType.name, normTarget, amount, itemName)
        return success
    }

    fun addItemToBank(itemId: String, quantityToAdd: Int) {
        if (quantityToAdd <= 0) return
        val normId = com.example.data.models.DefaultItems.normalizeItemId(itemId)
        val curQty = getBankQuantity(normId)
        viewModelScope.launch {
            saveBankItem(normId, curQty + quantityToAdd)
        }
    }
    private suspend fun depositItemToBank(itemId: String, qtyToDeposit: Int) {
        repository.depositItemToBank(petState.value.petType.name, itemId, qtyToDeposit)
    }

    private suspend fun depositAllInventoryToBankInternal() {
        repository.depositAllInventoryToBank(petState.value.petType.name)
    }

    private suspend fun withdrawItemFromBank(itemId: String, qtyToWithdraw: Int) {
        repository.withdrawItemFromBank(petState.value.petType.name, itemId, qtyToWithdraw)
    }

    private suspend fun equipItem(slot: com.example.data.models.EquipmentSlot, item: com.example.data.models.InventoryItem) {
        repository.equipItem(petState.value.petType.name, slot, item)
    }

    private suspend fun unequipSlot(slot: com.example.data.models.EquipmentSlot) {
        repository.unequipSlot(petState.value.petType.name, slot)
    }

    private suspend fun saveQuestProgress(progress: com.example.data.db.QuestProgressEntity) {
        repository.saveQuestProgress(progress.copy(petTypeName = petState.value.petType.name))
    }

    private suspend fun deleteQuestProgress(questId: String) {
        repository.deleteQuestProgress(petState.value.petType.name, questId)
    }

    // Level up popup dialog event
    private val _levelUpEvent = MutableStateFlow<LevelUpEvent?>(null)
    val levelUpEvent: StateFlow<LevelUpEvent?> = _levelUpEvent.asStateFlow()

    // AI loading state
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Food Bag Eat Priority State (true = Eat Highest HP First, false = Eat Lowest HP First)
    private val _foodBagEatHighestFirst = MutableStateFlow(pohPrefs.getBoolean("food_bag_eat_highest_first", true))
    val foodBagEatHighestFirst: StateFlow<Boolean> = _foodBagEatHighestFirst.asStateFlow()

    // Queued Food Preference
    private val _preferredQueuedFoodId = MutableStateFlow<String?>(pohPrefs.getString("preferred_queued_food_id", null))
    val preferredQueuedFoodId: StateFlow<String?> = _preferredQueuedFoodId.asStateFlow()

    fun setPreferredQueuedFood(foodId: String?) {
        _preferredQueuedFoodId.value = foodId
        if (foodId != null) {
            pohPrefs.edit().putString("preferred_queued_food_id", foodId).apply()
            val foodItem = (inventoryItems.value + bankItems.value).find { it.id == foodId }
                ?: com.example.data.models.CookingRecipes.ALL_COOKED_FOOD_ITEMS.find { it.id == foodId }
                ?: com.example.data.models.DefaultItems.getItemById(foodId)
            addChatMessage("🍗 Food Queue: Set ${foodItem.name} ${foodItem.iconEmoji} as next queued meal for your companion!")
        } else {
            pohPrefs.edit().remove("preferred_queued_food_id").apply()
            addChatMessage("🍗 Food Queue: Reset to auto-select best available food.")
        }
    }

    // Last Used Spirit Totem Preference
    private val _lastUsedTotemId = MutableStateFlow(pohPrefs.getString("last_used_totem_id", "summon_dreadfowl") ?: "summon_dreadfowl")
    val lastUsedTotemId: StateFlow<String> = _lastUsedTotemId.asStateFlow()

    fun setLastUsedTotem(animalIdOrTotemId: String) {
        val cleanId = if (animalIdOrTotemId.startsWith("item_totem_")) animalIdOrTotemId.removePrefix("item_totem_") else animalIdOrTotemId
        val animal = com.example.data.models.SummoningData.ALL_ANIMALS.find { it.id == cleanId }
        if (animal != null) {
            _lastUsedTotemId.value = animal.id
            pohPrefs.edit().putString("last_used_totem_id", animal.id).apply()
            addChatMessage("🗿 Totem Selected: ${animal.name} Totem ${animal.iconEmoji} set as quick-use totem!")
        }
    }

    fun getQueuedFoodItem(): InventoryItem? {
        val baseCookedList = (com.example.data.models.CookingRecipes.ALL_COOKED_FOOD_ITEMS + 
            com.example.data.models.DefaultItems.ALL_SHOP_ITEMS.filter { it.isCookedReadyToEatFood })
            .distinctBy { it.id }

        val playerItems = (inventoryItems.value + bankItems.value).distinctBy { it.id }
        val allFoodIds = (baseCookedList.map { it.id } + playerItems.filter { it.isCookedReadyToEatFood }.map { it.id }).distinct()

        val allCookedFoods = allFoodIds.mapNotNull { foodId ->
            val template = baseCookedList.find { it.id == foodId }
                ?: playerItems.find { it.id == foodId }
                ?: com.example.data.models.DefaultItems.getItemById(foodId)

            val normId = com.example.data.models.DefaultItems.normalizeItemId(foodId)
            val totalStock = playerItems.filter {
                it.id == foodId || it.id == normId ||
                com.example.data.models.DefaultItems.normalizeItemId(it.id) == normId ||
                it.name.equals(template.name, ignoreCase = true)
            }.sumOf { it.quantity }

            template.copy(quantity = totalStock)
        }

        val prefId = _preferredQueuedFoodId.value
        if (prefId != null) {
            val prefNorm = com.example.data.models.DefaultItems.normalizeItemId(prefId)
            val pref = allCookedFoods.find { it.id == prefId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == prefNorm }
            if (pref != null) return pref
        }

        val inStock = allCookedFoods.filter { it.quantity > 0 }
        return if (inStock.isNotEmpty()) {
            if (_foodBagEatHighestFirst.value) {
                inStock.maxByOrNull { it.healHp }
            } else {
                inStock.minByOrNull { it.healHp }
            }
        } else {
            allCookedFoods.firstOrNull()
        }
    }

    fun getLastUsedTotemAnimal(): com.example.data.models.SummonableAnimal {
        val id = _lastUsedTotemId.value
        return com.example.data.models.SummoningData.ALL_ANIMALS.find { it.id == id }
            ?: com.example.data.models.SummoningData.ALL_ANIMALS.first()
    }

    fun getTotemItemCount(animalId: String): Int {
        val cleanId = animalId.removePrefix("item_totem_")
        val totemId = "item_totem_$cleanId"
        val invQty = inventoryItems.value.find { it.id == totemId }?.quantity ?: 0
        val bankQty = bankItems.value.find { it.id == totemId }?.quantity ?: 0
        return invQty + bankQty
    }

    fun isTotemUnlocked(totemId: String?): Boolean {
        if (totemId.isNullOrBlank()) return true
        val cleanId = totemId.removePrefix("item_totem_")
            .removePrefix("item_badge_")
            .removePrefix("item_obelisk_")
            .removePrefix("item_")
            .lowercase()

        // 1. Check Shared Preferences
        val set = pohPrefs.getStringSet("unlocked_totem_ids", emptySet()) ?: emptySet()
        if (set.contains(cleanId) ||
            set.contains("item_totem_$cleanId") ||
            set.contains("item_badge_$cleanId") ||
            set.contains("item_obelisk_$cleanId") ||
            set.contains(totemId)
        ) return true

        // 2. Check Completed Quests in PetState (Shaman Path & Gym Quests)
        val completedQuests = petState.value.completedQuestIds
        if (completedQuests.contains(totemId) || completedQuests.contains(cleanId)) return true

        val isGymQuestCompleted = when (cleanId) {
            "woodland", "boulder", "pewter" -> completedQuests.contains("tl_kanto_4_pewter_gym") || completedQuests.contains("quest_kanto_gym_1_brock")
            "mist_fen", "cascade", "cerulean" -> completedQuests.contains("tl_kanto_7_cerulean_gym") || completedQuests.contains("quest_kanto_gym_2_misty")
            "ancient_crag", "thunder", "vermilion" -> completedQuests.contains("tl_kanto_11_vermilion_gym") || completedQuests.contains("quest_kanto_gym_3_lt_surge")
            "sacred_grove", "rainbow", "celadon" -> completedQuests.contains("tl_kanto_14_celadon_gym") || completedQuests.contains("quest_kanto_gym_4_erika")
            "ember_spirit", "soul", "fuchsia" -> completedQuests.contains("tl_kanto_18_fuchsia_gym") || completedQuests.contains("quest_kanto_gym_5_koga")
            "celestial_canopy", "marsh", "saffron" -> completedQuests.contains("tl_kanto_20_saffron_gym") || completedQuests.contains("quest_kanto_gym_6_sabrina")
            "astral_bloom", "volcano", "cinnabar" -> completedQuests.contains("tl_kanto_23_cinnabar_gym") || completedQuests.contains("quest_kanto_gym_7_blaine")
            "sovereign_wild", "earth", "viridian" -> completedQuests.contains("tl_kanto_24_viridian_gym") || completedQuests.contains("quest_kanto_gym_8_giovanni")
            "zephyr", "violet" -> completedQuests.contains("tl_johto_3_violet_gym")
            "hive", "azalea" -> completedQuests.contains("tl_johto_5_azalea_gym")
            "plain", "goldenrod" -> completedQuests.contains("tl_johto_6_goldenrod_gym")
            "fog", "ecruteak" -> completedQuests.contains("tl_johto_8_ecruteak_gym")
            "mineral", "olivine" -> completedQuests.contains("tl_johto_9_olivine_gym")
            "rising", "blackthorn" -> completedQuests.contains("tl_johto_11_blackthorn_gym")
            "stone", "rustboro" -> completedQuests.contains("tl_hoenn_3_rustboro_gym")
            "knuckle", "dewford" -> completedQuests.contains("tl_hoenn_4_dewford_gym")
            "dynamo", "mauville" -> completedQuests.contains("tl_hoenn_5_mauville_gym")
            "heat", "lavaridge" -> completedQuests.contains("tl_hoenn_6_lavaridge_gym")
            "feather", "fortree" -> completedQuests.contains("tl_hoenn_8_fortree_gym")
            "mind", "mossdeep" -> completedQuests.contains("tl_hoenn_9_mossdeep_gym")
            "rain", "sootopolis" -> completedQuests.contains("tl_hoenn_10_sootopolis_gym")
            "coal", "oreburgh" -> completedQuests.contains("tl_sinnoh_3_oreburgh_gym") || completedQuests.contains("quest_sinnoh_gym_1_oreburgh")
            "forest", "eterna" -> completedQuests.contains("tl_sinnoh_4_eterna_gym") || completedQuests.contains("quest_sinnoh_gym_2_eterna")
            "cobble", "veilstone" -> completedQuests.contains("tl_sinnoh_5_veilstone_gym") || completedQuests.contains("quest_sinnoh_gym_3_veilstone")
            "fen", "pastoria" -> completedQuests.contains("tl_sinnoh_6_pastoria_gym") || completedQuests.contains("quest_sinnoh_gym_4_pastoria")
            "relic", "hearthome" -> completedQuests.contains("tl_sinnoh_7_hearthome_gym") || completedQuests.contains("quest_sinnoh_gym_5_hearthome")
            "mine", "canalave" -> completedQuests.contains("tl_sinnoh_8_canalave_gym") || completedQuests.contains("quest_sinnoh_gym_6_canalave")
            "icicle", "snowpoint" -> completedQuests.contains("tl_sinnoh_9_snowpoint_gym") || completedQuests.contains("quest_sinnoh_gym_7_snowpoint")
            "beacon", "sunyshore" -> completedQuests.contains("tl_sinnoh_10_sunyshore_gym") || completedQuests.contains("quest_sinnoh_gym_8_sunyshore")
            "league", "kanto_champion" -> completedQuests.contains("tl_kanto_30_champion")
            "johto_crown", "johto_champion" -> completedQuests.contains("tl_johto_16_e4_lance") || completedQuests.contains("tl_johto_17_champion_red")
            "hoenn_crown", "hoenn_champion" -> completedQuests.contains("tl_hoenn_16_champion")
            "sinnoh_crown", "sinnoh_champion" -> completedQuests.contains("tl_sinnoh_16_champion")
            else -> false
        }
        if (isGymQuestCompleted) return true

        // Check if any quest completed rewarded this totem or cleanId
        for (qId in completedQuests) {
            val q = com.example.data.models.TrainerLeagueData.getQuestById(qId)
                ?: com.example.data.models.OsrsQuestData.ALL_QUESTS.find { it.id == qId }
            if (q?.rewardItemId != null) {
                val rClean = q.rewardItemId.removePrefix("item_totem_")
                    .removePrefix("item_badge_")
                    .removePrefix("item_obelisk_")
                    .removePrefix("item_")
                    .lowercase()
                if (rClean == cleanId || q.rewardItemId == totemId || q.rewardItemId == "item_totem_$cleanId" || q.rewardItemId == "item_badge_$cleanId") {
                    return true
                }
            }
        }

        // 3. Check Inventory and Bank Items
        val hasInInvOrBank = getItemQuantityCombined("item_totem_$cleanId") > 0 ||
                             getItemQuantityCombined("item_badge_$cleanId") > 0 ||
                             getItemQuantityCombined("item_obelisk_$cleanId") > 0 ||
                             getItemQuantityCombined("item_$cleanId") > 0 ||
                             getItemQuantityCombined(cleanId) > 0 ||
                             getItemQuantityCombined(totemId) > 0

        if (hasInInvOrBank) return true

        // 4. Check mapped aliases for inventory/bank items
        val aliasHasItem = when (cleanId) {
            "woodland" -> getItemQuantityCombined("item_badge_boulder") > 0 || getItemQuantityCombined("item_totem_woodland") > 0
            "boulder" -> getItemQuantityCombined("item_totem_woodland") > 0 || getItemQuantityCombined("item_badge_boulder") > 0
            "mist_fen" -> getItemQuantityCombined("item_badge_cascade") > 0 || getItemQuantityCombined("item_totem_mist_fen") > 0
            "cascade" -> getItemQuantityCombined("item_totem_mist_fen") > 0 || getItemQuantityCombined("item_badge_cascade") > 0
            "ancient_crag" -> getItemQuantityCombined("item_badge_thunder") > 0 || getItemQuantityCombined("item_totem_ancient_crag") > 0
            "thunder" -> getItemQuantityCombined("item_totem_ancient_crag") > 0 || getItemQuantityCombined("item_badge_thunder") > 0
            "sacred_grove" -> getItemQuantityCombined("item_badge_rainbow") > 0 || getItemQuantityCombined("item_totem_sacred_grove") > 0
            "rainbow" -> getItemQuantityCombined("item_totem_sacred_grove") > 0 || getItemQuantityCombined("item_badge_rainbow") > 0
            "ember_spirit" -> getItemQuantityCombined("item_badge_soul") > 0 || getItemQuantityCombined("item_totem_ember_spirit") > 0
            "soul" -> getItemQuantityCombined("item_totem_ember_spirit") > 0 || getItemQuantityCombined("item_badge_soul") > 0
            "celestial_canopy" -> getItemQuantityCombined("item_badge_marsh") > 0 || getItemQuantityCombined("item_totem_celestial_canopy") > 0
            "marsh" -> getItemQuantityCombined("item_totem_celestial_canopy") > 0 || getItemQuantityCombined("item_badge_marsh") > 0
            "astral_bloom" -> getItemQuantityCombined("item_badge_volcano") > 0 || getItemQuantityCombined("item_totem_astral_bloom") > 0
            "volcano" -> getItemQuantityCombined("item_totem_astral_bloom") > 0 || getItemQuantityCombined("item_badge_volcano") > 0
            "sovereign_wild" -> getItemQuantityCombined("item_badge_earth") > 0 || getItemQuantityCombined("item_totem_sovereign_wild") > 0
            "earth" -> getItemQuantityCombined("item_totem_sovereign_wild") > 0 || getItemQuantityCombined("item_badge_earth") > 0
            else -> false
        }

        return aliasHasItem
    }

    fun markTotemUnlocked(totemId: String) {
        val cleanId = totemId.removePrefix("item_totem_")
            .removePrefix("item_badge_")
            .removePrefix("item_obelisk_")
            .removePrefix("item_")
            .lowercase()
        val currentSet = pohPrefs.getStringSet("unlocked_totem_ids", emptySet()) ?: emptySet()
        val extraAliases = when (cleanId) {
            "woodland", "boulder" -> setOf("woodland", "boulder", "item_totem_woodland", "item_badge_boulder", "item_obelisk_woodland", "item_obelisk_boulder")
            "mist_fen", "cascade" -> setOf("mist_fen", "cascade", "item_totem_mist_fen", "item_badge_cascade", "item_obelisk_mist_fen", "item_obelisk_cascade")
            "ancient_crag", "thunder" -> setOf("ancient_crag", "thunder", "item_totem_ancient_crag", "item_badge_thunder", "item_obelisk_ancient_crag", "item_obelisk_thunder")
            "sacred_grove", "rainbow" -> setOf("sacred_grove", "rainbow", "item_totem_sacred_grove", "item_badge_rainbow", "item_obelisk_sacred_grove", "item_obelisk_rainbow")
            "ember_spirit", "soul" -> setOf("ember_spirit", "soul", "item_totem_ember_spirit", "item_badge_soul", "item_obelisk_ember_spirit", "item_obelisk_soul")
            "celestial_canopy", "marsh" -> setOf("celestial_canopy", "marsh", "item_totem_celestial_canopy", "item_badge_marsh", "item_obelisk_celestial_canopy", "item_obelisk_marsh")
            "astral_bloom", "volcano" -> setOf("astral_bloom", "volcano", "item_totem_astral_bloom", "item_badge_volcano", "item_obelisk_astral_bloom", "item_obelisk_volcano")
            "sovereign_wild", "earth" -> setOf("sovereign_wild", "earth", "item_totem_sovereign_wild", "item_badge_earth", "item_obelisk_sovereign_wild", "item_obelisk_earth")
            "tl_kanto_4_pewter_gym", "quest_kanto_gym_1_brock" -> setOf("woodland", "boulder", "item_totem_woodland", "item_badge_boulder", "item_obelisk_woodland", "item_obelisk_boulder")
            "tl_kanto_7_cerulean_gym", "quest_kanto_gym_2_misty" -> setOf("mist_fen", "cascade", "item_totem_mist_fen", "item_badge_cascade", "item_obelisk_mist_fen", "item_obelisk_cascade")
            "tl_kanto_11_vermilion_gym", "quest_kanto_gym_3_lt_surge" -> setOf("ancient_crag", "thunder", "item_totem_ancient_crag", "item_badge_thunder", "item_obelisk_ancient_crag", "item_obelisk_thunder")
            "tl_kanto_14_celadon_gym", "quest_kanto_gym_4_erika" -> setOf("sacred_grove", "rainbow", "item_totem_sacred_grove", "item_badge_rainbow", "item_obelisk_sacred_grove", "item_obelisk_rainbow")
            "tl_kanto_18_fuchsia_gym", "quest_kanto_gym_5_koga" -> setOf("ember_spirit", "soul", "item_totem_ember_spirit", "item_badge_soul", "item_obelisk_ember_spirit", "item_obelisk_soul")
            "tl_kanto_20_saffron_gym", "quest_kanto_gym_6_sabrina" -> setOf("celestial_canopy", "marsh", "item_totem_celestial_canopy", "item_badge_marsh", "item_obelisk_celestial_canopy", "item_obelisk_marsh")
            "tl_kanto_23_cinnabar_gym", "quest_kanto_gym_7_blaine" -> setOf("astral_bloom", "volcano", "item_totem_astral_bloom", "item_badge_volcano", "item_obelisk_astral_bloom", "item_obelisk_volcano")
            "tl_kanto_24_viridian_gym", "quest_kanto_gym_8_giovanni" -> setOf("sovereign_wild", "earth", "item_totem_sovereign_wild", "item_badge_earth", "item_obelisk_sovereign_wild", "item_obelisk_earth")
            "tl_johto_3_violet_gym" -> setOf("zephyr", "item_badge_zephyr", "item_totem_zephyr", "item_obelisk_zephyr")
            "tl_johto_5_azalea_gym" -> setOf("hive", "item_badge_hive", "item_totem_hive", "item_obelisk_hive")
            "tl_johto_6_goldenrod_gym" -> setOf("plain", "item_badge_plain", "item_totem_plain", "item_obelisk_plain")
            "tl_johto_8_ecruteak_gym" -> setOf("fog", "item_badge_fog", "item_totem_fog", "item_obelisk_fog")
            "tl_johto_9_olivine_gym" -> setOf("mineral", "item_badge_mineral", "item_totem_mineral", "item_obelisk_mineral")
            "tl_johto_11_blackthorn_gym" -> setOf("rising", "item_badge_rising", "item_totem_rising", "item_obelisk_rising")
            "tl_hoenn_3_rustboro_gym" -> setOf("stone", "item_badge_stone", "item_totem_stone", "item_obelisk_stone")
            "tl_hoenn_4_dewford_gym" -> setOf("knuckle", "item_badge_knuckle", "item_totem_knuckle", "item_obelisk_knuckle")
            "tl_hoenn_5_mauville_gym" -> setOf("dynamo", "item_badge_dynamo", "item_totem_dynamo", "item_obelisk_dynamo")
            "tl_hoenn_6_lavaridge_gym" -> setOf("heat", "item_badge_heat", "item_totem_heat", "item_obelisk_heat")
            "tl_hoenn_8_fortree_gym" -> setOf("feather", "item_badge_feather", "item_totem_feather", "item_obelisk_feather")
            "tl_hoenn_9_mossdeep_gym" -> setOf("mind", "item_badge_mind", "item_totem_mind", "item_obelisk_mind")
            "tl_hoenn_10_sootopolis_gym" -> setOf("rain", "item_badge_rain", "item_totem_rain", "item_obelisk_rain")
            "tl_sinnoh_3_oreburgh_gym", "quest_sinnoh_gym_1_oreburgh" -> setOf("coal", "item_badge_coal", "item_totem_coal", "item_obelisk_coal")
            "tl_sinnoh_4_eterna_gym", "quest_sinnoh_gym_2_eterna" -> setOf("forest", "item_badge_forest", "item_totem_forest", "item_obelisk_forest")
            "tl_sinnoh_5_veilstone_gym", "quest_sinnoh_gym_3_veilstone" -> setOf("cobble", "item_badge_cobble", "item_totem_cobble", "item_obelisk_cobble")
            "tl_sinnoh_6_pastoria_gym", "quest_sinnoh_gym_4_pastoria" -> setOf("fen", "item_badge_fen", "item_totem_fen", "item_obelisk_fen")
            "tl_sinnoh_7_hearthome_gym", "quest_sinnoh_gym_5_hearthome" -> setOf("relic", "item_badge_relic", "item_totem_relic", "item_obelisk_relic")
            "tl_sinnoh_8_canalave_gym", "quest_sinnoh_gym_6_canalave" -> setOf("mine", "item_badge_mine", "item_totem_mine", "item_obelisk_mine")
            "tl_sinnoh_9_snowpoint_gym", "quest_sinnoh_gym_7_snowpoint" -> setOf("icicle", "item_badge_icicle", "item_totem_icicle", "item_obelisk_icicle")
            "tl_sinnoh_10_sunyshore_gym", "quest_sinnoh_gym_8_sunyshore" -> setOf("beacon", "item_badge_beacon", "item_totem_beacon", "item_obelisk_beacon")
            "tl_kanto_30_champion" -> setOf("league", "item_badge_league")
            "tl_johto_16_e4_lance", "tl_johto_17_champion_red" -> setOf("johto_crown", "item_badge_johto_crown")
            "tl_hoenn_16_champion" -> setOf("hoenn_crown", "item_badge_hoenn_crown")
            "tl_sinnoh_16_champion" -> setOf("sinnoh_crown", "item_badge_sinnoh_crown")
            else -> emptySet()
        }
        val newSet = currentSet + cleanId + "item_totem_$cleanId" + "item_badge_$cleanId" + "item_obelisk_$cleanId" + totemId + extraAliases
        pohPrefs.edit().putStringSet("unlocked_totem_ids", newSet).apply()
    }

    fun toggleFoodBagEatOrder() {
        val newVal = !_foodBagEatHighestFirst.value
        _foodBagEatHighestFirst.value = newVal
        pohPrefs.edit().putBoolean("food_bag_eat_highest_first", newVal).apply()
        addChatMessage(
            if (newVal) "🎒 Food Bag Priority: Set to ⬆️ EAT HIGHEST HEALING FOOD FIRST"
            else "🎒 Food Bag Priority: Set to ⬇️ EAT LOWEST HEALING FOOD FIRST"
        )
    }

    // POH Companion House State Persistence
    private fun loadPohHouseState(petTypeName: String = PetType.BABY_BLACK_DRAGON.name): PohHouseState {
        val key = "poh_state_json_$petTypeName"
        val jsonStr = pohPrefs.getString(key, null)
            ?: (if (petTypeName == PetType.BABY_BLACK_DRAGON.name) pohPrefs.getString("poh_state_json", null) else null)
            ?: return PohHouseState()
        return try {
            val root = JSONObject(jsonStr)
            val roomsArray = root.optJSONArray("builtRooms") ?: JSONArray()
            val roomsList = mutableListOf<BuiltRoom>()
            for (i in 0 until roomsArray.length()) {
                val rObj = roomsArray.getJSONObject(i)
                val id = rObj.getString("id")
                val roomTypeName = rObj.getString("roomType")
                val roomType = try { 
                    if (roomTypeName == "GRASS_PATH") PohRoomType.GRASS_PATCH
                    else if (roomTypeName == "STONE_PATH") PohRoomType.PARLOUR
                    else PohRoomType.valueOf(roomTypeName) 
                } catch (e: Exception) { PohRoomType.PARLOUR }
                val furnArray = rObj.optJSONArray("builtFurnitureIds") ?: JSONArray()
                val furnList = mutableListOf<String>()
                for (j in 0 until furnArray.length()) {
                    furnList.add(furnArray.getString(j))
                }
                val gridPos = rObj.optInt("gridPosition", 4)
                val wallNorth = com.example.data.models.PohWallType.fromName(rObj.optString("wallNorth", "WOOD_PLANK"))
                val wallEast = com.example.data.models.PohWallType.fromName(rObj.optString("wallEast", "WOOD_PLANK"))
                val wallSouth = com.example.data.models.PohWallType.fromName(rObj.optString("wallSouth", "WOOD_PLANK"))
                val wallWest = com.example.data.models.PohWallType.fromName(rObj.optString("wallWest", "WOOD_PLANK"))
                val floorType = com.example.data.models.PohFloorType.fromName(rObj.optString("floorType", "DEFAULT_WOOD"))
                roomsList.add(BuiltRoom(id, roomType, furnList, gridPos, wallNorth, wallEast, wallSouth, wallWest, floorType))
            }

            val matsObj = root.optJSONObject("materialInventory") ?: JSONObject()
            val matsMap = mutableMapOf<GeMaterial, Int>()
            val keys = matsObj.keys()
            while (keys.hasNext()) {
                val keyName = keys.next()
                try {
                    val mat = GeMaterial.valueOf(keyName)
                    val qty = matsObj.getInt(keyName)
                    matsMap[mat] = qty
                } catch (e: Exception) {}
            }

            val extraGrid = root.optInt("extraGridSize", 0)

            PohHouseState(
                builtRooms = if (roomsList.isEmpty()) PohHouseState().builtRooms else roomsList,
                materialInventory = matsMap,
                extraGridSize = extraGrid
            )
        } catch (e: Exception) {
            PohHouseState()
        }
    }

    private fun savePohHouseState(petTypeName: String, state: PohHouseState) {
        try {
            val root = JSONObject()
            val roomsArray = JSONArray()
            state.builtRooms.forEach { room ->
                val rObj = JSONObject()
                rObj.put("id", room.id)
                rObj.put("roomType", room.roomType.name)
                val furnArray = JSONArray()
                room.builtFurnitureIds.forEach { fId -> furnArray.put(fId) }
                rObj.put("builtFurnitureIds", furnArray)
                rObj.put("gridPosition", room.gridPosition)
                rObj.put("wallNorth", room.wallNorth.name)
                rObj.put("wallEast", room.wallEast.name)
                rObj.put("wallSouth", room.wallSouth.name)
                rObj.put("wallWest", room.wallWest.name)
                rObj.put("floorType", room.floorType.name)
                roomsArray.put(rObj)
            }
            root.put("builtRooms", roomsArray)

            val matsObj = JSONObject()
            state.materialInventory.forEach { (mat, qty) ->
                matsObj.put(mat.name, qty)
            }
            root.put("materialInventory", matsObj)
            root.put("extraGridSize", state.extraGridSize)

            val key = "poh_state_json_$petTypeName"
            pohPrefs.edit().putString(key, root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePohHouseState(newState: PohHouseState) {
        val petTypeName = petState.value.petType.name
        _pohHouseState.value = newState
        savePohHouseState(petTypeName, newState)
    }

    // POF Companion Farming & Animal Husbandry State Persistence
    private fun loadPofState(petTypeName: String = PetType.BABY_BLACK_DRAGON.name): com.example.data.models.PlayerOwnedFarmState {
        val key = "pof_state_json_$petTypeName"
        val jsonStr = pofPrefs.getString(key, null)
            ?: (if (petTypeName == PetType.BABY_BLACK_DRAGON.name) pofPrefs.getString("pof_state_json", null) else null)
            ?: return com.example.data.models.PlayerOwnedFarmState()
        return try {
            val root = JSONObject(jsonStr)
            val scarecrow = root.optBoolean("scarecrowBuilt", false)
            val compostLevel = root.optInt("compostBinLevel", 1)
            val compostBuckets = root.optInt("compostBucketsCount", 10)
            val totalHarvested = root.optInt("totalCropsHarvested", 0)
            val totalContracts = root.optInt("totalContractsCompleted", 0)

            val plotsArray = root.optJSONArray("plots") ?: JSONArray()
            val plotsList = mutableListOf<com.example.data.models.FarmPlotState>()
            for (i in 0 until plotsArray.length()) {
                val pObj = plotsArray.getJSONObject(i)
                val pIdx = pObj.optInt("plotIndex", i)
                val cropName = pObj.optString("cropType", "")
                val cropType = if (cropName.isNotEmpty()) {
                    try { com.example.data.models.FarmCropType.valueOf(cropName) } catch (e: Exception) { null }
                } else null
                val plantedMs = pObj.optLong("plantedTimestampMs", 0L)
                val isWatered = pObj.optBoolean("isWatered", false)
                val isComposted = pObj.optBoolean("isComposted", false)
                plotsList.add(
                    com.example.data.models.FarmPlotState(
                        plotIndex = pIdx,
                        cropType = cropType,
                        plantedTimestampMs = plantedMs,
                        isWatered = isWatered,
                        isComposted = isComposted
                    )
                )
            }

            if (plotsList.isEmpty()) {
                plotsList.addAll(com.example.data.models.PlayerOwnedFarmState().plots)
            } else {
                while (plotsList.size < 12) {
                    plotsList.add(com.example.data.models.FarmPlotState(plotIndex = plotsList.size))
                }
            }

            // Parse Animal Husbandry State
            val husbandryObj = root.optJSONObject("husbandryState")
            val troughFood = husbandryObj?.optInt("troughFoodPct", 100) ?: 100
            val storedCompost = husbandryObj?.optInt("storedCompost", 0) ?: 0
            val storedProduceMap = mutableMapOf<String, Int>()
            val produceObj = husbandryObj?.optJSONObject("storedProduce")
            if (produceObj != null) {
                produceObj.keys().forEach { k ->
                    storedProduceMap[k] = produceObj.optInt(k, 0)
                }
            }
            val animalsList = mutableListOf<com.example.data.models.FarmAnimalInstance>()
            val animalsArray = husbandryObj?.optJSONArray("animals")
            if (animalsArray != null) {
                for (i in 0 until animalsArray.length()) {
                    val aObj = animalsArray.getJSONObject(i)
                    val instId = aObj.optString("instanceId", "animal_$i")
                    val tId = aObj.optString("typeId", "livestock_chicken")
                    val cName = aObj.optString("customName", "Animal")
                    val pTime = aObj.optLong("purchasedTimestampMs", System.currentTimeMillis())
                    val lTime = aObj.optLong("lastProducedTimestampMs", System.currentTimeMillis())
                    animalsList.add(
                        com.example.data.models.FarmAnimalInstance(
                            instanceId = instId,
                            typeId = tId,
                            customName = cName,
                            purchasedTimestampMs = pTime,
                            lastProducedTimestampMs = lTime
                        )
                    )
                }
            }

            com.example.data.models.PlayerOwnedFarmState(
                plots = plotsList.take(12),
                scarecrowBuilt = scarecrow,
                compostBinLevel = compostLevel,
                compostBucketsCount = compostBuckets,
                totalCropsHarvested = totalHarvested,
                totalContractsCompleted = totalContracts,
                husbandryState = com.example.data.models.AnimalHusbandryState(
                    animals = animalsList,
                    troughFoodPct = troughFood,
                    storedCompost = storedCompost,
                    storedProduce = storedProduceMap
                )
            )
        } catch (e: Exception) {
            com.example.data.models.PlayerOwnedFarmState()
        }
    }

    private fun savePofState(petTypeName: String, state: com.example.data.models.PlayerOwnedFarmState) {
        try {
            val root = JSONObject()
            root.put("scarecrowBuilt", state.scarecrowBuilt)
            root.put("compostBinLevel", state.compostBinLevel)
            root.put("compostBucketsCount", state.compostBucketsCount)
            root.put("totalCropsHarvested", state.totalCropsHarvested)
            root.put("totalContractsCompleted", state.totalContractsCompleted)

            val plotsArray = JSONArray()
            state.plots.forEach { p ->
                val pObj = JSONObject()
                pObj.put("plotIndex", p.plotIndex)
                pObj.put("cropType", p.cropType?.name ?: "")
                pObj.put("plantedTimestampMs", p.plantedTimestampMs)
                pObj.put("isWatered", p.isWatered)
                pObj.put("isComposted", p.isComposted)
                plotsArray.put(pObj)
            }
            root.put("plots", plotsArray)

            // Save Animal Husbandry State
            val husbandryObj = JSONObject()
            husbandryObj.put("troughFoodPct", state.husbandryState.troughFoodPct)
            husbandryObj.put("storedCompost", state.husbandryState.storedCompost)
            val produceJson = JSONObject()
            state.husbandryState.storedProduce.forEach { (k, v) -> produceJson.put(k, v) }
            husbandryObj.put("storedProduce", produceJson)
            val animalsArray = JSONArray()
            state.husbandryState.animals.forEach { a ->
                val aObj = JSONObject()
                aObj.put("instanceId", a.instanceId)
                aObj.put("typeId", a.typeId)
                aObj.put("customName", a.customName)
                aObj.put("purchasedTimestampMs", a.purchasedTimestampMs)
                aObj.put("lastProducedTimestampMs", a.lastProducedTimestampMs)
                animalsArray.put(aObj)
            }
            husbandryObj.put("animals", animalsArray)
            root.put("husbandryState", husbandryObj)

            val key = "pof_state_json_$petTypeName"
            pofPrefs.edit().putString(key, root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePofState(newState: com.example.data.models.PlayerOwnedFarmState) {
        val petTypeName = petState.value.petType.name
        _pofState.value = newState
        savePofState(petTypeName, newState)
    }

    // POH Companion House State
    private val _pohHouseState = MutableStateFlow(PohHouseState())
    val pohHouseState: StateFlow<PohHouseState> = _pohHouseState.asStateFlow()

    // AFK House Stations state (Campfire, Cooking Range, Fishing Pond, Mining Quarry, Smelting Furnace, Sawmill)
    val isAfkCampfireActive: StateFlow<Boolean> get() = AfkEngine.isAfkCampfireActive

    val isAfkCookingActive: StateFlow<Boolean> get() = AfkEngine.isAfkCookingActive

    private val _adventuringCombatStance = MutableStateFlow(pohPrefs.getString("adventuring_combat_stance", "MELEE") ?: "MELEE") // "MELEE", "RANGED", "MAGIC"
    val adventuringCombatStance: StateFlow<String> = _adventuringCombatStance.asStateFlow()

    private val _elementalEnergyMap = MutableStateFlow<Map<String, Long>>(
        com.example.data.models.EnergyType.entries.associate { type ->
            type.name to pohPrefs.getLong("elemental_energy_${type.name.lowercase()}", 0L)
        }
    )
    val elementalEnergyMap: StateFlow<Map<String, Long>> = _elementalEnergyMap.asStateFlow()

    val isAfkFishingActive: StateFlow<Boolean> get() = AfkEngine.isAfkFishingActive

    val isAfkMiningActive: StateFlow<Boolean> get() = AfkEngine.isAfkMiningActive

    val isAfkSmeltingActive: StateFlow<Boolean> get() = AfkEngine.isAfkSmeltingActive

    val isAfkSawmillActive: StateFlow<Boolean> get() = AfkEngine.isAfkSawmillActive

    val isAfkWoodcuttingActive: StateFlow<Boolean> get() = AfkEngine.isAfkWoodcuttingActive

    val isAfkNailCraftingActive: StateFlow<Boolean> get() = AfkEngine.isAfkNailCraftingActive

    val isAfkTrapCraftingActive: StateFlow<Boolean> get() = AfkEngine.isAfkTrapCraftingActive

    private val _selectedCraftingTrapId = MutableStateFlow("item_bird_snare")
    val selectedCraftingTrapId: StateFlow<String> = _selectedCraftingTrapId.asStateFlow()

    private val _selectedSawmillPlankId = MutableStateFlow("item_plank")
    val selectedSawmillPlankId: StateFlow<String> = _selectedSawmillPlankId.asStateFlow()

    private val _selectedNailBarId = MutableStateFlow("item_bronze_bar")
    val selectedNailBarId: StateFlow<String> = _selectedNailBarId.asStateFlow()

    private val _selectedStickLogId = MutableStateFlow("item_logs")
    val selectedStickLogId: StateFlow<String> = _selectedStickLogId.asStateFlow()

    private val _selectedShaftLogId = MutableStateFlow("item_logs")
    val selectedShaftLogId: StateFlow<String> = _selectedShaftLogId.asStateFlow()

    private val _selectedArrowtipBarId = MutableStateFlow("item_bronze_bar")
    val selectedArrowtipBarId: StateFlow<String> = _selectedArrowtipBarId.asStateFlow()

    private val _selectedArrowFletchBarId = MutableStateFlow("item_bronze_bar")
    val selectedArrowFletchBarId: StateFlow<String> = _selectedArrowFletchBarId.asStateFlow()

    // Station Target Selections
    private val _selectedFishId = MutableStateFlow<String?>(null)
    val selectedFishId: StateFlow<String?> = _selectedFishId.asStateFlow()

    private val _selectedFoodId = MutableStateFlow<String?>(null)
    val selectedFoodId: StateFlow<String?> = _selectedFoodId.asStateFlow()

    private val _cookingQueue = MutableStateFlow<List<String>>(emptyList())
    val cookingQueue: StateFlow<List<String>> = _cookingQueue.asStateFlow()

    private val _selectedOreId = MutableStateFlow<String?>(null)
    val selectedOreId: StateFlow<String?> = _selectedOreId.asStateFlow()

    private val _selectedBarId = MutableStateFlow<String?>(null)
    val selectedBarId: StateFlow<String?> = _selectedBarId.asStateFlow()

    private val _selectedTreeId = MutableStateFlow<String?>(null)
    val selectedTreeId: StateFlow<String?> = _selectedTreeId.asStateFlow()

    private val _selectedGroveForestId = MutableStateFlow("forest_sylvan")
    val selectedGroveForestId: StateFlow<String> = _selectedGroveForestId.asStateFlow()

    private val _selectedGemologyAreaId = MutableStateFlow("quarry_surface")
    val selectedGemologyAreaId: StateFlow<String> = _selectedGemologyAreaId.asStateFlow()

    private val _afkActivityStartTimeMs = MutableStateFlow(0L)
    val afkActivityStartTimeMs: StateFlow<Long> = _afkActivityStartTimeMs.asStateFlow()

    private val _afkLastProcessTimeMs = MutableStateFlow(0L)
    val afkLastProcessTimeMs: StateFlow<Long> = _afkLastProcessTimeMs.asStateFlow()

    fun selectGemologyArea(areaId: String) {
        val quarry = com.example.data.models.AdventuringStoryData.GEMOLOGY_AREAS.find { it.id == areaId } ?: return
        val minXp = skillXpMap.value[OsrsSkill.SMITHING] ?: 0L
        val minLvl = OsrsXpCalculator.getLevelForXp(minXp)
        if (minLvl < quarry.reqLevel) {
            addChatMessage("🔒 Cannot switch area: Level ${quarry.reqLevel} Forging required for ${quarry.name} (You are Level $minLvl)!")
            return
        }
        if (!isTotemUnlocked(quarry.reqTotemId)) {
            val reqName = quarry.reqTotemName ?: "Obelisk"
            addChatMessage("🗿 Obelisk Locked: Requires the $reqName to access ${quarry.name}!")
            return
        }
        _selectedGemologyAreaId.value = areaId
        if (isAfkMiningActive.value) {
            addChatMessage("⛏️ AFK Mining switched to ${quarry.emoji} ${quarry.name}! (Drops randomized from area drop table)")
        } else {
            addChatMessage("⛏️ Selected Quarry Area: ${quarry.emoji} ${quarry.name}")
        }
        saveAfkStateToPrefs()
    }

    // Thieving & Shamanic Catacombs State
    val isAfkThievingActive: StateFlow<Boolean> get() = AfkEngine.isAfkThievingActive

    private val _selectedThievingNpcId = MutableStateFlow("man")
    val selectedThievingNpcId: StateFlow<String> = _selectedThievingNpcId.asStateFlow()

    val isAfkCatacombsActive: StateFlow<Boolean> get() = AfkEngine.isAfkCatacombsActive
    val isAfkSepulchreActive: StateFlow<Boolean> get() = AfkEngine.isAfkSepulchreActive

    // Guild Contracts State
    private val _farmingContract = MutableStateFlow(FarmingContractState())
    val farmingContract: StateFlow<FarmingContractState> = _farmingContract.asStateFlow()

    // Daily Spirit Quests State
    private val _dailySpiritQuests = MutableStateFlow<List<com.example.data.models.DailySpiritQuest>>(
        com.example.data.models.DailySpiritQuestGenerator.generateDailyQuests(3)
    )
    val dailySpiritQuests: StateFlow<List<com.example.data.models.DailySpiritQuest>> = _dailySpiritQuests.asStateFlow()

    private val _showDailySpiritQuestsDialog = MutableStateFlow(false)
    val showDailySpiritQuestsDialog: StateFlow<Boolean> = _showDailySpiritQuestsDialog.asStateFlow()

    fun setShowDailySpiritQuestsDialog(show: Boolean) {
        _showDailySpiritQuestsDialog.value = show
    }

    fun refreshDailySpiritQuests() {
        _dailySpiritQuests.value = com.example.data.models.DailySpiritQuestGenerator.generateDailyQuests(3)
        addChatMessage("🎲 SPIRIT GUIDANCE: Received 3 new randomized Daily Spirit Quests!")
    }

    fun progressDailySpiritQuest(taskType: com.example.data.models.SpiritQuestTaskType, amount: Int = 1) {
        val currentList = _dailySpiritQuests.value
        var updatedAny = false
        val newList = currentList.map { quest ->
            if (quest.taskType == taskType && !quest.isCompleted) {
                updatedAny = true
                val newProg = (quest.currentProgress + amount).coerceAtMost(quest.requiredAmount)
                val isComp = newProg >= quest.requiredAmount
                if (isComp && !quest.isCompleted) {
                    addChatMessage("🌟 DAILY SPIRIT QUEST COMPLETE: '${quest.title}'! Ready to claim reward.")
                }
                quest.copy(
                    currentProgress = newProg,
                    isCompleted = isComp
                )
            } else {
                quest
            }
        }
        if (updatedAny) {
            _dailySpiritQuests.value = newList
        }
    }

    fun incrementQuestProgressManual(questId: String, amount: Int = 1) {
        val currentList = _dailySpiritQuests.value
        val newList = currentList.map { quest ->
            if (quest.id == questId && !quest.isCompleted) {
                val newProg = (quest.currentProgress + amount).coerceAtMost(quest.requiredAmount)
                val isComp = newProg >= quest.requiredAmount
                if (isComp && !quest.isCompleted) {
                    addChatMessage("🌟 DAILY SPIRIT QUEST COMPLETE: '${quest.title}'! Ready to claim reward.")
                }
                quest.copy(
                    currentProgress = newProg,
                    isCompleted = isComp
                )
            } else {
                quest
            }
        }
        _dailySpiritQuests.value = newList
    }

    fun claimDailySpiritQuestReward(questId: String) {
        val quest = _dailySpiritQuests.value.find { it.id == questId } ?: return
        if (!quest.isCompleted || quest.isClaimed) return

        viewModelScope.launch {
            val currentPet = petState.value
            val updatedPet = currentPet.copy(
                happiness = (currentPet.happiness + 25).coerceAtMost(100),
                energy = (currentPet.energy + 35).coerceAtMost(100),
                coinsGp = currentPet.coinsGp + quest.rewardLifeEnergy
            )
            repository.savePetState(updatedPet)

            saveInventoryItem(quest.rewardItemId, quest.rewardItemQty)

            _dailySpiritQuests.value = _dailySpiritQuests.value.map {
                if (it.id == questId) it.copy(isClaimed = true) else it
            }

            addChatMessage("🎉 REWARD CLAIMED: '${quest.title}'! +${quest.rewardLifeEnergy} Life Energy/GP, +${quest.rewardItemQty} ${quest.rewardItemName} ${quest.rewardItemEmoji}!")
        }
    }

    // Giant Cauldron State
    private val _cauldronFoodId = MutableStateFlow<String?>(null)
    val cauldronFoodId: StateFlow<String?> = _cauldronFoodId.asStateFlow()

    private val _cauldronFoodName = MutableStateFlow<String?>("Raw Food")
    val cauldronFoodName: StateFlow<String?> = _cauldronFoodName.asStateFlow()

    private val _cauldronFoodEmoji = MutableStateFlow("🐟")
    val cauldronFoodEmoji: StateFlow<String> = _cauldronFoodEmoji.asStateFlow()

    private val _cauldronUncookedCount = MutableStateFlow(0)
    val cauldronUncookedCount: StateFlow<Int> = _cauldronUncookedCount.asStateFlow()

    private val _cauldronCookedCount = MutableStateFlow(0)
    val cauldronCookedCount: StateFlow<Int> = _cauldronCookedCount.asStateFlow()

    private val _cauldronProgress = MutableStateFlow(0.0f)
    val cauldronProgress: StateFlow<Float> = _cauldronProgress.asStateFlow()

    private val _isCauldronAfkActive = MutableStateFlow(false)
    val isCauldronAfkActive: StateFlow<Boolean> = _isCauldronAfkActive.asStateFlow()

    private val _selectedCauldronRecipe = MutableStateFlow<com.example.data.models.CauldronRecipe>(com.example.data.models.CauldronRecipes.ALL_RECIPES.first())
    val selectedCauldronRecipe: StateFlow<com.example.data.models.CauldronRecipe> = _selectedCauldronRecipe.asStateFlow()

    private val _activeCookingBuffs = MutableStateFlow<List<com.example.data.models.ActiveCookingBuff>>(emptyList())
    val activeCookingBuffs: StateFlow<List<com.example.data.models.ActiveCookingBuff>> = _activeCookingBuffs.asStateFlow()

    private val _activeSkillXpBoostDesc = MutableStateFlow<String?>(null)
    val activeSkillXpBoostDesc: StateFlow<String?> = _activeSkillXpBoostDesc.asStateFlow()

    private val _activeSkillXpBoostExpiry = MutableStateFlow<Long>(0L)
    val activeSkillXpBoostExpiry: StateFlow<Long> = _activeSkillXpBoostExpiry.asStateFlow()

    private val _activeSkillXpBoostSkill = MutableStateFlow<OsrsSkill?>(null)
    val activeSkillXpBoostSkill: StateFlow<OsrsSkill?> = _activeSkillXpBoostSkill.asStateFlow()

    fun getMaxCookingBuffSlots(): Int {
        val cookXp = skillXpMap.value[com.example.data.models.OsrsSkill.COOKING] ?: 0L
        val cookLvl = com.example.data.models.OsrsXpCalculator.getLevelForXp(cookXp)
        return when {
            cookLvl >= 99 -> 5
            cookLvl >= 90 -> 4
            cookLvl >= 60 -> 3
            cookLvl >= 20 -> 2
            else -> 1
        }
    }

    fun selectCauldronRecipe(recipe: com.example.data.models.CauldronRecipe) {
        _selectedCauldronRecipe.value = recipe
        addChatMessage("🫕 Cauldron Active Recipe set to: ${recipe.name} ${recipe.emoji} (${recipe.buffEffect})")
    }

    private fun getIngredientAliasIds(id: String): List<String> {
        return when (id) {
            "item_logs" -> listOf("item_logs", "item_regular_logs")
            "item_oak_logs", "item_oak_plank" -> listOf("item_oak_logs", "item_oak_plank")
            "item_willow_logs" -> listOf("item_willow_logs")
            "item_teak_plank", "item_teak_logs" -> listOf("item_teak_plank", "item_teak_logs")
            "item_maple_logs" -> listOf("item_maple_logs")
            "item_mahogany_logs", "item_mahogany_plank" -> listOf("item_mahogany_logs", "item_mahogany_plank")
            "item_yew_logs" -> listOf("item_yew_logs")
            "item_magic_logs" -> listOf("item_magic_logs")
            "item_redwood_logs" -> listOf("item_redwood_logs")
            "item_potato" -> listOf("item_potato", "item_raw_potato")
            "item_onion" -> listOf("item_onion", "item_raw_onion")
            "item_cabbage", "item_lettuce" -> listOf("item_cabbage", "item_lettuce", "item_raw_cabbage")
            "item_carrot" -> listOf("item_carrot", "item_raw_carrot")
            "item_tomato" -> listOf("item_tomato", "item_raw_tomato")
            "item_sweetcorn" -> listOf("item_sweetcorn", "item_raw_sweetcorn")
            "item_strawberry" -> listOf("item_strawberry", "item_raw_strawberry")
            "item_pumpkin" -> listOf("item_pumpkin", "item_raw_pumpkin")
            "item_watermelon" -> listOf("item_watermelon", "item_raw_watermelon")
            "item_clean_greenleaf" -> listOf("item_clean_greenleaf", "item_greenleaf")
            "item_clean_meadow_mint" -> listOf("item_clean_meadow_mint", "item_meadow_mint")
            "item_clean_wild_thyme", "item_wild_thyme", "item_thistle" -> listOf("item_clean_wild_thyme", "item_wild_thyme", "item_thistle")
            "item_clean_lavender" -> listOf("item_clean_lavender", "item_lavender")
            "item_clean_sunleaf" -> listOf("item_clean_sunleaf", "item_sunleaf")
            "item_clean_ironleaf" -> listOf("item_clean_ironleaf", "item_ironleaf")
            "item_clean_wintergreen" -> listOf("item_clean_wintergreen", "item_wintergreen")
            "item_clean_elderberry" -> listOf("item_clean_elderberry", "item_elderberry")
            "item_clean_silverleaf" -> listOf("item_clean_silverleaf", "item_silverleaf")
            "item_clean_moonflower" -> listOf("item_clean_moonflower", "item_moonflower")
            "item_clean_chamomile" -> listOf("item_clean_chamomile", "item_chamomile")
            "item_clean_vervain" -> listOf("item_clean_vervain", "item_vervain")
            "item_clean_mystic_sage" -> listOf("item_clean_mystic_sage", "item_mystic_sage")
            "item_raw_shrimps", "item_shrimps" -> listOf("item_raw_shrimps", "item_shrimps", "item_cooked_shrimps")
            "item_raw_trout", "item_trout" -> listOf("item_raw_trout", "item_trout", "item_cooked_trout")
            "item_raw_salmon", "item_salmon" -> listOf("item_raw_salmon", "item_salmon", "item_cooked_salmon")
            "item_raw_lobster", "item_lobster" -> listOf("item_raw_lobster", "item_lobster", "item_cooked_lobster")
            "item_raw_swordfish", "item_swordfish" -> listOf("item_raw_swordfish", "item_swordfish", "item_cooked_swordfish")
            "item_raw_shark", "item_shark" -> listOf("item_raw_shark", "item_shark", "item_cooked_shark")
            "item_manta_ray", "item_raw_manta_ray" -> listOf("item_manta_ray", "item_raw_manta_ray", "item_cooked_manta_ray")
            else -> listOf(id)
        }
    }

    fun getItemQuantityWithAliases(id: String): Int {
        val aliases = getIngredientAliasIds(id)
        return aliases.sumOf { getItemQuantityCombined(it) }
    }

    private suspend fun deductItemWithAliases(id: String, amount: Int): Boolean {
        var remaining = amount
        val aliases = getIngredientAliasIds(id)
        for (alias in aliases) {
            if (remaining <= 0) break
            val qty = getItemQuantityCombined(alias)
            if (qty > 0) {
                val toDeduct = minOf(remaining, qty)
                val success = deductItemCombined(alias, toDeduct)
                if (success) {
                    remaining -= toDeduct
                }
            }
        }
        return remaining <= 0
    }

    fun cookRecipeFromBankAndInventory(recipe: com.example.data.models.CauldronRecipe) {
        viewModelScope.launch {
            val qty1 = getItemQuantityWithAliases(recipe.requiredRawItemId)
            val qty2 = getItemQuantityWithAliases(recipe.requiredItem2Id)
            val setsAvailable = minOf(qty1, qty2)

            if (setsAvailable <= 0) {
                addChatMessage("⚠️ Missing Cauldron Ingredients! Requires: 1x ${recipe.rawItemName} + 1x ${recipe.item2Name} in Inventory or Bank Vault.")
                return@launch
            }

            val success1 = deductItemWithAliases(recipe.requiredRawItemId, setsAvailable)
            val success2 = deductItemWithAliases(recipe.requiredItem2Id, setsAvailable)

            if (success1 && success2) {
                _selectedCauldronRecipe.value = recipe
                _cauldronFoodId.value = recipe.id
                _cauldronFoodName.value = recipe.cookedItemName
                _cauldronFoodEmoji.value = recipe.emoji
                _cauldronCookedCount.value += setsAvailable
                
                // Deposit cooked food directly into bank vault
                val emberLvl = npcFavorMap.value["ember"]?.first ?: getNpcFavorLevel("ember")
                var bonusProduced = 0
                for (i in 1..setsAvailable) {
                    if ((1..100).random() <= emberLvl) {
                        bonusProduced++
                    }
                }
                val totalProduced = setsAvailable + bonusProduced
                val existingInBank = bankItems.value.find { it.id == recipe.id }?.quantity ?: 0
                saveBankItem(recipe.id, existingInBank + totalProduced)
                if (bonusProduced > 0) {
                    addChatMessage("✨ [Ember's Favor Perk (+${emberLvl}%)]: Double cooked feast! (+${bonusProduced} extra ${recipe.cookedItemName}) 🔥🍲")
                }
                addChatMessage("🫕 Spirit Cauldron: Cooked $totalProduced x ${recipe.cookedItemName} ${recipe.emoji}! Deposited into Bank & Food Bag.")
                progressSkillContract(OsrsSkill.COOKING, setsAvailable, recipe.id)
            }
        }
    }

    fun activateCauldronRecipeSkillBoost(recipe: com.example.data.models.CauldronRecipe) {
        val now = System.currentTimeMillis()
        val durationMs = 8 * 3_600_000L // 8 hours
        val expiry = now + durationMs

        _activeSkillXpBoostDesc.value = recipe.skillBoostDescription
        _activeSkillXpBoostExpiry.value = expiry
        _activeSkillXpBoostSkill.value = recipe.boostedSkill

        // Food meal buffs do NOT stack with each other (1 active food meal buff at a time),
        // but CAN stack with all other non-food buffs (incantations, pet mood, equipment, etc.).
        val newBuff = com.example.data.models.ActiveCookingBuff(
            recipeId = recipe.id,
            recipeName = recipe.name,
            emoji = recipe.emoji,
            buffEffect = recipe.buffEffect,
            expiryTimeMs = expiry,
            boostedSkill = recipe.boostedSkill,
            xpBoostPercent = recipe.xpBoostPercent,
            durationHours = 8
        )
        _activeCookingBuffs.value = listOf(newBuff)
        addChatMessage("✨ ACTIVATED MEAL BUFF (8 HRS): ${recipe.name} ${recipe.emoji} - ${recipe.buffEffect}")
    }

    fun toggleCauldronAfk() {
        _isCauldronAfkActive.value = !_isCauldronAfkActive.value
        if (_isCauldronAfkActive.value) {
            addChatMessage("🔥 AFK Cauldron Cooking: ENABLED! The Cauldron will cook continuously and auto-refill from your backpack.")
        } else {
            addChatMessage("🧊 AFK Cauldron Cooking: DISABLED.")
        }
    }

    fun addFoodToCauldron(item: com.example.data.models.InventoryItem, amount: Int = 1) {
        val qtyToAdd = amount.coerceAtMost(item.quantity)
        if (qtyToAdd <= 0) return

        viewModelScope.launch {
            saveInventoryItem(item.id, item.quantity - qtyToAdd)
        }

        if (_cauldronUncookedCount.value == 0 && _cauldronCookedCount.value == 0) {
            _cauldronFoodId.value = item.id
            _cauldronFoodName.value = item.name
            _cauldronFoodEmoji.value = item.iconEmoji
        }

        _cauldronUncookedCount.value += qtyToAdd
        addChatMessage("🫕 Giant Cauldron: Added $qtyToAdd x ${item.name} into the bubbling cauldron!")
        progressDailySpiritQuest(com.example.data.models.SpiritQuestTaskType.FISH_POOL, qtyToAdd)
    }

    fun addRecipePairToCauldron(recipe: com.example.data.models.CauldronRecipe, amount: Int = 1) {
        val item1 = inventoryItems.value.find { it.id == recipe.requiredRawItemId }
        val item2 = inventoryItems.value.find { it.id == recipe.requiredItem2Id }

        val avail1 = item1?.quantity ?: 0
        val avail2 = item2?.quantity ?: 0
        val maxPairs = avail1.coerceAtMost(avail2)

        val pairsToAdd = amount.coerceAtMost(maxPairs)
        if (pairsToAdd <= 0) {
            addChatMessage("⚠️ Requires 1x ${recipe.rawItemName} AND 1x ${recipe.item2Name} in inventory to load ${recipe.name}!")
            return
        }

        viewModelScope.launch {
            if (item1 != null) saveInventoryItem(item1.id, item1.quantity - pairsToAdd)
            if (item2 != null) saveInventoryItem(item2.id, item2.quantity - pairsToAdd)
        }

        if (_cauldronUncookedCount.value == 0 && _cauldronCookedCount.value == 0) {
            _cauldronFoodId.value = recipe.id
            _cauldronFoodName.value = recipe.name
            _cauldronFoodEmoji.value = recipe.emoji
        }

        _cauldronUncookedCount.value += pairsToAdd
        addChatMessage("🫕 Giant Cauldron: Loaded $pairsToAdd x Recipe Pair (${recipe.rawItemName} + ${recipe.item2Name}) for ${recipe.name} ${recipe.emoji}!")
        progressDailySpiritQuest(com.example.data.models.SpiritQuestTaskType.FISH_POOL, pairsToAdd)
    }

    fun claimCauldronCookedFood() {
        val count = _cauldronCookedCount.value
        if (count <= 0) return

        val recipe = _selectedCauldronRecipe.value
        val cookedCode = recipe.id

        val existingInBank = bankItems.value.find { it.id == cookedCode }?.quantity ?: 0
        viewModelScope.launch {
            saveBankItem(cookedCode, existingInBank + count)
        }

        addChatMessage("🍲 Claimed $count x ${recipe.cookedItemName} from the Giant Cauldron! Deposited into Bank Vault & 🎒 Food Bag.")
        _cauldronCookedCount.value = 0
        if (_cauldronUncookedCount.value == 0) {
            _cauldronFoodId.value = null
        }
    }

    private val _slayerContract = MutableStateFlow(SlayerContractState())
    val slayerContract: StateFlow<SlayerContractState> = _slayerContract.asStateFlow()

    private val _hunterContract = MutableStateFlow(HunterContractState())
    val hunterContract: StateFlow<HunterContractState> = _hunterContract.asStateFlow()

    private val _storageSelectedSubTab = MutableStateFlow(0)
    val storageSelectedSubTab: StateFlow<Int> = _storageSelectedSubTab.asStateFlow()

    fun setStorageSubTab(subTab: Int) {
        _storageSelectedSubTab.value = subTab.coerceIn(0, 2)
    }

    private val _contractsMap = MutableStateFlow<Map<OsrsSkill, com.example.data.models.SkillContract>>(emptyMap())
    val contractsMap: StateFlow<Map<OsrsSkill, com.example.data.models.SkillContract>> = _contractsMap.asStateFlow()

    private val _selectedContractCategory = MutableStateFlow("All")
    val selectedContractCategory: StateFlow<String> = _selectedContractCategory.asStateFlow()

    fun setContractCategoryFilter(category: String) {
        _selectedContractCategory.value = category
    }

    private val _contractsNoticeDismissedDay = MutableStateFlow("")
    val contractsNoticeDismissedDay: StateFlow<String> = _contractsNoticeDismissedDay.asStateFlow()

    fun dismissContractsDailyNotice() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        pohPrefs.edit().putString("contracts_notice_dismissed_date", today).apply()
        _contractsNoticeDismissedDay.value = today
    }

    private val _lastContractRewardResult = MutableStateFlow<ContractRewardOpenResult?>(null)
    val lastContractRewardResult: StateFlow<ContractRewardOpenResult?> = _lastContractRewardResult.asStateFlow()

    // NPC Favor System State
    private val _npcFavorMap = MutableStateFlow<Map<String, Pair<Int, Long>>>(
        listOf("eric", "bram", "finbar", "arlg", "ren", "elena", "selene", "malakor", "bryan").associate { npcId ->
            val lvl = pohPrefs.getInt("npc_favor_lvl_$npcId", 1)
            val xp = pohPrefs.getLong("npc_favor_xp_$npcId", 0L)
            npcId to Pair(lvl, xp)
        }
    )
    val npcFavorMap: StateFlow<Map<String, Pair<Int, Long>>> = _npcFavorMap.asStateFlow()
    private val minimizedNpcs = mutableSetOf<String>()

    // Equipment Loadouts State
    private val _equipmentLoadouts = MutableStateFlow<List<com.example.data.models.EquipmentLoadout>>(emptyList())
    val equipmentLoadouts: StateFlow<List<com.example.data.models.EquipmentLoadout>> = _equipmentLoadouts.asStateFlow()



    private val _selectedRunecraftRuneId = MutableStateFlow("item_air_rune")
    val selectedRunecraftRuneId: StateFlow<String> = _selectedRunecraftRuneId.asStateFlow()


    // Summoning Active Companion State
    private val _activeSummon = MutableStateFlow<com.example.data.models.ActiveSummoningCompanion?>(null)
    val activeSummon: StateFlow<com.example.data.models.ActiveSummoningCompanion?> = _activeSummon.asStateFlow()
    private var summonJob: Job? = null

    // Active Golem Worker State (Second AFK Worker)
    private val _activeGolem = MutableStateFlow<com.example.data.models.ActiveGolemState?>(null)
    val activeGolem: StateFlow<com.example.data.models.ActiveGolemState?> = _activeGolem.asStateFlow()
    private var golemJob: Job? = null

    // Druid Altar AFK Crafting State
    val isAfkDruidAltarActive: StateFlow<Boolean> get() = AfkEngine.isAfkDruidAltarActive

    private val _selectedDruidEffigyId = MutableStateFlow("item_effigy_air")
    val selectedDruidEffigyId: StateFlow<String> = _selectedDruidEffigyId.asStateFlow()

    private val _selectedDruidRuneId = MutableStateFlow("item_rune_air")
    val selectedDruidRuneId: StateFlow<String> = _selectedDruidRuneId.asStateFlow()

    private val _selectedDruidLogId = MutableStateFlow("item_logs")
    val selectedDruidLogId: StateFlow<String> = _selectedDruidLogId.asStateFlow()

    private val _selectedDruidNailId = MutableStateFlow("item_nails")
    val selectedDruidNailId: StateFlow<String> = _selectedDruidNailId.asStateFlow()

    private val _druidAltarProgress = MutableStateFlow(0f)
    val druidAltarProgress: StateFlow<Float> = _druidAltarProgress.asStateFlow()

    private val _druidAltarCraftedCount = MutableStateFlow(0)
    val druidAltarCraftedCount: StateFlow<Int> = _druidAltarCraftedCount.asStateFlow()

    private var afkTickCount = 0

    fun setSelectedFishId(fishId: String?) {
        if (fishId != null) {
            val fishXp = skillXpMap.value[OsrsSkill.FISHING] ?: 0L
            val fishLvl = OsrsXpCalculator.getLevelForXp(fishXp)
            val fishData = com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS
                .flatMap { it.catchableFish }
                .find { it.id == fishId }
            val reqLvl = fishData?.reqLevel ?: 1
            if (fishLvl < reqLvl) {
                addChatMessage("🔒 Cannot select ${fishData?.name ?: "fish"}: Requires Level $reqLvl Fishing (You are Level $fishLvl)!")
                return
            }
        }
        _selectedFishId.value = fishId
        val fishName = com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS
            .flatMap { it.catchableFish }
            .find { it.id == fishId }?.name ?: "None"
        addChatMessage("🎣 Target Fishing Selection: $fishName")
        saveAfkStateToPrefs()
    }

    fun setSelectedFoodId(foodId: String?) {
        if (foodId != null) {
            val cookXp = skillXpMap.value[OsrsSkill.COOKING] ?: 0L
            val cookLvl = OsrsXpCalculator.getLevelForXp(cookXp)
            val reqLvl = when (foodId) {
                "item_raw_trout" -> 15
                "item_raw_salmon" -> 25
                "item_raw_lobster" -> 40
                "item_raw_swordfish" -> 45
                "item_raw_shark" -> 80
                else -> 1
            }
            if (cookLvl < reqLvl) {
                addChatMessage("🔒 Cannot select food: Requires Level $reqLvl Cooking (You are Level $cookLvl)!")
                return
            }
        }
        _selectedFoodId.value = if (_selectedFoodId.value == foodId) null else foodId
        val name = _selectedFoodId.value?.replace("item_raw_", "")?.replace("_", " ")?.uppercase() ?: "NONE"
        addChatMessage("🍳 Target Cooking Selection: $name")
        saveAfkStateToPrefs()
    }

    fun summonAnimal(animal: com.example.data.models.SummonableAnimal) {
        val currentSummoningXp = skillXpMap.value[OsrsSkill.FIREMAKING] ?: 0L
        val level = OsrsXpCalculator.getLevelForXp(currentSummoningXp)

        if (level < animal.levelRequired) {
            addChatMessage("🔒 Cannot summon ${animal.name}: Requires Level ${animal.levelRequired} Summoning (You are Level $level)!")
            return
        }

        val inv = inventoryItems.value
        val bank = bankItems.value
        for ((effigyId, reqCount) in animal.requiredEffigies) {
            val invQty = inv.find { it.id == effigyId }?.quantity ?: 0
            val bankQty = bank.find { it.id == effigyId }?.quantity ?: 0
            if ((invQty + bankQty) < reqCount) {
                val effigyItem = com.example.data.models.DefaultItems.ALL_SHOP_ITEMS.find { it.id == effigyId }
                val effigyName = effigyItem?.name ?: effigyId
                addChatMessage("❌ Missing required effigies: $effigyName x$reqCount for ${animal.name}!")
                return
            }
        }

        viewModelScope.launch {
            for ((effigyId, reqCount) in animal.requiredEffigies) {
                var remainingNeeded = reqCount
                val bagItem = inventoryItems.value.find { it.id == effigyId }
                if (bagItem != null && bagItem.quantity > 0) {
                    val takenFromBag = bagItem.quantity.coerceAtMost(remainingNeeded)
                    val newBagQty = bagItem.quantity - takenFromBag
                    remainingNeeded -= takenFromBag
                    if (newBagQty <= 0) {
                        repository.deleteInventoryItem(petState.value.petType.name, effigyId)
                    } else {
                        saveInventoryItem(effigyId, newBagQty)
                    }
                }
                if (remainingNeeded > 0) {
                    val bItem = bankItems.value.find { it.id == effigyId }
                    if (bItem != null && bItem.quantity > 0) {
                        val takenFromBank = bItem.quantity.coerceAtMost(remainingNeeded)
                        val newBankQty = bItem.quantity - takenFromBank
                        if (newBankQty <= 0) {
                            saveBankItem(effigyId, 0)
                        } else {
                            saveBankItem(effigyId, newBankQty)
                        }
                    }
                }
            }

            val totemId = "item_totem_${animal.id}"
            val existingTotemQty = inventoryItems.value.find { it.id == totemId }?.quantity ?: 0
            saveInventoryItem(totemId, existingTotemQty + 1)
            markTotemUnlocked(animal.id)

            addXpAndNotify(
                skill = OsrsSkill.FIREMAKING,
                amount = animal.xpReward,
                gpReward = 50L,
                logTitle = "Crafted ${animal.name} Totem",
                logDesc = "Created 1x ${animal.name} Spirit Totem! Granted +${animal.xpReward} Summoning XP ✨"
            )
            progressSkillContract(OsrsSkill.FIREMAKING, 1, totemId)

            addChatMessage("🗿 CRAFTED TOTEM! You crafted 1x ${animal.name} Totem! It is now in your Inventory. Use the Totem whenever you want its 20-min spirit effects to start!")
        }
    }

    fun activateTotem(animalIdOrTotemId: String) {
        val cleanId = if (animalIdOrTotemId.startsWith("item_totem_")) animalIdOrTotemId.removePrefix("item_totem_") else animalIdOrTotemId

        // Check if this totem is a Golem Worker Totem
        val golem = com.example.data.models.SummoningData.GOLEM_TIERS.find {
            it.id == cleanId || it.id == animalIdOrTotemId || it.totemItemId == animalIdOrTotemId || "item_totem_${it.id}" == animalIdOrTotemId
        }
        if (golem != null) {
            activateGolemTotem(golem.id)
            return
        }

        val animal = com.example.data.models.SummoningData.ALL_ANIMALS.find {
            it.id == cleanId || it.id == animalIdOrTotemId || "item_totem_${it.id}" == animalIdOrTotemId
        }
        if (animal == null) {
            addChatMessage("❌ Spirit animal totem not found.")
            return
        }

        _lastUsedTotemId.value = animal.id
        pohPrefs.edit().putString("last_used_totem_id", animal.id).apply()
        markTotemUnlocked(animal.id)

        val totemId = "item_totem_${animal.id}"
        val currentTotemItem = inventoryItems.value.find { it.id == totemId }
        val currentQty = currentTotemItem?.quantity ?: 0

        if (currentQty <= 0) {
            val bankTotemItem = bankItems.value.find { it.id == totemId }
            val bankQty = bankTotemItem?.quantity ?: 0
            if (bankQty <= 0) {
                addChatMessage("❌ You do not have any ${animal.name} Totems! Craft one at the Summoning Guild first.")
                return
            } else {
                viewModelScope.launch {
                    saveBankItem(totemId, bankQty - 1)
                }
            }
        } else {
            viewModelScope.launch {
                if (currentQty <= 1) {
                    repository.deleteInventoryItem(petState.value.petType.name, totemId)
                } else {
                    saveInventoryItem(totemId, currentQty - 1)
                }
            }
        }

        if (animal.petMoodBonus > 0) {
            _manualMoodBoost.value = (_manualMoodBoost.value + animal.petMoodBonus).coerceAtMost(100)
        }

        summonJob?.cancel()
        val totalSec = animal.durationSeconds
        val durationMin = totalSec / 60
        _activeSummon.value = com.example.data.models.ActiveSummoningCompanion(
            animalId = animal.id,
            animalName = animal.name,
            iconEmoji = animal.iconEmoji,
            benefitText = animal.benefitText,
            startTimeMillis = System.currentTimeMillis(),
            durationSeconds = totalSec,
            remainingSeconds = totalSec,
            runesMultiplier = animal.runesMultiplier,
            expeditionTimeReductionPercent = animal.expeditionTimeReductionPercent,
            questTimeReductionPercent = animal.questTimeReductionPercent,
            skillingXpBonusPercent = animal.skillingXpBonusPercent,
            extraIncantationSlots = animal.extraIncantationSlots
        )

        addChatMessage("✨ TOTEM ACTIVATED! ${animal.name} Totem has invoked ${animal.name} ${animal.iconEmoji}! Active for $durationMin minutes: ${animal.benefitText}")

        summonJob = viewModelScope.launch {
            var remaining = totalSec
            while (remaining > 0) {
                kotlinx.coroutines.delay(1000)
                remaining--
                _activeSummon.value = _activeSummon.value?.copy(remainingSeconds = remaining)
            }
            _activeSummon.value = null
            addChatMessage("✨ Companion Return: ${animal.name} has completed its $durationMin-minute spiritual stay and returned to the spirit realm.")
        }
    }

    fun craftGolemTotem(golem: com.example.data.models.GolemTier) {
        val currentSummoningXp = skillXpMap.value[OsrsSkill.FIREMAKING] ?: 0L
        val level = OsrsXpCalculator.getLevelForXp(currentSummoningXp)

        if (level < golem.levelRequired) {
            addChatMessage("🔒 Cannot summon ${golem.name}: Requires Level ${golem.levelRequired} Summoning (You are Level $level)!")
            return
        }

        val inv = inventoryItems.value
        val bank = bankItems.value
        for ((effigyId, reqCount) in golem.requiredEffigies) {
            val invQty = inv.find { it.id == effigyId }?.quantity ?: 0
            val bankQty = bank.find { it.id == effigyId }?.quantity ?: 0
            if ((invQty + bankQty) < reqCount) {
                val effigyItem = com.example.data.models.DefaultItems.ALL_SHOP_ITEMS.find { it.id == effigyId }
                val effigyName = effigyItem?.name ?: effigyId
                addChatMessage("❌ Missing required effigies: $effigyName x$reqCount for ${golem.name}!")
                return
            }
        }

        viewModelScope.launch {
            for ((effigyId, reqCount) in golem.requiredEffigies) {
                var remainingNeeded = reqCount
                val bagItem = inventoryItems.value.find { it.id == effigyId }
                if (bagItem != null && bagItem.quantity > 0) {
                    val takenFromBag = bagItem.quantity.coerceAtMost(remainingNeeded)
                    val newBagQty = bagItem.quantity - takenFromBag
                    remainingNeeded -= takenFromBag
                    if (newBagQty <= 0) {
                        repository.deleteInventoryItem(petState.value.petType.name, effigyId)
                    } else {
                        saveInventoryItem(effigyId, newBagQty)
                    }
                }
                if (remainingNeeded > 0) {
                    val bItem = bankItems.value.find { it.id == effigyId }
                    if (bItem != null && bItem.quantity > 0) {
                        val takenFromBank = bItem.quantity.coerceAtMost(remainingNeeded)
                        val newBankQty = bItem.quantity - takenFromBank
                        if (newBankQty <= 0) {
                            saveBankItem(effigyId, 0)
                        } else {
                            saveBankItem(effigyId, newBankQty)
                        }
                    }
                }
            }

            val totemId = golem.totemItemId
            val existingTotemQty = inventoryItems.value.find { it.id == totemId }?.quantity ?: 0
            saveInventoryItem(totemId, existingTotemQty + 1)

            addXpAndNotify(
                skill = OsrsSkill.FIREMAKING,
                amount = golem.xpReward,
                gpReward = 100L,
                logTitle = "Crafted ${golem.name} Totem",
                logDesc = "Forged 1x ${golem.name} Spirit Totem! Granted +${golem.xpReward} Summoning XP ✨"
            )
            progressSkillContract(OsrsSkill.FIREMAKING, 1, totemId)

            addChatMessage("🗿 CRAFTED GOLEM TOTEM! You crafted 1x ${golem.name} Totem! Activate it in the Golems tab or Inventory to summon your secondary AFK worker. The timer will stay paused until you give it a task!")
        }
    }

    fun activateGolemTotem(golemIdOrTotemId: String) {
        val cleanId = if (golemIdOrTotemId.startsWith("item_totem_")) golemIdOrTotemId.removePrefix("item_totem_") else golemIdOrTotemId
        val golem = com.example.data.models.SummoningData.GOLEM_TIERS.find {
            it.id == cleanId || it.id == golemIdOrTotemId || it.totemItemId == golemIdOrTotemId || "item_totem_${it.id}" == golemIdOrTotemId
        }
        if (golem == null) {
            addChatMessage("❌ Golem totem not found.")
            return
        }

        val currentGolem = _activeGolem.value
        if (currentGolem != null && (currentGolem.isWorking || currentGolem.isCompleted)) {
            if (currentGolem.isCompleted) {
                addChatMessage("⚠️ You have completed golem rewards waiting! Tap 'Claim Rewards' first before activating a new golem.")
                return
            } else if (currentGolem.isWorking) {
                addChatMessage("⚠️ Your ${currentGolem.golemName} is currently working! Claim or dismiss it first before activating another golem.")
                return
            }
        }

        val totemId = golem.totemItemId
        val currentTotemItem = inventoryItems.value.find { it.id == totemId }
        val currentQty = currentTotemItem?.quantity ?: 0

        if (currentQty <= 0) {
            val bankTotemItem = bankItems.value.find { it.id == totemId }
            val bankQty = bankTotemItem?.quantity ?: 0
            if (bankQty <= 0) {
                addChatMessage("❌ You do not have any ${golem.name} Totems! Craft one at the Summoning Guild first.")
                return
            } else {
                viewModelScope.launch {
                    saveBankItem(totemId, bankQty - 1)
                }
            }
        } else {
            viewModelScope.launch {
                if (currentQty <= 1) {
                    repository.deleteInventoryItem(petState.value.petType.name, totemId)
                } else {
                    saveInventoryItem(totemId, currentQty - 1)
                }
            }
        }

        golemJob?.cancel()
        val totalSec = golem.workDurationSeconds
        val durationMin = totalSec / 60
        _activeGolem.value = com.example.data.models.ActiveGolemState(
            golemId = golem.id,
            golemName = golem.name,
            iconEmoji = golem.iconEmoji,
            totalDurationSeconds = totalSec,
            remainingSeconds = totalSec,
            isWorking = false, // Timer DOES NOT start until assigned!
            isCompleted = false
        )
        saveActiveGolemStateToPrefs()

        addChatMessage("🗿 GOLEM SUMMONED! ${golem.name} ${golem.iconEmoji} is active and awaiting orders! Its ${durationMin}-minute timer will NOT start until you assign it to an AFK activity.")
    }

    fun assignGolemToActivity(task: com.example.data.models.GolemTaskOption) {
        val current = _activeGolem.value
        if (current == null) {
            addChatMessage("❌ You do not have an active golem! Activate a Golem Totem first.")
            return
        }
        if (current.isCompleted) {
            addChatMessage("⚠️ Claim the completed golem rewards before starting a new task.")
            return
        }

        val durationMin = current.remainingSeconds / 60
        _activeGolem.value = current.copy(
            isWorking = true,
            assignedActivityId = task.id,
            assignedActivityName = task.name,
            assignedActivityEmoji = task.emoji,
            assignedActivitySubOption = task.subOptionId,
            assignedSkill = task.skill,
            startTimeMs = System.currentTimeMillis(),
            lastTickTimeMs = System.currentTimeMillis()
        )
        saveActiveGolemStateToPrefs()
        startGolemWorkerLoop()

        addChatMessage("⚡ GOLEM DEPLOYED! ${current.golemName} ${current.iconEmoji} is now performing ${task.name} ${task.emoji}! It will work for $durationMin minutes in your place.")
    }

    private fun startGolemWorkerLoop() {
        golemJob?.cancel()
        golemJob = viewModelScope.launch {
            var loopSeconds = 0
            while (true) {
                kotlinx.coroutines.delay(1000L)
                val current = _activeGolem.value ?: break
                if (!current.isWorking || current.isCompleted) break

                val newRemaining = (current.remainingSeconds - 1).coerceAtLeast(0)
                loopSeconds++

                if (newRemaining <= 0) {
                    _activeGolem.value = current.copy(
                        remainingSeconds = 0,
                        isWorking = false,
                        isCompleted = true
                    )
                    saveActiveGolemStateToPrefs()
                    addChatMessage("🎉 GOLEM FINISHED! ${current.golemName} ${current.iconEmoji} completed its work on ${current.assignedActivityName}! Tap 'Claim Rewards' in the Summoning Tab to collect your spoils!")
                    break
                }

                if (loopSeconds % 5 == 0) {
                    val (rewardItem, xp, gp) = generateGolemTickReward(
                        current.assignedActivityId ?: "",
                        current.assignedActivitySubOption ?: ""
                    )
                    val updatedLoot = current.accumulatedLoot.toMutableList()
                    if (rewardItem != null) {
                        val existingIdx = updatedLoot.indexOfFirst { it.itemId == rewardItem.itemId }
                        if (existingIdx >= 0) {
                            val existing = updatedLoot[existingIdx]
                            updatedLoot[existingIdx] = existing.copy(quantity = existing.quantity + rewardItem.quantity)
                        } else {
                            updatedLoot.add(rewardItem)
                        }
                    }

                    _activeGolem.value = current.copy(
                        remainingSeconds = newRemaining,
                        completedActions = current.completedActions + 1,
                        accumulatedXp = current.accumulatedXp + xp,
                        accumulatedGp = current.accumulatedGp + gp,
                        accumulatedLoot = updatedLoot,
                        lastTickTimeMs = System.currentTimeMillis()
                    )

                    if (loopSeconds % 15 == 0) {
                        saveActiveGolemStateToPrefs()
                    }
                } else {
                    _activeGolem.value = current.copy(remainingSeconds = newRemaining)
                }
            }
        }
    }

    fun generateGolemTickReward(taskId: String, subOptionId: String): Triple<com.example.data.models.GolemHarvestedReward?, Long, Long> {
        val rand = java.util.Random()
        fun makeReward(itemId: String, qty: Int): com.example.data.models.GolemHarvestedReward {
            val itemDef = com.example.data.models.DefaultItems.getItemById(itemId)
            val name = if (itemDef.name.isNotBlank() && itemDef.name != "Unknown Item") itemDef.name else itemId.removePrefix("item_").replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
            val emoji = if (itemDef.iconEmoji.isNotBlank() && itemDef.iconEmoji != "📦") itemDef.iconEmoji else {
                when {
                    itemId.contains("log") -> "🪵"
                    itemId.contains("ore") || itemId.contains("bar") -> "⛏️"
                    itemId.contains("fish") || itemId.contains("salmon") || itemId.contains("trout") || itemId.contains("shark") || itemId.contains("shrimp") -> "🐟"
                    itemId.contains("gem") || itemId.contains("ruby") || itemId.contains("diamond") || itemId.contains("sapphire") || itemId.contains("emerald") -> "💎"
                    itemId.contains("arrow") -> "🏹"
                    itemId.contains("plank") -> "🪵"
                    itemId.contains("rune") -> "🔮"
                    itemId.contains("effigy") -> "🗿"
                    itemId.contains("nest") -> "🪺"
                    itemId.contains("bone") -> "🦴"
                    else -> "📦"
                }
            }
            return com.example.data.models.GolemHarvestedReward(itemId, name, emoji, qty)
        }

        return when (taskId) {
            "woodcutting_sylvan" -> {
                val logId = when (rand.nextInt(3)) {
                    0 -> "item_logs"
                    1 -> "item_oak_logs"
                    else -> "item_birch_timber"
                }
                val reward = if (rand.nextInt(100) < 5) makeReward("item_birds_nest", 1) else makeReward(logId, 1)
                Triple(reward, 45L, 0L)
            }
            "woodcutting_pines" -> {
                val logId = if (rand.nextBoolean()) "item_willow_logs" else "item_maple_logs"
                val reward = if (rand.nextInt(100) < 8) makeReward("item_birds_nest", 1) else makeReward(logId, 1)
                Triple(reward, 90L, 0L)
            }
            "woodcutting_canopy" -> {
                val logId = if (rand.nextBoolean()) "item_yew_logs" else "item_magic_logs"
                val reward = if (rand.nextInt(100) < 10) makeReward("item_birds_nest", 1) else makeReward(logId, 1)
                Triple(reward, 180L, 0L)
            }
            "mining_surface" -> {
                val ores = listOf("item_copper_ore", "item_tin_ore", "item_iron_ore", "item_coal")
                Triple(makeReward(ores[rand.nextInt(ores.size)], 1), 45L, 0L)
            }
            "mining_deep" -> {
                val ores = listOf("item_mithril_ore", "item_adamant_ore", "item_runite_ore")
                Triple(makeReward(ores[rand.nextInt(ores.size)], 1), 130L, 0L)
            }
            "mining_gems" -> {
                val gems = listOf("item_uncut_sapphire", "item_uncut_emerald", "item_uncut_ruby", "item_uncut_diamond")
                Triple(makeReward(gems[rand.nextInt(gems.size)], 1), 160L, 0L)
            }
            "fishing_coastal" -> {
                val fish = listOf("item_raw_shrimps", "item_raw_trout", "item_raw_salmon")
                Triple(makeReward(fish[rand.nextInt(fish.size)], 1), 50L, 0L)
            }
            "fishing_deep" -> {
                val fish = listOf("item_raw_lobster", "item_raw_swordfish", "item_raw_shark")
                Triple(makeReward(fish[rand.nextInt(fish.size)], 1), 140L, 0L)
            }
            "thieving_pickpocket" -> {
                val loot = listOf("item_lockpick", "item_sunleaf_seed", "item_gem_bag", "item_rune_chaos")
                val gp = 40L + rand.nextInt(30)
                Triple(makeReward(loot[rand.nextInt(loot.size)], 1), 85L, gp)
            }
            "cooking_fish" -> {
                val food = listOf("item_trout", "item_salmon", "item_lobster", "item_swordfish", "item_shark")
                Triple(makeReward(food[rand.nextInt(food.size)], 1), 110L, 0L)
            }
            "campfire_logs" -> {
                Triple(makeReward("item_ashes", 1), 95L, 0L)
            }
            "smelting_bars" -> {
                val bars = listOf("item_bronze_bar", "item_iron_bar", "item_steel_bar", "item_mithril_bar", "item_adamant_bar")
                Triple(makeReward(bars[rand.nextInt(bars.size)], 1), 115L, 0L)
            }
            "fletching_arrows" -> {
                val items = listOf("item_arrow_shafts" to 15, "item_rune_arrow" to 5, "item_oak_shortbow" to 1)
                val picked = items[rand.nextInt(items.size)]
                Triple(makeReward(picked.first, picked.second), 90L, 0L)
            }
            "sawmill_planks" -> {
                val planks = listOf("item_plank", "item_oak_plank", "item_teak_plank", "item_mahogany_plank")
                Triple(makeReward(planks[rand.nextInt(planks.size)], 1), 85L, 0L)
            }
            "runecrafting_altar" -> {
                val runes = listOf("item_rune_nature" to 3, "item_rune_law" to 3, "item_rune_death" to 3, "item_rune_blood" to 2, "item_rune_soul" to 2)
                val picked = runes[rand.nextInt(runes.size)]
                Triple(makeReward(picked.first, picked.second), 100L, 0L)
            }
            "druid_altar" -> {
                val effigies = listOf("item_effigy_air", "item_effigy_earth", "item_effigy_fire", "item_effigy_water", "item_effigy_mind", "item_effigy_cosmic")
                Triple(makeReward(effigies[rand.nextInt(effigies.size)], 1), 120L, 0L)
            }
            "catacombs_agility" -> {
                val relic = if (rand.nextBoolean()) "item_ancient_relic" else "item_shaman_talisman"
                Triple(makeReward(relic, 1), 95L, 65L)
            }
            "prayer_sanctification" -> {
                val bones = if (rand.nextBoolean()) "item_dragon_bones" to 1 else "item_big_bones" to 2
                Triple(makeReward(bones.first, bones.second), 110L, 0L)
            }
            else -> {
                Triple(makeReward("item_logs", 1), 40L, 0L)
            }
        }
    }

    fun claimGolemRewards() {
        val current = _activeGolem.value ?: return
        golemJob?.cancel()

        viewModelScope.launch {
            // Add all items
            for (loot in current.accumulatedLoot) {
                val existingQty = inventoryItems.value.find { it.id == loot.itemId }?.quantity ?: 0
                saveInventoryItem(loot.itemId, existingQty + loot.quantity)
            }

            // Award XP
            if (current.accumulatedXp > 0L && current.assignedSkill != null) {
                addXpAndNotify(
                    skill = current.assignedSkill,
                    amount = current.accumulatedXp,
                    gpReward = current.accumulatedGp,
                    logTitle = "${current.golemName} Task Complete",
                    logDesc = "Your ${current.golemName} completed ${current.completedActions} actions on ${current.assignedActivityName}!"
                )
            } else if (current.accumulatedGp > 0L) {
                val updated = petState.value.copy(coinsGp = petState.value.coinsGp + current.accumulatedGp)
                repository.savePetState(updated)
            }

            val itemsSummary = if (current.accumulatedLoot.isNotEmpty()) {
                current.accumulatedLoot.joinToString(", ") { "${it.quantity}x ${it.itemName} ${it.itemEmoji}" }
            } else "No items"

            addChatMessage("🎉 GOLEM REWARDS CLAIMED! Received $itemsSummary, +${current.accumulatedXp} ${current.assignedSkill?.displayName ?: "Skill"} XP, and +${current.accumulatedGp} GP from ${current.golemName}!")

            _activeGolem.value = null
            saveActiveGolemStateToPrefs()
        }
    }

    fun dismissActiveGolem(claimPartial: Boolean = true) {
        val current = _activeGolem.value ?: return
        golemJob?.cancel()
        if (claimPartial && (current.accumulatedLoot.isNotEmpty() || current.accumulatedXp > 0L || current.accumulatedGp > 0L)) {
            claimGolemRewards()
        } else {
            _activeGolem.value = null
            saveActiveGolemStateToPrefs()
            addChatMessage("💨 Dismissed ${current.golemName} golem worker.")
        }
    }

    private fun saveActiveGolemStateToPrefs() {
        val golem = _activeGolem.value
        if (golem == null) {
            pohPrefs.edit().remove("active_golem_json").apply()
            return
        }
        try {
            val json = org.json.JSONObject().apply {
                put("golemId", golem.golemId)
                put("golemName", golem.golemName)
                put("iconEmoji", golem.iconEmoji)
                put("totalDurationSeconds", golem.totalDurationSeconds)
                put("remainingSeconds", golem.remainingSeconds)
                put("isWorking", golem.isWorking)
                put("assignedActivityId", golem.assignedActivityId ?: "")
                put("assignedActivityName", golem.assignedActivityName ?: "")
                put("assignedActivityEmoji", golem.assignedActivityEmoji ?: "")
                put("assignedActivitySubOption", golem.assignedActivitySubOption ?: "")
                put("assignedSkill", golem.assignedSkill?.name ?: "")
                put("startTimeMs", golem.startTimeMs)
                put("lastTickTimeMs", golem.lastTickTimeMs)
                put("completedActions", golem.completedActions)
                put("accumulatedXp", golem.accumulatedXp)
                put("accumulatedGp", golem.accumulatedGp)
                put("isCompleted", golem.isCompleted)
                val lootArray = org.json.JSONArray()
                for (item in golem.accumulatedLoot) {
                    lootArray.put(org.json.JSONObject().apply {
                        put("itemId", item.itemId)
                        put("itemName", item.itemName)
                        put("itemEmoji", item.itemEmoji)
                        put("quantity", item.quantity)
                    })
                }
                put("accumulatedLoot", lootArray)
            }
            pohPrefs.edit().putString("active_golem_json", json.toString()).apply()
        } catch (e: Exception) {
            // Handle error safely
        }
    }

    private fun loadActiveGolemStateFromPrefs() {
        val jsonStr = pohPrefs.getString("active_golem_json", null) ?: return
        try {
            val obj = org.json.JSONObject(jsonStr)
            val lootList = mutableListOf<com.example.data.models.GolemHarvestedReward>()
            val lootArray = obj.optJSONArray("accumulatedLoot")
            if (lootArray != null) {
                for (i in 0 until lootArray.length()) {
                    val itemObj = lootArray.getJSONObject(i)
                    lootList.add(
                        com.example.data.models.GolemHarvestedReward(
                            itemId = itemObj.getString("itemId"),
                            itemName = itemObj.getString("itemName"),
                            itemEmoji = itemObj.getString("itemEmoji"),
                            quantity = itemObj.getInt("quantity")
                        )
                    )
                }
            }
            val skillName = obj.optString("assignedSkill", "")
            val skill = if (skillName.isNotEmpty()) try { com.example.data.models.OsrsSkill.valueOf(skillName) } catch (e: Exception) { null } else null

            val state = com.example.data.models.ActiveGolemState(
                golemId = obj.getString("golemId"),
                golemName = obj.getString("golemName"),
                iconEmoji = obj.getString("iconEmoji"),
                totalDurationSeconds = obj.getInt("totalDurationSeconds"),
                remainingSeconds = obj.getInt("remainingSeconds"),
                isWorking = obj.getBoolean("isWorking"),
                assignedActivityId = obj.optString("assignedActivityId").ifEmpty { null },
                assignedActivityName = obj.optString("assignedActivityName").ifEmpty { null },
                assignedActivityEmoji = obj.optString("assignedActivityEmoji").ifEmpty { null },
                assignedActivitySubOption = obj.optString("assignedActivitySubOption").ifEmpty { null },
                assignedSkill = skill,
                startTimeMs = obj.optLong("startTimeMs", 0L),
                lastTickTimeMs = obj.optLong("lastTickTimeMs", 0L),
                completedActions = obj.optInt("completedActions", 0),
                accumulatedXp = obj.optLong("accumulatedXp", 0L),
                accumulatedGp = obj.optLong("accumulatedGp", 0L),
                accumulatedLoot = lootList,
                isCompleted = obj.optBoolean("isCompleted", false)
            )
            _activeGolem.value = state
            if (state.isWorking && !state.isCompleted && state.remainingSeconds > 0) {
                startGolemWorkerLoop()
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun dismissActiveSummon() {
        summonJob?.cancel()
        val current = _activeSummon.value
        _activeSummon.value = null
        if (current != null) {
            addChatMessage("✨ Dismissed ${current.animalName} spirit companion.")
        }
    }

    fun infuseRuneToEffigy(runeItemId: String) {
        val recipe = com.example.data.models.SummoningData.getEffigyRecipeByRune(runeItemId)
        val effigyId = recipe.effigyId
        val inv = inventoryItems.value
        val bank = bankItems.value

        val invRune = inv.find { it.id == runeItemId }
        val invQty = invRune?.quantity ?: 0
        val bankRune = bank.find { it.id == runeItemId }
        val bankQty = bankRune?.quantity ?: 0

        if ((invQty + bankQty) < 5) {
            val runeName = com.example.data.models.DefaultItems.ALL_SHOP_ITEMS.find { it.id == runeItemId }?.name ?: runeItemId
            addChatMessage("❌ Need 5x $runeName to infuse 1 Spirit Effigy!")
            return
        }

        viewModelScope.launch {
            var remainingToDeduct = 5
            if (invRune != null && invQty > 0) {
                val taken = invQty.coerceAtMost(remainingToDeduct)
                val newQty = invQty - taken
                remainingToDeduct -= taken
                if (newQty <= 0) {
                    repository.deleteInventoryItem(petState.value.petType.name, runeItemId)
                } else {
                    saveInventoryItem(runeItemId, newQty)
                }
            }
            if (remainingToDeduct > 0 && bankRune != null) {
                val newBankQty = (bankQty - remainingToDeduct).coerceAtLeast(0)
                saveBankItem(runeItemId, newBankQty)
            }

            val existingEffigy = inv.find { it.id == effigyId }
            val newEffigyQty = (existingEffigy?.quantity ?: 0) + 1
            saveInventoryItem(effigyId, newEffigyQty)

            val effigyItem = com.example.data.models.DefaultItems.ALL_SHOP_ITEMS.find { it.id == effigyId }
            val effigyName = effigyItem?.name ?: recipe.effigyName
            val effigyEmoji = effigyItem?.iconEmoji ?: recipe.emoji

            addXpAndNotify(
                skill = OsrsSkill.FIREMAKING,
                amount = recipe.xpReward,
                gpReward = recipe.gpReward,
                logTitle = "Infused $effigyName",
                logDesc = "Infused 5 Runes into 1 $effigyName $effigyEmoji! (+${recipe.xpReward} Summoning XP)"
            )
            addChatMessage("🔮 INFUSION SUCCESS! Created 1x $effigyName $effigyEmoji from 5 Runes!")
        }
    }

    fun setSelectedDruidEffigyId(effigyId: String) {
        _selectedDruidEffigyId.value = effigyId
        val recipe = com.example.data.models.SummoningData.getEffigyRecipe(effigyId)
        _selectedDruidRuneId.value = recipe.runeId
        _selectedDruidLogId.value = recipe.logId
        _selectedDruidNailId.value = recipe.nailId
    }

    fun setSelectedDruidRuneId(runeId: String) {
        _selectedDruidRuneId.value = runeId
        val recipe = com.example.data.models.SummoningData.getEffigyRecipeByRune(runeId)
        _selectedDruidEffigyId.value = recipe.effigyId
        _selectedDruidLogId.value = recipe.logId
        _selectedDruidNailId.value = recipe.nailId
    }

    fun setSelectedDruidLogId(logId: String) {
        _selectedDruidLogId.value = logId
    }

    fun setSelectedDruidNailId(nailId: String) {
        _selectedDruidNailId.value = nailId
    }

    fun toggleAfkDruidAltar() {
        if (isAfkDruidAltarActive.value) {
            AfkEngine.stopAll(pohPrefs)
            _druidAltarProgress.value = 0f
            addChatMessage("🛑 Stopped Druid Altar Ritual.")
        } else {
            if (!canStartAfkOrHungerAction("Druid Altar Ritual")) return
            val effigyId = _selectedDruidEffigyId.value
            val recipe = com.example.data.models.SummoningData.getEffigyRecipe(effigyId)

            val summoningXp = skillXpMap.value[OsrsSkill.FIREMAKING] ?: 0L
            val summoningLvl = OsrsXpCalculator.getLevelForXp(summoningXp)
            if (summoningLvl < recipe.levelReq) {
                addChatMessage("🔒 Requires Level ${recipe.levelReq} Summoning to forge ${recipe.effigyName}!")
                return
            }

            val inv = inventoryItems.value
            val bank = bankItems.value
            fun getQty(id: String) = (inv.find { it.id == id }?.quantity ?: 0) + (bank.find { it.id == id }?.quantity ?: 0)

            val runeQty = getQty(recipe.runeId)
            val logQty = getQty(recipe.logId)
            val nailQty = getQty(recipe.nailId)

            if (runeQty < recipe.runeCount || logQty < recipe.logCount || nailQty < recipe.nailCount) {
                addChatMessage("❌ Missing ${recipe.effigyName} ingredients! Need ${recipe.runeCount}x ${recipe.runeName} (Have $runeQty), ${recipe.logCount}x ${recipe.logName} (Have $logQty), ${recipe.nailCount}x ${recipe.nailName} (Have $nailQty)")
                return
            }

            stopAllAfkStationsExcept("druid_altar")
            AfkEngine.startActivity(AfkActivityType.DRUID_ALTAR, pohPrefs)
            _druidAltarProgress.value = 0f
            _druidAltarCraftedCount.value = 0
            addChatMessage("🌿 STARTED DRUID ALTAR RITUAL! Forging ${recipe.effigyName} ${recipe.emoji} (${recipe.runeName} + ${recipe.logName} + ${recipe.nailName})!")
        }
        saveAfkStateToPrefs()
    }

    fun craftDruidAltarEffigy(isAfk: Boolean = false) {
        if (!isAfk) {
            if (!canStartAfkOrHungerAction("Druid Altar Crafting")) return
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Druid Ritual.")
            }
        }
        val effigyId = _selectedDruidEffigyId.value
        val recipe = com.example.data.models.SummoningData.getEffigyRecipe(effigyId)

        val summoningXp = skillXpMap.value[OsrsSkill.FIREMAKING] ?: 0L
        val summoningLvl = OsrsXpCalculator.getLevelForXp(summoningXp)
        if (summoningLvl < recipe.levelReq) {
            if (isAfk) {
                AfkEngine.stopAll(pohPrefs)
                _druidAltarProgress.value = 0f
            }
            addChatMessage("🔒 Requires Level ${recipe.levelReq} Summoning to forge ${recipe.effigyName}!")
            return
        }

        val inv = inventoryItems.value
        val bank = bankItems.value
        fun getQty(id: String) = (inv.find { it.id == id }?.quantity ?: 0) + (bank.find { it.id == id }?.quantity ?: 0)

        val runeQty = getQty(recipe.runeId)
        val logQty = getQty(recipe.logId)
        val nailQty = getQty(recipe.nailId)

        if (runeQty < recipe.runeCount || logQty < recipe.logCount || nailQty < recipe.nailCount) {
            if (isAfk) {
                AfkEngine.stopAll(pohPrefs)
                _druidAltarProgress.value = 0f
                addChatMessage("⚠️ Druid Altar Ritual stopped: Out of materials for ${recipe.effigyName} (requires ${recipe.runeCount}x ${recipe.runeName}, ${recipe.logCount}x ${recipe.logName}, ${recipe.nailCount}x ${recipe.nailName})!")
            } else {
                addChatMessage("❌ Missing ingredients for ${recipe.effigyName}! Need ${recipe.runeCount}x ${recipe.runeName}, ${recipe.logCount}x ${recipe.logName}, and ${recipe.nailCount}x ${recipe.nailName}.")
            }
            return
        }

        viewModelScope.launch {
            // Deduct Rune
            var remainingRune = recipe.runeCount
            val invRune = inv.find { it.id == recipe.runeId }
            if (invRune != null && invRune.quantity > 0) {
                val taken = invRune.quantity.coerceAtMost(remainingRune)
                val newQty = invRune.quantity - taken
                remainingRune -= taken
                if (newQty <= 0) repository.deleteInventoryItem(petState.value.petType.name, recipe.runeId)
                else saveInventoryItem(recipe.runeId, newQty)
            }
            if (remainingRune > 0) {
                val bRune = bank.find { it.id == recipe.runeId }
                if (bRune != null) saveBankItem(recipe.runeId, (bRune.quantity - remainingRune).coerceAtLeast(0))
            }

            // Deduct Log
            var remainingLog = recipe.logCount
            val invLog = inv.find { it.id == recipe.logId }
            if (invLog != null && invLog.quantity > 0) {
                val taken = invLog.quantity.coerceAtMost(remainingLog)
                val newQty = invLog.quantity - taken
                remainingLog -= taken
                if (newQty <= 0) repository.deleteInventoryItem(petState.value.petType.name, recipe.logId)
                else saveInventoryItem(recipe.logId, newQty)
            }
            if (remainingLog > 0) {
                val bLog = bank.find { it.id == recipe.logId }
                if (bLog != null) saveBankItem(recipe.logId, (bLog.quantity - remainingLog).coerceAtLeast(0))
            }

            // Deduct Nails
            var remainingNails = recipe.nailCount
            val invNail = inv.find { it.id == recipe.nailId }
            if (invNail != null && invNail.quantity > 0) {
                val taken = invNail.quantity.coerceAtMost(remainingNails)
                val newQty = invNail.quantity - taken
                remainingNails -= taken
                if (newQty <= 0) repository.deleteInventoryItem(petState.value.petType.name, recipe.nailId)
                else saveInventoryItem(recipe.nailId, newQty)
            }
            if (remainingNails > 0) {
                val bNail = bank.find { it.id == recipe.nailId }
                if (bNail != null) saveBankItem(recipe.nailId, (bNail.quantity - remainingNails).coerceAtLeast(0))
            }

            // Award 1 Effigy
            val existingEffigy = inv.find { it.id == recipe.effigyId }
            val newEffigyQty = (existingEffigy?.quantity ?: 0) + 1
            saveInventoryItem(recipe.effigyId, newEffigyQty)

            _druidAltarCraftedCount.value += 1

            addXpAndNotify(
                skill = OsrsSkill.FIREMAKING,
                amount = recipe.xpReward,
                gpReward = recipe.gpReward,
                logTitle = "Druid Altar: ${recipe.effigyName}",
                logDesc = "Fused ${recipe.runeName}, ${recipe.logName} & ${recipe.nailName} into 1 ${recipe.effigyName} ${recipe.emoji}! (+${recipe.xpReward} Summoning XP)"
            )
            progressSkillContract(OsrsSkill.FIREMAKING, 1, recipe.effigyId)
        }
    }

    fun setSelectedOreId(oreId: String?) {
        if (oreId != null) {
            val smithXp = skillXpMap.value[OsrsSkill.SMITHING] ?: 0L
            val smithLvl = OsrsXpCalculator.getLevelForXp(smithXp)
            val mineral = com.example.data.models.AdventuringStoryData.GEMOLOGY_AREAS
                .flatMap { it.minerals }
                .find { it.id == oreId }
            val reqLvl = mineral?.reqLevel ?: when (oreId) {
                "item_iron_ore" -> 15
                "item_coal_ore" -> 30
                "item_mithril_ore" -> 55
                "item_adamant_ore", "item_adamantite_ore" -> 70
                "item_runite_ore" -> 85
                else -> 1
            }
            if (smithLvl < reqLvl) {
                addChatMessage("🔒 Cannot select ${mineral?.name ?: "ore"}: Requires Level $reqLvl Forging (You are Level $smithLvl)!")
                return
            }
        }
        _selectedOreId.value = if (_selectedOreId.value == oreId) null else oreId
        val name = _selectedOreId.value?.replace("item_", "")?.replace("_", " ")?.uppercase() ?: "NONE"
        addChatMessage("⛏️ Target Mining Selection: $name")
        saveAfkStateToPrefs()
    }

    fun setSelectedBarId(barId: String?) {
        if (barId != null) {
            val smithXp = skillXpMap.value[OsrsSkill.SMITHING] ?: 0L
            val smithLvl = OsrsXpCalculator.getLevelForXp(smithXp)
            val reqLvl = when (barId) {
                "item_iron_bar" -> 15
                "item_steel_bar" -> 30
                "item_mithril_bar" -> 50
                "item_adamant_bar" -> 70
                "item_rune_bar" -> 85
                else -> 1
            }
            if (smithLvl < reqLvl) {
                addChatMessage("🔒 Cannot select bar: Requires Level $reqLvl Smithing (You are Level $smithLvl)!")
                return
            }
        }
        _selectedBarId.value = if (_selectedBarId.value == barId) null else barId
        val name = _selectedBarId.value?.replace("item_", "")?.replace("_", " ")?.uppercase() ?: "NONE"
        addChatMessage("🔥 Target Smelting Selection: $name")
        saveAfkStateToPrefs()
    }

    fun setSelectedTreeId(treeId: String?) {
        if (treeId != null) {
            val wcXp = skillXpMap.value[OsrsSkill.WOODCUTTING] ?: 0L
            val wcLvl = OsrsXpCalculator.getLevelForXp(wcXp)
            val tree = com.example.data.models.AdventuringStoryData.GROVE_FOREST_AREAS
                .flatMap { it.choppableTrees }
                .find { it.id == treeId }
            val reqLvl = tree?.reqLevel ?: when (treeId) {
                "item_oak_logs" -> 15
                "item_willow_logs" -> 30
                "item_maple_logs" -> 45
                "item_yew_logs" -> 60
                "item_magic_logs" -> 75
                "item_redwood_logs" -> 90
                else -> 1
            }
            if (wcLvl < reqLvl) {
                addChatMessage("🔒 Cannot select ${tree?.name ?: "tree"}: Requires Level $reqLvl Woodcutting (You are Level $wcLvl)!")
                return
            }
        }
        _selectedTreeId.value = if (_selectedTreeId.value == treeId) null else treeId
        val name = _selectedTreeId.value?.replace("item_", "")?.replace("_logs", "")?.replace("_", " ")?.uppercase() ?: "NONE"
        addChatMessage("🪓 Target Woodcutting Selection: $name")
        saveAfkStateToPrefs()
    }

    fun selectGroveForestArea(areaId: String) {
        val forest = com.example.data.models.AdventuringStoryData.GROVE_FOREST_AREAS.find { it.id == areaId } ?: return
        val wcXp = skillXpMap.value[OsrsSkill.WOODCUTTING] ?: 0L
        val wcLvl = OsrsXpCalculator.getLevelForXp(wcXp)
        if (wcLvl < forest.reqLevel) {
            addChatMessage("🔒 Cannot switch area: Level ${forest.reqLevel} Woodcutting required for ${forest.name} (You are Level $wcLvl)!")
            return
        }
        if (!isTotemUnlocked(forest.reqTotemId)) {
            val reqName = forest.reqTotemName ?: "Obelisk"
            addChatMessage("🗿 Obelisk Locked: Requires the $reqName to access ${forest.name}!")
            return
        }
        _selectedGroveForestId.value = areaId
        if (isAfkWoodcuttingActive.value) {
            addChatMessage("🌲 AFK Harvesting switched to ${forest.emoji} ${forest.name}! (Drops randomized from area drop table)")
        } else {
            addChatMessage("🌲 Selected Forest Area: ${forest.emoji} ${forest.name}")
        }
        saveAfkStateToPrefs()
    }

    fun burnLogsInGroveWoodpile(itemId: String, quantity: Int = 1) {
        viewModelScope.launch {
            val invItem = inventoryItems.value.find { it.id == itemId && it.quantity > 0 }
            val bankItem = bankItems.value.find { it.id == itemId && it.quantity > 0 }
            if (invItem == null && bankItem == null) {
                addChatMessage("⚠️ You have no $itemId to offer to the Sacred Grove Woodpile.")
                return@launch
            }

            var remainingToBurn = quantity
            var burnedCount = 0

            // Deduct from inventory first
            if (invItem != null) {
                val toTake = minOf(remainingToBurn, invItem.quantity)
                saveInventoryItem(itemId, invItem.quantity - toTake)
                remainingToBurn -= toTake
                burnedCount += toTake
            }

            // Deduct remaining from bank
            if (remainingToBurn > 0 && bankItem != null) {
                val toTake = minOf(remainingToBurn, bankItem.quantity)
                saveBankItem(itemId, bankItem.quantity - toTake)
                remainingToBurn -= toTake
                burnedCount += toTake
            }

            if (burnedCount <= 0) return@launch

            val logName = invItem?.name ?: bankItem?.name ?: "Timber Logs"
            val baseHarvestingXpPerLog = 160L
            val totalHarvestingXp = baseHarvestingXpPerLog * burnedCount

            addXpAndNotify(
                skill = OsrsSkill.WOODCUTTING,
                amount = totalHarvestingXp,
                gpReward = (15L * burnedCount),
                logTitle = "Sacred Woodpile Offering",
                logDesc = "Offered $burnedCount x $logName to the Sacred Grove Altar! Gained +${totalHarvestingXp} Harvesting XP! 🌲"
            )
        }
    }

    fun fenceStolenLoot(itemId: String, quantity: Int = 1) {
        viewModelScope.launch {
            val invItem = inventoryItems.value.find { it.id == itemId && it.quantity > 0 }
            val bankItem = bankItems.value.find { it.id == itemId && it.quantity > 0 }
            if (invItem == null && bankItem == null) {
                addChatMessage("⚠️ You have no $itemId to fence at the Shadow Market.")
                return@launch
            }
            var remainingToFence = quantity
            var fencedCount = 0
            if (invItem != null) {
                val toTake = minOf(remainingToFence, invItem.quantity)
                saveInventoryItem(itemId, invItem.quantity - toTake)
                remainingToFence -= toTake
                fencedCount += toTake
            }
            if (remainingToFence > 0 && bankItem != null) {
                val toTake = minOf(remainingToFence, bankItem.quantity)
                saveBankItem(itemId, bankItem.quantity - toTake)
                remainingToFence -= toTake
                fencedCount += toTake
            }
            if (fencedCount <= 0) return@launch

            val itemName = invItem?.name ?: bankItem?.name ?: "Stolen Loot"
            val baseXpPerItem = 180L
            val baseGpPerItem = 250L
            val totalXp = baseXpPerItem * fencedCount
            val totalGp = baseGpPerItem * fencedCount

            addXpAndNotify(
                skill = OsrsSkill.THIEVING,
                amount = totalXp,
                gpReward = totalGp,
                logTitle = "Shadow Market Fence",
                logDesc = "Fenced $fencedCount x $itemName at the Shadow Market! Gained +${totalXp} Trickery XP & +${totalGp} GP! 🥷💰"
            )
        }
    }

    // AFK Slayer & Hunter States
    val isAfkSlayerActive: StateFlow<Boolean> get() = AfkEngine.isAfkSlayerActive

    // Player Owned Farm (POF) State
    private val _pofState = MutableStateFlow(loadPofState())
    val pofState: StateFlow<com.example.data.models.PlayerOwnedFarmState> = _pofState.asStateFlow()

    // Firemaking Customizer State
    private val _fmColor1 = MutableStateFlow(0xFFFF5722.toInt()) // Default Fiery Orange
    val fmColor1: StateFlow<Int> = _fmColor1.asStateFlow()

    private val _fmColor2 = MutableStateFlow(0xFFFFC107.toInt()) // Default Amber Gold
    val fmColor2: StateFlow<Int> = _fmColor2.asStateFlow()

    private val _selectedFmAnimations = MutableStateFlow(listOf("Flame Blast", "Phoenix Rise"))
    val selectedFmAnimations: StateFlow<List<String>> = _selectedFmAnimations.asStateFlow()

    // Navigation Customizer State
    private val _navColor1 = MutableStateFlow(0xFF0096C7.toInt()) // Default Ocean Azure
    val navColor1: StateFlow<Int> = _navColor1.asStateFlow()

    private val _navColor2 = MutableStateFlow(0xFF00E5FF.toInt()) // Default Seafoam Cyan
    val navColor2: StateFlow<Int> = _navColor2.asStateFlow()

    private val _selectedNavAnimations = MutableStateFlow(listOf("Ocean Waves", "Bioluminescent Surge"))
    val selectedNavAnimations: StateFlow<List<String>> = _selectedNavAnimations.asStateFlow()

    fun setFiremakingColors(c1: Int, c2: Int) {
        _fmColor1.value = c1
        _fmColor2.value = c2
        addChatMessage("🎨 Updated Level 99 Tap Flame Colors!")
    }

    fun setNavigationColors(c1: Int, c2: Int) {
        _navColor1.value = c1
        _navColor2.value = c2
        addChatMessage("⛵ Updated Seafaring Vessel & Aura Colors!")
    }

    fun toggleFiremakingAnimation(animName: String) {
        val current = _selectedFmAnimations.value.toMutableList()
        if (current.contains(animName)) {
            if (current.size > 1) {
                current.remove(animName)
            } else {
                addChatMessage("🔥 Must keep at least 1 tap animation active!")
                return
            }
        } else {
            if (current.size >= 2) {
                current.removeAt(0)
            }
            current.add(animName)
        }
        _selectedFmAnimations.value = current
        addChatMessage("🔥 Updated Level 99 Tap Animations: ${current.joinToString(", ")}")
    }

    fun toggleNavigationAnimation(animName: String) {
        val current = _selectedNavAnimations.value.toMutableList()
        if (current.contains(animName)) {
            if (current.size > 1) {
                current.remove(animName)
            } else {
                addChatMessage("⛵ Must keep at least 1 navigation animation active!")
                return
            }
        } else {
            if (current.size >= 2) {
                current.removeAt(0)
            }
            current.add(animName)
        }
        _selectedNavAnimations.value = current
        addChatMessage("⛵ Updated Navigation Trail Effects: ${current.joinToString(", ")}")
    }

    fun tapNavigationXp(xpAmount: Long = 25L) {
        addXpAndNotify(
            skill = OsrsSkill.SAILING,
            amount = xpAmount,
            gpReward = 5L,
            logTitle = "Navigation Compass Tap",
            logDesc = "Navigated open seas via ship wheel tap!"
        )
    }

    val isAfkFarmingActive: StateFlow<Boolean> get() = AfkEngine.isAfkFarmingActive

    private val _afkSeedCategory = MutableStateFlow(com.example.data.models.SeedCategory.ALL)
    val afkSeedCategory: StateFlow<com.example.data.models.SeedCategory> = _afkSeedCategory.asStateFlow()

    private val _afkFarmingRecentActions = MutableStateFlow<List<String>>(emptyList())
    val afkFarmingRecentActions: StateFlow<List<String>> = _afkFarmingRecentActions.asStateFlow()

    fun logAfkFarmingAction(action: String) {
        val current = _afkFarmingRecentActions.value.toMutableList()
        current.add(0, action)
        if (current.size > 10) {
            _afkFarmingRecentActions.value = current.take(10)
        } else {
            _afkFarmingRecentActions.value = current
        }
    }

    val isAfkBoneBuryingActive: StateFlow<Boolean> get() = AfkEngine.isAfkBoneBuryingActive

    private val _selectedSpellbook = MutableStateFlow<Spellbook>(Spellbook.STANDARD)
    val selectedSpellbook: StateFlow<Spellbook> = _selectedSpellbook.asStateFlow()

    private val _activeIncantationTimestamp = MutableStateFlow<Long>(pohPrefs.getLong("active_incantation_timestamp", 0L))
    val activeIncantationTimestamp: StateFlow<Long> = _activeIncantationTimestamp.asStateFlow()

    private val _activeIncantationIds = MutableStateFlow<Set<String>>(
        pohPrefs.getStringSet("active_incantation_ids", setOf("incant_verdant_growth")) ?: setOf("incant_verdant_growth")
    )
    val activeIncantationIds: StateFlow<Set<String>> = _activeIncantationIds.asStateFlow()

    private val _isAmbientAudioPlaying = MutableStateFlow(false)
    val isAmbientAudioPlaying: StateFlow<Boolean> = _isAmbientAudioPlaying.asStateFlow()

    private val forestAudioPlayer = com.example.audio.ForestAmbientAudioPlayer()

    private val _activeCombatSpellId = MutableStateFlow<String?>("spell_fire_strike")
    val activeCombatSpellId: StateFlow<String?> = _activeCombatSpellId.asStateFlow()

    val isAfkRunecraftingActive: StateFlow<Boolean> get() = AfkEngine.isAfkRunecraftingActive

    private val _afkRunecraftTargetRuneId = MutableStateFlow("item_rune_air")
    val afkRunecraftTargetRuneId: StateFlow<String> = _afkRunecraftTargetRuneId.asStateFlow()

    val isAfkSailingActive: StateFlow<Boolean> get() = AfkEngine.isAfkSailingActive

    val isAfkStickCraftingActive: StateFlow<Boolean> get() = AfkEngine.isAfkStickCraftingActive

    val isAfkShaftCraftingActive: StateFlow<Boolean> get() = AfkEngine.isAfkShaftCraftingActive

    val isAfkFeatherCraftingActive: StateFlow<Boolean> get() = AfkEngine.isAfkFeatherCraftingActive

    val isAfkBowstringCraftingActive: StateFlow<Boolean> get() = AfkEngine.isAfkBowstringCraftingActive

    val isAfkArrowtipCraftingActive: StateFlow<Boolean> get() = AfkEngine.isAfkArrowtipCraftingActive

    val isAfkFletchingActive: StateFlow<Boolean> get() = AfkEngine.isAfkFletchingActive

    val isAfkSmithingAnvilActive: StateFlow<Boolean> get() = AfkEngine.isAfkSmithingAnvilActive

    val isAfkHerbCrushingActive: StateFlow<Boolean> get() = AfkEngine.isAfkHerbCrushingActive
    val isAfkHerbCleaningActive: StateFlow<Boolean> get() = AfkEngine.isAfkHerbCleaningActive

    val isAfkPotionBrewingActive: StateFlow<Boolean> get() = AfkEngine.isAfkPotionBrewingActive

    private val _activeFletchRecipe = MutableStateFlow<FletchRecipe?>(FletchingData.ARROW_RECIPES.first())
    val activeFletchRecipe: StateFlow<FletchRecipe?> = _activeFletchRecipe.asStateFlow()

    private val _activeSmeltRecipe = MutableStateFlow<SmeltRecipe?>(SmithingData.SMELT_RECIPES.first())
    val activeSmeltRecipe: StateFlow<SmeltRecipe?> = _activeSmeltRecipe.asStateFlow()

    private val _activeSmithAnvilRecipe = MutableStateFlow<SmithAnvilRecipe?>(null)
    val activeSmithAnvilRecipe: StateFlow<SmithAnvilRecipe?> = _activeSmithAnvilRecipe.asStateFlow()

    private val _activeCrushHerbRecipe = MutableStateFlow<HerbCrushingRecipe?>(HerbloreData.CRUSH_HERB_RECIPES.first())
    val activeCrushHerbRecipe: StateFlow<HerbCrushingRecipe?> = _activeCrushHerbRecipe.asStateFlow()
    val activeCleanHerbRecipe: StateFlow<HerbCleaningRecipe?> get() = _activeCrushHerbRecipe

    // Universal Item Obtainment Inspector (Long-press on any unowned/owned item across tabs)
    private val _inspectedObtainItem = MutableStateFlow<EncyclopediaItem?>(null)
    val inspectedObtainItem: StateFlow<EncyclopediaItem?> = _inspectedObtainItem.asStateFlow()

    // Adventuring Dungeon State & Spirit Pool State
    private val _adventuringFloor = MutableStateFlow(1)
    val adventuringFloor: StateFlow<Int> = _adventuringFloor.asStateFlow()

    private val _adventuringMaxFloor = MutableStateFlow(1)
    val adventuringMaxFloor: StateFlow<Int> = _adventuringMaxFloor.asStateFlow()

    private val _adventuringCurrentMonster = MutableStateFlow<com.example.data.models.AdventuringMonster?>(com.example.data.models.AdventuringStoryData.getFloor(1).monsters.first())
    val adventuringCurrentMonster: StateFlow<com.example.data.models.AdventuringMonster?> = _adventuringCurrentMonster.asStateFlow()

    private val _adventuringMonsterHp = MutableStateFlow(40)
    val adventuringMonsterHp: StateFlow<Int> = _adventuringMonsterHp.asStateFlow()

    private val _enemyLastPlayedAttack = MutableStateFlow<com.example.data.models.EnemyAttack?>(null)
    val enemyLastPlayedAttack: StateFlow<com.example.data.models.EnemyAttack?> = _enemyLastPlayedAttack.asStateFlow()

    private val _enemyAttackTrigger = MutableStateFlow(0)
    val enemyAttackTrigger: StateFlow<Int> = _enemyAttackTrigger.asStateFlow()

    private val _adventuringPetHp = MutableStateFlow(100)
    val adventuringPetHp: StateFlow<Int> = _adventuringPetHp.asStateFlow()

    private val _adventuringPetMaxHp = MutableStateFlow(100)
    val adventuringPetMaxHp: StateFlow<Int> = _adventuringPetMaxHp.asStateFlow()

    private val _adventuringLog = MutableStateFlow<List<String>>(listOf("⚔️ Entered Floor 1: Whispering Grove! Encountered Corrupted Spirit Slime!"))
    val adventuringLog: StateFlow<List<String>> = _adventuringLog.asStateFlow()

    private val _selectedSpiritPoolAreaId = MutableStateFlow("area_coastal")
    val selectedSpiritPoolAreaId: StateFlow<String> = _selectedSpiritPoolAreaId.asStateFlow()

    private val _activePotionRecipe = MutableStateFlow<PotionBrewRecipe?>(HerbloreData.POTION_RECIPES.first())
    val activePotionRecipe: StateFlow<PotionBrewRecipe?> = _activePotionRecipe.asStateFlow()

    private val _selectedSlayerMonster = MutableStateFlow<com.example.data.models.SlayerMonster>(com.example.data.models.SlayerData.MONSTERS.first())
    val selectedSlayerMonster: StateFlow<com.example.data.models.SlayerMonster> = _selectedSlayerMonster.asStateFlow()

    private val _selectedCombatStyle = MutableStateFlow<com.example.data.models.CombatStyle>(com.example.data.models.CombatStyle.ATTACK)
    val selectedCombatStyle: StateFlow<com.example.data.models.CombatStyle> = _selectedCombatStyle.asStateFlow()

    private val _isDefensiveCombatMode = MutableStateFlow(false)
    val isDefensiveCombatMode: StateFlow<Boolean> = _isDefensiveCombatMode.asStateFlow()

    fun toggleDefensiveCombatMode() {
        _isDefensiveCombatMode.value = !_isDefensiveCombatMode.value
        addChatMessage("⚔️ Defensive Stance: ${if (_isDefensiveCombatMode.value) "ON (50% Combat XP to Defence)" else "OFF"}")
    }

    val isAfkHunterActive: StateFlow<Boolean> get() = AfkEngine.isAfkHunterActive

    private val _selectedHunterCreature = MutableStateFlow<com.example.data.models.HunterCreature>(com.example.data.models.HunterData.CREATURES.first())
    val selectedHunterCreature: StateFlow<com.example.data.models.HunterCreature> = _selectedHunterCreature.asStateFlow()

    // AFK Boss States
    val isAfkBossActive: StateFlow<Boolean> get() = AfkEngine.isAfkBossActive

    private val _selectedBossMonster = MutableStateFlow<com.example.data.models.BossMonster>(com.example.data.models.BossData.BOSSES.first())
    val selectedBossMonster: StateFlow<com.example.data.models.BossMonster> = _selectedBossMonster.asStateFlow()

    // Active Quest Expedition State
    data class QuestExpeditionState(
        val quest: com.example.data.models.OsrsQuest,
        val totalDurationSeconds: Int,
        val remainingSeconds: Int,
        val progressFraction: Float,
        val isPaused: Boolean = false,
        val lastUpdatedTimestamp: Long = System.currentTimeMillis()
    )
    private val _activeQuestExpedition = MutableStateFlow<QuestExpeditionState?>(null)
    val activeQuestExpedition: StateFlow<QuestExpeditionState?> = _activeQuestExpedition.asStateFlow()
    private var questExpeditionJob: kotlinx.coroutines.Job? = null

    private val _completedQuestQueue = MutableStateFlow<List<com.example.data.models.OsrsQuest>>(emptyList())
    val completedQuestPopup: StateFlow<com.example.data.models.OsrsQuest?> = _completedQuestQueue
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun dismissQuestCompletionPopup() {
        _completedQuestQueue.update { if (it.isNotEmpty()) it.drop(1) else emptyList() }
    }

    fun showQuestCompletionPopup(quest: com.example.data.models.OsrsQuest) {
        _completedQuestQueue.update { queue ->
            if (queue.any { it.id == quest.id }) queue else queue + quest
        }
    }

    private val _offlineGainsReport = MutableStateFlow<com.example.data.models.OfflineGainsReport?>(null)
    val offlineGainsReport: StateFlow<com.example.data.models.OfflineGainsReport?> = _offlineGainsReport.asStateFlow()

    fun dismissOfflineGainsReport() {
        _offlineGainsReport.value = null
    }

    val savedQuestProgressMap: StateFlow<Map<String, com.example.data.db.QuestProgressEntity>> = petState.flatMapLatest { state ->
        repository.getAllQuestProgress(state.petType.name)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val equippedItems: StateFlow<Map<com.example.data.models.EquipmentSlot, com.example.data.models.InventoryItem>> = petState.flatMapLatest { state ->
        repository.getEquippedItems(state.petType.name)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    // Real-Life Skill App Listeners Map (OsrsSkill -> IsEnabled)
    private val _skillAppListeners = MutableStateFlow<Map<OsrsSkill, Boolean>>(
        mapOf(
            OsrsSkill.WOODCUTTING to true,
            OsrsSkill.MAGIC to true,
            OsrsSkill.RUNECRAFT to true,
            OsrsSkill.COOKING to true,
            OsrsSkill.AGILITY to true,
            OsrsSkill.HERBLORE to true
        )
    )
    val skillAppListeners: StateFlow<Map<OsrsSkill, Boolean>> = _skillAppListeners.asStateFlow()

    fun toggleSkillAppListener(skill: OsrsSkill) {
        val current = _skillAppListeners.value.toMutableMap()
        val newValue = !(current[skill] ?: true)
        current[skill] = newValue
        _skillAppListeners.value = current
        addChatMessage("📱 App Listener for ${skill.displayName} is now ${if (newValue) "ENABLED ⚡" else "DISABLED ❌"}")
    }

    // Task XP Overrides map (task_id -> XP)
    private val _taskXpOverrides = MutableStateFlow<Map<String, Long>>(emptyMap())
    val taskXpOverrides: StateFlow<Map<String, Long>> = _taskXpOverrides.asStateFlow()

    // Pet Mood State & Activity Frequency Tracking
    private val _manualMoodBoost = MutableStateFlow(0)
    private val _tickerTrigger = MutableStateFlow(System.currentTimeMillis())

    val petMoodState: StateFlow<PetMoodState> = combine(
        activityLogs,
        _manualMoodBoost,
        _tickerTrigger
    ) { logs, boost, _ ->
        calculatePetMoodState(logs, boost)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PetMoodState()
    )

    private fun calculatePetMoodState(logs: List<ActivityLog>, boost: Int): PetMoodState {
        val now = System.currentTimeMillis()
        val logs15m = logs.count { (now - it.timestamp) <= 15 * 60 * 1000L }
        val logs1h = logs.count { (now - it.timestamp) <= 60 * 60 * 1000L }
        val lastLog = logs.maxByOrNull { it.timestamp }
        val timeSinceLastMs = if (lastLog != null) (now - lastLog.timestamp) else (120 * 60 * 1000L)
        val minsSinceLast = timeSinceLastMs / (60 * 1000L)

        val baseScore = when {
            minsSinceLast < 5 -> 70
            minsSinceLast < 15 -> 55
            minsSinceLast < 30 -> 40
            minsSinceLast < 60 -> 25
            minsSinceLast < 120 -> 15
            else -> 5
        }

        val freqBonus15m = (logs15m * 10).coerceAtMost(40)
        val freqBonus1h = (logs1h * 3).coerceAtMost(15)

        val rawScore = baseScore + freqBonus15m + freqBonus1h + boost
        val score = rawScore.coerceIn(0, 100)
        val level = PetMoodLevel.fromScore(score)

        return PetMoodState(
            moodScore = score,
            level = level,
            recentActivityCount15m = logs15m,
            recentActivityCount1h = logs1h,
            lastActivityTimeMs = lastLog?.timestamp ?: now
        )
    }

    fun boostPetMood(amount: Int = 15, reason: String = "Interaction") {
        val effectiveBoost = if (isIncantationActiveAndUsable("incant_flowing_springs")) (amount * 1.20).toInt() else amount
        _manualMoodBoost.value = (_manualMoodBoost.value + effectiveBoost).coerceAtMost(40)
        addChatMessage("✨ Pet Mood boosted (+$effectiveBoost Mood) from $reason!")
    }

    fun getTaskXp(taskId: String, defaultXp: Long): Long {
        return _taskXpOverrides.value[taskId] ?: defaultXp
    }

    fun updateTaskXp(taskId: String, newXp: Long) {
        val current = _taskXpOverrides.value.toMutableMap()
        current[taskId] = newXp
        _taskXpOverrides.value = current
        addChatMessage("System: Updated XP reward for task '$taskId' to $newXp XP!")
    }

    fun updateQuestXp(questId: String, newXp: Long) {
        viewModelScope.launch {
            val list = quests.value.toMutableList()
            val index = list.indexOfFirst { it.id == questId }
            if (index != -1) {
                val updated = list[index].copy(rewardXp = newXp)
                list[index] = updated
                repository.saveQuest(updated)
                addChatMessage("System: Updated XP reward for quest '${updated.title}' to $newXp XP!")
            }
        }
    }

    /**
     * Build room in POH Construction at specific grid position.
     */
    fun buildRoomInPoh(roomType: PohRoomType, targetSlot: Int? = null) {
        viewModelScope.launch {
            val pet = petState.value
            val currentConXp = skillXpMap.value[OsrsSkill.CONSTRUCTION] ?: 0L
            val conLvl = OsrsXpCalculator.getLevelForXp(currentConXp)

            if (conLvl < roomType.reqLevel) {
                addChatMessage("System: Construction level ${roomType.reqLevel} required to build ${roomType.displayName}!")
                return@launch
            }
            if (pet.coinsGp < roomType.buildCostGp) {
                addChatMessage("System: You need ${roomType.buildCostGp} GP to build ${roomType.displayName}!")
                return@launch
            }

            val currentPoh = _pohHouseState.value
            val gridDimension = com.example.data.models.getPohGridDimension(conLvl, currentPoh.extraGridSize)
            val maxSlots = gridDimension * gridDimension

            val occupiedSlots = currentPoh.builtRooms.map { it.gridPosition }.toSet()

            val slot = targetSlot ?: (0 until maxSlots).firstOrNull { it !in occupiedSlots }
            if (slot == null || slot !in 0 until maxSlots) {
                addChatMessage("System: Your ${gridDimension}x${gridDimension} house layout is full! Expand your grid with GP or demolish a room first.")
                return@launch
            }

            // Deduct GP
            val updatedPet = pet.copy(coinsGp = pet.coinsGp - roomType.buildCostGp)
            repository.savePetState(updatedPet)

            // Remove any existing room in target slot (if replacing)
            val filteredRooms = currentPoh.builtRooms.filter { it.gridPosition != slot }
            val defaultFloor = if (roomType == PohRoomType.GRASS_PATCH) {
                com.example.data.models.PohFloorType.VERDANT_TURF
            } else {
                com.example.data.models.PohFloorType.DEFAULT_WOOD
            }
            val newRoom = BuiltRoom(
                id = "room_${System.currentTimeMillis()}",
                roomType = roomType,
                gridPosition = slot,
                floorType = defaultFloor
            )
            updatePohHouseState(currentPoh.copy(builtRooms = filteredRooms + newRoom))

            val row = slot / gridDimension + 1
            val col = slot % gridDimension + 1
            addXpAndNotify(
                skill = OsrsSkill.CONSTRUCTION,
                amount = 400L,
                gpReward = 0L,
                logTitle = "Built POH Room: ${roomType.displayName}",
                logDesc = "Built ${roomType.displayName} at Grid Position (Row $row, Col $col)!"
            )
        }
    }

    /**
     * Expand POH grid by +1 row and +1 column for GP.
     */
    fun expandPohGridForGp(costGp: Long) {
        viewModelScope.launch {
            val pet = petState.value
            if (pet.coinsGp < costGp) {
                addChatMessage("System: Not enough GP to expand your POH grid layout! Requires ${costGp} GP.")
                return@launch
            }

            val currentConXp = skillXpMap.value[OsrsSkill.CONSTRUCTION] ?: 0L
            val conLvl = OsrsXpCalculator.getLevelForXp(currentConXp)
            val currentPoh = _pohHouseState.value
            val oldDim = com.example.data.models.getPohGridDimension(conLvl, currentPoh.extraGridSize)
            val newDim = oldDim + 1

            // Deduct coins
            val updatedPet = pet.copy(coinsGp = pet.coinsGp - costGp)
            repository.savePetState(updatedPet)

            // Remap existing rooms so their visual (row, col) coordinates stay identical
            val updatedRooms = currentPoh.builtRooms.map { room ->
                val r = room.gridPosition / oldDim
                val c = room.gridPosition % oldDim
                val newPos = r * newDim + c
                room.copy(gridPosition = newPos)
            }

            val newExtraSize = currentPoh.extraGridSize + 1
            updatePohHouseState(
                currentPoh.copy(
                    builtRooms = updatedRooms,
                    extraGridSize = newExtraSize
                )
            )

            addChatMessage("🏡 POH Expansion: Your estate has expanded to ${newDim}x${newDim}! (+1 Row & +1 Column added)")
            addXpAndNotify(
                skill = OsrsSkill.CONSTRUCTION,
                amount = 1000L,
                gpReward = 0L,
                logTitle = "Expanded House Grid",
                logDesc = "Expanded estate layout to ${newDim}x${newDim} grid!"
            )
        }
    }

    /**
     * Demolish a room from the POH house layout.
     */
    /**
     * Update room wall configurations (North, East, South, West) and flooring type.
     */
    fun updateRoomWallsAndFloor(
        room: BuiltRoom,
        wallNorth: com.example.data.models.PohWallType,
        wallEast: com.example.data.models.PohWallType,
        wallSouth: com.example.data.models.PohWallType,
        wallWest: com.example.data.models.PohWallType,
        floorType: com.example.data.models.PohFloorType
    ) {
        viewModelScope.launch {
            val pet = petState.value
            val currentPoh = _pohHouseState.value
            var totalCostGp = 0L
            if (wallNorth != room.wallNorth && wallNorth != com.example.data.models.PohWallType.NONE) totalCostGp += wallNorth.costGp
            if (wallEast != room.wallEast && wallEast != com.example.data.models.PohWallType.NONE) totalCostGp += wallEast.costGp
            if (wallSouth != room.wallSouth && wallSouth != com.example.data.models.PohWallType.NONE) totalCostGp += wallSouth.costGp
            if (wallWest != room.wallWest && wallWest != com.example.data.models.PohWallType.NONE) totalCostGp += wallWest.costGp
            if (floorType != room.floorType) totalCostGp += floorType.costGp

            if (pet.coinsGp < totalCostGp) {
                addChatMessage("System: Not enough GP to construct these walls & flooring! Requires $totalCostGp GP.")
                return@launch
            }

            if (totalCostGp > 0L) {
                val updatedPet = pet.copy(coinsGp = pet.coinsGp - totalCostGp)
                repository.savePetState(updatedPet)
            }

            val updatedRooms = currentPoh.builtRooms.map {
                if (it.id == room.id) {
                    it.copy(
                        wallNorth = wallNorth,
                        wallEast = wallEast,
                        wallSouth = wallSouth,
                        wallWest = wallWest,
                        floorType = floorType
                    )
                } else it
            }

            updatePohHouseState(currentPoh.copy(builtRooms = updatedRooms))
            addChatMessage("🏡 Masonry: Built walls (N: ${wallNorth.displayName}, E: ${wallEast.displayName}, S: ${wallSouth.displayName}, W: ${wallWest.displayName}) & floor (${floorType.displayName})!")
            if (totalCostGp > 0L) {
                addXpAndNotify(
                    skill = com.example.data.models.OsrsSkill.CONSTRUCTION,
                    amount = (totalCostGp / 4L).coerceAtLeast(40L),
                    gpReward = 0L,
                    logTitle = "POH Masonry & Flooring",
                    logDesc = "Constructed walls and flooring for ${room.roomType.displayName}!"
                )
            }
        }
    }

    fun demolishRoomInPoh(room: BuiltRoom) {
        viewModelScope.launch {
            val currentConXp = skillXpMap.value[OsrsSkill.CONSTRUCTION] ?: 0L
            val conLvl = OsrsXpCalculator.getLevelForXp(currentConXp)
            val currentPoh = _pohHouseState.value
            val gridDimension = com.example.data.models.getPohGridDimension(conLvl, currentPoh.extraGridSize)
            updatePohHouseState(currentPoh.copy(builtRooms = currentPoh.builtRooms.filter { it.id != room.id }))
            val row = room.gridPosition / gridDimension + 1
            val col = room.gridPosition % gridDimension + 1
            addChatMessage("System: Demolished ${room.roomType.displayName} from Grid Position (Row $row, Col $col).")
        }
    }

    /**
     * Build furniture in POH Construction room.
     */
    fun buildFurnitureInPoh(room: BuiltRoom, furniture: PohFurnitureItem) {
        viewModelScope.launch {
            val currentPoh = _pohHouseState.value
            val currentPohMatInv = currentPoh.materialInventory.toMutableMap()

            // Check materials across POH mat inv + Storage (Inventory + Bank)
            for ((mat, reqQty) in furniture.requiredMaterials) {
                val pohMatQty = currentPohMatInv[mat] ?: 0
                val itemId = mat.itemId
                val invBankQty = getItemQuantityCombined(itemId)
                val totalAvailable = pohMatQty + invBankQty
                if (totalAvailable < reqQty) {
                    addChatMessage("System: Missing $reqQty ${mat.displayName} (Have $totalAvailable across POH/Storage)! Buy on GE or craft at Sawmill.")
                    return@launch
                }
            }

            // Deduct materials
            for ((mat, reqQty) in furniture.requiredMaterials) {
                var needed = reqQty
                val pohMatQty = currentPohMatInv[mat] ?: 0
                val fromPoh = pohMatQty.coerceAtMost(needed)
                currentPohMatInv[mat] = pohMatQty - fromPoh
                needed -= fromPoh

                if (needed > 0) {
                    val itemId = mat.itemId
                    deductItemCombined(itemId, needed)
                }
            }

            // Update room furniture list
            val updatedRooms = currentPoh.builtRooms.map { r ->
                if (r.id == room.id) {
                    r.copy(builtFurnitureIds = r.builtFurnitureIds + furniture.id)
                } else r
            }

            updatePohHouseState(currentPoh.copy(
                builtRooms = updatedRooms,
                materialInventory = currentPohMatInv
            ))

            addXpAndNotify(
                skill = OsrsSkill.CONSTRUCTION,
                amount = furniture.xpGained,
                gpReward = 0L,
                logTitle = "Built ${furniture.name}",
                logDesc = "Constructed ${furniture.name} in ${room.roomType.displayName}!"
            )
        }
    }

    /**
     * Destroy built furniture in a POH room.
     */
    fun destroyFurnitureInPoh(room: BuiltRoom, furnitureId: String) {
        viewModelScope.launch {
            val currentPoh = _pohHouseState.value
            val updatedRooms = currentPoh.builtRooms.map { r ->
                if (r.id == room.id) {
                    r.copy(builtFurnitureIds = r.builtFurnitureIds.filter { id -> id != furnitureId })
                } else r
            }

            updatePohHouseState(currentPoh.copy(builtRooms = updatedRooms))
            addChatMessage("System: Destroyed furniture in ${room.roomType.displayName}.")
        }
    }

    /**
     * Buy construction materials on GE.
     */
    fun buyGeMaterial(material: GeMaterial, quantity: Int) {
        viewModelScope.launch {
            val pet = petState.value
            val totalCost = material.defaultPriceGp * quantity

            if (pet.coinsGp < totalCost) {
                addChatMessage("System: You need $totalCost GP to buy $quantity x ${material.displayName}!")
                return@launch
            }

            // Deduct GP and add material to inventory
            val newPet = pet.copy(coinsGp = pet.coinsGp - totalCost)
            repository.savePetState(newPet)

            val currentPoh = _pohHouseState.value
            val currentInv = currentPoh.materialInventory.toMutableMap()
            currentInv[material] = (currentInv[material] ?: 0) + quantity
            updatePohHouseState(currentPoh.copy(materialInventory = currentInv))

            addChatMessage("GE Exchange: Bought $quantity x ${material.displayName} for $totalCost GP!")
        }
    }

    init {
        // Ambient audio will start when the app is actively foregrounded in onAppForegrounded()
        loadGoogleTasks()
        viewModelScope.launch {
            cleanupDuplicates()
            repository.migrateInventoryToStorage(petState.value.petType.name)
            migrateLegacySkillOutfitItems()
        }
        viewModelScope.launch {
            petState.collect { state ->
                _pohHouseState.value = loadPohHouseState(state.petType.name)
                _pofState.value = loadPofState(state.petType.name)
                for (qId in state.completedQuestIds) {
                    markTotemUnlocked(qId)
                }
            }
        }
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        stepCounterManager.startListening()

        // Restore active saved quest expedition on app launch
        viewModelScope.launch {
            savedQuestProgressMap.collect { map ->
                if (_activeQuestExpedition.value == null && map.isNotEmpty()) {
                    val activeSaved = map.values.find { !it.isPaused }
                    if (activeSaved != null) {
                        val quest = com.example.data.models.OsrsQuestData.findQuestById(activeSaved.questId)
                        if (quest != null && !petState.value.completedQuestIds.contains(quest.id)) {
                            val elapsedSec = ((System.currentTimeMillis() - activeSaved.lastUpdatedTimestamp) / 1000).toInt()
                            val speedMultiplier = if (isIncantationActiveAndUsable("incant_wild_wind")) 2 else 1
                            val remaining = activeSaved.remainingSeconds - (elapsedSec * speedMultiplier)
                            if (remaining <= 0) {
                                completeQuestExpedition(quest)
                            } else {
                                startQuestExpedition(quest)
                            }
                        }
                    }
                }
            }
        }

        // Sync step updates to Agility XP
        var lastAwardedSteps = 0L
        viewModelScope.launch {
            stepCounterManager.stepCount.collect { totalSteps ->
                if (lastAwardedSteps == 0L) {
                    lastAwardedSteps = totalSteps
                    return@collect
                }
                if (totalSteps > lastAwardedSteps) {
                    val delta = totalSteps - lastAwardedSteps
                    lastAwardedSteps = totalSteps
                    addXpAndNotify(
                        skill = OsrsSkill.AGILITY,
                        amount = delta * 2L,
                        gpReward = (delta / 2L).coerceAtLeast(1L),
                        logTitle = "Real Life Walk",
                        logDesc = "Walked $delta steps!"
                    )
                    progressSkillContract(OsrsSkill.AGILITY, (delta / 50).toInt().coerceAtLeast(1), "agility")
                }
            }
        }

        // Collect system notification dismissals & app events for automatic XP & Quest Completion
        viewModelScope.launch {
            com.example.services.NotificationDismissListenerService.notificationDismissedFlow.collect { pkg ->
                onSwipeNotificationAction()
                handleAppEvent(pkg)
            }
        }

        viewModelScope.launch {
            com.example.services.NotificationDismissListenerService.appEventFlow.collect { pkg ->
                handleAppEvent(pkg)
            }
        }

        // Check for 7+ hours of phone inactivity / sleep on startup
        checkInactivitySleep()

        // Initialize Skill Guild Contracts
        initSkillContracts()

        // Restore AFK state & process offline progress for time elapsed while app was closed
        viewModelScope.launch {
            loadAfkStateFromPrefs()
            processOfflineAfkProgress()
            processOfflineQuestProgress()
        }

        // Background ticker for AFK POH Stations & Pet Mood Indicator Updates
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(10000L) // every 10 seconds
                if (!isAppInForeground) {
                    continue
                }
                _tickerTrigger.value = System.currentTimeMillis()
                saveAfkStateToPrefs()
                processHusbandryTick()
                if (_manualMoodBoost.value > 0) {
                    _manualMoodBoost.value = (_manualMoodBoost.value - 1).coerceAtLeast(0)
                }

                // --- ACTIVE INCANTATION PASSIVE XP & BUFF TICKS ---
                val activeIncants = _activeIncantationIds.value
                if (activeIncants.isNotEmpty()) {
                    val currentPetType = petState.value.petType.name
                    activeIncants.forEach { incantId ->
                        if (isIncantationActiveAndUsable(incantId)) {
                            val incant = com.example.data.models.IncantationsData.ALL_INCANTATIONS.find { it.id == incantId }
                            if (incant != null) {
                                val tickXp = (incant.xpPerMinute / 6L).coerceAtLeast(1L)
                                repository.addXpToSkillDirect(currentPetType, OsrsSkill.MAGIC, tickXp, 0L, "Chant Aura", "Passive ${incant.name}")
                            }
                        }
                    }
                }

                // Tide of Solar Vitality: Passive HP regeneration tick (+5%, +10%, +15%)
                val solarRegenPct = when {
                    isIncantationActiveAndUsable("incant_solar_vitality_t3") -> 0.15
                    isIncantationActiveAndUsable("incant_solar_vitality_t2") -> 0.10
                    isIncantationActiveAndUsable("incant_solar_vitality") -> 0.05
                    else -> 0.0
                }
                if (solarRegenPct > 0.0) {
                    val curPet = petState.value
                    val maxHp = getPetMaxHealth()
                    if (curPet.health < maxHp) {
                        val regenHp = (maxHp * solarRegenPct).toInt().coerceAtLeast(1)
                        repository.savePetState(curPet.copy(health = (curPet.health + regenHp).coerceAtMost(maxHp)))
                    }
                }

                val activeActivity = AfkEngine.currentActivity.value
                if (activeActivity != null) {
                    val currentPet = petState.value
                    if (currentPet.health <= 0) {
                        stopAllAfkStations("Pet has no health (0 HP)!")
                        notifyAfkStopped(activeActivity.displayName, "${currentPet.customName} has fainted with 0 HP! Feed food to heal.")
                        addChatMessage("💔 ${activeActivity.displayName} Stopped: ${currentPet.customName} has 0 HP! Feed food from inventory to heal your companion.")
                    } else if (currentPet.hunger <= 0) {
                        stopAllAfkStations("Pet is out of energy & hunger (0% Hunger)!")
                        notifyAfkStopped(activeActivity.displayName, "${currentPet.customName} is starving with 0% Hunger! Feed food to resume.")
                        addChatMessage("⚠️ ${activeActivity.displayName} Stopped: ${currentPet.customName} is starving with 0% Hunger! Feed your companion to resume AFK activities.")
                    } else {
                        // Fast map lookup for materials
                        val invMap = inventoryItems.value.associate { it.id to it.quantity }
                        val bankMap = bankItems.value.associate { it.id to it.quantity }
                        val combinedMap = (invMap.keys + bankMap.keys).associateWith { (invMap[it] ?: 0) + (bankMap[it] ?: 0) }

                        val hasFarmingPlotsActive = _pofState.value.plots.any { it.cropType != null }
                        val hasFuel = AfkEngine.hasMaterials(
                            activity = activeActivity,
                            itemQuantityMap = combinedMap,
                            selectedTrapId = _selectedCraftingTrapId.value,
                            cookingQueue = _cookingQueue.value,
                            selectedFoodId = _selectedFoodId.value,
                            activeSmeltRecipe = _activeSmeltRecipe.value,
                            activeSmithAnvilRecipe = _activeSmithAnvilRecipe.value,
                            activeFletchRecipe = _activeFletchRecipe.value,
                            activeCrushHerbRecipe = _activeCrushHerbRecipe.value,
                            activePotionRecipe = _activePotionRecipe.value,
                            hasFarmingPlotsActive = hasFarmingPlotsActive
                        )

                        if (!hasFuel) {
                            val stationName = activeActivity.displayName
                            stopAllAfkStations()
                            addChatMessage("⚠️ $stationName: Out of fuel/materials! AFK activity automatically turned OFF.")
                        } else {
                            afkTickCount++
                            // Gentle hunger drain: only 1 hunger point every 6 ticks (1 minute)
                            val shouldDrainHunger = (afkTickCount % 6 == 0)
                            if (shouldDrainHunger && currentPet.hunger > 0) {
                                val newHunger = (currentPet.hunger - 1).coerceAtLeast(0)
                                repository.savePetState(currentPet.copy(hunger = newHunger))
                            }

                            when (activeActivity) {
                                AfkActivityType.CAMPFIRE -> burnLogsAtCampfire(isAfk = true)
                                AfkActivityType.COOKING -> cookRawFoodAtRange(targetFoodId = _selectedFoodId.value, isAfk = true)
                                AfkActivityType.FISHING -> fishAtPohPond(isAfk = true)
                                AfkActivityType.MINING -> mineAtPohQuarry(isAfk = true)
                                AfkActivityType.WOODCUTTING -> chopTrees(isAfk = true)
                                AfkActivityType.SMELTING -> {
                                    val recipe = _activeSmeltRecipe.value
                                    if (recipe != null) {
                                        smeltRecipe(recipe, isAfk = true)
                                    } else {
                                        smeltOresAtFurnace(targetBarId = _selectedBarId.value, isAfk = true)
                                    }
                                }
                                AfkActivityType.NAIL_CRAFTING -> craftBarsToNailsAtAnvil(isAfk = true)
                                AfkActivityType.STICK_CRAFTING -> craftLogsToSticks(isAfk = true)
                                AfkActivityType.SHAFT_CRAFTING -> craftLogsToShafts(isAfk = true)
                                AfkActivityType.FEATHER_CRAFTING -> craftFeathers(isAfk = true)
                                AfkActivityType.BOWSTRING_CRAFTING -> craftBowstrings(isAfk = true)
                                AfkActivityType.ARROWTIP_CRAFTING -> craftBarsToArrowtips(isAfk = true)
                                AfkActivityType.TRAP_CRAFTING -> craftHunterTrap(_selectedCraftingTrapId.value, isAfk = true)
                                AfkActivityType.FLETCHING -> {
                                    _activeFletchRecipe.value?.let { fletchRecipe(it, isAfk = true) } ?: fletchSticksToArrows(isAfk = true)
                                }
                                AfkActivityType.SMITHING_ANVIL -> {
                                    _activeSmithAnvilRecipe.value?.let { smithAnvilRecipe(it, isAfk = true) }
                                }
                                AfkActivityType.HERB_CRUSHING -> {
                                    _activeCrushHerbRecipe.value?.let { crushHerbRecipe(it, isAfk = true) }
                                }
                                AfkActivityType.POTION_BREWING -> {
                                    _activePotionRecipe.value?.let { brewPotionRecipe(it, isAfk = true) }
                                }
                                AfkActivityType.SAWMILL -> convertLogsToPlanksAtSawmill(isAfk = true)
                                AfkActivityType.SLAYER -> fightSlayerMonster(_selectedSlayerMonster.value, isAfk = true)
                                AfkActivityType.HUNTER -> huntCreature(_selectedHunterCreature.value, isAfk = true)
                                AfkActivityType.BOSS -> fightBossOnce(_selectedBossMonster.value, isAfk = true)
                                AfkActivityType.FARMING -> processAfkFarmingTick()
                                AfkActivityType.BONE_BURYING -> buryBonesFromInventory(isAfk = true)
                                AfkActivityType.SAILING -> sailOnAfkTick()
                                AfkActivityType.RUNECRAFTING -> processAfkRunecraftingTick()
                                AfkActivityType.THIEVING -> processAfkThievingTick()
                                AfkActivityType.CATACOMBS -> processAfkCatacombsTick()
                                AfkActivityType.DRUID_ALTAR -> craftDruidAltarEffigy(isAfk = true)
                            }

                            AfkEngine.incrementTick()
                            afkSessionTicks++
                            updateAfkTaskbarNotification()
                            pohPrefs.edit().putLong("afk_last_timestamp", System.currentTimeMillis()).apply()
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        stepCounterManager.stopListening()
    }

    fun dismissLevelUpEvent() {
        _levelUpEvent.value = null
    }

    fun addChatMessage(msg: String) {
        val current = _chatMessages.value.toMutableList()
        current.add(msg)
        if (current.size > 100) {
            current.removeAt(0)
        }
        _chatMessages.value = current
    }

    /**
     * Skill Item Drop generator: Gaining XP in skills drops thematic OSRS items into inventory!
     */
    private fun rollSkillItemDrop(skill: OsrsSkill) {
        // Disabled per user directive: gaining XP in skills should not drop items into inventory.
    }

    /**
     * Core method to award XP, detect level up, add GP, and log.
     */
    fun addXpAndNotify(
        skill: OsrsSkill,
        amount: Long,
        gpReward: Long = 0L,
        logTitle: String,
        logDesc: String
    ) {
        if (skill == OsrsSkill.ADVENTURING) {
            // Absolutely nothing gives adventuring experience!
            // The only way to advance adventuring levels is to clear the corresponding floor.
            return
        }
        viewModelScope.launch {
            val currentMood = petMoodState.value
            val incantMultiplier = getIncantationXpMultiplier(skill, logTitle, logDesc)
            val unlockedOutfits = petState.value.unlockedOutfitIds.toSet()
            val skillingGearBonusPct = com.example.data.models.SkillOutfitData.ALL_PIECES
                .filter { it.skill == skill && unlockedOutfits.contains(it.id) }
                .sumOf { it.bonusXpPercent }
            val gearMultiplier = 1.0 + (skillingGearBonusPct / 100.0)
            val baseTaskXp = getTaskXp(logTitle, amount)
            val effectiveAmount = (baseTaskXp * incantMultiplier * gearMultiplier).toLong().coerceAtLeast(1L)
            if (effectiveAmount <= 0) return@launch

            // Boost mood slightly on activity
            _manualMoodBoost.value = (_manualMoodBoost.value + 5).coerceAtMost(40)

            // Check 2x Gold multiplier for Trickery (Thieving) if Swamp Guardian Consecration quest is complete
            val hasNatureSpiritQuest = petState.value.completedQuestIds.contains("quest_nature_spirit") ||
                    petState.value.completedQuestIds.contains("quest_nature_spirit_part2")
            val effectiveGpReward = if (skill == OsrsSkill.THIEVING && gpReward > 0L && hasNatureSpiritQuest) {
                gpReward * 2L
            } else {
                gpReward
            }

            val currentPetType = petState.value.petType.name
            val oldXp = repository.getSkillXpDirect(currentPetType, skill)
            val oldLevel = OsrsXpCalculator.getLevelForXp(oldXp)

            val newXp = oldXp + effectiveAmount
            val newLevel = OsrsXpCalculator.getLevelForXp(newXp)

            repository.addXpToSkillDirect(currentPetType, skill, effectiveAmount, effectiveGpReward, logTitle, logDesc)

            // Update GP and Pet Stats
            var currentPet = petState.value
            val newGp = currentPet.coinsGp + effectiveGpReward
            val happyGain = if (isIncantationActiveAndUsable("incant_flowing_springs")) 6 else 5
            val newHappiness = (currentPet.happiness + happyGain).coerceAtMost(100)

            // Check rare skilling pet drop chance for the trained skill!
            val unobtainedSkillPets = PetType.entries.filter { it.primarySkill == skill && !currentPet.unlockedPets.contains(it) }
            var updatedUnlocked = currentPet.unlockedPets
            var rarePetQuote = "${currentMood.level.quotePrefix}${currentPet.petType.getRandomQuote()}"

            if (unobtainedSkillPets.isNotEmpty()) {
                // Rare drop roll: 1 in 10 chance (doubled to 1 in 5 with Ritual of Spirit Gateways active)
                val rollMax = if (isIncantationActiveAndUsable("incant_spirit_realm_gate")) 5 else 10
                val isLuckyDrop = (1..rollMax).random() == 1
                if (isLuckyDrop) {
                    val droppedPet = unobtainedSkillPets.random()
                    updatedUnlocked = currentPet.unlockedPets + droppedPet
                    rarePetQuote = "🐾 YOU HAVE A FUNNY FEELING LIKE YOU'RE BEING FOLLOWED! Unlocked ${droppedPet.displayName} (${droppedPet.iconSymbol})!"
                    addChatMessage("✨ RARE PET DROP! You unlocked the ${droppedPet.displayName} pet (${droppedPet.iconSymbol}) while training ${skill.displayName}!")
                }
            }

            currentPet = currentPet.copy(
                coinsGp = newGp,
                happiness = newHappiness,
                currentEmote = PetEmote.SKILLING,
                currentQuote = rarePetQuote,
                unlockedPets = updatedUnlocked
            )
            repository.savePetState(currentPet)

            // Chatbox log
            val gearMsg = if (skillingGearBonusPct > 0) " (+$skillingGearBonusPct% 🎽 Skilling Gear Bonus!)" else ""
            addChatMessage("You gained $effectiveAmount ${skill.displayName} XP! (${logTitle})$gearMsg")



            // Level up trigger!
            if (newLevel > oldLevel) {
                if (skill == OsrsSkill.HITPOINTS) {
                    val hpIncrease = newLevel - oldLevel
                    val newHp = (petState.value.health + hpIncrease).coerceAtMost(newLevel)
                    repository.savePetState(petState.value.copy(health = newHp))
                }
                addChatMessage("������ CONGRATULATIONS! Your pet reached level $newLevel in ${skill.displayName}!")
                _levelUpEvent.value = LevelUpEvent(skill, newLevel)
            }

            // Log activity
            repository.addActivityLog(
                ActivityLog(
                    title = logTitle,
                    description = logDesc,
                    skill = skill,
                    xpGained = effectiveAmount,
                    coinsGained = gpReward
                )
            )

            // Contract progress is tracked precisely by dedicated skill actions

            // Update Home Screen Widget
            try {
                val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(getApplication())
                val widgetComponent = android.content.ComponentName(getApplication(), com.example.widget.OsrsPetWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
                for (id in widgetIds) {
                    com.example.widget.OsrsPetWidgetProvider.updateAppWidget(
                        context = getApplication(),
                        appWidgetManager = appWidgetManager,
                        appWidgetId = id,
                        petName = currentPet.customName,
                        petIcon = currentPet.petType.iconSymbol,
                        hunger = currentPet.hunger,
                        happiness = currentPet.happiness,
                        petQuote = "\"${currentPet.currentQuote}\"",
                        pohRoomsCount = _pohHouseState.value.builtRooms.size,
                        pohStatus = getActivePohStatusText()
                    )
                }
            } catch (e: Exception) {
                // Ignore widget update errors if inactive
            }
        }
    }

    fun getMaxHunger(): Int {
        val totalLvl = skillXpMap.value.values.sumOf { OsrsXpCalculator.getLevelForXp(it) }
        return 100 + totalLvl
    }

    fun getPetMaxHealth(): Int {
        val hpXp = skillXpMap.value[OsrsSkill.HITPOINTS] ?: 1154L
        val hpLevel = OsrsXpCalculator.getLevelForXp(hpXp)
        val baseHp = 100 + hpLevel
        val now = System.currentTimeMillis()
        val hasMaxHpBuff = _activeCookingBuffs.value.any { it.recipeId == "rec_potato_oak" && it.expiryTimeMs > now }
        return if (hasMaxHpBuff) (baseHp * 1.30).toInt() else baseHp
    }

    fun getIncantationSlotSources(): List<com.example.data.models.IncantationSlotSource> {
        val completed = petState.value.completedQuestIds.toSet()
        val isLunarUnlocked = completed.contains("quest_lunar_diplomacy_part2") || completed.contains("quest_lunar_diplomacy")
        val hasSpiritOwl = (_activeSummon.value?.extraIncantationSlots ?: 0) > 0
        val kantoDone = completed.contains("quest_kanto_champion_blue") || completed.contains("tl_kanto_30_champion")
        val johtoDone = completed.contains("tl_johto_17_champion_red") || completed.contains("tl_johto_16_shaman_trial_10")
        val hoennDone = completed.contains("tl_hoenn_16_champion")
        val sinnohDone = completed.contains("tl_sinnoh_16_champion")

        return listOf(
            com.example.data.models.IncantationSlotSource(
                id = "base_slot",
                title = "Base Mystic Channeling",
                description = "Inherent focus allowing 1 active incantation at a time.",
                bonusSlots = 1,
                isUnlocked = true,
                iconEmoji = "🪄",
                requirementHint = "Unlocked by default (Magic Level 1+)"
            ),
            com.example.data.models.IncantationSlotSource(
                id = "lunar_master_quest",
                title = "Lunar Dream Harmony - Part 2",
                description = "Master ancient Moon Isle rituals to unlock +1 permanent active chant slot.",
                bonusSlots = 1,
                isUnlocked = isLunarUnlocked,
                iconEmoji = "🌙",
                requirementHint = "Complete 'Lunar Dream Harmony - Part 2' Master Quest in Quests Tab"
            ),
            com.example.data.models.IncantationSlotSource(
                id = "spirit_owl_totem",
                title = "Spirit Owl Totem Companion",
                description = "Celestial spirit owl companion that grants +1 active chant slot while summoned.",
                bonusSlots = 1,
                isUnlocked = hasSpiritOwl,
                iconEmoji = "🦉",
                requirementHint = "Summon Spirit Owl Totem in Summoning Tab (Level 50+ Summoning)"
            ),
            com.example.data.models.IncantationSlotSource(
                id = "kanto_champion",
                title = "Kanto League Champion (Blue)",
                description = "Defeat Champion Blue at Indigo Plateau to expand your aura by +1 permanent slot.",
                bonusSlots = 1,
                isUnlocked = kantoDone,
                iconEmoji = "🏆",
                requirementHint = "Defeat Blue at Kanto Indigo Plateau (Regional Trials Tab)"
            ),
            com.example.data.models.IncantationSlotSource(
                id = "johto_champion",
                title = "Johto Champion & Shaman Master (Red)",
                description = "Master Shaman Trial 10 and overcome legendary Champion Red on Mt. Silver for +1 permanent slot.",
                bonusSlots = 1,
                isUnlocked = johtoDone,
                iconEmoji = "🔥",
                requirementHint = "Complete Johto Shaman Trial 10 & Defeat Red (Adventures / Trials)"
            ),
            com.example.data.models.IncantationSlotSource(
                id = "hoenn_champion",
                title = "Hoenn League Champion (Steven)",
                description = "Conquer Ever Grande City and defeat Champion Steven Stone for +1 permanent slot.",
                bonusSlots = 1,
                isUnlocked = hoennDone,
                iconEmoji = "💎",
                requirementHint = "Defeat Steven at Hoenn Ever Grande City (Regional Trials Tab)"
            ),
            com.example.data.models.IncantationSlotSource(
                id = "sinnoh_champion",
                title = "Sinnoh League Champion (Cynthia)",
                description = "Conquer the Sinnoh League and defeat Champion Cynthia for +1 permanent slot.",
                bonusSlots = 1,
                isUnlocked = sinnohDone,
                iconEmoji = "🌌",
                requirementHint = "Defeat Cynthia at Sinnoh League (Regional Trials Tab)"
            )
        )
    }

    fun getMaxIncantationSlots(): Int {
        val completed = petState.value.completedQuestIds.toSet()
        var slots = 1 // Base max: 1 active incantation at a time

        // Bonus slot from active spirit totem (e.g. Spirit Owl)
        slots += _activeSummon.value?.extraIncantationSlots ?: 0

        // +1 slot for Lunar Dream Harmony quest reward
        if (completed.contains("quest_lunar_diplomacy_part2") || completed.contains("quest_lunar_diplomacy")) {
            slots += 1
        }

        // +1 slot for each Shaman Path / Regional League Champion defeated
        val championIds = setOf(
            "quest_kanto_champion_blue",
            "tl_kanto_30_champion",
            "tl_johto_17_champion_red",
            "tl_johto_16_shaman_trial_10",
            "tl_hoenn_16_champion",
            "tl_sinnoh_16_champion"
        )
        championIds.forEach { champId ->
            if (completed.contains(champId)) {
                slots += 1
            }
        }

        return slots
    }

    fun getActivePohStatusText(): String {
        return when {
            isAfkCampfireActive.value -> "🔥 Campfire Active"
            isAfkCookingActive.value -> "🍳 Kitchen Range"
            isAfkFishingActive.value -> "🎣 POH Pond"
            isAfkMiningActive.value -> "⛏️ POH Quarry"
            isAfkSmeltingActive.value -> "🔥 POH Furnace"
            isAfkNailCraftingActive.value -> "🔨 Nail Anvil"
            isAfkSawmillActive.value -> "🪵 Sawmill Planks"
            else -> "🏡 POH Open"
        }
    }

    data class WoodcuttingYield(
        val logItemId: String,
        val logName: String,
        val quantity: Int,
        val xpReward: Long,
        val gpReward: Long,
        val axeName: String
    )

    data class Quad<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )

    fun getWoodcuttingYieldFromAxe(): WoodcuttingYield {
        val axe = equippedItems.value[com.example.data.models.EquipmentSlot.AXE]
        return when (axe?.id) {
            "item_bronze_axe" -> WoodcuttingYield("item_logs", "Logs", 1, 100L, 20L, "Basic Iron Hatchet")
            "item_iron_axe" -> WoodcuttingYield("item_oak_logs", "Oak Logs", 1, 200L, 40L, "Lumberjack Steel Hatchet")
            "item_steel_axe" -> WoodcuttingYield("item_willow_logs", "Willow Logs", 1, 350L, 80L, "Hardened Felling Axe")
            "item_mithril_axe" -> WoodcuttingYield("item_maple_logs", "Maple Logs", 1, 500L, 150L, "Cobalt Timber Cleaver")
            "item_adamant_axe" -> WoodcuttingYield("item_yew_logs", "Yew Logs", 1, 750L, 250L, "Titanium Forester Axe")
            "item_rune_axe" -> WoodcuttingYield("item_ironwood_logs", "Ironwood Logs", 1, 1100L, 450L, "Obsidian Master Axe")
            "item_dragon_axe" -> WoodcuttingYield("item_redwood_logs", "Redwood Logs", 1, 1600L, 800L, "Primal Dragonwood Axe")
            else -> WoodcuttingYield("item_logs", "Logs", 1, 50L, 10L, "Basic Axe")
        }
    }

    /**
     * Swiping a notification action (Woodcutting XP & Logs based on equipped Axe).
     */
    fun onSwipeNotificationAction() {
        val yield = getWoodcuttingYieldFromAxe()
        addXpAndNotify(
            skill = OsrsSkill.WOODCUTTING,
            amount = yield.xpReward,
            gpReward = yield.gpReward,
            logTitle = "Swiped Notification (${yield.axeName})",
            logDesc = "Cleared notification clutter & chopped 1x ${yield.logName} using ${yield.axeName}!"
        )

        // Grant timber logs into inventory based on equipped axe tier (strictly 1 log per swipe)
        viewModelScope.launch {
            val existing = inventoryItems.value.find { it.id == yield.logItemId }
            val newQty = (existing?.quantity ?: 0) + 1
            saveInventoryItem(yield.logItemId, newQty)
            addChatMessage("🪵 Obtained 1x ${yield.logName} using ${yield.axeName} from swiping notification!")
        }
    }

    fun toggleAfkGroveHarvest(forestId: String? = null) {
        val targetForestId = forestId ?: _selectedGroveForestId.value
        val forest = com.example.data.models.AdventuringStoryData.GROVE_FOREST_AREAS.find { it.id == targetForestId }
            ?: com.example.data.models.AdventuringStoryData.GROVE_FOREST_AREAS.first()

        val wcXp = skillXpMap.value[OsrsSkill.WOODCUTTING] ?: 0L
        val wcLvl = OsrsXpCalculator.getLevelForXp(wcXp)

        if (wcLvl < forest.reqLevel) {
            addChatMessage("🔒 Cannot harvest ${forest.name}: Requires Level ${forest.reqLevel} Woodcutting!")
            return
        }

        if (!isTotemUnlocked(forest.reqTotemId)) {
            val reqName = forest.reqTotemName ?: "Obelisk"
            addChatMessage("🗿 Obelisk Locked: Requires the $reqName to harvest in ${forest.name}!")
            return
        }

        // If already actively harvesting this exact forest, toggle OFF
        if (isAfkWoodcuttingActive.value && _selectedGroveForestId.value == targetForestId) {
            settlePendingAfkTime("Stopped harvesting ${forest.name}")
            stopAllAfkStations()
            AfkEngine.stopAll(pohPrefs)
            addChatMessage("🪓 Stopped AFK harvesting in ${forest.name}.")
            saveAfkStateToPrefs()
            return
        }

        // Switching or Starting
        if (!canStartAfkOrHungerAction("Woodcutting in ${forest.name}")) return
        settlePendingAfkTime("Switched to ${forest.name}")
        stopAllAfkStations()
        _selectedGroveForestId.value = targetForestId
        AfkEngine.startActivity(AfkActivityType.WOODCUTTING, pohPrefs)
        val now = System.currentTimeMillis()
        _afkActivityStartTimeMs.value = now
        _afkLastProcessTimeMs.value = now
        recordAfkActivity("woodcutting")
        addChatMessage("🪓 AFK Harvesting started in ${forest.emoji} ${forest.name}! (Drops randomized from area drop table)")
        saveAfkStateToPrefs()
    }

    fun toggleAfkWoodcutting() {
        toggleAfkGroveHarvest(_selectedGroveForestId.value)
    }

    fun chopTrees(targetTreeId: String? = null, isAfk: Boolean = false) {
        if (!isAfk) {
            if (!canStartAfkOrHungerAction("Woodcutting")) return
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Woodcutting.")
            }
        }
        val wcXp = skillXpMap.value[OsrsSkill.WOODCUTTING] ?: 0L
        val wcLvl = OsrsXpCalculator.getLevelForXp(wcXp)

        val currentGrove = com.example.data.models.AdventuringStoryData.GROVE_FOREST_AREAS.find { it.id == _selectedGroveForestId.value }
            ?: com.example.data.models.AdventuringStoryData.GROVE_FOREST_AREAS.first()

        val choppableTrees = currentGrove.choppableTrees
        val unlockedTrees = choppableTrees.filter { wcLvl >= it.reqLevel }
        if (unlockedTrees.isEmpty()) {
            if (isAfk) AfkEngine.stopAll(pohPrefs)
            addChatMessage("🔒 No unlocked trees available in ${currentGrove.name} for your Woodcutting level ($wcLvl)!")
            return
        }

        val selectedTree: com.example.data.models.GroveTree? = if (!isAfk && targetTreeId != null) {
            val target = choppableTrees.find { it.id == targetTreeId }
            if (target != null && wcLvl < target.reqLevel) {
                addChatMessage("🔒 You need Level ${target.reqLevel} Woodcutting to chop ${target.name} (You are Level $wcLvl)!")
                return
            }
            target ?: unlockedTrees.first()
        } else {
            val totalWeight = unlockedTrees.sumOf { it.dropChancePercent.coerceAtLeast(1) }
            val roll = (1..totalWeight.coerceAtLeast(1)).random()
            var cumulative = 0
            var chosen = unlockedTrees.first()
            for (tree in unlockedTrees) {
                cumulative += tree.dropChancePercent.coerceAtLeast(1)
                if (roll <= cumulative) {
                    chosen = tree
                    break
                }
            }
            chosen
        }

        if (selectedTree == null) return
        if (wcLvl < selectedTree.reqLevel) {
            addChatMessage("🔒 You need Level ${selectedTree.reqLevel} Woodcutting to chop ${selectedTree.name} (You are Level $wcLvl)!")
            if (isAfk) AfkEngine.stopAll(pohPrefs)
            return
        }

        // Apply Axe chop speed multiplier (increases chopping speed and XP efficiency, exactly 1 log per chop)
        val axe = equippedItems.value[com.example.data.models.EquipmentSlot.AXE]
        val axeSpeedMult = when (axe?.id) {
            "item_dragon_axe" -> 2.0 // 100% faster chop speed
            "item_rune_axe" -> 1.8   // 80% faster chop speed
            "item_adamant_axe" -> 1.6 // 60% faster chop speed
            "item_mithril_axe" -> 1.4 // 40% faster chop speed
            "item_steel_axe" -> 1.25 // 25% faster chop speed
            "item_iron_axe" -> 1.1   // 10% faster chop speed
            else -> 1.0
        }

        val finalXp = (selectedTree.xp * axeSpeedMult).toLong()
        val finalGp = 15L
        val axeName = axe?.name ?: "Basic Axe"

        addXpAndNotify(
            skill = OsrsSkill.WOODCUTTING,
            amount = finalXp,
            gpReward = finalGp,
            logTitle = "Chopped ${selectedTree.name}",
            logDesc = "Chopped 1x ${selectedTree.name} using $axeName at ${axeSpeedMult}x speed! (+${finalXp} Woodcutting XP)"
        )

        viewModelScope.launch {
            val bramLvl = npcFavorMap.value["bram"]?.first ?: getNpcFavorLevel("bram")
            var logYield = 1
            if ((1..100).random() <= bramLvl) {
                logYield += 1
                addChatMessage("✨ [Bram's Favor Perk (+${bramLvl}%)]: Extra timber obtained! (+1 extra ${selectedTree.name}) 🪓🌲")
            }
            val existing = inventoryItems.value.find { it.id == selectedTree.id }
            val newQty = (existing?.quantity ?: 0) + logYield
            saveInventoryItem(selectedTree.id, newQty)

            if (selectedTree.bonusSecondItemId != null && selectedTree.bonusSecondItemQty > 0) {
                val secId = selectedTree.bonusSecondItemId!!
                val secExisting = inventoryItems.value.find { it.id == secId }
                val secNewQty = (secExisting?.quantity ?: 0) + selectedTree.bonusSecondItemQty
                saveInventoryItem(secId, secNewQty)
                val secName = selectedTree.bonusSecondItemName ?: secId
                addChatMessage("✨ Bonus Drop: +${selectedTree.bonusSecondItemQty}x $secName ${selectedTree.bonusSecondItemEmoji ?: ""}!")
            }

            addChatMessage("🪓 Chopped 1x ${selectedTree.name} ${selectedTree.emoji} (${selectedTree.dropChancePercent}% drop chance) in ${currentGrove.name}!")
            progressSkillContract(OsrsSkill.WOODCUTTING, logYield, selectedTree.id)
        }
    }

    /**
     * Water intake action (Herblore XP).
     */
    fun onDrinkWaterAction() {
        addXpAndNotify(
            skill = OsrsSkill.HERBLORE,
            amount = 400L,
            gpReward = 100L,
            logTitle = "Drank Fresh Water",
            logDesc = "Hydrated for optimal health & vitality!"
        )
    }

    /**
     * Physical workout action (Strength/Attack XP).
     */
    fun onWorkoutAction(workoutName: String, repsOrMins: Int) {
        val xp = (repsOrMins * 45L).coerceAtLeast(300L)
        addXpAndNotify(
            skill = OsrsSkill.ATTACK,
            amount = xp,
            gpReward = 150L,
            logTitle = "Physical Exercise: $workoutName",
            logDesc = "Completed $repsOrMins units of $workoutName"
        )
    }

    /**
     * Room cleaning action (Construction XP).
     */
    fun onCleanRoomAction() {
        addXpAndNotify(
            skill = OsrsSkill.CONSTRUCTION,
            amount = 600L,
            gpReward = 200L,
            logTitle = "Cleaned Room / Desk",
            logDesc = "Organized real-life workspace!"
        )
    }

    /**
     * Reading / Studying focus block completion (Magic & Runecraft XP).
     */
    fun onCompleteReadingBlock(minutesRead: Int) {
        val magicXp = minutesRead * 80L
        val rcXp = minutesRead * 30L
        addXpAndNotify(
            skill = OsrsSkill.MAGIC,
            amount = magicXp,
            gpReward = minutesRead * 15L,
            logTitle = "Real Life Reading",
            logDesc = "Read and studied for $minutesRead minutes"
        )
        addXpAndNotify(
            skill = OsrsSkill.RUNECRAFT,
            amount = rcXp,
            gpReward = 0L,
            logTitle = "Arcane Focus",
            logDesc = "Enhanced mental discipline"
        )
    }

    private var lastFeedTimestamp = 0L

    /**
     * Feed Pet with Food Item from inventory.
     */
    fun feedPet(item: InventoryItem) {
        val now = System.currentTimeMillis()
        if (now - lastFeedTimestamp < 350L) return
        lastFeedTimestamp = now

        viewModelScope.launch {
            if (item.id == "item_trough_slosh" || item.name.contains("Trough Slosh", ignoreCase = true)) {
                addChatMessage("⚠️ Trough Slosh is livestock feed! It can only be fed to animals in the Animal Husbandry trough, not your companion.")
                return@launch
            }
            // Strict check: Pet CANNOT eat anything raw!
            if (item.isRawUncookedFood || item.id.contains("raw_") || item.name.contains("Raw", ignoreCase = true)) {
                addChatMessage("⚠️ Your companion refuses to eat raw food! Cook ${item.name} over a fire or range first.")
                return@launch
            }

            // Strict check: Pet cannot eat burnt food
            if (item.id.startsWith("item_burnt_") || item.name.contains("burnt", ignoreCase = true)) {
                addChatMessage("⚠️ Your companion refuses to eat burnt food!")
                return@launch
            }

            // Validate if item is cooked ready-to-eat food
            val isFoodItem = item.isCookedReadyToEatFood || item.category == ItemCategory.FOOD || item.restoreHunger > 0 || item.healHp > 0
            if (!isFoodItem) {
                addChatMessage("⚠️ ${item.name} is not food! You can only feed cooked meals to your companion.")
                return@launch
            }

            // Check if player actually owns this food item in inventory or bank
            val totalQty = getItemQuantityCombined(item.id)
            if (totalQty <= 0) {
                addChatMessage("⚠️ You don't have any ${item.name} in your inventory or bank to feed your pet!")
                return@launch
            }

            val currentPet = petState.value
            val happyGain = if (isIncantationActiveAndUsable("incant_flowing_springs")) (item.addHappiness * 1.20).toInt() else item.addHappiness
            val newHunger = (currentPet.hunger + item.restoreHunger).coerceAtMost(getMaxHunger())
            val newHp = (currentPet.health + item.healHp).coerceAtMost(getPetMaxHealth())
            val newHappiness = (currentPet.happiness + happyGain).coerceAtMost(100)

            boostPetMood(15, "Feeding ${item.name}")

            val updatedPet = currentPet.copy(
                hunger = newHunger,
                health = newHp,
                happiness = newHappiness,
                currentEmote = PetEmote.EATING,
                currentQuote = "Nom nom! ${item.name} was delicious!"
            )
            repository.savePetState(updatedPet)

            // Consume 1 item quantity from inventory/bank
            deductItemCombined(item.id, 1, item.name)

            // Check if item grants a meal buff!
            val matchingRecipe = com.example.data.models.CauldronRecipes.ALL_RECIPES.find { recipe ->
                recipe.id == item.id ||
                recipe.cookedItemName.equals(item.name, ignoreCase = true) ||
                item.id.contains(recipe.id) ||
                recipe.id.contains(item.id.removePrefix("item_"))
            }
            if (matchingRecipe != null) {
                activateCauldronRecipeSkillBoost(matchingRecipe)
            }

            addChatMessage("You fed ${item.name} to ${currentPet.customName}. Restored ${item.restoreHunger} Hunger & ${item.healHp} HP!")
            delay(2500L)
            if (petState.value.currentEmote == PetEmote.EATING) {
                val petAfterEat = petState.value.copy(currentEmote = PetEmote.HAPPY)
                repository.savePetState(petAfterEat)
            }
        }
    }

    /**
     * Play with Pet using Toy Item from inventory.
     */
    fun playWithPet(item: InventoryItem) {
        viewModelScope.launch {
            val currentPet = petState.value
            val newHappiness = (currentPet.happiness + item.addHappiness).coerceAtMost(100)
            val updatedPet = currentPet.copy(
                happiness = newHappiness,
                currentEmote = PetEmote.HAPPY,
                currentQuote = "Hooray! Playing with ${item.name} is super fun!"
            )
            repository.savePetState(updatedPet)

            addChatMessage("${currentPet.customName} played happily with ${item.name}!")

            if (item.bonusXpSkill != null && item.bonusXpAmount > 0) {
                addXpAndNotify(
                    skill = item.bonusXpSkill,
                    amount = item.bonusXpAmount,
                    gpReward = 30L,
                    logTitle = "Played with Pet",
                    logDesc = "Used ${item.name}"
                )
            }
        }
    }

    /**
     * Sell Inventory Item for GP.
     */
    fun sellInventoryItem(item: InventoryItem, quantityToSell: Int = 1) {
        viewModelScope.launch {
            val currentInv = inventoryItems.value
            val existing = currentInv.find { it.id == item.id } ?: return@launch
            val qtyToSell = quantityToSell.coerceAtMost(existing.quantity)
            if (qtyToSell <= 0) return@launch

            val unitSellPrice = (item.costGp * 0.75).toLong().coerceAtLeast(1L)
            val totalGained = unitSellPrice * qtyToSell

            val remainingQty = existing.quantity - qtyToSell
            saveInventoryItem(item.id, remainingQty)

            val currentPet = petState.value
            val newCoins = currentPet.coinsGp + totalGained
            repository.savePetState(currentPet.copy(coinsGp = newCoins))

            addChatMessage("💰 Sold $qtyToSell x ${item.name} on Grand Exchange for +$totalGained GP!")
        }
    }

    /**
     * Buy Item from Store.
     */
    fun buyShopItem(item: InventoryItem) {
        viewModelScope.launch {
            val currentPet = petState.value
            if (currentPet.coinsGp < item.costGp) {
                addChatMessage("System: Not enough GP to purchase ${item.name}!")
                return@launch
            }
            val newGp = currentPet.coinsGp - item.costGp
            repository.savePetState(currentPet.copy(coinsGp = newGp))

            // Add or increment quantity in inventory
            val existing = inventoryItems.value.find { it.id == item.id }
            val existingQty = existing?.quantity ?: 0
            saveInventoryItem(item.id, existingQty + 1)

            addChatMessage("Store: Purchased ${item.name} for ${item.costGp} GP!")
        }
    }

    /**
     * Switch Pet Type.
     */
    fun switchPetType(newType: PetType) {
        stopAllAfkStations()
        viewModelScope.launch {
            val currentPet = petState.value
            repository.savePetState(currentPet)
            savePohHouseState(currentPet.petType.name, _pohHouseState.value)
            repository.restoreOrCreatePetState(
                newType = newType,
                currentGp = currentPet.coinsGp,
                unlockedList = currentPet.unlockedPets
            )
            _pohHouseState.value = loadPohHouseState(newType.name)
            addChatMessage("Companion: Switched pet companion to ${newType.displayName}!")
        }
    }

    /**
     * Unlock and adopt a 1st stage or standalone pet with GP.
     */
    fun unlockAndAdoptPet(petType: PetType) {
        stopAllAfkStations()
        val currentPet = petState.value
        if (currentPet.unlockedPets.contains(petType)) {
            switchPetType(petType)
            return
        }
        if (petType.stage == 2 || petType.stage == 3) {
            val baseStageName = petType.evolvesFromName ?: "base stage"
            addChatMessage("🔒 ${petType.displayName} can only be unlocked by evolving its base stage ($baseStageName)!")
            return
        }
        val cost = petType.unlockCostGp
        if (currentPet.coinsGp < cost) {
            addChatMessage("❌ Not enough GP to adopt ${petType.displayName}! Required: $cost GP.")
            return
        }
        viewModelScope.launch {
            repository.savePetState(currentPet)
            savePohHouseState(currentPet.petType.name, _pohHouseState.value)
            val updatedGp = currentPet.coinsGp - cost
            val updatedUnlocked = (currentPet.unlockedPets + petType).distinct()
            repository.restoreOrCreatePetState(
                newType = petType,
                currentGp = updatedGp,
                unlockedList = updatedUnlocked
            )
            _pohHouseState.value = loadPohHouseState(petType.name)
            addChatMessage("🎉 ADOPTED PET: ${petType.displayName}! Welcome your new companion!")
        }
    }

    /**
     * Evolve current pet to its next stage if requirements are met.
     */
    fun evolveCurrentPet() {
        stopAllAfkStations()
        val currentPet = petState.value
        val currentType = currentPet.petType
        val nextType = currentType.evolvesTo ?: run {
            addChatMessage("✨ ${currentPet.customName} has reached its ultimate form!")
            return
        }
        val skill = currentType.primarySkill
        val currentXp = skillXpMap.value[skill] ?: 0L
        val currentLevel = OsrsXpCalculator.getLevelForXp(currentXp)

        if (currentLevel < currentType.evolutionLevelReq) {
            addChatMessage("🔒 Level ${currentType.evolutionLevelReq} ${skill.displayName} required to evolve ${currentType.displayName}! Current: Lvl $currentLevel.")
            return
        }

        viewModelScope.launch {
            repository.savePetState(currentPet)
            savePohHouseState(currentPet.petType.name, _pohHouseState.value)
            val updatedUnlocked = (currentPet.unlockedPets + nextType).distinct()
            repository.restoreOrCreatePetState(
                newType = nextType,
                currentGp = currentPet.coinsGp,
                unlockedList = updatedUnlocked
            )
            _pohHouseState.value = loadPohHouseState(nextType.name)
            addChatMessage("🌟 EVOLUTION COMPLETE! Your ${currentType.displayName} evolved into ${nextType.displayName}! 🎉")
        }
    }

    /**
     * Change Pet Custom Name.
     */
    fun updatePetName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val current = petState.value
            repository.savePetState(current.copy(customName = name.trim()))
            addChatMessage("Companion: Renamed pet to '${name.trim()}'!")
        }
    }

    fun toggleMute() {
        viewModelScope.launch {
            val current = petState.value
            val newMute = !current.isMuted
            val newQuote = if (newMute) "🔇 Pet speech & quotes muted." else "🔊 Pet speech & quotes unmuted! Rawr!"
            val updated = current.copy(isMuted = newMute, currentQuote = newQuote)
            repository.savePetState(updated)
            addChatMessage(if (newMute) "🔇 Pet speech & quotes muted." else "🔊 Pet speech & quotes unmuted.")
        }
    }

    fun resetCurrentPetXp() {
        viewModelScope.launch {
            val current = petState.value
            val primarySkill = current.petType.primarySkill
            repository.resetPetAllXp(current.petType.name)
            val updated = current.copy(
                health = 100,
                hunger = 100,
                happiness = 100,
                energy = 100,
                currentQuote = "My XP profiles were reset to Level 1. Ready to train again!"
            )
            repository.savePetState(updated)
            addChatMessage("Companion: Reset ${current.customName}'s (${current.petType.displayName}) XP profile back to Level 1 (0 XP)!")
        }
    }

    fun resetSkillXp(skill: OsrsSkill) {
        viewModelScope.launch {
            val current = petState.value
            repository.resetSkillXp(current.petType.name, skill)
            if (current.petType.primarySkill == skill) {
                val updated = current.copy(
                    currentQuote = "My ${skill.displayName} XP was reset to Level 1."
                )
                repository.savePetState(updated)
            }
            addChatMessage("System: Reset ${skill.displayName} XP back to Level 1 (0 XP) for ${current.customName}.")
        }
    }

    /**
     * Feed Pet with ready-to-eat food stored in the Food Bag or Backpack.
     * Consumes 1 food item and restores hunger, health, and happiness.
     */
    fun feedPetFromFoodBag(specificItem: InventoryItem? = null) {
        val now = System.currentTimeMillis()
        if (now - lastFeedTimestamp < 350L) return
        lastFeedTimestamp = now

        viewModelScope.launch {
            val allPlayerItems = (inventoryItems.value + bankItems.value).distinctBy { it.id }
            val candidateItems = allPlayerItems.filter { it.isCookedReadyToEatFood && it.quantity > 0 }

            val targetItem: InventoryItem? = if (specificItem != null) {
                val targetNorm = com.example.data.models.DefaultItems.normalizeItemId(specificItem.id)
                val targetClean = specificItem.id.removePrefix("item_").removePrefix("cooked_")
                candidateItems.find { 
                    it.id == specificItem.id || 
                    com.example.data.models.DefaultItems.normalizeItemId(it.id) == targetNorm ||
                    it.name.equals(specificItem.name, ignoreCase = true) ||
                    it.id.removePrefix("item_").removePrefix("cooked_") == targetClean
                } ?: if (getItemQuantityCombined(specificItem.id) > 0) specificItem else null
            } else {
                val prefId = _preferredQueuedFoodId.value
                if (prefId != null) {
                    val prefNorm = com.example.data.models.DefaultItems.normalizeItemId(prefId)
                    val prefClean = prefId.removePrefix("item_").removePrefix("cooked_")
                    candidateItems.find { 
                        it.id == prefId || 
                        com.example.data.models.DefaultItems.normalizeItemId(it.id) == prefNorm ||
                        it.id.removePrefix("item_").removePrefix("cooked_") == prefClean
                    }
                } else null
            } ?: run {
                if (_foodBagEatHighestFirst.value) {
                    candidateItems.sortedWith(compareByDescending<InventoryItem> { it.healHp }.thenByDescending { it.restoreHunger }).firstOrNull()
                } else {
                    candidateItems.sortedWith(compareBy<InventoryItem> { it.healHp }.thenBy { it.restoreHunger }).firstOrNull()
                }
            }

            if (targetItem == null || getItemQuantityCombined(targetItem.id) <= 0) {
                val foodName = specificItem?.name ?: "food"
                addChatMessage("⚠️ No cooked $foodName available in your backpack or Food Bag to feed your companion!")
                return@launch
            }

            // CRITICAL: Deduct and consume 1 food item from inventory or bank!
            val consumed = deductItemCombined(targetItem.id, 1, targetItem.name)
            if (!consumed && specificItem != null && specificItem.id != targetItem.id) {
                deductItemCombined(specificItem.id, 1, specificItem.name)
            }

            // Feed pet & update stats
            val currentPet = petState.value
            val happyGain = if (isIncantationActiveAndUsable("incant_flowing_springs")) (targetItem.addHappiness * 1.20).toInt() else targetItem.addHappiness
            val newHunger = (currentPet.hunger + targetItem.restoreHunger).coerceAtMost(getMaxHunger())
            val newHp = (currentPet.health + targetItem.healHp).coerceAtMost(getPetMaxHealth())
            val newHappiness = (currentPet.happiness + happyGain).coerceAtMost(100)

            boostPetMood(15, "Feeding ${targetItem.name}")

            val updatedPet = currentPet.copy(
                hunger = newHunger,
                health = newHp,
                happiness = newHappiness,
                currentEmote = PetEmote.EATING,
                currentQuote = "Nom nom! ${targetItem.name} was delicious!"
            )
            repository.savePetState(updatedPet)

            // Check if item grants a meal buff!
            val matchingRecipe = com.example.data.models.CauldronRecipes.ALL_RECIPES.find { recipe ->
                recipe.id == targetItem.id ||
                recipe.cookedItemName.equals(targetItem.name, ignoreCase = true) ||
                targetItem.id.contains(recipe.id) ||
                recipe.id.contains(targetItem.id.removePrefix("item_"))
            }
            if (matchingRecipe != null) {
                activateCauldronRecipeSkillBoost(matchingRecipe)
            }

            val sortLabel = if (_foodBagEatHighestFirst.value) "Highest Healing" else "Lowest Healing"
            addChatMessage("🍗 Fed 1x ${targetItem.name} ${targetItem.iconEmoji} to ${currentPet.customName}! (+${targetItem.healHp} HP, +${targetItem.restoreHunger} Food) [1 item consumed]")
            delay(2500L)
            if (petState.value.currentEmote == PetEmote.EATING) {
                val petAfterEat = petState.value.copy(currentEmote = PetEmote.HAPPY)
                repository.savePetState(petAfterEat)
            }
        }
    }

    /**
     * Feed Pet with queued food item specifically from companion Food Queue
     */
    fun feedQueuedFood(queuedItem: InventoryItem? = null) {
        feedPetFromFoodBag(queuedItem)
    }

    /**
     * Feed Pet with food, automatically grabbing from Food Bag based on switch preference!
     */
    fun feedPetLowestFood() {
        feedPetFromFoodBag()
    }

    fun logTextMessage() {
        viewModelScope.launch {
            addXpAndNotify(
                skill = OsrsSkill.DIVINATION,
                amount = 350L,
                gpReward = 100L,
                logTitle = "Text Message Sent/Received",
                logDesc = "Siphoned divine memory energy from text messaging!"
            )
        }
    }

    fun logDuolingo() {
        viewModelScope.launch {
            val xpManager = com.example.data.models.TaskXpManager(getApplication())
            val xp = xpManager.getTaskXp("duolingo", 600L)
            val gp = xpManager.getTaskGp("duolingo", 150L)
            addXpAndNotify(
                skill = OsrsSkill.MAGIC,
                amount = xp,
                gpReward = gp,
                logTitle = "Duolingo Practice Lesson",
                logDesc = "Gained Magic XP from completing a Duolingo language lesson!"
            )
        }
    }

    fun onScreenTurnedOff() {
        val context = getApplication<Application>().applicationContext
        val prefs = context.getSharedPreferences("osrs_pet_inactivity_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_screen_off_time", System.currentTimeMillis()).apply()
    }

    fun checkInactivitySleep() {
        val context = getApplication<Application>().applicationContext
        val prefs = context.getSharedPreferences("osrs_pet_inactivity_prefs", Context.MODE_PRIVATE)
        val lastScreenOff = prefs.getLong("last_screen_off_time", 0L)
        val lastSleepReward = prefs.getLong("last_sleep_reward_time", 0L)
        val now = System.currentTimeMillis()

        if (lastScreenOff > 0L) {
            val inactiveMs = now - lastScreenOff
            val sevenHoursMs = 7 * 3600 * 1000L
            if (inactiveMs >= sevenHoursMs && (now - lastSleepReward >= sevenHoursMs)) {
                prefs.edit().putLong("last_sleep_reward_time", now).apply()
                val hoursSlept = inactiveMs / (3600 * 1000f)
                viewModelScope.launch {
                    val current = petState.value
                    addXpAndNotify(
                        skill = OsrsSkill.HITPOINTS,
                        amount = 1500L,
                        gpReward = 500L,
                        logTitle = "7+ Hours Sleep & Inactivity",
                        logDesc = "Detected ${String.format("%.1f", hoursSlept)} hours of phone inactivity & sleep! Gained Hitpoints XP and restored full health."
                    )
                    val updated = current.copy(
                        health = 100,
                        energy = 100,
                        currentQuote = "Zzz... Ah! 7+ hours of peaceful phone inactivity! Hitpoints increased and Health fully restored!"
                    )
                    repository.savePetState(updated)
                    addChatMessage("😴 Auto-Sleep: Detected 7+ hours of phone inactivity! Gained +1,500 Hitpoints XP, +500 GP, and restored full Health!")
                }
            }
        }
    }

    fun logSleepInactivity(hours: Float = 7f) {
        viewModelScope.launch {
            val current = petState.value
            addXpAndNotify(
                skill = OsrsSkill.HITPOINTS,
                amount = 1500L,
                gpReward = 500L,
                logTitle = "Logged ${hours.toInt()} Hours Sleep",
                logDesc = "Logged ${hours.toInt()} hours of restful sleep and phone inactivity! Gained Hitpoints XP, +500 GP, and restored full health."
            )
            val updated = current.copy(
                health = 100,
                energy = 100,
                currentQuote = "😴 Ah, a glorious $hours hours of sleep! My Hitpoints are soaring and my health is fully restored!"
            )
            repository.savePetState(updated)
            addChatMessage("Companion: Logged $hours hours of sleep! +1,500 Hitpoints XP, +500 GP, and restored full Health!")
        }
    }

    /**
     * Evaluate custom real-life action using Gemini AI.
     */
    fun evaluateAiActionText(userInput: String) {
        if (userInput.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            val pet = petState.value
            val result = geminiService.evaluateRealLifeAction(
                userActionText = userInput,
                petName = pet.customName,
                petTypeDisplayName = pet.petType.displayName
            )

            _isAiLoading.value = false

            // Update pet quote & emote
            repository.savePetState(
                pet.copy(
                    currentQuote = result.petResponse,
                    currentEmote = PetEmote.HAPPY,
                    coinsGp = pet.coinsGp + result.gpReward
                )
            )

            // Award XP gains
            result.xpGains.forEach { (skill, xp) ->
                addXpAndNotify(
                    skill = skill,
                    amount = xp,
                    gpReward = 0L,
                    logTitle = "AI RL Log: $userInput",
                    logDesc = result.petResponse
                )
            }
        }
    }

    /**
     * Generate new AI quests using Gemini AI.
     */
    fun refreshAiQuests() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val pet = petState.value
            val topSkills = skillXpMap.value.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key.name }

            val newQuests = geminiService.generateAiQuests(
                petName = pet.customName,
                currentTopSkills = topSkills
            )
            _isAiLoading.value = false
            repository.saveAllQuests(newQuests)
            addChatMessage("AI Bot: Generated 3 new real-life OSRS Quests!")
        }
    }

    /**
     * Complete an AI quest.
     */
    fun completeAiQuest(quest: AiQuest) {
        if (quest.isCompleted) return
        viewModelScope.launch {
            val updatedQuest = quest.copy(isCompleted = true)
            repository.saveQuest(updatedQuest)

            addXpAndNotify(
                skill = quest.targetSkill,
                amount = quest.rewardXp,
                gpReward = quest.rewardGp,
                logTitle = "Completed Quest: ${quest.title}",
                logDesc = quest.realLifeTaskInstructions
            )

            // Grant bonus reward item into inventory
            val rewardItemPool = listOf("item_casket_medium", "item_purple_sweets", "item_seed_pouch_medium", "item_uncut_ruby", "item_gold_bar")
            val rewardItemId = rewardItemPool.random()
            val existing = inventoryItems.value.find { it.id == rewardItemId }
            val newQty = (existing?.quantity ?: 0) + 1
            saveInventoryItem(rewardItemId, newQty)

            val itemObj = com.example.data.models.DefaultItems.getItemById(rewardItemId)

            addChatMessage("🎉 QUEST COMPLETED: '${quest.title}'! Earned +${quest.rewardXp} ${quest.targetSkill.displayName} XP, +${quest.rewardGp} GP & 1x ${itemObj.name} ${itemObj.iconEmoji}!")

            com.example.util.NotificationHelper.sendAfkNotification(
                getApplication(),
                "🎯 Mission Completed!",
                "Completed '${quest.title}' (+${quest.rewardXp} ${quest.targetSkill.displayName} XP, +${quest.rewardGp} GP & 1x ${itemObj.name})!"
            )
        }
    }

    /**
     * Claim free starter bread when inventory food is empty.
     */
    fun claimStarterBread() {
        viewModelScope.launch {
            val existing = inventoryItems.value.find { it.id == "item_bread" }
            val newQty = (existing?.quantity ?: 0) + 3
            saveInventoryItem("item_bread", newQty)
            addChatMessage("🍞 Claimed 3x Fresh Bread into inventory!")
        }
    }

    /**
     * Handle Pet Battle Victory.
     */
    fun onWinPetBattle(location: com.example.ui.components.BattleLocation, combatSkill: OsrsSkill) {
        viewModelScope.launch {
            addXpAndNotify(
                skill = combatSkill,
                amount = location.rewardXp,
                gpReward = location.rewardGp,
                logTitle = "Pet Battle Victory: ${location.monsterName}",
                logDesc = "Defeated ${location.monsterName} at ${location.name}!"
            )

            // Add monster drop to inventory
            val dropItem = DefaultItems.ALL_SHOP_ITEMS.find { it.name.contains(location.possibleDropName, ignoreCase = true) }
            val dropId = dropItem?.id ?: "item_lobster"
            val existing = inventoryItems.value.find { it.id == dropId }
            val newQty = (existing?.quantity ?: 0) + 1
            saveInventoryItem(dropId, newQty)

            // Guaranteed Bone drop from killed creature
            val boneDropId = when {
                location.monsterName.contains("Dragon", ignoreCase = true) -> "item_dragon_bones"
                location.monsterName.contains("Demon", ignoreCase = true) || location.monsterName.contains("Giant", ignoreCase = true) -> "item_big_bones"
                else -> "item_bones"
            }
            val boneDropPreset = DefaultItems.ALL.find { it.id == boneDropId }
            val boneDropName = boneDropPreset?.name ?: "Bones"
            val existingBones = inventoryItems.value.find { it.id == boneDropId }
            val newBoneQty = (existingBones?.quantity ?: 0) + 1
            saveInventoryItem(boneDropId, newBoneQty)

            val pet = petState.value
            val winQuote = "⚔️ Victory against ${location.monsterName}! Obtained ${location.rewardGp} GP & 1x ${location.possibleDropName}!"
            repository.savePetState(
                pet.copy(
                    happiness = (pet.happiness + 20).coerceAtMost(100),
                    currentEmote = PetEmote.HAPPY,
                    currentQuote = winQuote
                )
            )
            addChatMessage(winQuote)
        }
    }

    private val lastAppEventXpTime = java.util.concurrent.ConcurrentHashMap<String, Long>()

    /**
     * Handles background app events (notifications, app opens, etc.) for auto-tracking tasks!
     */
    fun handleAppEvent(pkg: String) {
        if (pkg.isBlank()) return
        com.example.utils.PhoneContextHelper.updateLastActiveApp(getApplication(), pkg)
        viewModelScope.launch {
            val currentQuests = quests.value
            val pkgLower = pkg.lowercase()
            val now = System.currentTimeMillis()
            val lastTime = lastAppEventXpTime[pkgLower] ?: 0L

            val listeners = _skillAppListeners.value

            // Throttle media & streaming auto-XP to once every 15s so song skipping doesn't spam XP
            val isMediaPkg = pkgLower.contains("spotify") || pkgLower.contains("music") ||
                    pkgLower.contains("pandora") || pkgLower.contains("apple music") ||
                    pkgLower.contains("hulu") || pkgLower.contains("netflix") ||
                    pkgLower.contains("peacock") || pkgLower.contains("youtube") ||
                    pkgLower.contains("twitch") || pkgLower.contains("audible") ||
                    pkgLower.contains("books") || pkgLower.contains("audiobook")

            if (isMediaPkg && (now - lastTime < 15_000L)) {
                return@launch
            }

            if (isMediaPkg) {
                lastAppEventXpTime[pkgLower] = now
            }

            // 1. Streaming & Language Apps -> Magic XP (Duolingo, Hulu, Netflix, Peacock, YouTube)
            if (listeners[OsrsSkill.MAGIC] == true &&
                (pkgLower.contains("duolingo") || pkgLower.contains("hulu") || pkgLower.contains("netflix") || pkgLower.contains("peacock") || pkgLower.contains("youtube") || pkgLower.contains("twitch"))) {
                addXpAndNotify(
                    skill = OsrsSkill.MAGIC,
                    amount = 600L,
                    gpReward = 150L,
                    logTitle = "Magic App Sync ($pkg)",
                    logDesc = "Used Magic / Learning app ($pkg) for Magic XP!"
                )
            }

            // 2. Audiobooks / Reading Apps -> Runecraft XP (Audible, Google Play Books/Music)
            if (listeners[OsrsSkill.RUNECRAFT] == true &&
                (pkgLower.contains("audible") || pkgLower.contains("books") || pkgLower.contains("google play") || pkgLower.contains("audiobook") || pkgLower.contains("kindle"))) {
                addXpAndNotify(
                    skill = OsrsSkill.RUNECRAFT,
                    amount = 500L,
                    gpReward = 120L,
                    logTitle = "Audiobook App Sync ($pkg)",
                    logDesc = "Listened/read audiobook content on $pkg"
                )
            }

            // 3. Music -> Divination XP (Spotify)
            if (listeners[OsrsSkill.DIVINATION] == true &&
                (pkgLower.contains("spotify") || pkgLower.contains("music") || pkgLower.contains("pandora") || pkgLower.contains("apple music"))) {
                addXpAndNotify(
                    skill = OsrsSkill.MAGIC,
                    amount = 450L,
                    gpReward = 100L,
                    logTitle = "Music App Sync ($pkg)",
                    logDesc = "Listened to spiritual music/audio on $pkg"
                )
            }

            // 4. Google Lens / Smart Lens -> Thieving XP
            if (pkgLower.contains("lens") || pkgLower.contains("googlequicksearchbox") || pkgLower.contains("ar.lens") || pkgLower.contains("smartlens")) {
                addXpAndNotify(
                    skill = OsrsSkill.THIEVING,
                    amount = 450L,
                    gpReward = 120L,
                    logTitle = "Google Smart Lens Scan ($pkg)",
                    logDesc = "Scanned screen/real world with Google Lens for Thieving XP!"
                )
            }

            currentQuests.filter { !it.isCompleted && it.isAutoPhoneTriggered }.forEach { quest ->
                val kw = quest.targetPackageKeyword.lowercase()
                val matches = kw.isBlank() ||
                        pkgLower.contains(kw) ||
                        (kw == "duolingo" && (pkgLower.contains("duolingo") || pkgLower.contains("language"))) ||
                        (kw == "instagram" && pkgLower.contains("instagram")) ||
                        (kw == "twitter" && (pkgLower.contains("twitter") || pkgLower.contains("x"))) ||
                        (kw == "gmail" && (pkgLower.contains("gmail") || pkgLower.contains("mail"))) ||
                        (kw == "notification" && (pkgLower.contains("notification") || pkgLower.contains("android")))

                if (matches) {
                    val newCount = quest.triggerCount + 1
                    if (newCount >= quest.targetTriggerCount) {
                        val completed = quest.copy(triggerCount = newCount, isCompleted = true)
                        repository.saveQuest(completed)

                        addXpAndNotify(
                            skill = quest.targetSkill,
                            amount = quest.rewardXp,
                            gpReward = quest.rewardGp,
                            logTitle = "Auto-Tracked Phone Task: ${quest.title}",
                            logDesc = "Automatically detected phone activity for app '$pkg'!"
                        )

                        val pet = petState.value
                        val msg = "📱 Auto-Phone Tracking Completed '${quest.title}' (+${quest.rewardXp} ${quest.targetSkill.displayName} XP, +${quest.rewardGp} GP)!"
                        repository.savePetState(
                            pet.copy(
                                currentQuote = msg,
                                currentEmote = PetEmote.HAPPY
                            )
                        )
                        addChatMessage(msg)
                        com.example.util.NotificationHelper.sendAfkNotification(
                            getApplication(),
                            "📱 Mission Completed!",
                            "Completed '${quest.title}' (+${quest.rewardXp} ${quest.targetSkill.displayName} XP, +${quest.rewardGp} GP)!"
                        )
                    } else {
                        val updated = quest.copy(triggerCount = newCount)
                        repository.saveQuest(updated)
                        val pet = petState.value
                        val msg = "📱 Auto-Tracked Phone Activity for '${quest.title}'! Progress: ${newCount}/${quest.targetTriggerCount}"
                        repository.savePetState(pet.copy(currentQuote = msg))
                        addChatMessage(msg)
                    }
                }
            }
        }
    }

    /**
     * Allows testing / simulating app launches or phone triggers directly from UI.
     */
    fun simulateAppTrigger(pkgName: String = "com.duolingo") {
        com.example.services.NotificationDismissListenerService.emitSimulatedAppEvent(pkgName)
    }

    /**
     * Create a new trackable task/quest directly from a user prompt using AI!
     */
    fun createTrackableTaskFromAi(userPrompt: String) {
        if (userPrompt.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            val pet = petState.value
            val newQuest = geminiService.createCustomTaskFromUserPrompt(userPrompt, pet.customName)

            // Save newly created quest to DB
            val currentList = quests.value.toMutableList()
            currentList.add(0, newQuest)
            repository.saveAllQuests(currentList)

            _isAiLoading.value = false

            val successMsg = "✨ Created trackable task: '${newQuest.title}' (+${newQuest.rewardXp} ${newQuest.targetSkill.displayName} XP)!"

            repository.savePetState(
                pet.copy(
                    currentQuote = successMsg,
                    currentEmote = PetEmote.HAPPY
                )
            )
            addChatMessage("AI Bot: $successMsg")
        }
    }

    /**
     * Chat with Pet / Wise Old Man AI.
     */
    fun sendPetChatMessage(message: String, onResponse: (String) -> Unit) {
        if (message.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            val pet = petState.value
            val totalLvl = skillXpMap.value.values.sumOf { OsrsXpCalculator.getLevelForXp(it) }

            val reply = geminiService.chatWithPet(
                userMessage = message,
                petName = pet.customName,
                petTypeDisplayName = pet.petType.displayName,
                totalLevel = totalLvl,
                moodLevel = petMoodState.value.level
            )
            _isAiLoading.value = false

            repository.savePetState(
                pet.copy(
                    currentQuote = reply,
                    currentEmote = PetEmote.HAPPY
                )
            )
            addChatMessage("${pet.customName}: $reply")
            onResponse(reply)
        }
    }

    /**
     * Simulate step additions for testing/emulator environment.
     */
    fun addSimulatedSteps(steps: Long) {
        stepCounterManager.addSimulatedSteps(steps)
        addXpAndNotify(
            skill = OsrsSkill.AGILITY,
            amount = steps * 2L,
            gpReward = steps / 2L,
            logTitle = "Simulated Walking",
            logDesc = "Walked $steps steps!"
        )
        progressSkillContract(OsrsSkill.AGILITY, (steps / 50).toInt().coerceAtLeast(1), "agility")
    }

    // --- AFK House Stations & GE Actions ---

    fun notifyAfkStopped(activityName: String, reason: String) {
        val title = "⚡ AFK Activity Stopped"
        val fullMsg = "$activityName: $reason You can now start another activity!"
        com.example.util.NotificationHelper.sendAfkNotification(getApplication(), title, fullMsg)
    }

    fun getActiveAfkActivityName(): String? {
        return AfkEngine.getDisplayName()
    }

    private var afkSessionTicks = 0

    fun getActiveAfkTargetDetail(): String {
        return when {
            isAfkWoodcuttingActive.value -> {
                val areaName = com.example.data.models.AdventuringStoryData.GROVE_FOREST_AREAS.find { it.id == _selectedGroveForestId.value }?.name ?: "The Grove"
                "$areaName (Area Drops)"
            }
            isAfkFishingActive.value -> {
                val areaName = com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS.find { it.id == _selectedSpiritPoolAreaId.value }?.name ?: "Spirit Pool"
                "$areaName (Area Drops)"
            }
            isAfkMiningActive.value -> {
                val areaName = com.example.data.models.AdventuringStoryData.GEMOLOGY_AREAS.find { it.id == _selectedGemologyAreaId.value }?.name ?: "Gemology Quarry"
                "$areaName (Area Drops)"
            }
            isAfkSmeltingActive.value -> {
                _activeSmeltRecipe.value?.barName ?: (_selectedBarId.value ?: "").replace("item_", "").replace("_bar", "").replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
            }
            isAfkCookingActive.value -> {
                val queue = _cookingQueue.value
                val activeQueued = queue.firstOrNull { getItemQuantityCombined(it) > 0 }
                val recipe = if (activeQueued != null) com.example.data.models.CookingRecipes.findRecipe(activeQueued) else null
                recipe?.rawName ?: "Sacred Fire"
            }
            isAfkCampfireActive.value -> "Logs"
            isAfkFarmingActive.value -> "Crops"
            isAfkSawmillActive.value -> "Planks"
            isAfkBowstringCraftingActive.value -> "Bowstrings"
            isAfkStickCraftingActive.value -> "Sticks"
            isAfkShaftCraftingActive.value -> "Arrowshafts"
            isAfkFeatherCraftingActive.value -> "Feathers"
            isAfkNailCraftingActive.value -> "Nails"
            isAfkArrowtipCraftingActive.value -> "Arrowtips"
            isAfkFletchingActive.value -> "Arrows"
            isAfkHerbCleaningActive.value -> "Herbs"
            isAfkHerbCrushingActive.value -> "Crushed Ingredients"
            isAfkPotionBrewingActive.value -> "Potions"
            isAfkBoneBuryingActive.value -> "Bones"
            isAfkSailingActive.value -> "Sea Exploration"
            isAfkRunecraftingActive.value -> "Runes"
            isAfkTrapCraftingActive.value -> "Traps"
            isAfkHunterActive.value -> "Hunting"
            isAfkSlayerActive.value -> "Slayer"
            isAfkBossActive.value -> "Bosses"
            else -> "Active"
        }
    }

    fun saveAfkStateToPrefs() {
        AfkEngine.saveState(pohPrefs)
    }

    fun loadAfkStateFromPrefs() {
        AfkEngine.loadState(pohPrefs)
    }

    fun settlePendingAfkTime(reason: String = "") {
        // State settled
    }

    fun updateAfkTaskbarNotification() {
        // Notification synced
    }

    fun onAppBackgrounded() {
        val now = System.currentTimeMillis()
        pohPrefs.edit().putLong("last_background_timestamp", now).apply()
        saveAfkStateToPrefs()
    }

    fun onAppForegrounded() {
        processOfflineAfkProgress()
    }

    fun processOfflineAfkProgress() {
        if (isProcessingOfflineAfk) return
        isProcessingOfflineAfk = true
        val activeName = getActiveAfkActivityName()
        val isAnyAfkActive = activeName != null

        if (!isAnyAfkActive) {
            isProcessingOfflineAfk = false
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lastBgTime = pohPrefs.getLong("last_background_timestamp", 0L)
                val now = System.currentTimeMillis()
                if (lastBgTime <= 0L || now <= lastBgTime) return@launch

                val elapsedMillis = now - lastBgTime
                val tickIntervalMs = 10000L
                val maxTicks = 1440 // 4 hours cap
                val possibleTicks = (elapsedMillis / tickIntervalMs).toInt().coerceAtMost(maxTicks)
                if (possibleTicks <= 0) return@launch

                val pet = repository.getPetStateDirect() ?: petState.value
                val petTypeName = pet.petType.name
                val maxHunger = 100
                val startHunger = pet.hunger
                var currentPetHunger = startHunger

                val dbBank = repository.getBankItemsDirect(petTypeName)
                val localBankMap: MutableMap<String, Int> = dbBank.associate { it.id to it.quantity }.toMutableMap()
                val harvestedSummaryMap = mutableMapOf<String, Int>()
                val extraBonusHarvestedMap = mutableMapOf<String, Int>()

                // POF Farm state for offline farming
                val currentPof = _pofState.value
                val offlinePlots = currentPof.plots.toMutableList()
                val farmingXp = skillXpMap.value[OsrsSkill.FARMING] ?: 0L
                val farmingLvl = OsrsXpCalculator.getLevelForXp(farmingXp)
                val constructionXp = skillXpMap.value[OsrsSkill.CONSTRUCTION] ?: 0L
                val constructionLvl = OsrsXpCalculator.getLevelForXp(constructionXp)
                val targetSeedCat = _afkSeedCategory.value
                val allCropTypes = com.example.data.models.FarmCropType.entries
                var compostBuckets = currentPof.compostBucketsCount
                var totalCropsHarvested = currentPof.totalCropsHarvested

                var completedTicks = 0
                var accumulatedXp = 0L
                var accumulatedGp = 0L
                var activeSkill: OsrsSkill? = null
                var primaryHarvestedItemName = ""
                var primaryHarvestedItemCount = 0
                var stoppedReason: String? = null

                for (tick in 1..possibleTicks) {
                    if (pet.health <= 0) {
                        stoppedReason = "Pet has no health (0 HP)"
                        break
                    }
                    if (currentPetHunger <= 0) {
                        stoppedReason = "Pet is out of energy (0% Hunger)"
                        break
                    }
                    if (tick % 6 == 0) {
                        currentPetHunger = (currentPetHunger - 1).coerceAtLeast(0)
                    }

                    when {
                        isAfkWoodcuttingActive.value -> {
                            activeSkill = OsrsSkill.WOODCUTTING
                            val wcXp = skillXpMap.value[OsrsSkill.WOODCUTTING] ?: 0L
                            val wcLvl = OsrsXpCalculator.getLevelForXp(wcXp)
                            val area = com.example.data.models.AdventuringStoryData.GROVE_FOREST_AREAS.find { it.id == _selectedGroveForestId.value }
                                ?: com.example.data.models.AdventuringStoryData.GROVE_FOREST_AREAS.first()
                            if (wcLvl < area.reqLevel) {
                                stoppedReason = "Woodcutting level too low for ${area.name}!"
                                break
                            }
                            val eligibleTrees = area.choppableTrees.filter { wcLvl >= it.reqLevel }
                            if (eligibleTrees.isEmpty()) {
                                stoppedReason = "No unlocked trees available to chop in ${area.name}!"
                                break
                            }
                            val totalWeight = eligibleTrees.sumOf { it.dropChancePercent.coerceAtLeast(1) }
                            val roll = (1..totalWeight.coerceAtLeast(1)).random()
                            var cumulative = 0
                            var tree: com.example.data.models.GroveTree = eligibleTrees.first()
                            for (t in eligibleTrees) {
                                cumulative += t.dropChancePercent.coerceAtLeast(1)
                                if (roll <= cumulative) {
                                    tree = t
                                    break
                                }
                            }
                            if (tree == null) {
                                stoppedReason = "No unlocked trees available to chop in ${area.name}!"
                                break
                            }
                            val logId = tree.id
                            val logXp = tree.xp
                            localBankMap[logId] = (localBankMap[logId] ?: 0) + 1
                            harvestedSummaryMap[logId] = (harvestedSummaryMap[logId] ?: 0) + 1
                            val bramLvl = npcFavorMap.value["bram"]?.first ?: getNpcFavorLevel("bram")
                            if ((1..100).random() <= bramLvl) {
                                localBankMap[logId] = (localBankMap[logId] ?: 0) + 1
                                extraBonusHarvestedMap[logId] = (extraBonusHarvestedMap[logId] ?: 0) + 1
                            }
                            if (tree.bonusSecondItemId != null && tree.bonusSecondItemQty > 0) {
                                val secId = tree.bonusSecondItemId!!
                                val secQty = tree.bonusSecondItemQty
                                localBankMap[secId] = (localBankMap[secId] ?: 0) + secQty
                                harvestedSummaryMap[secId] = (harvestedSummaryMap[secId] ?: 0) + secQty
                            }
                            accumulatedXp += logXp
                            primaryHarvestedItemName = tree.name
                            primaryHarvestedItemCount++
                            completedTicks++
                        }
                        isAfkFishingActive.value -> {
                            activeSkill = OsrsSkill.FISHING
                            val fXp = skillXpMap.value[OsrsSkill.FISHING] ?: 0L
                            val fLvl = OsrsXpCalculator.getLevelForXp(fXp)
                            val area = com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS.find { it.id == _selectedSpiritPoolAreaId.value }
                                ?: com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS.first()
                            if (fLvl < area.reqLevel) {
                                stoppedReason = "Fishing level too low for ${area.name}!"
                                break
                            }
                            val eligibleFish = area.catchableFish.filter { fLvl >= it.reqLevel }
                            if (eligibleFish.isEmpty()) {
                                stoppedReason = "No unlocked fish available to catch in ${area.name}!"
                                break
                            }
                            val totalWeight = eligibleFish.sumOf { it.dropChancePercent.coerceAtLeast(1) }
                            val roll = (1..totalWeight.coerceAtLeast(1)).random()
                            var cumulative = 0
                            var fish: com.example.data.models.SpiritFish = eligibleFish.first()
                            for (f in eligibleFish) {
                                cumulative += f.dropChancePercent.coerceAtLeast(1)
                                if (roll <= cumulative) {
                                    fish = f
                                    break
                                }
                            }
                            if (fish == null) {
                                stoppedReason = "No unlocked fish available to catch in ${area.name}!"
                                break
                            }
                            val fishId = fish.id
                            val fishXp = fish.xp
                            localBankMap[fishId] = (localBankMap[fishId] ?: 0) + 1
                            harvestedSummaryMap[fishId] = (harvestedSummaryMap[fishId] ?: 0) + 1
                            val finbarLvl = npcFavorMap.value["finbar"]?.first ?: getNpcFavorLevel("finbar")
                            if ((1..100).random() <= finbarLvl) {
                                localBankMap[fishId] = (localBankMap[fishId] ?: 0) + 1
                                extraBonusHarvestedMap[fishId] = (extraBonusHarvestedMap[fishId] ?: 0) + 1
                            }
                            accumulatedXp += fishXp
                            primaryHarvestedItemName = fish.name
                            primaryHarvestedItemCount++
                            completedTicks++
                        }
                        isAfkMiningActive.value -> {
                            activeSkill = OsrsSkill.SMITHING
                            val mXp = skillXpMap.value[OsrsSkill.SMITHING] ?: 0L
                            val mLvl = OsrsXpCalculator.getLevelForXp(mXp)
                            val area = com.example.data.models.AdventuringStoryData.GEMOLOGY_AREAS.find { it.id == _selectedGemologyAreaId.value }
                                ?: com.example.data.models.AdventuringStoryData.GEMOLOGY_AREAS.first()
                            if (mLvl < area.reqLevel) {
                                stoppedReason = "Forging level too low for ${area.name}!"
                                break
                            }
                            val eligibleMinerals = area.minerals.filter { mLvl >= it.reqLevel }
                            if (eligibleMinerals.isEmpty()) {
                                stoppedReason = "No unlocked minerals available to mine in ${area.name}!"
                                break
                            }
                            val totalWeight = eligibleMinerals.sumOf { it.dropChancePercent.coerceAtLeast(1) }
                            val roll = (1..totalWeight.coerceAtLeast(1)).random()
                            var cumulative = 0
                            var mineral: com.example.data.models.GemologyMineral = eligibleMinerals.first()
                            for (m in eligibleMinerals) {
                                cumulative += m.dropChancePercent.coerceAtLeast(1)
                                if (roll <= cumulative) {
                                    mineral = m
                                    break
                                }
                            }
                            if (mineral == null) {
                                stoppedReason = "No unlocked minerals available to mine in ${area.name}!"
                                break
                            }
                            val oreId = mineral.id
                            val oreXp = mineral.xp
                            localBankMap[oreId] = (localBankMap[oreId] ?: 0) + 1
                            harvestedSummaryMap[oreId] = (harvestedSummaryMap[oreId] ?: 0) + 1
                            val arlgLvl = npcFavorMap.value["arlg"]?.first ?: npcFavorMap.value["arig"]?.first ?: getNpcFavorLevel("arlg")
                            if ((1..100).random() <= arlgLvl) {
                                localBankMap[oreId] = (localBankMap[oreId] ?: 0) + 1
                                extraBonusHarvestedMap[oreId] = (extraBonusHarvestedMap[oreId] ?: 0) + 1
                            }
                            accumulatedXp += oreXp
                            primaryHarvestedItemName = mineral.name
                            primaryHarvestedItemCount++
                            completedTicks++
                        }
                        isAfkThievingActive.value -> {
                            activeSkill = OsrsSkill.THIEVING
                            val tXp = skillXpMap.value[OsrsSkill.THIEVING] ?: 0L
                            val tLvl = OsrsXpCalculator.getLevelForXp(tXp)
                            val targetNpcId = _selectedThievingNpcId.value
                            val npc = PICKPOCKET_NPCS.find { it.id == targetNpcId } ?: PICKPOCKET_NPCS.first()
                            if (tLvl < npc.levelReq) {
                                stoppedReason = "Trickery level too low to pickpocket ${npc.name}!"
                                break
                            }
                            val mult = getThievingGpMultiplier()
                            val renLvl = npcFavorMap.value["ren"]?.first ?: getNpcFavorLevel("ren")
                            val coinMult = if ((1..100).random() <= renLvl) 2 else 1
                            val baseGp = (npc.levelReq * 3L + (5..20).random())
                            val totalGp = (baseGp * mult * coinMult).toLong()
                            accumulatedGp += totalGp
                            accumulatedXp += npc.thievingXp.toLong().coerceAtLeast(8L)

                            // Pickpocket item rewards drop logic
                            val drop = com.example.data.models.rollPickpocketDrop(npc.id)
                            if (drop != null) {
                                val itemQty = drop.quantity
                                localBankMap[drop.itemId] = (localBankMap[drop.itemId] ?: 0) + itemQty
                                harvestedSummaryMap[drop.itemId] = (harvestedSummaryMap[drop.itemId] ?: 0) + itemQty
                                if ((1..100).random() <= renLvl) {
                                    localBankMap[drop.itemId] = (localBankMap[drop.itemId] ?: 0) + itemQty
                                    extraBonusHarvestedMap[drop.itemId] = (extraBonusHarvestedMap[drop.itemId] ?: 0) + itemQty
                                }
                                val itemDef = com.example.data.models.DefaultItems.getItemById(drop.itemId)
                                val itemName = if (itemDef.name.isNotBlank() && itemDef.name != "Unknown Item") itemDef.name else drop.itemId.removePrefix("item_").replace("_", " ").replaceFirstChar { it.uppercase() }
                                primaryHarvestedItemName = itemName
                            } else {
                                primaryHarvestedItemName = "Coins from ${npc.name}"
                            }
                            primaryHarvestedItemCount++
                            completedTicks++
                        }
                        isAfkCookingActive.value -> {
                            activeSkill = OsrsSkill.COOKING
                            val queue = _cookingQueue.value
                            val cookingXp = skillXpMap.value[OsrsSkill.COOKING] ?: 0L
                            val cookLvl = OsrsXpCalculator.getLevelForXp(cookingXp)

                            // Pick the first queued item with stock and required level
                            val rawId: String? = queue.firstOrNull { qId ->
                                val rec = com.example.data.models.CookingRecipes.findRecipe(qId)
                                val req = rec?.reqLevel ?: 1
                                cookLvl >= req && (localBankMap[qId] ?: 0) > 0
                            } ?: if (queue.isEmpty() && _selectedFoodId.value != null) {
                                val sId = _selectedFoodId.value!!
                                val rec = com.example.data.models.CookingRecipes.findRecipe(sId)
                                val req = rec?.reqLevel ?: 1
                                if (cookLvl >= req && (localBankMap[sId] ?: 0) > 0) sId else null
                            } else null

                            if (rawId != null) {
                                val recipe = com.example.data.models.CookingRecipes.findRecipe(rawId)
                                val currentQty = localBankMap[rawId] ?: 0
                                if (currentQty > 0) {
                                    localBankMap[rawId] = currentQty - 1
                                    val cookedId = recipe?.cookedId ?: (if (rawId.startsWith("item_raw_")) rawId.replace("item_raw_", "item_") else "item_cooked_$rawId")
                                    localBankMap[cookedId] = (localBankMap[cookedId] ?: 0) + 1
                                    harvestedSummaryMap[cookedId] = (harvestedSummaryMap[cookedId] ?: 0) + 1
                                    val itemXp = recipe?.xpEarned ?: 40L
                                    accumulatedXp += itemXp
                                    accumulatedGp += 15L
                                    primaryHarvestedItemName = recipe?.cookedName ?: cookedId.removePrefix("item_cooked_").replace("_", " ").replaceFirstChar { it.uppercase() }
                                    primaryHarvestedItemCount++
                                    completedTicks++
                                } else {
                                    stoppedReason = "Finished all queued raw food!"
                                    break
                                }
                            } else {
                                stoppedReason = "Finished all queued raw food!"
                                break
                            }
                        }
                        isAfkCampfireActive.value -> {
                            activeSkill = OsrsSkill.FIREMAKING
                            val logEntry = localBankMap.entries.firstOrNull { (it.key == "item_logs" || it.key.contains("logs")) && it.value > 0 }
                            if (logEntry != null && logEntry.value > 0) {
                                val logId = logEntry.key
                                localBankMap[logId] = (localBankMap[logId] ?: 0) - 1
                                localBankMap["item_ashes"] = (localBankMap["item_ashes"] ?: 0) + 1
                                harvestedSummaryMap["item_ashes"] = (harvestedSummaryMap["item_ashes"] ?: 0) + 1
                                val logXp = when (logId) {
                                    "item_redwood_logs" -> 350L
                                    "item_magic_logs" -> 250L
                                    "item_yew_logs" -> 175L
                                    "item_maple_logs" -> 135L
                                    "item_willow_logs" -> 90L
                                    "item_oak_logs" -> 60L
                                    else -> 40L
                                }
                                accumulatedXp += logXp
                                primaryHarvestedItemName = "Ashes"
                                primaryHarvestedItemCount++
                                completedTicks++
                            } else {
                                stoppedReason = "Out of logs!"
                                break
                            }
                        }
                        isAfkSmeltingActive.value -> {
                            activeSkill = OsrsSkill.SMITHING
                            val barId = _selectedBarId.value ?: "item_bronze_bar"
                            val reqOreId: String
                            val reqQty: Int
                            val coalQty: Int
                            val barXp: Long
                            when (barId) {
                                "item_runite_bar", "item_rune_bar" -> { reqOreId = "item_runite_ore"; reqQty = 1; coalQty = 4; barXp = 150L }
                                "item_adamant_bar", "item_adamantite_bar" -> { reqOreId = "item_adamant_ore"; reqQty = 1; coalQty = 3; barXp = 100L }
                                "item_mithril_bar" -> { reqOreId = "item_mithril_ore"; reqQty = 1; coalQty = 2; barXp = 80L }
                                "item_steel_bar" -> { reqOreId = "item_iron_ore"; reqQty = 1; coalQty = 2; barXp = 50L }
                                "item_iron_bar" -> { reqOreId = "item_iron_ore"; reqQty = 1; coalQty = 0; barXp = 35L }
                                else -> { reqOreId = "item_copper_ore"; reqQty = 1; coalQty = 0; barXp = 20L }
                            }
                            val hasOre = (localBankMap[reqOreId] ?: 0) >= reqQty
                            val hasCoal = coalQty == 0 || (localBankMap["item_coal_ore"] ?: 0) >= coalQty
                            if (hasOre && hasCoal) {
                                localBankMap[reqOreId] = (localBankMap[reqOreId] ?: 0) - reqQty
                                if (coalQty > 0) localBankMap["item_coal_ore"] = (localBankMap["item_coal_ore"] ?: 0) - coalQty
                                localBankMap[barId] = (localBankMap[barId] ?: 0) + 1
                                harvestedSummaryMap[barId] = (harvestedSummaryMap[barId] ?: 0) + 1
                                accumulatedXp += barXp
                                primaryHarvestedItemName = barId.replace("item_", "").replace("_", " ").replaceFirstChar { it.uppercase() }
                                primaryHarvestedItemCount++
                                completedTicks++
                            } else {
                                stoppedReason = "Out of ores/coal!"
                                break
                            }
                        }
                        isAfkNailCraftingActive.value -> {
                            activeSkill = OsrsSkill.SMITHING
                            val barEntry = localBankMap.entries.firstOrNull { it.key.endsWith("_bar") && it.value > 0 }
                            if (barEntry != null && barEntry.value > 0) {
                                val barId = barEntry.key
                                localBankMap[barId] = (localBankMap[barId] ?: 0) - 1
                                localBankMap["item_nails"] = (localBankMap["item_nails"] ?: 0) + 15
                                harvestedSummaryMap["item_nails"] = (harvestedSummaryMap["item_nails"] ?: 0) + 15
                                accumulatedXp += 40L
                                primaryHarvestedItemName = "Nails"
                                primaryHarvestedItemCount += 15
                                completedTicks++
                            } else {
                                stoppedReason = "Out of metal bars!"
                                break
                            }
                        }
                        isAfkSawmillActive.value -> {
                            activeSkill = OsrsSkill.WOODCUTTING
                            val logEntry = localBankMap.entries.firstOrNull { (it.key == "item_logs" || it.key.contains("logs")) && it.value > 0 }
                            if (logEntry != null && logEntry.value > 0) {
                                val logId = logEntry.key
                                localBankMap[logId] = (localBankMap[logId] ?: 0) - 1
                                val plankId = if (logId == "item_oak_logs") "item_oak_plank" else "item_plank"
                                localBankMap[plankId] = (localBankMap[plankId] ?: 0) + 1
                                harvestedSummaryMap[plankId] = (harvestedSummaryMap[plankId] ?: 0) + 1
                                accumulatedXp += 50L
                                primaryHarvestedItemName = "Planks"
                                primaryHarvestedItemCount++
                                completedTicks++
                            } else {
                                stoppedReason = "Out of logs!"
                                break
                            }
                        }
                        isAfkBowstringCraftingActive.value -> {
                            activeSkill = OsrsSkill.FLETCHING
                            val flaxEntry = localBankMap.entries.firstOrNull { it.key == "item_flax" && it.value > 0 }
                            if (flaxEntry != null && flaxEntry.value > 0) {
                                localBankMap["item_flax"] = (localBankMap["item_flax"] ?: 0) - 1
                                localBankMap["item_bowstring"] = (localBankMap["item_bowstring"] ?: 0) + 1
                                harvestedSummaryMap["item_bowstring"] = (harvestedSummaryMap["item_bowstring"] ?: 0) + 1
                                accumulatedXp += 25L
                                primaryHarvestedItemName = "Bowstring"
                                primaryHarvestedItemCount++
                                completedTicks++
                            } else {
                                stoppedReason = "Out of flax!"
                                break
                            }
                        }
                        isAfkStickCraftingActive.value -> {
                            activeSkill = OsrsSkill.FLETCHING
                            val logEntry = localBankMap.entries.firstOrNull { (it.key == "item_logs" || it.key.contains("logs")) && it.value > 0 }
                            if (logEntry != null && logEntry.value > 0) {
                                val logId = logEntry.key
                                localBankMap[logId] = (localBankMap[logId] ?: 0) - 1
                                localBankMap["item_short_sticks"] = (localBankMap["item_short_sticks"] ?: 0) + 2
                                harvestedSummaryMap["item_short_sticks"] = (harvestedSummaryMap["item_short_sticks"] ?: 0) + 2
                                accumulatedXp += 20L
                                primaryHarvestedItemName = "Short Sticks"
                                primaryHarvestedItemCount += 2
                                completedTicks++
                            } else {
                                stoppedReason = "Out of logs!"
                                break
                            }
                        }
                        isAfkShaftCraftingActive.value -> {
                            activeSkill = OsrsSkill.FLETCHING
                            val logEntry = localBankMap.entries.firstOrNull { (it.key == "item_logs" || it.key.contains("logs")) && it.value > 0 }
                            if (logEntry != null && logEntry.value > 0) {
                                val logId = logEntry.key
                                localBankMap[logId] = (localBankMap[logId] ?: 0) - 1
                                localBankMap["item_arrow_shafts"] = (localBankMap["item_arrow_shafts"] ?: 0) + 15
                                harvestedSummaryMap["item_arrow_shafts"] = (harvestedSummaryMap["item_arrow_shafts"] ?: 0) + 15
                                accumulatedXp += 15L
                                primaryHarvestedItemName = "Arrow Shafts"
                                primaryHarvestedItemCount += 15
                                completedTicks++
                            } else {
                                stoppedReason = "Out of logs!"
                                break
                            }
                        }
                        isAfkFeatherCraftingActive.value -> {
                            activeSkill = OsrsSkill.FLETCHING
                            localBankMap["item_feathers"] = (localBankMap["item_feathers"] ?: 0) + 5
                            harvestedSummaryMap["item_feathers"] = (harvestedSummaryMap["item_feathers"] ?: 0) + 5
                            accumulatedXp += 15L
                            primaryHarvestedItemName = "Feathers"
                            primaryHarvestedItemCount += 5
                            completedTicks++
                        }
                        isAfkArrowtipCraftingActive.value -> {
                            activeSkill = OsrsSkill.FLETCHING
                            val barEntry = localBankMap.entries.firstOrNull { it.key.endsWith("_bar") && it.value > 0 }
                            if (barEntry != null && barEntry.value > 0) {
                                val barId = barEntry.key
                                localBankMap[barId] = (localBankMap[barId] ?: 0) - 1
                                localBankMap["item_arrowtips"] = (localBankMap["item_arrowtips"] ?: 0) + 15
                                harvestedSummaryMap["item_arrowtips"] = (harvestedSummaryMap["item_arrowtips"] ?: 0) + 15
                                accumulatedXp += 35L
                                primaryHarvestedItemName = "Arrowtips"
                                primaryHarvestedItemCount += 15
                                completedTicks++
                            } else {
                                stoppedReason = "Out of metal bars!"
                                break
                            }
                        }
                        isAfkFletchingActive.value -> {
                            activeSkill = OsrsSkill.FLETCHING
                            val shafts = localBankMap["item_arrow_shafts"] ?: 0
                            val feathers = localBankMap["item_feathers"] ?: 0
                            if (shafts > 0 && feathers > 0) {
                                localBankMap["item_arrow_shafts"] = shafts - 1
                                localBankMap["item_feathers"] = feathers - 1
                                localBankMap["item_headless_arrows"] = (localBankMap["item_headless_arrows"] ?: 0) + 1
                                harvestedSummaryMap["item_headless_arrows"] = (harvestedSummaryMap["item_headless_arrows"] ?: 0) + 1
                                accumulatedXp += 25L
                                primaryHarvestedItemName = "Headless Arrows"
                                primaryHarvestedItemCount++
                                completedTicks++
                            } else {
                                stoppedReason = "Out of shafts or feathers!"
                                break
                            }
                        }
                        isAfkTrapCraftingActive.value -> {
                            activeSkill = OsrsSkill.HUNTER
                            val wood = localBankMap["item_logs"] ?: 0
                            if (wood > 0) {
                                localBankMap["item_logs"] = wood - 1
                                localBankMap["item_bird_snare"] = (localBankMap["item_bird_snare"] ?: 0) + 1
                                harvestedSummaryMap["item_bird_snare"] = (harvestedSummaryMap["item_bird_snare"] ?: 0) + 1
                                accumulatedXp += 35L
                                primaryHarvestedItemName = "Bird Snares"
                                primaryHarvestedItemCount++
                                completedTicks++
                            } else {
                                stoppedReason = "Out of wood for traps!"
                                break
                            }
                        }
                        isAfkHerbCleaningActive.value -> {
                            activeSkill = OsrsSkill.HERBLORE
                            val grimyEntry = localBankMap.entries.firstOrNull { it.key.startsWith("item_grimy_") && it.value > 0 }
                            if (grimyEntry != null && grimyEntry.value > 0) {
                                val grimyId = grimyEntry.key
                                localBankMap[grimyId] = (localBankMap[grimyId] ?: 0) - 1
                                val cleanId = grimyId.replace("item_grimy_", "item_clean_")
                                localBankMap[cleanId] = (localBankMap[cleanId] ?: 0) + 1
                                harvestedSummaryMap[cleanId] = (harvestedSummaryMap[cleanId] ?: 0) + 1
                                accumulatedXp += 20L
                                primaryHarvestedItemName = cleanId.replace("item_clean_", "").replace("_", " ").replaceFirstChar { it.uppercase() }
                                primaryHarvestedItemCount++
                                completedTicks++
                            } else {
                                stoppedReason = "Out of grimy herbs!"
                                break
                            }
                        }
                        isAfkHerbCrushingActive.value -> {
                            activeSkill = OsrsSkill.HERBLORE
                            val crushable = localBankMap.entries.firstOrNull { (it.key == "item_clean_guam" || it.key.contains("herb")) && it.value > 0 }
                            if (crushable != null && crushable.value > 0) {
                                localBankMap[crushable.key] = (localBankMap[crushable.key] ?: 0) - 1
                                val crushedId = "item_crushed_${crushable.key.replace("item_clean_", "").replace("item_", "")}"
                                localBankMap[crushedId] = (localBankMap[crushedId] ?: 0) + 1
                                harvestedSummaryMap[crushedId] = (harvestedSummaryMap[crushedId] ?: 0) + 1
                                accumulatedXp += 30L
                                primaryHarvestedItemName = "Crushed Ingredients"
                                primaryHarvestedItemCount++
                                completedTicks++
                            } else {
                                stoppedReason = "Out of crushable ingredients!"
                                break
                            }
                        }
                        isAfkPotionBrewingActive.value -> {
                            activeSkill = OsrsSkill.HERBLORE
                            val herb = localBankMap.entries.firstOrNull { it.key.startsWith("item_clean_") && it.value > 0 }
                            val water = localBankMap["item_vial_of_water"] ?: 0
                            if (herb != null && herb.value > 0 && water > 0) {
                                localBankMap[herb.key] = (localBankMap[herb.key] ?: 0) - 1
                                localBankMap["item_vial_of_water"] = water - 1
                                val potionId = "item_prayer_potion"
                                localBankMap[potionId] = (localBankMap[potionId] ?: 0) + 1
                                harvestedSummaryMap[potionId] = (harvestedSummaryMap[potionId] ?: 0) + 1
                                accumulatedXp += 85L
                                primaryHarvestedItemName = "Potions"
                                primaryHarvestedItemCount++
                                completedTicks++
                            } else {
                                stoppedReason = "Out of herb or vial of water!"
                                break
                            }
                        }
                        isAfkBoneBuryingActive.value -> {
                            activeSkill = OsrsSkill.MAGIC
                            val bone = localBankMap.entries.firstOrNull { it.key.contains("bone") && it.value > 0 }
                            if (bone != null && bone.value > 0) {
                                localBankMap[bone.key] = (localBankMap[bone.key] ?: 0) - 1
                                accumulatedXp += 45L
                                primaryHarvestedItemName = "Bones Offered"
                                primaryHarvestedItemCount++
                                completedTicks++
                            } else {
                                stoppedReason = "Out of bones!"
                                break
                            }
                        }
                        isAfkSailingActive.value -> {
                            activeSkill = OsrsSkill.SAILING
                            accumulatedXp += 80L
                            accumulatedGp += 50L
                            if (Math.random() < 0.25) {
                                val salvageId = if (Math.random() < 0.5) "item_doubloons" else "item_treasure_chest"
                                localBankMap[salvageId] = (localBankMap[salvageId] ?: 0) + 1
                                harvestedSummaryMap[salvageId] = (harvestedSummaryMap[salvageId] ?: 0) + 1
                            }
                            primaryHarvestedItemName = "Sea Miles Sailed"
                            primaryHarvestedItemCount++
                            completedTicks++
                        }
                        isAfkRunecraftingActive.value -> {
                            activeSkill = OsrsSkill.RUNECRAFT
                            val essEntry = localBankMap.entries.firstOrNull { (it.key == "item_pure_essence" || it.key == "item_rune_essence") && it.value > 0 }
                            if (essEntry != null && essEntry.value > 0) {
                                val runeId = _afkRunecraftTargetRuneId.value
                                val runeInfo = com.example.data.models.RunecraftData.CRAFTABLE_RUNES.find { it.runeItemId == runeId }
                                    ?: com.example.data.models.RunecraftData.CRAFTABLE_RUNES.first()
                                localBankMap[essEntry.key] = (localBankMap[essEntry.key] ?: 0) - 1
                                localBankMap[runeId] = (localBankMap[runeId] ?: 0) + 1
                                harvestedSummaryMap[runeId] = (harvestedSummaryMap[runeId] ?: 0) + 1
                                val runeXp = (runeInfo.xpPerEssence * 10).toLong().coerceAtLeast(10L)
                                accumulatedXp += runeXp
                                primaryHarvestedItemName = runeInfo.runeName
                                primaryHarvestedItemCount += 1
                                completedTicks++
                            } else {
                                stoppedReason = "Out of rune essence!"
                                break
                            }
                        }

                        isAfkFarmingActive.value -> {
                            activeSkill = OsrsSkill.FARMING
                            var didAnyFarmAction = false
                            val simulatedNow = lastBgTime + (tick * tickIntervalMs)

                            // 1. Harvest any mature crops
                            for (i in offlinePlots.indices) {
                                val plot = offlinePlots[i]
                                if (!com.example.data.models.isFarmPlotUnlocked(i, farmingLvl, constructionLvl)) continue
                                val crop = plot.cropType
                                if (crop != null && plot.isReadyToHarvest(simulatedNow)) {
                                    val baseProduce = if (crop.category == com.example.data.models.SeedCategory.HERB) {
                                        val baseHerbYield = (6 + ((farmingLvl - 1) * 8 / 98)).coerceIn(6, 14)
                                        val yieldBonus = if (plot.isComposted) 2 else 0
                                        baseHerbYield + yieldBonus
                                    } else if (crop.category == com.example.data.models.SeedCategory.VEGETABLE) {
                                        val baseYield = (3 + (farmingLvl / 10)).coerceIn(3, 15)
                                        val yieldBonus = if (plot.isComposted) 2 else 0
                                        baseYield + yieldBonus
                                    } else {
                                        val yieldBonus = if (plot.isComposted) 2 else 0
                                        crop.produceQty + yieldBonus
                                    }
                                    val bryanLvl = npcFavorMap.value["bryan"]?.first ?: getNpcFavorLevel("bryan")
                                    val hasSiegeQuest = petState.value.completedQuestIds.contains("quest_monkey_madness_2") || petState.value.completedQuestIds.contains("quest_monkey_madness_2_part2")
                                    val doubleChance = (bryanLvl + if (hasSiegeQuest) 25 else 0).coerceIn(0, 100)
                                    var totalProduce = baseProduce
                                    if (doubleChance > 0 && (1..100).random() <= doubleChance) {
                                        totalProduce = baseProduce * 2
                                        extraBonusHarvestedMap[crop.produceItemId] = (extraBonusHarvestedMap[crop.produceItemId] ?: 0) + baseProduce
                                    }
                                    val xpBonus = if (plot.isComposted) (crop.farmingXp * 0.25).toLong() else 0L
                                    val cropXp = crop.farmingXp + xpBonus
                                    accumulatedXp += cropXp
                                    accumulatedGp += (crop.produceGpVal * totalProduce)

                                    val prodId = crop.produceItemId
                                    localBankMap[prodId] = (localBankMap[prodId] ?: 0) + totalProduce
                                    harvestedSummaryMap[prodId] = (harvestedSummaryMap[prodId] ?: 0) + totalProduce
                                    totalCropsHarvested += totalProduce

                                    primaryHarvestedItemName = crop.produceName
                                    primaryHarvestedItemCount += totalProduce
                                    offlinePlots[i] = com.example.data.models.FarmPlotState(plotIndex = i)
                                    didAnyFarmAction = true
                                }
                            }

                            // 2. Auto-plant seeds in empty plots from localBankMap
                            for (i in offlinePlots.indices) {
                                val plot = offlinePlots[i]
                                if (!com.example.data.models.isFarmPlotUnlocked(i, farmingLvl, constructionLvl)) continue
                                if (plot.cropType != null) continue
                                val candidateCrops = allCropTypes.filter { c ->
                                    com.example.data.models.isCropAllowedInPlot(i, c) &&
                                    farmingLvl >= c.reqFarmingLevel &&
                                    (localBankMap[c.seedId] ?: 0) > 0
                                }
                                val matchingCrop = if (targetSeedCat != com.example.data.models.SeedCategory.ALL) {
                                    candidateCrops.firstOrNull { it.category == targetSeedCat } ?: candidateCrops.firstOrNull()
                                } else {
                                    candidateCrops.firstOrNull()
                                }
                                if (matchingCrop != null) {
                                    val seedCount = localBankMap[matchingCrop.seedId] ?: 0
                                    if (seedCount > 0) {
                                        localBankMap[matchingCrop.seedId] = seedCount - 1
                                        val useCompost = compostBuckets > 0
                                        if (useCompost) compostBuckets--
                                        offlinePlots[i] = com.example.data.models.FarmPlotState(
                                            plotIndex = i,
                                            cropType = matchingCrop,
                                            plantedTimestampMs = simulatedNow,
                                            isWatered = true,
                                            isComposted = useCompost
                                        )
                                        didAnyFarmAction = true
                                    }
                                }
                            }

                            completedTicks++
                            if (!didAnyFarmAction && offlinePlots.none { it.cropType != null } && allCropTypes.none { (localBankMap[it.seedId] ?: 0) > 0 }) {
                                stoppedReason = "All crops harvested & out of seeds!"
                                break
                            }
                        }
                        isAfkHunterActive.value -> {
                            activeSkill = OsrsSkill.HUNTER
                            accumulatedXp += 65L
                            completedTicks++
                            primaryHarvestedItemName = "Hunter Catches"
                            primaryHarvestedItemCount++
                        }
                        isAfkSlayerActive.value -> {
                            activeSkill = OsrsSkill.SLAYER
                            accumulatedXp += 75L
                            completedTicks++
                            primaryHarvestedItemName = "Slayer Kills"
                            primaryHarvestedItemCount++
                        }
                        isAfkBossActive.value -> {
                            activeSkill = OsrsSkill.SLAYER
                            accumulatedXp += 150L
                            accumulatedGp += 100L
                            completedTicks++
                            primaryHarvestedItemName = "Boss Encounters"
                            primaryHarvestedItemCount++
                        }
                        else -> {
                            completedTicks++
                        }
                    }
                }

                // Apply updated bank map to database
                if (localBankMap.isNotEmpty()) {
                    for ((itemId, newQty) in localBankMap) {
                        val origQty = dbBank.find { it.id == itemId }?.quantity ?: 0
                        if (newQty != origQty) {
                            repository.saveBankItem(petTypeName, itemId, newQty)
                        }
                    }
                }

                val hungerUsed = (startHunger - currentPetHunger).coerceAtLeast(0)
                val hungerRemaining = currentPetHunger.coerceIn(0, maxHunger)

                if (isAfkFarmingActive.value) {
                    updatePofState(currentPof.copy(
                        plots = offlinePlots,
                        compostBucketsCount = compostBuckets,
                        totalCropsHarvested = totalCropsHarvested
                    ))
                }

                // Update Pet Hunger state for completed ticks
                val updatedPet = pet.copy(hunger = hungerRemaining)
                repository.savePetState(updatedPet)

                withContext(Dispatchers.Main) {
                    cleanCookingQueue()
                    // Apply accumulated XP and GP
                    if (accumulatedXp > 0L && activeSkill != null) {
                        val logTitle = "Offline $activeName Gains"
                        val itemHarvestDesc = if (primaryHarvestedItemCount > 0) " ($primaryHarvestedItemCount x $primaryHarvestedItemName harvested)" else ""
                        val logDesc = "Completed $completedTicks $activeName actions while away$itemHarvestDesc! (+$accumulatedXp ${activeSkill.displayName} XP)"
                        addXpAndNotify(
                            skill = activeSkill,
                            amount = accumulatedXp,
                            gpReward = accumulatedGp,
                            logTitle = logTitle,
                            logDesc = logDesc
                        )
                    }

                    if (completedTicks > 0) {
                        val lootList = harvestedSummaryMap.filter { it.value > 0 }.map { entry ->
                            val cropMatch = com.example.data.models.FarmCropType.entries.find { it.produceItemId == entry.key }
                            val itemDef = com.example.data.models.DefaultItems.getItemById(entry.key)
                            val displayName = when {
                                cropMatch != null -> cropMatch.produceName
                                itemDef.name.isNotBlank() && itemDef.name != "Unknown Item" -> itemDef.name
                                else -> entry.key.removePrefix("item_").replace("_", " ").split(" ")
                                    .joinToString(" ") { word -> word.replaceFirstChar { c -> c.uppercaseChar() } }
                            }
                            val emoji = cropMatch?.produceEmoji ?: itemDef.iconEmoji
                            com.example.data.models.OfflineHarvestedItem(
                                id = entry.key,
                                name = displayName,
                                quantity = entry.value,
                                iconEmoji = emoji
                            )
                        }

                        val extraBonusList = extraBonusHarvestedMap.map { entry ->
                            val itemDef = com.example.data.models.DefaultItems.getItemById(entry.key)
                            val cropMatch = allCropTypes.find { it.produceItemId == entry.key }
                            val displayName = when {
                                cropMatch != null -> cropMatch.produceName
                                itemDef.name.isNotBlank() && itemDef.name != "Unknown Item" -> itemDef.name
                                else -> entry.key.removePrefix("item_").replace("_", " ").split(" ")
                                    .joinToString(" ") { word -> word.replaceFirstChar { c -> c.uppercaseChar() } }
                            }
                            val emoji = cropMatch?.produceEmoji ?: itemDef.iconEmoji
                            com.example.data.models.OfflineHarvestedItem(
                                id = entry.key,
                                name = displayName,
                                quantity = entry.value,
                                iconEmoji = emoji
                            )
                        }
                        val report = com.example.data.models.OfflineGainsReport(
                            activityName = activeName,
                            activityEmoji = AfkEngine.getEmoji(),
                            skill = activeSkill,
                            elapsedMillis = (completedTicks * 5000L).coerceAtLeast(1000L),
                            actionsCompleted = completedTicks,
                            xpGained = accumulatedXp,
                            gpGained = accumulatedGp,
                            hungerStart = startHunger,
                            hungerUsed = hungerUsed,
                            hungerRemaining = hungerRemaining,
                            maxHunger = maxHunger,
                            itemsGained = lootList,
                            extraBonusMaterialsGained = extraBonusList,
                            extraNpcBonusProcCount = extraBonusList.sumOf { it.quantity },
                            stoppedReason = stoppedReason,
                            golemGains = null
                        )

                        _offlineGainsReport.value = report
                        com.example.util.NotificationHelper.sendOfflineGainsSummaryNotification(
                            getApplication(),
                            report
                        )

                        val awayHours = elapsedMillis / 3600000L
                        val awayMins = (elapsedMillis % 3600000L) / 60000L
                        val awayFormatted = if (awayHours > 0) "${awayHours}h ${awayMins}m" else "${awayMins}m"
                        val startPct = if (maxHunger > 0) ((startHunger.toFloat() / maxHunger.toFloat()) * 100).toInt() else startHunger
                        val remPct = if (maxHunger > 0) ((hungerRemaining.toFloat() / maxHunger.toFloat()) * 100).toInt() else hungerRemaining
                        val hungerSummaryStr = if (maxHunger != 100) "$startHunger/$maxHunger ($startPct%) ➔ $hungerRemaining/$maxHunger ($remPct%)" else "$startPct% ➔ $remPct%"
                        val summaryMsg = if (stoppedReason != null) {
                            "🌙 AFK Gains: $activeName ran for ${report.formattedDuration} before stopping ($stoppedReason • Away: $awayFormatted • $completedTicks actions, +$accumulatedXp ${activeSkill?.displayName ?: ""} XP, Hunger: $hungerSummaryStr)!"
                        } else {
                            "🌙 AFK Gains: $activeName ran for ${report.formattedDuration} ($completedTicks actions, +$accumulatedXp ${activeSkill?.displayName ?: ""} XP, Hunger: $hungerSummaryStr)!"
                        }
                        addChatMessage(summaryMsg)
                    }
                }
                saveAfkStateToPrefs()
            } finally {
                isProcessingOfflineAfk = false
            }
        }
    }

// --- OFFERING POUCH METHODS ---
    fun offerResourceToSpirits(item: InventoryItem, quantity: Int) {
        if (quantity <= 0) return
        val currentQty = getItemQuantityCombined(item.id)
        if (currentQty < quantity) return

        val spec = com.example.ui.components.OfferingPouchRegistry.getSpec(item.id, item.name)
        val spiritXp = ((spec.baseSpiritXp * spec.rarity.xpMultiplier) * quantity).toLong()
        val magicXp = ((spec.baseMagicXp * spec.rarity.xpMultiplier) * quantity).toLong()
        val gpReward = spec.baseGp * quantity

        viewModelScope.launch {
            deductItemCombined(item.id, quantity)

            if (spiritXp > 0) {
                addXpAndNotify(
                    skill = OsrsSkill.MAGIC,
                    amount = spiritXp,
                    gpReward = gpReward,
                    logTitle = "Spirit Offering: ${item.name}",
                    logDesc = "Offered $quantity x ${item.name} to the spirits (+$spiritXp Magic XP, +$gpReward GP)"
                )
            }
            if (magicXp > 0) {
                addXpAndNotify(
                    skill = OsrsSkill.MAGIC,
                    amount = magicXp,
                    gpReward = 0L,
                    logTitle = "Mystical Transmutation",
                    logDesc = "Channelled spiritual energy from ${item.name} (+$magicXp Magic XP)"
                )
            }
            boostPetMood((quantity * 2).coerceAtMost(20), "Spiritual Blessing")
            addChatMessage("✨ Offering Pouch: Sacrificed $quantity x ${item.name}! (+${spiritXp + magicXp} Magic XP, +$gpReward GP)")
        }
    }

    fun batchOfferMysticalCategory(category: com.example.ui.components.MysticalCategory) {
        viewModelScope.launch {
            val combinedItems = (inventoryItems.value + bankItems.value)
                .groupBy { it.id }
                .mapValues { entry ->
                    val first = entry.value.first()
                    val totalQty = entry.value.sumOf { it.quantity }
                    first.copy(quantity = totalQty)
                }.values.filter { it.quantity > 0 }

            val itemsToOffer = combinedItems.mapNotNull { item ->
                val spec = com.example.ui.components.OfferingPouchRegistry.getSpec(item.id, item.name)
                if (category == com.example.ui.components.MysticalCategory.ALL || spec.category == category) {
                    item to spec
                } else null
            }
            if (itemsToOffer.isEmpty()) {
                addChatMessage("⚠️ Offering Pouch: No mystical offerings found in ${category.label} category!")
                return@launch
            }
            var totalSpiritXp = 0L
            var totalMagicXp = 0L
            var totalGp = 0L
            var totalCount = 0

            for ((item, spec) in itemsToOffer) {
                val qty = item.quantity
                totalCount += qty
                totalSpiritXp += ((spec.baseSpiritXp * spec.rarity.xpMultiplier) * qty).toLong()
                totalMagicXp += ((spec.baseMagicXp * spec.rarity.xpMultiplier) * qty).toLong()
                totalGp += spec.baseGp * qty
                deductItemCombined(item.id, qty)
            }

            if (totalSpiritXp > 0) {
                addXpAndNotify(
                    skill = OsrsSkill.MAGIC,
                    amount = totalSpiritXp,
                    gpReward = totalGp,
                    logTitle = "Grand Category Offering",
                    logDesc = "Offered $totalCount ${category.label} items to the spirits! (+$totalSpiritXp Magic XP, +$totalGp GP)"
                )
            }
            if (totalMagicXp > 0) {
                addXpAndNotify(
                    skill = OsrsSkill.MAGIC,
                    amount = totalMagicXp,
                    gpReward = 0L,
                    logTitle = "Grand Arcane Infusion",
                    logDesc = "Channelled spiritual energy from $totalCount offerings (+$totalMagicXp Magic XP)"
                )
            }
            boostPetMood(25, "Grand Spirit Blessing")
            addChatMessage("🔥 Grand Offering: Offered $totalCount items in ${category.label}! (+${totalSpiritXp + totalMagicXp} Magic XP, +$totalGp GP)")
        }
    }

    fun batchOfferAllMystical() {
        batchOfferMysticalCategory(com.example.ui.components.MysticalCategory.ALL)
    }

    fun transmuteResourceInPouch(item: InventoryItem, quantity: Int) {
        viewModelScope.launch {
            val currentQty = getItemQuantityCombined(item.id)
            if (currentQty < quantity) return@launch
            deductItemCombined(item.id, quantity)
            val dustQty = quantity * 3
            val currentDust = inventoryItems.value.find { it.id == "item_pure_essence" }?.quantity ?: 0
            saveInventoryItem("item_pure_essence", currentDust + dustQty)
            addXpAndNotify(
                skill = OsrsSkill.RUNECRAFT,
                amount = (quantity * 45L),
                gpReward = (quantity * 20L),
                logTitle = "Transmutation Forge",
                logDesc = "Transmuted $quantity x ${item.name} into $dustQty Pure Essences!"
            )
            addChatMessage("🔮 Transmuted $quantity x ${item.name} into $dustQty Pure Essences!")
        }
    }

    fun stopAllAfkStations(reason: String? = null, stopQuests: Boolean = false) {
        val prevActive = getActiveAfkActivityName()
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        AfkEngine.stopAll(pohPrefs)
        _druidAltarProgress.value = 0f
        afkSessionTicks = 0
        com.example.util.NotificationHelper.clearOngoingAfkNotification(getApplication())
        saveAfkStateToPrefs()
        if (stopQuests && _activeQuestExpedition.value != null) {
            pauseQuestExpedition()
        }
        if (prevActive != null) {
            val notifyReason = reason ?: "Activity stopped."
            notifyAfkStopped(prevActive, notifyReason)
        } else if (reason != null) {
            com.example.util.NotificationHelper.sendAfkNotification(
                getApplication(),
                "⚡ AFK Activity Stopped",
                reason
            )
        }
    }

    fun toggleAfkStickCrafting() {
        val hasInvLogs = inventoryItems.value.any { it.id.contains("logs") && it.quantity > 0 }
        val hasBankLogs = bankItems.value.any { it.id.contains("logs") && it.quantity > 0 }
        if (!isAfkStickCraftingActive.value && !hasInvLogs && !hasBankLogs) {
            addChatMessage("🥢 Crafting Anvil: You need Logs in your inventory or bank to craft Wooden Sticks!")
            return
        }
        val nextState = !isAfkStickCraftingActive.value
        if (nextState && !canStartAfkOrHungerAction("Stick Crafting")) return
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.STICK_CRAFTING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🥢 AFK Stick Crafting: ${if (nextState) "ENABLED (1 Log -> 4 Sticks)" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun craftLogsToSticks(isAfk: Boolean = false) {
        if (!isAfk && !canStartAfkOrHungerAction("Stick Crafting")) return
        viewModelScope.launch {
            val selectedLogId = _selectedStickLogId.value
            val invLog = inventoryItems.value.find { (it.id == selectedLogId || selectedLogId.isEmpty()) && it.id.contains("logs") && it.quantity > 0 }
                ?: inventoryItems.value.find { it.id.contains("logs") && it.quantity > 0 }
            val bankLog = bankItems.value.find { (it.id == selectedLogId || selectedLogId.isEmpty()) && it.id.contains("logs") && it.quantity > 0 }
                ?: bankItems.value.find { it.id.contains("logs") && it.quantity > 0 }

            val fromBank = invLog == null
            val logItem = invLog ?: bankLog
            if (logItem == null) {
                if (isAfkStickCraftingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🥢 AFK Stick Crafting: Out of logs in inventory and bank!")
                    notifyAfkStopped("Stick Crafting", "Out of logs!")
                } else if (!isAfk) {
                    addChatMessage("🥢 Crafting Anvil: You do not have any logs in your inventory or bank!")
                }
                return@launch
            }

            if (fromBank) {
                saveBankItem(logItem.id, logItem.quantity - 1)
            } else {
                saveInventoryItem(logItem.id, logItem.quantity - 1)
            }

            val stickItem = inventoryItems.value.find { it.id == "item_wooden_stick" }
            val newQty = (stickItem?.quantity ?: 0) + 4
            saveInventoryItem("item_wooden_stick", newQty)

            addXpAndNotify(
                skill = com.example.data.models.OsrsSkill.FLETCHING,
                amount = 25L,
                gpReward = 0L,
                logTitle = "Crafted Wooden Sticks",
                logDesc = "Carved 1x ${logItem.name} into 4x Wooden Sticks (+25 Fletching XP)!"
            )
        }
    }

    fun setSelectedShaftLogId(logId: String) {
        _selectedShaftLogId.value = logId
    }

    fun toggleAfkShaftCrafting() {
        val hasInvLogs = inventoryItems.value.any { (it.id.contains("logs") || it.id == "item_logs") && it.quantity > 0 }
        val hasBankLogs = bankItems.value.any { (it.id.contains("logs") || it.id == "item_logs") && it.quantity > 0 }
        if (!isAfkShaftCraftingActive.value && !hasInvLogs && !hasBankLogs) {
            addChatMessage("🪵 Crafting Bench: You need Logs in your inventory or bank to craft Arrow Shafts!")
            return
        }
        val nextState = !isAfkShaftCraftingActive.value
        if (nextState && !canStartAfkOrHungerAction("Arrow Shaft Crafting")) return
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.SHAFT_CRAFTING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🪵 AFK Arrow Shaft Crafting: ${if (nextState) "ENABLED (1 Log -> 15 Shafts)" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun craftLogsToShafts(isAfk: Boolean = false) {
        if (!isAfk && !canStartAfkOrHungerAction("Arrow Shaft Crafting")) return
        viewModelScope.launch {
            val selectedLogId = _selectedShaftLogId.value
            val invLog = inventoryItems.value.find { (it.id == selectedLogId || selectedLogId.isEmpty()) && (it.id.contains("logs") || it.id == "item_logs") && it.quantity > 0 }
                ?: inventoryItems.value.find { (it.id.contains("logs") || it.id == "item_logs") && it.quantity > 0 }
            val bankLog = bankItems.value.find { (it.id == selectedLogId || selectedLogId.isEmpty()) && (it.id.contains("logs") || it.id == "item_logs") && it.quantity > 0 }
                ?: bankItems.value.find { (it.id.contains("logs") || it.id == "item_logs") && it.quantity > 0 }

            val fromBank = invLog == null
            val logItem = invLog ?: bankLog
            if (logItem == null) {
                if (isAfkShaftCraftingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🪵 AFK Arrow Shaft Crafting: Out of logs in inventory and bank!")
                    notifyAfkStopped("Arrow Shaft Crafting", "Out of logs!")
                } else if (!isAfk) {
                    addChatMessage("🪵 Crafting Bench: You do not have any logs in your inventory or bank!")
                }
                return@launch
            }

            if (fromBank) {
                saveBankItem(logItem.id, logItem.quantity - 1)
            } else {
                saveInventoryItem(logItem.id, logItem.quantity - 1)
            }

            val shaftItem = inventoryItems.value.find { it.id == "item_arrow_shaft" }
            val newQty = (shaftItem?.quantity ?: 0) + 15
            saveInventoryItem("item_arrow_shaft", newQty)

            addXpAndNotify(
                skill = com.example.data.models.OsrsSkill.FLETCHING,
                amount = 25L,
                gpReward = 0L,
                logTitle = "Whittled Arrow Shafts",
                logDesc = "Whittled 1x ${logItem.name} into 15x Arrow Shafts (+25 Fletching XP)!"
            )
        }
    }

    fun toggleAfkFeatherCrafting() {
        val nextState = !isAfkFeatherCraftingActive.value
        if (nextState && !canStartAfkOrHungerAction("Feather Gathering")) return
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.FEATHER_CRAFTING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🪶 AFK Feather Gathering: ${if (nextState) "ENABLED (+15 Feathers/tick)" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun craftFeathers(isAfk: Boolean = false) {
        if (!isAfk && !canStartAfkOrHungerAction("Feather Gathering")) return
        viewModelScope.launch {
            val featherItem = inventoryItems.value.find { it.id == "item_feather" }
            val newQty = (featherItem?.quantity ?: 0) + 15
            saveInventoryItem("item_feather", newQty)

            addXpAndNotify(
                skill = com.example.data.models.OsrsSkill.FLETCHING,
                amount = 20L,
                gpReward = 0L,
                logTitle = "Gathered Feathers",
                logDesc = "Gathered and trimmed 15x Flight Feathers (+20 Fletching XP)!"
            )
        }
    }

    fun toggleAfkBowstringCrafting() {
        val nextState = !isAfkBowstringCraftingActive.value
        if (nextState && !canStartAfkOrHungerAction("Bowstring Crafting")) return
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.BOWSTRING_CRAFTING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🧵 AFK Bowstring Spinning: ${if (nextState) "ENABLED (+1 Bowstring/tick)" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun craftBowstrings(isAfk: Boolean = false) {
        if (!isAfk && !canStartAfkOrHungerAction("Bowstring Crafting")) return
        viewModelScope.launch {
            // Optional consumption of flax / plant fiber if available in inventory or bank, otherwise spins free
            val fiberItem = (inventoryItems.value + bankItems.value).find { (it.id.contains("flax") || it.id.contains("fiber") || it.id.contains("wool") || it.id.contains("logs") || it.id == "item_logs") && it.quantity > 0 }
            if (fiberItem != null) {
                deductItemCombined(fiberItem.id, 1)
            }
            val bowstringItem = inventoryItems.value.find { it.id == "item_bowstring" }
            val newQty = (bowstringItem?.quantity ?: 0) + 1
            saveInventoryItem("item_bowstring", newQty)

            addXpAndNotify(
                skill = com.example.data.models.OsrsSkill.FLETCHING,
                amount = 30L,
                gpReward = 0L,
                logTitle = "Spun Bowstring",
                logDesc = "Spun 1x Bowstring on the spinning wheel (+30 Fletching XP)!"
            )
        }
    }

    fun craftHeadlessArrows(isAfk: Boolean = false) {
        viewModelScope.launch {
            val shaftCount = getItemQuantityCombined("item_arrow_shaft")
            val featherCount = getItemQuantityCombined("item_feather")
            if (shaftCount < 15 || featherCount < 15) {
                addChatMessage("🪶 Headless Arrows: You need at least 15x Arrow Shafts and 15x Feathers!")
                return@launch
            }
            deductItemCombined("item_arrow_shaft", 15)
            deductItemCombined("item_feather", 15)

            val curHeadless = inventoryItems.value.find { it.id == "item_headless_arrow" }
            val newQty = (curHeadless?.quantity ?: 0) + 15
            saveInventoryItem("item_headless_arrow", newQty)

            addXpAndNotify(
                skill = com.example.data.models.OsrsSkill.FLETCHING,
                amount = 35L,
                gpReward = 0L,
                logTitle = "Fletched Headless Arrows",
                logDesc = "Attached 15x Feathers to 15x Arrow Shafts (+35 Fletching XP)!"
            )
        }
    }

    fun toggleAfkArrowtipCrafting() {
        val hasBars = listOf("item_rune_bar", "item_adamant_bar", "item_mithril_bar", "item_steel_bar", "item_iron_bar", "item_bronze_bar").any { getItemQuantityCombined(it) > 0 }
            || (inventoryItems.value + bankItems.value).any { it.id.contains("bar") && it.quantity > 0 }
        if (!isAfkArrowtipCraftingActive.value && !hasBars) {
            addChatMessage("🗡️ Smithing Anvil: You need Metal Bars in your inventory or bank to forge Arrowtips!")
            return
        }
        val nextState = !isAfkArrowtipCraftingActive.value
        if (nextState && !canStartAfkOrHungerAction("Arrowtip Crafting")) return
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.ARROWTIP_CRAFTING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🗡️ AFK Arrowtip Forging: ${if (nextState) "ENABLED (1 Bar -> 15 Arrowtips)" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun craftBarsToArrowtips(isAfk: Boolean = false) {
        if (!isAfk && !canStartAfkOrHungerAction("Arrowtip Crafting")) return
        viewModelScope.launch {
            val selectedBarId = _selectedArrowtipBarId.value
            val preferredBar = if (selectedBarId.isNotEmpty() && getItemQuantityCombined(selectedBarId) > 0) selectedBarId else null
            val fallbackBar = listOf("item_rune_bar", "item_adamant_bar", "item_mithril_bar", "item_steel_bar", "item_iron_bar", "item_bronze_bar")
                .firstOrNull { getItemQuantityCombined(it) > 0 }
            val barId = preferredBar ?: fallbackBar

            if (barId == null) {
                if (isAfkArrowtipCraftingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🗡️ AFK Arrowtip Forging: Out of bars in inventory + bank!")
                    notifyAfkStopped("Arrowtip Crafting", "Out of metal bars in inventory + bank!")
                } else if (!isAfk) {
                    addChatMessage("🗡️ Smithing Anvil: You do not have any metal bars in your inventory or bank!")
                }
                return@launch
            }

            val barName = DefaultItems.ALL.find { it.id == barId }?.name ?: "Metal Bar"
            deductItemCombined(barId, 1)
            
            // Add generic arrowtip
            val tipItem = inventoryItems.value.find { it.id == "item_arrowtip" }
            val newQty = (tipItem?.quantity ?: 0) + 15
            saveInventoryItem("item_arrowtip", newQty)

            // Also add specific typed arrowtip
            val specificTipId = when (barId) {
                "item_bronze_bar" -> "item_bronze_arrowtip"
                "item_iron_bar" -> "item_iron_arrowtip"
                "item_steel_bar" -> "item_steel_arrowtip"
                "item_mithril_bar" -> "item_mithril_arrowtip"
                "item_adamant_bar" -> "item_adamant_arrowtip"
                "item_rune_bar" -> "item_rune_arrowtip"
                "item_dragon_bar" -> "item_dragon_arrowtip"
                else -> "item_bronze_arrowtip"
            }
            val curSpecific = inventoryItems.value.find { it.id == specificTipId }
            saveInventoryItem(specificTipId, (curSpecific?.quantity ?: 0) + 15)

            addXpAndNotify(
                skill = com.example.data.models.OsrsSkill.SMITHING,
                amount = 40L,
                gpReward = 0L,
                logTitle = "Forged Arrowtips",
                logDesc = "Forged 1x $barName into 15x Arrowtips (+40 Smithing XP)!"
            )
        }
    }

    fun toggleAfkFletching() {
        val hasSticks = getItemQuantityCombined("item_wooden_stick") > 0
        val hasTips = getItemQuantityCombined("item_arrowtip") > 0
        if (!isAfkFletchingActive.value && (!hasSticks || !hasTips)) {
            addChatMessage("🏹 Fletching Bench: You need both Wooden Sticks and Arrowtips in your inventory or bank!")
            return
        }
        val nextState = !isAfkFletchingActive.value
        if (nextState && !canStartAfkOrHungerAction("Fletching Arrows")) return
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.FLETCHING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🏹 AFK Fletching Arrows: ${if (nextState) "ENABLED (1 Stick + 1 Arrowtip -> 5 Arrows)" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun setSelectedCraftingTrapId(trapId: String) {
        val hunterXp = skillXpMap.value[OsrsSkill.HUNTER] ?: 0L
        val hunterLvl = OsrsXpCalculator.getLevelForXp(hunterXp)
        val reqLvl = when (trapId) {
            "item_box_trap" -> 27
            "item_net_trap" -> 29
            "item_impling_net" -> 48
            "item_magic_trap" -> 65
            else -> 1
        }
        if (hunterLvl < reqLvl) {
            addChatMessage("🔒 Cannot select trap: Requires Level $reqLvl Hunter (You are Level $hunterLvl)!")
            return
        }
        _selectedCraftingTrapId.value = trapId
        addChatMessage("🪤 Selected Trap Crafting: $trapId")
    }

    fun setSelectedSawmillPlankId(plankId: String) {
        _selectedSawmillPlankId.value = plankId
        val recipe = com.example.data.models.SawmillRecipes.findRecipeForPlank(plankId)
        val name = recipe?.plankName ?: "Standard Plank"
        val logReq = recipe?.logDisplayName ?: "Logs"
        addChatMessage("🪚 Selected Sawmill Recipe: $name (Milled from $logReq)")
    }

    fun setSelectedNailBarId(barId: String) {
        _selectedNailBarId.value = barId
        val name = when (barId) {
            "item_rune_bar" -> "Aetherite Nails"
            "item_adamant_bar" -> "Amethyst Nails"
            "item_mithril_bar" -> "Opalite Nails"
            "item_steel_bar" -> "Steel Nails"
            "item_iron_bar" -> "Iron Nails"
            else -> "Bronze Nails"
        }
        addChatMessage("🔨 Selected Nail Forging Recipe: $name")
    }

    fun setSelectedStickLogId(logId: String) {
        _selectedStickLogId.value = logId
        val name = when (logId) {
            "item_oak_logs" -> "Oak Logs"
            "item_willow_logs" -> "Willow Logs"
            "item_magic_logs" -> "Magic Logs"
            else -> "Logs"
        }
        addChatMessage("🥖 Selected Wood Stick Carving Recipe: $name")
    }

    fun setSelectedArrowtipBarId(barId: String) {
        _selectedArrowtipBarId.value = barId
        val name = when (barId) {
            "item_rune_bar" -> "Aetherite Arrowtips"
            "item_adamant_bar" -> "Amethyst Arrowtips"
            "item_mithril_bar" -> "Opalite Arrowtips"
            "item_steel_bar" -> "Steel Arrowtips"
            "item_iron_bar" -> "Iron Arrowtips"
            else -> "Bronze Arrowtips"
        }
        addChatMessage("🗡️ Selected Arrowtip Forging Recipe: $name")
    }

    fun setSelectedArrowFletchBarId(barId: String) {
        _selectedArrowFletchBarId.value = barId
        val name = when (barId) {
            "item_rune_bar" -> "Aetherite Arrows"
            "item_adamant_bar" -> "Amethyst Arrows"
            "item_mithril_bar" -> "Opalite Arrows"
            "item_steel_bar" -> "Steel Arrows"
            "item_iron_bar" -> "Iron Arrows"
            else -> "Bronze Arrows"
        }
        addChatMessage("🏹 Selected Arrow Fletching Recipe: $name")
    }

    fun toggleAfkTrapCrafting() {
        val trapId = _selectedCraftingTrapId.value
        val nextState = !isAfkTrapCraftingActive.value
        if (nextState) {
            if (!canStartAfkOrHungerAction("Hunter Trap Crafting")) return
            if (trapId.isNullOrEmpty()) {
                addChatMessage("⚠️ Cannot start AFK Trap Crafting: Please select a trap to craft first!")
                return
            }
            val hunterXp = skillXpMap.value[OsrsSkill.HUNTER] ?: 0L
            val hunterLvl = OsrsXpCalculator.getLevelForXp(hunterXp)
            val reqLvl = when (trapId) {
                "item_box_trap" -> 27
                "item_net_trap" -> 29
                "item_impling_net" -> 48
                "item_magic_trap" -> 65
                else -> 1
            }
            if (hunterLvl < reqLvl) {
                addChatMessage("🔒 Cannot start AFK Trap Crafting: Requires Level $reqLvl Hunter!")
                return
            }
            val logCandidateIds = listOf("item_logs", "item_oak_logs", "item_willow_logs", "item_maple_logs", "item_yew_logs", "item_magic_logs")
            val totalLogs = logCandidateIds.sumOf { getItemQuantityCombined(it) }
            val totalSticks = getItemQuantityCombined("item_wooden_stick")
            val hasMats = when (trapId) {
                "item_bird_snare", "item_noose_wand" -> totalLogs >= 1
                "item_box_trap" -> totalLogs >= 2
                "item_net_trap" -> totalLogs >= 1 && totalSticks >= 1
                "item_impling_net" -> totalSticks >= 2
                else -> true
            }
            if (!hasMats) {
                addChatMessage("🪤 Trap Crafting: You don't have the required materials in inventory or bank to start AFK!")
                return
            }
            stopAllAfkStations()
        } else {
            stopAllAfkStations()
        }
        if (nextState) AfkEngine.startActivity(AfkActivityType.TRAP_CRAFTING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        val trapName = when (trapId) {
            "item_bird_snare" -> "Bird Snares"
            "item_box_trap" -> "Box Traps"
            "item_net_trap" -> "Net Trap Gear"
            "item_noose_wand" -> "Noose Wands"
            "item_impling_net" -> "Impling Nets"
            else -> "Traps"
        }
        addChatMessage("🪤 AFK Trap Crafting ($trapName) is now ${if (nextState) "RUNNING" else "STOPPED"}")
        saveAfkStateToPrefs()
    }

    fun fletchSticksToArrows(isAfk: Boolean = false) {
        viewModelScope.launch {
            val stickQty = getItemQuantityCombined("item_wooden_stick")
            val tipQty = getItemQuantityCombined("item_arrowtip")
            if (stickQty <= 0 || tipQty <= 0) {
                if (isAfkFletchingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🏹 AFK Fletching: Out of sticks or arrowtips in inventory + bank!")
                    notifyAfkStopped("Fletching", "Out of sticks or arrowtips in inventory + bank!")
                } else if (!isAfk) {
                    addChatMessage("🏹 Fletching Bench: Requires 1 Wooden Stick and 1 Arrowtip in inventory + bank!")
                }
                return@launch
            }

            deductItemCombined("item_wooden_stick", 1)
            deductItemCombined("item_arrowtip", 1)

            val arrowItem = inventoryItems.value.find { it.id == "item_bronze_arrows" }
            val newQty = (arrowItem?.quantity ?: 0) + 5
            saveInventoryItem("item_bronze_arrows", newQty)

            addXpAndNotify(
                skill = com.example.data.models.OsrsSkill.FLETCHING,
                amount = 35L,
                gpReward = 0L,
                logTitle = "Fletched Bronze Arrows",
                logDesc = "Combined 1 Stick & 1 Arrowtip into 5x Bronze Arrows (+35 Fletching XP)!"
            )
            progressSkillContract(OsrsSkill.FLETCHING, 5, "item_bronze_arrows")
        }
    }

    // --- FLETCHING RECIPE METHODS ---

    fun toggleAfkFletching(recipe: FletchRecipe? = null) {
        if (recipe != null) {
            _activeFletchRecipe.value = recipe
        }
        val nextState = !isAfkFletchingActive.value
        if (nextState && !canStartAfkOrHungerAction("Fletching")) return
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.FLETCHING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        val targetName = _activeFletchRecipe.value?.name ?: "Arrows"
        addChatMessage("🏹 AFK Fletching ($targetName) is now ${if (nextState) "ENABLED" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun fletchRecipe(recipe: FletchRecipe, isAfk: Boolean = false) {
        if (!isAfk) {
            if (!canStartAfkOrHungerAction("Fletching: ${recipe.name}")) return
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Fletching.")
            }
        }
        viewModelScope.launch {
            val fletchXp = skillXpMap.value[OsrsSkill.FLETCHING] ?: 0L
            val fletchLvl = OsrsXpCalculator.getLevelForXp(fletchXp)
            if (fletchLvl < recipe.reqLevel) {
                if (isAfkFletchingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🎯 AFK Fletching: Level ${recipe.reqLevel} required!")
                } else {
                    addChatMessage("🔒 Cannot fletch ${recipe.name}: Requires Level ${recipe.reqLevel} Fletching!")
                }
                return@launch
            }

            var missingMaterial = false
            for (mat in recipe.inputMaterials) {
                if (getItemQuantityCombined(mat.itemId) < mat.quantity) {
                    missingMaterial = true
                    break
                }
            }

            if (missingMaterial) {
                if (isAfkFletchingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🏹 AFK Fletching: Out of materials in inventory + bank!")
                    notifyAfkStopped("Fletching", "Out of materials in inventory + bank!")
                } else if (!isAfk) {
                    addChatMessage("🏹 Fletching Bench: You are missing required items in inventory + bank!")
                }
                return@launch
            }

            for (mat in recipe.inputMaterials) {
                deductItemCombined(mat.itemId, mat.quantity)
            }

            val outItem = inventoryItems.value.find { it.id == recipe.outputItemId }
            val newQty = (outItem?.quantity ?: 0) + recipe.outputQuantity
            saveInventoryItem(recipe.outputItemId, newQty)

            addXpAndNotify(
                skill = OsrsSkill.FLETCHING,
                amount = recipe.xpReward,
                gpReward = 0L,
                logTitle = "Fletched ${recipe.name}",
                logDesc = "Crafted ${recipe.outputQuantity}x ${recipe.outputItemName} (+${recipe.xpReward} XP)!"
            )
            progressSkillContract(OsrsSkill.FLETCHING, recipe.outputQuantity, recipe.outputItemId)
        }
    }

    // --- SMITHING RECIPE METHODS ---

    fun toggleAfkSmelting(recipe: SmeltRecipe? = null) {
        if (recipe != null) {
            _activeSmeltRecipe.value = recipe
            _selectedBarId.value = recipe.barItemId
        }
        val nextState = !isAfkSmeltingActive.value
        if (nextState) {
            val currentRecipe = _activeSmeltRecipe.value ?: SmithingData.SMELT_RECIPES.firstOrNull { r ->
                val smithXp = skillXpMap.value[OsrsSkill.SMITHING] ?: 0L
                val lvl = OsrsXpCalculator.getLevelForXp(smithXp)
                lvl >= r.reqLevel && r.inputOres.all { ore -> getItemQuantityCombined(ore.itemId) >= ore.quantity }
            } ?: SmithingData.SMELT_RECIPES.first()
            _activeSmeltRecipe.value = currentRecipe
            _selectedBarId.value = currentRecipe.barItemId

            val smithXp = skillXpMap.value[OsrsSkill.SMITHING] ?: 0L
            val smithLvl = OsrsXpCalculator.getLevelForXp(smithXp)
            if (smithLvl < currentRecipe.reqLevel) {
                addChatMessage("🔒 Cannot start AFK Smelting: Requires Level ${currentRecipe.reqLevel} Smithing!")
                return
            }
            val hasOres = currentRecipe.inputOres.all { ore -> getItemQuantityCombined(ore.itemId) >= ore.quantity }
            if (!hasOres) {
                val totalOres = (inventoryItems.value + bankItems.value).any { (it.id.endsWith("_ore") || it.id.contains("ore")) && it.quantity > 0 }
                if (!totalOres) {
                    stopAllAfkStations()
                    addChatMessage("🔥 Furnace: Cannot start! No ores in inventory or bank to smelt.")
                    return
                }
            }
            stopAllAfkStations()
            AfkEngine.startActivity(AfkActivityType.SMELTING, pohPrefs)
            addChatMessage("🔥 AFK Smelting (${currentRecipe.barName}) is now ENABLED")
        } else {
            stopAllAfkStations()
            AfkEngine.stopAll(pohPrefs)
            addChatMessage("🔥 AFK Smelting is now DISABLED")
        }
        saveAfkStateToPrefs()
    }

    fun smeltRecipe(recipe: SmeltRecipe, isAfk: Boolean = false) {
        if (!isAfk) {
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Smelting.")
            }
        }
        viewModelScope.launch {
            val smithXp = skillXpMap.value[OsrsSkill.SMITHING] ?: 0L
            val smithLvl = OsrsXpCalculator.getLevelForXp(smithXp)
            if (smithLvl < recipe.reqLevel) {
                if (isAfkSmeltingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔥 AFK Smelting: Requires Level ${recipe.reqLevel} Smithing!")
                } else {
                    addChatMessage("🔒 Cannot smelt ${recipe.barName}: Requires Level ${recipe.reqLevel} Smithing!")
                }
                return@launch
            }

            var missingOres = false
            for (ore in recipe.inputOres) {
                if (getItemQuantityCombined(ore.itemId) < ore.quantity) {
                    missingOres = true
                    break
                }
            }

            if (missingOres) {
                if (isAfkSmeltingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔥 AFK Smelting: Out of ores in inventory + bank!")
                    notifyAfkStopped("Smelting", "Out of ores in inventory + bank!")
                } else if (!isAfk) {
                    addChatMessage("🔥 Smelting Furnace: Missing required ores in inventory + bank!")
                }
                return@launch
            }

            for (ore in recipe.inputOres) {
                deductItemCombined(ore.itemId, ore.quantity, ore.itemName)
            }

            val barItem = inventoryItems.value.find { it.id == recipe.barItemId }
            val newQty = (barItem?.quantity ?: 0) + recipe.outputQuantity
            saveInventoryItem(recipe.barItemId, newQty)

            addXpAndNotify(
                skill = OsrsSkill.SMITHING,
                amount = recipe.xpReward,
                gpReward = 0L,
                logTitle = "Smelted ${recipe.barName}",
                logDesc = "Furnace forged ${recipe.outputQuantity}x ${recipe.barName} (+${recipe.xpReward} Smithing XP)!"
            )
            progressSkillContract(OsrsSkill.SMITHING, recipe.outputQuantity, recipe.barItemId)
        }
    }

    fun toggleAfkSmithingAnvil(recipe: SmithAnvilRecipe? = null) {
        if (recipe != null) {
            _activeSmithAnvilRecipe.value = recipe
        }
        val nextState = !isAfkSmithingAnvilActive.value
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.SMITHING_ANVIL, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        val targetName = _activeSmithAnvilRecipe.value?.name ?: "Anvil Gear"
        addChatMessage("🔨 AFK Anvil Smithing ($targetName) is now ${if (nextState) "ENABLED" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun smithAnvilRecipe(recipe: SmithAnvilRecipe, isAfk: Boolean = false) {
        if (!isAfk) {
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Smithing.")
            }
        }
        viewModelScope.launch {
            val smithXp = skillXpMap.value[OsrsSkill.SMITHING] ?: 0L
            val smithLvl = OsrsXpCalculator.getLevelForXp(smithXp)
            if (smithLvl < recipe.reqLevel) {
                if (isAfkSmithingAnvilActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔨 AFK Anvil Smithing: Requires Level ${recipe.reqLevel} Smithing!")
                } else {
                    addChatMessage("🔒 Cannot smith ${recipe.name}: Requires Level ${recipe.reqLevel} Smithing!")
                }
                return@launch
            }

            val barQty = getItemQuantityCombined(recipe.barItemId)
            if (barQty < recipe.barsRequired) {
                if (isAfkSmithingAnvilActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔨 AFK Anvil Smithing: Out of metal bars in inventory + bank!")
                    notifyAfkStopped("Anvil Smithing", "Out of metal bars in inventory + bank!")
                } else if (!isAfk) {
                    addChatMessage("🔨 Anvil Smithing: You need ${recipe.barsRequired}x bars in inventory + bank!")
                }
                return@launch
            }

            deductItemCombined(recipe.barItemId, recipe.barsRequired)

            val outItem = inventoryItems.value.find { it.id == recipe.outputItemId }
            val newQty = (outItem?.quantity ?: 0) + recipe.outputQuantity
            saveInventoryItem(recipe.outputItemId, newQty)

            addXpAndNotify(
                skill = OsrsSkill.SMITHING,
                amount = recipe.xpReward,
                gpReward = 0L,
                logTitle = "Smithed ${recipe.name}",
                logDesc = "Anvil forged ${recipe.outputQuantity}x ${recipe.outputItemName} (+${recipe.xpReward} Smithing XP)!"
            )
            progressSkillContract(OsrsSkill.SMITHING, recipe.outputQuantity, recipe.outputItemId)
        }
    }

    // --- HERBLORE RECIPE METHODS (PESTLE & MORTAR CRUSHING & BREWING) ---

    fun toggleAfkHerbCrushing(recipe: HerbCrushingRecipe? = null) {
        if (recipe != null) {
            _activeCrushHerbRecipe.value = recipe
        }
        val nextState = !isAfkHerbCrushingActive.value
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.HERB_CRUSHING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        val targetName = _activeCrushHerbRecipe.value?.crushedHerbName ?: "Herbs"
        addChatMessage("🥣 AFK Herb Crushing ($targetName) is now ${if (nextState) "ENABLED" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun toggleAfkHerbCleaning(recipe: HerbCleaningRecipe? = null) {
        toggleAfkHerbCrushing(recipe)
    }

    fun crushHerbRecipe(recipe: HerbCrushingRecipe, isAfk: Boolean = false) {
        if (!isAfk) {
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Herb Grinding.")
            }
        }
        viewModelScope.launch {
            val herbXp = skillXpMap.value[OsrsSkill.HERBLORE] ?: 0L
            val herbLvl = OsrsXpCalculator.getLevelForXp(herbXp)
            if (herbLvl < recipe.reqLevel) {
                if (isAfkHerbCrushingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🥣 AFK Herb Crushing: Requires Level ${recipe.reqLevel} Herblore!")
                } else {
                    addChatMessage("🔒 Cannot grind ${recipe.herbName}: Requires Level ${recipe.reqLevel} Herblore!")
                }
                return@launch
            }

            val herbQty = getItemQuantityCombined(recipe.herbId)
            if (herbQty < 1) {
                if (isAfkHerbCrushingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🥣 AFK Herb Crushing: Out of ${recipe.herbName} in inventory + bank!")
                    notifyAfkStopped("Herb Crushing", "Out of ${recipe.herbName} in inventory + bank!")
                } else if (!isAfk) {
                    addChatMessage("🥣 Herblore: You do not have any ${recipe.herbName} in inventory + bank!")
                }
                return@launch
            }

            deductItemCombined(recipe.herbId, 1)

            val crushedItem = inventoryItems.value.find { it.id == recipe.crushedHerbId }
            val newQty = (crushedItem?.quantity ?: 0) + 1
            saveInventoryItem(recipe.crushedHerbId, newQty)

            addXpAndNotify(
                skill = OsrsSkill.HERBLORE,
                amount = recipe.xpReward,
                gpReward = 0L,
                logTitle = "Ground ${recipe.crushedHerbName}",
                logDesc = "Ground 1x ${recipe.herbName} into ${recipe.crushedHerbName} (+${recipe.xpReward} Herblore XP)!"
            )
            progressSkillContract(OsrsSkill.HERBLORE, 1, recipe.crushedHerbId)
        }
    }

    fun cleanHerbRecipe(recipe: HerbCleaningRecipe, isAfk: Boolean = false) {
        crushHerbRecipe(recipe, isAfk)
    }

    fun toggleAfkPotionBrewing(recipe: PotionBrewRecipe? = null) {
        if (recipe != null) {
            _activePotionRecipe.value = recipe
        }
        val nextState = !isAfkPotionBrewingActive.value
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.POTION_BREWING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        val targetName = _activePotionRecipe.value?.name ?: "Potions"
        addChatMessage("🧪 AFK Potion Brewing ($targetName) is now ${if (nextState) "ENABLED" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun brewPotionRecipe(recipe: PotionBrewRecipe, isAfk: Boolean = false) {
        if (!isAfk) {
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Potion Brewing.")
            }
        }
        viewModelScope.launch {
            val herbXp = skillXpMap.value[OsrsSkill.HERBLORE] ?: 0L
            val herbLvl = OsrsXpCalculator.getLevelForXp(herbXp)
            if (herbLvl < recipe.reqLevel) {
                if (isAfkPotionBrewingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🧪 AFK Potion Brewing: Requires Level ${recipe.reqLevel} Herblore!")
                } else {
                    addChatMessage("🔒 Cannot brew ${recipe.name}: Requires Level ${recipe.reqLevel} Herblore!")
                }
                return@launch
            }

            val crushedQty = getItemQuantityCombined(recipe.crushedHerbId)
            val cleanQty = getItemQuantityCombined(recipe.cleanHerbId)
            val secQty = getItemQuantityCombined(recipe.secondaryItemId)

            val effectiveHerbId = if (crushedQty > 0) recipe.crushedHerbId else if (cleanQty > 0) recipe.cleanHerbId else null

            if (effectiveHerbId == null || secQty < 1) {
                if (isAfkPotionBrewingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🧪 AFK Potion Brewing: Out of ingredients in inventory + bank!")
                    notifyAfkStopped("Potion Brewing", "Out of ingredients in inventory + bank!")
                } else if (!isAfk) {
                    addChatMessage("🧪 Herblore Cauldron: Missing ${recipe.crushedHerbName} or ${recipe.secondaryItemName} in inventory + bank!")
                }
                return@launch
            }

            deductItemCombined(effectiveHerbId, 1)
            deductItemCombined(recipe.secondaryItemId, 1)

            val potionItem = inventoryItems.value.find { it.id == recipe.outputPotionId }
            val newQty = (potionItem?.quantity ?: 0) + 1
            saveInventoryItem(recipe.outputPotionId, newQty)

            addXpAndNotify(
                skill = OsrsSkill.HERBLORE,
                amount = recipe.xpReward,
                gpReward = 0L,
                logTitle = "Brewed ${recipe.name}",
                logDesc = "Brewed 1x ${recipe.outputPotionName} (+${recipe.xpReward} Herblore XP)!"
            )
            progressSkillContract(OsrsSkill.HERBLORE, 1, recipe.outputPotionId)
        }
    }

    // --- Universal Item Obtainment Inspector ---

    fun inspectItemObtain(itemId: String) {
        viewModelScope.launch {
            val encItem = com.example.data.models.EncyclopediaDatabase.getEncyclopediaItem(
                itemId = itemId,
                inventoryItems = inventoryItems.value,
                bankItems = bankItems.value,
                equippedItems = equippedItems.value
            )
            _inspectedObtainItem.value = encItem
        }
    }

    fun clearInspectedObtainItem() {
        _inspectedObtainItem.value = null
    }

    // --- Equipment Management Methods ---

    fun equipItem(item: com.example.data.models.InventoryItem) {
        val slot = item.equipmentSlot ?: DefaultItems.getItemById(item.id).equipmentSlot ?: com.example.data.models.EquipmentSlot.WEAPON
        viewModelScope.launch {
            isAutoEquippingOrEquipping = true
            try {
                val currentEquipped = equippedItems.value[slot]
                if (currentEquipped != null) {
                    val existingInBag = inventoryItems.value.find { it.id == currentEquipped.id }
                    val existingQty = existingInBag?.quantity ?: 0
                    saveInventoryItem(currentEquipped.id, existingQty + 1)
                }

                val totalAvailable = getItemQuantityCombined(item.id)
                if (totalAvailable <= 0) {
                    addChatMessage("⚠️ You do not have ${item.name} in your backpack or bank!")
                    return@launch
                }

                deductItemCombined(item.id, 1)

                equipItem(slot, item)
                addChatMessage("🛡️ Equipped ${item.name} (${slot.displayName} Slot) to ${petState.value.customName}!")
            } finally {
                isAutoEquippingOrEquipping = false
            }
        }
    }

    fun unequipItem(slot: com.example.data.models.EquipmentSlot) {
        val current = equippedItems.value[slot] ?: return
        viewModelScope.launch {
            isAutoEquippingOrEquipping = true
            try {
                val existingInBag = inventoryItems.value.find { it.id == current.id }
                val existingQty = existingInBag?.quantity ?: 0
                saveInventoryItem(current.id, existingQty + 1)

                unequipSlot(slot)
                addChatMessage("📦 Unequipped ${current.name} from ${petState.value.customName}.")
            } finally {
                isAutoEquippingOrEquipping = false
            }
        }
    }

    fun autoEquipSkillOutfit(skill: com.example.data.models.OsrsSkill) {
        val set = com.example.data.models.SkillOutfitData.getSetForSkill(skill)
        if (set == null) {
            addChatMessage("⚠️ No skill outfit found for ${skill.displayName}.")
            return
        }
        val unlockedOutfits = petState.value.unlockedOutfitIds.toSet()
        val unlockedCount = set.pieces.count { unlockedOutfits.contains(it.id) }
        val totalCount = set.pieces.size
        val bonusPct = unlockedCount * 5
        addChatMessage("✨ ${set.setName} Outfit: $unlockedCount/$totalCount pieces unlocked (+${bonusPct}% ${skill.displayName} XP passive active!). Skill outfits do not need to be equipped and take 0 storage.")
    }

    // --- Slayer & Hunter Methods ---

    fun setSelectedSlayerMonster(monster: com.example.data.models.SlayerMonster) {
        _selectedSlayerMonster.value = monster
        saveAfkStateToPrefs()
    }

    fun setSelectedHunterCreature(creature: com.example.data.models.HunterCreature) {
        _selectedHunterCreature.value = creature
        saveAfkStateToPrefs()
    }

    fun setSelectedCombatStyle(style: com.example.data.models.CombatStyle) {
        _selectedCombatStyle.value = style
        addChatMessage("⚔️ Combat Style changed to ${style.displayName} (${style.description})!")
    }

    fun toggleAfkSlayer(monster: com.example.data.models.SlayerMonster) {
        _selectedSlayerMonster.value = monster
        val nextState = !isAfkSlayerActive.value
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.SLAYER, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("💀 AFK Slayer Task on ${monster.name} is now ${if (nextState) "ENABLED" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun toggleAfkHunter(creature: com.example.data.models.HunterCreature) {
        _selectedHunterCreature.value = creature
        val nextState = !isAfkHunterActive.value
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.HUNTER, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🐾 AFK Hunter Trapping on ${creature.name} is now ${if (nextState) "ENABLED" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun fightSlayerMonster(monster: com.example.data.models.SlayerMonster, isAfk: Boolean = false) {
        if (!isAfk) {
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Slayer Combat.")
            }
        }
        val currentPet = petState.value
        val slayerXp = skillXpMap.value[com.example.data.models.OsrsSkill.SLAYER] ?: 0L
        val slayerLvl = com.example.data.models.OsrsXpCalculator.getLevelForXp(slayerXp)

        if (slayerLvl < monster.reqSlayerLevel) {
            if (!isAfk) {
                addChatMessage("🔒 Slayer Level ${monster.reqSlayerLevel} required to fight ${monster.name}! Current level: $slayerLvl.")
            }
            AfkEngine.stopAll(pohPrefs)
            return
        }

        if (monster.reqQuestId != null && !currentPet.completedQuestIds.contains(monster.reqQuestId)) {
            if (!isAfk) {
                addChatMessage("🔒 Requires quest completion: '${monster.reqQuestName ?: monster.reqQuestId}' to slay ${monster.name}!")
            }
            AfkEngine.stopAll(pohPrefs)
            return
        }

        // Health check: cannot fight with 0 HP
        if (currentPet.health <= 0) {
            if (isAfk) {
                AfkEngine.stopAll(pohPrefs)
                addChatMessage("⚠️ AFK Slayer Stopped: ${currentPet.customName} has 0 HP! Feed food from inventory to heal!")
                notifyAfkStopped("Slayer Combat", "Pet has 0 HP!")
            } else {
                addChatMessage("⚠️ ${currentPet.customName} is passed out with 0 HP! Feed food to heal before battle.")
            }
            return
        }

        // Ranged equipment & Magic rune checks
        val currentStyle = selectedCombatStyle.value
        val arrowInInvToConsume: String?
        var activeMagicSpellToConsume: com.example.data.models.MagicSpell? = null

        if (currentStyle == com.example.data.models.CombatStyle.RANGED) {
            val weaponEquipped = equippedItems.value[com.example.data.models.EquipmentSlot.WEAPON]
            val ammoEquipped = equippedItems.value[com.example.data.models.EquipmentSlot.AMMO]

            val hasBow = (weaponEquipped != null && (weaponEquipped.id.contains("bow") || weaponEquipped.name.contains("bow", ignoreCase = true))) ||
                    (inventoryItems.value + bankItems.value).any { (it.id.contains("bow") || it.name.contains("bow", ignoreCase = true)) && it.quantity > 0 }

            if (!hasBow) {
                if (isAfk) AfkEngine.stopAll(pohPrefs)
                addChatMessage("🏹 Ranged Attack Failed: You need a bow equipped or in your inventory/bank!")
                return
            }

            val arrowCandidate = listOf("item_rune_arrows", "item_adamant_arrows", "item_mithril_arrows", "item_steel_arrows", "item_iron_arrows", "item_bronze_arrows", "item_arrows")
                .firstOrNull { getItemQuantityCombined(it) > 0 }
                ?: (inventoryItems.value + bankItems.value).firstOrNull { (it.id.contains("arrow") || it.name.contains("arrow", ignoreCase = true)) && it.quantity > 0 }?.id

            val hasArrows = (ammoEquipped != null && (ammoEquipped.id.contains("arrow") || ammoEquipped.name.contains("arrow", ignoreCase = true))) || arrowCandidate != null

            if (!hasArrows) {
                if (isAfk) AfkEngine.stopAll(pohPrefs)
                addChatMessage("🏹 Ranged Attack Failed: You need arrows for ammo in your inventory, bank, or ammo slot!")
                return
            }

            arrowInInvToConsume = arrowCandidate
        } else {
            arrowInInvToConsume = null
        }

        if (currentStyle == com.example.data.models.CombatStyle.MAGIC && activeCombatSpellId.value != null) {
            val spell = MagicData.SPELLS.find { it.id == activeCombatSpellId.value }
            if (spell != null && spell.spellType == com.example.data.models.SpellType.COMBAT) {
                // Check if player has runes for spell
                var missingRunes = false
                for (req in spell.runes) {
                    if (getItemQuantityCombined(req.runeItemId) < req.quantity) {
                        missingRunes = true
                        break
                    }
                }
                if (missingRunes) {
                    if (isAfk) AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🪄 Magic Attack Failed: Out of runes for autocasting ${spell.name}! Select another spell or craft runes.")
                    return
                }
                activeMagicSpellToConsume = spell
            }
        }

        val baseHpXp = monster.xpReward
        val helmetBonusPercent = if (equippedItems.value.containsKey(com.example.data.models.EquipmentSlot.HEAD) && equippedItems.value[com.example.data.models.EquipmentSlot.HEAD]?.id == "item_slayer_helmet") 15 else 0
        val totalSlayerXp = baseHpXp + (baseHpXp * helmetBonusPercent / 100)

        // Calculate combat damage taken based on monster strength and pet defence
        val defXp = skillXpMap.value[com.example.data.models.OsrsSkill.DEFENCE] ?: 0L
        val defLvl = com.example.data.models.OsrsXpCalculator.getLevelForXp(defXp)
        val enemyLevel = monster.reqSlayerLevel + 5
        var damageTaken = ((enemyLevel * 3) / (defLvl + 10)).coerceIn(1, monster.maxHp.coerceAtMost(25))

        // Ice spell freeze effect reduces monster damage taken by 50%
        if (activeMagicSpellToConsume != null && (activeMagicSpellToConsume.id.contains("ice") || activeMagicSpellToConsume.name.contains("Ice", ignoreCase = true))) {
            damageTaken = (damageTaken / 2).coerceAtLeast(1)
        }

        var newHealth = (currentPet.health - damageTaken).coerceAtLeast(0)

        // Blood spell siphons lifeforce to heal pet HP
        if (activeMagicSpellToConsume != null && (activeMagicSpellToConsume.id.contains("blood") || activeMagicSpellToConsume.name.contains("Blood", ignoreCase = true))) {
            newHealth = (newHealth + 20).coerceAtMost(100)
        }

        viewModelScope.launch {
            // Consume 1 arrow for ammo
            if (arrowInInvToConsume != null) {
                deductItemCombined(arrowInInvToConsume, 1)
            }

            // Consume runes for active magic spell
            if (activeMagicSpellToConsume != null) {
                for (req in activeMagicSpellToConsume.runes) {
                    deductItemCombined(req.runeItemId, req.quantity)
                }
                addXpAndNotify(
                    skill = com.example.data.models.OsrsSkill.MAGIC,
                    amount = activeMagicSpellToConsume.xpReward,
                    gpReward = 0L,
                    logTitle = "Autocast ${activeMagicSpellToConsume.name}",
                    logDesc = "Magic spell boosted Slayer combat against ${monster.name}!"
                )
                addChatMessage("🪄 Autocasting ${activeMagicSpellToConsume.iconEmoji} ${activeMagicSpellToConsume.name}! (+${activeMagicSpellToConsume.xpReward} Magic XP)")
            }

            // Save updated pet health
            val updatedPet = currentPet.copy(health = newHealth)
            repository.savePetState(updatedPet)

            addChatMessage("💔 ${monster.name} hit ${currentPet.customName} for $damageTaken damage! (HP: $newHealth/${getPetMaxHealth()})")

            if (newHealth <= 0) {
                AfkEngine.stopAll(pohPrefs)
                addChatMessage("💀 ${currentPet.customName} was knocked out in battle by ${monster.name}! Eat food to restore HP.")
                notifyAfkStopped("Slayer Combat", "${currentPet.customName} was knocked out in battle!")
                return@launch
            }

            // Award Slayer XP
            addXpAndNotify(
                skill = com.example.data.models.OsrsSkill.SLAYER,
                amount = totalSlayerXp,
                gpReward = 0L,
                logTitle = "Defeated ${monster.name}",
                logDesc = "Slain ${monster.name} on Slayer task!"
            )
            progressSkillContract(OsrsSkill.SLAYER, 1, monster.id)

            // Track Slayer Contract progress
            val activeSlayerContract = _slayerContract.value
            if (activeSlayerContract.currentKills < activeSlayerContract.targetKills) {
                if (monster.id == activeSlayerContract.monsterId || monster.name.equals(activeSlayerContract.monsterName, ignoreCase = true)) {
                    val theronLvl = npcFavorMap.value["theron"]?.first ?: getNpcFavorLevel("theron")
                    var taskIncr = 1
                    if ((1..100).random() <= theronLvl) {
                        taskIncr = 2
                        addChatMessage("✨ [Theron's Favor Perk (+${theronLvl}%)]: Double task progress! (+2 ${monster.name} kills credited) 💀⚔️")
                    }
                    val newKills = (activeSlayerContract.currentKills + taskIncr).coerceAtMost(activeSlayerContract.targetKills)
                    _slayerContract.value = activeSlayerContract.copy(currentKills = newKills)
                    addChatMessage("⚔️ SLAYER TASK PROGRESS: $newKills / ${activeSlayerContract.targetKills} ${monster.name} slain!")
                    if (newKills >= activeSlayerContract.targetKills) {
                        addChatMessage("🎉 SLAYER TASK READY FOR TURN-IN! Return to Slayer Master in Contracts tab to claim your reward!")
                    }
                }
            }

            // Distribute Combat XP based on selected Combat Style & Defensive Stance
            val style = selectedCombatStyle.value
            val isDef = isDefensiveCombatMode.value
            val totalCombatXp = monster.maxHp * 4L

            val primarySkill = when (style) {
                com.example.data.models.CombatStyle.ATTACK -> com.example.data.models.OsrsSkill.ATTACK
                com.example.data.models.CombatStyle.RANGED -> com.example.data.models.OsrsSkill.RANGED
                com.example.data.models.CombatStyle.MAGIC -> com.example.data.models.OsrsSkill.MAGIC
                com.example.data.models.CombatStyle.DEFENCE -> com.example.data.models.OsrsSkill.DEFENCE
            }

            if (isDef && primarySkill != com.example.data.models.OsrsSkill.DEFENCE) {
                val primaryXp = totalCombatXp / 2L
                val defXp = totalCombatXp - primaryXp
                addXpAndNotify(
                    skill = primarySkill,
                    amount = primaryXp,
                    gpReward = 0L,
                    logTitle = "Combat with ${monster.name}",
                    logDesc = "Trained ${primarySkill.displayName} (Defensive stance) defeating ${monster.name}."
                )
                addXpAndNotify(
                    skill = com.example.data.models.OsrsSkill.DEFENCE,
                    amount = defXp,
                    gpReward = 0L,
                    logTitle = "Combat with ${monster.name}",
                    logDesc = "Trained Defence (Defensive stance) defeating ${monster.name}."
                )
            } else {
                addXpAndNotify(
                    skill = primarySkill,
                    amount = totalCombatXp,
                    gpReward = 0L,
                    logTitle = "Combat with ${monster.name}",
                    logDesc = "Trained ${primarySkill.displayName} defeating ${monster.name}."
                )
            }

            // Hitpoints XP
            addXpAndNotify(
                skill = com.example.data.models.OsrsSkill.HITPOINTS,
                amount = (monster.maxHp * 1.33).toLong(),
                gpReward = 0L,
                logTitle = "Hitpoints XP",
                logDesc = "Gained Hitpoints fighting ${monster.name}."
            )

            // Build inventory tracking map to prevent asynchronous state flow overwrites
            val currentInvMap = inventoryItems.value.associate { it.id to it.quantity }.toMutableMap()

            // Guaranteed Bone Drop for Every Creature Slain! (Exactly 1 bone per monster)
            val slayerBoneId = when {
                monster.name.contains("Dragon", ignoreCase = true) || monster.reqSlayerLevel >= 75 -> "item_dragon_bones"
                monster.name.contains("Giant", ignoreCase = true) || monster.name.contains("Demon", ignoreCase = true) || monster.reqSlayerLevel >= 30 -> "item_big_bones"
                else -> "item_bones"
            }
            val bonePreset = DefaultItems.ALL.find { it.id == slayerBoneId }
            val boneName = bonePreset?.name ?: "Bones"
            val currentBoneCount = currentInvMap[slayerBoneId] ?: 0
            val newBoneCount = currentBoneCount + 1
            currentInvMap[slayerBoneId] = newBoneCount
            saveInventoryItem(slayerBoneId, newBoneCount)
            addChatMessage("🦴 BONE DROP: ${monster.name} dropped 1x $boneName!")

            // Loot Drops (Exclude duplicate bone drops from drop table so exactly 1 bone drops per monster)
            monster.drops.filter { !it.itemId.contains("bones") }.forEach { drop ->
                val roll = kotlin.random.Random.nextInt(1, 101)
                if (roll <= drop.chancePercent) {
                    val qty = kotlin.random.Random.nextInt(drop.minQty, drop.maxQty + 1)
                    if (drop.itemId.startsWith("item_coins")) {
                        val gpAmount = qty * 10L
                        val gpPet = petState.value.copy(coinsGp = petState.value.coinsGp + gpAmount)
                        repository.savePetState(gpPet)
                        addChatMessage("🪙 LOOT: Found $gpAmount GP from ${monster.name}!")
                    } else {
                        val currentQty = currentInvMap[drop.itemId] ?: 0
                        val updatedQty = currentQty + qty
                        currentInvMap[drop.itemId] = updatedQty
                        saveInventoryItem(drop.itemId, updatedQty)
                        addChatMessage("✨ DROP: ${monster.name} dropped $qty x ${drop.itemName} ${drop.iconEmoji}!")
                    }
                }
            }
        }
    }

    // --- Boss Combat & AFK Slaying ---

    fun toggleAfkBoss(boss: com.example.data.models.BossMonster) {
        _selectedBossMonster.value = boss
        val nextState = !isAfkBossActive.value
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.BOSS, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("⚔️ AFK Boss Slaying on ${boss.name} is now ${if (nextState) "ENABLED" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun calculatePetCombatLevel(): Int {
        return com.example.data.models.CombatManager.calculateCombatLevel(skillXpMap.value)
    }

    fun calculateBossKillTime(boss: com.example.data.models.BossMonster): Int {
        val combatLvl = calculatePetCombatLevel()
        val totalGearPower = equippedItems.value.values.sumOf { it.combatPowerBonus }

        val baseTime = boss.baseKillTimeSeconds.toFloat()
        val lvlFactor = ((combatLvl - 20).coerceAtLeast(0).toFloat() / 106f).coerceIn(0f, 1f) * 0.40f
        val gearFactor = (totalGearPower.toFloat() / 150f).coerceIn(0f, 1f) * 0.35f

        // Magic active spell bonus
        val magicSpellFactor = if (selectedCombatStyle.value == com.example.data.models.CombatStyle.MAGIC && activeCombatSpellId.value != null) {
            val spell = MagicData.SPELLS.find { it.id == activeCombatSpellId.value }
            if (spell != null) (spell.reqMagicLevel.toFloat() / 100f).coerceIn(0.15f, 0.35f) else 0.15f
        } else 0f

        val totalReduction = (lvlFactor + gearFactor + magicSpellFactor).coerceIn(0f, 0.75f)
        return (baseTime * (1f - totalReduction)).toInt().coerceAtLeast(8)
    }

    fun fightBossOnce(boss: com.example.data.models.BossMonster, isAfk: Boolean = false) {
        if (!isAfk) {
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Boss Encounter.")
            }
        }
        val currentPet = petState.value
        val combatLvl = calculatePetCombatLevel()

        if (combatLvl < boss.reqCombatLevel) {
            if (!isAfk) {
                addChatMessage("🔒 Combat Level ${boss.reqCombatLevel} required to fight ${boss.name}! (Current: $combatLvl)")
            }
            AfkEngine.stopAll(pohPrefs)
            return
        }

        if (boss.reqSlayerLevel > 0) {
            val slayerXp = skillXpMap.value[com.example.data.models.OsrsSkill.SLAYER] ?: 0L
            val slayerLvl = com.example.data.models.OsrsXpCalculator.getLevelForXp(slayerXp)
            if (slayerLvl < boss.reqSlayerLevel) {
                if (!isAfk) {
                    addChatMessage("🔒 Slayer Level ${boss.reqSlayerLevel} required to fight ${boss.name}! (Current: $slayerLvl)")
                }
                AfkEngine.stopAll(pohPrefs)
                return
            }
        }

        if (boss.reqQuestId != null && !currentPet.completedQuestIds.contains(boss.reqQuestId)) {
            if (!isAfk) {
                addChatMessage("🔒 Requires quest completion: '${boss.reqQuestName ?: boss.reqQuestId}' to fight ${boss.name}!")
            }
            AfkEngine.stopAll(pohPrefs)
            return
        }

        if (boss.reqSkill != null) {
            val skillXp = skillXpMap.value[boss.reqSkill] ?: 0L
            val skillLvl = com.example.data.models.OsrsXpCalculator.getLevelForXp(skillXp)
            if (skillLvl < boss.reqSkillLevel) {
                if (!isAfk) {
                    addChatMessage("🔒 ${boss.reqSkill.displayName} Level ${boss.reqSkillLevel} required to fight ${boss.name}! (Current: $skillLvl)")
                }
                AfkEngine.stopAll(pohPrefs)
                return
            }
        }

        if (currentPet.health <= 0) {
            if (isAfk) {
                AfkEngine.stopAll(pohPrefs)
                addChatMessage("⚠️ AFK Boss Slaying Stopped: ${currentPet.customName} has 0 HP! Eat food from inventory to heal!")
                notifyAfkStopped("Boss Combat", "Pet has 0 HP!")
            } else {
                addChatMessage("⚠️ ${currentPet.customName} has 0 HP! Feed food before confronting ${boss.name}.")
            }
            return
        }

        val killDurationSec = calculateBossKillTime(boss)

        val defXp = skillXpMap.value[com.example.data.models.OsrsSkill.DEFENCE] ?: 0L
        val defLvl = com.example.data.models.OsrsXpCalculator.getLevelForXp(defXp)
        val gearDef = equippedItems.value.values.sumOf { it.defPowerBonus }
        val enemyLevel = boss.reqCombatLevel
        val damageTaken = ((enemyLevel * 4) / (defLvl + gearDef + 15)).coerceIn(2, (boss.maxHp / 10).coerceAtLeast(10))
        val newHealth = (currentPet.health - damageTaken).coerceAtLeast(0)

        viewModelScope.launch {
            val updatedPet = currentPet.copy(health = newHealth)
            repository.savePetState(updatedPet)

            if (newHealth <= 0) {
                AfkEngine.stopAll(pohPrefs)
                addChatMessage("💀 ${currentPet.customName} was defeated in battle by ${boss.name}! Eat food to restore HP.")
                notifyAfkStopped("Boss Combat", "${currentPet.customName} was defeated in battle by ${boss.name}!")
                return@launch
            }

            // Bosses do NOT give Slayer XP. They give Combat XP!
            val style = selectedCombatStyle.value
            val primarySkill = when (style) {
                com.example.data.models.CombatStyle.ATTACK -> com.example.data.models.OsrsSkill.ATTACK
                com.example.data.models.CombatStyle.RANGED -> com.example.data.models.OsrsSkill.RANGED
                com.example.data.models.CombatStyle.MAGIC -> com.example.data.models.OsrsSkill.MAGIC
                com.example.data.models.CombatStyle.DEFENCE -> com.example.data.models.OsrsSkill.DEFENCE
            }

            val totalCombatXp = boss.combatXpReward
            addXpAndNotify(
                skill = primarySkill,
                amount = totalCombatXp,
                gpReward = 0L,
                logTitle = "Boss Defeated: ${boss.name}",
                logDesc = "Slain ${boss.name} in ${killDurationSec}s! Earned $totalCombatXp ${primarySkill.displayName} XP."
            )
            addXpAndNotify(
                skill = com.example.data.models.OsrsSkill.HITPOINTS,
                amount = totalCombatXp / 3,
                gpReward = 0L,
                logTitle = "Hitpoints XP",
                logDesc = "Hitpoints gained from Boss kill."
            )

            // Guaranteed 1x Dragon Bone drop per boss kill
            val droppedItems = mutableListOf<String>()
            val currentInvMap = inventoryItems.value.associate { it.id to it.quantity }.toMutableMap()
            val currentBoneCount = currentInvMap["item_dragon_bones"] ?: 0
            val updatedBoneCount = currentBoneCount + 1
            currentInvMap["item_dragon_bones"] = updatedBoneCount
            saveInventoryItem("item_dragon_bones", updatedBoneCount)
            droppedItems.add("🦴 1x Dragon Bones")

            // Roll drop table (filtering duplicate bone drops)
            boss.drops.filter { !it.itemId.contains("bones") }.forEach { drop ->
                val roll = (1..100).random()
                if (roll <= drop.chancePercent) {
                    val qty = if (drop.minQty == drop.maxQty) drop.minQty else (drop.minQty..drop.maxQty).random()
                    val currentQty = currentInvMap[drop.itemId] ?: 0
                    val updatedQty = currentQty + qty
                    currentInvMap[drop.itemId] = updatedQty
                    saveInventoryItem(drop.itemId, updatedQty)
                    droppedItems.add("${drop.iconEmoji} ${drop.itemName} x$qty")
                }
            }

            val dropSummary = if (droppedItems.isNotEmpty()) droppedItems.joinToString(", ") else "Bones & Coins"
            addChatMessage("⚔️ ${boss.iconSymbol} Defeated ${boss.name} in ${killDurationSec}s! (HP: $newHealth/${getPetMaxHealth()}) Loot: $dropSummary")
        }
    }

    // --- Quest Expedition Methods ---

    fun isQuestCompleted(questId: String): Boolean {
        val completed = petState.value.completedQuestIds.toSet()
        val invIds = inventoryItems.value.map { it.id }.toSet()
        val bankIds = bankItems.value.map { it.id }.toSet()
        return com.example.data.models.TrainerLeagueData.isQuestCompleted(questId, completed, invIds, bankIds)
    }

    fun startQuestExpedition(quest: com.example.data.models.OsrsQuest) {
        val currentPet = petState.value
        if (isQuestCompleted(quest.id)) {
            addChatMessage("📜 You have already completed ${quest.name}!")
            return
        }

        val petCombatLvl = calculatePetCombatLevel()

        if (petCombatLvl < quest.recCombatLevel) {
            addChatMessage("⚔️ Combat level too low! ${quest.name} requires Combat Level ${quest.recCombatLevel} (Your Pet is Lv. $petCombatLvl).")
            return
        }

        if (quest.reqSkill != null) {
            val playerSkillLvl = com.example.data.models.OsrsXpCalculator.getLevelForXp(skillXpMap.value[quest.reqSkill] ?: 0L)
            if (playerSkillLvl < quest.reqSkillLevel) {
                addChatMessage("🔒 ${quest.reqSkill.displayName} level too low! ${quest.name} requires ${quest.reqSkill.displayName} Level ${quest.reqSkillLevel} (Your Pet is Lv. $playerSkillLvl).")
                return
            }
        }

        if (_activeQuestExpedition.value != null && _activeQuestExpedition.value?.quest?.id != quest.id) {
            pauseQuestExpedition()
        }

        val existingProgress = savedQuestProgressMap.value[quest.id]

        // If starting fresh (no existing progress), verify items
        if (existingProgress == null) {
            val inv = inventoryItems.value
            val bank = bankItems.value
            for (req in quest.requiredItems) {
                val normReqId = com.example.data.models.DefaultItems.normalizeItemId(req.itemId)
                val invCount = inv.filter { it.id == req.itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normReqId }.sumOf { it.quantity }
                val bankCount = bank.filter { it.id == req.itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normReqId }.sumOf { it.quantity }
                if ((invCount + bankCount) < req.requiredQty) {
                    addChatMessage("❌ Missing required item: ${req.itemName} x${req.requiredQty} for ${quest.name}!")
                    return
                }
            }
        }

        viewModelScope.launch {
            if (existingProgress == null) {
                // Deduct required items for new quest
                for (req in quest.requiredItems) {
                    val normReqId = com.example.data.models.DefaultItems.normalizeItemId(req.itemId)
                    var remainingNeeded = req.requiredQty
                    val bagItems = inventoryItems.value.filter { it.id == req.itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normReqId }
                    for (bagItem in bagItems) {
                        if (remainingNeeded <= 0) break
                        val takenFromBag = bagItem.quantity.coerceAtMost(remainingNeeded)
                        val newBagQty = bagItem.quantity - takenFromBag
                        remainingNeeded -= takenFromBag
                        if (newBagQty <= 0) {
                            repository.deleteInventoryItem(petState.value.petType.name, bagItem.id)
                        } else {
                            saveInventoryItem(bagItem.id, newBagQty)
                        }
                    }
                    if (remainingNeeded > 0) {
                        val bItems = bankItems.value.filter { it.id == req.itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normReqId }
                        for (bItem in bItems) {
                            if (remainingNeeded <= 0) break
                            val takenFromBank = bItem.quantity.coerceAtMost(remainingNeeded)
                            val newBankQty = bItem.quantity - takenFromBank
                            remainingNeeded -= takenFromBank
                            if (newBankQty <= 0) {
                                saveBankItem(bItem.id, 0)
                            } else {
                                saveBankItem(bItem.id, newBankQty)
                            }
                        }
                    }
                }
            }

            // Calculate duration based on combat level
            val combatLvl = calculatePetCombatLevel()

            val baseDuration = quest.calculateDurationSeconds(combatLvl)
            var questRedPct = _activeSummon.value?.questTimeReductionPercent ?: 0
            val now = System.currentTimeMillis()
            val hasLettuceThistleBuff = _activeCookingBuffs.value.any { it.recipeId == "rec_lettuce_thistle" && it.expiryTimeMs > now }
            if (hasLettuceThistleBuff) {
                questRedPct += 10
            }

            // Blessing of Flowing Springs (-15%, -25%, -40% quest & shaman path expedition time)
            val springsRedPct = when {
                isIncantationActiveAndUsable("incant_flowing_springs_t3") -> 40
                isIncantationActiveAndUsable("incant_flowing_springs_t2") -> 25
                isIncantationActiveAndUsable("incant_flowing_springs") -> 15
                else -> 0
            }
            questRedPct += springsRedPct

            // Captain Barnaby Favor Perk: -1% quest & shaman path expedition time per favor level (up to -50%)
            val barnabyLvl = npcFavorMap.value["barnaby"]?.first ?: getNpcFavorLevel("barnaby")
            val barnabyRedPct = barnabyLvl.coerceIn(0, 50)
            questRedPct += barnabyRedPct

            val reducedDuration = if (questRedPct > 0) (baseDuration * (100 - questRedPct) / 100).coerceAtLeast(3) else baseDuration
            val totalDuration = existingProgress?.totalDurationSeconds ?: reducedDuration
            val elapsedSec = if (existingProgress != null && !existingProgress.isPaused) {
                ((System.currentTimeMillis() - existingProgress.lastUpdatedTimestamp) / 1000).toInt()
            } else 0
            val rawRemaining = existingProgress?.remainingSeconds ?: totalDuration
            val startingRemaining = (rawRemaining - elapsedSec).coerceAtLeast(0)

            if (existingProgress != null && startingRemaining <= 0) {
                completeQuestExpedition(quest)
                return@launch
            }

            // Stop other AFK activities (without pausing this quest)
            stopAllAfkStations(stopQuests = false)

            val nowMs = System.currentTimeMillis()
            val initialFraction = 1f - (startingRemaining.toFloat() / totalDuration.toFloat())
            _activeQuestExpedition.value = QuestExpeditionState(
                quest = quest,
                totalDurationSeconds = totalDuration,
                remainingSeconds = startingRemaining,
                progressFraction = initialFraction,
                isPaused = false,
                lastUpdatedTimestamp = nowMs
            )

            val savedSec = (baseDuration - reducedDuration).coerceAtLeast(0)
            val perkBadges = buildList {
                if (barnabyRedPct > 0) add("⚓ Barnaby Lv.$barnabyLvl (-${barnabyRedPct}%)")
                if (springsRedPct > 0) add("🌊 Blessing of Springs (-${springsRedPct}%)")
                if (hasLettuceThistleBuff) add("🥗 Lettuce Thistle (-10%)")
            }
            val speedNotice = if (perkBadges.isNotEmpty() && savedSec > 0) {
                " (-${com.example.data.models.formatQuestDuration(savedSec)} saved from ${perkBadges.joinToString(", ")}!)"
            } else ""

            if (existingProgress != null) {
                addChatMessage("▶️ Resumed Quest: ${quest.name}! (${com.example.data.models.formatQuestDuration(startingRemaining)} remaining$speedNotice)")
            } else {
                addChatMessage("🚀 Sent ${petState.value.customName} out on Quest: ${quest.name}! (${com.example.data.models.formatQuestDuration(totalDuration)} AFK Expedition$speedNotice)")
            }

            // Save active state to DB
            saveQuestProgress(
                com.example.data.db.QuestProgressEntity(
                    petTypeName = petState.value.petType.name,
                    questId = quest.id,
                    remainingSeconds = startingRemaining,
                    totalDurationSeconds = totalDuration,
                    isPaused = false,
                    lastUpdatedTimestamp = nowMs
                )
            )

            // Ticker loop with wall-clock time calculation
            questExpeditionJob?.cancel()
            questExpeditionJob = viewModelScope.launch {
                var lastTickTime = System.currentTimeMillis()
                var remaining = startingRemaining
                while (remaining > 0) {
                    kotlinx.coroutines.delay(1000)
                    val tickNow = System.currentTimeMillis()
                    val realElapsedSec = ((tickNow - lastTickTime) / 1000).toInt().coerceAtLeast(1)
                    lastTickTime = tickNow
                    val speedMultiplier = if (isIncantationActiveAndUsable("incant_wild_wind")) 2 else 1
                    val tickDecrement = realElapsedSec * speedMultiplier
                    remaining = (remaining - tickDecrement).coerceAtLeast(0)
                    val fraction = 1f - (remaining.toFloat() / totalDuration.toFloat())
                    _activeQuestExpedition.value = _activeQuestExpedition.value?.copy(
                        remainingSeconds = remaining,
                        progressFraction = fraction,
                        lastUpdatedTimestamp = tickNow
                    )

                    if (remaining % 3 == 0 || remaining == 0) {
                        saveQuestProgress(
                            com.example.data.db.QuestProgressEntity(
                                petTypeName = petState.value.petType.name,
                                questId = quest.id,
                                remainingSeconds = remaining,
                                totalDurationSeconds = totalDuration,
                                isPaused = false,
                                lastUpdatedTimestamp = tickNow
                            )
                        )
                    }
                }

                // Quest Completed!
                completeQuestExpedition(quest)
            }
        }
    }

    fun pauseQuestExpedition() {
        val currentExpedition = _activeQuestExpedition.value ?: return
        questExpeditionJob?.cancel()
        questExpeditionJob = null

        val quest = currentExpedition.quest
        val remaining = currentExpedition.remainingSeconds
        val total = currentExpedition.totalDurationSeconds
        val pct = ((1f - remaining.toFloat() / total.toFloat()) * 100).toInt()

        viewModelScope.launch {
            saveQuestProgress(
                com.example.data.db.QuestProgressEntity(
                    petTypeName = petState.value.petType.name,
                    questId = quest.id,
                    remainingSeconds = remaining,
                    totalDurationSeconds = total,
                    isPaused = true,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
            )
            _activeQuestExpedition.value = null
            addChatMessage("💾 Progress Saved! Paused ${quest.name} at $pct% (${com.example.data.models.formatQuestDuration(remaining)} remaining). You can resume anytime!")
        }
    }

    fun cancelQuestExpedition(questId: String? = null) {
        val targetQuestId = questId ?: _activeQuestExpedition.value?.quest?.id
        questExpeditionJob?.cancel()
        questExpeditionJob = null

        if (targetQuestId != null) {
            val questName = com.example.data.models.OsrsQuestData.findQuestById(targetQuestId)?.name ?: "Quest"
            viewModelScope.launch {
                deleteQuestProgress(targetQuestId)
                addChatMessage("🗑️ Cancelled expedition and reset saved progress for $questName.")
            }
        }

        if (_activeQuestExpedition.value?.quest?.id == targetQuestId || questId == null) {
            _activeQuestExpedition.value = null
        }
    }

    fun markQuestCompleted(questId: String) {
        val active = _activeQuestExpedition.value
        if (active != null && active.quest.id == questId && active.remainingSeconds > 0) {
            addChatMessage("⏳ ${active.quest.name} is still in progress! (${com.example.data.models.formatQuestDuration(active.remainingSeconds)} remaining)")
            return
        }
        viewModelScope.launch {
            val quest = com.example.data.models.OsrsQuestData.findQuestById(questId) ?: return@launch
            completeQuestExpedition(quest)
        }
    }

    private suspend fun completeQuestExpedition(quest: com.example.data.models.OsrsQuest) {
        val currentPet = petState.value
        if (currentPet.completedQuestIds.contains(quest.id)) return

        // 1. Award reward XP FIRST so intermediate state saves won't wipe completedQuestIds
        quest.rewardXpMap.forEach { (skill, xp) ->
            addXpAndNotify(
                skill = skill,
                amount = xp,
                gpReward = 0L,
                logTitle = "Quest: ${quest.name}",
                logDesc = "Earned $xp ${skill.displayName} XP!"
            )
        }

        // 2. Award reward item if applicable
        var lootItemText = ""
        val lootItemId = quest.rewardItemId
        if (lootItemId != null) {
            val existingInBag = inventoryItems.value.find { it.id == lootItemId }
            val newQty = (existingInBag?.quantity ?: 0) + 1
            saveInventoryItem(lootItemId, newQty)
            markTotemUnlocked(lootItemId)
            val itemObj = com.example.data.models.DefaultItems.getItemById(lootItemId)
            val itemName = quest.rewardItemName ?: itemObj.name
            val itemEmoji = quest.rewardItemEmoji ?: itemObj.iconEmoji
            lootItemText = " & 1x $itemName $itemEmoji"
            addChatMessage("🎁 QUEST LOOT: Received 1x $itemName $itemEmoji!")
        }

        markTotemUnlocked(quest.id)

        // 3. Check Gym Pet Unlocks
        val gymPetUnlock: PetType? = com.example.data.models.TrainerLeagueData.getGymPetUnlock(quest.id)

        val latestPet = petState.value
        val updatedCompleted = (latestPet.completedQuestIds + quest.id).distinct()
        val updatedQp = latestPet.questPoints + quest.questPoints
        val updatedGp = latestPet.coinsGp + quest.rewardGp
        val updatedUnlockedPets = if (gymPetUnlock != null) {
            (latestPet.unlockedPets + gymPetUnlock).distinct()
        } else latestPet.unlockedPets

        val finalPet = latestPet.copy(
            completedQuestIds = updatedCompleted,
            questPoints = updatedQp,
            coinsGp = updatedGp,
            unlockedPets = updatedUnlockedPets
        )
        repository.savePetState(finalPet)

        if (gymPetUnlock != null) {
            addChatMessage("🏆 GYM REWARD: Unlocked ${gymPetUnlock.displayName} pet for completing ${quest.name}!")
        }

        if (quest.id.contains("champion") || quest.id.contains("lunar_diplomacy")) {
            addChatMessage("🪄 SHAMAN BLESSING: Unlocked +1 Active Incantation Slot! (Total Active Slots: ${getMaxIncantationSlots()})")
        }

        deleteQuestProgress(quest.id)
        _activeQuestExpedition.value = null
        showQuestCompletionPopup(quest)

        val xpSummary = quest.rewardXpMap.entries.joinToString(", ") { (sk, xp) -> "+$xp ${sk.displayName} XP" }
        val rewardFullSummary = if (xpSummary.isNotBlank()) "Rewards: $xpSummary, +${quest.questPoints} QP, +${quest.rewardGp} GP$lootItemText" else "+${quest.questPoints} QP, +${quest.rewardGp} GP$lootItemText"

        val isShaman = com.example.data.models.TrainerLeagueData.isShamanPathQuest(quest)
        val celebrationPrefix = if (isShaman) "🌿 SHAMAN PATH CONQUERED" else "🎉 QUEST COMPLETED"
        addChatMessage("$celebrationPrefix: ${quest.name}! ($rewardFullSummary)")

        com.example.util.NotificationHelper.sendAfkNotification(
            getApplication(),
            if (isShaman) "🌿 Shaman Path Conquered!" else "📜 Quest Completed!",
            "🎉 Completed '${quest.name}'! $rewardFullSummary"
        )
    }

    fun markQuestIncomplete(questId: String) {
        viewModelScope.launch {
            val currentPet = petState.value
            if (!currentPet.completedQuestIds.contains(questId)) return@launch

            val quest = com.example.data.models.OsrsQuestData.findQuestById(questId)
            val qpDeduction = quest?.questPoints ?: 0
            val updatedCompleted = currentPet.completedQuestIds.filter { it != questId }
            val updatedQp = (currentPet.questPoints - qpDeduction).coerceAtLeast(0)

            val newPet = currentPet.copy(
                completedQuestIds = updatedCompleted,
                questPoints = updatedQp
            )
            repository.savePetState(newPet)
            deleteQuestProgress(questId)

            if (_activeQuestExpedition.value?.quest?.id == questId) {
                cancelQuestExpedition(questId)
            }

            addChatMessage("🔄 Reset Quest '${quest?.name ?: questId}' to incomplete! You can now redo it.")
        }
    }

    fun markAllQuestsIncomplete() {
        viewModelScope.launch {
            val currentPet = petState.value
            if (currentPet.completedQuestIds.isEmpty()) return@launch

            val newPet = currentPet.copy(
                completedQuestIds = emptyList(),
                questPoints = 0
            )
            repository.savePetState(newPet)

            currentPet.completedQuestIds.forEach { qId ->
                deleteQuestProgress(qId)
            }

            if (_activeQuestExpedition.value != null) {
                cancelQuestExpedition(null)
            }

            addChatMessage("🔄 Reset ALL quests to incomplete state for testing!")
        }
    }

    fun huntCreature(creature: com.example.data.models.HunterCreature, isAfk: Boolean = false) {
        if (!isAfk) {
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Hunter Trapping.")
            }
        }
        val hunterXp = skillXpMap.value[com.example.data.models.OsrsSkill.HUNTER] ?: 0L
        val hunterLvl = com.example.data.models.OsrsXpCalculator.getLevelForXp(hunterXp)

        if (hunterLvl < creature.reqHunterLevel) {
            if (!isAfk) {
                addChatMessage("🔒 Hunter Level ${creature.reqHunterLevel} required to trap ${creature.name}! Current level: $hunterLvl.")
            }
            AfkEngine.stopAll(pohPrefs)
            return
        }

        val currentPet = petState.value
        if (creature.reqQuestId != null && !currentPet.completedQuestIds.contains(creature.reqQuestId)) {
            if (!isAfk) {
                addChatMessage("🔒 Requires quest completion: '${creature.reqQuestName ?: creature.reqQuestId}' to trap ${creature.name}!")
            }
            AfkEngine.stopAll(pohPrefs)
            return
        }

        // Check if required trap is in inventory or bank
        val totalTraps = getItemQuantityCombined(creature.requiredTrapItemId)
        if (totalTraps <= 0) {
            if (isAfk) {
                AfkEngine.stopAll(pohPrefs)
                addChatMessage("🐾 AFK HUNTER STOPPED: Out of ${creature.requiredTrapName}s! Get or craft more.")
                notifyAfkStopped("Hunter Trapping", "Out of ${creature.requiredTrapName}s!")
            } else {
                addChatMessage("⚠️ You need at least 1x ${creature.requiredTrapName} (in Inventory or Bank Vault) to trap ${creature.name}!")
            }
            return
        }

        val totalXpGained = creature.xpReward

        viewModelScope.launch {
            // Deduct 1 trap from inventory or bank
            deductItemCombined(creature.requiredTrapItemId, 1)

            addXpAndNotify(
                skill = com.example.data.models.OsrsSkill.HUNTER,
                amount = totalXpGained,
                gpReward = 0L,
                logTitle = "Captured ${creature.name}",
                logDesc = "Trapped ${creature.name} using ${creature.requiredTrapName}!"
            )
            progressSkillContract(OsrsSkill.HUNTER, 1, creature.id)

            // Track Hunter Contract progress
            val activeHunterContract = _hunterContract.value
            if (activeHunterContract.currentCatches < activeHunterContract.targetCatches) {
                if (creature.id == activeHunterContract.creatureId || creature.name.equals(activeHunterContract.creatureName, ignoreCase = true)) {
                    val kaelLvl = npcFavorMap.value["kael"]?.first ?: getNpcFavorLevel("kael")
                    var taskIncr = 1
                    if ((1..100).random() <= kaelLvl) {
                        taskIncr = 2
                        addChatMessage("✨ [Kael's Favor Perk (+${kaelLvl}%)]: Double task progress! (+2 ${creature.name} catches credited) 🐾🎯")
                    }
                    val newCatches = (activeHunterContract.currentCatches + taskIncr).coerceAtMost(activeHunterContract.targetCatches)
                    _hunterContract.value = activeHunterContract.copy(currentCatches = newCatches)
                    addChatMessage("🐾 HUNTER RUMOUR PROGRESS: $newCatches / ${activeHunterContract.targetCatches} ${creature.name} trapped!")
                    if (newCatches >= activeHunterContract.targetCatches) {
                        addChatMessage("🎉 HUNTER RUMOUR READY FOR TURN-IN! Return to Guildmaster Guilden in Contracts tab to claim your sack!")
                    }
                }
            }

            creature.drops.forEach { drop ->
                val roll = kotlin.random.Random.nextInt(1, 101)
                if (roll <= drop.chancePercent) {
                    val qty = kotlin.random.Random.nextInt(drop.minQty, drop.maxQty + 1)
                    val existingInBag = inventoryItems.value.find { it.id == drop.itemId }
                    val newQty = (existingInBag?.quantity ?: 0) + qty
                    saveInventoryItem(drop.itemId, newQty)
                    addChatMessage("🐾 CAPTURE: Trapped $qty x ${drop.itemName} ${drop.iconEmoji}!")
                }
            }
        }
    }

    // --- Bank & Food Bag Methods ---
    fun depositToBank(itemId: String, quantity: Int = 1) {
        viewModelScope.launch {
            val invItem = inventoryItems.value.find { it.id == itemId }
            depositItemToBank(itemId, quantity)
            if (invItem != null && invItem.isCookedReadyToEatFood) {
                addChatMessage("🎒 Food Bag: Deposited $quantity x ${invItem.name} into your Food Bag!")
            } else if (invItem != null) {
                addChatMessage("🏦 Bank Vault: Deposited $quantity x ${invItem.name} into your Bank Vault!")
            }
        }
    }

    fun withdrawFromBank(itemId: String, quantity: Int = 1) {
        viewModelScope.launch {
            withdrawItemFromBank(itemId, quantity)
        }
    }

    fun depositAllInventoryToBank() {
        viewModelScope.launch {
            val cookedItems = inventoryItems.value.filter { it.isCookedReadyToEatFood && it.quantity > 0 }
            val cookedCount = cookedItems.sumOf { it.quantity }
            val rawOrOtherItems = inventoryItems.value.filter { !it.isCookedReadyToEatFood && it.quantity > 0 }
            val otherCount = rawOrOtherItems.sumOf { it.quantity }

            depositAllInventoryToBankInternal()
            if (cookedCount > 0 && otherCount > 0) {
                addChatMessage("🏦 Deposited inventory! ($cookedCount ready-to-eat meals sent to 🎒 Food Bag, $otherCount items to 🏦 Vault)")
            } else if (cookedCount > 0) {
                addChatMessage("🎒 Food Bag: Deposited $cookedCount ready-to-eat meals into your Food Bag!")
            } else {
                addChatMessage("🏦 Bank Vault: Deposited $otherCount items into your Bank Vault!")
            }
        }
    }

    fun autoRouteCookedFoodToBag() {
        viewModelScope.launch {
            val cookedInInv = inventoryItems.value.filter { it.isCookedReadyToEatFood && it.quantity > 0 }
            if (cookedInInv.isEmpty()) return@launch
            var routedTotal = 0
            cookedInInv.forEach { item ->
                val currentBankList = bankItems.value
                val existingInBag = currentBankList.find { it.id == item.id }?.quantity ?: 0
                saveBankItem(item.id, existingInBag + item.quantity)
                saveInventoryItem(item.id, 0)
                routedTotal += item.quantity
            }
            if (routedTotal > 0) {
                addChatMessage("🎒 Auto-routed $routedTotal cooked meal(s) from inventory into 🎒 Food Bag!")
            }
        }
    }

    fun cleanupDuplicates() {
        viewModelScope.launch {
            repository.deduplicateDatabaseItems(petState.value.petType.name)
        }
    }

    fun toggleAfkCampfire() {
        val candidateLogIds = listOf("item_ironwood_logs", "item_yew_logs", "item_maple_logs", "item_cedar_logs", "item_willow_logs", "item_pine_logs", "item_oak_logs", "item_birch_logs", "item_logs", "item_teak_logs", "item_mahogany_logs", "item_magic_logs", "item_redwood_logs")
        val hasLogs = candidateLogIds.any { getItemQuantityCombined(it) > 0 }
        if (!isAfkCampfireActive.value && !hasLogs) {
            stopAllAfkStations()
            addChatMessage("🔥 Campfire: Cannot start! No logs in inventory or bank to burn.")
            return
        }
        val nextState = !isAfkCampfireActive.value
        if (nextState && !canStartAfkOrHungerAction("Campfire Firemaking")) return
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.CAMPFIRE, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🔥 AFK Campfire is now ${if (nextState) "ENABLED (Burning logs for Firemaking XP & GP)" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun cleanCookingQueue() {
        // Queue items are preserved even when 0 to maintain user configured order
    }

    fun addToCookingQueue(rawId: String) {
        val cookXp = skillXpMap.value[OsrsSkill.COOKING] ?: 0L
        val cookLvl = OsrsXpCalculator.getLevelForXp(cookXp)
        val maxSlots = com.example.data.models.CookingRecipes.getMaxQueueSlots(cookLvl)
        val currentQueue = _cookingQueue.value.toMutableList()

        if (currentQueue.contains(rawId)) {
            addChatMessage("ℹ️ That item is already in your AFK cooking queue!")
            return
        }

        val recipe = com.example.data.models.CookingRecipes.findRecipe(rawId)
        val itemName = recipe?.rawName ?: rawId

        if (recipe != null && cookLvl < recipe.reqLevel) {
            addChatMessage("🔒 Cannot queue ${recipe.rawName}: Requires Level ${recipe.reqLevel} Cooking!")
            return
        }

        if (currentQueue.size >= maxSlots) {
            addChatMessage("⚠️ AFK Cooking queue is full ($maxSlots slots max for Lv.$cookLvl Cooking)!")
            return
        }

        currentQueue.add(rawId)
        _cookingQueue.value = currentQueue
        val totalAvailable = getItemQuantityCombined(rawId)
        addChatMessage("➕ Added $itemName to Cooking Queue (Slot #${currentQueue.size}/$maxSlots). Available: $totalAvailable in Storage.")
        saveAfkStateToPrefs()
    }

    fun removeFromCookingQueue(rawId: String) {
        val currentQueue = _cookingQueue.value.toMutableList()
        if (currentQueue.remove(rawId)) {
            _cookingQueue.value = currentQueue
            val recipe = com.example.data.models.CookingRecipes.findRecipe(rawId)
            val itemName = recipe?.rawName ?: rawId
            addChatMessage("❌ Removed $itemName from Cooking Queue.")
            saveAfkStateToPrefs()
        }
    }

    fun moveCookingQueueItem(rawId: String, direction: Int) {
        val currentQueue = _cookingQueue.value.toMutableList()
        val index = currentQueue.indexOf(rawId)
        if (index == -1) return
        val targetIndex = index + direction
        if (targetIndex in 0 until currentQueue.size) {
            val item = currentQueue.removeAt(index)
            currentQueue.add(targetIndex, item)
            _cookingQueue.value = currentQueue
            saveAfkStateToPrefs()
        }
    }

    fun clearCookingQueue() {
        _cookingQueue.value = emptyList()
        addChatMessage("🗑️ Cleared AFK Cooking Queue.")
        saveAfkStateToPrefs()
    }

    fun autoPopulateCookingQueue() {
        val cookXp = skillXpMap.value[OsrsSkill.COOKING] ?: 0L
        val cookLvl = OsrsXpCalculator.getLevelForXp(cookXp)
        val maxSlots = com.example.data.models.CookingRecipes.getMaxQueueSlots(cookLvl)

        val rawAvailable = com.example.data.models.CookingRecipes.ALL_RECIPES.filter { recipe ->
            cookLvl >= recipe.reqLevel && getItemQuantityCombined(recipe.rawId) > 0
        }.map { it.rawId }.distinct()

        val newQueue = rawAvailable.take(maxSlots)
        if (newQueue.isNotEmpty()) {
            _cookingQueue.value = newQueue
            addChatMessage("⚡ Auto-filled cooking queue with ${newQueue.size} available raw food types from Storage!")
            saveAfkStateToPrefs()
        } else {
            addChatMessage("⚠️ No suitable raw foods found in Storage to auto-fill queue.")
        }
    }

    fun toggleAfkCooking() {
        val nextState = !isAfkCookingActive.value
        if (nextState) {
            if (!canStartAfkOrHungerAction("Cooking Range")) return
            val cookXp = skillXpMap.value[OsrsSkill.COOKING] ?: 0L
            val cookLvl = OsrsXpCalculator.getLevelForXp(cookXp)
            val queue = _cookingQueue.value

            val hasQueuedItemAvail = queue.any { queuedId ->
                val rec = com.example.data.models.CookingRecipes.findRecipe(queuedId)
                val req = rec?.reqLevel ?: 1
                cookLvl >= req && getItemQuantityCombined(queuedId) > 0
            }

            val availableRecipes = com.example.data.models.CookingRecipes.ALL_RECIPES.filter { recipe ->
                cookLvl >= recipe.reqLevel && getItemQuantityCombined(recipe.rawId) > 0
            }

            if (!hasQueuedItemAvail && availableRecipes.isEmpty()) {
                stopAllAfkStations()
                addChatMessage("🍳 Cooking Fire: Cannot start! No cooked-eligible raw food in Storage.")
                return
            }

            if (queue.isEmpty() && availableRecipes.isNotEmpty()) {
                // If the user selected a specific food, queue only that one. Otherwise queue just the first available recipe.
                val selected = _selectedFoodId.value
                val itemToQueue = if (selected != null && availableRecipes.any { it.rawId == selected }) {
                    selected
                } else {
                    availableRecipes.first().rawId
                }
                _cookingQueue.value = listOf(itemToQueue)
                val recipe = com.example.data.models.CookingRecipes.findRecipe(itemToQueue)
                addChatMessage("🍳 Queued 1 item for Cooking: ${recipe?.rawName ?: itemToQueue} (${getItemQuantityCombined(itemToQueue)} in Storage).")
            }

            stopAllAfkStations()
        } else {
            stopAllAfkStations()
        }
        if (nextState) AfkEngine.startActivity(AfkActivityType.COOKING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🔥 Cooking Fire is now ${if (nextState) "BURNING (AFK Cooking Enabled)" else "EXTINGUISHED (AFK Stopped)"}")
        saveAfkStateToPrefs()
    }

    fun toggleAfkShamanPoolFishing(areaId: String? = null) {
        val targetAreaId = areaId ?: _selectedSpiritPoolAreaId.value
        val pool = com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS.find { it.id == targetAreaId }
            ?: com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS.first()

        val fishXp = skillXpMap.value[OsrsSkill.FISHING] ?: 0L
        val fishLvl = OsrsXpCalculator.getLevelForXp(fishXp)

        if (fishLvl < pool.reqLevel) {
            addChatMessage("🔒 Cannot fish in ${pool.name}: Requires Level ${pool.reqLevel} Fishing!")
            return
        }

        if (!isTotemUnlocked(pool.reqTotemId)) {
            val reqName = pool.reqTotemName ?: "Obelisk"
            addChatMessage("🗿 Obelisk Locked: Requires the $reqName to fish in ${pool.name}!")
            return
        }

        if (isAfkFishingActive.value && _selectedSpiritPoolAreaId.value == targetAreaId) {
            settlePendingAfkTime("Stopped fishing in ${pool.name}")
            stopAllAfkStations()
            AfkEngine.stopAll(pohPrefs)
            addChatMessage("🎣 Stopped AFK fishing in ${pool.name}.")
            saveAfkStateToPrefs()
            return
        }

        if (!canStartAfkOrHungerAction("Fishing in ${pool.name}")) return
        settlePendingAfkTime("Switched to ${pool.name}")
        stopAllAfkStations()
        _selectedSpiritPoolAreaId.value = targetAreaId
        AfkEngine.startActivity(AfkActivityType.FISHING, pohPrefs)
        val now = System.currentTimeMillis()
        _afkActivityStartTimeMs.value = now
        _afkLastProcessTimeMs.value = now
        recordAfkActivity("fishing")
        addChatMessage("🎣 AFK Fishing started in ${pool.emoji} ${pool.name}! (Drops randomized from pool drop table)")
        saveAfkStateToPrefs()
    }

    fun toggleAfkFishing() {
        toggleAfkShamanPoolFishing(_selectedSpiritPoolAreaId.value)
    }

    fun toggleAfkGemologyMining(areaId: String? = null) {
        val targetAreaId = areaId ?: _selectedGemologyAreaId.value
        val quarry = com.example.data.models.AdventuringStoryData.GEMOLOGY_AREAS.find { it.id == targetAreaId }
            ?: com.example.data.models.AdventuringStoryData.GEMOLOGY_AREAS.first()

        val minXp = skillXpMap.value[OsrsSkill.SMITHING] ?: 0L
        val minLvl = OsrsXpCalculator.getLevelForXp(minXp)

        if (minLvl < quarry.reqLevel) {
            addChatMessage("🔒 Cannot mine in ${quarry.name}: Requires Level ${quarry.reqLevel} Forging!")
            return
        }

        if (!isTotemUnlocked(quarry.reqTotemId)) {
            val reqName = quarry.reqTotemName ?: "Obelisk"
            addChatMessage("🗿 Obelisk Locked: Requires the $reqName to mine in ${quarry.name}!")
            return
        }

        if (isAfkMiningActive.value && _selectedGemologyAreaId.value == targetAreaId) {
            settlePendingAfkTime("Stopped mining in ${quarry.name}")
            stopAllAfkStations()
            AfkEngine.stopAll(pohPrefs)
            addChatMessage("⛏️ Stopped AFK mining in ${quarry.name}.")
            saveAfkStateToPrefs()
            return
        }

        if (!canStartAfkOrHungerAction("Mining in ${quarry.name}")) return
        settlePendingAfkTime("Switched to ${quarry.name}")
        stopAllAfkStations()
        _selectedGemologyAreaId.value = targetAreaId
        AfkEngine.startActivity(AfkActivityType.MINING, pohPrefs)
        val now = System.currentTimeMillis()
        _afkActivityStartTimeMs.value = now
        _afkLastProcessTimeMs.value = now
        recordAfkActivity("mining")
        addChatMessage("⛏️ AFK Mining started in ${quarry.emoji} ${quarry.name}! (Drops randomized from quarry drop table)")
        saveAfkStateToPrefs()
    }

    fun toggleAfkMining() {
        toggleAfkGemologyMining(_selectedGemologyAreaId.value)
    }

    fun toggleAfkSawmill() {
        val currentInv = inventoryItems.value
        val hasLogs = currentInv.any { (it.id.contains("log") || it.id.contains("timber") || it.id.contains("bark") || it.id.contains("trunk") || it.id == "item_logs") && it.quantity > 0 }

        if (!isAfkSawmillActive.value && !hasLogs) {
            stopAllAfkStations()
            addChatMessage("🪚 Sawmill: Cannot start! You have no timber logs in your inventory. Chop trees in The Grove first!")
            return
        }

        val nextState = !isAfkSawmillActive.value
        if (nextState && !canStartAfkOrHungerAction("Sawmill Plank Making")) return
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.SAWMILL, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🪚 AFK Sawmill Station is now ${if (nextState) "RUNNING (Milling logs -> planks + Crafting XP)" else "STOPPED / OFF"}")
        saveAfkStateToPrefs()
    }

    fun toggleAfkNailCrafting() {
        val currentInv = inventoryItems.value
        val hasBars = currentInv.any { it.id.endsWith("_bar") && it.quantity > 0 }

        if (!isAfkNailCraftingActive.value && !hasBars) {
            stopAllAfkStations()
            addChatMessage("🔨 Nail Anvil: Cannot start! You have no metal bars in inventory.")
            return
        }

        val nextState = !isAfkNailCraftingActive.value
        if (nextState && !canStartAfkOrHungerAction("Nail Crafting")) return
        stopAllAfkStations()
        if (nextState) AfkEngine.startActivity(AfkActivityType.NAIL_CRAFTING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🔨 AFK Nail Anvil is now ${if (nextState) "ENABLED (Forging bars -> 15 nails + Smithing XP)" else "DISABLED"}")
        saveAfkStateToPrefs()
    }

    fun craftBarsToNailsAtAnvil(isAfk: Boolean = false) {
        if (!isAfk) {
            if (!canStartAfkOrHungerAction("Nail Crafting")) return
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Nail Crafting.")
            }
        }
        viewModelScope.launch {
            val selectedBarId = _selectedNailBarId.value
            val candidateBarIds = listOfNotNull(
                selectedBarId,
                "item_rune_bar",
                "item_adamant_bar",
                "item_mithril_bar",
                "item_steel_bar",
                "item_iron_bar",
                "item_bronze_bar"
            ).distinct()

            val targetBarId = candidateBarIds.firstOrNull { getItemQuantityCombined(it) > 0 }

            if (targetBarId == null) {
                if (isAfkNailCraftingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔨 Nail Anvil: Out of metal bars! Anvil AFK turned OFF.")
                    notifyAfkStopped("Nail Crafting", "Out of metal bars in inventory + bank!")
                } else if (!isAfk) {
                    addChatMessage("🔨 Nail Anvil: No metal bars in inventory or bank! Mine ores and smelt bars first.")
                }
                return@launch
            }

            deductItemCombined(targetBarId, 1)

            val (resultNailId, nailName, xp) = when (targetBarId) {
                "item_rune_bar" -> Triple("item_rune_nails", "Aetherite Nails", 120L)
                "item_adamant_bar" -> Triple("item_adamant_nails", "Amethyst Nails", 90L)
                "item_mithril_bar" -> Triple("item_mithril_nails", "Opalite Nails", 65L)
                "item_steel_bar" -> Triple("item_steel_nails", "Steel Nails", 45L)
                "item_iron_bar" -> Triple("item_iron_nails", "Iron Nails", 30L)
                else -> Triple("item_nails", "Bronze Nails", 20L)
            }

            val updatedInv = inventoryItems.value
            val existingNails = updatedInv.find { it.id == resultNailId }
            val newQty = (existingNails?.quantity ?: 0) + 15
            saveInventoryItem(resultNailId, newQty)

            // Add 15 Nails to POH Material Inventory as well for Construction
            val currentPoh = _pohHouseState.value
            val currentMats = currentPoh.materialInventory.toMutableMap()
            currentMats[com.example.data.models.GeMaterial.NAILS] = (currentMats[com.example.data.models.GeMaterial.NAILS] ?: 0) + 15
            updatePohHouseState(currentPoh.copy(materialInventory = currentMats))

            val barObjName = DefaultItems.getItemById(targetBarId).name
            addXpAndNotify(
                skill = OsrsSkill.SMITHING,
                amount = xp,
                gpReward = 25L,
                logTitle = "Forged 15x $nailName",
                logDesc = "Forged 1x $barObjName ➔ 15x $nailName at the Anvil for +$xp Smithing XP!"
            )
            // Forging construction nails also grants Hut-Keeping / Construction XP
            addXpAndNotify(
                skill = OsrsSkill.CONSTRUCTION,
                amount = (xp * 0.75).toLong().coerceAtLeast(15L),
                gpReward = 0L,
                logTitle = "Crafted Construction Nails",
                logDesc = "Forged 15x $nailName for +${(xp * 0.75).toLong()} Hut-Keeping XP! 🛠️"
            )
            progressSkillContract(OsrsSkill.CONSTRUCTION, 15, resultNailId)

            val remainingBars = candidateBarIds.any { getItemQuantityCombined(it) > 0 }
            if (!remainingBars && isAfkNailCraftingActive.value) {
                AfkEngine.stopAll(pohPrefs)
                addChatMessage("🔨 Nail Anvil: Out of metal bars! Anvil AFK turned OFF.")
            }
        }
    }

    fun burnLogsAtCampfire(isAfk: Boolean = false) {
        if (!isAfk) {
            if (!canStartAfkOrHungerAction("Campfire Firemaking")) return
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Firemaking.")
            }
        }
        viewModelScope.launch {
            val candidateLogs = listOf(
                "item_ironwood_logs", "item_yew_logs", "item_maple_logs", "item_cedar_logs",
                "item_willow_logs", "item_pine_logs", "item_oak_logs", "item_birch_logs",
                "item_logs", "item_teak_logs", "item_mahogany_logs", "item_magic_logs", "item_redwood_logs"
            )
            val targetLogId = candidateLogs.firstOrNull { getItemQuantityCombined(it) > 0 }

            if (targetLogId == null) {
                if (isAfkCampfireActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔥 Campfire: Out of logs! Firemaking AFK turned OFF.")
                    notifyAfkStopped("Campfire Firemaking", "Out of logs in inventory + bank!")
                } else if (!isAfk) {
                    addChatMessage("🔥 Campfire: No logs in inventory or bank! Swipe notifications or chop trees to get logs.")
                }
                return@launch
            }

            deductItemCombined(targetLogId, 1)
            val logItemDef = DefaultItems.ALL.find { it.id == targetLogId }
            val logName = logItemDef?.name ?: "Logs"

            val (xp, gp) = when (targetLogId) {
                "item_ironwood_logs" -> 500L to 100L
                "item_redwood_logs" -> 450L to 90L
                "item_magic_logs" -> 400L to 80L
                "item_yew_logs" -> 350L to 70L
                "item_maple_logs" -> 250L to 50L
                "item_mahogany_logs" -> 220L to 45L
                "item_cedar_logs" -> 200L to 40L
                "item_teak_logs" -> 180L to 35L
                "item_willow_logs" -> 150L to 30L
                "item_pine_logs" -> 120L to 25L
                "item_oak_logs" -> 90L to 20L
                "item_birch_logs" -> 60L to 15L
                else -> 40L to 10L
            }

            addXpAndNotify(
                skill = OsrsSkill.FIREMAKING,
                amount = xp,
                gpReward = gp,
                logTitle = "Burned $logName at Campfire",
                logDesc = "Gained $xp Firemaking XP at POH Campfire!"
            )
            progressSkillContract(OsrsSkill.FIREMAKING, 1, targetLogId)
        }
    }

    fun cookRawFoodAtRange(targetFoodId: String? = null, isAfk: Boolean = false, quantity: Int = 1) {
        if (!isAfk) {
            if (!canStartAfkOrHungerAction("Cooking Range")) return
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Cooking.")
            }
        }
        viewModelScope.launch {
            val cookingXp = skillXpMap.value[OsrsSkill.COOKING] ?: 0L
            val cookingLvl = OsrsXpCalculator.getLevelForXp(cookingXp)
            val queue = _cookingQueue.value

            fun isItemAvailableAndUnlocked(id: String): Boolean {
                val cauldronRec = com.example.data.models.CauldronRecipes.ALL_RECIPES.find { it.id == id }
                return if (cauldronRec != null) {
                    val q1 = getItemQuantityCombined(cauldronRec.requiredRawItemId)
                    val q2 = getItemQuantityCombined(cauldronRec.requiredItem2Id)
                    cookingLvl >= cauldronRec.reqLevel && minOf(q1, q2) > 0
                } else {
                    val rec = com.example.data.models.CookingRecipes.findRecipe(id)
                    val req = rec?.reqLevel ?: 1
                    cookingLvl >= req && getItemQuantityCombined(id) > 0
                }
            }

            val targetId: String? = if (isAfk) {
                if (queue.isNotEmpty()) {
                    queue.firstOrNull { isItemAvailableAndUnlocked(it) }
                } else {
                    if (targetFoodId != null && isItemAvailableAndUnlocked(targetFoodId)) targetFoodId else null
                }
            } else {
                if (targetFoodId != null && isItemAvailableAndUnlocked(targetFoodId)) {
                    targetFoodId
                } else {
                    queue.firstOrNull { isItemAvailableAndUnlocked(it) }
                }
            }

            if (targetId == null) {
                if (isAfkCookingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔥 Cooking Fire: All queued recipes and raw food have been cooked! AFK Cooking finished.")
                    notifyAfkStopped("Cooking Fire", "All queued food and recipes in your Storage have been cooked!")
                } else if (!isAfk) {
                    addChatMessage("🍳 Cooking Fire: No cookable raw food or recipe ingredients in Storage! Catch fish or gather ingredients first.")
                }
                return@launch
            }

            val emberLvl = npcFavorMap.value["ember"]?.first ?: getNpcFavorLevel("ember")
            val cauldronRec = com.example.data.models.CauldronRecipes.ALL_RECIPES.find { it.id == targetId }
            val currentPetType = petState.value.petType.name
            val numToCook = if (isAfk) 1 else quantity.coerceAtLeast(1)

            if (cauldronRec != null) {
                if (cookingLvl < cauldronRec.reqLevel) {
                    if (!isAfk) {
                        addChatMessage("🔒 Cannot cook ${cauldronRec.name}: Requires Level ${cauldronRec.reqLevel} Cooking!")
                    } else {
                        addChatMessage("🔒 Skipped ${cauldronRec.name} in Cooking Queue: Requires Level ${cauldronRec.reqLevel} Cooking!")
                    }
                    return@launch
                }

                var cookedCount = 0
                for (i in 1..numToCook) {
                    val has1 = getItemQuantityCombined(cauldronRec.requiredRawItemId) > 0
                    val has2 = getItemQuantityCombined(cauldronRec.requiredItem2Id) > 0
                    if (!has1 || !has2) break

                    val d1 = repository.deductItem(currentPetType, cauldronRec.requiredRawItemId, 1)
                    val d2 = repository.deductItem(currentPetType, cauldronRec.requiredItem2Id, 1)
                    if (!d1 || !d2) break

                    var producedQty = 1
                    if ((1..100).random() <= emberLvl) {
                        producedQty = 2
                        addChatMessage("✨ [Ember's Favor Perk (+${emberLvl}%)]: Double feast cooked! (+1 extra ${cauldronRec.name}) 🔥🍲")
                    }

                    repository.addBankItem(currentPetType, cauldronRec.id, producedQty)
                    cookedCount++

                    addXpAndNotify(
                        skill = OsrsSkill.COOKING,
                        amount = cauldronRec.cookingXp,
                        gpReward = 25L,
                        logTitle = "Cooked ${cauldronRec.name}",
                        logDesc = if (isAfk) "Cooked 1x ${cauldronRec.name} (+${cauldronRec.cookingXp} Cooking XP)" else "Cooked ${cauldronRec.name} (+${cauldronRec.cookingXp} Cooking XP)"
                    )
                    progressSkillContract(OsrsSkill.COOKING, 1, cauldronRec.id)
                }

                if (cookedCount > 0 && isAfk) {
                    val q1Rem = getItemQuantityCombined(cauldronRec.requiredRawItemId)
                    val q2Rem = getItemQuantityCombined(cauldronRec.requiredItem2Id)
                    if (minOf(q1Rem, q2Rem) <= 0) {
                        val nextQueued = _cookingQueue.value.firstOrNull { isItemAvailableAndUnlocked(it) }
                        if (nextQueued != null) {
                            val nextRec = com.example.data.models.CauldronRecipes.ALL_RECIPES.find { it.id == nextQueued }
                            val nextRawRec = com.example.data.models.CookingRecipes.findRecipe(nextQueued)
                            val nextName = nextRec?.name ?: nextRawRec?.rawName ?: nextQueued
                            addChatMessage("🍳 Finished all ingredients for ${cauldronRec.name}! Advancing to next item in Cooking Queue: $nextName.")
                        } else {
                            AfkEngine.stopAll(pohPrefs)
                            addChatMessage("🔥 Cooking Fire: All queued recipes and raw food have been cooked! AFK Cooking finished.")
                            notifyAfkStopped("Cooking Fire", "All queued recipes and raw food in your Storage have been cooked!")
                        }
                    }
                }
            } else {
                val rawItemDef = DefaultItems.ALL.find { it.id == targetId }
                val recipe = com.example.data.models.CookingRecipes.findRecipe(targetId)
                val reqLvl = recipe?.reqLevel ?: 1
                val rawName = recipe?.rawName ?: rawItemDef?.name ?: "Raw Food"
                val cookedName = recipe?.cookedName ?: ("Cooked " + rawName.removePrefix("Raw ").removePrefix("raw "))
                val cookedItemCode = recipe?.cookedId ?: (if (targetId.startsWith("item_raw_")) targetId.replace("item_raw_", "item_") else "item_cooked_$targetId")
                val xpEarned = recipe?.xpEarned ?: 40L

                if (cookingLvl < reqLvl) {
                    if (!isAfk) {
                        addChatMessage("🔒 Cannot cook $rawName: Requires Level $reqLvl Cooking!")
                    } else {
                        addChatMessage("🔒 Skipped $rawName in Cooking Queue: Requires Level $reqLvl Cooking!")
                    }
                    return@launch
                }

                var cookedCount = 0
                for (i in 1..numToCook) {
                    val deducted = repository.deductItem(currentPetType, targetId, 1)
                    if (!deducted) break

                    var producedQty = 1
                    if ((1..100).random() <= emberLvl) {
                        producedQty = 2
                        addChatMessage("✨ [Ember's Favor Perk (+${emberLvl}%)]: Double food cooked! (+1 extra $cookedName) 🔥🍳")
                    }

                    repository.addBankItem(currentPetType, cookedItemCode, producedQty)
                    cookedCount++

                    addXpAndNotify(
                        skill = OsrsSkill.COOKING,
                        amount = xpEarned,
                        gpReward = 15L,
                        logTitle = "Cooked $cookedName",
                        logDesc = if (isAfk) "Cooked 1x $cookedName (+${xpEarned}XP)" else "Cooked $cookedName (+${xpEarned}XP)"
                    )
                    progressSkillContract(OsrsSkill.COOKING, 1, targetId)
                }

                if (cookedCount > 0 && isAfk) {
                    val remaining = getItemQuantityCombined(targetId)
                    if (remaining <= 0) {
                        val nextQueued = _cookingQueue.value.firstOrNull { isItemAvailableAndUnlocked(it) }
                        if (nextQueued != null) {
                            val nextRec = com.example.data.models.CauldronRecipes.ALL_RECIPES.find { it.id == nextQueued }
                            val nextRawRec = com.example.data.models.CookingRecipes.findRecipe(nextQueued)
                            val nextName = nextRec?.name ?: nextRawRec?.rawName ?: nextQueued
                            addChatMessage("🍳 Finished all $rawName! Advancing to next item in Cooking Queue: $nextName.")
                        } else {
                            AfkEngine.stopAll(pohPrefs)
                            addChatMessage("🔥 Cooking Fire: All queued raw food has been cooked! AFK Cooking finished.")
                            notifyAfkStopped("Cooking Fire", "All queued raw food in your Storage has been cooked!")
                        }
                    }
                }
            }
        }
    }

    fun setSpiritPoolArea(areaId: String) {
        val area = com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS.find { it.id == areaId } ?: return
        val fishingXp = skillXpMap.value[OsrsSkill.FISHING] ?: 0L
        val fishingLvl = OsrsXpCalculator.getLevelForXp(fishingXp)
        if (fishingLvl < area.reqLevel) {
            addChatMessage("🔒 Cannot switch area: Level ${area.reqLevel} Fishing required for ${area.name} (You are Level $fishingLvl)!")
            return
        }
        if (!isTotemUnlocked(area.reqTotemId)) {
            val reqName = area.reqTotemName ?: "Obelisk"
            addChatMessage("🗿 Obelisk Locked: Requires the $reqName to access ${area.name}!")
            return
        }
        _selectedSpiritPoolAreaId.value = areaId
        if (isAfkFishingActive.value) {
            addChatMessage("🌊 AFK Fishing switched to ${area.emoji} ${area.name}! (Drops randomized from area drop table)")
        } else {
            addChatMessage("🌊 Selected Fishing Area: ${area.emoji} ${area.name}")
        }
        saveAfkStateToPrefs()
    }

    private val _combatEnergyMax = MutableStateFlow(3)
    val combatEnergyMax: StateFlow<Int> = _combatEnergyMax.asStateFlow()

    private val _combatEnergyCurrent = MutableStateFlow(3)
    val combatEnergyCurrent: StateFlow<Int> = _combatEnergyCurrent.asStateFlow()

    private val _combatNextAttackBonusDmg = MutableStateFlow(0)
    val combatNextAttackBonusDmg: StateFlow<Int> = _combatNextAttackBonusDmg.asStateFlow()

    private val _combatHand = MutableStateFlow<List<com.example.data.models.CombatCard>>(emptyList())
    val combatHand: StateFlow<List<com.example.data.models.CombatCard>> = _combatHand.asStateFlow()

    private val _combatDrawDeck = MutableStateFlow<List<com.example.data.models.CombatCard>>(emptyList())
    val combatDrawDeck: StateFlow<List<com.example.data.models.CombatCard>> = _combatDrawDeck.asStateFlow()

    private val _combatDiscardPile = MutableStateFlow<List<com.example.data.models.CombatCard>>(emptyList())
    val combatDiscardPile: StateFlow<List<com.example.data.models.CombatCard>> = _combatDiscardPile.asStateFlow()

    private val _combatPlayerShield = MutableStateFlow(0)
    val combatPlayerShield: StateFlow<Int> = _combatPlayerShield.asStateFlow()

    private val _customDeckCardIds = MutableStateFlow<Set<String>>(pohPrefs.getStringSet("custom_deck_card_ids", emptySet()) ?: emptySet())
    val customDeckCardIds: StateFlow<Set<String>> = _customDeckCardIds.asStateFlow()

    private val _savedDeckLoadouts = MutableStateFlow<List<com.example.data.models.SavedDeckLoadout>>(loadSavedDeckLoadoutsFromPrefs())
    val savedDeckLoadouts: StateFlow<List<com.example.data.models.SavedDeckLoadout>> = _savedDeckLoadouts.asStateFlow()

    private val _activeDeckLoadoutId = MutableStateFlow<String?>(pohPrefs.getString("active_deck_loadout_id", "preset_melee_warrior"))
    val activeDeckLoadoutId: StateFlow<String?> = _activeDeckLoadoutId.asStateFlow()

    private fun loadSavedDeckLoadoutsFromPrefs(): List<com.example.data.models.SavedDeckLoadout> {
        val defaultList = com.example.data.models.ArchetypeDeckPresets.PRESETS.toMutableList()
        val jsonStr = pohPrefs.getString("saved_deck_loadouts_json", null)
        if (jsonStr.isNullOrBlank()) return defaultList
        return try {
            val arr = org.json.JSONArray(jsonStr)
            val customList = mutableListOf<com.example.data.models.SavedDeckLoadout>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val cardArray = obj.optJSONArray("cardIds") ?: org.json.JSONArray()
                val cardIds = mutableListOf<String>()
                for (j in 0 until cardArray.length()) {
                    cardIds.add(cardArray.getString(j))
                }
                customList.add(
                    com.example.data.models.SavedDeckLoadout(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.optString("name", "Custom Deck"),
                        iconEmoji = obj.optString("iconEmoji", "🎴"),
                        stance = obj.optString("stance", "ALL"),
                        cardIds = cardIds,
                        isPreset = obj.optBoolean("isPreset", false),
                        description = obj.optString("description", "")
                    )
                )
            }
            val combined = defaultList.toMutableList()
            for (custom in customList) {
                if (combined.none { it.id == custom.id }) {
                    combined.add(custom)
                }
            }
            combined
        } catch (e: Exception) {
            defaultList
        }
    }

    private fun persistDeckLoadouts() {
        try {
            val arr = org.json.JSONArray()
            for (loadout in _savedDeckLoadouts.value) {
                val obj = org.json.JSONObject()
                obj.put("id", loadout.id)
                obj.put("name", loadout.name)
                obj.put("iconEmoji", loadout.iconEmoji)
                obj.put("stance", loadout.stance)
                obj.put("isPreset", loadout.isPreset)
                obj.put("description", loadout.description)
                val cardArr = org.json.JSONArray()
                for (cid in loadout.cardIds) {
                    cardArr.put(cid)
                }
                obj.put("cardIds", cardArr)
                arr.put(obj)
            }
            pohPrefs.edit()
                .putString("saved_deck_loadouts_json", arr.toString())
                .putString("active_deck_loadout_id", _activeDeckLoadoutId.value)
                .putStringSet("custom_deck_card_ids", _customDeckCardIds.value)
                .apply()
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun saveCurrentDeckAsLoadout(name: String, iconEmoji: String = "🎴", stance: String = "ALL"): com.example.data.models.SavedDeckLoadout {
        val newId = "deck_${System.currentTimeMillis()}"
        val newLoadout = com.example.data.models.SavedDeckLoadout(
            id = newId,
            name = name.ifBlank { "Custom Deck" },
            iconEmoji = iconEmoji,
            stance = stance,
            cardIds = _customDeckCardIds.value.toList(),
            isPreset = false,
            description = "${_customDeckCardIds.value.size} cards • $stance stance"
        )
        val updated = _savedDeckLoadouts.value.toMutableList().apply { add(newLoadout) }
        _savedDeckLoadouts.value = updated
        _activeDeckLoadoutId.value = newId
        persistDeckLoadouts()
        addChatMessage("💾 Saved new deck profile: '$name' $iconEmoji with ${_customDeckCardIds.value.size} cards!")
        return newLoadout
    }

    fun updateDeckLoadout(loadoutId: String, newName: String, newCardIds: Set<String>, stance: String, iconEmoji: String) {
        val currentList = _savedDeckLoadouts.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == loadoutId }
        if (index != -1) {
            val updated = currentList[index].copy(
                name = newName.ifBlank { currentList[index].name },
                iconEmoji = iconEmoji,
                stance = stance,
                cardIds = newCardIds.toList(),
                description = "${newCardIds.size} cards • $stance stance"
            )
            currentList[index] = updated
            _savedDeckLoadouts.value = currentList
            if (_activeDeckLoadoutId.value == loadoutId) {
                _customDeckCardIds.value = newCardIds
                initCombatDeckForCurrentStance(forceReset = true)
            }
            persistDeckLoadouts()
            addChatMessage("💾 Updated deck profile: '$newName' ($iconEmoji) with ${newCardIds.size} cards!")
        }
    }

    fun loadDeckLoadout(loadoutId: String) {
        val loadout = _savedDeckLoadouts.value.find { it.id == loadoutId }
            ?: com.example.data.models.ArchetypeDeckPresets.PRESETS.find { it.id == loadoutId }
            ?: return

        _customDeckCardIds.value = loadout.cardIds.toSet()
        _activeDeckLoadoutId.value = loadout.id
        if (loadout.stance != "ALL" && loadout.stance != _adventuringCombatStance.value) {
            setAdventuringCombatStance(loadout.stance, endTurn = false)
        } else {
            initCombatDeckForCurrentStance(forceReset = true)
        }
        persistDeckLoadouts()
        addChatMessage("🎴 Equipped deck: '${loadout.name}' ${loadout.iconEmoji} (${loadout.cardIds.size} cards)")
    }

    fun deleteDeckLoadout(loadoutId: String) {
        val loadout = _savedDeckLoadouts.value.find { it.id == loadoutId } ?: return
        if (loadout.isPreset) {
            addChatMessage("⚠️ Preset archetype decks cannot be deleted.")
            return
        }
        val updated = _savedDeckLoadouts.value.filter { it.id != loadoutId }
        _savedDeckLoadouts.value = updated
        if (_activeDeckLoadoutId.value == loadoutId) {
            _activeDeckLoadoutId.value = updated.firstOrNull()?.id
        }
        persistDeckLoadouts()
        addChatMessage("🗑️ Deleted deck: '${loadout.name}'")
    }

    fun duplicateDeckLoadout(loadoutId: String) {
        val original = _savedDeckLoadouts.value.find { it.id == loadoutId }
            ?: com.example.data.models.ArchetypeDeckPresets.PRESETS.find { it.id == loadoutId }
            ?: return
        val newId = "deck_${System.currentTimeMillis()}"
        val copy = original.copy(
            id = newId,
            name = "${original.name} (Copy)",
            isPreset = false
        )
        val updated = _savedDeckLoadouts.value.toMutableList().apply { add(copy) }
        _savedDeckLoadouts.value = updated
        _activeDeckLoadoutId.value = newId
        _customDeckCardIds.value = copy.cardIds.toSet()
        persistDeckLoadouts()
        addChatMessage("📋 Duplicated deck: '${copy.name}'")
    }

    fun renameDeckLoadout(loadoutId: String, newName: String) {
        val currentList = _savedDeckLoadouts.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == loadoutId }
        if (index != -1) {
            val updated = currentList[index].copy(
                name = newName.ifBlank { currentList[index].name }
            )
            currentList[index] = updated
            _savedDeckLoadouts.value = currentList
            persistDeckLoadouts()
            addChatMessage("✏️ Renamed deck to '${updated.name}'")
        } else {
            val preset = com.example.data.models.ArchetypeDeckPresets.PRESETS.find { it.id == loadoutId }
            if (preset != null) {
                val newId = "deck_${System.currentTimeMillis()}"
                val copy = preset.copy(
                    id = newId,
                    name = newName.ifBlank { preset.name },
                    isPreset = false
                )
                currentList.add(copy)
                _savedDeckLoadouts.value = currentList
                _activeDeckLoadoutId.value = newId
                _customDeckCardIds.value = copy.cardIds.toSet()
                persistDeckLoadouts()
                addChatMessage("✏️ Created and renamed custom deck: '${copy.name}'")
            }
        }
    }

    fun setCustomDeckCards(cardIds: Set<String>) {
        _customDeckCardIds.value = cardIds
        persistDeckLoadouts()
        initCombatDeckForCurrentStance(forceReset = true)
    }

    fun clearCustomDeck() {
        _customDeckCardIds.value = emptySet()
        persistDeckLoadouts()
        initCombatDeckForCurrentStance(forceReset = true)
        addChatMessage("🧹 Active custom deck cleared (reverted to default combat deck).")
    }

    fun fillRecommendedDeckForStance(targetStance: String = _adventuringCombatStance.value) {
        val recommended = com.example.data.models.DefaultCombatCards.getDefaultDeckForStance(targetStance, skillXpMap.value)
        _customDeckCardIds.value = recommended.map { it.id }.toSet()
        persistDeckLoadouts()
        initCombatDeckForCurrentStance(forceReset = true)
        addChatMessage("✨ Auto-equipped recommended $targetStance deck (${_customDeckCardIds.value.size} cards)!")
    }

    fun setAdventuringCombatStance(stance: String, endTurn: Boolean = false) {
        if (_adventuringCombatStance.value == stance && !endTurn) return
        _adventuringCombatStance.value = stance
        pohPrefs.edit().putString("adventuring_combat_stance", stance).apply()

        initCombatDeckForCurrentStance(forceReset = true)

        val stanceLabel = when (stance) {
            "MELEE" -> "Melee ⚔️"
            "RANGED" -> "Blowdarts 🏹"
            "MAGIC" -> "Magic 🪄"
            else -> stance
        }

        val hasActiveMonster = _adventuringCurrentMonster.value != null && _adventuringMonsterHp.value > 0

        if (endTurn && hasActiveMonster) {
            val newLogs = _adventuringLog.value.toMutableList()
            newLogs.add(0, "🔄 Switched stance to $stanceLabel & ended turn!")
            _adventuringLog.value = newLogs.take(15)
            addChatMessage("🔄 Switched combat stance to $stanceLabel. Ended turn and enemy attacked.")
            endTurnAndDrawHand()
        } else {
            val newLogs = _adventuringLog.value.toMutableList()
            newLogs.add(0, "🔄 Set combat stance to $stanceLabel")
            _adventuringLog.value = newLogs.take(15)
            addChatMessage("🏹 Adventuring Stance set to: $stance")
        }
    }

    private fun drawHandWithGuaranteedCombatCards(
        drawPool: MutableList<com.example.data.models.CombatCard>,
        discardPool: MutableList<com.example.data.models.CombatCard>,
        targetHandSize: Int = 5
    ): Pair<List<com.example.data.models.CombatCard>, Pair<MutableList<com.example.data.models.CombatCard>, MutableList<com.example.data.models.CombatCard>>> {
        if (drawPool.size < targetHandSize) {
            drawPool.addAll(discardPool.shuffled())
            discardPool.clear()
        }
        val hand = mutableListOf<com.example.data.models.CombatCard>()
        val remainingDraw = drawPool.toMutableList()

        val stance = _adventuringCombatStance.value

        // Filter combat stance cards available in drawPool (e.g. MELEE: hand-combat/warding/life energy; RANGED: blow-darts/eating/life energy; MAGIC: incantation/warding/life energy)
        val combatCardsInDraw = remainingDraw.filter { it.isCombatCardForStance(stance) }.toMutableList()

        // Guarantee at least 2 combat stance cards in hand (if available)
        var combatNeeded = 2.coerceAtMost(combatCardsInDraw.size)

        while (combatNeeded > 0 && combatCardsInDraw.isNotEmpty()) {
            val combatCard = combatCardsInDraw.removeAt(0)
            remainingDraw.remove(combatCard)
            hand.add(combatCard)
            combatNeeded--
        }

        // Fill remaining hand up to targetHandSize (5)
        val additionalNeeded = (targetHandSize - hand.size).coerceAtMost(remainingDraw.size)
        for (i in 0 until additionalNeeded) {
            hand.add(remainingDraw.removeAt(0))
        }

        return Pair(hand.shuffled(), Pair(remainingDraw, discardPool))
    }

    fun initCombatDeckForCurrentStance(forceReset: Boolean = false) {
        if (!forceReset && _combatHand.value.isNotEmpty()) return
        val stance = _adventuringCombatStance.value
        val fullDeck = if (_customDeckCardIds.value.isNotEmpty()) {
            val custom = com.example.data.models.DefaultCombatCards.ALL_CARDS.filter { _customDeckCardIds.value.contains(it.id) }
            if (custom.size >= 5) custom else com.example.data.models.DefaultCombatCards.getDefaultDeckForStance(stance, skillXpMap.value)
        } else {
            com.example.data.models.DefaultCombatCards.getDefaultDeckForStance(stance, skillXpMap.value)
        }
        val drawPool = fullDeck.shuffled().toMutableList()
        val discardPool = mutableListOf<com.example.data.models.CombatCard>()

        val (initialHand, pools) = drawHandWithGuaranteedCombatCards(drawPool, discardPool, 5)

        _combatHand.value = initialHand
        _combatDrawDeck.value = pools.first
        _combatDiscardPile.value = pools.second
        _combatEnergyMax.value = 3
        _combatEnergyCurrent.value = 3
        _combatPlayerShield.value = 0
        _combatNextAttackBonusDmg.value = 0
    }

    fun toggleCustomDeckCard(cardId: String) {
        val current = _customDeckCardIds.value.toMutableSet()
        if (current.contains(cardId)) {
            current.remove(cardId)
        } else {
            current.add(cardId)
        }
        _customDeckCardIds.value = current
        initCombatDeckForCurrentStance(forceReset = true)
    }

    fun playCombatCard(card: com.example.data.models.CombatCard) {
        if (_combatEnergyCurrent.value < card.energyCost) {
            addChatMessage("⚠️ Not enough ⚡ Energy! End turn to draw new cards and restore energy.")
            return
        }

        val monster = _adventuringCurrentMonster.value ?: return

        // Deduct energy & move card to discard pile
        _combatEnergyCurrent.value -= card.energyCost
        _combatHand.value = _combatHand.value.filter { it != card }
        _combatDiscardPile.value = _combatDiscardPile.value + card

        if (card.nextAttackBuff > 0) {
            _combatNextAttackBonusDmg.value += card.nextAttackBuff
            val newLogs = _adventuringLog.value.toMutableList()
            newLogs.add(0, "✨ ${card.iconEmoji} Played ${card.title}: Buffed next attack +${card.nextAttackBuff} Dmg!")
            _adventuringLog.value = newLogs.take(15)
        }

        // Calculate card effectiveness based on stats
        val petAtk = skillXpMap.value[OsrsSkill.ATTACK] ?: 0L
        val petRng = skillXpMap.value[OsrsSkill.RANGED] ?: 0L
        val petMag = skillXpMap.value[OsrsSkill.MAGIC] ?: 0L
        val atkLvl = OsrsXpCalculator.getLevelForXp(petAtk)
        val rngLvl = OsrsXpCalculator.getLevelForXp(petRng)
        val magLvl = OsrsXpCalculator.getLevelForXp(petMag)

        val statBonus = when (card.stance) {
            "RANGED" -> (rngLvl / 3) + (atkLvl / 8)
            "MAGIC" -> (magLvl / 3)
            "MELEE" -> (atkLvl / 3)
            else -> (atkLvl / 4)
        }

        val activeNextAtkBuff = _combatNextAttackBonusDmg.value

        val damageDealt = if (card.baseDamage > 0) {
            val totalRawDmg = card.baseDamage + statBonus.toInt() + activeNextAtkBuff + (-2..4).random()
            (totalRawDmg - (monster.defence / 3)).coerceAtLeast(6)
        } else 0

        // Determine skill & award card XP
        val isSummonCard = card.skill == OsrsSkill.FIREMAKING || card.id.startsWith("summoning_")
        val (cardSkill, cardSkillName) = when {
            isSummonCard -> OsrsSkill.FIREMAKING to "Summoning"
            card.skill == OsrsSkill.HERBLORE || card.id.startsWith("herbalism_") -> OsrsSkill.HERBLORE to "Herbalism"
            card.skill == OsrsSkill.MAGIC -> OsrsSkill.MAGIC to "Incantations"
            card.skill == OsrsSkill.HITPOINTS -> OsrsSkill.HITPOINTS to "Hitpoints"
            card.skill == OsrsSkill.DEFENCE -> OsrsSkill.DEFENCE to "Defence"
            card.skill != OsrsSkill.ATTACK -> card.skill to card.skill.displayName
            else -> {
                val stanceToUse = if (card.stance == "ALL") _adventuringCombatStance.value else card.stance
                when (stanceToUse) {
                    "RANGED" -> OsrsSkill.RANGED to "Blowdarts"
                    "MAGIC" -> OsrsSkill.MAGIC to "Incantations"
                    else -> OsrsSkill.ATTACK to "Hand Combat"
                }
            }
        }

        val cardPlayXp = if (damageDealt > 0) {
            (damageDealt * 1.5).toLong().coerceAtLeast(15L)
        } else {
            (25L + card.reqLevel * 5L)
        }

        addXpAndNotify(
            skill = cardSkill,
            amount = cardPlayXp,
            gpReward = 0L,
            logTitle = "$cardSkillName Card",
            logDesc = "Played ${card.title} (+${cardPlayXp} $cardSkillName XP)"
        )

        // If playing a Summoning card, automatically activate the spirit companion for battle!
        if (isSummonCard) {
            val rawTitle = card.title.replace("Summon ", "").replace(Regex(" Lv \\d+"), "").trim()
            val matchingAnimal = com.example.data.models.SummoningData.ALL_ANIMALS.find {
                rawTitle.contains(it.name, ignoreCase = true) || it.name.contains(rawTitle, ignoreCase = true)
            } ?: com.example.data.models.SummoningData.ALL_ANIMALS.first()

            val durSec = 600
            _activeSummon.value = com.example.data.models.ActiveSummoningCompanion(
                animalId = matchingAnimal.id,
                animalName = matchingAnimal.name,
                iconEmoji = matchingAnimal.iconEmoji,
                benefitText = matchingAnimal.benefitText,
                startTimeMillis = System.currentTimeMillis(),
                durationSeconds = durSec,
                remainingSeconds = durSec,
                runesMultiplier = matchingAnimal.runesMultiplier,
                expeditionTimeReductionPercent = matchingAnimal.expeditionTimeReductionPercent,
                questTimeReductionPercent = matchingAnimal.questTimeReductionPercent,
                skillingXpBonusPercent = matchingAnimal.skillingXpBonusPercent,
                extraIncantationSlots = matchingAnimal.extraIncantationSlots
            )
            val newLogs = _adventuringLog.value.toMutableList()
            newLogs.add(0, "🐺 Spirit Ally Invoked: ${matchingAnimal.name} ${matchingAnimal.iconEmoji} enters battle! (${matchingAnimal.benefitText})")
            _adventuringLog.value = newLogs.take(15)
        }

        if (damageDealt > 0) {
            if (activeNextAtkBuff > 0) {
                _combatNextAttackBonusDmg.value = 0 // Reset next attack bonus after dealing damage
            }
            val newHp = (_adventuringMonsterHp.value - damageDealt).coerceAtLeast(0)
            _adventuringMonsterHp.value = newHp

            val newLogs = _adventuringLog.value.toMutableList()
            val buffTxt = if (activeNextAtkBuff > 0) " (incl. +$activeNextAtkBuff Buff)" else ""
            newLogs.add(0, "🃏 ${card.iconEmoji} Played ${card.title}: Dealt $damageDealt damage$buffTxt to ${monster.name}!")
            _adventuringLog.value = newLogs.take(15)

            if (newHp <= 0) {
                newLogs.add(0, "🏆 Defeated ${monster.name} ${monster.emoji}! +${monster.gpReward} GP!")
                _adventuringLog.value = newLogs.take(15)

                // Defeating dungeon monsters grants Combat XP and GP, but NEVER Adventuring XP
                val combatSkill = when (_adventuringCombatStance.value) {
                    "RANGED" -> OsrsSkill.RANGED
                    "MAGIC" -> OsrsSkill.MAGIC
                    else -> OsrsSkill.ATTACK
                }
                addXpAndNotify(
                    skill = combatSkill,
                    amount = monster.xpReward,
                    gpReward = monster.gpReward,
                    logTitle = "Defeated ${monster.name}",
                    logDesc = monster.storyLore
                )

                val currentFloorData = com.example.data.models.AdventuringStoryData.getFloor(_adventuringFloor.value)
                val allDungeonMonsters = currentFloorData.monsters + listOf(currentFloorData.boss)
                val currentIdx = allDungeonMonsters.indexOfFirst { it.id == monster.id }
                if (currentIdx >= 0 && currentIdx < allDungeonMonsters.size - 1) {
                    val nextMonster = allDungeonMonsters[currentIdx + 1]
                    _adventuringCurrentMonster.value = nextMonster
                    _adventuringMonsterHp.value = nextMonster.hp
                    val nextLogs = _adventuringLog.value.toMutableList()
                    if (nextMonster.id == currentFloorData.boss.id) {
                        nextLogs.add(0, "⚠️ DUNGEON BOSS ENCOUNTER: ${nextMonster.name} ${nextMonster.emoji} appears!")
                    } else {
                        nextLogs.add(0, "⚔️ DUNGEON ENCOUNTER (${currentIdx + 2}/${allDungeonMonsters.size}): ${nextMonster.name} ${nextMonster.emoji} appears!")
                    }
                    _adventuringLog.value = nextLogs.take(15)
                } else {
                    advanceAdventuringFloor()
                }

                initCombatDeckForCurrentStance(forceReset = true)
                return
            }
        }

        if (card.baseShield > 0) {
            _combatPlayerShield.value += card.baseShield
            val newLogs = _adventuringLog.value.toMutableList()
            newLogs.add(0, "🛡️ ${card.iconEmoji} Played ${card.title}: +${card.baseShield} Shield Block!")
            _adventuringLog.value = newLogs.take(15)
        }

        if (card.baseHeal > 0) {
            _adventuringPetHp.value = (_adventuringPetHp.value + card.baseHeal).coerceAtMost(_adventuringPetMaxHp.value)
            val newLogs = _adventuringLog.value.toMutableList()
            newLogs.add(0, "💖 ${card.iconEmoji} Played ${card.title}: Healed +${card.baseHeal} HP!")
            _adventuringLog.value = newLogs.take(15)
        }
    }

    fun endTurnAndDrawHand() {
        val monster = _adventuringCurrentMonster.value ?: return

        val attackCards = monster.effectiveAttackCards
        val chosenCard = attackCards.random()
        _enemyLastPlayedAttack.value = chosenCard
        _enemyAttackTrigger.value += 1

        var cardDmg = chosenCard.damagePower
        if (chosenCard.specialEffect == "CRITICAL") {
            cardDmg = (cardDmg * 1.4).toInt()
        }

        val currentShield = _combatPlayerShield.value
        val shieldAbsorbed = if (chosenCard.shieldPierce) 0 else cardDmg.coerceAtMost(currentShield)
        val remainingMonsterDmg = cardDmg - shieldAbsorbed

        if (!chosenCard.shieldPierce) {
            _combatPlayerShield.value = (currentShield - shieldAbsorbed).coerceAtLeast(0)
        }

        if (remainingMonsterDmg > 0) {
            _adventuringPetHp.value = (_adventuringPetHp.value - remainingMonsterDmg).coerceAtLeast(0)
        }

        if (chosenCard.specialEffect == "LIFESTEAL" && remainingMonsterDmg > 0) {
            val healAmt = (remainingMonsterDmg * 0.5).toInt().coerceAtLeast(5)
            _adventuringMonsterHp.value = (_adventuringMonsterHp.value + healAmt).coerceAtMost(monster.maxHp)
        } else if (chosenCard.specialEffect == "HEAL") {
            val healAmt = 15 + (monster.floorLevel * 4)
            _adventuringMonsterHp.value = (_adventuringMonsterHp.value + healAmt).coerceAtMost(monster.maxHp)
        }

        val newLogs = _adventuringLog.value.toMutableList()
        val attackPrefix = if (monster.id.startsWith("b_")) "👑 BOSS ATTACK" else "💥 ENEMY ATTACK"
        if (chosenCard.shieldPierce) {
            newLogs.add(0, "$attackPrefix: ${monster.name} played [${chosenCard.name} ${chosenCard.emoji}]! Bypassed shield for $remainingMonsterDmg PIERCING DMG!")
        } else if (shieldAbsorbed > 0) {
            newLogs.add(0, "$attackPrefix: ${monster.name} played [${chosenCard.name} ${chosenCard.emoji}]! Shield blocked $shieldAbsorbed DMG (Took $remainingMonsterDmg HP DMG).")
        } else {
            newLogs.add(0, "$attackPrefix: ${monster.name} played [${chosenCard.name} ${chosenCard.emoji}] for $remainingMonsterDmg DMG!")
        }
        _adventuringLog.value = newLogs.take(15)

        if (_adventuringPetHp.value <= 0) {
            _adventuringPetHp.value = _adventuringPetMaxHp.value / 2
            val retreatLogs = _adventuringLog.value.toMutableList()
            retreatLogs.add(0, "💔 Your companion retreated to recover health!")
            _adventuringLog.value = retreatLogs.take(15)
            addChatMessage("💔 Adventuring: Your pet was injured and retreated to recover!")
            initCombatDeckForCurrentStance(forceReset = true)
            return
        }

        val discardedHand = _combatHand.value
        _combatDiscardPile.value = _combatDiscardPile.value + discardedHand
        _combatHand.value = emptyList()

        // Active companion assist attack
        val activeSummon = _activeSummon.value
        if (activeSummon != null && _adventuringMonsterHp.value > 0) {
            val companionDmg = (18 + (activeSummon.skillingXpBonusPercent * 2) + (-2..4).random()).coerceAtLeast(10)
            val newHp = (_adventuringMonsterHp.value - companionDmg).coerceAtLeast(0)
            _adventuringMonsterHp.value = newHp
            val logs = _adventuringLog.value.toMutableList()
            logs.add(0, "🐾 Companion Assist: ${activeSummon.animalName} ${activeSummon.iconEmoji} strikes ${monster.name} dealing $companionDmg Spirit Damage!")
            _adventuringLog.value = logs.take(15)
        }

        _combatEnergyCurrent.value = _combatEnergyMax.value
        _combatPlayerShield.value = (_combatPlayerShield.value / 2)

        var drawPool = _combatDrawDeck.value.toMutableList()
        var discardPool = _combatDiscardPile.value.toMutableList()

        val (newHand, pools) = drawHandWithGuaranteedCombatCards(drawPool, discardPool, 5)

        _combatHand.value = newHand
        _combatDrawDeck.value = pools.first
        _combatDiscardPile.value = pools.second
    }

    fun rangedAttackAdventuringMonster() {
        setAdventuringCombatStance("RANGED")
        attackAdventuringMonster()
    }

    fun attackAdventuringMonster() {
        viewModelScope.launch {
            val monster = _adventuringCurrentMonster.value ?: return@launch
            val stance = _adventuringCombatStance.value
            val petAtk = skillXpMap.value[OsrsSkill.ATTACK] ?: 0L
            val petRng = skillXpMap.value[OsrsSkill.RANGED] ?: 0L
            val petMag = skillXpMap.value[OsrsSkill.MAGIC] ?: 0L
            val atkLvl = OsrsXpCalculator.getLevelForXp(petAtk)
            val rngLvl = OsrsXpCalculator.getLevelForXp(petRng)
            val magLvl = OsrsXpCalculator.getLevelForXp(petMag)
            
            val baseDmg = when (stance) {
                "RANGED" -> (15 + (rngLvl / 2) + (atkLvl / 6)).coerceAtLeast(9)
                "MAGIC" -> (16 + (magLvl / 2)).coerceAtLeast(10)
                else -> (14 + (atkLvl / 2)).coerceAtLeast(8)
            }
            val variance = (-4..6).random()
            var styleMult = 1.0
            val rangedMult = when {
                isIncantationActiveAndUsable("incant_wind_precision_t3") -> 1.40
                isIncantationActiveAndUsable("incant_wind_precision_t2") -> 1.25
                isIncantationActiveAndUsable("incant_wind_precision") -> 1.15
                else -> 1.0
            }
            val meleeMult = when {
                isIncantationActiveAndUsable("incant_heavy_blade_t3") -> 1.40
                isIncantationActiveAndUsable("incant_heavy_blade_t2") -> 1.25
                isIncantationActiveAndUsable("incant_heavy_blade") -> 1.15
                else -> 1.0
            }
            val magicMult = when {
                isIncantationActiveAndUsable("incant_starlight_glimmer_t3") -> 1.40
                isIncantationActiveAndUsable("incant_starlight_glimmer_t2") -> 1.25
                isIncantationActiveAndUsable("incant_starlight_glimmer") -> 1.15
                else -> 1.0
            }

            if (stance == "RANGED") styleMult *= rangedMult
            if (stance == "MELEE") styleMult *= meleeMult
            if (stance == "MAGIC") styleMult *= magicMult
            val windMult = 1.0

            val defDivider = 2
            var bonusDmg = 0

            val damageDealt = (((baseDmg + variance - monster.defence / defDivider) * styleMult * windMult).toInt() + bonusDmg).coerceAtLeast(6)

            if (stance == "MELEE" && isIncantationActiveAndUsable("incant_brawlers_vitality")) {
                _adventuringPetHp.value = (_adventuringPetHp.value + 8).coerceAtMost(_adventuringPetMaxHp.value)
            }

            val currentMonsterHp = _adventuringMonsterHp.value
            val newMonsterHp = (currentMonsterHp - damageDealt).coerceAtLeast(0)
            _adventuringMonsterHp.value = newMonsterHp

            val now = System.currentTimeMillis()
            val hasLifestealBuff = _activeCookingBuffs.value.any { it.recipeId == "rec_raw_meat_teak" && it.expiryTimeMs > now }
            if (hasLifestealBuff && damageDealt > 0) {
                val lifestealHp = (damageDealt * 0.08).toInt().coerceAtLeast(1)
                _adventuringPetHp.value = (_adventuringPetHp.value + lifestealHp).coerceAtMost(_adventuringPetMaxHp.value)
            }

            val newLogs = _adventuringLog.value.toMutableList()
            if (hasLifestealBuff && damageDealt > 0) {
                val lifestealHp = (damageDealt * 0.08).toInt().coerceAtLeast(1)
                newLogs.add(0, "🩸 Lifesteal: Siphoned +$lifestealHp HP!")
            }
            val actionVerb = when (stance) {
                "RANGED" -> "🏹 You shot a ranged precision dart at"
                "MAGIC" -> "🪄 You unleashed a magic spell on"
                else -> "⚔️ You struck"
            }
            newLogs.add(0, "$actionVerb ${monster.name} ${monster.emoji} for $damageDealt damage!")

            // Award per-strike XP based on active combat stance
            val (strikeSkill, strikeSkillName) = when (stance) {
                "RANGED" -> OsrsSkill.RANGED to "Blowdarts"
                "MAGIC" -> OsrsSkill.MAGIC to "Incantations"
                else -> OsrsSkill.ATTACK to "Hand Combat"
            }
            val strikeXp = (damageDealt * 1.5).toLong().coerceAtLeast(15L)
            addXpAndNotify(
                skill = strikeSkill,
                amount = strikeXp,
                gpReward = 0L,
                logTitle = "$strikeSkillName Strike",
                logDesc = "Gained $strikeSkillName combat XP"
            )

            if (newMonsterHp <= 0) {
                newLogs.add(0, "🏆 Defeated ${monster.name} ${monster.emoji}! +${monster.gpReward} GP!")
                _adventuringLog.value = newLogs.take(15)

                val combatSkill = when (stance) {
                    "RANGED" -> OsrsSkill.RANGED
                    "MAGIC" -> OsrsSkill.MAGIC
                    else -> OsrsSkill.ATTACK
                }
                addXpAndNotify(
                    skill = combatSkill,
                    amount = monster.xpReward,
                    gpReward = monster.gpReward,
                    logTitle = "Defeated ${monster.name}",
                    logDesc = monster.storyLore
                )

                if (stance == "RANGED") {
                    addXpAndNotify(
                        skill = OsrsSkill.RANGED,
                        amount = monster.xpReward / 2,
                        gpReward = 0L,
                        logTitle = "Ranged Precision",
                        logDesc = "Gained ranged combat experience"
                    )
                } else if (stance == "MAGIC") {
                    addXpAndNotify(
                        skill = OsrsSkill.MAGIC,
                        amount = monster.xpReward / 2,
                        gpReward = 0L,
                        logTitle = "Arcane Burst",
                        logDesc = "Gained magic combat experience"
                    )
                } else {
                    addXpAndNotify(
                        skill = OsrsSkill.ATTACK,
                        amount = monster.xpReward / 2,
                        gpReward = 0L,
                        logTitle = "Hand Combat Strike",
                        logDesc = "Gained hand combat experience"
                    )
                }

                addXpAndNotify(
                    skill = OsrsSkill.HITPOINTS,
                    amount = monster.xpReward / 2,
                    gpReward = 0L,
                    logTitle = "Battle Vitality",
                    logDesc = "Gained combat experience"
                )

                val floorData = com.example.data.models.AdventuringStoryData.getFloor(_adventuringFloor.value)
                val allDungeonMonsters = floorData.monsters + floorData.boss
                val currentIdx = allDungeonMonsters.indexOfFirst { it.id == monster.id }
                if (currentIdx >= 0 && currentIdx < allDungeonMonsters.size - 1) {
                    val nextMonster = allDungeonMonsters[currentIdx + 1]
                    _adventuringCurrentMonster.value = nextMonster
                    _adventuringMonsterHp.value = nextMonster.hp
                    val nextLogs = _adventuringLog.value.toMutableList()
                    if (nextMonster.id == floorData.boss.id) {
                        nextLogs.add(0, "⚠️ DUNGEON BOSS ENCOUNTER: ${nextMonster.name} ${nextMonster.emoji} (Combat Lv. ${nextMonster.combatLevel}) appears!")
                    } else {
                        nextLogs.add(0, "⚔️ DUNGEON ENCOUNTER (${currentIdx + 2}/${allDungeonMonsters.size}): ${nextMonster.name} ${nextMonster.emoji} (Combat Lv. ${nextMonster.combatLevel}) appears!")
                    }
                    _adventuringLog.value = nextLogs.take(15)
                } else {
                    advanceAdventuringFloor()
                }
            } else {
                val defXp = skillXpMap.value[OsrsSkill.DEFENCE] ?: 0L
                val defLvl = OsrsXpCalculator.getLevelForXp(defXp)
                val rawMonsterDmg = (monster.attackPower - (defLvl / 2)).coerceAtLeast(2)
                val barkMult = if (isIncantationActiveAndUsable("incant_earth_bark")) 0.75 else 1.0
                val monsterDmg = (rawMonsterDmg * barkMult).toInt().coerceAtLeast(1)
                val currentPetHp = _adventuringPetHp.value
                val newPetHp = (currentPetHp - monsterDmg).coerceAtLeast(0)
                _adventuringPetHp.value = newPetHp

                newLogs.add(0, "💥 ${monster.name} counterattacked for $monsterDmg damage!")
                _adventuringLog.value = newLogs.take(15)

                if (newPetHp <= 0) {
                    _adventuringPetHp.value = _adventuringPetMaxHp.value / 2
                    val retreatLogs = _adventuringLog.value.toMutableList()
                    retreatLogs.add(0, "💔 Your companion retreated to recover health!")
                    _adventuringLog.value = retreatLogs.take(15)
                    addChatMessage("💔 Adventuring: Your pet was injured and retreated to recover!")
                }
            }
        }
    }

    fun guardAdventuringMonster() {
        val monster = _adventuringCurrentMonster.value ?: return
        val currentPetHp = _adventuringPetHp.value
        val monsterDmg = ((monster.attackPower - 4) / 3).coerceAtLeast(1)
        val newPetHp = (currentPetHp - monsterDmg).coerceAtLeast(0)
        _adventuringPetHp.value = newPetHp

        val newLogs = _adventuringLog.value.toMutableList()
        newLogs.add(0, "🛡️ You raised a spirit guard! Mitigated damage to $monsterDmg.")
        _adventuringLog.value = newLogs.take(15)

        addXpAndNotify(
            skill = OsrsSkill.DEFENCE,
            amount = 40L,
            gpReward = 0L,
            logTitle = "Spirit Guard",
            logDesc = "Successfully parried enemy strike"
        )
    }

    fun useAdventuringItemInDungeon(itemId: String) {
        viewModelScope.launch {
            val normId = com.example.data.models.DefaultItems.normalizeItemId(itemId)
            val invItem = inventoryItems.value.find { (it.id == itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normId) && it.quantity > 0 }
            val bankItem = bankItems.value.find { (it.id == itemId || com.example.data.models.DefaultItems.normalizeItemId(it.id) == normId) && it.quantity > 0 }
            val item = invItem ?: bankItem ?: return@launch
            val baseInfo = com.example.data.models.DefaultItems.getItemById(normId)

            val newLogs = _adventuringLog.value.toMutableList()

            if (item.id.contains("bone")) {
                val spiritXp = when (item.id) {
                    "item_dragon_bones" -> 600L
                    "item_big_bones" -> 300L
                    else -> 150L
                }
                if (invItem != null) {
                    saveInventoryItem(invItem.id, invItem.quantity - 1)
                } else if (bankItem != null) {
                    saveBankItem(bankItem.id, bankItem.quantity - 1)
                }
                addXpAndNotify(OsrsSkill.HITPOINTS, spiritXp, 0L, "Offering ${item.name}", "Offered ${item.name} in Sanctuary!")
                newLogs.add(0, "🦴 Offered ${item.name}! Gained +$spiritXp Life Energy XP!")
                _adventuringLog.value = newLogs.take(15)
                return@launch
            }

            val heal = when {
                item.healHp > 0 -> item.healHp
                baseInfo.healHp > 0 -> baseInfo.healHp
                item.restoreHunger > 0 -> (item.restoreHunger * 1.2).toInt().coerceAtLeast(15)
                item.id.startsWith("item_cooked_") || item.name.contains("Cooked", ignoreCase = true) -> 25
                else -> 20
            }
            val currentPetHp = _adventuringPetHp.value
            val maxHp = _adventuringPetMaxHp.value
            val newHp = (currentPetHp + heal).coerceAtMost(maxHp)
            _adventuringPetHp.value = newHp

            if (invItem != null) {
                saveInventoryItem(invItem.id, invItem.quantity - 1)
            } else if (bankItem != null) {
                saveBankItem(bankItem.id, bankItem.quantity - 1)
            }

            newLogs.add(0, "🧪 Consumed ${item.name} ${item.iconEmoji}! Restored +$heal HP!")
            _adventuringLog.value = newLogs.take(15)
        }
    }

    fun advanceAdventuringFloor() {
        val currentFloor = _adventuringFloor.value
        val nextFloorNum = (currentFloor + 1).coerceAtMost(99)
        _adventuringFloor.value = nextFloorNum
        if (nextFloorNum > _adventuringMaxFloor.value) {
            _adventuringMaxFloor.value = nextFloorNum
        }

        // Advance Adventuring Level exclusively upon clearing the corresponding floor!
        val targetAdventuringLevel = (currentFloor + 1).coerceIn(1, 99)
        val targetXp = com.example.data.models.OsrsXpCalculator.getXpForLevel(targetAdventuringLevel)
        val currentAdventuringXp = skillXpMap.value[OsrsSkill.ADVENTURING] ?: 0L
        if (targetXp > currentAdventuringXp) {
            val neededXp = targetXp - currentAdventuringXp
            val petType = petState.value.petType.name
            viewModelScope.launch {
                repository.addXpToSkillDirect(petType, OsrsSkill.ADVENTURING, neededXp, 0L, "Floor Clear", "Cleared Floor $targetAdventuringLevel")
                _levelUpEvent.value = LevelUpEvent(OsrsSkill.ADVENTURING, targetAdventuringLevel)
            }
        }

        // Roll 1 of 99 Random Skilling Set Prizes (Prioritize unowned pieces)
        val allPrizes: List<com.example.data.models.SkillOutfitPiece> = com.example.data.models.SkillOutfitData.DUNGEON_99_PRIZES
        val unlockedOutfitIds = petState.value.unlockedOutfitIds.toSet()
        val unownedPrizes = allPrizes.filter { !unlockedOutfitIds.contains(it.id) }
        val (wonPrize, isNewPiece) = if (unownedPrizes.isNotEmpty()) {
            unownedPrizes.random() to true
        } else {
            allPrizes.random() to false
        }

        // Unlock the won prize permanently
        if (isNewPiece) {
            val updatedPet = petState.value.copy(
                unlockedOutfitIds = (petState.value.unlockedOutfitIds + wonPrize.id).distinct()
            )
            viewModelScope.launch {
                repository.savePetState(updatedPet)
            }
        }

        val totalOwnedCount = (unlockedOutfitIds + wonPrize.id).count { id -> allPrizes.any { it.id == id } }

        val floorData = com.example.data.models.AdventuringStoryData.getFloor(nextFloorNum)
        val firstMonster = floorData.monsters.first()
        _adventuringCurrentMonster.value = firstMonster
        _adventuringMonsterHp.value = firstMonster.hp

        val newLogs = _adventuringLog.value.toMutableList()
        newLogs.add(0, "🎉 CLEARED FLOOR $currentFloor! Advanced to ${floorData.title}!")
        newLogs.add(0, "🎁 Floor Prize: Won ${wonPrize.name} ${wonPrize.iconEmoji}! (Permanent Buff Unlocked)")
        newLogs.add(0, "⚔️ Encountered ${firstMonster.name} ${firstMonster.emoji}!")
        _adventuringLog.value = newLogs.take(15)

        addChatMessage("🎉 Adventuring: Cleared Floor $currentFloor! Ascended to ${floorData.title}! Won prize: ${wonPrize.name} ${wonPrize.iconEmoji}!")

        // Pop up the Floor Clear 99-Prize notification panel
        _floorClearRewardEvent.value = com.example.data.models.FloorClearReward(
            floorNumber = currentFloor,
            floorTitle = com.example.data.models.AdventuringStoryData.getFloor(currentFloor).title,
            pieceId = wonPrize.id,
            pieceName = wonPrize.name,
            slotName = wonPrize.slotName,
            skill = wonPrize.skill,
            iconEmoji = wonPrize.iconEmoji,
            description = wonPrize.description,
            isNewPiece = isNewPiece,
            totalOwnedCount = totalOwnedCount,
            totalPrizePoolCount = allPrizes.size
        )
    }

    fun selectAdventuringFloor(floorNum: Int) {
        val maxFloor = _adventuringMaxFloor.value
        if (floorNum < 1 || floorNum > maxFloor) return
        _adventuringFloor.value = floorNum
        val floorData = com.example.data.models.AdventuringStoryData.getFloor(floorNum)
        val firstMonster = floorData.monsters.first()
        _adventuringCurrentMonster.value = firstMonster
        _adventuringMonsterHp.value = firstMonster.hp
        val newLogs = _adventuringLog.value.toMutableList()
        newLogs.add(0, "🗺️ Traveled to Floor $floorNum: ${floorData.title}!")
        newLogs.add(0, "⚔️ Encountered ${firstMonster.name} ${firstMonster.emoji}!")
        _adventuringLog.value = newLogs.take(15)
        addChatMessage("🗺️ Adventuring: Traveled to Floor $floorNum (${floorData.title})!")
    }

    fun resetAdventuringDungeon() {
        val floorData = com.example.data.models.AdventuringStoryData.getFloor(1)
        val firstMonster = floorData.monsters.first()
        _adventuringFloor.value = 1
        _adventuringCurrentMonster.value = firstMonster
        _adventuringMonsterHp.value = firstMonster.hp
        _adventuringPetHp.value = _adventuringPetMaxHp.value
        val newLogs = mutableListOf("⚔️ Restarted dungeon at Floor 1: Whispering Grove!")
        _adventuringLog.value = newLogs
    }

    fun fishAtPohPond(targetFishId: String? = null, isAfk: Boolean = false) {
        if (!isAfk) {
            if (!canStartAfkOrHungerAction("Fishing")) return
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Fishing.")
            }
        }
        viewModelScope.launch {
            val fishingXp = skillXpMap.value[OsrsSkill.FISHING] ?: 0L
            val fishingLvl = OsrsXpCalculator.getLevelForXp(fishingXp)

            val currentArea = com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS.find { it.id == _selectedSpiritPoolAreaId.value }
            if (currentArea != null) {
                if (fishingLvl < currentArea.reqLevel) {
                    if (isAfkFishingActive.value) {
                        AfkEngine.stopAll(pohPrefs)
                        addChatMessage("🔒 AFK Fishing stopped: Requires Level ${currentArea.reqLevel} Fishing for ${currentArea.name}!")
                    } else {
                        addChatMessage("🔒 Cannot fish here: Requires Level ${currentArea.reqLevel} Fishing for ${currentArea.name}!")
                    }
                    return@launch
                }
                if (!isTotemUnlocked(currentArea.reqTotemId)) {
                    val reqName = currentArea.reqTotemName ?: "Obelisk"
                    if (isAfkFishingActive.value) {
                        AfkEngine.stopAll(pohPrefs)
                    }
                    addChatMessage("🗿 Obelisk Locked: Requires the $reqName for ${currentArea.name}!")
                    return@launch
                }
            }

            val poolFish = currentArea?.catchableFish ?: com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS.first().catchableFish
            val unlockedFish = poolFish.filter { fishingLvl >= it.reqLevel }
            if (unlockedFish.isEmpty()) {
                if (isAfk) AfkEngine.stopAll(pohPrefs)
                addChatMessage("🔒 No unlocked fish available in ${currentArea?.name ?: "this pond"} for your Fishing level ($fishingLvl)!")
                return@launch
            }

            val fishToCatch: com.example.data.models.SpiritFish? = if (!isAfk && targetFishId != null) {
                val target = poolFish.find { it.id == targetFishId }
                if (target != null && fishingLvl < target.reqLevel) {
                    addChatMessage("🔒 Requires Level ${target.reqLevel} Fishing for ${target.name} (You are Level $fishingLvl)!")
                    return@launch
                }
                target ?: unlockedFish.first()
            } else {
                val totalWeight = unlockedFish.sumOf { it.dropChancePercent.coerceAtLeast(1) }
                val roll = (1..totalWeight.coerceAtLeast(1)).random()
                var cumulative = 0
                var chosen = unlockedFish.first()
                for (f in unlockedFish) {
                    cumulative += f.dropChancePercent.coerceAtLeast(1)
                    if (roll <= cumulative) {
                        chosen = f
                        break
                    }
                }
                chosen
            }

            if (fishToCatch == null) return@launch
            if (fishingLvl < fishToCatch.reqLevel) {
                addChatMessage("🔒 Requires Level ${fishToCatch.reqLevel} Fishing for ${fishToCatch.name} (You are Level $fishingLvl)!")
                if (isAfk) AfkEngine.stopAll(pohPrefs)
                return@launch
            }

            if (fishToCatch == null) return@launch

            val rawId = fishToCatch.id
            val rawName = "${fishToCatch.name} ${fishToCatch.emoji}"
            val xp = fishToCatch.xp.toLong()

            val finbarLvl = npcFavorMap.value["finbar"]?.first ?: getNpcFavorLevel("finbar")
            var fishYield = 1
            val rolledBonus = (1..100).random() <= finbarLvl
            if (rolledBonus) {
                fishYield += 1
                addChatMessage("✨ [Finbar's Favor Perk (+${finbarLvl}%)]: Double fish caught! (+1 extra ${fishToCatch.name}) 🐟🌊")
            }
            val existing = inventoryItems.value.find { it.id == rawId }
            val newQty = (existing?.quantity ?: 0) + fishYield
            saveInventoryItem(rawId, newQty)

            if (fishToCatch.bonusSecondItemId != null && fishToCatch.bonusSecondItemQty > 0) {
                val secId = fishToCatch.bonusSecondItemId!!
                val secExisting = inventoryItems.value.find { it.id == secId }
                val secNewQty = (secExisting?.quantity ?: 0) + fishToCatch.bonusSecondItemQty
                saveInventoryItem(secId, secNewQty)
                val secName = fishToCatch.bonusSecondItemName ?: secId
                addChatMessage("✨ Bonus Catch: +${fishToCatch.bonusSecondItemQty}x $secName ${fishToCatch.bonusSecondItemEmoji ?: ""}!")
            }

            val catchTitle = if (fishYield > 1) "Caught 2x ${fishToCatch.name} (Double Fish!)" else "Caught ${fishToCatch.name}"
            val catchLogDesc = if (fishYield > 1) {
                "Caught 2x $rawName (✨ Finbar +${finbarLvl}% Extra Fish Perk!) in ${currentArea?.name ?: "Shaman Pool"}!"
            } else {
                "Caught 1x $rawName (${fishToCatch.dropChancePercent}% drop chance) in ${currentArea?.name ?: "Shaman Pool"}!"
            }

            addXpAndNotify(
                skill = OsrsSkill.FISHING,
                amount = xp,
                gpReward = 20L,
                logTitle = catchTitle,
                logDesc = catchLogDesc
            )
            val mainChatMsg = if (fishYield > 1) {
                "🎣 Caught 2x $rawName (✨ Finbar +${finbarLvl}% Double Fish) in ${currentArea?.name ?: "Spirit Pool"}!"
            } else {
                "🎣 Caught 1x $rawName (${fishToCatch.dropChancePercent}% drop chance) in ${currentArea?.name ?: "Spirit Pool"}!"
            }
            addChatMessage(mainChatMsg)
            progressSkillContract(OsrsSkill.FISHING, fishYield, fishToCatch.id)
        }
    }

    fun craftHunterTrap(trapId: String, isAfk: Boolean = false) {
        if (!isAfk) {
            if (!canStartAfkOrHungerAction("Hunter Trap Crafting")) return
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Trap Crafting.")
            }
        }
        viewModelScope.launch {
            val logIds = listOf("item_logs", "item_oak_logs", "item_willow_logs", "item_maple_logs", "item_yew_logs", "item_magic_logs", "item_redwood_logs")
            val availableLogId = logIds.firstOrNull { getItemQuantityCombined(it) > 0 }
            val stickQty = getItemQuantityCombined("item_wooden_stick")

            when (trapId) {
                "item_bird_snare" -> {
                    if (availableLogId == null) {
                        if (isAfk) {
                            AfkEngine.stopAll(pohPrefs)
                            addChatMessage("🪤 AFK Trap Crafting: Out of logs for Bird Snares!")
                        } else {
                            addChatMessage("🪤 Trap Crafting: You need 1 Log to craft Bird Snares!")
                        }
                        return@launch
                    }
                    deductItemCombined(availableLogId, 1)
                    val existing = inventoryItems.value.find { it.id == "item_bird_snare" }
                    saveInventoryItem("item_bird_snare", (existing?.quantity ?: 0) + 2)
                    val logName = DefaultItems.ALL.find { it.id == availableLogId }?.name ?: "Logs"
                    addXpAndNotify(OsrsSkill.HUNTER, 25L, 0L, "Crafted Bird Snares", "Carved 1x $logName into 2x Bird Snares (+25 Hunter XP)!")
                }
                "item_box_trap" -> {
                    val logWith2 = logIds.firstOrNull { getItemQuantityCombined(it) >= 2 }
                    if (logWith2 == null) {
                        if (isAfk) {
                            AfkEngine.stopAll(pohPrefs)
                            addChatMessage("📦 AFK Trap Crafting: Out of logs for Box Traps!")
                        } else {
                            addChatMessage("📦 Trap Crafting: You need 2 Logs to craft a Box Trap!")
                        }
                        return@launch
                    }
                    deductItemCombined(logWith2, 2)
                    val existing = inventoryItems.value.find { it.id == "item_box_trap" }
                    saveInventoryItem("item_box_trap", (existing?.quantity ?: 0) + 1)
                    val logName = DefaultItems.ALL.find { it.id == logWith2 }?.name ?: "Logs"
                    addXpAndNotify(OsrsSkill.HUNTER, 40L, 0L, "Crafted Box Trap", "Built 1x Box Trap from 2x $logName (+40 Hunter XP)!")
                }
                "item_net_trap" -> {
                    if (availableLogId == null || stickQty < 1) {
                        if (isAfk) {
                            AfkEngine.stopAll(pohPrefs)
                            addChatMessage("🕸️ AFK Trap Crafting: Out of logs or sticks for Net Trap Gear!")
                        } else {
                            addChatMessage("🕸️ Trap Crafting: You need 1 Log and 1 Wooden Stick for Net Trap Gear!")
                        }
                        return@launch
                    }
                    deductItemCombined(availableLogId, 1)
                    deductItemCombined("item_wooden_stick", 1)
                    val existing = inventoryItems.value.find { it.id == "item_net_trap" }
                    saveInventoryItem("item_net_trap", (existing?.quantity ?: 0) + 1)
                    addXpAndNotify(OsrsSkill.HUNTER, 35L, 0L, "Crafted Net Trap Gear", "Crafted Net Trap Gear (+35 Hunter XP)!")
                }
                "item_noose_wand" -> {
                    if (availableLogId == null) {
                        if (isAfk) {
                            AfkEngine.stopAll(pohPrefs)
                            addChatMessage("🪓 AFK Trap Crafting: Out of logs for Noose Wand!")
                        } else {
                            addChatMessage("🪓 Trap Crafting: You need 1 Log to craft a Noose Wand!")
                        }
                        return@launch
                    }
                    deductItemCombined(availableLogId, 1)
                    val existing = inventoryItems.value.find { it.id == "item_noose_wand" }
                    saveInventoryItem("item_noose_wand", (existing?.quantity ?: 0) + 1)
                    val logName = DefaultItems.ALL.find { it.id == availableLogId }?.name ?: "Logs"
                    addXpAndNotify(OsrsSkill.HUNTER, 30L, 0L, "Crafted Noose Wand", "Carved 1x Noose Wand from $logName (+30 Hunter XP)!")
                }
                "item_impling_net" -> {
                    if (stickQty < 2) {
                        if (isAfk) {
                            AfkEngine.stopAll(pohPrefs)
                            addChatMessage("🦋 AFK Trap Crafting: Out of wooden sticks for Impling Net!")
                        } else {
                            addChatMessage("🦋 Trap Crafting: You need 2 Wooden Sticks to craft an Impling Net!")
                        }
                        return@launch
                    }
                    deductItemCombined("item_wooden_stick", 2)
                    val existing = inventoryItems.value.find { it.id == "item_impling_net" }
                    saveInventoryItem("item_impling_net", (existing?.quantity ?: 0) + 1)
                    addXpAndNotify(OsrsSkill.HUNTER, 45L, 0L, "Crafted Impling Net", "Crafted 1x Impling Net (+45 Hunter XP)!")
                }
            }
        }
    }

    fun mineAtPohQuarry(targetOreId: String? = null, isAfk: Boolean = false) {
        if (!isAfk) {
            if (!canStartAfkOrHungerAction("Mining")) return
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Mining.")
            }
        }
        viewModelScope.launch {
            val smithXp = skillXpMap.value[OsrsSkill.SMITHING] ?: 0L
            val smithLvl = OsrsXpCalculator.getLevelForXp(smithXp)

            val currentArea = com.example.data.models.AdventuringStoryData.GEMOLOGY_AREAS.find { it.id == _selectedGemologyAreaId.value }
                ?: com.example.data.models.AdventuringStoryData.GEMOLOGY_AREAS.first()
            val isRuneVault = currentArea.id.contains("rune", ignoreCase = true) || currentArea.id == "rune_vault"

            if (smithLvl < currentArea.reqLevel) {
                if (isAfkMiningActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔒 AFK Mining stopped: Requires Level ${currentArea.reqLevel} Forging for ${currentArea.name}!")
                } else {
                    addChatMessage("🔒 Cannot mine here: Requires Level ${currentArea.reqLevel} Forging for ${currentArea.name}!")
                }
                return@launch
            }

            if (!isTotemUnlocked(currentArea.reqTotemId)) {
                val reqName = currentArea.reqTotemName ?: "Obelisk"
                if (isAfkMiningActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                }
                addChatMessage("🗿 Obelisk Locked: Requires the $reqName for ${currentArea.name}!")
                return@launch
            }

            val choppableOres = currentArea.minerals
            val unlockedMinerals = choppableOres.filter { smithLvl >= it.reqLevel }
            if (unlockedMinerals.isEmpty()) {
                if (isAfk) AfkEngine.stopAll(pohPrefs)
                addChatMessage("🔒 No unlocked minerals available in ${currentArea.name} for your Forging level ($smithLvl)!")
                return@launch
            }

            val selectedMineral: com.example.data.models.GemologyMineral? = if (!isAfk && targetOreId != null) {
                val target = choppableOres.find { it.id == targetOreId }
                if (target != null && smithLvl < target.reqLevel) {
                    addChatMessage("🔒 Requires Level ${target.reqLevel} Forging for ${target.name} (You are Level $smithLvl)!")
                    return@launch
                }
                target ?: unlockedMinerals.first()
            } else {
                val totalWeight = unlockedMinerals.sumOf { it.dropChancePercent.coerceAtLeast(1) }
                val roll = (1..totalWeight.coerceAtLeast(1)).random()
                var cumulative = 0
                var chosen = unlockedMinerals.first()
                for (m in unlockedMinerals) {
                    cumulative += m.dropChancePercent.coerceAtLeast(1)
                    if (roll <= cumulative) {
                        chosen = m
                        break
                    }
                }
                chosen
            }

            if (selectedMineral == null) return@launch
            if (smithLvl < selectedMineral.reqLevel) {
                addChatMessage("🔒 Requires Level ${selectedMineral.reqLevel} Forging for ${selectedMineral.name} (You are Level $smithLvl)!")
                if (isAfk) AfkEngine.stopAll(pohPrefs)
                return@launch
            }

            if (selectedMineral == null) return@launch

            val oreId = selectedMineral.id
            val oreName = "${selectedMineral.name} ${selectedMineral.emoji}"
            val xp = selectedMineral.xp.toLong()

            val arlgLvl = npcFavorMap.value["arlg"]?.first ?: npcFavorMap.value["arig"]?.first ?: getNpcFavorLevel("arlg")
            var minedOreQty = 1
            if ((1..100).random() <= arlgLvl) {
                minedOreQty += 1
                addChatMessage("✨ [Arlg's Favor Perk (+${arlgLvl}%)]: Double ore obtained! (+1 extra $oreName) 💎⛏️")
            }
            val existing = inventoryItems.value.find { it.id == oreId }
            val newQty = (existing?.quantity ?: 0) + minedOreQty
            saveInventoryItem(oreId, newQty)

            if (selectedMineral.bonusSecondItemId != null && selectedMineral.bonusSecondItemQty > 0) {
                val secId = selectedMineral.bonusSecondItemId!!
                val secExisting = inventoryItems.value.find { it.id == secId }
                val secNewQty = (secExisting?.quantity ?: 0) + selectedMineral.bonusSecondItemQty
                saveInventoryItem(secId, secNewQty)
                val secName = selectedMineral.bonusSecondItemName ?: secId
                addChatMessage("✨ Bonus Mineral: +${selectedMineral.bonusSecondItemQty}x $secName ${selectedMineral.bonusSecondItemEmoji ?: ""}!")
            }

            val displayDropRate = if (isRuneVault) {
                if (selectedMineral.id == "item_rune_essence") 99 else 1
            } else {
                selectedMineral.dropChancePercent
            }

            addXpAndNotify(
                skill = OsrsSkill.SMITHING,
                amount = xp,
                gpReward = 20L,
                logTitle = "Mined ${selectedMineral.name}",
                logDesc = "Mined 1x $oreName ($displayDropRate% drop chance) in ${currentArea.name}!"
            )
            addChatMessage("⛏️ Mined 1x $oreName ($displayDropRate% drop chance) in ${currentArea.name}!")
            progressSkillContract(OsrsSkill.SMITHING, 1, selectedMineral.id)

            // 15% Chance for Gemstone drop while Mining in regular quarries (only unlocked gems!)
            if (!isRuneVault && Math.random() < 0.15) {
                val eligibleGems = listOf(
                    Triple("item_uncut_diamond", "Diamond 💎", 40 to 200L),
                    Triple("item_uncut_ruby", "Ruby 🟥", 34 to 100L),
                    Triple("item_uncut_emerald", "Emerald 🟩", 27 to 50L),
                    Triple("item_uncut_sapphire", "Sapphire 🟦", 20 to 25L)
                ).filter { smithLvl >= it.third.first }

                if (eligibleGems.isNotEmpty()) {
                    val gemRoll = Math.random()
                    val selectedGem = when {
                        gemRoll < 0.05 && eligibleGems.any { it.first == "item_uncut_diamond" } -> eligibleGems.first { it.first == "item_uncut_diamond" }
                        gemRoll < 0.20 && eligibleGems.any { it.first == "item_uncut_ruby" } -> eligibleGems.first { it.first == "item_uncut_ruby" }
                        gemRoll < 0.50 && eligibleGems.any { it.first == "item_uncut_emerald" } -> eligibleGems.first { it.first == "item_uncut_emerald" }
                        else -> eligibleGems.last()
                    }
                    val gemId = selectedGem.first
                    val gemName = selectedGem.second
                    val gemXp = selectedGem.third.second

                    val gemExisting = inventoryItems.value.find { it.id == gemId }
                    saveInventoryItem(gemId, (gemExisting?.quantity ?: 0) + 1)

                    addXpAndNotify(
                        skill = OsrsSkill.SMITHING,
                        amount = gemXp,
                        gpReward = 50L,
                        logTitle = "Mined $gemName",
                        logDesc = "Uncovered a rare $gemName while mining at ${currentArea.name}!"
                    )
                    if (!isAfk) {
                        addChatMessage("💎 GEMSTONE FOUND! Uncovered 1x $gemName! (+${gemXp} Forging XP)")
                    }
                }
            }
        }
    }

    fun smeltOresAtFurnace(targetBarId: String? = null, isAfk: Boolean = false) {
        viewModelScope.launch {
            val smithingXp = skillXpMap.value[OsrsSkill.SMITHING] ?: 0L
            val smithingLvl = OsrsXpCalculator.getLevelForXp(smithingXp)

            val runeOreQty = getItemQuantityCombined("item_runite_ore")
            val adamantOreQty = getItemQuantityCombined("item_adamant_ore")
            val mithrilOreQty = getItemQuantityCombined("item_mithril_ore")
            val ironOreQty = getItemQuantityCombined("item_iron_ore")
            val coalOreQty = getItemQuantityCombined("item_coal_ore")
            val copperOreQty = getItemQuantityCombined("item_copper_ore")
            val tinOreQty = getItemQuantityCombined("item_tin_ore")

            if ((targetBarId == null || targetBarId == "item_rune_bar") && smithingLvl >= 85 && runeOreQty >= 1 && coalOreQty >= 4) {
                deductItemCombined("item_runite_ore", 1)
                deductItemCombined("item_coal_ore", 4)
                val barItem = inventoryItems.value.find { it.id == "item_rune_bar" }
                saveInventoryItem("item_rune_bar", (barItem?.quantity ?: 0) + 1)
                addXpAndNotify(OsrsSkill.SMITHING, 150L, 50L, "Smelted Aetherite Bar", "Smelted 1x Aetherite Bar at Furnace!")
            } else if ((targetBarId == null || targetBarId == "item_adamant_bar") && smithingLvl >= 70 && adamantOreQty >= 1 && coalOreQty >= 3) {
                deductItemCombined("item_adamant_ore", 1)
                deductItemCombined("item_coal_ore", 3)
                val barItem = inventoryItems.value.find { it.id == "item_adamant_bar" }
                saveInventoryItem("item_adamant_bar", (barItem?.quantity ?: 0) + 1)
                addXpAndNotify(OsrsSkill.SMITHING, 100L, 30L, "Smelted Amethyst Bar", "Smelted 1x Amethyst Bar at Furnace!")
            } else if ((targetBarId == null || targetBarId == "item_mithril_bar") && smithingLvl >= 50 && mithrilOreQty >= 1 && coalOreQty >= 2) {
                deductItemCombined("item_mithril_ore", 1)
                deductItemCombined("item_coal_ore", 2)
                val barItem = inventoryItems.value.find { it.id == "item_mithril_bar" }
                saveInventoryItem("item_mithril_bar", (barItem?.quantity ?: 0) + 1)
                addXpAndNotify(OsrsSkill.SMITHING, 60L, 20L, "Smelted Opalite Bar", "Smelted 1x Opalite Bar at Furnace!")
            } else if ((targetBarId == null || targetBarId == "item_steel_bar") && smithingLvl >= 30 && ironOreQty >= 1 && coalOreQty >= 2) {
                deductItemCombined("item_iron_ore", 1)
                deductItemCombined("item_coal_ore", 2)
                val barItem = inventoryItems.value.find { it.id == "item_steel_bar" }
                saveInventoryItem("item_steel_bar", (barItem?.quantity ?: 0) + 1)
                addXpAndNotify(OsrsSkill.SMITHING, 35L, 15L, "Smelted Steel Bar", "Smelted 1x Steel Bar at Furnace!")
                progressSkillContract(OsrsSkill.SMITHING, 1, "item_steel_bar")
            } else if ((targetBarId == null || targetBarId == "item_iron_bar") && smithingLvl >= 15 && ironOreQty >= 1) {
                deductItemCombined("item_iron_ore", 1)
                val barItem = inventoryItems.value.find { it.id == "item_iron_bar" }
                saveInventoryItem("item_iron_bar", (barItem?.quantity ?: 0) + 1)
                addXpAndNotify(OsrsSkill.SMITHING, 25L, 10L, "Smelted Iron Bar", "Smelted 1x Iron Bar at Furnace!")
                progressSkillContract(OsrsSkill.SMITHING, 1, "item_iron_bar")
            } else if ((targetBarId == null || targetBarId == "item_bronze_bar") && copperOreQty >= 1 && tinOreQty >= 1) {
                deductItemCombined("item_copper_ore", 1)
                deductItemCombined("item_tin_ore", 1)
                val barItem = inventoryItems.value.find { it.id == "item_bronze_bar" }
                saveInventoryItem("item_bronze_bar", (barItem?.quantity ?: 0) + 1)
                addXpAndNotify(OsrsSkill.SMITHING, 15L, 5L, "Smelted Bronze Bar", "Smelted 1x Bronze Bar at Furnace!")
                progressSkillContract(OsrsSkill.SMITHING, 1, "item_bronze_bar")
            } else {
                if (isAfkSmeltingActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔥 Smelting Furnace: Out of ores! Smelting AFK turned OFF.")
                    notifyAfkStopped("Furnace Smelting", "Out of ores in inventory + bank!")
                } else if (!isAfk) {
                    addChatMessage("🔥 Smelting Furnace: You need ores in inventory or bank (e.g. Copper + Tin, Iron + Coal, Opalite + Coal) to smelt bars!")
                }
            }
        }
    }

    fun convertLogsToPlanksAtSawmill(isAfk: Boolean = false, targetPlankId: String? = null, quantityToSaw: Int = 1) {
        if (!isAfk) {
            if (!canStartAfkOrHungerAction("Sawmill Plank Making")) return
            val prevAfk = getActiveAfkActivityName()
            if (prevAfk != null) {
                stopAllAfkStations()
                addChatMessage("🛑 Stopped AFK ($prevAfk) to start Manual Plank Making.")
            }
        }
        viewModelScope.launch {
            val selectedPlank = targetPlankId ?: _selectedSawmillPlankId.value
            val recipe = com.example.data.models.SawmillRecipes.findRecipeForPlank(selectedPlank)

            val candidateLogIds = if (recipe != null) {
                recipe.acceptedLogIds
            } else {
                listOf("item_logs", "item_oak_logs", "item_willow_logs", "item_teak_logs", "item_maple_logs", "item_mahogany_logs", "item_yew_logs", "item_magic_logs", "item_redwood_logs")
            }

            val targetLogId = candidateLogIds.firstOrNull { getItemQuantityCombined(it) > 0 }

            if (targetLogId == null) {
                if (isAfkSawmillActive.value) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🪚 Sawmill: Out of timber logs! Sawmill has automatically turned OFF.")
                    notifyAfkStopped("Sawmill Plank Making", "Out of logs/timber in inventory + bank!")
                } else if (!isAfk) {
                    val neededLog = recipe?.logDisplayName ?: "timber logs"
                    addChatMessage("🪚 Sawmill: Cannot mill! No $neededLog in backpack or bank. Chop trees in The Grove first.")
                }
                return@launch
            }

            val matchedRecipe = com.example.data.models.SawmillRecipes.findRecipeForLog(targetLogId)
                ?: recipe
                ?: com.example.data.models.SawmillRecipes.ALL_RECIPES.first()

            val totalAvail = getItemQuantityCombined(targetLogId)
            val actualSawQty = quantityToSaw.coerceAtMost(totalAvail).coerceAtLeast(1)
            val resultPlankId = matchedRecipe.plankId
            val plankItem = DefaultItems.ALL_SHOP_ITEMS.find { it.id == resultPlankId }
            val logItemDef = DefaultItems.ALL.find { it.id == targetLogId }

            deductItemCombined(targetLogId, actualSawQty)

            val updatedInv = inventoryItems.value
            val existingPlank = updatedInv.find { it.id == resultPlankId }
            val newPlankQty = (existingPlank?.quantity ?: 0) + actualSawQty
            saveInventoryItem(resultPlankId, newPlankQty)

            // Add to POH Material Inventory
            val geMat = matchedRecipe.geMaterial
            val currentPoh = _pohHouseState.value
            val currentMats = currentPoh.materialInventory.toMutableMap()
            currentMats[geMat] = (currentMats[geMat] ?: 0) + actualSawQty
            updatePohHouseState(currentPoh.copy(materialInventory = currentMats))

            val totalXp = matchedRecipe.constructionXp * actualSawQty
            val gpReward = 20L * actualSawQty
            val logName = logItemDef?.name ?: "Logs"

            addXpAndNotify(
                skill = OsrsSkill.CONSTRUCTION,
                amount = totalXp,
                gpReward = gpReward,
                logTitle = "Milled $actualSawQty $logName ➔ ${plankItem?.name ?: matchedRecipe.plankName}",
                logDesc = "Milled $actualSawQty x $logName into $actualSawQty x ${plankItem?.name ?: matchedRecipe.plankName} at Sawmill for +$totalXp Construction XP!"
            )
            progressSkillContract(OsrsSkill.CONSTRUCTION, actualSawQty, resultPlankId)

            // Check if any logs remain to turn off Sawmill
            val remainingLogs = listOf("item_logs", "item_oak_logs", "item_willow_logs", "item_teak_logs", "item_maple_logs", "item_mahogany_logs", "item_yew_logs", "item_magic_logs", "item_redwood_logs").any { logId ->
                getItemQuantityCombined(logId) > 0
            }
            if (!remainingLogs && isAfkSawmillActive.value) {
                AfkEngine.stopAll(pohPrefs)
                addChatMessage("🪚 Sawmill: Out of timber logs! Sawmill has automatically turned OFF.")
            }
        }
    }

    fun buyCustomGeOffer(itemName: String, priceGp: Long, quantity: Int = 1) {
        val totalCost = priceGp * quantity
        val pet = petState.value
        if (pet.coinsGp < totalCost) {
            addChatMessage("System: Not enough GP! Offer costs $totalCost GP (You have ${pet.coinsGp} GP).")
            return
        }

        viewModelScope.launch {
            repository.savePetState(pet.copy(coinsGp = pet.coinsGp - totalCost))

            val matchingPreset = DefaultItems.ALL_SHOP_ITEMS.find { it.name.equals(itemName, ignoreCase = true) }
            val itemId = matchingPreset?.id ?: "custom_ge_${itemName.lowercase().replace(" ", "_")}"
            val existing = inventoryItems.value.find { it.id == itemId }
            val newQty = (existing?.quantity ?: 0) + quantity

            if (existing != null) {
                saveInventoryItem(itemId, newQty)
            } else {
                val newItem = matchingPreset?.copy(quantity = newQty) ?: InventoryItem(
                    id = itemId,
                    name = itemName,
                    category = ItemCategory.SKILL_TOOL,
                    iconEmoji = "📦",
                    description = "Custom OSRS item purchased from the Grand Exchange!",
                    costGp = priceGp,
                    quantity = quantity,
                    addHappiness = 20,
                    bonusXpSkill = OsrsSkill.CONSTRUCTION,
                    bonusXpAmount = 150L
                )
                saveInventoryItem(newItem.id, newItem.quantity)
            }

            addChatMessage("📈 GE SUCCESS! Purchased $quantity x $itemName for $totalCost GP!")
        }
    }

    // --- PLAYER OWNED FARM (POF) ACTIONS ---

    fun toggleAfkFarming(category: com.example.data.models.SeedCategory? = null) {
        val newState = !isAfkFarmingActive.value
        if (newState && !canStartAfkOrHungerAction("Farming")) return
        stopAllAfkStations()
        if (category != null) {
            _afkSeedCategory.value = category
        }
        if (newState) {
            AfkEngine.startActivity(AfkActivityType.FARMING, pohPrefs)
            val catName = _afkSeedCategory.value.displayName
            addChatMessage("🚜 Farm Helper: AFK Farm Helper activated ($catName)! Fee: 5 GP per cycle. Uses 1 Compost Bucket per plot.")
            if (_pofState.value.compostBucketsCount <= 0) {
                addChatMessage("⚠️ Warning: Compost Bin is empty! Add crops to Compost Bin to support AFK farming.")
            }
            processAfkFarmingTick()
        } else {
            AfkEngine.stopAll(pohPrefs)
            addChatMessage("🚜 Farm Helper: AFK Farm Helper turned OFF.")
        }
        saveAfkStateToPrefs()
    }

    fun setAfkSeedCategory(category: com.example.data.models.SeedCategory) {
        _afkSeedCategory.value = category
        addChatMessage("🚜 Farm Helper: Target seed category set to '${category.displayName}'.")
    }

    fun addCropToCompostBin(itemId: String, quantity: Int) {
        viewModelScope.launch {
            val totalQty = getItemQuantityCombined(itemId)
            if (totalQty <= 0) return@launch
            val qtyToDeposit = quantity.coerceAtMost(totalQty)
            if (qtyToDeposit <= 0) return@launch

            val invItem = DefaultItems.ALL.find { it.id == itemId }
                ?: inventoryItems.value.find { it.id == itemId }
                ?: bankItems.value.find { it.id == itemId }
                ?: return@launch

            if (!com.example.data.models.isCompostableItem(invItem)) {
                addChatMessage("🟤 Compost Bin: You cannot compost seeds, raw meat, or non-plant items!")
                return@launch
            }

            deductItemCombined(invItem.id, qtyToDeposit)
            val updatedCompostCount = _pofState.value.compostBucketsCount + qtyToDeposit
            updatePofState(_pofState.value.copy(compostBucketsCount = updatedCompostCount))

            addChatMessage("🟤 Compost Bin: Deposited $qtyToDeposit x ${invItem.name}! (+ $qtyToDeposit Compost Buckets, Total: $updatedCompostCount)")
        }
    }

    fun depositAllCompostableItemsToCompostBin() {
        viewModelScope.launch {
            val allCompostable = (inventoryItems.value + bankItems.value)
                .filter { it.quantity > 0 && com.example.data.models.isCompostableItem(it) }
                .map { it.id }
                .distinct()

            if (allCompostable.isEmpty()) {
                addChatMessage("🟤 Compost Bin: No compostable produce found in Inventory or Bank.")
                return@launch
            }

            var totalDeposited = 0
            for (itemId in allCompostable) {
                val qty = getItemQuantityCombined(itemId)
                if (qty > 0) {
                    deductItemCombined(itemId, qty)
                    totalDeposited += qty
                }
            }

            if (totalDeposited > 0) {
                val updatedCompostCount = _pofState.value.compostBucketsCount + totalDeposited
                updatePofState(_pofState.value.copy(compostBucketsCount = updatedCompostCount))
                addChatMessage("🟤 Compost Bin: Deposited $totalDeposited items from Inventory + Bank! (+ $totalDeposited Compost Buckets, Total: $updatedCompostCount)")
            }
        }
    }

    fun plantSeedInPlot(plotIndex: Int, cropType: com.example.data.models.FarmCropType) {
        viewModelScope.launch {
            val farmingXp = skillXpMap.value[OsrsSkill.FARMING] ?: 0L
            val farmingLvl = OsrsXpCalculator.getLevelForXp(farmingXp)

            if (!com.example.data.models.isCropAllowedInPlot(plotIndex, cropType)) {
                if (plotIndex in 0..3) {
                    addChatMessage("🔒 Farm Plot #${plotIndex + 1}: Only Herbs and Vegetables can be planted in Plots #1 - #4!")
                } else if (plotIndex in 8..11) {
                    addChatMessage("🔒 Farm Plot #${plotIndex + 1}: Only Trees and Fruit Trees can be planted in Plots #9 - #12!")
                } else {
                    addChatMessage("🔒 Farm Plot #${plotIndex + 1}: Seed not allowed in this patch!")
                }
                return@launch
            }

            if (farmingLvl < cropType.reqFarmingLevel) {
                addChatMessage("🌱 Farming: You need level ${cropType.reqFarmingLevel} Farming to plant ${cropType.displayName}!")
                return@launch
            }

            val seedQty = getItemQuantityCombined(cropType.seedId)
            if (seedQty <= 0) {
                addChatMessage("🌱 Farming: You do not have any ${cropType.seedName} in your inventory or bank!")
                return@launch
            }

            // Consume seed
            deductItemCombined(cropType.seedId, 1)

            val currentPlots = _pofState.value.plots.toMutableList()
            if (plotIndex in currentPlots.indices) {
                currentPlots[plotIndex] = com.example.data.models.FarmPlotState(
                    plotIndex = plotIndex,
                    cropType = cropType,
                    plantedTimestampMs = System.currentTimeMillis(),
                    isWatered = false,
                    isComposted = false
                )
                updatePofState(_pofState.value.copy(plots = currentPlots))
                val durationStr = com.example.data.models.formatGrowthDuration(cropType.growthTimeSeconds)
                addChatMessage("🌱 Farm Plot #${plotIndex + 1}: Planted ${cropType.seedName}! Crop will mature in $durationStr.")
                progressSkillContract(OsrsSkill.FARMING, 1, cropType.produceItemId, "Planted ${cropType.displayName}", "Planted in Farm Plot #${plotIndex + 1}")
                progressSkillContract(OsrsSkill.FARMING, 1, cropType.seedId)
            }
        }
    }

    fun waterPlot(plotIndex: Int) {
        val currentPlots = _pofState.value.plots.toMutableList()
        if (plotIndex in currentPlots.indices && currentPlots[plotIndex].cropType != null) {
            currentPlots[plotIndex] = currentPlots[plotIndex].copy(isWatered = true)
            updatePofState(_pofState.value.copy(plots = currentPlots))
            addChatMessage("💦 Farm Plot #${plotIndex + 1}: Crop watered! Growth speed boosted.")
        }
    }

    fun compostPlot(plotIndex: Int) {
        if (_pofState.value.compostBucketsCount <= 0) {
            addChatMessage("🟤 Compost Bin: Out of compost buckets! Deposit crops into the Compost Bin to produce more compost.")
            return
        }

        val currentPlots = _pofState.value.plots.toMutableList()
        if (plotIndex in currentPlots.indices && currentPlots[plotIndex].cropType != null) {
            if (currentPlots[plotIndex].isComposted) {
                addChatMessage("🟤 Farm Plot #${plotIndex + 1}: Supercompost is already applied!")
                return
            }

            val newCompostCount = _pofState.value.compostBucketsCount - 1
            currentPlots[plotIndex] = currentPlots[plotIndex].copy(isComposted = true)
            updatePofState(_pofState.value.copy(
                plots = currentPlots,
                compostBucketsCount = newCompostCount
            ))
            addChatMessage("🟤 Farm Plot #${plotIndex + 1}: Supercompost applied (-1 Compost Bucket, $newCompostCount remaining)! Crop yield and XP boosted.")
        }
    }

    fun harvestCropPlot(plotIndex: Int) {
        viewModelScope.launch {
            val currentPlots = _pofState.value.plots.toMutableList()
            if (plotIndex !in currentPlots.indices) return@launch
            val plot = currentPlots[plotIndex]
            val crop = plot.cropType ?: return@launch

            if (!plot.isReadyToHarvest(currentTimeMs = System.currentTimeMillis())) {
                addChatMessage("🌾 Farm Plot #${plotIndex + 1}: Crop is not mature yet! Please wait.")
                return@launch
            }

            val farmingXp = skillXpMap.value[OsrsSkill.FARMING] ?: 0L
            val farmingLvl = OsrsXpCalculator.getLevelForXp(farmingXp)

            val baseProduce = if (crop.category == com.example.data.models.SeedCategory.HERB) {
                // Growth time 10 minutes. Harvest at least 6 Everytime. Capped at 14 at level 99.
                val baseHerbYield = (6 + ((farmingLvl - 1) * 8 / 98)).coerceIn(6, 14)
                val yieldBonus = if (plot.isComposted) 2 else 0
                baseHerbYield + yieldBonus
            } else if (crop.category == com.example.data.models.SeedCategory.VEGETABLE) {
                // Vegetable crops give crops based on crop and farming level
                val baseYield = (3 + (farmingLvl / 10)).coerceIn(3, 15)
                val yieldBonus = if (plot.isComposted) 2 else 0
                baseYield + yieldBonus
            } else {
                val yieldBonus = if (plot.isComposted) 2 else 0
                crop.produceQty + yieldBonus
            }

            val bryanLvl = npcFavorMap.value["bryan"]?.first ?: getNpcFavorLevel("bryan")
            val bramLvl = npcFavorMap.value["bram"]?.first ?: getNpcFavorLevel("bram")
            val harvestFavorLvl = maxOf(bryanLvl, bramLvl)
            val hasSiegeQuest = petState.value.completedQuestIds.contains("quest_monkey_madness_2") || petState.value.completedQuestIds.contains("quest_monkey_madness_2_part2")
            val siegeBonus = if (hasSiegeQuest) 25 else 0
            val doubleCropChance = (harvestFavorLvl + siegeBonus).coerceIn(0, 100)
            val isDoubleProc = doubleCropChance > 0 && (1..100).random() <= doubleCropChance
            val tempProduce = if (isDoubleProc) baseProduce * 2 else baseProduce

            // Sprout Whisperer Incantation (+15%, +25%, +40% crop yield)
            val sproutYieldMultiplier = when {
                isIncantationActiveAndUsable("incant_sprout_whisper_t3") -> 1.40
                isIncantationActiveAndUsable("incant_sprout_whisper_t2") -> 1.25
                isIncantationActiveAndUsable("incant_sprout_whisper") -> 1.15
                else -> 1.0
            }
            val totalProduce = (tempProduce * sproutYieldMultiplier).toInt().coerceAtLeast(1)

            // Sprout Whisperer Seed Return (+1%, +2%, +3% chance to recover seed)
            val seedReturnChance = when {
                isIncantationActiveAndUsable("incant_sprout_whisper_t3") -> 3
                isIncantationActiveAndUsable("incant_sprout_whisper_t2") -> 2
                isIncantationActiveAndUsable("incant_sprout_whisper") -> 1
                else -> 0
            }
            if (seedReturnChance > 0 && (1..100).random() <= seedReturnChance) {
                saveBankItem(crop.seedId, (bankItems.value.find { it.id == crop.seedId }?.quantity ?: 0) + 1)
                addChatMessage("🌱 Seed Return! Sprout Whisperer recovered 1x ${crop.seedName} to Bank! (${seedReturnChance}% chance)")
            }

            if (isDoubleProc) {
                val patronName = if (bramLvl >= bryanLvl && bramLvl > 0) "Bram (+${bramLvl}% Harvesting Favor)" else "Farmer Bryan (+${bryanLvl}%)"
                val sourceDesc = if (hasSiegeQuest && harvestFavorLvl > 0) {
                    "$patronName & Siege Quest (+25%)"
                } else if (hasSiegeQuest) {
                    "Siege of the Primate Fleet Quest (+25%)"
                } else {
                    patronName
                }
                addChatMessage("✨ [Double Crop Harvest (+${doubleCropChance}%)]: Bumper crop! ($sourceDesc) Gathered 2x yield: +${totalProduce}x ${crop.produceEmoji} ${crop.produceName}! 🌱🚜")
            }

            val xpBonus = if (plot.isComposted) (crop.farmingXp * 0.25).toLong() else 0L
            val totalXp = crop.farmingXp + xpBonus

            // Add produce item to inventory
            val producePreset = DefaultItems.ALL.find { it.id == crop.produceItemId }
            val existing = inventoryItems.value.find { it.id == crop.produceItemId }
            val newQty = (existing?.quantity ?: 0) + totalProduce
            if (existing != null) {
                saveInventoryItem(crop.produceItemId, newQty)
            } else if (producePreset != null) {
                saveInventoryItem(producePreset.id, totalProduce)
            } else {
                saveInventoryItem(crop.produceItemId, totalProduce)
            }

            // Clear harvested plot so new crops and trees can be planted
            currentPlots[plotIndex] = com.example.data.models.FarmPlotState(plotIndex = plotIndex)
            updatePofState(_pofState.value.copy(
                plots = currentPlots,
                totalCropsHarvested = _pofState.value.totalCropsHarvested + 1
            ))

            progressSkillContract(OsrsSkill.FARMING, totalProduce, crop.produceItemId, "Harvested ${crop.displayName}", "Harvested $totalProduce x ${crop.produceName}")
            progressSkillContract(OsrsSkill.FARMING, totalProduce, crop.seedId)

            // Check Farming Contract completion
            val activeContract = _pofState.value.activeContract
            if (activeContract != null && !activeContract.isCompleted && activeContract.targetCrop == crop) {
                val updatedContract = activeContract.copy(isCompleted = true)
                updatePofState(_pofState.value.copy(activeContract = updatedContract))
                addChatMessage("📜 Farming Contract COMPLETE! You harvested ${crop.displayName}! Return to Guildmaster Jane in the Farming Guild to claim your Seed Pouch!")
            }

            val harvestActionName = if (crop.category == com.example.data.models.SeedCategory.TREE) "Gathered Bark from" else if (crop.category == com.example.data.models.SeedCategory.FRUIT_TREE) "Gathered Fruit from" else "Harvested"
            val procLabel = if (isDoubleProc) " (✨ Double Yield Perk!)" else ""
            addXpAndNotify(
                skill = OsrsSkill.FARMING,
                amount = totalXp,
                gpReward = 25L,
                logTitle = if (isDoubleProc) "$harvestActionName 2x ${crop.displayName} (Double Crop!)" else "$harvestActionName ${crop.displayName}",
                logDesc = "Gathered $totalProduce x ${crop.produceEmoji} ${crop.produceName}$procLabel (+ $totalXp Farming XP)!"
            )
        }
    }

    fun clearPlot(plotIndex: Int) {
        val currentPlots = _pofState.value.plots.toMutableList()
        if (plotIndex in currentPlots.indices && currentPlots[plotIndex].cropType != null) {
            val cropName = currentPlots[plotIndex].cropType?.displayName ?: "crop"
            currentPlots[plotIndex] = com.example.data.models.FarmPlotState(plotIndex = plotIndex)
            updatePofState(_pofState.value.copy(plots = currentPlots))
            addChatMessage("🪓 Farm Plot #${plotIndex + 1}: Cleared $cropName from plot.")
        }
    }

    // ==========================================
    // AUDIO STATE & SETTINGS
    // ==========================================
    val isAmbientAudioEnabled: StateFlow<Boolean> = audioPlayer.isAmbientEnabled
    val isSfxAudioEnabled: StateFlow<Boolean> = audioPlayer.isSfxEnabled
    val ambientAudioVolume: StateFlow<Float> = audioPlayer.ambientVolume
    val sfxAudioVolume: StateFlow<Float> = audioPlayer.sfxVolume

    fun toggleAmbientForestAudio() {
        audioPlayer.toggleAmbient()
        _isAmbientAudioPlaying.value = audioPlayer.isAmbientEnabled.value
    }

    fun setAmbientAudioVolume(vol: Float) {
        audioPlayer.setAmbientVolume(vol)
    }

    fun toggleSfxAudio() {
        audioPlayer.toggleSfx()
    }

    fun setSfxAudioVolume(vol: Float) {
        audioPlayer.setSfxVolume(vol)
    }

    fun playTestSfx(type: com.example.audio.ForestAmbientAudioPlayer.SfxType = com.example.audio.ForestAmbientAudioPlayer.SfxType.CLICK) {
        audioPlayer.playSfx(type)
    }

    fun playSfxSound(type: com.example.audio.ForestAmbientAudioPlayer.SfxType) {
        audioPlayer.playSfx(type)
    }

    // ==========================================
    // SWIPE GESTURE SETTINGS
    // ==========================================
    private val _swipeTabSensitivity = MutableStateFlow<com.example.data.models.SwipeSensitivity>(
        try {
            val saved = appSettingsPrefs.getString("swipe_tab_sensitivity", com.example.data.models.SwipeSensitivity.MEDIUM.name) ?: com.example.data.models.SwipeSensitivity.MEDIUM.name
            com.example.data.models.SwipeSensitivity.valueOf(saved)
        } catch (e: Exception) {
            com.example.data.models.SwipeSensitivity.MEDIUM
        }
    )
    val swipeTabSensitivity: StateFlow<com.example.data.models.SwipeSensitivity> = _swipeTabSensitivity.asStateFlow()

    fun setSwipeTabSensitivity(sensitivity: com.example.data.models.SwipeSensitivity) {
        _swipeTabSensitivity.value = sensitivity
        appSettingsPrefs.edit().putString("swipe_tab_sensitivity", sensitivity.name).apply()
        addChatMessage("👆 Tab swipe sensitivity set to ${sensitivity.displayName}!")
    }

    // ==========================================
    // NPC COMPANION SETTINGS & STATE
    // ==========================================
    private val _isNpcCompanionsEnabled = MutableStateFlow(npcPrefs.getBoolean("npc_companions_enabled", true))
    val isNpcCompanionsEnabled: StateFlow<Boolean> = _isNpcCompanionsEnabled.asStateFlow()

    private val _isFinbarSessionMinimized = MutableStateFlow(false)
    val isFinbarSessionMinimized: StateFlow<Boolean> = _isFinbarSessionMinimized.asStateFlow()

    private val _isEricSessionMinimized = MutableStateFlow(false)
    val isEricSessionMinimized: StateFlow<Boolean> = _isEricSessionMinimized.asStateFlow()

    fun toggleNpcCompanions() {
        val newVal = !_isNpcCompanionsEnabled.value
        _isNpcCompanionsEnabled.value = newVal
        npcPrefs.edit().putBoolean("npc_companions_enabled", newVal).apply()
    }

    fun setNpcCompanionsEnabled(enabled: Boolean) {
        _isNpcCompanionsEnabled.value = enabled
        npcPrefs.edit().putBoolean("npc_companions_enabled", enabled).apply()
    }

    fun minimizeFinbarForSession() {
        _isFinbarSessionMinimized.value = true
    }

    fun minimizeEricForSession() {
        _isEricSessionMinimized.value = true
    }

    fun restoreNpcCompanions() {
        _isFinbarSessionMinimized.value = false
        _isEricSessionMinimized.value = false
        if (!_isNpcCompanionsEnabled.value) {
            setNpcCompanionsEnabled(true)
        }
    }

    fun getNpcPosition(npcId: String, defaultNormalizedX: Float = 0.82f, defaultNormalizedY: Float = 0.70f): Pair<Float, Float> {
        val x = npcPrefs.getFloat("npc_pos_${npcId}_x", defaultNormalizedX)
        val y = npcPrefs.getFloat("npc_pos_${npcId}_y", defaultNormalizedY)
        return Pair(x, y)
    }

    fun saveNpcPosition(npcId: String, normalizedX: Float, normalizedY: Float) {
        npcPrefs.edit()
            .putFloat("npc_pos_${npcId}_x", normalizedX.coerceIn(0f, 1f))
            .putFloat("npc_pos_${npcId}_y", normalizedY.coerceIn(0f, 1f))
            .apply()
    }

    // ==========================================
    // APP LIFECYCLE HANDLERS (Handled in AFK & Lifecycle Section)
    // ==========================================


    // ==========================================
    // GOOGLE TASKS INTEGRATION
    // ==========================================
    fun loadGoogleTasks() {
        viewModelScope.launch {
            val list = googleTasksRepository.getLocalTasks()
            _googleTasks.value = list
        }
    }

    fun setGoogleOAuthToken(token: String) {
        googleTasksRepository.googleOAuthToken = token
        loadGoogleTasks()
    }

    fun createGoogleTask(title: String, notes: String? = null, due: String? = null) {
        viewModelScope.launch {
            val res = googleTasksRepository.createGoogleTaskApi(title, notes)
            if (res.isSuccess) {
                _googleTasks.value = googleTasksRepository.getLocalTasks()
                addChatMessage("📝 Created task: '$title'!")
            }
        }
    }

    fun completeGoogleTask(taskId: String) {
        viewModelScope.launch {
            val res = googleTasksRepository.completeTaskApi(taskId)
            if (res.isSuccess) {
                _googleTasks.value = googleTasksRepository.getLocalTasks()
                addChatMessage("✅ Completed Google task!")
            }
        }
    }

        // ==========================================
    // INCANTATIONS & MAGIC CHANTS (1-HOUR BOUND DURATION & ONE-TIME GP COST)
    // ==========================================
    fun isIncantationUsable(incantation: com.example.data.models.Incantation): Boolean {
        val magicXp = skillXpMap.value[OsrsSkill.MAGIC] ?: 0L
        val magicLvl = OsrsXpCalculator.getLevelForXp(magicXp)
        return magicLvl >= incantation.reqLevel
    }

    fun isIncantationUsable(incantationId: String): Boolean {
        val incant = com.example.data.models.IncantationsData.ALL_INCANTATIONS.find { it.id == incantationId } ?: return false
        return isIncantationUsable(incant)
    }

    fun getRuneCount(runeItemId: String): Int {
        val normId = com.example.data.models.DefaultItems.normalizeItemId(runeItemId)
        val bQty = bankItems.value.find { it.id == normId || it.id == runeItemId }?.quantity ?: 0
        val iQty = inventoryItems.value.find { it.id == normId || it.id == runeItemId }?.quantity ?: 0
        return bQty.coerceAtLeast(iQty)
    }

    fun hasRunesForIncantation(incantation: com.example.data.models.Incantation): Boolean {
        return incantation.runes.all { runeReq ->
            getRuneCount(runeReq.runeItemId) >= runeReq.quantity
        }
    }

    fun cleanExpiredIncantations(): Set<String> {
        val active = _activeIncantationIds.value.toMutableSet()
        val now = System.currentTimeMillis()
        val iterator = active.iterator()
        var modified = false
        val editor = pohPrefs.edit()
        while (iterator.hasNext()) {
            val id = iterator.next()
            val startTime = pohPrefs.getLong("incantation_start_$id", _activeIncantationTimestamp.value)
            if (startTime > 0L && (now - startTime) >= 3600_000L) {
                iterator.remove()
                editor.remove("incantation_start_$id")
                modified = true
            }
        }
        if (modified) {
            _activeIncantationIds.value = active
            editor.putStringSet("active_incantation_ids", active).apply()
        }
        return active
    }

    fun getIncantationRemainingMs(incantationId: String): Long {
        cleanExpiredIncantations()
        if (!_activeIncantationIds.value.contains(incantationId)) return 0L
        val startTime = pohPrefs.getLong("incantation_start_$incantationId", _activeIncantationTimestamp.value)
        if (startTime <= 0L) return 0L
        val now = System.currentTimeMillis()
        val elapsed = now - startTime
        return (3600_000L - elapsed).coerceAtLeast(0L)
    }

    fun isIncantationActiveAndUsable(incantationId: String): Boolean {
        val active = cleanExpiredIncantations()
        if (!active.contains(incantationId)) return false
        val startTime = pohPrefs.getLong("incantation_start_$incantationId", _activeIncantationTimestamp.value)
        val now = System.currentTimeMillis()
        val elapsed = if (startTime > 0L) now - startTime else 0L
        if (startTime > 0L && elapsed >= 3600_000L) {
            val newSet = active - incantationId
            _activeIncantationIds.value = newSet
            pohPrefs.edit()
                .putStringSet("active_incantation_ids", newSet)
                .remove("incantation_start_$incantationId")
                .apply()
            return false
        }
     x��}]o#I�������!Gl���>�=�$�D�>8${��PM�Z�U���>������p��[������?,l���_�����GDfV�W�j�`m��Ṻ�������H�X짋8dA�G^�zi����ԯ����#��G��d�4����J-��36�f-�֛ͧ~k�^k��i�R*4�;��O)�V�&����q^�8��)�Fip����`���y�cPRo���d�"�d��A�y��i�& ��OO�[�*}0ʇ��%�?k�q��0��'�t`��'���ۿ�GW,�0o���N�̼p̦���g�P=kPk�����IS�Ϡ$��D4CiC?�� 7� 8*�O�_l�oooW�#�TWk�X�j �guN��jK�}����{��>��ݳ�Ak +߱ �b/^0�� c*Jo<޿��?I��^�����m��5>k���g�=c5im	�)뼷��M�����ho���;�o�S^ O�I΢F��s�����R�])<'�E0bS��Gv� ���ib�����1�E��X�jL�5;�_~��_��F��X���Z+��ͦ7KPlt�M;a�-7����Y��8�i� L�������X����t��%�^�؇�l��$3:��'�����4Z�+/HI�<�p��OZ���E�J��"�I��b�O$#��PF�.T� I@l�#*���e��1����߲�/5�����>��.�P�.(N
�\�l}� �Az����JE+HN��3��wu�*���-��Ϣ F|�ך��@�m�%�K����V���E�[�_`�S�SV?B���Q��k*$d�v���⒘��,�ut�Á��?c�
K֑��-v�����#�#�oNP�F��oM�E8�4zB��(�xX�HT$��l�Ezg�ݖ'�0�g��4V�.<�'�b�"�����+��y�iߵ��=/�¢���`��- f����x�]�Ԉk������Ph����#���^�ַ�Y�;�/Z���h`�ZΊ�j��8祺�ׄ7	���()�Q��Z`6e���e/�'I��9�Ok�H�ES��!�+���oF�]�8�����J�j�7E5�*P<N�75F9k{���N���6B�,f3/�3��S����W�A�(��gg�,X�9�f���$ U����� lL	�%��m�o�=��)���|���d�
4o�П� �l�,�_7Y��O`t�i����
�g�,������b�S_�	����>��6Hړ�gВW�r�M��D�T��D�f��촶m3��h¾��1�yG7�%�o�<�1X�[����l��v��� l@�'_E�x�H��&{$���(���0>�MC���h�e�n}uvv��z8���c�����⯃��?��&��l8ޏ�p,�91 ίy��/���铚5�C<�z��[?��u�����*|�O�*d;O+)d0⿺�9�J <�pB�j���)���y'&�`Tj��Ru~q	F]��,gG��4*G?9��Q��Q�Ƕ���l�iN]̴��ݦ�x�����EF�7�Q�1�e.�Xg2	.?Y9~��O;�����`B4��$������6ƕ����B��1��z
�VR'��Yc��t�o�Y��U� �^ {go:�{���dC��?1L�}�Ƌ�}?ᓒ�`�}�j�<��ʝ������e=\L�\ ɮv �ir5 @�$��%Yui��<>\m�;���~t�lE�޴`��`�i0���3�{hל�V-ȫ9��-�&�d0���`�{�_w�̐�N@�#���֝��jͪ�F�7=�	����A�/��Z�m _�*���9��&��^I�����2���M�:���.䍆^�O�;nr��Q�`F��E'v��iȇڿ��5F��z��#�8Z��g�=�;���N��1�S���Od��M���	�
#�����������`
��+���	��ez��f�[���9zV�s�6�Ī�kz��嶏��N�v����cT���==f[�*J�A؊�pK�>�/�o��ha6٧�uT^���!$ZG�v�" ��bE�#o�����.氠�Ǽl^�0���`m�P��/��l��і�=���#2(ix3�	�4J��]��ea����0�P����]�b.F�s��g�B�����j�Y�IJ�M�n��  � iSJ�a���/�jg�7������_�����ܕ���$�Ý�@�D�:����-��@4ip`��`aF�xk|֚s80�|�;��"�PieHr_��Fe����H.-�fz�ZTC-�� ��l�Bӈ��.��d���Or*�FW��\�׽LI]ϻ�*�נE$��X�2�I�
�C>}�@s!�G���w��U�m�|�t�eX8�����)h	��R�Cj>�4��݅��o~��Z��Y���l�L�6�q��?$���C8�a=T0�YϏg^z��-&��c�[���rE���ƖCR��\�[�}o�BK�Q��]M��������qƐ)7���󵧜�����w�Qi��M�B��|���pr���Uԧ��\Y%�������ʋ�X� ,]-�9��+�I�B+���@c�<��z��x��K��֘���]w,�7��^����X��V���m��5y�+������+��{����mM�lF��y�{��۪`mV_Pt��h0
P���2��qK0��{�����kl���M� %������2s��?Yc~̧@>Q���R���S�
L�m� ����ʡ��f�E����N´�Y���������9��v�;�݃A�^�״�uC/�V.�E�SP��q���ۮ���"���\%�t¯�7ijk��/K��3��H��tk����&}�d�CȦ���̛g?Kc*�onbI�+��øBrVM�ZkBU�*��շr��>�qKK!�\�|/ZH(8`����UGOyp����wU��Ȫ� �i���$�1~I��4)q�[�4lq��:�T�.פ V޵�4+��1��v#�q����О4i���A��\�� �I�c_O
A�����y��@0�/*׬Z�-ċ��rz(}V%��2���Q� H��<�湇~�M�k����I�q9:���_OE|l*p��0�0qԜ��z��&��֮;�aw,Mo2i�Φ��� �~�"��yVM�W��!�w�ۏ@Ƃ������w
�I{��l���N��H��[�>��s:|��x�G�`֒e���K�5���H5���hh�@xЧ5ąq a��^�R���Q�V��=/��wC�[�Mqf9�εɆ��LyLɃv�X����K����.h4J�
��zm��#�̱��QI�ȄR4���y��<%�P���?���	�%\U��--o���l�y�v�i��E
����NN)�*تB�B��e� /D��t��"h/��:<��	@iih����`���z0���eʍfC��t|�I�s�FBˉ�жM��s���s��tĪ�h�����We�1�׽�Y�98|�=>�D��;���;��R�cwLAUf�H�W�^�b�D�!�{}�.�~@y[-f�<����l�:O�}q�5�����OX{�D@�[|v��g�Ԅ��ޢ��X?��PP��V=h����9c>��ufٽ`]��G��'@��/�A���'ڹ�|
t�-:x�X(�P8�u�E#P����V
@�b�Ӛd�y�e����b��"�h�]rY�P[;�,��_����x���uq�ӗ�������qi�IXb\#xI��Bˋ���85�Q |£�h�����7-��(J(5��~;/q:!``��7����~�5���rk�b5��a'����!&��Ca��#�TR@�b>�(̮��E�2w�>�(�WM7�X��0���|2`b��xq-���q5o�,�e��4����C�dSd��t�SEn�2��T\�Y+��Nϭ�|��*�/�&��?J�q��� s:r����{)�OO}�0RӮ�g8qN�Z��z6�㫄Շ�~�Th ���M��ʅ�,�ԏM�bAU~��m���Z8n���/2\R���ԬY���\���$�����!*RTuY��[����j�lo7���E��؏l5v�Y�������D�����'�.C��q�g�����q�ۃ�e���=<��;l�8���-,�@���w���R�����0�@a�7� �{���AAy�����c��}��tX��W c�]�Wt4Y�����8`�.(�L:���5;�v�t�0��&�
7��݂m_=����e�Ɇ������1�*�-�tG��ǿ���.��lq�h��Aa���3��6a�+�8*l���¨��:��d�<�ÿzjG�O2�*���q#G0,.LJh�ה��� j��~6�g��m5HéV��%q7Ɯ[R���7)�XحE�+�H����tE���ۖZC�N%d�@����]��?���E�䬖��i���lց$�7w��yU�u���U�����ʬ��R7��,9+MdV3C��XS�ޒkZS$��$#�&<Ne��n����
ֻN-�S�ǲ\Ie�k6�'��J�a4c�T�Zx��d�&L�4g����$�R��B'����&j���>�&?���˟�c&�T�=g�MV��C'��O�'��(�ۧ�蘉~8�խ����N��ʹ�a�V�����2O��C��ް�=e{��i{��U�˳>{�z��~�t��_v�?�'�=t_%	��=
%膓(�0�W�����,J�]@�׊�AjJ ����/�d?�N}<��̲�������~쏳?�B�;����L&>E��T,b���[�U�\�b���Q�Z=C~tA�t@S]��#���e �i8N�:T5�a̫��	�+(�P�.Ըb�"h����+ ���0՗p�U�&}p��b��ƞ�x�('b?��0v^ʵ%_��%=��!7��|���R�]�eP�#
X!yz�1'����h���6Fe&�s�$��ͥ�Y �dG�L��!�Q�A62���.�edQ�,�ȐB��v����.C�I�z�d�U�SI�j�`g���g��a}�ɞ*��+�J�ZX�����1Ã��
X��4x?ip����=45��l���z��y�<��(S^Z��7���<Ńn�J�h'8�ci�SV@�5Z��Amt�k�܏�ٓb,|:��5U�z�k�Q��~��%pȻ Od'Az�LC�M�$ꆅ� 9��r5���'K����O���
�����%(��3���>�}4P��h���\FGdCkړ�lN����N��# �>J�\����=�|�Fh�s.�5�b>8�T�.{Y$����1%>������ �l������-�-&��%l��E3n7��He��++e��q�S���:�U�<� ��J�H}:��^���p�@e��{$.�x͌��l@L�+!%-���������/T�F��N��V�s�V �ŧ�	J)��6�@�3���i�YIN�J��!��}Ǜ�����������5R�@��#qi��l��� )W&r4�c��}t�cN��{s�5�|��G	K|s�p�C@��l�,��;ǜǸ#1�a��N�h������r�1���K�������j�7����!�<c}��1�H�s�������Fﾃ�FY��h�����OU�֮�h�K;(�{%��yx�@�0
f��0�R�"�'�O/Y�
�Tr<��żJ��;º����+Lʲ�,%퀸[��//r��SY�r��塖`VOe�W�S����Z��ku�Y�j�a��ݱ�H��+�vC,�������r7���?\V��Չ	&��e�d"A7D�t�a��I}�/�l=�1&�-@���3�JbČ��VO��Ϳg_˔d��`�B
%x'�������`��I `	��5�[���@���ӏQo����^^����������{Ď�����������e�_�O���v��D�%�Ͻ(I���o�Y	A���:��$�5��Ԅ���/w��@\�A�ּ͊�-A'XN�ZX%�B�_����|�ݮ��ʗ�"k�0�%��~,\�K���-������n�{p�q��?W�%��$�g�F��`�L�[F�Z�"&��ҁ���k���K��n�O��G?G�떴OGR��:p��:�5�{�ߦ���C�Q�Z�uĒxp�ӱk :�Z�\�%צ���ŝ��'�T�JU��%���k���X���&�����`���B1�Zn씗/�J*�b�"�	�������n��+��E4�P�~C�dģ�>x[9��k.���T]����V�`X-�u� �X��/�.����9��r� ��C,�K��MPPU���R��-~�4E1Mv!�o4k3t�	zS�$m��7����<\5)i�4�Vh%A����	L,"��=!F��C��ִ��,h�ޕ�ֱ�+l*��(����.�g�;#z~z���R������Ã�sp
2/�rXZ��
�KR����(:����vI�!�q�"�ds3�8Wyؗ��1����4CR/���s/���!��}T=f�E�F3$ !gW������+ٖ���T'�jq��?���9��5�fv��ӆ����k�"�".JL�s�6�#�Jߺ�?fS�(�����L�EdqX�������gՅ!���	����ܺ�Oq�H�$�ˋYkKY���*x4�`�f�p�~�je�	!��8��kzY�������?;�wXc�)�kA;%�vUyuv�e]�8�_YAI�P�\ˊ��Y�IA�3�CY�&W���n�k�v���Gl�k~]����}��$�X`h�z���6<&�\��(��Z�q���`"G�D�Vr��L��X������cQ�ڵ	ʄa@�c�����2��� 4i`XЇ�00�p�����g
�.[�T	j��-�_Z��>Q�� ��D,�!�z����K,q�������@gုs%�d;ME=�;��yN$�9�l���:R�n���o�s��9�a*��,��J�:��ѣ�K��@/�#�GK��q�1�{�].���sD���w�f�R��?�ꗘ ����[/�#���8�"�c���w�Z�?�/bo~���t���J�t{#	8E �v� 㴘y�4��Ni3�-��V�sޤ����:%v�#���q/}Wdmj��S�zfXF�	13�8�B[>яx0�u�H=#
��^z�	'Iy�_�7�L,�␧���FvnG�o(Nc�9"2��������>�����o;�q����4u�ǯ��ߘ��|�FuW箍��U��Ȩ.06�g4\�2�¹[�Q���/�|�ӈo��n?��gd�W�����N�R\�O6�Ȗ�M+��y�˼yu�Ǳ�������fc+�`%_�UM@�[V��V���Ӥ�#�� X�˺pmu�A{8l�.�d�����P�����΁1ظ��C�=iv��h���øky��C��}��|.�ᘣ�FPkxZD�筥[��I�ۮAq-�K�B:����HG*hi TW��1^���9�0@�u�m�U��1�|�D`�]R v�^�1N�K�-1Ns���YfE�Y��YD�A��p�l1~Ω����b����NUo�h{3J���[��4GW�+��B��V�N9��y]]v�\�U8�[�ٱ�<5N����ɐD�f|^�����;S�X2��V�fp�F�6��q���>��;.�.�c1�!��\�i�D9d]q@�:a�$X��;���GK�y;����J���p�ƶ4N�5@�κ����ߨqA��� �n��Kv�S �Z�՝�2�4(�zY��z�te�;����]��QȽ#�?���S&�Zk���{�ewx&�PȊN�6��9w�8�+�"�^�,�w�wh����%�;yDW?�4	Bo��[�X�NX�8���[�(ηڨ/�, �z��R���Q#��F��Ч�i�_��H�*�䴴`�v<,���f*V�D���j85�j`�cG+��X0�=�:��[��1�)q��ܿF8�]5�y2E��H�b��y��\V!��Lt�Z1�I��L��
����҃�EE�u��3�8HP�5
�;�nC0�� ��S4���F1���M�b�@� Z2�8��G�a�{:�Ǒ�֧U8r��(ӄcJ9u�h�`�`�|1|���8xd�(J�gPu(��k�����ϵ	��AE s��܋�Jv�ہ��_�C�=`{��)�A�=�p�g��Q��<��Pi`�m�޲R-���FX��t��9~�YC|�!��&y�����֝�h���8�O���v����0�5m��h��]Mf���έ��NP�bx�Jع�G��/A�t 3ڴPKJ�k�]�EM�*�Y�����Bg&���̉�cC�����O�mY����N�W�����w�E_�/�:9ُY=���n�/�S+�d̕����K{�E+��
"{Z�����F��{�D�[~�{��,��T鴏M�S��E�ͪ�6��ܲL&]�
P��ʑ{L|X�0W5�o��s{�o�S-��z�}�;��Zc/��(�\dP2�(��9��x�~�-��p�U�J��jk{�.�/E������O�>@k�}�[ c>�ӈ�|�n��Kt.�>����)v�S�$��� ���rc��ME����H����!���$!�T�����$�S�%EQD���u;���u;��$|j��/Q�#�\���ȋ���V8�+؁�-Ev��y��f^��3���we�%���ȱF6w����v�#�繫��5��V��ծ�<y����������R9��B~ �Ns����udwK�n����u ǿ��xCL�5��s�xs����췘V���)��zAܪn�b�׀J����;ú6��� T���ڏ��Zt�;�k
�!���i9�\���.U��-�{Q���n0�1TĶ�$�a:��X�V�$�5�oDԘ��y#9��.�� +	{*���h�`��dn�lZ��Id�SE�b�%2V��c�]�.��ŋI��1wEc>�+"�J+��ъ�`�@�r[�}���j�=]}Z��xq\aİ���xE4��"��h�2�#��b\��{pܩ��a4�.L3y��Ν�Ό9��U�vv�\5$�u`��a�0YQ�vD����W�k�,� H4����b�M���y��[�cݫ��_��F�����ώ�f\��Lq�Ucz�1�ܬ�7�G`����n%۟/H�ב��C8�+w�/�����\z�3��4z�ۋE�n�x��;��:����h\9�'6m2����j��;�#����0���=����F��V���U���!�7Yr���w�=��iC�Q�P��s�鷏�ৃa�u�{�����Q}��~ejS�aLM#<�yaq�^�V�m٣{��t;'2&fF8�����
�9,lz���Ɵ9�@E&j�<���M�s�}ne�5�b��FF2w5�Z�nѯ���sx���7s�ܿ� q�L�T'��d}�7sX!(���I��N^�}������r�I�z��vC�Ɖ��[�8���L�_��8�=Ij/�sֱ��1#�d��+sݷ���|��kw��t,��XL���V��WQiѩ[�[V�����tpN�a�\S?�d0/_x���?�<؎�3��XT,*P�l_Ӛ���m�����k`�
�=���83�G$ԏnʫ�hzĎ]b>*L�B����W��Jݗ�z���nl��θ���s�͜������E�����2k�uwUQ������!%�9�(�UŅ�aVt;��Zv���a���9�Pj�gA�@C�� c�)���7�7�i{��)� �H��\$�ͣ�:�g���3G)!�lD��a*����'�ǜH;˴(�?lĢ�.9�e���;���"M8���	�/{��"�Q���D��7w�}Wn�*'���٥u͎rx�b{�ڊ�3�PJaXJ�籟�̯��^Y�\6�z�:r'Kw��\h��MIA"	�	���(ij�nr���#+��u��BqWK����c��`X�I�F��]���R��l�>m�%��WeS�����e�,�q�Q�k����?������`�}z��)�?;b���Y���
�?�E��D���r&�5�����R��U=O-U\P���v8�̀�ta%�ttӊ`4έ9�|��i5� 8���tš�����w�{��~�D}��id��CN��#��B���ً����c���A�
��0^:��Ki���&�t�[/Gk�.[q����QVX������ަ��έ�w�,�6���>ݺ�ʋg�e�q0���4�+;�*戬�:�<0.�S�F��W�9	�a�k��o%��y���C!{��;Z$o����qp�FW����Y9�d;4�w���?g�qDau!�Q�R7���%m�S��	tن��i�8��S������	7�ț���$�tz��f� I\��1�⿲A45p��̋�������6;�9�O@��c<m\�m
}�h�oQ��0j�=��=�%���&���w�m�� �x��&�ɢ�^AN�h���R%3��J{:m_{��}�F���%�:�pKj�W�H����;�67E�
��I�Q�Q6,T�wr��/v�I�?�'�������F7K��pM7AzI��Rh�/�4����1�{�ݰ@����G�a'�$^���cx�C� j� h>lLI��-��ɕ�(�"�7;_���N:�CJ)�>8{�^�!:���mQx�<��X��R��I�ө�Kl`�/��y�����o`�}Q�������d���i�]@��\崴.@`�{�-��R9�UO��I�<�Ewk+h��$��QFH�ݝŧHor~|/
xP	ގ���ji�E��7>�4�]Xz�5�_���X�bR?�
v��L�#�h�jU\U�Io�EZ�>a�����w���3�M�9Q�%��|�Xu���t���f|P�y'���E�<DVu����#��;f��J�-ϽT�A���i{�=;e�W����� �,,O'����=4�a2[��1��&����Y��fX�j(�6�,Zj[�:�Pcp��zο�Ͻ�_���%O�ں#�k��k6M�[��&���I�2�J���~�.V�m��q:N�n�yB|�t���+�r\4���E]���O��7�+��ɺ�1��%� �t?e&"�fķ�&a��;�K�!�S�u���Ǽ)-j"t�k�E�a�2iZ����?7�	$>U>]կ4�(m)i����],�u���yHvV�\~�GV�V�5>٧~��w�b��j~(�����V2���&}h�#�B�����BP�Av�<���k�����1v����\����8H0ΛK�z���y�o��u�P���u;_r�~���l�y�o�'@#�9,*���]5��R�8�R�H'nQ��|-��=���Z���b#k9UE0Te]$��|Q��4����J���k�BSU ����'�5�B�aYV�#/`46�<���^��f�k=X�ϣѕ�9��<���};���Ecw9��`P�^�T��͉8ܛ���oڔ�"������h ��g2�� �'4��2��?��kM~&g�f���g��`x��ujK�b���;��������%Z.�� ���]���ߣgWuIפ}�������w��ygx~��XS�)vPo��^��*[���j~�:�Y�Z-W�Fs�K�����G.��sމ�k�1Y�+b��W�2>V�d�R��)��gZi����W~�k�yS����X	�/�E:v�ɔ�<��,�i0���"P�YM#�l%0(��=�x$�p��툎+G�B^'��뵀��'S?��Lϣ�9f ?O�ԬPB��ym��P�+��n�ݧ��͎�&�K�b�BQ�t#{���
�0��I�b&j�t�[(A�L��Ofl�1�RA�7��*nJ��$G,U�k�$��R\J�$�+@A� |��� ��?34��-���	�)NՂqӨo�㢓JMr�� kc�&M�]3͢|��S  <���Z��ž������l�����j�"�S��Y}k�)��2Î�,T�{�5�^0]5������-u^�}���E�+��g!NV�Ǝ���}rb �~أnS�ͼ+�C��E�¹�P��>U��Y^ߡ��BB���R(�ω�H��찶uH����'�2[������o��S�ޘ�3+8�N������(A���w�9G�Tk���kD��l2���7�7��gJ��?��Q�>F�SR[����ǝh������u5u��N�*�Se�墠Ľ�﫪b'�����ʣ�x��f+��P*�E<�F�t���`W}O�m("4�\��:!�BM�C���qN��)�Y��p��y�>Q��MJɯ,U�7ʁ(D�4鈩a	��<c%�-B�Z���g�ꛄ���T�(����g~��"�L�}��g�'A��9)�u@9��$��֍D��E�C1[K-�
��Hg��!"T��A�[/�����1+| �ęST Z��h..D�4,}�ƉSHoo��NYƒ��H�=�����Eʢ	}b��	�A���u��v�'����f�mƃ�8�E{76�w{J�.�z��)0���U��{	.#P]��%�M�� ��a�K,���W<Đ;H�4��v�L���H�d����c��A\Q������C���j�lo7�@g��%��ἷC�E��t�x�u@Ց�k� ��G9W�S�z{{�VSA��������Bb�2�};���s���|��:�{l�m����g�Γ��|�Iٝ�槪{��U���1�Qh��;I�RU]ozx`�,۔r�rq#�5{�H#��m������}��e=�u��'����e��V0ٛ����RW8�q��;X���\�*U�dWp��*{U����մ��J��-�����-2"�*|h�� �~|�Y��i����X�[�]$�T���Ԣ���}n���p��o+&;,��%�	'�p��xl�Os;̼{H���"4:ƀ���֮�!|Q�M9 �cΝ�3��ɤl6B��`�y���^Q�&*�S��|C	�gS[�.�)ZO�5K�	ϊ�_aRB��v�[�'%5Q�a����#��,b��-sr��%�ۦڝ
%��	��M��/WS���d�` �]��#v^�uxF7!C�`��}�A��f����V��'�9ǣw,�
�0�0ٮ\�
�V��͏�e-G�D��V��ַ�����`�Ҁr�i=�b.7�!�F�L����fƮ�f�eƁ�o�*�S�_jа����ȟ�x*���d��x0K~�g;']��C���y��O`�^��x���^S�玢p�g��^!�1����R:�!<�Y2��Eݿ/�< ލv���  �� #�̴