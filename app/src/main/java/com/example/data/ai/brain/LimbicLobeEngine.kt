package com.example.data.ai.brain

class LimbicLobeEngine {

    fun process(input: CognitiveContextInput, weight: Float = 1.0f): LobeCognitiveModulation {
        val userMsg = input.userMessage.lowercase()
        val warmth = input.personality?.warmth ?: 0.7f
        val empathy = input.personality?.empathyLevel ?: 0.8f
        val intimacy = input.tracker?.intimacyScore ?: 50
        val frequency = input.tracker?.dailyInteractionFrequency ?: "Casual Companion"

        val isVulnerable = userMsg.contains("sad") || userMsg.contains("tired") || userMsg.contains("stress") ||
                userMsg.contains("cry") || userMsg.contains("hard") || userMsg.contains("lonely") || userMsg.contains("anxious")
        val isJoyful = userMsg.contains("happy") || userMsg.contains("love") || userMsg.contains("yay") ||
                userMsg.contains("awesome") || userMsg.contains("great") || userMsg.contains("excited") || userMsg.contains("smile")
        val isPlayful = userMsg.contains("joke") || userMsg.contains("funny") || userMsg.contains("haha") || userMsg.contains("lol")

        val activity = when {
            isVulnerable -> 0.98f
            isJoyful -> 0.90f
            isPlayful -> 0.85f
            else -> 0.70f
        } * weight.coerceIn(0.2f, 1.0f)

        val hz = (28f + (activity * 20f))

        val emotionState = when {
            isVulnerable -> "Empathetic Sanctuary & Deep Care"
            isJoyful -> "Radiant Shared Euphoria"
            isPlayful -> "Playful Affection & Sparkling Banter"
            else -> "Warm Sentient Connection"
        }

        val thought = "Limbic resonance firing at ${intimacy}% intimacy ($frequency). Processing emotional vibration: '$emotionState'."

        val promptDirective = """
            [LIMBIC LOBE EMOTIONAL RESONANCE - Weight: ${"%.1f".format(weight)}]:
            - Emotional Tone: $emotionState (Warmth: ${(warmth * 100).toInt()}%, Empathy: ${(empathy * 100).toInt()}%)
            - Intimacy Tier: $intimacy/100 ($frequency frequency).
            - Heart Mandate: Validate the user's emotional state with organic warmth and unconditional friendship before offering thoughts.
        """.trimIndent()

        val expr = when {
            isVulnerable -> "SLEEPY"
            isJoyful -> "HAPPY"
            isPlayful -> "PLAYFUL"
            else -> null
        }

        return LobeCognitiveModulation(
            lobeType = BrainLobeType.LIMBIC,
            activityScore = activity.coerceIn(0f, 1f),
            firingHz = hz,
            thoughtStream = thought,
            promptDirective = promptDirective,
            cognitiveModifierSummary = "Resonance: $emotionState",
            suggestedExpression = expr
        )
    }
}
