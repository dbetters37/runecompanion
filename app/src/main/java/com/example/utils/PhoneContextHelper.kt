package com.example.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PhoneContextInfo(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val timeOfDay: String,
    val currentWeather: String,
    val activeAppOrScreen: String,
    val networkType: String
)

object PhoneContextHelper {

    fun getPhoneContext(context: Context): PhoneContextInfo {
        // 1. Battery Info
        val batteryStatus: Intent? = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED), Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            }
        } catch (e: Exception) {
            null
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 88

        // 2. Time of day (12-hour format)
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        val currentTimeStr = sdf.format(Date())
        val hour = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toIntOrNull() ?: 14
        val timeOfDay = when (hour) {
            in 5..11 -> "Morning ($currentTimeStr)"
            in 12..16 -> "Afternoon ($currentTimeStr)"
            in 17..21 -> "Evening ($currentTimeStr)"
            else -> "Late Night ($currentTimeStr)"
        }

        // 3. Real Weather Context from WeatherApiService
        val weatherService = com.example.data.api.WeatherApiService(context)
        val cachedWeather = weatherService.getCachedWeather()
        val weather = cachedWeather.condition

        // 4. Screen / Active App Context
        val prefs = context.getSharedPreferences("osrs_master_control_panel_prefs", Context.MODE_PRIVATE)
        val rawApp = prefs.getString("last_active_app", "Android Home Screen") ?: "Android Home Screen"
        val formattedApp = formatAppName(rawApp)

        return PhoneContextInfo(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            timeOfDay = timeOfDay,
            currentWeather = weather,
            activeAppOrScreen = formattedApp,
            networkType = "WiFi Connected (High Speed)"
        )
    }

    fun updateLastActiveApp(context: Context, appNameOrPkg: String) {
        val cleanName = formatAppName(appNameOrPkg)
        val prefs = context.getSharedPreferences("osrs_master_control_panel_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("last_active_app", cleanName).apply()
    }

    fun formatAppName(raw: String): String {
        val lower = raw.lowercase().trim()
        return when {
            lower.contains("duolingo") -> "Duolingo"
            lower.contains("youtube") -> "YouTube"
            lower.contains("netflix") -> "Netflix"
            lower.contains("hulu") -> "Hulu"
            lower.contains("peacock") -> "Peacock"
            lower.contains("spotify") -> "Spotify"
            lower.contains("audible") -> "Audible"
            lower.contains("strava") || lower.contains("pedometer") || lower.contains("fit") || lower.contains("health") -> "Strava / Fitness Tracker"
            lower.contains("googlequicksearchbox") || lower.contains("quicksearchbox") -> "Google Search Widget"
            lower.contains("lens") || lower.contains("smartlens") -> "Google Smart Lens"
            lower.contains("whatsapp") -> "WhatsApp"
            lower.contains("chrome") -> "Google Chrome"
            lower.contains("babbel") -> "Babbel"
            lower.contains("memrise") -> "Memrise"
            lower.contains("kindle") -> "Kindle Reader"
            lower.contains("instagram") -> "Instagram"
            lower.contains("twitter") || lower.contains("x.android") -> "X (Twitter)"
            lower.contains("reddit") -> "Reddit"
            lower.contains("tiktok") -> "TikTok"
            lower.contains("discord") -> "Discord"
            lower.contains("slack") -> "Slack"
            lower.contains("twitch") -> "Twitch"
            raw.isNotBlank() && !raw.startsWith("com.") -> raw
            raw.contains(".") -> raw.substringAfterLast(".").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            else -> raw.ifBlank { "Android Home Screen" }
        }
    }

    suspend fun refreshWeather(context: Context): PhoneContextInfo {
        val weatherService = com.example.data.api.WeatherApiService(context)
        weatherService.fetchCurrentWeather()
        return getPhoneContext(context)
    }
}
