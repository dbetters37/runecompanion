package com.example.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.data.PetRepository
import com.example.data.ai.GeminiPetService
import com.example.data.db.AppDatabase
import com.example.data.drive.GoogleDriveSyncManager
import com.example.data.telemetry.PhoneTelemetryCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EnvironmentPerceptionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var telemetryCollector: PhoneTelemetryCollector
    private lateinit var repository: PetRepository

    private var lastPerceptionTimestamp = 0L

    private val environmentBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.action?.let { action ->
                when (action) {
                    Intent.ACTION_POWER_CONNECTED,
                    Intent.ACTION_POWER_DISCONNECTED,
                    Intent.ACTION_SCREEN_ON,
                    Intent.ACTION_TIME_TICK -> {
                        serviceScope.launch {
                            try {
                                repository.checkAndTriggerMorningGreetingIfNeeded(applicationContext)
                                repository.ensureDailyJournalsFilledForToday()
                            } catch (e: Exception) {
                                // Ignore
                            }

                            // Keep overlay alive if user enabled it
                            try {
                                val prefs = applicationContext.getSharedPreferences(
                                    PetOverlayService.PREFS_NAME,
                                    Context.MODE_PRIVATE
                                )
                                val isOverlayEnabled = prefs.getBoolean(PetOverlayService.KEY_OVERLAY_ENABLED, false)
                                if (isOverlayEnabled &&
                                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || android.provider.Settings.canDrawOverlays(applicationContext)) &&
                                    !PetOverlayService.isOverlayRunning
                                ) {
                                    PetOverlayService.startOverlay(applicationContext)
                                }
                            } catch (_: Exception) { }
                        }
                        evaluateEnvironmentAndPipeToLLM()
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        telemetryCollector = PhoneTelemetryCollector(applicationContext)

        val db = AppDatabase.getDatabase(applicationContext)
        val driveSyncManager = GoogleDriveSyncManager(applicationContext, db.driveSyncLogDao())
        val geminiService = GeminiPetService()
        repository = PetRepository(
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


        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_TIME_TICK)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(environmentBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(environmentBroadcastReceiver, filter)
            }
        } catch (e: Exception) {
            // Log receiver registration failure silently
        }

        startForegroundNotification()

        // Start perception loop
        startPerceptionLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        return START_STICKY
    }

    private fun startForegroundNotification() {
        try {
            val channelId = "pet_environment_channel"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Shaman Pet Autonomous Perception",
                    NotificationManager.IMPORTANCE_LOW
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Shaman Pet Perception Service")
                .setContentText("Monitoring light, motion, time perception, and ambient context...")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setOngoing(true)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1001, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(1001, notification)
            }
        } catch (e: Exception) {
            // Ignore notification failure
        }
    }

    private fun startPerceptionLoop() {
        // Environment perception LLM commentary disabled per user request
    }

    private fun evaluateEnvironmentAndPipeToLLM() {
        // Environment perception LLM commentary disabled per user request
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(environmentBroadcastReceiver)
        } catch (e: Exception) {
            // Ignore
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun startService(context: Context) {
            try {
                val intent = Intent(context, EnvironmentPerceptionService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Ignore start failure
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, EnvironmentPerceptionService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                // Ignore stop failure
            }
        }
    }
}
