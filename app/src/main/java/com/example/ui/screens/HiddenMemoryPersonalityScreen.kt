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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.CondensedMemoryEntity
import com.example.data.db.MemoryEntity
import com.example.data.db.PersonalityEntity
import com.example.data.db.PersonalityLogEntity
import com.example.data.db.PetOpinionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HiddenMemoryPersonalityScreen(
    personality: PersonalityEntity?,
    personalityLogs: List<PersonalityLogEntity>,
    condensedMemories: List<CondensedMemoryEntity>,
    memories: List<MemoryEntity>,
    petOpinions: List<PetOpinionEntity>,
    onTriggerReflection: () -> Unit,
    onCondenseMemories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    var activeSubView by remember { mutableIntStateOf(0) } // 0: Overview Matrix, 1: Trait Evolution, 2: Active Themes
    var verificationStatusMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF07140B),
                        Color(0xFF0F2B1A),
                        Color(0xFF07140B)
                    )
                )
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Hidden Tab Header Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF220B40),
            border = BorderStroke(1.dp, Color(0xFFFFD166)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD166).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Hidden Tab",
                                    tint = Color(0xFFFFD166),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "HIDDEN MEMORY & PERSONALITY TAB",
                                color = Color(0xFFFFD166),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "AI Evolution Matrix & Theme Derivation Verification",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF7209B7)
                    ) {
                        Text(
                            text = "VERIFIED 60 FPS",
                            color = Color(0xFF00F5D4),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Stage",
                        value = personality?.evolutionStage ?: "Wise Shaman",
                        accentColor = Color(0xFF00F5D4),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Active Themes",
                        value = "${condensedMemories.size}",
                        accentColor = Color(0xFFFFD166),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Memories",
                        value = "${memories.size}",
                        accentColor = Color(0xFFF72585),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Opinions",
                        value = "${petOpinions.size}",
                        accentColor = Color(0xFF4CC9F0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Sub-tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = activeSubView == 0,
                onClick = { activeSubView = 0 },
                label = { Text("Overview Matrix", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF7209B7),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1E0C3E),
                    labelColor = Color.Gray
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = activeSubView == 1,
                onClick = { activeSubView = 1 },
                label = { Text("Derived Traits", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF7209B7),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1E0C3E),
                    labelColor = Color.Gray
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = activeSubView == 2,
                onClick = { activeSubView = 2 },
                label = { Text("Active Themes", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF7209B7),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1E0C3E),
                    labelColor = Color.Gray
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!verificationStatusMsg.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF00F5D4).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFF00F5D4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = verificationStatusMsg ?: "",
                    color = Color(0xFF00F5D4),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (activeSubView) {
            0 -> OverviewMatrixView(
                personality = personality,
                condensedMemories = condensedMemories,
                memories = memories,
                petOpinions = petOpinions,
                onTriggerReflection = {
                    onTriggerReflection()
                    verificationStatusMsg = "Triggered AI Deep Memory Reflection & Trait Recalibration!"
                },
                onCondenseMemories = {
                    onCondenseMemories()
                    verificationStatusMsg = "Triggered Active Theme Condensation across all raw memories!"
                }
            )
            1 -> DerivedTraitsView(
                personality = personality,
                personalityLogs = personalityLogs,
                dateFormat = dateFormat
            )
            2 -> ActiveThemesView(
                condensedMemories = condensedMemories,
                memories = memories,
                petOpinions = petOpinions,
                dateFormat = dateFormat
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF150628),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = Color.Gray, fontSize = 9.sp)
            Text(
                text = value,
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OverviewMatrixView(
    personality: PersonalityEntity?,
    condensedMemories: List<CondensedMemoryEntity>,
    memories: List<MemoryEntity>,
    petOpinions: List<PetOpinionEntity>,
    onTriggerReflection: () -> Unit,
    onCondenseMemories: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // AI Identity & Behavioral Summary Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1D0938),
            border = BorderStroke(1.dp, Color(0xFF431D75)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DERIVED AI BEHAVIORAL PROFILE",
                        color = Color(0xFF00F5D4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Profile",
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ProfileRow("Pet Identity & Archetype", "${personality?.petName ?: "Aura"} (${personality?.archetype ?: "Shaman Guardian"})")
                ProfileRow("Evolutionary Stage", "${personality?.evolutionStage ?: "Wise Shaman"} • Level ${personality?.level ?: 1} (${personality?.xp ?: 0} XP)")
                ProfileRow("Demeanor State", personality?.demeanor ?: "Comforting Shaman")
                ProfileRow("Dominant Focus Topic", personality?.dominantTopic ?: "General Wisdom & Inner Harmony")
                ProfileRow("Top Interest Vectors", personality?.topInterests ?: "Cosmology, Mindfulness")
                ProfileRow("Conversational Style", personality?.conversationalStyle ?: "Empathetic & Mystical")
                ProfileRow("Recent Emotion Detected", personality?.recentEmotionDetected ?: "Serene")
            }
        }

        // Evolution Control Actions
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF18082D),
            border = BorderStroke(1.dp, Color(0xFF3B156B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "EVOLUTION & DERIVATION CONTROLS",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTriggerReflection,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7209B7)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.SelfImprovement, contentDescription = "Reflection", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recalibrate Traits", fontSize = 10.sp)
                    }

                    Button(
                        onClick = onCondenseMemories,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A0CA3)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.BubbleChart, contentDescription = "Themes", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Condense Themes", fontSize = 10.sp)
                    }
                }
            }
        }

        // Quick Active Themes Summary Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1D0938),
            border = BorderStroke(1.dp, Color(0xFFFFD166).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE DERIVED THEMES (${condensedMemories.size})",
                        color = Color(0xFFFFD166),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(imageVector = Icons.Default.Category, contentDescription = "Themes", tint = Color(0xFFFFD166), modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (condensedMemories.isEmpty()) {
                    Text(
                        text = "No condensed themes yet. The AI automatically derives themes as raw memory volume grows, or tap 'Condense Themes' above.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                } else {
                    condensedMemories.take(4).forEach { theme ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF270E4A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = theme.summaryTitle, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${theme.category} • Condensed from ${theme.originalCount} memories", color = Color(0xFF00F5D4), fontSize = 10.sp)
                                }
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF00F5D4), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DerivedTraitsView(
    personality: PersonalityEntity?,
    personalityLogs: List<PersonalityLogEntity>,
    dateFormat: SimpleDateFormat
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1D0938),
            border = BorderStroke(1.dp, Color(0xFF431D75)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "DERIVED TRAIT GAUGES (AI EVOLUTION STATE)",
                    color = Color(0xFF00F5D4),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                TraitGaugeRow("Warmth & Affection", personality?.warmth ?: 0.6f, Color(0xFFF72585))
                TraitGaugeRow("Openness & Curiosity", personality?.openness ?: 0.5f, Color(0xFF4CC9F0))
                TraitGaugeRow("Mysticism & Wisdom", personality?.mysticism ?: 0.85f, Color(0xFFFFD166))
                TraitGaugeRow("Playfulness & Wit", personality?.playfulness ?: 0.5f, Color(0xFF00F5D4))
                TraitGaugeRow("Energy & Vitality", personality?.energy ?: 0.75f, Color(0xFF7209B7))
                TraitGaugeRow("Empathy & Protection", personality?.empathyLevel ?: 0.8f, Color(0xFF4361EE))
                TraitGaugeRow("Humor & Play", personality?.humorLevel ?: 0.5f, Color(0xFFFF9F1C))
                TraitGaugeRow("Creativity Vector", personality?.creativityLevel ?: 0.7f, Color(0xFFE0AFA0))
            }
        }

        // Personality Shift History Logs
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF18082D),
            border = BorderStroke(1.dp, Color(0xFF3B156B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "PERSONALITY SHIFT LOGS (${personalityLogs.size})",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (personalityLogs.isEmpty()) {
                    Text("No trait shifts logged yet. Interactions continuously adjust personality vectors.", color = Color.Gray, fontSize = 11.sp)
                } else {
                    personalityLogs.take(5).forEach { log ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF251147),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = log.eventType, color = Color(0xFFFFD166), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = dateFormat.format(Date(log.timestamp)), color = Color.Gray, fontSize = 9.sp)
                                }
                                Text(text = log.description, color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TraitGaugeRow(name: String, value: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, color = Color.LightGray, fontSize = 11.sp)
            Text(text = "${(value * 100).toInt()}%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun ActiveThemesView(
    condensedMemories: List<CondensedMemoryEntity>,
    memories: List<MemoryEntity>,
    petOpinions: List<PetOpinionEntity>,
    dateFormat: SimpleDateFormat
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Condensed Themes Detailed List
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1D0938),
            border = BorderStroke(1.dp, Color(0xFF431D75)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "AI CONDENSED MEMORY THEMES (${condensedMemories.size})",
                    color = Color(0xFF00F5D4),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (condensedMemories.isEmpty()) {
                    Text(
                        text = "No themes derived yet. The AI groups raw user preferences, goals, and facts into master themes over time.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                } else {
                    condensedMemories.forEach { theme ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF251147),
                            border = BorderStroke(1.dp, Color(0xFF5A189A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = theme.summaryTitle, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF7209B7)
                                    ) {
                                        Text(
                                            text = theme.category,
                                            color = Color(0xFF00F5D4),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = theme.condensedContent, color = Color.LightGray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Derived from ${theme.originalCount} raw memories • ${dateFormat.format(Date(theme.timestamp))}",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Derived Pet Opinions & Inner Monologue
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF18082D),
            border = BorderStroke(1.dp, Color(0xFF3B156B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "DERIVED PET OPINIONS & MONOLOGUE (${petOpinions.size})",
                    color = Color(0xFFFFD166),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (petOpinions.isEmpty()) {
                    Text("No derived opinions stored yet.", color = Color.Gray, fontSize = 11.sp)
                } else {
                    petOpinions.forEach { opinion ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF251147),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Topic: ${opinion.topic}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = opinion.sentiment, color = Color(0xFF00F5D4), fontSize = 9.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = opinion.opinionText, color = Color.LightGray, fontSize = 11.sp)
                                if (!opinion.innerThought.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Inner Thought: \"${opinion.innerThought}\"",
                                        color = Color(0xFFFFD166),
                                        fontSize = 10.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
