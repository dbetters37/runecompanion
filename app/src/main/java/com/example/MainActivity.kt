package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.PetViewModel
import com.example.ui.screens.BrainAndMindHubScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PersonalityScreen
import com.example.ui.screens.PetJournalScreen
import com.example.ui.screens.SkinsScreen
import com.example.ui.theme.MyApplicationTheme

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.example.data.service.PetOverlayService

class MainActivity : ComponentActivity() {

    private val viewModel: PetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                PetAppMainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure today's daily AI journals & dream journals are filled
        viewModel.ensureTodayJournalsFilled()

        // Check if user previously enabled overlay, and restore if killed by system
        val prefs = getSharedPreferences(PetOverlayService.PREFS_NAME, Context.MODE_PRIVATE)
        val isOverlayEnabled = prefs.getBoolean(PetOverlayService.KEY_OVERLAY_ENABLED, false)
        if (isOverlayEnabled && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this))) {
            if (!PetOverlayService.isOverlayRunning) {
                PetOverlayService.startOverlay(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetAppMainScreen(viewModel: PetViewModel) {
    val memories by viewModel.memoriesState.collectAsStateWithLifecycle()
    val conversations by viewModel.conversationsState.collectAsStateWithLifecycle()
    val personality by viewModel.personalityState.collectAsStateWithLifecycle()
    val personalityLogs by viewModel.personalityLogsState.collectAsStateWithLifecycle()
    val syncLogs by viewModel.syncLogsState.collectAsStateWithLifecycle()
    val telemetryLogs by viewModel.telemetryLogsState.collectAsStateWithLifecycle()
    val petOpinions by viewModel.petOpinionsState.collectAsStateWithLifecycle()
    val condensedMemories by viewModel.condensedMemoriesState.collectAsStateWithLifecycle()
    val dailyJournals by viewModel.dailyJournalsState.collectAsStateWithLifecycle()
    val dreamJournals by viewModel.dreamJournalsState.collectAsStateWithLifecycle()
    val currentExpression by viewModel.currentExpression.collectAsStateWithLifecycle()

    val personalityTracker by viewModel.personalityTrackerState.collectAsStateWithLifecycle()
    val brainLobeStates by viewModel.brainLobeStates.collectAsStateWithLifecycle()
    val brainNeuralLogs by viewModel.brainNeuralLogs.collectAsStateWithLifecycle()
    val worldModel by viewModel.worldModelState.collectAsStateWithLifecycle()
    val autonomousGoals by viewModel.autonomousGoalsState.collectAsStateWithLifecycle()
    val memoryLoops by viewModel.memoryLoopsState.collectAsStateWithLifecycle()

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val localTextLogContent by viewModel.localTextLogContent.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val syncStatusMessage by viewModel.syncStatusMessage.collectAsStateWithLifecycle()
    val driveAuthState by viewModel.driveAuthState.collectAsStateWithLifecycle()
    val cooldownRemaining by viewModel.cooldownRemainingState.collectAsStateWithLifecycle()
    val timeOfDayMode by viewModel.timeOfDayModeState.collectAsStateWithLifecycle()
    val movementBehavior by viewModel.movementBehaviorState.collectAsStateWithLifecycle()
    val spatialPlacementState by viewModel.currentSpatialState.collectAsStateWithLifecycle()
    val isApiKeyLinked by viewModel.isApiKeyLinkedState.collectAsStateWithLifecycle()
    val apiKeySource by viewModel.apiKeySourceState.collectAsStateWithLifecycle()
    val activeAiProvider by viewModel.activeAiProviderState.collectAsStateWithLifecycle()
    val maskedApiKey by viewModel.maskedApiKeyConfiguredState.collectAsStateWithLifecycle()
    val apiKeyValidationMessage by viewModel.apiKeyValidationMessageState.collectAsStateWithLifecycle()
    val isTestingApiKey by viewModel.isTestingApiKeyState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF071B12),
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Pet Stage
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectedTab.value = 0 },
                    icon = { Icon(Icons.Default.Pets, contentDescription = "Pet Stage") },
                    label = { Text("Pet Stage", fontSize = 10.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00F5D4),
                        selectedTextColor = Color(0xFF00F5D4),
                        indicatorColor = Color(0xFF1B4D36),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Tab 1: Personality
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectedTab.value = 1 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Personality") },
                    label = { Text("Personality", fontSize = 10.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00F5D4),
                        selectedTextColor = Color(0xFF00F5D4),
                        indicatorColor = Color(0xFF1B4D36),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Tab 2: Journal
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectedTab.value = 2 },
                    icon = { Icon(Icons.Default.Book, contentDescription = "Pet Journal") },
                    label = { Text("Journal", fontSize = 10.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00F5D4),
                        selectedTextColor = Color(0xFF00F5D4),
                        indicatorColor = Color(0xFF1B4D36),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Tab 3: Skins
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectedTab.value = 3 },
                    icon = { Icon(Icons.Default.Palette, contentDescription = "Skins") },
                    label = { Text("Skins", fontSize = 10.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00F5D4),
                        selectedTextColor = Color(0xFF00F5D4),
                        indicatorColor = Color(0xFF1B4D36),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Tab 4: Combined Brain & Mind Hub (All other tabs condensed into monitorable hub)
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.selectedTab.value = 4 },
                    icon = { Icon(Icons.Default.AccountTree, contentDescription = "Mind & Brain Hub") },
                    label = { Text("Mind Hub", fontSize = 10.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFFD166),
                        selectedTextColor = Color(0xFFFFD166),
                        indicatorColor = Color(0xFF1B4D36),
                        unselectedIconColor = Color(0xFFFFD166).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFFFFD166).copy(alpha = 0.6f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Active View
            when (selectedTab) {
                0 -> HomeScreen(
                    personality = personality,
                    conversations = conversations,
                    currentExpression = currentExpression,
                    isGenerating = isGenerating,
                    timeOfDayMode = timeOfDayMode,
                    cooldownRemaining = cooldownRemaining,
                    movementBehavior = movementBehavior,
                    spatialPlacementState = spatialPlacementState,
                    isApiKeyLinked = isApiKeyLinked,
                    apiKeySource = apiKeySource,
                    activeAiProvider = activeAiProvider,
                    maskedApiKey = maskedApiKey,
                    apiKeyValidationMessage = apiKeyValidationMessage,
                    isTestingApiKey = isTestingApiKey,
                    onSaveApiKey = { key, _, callback -> viewModel.saveCustomApiKey(key, callback) },
                    onClearApiKey = { _ -> viewModel.clearCustomApiKey() },
                    onTestApiKey = { _ -> viewModel.testCurrentApiKey() },
                    onSelectAiProvider = { },
                    onResetMovementBehavior = { viewModel.resetMovementBehavior() },
                    onExecuteSpatialCommand = { viewModel.executeSpatialCommand(it) },
                    onShiftToZone = { viewModel.shiftToZone(it) },
                    onShiftRelative = { dx, dy, desc -> viewModel.shiftRelative(dx, dy, desc) },
                    onSendMessage = { viewModel.sendMessage(it) },
                    onSelectExpression = { viewModel.selectExpression(it) },
                    onManualSync = { viewModel.triggerDriveSync() },
                    onTriggerDailyPrompt = { viewModel.triggerDailyReflectionPrompt() },
                    onTriggerAutonomousCuriosity = { viewModel.triggerAutonomousCuriosityScan() },
                    onTriggerEnvironmentPerception = { viewModel.triggerEnvironmentPerceptionScan() },
                    onTriggerDeepThought = { viewModel.triggerDeepThoughtReflection() },
                    onSparkEpiphany = { viewModel.triggerSpontaneousEpiphany() },
                    onTactileTouch = { viewModel.processTactileTouch(it) }
                )
                1 -> PersonalityScreen(
                    personality = personality,
                    personalityLogs = personalityLogs,
                    latestPetMessage = conversations.lastOrNull { it.sender == "SHAMAN" }?.message,
                    isGenerating = isGenerating,
                    onRenamePet = { viewModel.updatePetName(it) },
                    onSelectArchetype = { viewModel.updateArchetype(it) },
                    onUpdateDirectives = { viewModel.updateCustomDirectives(it) },
                    onUpdateTraits = { w, o, m, p, e, h, emp, c ->
                        viewModel.updatePersonalityTraits(w, o, m, p, e, h, emp, c)
                    },
                    onTriggerReflection = { viewModel.triggerDeepMemoryReflection() },
                    onTestCustomResponse = { viewModel.sendMessage(it) }
                )
                2 -> PetJournalScreen(
                    personality = personality,
                    dailyJournals = dailyJournals,
                    dreamJournals = dreamJournals,
                    isGenerating = isGenerating,
                    onGenerateDailyJournal = { viewModel.generateDailyJournal() },
                    onGenerateDreamJournal = { viewModel.generateDreamJournal() },
                    onAddCustomDailyJournal = { title, content, mood, vibe, takeaway, gratitude ->
                        viewModel.addCustomDailyJournal(title, content, mood, vibe, takeaway, gratitude)
                    },
                    onAddCustomDreamJournal = { title, content, symbol, lucidity, tone, reflection ->
                        viewModel.addCustomDreamJournal(title, content, symbol, lucidity, tone, reflection)
                    },
                    onDeleteDailyJournal = { viewModel.deleteDailyJournal(it) },
                    onDeleteDreamJournal = { viewModel.deleteDreamJournal(it) },
                    onEnsureTodayJournals = { viewModel.ensureTodayJournalsFilled() }
                )
                3 -> SkinsScreen(
                    personality = personality,
                    onSelectSkin = { viewModel.selectSkin(it) },
                    onToggleAutoShift = { viewModel.toggleAutoSkinShift(it) },
                    onTriggerRandomShift = { viewModel.triggerRandomSkinShift() }
                )
                4 -> BrainAndMindHubScreen(
                    personality = personality,
                    personalityTracker = personalityTracker,
                    brainLobeStates = brainLobeStates,
                    brainNeuralLogs = brainNeuralLogs,
                    worldModel = worldModel,
                    autonomousGoals = autonomousGoals,
                    memoryLoops = memoryLoops,
                    memories = memories,
                    petOpinions = petOpinions,
                    condensedMemories = condensedMemories,
                    telemetryLogs = telemetryLogs,
                    syncLogs = syncLogs,
                    localTextLogContent = localTextLogContent,
                    syncStatusMessage = syncStatusMessage,
                    driveAuthState = driveAuthState,
                    onStimulateLobe = { viewModel.stimulateLobe(it) },
                    onSetLobeWeight = { id, w -> viewModel.setLobeWeight(id, w) },
                    onClearNeuralLogs = { viewModel.clearNeuralLogs() },
                    onResetPersonalityTracker = { viewModel.resetPersonalityTracker() },
                    onTriggerMemoryLoop = { viewModel.triggerMemoryLoopCycle() },
                    onIntrospectWorldModel = { viewModel.introspectWorldModel() },
                    onResetWorldModel = { viewModel.resetWorldModel() },
                    onAdvanceGoal = { id, delta -> viewModel.advanceGoal(id, delta) },
                    onSpawnGoal = { viewModel.spawnCuriosityGoal() },
                    onDeleteGoal = { viewModel.deleteGoal(it) },
                    onClearMemoryLoops = { viewModel.clearMemoryLoops() },
                    onAskCuriosityInquiry = { viewModel.askCuriosityInquiry(it) },
                    onDeleteMemory = { viewModel.deleteMemory(it) },
                    onCondenseMemories = { viewModel.triggerMemoryCondensation() },
                    onRefreshTelemetry = { viewModel.recordTelemetrySnapshot() },
                    onAuthenticateDrive = { viewModel.authenticateDrive(it) },
                    onCreateFolder = { viewModel.createDedicatedFolder() },
                    onDisconnectDrive = { viewModel.disconnectDrive() },
                    onTriggerSync = { viewModel.triggerDriveSync() },
                    onRefreshTextLog = { viewModel.refreshLocalTextFileContent() }
                )
                else -> HomeScreen(
                    personality = personality,
                    conversations = conversations,
                    currentExpression = currentExpression,
                    isGenerating = isGenerating,
                    timeOfDayMode = timeOfDayMode,
                    cooldownRemaining = cooldownRemaining,
                    movementBehavior = movementBehavior,
                    spatialPlacementState = spatialPlacementState,
                    onResetMovementBehavior = { viewModel.resetMovementBehavior() },
                    onExecuteSpatialCommand = { viewModel.executeSpatialCommand(it) },
                    onShiftToZone = { viewModel.shiftToZone(it) },
                    onShiftRelative = { dx, dy, desc -> viewModel.shiftRelative(dx, dy, desc) },
                    onSendMessage = { viewModel.sendMessage(it) },
                    onSelectExpression = { viewModel.selectExpression(it) },
                    onManualSync = { viewModel.triggerDriveSync() },
                    onTriggerDailyPrompt = { viewModel.triggerDailyReflectionPrompt() },
                    onTriggerAutonomousCuriosity = { viewModel.triggerAutonomousCuriosityScan() },
                    onTriggerEnvironmentPerception = { viewModel.triggerEnvironmentPerceptionScan() },
                    onTriggerDeepThought = { viewModel.triggerDeepThoughtReflection() },
                    onSparkEpiphany = { viewModel.triggerSpontaneousEpiphany() },
                    onTactileTouch = { viewModel.processTactileTouch(it) }
                )
            }
        }
    }
}
