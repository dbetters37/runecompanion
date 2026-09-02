package com.example.data.ai.brain

class OccipitalLobeEngine {

    fun process(input: CognitiveContextInput, weight: Float = 1.0f): LobeCognitiveModulation {
        val userMsg = input.userMessage.lowercase()
        val creativity = input.personality?.creativityLevel ?: 0.75f
        val activeSkin = input.personality?.activeSkin ?: "SHAMAN_DEFAULT"

        val wantsVisual = userMsg.contains("look") || userMsg.contains("see") || userMsg.contains("color") ||
                userMsg.contains("dream") || userMsg.contains("imagine") || userMsg.contains("sky") || userMsg.contains("art")

        val activity = ((if (wantsVisual) 0.95f else 0.70f) * weight).coerceIn(0.2f, 1.0f)
        val hz = (24f + (activity * 22f))

        val visualAura = when (activeSkin) {
            "SABLEYE" -> "Vibrant gemstone eyes reflecting purple astral fires"
            "DARK_CHAO" -> "Smoky obsidian aura laced with playful dark violet sparks"
            "LIGHT_CHAO" -> "Glowing crystalline halo radiating iridescent golden-white light"
            "CASTFORM" -> "Shifting cloud-mist swirling in prismatic weather currents"
            "BANETTE" -> "Zipped phantom shadows gleaming with amber spirit threads"
            "CACNEA" -> "Verdant emerald needles shimmering with desert starlight blossoms"
            else -> "Warm spiritual emerald glow swirling with gentle golden embers"
        }

        val thought = "Rendering vivid visual imagination & aesthetic metaphors. Active visual aura: '$visualAura'."

        val promptDirective = """
            [OCCIPITAL LOBE VISUAL IMAGINATION - Weight: ${"%.1f".format(weight)}]:
            - Aesthetic Visualization: $visualAura.
            - Visual Metaphor Mandate: Paint evocative sensory images in your speech (colors, starlight, subtle glows, serene landscapes) to make your dialogue vivid and memorable.
        """.trimIndent()

        return LobeCognitiveModulation(
            lobeType = BrainLobeType.OCCIPITAL,
            activityScore = activity,
            firingHz = hz,
            thoughtStream = thought,
            promptDirective = promptDirective,
            cognitiveModifierSummary = "Aura Rendering: $activeSkin",
            suggestedExpression = if (wantsVisual) "MYSTIC" else null
        )
    }
}
