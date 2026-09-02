package com.example.data.api

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class WeatherData(
    val temperatureF: Int,
    val condition: String,
    val weatherCode: Int,
    val isRaining: Boolean,
    val isSnowing: Boolean,
    val isSunny: Boolean,
    val petAdvice: String
)

class WeatherApiService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("osrs_weather_cache", Context.MODE_PRIVATE)

    suspend fun fetchCurrentWeather(): WeatherData = withContext(Dispatchers.IO) {
        try {
            // Step 1: Detect latitude and longitude via IP Geolocation (no permission needed)
            var lat = 37.7749
            var lon = -122.4194

            try {
                val geoRequest = Request.Builder()
                    .url("https://ip-api.com/json/?fields=lat,lon,city,status")
                    .build()
                val geoResponse = client.newCall(geoRequest).execute()
                val geoBody = geoResponse.body?.string()
                if (geoResponse.isSuccessful && !geoBody.isNullOrBlank()) {
                    val geoJson = JSONObject(geoBody)
                    if (geoJson.optString("status") == "success") {
                        lat = geoJson.optDouble("lat", lat)
                        lon = geoJson.optDouble("lon", lon)
                    }
                }
            } catch (e: Exception) {
                // Fallback to default coordinates
            }

            // Step 2: Query Open-Meteo Free Weather API
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true&temperature_unit=fahrenheit"
            val weatherRequest = Request.Builder().url(weatherUrl).build()
            val weatherResponse = client.newCall(weatherRequest).execute()
            val weatherBody = weatherResponse.body?.string()

            if (weatherResponse.isSuccessful && !weatherBody.isNullOrBlank()) {
                val json = JSONObject(weatherBody)
                val currentWeather = json.optJSONObject("current_weather")
                if (currentWeather != null) {
                    val temp = currentWeather.optDouble("temperature", 72.0).toInt()
                    val code = currentWeather.optInt("weathercode", 0)

                    val parsed = parseWeatherCode(temp, code)
                    saveWeatherCache(parsed)
                    return@withContext parsed
                }
            }
        } catch (e: Exception) {
            // Ignore error and fallback to cache or defaults
        }

        return@withContext getCachedWeather()
    }

    private fun parseWeatherCode(tempF: Int, code: Int): WeatherData {
        val (condition, isRaining, isSnowing, isSunny, advice) = when (code) {
            0 -> Tuple5("Clear & Sunny ☀️", false, false, true, "Great day for a walk! Stock up on stamina potions!")
            1, 2 -> Tuple5("Partly Cloudy ⛅", false, false, true, "Nice pleasant weather! Perfect time for a Farm Run!")
            3 -> Tuple5("Overcast ☁️", false, false, false, "Cloudy skies overhead. Keep your eyes open for wild implings!")
            45, 48 -> Tuple5("Foggy 🌫️", false, false, false, "Thick fog outside! Mind your step near Lumbridge Swamp!")
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> Tuple5(
                "Raining 🌧️",
                true,
                false,
                false,
                "It's raining outside, stay cozy inside with your pet!"
            )
            71, 73, 75, 77, 85, 86 -> Tuple5(
                "Snowing ❄️",
                false,
                true,
                false,
                "Brrr! It's snowing outside! Put on your Warm Woolly Hat!"
            )
            95, 96, 99 -> Tuple5(
                "Thunderstorm ⛈️",
                true,
                false,
                false,
                "Thunderstorms raging! Ancient magicks at work! Stay safe indoors!"
            )
            else -> Tuple5("Mild 🌤️", false, false, true, "Pleasant weather outside! Stay active and level up!")
        }

        return WeatherData(
            temperatureF = tempF,
            condition = "$tempF°F $condition",
            weatherCode = code,
            isRaining = isRaining,
            isSnowing = isSnowing,
            isSunny = isSunny,
            petAdvice = advice
        )
    }

    private fun saveWeatherCache(weather: WeatherData) {
        prefs.edit()
            .putInt("temp", weather.temperatureF)
            .putString("condition", weather.condition)
            .putInt("code", weather.weatherCode)
            .putBoolean("isRaining", weather.isRaining)
            .putBoolean("isSnowing", weather.isSnowing)
            .putBoolean("isSunny", weather.isSunny)
            .putString("advice", weather.petAdvice)
            .apply()
    }

    fun getCachedWeather(): WeatherData {
        val temp = prefs.getInt("temp", 74)
        val condition = prefs.getString("condition", "74°F Clear & Sunny ☀️") ?: "74°F Clear & Sunny ☀️"
        val code = prefs.getInt("code", 0)
        val isRaining = prefs.getBoolean("isRaining", false)
        val isSnowing = prefs.getBoolean("isSnowing", false)
        val isSunny = prefs.getBoolean("isSunny", true)
        val advice = prefs.getString("advice", "Great day for a walk! Stay active and level up!") ?: "Great day for a walk!"

        return WeatherData(
            temperatureF = temp,
            condition = condition,
            weatherCode = code,
            isRaining = isRaining,
            isSnowing = isSnowing,
            isSunny = isSunny,
            petAdvice = advice
        )
    }

    private data class Tuple5<A, B, C, D, E>(
        val a: A, val b: B, val c: C, val d: D, val e: E
    )
}
