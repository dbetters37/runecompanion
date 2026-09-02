package com.example.data.repository

import android.content.Context
import com.example.data.db.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PetRepository(private val dao: PetDao, private val context: Context? = null) {

    private fun getBackupQuestIds(): Set<String> {
        return try {
            context?.getSharedPreferences("osrs_trainer_quest_backup", Context.MODE_PRIVATE)
                ?.getStringSet("completed_quest_ids", emptySet()) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun saveBackupQuestIds(ids: List<String>) {
        try {
            context?.getSharedPreferences("osrs_trainer_quest_backup", Context.MODE_PRIVATE)?.let { prefs ->
                val existing = prefs.getStringSet("completed_quest_ids", emptySet()) ?: emptySet()
                val merged = (existing + ids).filter { it.isNotBlank() }.toSet()
                prefs.edit().putStringSet("completed_quest_ids", merged).apply()
            }
        } catch (e: Exception) {
            // ignore backup save errors
        }
    }

    fun getAllQuestProgress(petTypeName: String): Flow<Map<String, QuestProgressEntity>> = dao.getAllQuestProgress(petTypeName).map { list ->
        list.associateBy { it.questId }
    }

    suspend fun saveQuestProgress(progress: QuestProgressEntity) {
        dao.saveQuestProgress(progress)
    }

    suspend fun deleteQuestProgress(petTypeName: String, questId: String) {
        dao.deleteQuestProgress(petTypeName, questId)
    }

    suspend fun deleteAllQuestProgress(petTypeName: String) {
        dao.deleteAllQuestProgress(petTypeName)
    }

    val petState: Flow<PetState> = dao.getPetState().map { entity ->
        if (entity == null) {
            PetState()
        } else {
            val completedList = if (entity.completedQuestIdsCsv.isBlank()) {
                emptyList()
            } else {
                entity.completedQuestIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
            val mergedCompletedList = (completedList + getBackupQuestIds()).distinct().filter { it.isNotEmpty() }
            val unlockedOutfits = if (entity.unlockedOutfitIdsCsv.isBlank()) {
                listOf("default", "barrows_dharok", "pokemon_pikachu", "skilling_graceful")
            } else {
                (entity.unlockedOutfitIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() } + "default").distinct()
            }
            PetState(
                petType = try { PetType.valueOf(entity.petTypeName) } catch (e: Exception) { PetType.BABY_BLACK_DRAGON },
                customName = entity.customName,
                hunger = entity.hunger,
                happiness = entity.happiness,
                energy = entity.energy,
                health = entity.health,
                coinsGp = entity.coinsGp,
                questPoints = entity.questPoints,
                completedQuestIds = mergedCompletedList,
                currentEmote = try { PetEmote.valueOf(entity.currentEmoteName) } catch (e: Exception) { PetEmote.IDLE },
                currentQuote = entity.currentQuote,
                currentOutfitId = entity.currentOutfitId.ifBlank { "default" },
                unlockedOutfitIds = unlockedOutfits
            )
        }
    }

    val skillXpMap: Flow<Map<OsrsSkill, Long>> = dao.getAllSkillXp().map { list ->
        val map = OsrsSkill.entries.associateWith { skill ->
            if (skill == OsrsSkill.HITPOINTS) 1154L else 0L
        }.toMutableMap()
        list.forEach { entity ->
            val skill = OsrsSkill.fromName(entity.skillName)
            map[skill] = entity.xp
        }
        map
    }

    fun getSkillXpForPet(petTypeName: String): Flow<Map<OsrsSkill, Long>> = dao.getPetSkillXp(petTypeName).map { list ->
        val map = OsrsSkill.entries.associateWith { skill ->
            if (skill == OsrsSkill.HITPOINTS) 1154L else 0L
        }.toMutableMap()
        list.forEach { entity ->
            val skill = OsrsSkill.fromName(entity.skillName)
            map[skill] = entity.xp
        }
        map
    }

    fun getInventoryItems(petTypeName: String): Flow<List<InventoryItem>> = dao.getInventoryItems(petTypeName).map { list ->
        val itemMap = mutableMapOf<String, InventoryItem>()
        list.forEach { entity ->
            val normalizedId = DefaultItems.normalizeItemId(entity.itemId)
            val baseItem = DefaultItems.getItemById(normalizedId)
            val existing = itemMap[normalizedId]
            if (existing != null) {
                itemMap[normalizedId] = existing.copy(quantity = existing.quantity + entity.quantity)
            } else {
                itemMap[normalizedId] = baseItem.copy(id = normalizedId, quantity = entity.quantity)
            }
        }
        itemMap.values.toList()
    }

    fun getBankItems(petTypeName: String): Flow<List<InventoryItem>> = dao.getBankItems(petTypeName).map { list ->
        val itemMap = mutableMapOf<String, InventoryItem>()
        list.forEach { entity ->
            val normalizedId = DefaultItems.normalizeItemId(entity.itemId)
            val baseItem = DefaultItems.getItemById(normalizedId)
            val existing = itemMap[normalizedId]
            if (existing != null) {
                itemMap[normalizedId] = existing.copy(quantity = existing.quantity + entity.quantity)
            } else {
                itemMap[normalizedId] = baseItem.copy(id = normalizedId, quantity = entity.quantity)
            }
        }
        itemMap.values.toList()
    }

    val quests: Flow<List<AiQuest>> = dao.getQuests().map { list ->
        list.map { entity ->
            AiQuest(
                id = entity.questId,
                title = entity.title,
                description = entity.description,
                realLifeTaskInstructions = entity.realLifeTaskInstructions,
                targetSkill = OsrsSkill.fromName(entity.targetSkillName),
                rewardXp = entity.rewardXp,
                rewardGp = entity.rewardGp,
                isCompleted = entity.isCompleted
            )
        }
    }

    val activityLogs: Flow<List<ActivityLog>> = dao.getActivityLogs().map { list ->
        list.map { entity ->
            ActivityLog(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                skill = OsrsSkill.fromName(entity.skillName),
                xpGained = entity.xpGained,
                coinsGained = entity.coinsGained,
                timestamp = entity.timestamp
            )
        }
    }

    fun getEquippedItems(petTypeName: String): Flow<Map<EquipmentSlot, InventoryItem>> = dao.getEquippedItems(petTypeName).map { list ->
        val result = mutableMapOf<EquipmentSlot, InventoryItem>()
        list.forEach { entity ->
            val slot = try { EquipmentSlot.valueOf(entity.slotName) } catch (e: Exception) { null }
            val item = DefaultItems.getItemById(entity.itemId)
            val effectiveSlot = slot ?: item.equipmentSlot
            if (effectiveSlot != null) {
                result[effectiveSlot] = item
            }
        }
        result
    }

    suspend fun equipItem(petTypeName: String, slot: EquipmentSlot, item: InventoryItem) {
        dao.saveEquippedItem(EquippedEntity(petTypeName = petTypeName, slotName = slot.name, itemId = item.id))
    }

    suspend fun unequipSlot(petTypeName: String, slot: EquipmentSlot) {
        dao.deleteEquippedItem(petTypeName = petTypeName, slotName = slot.name)
    }

    suspend fun getEquippedItemsDirect(petTypeName: String): Map<EquipmentSlot, InventoryItem> {
        val catalogMap = DefaultItems.ALL_SHOP_ITEMS.associateBy { it.id }
        val list = dao.getEquippedItemsDirect(petTypeName)
        val result = mutableMapOf<EquipmentSlot, InventoryItem>()
        list.forEach { entity ->
            val slot = try { EquipmentSlot.valueOf(entity.slotName) } catch (e: Exception) { null }
            val item = catalogMap[entity.itemId]
            if (slot != null && item != null) {
                result[slot] = item
            }
        }
        return result
    }

    suspend fun deleteInventoryItem(petTypeName: String, itemId: String) {
        dao.deleteInventoryItem(petTypeName, itemId)
    }

    suspend fun savePetState(pet: PetState) {
        saveBackupQuestIds(pet.completedQuestIds)
        dao.savePetState(
            PetEntity(
                id = 1,
                petTypeName = pet.petType.name,
                customName = pet.customName,
                hunger = pet.hunger,
                happiness = pet.happiness,
                energy = pet.energy,
                health = pet.health,
                coinsGp = pet.coinsGp,
                currentEmoteName = pet.currentEmote.name,
                currentQuote = pet.currentQuote,
                totalStepsTracked = 0L,
                questPoints = pet.questPoints,
                completedQuestIdsCsv = pet.completedQuestIds.distinct().joinToString(","),
                currentOutfitId = pet.currentOutfitId,
                unlockedOutfitIdsCsv = pet.unlockedOutfitIds.distinct().joinToString(",")
            )
        )
        dao.saveIndividualPetStats(
            IndividualPetEntity(
                petTypeName = pet.petType.name,
                customName = pet.customName,
                hunger = pet.hunger,
                happiness = pet.happiness,
                energy = pet.energy,
                health = pet.health,
                currentEmoteName = pet.currentEmote.name,
                currentQuote = pet.currentQuote,
                questPoints = pet.questPoints,
                completedQuestIdsCsv = pet.completedQuestIds.distinct().joinToString(","),
                currentOutfitId = pet.currentOutfitId,
                unlockedOutfitIdsCsv = pet.unlockedOutfitIds.distinct().joinToString(",")
            )
        )
    }

    suspend fun restoreOrCreatePetState(
        newType: PetType,
        currentGp: Long,
        unlockedList: List<PetType>
    ): PetState {
        val individual = dao.getIndividualPetStatsDirect(newType.name)
        val restored = if (individual != null) {
            val completedList = if (individual.completedQuestIdsCsv.isBlank()) {
                emptyList()
            } else {
                individual.completedQuestIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
            val unlockedOutfits = if (individual.unlockedOutfitIdsCsv.isBlank()) {
                listOf("default", "barrows_dharok", "pokemon_pikachu", "skilling_graceful")
            } else {
                (individual.unlockedOutfitIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() } + "default").distinct()
            }
            PetState(
                petType = newType,
                customName = individual.customName,
                hunger = individual.hunger,
                happiness = individual.happiness,
                energy = individual.energy,
                health = individual.health,
                coinsGp = currentGp,
                questPoints = individual.questPoints,
                completedQuestIds = completedList,
                currentEmote = try { PetEmote.valueOf(individual.currentEmoteName) } catch (e: Exception) { PetEmote.HAPPY },
                currentQuote = individual.currentQuote,
                currentOutfitId = individual.currentOutfitId.ifBlank { "default" },
                unlockedOutfitIds = unlockedOutfits,
                unlockedPets = unlockedList
            )
        } else {
            PetState(
                petType = newType,
                customName = newType.displayName,
                hunger = 85,
                happiness = 90,
                energy = 90,
                health = 110,
                coinsGp = currentGp,
                questPoints = 0,
                completedQuestIds = emptyList(),
                currentEmote = PetEmote.HAPPY,
                currentQuote = newType.defaultQuote,
                unlockedPets = unlockedList
            )
        }
        savePetState(restored)
        return restored
    }

    suspend fun getPetStateDirect(): PetState? {
        val entity = dao.getPetStateDirect() ?: return null
        val completedList = if (entity.completedQuestIdsCsv.isBlank()) {
            emptyList()
        } else {
            entity.completedQuestIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        val unlockedOutfits = if (entity.unlockedOutfitIdsCsv.isBlank()) {
            listOf("default", "barrows_dharok", "pokemon_pikachu", "skilling_graceful")
        } else {
            (entity.unlockedOutfitIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() } + "default").distinct()
        }
        return PetState(
            petType = try { PetType.valueOf(entity.petTypeName) } catch (e: Exception) { PetType.BABY_BLACK_DRAGON },
            customName = entity.customName,
            hunger = entity.hunger,
            happiness = entity.happiness,
            energy = entity.energy,
            health = entity.health,
            coinsGp = entity.coinsGp,
            questPoints = entity.questPoints,
            completedQuestIds = completedList,
            currentEmote = try { PetEmote.valueOf(entity.currentEmoteName) } catch (e: Exception) { PetEmote.IDLE },
            currentQuote = entity.currentQuote,
            currentOutfitId = entity.currentOutfitId.ifBlank { "default" },
            unlockedOutfitIds = unlockedOutfits
        )
    }

    suspend fun getInventoryItemsDirect(petTypeName: String): List<InventoryItem> {
        val list = dao.getInventoryItemsDirect(petTypeName)
        val catalogMap = DefaultItems.ALL_SHOP_ITEMS.associateBy { it.id }
        val itemMap = mutableMapOf<String, InventoryItem>()
        list.forEach { entity ->
            val normalizedId = DefaultItems.normalizeItemId(entity.itemId)
            val baseItem = catalogMap[normalizedId] ?: DefaultItems.getItemById(normalizedId)
            val existing = itemMap[normalizedId]
            if (existing != null) {
                itemMap[normalizedId] = existing.copy(quantity = existing.quantity + entity.quantity)
            } else {
                itemMap[normalizedId] = baseItem.copy(id = normalizedId, quantity = entity.quantity)
            }
        }
        return itemMap.values.toList()
    }

    suspend fun getBankItemsDirect(petTypeName: String): List<InventoryItem> {
        val list = dao.getBankItemsDirect(petTypeName)
        val catalogMap = DefaultItems.ALL_SHOP_ITEMS.associateBy { it.id }
        val itemMap = mutableMapOf<String, InventoryItem>()
        list.forEach { entity ->
            val normalizedId = DefaultItems.normalizeItemId(entity.itemId)
            val baseItem = catalogMap[normalizedId] ?: DefaultItems.getItemById(normalizedId)
            val existing = itemMap[normalizedId]
            if (existing != null) {
                itemMap[normalizedId] = existing.copy(quantity = existing.quantity + entity.quantity)
            } else {
                itemMap[normalizedId] = baseItem.copy(id = normalizedId, quantity = entity.quantity)
            }
        }
        return itemMap.values.toList()
    }

    suspend fun getAllSkillXpDirect(petTypeName: String): Map<OsrsSkill, Long> {
        val list = dao.getPetSkillXpDirect(petTypeName)
        val map = OsrsSkill.entries.associateWith { skill ->
            if (skill == OsrsSkill.HITPOINTS) 1154L else 0L
        }.toMutableMap()
        list.forEach { entity ->
            val skill = OsrsSkill.fromName(entity.skillName)
            map[skill] = entity.xp
        }
        val globalList = dao.getAllSkillXpDirect()
        globalList.forEach { entity ->
            val skill = OsrsSkill.fromName(entity.skillName)
            if ((map[skill] ?: 0L) < entity.xp) {
                map[skill] = entity.xp
            }
        }
        return map
    }

    suspend fun getSkillXpDirect(petTypeName: String, skill: OsrsSkill): Long {
        val list = dao.getPetSkillXpDirect(petTypeName)
        val entity = list.find { it.skillName == skill.name }
        if (entity != null) return entity.xp
        return if (skill == OsrsSkill.HITPOINTS) 1154L else 0L
    }

    suspend fun addXpToSkillDirect(
        petTypeName: String,
        skill: OsrsSkill,
        amount: Long,
        gpReward: Long = 0L,
        logTitle: String,
        logDesc: String
    ) {
        val oldXp = getSkillXpDirect(petTypeName, skill)
        val newXp = oldXp + amount

        val key = "${petTypeName}_${skill.name}"
        dao.savePetSkillXp(PetSkillXpEntity(petSkillKey = key, petTypeName = petTypeName, skillName = skill.name, xp = newXp))
        dao.saveSkillXp(SkillXpEntity(skillName = skill.name, xp = newXp))

        dao.insertActivityLog(
            ActivityLogEntity(
                title = logTitle,
                description = logDesc,
                skillName = skill.name,
                xpGained = amount,
                coinsGained = gpReward,
                timestamp = System.currentTimeMillis()
            )
        )

        val petEntity = dao.getPetStateDirect()
        if (petEntity != null) {
            val updatedPet = petEntity.copy(
                coinsGp = petEntity.coinsGp + gpReward,
                happiness = (petEntity.happiness + 5).coerceAtMost(100),
                currentQuote = "Gained +$amount ${skill.displayName} XP from background activity ($logTitle)!"
            )
            dao.savePetState(updatedPet)
        }
    }

    suspend fun resetSkillXp(petTypeName: String, skill: OsrsSkill) {
        val resetXp = if (skill == OsrsSkill.HITPOINTS) 1154L else 0L
        val key = "${petTypeName}_${skill.name}"
        dao.savePetSkillXp(PetSkillXpEntity(petSkillKey = key, petTypeName = petTypeName, skillName = skill.name, xp = resetXp))
        dao.saveSkillXp(SkillXpEntity(skillName = skill.name, xp = resetXp))
    }

    suspend fun resetPetAllXp(petTypeName: String) {
        dao.resetPetSkillXp(petTypeName)
    }

    suspend fun addSkillXp(petTypeName: String, skill: OsrsSkill, amount: Long) {
        addXpToSkillDirect(petTypeName, skill, amount, 0L, "Skill Action", "Gained $amount XP")
    }

    suspend fun saveAllSkillXp(map: Map<OsrsSkill, Long>) {
        dao.saveAllSkillXp(
            map.map { (skill, xp) ->
                SkillXpEntity(skillName = skill.name, xp = xp)
            }
        )
    }

    suspend fun migrateInventoryToStorage(petTypeName: String) {
        try {
            val invList = dao.getInventoryItemsDirect(petTypeName)
            if (invList.isNotEmpty()) {
                val bankList = dao.getBankItemsDirect(petTypeName).associateBy { DefaultItems.normalizeItemId(it.itemId) }
                invList.forEach { inv ->
                    val normId = DefaultItems.normalizeItemId(inv.itemId)
                    val currentBankQty = bankList[normId]?.quantity ?: 0
                    saveBankItem(petTypeName, normId, currentBankQty + inv.quantity)
                    dao.deleteInventoryItem(petTypeName, inv.itemId)
                    if (inv.itemId != normId) {
                        dao.deleteInventoryItem(petTypeName, normId)
                    }
                }
            }
        } catch (e: Exception) {
            // Migration safety catch
        }
    }

    suspend fun saveInventoryItem(petTypeName: String, itemId: String, quantity: Int) {
        // Player inventory is unified into Bank / Storage
        saveBankItem(petTypeName, itemId, quantity)
    }

    suspend fun deduplicateDatabaseItems(petTypeName: String) {
        val bankList = dao.getBankItemsDirect(petTypeName)
        val bankGrouped = bankList.groupBy { DefaultItems.normalizeItemId(it.itemId) }
        bankGrouped.forEach { (normalizedId, list) ->
            val hasNonNormalized = list.any { it.itemId != normalizedId }
            if (list.size > 1 || hasNonNormalized) {
                val totalQty = list.maxOfOrNull { it.quantity } ?: list.first().quantity
                list.forEach { entity ->
                    dao.deleteBankItem(petTypeName, entity.itemId)
                }
                if (totalQty > 0) {
                    dao.saveBankItem(BankEntity(petTypeName = petTypeName, itemId = normalizedId, quantity = totalQty))
                }
            }
        }
        val invList = dao.getInventoryItemsDirect(petTypeName)
        if (invList.isNotEmpty()) {
            invList.forEach { inv ->
                val normId = DefaultItems.normalizeItemId(inv.itemId)
                val existingBank = dao.getBankItemsDirect(petTypeName).find { DefaultItems.normalizeItemId(it.itemId) == normId }
                if (existingBank == null && inv.quantity > 0) {
                    dao.saveBankItem(BankEntity(petTypeName, normId, inv.quantity))
                }
                dao.deleteInventoryItem(petTypeName, inv.itemId)
            }
        }
    }

    suspend fun saveBankItem(petTypeName: String, itemId: String, quantity: Int) {
        val normalizedId = DefaultItems.normalizeItemId(itemId)

        // Delete any alias rows for this item in bank
        val bankList = dao.getBankItemsDirect(petTypeName)
        bankList.forEach { entity ->
            val eNorm = DefaultItems.normalizeItemId(entity.itemId)
            if (entity.itemId != normalizedId && (eNorm == normalizedId || entity.itemId == itemId)) {
                dao.deleteBankItem(petTypeName, entity.itemId)
            }
        }

        // Delete any leftover rows for this item in inventory
        val invList = dao.getInventoryItemsDirect(petTypeName)
        invList.forEach { entity ->
            val eNorm = DefaultItems.normalizeItemId(entity.itemId)
            if (eNorm == normalizedId || entity.itemId == itemId || entity.itemId == normalizedId) {
                dao.deleteInventoryItem(petTypeName, entity.itemId)
            }
        }

        if (quantity <= 0) {
            dao.deleteBankItem(petTypeName, normalizedId)
            if (itemId != normalizedId) {
                dao.deleteBankItem(petTypeName, itemId)
            }
        } else {
            dao.saveBankItem(BankEntity(petTypeName, normalizedId, quantity))
        }
    }

    suspend fun addBankItem(petTypeName: String, itemId: String, amount: Int) {
        if (amount <= 0) return
        val normalizedId = DefaultItems.normalizeItemId(itemId)
        val bankList = dao.getBankItemsDirect(petTypeName)
        var currentQty = 0
        bankList.forEach { entity ->
            val eNorm = DefaultItems.normalizeItemId(entity.itemId)
            if (entity.itemId == normalizedId || entity.itemId == itemId || eNorm == normalizedId) {
                currentQty += entity.quantity
                dao.deleteBankItem(petTypeName, entity.itemId)
            }
        }
        val newQty = currentQty + amount
        dao.saveBankItem(BankEntity(petTypeName, normalizedId, newQty))
    }

    suspend fun deductItem(petTypeName: String, itemId: String, amount: Int, itemName: String? = null): Boolean {
        if (amount <= 0) return true
        var remainingToDeduct = amount
        val normalizedTarget = DefaultItems.normalizeItemId(itemId)

        fun matchesEntity(entityItemId: String): Boolean {
            if (entityItemId == itemId) return true
            if (entityItemId == normalizedTarget) return true
            val norm = DefaultItems.normalizeItemId(entityItemId)
            if (norm == normalizedTarget) return true
            if (itemName != null) {
                val item = DefaultItems.getItemById(entityItemId)
                if (item.name.equals(itemName, ignoreCase = true)) return true
            }
            return false
        }

        // Deduct from bank_items
        val bankList = dao.getBankItemsDirect(petTypeName)
        val matchingBank = bankList.filter { matchesEntity(it.itemId) }
        var totalBankQty = 0
        for (b in matchingBank) {
            if (remainingToDeduct > 0) {
                val take = minOf(b.quantity, remainingToDeduct)
                val newQty = b.quantity - take
                remainingToDeduct -= take
                if (newQty > 0) {
                    totalBankQty += newQty
                }
            } else {
                totalBankQty += b.quantity
            }
            dao.deleteBankItem(petTypeName, b.itemId)
        }
        if (totalBankQty > 0) {
            dao.saveBankItem(BankEntity(petTypeName, normalizedTarget, totalBankQty))
        }

        // Fallback: if any legacy inventory entities exist
        if (remainingToDeduct > 0) {
            val invList = dao.getInventoryItemsDirect(petTypeName)
            val matchingInv = invList.filter { matchesEntity(it.itemId) }
            for (inv in matchingInv) {
                if (remainingToDeduct <= 0) break
                val take = minOf(inv.quantity, remainingToDeduct)
                val newQty = inv.quantity - take
                remainingToDeduct -= take
                if (newQty <= 0) {
                    dao.deleteInventoryItem(petTypeName, inv.itemId)
                } else {
                    dao.saveInventoryItem(InventoryEntity(petTypeName, inv.itemId, newQty))
                }
            }
        }

        return remainingToDeduct == 0
    }

    suspend fun depositItemToBank(petTypeName: String, itemId: String, qtyToDeposit: Int) {
        // No-op since items are already in bank
    }

    suspend fun withdrawItemFromBank(petTypeName: String, itemId: String, qtyToWithdraw: Int) {
        // No-op since items are already in bank
    }

    suspend fun depositAllInventoryToBank(petTypeName: String) {
        migrateInventoryToStorage(petTypeName)
    }

    suspend fun saveQuest(quest: AiQuest) {
        dao.saveQuest(
            QuestEntity(
                questId = quest.id,
                title = quest.title,
                description = quest.description,
                realLifeTaskInstructions = quest.realLifeTaskInstructions,
                targetSkillName = quest.targetSkill.name,
                rewardXp = quest.rewardXp,
                rewardGp = quest.rewardGp,
                isCompleted = quest.isCompleted
            )
        )
    }

    suspend fun saveAllQuests(quests: List<AiQuest>) {
        dao.saveAllQuests(
            quests.map { q ->
                QuestEntity(
                    questId = q.id,
                    title = q.title,
                    description = q.description,
                    realLifeTaskInstructions = q.realLifeTaskInstructions,
                    targetSkillName = q.targetSkill.name,
                    rewardXp = q.rewardXp,
                    rewardGp = q.rewardGp,
                    isCompleted = q.isCompleted
                )
            }
        )
    }

    suspend fun addActivityLog(log: ActivityLog) {
        dao.insertActivityLog(
            ActivityLogEntity(
                title = log.title,
                description = log.description,
                skillName = log.skill.name,
                xpGained = log.xpGained,
                coinsGained = log.coinsGained,
                timestamp = log.timestamp
            )
        )
    }

    suspend fun seedStarterInventoryIfNeeded(petTypeName: String) {
        val existingInventory = dao.getInventoryItemsDirect(petTypeName)
        if (existingInventory.isEmpty()) {
            saveInventoryItem(petTypeName, "item_lobster", 5)
            saveInventoryItem(petTypeName, "item_logs", 10)
            saveInventoryItem(petTypeName, "item_bar_bronze", 5)
            saveInventoryItem(petTypeName, "item_shortbow", 1)
            saveInventoryItem(petTypeName, "item_bronze_arrows", 25)
            saveInventoryItem(petTypeName, "item_toy_mouse", 1)
            saveInventoryItem(petTypeName, "item_bones", 10)
            saveInventoryItem(petTypeName, "item_big_bones", 5)
            saveInventoryItem(petTypeName, "item_bird_snare", 10)
            saveInventoryItem(petTypeName, "item_net_trap", 10)
            saveInventoryItem(petTypeName, "item_box_trap", 10)
            saveInventoryItem(petTypeName, "item_noose_wand", 5)
            saveInventoryItem(petTypeName, "item_impling_net", 5)
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        val defaultPetType = dao.getPetStateDirect()?.petTypeName ?: PetType.BABY_BLACK_DRAGON.name
        val existingSkills = dao.getAllSkillXpDirect()
        if (existingSkills.isEmpty()) {
            val defaultSkillsMap = OsrsSkill.entries.associateWith { 0L }.toMutableMap()
            defaultSkillsMap[OsrsSkill.HITPOINTS] = 1154L
            saveAllSkillXp(defaultSkillsMap)
        }

        seedStarterInventoryIfNeeded(defaultPetType)

        val existingQuests = dao.getQuestsDirect()
        if (existingQuests.isEmpty()) {
            val initialQuests = listOf(
                AiQuest(
                    id = "q_lumbridge_walk",
                    title = "Lumbridge Walkathon",
                    description = "Build agility and stamina by walking in real life!",
                    realLifeTaskInstructions = "Walk 300 steps or do a 5-minute walk.",
                    targetSkill = OsrsSkill.AGILITY,
                    rewardXp = 800L,
                    rewardGp = 200L
                ),
                AiQuest(
                    id = "q_varrock_reading",
                    title = "Varrock Library Study",
                    description = "Channel arcane magical focus through real-life learning.",
                    realLifeTaskInstructions = "Read a book, manual, or educational article.",
                    targetSkill = OsrsSkill.MAGIC,
                    rewardXp = 1000L,
                    rewardGp = 250L
                ),
                AiQuest(
                    id = "q_notification_slash",
                    title = "Notification Lumberjack",
                    description = "Clear digital clutter like chopping down Yew trees!",
                    realLifeTaskInstructions = "Swipe away notifications or clear 3 inbox messages.",
                    targetSkill = OsrsSkill.WOODCUTTING,
                    rewardXp = 600L,
                    rewardGp = 150L
                )
            )
            saveAllQuests(initialQuests)
        }

        val existingPet = dao.getPetStateDirect()
        if (existingPet == null) {
            savePetState(
                PetState(
                    petType = PetType.BABY_BLACK_DRAGON,
                    customName = "Baby Black Dragon",
                    hunger = 85,
                    happiness = 90,
                    energy = 95,
                    health = 110,
                    coinsGp = 500L,
                    currentEmote = PetEmote.IDLE,
                    currentQuote = "Rawr! Welcome to RuneCompanion! Train your real life skills to level up!"
                )
            )
        }
    }

    val allTribeNpcs: Flow<List<NpcEntity>> = dao.getAllTribeNpcs()

    suspend fun getAllTribeNpcsDirect(): List<NpcEntity> = dao.getAllTribeNpcsDirect()

    suspend fun saveNpcProgress(npcId: String, completedFavorsCount: Int, affinityXp: Long) {
        val existing = dao.getNpcDirect(npcId)
        val entity = NpcEntity(
            npcId = npcId,
            completedFavorsCount = completedFavorsCount,
            affinityXp = affinityXp,
            lastFavorCompletedMs = System.currentTimeMillis()
        )
        dao.saveNpc(entity)
    }

    // ================= FAVOR ENGINE REPOSITORY =================
    fun getFavorContracts(petTypeName: String): Flow<List<FavorContractEntity>> =
        dao.getFavorContracts(petTypeName)

    suspend fun getFavorContractsDirect(petTypeName: String): List<FavorContractEntity> =
        dao.getFavorContractsDirect(petTypeName)

    suspend fun saveFavorContract(contract: FavorContractEntity) {
        dao.saveFavorContract(contract)
    }

    suspend fun saveAllFavorContracts(contracts: List<FavorContractEntity>) {
        dao.saveAllFavorContracts(contracts)
    }

    suspend fun deleteFavorContract(petTypeName: String, skillName: String) {
        dao.deleteFavorContract(petTypeName, skillName)
    }

    suspend fun deleteAllFavorContracts(petTypeName: String) {
        dao.deleteAllFavorContracts(petTypeName)
    }

    fun getAllNpcFavorProgress(petTypeName: String): Flow<List<NpcFavorProgressEntity>> =
        dao.getAllNpcFavorProgress(petTypeName)

    suspend fun getAllNpcFavorProgressDirect(petTypeName: String): List<NpcFavorProgressEntity> =
        dao.getAllNpcFavorProgressDirect(petTypeName)

    suspend fun saveNpcFavorProgress(entity: NpcFavorProgressEntity) {
        dao.saveNpcFavorProgress(entity)
    }

    suspend fun saveAllNpcFavorProgress(entities: List<NpcFavorProgressEntity>) {
        dao.saveAllNpcFavorProgress(entities)
    }

    fun getFavorHistory(petTypeName: String): Flow<List<FavorHistoryEntity>> =
        dao.getFavorHistory(petTypeName)

    suspend fun recordFavorHistory(entry: FavorHistoryEntity) {
        dao.insertFavorHistory(entry)
    }
}
