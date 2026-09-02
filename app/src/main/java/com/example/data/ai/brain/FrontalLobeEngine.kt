package com.example.data.ai.brain

class FrontalLobeEngine {

    fun process(input: CognitiveContextInput, weight: Float = 1.0f): LobeCognitiveModulation {
        val userMsg = input.userMessage.lowercase()
        val directives = input.personality?.customDirectives?.trim() ?: ""
        val stage = input.personality?.evolutionStage ?: "Wise Shaman"
        val intellect = input.tracker?.intellectualNuance ?: 1.0f

        val isComplexQuery = userMsg.length > 50 || userMsg.contains("why") || userMsg.contains("how") ||
                userMsg.contains("think") || userMsg.contains("philosophy") || userMsg.contains("mean") || userMsg.contains("code")

        val activity = ((if (isComplexQuery) 0.92f else 0.72f) * weight).coerceIn(0.2f, 1.0f)
        val hz = (35f + (activity * 25f)) * weight

        val thought = when {
            isComplexQuery -> "Synthesizing deep executive logic & multi-layered conceptual framing for '${input.userMessage.take(30)}...'"
            directives.isNotBlank() -> "Aligning syntax and linguistic tone strictly to user directive: '${directives.take(35)}...'"
            else -> "Structuring clear conversational syntax, goal alignment, and intellectual clarity ($stage)."
        }

        val promptDirective = """
            [FRONTAL LOBE EXECUTIVE COGNITION - Weight: ${"%.1f".format(weight)}]:
            - Executive Logic: Deliver structured, coherent, and thoughtfully reasoned dialogue.
            - Intellectual Nuance Level: ${"%.1f".format(intellect * 100)}%
            - Philosophical Depth: Balance wit with profound mindfulness. Reflect on the underlying premise of user inquiries.
        """.trimIndent()

        return LobeCognitiveModulation(
            lobeType = BrainLobeType.FRONTAL,
            activityScore = activity,
            firingHz = hz,
            thoughtStream = thought,
            promptDirective = promptDirective,
            cognitiveModifierSummary = if (isComplexQuery) "Deep Intellectual Framing" else "Executive Syntax Active",
            suggestedExpression = if (isComplexQuery) "THINKING" else null
        )
    }
}
