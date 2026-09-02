package com.example.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.PetRepository
import com.example.data.ai.GeminiPetService
import com.example.data.db.AppDatabase
import com.example.data.drive.GoogleDriveSyncManager
import java.util.concurrent.TimeUnit

class GoogleDriveSyncWorker(
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


            // Ensure dedicated folder exists in authenticated user's Drive
            driveSyncManager.ensureDedicatedFolderExists()

            // Perform full memory log & chat history background sync to Google Drive
            val syncResult = repository.manualSyncToDrive()

            if (syncResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

object GoogleDriveSyncScheduler {
    private const val PERIODIC_WORK_NAME = "GoogleDriveBackgroundSyncWork"

    fun schedulePeriodicDriveSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<GoogleDriveSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    fun triggerImmediateDriveSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWork = OneTimeWorkRequestBuilder<GoogleDriveSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeWork)
    }
}
