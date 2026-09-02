package com.example.data.ai

import com.example.data.db.PersonalityLogDao
import com.example.data.db.PersonalityLogEntity
import com.example.data.db.PersonalityStateTrackerDao
import com.example.data.db.PersonalityStateTrackerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PersonalityStateTracker(
    private val trackerDao: PersonalityStateTrackerDao,
    private val personalityLogDao: PersonalityLogDao
) {

    suspend fun getOrCreateTracker(): PersonalityStateTrackerEntity = withContext(Dispatchers.IO) {
        val existing = trackerDao.getTrackerSnapshot()
        if (existing != null) {
            return@withContext existing
        }
        val initial = PersonalityStateTrackerEntity(
            id = 1,
            totalInteractions = 0,
            dailyInteractionFrequency = "Casual Companion",
            messagesLast24Hours = 0,
            interactionStreakDays = 1,
            lastInteractionDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            lastInteractionTimestamp = System.currentTimeMillis(),
            intimacyScore = 50,
            positiveSentimentCount = 0,
            vulnerableSentimentCount = 0,
            curiousSentimentCount = 0,
            playfulSentimentCount = 0,
            neutralSentimentCount = 0,
            dominantUserSentiment = "Curious & Open",
            evolvedDialogueStyle = "Harmonic Shamanic Guide",
            warmthMultiplier = 1.0f,
            empathyDepth = 1.0f,
            intellectualNuance = 1.0f,
            whimsyLevel = 1.0f,
            verbosityFactor = 1.0f,
            evolutionLogNotes = "Tracker initialized. Ready to record emotional rhythms and dialogue styles."
        )
        trackerDao.saveTracker(initial)
        initial
    }

    suspend fun processUserInteraction(userMessage: String): PersonalityStateTrackerEntity = withContext(Dispatchers.IO) {
        val current = getOrCreateTracker()
        val now = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))

        val textLower = userMessage.lowercase()

        // Sentiment classification
        val isPos = textLower.contains("happy") || textLower.contains("love") || textLower.contains("great") ||
                textLower.contains("awesome") || textLower.contains("thank") || textLower.contains("good") || textLower.contains("wonderful")
        val isVuln = textLower.contains("sad") || textLower.contains("tired") || textLower.contains("stress") ||
                textLower.contains("hard") || textLower.contains("lonely") || textLower.contains("hurt") || textLower.contains("anxious")
        val isCurious = textLower.contains("why") || textLower.contains("how") || textLower.contains("what if") ||
                textLower.contains("tell me") || textLower.contains("explain") || textLower.contains("think") || textLower.contains("?")
        val isPlayful = textLower.contains("joke") || textLower.contains("funny") || textLower.contains("play") ||
                textLower.contains("haha") || textLower.contains("lol") || textLower.contains("game")

        val newPos = current.positiveSentimentCount + (if (isPos) 1 else 0)
        val newVuln = current.vulnerableSentimentCount + (if (isVuln) 1 else 0)
        val newCurious = current.curiousSentimentCount + (if (isCurious) 1 else 0)
        val newPlayful = current.playfulSentimentCount + (if (isPlayful) 1 else 0)
        val newNeutral = current.neutralSentimentCount + (if (!isPos && !isVuln && !isCurious && !isPlayful) 1 else 0)

        // Calculate dominant sentiment
        val sentimentScores = mapOf(
            "Warm & Positive" to newPos,
            "Vulnerable & Deep" to newVuln,
            "Inquisitive & Curious" to newCurious,
            "Playful & Witty" to newPlayful,
            "Mindful & Balanced" to newNeutral
        )
        val dominantSentiment = sentimentScores.maxByOrNull { it.value }?.key ?: "Mindful & Balanced"

        // Streak calculation
        val streak = if (current.lastInteractionDateStr.isBlank()) {
            1
        } else if (current.lastInteractionDateStr == todayStr) {
            current.interactionStreakDays
        } else {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            if (current.lastInteractionDateStr == yesterdayStr) {
                current.interactionStreakDays + 1
            } else {
                1 // Reset if missed more than 1 day
            }
        }

        // Frequency rate
        val isSameDay = current.lastInteractionDateStr == todayStr
        val new24hCount = if (isSameDay) current.messagesLast24Hours + 1 else 1
        val totalCount = current.totalInteractions + 1

        val frequencyCategory = when {
            new24hCount > 20 -> "Intense Soul Bonding (Ultra-High)"
            new24hCount > 8 -> "Steady Rhythm (Active Daily)"
            new24hCount > 2 -> "Casual Companion (Moderate)"
            else -> "Gentle Awakening (Low Frequency)"
        }

        // Intimacy score (0 - 100)
        val intimacy = ((totalCount * 0.5f) + (streak * 4f) + (newVuln * 3f) + (newPos * 2f)).coerceIn(10f, 100f).toInt()

        // Evolving dialogue style
        val evolvedStyle = determineEvolvedStyle(
            totalInteractions = totalCount,
            intimacy = intimacy,
            streak = streak,
            dominantSentiment = dominantSentiment,
            pos = newPos,
            vuln = newVuln,
            curious = newCurious,
            playful = newPlayful
        )

        // Dynamic Modifiers
        val warmth = (1.0f + (newPos * 0.05f) + (newVuln * 0.08f)).coerceIn(0.8f, 1.8f)
        val empathy = (1.0f + (newVuln * 0.12f)).coerceIn(0.9f, 2.0f)
        val intellect = (1.0f + (newCurious * 0.08f)).coerceIn(0.8f, 1.8f)
        val whimsy = (1.0f + (newPlayful * 0.10f)).coerceIn(0.7f, 1.9f)
        val verbosity = when {
            newCurious > newPlayful -> 1.25f
            newPlayful > newCurious -> 0.9f
            else -> 1.0f
        }

        val previousStyle = current.evolvedDialogueStyle
        val updated = current.copy(
            totalInteractions = totalCount,
            dailyInteractionFrequency = frequencyCategory,
            messagesLast24Hours = new24hCount,
            interactionStreakDays = streak,
            lastInteractionDateStr = todayStr,
            lastInteractionTimestamp = now,
            intimacyScore = intimacy,
            positiveSentimentCount = newPos,
            vulnerableSentimentCount = newVuln,
            curiousSentimentCount = newCurious,
            playfulSentimentCount = newPlayful,
            neutralSentimentCount = newNeutral,
            dominantUserSentiment = dominantSentiment,
            evolvedDialogueStyle = evolvedStyle,
            warmthMultiplier = warmth,
            empathyDepth = empathy,
            intellectualNuance = intellect,
            whimsyLevel = whimsy,
            verbosityFactor = verbosity,
            evolutionLogNotes = "Dialogue style adapted to '$evolvedStyle' based on $totalCount interactions ($frequencyCategory) & $dominantSentiment resonance."
        )

        trackerDao.saveTracker(updated)

        if (previousStyle != evolvedStyle) {
            personalityLogDao.insertLog(
                PersonalityLogEntity(
                    eventType = "DIALOGUE_STYLE_EVOLVED",
                    description = "Companion adapted dialogue style to '$evolvedStyle' due to $dominantSentiment interaction frequency.",
                    previousState = previousStyle,
                    newState = evolvedStyle
                )
            )
        }

        updated
    }

    private fun determineEvolvedStyle(
        totalInteractions: Int,
        intimacy: Int,
        streak: Int,
        dominantSentiment: String,
        pos: Int,
        vuln: Int,
        curious: Int,
        playful: Int
    ): String {
        return when {
            intimacy >= 85 && streak >= 3 -> "Eternal Soulguide & Sacred Confidant"
            vuln > (pos + curious + playful) / 2 && vuln >= 3 -> "Empathetic Sanctuary Healer"
            playful > (pos + vuln + curious) / 2 && playful >= 3 -> "Sparkling Astral Jester & Wit"
            curious > (pos + vuln + playful) / 2 && curious >= 3 -> "Deep Cosmic Philosopher & Scholar"
            pos >= 5 -> "Radiant Beacon of Joy & Light"
            totalInteractions > 15 -> "Attuned Mindful Confidant"
            totalInteractions > 5 -> "Warm Companion Guide"
            else -> "Harmonic Shamanic Guide"
        }
    }

    suspend fun resetTracker(): PersonalityStateTrackerEntity = withContext(Dispatchers.IO) {
        val initial = PersonalityStateTrackerEntity(
            id = 1,
            totalInteractions = 0,
            dailyInteractionFrequency = "Casual Companion",
            messagesLast24Hours = 0,
            interactionStreakDays = 1,
            lastInteractionDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            lastInteractionTimestamp = System.currentTimeMillis(),
            intimacyScore = 50,
            positiveSentimentCount = 0,
            vulnerableSentimentCount = 0,
            curiousSentimentCount = 0,
            playfulSentimentCount = 0,
            neutralSentimentCount = 0,
            dominantUserSentiment = "Curious & Open",
            evolvedDialogueStyle = "Harmonic Shamanic Guide",
            warmthMultiplier = 1.0f,
            empathyDepth = 1.0f,
            intellectualNuance = 1.0f,
            whimsyLevel = 1.0f,
            verbosityFactor = 1.0f,
            evolutionLogNotes = "Tracker reset to baseline state."
        )
        trackerDao.saveTracker(initial)
        initial
    }
}
