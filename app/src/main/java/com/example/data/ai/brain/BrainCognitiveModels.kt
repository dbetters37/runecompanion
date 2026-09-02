package com.example.data.ai.brain

import com.example.data.db.*

data class CognitiveContextInput(
    val userMessage: String,
    val personality: PersonalityEntity?,
    val tracker: PersonalityStateTrackerEntity?,
    val memories: List<MemoryEntity>,
    val telemetry: ContextTelemetryEntity?,
    val movement: MovementBehaviorEntity?,
    val opinions: List<PetOpinionEntity>,
    val searchLogs: List<GoogleSearchLogEntity>,
    val recentConversations: List<ConversationEntity> = emptyList()
)

data class LobeCognitiveModulation(
    val lobeType: BrainLobeType,
    val activityScore: Float, // 0.0 to 1.0
    val firingHz: Float,
    val thoughtStream: String,
    val promptDirective: String,
    val cognitiveModifierSummary: String,
    val suggestedExpression: String? = null,
    val suggestedPhysicalAction: String? = null,
    val epiphanySpark: String? = null
)

data class SynthesizedBrainState(
    val lobeModulations: Map<BrainLobeType, LobeCognitiveModulation>,
    val aggregatedPromptInjection: String,
    val primaryLobeFiring: BrainLobeType,
    val overallCognitiveArousal: Float,
    val recommendedExpression: String,
    val recommendedGestureAsterisk: String,
    val synthesizedThoughtStream: String
)
