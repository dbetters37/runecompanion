package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_state")
data class PetEntity(
    @PrimaryKey val id: Int = 1,
    val petTypeName: String,
    val customName: String,
    val hunger: Int,
    val happiness: Int,
    val energy: Int,
    val health: Int,
    val coinsGp: Long,
    val currentEmoteName: String,
    val currentQuote: String,
    val totalStepsTracked: Long = 0L,
    val questPoints: Int = 0,
    val completedQuestIdsCsv: String = "",
    val currentOutfitId: String = "default",
    val unlockedOutfitIdsCsv: String = "default,barrows_dharok,pokemon_pikachu,skilling_graceful"
)

@Entity(tableName = "skill_xp")
data class SkillXpEntity(
    @PrimaryKey val skillName: String,
    val xp: Long
)

@Entity(tableName = "inventory_items", primaryKeys = ["petTypeName", "itemId"])
data class InventoryEntity(
    val petTypeName: String,
    val itemId: String,
    val quantity: Int
)

@Entity(tableName = "ai_quests")
data class QuestEntity(
    @PrimaryKey val questId: String,
    val title: String,
    val description: String,
    val realLifeTaskInstructions: String,
    val targetSkillName: String,
    val rewardXp: Long,
    val rewardGp: Long,
    val isCompleted: Boolean
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val skillName: String,
    val xpGained: Long,
    val coinsGained: Long,
    val timestamp: Long
)

@Entity(tableName = "equipped_items", primaryKeys = ["petTypeName", "slotName"])
data class EquippedEntity(
    val petTypeName: String,
    val slotName: String,
    val itemId: String
)

@Entity(tableName = "bank_items", primaryKeys = ["petTypeName", "itemId"])
data class BankEntity(
    val petTypeName: String,
    val itemId: String,
    val quantity: Int
)

@Entity(tableName = "pet_skill_xp")
data class PetSkillXpEntity(
    @PrimaryKey val petSkillKey: String,
    val petTypeName: String,
    val skillName: String,
    val xp: Long
)

@Entity(tableName = "individual_pet_stats")
data class IndividualPetEntity(
    @PrimaryKey val petTypeName: String,
    val customName: String,
    val hunger: Int,
    val happiness: Int,
    val energy: Int,
    val health: Int,
    val currentEmoteName: String,
    val currentQuote: String,
    val questPoints: Int = 0,
    val completedQuestIdsCsv: String = "",
    val currentOutfitId: String = "default",
    val unlockedOutfitIdsCsv: String = "default,barrows_dharok,pokemon_pikachu,skilling_graceful"
)

@Entity(tableName = "quest_progress", primaryKeys = ["petTypeName", "questId"])
data class QuestProgressEntity(
    val petTypeName: String,
    val questId: String,
    val remainingSeconds: Int,
    val totalDurationSeconds: Int,
    val isPaused: Boolean,
    val lastUpdatedTimestamp: Long
)

@Entity(tableName = "tribe_npcs")
data class NpcEntity(
    @PrimaryKey val npcId: String,
    val completedFavorsCount: Int = 0,
    val affinityXp: Long = 0L,
    val lastFavorCompletedMs: Long = 0L
)

@Entity(tableName = "favor_contracts", primaryKeys = ["petTypeName", "skillName"])
data class FavorContractEntity(
    val petTypeName: String,
    val skillName: String,
    val taskTitle: String,
    val targetQty: Int,
    val currentQty: Int = 0,
    val targetEntityId: String = "",
    val iconSymbol: String = "📜",
    val rewardXp: Long = 0L,
    val rewardGp: Long = 0L,
    val rewardItemName: String = "",
    val rewardItemId: String = "",
    val rewardFavorXp: Long = 75L,
    val npcId: String = "npc_arlg",
    val npcName: String = "Afrig",
    val npcRole: String = "Tribe Blacksmith",
    val npcEmoji: String = "⚒️",
    val npcLoreQuote: String = "",
    val favorTypeTitle: String = "",
    val guildName: String = "",
    val guildMaster: String = "",
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "npc_favor_progress", primaryKeys = ["petTypeName", "npcId"])
data class NpcFavorProgressEntity(
    val petTypeName: String,
    val npcId: String,
    val favorLevel: Int = 1,
    val favorXp: Long = 0L,
    val completedFavorsCount: Int = 0,
    val lastCompletedTimestamp: Long = 0L
)

@Entity(tableName = "favor_history_logs")
data class FavorHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petTypeName: String,
    val npcId: String,
    val npcName: String,
    val skillName: String,
    val taskTitle: String,
    val xpGained: Long,
    val gpGained: Long,
    val favorXpGained: Long,
    val timestamp: Long = System.currentTimeMillis()
)
