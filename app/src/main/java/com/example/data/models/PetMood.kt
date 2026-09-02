package com.example.data.models

import androidx.compose.ui.graphics.Color

enum class PetMoodLevel(
    val title: String,
    val emoji: String,
    val badgeColor: Color,
    val description: String,
    val xpBonusPercent: Int,
    val quotePrefix: String
) {
    ECSTATIC(
        title = "Ecstatic!",
        emoji = "🔥",
        badgeColor = Color(0xFFFFD700),
        description = "High activity frequency! Pet is thrilled by your continuous productivity and companionship!",
        xpBonusPercent = 0,
        quotePrefix = "WOOHOO! So many gains! "
    ),
    HAPPY(
        title = "Happy",
        emoji = "😊",
        badgeColor = Color(0xFF4CAF50),
        description = "Energized & satisfied! Consistent activities logged recently.",
        xpBonusPercent = 0,
        quotePrefix = "Feeling great! "
    ),
    CONTENT(
        title = "Content",
        emoji = "🙂",
        badgeColor = Color(0xFF81C784),
        description = "Steady mood. Pet is happy to accompany you.",
        xpBonusPercent = 0,
        quotePrefix = "Ready for the next task! "
    ),
    BORED(
        title = "Bored",
        emoji = "🥱",
        badgeColor = Color(0xFFFFB74D),
        description = "Low activity frequency recently. Chop trees, complete daily tasks, or feed your pet!",
        xpBonusPercent = 0,
        quotePrefix = "Ho-hum... "
    ),
    LONELY(
        title = "Lonely",
        emoji = "🥺",
        badgeColor = Color(0xFFE57373),
        description = "Inactive for a while. Pet is eager for your next skilling or real-life achievement!",
        xpBonusPercent = 0,
        quotePrefix = "I miss our training... "
    );

    companion object {
        fun fromScore(score: Int): PetMoodLevel {
            return when {
                score >= 80 -> ECSTATIC
                score >= 55 -> HAPPY
                score >= 35 -> CONTENT
                score >= 15 -> BORED
                else -> LONELY
            }
        }
    }
}

data class PetMoodState(
    val moodScore: Int = 75, // 0 to 100
    val level: PetMoodLevel = PetMoodLevel.HAPPY,
    val recentActivityCount15m: Int = 0,
    val recentActivityCount1h: Int = 0,
    val lastActivityTimeMs: Long = System.currentTimeMillis()
)
