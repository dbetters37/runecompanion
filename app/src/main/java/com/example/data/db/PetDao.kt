package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    @Query("SELECT * FROM pet_state WHERE id = 1")
    fun getPetState(): Flow<PetEntity?>

    @Query("SELECT * FROM pet_state WHERE id = 1")
    suspend fun getPetStateDirect(): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePetState(pet: PetEntity)

    @Query("SELECT * FROM skill_xp")
    fun getAllSkillXp(): Flow<List<SkillXpEntity>>

    @Query("SELECT * FROM skill_xp")
    suspend fun getAllSkillXpDirect(): List<SkillXpEntity>

    @Query("SELECT xp FROM skill_xp WHERE skillName = :skillName")
    suspend fun getSkillXpDirect(skillName: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSkillXp(skillXp: SkillXpEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllSkillXp(list: List<SkillXpEntity>)

    @Query("SELECT * FROM inventory_items WHERE petTypeName = :petTypeName")
    fun getInventoryItems(petTypeName: String): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory_items WHERE petTypeName = :petTypeName")
    suspend fun getInventoryItemsDirect(petTypeName: String): List<InventoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInventoryItem(item: InventoryEntity)

    @Query("DELETE FROM inventory_items WHERE petTypeName = :petTypeName AND itemId = :itemId")
    suspend fun deleteInventoryItem(petTypeName: String, itemId: String)

    @Query("SELECT * FROM ai_quests")
    fun getQuests(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM ai_quests")
    suspend fun getQuestsDirect(): List<QuestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuest(quest: QuestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllQuests(quests: List<QuestEntity>)

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 50")
    fun getActivityLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLogEntity)

    @Query("SELECT * FROM equipped_items WHERE petTypeName = :petTypeName")
    fun getEquippedItems(petTypeName: String): Flow<List<EquippedEntity>>

    @Query("SELECT * FROM equipped_items WHERE petTypeName = :petTypeName")
    suspend fun getEquippedItemsDirect(petTypeName: String): List<EquippedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEquippedItem(equipped: EquippedEntity)

    @Query("DELETE FROM equipped_items WHERE petTypeName = :petTypeName AND slotName = :slotName")
    suspend fun deleteEquippedItem(petTypeName: String, slotName: String)

    @Query("SELECT * FROM bank_items WHERE petTypeName = :petTypeName")
    fun getBankItems(petTypeName: String): Flow<List<BankEntity>>

    @Query("SELECT * FROM bank_items WHERE petTypeName = :petTypeName")
    suspend fun getBankItemsDirect(petTypeName: String): List<BankEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBankItem(item: BankEntity)

    @Query("DELETE FROM bank_items WHERE petTypeName = :petTypeName AND itemId = :itemId")
    suspend fun deleteBankItem(petTypeName: String, itemId: String)

    @Query("SELECT * FROM pet_skill_xp WHERE petTypeName = :petTypeName")
    fun getPetSkillXp(petTypeName: String): Flow<List<PetSkillXpEntity>>

    @Query("SELECT * FROM pet_skill_xp WHERE petTypeName = :petTypeName")
    suspend fun getPetSkillXpDirect(petTypeName: String): List<PetSkillXpEntity>

    @Query("SELECT * FROM pet_skill_xp")
    suspend fun getAllPetSkillXpDirect(): List<PetSkillXpEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePetSkillXp(item: PetSkillXpEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllPetSkillXp(items: List<PetSkillXpEntity>)

    @Query("DELETE FROM pet_skill_xp WHERE petTypeName = :petTypeName")
    suspend fun resetPetSkillXp(petTypeName: String)

    @Query("SELECT * FROM individual_pet_stats WHERE petTypeName = :petTypeName")
    suspend fun getIndividualPetStatsDirect(petTypeName: String): IndividualPetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveIndividualPetStats(stats: IndividualPetEntity)

    @Query("SELECT * FROM quest_progress WHERE petTypeName = :petTypeName")
    fun getAllQuestProgress(petTypeName: String): Flow<List<QuestProgressEntity>>

    @Query("SELECT * FROM quest_progress WHERE petTypeName = :petTypeName")
    suspend fun getAllQuestProgressDirect(petTypeName: String): List<QuestProgressEntity>

    @Query("SELECT * FROM quest_progress WHERE petTypeName = :petTypeName AND questId = :questId")
    suspend fun getQuestProgressDirect(petTypeName: String, questId: String): QuestProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuestProgress(progress: QuestProgressEntity)

    @Query("DELETE FROM quest_progress WHERE petTypeName = :petTypeName AND questId = :questId")
    suspend fun deleteQuestProgress(petTypeName: String, questId: String)

    @Query("DELETE FROM quest_progress WHERE petTypeName = :petTypeName")
    suspend fun deleteAllQuestProgress(petTypeName: String)

    @Query("SELECT * FROM tribe_npcs")
    fun getAllTribeNpcs(): Flow<List<NpcEntity>>

    @Query("SELECT * FROM tribe_npcs")
    suspend fun getAllTribeNpcsDirect(): List<NpcEntity>

    @Query("SELECT * FROM tribe_npcs WHERE npcId = :npcId")
    suspend fun getNpcDirect(npcId: String): NpcEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNpc(npc: NpcEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllNpcs(npcs: List<NpcEntity>)

    // ================= FAVOR ENGINE DATABASE DAOS =================
    @Query("SELECT * FROM favor_contracts WHERE petTypeName = :petTypeName")
    fun getFavorContracts(petTypeName: String): Flow<List<FavorContractEntity>>

    @Query("SELECT * FROM favor_contracts WHERE petTypeName = :petTypeName")
    suspend fun getFavorContractsDirect(petTypeName: String): List<FavorContractEntity>

    @Query("SELECT * FROM favor_contracts WHERE petTypeName = :petTypeName AND skillName = :skillName")
    suspend fun getFavorContractDirect(petTypeName: String, skillName: String): FavorContractEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavorContract(contract: FavorContractEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllFavorContracts(contracts: List<FavorContractEntity>)

    @Query("DELETE FROM favor_contracts WHERE petTypeName = :petTypeName AND skillName = :skillName")
    suspend fun deleteFavorContract(petTypeName: String, skillName: String)

    @Query("DELETE FROM favor_contracts WHERE petTypeName = :petTypeName")
    suspend fun deleteAllFavorContracts(petTypeName: String)

    @Query("SELECT * FROM npc_favor_progress WHERE petTypeName = :petTypeName")
    fun getAllNpcFavorProgress(petTypeName: String): Flow<List<NpcFavorProgressEntity>>

    @Query("SELECT * FROM npc_favor_progress WHERE petTypeName = :petTypeName")
    suspend fun getAllNpcFavorProgressDirect(petTypeName: String): List<NpcFavorProgressEntity>

    @Query("SELECT * FROM npc_favor_progress WHERE petTypeName = :petTypeName AND npcId = :npcId")
    suspend fun getNpcFavorProgressDirect(petTypeName: String, npcId: String): NpcFavorProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNpcFavorProgress(entity: NpcFavorProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllNpcFavorProgress(entities: List<NpcFavorProgressEntity>)

    @Query("SELECT * FROM favor_history_logs WHERE petTypeName = :petTypeName ORDER BY timestamp DESC LIMIT 50")
    fun getFavorHistory(petTypeName: String): Flow<List<FavorHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorHistory(entry: FavorHistoryEntity)
}
