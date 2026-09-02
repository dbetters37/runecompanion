package com.example.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object MemorySummarizerScheduler {

    private const val WORK_NAME = "DailyMemorySummarizerWork"

    fun enqueueDailyMemorySummaryWork(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<MemorySummarizerWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    fun triggerImmediateMemorySummaryWork(context: Context) {
        val oneTimeWork = OneTimeWorkRequestBuilder<MemorySummarizerWorker>()
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeWork)
    }
}
