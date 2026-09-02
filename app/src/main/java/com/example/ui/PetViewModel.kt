package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PetRepository
import com.example.data.ai.GeminiPetService
import com.example.data.db.*
import com.example.data.drive.GoogleDriveSyncManager
import com.example.ui.components.PetExpression
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PetViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val driveSyncManager = GoogleDriveSyncManager(application, db.driveSyncLogDao())
    private val geminiService = GeminiPetService()

    private val repository = PetRepository(
        memoryDao = db.memoryDao(),
        conversationDao = db.conversationDao(),
        personalityDao = db.personalityDao(),
        personalityLogDao = db.personalityLogDao(),
        driveSyncLogDao = db.driveSyncLogDao(),
        telemetryDao = db.telemetryDao(),
        petOpinionDao = db.petOpinionDao(),
        condensedMemoryDao = db.condensedMemoryDao(),
        movementBehaviorDao = db.movementBehaviorDao(),
        petDailyJournalDao = db.petDailyJournalDao(),
        petDreamJournalDao = db.petDreamJournalDao(),
        googleSearchLogDao = db.googleSearchLogDao(),
        personalityStateTrackerDao = db.personalityStateTrackerDao(),
        brainLobeStateDao = db.brainLobeStateDao(),
        brainNeuralLogDao = db.brainNeuralLogDao(),
        subjectiveWorldModelDao = db.subjectiveWorldModelDao(),
        autonomousGoalDao = db.autonomousGoalDao(),
        persistentMemoryLoopDao = db.persistentMemoryLoopDao(),
        driveSyncManager = driveSyncManager,
        geminiPetService = geminiService
    )

    val memoriesState: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversationsState: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalityState: StateFlow<PersonalityEntity?> = repository.personality
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val personalityLogsState: StateFlow<List<PersonalityLogEntity>> = repository.personalityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncLogsState: StateFlow<List<DriveSyncLogEntity>> = repository.syncLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val telemetryLogsState: StateFlow<List<ContextTelemetryEntity>> = repository.telemetryLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val petOpinionsState: StateFlow<List<PetOpinionEntity>> = repository.petOpinions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val condensedMemoriesState: StateFlow<List<CondensedMemoryEntity>> = repository.condensedMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movementBehaviorState: StateFlow<MovementBehaviorEntity?> = repository.movementBehavior
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyJournalsState: StateFlow<List<PetDailyJournalEntity>> = repository.dailyJournals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dreamJournalsState: StateFlow<List<PetDreamJournalEntity>> = repository.dreamJournals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchLogsState: StateFlow<List<GoogleSearchLogEntity>> = repository.searchLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalityTrackerState: StateFlow<PersonalityStateTrackerEntity?> = repository.personalityTracker
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val brainLobeStates: StateFlow<List<BrainLobeStateEntity>> = repository.brainLobeStates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val brainNeuralLogs: StateFlow<List<BrainNeuralLogEntity>> = repository.brainNeuralLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val worldModelState: StateFlow<SubjectiveWorldModelEntity?> = repository.worldModel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val autonomousGoalsState: StateFlow<List<AutonomousGoalEntity>> = repository.autonomousGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCuriosityGoalsState: StateFlow<List<AutonomousGoalEntity>> = repository.activeCuriosityGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memoryLoopsState: StateFlow<List<PersistentMemoryLoopEntity>> = repository.memoryLoops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSpatialState = MutableStateFlow(com.example.data.ai.SpatialPlacementState())

    val isApiKeyLinkedState = MutableStateFlow(geminiService.isApiKeyLinked())
    val apiKeySourceState = MutableStateFlow(geminiService.getApiKeySource())
    val activeAiProviderState: MutableStateFlow<String> = MutableStateFlow(com.example.data.ai.GeminiPetService.getSelectedProvider())
    val maskedApiKeyConfiguredState = MutableStateFlow(geminiService.getMaskedApiKey())
    val apiKeyValidationMessageState = MutableStateFlow<String?>(null)
    val isTestingApiKeyState = MutableStateFlow(false)

    val currentExpression = MutableStateFlow(PetExpression.IDLE)
    val isGenerating = MutableStateFlow(false)
    val localTextLogContent = MutableStateFlow("")
    val selectedTab = MutableStateFlow(0) // 0: Home/Pet Stage, 1: Memory Log, 2: Evolution, 3: Drive Sync
    val syncStatusMessage = MutableStateFlow<String?>(null)
    val driveAuthState = MutableStateFlow(repository.getDriveAuthState())

    val cooldownRemainingState = MutableStateFlow(0)
    val timeOfDayModeState = MutableStateFlow("☀️ Daytime Vitality (3s Cooldown)")

    private var lastInteractionTimeMs: Long = 0L

    fun getInteractionCooldownSeconds(): Int {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour >= 22 || hour < 6 -> 20 // Late Night Slumber Mode (20s cooldown)
            hour in 18..21 -> 8         // Evening Wind-Down Mode (8s cooldown)
            else -> 3                  // Daytime Vitality Mode (3s cooldown)
        }
    }

    fun getTimeOfDayModeLabel(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour >= 22 || hour < 6 -> "🌙 Night Slumber Mode (20s Cooldown)"
            hour in 18..21 -> "🌆 Evening Wind-Down Mode (8s Cooldown)"
            else -> "☀️ Daytime Vitality Mode (3s Cooldown)"
        }
    }

    fun getRemainingCooldownSeconds(): Int {
        val cooldownMaxSec = getInteractionCooldownSeconds()
        val elapsedSec = ((System.currentTimeMillis() - lastInteractionTimeMs) / 1000L).toInt()
        val remaining = cooldownMaxSec - elapsedSec
        return if (remaining > 0) remaining else 0
    }

    init {
        val prefs = getApplication<Application>().getSharedPreferences("pet_ai_preferences", android.content.Context.MODE_PRIVATE)
        val savedOpenAiKey = prefs.getString("user_openai_api_key", "") ?: ""

        if (savedOpenAiKey.isNotBlank()) {
            com.example.data.ai.GeminiPetService.setCustomOpenAiKey(savedOpenAiKey)
        }
        refreshApiKeyStates()

        viewModelScope.launch {
            repository.initializeDefaultPersonalityIfNeeded()
            repository.seedInitialJournalsIfNeeded()
            repository.ensureBrainInitialized()
            repository.checkAndTriggerMorningGreetingIfNeeded(getApplication())
            recordTelemetrySnapshot()
            refreshLocalTextFileContent()
            // Schedule background worker to run daily and summarize raw memories
            com.example.data.worker.MemorySummarizerScheduler.enqueueDailyMemorySummaryWork(getApplication())
            // Schedule background Google Drive sync worker service
            com.example.data.worker.GoogleDriveSyncScheduler.schedulePeriodicDriveSync(getApplication())
            // Start background environment perception service
            try {
                com.example.data.service.EnvironmentPerceptionService.startService(getApplication())
            } catch (e: Exception) {
                // Ignore service start failure
            }

            // Sync movement behavior to active spatial placement state
            viewModelScope.launch {
                repository.movementBehavior.collect { behavior ->
                    if (behavior != null) {
                        val state = com.example.data.ai.SpatialPlacementState.fromJson(behavior.structuredStateJson)
                        currentSpatialState.value = state
                    }
                }
            }
        }


        // Timer loop for real-time cooldown & night time default state
        viewModelScope.launch {
            while (true) {
                cooldownRemainingState.value = getRemainingCooldownSeconds()
                timeOfDayModeState.value = getTimeOfDayModeLabel()
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                if ((hour >= 22 || hour < 6) && currentExpression.value == PetExpression.IDLE) {
                    currentExpression.value = PetExpression.SLEEPY
                }
                kotlinx.coroutines.delay(1000L)
            }
        }
    }

    fun triggerEnvironmentPerceptionScan() {
        viewModelScope.launch {
            currentExpression.value = PetExpression.MYSTIC
            syncStatusMessage.value = "Scanning ambient light, motion, & time perception..."
            val collector = com.example.data.telemetry.PhoneTelemetryCollector(getApplication())
            val comment = repository.recordAndCommentOnEnvironmentPerception(collector, getApplication())
            syncStatusMessage.value = "Pet perceived environment: $comment"
            refreshLocalTextFileContent()
        }
    }

    fun triggerDeepThoughtReflection() {
        viewModelScope.launch {
            currentExpression.value = PetExpression.MYSTIC
            syncStatusMessage.value = "Contemplating personality traits & daily memory log..."
            val reflection = repository.triggerDeepThoughtReflection()
            currentExpression.value = PetExpression.MYSTIC
            syncStatusMessage.value = "Deep thought reflection: $reflection"
            refreshLocalTextFileContent()
        }
    }

    fun triggerScheduledWorker() {
        viewModelScope.launch {
            syncStatusMessage.value = "Executing scheduled daily memory summarizer worker..."
            com.example.data.worker.MemorySummarizerScheduler.triggerImmediateMemorySummaryWork(getApplication())
            com.example.data.worker.GoogleDriveSyncScheduler.triggerImmediateDriveSync(getApplication())
            kotlinx.coroutines.delay(1000)
            refreshLocalTextFileContent()
        }
    }

    fun triggerBackgroundDriveSyncWorker() {
        viewModelScope.launch {
            syncStatusMessage.value = "Enqueuing background Google Drive Sync Worker service..."
            com.example.data.worker.GoogleDriveSyncScheduler.triggerImmediateDriveSync(getApplication())
            kotlinx.coroutines.delay(1000)
            syncStatusMessage.value = "Background Drive Sync Worker service executed!"
            refreshLocalTextFileContent()
        }
    }

    fun recordTelemetrySnapshot() {
        viewModelScope.launch {
            val collector = com.example.data.telemetry.PhoneTelemetryCollector(getApplication())
            repository.recordTelemetrySnapshot(collector)
        }
    }

    fun triggerMemoryCondensation() {
        viewModelScope.launch {
            syncStatusMessage.value = "Condensing & consolidating raw memories..."
            val resultMessage = repository.condenseAndConsolidateMemories()
            syncStatusMessage.value = resultMessage
            refreshLocalTextFileContent()
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || isGenerating.value) return

        val remaining = getRemainingCooldownSeconds()
        if (remaining > 0) {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            if (hour >= 22 || hour < 6) {
                currentExpression.value = PetExpression.SLEEPY
                syncStatusMessage.value = "🌙 *Companion is resting soundly... zzz (Cooldown active: ${remaining}s remaining)*"
            } else {
                syncStatusMessage.value = "⏳ *Companion is catching its breath... (Cooldown active: ${remaining}s remaining)*"
            }
            return
        }

        lastInteractionTimeMs = System.currentTimeMillis()

        viewModelScope.launch {
            isGenerating.value = true
            currentExpression.value = PetExpression.LISTENING

            try {
                val result = repository.sendMessageAndReceiveResponse(userText)

                // Update pet expression based on reaction
                val newExpr = try {
                    PetExpression.valueOf(result.expression.uppercase())
                } catch (e: Exception) {
                    PetExpression.MYSTIC
                }
                currentExpression.value = newExpr

                refreshLocalTextFileContent()
            } catch (e: Exception) {
                currentExpression.value = PetExpression.SLEEPY
            } finally {
                isGenerating.value = false
            }
        }
    }

    fun triggerAutonomousCuriosityScan() {
        viewModelScope.launch {
            currentExpression.value = PetExpression.THINKING
            syncStatusMessage.value = "Pet scanning surroundings with autonomous curiosity..."
            repository.generateAutonomousCuriosityObservation(getApplication())
            syncStatusMessage.value = "Pet generated a curious observation about its surroundings!"
            refreshLocalTextFileContent()
        }
    }

    fun triggerDailyReflectionPrompt() {
        viewModelScope.launch {
            currentExpression.value = PetExpression.LISTENING
            repository.triggerDailyReflectionPrompt(getApplication())
            refreshLocalTextFileContent()
        }
    }

    fun triggerDeepMemoryReflection() {
        viewModelScope.launch {
            repository.reanalyzeMemoriesAndEvolve()
            refreshLocalTextFileContent()
        }
    }

    fun selectExpression(expr: PetExpression) {
        currentExpression.value = expr
    }

    fun triggerDriveSync() {
        viewModelScope.launch {
            syncStatusMessage.value = "Syncing memories to Google Drive text log..."
            val result = repository.manualSyncToDrive()
            if (result.isSuccess) {
                syncStatusMessage.value = result.getOrNull() ?: "Synced to Google Drive .txt log!"
                refreshLocalTextFileContent()
                driveAuthState.value = repository.getDriveAuthState()
            } else {
                syncStatusMessage.value = "Sync failed: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun authenticateDrive(email: String = "dbetters37@gmail.com") {
        viewModelScope.launch {
            syncStatusMessage.value = "Authenticating with Google Drive ($email)..."
            val result = repository.authenticateGoogleDrive(email)
            if (result.isSuccess) {
                syncStatusMessage.value = result.getOrNull()
                driveAuthState.value = repository.getDriveAuthState()
                refreshLocalTextFileContent()
            } else {
                syncStatusMessage.value = "Authentication error: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun createDedicatedFolder() {
        viewModelScope.launch {
            syncStatusMessage.value = "Creating dedicated Google Drive folder..."
            val result = repository.createDedicatedDriveFolder()
            if (result.isSuccess) {
                val folder = result.getOrNull()
                syncStatusMessage.value = "Folder ready: ${folder?.folderName} (ID: ${folder?.folderId})"
                driveAuthState.value = repository.getDriveAuthState()
            } else {
                syncStatusMessage.value = "Folder creation failed: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun disconnectDrive() {
        repository.disconnectGoogleDrive()
        driveAuthState.value = repository.getDriveAuthState()
        syncStatusMessage.value = "Disconnected from Google Drive."
    }

    fun refreshLocalTextFileContent() {
        viewModelScope.launch {
            localTextLogContent.value = repository.getLocalTextLogFile()
        }
    }

    fun updatePetName(name: String) {
        viewModelScope.launch {
            repository.updatePetName(name)
            refreshLocalTextFileContent()
        }
    }

    fun updateArchetype(archetypeId: String) {
        viewModelScope.launch {
            repository.updateArchetype(archetypeId)
            refreshLocalTextFileContent()
        }
    }

    fun updateCustomDirectives(directives: String) {
        viewModelScope.launch {
            repository.updateCustomDirectives(directives)
            refreshLocalTextFileContent()
        }
    }

    fun updatePersonalityTraits(
        warmth: Float,
        openness: Float,
        mysticism: Float,
        playfulness: Float,
        energy: Float,
        humor: Float,
        empathy: Float,
        creativity: Float
    ) {
        viewModelScope.launch {
            repository.updatePersonalityTraits(
                warmth, openness, mysticism, playfulness, energy, humor, empathy, creativity
            )
            refreshLocalTextFileContent()
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
            refreshLocalTextFileContent()
        }
    }

    fun selectSkin(skinId: String) {
        viewModelScope.launch {
            repository.updateActiveSkin(skinId)
            refreshLocalTextFileContent()
        }
    }

    fun toggleAutoSkinShift(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAutoSkinShiftEnabled(enabled)
            refreshLocalTextFileContent()
        }
    }

    fun triggerRandomSkinShift() {
        viewModelScope.launch {
            currentExpression.value = PetExpression.HAPPY
            val newSkin = repository.maybeTriggerAutonomousSkinShift()
            if (newSkin != null) {
                syncStatusMessage.value = "Companion shifted skin to $newSkin!"
            } else {
                syncStatusMessage.value = "Autonomous skin shifts disabled."
            }
            refreshLocalTextFileContent()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    fun processTactileTouch(bodyZone: String) {
        viewModelScope.launch {
            currentExpression.value = when (bodyZone.uppercase()) {
                "HEAD", "HALO" -> PetExpression.HAPPY
                "HEART", "CHEST" -> PetExpression.MYSTIC
                "EYES", "FACE" -> PetExpression.PLAYFUL
                else -> PetExpression.HAPPY
            }
            val reaction = repository.processTactileTouch(bodyZone)
            syncStatusMessage.value = "Touched $bodyZone: $reaction"
            refreshLocalTextFileContent()
        }
    }

    fun triggerSpontaneousEpiphany() {
        viewModelScope.launch {
            isGenerating.value = true
            currentExpression.value = PetExpression.EVOLVING
            val epiphany = repository.triggerSpontaneousEpiphany()
            syncStatusMessage.value = "Epiphany sparked: $epiphany"
            isGenerating.value = false
            currentExpression.value = PetExpression.PROUD
            refreshLocalTextFileContent()
        }
    }

    fun recordPetMovement(xPx: Float, yPx: Float, screenWidthPx: Float, screenHeightPx: Float) {
        viewModelScope.launch {
            val updated = repository.recordPetMovement(xPx, yPx, screenWidthPx, screenHeightPx)
            syncStatusMessage.value = "Recorded movement: ${updated.lastZoneMovedTo} (Fav: ${updated.favoriteZone})"
            refreshLocalTextFileContent()
        }
    }

    fun executeSpatialCommand(command: String) {
        viewModelScope.launch {
            isGenerating.value = true
            currentExpression.value = PetExpression.PLAYFUL
            val state = repository.interpretAndApplySpatialCommand(command)
            currentSpatialState.value = state
            syncStatusMessage.value = "Spatial Command Executed: ${state.interpretedRationale}"
            isGenerating.value = false
            currentExpression.value = PetExpression.HAPPY
            refreshLocalTextFileContent()
        }
    }

    fun shiftToZone(zone: com.example.data.ai.ScreenZone) {
        executeSpatialCommand("move to ${zone.displayName}")
    }

    fun shiftRelative(deltaX: Float, deltaY: Float, description: String) {
        viewModelScope.launch {
            val cur = currentSpatialState.value
            val targetX = (cur.normalizedX + deltaX).coerceIn(0.12f, 0.88f)
            val targetY = (cur.normalizedY + deltaY).coerceIn(0.14f, 0.86f)
            executeSpatialCommand("shift to position $targetX $targetY $description")
        }
    }

    fun resetMovementBehavior() {
        viewModelScope.launch {
            repository.resetMovementBehavior()
            currentSpatialState.value = com.example.data.ai.SpatialPlacementState()
            syncStatusMessage.value = "Learned movement memory reset to sanctuary center."
            refreshLocalTextFileContent()
        }
    }

    fun generateDailyJournal(customTopic: String? = null) {
        viewModelScope.launch {
            isGenerating.value = true
            currentExpression.value = PetExpression.THINKING
            val entry = repository.generateAndSaveDailyJournal(customTopic)
            syncStatusMessage.value = "New Daily Journal generated: '${entry.title}'"
            isGenerating.value = false
            currentExpression.value = PetExpression.HAPPY
        }
    }

    fun generateDreamJournal(customTopic: String? = null) {
        viewModelScope.launch {
            isGenerating.value = true
            currentExpression.value = PetExpression.MYSTIC
            val dream = repository.generateAndSaveDreamJournal(customTopic)
            syncStatusMessage.value = "New Dream Journal generated: '${dream.dreamTitle}'"
            isGenerating.value = false
            currentExpression.value = PetExpression.SLEEPY
        }
    }

    fun addCustomDailyJournal(
        title: String,
        content: String,
        mood: String,
        vibe: String,
        keyTakeaway: String,
        gratitudeNote: String
    ) {
        viewModelScope.launch {
            repository.insertCustomDailyJournal(title, content, mood, vibe, keyTakeaway, gratitudeNote)
            syncStatusMessage.value = "Custom Daily Journal saved!"
        }
    }

    fun addCustomDreamJournal(
        dreamTitle: String,
        dreamContent: String,
        dreamSymbol: String,
        lucidityLevel: String,
        emotionalTone: String,
        wakingReflection: String
    ) {
        viewModelScope.launch {
            repository.insertCustomDreamJournal(
                dreamTitle, dreamContent, dreamSymbol, lucidityLevel, emotionalTone, wakingReflection
            )
            syncStatusMessage.value = "Custom Dream Journal saved!"
        }
    }

    fun deleteDailyJournal(id: Long) {
        viewModelScope.launch {
            repository.deleteDailyJournal(id)
            syncStatusMessage.value = "Daily Journal deleted."
        }
    }

    fun deleteDreamJournal(id: Long) {
        viewModelScope.launch {
            repository.deleteDreamJournal(id)
            syncStatusMessage.value = "Dream Journal deleted."
        }
    }

    fun logGoogleSearch(query: String, category: String, source: String = "Google Search", insights: String = "") {
        viewModelScope.launch {
            repository.logGoogleSearch(query, category, source, insights)
            syncStatusMessage.value = "Google Search activity logged to intelligence base."
        }
    }

    fun deleteSearchLog(id: Long) {
        viewModelScope.launch {
            repository.deleteSearchLog(id)
            syncStatusMessage.value = "Search record removed."
        }
    }

    fun clearSearchLogs() {
        viewModelScope.launch {
            repository.clearSearchLogs()
            syncStatusMessage.value = "Search history cleared."
        }
    }

    fun ensureTodayJournalsFilled() {
        viewModelScope.launch {
            repository.ensureDailyJournalsFilledForToday()
        }
    }

    fun seedSampleSearchIntelligence() {
        viewModelScope.launch {
            repository.seedSampleSearchIntelligence()
            syncStatusMessage.value = "Sample search intelligence topics seeded!"
        }
    }

    fun stimulateLobe(lobeId: String) {
        viewModelScope.launch {
            repository.stimulateBrainLobe(lobeId)
            syncStatusMessage.value = "Stimulated neural activity in $lobeId!"
        }
    }

    fun setLobeWeight(lobeId: String, weight: Float) {
        viewModelScope.launch {
            repository.setLobeInfluenceWeight(lobeId, weight)
        }
    }

    fun clearNeuralLogs() {
        viewModelScope.launch {
            repository.clearNeuralLogs()
            syncStatusMessage.value = "Neural thought logs cleared."
        }
    }

    fun resetPersonalityTracker() {
        viewModelScope.launch {
            repository.resetPersonalityTracker()
            syncStatusMessage.value = "Personality state tracker reset."
        }
    }

    fun triggerMemoryLoopCycle() {
        viewModelScope.launch {
            val loop = repository.triggerMemoryLoopCycle()
            syncStatusMessage.value = "Memory loop iterated: ${loop.activeRecallTopic}"
        }
    }

    fun introspectWorldModel() {
        viewModelScope.launch {
            val model = repository.introspectWorldModel()
            syncStatusMessage.value = "World Model evolved: ${model.worldModelEvolutionStage}"
        }
    }

    fun resetWorldModel() {
        viewModelScope.launch {
            repository.resetWorldModel()
            syncStatusMessage.value = "Subjective world model reset to harmonic defaults."
        }
    }

    fun advanceGoal(goalId: Long, delta: Int = 15) {
        viewModelScope.launch {
            repository.advanceGoalProgress(goalId, delta)
            syncStatusMessage.value = "Curiosity goal investigated (+${delta}% progress)"
        }
    }

    fun spawnCuriosityGoal() {
        viewModelScope.launch {
            val newGoal = repository.spawnCuriosityGoal()
            syncStatusMessage.value = "Formulated new curiosity quest: ${newGoal.goalTitle}"
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteAutonomousGoal(goalId)
            syncStatusMessage.value = "Curiosity goal removed."
        }
    }

    fun clearMemoryLoops() {
        viewModelScope.launch {
            repository.clearMemoryLoops()
            syncStatusMessage.value = "Persistent memory loop history cleared."
        }
    }

    fun askCuriosityInquiry(question: String) {
        viewModelScope.launch {
            sendMessage(question)
        }
    }

    fun refreshApiKeyStates() {
        isApiKeyLinkedState.value = geminiService.isApiKeyLinked()
        apiKeySourceState.value = geminiService.getApiKeySource()
        activeAiProviderState.value = "OPENAI"
        maskedApiKeyConfiguredState.value = geminiService.getMaskedApiKey()
    }

    fun saveCustomApiKey(key: String, onResult: ((Boolean, String) -> Unit)? = null) {
        val clean = key.trim()
        val prefs = getApplication<Application>().getSharedPreferences("pet_ai_preferences", android.content.Context.MODE_PRIVATE)

        if (clean.isBlank()) {
            prefs.edit().remove("user_openai_api_key").apply()
            com.example.data.ai.GeminiPetService.clearCustomOpenAiKey()
            refreshApiKeyStates()
            apiKeyValidationMessageState.value = "API key cleared."
            onResult?.invoke(true, "Key cleared")
            return
        }

        viewModelScope.launch {
            isTestingApiKeyState.value = true
            apiKeyValidationMessageState.value = "Verifying ChatGPT / OpenAI API key connection..."
            val (success, message) = geminiService.validateApiKeyConnection(clean)
            isTestingApiKeyState.value = false

            prefs.edit().putString("user_openai_api_key", clean).apply()
            com.example.data.ai.GeminiPetService.setCustomOpenAiKey(clean)
            refreshApiKeyStates()

            if (success) {
                apiKeyValidationMessageState.value = "✅ $message"
                currentExpression.value = PetExpression.HAPPY
                syncStatusMessage.value = "ChatGPT API Key linked successfully!"
                onResult?.invoke(true, message)
            } else {
                apiKeyValidationMessageState.value = "⚠️ Key saved, but validation returned: $message"
                syncStatusMessage.value = "Key saved (Verification issue: $message)"
                onResult?.invoke(false, message)
            }
        }
    }

    fun clearCustomApiKey() {
        val prefs = getApplication<Application>().getSharedPreferences("pet_ai_preferences", android.content.Context.MODE_PRIVATE)
        prefs.edit().remove("user_openai_api_key").apply()
        com.example.data.ai.GeminiPetService.clearCustomApiKey()
        refreshApiKeyStates()
        apiKeyValidationMessageState.value = "API key removed. Pet is using local offline companion engine."
        syncStatusMessage.value = "API key cleared."
    }

    fun testCurrentApiKey() {
        val currentKey = geminiService.getEffectiveApiKey()
        if (currentKey.isBlank()) {
            apiKeyValidationMessageState.value = "No API key configured. Please enter your ChatGPT (sk-...) API key."
            return
        }
        viewModelScope.launch {
            isTestingApiKeyState.value = true
            apiKeyValidationMessageState.value = "Testing connection with OpenAI ChatGPT..."
            val (success, message) = geminiService.validateApiKeyConnection(currentKey)
            isTestingApiKeyState.value = false
            apiKeyValidationMessageState.value = if (success) "✅ $message" else "❌ Validation error: $message"
        }
    }
}

