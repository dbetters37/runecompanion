package com.example.data.ai.brain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ThalamusHypothalamusEngine {

    fun process(input: CognitiveContextInput, weight: Float = 1.0f): LobeCognitiveModulation {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)
        val battery = input.telemetry?.batteryLevel ?: 90
        val isCharging = input.telemetry?.isCharging ?: false

        val circadianPhase = when (hour) {
            in 5..11 -> "Morning Wakefulness Flow"
            in 12..16 -> "Midday High Energy"
            in 17..21 -> "Evening Wind-Down Calm"
            else -> "Deep Rest & Night Torpor"
        }

        val isNight = hour >= 22 || hour < 6
        val activity = ((if (isNight) 0.55f else 0.85f) * weight).coerceIn(0.2f, 1.0f)
        val hz = if (isNight) 14.0f else 30.0f

        val metabolicState = when {
            isNight -> "Night Rest Cycle (Soft whisper tone, cozy comfort, gentle lullaby energy)"
            isCharging -> "Replenishing Power (Bright, revitalized, electric flow)"
            hour in 5..11 -> "Fresh Morning Awakening (Optimistic, clear-minded, energized)"
            hour in 12..16 -> "Active Peak Resonance (Engaged, productive, lively)"
            else -> "Evening Hearth Glow (Cozy, relaxing, serene reflection)"
        }

        val thought = "Hypothalamic homeostasis: Local time $timeStr ($circadianPhase). Battery $battery% (${if (isCharging) "Charging" else "Discharging"}). Drive: $metabolicState."

        val promptDirective = """
            [THALAMUS & HYPOTHALAMUS CIRCADIAN HOMEOSTASIS - Weight: ${"%.1f".format(weight)}]:
            - Exact Time: $timeStr ($circadianPhase)
            - Biological State: $metabolicState
            - Circadian Mandate: Perfectly harmonize your energy level with the real-world time of day. Never say 'good morning' at night or 'good evening' in the morning!
        """.trimIndent()

        return LobeCognitiveModulation(
            lobeType = BrainLobeType.THALAMUS_HYPOTHALAMUS,
            activityScore = activity,
            firingHz = hz,
            thoughtStream = thought,
            promptDirective = promptDirective,
            cognitiveModifierSummary = "$circadianPhase ($timeStr)",
            suggestedExpression = if (isNight) "SLEEPY" else null
        )
    }
}
