package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.brain.BrainLobeType
import com.example.data.db.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MindHubSubTab(val title: String, val icon: ImageVector) {
    BRAIN_ENGINES("Brain Engines", Icons.Default.Psychology),
    AUTONOMOUS_ENTITY("Autonomous Entity", Icons.Default.AutoAwesome),
    PERSONALITY_TRACKER("Personality State", Icons.Default.Tune),
    MEMORY_VAULT("Memories & Opinions", Icons.Default.Memory),
    DRIVE_BACKUP("Drive & Sync", Icons.Default.CloudSync),
    TELEMETRY("Telemetry & Sensors", Icons.Default.Sensors)
}

@Composable
fun BrainAndMindHubScreen(
    personality: PersonalityEntity?,
    personalityTracker: PersonalityStateTrackerEntity?,
    brainLobeStates: List<BrainLobeStateEntity>,
    brainNeuralLogs: List<BrainNeuralLogEntity>,
    worldModel: SubjectiveWorldModelEntity?,
    autonomousGoals: List<AutonomousGoalEntity>,
    memoryLoops: List<PersistentMemoryLoopEntity>,
    memories: List<MemoryEntity>,
    petOpinions: List<PetOpinionEntity>,
    condensedMemories: List<CondensedMemoryEntity>,
    telemetryLogs: List<ContextTelemetryEntity>,
    syncLogs: List<DriveSyncLogEntity>,
    localTextLogContent: String,
    syncStatusMessage: String?,
    driveAuthState: com.example.data.drive.DriveAuthState,
    onStimulateLobe: (String) -> Unit,
    onSetLobeWeight: (String, Float) -> Unit,
    onClearNeuralLogs: () -> Unit,
    onResetPersonalityTracker: () -> Unit,
    onTriggerMemoryLoop: () -> Unit,
    onIntrospectWorldModel: () -> Unit,
    onResetWorldModel: () -> Unit,
    onAdvanceGoal: (Long, Int) -> Unit,
    onSpawnGoal: () -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onClearMemoryLoops: () -> Unit,
    onAskCuriosityInquiry: (String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onCondenseMemories: () -> Unit,
    onRefreshTelemetry: () -> Unit,
    onAuthenticateDrive: (String) -> Unit,
    onCreateFolder: () -> Unit,
    onDisconnectDrive: () -> Unit,
    onTriggerSync: () -> Unit,
    onRefreshTextLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(MindHubSubTab.BRAIN_ENGINES) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF07130E),
                        Color(0xFF0D2319),
                        Color(0xFF040B07)
                    )
                )
            )
    ) {
        // Header
        Surface(
            color = Color(0xFF0E281C).copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00F5D4).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF00F5D4), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = "Mind & Brain Hub",
                                tint = Color(0xFF00F5D4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cognitive Mind & Hub",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "7-Lobe Neural Architecture & Companion State",
                                color = Color(0xFF70E000),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Status Pill
                    Surface(
                        color = Color(0xFF1B4D3E),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "7 Lobes Active",
                            color = Color(0xFF00F5D4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Segmented Horizontal Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MindHubSubTab.values()) { tab ->
                        val isSelected = activeSubTab == tab
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFF00F5D4) else Color(0xFF133626),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF00F5D4) else Color(0xFF2D6A4F)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { activeSubTab = tab }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) Color(0xFF071A12) else Color(0xFF95D5B2),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) Color(0xFF071A12) else Color(0xFFE8F5E9),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sub-Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                MindHubSubTab.BRAIN_ENGINES -> BrainEnginesView(
                    brainLobeStates = brainLobeStates,
                    neuralLogs = brainNeuralLogs,
                    onStimulateLobe = onStimulateLobe,
                    onSetLobeWeight = onSetLobeWeight,
                    onClearNeuralLogs = onClearNeuralLogs
                )
                MindHubSubTab.AUTONOMOUS_ENTITY -> AutonomousEntityView(
                    worldModel = worldModel,
                    autonomousGoals = autonomousGoals,
                    memoryLoops = memoryLoops,
                    onTriggerMemoryLoop = onTriggerMemoryLoop,
                    onIntrospectWorldModel = onIntrospectWorldModel,
                    onResetWorldModel = onResetWorldModel,
                    onAdvanceGoal = onAdvanceGoal,
                    onSpawnGoal = onSpawnGoal,
                    onDeleteGoal = onDeleteGoal,
                    onClearMemoryLoops = onClearMemoryLoops,
                    onAskCuriosityInquiry = onAskCuriosityInquiry
                )
                MindHubSubTab.PERSONALITY_TRACKER -> PersonalityTrackerView(
                    personality = personality,
                    tracker = personalityTracker,
                    onResetTracker = onResetPersonalityTracker
                )
                MindHubSubTab.MEMORY_VAULT -> MemoriesAndOpinionsView(
                    memories = memories,
                    opinions = petOpinions,
                    condensedMemories = condensedMemories,
                    onDeleteMemory = onDeleteMemory,
                    onCondenseMemories = onCondenseMemories
                )
                MindHubSubTab.DRIVE_BACKUP -> DriveBackupView(
                    localTextLogContent = localTextLogContent,
                    syncLogs = syncLogs,
                    syncStatusMessage = syncStatusMessage,
                    driveAuthState = driveAuthState,
                    onAuthenticateDrive = onAuthenticateDrive,
                    onCreateFolder = onCreateFolder,
                    onDisconnectDrive = onDisconnectDrive,
                    onTriggerSync = onTriggerSync,
                    onRefreshTextLog = onRefreshTextLog
                )
                MindHubSubTab.TELEMETRY -> TelemetryView(
                    telemetryLogs = telemetryLogs,
                    onRefreshTelemetry = onRefreshTelemetry
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 1. BRAIN ENGINES VIEW (All 7 Lobes with real-time modulations)
// -------------------------------------------------------------
@Composable
private fun BrainEnginesView(
    brainLobeStates: List<BrainLobeStateEntity>,
    neuralLogs: List<BrainNeuralLogEntity>,
    onStimulateLobe: (String) -> Unit,
    onSetLobeWeight: (String, Float) -> Unit,
    onClearNeuralLogs: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // High-level summary banner
        item {
            BrainNeuralSummaryBanner(brainLobeStates = brainLobeStates)
        }

        // Section header for Lobes
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "7-Lobe Cognitive Engines",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pre-Response Synthesis",
                    color = Color(0xFF00F5D4),
                    fontSize = 11.sp
                )
            }
        }

        // Render all 7 lobes
        val allLobeTypes = BrainLobeType.values()
        items(allLobeTypes) { lobeType ->
            val entity = brainLobeStates.find { it.lobeId == lobeType.id }
            BrainLobeCard(
                lobeType = lobeType,
                entity = entity,
                onStimulate = { onStimulateLobe(lobeType.id) },
                onWeightChange = { onSetLobeWeight(lobeType.id, it) }
            )
        }

        // Neural Thought Stream Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Neural Log",
                        tint = Color(0xFF70E000),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Neural Firing & Thought Stream",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (neuralLogs.isNotEmpty()) {
                    TextButton(onClick = onClearNeuralLogs) {
                        Text("Clear", color = Color(0xFFFF6B6B), fontSize = 11.sp)
                    }
                }
            }
        }

        if (neuralLogs.isEmpty()) {
            item {
                Surface(
                    color = Color(0xFF0B1F16),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E4D37)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No neural thoughts logged yet. Talk with your pet to see real-time 7-lobe modulations fire before each reply!",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(neuralLogs.take(20)) { log ->
                NeuralLogItemCard(log = log)
            }
        }
    }
}

@Composable
private fun BrainNeuralSummaryBanner(brainLobeStates: List<BrainLobeStateEntity>) {
    val totalActivity = if (brainLobeStates.isNotEmpty()) {
        brainLobeStates.map { it.activityLevel }.average().toFloat()
    } else 0.72f

    val highestLobe = brainLobeStates.maxByOrNull { it.activityLevel }?.displayName ?: "Frontal Lobe"

    Surface(
        color = Color(0xFF0F2D1F),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "AI Cognitive Matrix",
                        color = Color(0xFF95D5B2),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Real-Time 7-Lobe Integration",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = Color(0xFF00F5D4).copy(alpha = 0.15f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFF00F5D4))
                ) {
                    Text(
                        text = "${(totalActivity * 100).toInt()}% Arousal",
                        color = Color(0xFF00F5D4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { totalActivity.coerceIn(0f, 1f) },
                color = Color(0xFF00F5D4),
                trackColor = Color(0xFF1E4D37),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "⚡ Primary Firing Center: $highestLobe\nEvery user message executes a cognitive pass across all 7 engines to personalize emotion, logic, gestures, and memory synthesis.",
                color = Color(0xFFD8F3DC),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun BrainLobeCard(
    lobeType: BrainLobeType,
    entity: BrainLobeStateEntity?,
    onStimulate: () -> Unit,
    onWeightChange: (Float) -> Unit
) {
    val activity = entity?.activityLevel ?: 0.65f
    val weight = entity?.influenceWeight ?: 1.0f
    val timesFired = entity?.totalModificationsApplied ?: 0
    val lastThought = entity?.currentThought ?: "Standing by for cognitive synthesis..."
    val firingHz = entity?.neuralFiringHz ?: lobeType.baseHz

    Surface(
        color = Color(0xFF0D2418),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF2D6A4F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = lobeType.badgeEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = lobeType.displayName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = lobeType.anatomicalRole,
                            color = Color(0xFF70E000),
                            fontSize = 10.sp
                        )
                    }
                }

                // Firing count
                Surface(
                    color = Color(0xFF163E2B),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$timesFired mods",
                        color = Color(0xFF95D5B2),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Activity level bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Neural Firing (${"%.1f".format(firingHz)} Hz)",
                    color = Color(0xFF95D5B2),
                    fontSize = 11.sp
                )
                Text(
                    text = "${(activity * 100).toInt()}%",
                    color = Color(0xFF00F5D4),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { activity.coerceIn(0f, 1f) },
                color = Color(0xFF00F5D4),
                trackColor = Color(0xFF133826),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cognitive Directive Snippet
            Surface(
                color = Color(0xFF081810),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF1B4D36)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "COGNITIVE SYNTHESIS THOUGHT STREAM:",
                        color = Color(0xFFFFD166),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = lastThought,
                        color = Color(0xFFD8F3DC),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        maxLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Controls: Stimulate button & Weight
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onStimulate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B4D36),
                        contentColor = Color(0xFF00F5D4)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Stimulate",
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("⚡ Stimulate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Weight: ${"%.1f".format(weight)}x",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun NeuralLogItemCard(log: BrainNeuralLogEntity) {
    val dateStr = try {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    } catch (e: Exception) { "" }

    Surface(
        color = Color(0xFF091C13),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF1E4D37)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🧠 ${log.eventTitle}",
                    color = Color(0xFF00F5D4),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$dateStr • ${(log.activityLevel * 100).toInt()}% (${"%.1f".format(log.firingRate)} Hz)",
                    color = Color(0xFF70E000),
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.cognitiveSynthesisDetail,
                color = Color(0xFFD8F3DC),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

// -------------------------------------------------------------
// 2. PERSONALITY STATE TRACKER VIEW
// -------------------------------------------------------------
@Composable
private fun PersonalityTrackerView(
    personality: PersonalityEntity?,
    tracker: PersonalityStateTrackerEntity?,
    onResetTracker: () -> Unit
) {
    val totalInteractions = tracker?.totalInteractions ?: 12
    val frequency = tracker?.dailyInteractionFrequency ?: "High Intimacy Daily Flow"
    val streak = tracker?.interactionStreakDays ?: 1
    val intimacy = tracker?.intimacyScore ?: 88
    val dominantSentiment = tracker?.dominantUserSentiment ?: "Warm & Inquisitive"
    val dialogueStyle = tracker?.evolvedDialogueStyle ?: "Gentle Sage & Warm Intimacy"

    val warmthMult = tracker?.warmthMultiplier ?: 1.25f
    val empathyMult = tracker?.empathyDepth ?: 1.30f
    val intellectMult = tracker?.intellectualNuance ?: 1.20f
    val whimsyMult = tracker?.whimsyLevel ?: 1.10f

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                color = Color(0xFF0F2D1F),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Personality Evolution Tracker",
                                color = Color(0xFF95D5B2),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = dialogueStyle,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = Color(0xFF00F5D4).copy(alpha = 0.15f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color(0xFF00F5D4))
                        ) {
                            Text(
                                text = "$intimacy/100 Intimacy",
                                color = Color(0xFF00F5D4),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (intimacy / 100f).coerceIn(0f, 1f) },
                        color = Color(0xFF00F5D4),
                        trackColor = Color(0xFF1E4D37),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tracks interaction frequency, user sentiment balance, and multi-day streaks to continuously evolve your companion's dialogue style, tone, and empathy depth.",
                        color = Color(0xFFD8F3DC),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Metrics Grid
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    title = "Interactions",
                    value = "$totalInteractions",
                    subtitle = frequency,
                    icon = Icons.Default.ChatBubble,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Streak Active",
                    value = "$streak days",
                    subtitle = "Daily Connection",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Surface(
                color = Color(0xFF0D2418),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF2D6A4F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Dominant Sentiment Received",
                        color = Color(0xFF95D5B2),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = dominantSentiment,
                        color = Color(0xFFFFD166),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Calculated from recent conversational turns and sentiment markers.",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Multipliers Card
        item {
            Surface(
                color = Color(0xFF0D2418),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF2D6A4F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Evolved Dialogue Multipliers",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    MultiplierRow("Warmth Multiplier", warmthMult, Color(0xFFFF758F))
                    MultiplierRow("Empathy Depth", empathyMult, Color(0xFF00F5D4))
                    MultiplierRow("Intellectual Nuance", intellectMult, Color(0xFF70E000))
                    MultiplierRow("Whimsical Playfulness", whimsyMult, Color(0xFFFFD166))
                }
            }
        }

        // Reset Tracker Button
        item {
            OutlinedButton(
                onClick = onResetTracker,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
                border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset",
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Personality State Tracker", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0D2418),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF2D6A4F)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, color = Color(0xFF95D5B2), fontSize = 11.sp)
                Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF00F5D4), modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
private fun MultiplierRow(label: String, mult: Float, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = label, color = Color(0xFFD8F3DC), fontSize = 12.sp)
        Text(
            text = "${"%.2f".format(mult)}x",
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// -------------------------------------------------------------
// 3. MEMORIES & OPINIONS CONDENSED VIEW
// -------------------------------------------------------------
@Composable
private fun MemoriesAndOpinionsView(
    memories: List<MemoryEntity>,
    opinions: List<PetOpinionEntity>,
    condensedMemories: List<CondensedMemoryEntity>,
    onDeleteMemory: (Long) -> Unit,
    onCondenseMemories: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                color = Color(0xFF0F2D1F),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF2D6A4F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Column {
                        Text(text = "Memory & Opinion Vault", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${memories.size} raw memories • ${opinions.size} pet opinions", color = Color(0xFF95D5B2), fontSize = 11.sp)
                    }
                    Button(
                        onClick = onCondenseMemories,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D36), contentColor = Color(0xFF00F5D4)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Compress, contentDescription = "Condense", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Condense", fontSize = 11.sp)
                    }
                }
            }
        }

        // Pet Inner Opinions
        if (opinions.isNotEmpty()) {
            item {
                Text(text = "Pet's Inner Opinions", color = Color(0xFFFFD166), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            items(opinions) { op ->
                Surface(
                    color = Color(0xFF0D2418),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E4D37)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Topic: ${op.topic}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = op.sentiment, color = Color(0xFF70E000), fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = op.opinionText, color = Color(0xFFD8F3DC), fontSize = 11.sp)
                    }
                }
            }
        }

        // Raw Memories
        item {
            Text(text = "Extracted Fact Memories (${memories.size})", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        if (memories.isEmpty()) {
            item {
                Surface(
                    color = Color(0xFF0B1F16),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No memories extracted yet. Chat with your pet and share details about yourself to start filling the vault!",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
            items(memories) { mem ->
                Surface(
                    color = Color(0xFF0D2418),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF2D6A4F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "[${mem.category}]", color = Color(0xFF00F5D4), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = mem.keyFact, color = Color.White, fontSize = 12.sp)
                        }
                        IconButton(onClick = { onDeleteMemory(mem.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF6B6B), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. DRIVE SYNC & BACKUP CONDENSED VIEW
// -------------------------------------------------------------
@Composable
private fun DriveBackupView(
    localTextLogContent: String,
    syncLogs: List<DriveSyncLogEntity>,
    syncStatusMessage: String?,
    driveAuthState: com.example.data.drive.DriveAuthState,
    onAuthenticateDrive: (String) -> Unit,
    onCreateFolder: () -> Unit,
    onDisconnectDrive: () -> Unit,
    onTriggerSync: () -> Unit,
    onRefreshTextLog: () -> Unit
) {
    DriveSyncScreen(
        localTextLogContent = localTextLogContent,
        syncLogs = syncLogs,
        syncStatusMessage = syncStatusMessage,
        driveAuthState = driveAuthState,
        onAuthenticateDrive = onAuthenticateDrive,
        onCreateFolder = onCreateFolder,
        onDisconnectDrive = onDisconnectDrive,
        onTriggerSync = onTriggerSync,
        onRefreshTextLog = onRefreshTextLog
    )
}

// -------------------------------------------------------------
// 5. TELEMETRY & SENSORS CONDENSED VIEW
// -------------------------------------------------------------
@Composable
private fun TelemetryView(
    telemetryLogs: List<ContextTelemetryEntity>,
    onRefreshTelemetry: () -> Unit
) {
    val latest = telemetryLogs.firstOrNull()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                color = Color(0xFF0F2D1F),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF2D6A4F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Environment & Sensor Status", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onRefreshTelemetry) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF00F5D4))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (latest != null) {
                        Text(text = "• Ambient Light: ${latest.ambientLightLux} lux (${latest.lightLevelCategory})", color = Color(0xFFD8F3DC), fontSize = 12.sp)
                        Text(text = "• Time Perception: ${latest.perceptionOfTime}", color = Color(0xFFD8F3DC), fontSize = 12.sp)
                        Text(text = "• Motion State: ${latest.motionState}", color = Color(0xFFD8F3DC), fontSize = 12.sp)
                        Text(text = "• Battery: ${latest.batteryLevel}% (Charging: ${latest.isCharging})", color = Color(0xFFD8F3DC), fontSize = 12.sp)
                        Text(text = "• Network: ${latest.networkType}", color = Color(0xFFD8F3DC), fontSize = 12.sp)
                    } else {
                        Text(text = "No telemetry recorded yet.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
