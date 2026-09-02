package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.repository.PetRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.*

class AfkCompanionBackgroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var trackingJob: Job? = null
    private lateinit var pohPrefs: SharedPreferences
    private lateinit var repository: PetRepository

    companion object {
        const val ACTION_START = "ACTION_START_AFK_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_AFK_TRACKING"
        private const val CHANNEL_ID = "afk_companion_service_channel"
        private const val CHANNEL_NAME = "Companion AFK Service"
        private const val NOTIFICATION_ID = 9991

        fun startService(context: Context) {
            try {
                val intent = Intent(context, AfkCompanionBackgroundService::class.java).apply {
                    action = ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, AfkCompanionBackgroundService::class.java).apply {
                    action = ACTION_STOP
                }
                context.stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        pohPrefs = getSharedPreferences("osrs_poh_house_prefs", Context.MODE_PRIVATE)
        val database = AppDatabase.getDatabase(applicationContext)
        repository = PetRepository(database.petDao(), applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, null -> {
                startForeground(NOTIFICATION_ID, createOngoingNotification("Companion Active", "Tracking AFK training & hunger..."))
                startTracking()
            }
        }
        return START_STICKY
    }

    private fun startTracking() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            while (isActive) {
                try {
                    // Check pet hunger from db
                    val activePetTypeName = pohPrefs.getString("active_pet_type", "SHAMANIC_WOLF") ?: "SHAMANIC_WOLF"
                    val pet = repository.getPetStateDirect()
                    
                    if (pet != null) {
                        val activityName = pohPrefs.getString("afk_active_activity_name", "AFK Training") ?: "AFK Training"
                        val notification = createOngoingNotification(
                            title = "🐾 Pet Training: $activityName",
                            text = "Pet Hunger: ${pet.hunger}% • Energy: Active"
                        )
                        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                        nm?.notify(NOTIFICATION_ID, notification)

                        // If pet has no health, notify and finish
                        if (pet.health <= 0) {
                            NotificationHelper.sendAfkNotification(
                                applicationContext,
                                "💔 AFK Stopped: Pet Has No Health",
                                "${pet.customName} has fainted (0 HP)! Open the app to heal and feed your companion."
                            )
                            break
                        }

                        // If pet is completely out of hunger, notify and finish
                        if (pet.hunger <= 0) {
                            val items = repository.getInventoryItemsDirect(activePetTypeName)
                            val hasFood = items.any { it.isCookedReadyToEatFood && it.quantity > 0 }
                            if (!hasFood) {
                                NotificationHelper.sendAfkNotification(
                                    applicationContext,
                                    "🍗 AFK Stopped: Hunger Depleted",
                                    "Your companion ran out of food & energy (0% Hunger). Open the app to feed your pet!"
                                )
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(15000L) // Check every 15 seconds
            }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows companion active status while app is closed or in background"
                enableVibration(false)
                setSound(null, null)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    private fun createOngoingNotification(title: String, text: String): android.app.Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_shaman)
            .setColor(0xFFFFB703.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
