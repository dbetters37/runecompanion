package com.example.data.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    suspend fun getAllMemoriesSnapshot(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE isDriveSynced = 0")
    suspend fun getUnsyncedMemories(): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Query("UPDATE memories SET isDriveSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM memories")
    suspend fun clearRawMemories()

    @Query("SELECT COUNT(*) FROM memories")
    fun getMemoryCount(): Flow<Int>
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp ASC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: Long)

    @Query("DELETE FROM conversations")
    suspend fun clearHistory()
}

@Dao
interface PersonalityDao {
    @Query("SELECT * FROM personality WHERE id = 1")
    fun getPersonality(): Flow<PersonalityEntity?>

    @Query("SELECT * FROM personality WHERE id = 1")
    suspend fun getPersonalitySnapshot(): PersonalityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePersonality(personality: PersonalityEntity)
}

@Dao
interface DriveSyncLogDao {
    @Query("SELECT * FROM drive_sync_logs ORDER BY timestamp DESC LIMIT 20")
    fun getSyncLogs(): Flow<List<DriveSyncLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DriveSyncLogEntity)
}

@Dao
interface PersonalityLogDao {
    @Query("SELECT * FROM personality_logs ORDER BY timestamp DESC LIMIT 30")
    fun getPersonalityLogs(): Flow<List<PersonalityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PersonalityLogEntity)

    @Query("DELETE FROM personality_logs")
    suspend fun clearLogs()
}

@Dao
interface TelemetryDao {
    @Query("SELECT * FROM context_telemetry ORDER BY timestamp DESC LIMIT 30")
    fun getTelemetryLogs(): Flow<List<ContextTelemetryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(telemetry: ContextTelemetryEntity)

    @Query("SELECT * FROM context_telemetry ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestTelemetry(): ContextTelemetryEntity?

    @Query("DELETE FROM context_telemetry")
    suspend fun clearTelemetry()
}

@Dao
interface PetOpinionDao {
    @Query("SELECT * FROM pet_opinions ORDER BY timestamp DESC")
    fun getAllOpinions(): Flow<List<PetOpinionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpinion(opinion: PetOpinionEntity)

    @Query("DELETE FROM pet_opinions WHERE topic = :topic")
    suspend fun deleteOpinionsByTopic(topic: String)
}

@Dao
interface CondensedMemoryDao {
    @Query("SELECT * FROM condensed_memories ORDER BY timestamp DESC")
    fun getAllCondensedMemories(): Flow<List<CondensedMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCondensedMemory(condensed: CondensedMemoryEntity)

    @Query("DELETE FROM condensed_memories WHERE id = :id")
    suspend fun deleteCondensedMemory(id: Long)

    @Query("DELETE FROM condensed_memories")
    suspend fun clearCondensedMemories()

    @Query("DELETE FROM memories")
    suspend fun clearRawMemories()
}

@Dao
interface MovementBehaviorDao {
    @Query("SELECT * FROM movement_behaviors WHERE id = 1")
    fun getMovementBehavior(): Flow<MovementBehaviorEntity?>

    @Query("SELECT * FROM movement_behaviors WHERE id = 1")
    suspend fun getMovementBehaviorSnapshot(): MovementBehaviorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovementBehavior(behavior: MovementBehaviorEntity)
}

@Dao
interface PetDailyJournalDao {
    @Query("SELECT * FROM pet_daily_journals ORDER BY timestamp DESC")
    fun getAllDailyJournals(): Flow<List<PetDailyJournalEntity>>

    @Query("SELECT * FROM pet_daily_journals WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getJournalForDate(dateStr: String): PetDailyJournalEntity?

    @Query("SELECT * FROM pet_daily_journals ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentJournals(limit: Int): List<PetDailyJournalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: PetDailyJournalEntity): Long

    @Query("DELETE FROM pet_daily_journals WHERE id = :id")
    suspend fun deleteJournal(id: Long)

    @Query("DELETE FROM pet_daily_journals")
    suspend fun clearDailyJournals()
}

@Dao
interface PetDreamJournalDao {
    @Query("SELECT * FROM pet_dream_journals ORDER BY timestamp DESC")
    fun getAllDreamJournals(): Flow<List<PetDreamJournalEntity>>

    @Query("SELECT * FROM pet_dream_journals WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getDreamJournalForDate(dateStr: String): PetDreamJournalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDreamJournal(dream: PetDreamJournalEntity): Long

    @Query("DELETE FROM pet_dream_journals WHERE id = :id")
    suspend fun deleteDreamJournal(id: Long)

    @Query("DELETE FROM pet_dream_journals")
    suspend fun clearDreamJournals()
}

@Dao
interface GoogleSearchLogDao {
    @Query("SELECT * FROM google_search_logs ORDER BY timestamp DESC")
    fun getAllSearchLogs(): Flow<List<GoogleSearchLogEntity>>

    @Query("SELECT * FROM google_search_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearches(limit: Int): Flow<List<GoogleSearchLogEntity>>

    @Query("SELECT * FROM google_search_logs ORDER BY timestamp DESC")
    suspend fun getAllSearchesSnapshot(): List<GoogleSearchLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: GoogleSearchLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearches(searches: List<GoogleSearchLogEntity>)

    @Query("DELETE FROM google_search_logs WHERE id = :id")
    suspend fun deleteSearch(id: Long)

    @Query("DELETE FROM google_search_logs")
    suspend fun clearSearchLogs()

    @Query("SELECT COUNT(*) FROM google_search_logs")
    fun getSearchCount(): Flow<Int>
}

@Dao
interface PersonalityStateTrackerDao {
    @Query("SELECT * FROM personality_state_tracker WHERE id = 1")
    fun getTracker(): Flow<PersonalityStateTrackerEntity?>

    @Query("SELECT * FROM personality_state_tracker WHERE id = 1")
    suspend fun getTrackerSnapshot(): PersonalityStateTrackerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTracker(tracker: PersonalityStateTrackerEntity)
}

@Dao
interface BrainLobeStateDao {
    @Query("SELECT * FROM brain_lobe_states ORDER BY lobeId ASC")
    fun getAllLobeStates(): Flow<List<BrainLobeStateEntity>>

    @Query("SELECT * FROM brain_lobe_states WHERE lobeId = :id")
    suspend fun getLobeState(id: String): BrainLobeStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLobe(lobe: BrainLobeStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLobes(lobes: List<BrainLobeStateEntity>)

    @Query("UPDATE brain_lobe_states SET influenceWeight = :weight WHERE lobeId = :id")
    suspend fun updateLobeWeight(id: String, weight: Float)
}

@Dao
interface BrainNeuralLogDao {
    @Query("SELECT * FROM brain_neural_logs ORDER BY timestamp DESC LIMIT 40")
    fun getRecentNeuralLogs(): Flow<List<BrainNeuralLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BrainNeuralLogEntity)

    @Query("DELETE FROM brain_neural_logs")
    suspend fun clearLogs()
}

@Dao
interface SubjectiveWorldModelDao {
    @Query("SELECT * FROM subjective_world_model WHERE id = 1")
    fun getWorldModel(): Flow<SubjectiveWorldModelEntity?>

    @Query("SELECT * FROM subjective_world_model WHERE id = 1")
    suspend fun getWorldModelSnapshot(): SubjectiveWorldModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWorldModel(model: SubjectiveWorldModelEntity)
}

@Dao
interface AutonomousGoalDao {
    @Query("SELECT * FROM autonomous_goals ORDER BY lastUpdatedTimestamp DESC")
    fun getAllGoals(): Flow<List<AutonomousGoalEntity>>

    @Query("SELECT * FROM autonomous_goals WHERE status = 'ACTIVE_INVESTIGATION' OR status = 'PONDERING_BREAKTHROUGH' ORDER BY progressPercentage DESC")
    fun getActiveGoals(): Flow<List<AutonomousGoalEntity>>

    @Query("SELECT * FROM autonomous_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): AutonomousGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: AutonomousGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<AutonomousGoalEntity>)

    @Update
    suspend fun updateGoal(goal: AutonomousGoalEntity)

    @Query("DELETE FROM autonomous_goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)

    @Query("DELETE FROM autonomous_goals")
    suspend fun clearGoals()
}

@Dao
interface PersistentMemoryLoopDao {
    @Query("SELECT * FROM persistent_memory_loops ORDER BY timestamp DESC LIMIT 40")
    fun getMemoryLoops(): Flow<List<PersistentMemoryLoopEntity>>

    @Query("SELECT * FROM persistent_memory_loops ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMemoryLoop(): PersistentMemoryLoopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoop(loop: PersistentMemoryLoopEntity): Long

    @Query("DELETE FROM persistent_memory_loops WHERE id = :id")
    suspend fun deleteLoop(id: Long)

    @Query("DELETE FROM persistent_memory_loops")
    suspend fun clearLoops()
}

@Database(
    entities = [
        MemoryEntity::class,
        ConversationEntity::class,
        PersonalityEntity::class,
        PersonalityLogEntity::class,
        DriveSyncLogEntity::class,
        ContextTelemetryEntity::class,
        PetOpinionEntity::class,
        CondensedMemoryEntity::class,
        MovementBehaviorEntity::class,
        PetDailyJournalEntity::class,
        PetDreamJournalEntity::class,
        GoogleSearchLogEntity::class,
        PersonalityStateTrackerEntity::class,
        BrainLobeStateEntity::class,
        BrainNeuralLogEntity::class,
        SubjectiveWorldModelEntity::class,
        AutonomousGoalEntity::class,
        PersistentMemoryLoopEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun conversationDao(): ConversationDao
    abstract fun personalityDao(): PersonalityDao
    abstract fun personalityLogDao(): PersonalityLogDao
    abstract fun driveSyncLogDao(): DriveSyncLogDao
    abstract fun telemetryDao(): TelemetryDao
    abstract fun petOpinionDao(): PetOpinionDao
    abstract fun condensedMemoryDao(): CondensedMemoryDao
    abstract fun movementBehaviorDao(): MovementBehaviorDao
    abstract fun petDailyJournalDao(): PetDailyJournalDao
    abstract fun petDreamJournalDao(): PetDreamJournalDao
    abstract fun googleSearchLogDao(): GoogleSearchLogDao
    abstract fun personalityStateTrackerDao(): PersonalityStateTrackerDao
    abstract fun brainLobeStateDao(): BrainLobeStateDao
    abstract fun brainNeuralLogDao(): BrainNeuralLogDao
    abstract fun subjectiveWorldModelDao(): SubjectiveWorldModelDao
    abstract fun autonomousGoalDao(): AutonomousGoalDao
    abstract fun persistentMemoryLoopDao(): PersistentMemoryLoopDao


    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shaman_pet_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
