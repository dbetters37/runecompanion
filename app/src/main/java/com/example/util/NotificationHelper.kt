package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "afk_activity_channel"
    private const val CHANNEL_NAME = "AFK Activity Alerts"

    private const val ONGOING_CHANNEL_ID = "afk_ongoing_status_channel"
    private const val ONGOING_CHANNEL_NAME = "AFK Status Bar Indicator"

    const val ONGOING_NOTIFICATION_ID = 8888

    fun sendAfkNotification(context: Context, title: String, message: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications sent when AFK activities complete or stop"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(com.example.R.drawable.ic_notification_shaman)
                .setColor(0xFFFFB703.toInt())
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendOfflineGainsSummaryNotification(
        context: Context,
        report: com.example.data.models.OfflineGainsReport
    ) {
        val skillName = report.skill?.displayName ?: "Adventure"
        val lootSummary = if (report.itemsGained.isNotEmpty()) {
            report.itemsGained.joinToString(", ") { "${it.quantity}x ${it.name}" }
        } else {
            "None"
        }

        val bigText = buildString {
            appendLine("⏱️ Away Duration: ${report.formattedDuration} (${report.actionsCompleted} actions)")
            if (report.xpGained > 0) {
                appendLine("✨ XP Gained: +${"%,d".format(report.xpGained)} $skillName XP")
            }
            if (report.gpGained > 0) {
                appendLine("🪙 Coins Gained: +${"%,d".format(report.gpGained)} GP")
            }
            appendLine("🎒 Loot Acquired: $lootSummary")
            if (report.extraBonusMaterialsGained.isNotEmpty()) {
                val bonusSummary = report.extraBonusMaterialsGained.joinToString(", ") { "+${it.quantity} ${it.name}" }
                appendLine("✨ NPC Favor Perks: $bonusSummary")
            }
            val maxH = if (report.maxHunger > 0) report.maxHunger else 100
            val startPct = if (maxH > 0) ((report.hungerStart.toFloat() / maxH.toFloat()) * 100).toInt() else report.hungerStart
            val remPct = if (maxH > 0) ((report.hungerRemaining.toFloat() / maxH.toFloat()) * 100).toInt() else report.hungerRemaining
            val usedPct = if (maxH > 0) ((report.hungerUsed.toFloat() / maxH.toFloat()) * 100).toInt() else report.hungerUsed
            val hungerLine = if (maxH != 100) {
                "🍖 Hunger: ${report.hungerStart}/$maxH ($startPct%) ➔ ${report.hungerRemaining}/$maxH ($remPct%) (-${report.hungerUsed})"
            } else {
                "🍖 Hunger: $startPct% ➔ $remPct% (-${report.hungerUsed}%)"
            }
            appendLine(hungerLine)
            if (report.golemGains != null) {
                val golem = report.golemGains
                appendLine("${golem.golemEmoji} ${golem.golemName}: +${"%,d".format(golem.xpGained)} XP, +${"%,d".format(golem.gpGained)} GP (${golem.actionsCompleted} ticks)")
            }
            if (!report.stoppedReason.isNullOrBlank()) {
                appendLine("⚠️ Status: ${report.stoppedReason}")
            }
        }.trimEnd()

        val title = "${report.activityEmoji} AFK Gains: ${report.activityName}"
        sendAfkNotification(context, title, bigText)
    }

    fun updateOngoingAfkNotification(
        context: Context,
        title: String,
        subText: String,
        details: String,
        progressCurrent: Int = 0,
        progressMax: Int = 100,
        indeterminate: Boolean = false
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    ONGOING_CHANNEL_ID,
                    ONGOING_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Ongoing taskbar notification showing current AFK activity and progress"
                    enableVibration(false)
                    setSound(null, null)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, ONGOING_CHANNEL_ID)
                .setSmallIcon(com.example.R.drawable.ic_notification_shaman)
                .setColor(0xFFFFB703.toInt())
                .setSubText("RuneCompanion AFK")
                .setContentTitle(title)
                .setContentText(subText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(details))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)

            if (progressMax > 0 || indeterminate) {
                builder.setProgress(progressMax, progressCurrent, indeterminate)
            }

            notificationManager.notify(ONGOING_NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearOngoingAfkNotification(context: Context) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            notificationManager.cancel(ONGOING_NOTIFICATION_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
