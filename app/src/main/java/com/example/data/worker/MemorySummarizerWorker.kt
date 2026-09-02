package com.example.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.PetRepository
import com.example.data.ai.GeminiPetService
import com.example.data.db.AppDatabase
import com.example.data.drive.GoogleDriveSyncManager

class MemorySummarizerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val driveSyncManager = GoogleDriveSyncManager(applicationContext, db.driveSyncLogDao())
            val geminiService = GeminiPetService()
            val repository = PetRepository(
                conversationDao = db.conversationDao(),
                memoryDao = db.memoryDao(),
                personalityDao = db.personalityDao(),
                personalityLogDao = db.personalityLogDao(),
                driveSyncLogDao = db.driveSyncLogDao(),
                telemetryDao = db.telemetryDao(),
                petOpinionDao = db.petOpinionDao(),
                condensedMemoryDao = db.condensedMemoryDao(),
                movementBehaviorDao = db.movementBehaviorDao(),
                petDailyJournalDao = db.petDailyJournalDao(),
                petDreamJournalDao = db.petDreamJournalDao(),
                googleSearchLogDao = db.googleSearchLogDao(),
                personalityStateTrackerDao = db.personalityStateTrackerDao(),
                brainLobeStateDao = db.brainLobeStateDao(),
                brainNeuralLogDao = db.brainNeuralLogDao(),
                subjectiveWorldModelDao = db.subjectiveWorldModelDao(),
                autonomousGoalDao = db.autonomousGoalDao(),
                persistentMemoryLoopDao = db.persistentMemoryLoopDao(),
                driveSyncManager = driveSyncManager,
                geminiPetService = geminiService
            )


            // 1. Ensure daily AI journal and dream journal are filled for today
            repository.ensureDailyJournalsFilledForToday()

            // 2. Condense & summarize raw memories into compact themes
            repository.condenseAndConsolidateMemories()

            // 3. Refresh pet's subjective opinions and context
            repository.refreshPetOpinionsAndThoughts()

            // 4. Perform Drive sync to keep Google Drive file compact and optimized
            repository.manualSyncToDrive()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
