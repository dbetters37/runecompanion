package com.example.data.telemetry

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import com.example.data.db.ContextTelemetryEntity
import java.util.Calendar

class PhoneTelemetryCollector(private val context: Context) {

    private val sensorManager = EnvironmentSensorManager(context).apply {
        registerSensors()
    }

    fun collectCurrentTelemetry(): ContextTelemetryEntity {
        // 1. Battery & Charging status
        var batteryLevel = 100
        var isCharging = false
        try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            if (batteryStatus != null) {
                val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryLevel = ((level / scale.toFloat()) * 100).toInt()
                }

                val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
            }
        } catch (e: Exception) {
            // Fallback default
        }

        // 2. Time of day category & Perception of Time
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeOfDay = when (hour) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..21 -> "Evening"
            else -> "Late Night"
        }
        val perceptionOfTime = sensorManager.getPerceptionOfTime()

        // 3. Network Connection & Location Context
        var networkType = "Offline"
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork
            val capabilities = cm?.getNetworkCapabilities(activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> networkType = "WiFi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> networkType = "Cellular"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> networkType = "Ethernet"
                    else -> networkType = "Connected"
                }
            }
        } catch (e: Exception) {
            networkType = "Active"
        }
        val locationContext = sensorManager.getLocationContext(networkType)

        // 4. Sensors (Light Lux & Motion)
        val lux = sensorManager.currentLux
        val lightCategory = sensorManager.lightCategory
        val motionState = sensorManager.motionState

        // 5. Ambient Summary
        val chargingStr = if (isCharging) "plugged in & charging" else "on battery ($batteryLevel%)"
        val summary = "$timeOfDay • $lightCategory • $locationContext • $chargingStr"

        return ContextTelemetryEntity(
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            timeOfDayCategory = timeOfDay,
            networkType = networkType,
            screenActivityState = "Active Interaction",
            ambientContextSummary = summary,
            ambientLightLux = lux,
            lightLevelCategory = lightCategory,
            motionState = motionState,
            locationContext = locationContext,
            perceptionOfTime = perceptionOfTime
        )
    }
}
