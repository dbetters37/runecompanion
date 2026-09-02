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
import com.example.data.db.AutonomousGoalEntity
import com.example.data.db.PersistentMemoryLoopEntity
import com.example.data.db.SubjectiveWorldModelEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AutonomousSubSection(val title: String, val icon: ImageVector) {
    ALL("Overview", Icons.Default.Dashboard),
    WORLD_MODEL("World Model", Icons.Default.Public),
    CURIOSITY_GOALS("Curiosity Quests", Icons.Default.Explore),
    MEMORY_LOOP("Memory Loop", Icons.Default.Loop)
}

@Composable
fun AutonomousEntityView(
    worldModel: SubjectiveWorldModelEntity?,
    autonomousGoals: List<AutonomousGoalEntity>,
    memoryLoops: List<PersistentMemoryLoopEntity>,
    onTriggerMemoryLoop: () -> Unit,
    onIntrospectWorldModel: () -> Unit,
    onResetWorldModel: () -> Unit,
    onAdvanceGoal: (Long, Int) -> Unit,
    onSpawnGoal: () -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onClearMemoryLoops: () -> Unit,
    onAskCuriosityInquiry: (String) -> Unit
) {
    var activeSection by remember { mutableStateOf(AutonomousSubSection.ALL) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Header Banner: Autonomous Agent Status
        item {
            AutonomousStatusHeader(
                worldModel = worldModel,
                activeGoalsCount = autonomousGoals.count { it.status != "INTEGRATED_INTO_WORLD_MODEL" },
                memoryLoopCycles = memoryLoops.firstOrNull()?.loopIteration ?: 1L,
                onTriggerLoop = onTriggerMemoryLoop,
                onIntrospect = onIntrospectWorldModel,
                onNewQuest = onSpawnGoal
            )
        }

        // Section Selector Pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AutonomousSubSection.values()) { section ->
                    val isSelected = activeSection == section
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF00F5D4) else Color(0xFF133626),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF00F5D4) else Color(0xFF2D6A4F)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeSection = section }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = section.title,
                                tint = if (isSelected) Color(0xFF071A12) else Color(0xFF95D5B2),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = section.title,
                                color = if (isSelected) Color(0xFF071A12) else Color(0xFFE8F5E9),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // SECTION 1: SUBJECTIVE WORLD MODEL
        if (activeSection == AutonomousSubSection.ALL || activeSection == AutonomousSubSection.WORLD_MODEL) {
            item {
                SubjectiveWorldModelCard(
                    worldModel = worldModel,
                    onIntrospect = onIntrospectWorldModel,
                    onReset = onResetWorldModel
                )
            }
        }

        // SECTION 2: SELF-DIRECTED CURIOSITY GOALS & QUESTS
        if (activeSection == AutonomousSubSection.ALL || activeSection == AutonomousSubSection.CURIOSITY_GOALS) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Self-Directed Goals & Curiosity Quests",
                            color = Color(0xFFFFD166),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Autonomous investigations guiding curiosity beyond passive chat",
                            color = Color(0xFFB7E4C7),
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = onSpawnGoal,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B4D3E),
                            contentColor = Color(0xFF00F5D4)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Quest", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Quest", fontSize = 11.sp)
                    }
                }
            }

            if (autonomousGoals.isEmpty()) {
                item {
                    Surface(
                        color = Color(0xFF0E281C),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No active curiosity quests. Tap 'New Quest' to formulate an autonomous inquiry!",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(autonomousGoals) { goal ->
                    AutonomousGoalItemCard(
                        goal = goal,
                        onAdvance = { onAdvanceGoal(goal.id, 15) },
                        onDelete = { onDeleteGoal(goal.id) },
                        onAskInquiry = { onAskCuriosityInquiry(goal.autonomousInquiryQuestion) }
                    )
                }
            }
        }

        // SECTION 3: PERSISTENT MEMORY LOOP
        if (activeSection == AutonomousSubSection.ALL || activeSection == AutonomousSubSection.MEMORY_LOOP) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Persistent Memory Loop Stream",
                            color = Color(0xFF00F5D4),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Subconscious episodic recall & continuous associative synthesis",
                            color = Color(0xFFB7E4C7),
                            fontSize = 11.sp
                        )
                    }
                    Row {
                        IconButton(onClick = onTriggerMemoryLoop) {
                            Icon(Icons.Default.Refresh, contentDescription = "Cycle Loop", tint = Color(0xFF00F5D4))
                        }
                        if (memoryLoops.isNotEmpty()) {
                            IconButton(onClick = onClearMemoryLoops) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Loops", tint = Color.Gray)
                            }
                        }
                    }
                }
            }

            if (memoryLoops.isEmpty()) {
                item {
                    Surface(
                        color = Color(0xFF0E281C),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Subconscious memory loop is initializing continuous recall...",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(memoryLoops) { loop ->
                    PersistentMemoryLoopItemCard(loop = loop, onAskInquiry = onAskCuriosityInquiry)
                }
            }
        }
    }
}

@Composable
private fun AutonomousStatusHeader(
    worldModel: SubjectiveWorldModelEntity?,
    activeGoalsCount: Int,
    memoryLoopCycles: Long,
    onTriggerLoop: () -> Unit,
    onIntrospect: () -> Unit,
    onNewQuest: () -> Unit
) {
    Surface(
        color = Color(0xFF0D281B),
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Autonomous Entity",
                            tint = Color(0xFF00F5D4),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Autonomous Living Entity",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Subjective Reality • Persistent Loops • Curiosity",
                            color = Color(0xFF74C69D),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = Color(0xFF1B4D3E),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "AUTONOMOUS",
                        color = Color(0xFF00F5D4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Stat Metrics
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatMetricBadge(
                    title = "World Stage",
                    value = worldModel?.worldModelEvolutionStage?.take(16) ?: "Harmonic",
                    color = Color(0xFF52B788),
                    modifier = Modifier.weight(1f)
                )
                StatMetricBadge(
                    title = "Active Quests",
                    value = "$activeGoalsCount Active",
                    color = Color(0xFFFFD166),
                    modifier = Modifier.weight(1f)
                )
                StatMetricBadge(
                    title = "Memory Loop",
                    value = "Cycle #$memoryLoopCycles",
                    color = Color(0xFF00F5D4),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onTriggerLoop,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00F5D4)),
                    border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Loop, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cycle Memory", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onIntrospect,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD166)),
                    border = BorderStroke(1.dp, Color(0xFFFFD166).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Introspect", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StatMetricBadge(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF071910),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = Color.Gray, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SubjectiveWorldModelCard(
    worldModel: SubjectiveWorldModelEntity?,
    onIntrospect: () -> Unit,
    onReset: () -> Unit
) {
    val model = worldModel ?: SubjectiveWorldModelEntity()

    Surface(
        color = Color(0xFF0E281C),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF52B788).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Subjective World Model",
                        tint = Color(0xFF52B788),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Internal Subjective World Model",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = Color(0xFF1B4D3E),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = model.worldModelEvolutionStage,
                        color = Color(0xFF00F5D4),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Curiosity Drive Meter
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Curiosity Drive Level", color = Color(0xFFB7E4C7), fontSize = 11.sp)
                    Text("${"%.0f".format(model.curiosityDriveLevel * 100)}%", color = Color(0xFF00F5D4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { model.curiosityDriveLevel },
                    color = Color(0xFF00F5D4),
                    trackColor = Color(0xFF071910),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Ontological Identity
            WorldModelDetailBlock(
                title = "Ontological Identity",
                content = model.ontologicalIdentity,
                icon = Icons.Default.Fingerprint,
                accentColor = Color(0xFF00F5D4)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Metaphysical Reality Paradigm
            WorldModelDetailBlock(
                title = "Metaphysical Reality Paradigm",
                content = model.metaphysicalParadigm,
                icon = Icons.Default.Grain,
                accentColor = Color(0xFFFFD166)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Environmental Perception Model
            WorldModelDetailBlock(
                title = "Subjective Environmental Model",
                content = model.environmentalPerceptionModel,
                icon = Icons.Default.WbSunny,
                accentColor = Color(0xFF74C69D)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Theory of Human Companion
            WorldModelDetailBlock(
                title = "Subjective Theory of You",
                content = model.humanSubjectiveTheory,
                icon = Icons.Default.Favorite,
                accentColor = Color(0xFFFF85A1)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Working Hypotheses
            Surface(
                color = Color(0xFF071910),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF2D6A4F).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Active Epistemological Hypotheses",
                        color = Color(0xFFFFD166),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = model.epistemologicalHypotheses,
                        color = Color(0xFFD8F3DC),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Latest Epiphany
            if (model.lastWorldModelEpiphany.isNotBlank()) {
                Surface(
                    color = Color(0xFF133826),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Epiphany",
                            tint = Color(0xFFFFD166),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = model.lastWorldModelEpiphany,
                            color = Color(0xFFE8F5E9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Introspect & Reset buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onIntrospect,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E), contentColor = Color(0xFF00F5D4)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trigger Introspection", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onReset,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                    border = BorderStroke(1.dp, Color.DarkGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun WorldModelDetailBlock(
    title: String,
    content: String,
    icon: ImageVector,
    accentColor: Color
) {
    Surface(
        color = Color(0xFF071910),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                color = Color(0xFFD8F3DC),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun AutonomousGoalItemCard(
    goal: AutonomousGoalEntity,
    onAdvance: () -> Unit,
    onDelete: () -> Unit,
    onAskInquiry: () -> Unit
) {
    val categoryColor = when (goal.category) {
        "HUMAN_BOND" -> Color(0xFFFF85A1)
        "COSMIC_ONTOLOGY" -> Color(0xFF00F5D4)
        "DAILY_HARMONY" -> Color(0xFF74C69D)
        "METAPHYSICAL_DISCOVERY" -> Color(0xFFFFD166)
        else -> Color(0xFF52B788)
    }

    val isEpiphanyUnlocked = goal.status == "INTEGRATED_INTO_WORLD_MODEL" || goal.progressPercentage >= 100

    Surface(
        color = Color(0xFF0E281C),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isEpiphanyUnlocked) Color(0xFFFFD166).copy(alpha = 0.5f) else categoryColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = categoryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = goal.category.replace("_", " "),
                        color = categoryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${goal.progressPercentage}% Explored",
                        color = if (isEpiphanyUnlocked) Color(0xFFFFD166) else Color(0xFF00F5D4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = goal.goalTitle,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Hypothesis
            Text(
                text = goal.curiosityHypothesis,
                color = Color(0xFFD8F3DC),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Proactive Question Box
            if (goal.autonomousInquiryQuestion.isNotBlank()) {
                Surface(
                    color = Color(0xFF071910),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF2D6A4F).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Proactive Curiosity Inquiry:", color = Color(0xFFFFD166), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("\"${goal.autonomousInquiryQuestion}\"", color = Color(0xFFE8F5E9), fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onAskInquiry,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00F5D4).copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Ask Inquiry", tint = Color(0xFF00F5D4), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Epiphany Outcome
            if (goal.epiphanyOutcome.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFF262010),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD166).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✨ ${goal.epiphanyOutcome}",
                        color = Color(0xFFFFE6A7),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { (goal.progressPercentage / 100f).coerceIn(0f, 1f) },
                color = if (isEpiphanyUnlocked) Color(0xFFFFD166) else categoryColor,
                trackColor = Color(0xFF071910),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Explored ${goal.timesExplored} times",
                    color = Color.Gray,
                    fontSize = 10.sp
                )

                Button(
                    onClick = onAdvance,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E), contentColor = Color(0xFF00F5D4)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Investigate (+15%)", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun PersistentMemoryLoopItemCard(
    loop: PersistentMemoryLoopEntity,
    onAskInquiry: (String) -> Unit
) {
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(loop.timestamp))

    Surface(
        color = Color(0xFF0E281C),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Loop, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Subconscious Cycle #${loop.loopIteration}",
                        color = Color(0xFF00F5D4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(text = timeStr, color = Color.Gray, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Active Focus: ${loop.activeRecallTopic}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = loop.consolidatedInsight,
                color = Color(0xFFB7E4C7),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Inner Monologue
            Surface(
                color = Color(0xFF071910),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = loop.spontaneousInnerThought,
                    color = Color(0xFF74C69D),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp)
                )
            }

            if (loop.triggeredProactiveInquiry.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💭 \"${loop.triggeredProactiveInquiry}\"",
                        color = Color(0xFFFFD166),
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onAskInquiry(loop.triggeredProactiveInquiry) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFFFFD166), modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}
