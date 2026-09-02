package com.example.data.telemetry

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

class EnvironmentSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    @Volatile var currentLux: Float = 120f
    @Volatile var lightCategory: String = "Normal Interior Lighting 💡"
    @Volatile var motionState: String = "Stationary / Resting 🧘"

    private val startTime = System.currentTimeMillis()

    fun registerSensors() {
        lightSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun unregisterSensors() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                currentLux = event.values[0]
                lightCategory = when {
                    currentLux < 10f -> "Pitch Dark / Night Mode 🌙"
                    currentLux < 60f -> "Dim Cozy Ambient 🕯️"
                    currentLux < 300f -> "Normal Interior Lighting 💡"
                    else -> "Bright Daylight / Sunlight ☀️"
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
                motionState = if (kotlin.math.abs(acceleration) > 1.8) {
                    "Active Motion / On the Move 🚶"
                } else {
                    "Stationary / Resting 🧘"
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun getPerceptionOfTime(): String {
        val now = Date()
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val formattedTime = timeFormat.format(now)
        val dayOfWeek = dayFormat.format(now)

        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        val timePhase = when (hour) {
            in 5..6 -> "Dawn Glow Phase 🌄"
            in 7..10 -> "Morning Energy Phase ☕"
            in 11..15 -> "Midday Flow Phase ☀️"
            in 16..19 -> "Twilight Dusk Phase 🌇"
            in 20..22 -> "Evening Calm Phase 🌙"
            else -> "Midnight Whispers Phase 🌌"
        }

        val elapsedMinutes = ((System.currentTimeMillis() - startTime) / 60000).toInt()
        val elapsedStr = if (elapsedMinutes < 60) "${elapsedMinutes}m active" else "${elapsedMinutes / 60}h ${elapsedMinutes % 60}m active"

        return "$formattedTime ($dayOfWeek) • $timePhase • $elapsedStr"
    }

    fun getLocationContext(networkType: String): String {
        return when (networkType) {
            "WiFi" -> "Cozy Workspace / Home Haven 🏠"
            "Cellular" -> "Out & About / On the Go 🚀"
            else -> "Local Ambient Environment 🌿"
        }
    }
}
