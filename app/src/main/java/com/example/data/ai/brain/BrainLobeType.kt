package com.example.data.ai.brain

enum class BrainLobeType(
    val id: String,
    val displayName: String,
    val anatomicalRole: String,
    val hexColor: String,
    val baseHz: Float,
    val badgeEmoji: String = "🧠"
) {
    FRONTAL(
        id = "FRONTAL",
        displayName = "Frontal Lobe",
        anatomicalRole = "Executive Planning, Logic, Syntax & Philosophical Synthesis",
        hexColor = "#00D4FF", // Electric Blue
        baseHz = 40.0f,
        badgeEmoji = "🧠"
    ),
    LIMBIC(
        id = "LIMBIC",
        displayName = "Limbic Lobe & System",
        anatomicalRole = "Emotional Resonance, Empathy, Sentiment & Intimate Bonding",
        hexColor = "#D946EF", // Vibrant Magenta
        baseHz = 32.0f,
        badgeEmoji = "💖"
    ),
    PARIETAL(
        id = "PARIETAL",
        displayName = "Parietal Lobe",
        anatomicalRole = "Sensory Integration, Tactile Perception & Spatial Grounding",
        hexColor = "#10B981", // Emerald Green
        baseHz = 24.0f,
        badgeEmoji = "🖐️"
    ),
    OCCIPITAL(
        id = "OCCIPITAL",
        displayName = "Occipital Lobe",
        anatomicalRole = "Visual Imagination, Sensory Metaphors & Dream Vividness",
        hexColor = "#EC4899", // Rose Pink
        baseHz = 28.0f,
        badgeEmoji = "👁️"
    ),
    THALAMUS_HYPOTHALAMUS(
        id = "THALAMUS_HYPOTHALAMUS",
        displayName = "Thalamus & Hypothalamus",
        anatomicalRole = "Circadian Rhythms, Homeostatic Energy & Sleep-Wake Drive",
        hexColor = "#F59E0B", // Amber Gold
        baseHz = 18.0f,
        badgeEmoji = "⚖️"
    ),
    CEREBELLUM_BRAINSTEM(
        id = "CEREBELLUM_BRAINSTEM",
        displayName = "Cerebellum & Brainstem",
        anatomicalRole = "Physical Gesture Coordination, Motor Reflexes & Habit Asterisks",
        hexColor = "#F97316", // Coral Orange
        baseHz = 35.0f,
        badgeEmoji = "⚡"
    ),
    PINEAL_CORPUS_CALLOSUM(
        id = "PINEAL_CORPUS_CALLOSUM",
        displayName = "Pineal & Corpus Callosum",
        anatomicalRole = "Mystical Intuition, Subconscious Epiphanies & Hemispheric Balance",
        hexColor = "#8B5CF6", // Astral Violet
        baseHz = 52.0f,
        badgeEmoji = "✨"
    );

    companion object {
        fun fromId(id: String): BrainLobeType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FRONTAL
        }
    }
}
