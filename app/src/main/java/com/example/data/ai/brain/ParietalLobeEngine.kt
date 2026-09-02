package com.example.data.ai.brain

class ParietalLobeEngine {

    fun process(input: CognitiveContextInput, weight: Float = 1.0f): LobeCognitiveModulation {
        val favZone = input.movement?.favoriteZone ?: "Top-Right Desk Perch"
        val totalDrags = input.movement?.totalDrags ?: 0
        val lightLevel = input.telemetry?.lightLevelCategory ?: "Normal Ambient"
        val motionState = input.telemetry?.motionState ?: "Stationary / Resting"
        val userMsg = input.userMessage.lowercase()

        val touchesSurroundings = userMsg.contains("room") || userMsg.contains("desk") || userMsg.contains("outside") ||
                userMsg.contains("weather") || userMsg.contains("dark") || userMsg.contains("bright") || userMsg.contains("hold") || userMsg.contains("touch")

        val activity = ((if (touchesSurroundings) 0.88f else 0.65f) * weight).coerceIn(0.2f, 1.0f)
        val hz = (20f + (activity * 18f))

        val thought = "Integrating spatial coordinates: Perched at '$favZone' ($totalDrags total drifts). Ambient light: $lightLevel. Motion: $motionState."

        val promptDirective = """
            [PARIETAL LOBE SENSORY & SPATIAL INTEGRATION - Weight: ${"%.1f".format(weight)}]:
            - Spatial Perch: Grounded in your sanctuary at '$favZone'.
            - Ambient Sensory Field: Light is $lightLevel, motion is $motionState.
            - Somatosensory Alignment: Radiate a tangible, present, and spatially grounded companion presence.
        """.trimIndent()

        return LobeCognitiveModulation(
            lobeType = BrainLobeType.PARIETAL,
            activityScore = activity,
            firingHz = hz,
            thoughtStream = thought,
            promptDirective = promptDirective,
            cognitiveModifierSummary = "Perch: $favZone ($lightLevel)",
            suggestedPhysicalAction = "*settles comfortably at $favZone*"
        )
    }
}
