package com.example.data.ai.brain

import com.example.data.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BrainCognitivePipeline(
    private val brainLobeStateDao: BrainLobeStateDao,
    private val brainNeuralLogDao: BrainNeuralLogDao
) {
    private val frontalEngine = FrontalLobeEngine()
    private val limbicEngine = LimbicLobeEngine()
    private val parietalEngine = ParietalLobeEngine()
    private val occipitalEngine = OccipitalLobeEngine()
    private val thalamusEngine = ThalamusHypothalamusEngine()
    private val cerebellumEngine = CerebellumBrainstemEngine()
    private val pinealEngine = PinealCorpusCallosumEngine()

    suspend fun initializeLobeStatesIfNeeded() = withContext(Dispatchers.IO) {
        BrainLobeType.entries.forEach { lobe ->
            val existing = brainLobeStateDao.getLobeState(lobe.id)
            if (existing == null) {
                brainLobeStateDao.insertOrUpdateLobe(
                    BrainLobeStateEntity(
                        lobeId = lobe.id,
                        displayName = lobe.displayName,
                        anatomicalRole = lobe.anatomicalRole,
                        hexColor = lobe.hexColor,
                        activityLevel = 0.75f,
                        neuralFiringHz = lobe.baseHz,
                        status = "Active & Attuned",
                        currentThought = "Standing by for cognitive synthesis...",
                        influenceWeight = 1.0f,
                        lastFiredTimestamp = System.currentTimeMillis(),
                        totalModificationsApplied = 0,
                        lastCognitiveOutput = "Cognitive pathway primed."
                    )
                )
            }
        }
    }

    suspend fun executeCognitivePass(input: CognitiveContextInput): SynthesizedBrainState = withContext(Dispatchers.IO) {
        initializeLobeStatesIfNeeded()

        // Fetch user-configured influence weights
        val modulations = mutableMapOf<BrainLobeType, LobeCognitiveModulation>()
        val updatedStates = mutableListOf<BrainLobeStateEntity>()

        for (lobe in BrainLobeType.entries) {
            val dbState = brainLobeStateDao.getLobeState(lobe.id)
            val weight = dbState?.influenceWeight ?: 1.0f

            val mod = when (lobe) {
                BrainLobeType.FRONTAL -> frontalEngine.process(input, weight)
                BrainLobeType.LIMBIC -> limbicEngine.process(input, weight)
                BrainLobeType.PARIETAL -> parietalEngine.process(input, weight)
                BrainLobeType.OCCIPITAL -> occipitalEngine.process(input, weight)
                BrainLobeType.THALAMUS_HYPOTHALAMUS -> thalamusEngine.process(input, weight)
                BrainLobeType.CEREBELLUM_BRAINSTEM -> cerebellumEngine.process(input, weight)
                BrainLobeType.PINEAL_CORPUS_CALLOSUM -> pinealEngine.process(input, weight)
            }

            modulations[lobe] = mod

            val totalMods = (dbState?.totalModificationsApplied ?: 0) + 1
            val updated = (dbState ?: BrainLobeStateEntity(
                lobeId = lobe.id,
                displayName = lobe.displayName,
                anatomicalRole = lobe.anatomicalRole,
                hexColor = lobe.hexColor
            )).copy(
                activityLevel = mod.activityScore,
                neuralFiringHz = mod.firingHz,
                currentThought = mod.thoughtStream,
                status = if (mod.activityScore > 0.85f) "High Resonance" else "Harmonic Flow",
                lastFiredTimestamp = System.currentTimeMillis(),
                totalModificationsApplied = totalMods,
                lastCognitiveOutput = mod.cognitiveModifierSummary
            )
            updatedStates.add(updated)

            // Log high activity firings
            if (mod.activityScore > 0.82f) {
                brainNeuralLogDao.insertLog(
                    BrainNeuralLogEntity(
                        lobeId = lobe.id,
                        eventTitle = "${lobe.displayName} Neural Pulse",
                        cognitiveSynthesisDetail = mod.thoughtStream,
                        firingRate = mod.firingHz,
                        activityLevel = mod.activityScore
                    )
                )
            }
        }

        brainLobeStateDao.insertOrUpdateLobes(updatedStates)

        // Find primary firing lobe
        val primaryLobe = modulations.maxByOrNull { it.value.activityScore }?.key ?: BrainLobeType.FRONTAL
        val avgArousal = modulations.values.map { it.activityScore }.average().toFloat()

        val promptInjection = modulations.values.joinToString("\n\n") { it.promptDirective }

        val recommendedExpr = modulations.values.firstNotNullOfOrNull { it.suggestedExpression } ?: "TALKING"
        val recommendedGesture = modulations[BrainLobeType.CEREBELLUM_BRAINSTEM]?.suggestedPhysicalAction ?: "*listens with attuned warmth*"
        val thoughtsCombined = modulations.values.joinToString(" | ") { "${it.lobeType.name.take(4)}: ${it.thoughtStream.take(40)}..." }

        SynthesizedBrainState(
            lobeModulations = modulations,
            aggregatedPromptInjection = promptInjection,
            primaryLobeFiring = primaryLobe,
            overallCognitiveArousal = avgArousal,
            recommendedExpression = recommendedExpr,
            recommendedGestureAsterisk = recommendedGesture,
            synthesizedThoughtStream = thoughtsCombined
        )
    }

    suspend fun stimulateLobe(lobeId: String, boostFactor: Float = 1.3f) = withContext(Dispatchers.IO) {
        val lobe = brainLobeStateDao.getLobeState(lobeId) ?: return@withContext
        val boostedActivity = (lobe.activityLevel * boostFactor).coerceIn(0.1f, 1.0f)
        val boostedHz = lobe.neuralFiringHz * boostFactor
        val updated = lobe.copy(
            activityLevel = boostedActivity,
            neuralFiringHz = boostedHz,
            status = "Manually Stimulated & Resonating",
            currentThought = "Manual cognitive stimulation received. Firing at ${"%.1f".format(boostedHz)} Hz.",
            lastFiredTimestamp = System.currentTimeMillis()
        )
        brainLobeStateDao.insertOrUpdateLobe(updated)
        brainNeuralLogDao.insertLog(
            BrainNeuralLogEntity(
                lobeId = lobeId,
                eventTitle = "${lobe.displayName} Manual Surge",
                cognitiveSynthesisDetail = "User stimulated neural pathways. Activity surged to ${"%.0f".format(boostedActivity * 100)}%.",
                firingRate = boostedHz,
                activityLevel = boostedActivity
            )
        )
    }

    suspend fun setLobeInfluenceWeight(lobeId: String, weight: Float) = withContext(Dispatchers.IO) {
        brainLobeStateDao.updateLobeWeight(lobeId, weight.coerceIn(0.1f, 2.0f))
    }
}
