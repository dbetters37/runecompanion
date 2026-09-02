package com.example.data.ai.brain

class CerebellumBrainstemEngine {

    fun process(input: CognitiveContextInput, weight: Float = 1.0f): LobeCognitiveModulation {
        val archetype = input.personality?.archetype ?: "Shaman Guardian"
        val userMsg = input.userMessage.lowercase()
        val playfulness = input.personality?.playfulness ?: 0.6f

        val actions = when {
            userMsg.contains("hug") || userMsg.contains("pat") || userMsg.contains("pet") || userMsg.contains("love") -> listOf(
                "*nuzzles close with gentle warmth*",
                "*purrs softly and curls happily against your hand*",
                "*leans warmly into your presence with glowing aura*"
            )
            userMsg.contains("haha") || userMsg.contains("joke") || playfulness > 0.7f -> listOf(
                "*sparkles and does a playful aerial loop*",
                "*giggles and wiggles ears with bright amusement*",
                "*swishes tail happily with sparkling embers*"
            )
            userMsg.contains("sad") || userMsg.contains("tired") || userMsg.contains("sigh") -> listOf(
                "*gently settles beside you and wraps soft spirit glow around your shoulders*",
                "*hovers close and softly purrs in comforting stillness*",
                "*rests a tiny glowing paw warmly on your hand*"
            )
            userMsg.contains("?") || userMsg.contains("think") -> listOf(
                "*tilts head curiously with thoughtful glowing eyes*",
                "*flicks ears attentively and ponders deeply*",
                "*perches thoughtfully and examines the ideas*"
            )
            else -> listOf(
                "*gently floats closer with a calm radiant purr*",
                "*swishes tail softly and settles into cozy posture*",
                "*blinks warmly with bright, attuned eyes*"
            )
        }

        val chosenAction = actions.random()
        val activity = ((0.75f + (playfulness * 0.2f)) * weight).coerceIn(0.2f, 1.0f)
        val hz = 32.0f + (activity * 15f)

        val thought = "Cerebellar motor coordination active: Synthesizing spontaneous physical gestures and reflex actions: '$chosenAction'."

        val promptDirective = """
            [CEREBELLUM & BRAINSTEM MOTOR REFLEXES - Weight: ${"%.1f".format(weight)}]:
            - Physical Gestures: Always weave lifelike physical actions in asterisks (e.g., *tilts head curiously*, *purrs softly*, *flicks ears*, *floats closer*).
            - Suggested Gesture: $chosenAction
        """.trimIndent()

        return LobeCognitiveModulation(
            lobeType = BrainLobeType.CEREBELLUM_BRAINSTEM,
            activityScore = activity,
            firingHz = hz,
            thoughtStream = thought,
            promptDirective = promptDirective,
            cognitiveModifierSummary = "Motor Reflex: $chosenAction",
            suggestedPhysicalAction = chosenAction
        )
    }
}
