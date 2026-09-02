package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.CondensedMemoryEntity
import com.example.data.db.ContextTelemetryEntity
import com.example.data.db.PetOpinionEntity
import com.example.data.db.PersonalityEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetMindAndContextScreen(
    personality: PersonalityEntity?,
    telemetryList: List<ContextTelemetryEntity>,
    opinionsList: List<PetOpinionEntity>,
    condensedMemoriesList: List<CondensedMemoryEntity>,
    rawMemoriesCount: Int,
    onRefreshTelemetry: () -> Unit,
    onCondenseMemories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val petName = personality?.petName ?: "Aura"
    val latestTelemetry = telemetryList.firstOrNull()
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    var selectedSection by remember { mutableIntStateOf(0) } // 0: Opinions & Thoughts, 1: Phone Telemetry, 2: Memory Condensation

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "$petName's Mind & Context",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Live Phone Sensing • Subjective Opinions • Memory Condensation",
                            color = Color(0xFF00F5D4),
                            fontSize = 11.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0E2417)
                ),
                actions = {
                    IconButton(onClick = onRefreshTelemetry) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Refresh Sensing",
                            tint = Color(0xFF00F5D4)
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF07140B)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    label = { Text("Likes & Fears", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(12.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF381A66),
                        selectedLabelColor = Color(0xFF00F5D4)
                    )
                )

                FilterChip(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    label = { Text("Opinions (${opinionsList.size})", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(12.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF381A66),
                        selectedLabelColor = Color(0xFF00F5D4)
                    )
                )

                FilterChip(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    label = { Text("Sensing", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(12.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF381A66),
                        selectedLabelColor = Color(0xFF00F5D4)
                    )
                )

                FilterChip(
                    selected = selectedSection == 3,
                    onClick = { selectedSection = 3 },
                    label = { Text("Condense", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.Compress, contentDescription = null, modifier = Modifier.size(12.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF381A66),
                        selectedLabelColor = Color(0xFF00F5D4)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedSection) {
                0 -> PetLikesDislikesFearsSection(
                    personality = personality,
                    opinionsList = opinionsList
                )
                1 -> PetOpinionsSection(
                    petName = petName,
                    opinions = opinionsList,
                    personality = personality
                )
                2 -> PhoneTelemetrySection(
                    latestTelemetry = latestTelemetry,
                    telemetryList = telemetryList,
                    onRefreshTelemetry = onRefreshTelemetry,
                    dateFormat = dateFormat
                )
                3 -> MemoryCondensationSection(
                    petName = petName,
                    rawCount = rawMemoriesCount,
                    condensedList = condensedMemoriesList,
                    onCondenseMemories = onCondenseMemories,
                    dateFormat = dateFormat
                )
            }
        }
    }
}

@Composable
private fun PetOpinionsSection(
    petName: String,
    opinions: List<PetOpinionEntity>,
    personality: PersonalityEntity?
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF163B25)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2D6A4F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD166)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$petName's Subjective Mind State",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Demeanor: ${personality?.demeanor ?: "Wise Shaman"} • ${personality?.conversationalStyle ?: "Empathetic"}",
                                color = Color(0xFFB388FF),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$petName formulates real subjective thoughts and personal opinions based on your phone telemetry, chats, and lifelong memory patterns.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (opinions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No opinions formed yet. Chat with $petName or refresh phone sensing to generate new thoughts!",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(opinions, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D0C54)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.topic,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F5D4),
                                fontSize = 14.sp
                            )

                            val badgeBg = when (item.sentiment) {
                                "PROTECTIVE" -> Color(0xFFE63946)
                                "ADMIRING" -> Color(0xFF7000FF)
                                "AMUSED" -> Color(0xFFFFB703)
                                else -> Color(0xFF06D6A0)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.sentiment,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.opinionText,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        if (item.innerThought.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1A0033))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FormatQuote,
                                        contentDescription = "Inner Monologue",
                                        tint = Color(0xFFFFD166),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Inner Monologue: \"${item.innerThought}\"",
                                        color = Color(0xFFFFD166),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
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

@Composable
private fun PhoneTelemetrySection(
    latestTelemetry: ContextTelemetryEntity?,
    telemetryList: List<ContextTelemetryEntity>,
    onRefreshTelemetry: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF230047)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Device Telemetry",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )

                        Button(
                            onClick = onRefreshTelemetry,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381A66)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sensors", fontSize = 11.sp, color = Color(0xFF00F5D4))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (latestTelemetry != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TelemetryMetricCard(
                                title = "Battery",
                                value = "${latestTelemetry.batteryLevel}%",
                                subtitle = if (latestTelemetry.isCharging) "Charging ⚡" else "On Battery",
                                icon = Icons.Default.BatteryChargingFull,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TelemetryMetricCard(
                                title = "Ambient Light",
                                value = "${latestTelemetry.ambientLightLux.toInt()} lux",
                                subtitle = latestTelemetry.lightLevelCategory.take(16),
                                icon = Icons.Default.LightMode,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TelemetryMetricCard(
                                title = "Motion & Context",
                                value = if (latestTelemetry.motionState.contains("Motion")) "Moving 🚶" else "Resting 🧘",
                                subtitle = latestTelemetry.locationContext.take(16),
                                icon = Icons.Default.DirectionsWalk,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF381A66))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Perception of Time:",
                                        color = Color(0xFF00F5D4),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = latestTelemetry.perceptionOfTime,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Observation: ${latestTelemetry.ambientContextSummary}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        Text("Tap button above to collect initial phone telemetry snapshot.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text(
                text = "Telemetry History Log (${telemetryList.size})",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
        }

        items(telemetryList, key = { it.id }) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0038)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = log.ambientContextSummary,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = dateFormat.format(Date(log.timestamp)),
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D0C54)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Text(text = subtitle, color = Color.Gray, fontSize = 9.5.sp)
        }
    }
}

@Composable
private fun MemoryCondensationSection(
    petName: String,
    rawCount: Int,
    condensedList: List<CondensedMemoryEntity>,
    onCondenseMemories: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF230047)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Memory Condensation Engine",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "To prevent database bloating while preserving lifelong knowledge, $petName condenses redundant raw memories once a day into distilled core wisdom archives and backs them up to Google Drive.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Uncondensed Raw Facts: $rawCount",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD166),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Condensed Core Archives: ${condensedList.size}",
                                color = Color(0xFF00F5D4),
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onCondenseMemories,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7000FF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Compress, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Condense Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Condensed Core Memory Archives (${condensedList.size})",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
        }

        if (condensedList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No condensed archives yet. Tap 'Condense Now' above to consolidate memories!",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(condensedList, key = { it.id }) { condensed ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D0C54)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = condensed.summaryTitle,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F5D4),
                                fontSize = 13.sp
                            )
                            Text(
                                text = dateFormat.format(Date(condensed.timestamp)),
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = condensed.condensedContent,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PetLikesDislikesFearsSection(
    personality: PersonalityEntity?,
    opinionsList: List<PetOpinionEntity>
) {
    val preferences = remember(personality, opinionsList) {
        com.example.data.ai.PersonalityEngine.getDynamicPetPreferences(
            personality = personality,
            currentSkin = personality?.activeSkin ?: "SHAMAN_DEFAULT",
            opinions = opinionsList
        )
    }

    var subFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Likes, 2: Dislikes, 3: Fears

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Consciousness Stream & Epiphany Spotlight Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF280B3A)),
                border = BorderStroke(1.dp, Color(0xFFE040FB)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4A148C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD166),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "🧠 Unfiltered Consciousness & Epiphanies",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Companion Vibe Resonance Alignment: ${personality?.vibeResonanceScore ?: 98}%",
                                color = Color(0xFF00F5D4),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "INNER MONOLOGUE STREAM:",
                        color = Color(0xFFE040FB),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "\"${personality?.latestInnerMonologue ?: "Observing human companion warmly... sensing gentle ambient light and steady focus."}\"",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "LAST SPONTANEOUS EPIPHANY:",
                        color = Color(0xFFFFD166),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = personality?.lastEpiphany ?: "Everything in the universe flows in rhythmic balance, like breath and starlight.",
                        color = Color(0xFFFFD166).copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF230047)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF381A66)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFFF70A6)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${preferences.petName}'s Likes, Dislikes & Fears",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${preferences.archetypeTitle} • Active Skin: ${personality?.activeSkin ?: "Shaman Spirit"}",
                                color = Color(0xFF00F5D4),
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "This psychological profile evolves dynamically as ${preferences.petName} learns, levels up, changes archetypes, or shifts skins!",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sub filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = subFilter == 0,
                            onClick = { subFilter = 0 },
                            label = { Text("All (${preferences.likes.size + preferences.dislikes.size + preferences.fears.size})", fontSize = 9.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4A148C), selectedLabelColor = Color.White)
                        )
                        FilterChip(
                            selected = subFilter == 1,
                            onClick = { subFilter = 1 },
                            label = { Text("❤️ Likes (${preferences.likes.size})", fontSize = 9.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF1B4332), selectedLabelColor = Color(0xFF00F5D4))
                        )
                        FilterChip(
                            selected = subFilter == 2,
                            onClick = { subFilter = 2 },
                            label = { Text("💔 Dislikes (${preferences.dislikes.size})", fontSize = 9.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4A2800), selectedLabelColor = Color(0xFFFFB703))
                        )
                        FilterChip(
                            selected = subFilter == 3,
                            onClick = { subFilter = 3 },
                            label = { Text("😱 Fears (${preferences.fears.size})", fontSize = 9.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4A0E17), selectedLabelColor = Color(0xFFFF4D6D))
                        )
                    }
                }
            }
        }

        val displayItems = when (subFilter) {
            1 -> preferences.likes
            2 -> preferences.dislikes
            3 -> preferences.fears
            else -> preferences.likes + preferences.dislikes + preferences.fears
        }

        items(displayItems) { item ->
            val cardBg = when (item.category) {
                "LIKE" -> Color(0xFF0A3222)
                "DISLIKE" -> Color(0xFF381C02)
                else -> Color(0xFF3B0B14)
            }
            val borderClr = when (item.category) {
                "LIKE" -> Color(0xFF00F5D4)
                "DISLIKE" -> Color(0xFFFFB703)
                else -> Color(0xFFFF4D6D)
            }
            val categoryBadgeText = when (item.category) {
                "LIKE" -> "❤️ LIKES"
                "DISLIKE" -> "💔 DISLIKES"
                else -> "😱 FEARS"
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(0.8.dp, borderClr.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = borderClr.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, borderClr)
                        ) {
                            Text(
                                text = categoryBadgeText,
                                color = borderClr,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress / Affinity indicator bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { item.affinityScore },
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = borderClr,
                            trackColor = Color.Black.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "${(item.affinityScore * 100).toInt()}% Intensity",
                            color = borderClr,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Source: ${item.sourceTrigger}",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
