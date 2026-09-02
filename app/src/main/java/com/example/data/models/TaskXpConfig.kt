package com.example.data.models

import android.content.Context
import android.content.SharedPreferences

data class TaskXpConfig(
    val taskId: String,
    val taskName: String,
    val defaultSkill: OsrsSkill,
    val defaultXp: Long,
    val defaultGp: Long,
    val iconEmoji: String,
    val description: String,
    val associatedPackages: List<String> = emptyList(),
    val defaultXpPerMin: Long = 100L
) {
    companion object {
        val ALL_DEFAULT_TASKS = listOf(
            TaskXpConfig("duolingo", "🦉 Duolingo & Language Practice", OsrsSkill.MAGIC, 600L, 150L, "🦉", "Duolingo, Babbel, Memrise language lessons", listOf("com.duolingo", "duolingo", "babbel", "memrise", "busuu", "rosetta", "lingodeeer"), 120L),
            TaskXpConfig("streaming", "📺 Streaming Video (YouTube/Netflix/Duolingo)", OsrsSkill.MAGIC, 600L, 150L, "📺", "YouTube, Netflix, Hulu, Twitch, Duolingo streaming & practice", listOf("youtube", "netflix", "hulu", "twitch", "peacock", "duolingo"), 120L),
            TaskXpConfig("audiobook", "📚 Audiobooks & Reading", OsrsSkill.RUNECRAFT, 500L, 120L, "📚", "Audible, Google Play Books, Kindle, Audiobooks", listOf("audible", "books", "kindle", "audiobook"), 100L),
            TaskXpConfig("music", "🎵 Music Listening", OsrsSkill.DIVINATION, 450L, 100L, "🎵", "Spotify, Apple Music, YouTube Music, Pandora", listOf("spotify", "music", "pandora", "apple music"), 90L),
            TaskXpConfig("fitness", "🏃 Fitness & Running", OsrsSkill.AGILITY, 500L, 100L, "🏃", "Strava, Nike Run Club, Pedometer, Fitness apps", listOf("fit", "health", "strava", "pedometer", "nike"), 150L),
            TaskXpConfig("google_search", "🔍 Google Search Widget", OsrsSkill.HERBLORE, 350L, 100L, "🔍", "Google Search & Widget lookups", listOf("googlequicksearchbox", "quicksearchbox"), 60L),
            TaskXpConfig("lens", "🔍 Google Lens Scans", OsrsSkill.THIEVING, 450L, 120L, "🔍", "Google Smart Lens & Camera object scans", listOf("ar.lens", "smartlens", "com.google.android.apps.lens"), 90L),
            TaskXpConfig("sleep", "😴 7+ Hours Sleep & Inactivity", OsrsSkill.HITPOINTS, 1500L, 500L, "😴", "7+ hours of phone screen-off inactivity & sleep", listOf("sleep", "inactivity"), 30L),
            TaskXpConfig("messaging", "💬 Text Messaging & Chat", OsrsSkill.DIVINATION, 350L, 100L, "💬", "Sending/receiving SMS, WhatsApp, Messenger", listOf("messaging", "sms", "whatsapp", "signal", "telegram", "messenger", "chat", "text"), 70L),
            TaskXpConfig("workout", "💪 Gym Workout & Reps", OsrsSkill.ATTACK, 500L, 150L, "💪", "Pushups, squats, weight lifting reps", emptyList(), 100L),
            TaskXpConfig("reading_block", "📖 Focused Reading Session", OsrsSkill.RUNECRAFT, 600L, 200L, "📖", "30 minutes of book reading or study", emptyList(), 120L)
        )
    }
}

class TaskXpManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("osrs_master_control_panel_prefs", Context.MODE_PRIVATE)

    fun getXpMultiplier(): Float {
        return prefs.getFloat("global_xp_multiplier", 1.0f)
    }

    fun setXpMultiplier(multiplier: Float) {
        prefs.edit().putFloat("global_xp_multiplier", multiplier.coerceIn(0.1f, 100.0f)).apply()
    }

    fun getTaskXp(taskId: String, defaultXp: Long): Long {
        val base = prefs.getLong("task_xp_$taskId", defaultXp)
        return (base * getXpMultiplier()).toLong()
    }

    fun setTaskXp(taskId: String, xp: Long) {
        prefs.edit().putLong("task_xp_$taskId", xp).apply()
    }

    fun getTaskXpPerMin(taskId: String, defaultXpPerMin: Long): Long {
        val base = prefs.getLong("task_xp_per_min_$taskId", defaultXpPerMin)
        return (base * getXpMultiplier()).toLong()
    }

    fun setTaskXpPerMin(taskId: String, xpPerMin: Long) {
        prefs.edit().putLong("task_xp_per_min_$taskId", xpPerMin).apply()
    }

    fun getTaskGp(taskId: String, defaultGp: Long): Long {
        val base = prefs.getLong("task_gp_$taskId", defaultGp)
        return (base * getXpMultiplier()).toLong()
    }

    fun setTaskGp(taskId: String, gp: Long) {
        prefs.edit().putLong("task_gp_$taskId", gp).apply()
    }

    fun getCustomAppPackages(): Set<String> {
        return prefs.getStringSet("custom_app_packages", emptySet()) ?: emptySet()
    }

    fun addCustomAppPackage(pkgName: String) {
        val current = getCustomAppPackages().toMutableSet()
        current.add(pkgName.lowercase().trim())
        prefs.edit().putStringSet("custom_app_packages", current).apply()
    }
}
