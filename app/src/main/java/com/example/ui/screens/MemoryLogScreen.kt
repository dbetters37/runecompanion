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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.DriveSyncLogEntity
import com.example.data.db.MemoryEntity
import com.example.data.db.PersonalityEntity
import com.example.data.db.PersonalityLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UnifiedMilestone(
    val id: String,
    val timestamp: Long,
    val title: String,
    val detail: String,
    val categoryTag: String,
    val milestoneType: Type,
    val contextSnippet: String = "",
    val memoryId: Long? = null
) {
    enum class Type {
        MEMORY, EVOLUTION, DRIVE_SYNC
    }
}

@Composable
fun MemoryLogScreen(
    memories: List<MemoryEntity>,
    personality: PersonalityEntity?,
    personalityLogs: List<PersonalityLogEntity>,
    syncLogs: List<DriveSyncLogEntity>,
    onDeleteMemory: (Long) -> Unit,
    onTriggerSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeViewMode by remember { mutableIntStateOf(0) } // 0: Visual Timeline, 1: Milestone Summary, 2: All Memories List
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "Preference", "Personal History", "Emotion", "Goal", "Fact")

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Combine Memories, Personality Logs, and Sync Logs into a unified timeline sorted descending
    val unifiedTimeline = remember(memories, personalityLogs, syncLogs) {
        val list = mutableListOf<UnifiedMilestone>()

        memories.forEach { m ->
            list.add(
                UnifiedMilestone(
                    id = "mem_${m.id}",
                    timestamp = m.timestamp,
                    title = "LEARNED FACT",
                    detail = m.keyFact,
                    categoryTag = m.category,
                    milestoneType = UnifiedMilestone.Type.MEMORY,
                    contextSnippet = m.contextSnippet,
                    memoryId = m.id
                )
            )
        }

        personalityLogs.forEach { p ->
            list.add(
                UnifiedMilestone(
                    id = "plog_${p.id}",
                    timestamp = p.timestamp,
                    title = "EVOLUTION MILESTONE",
                    detail = p.description,
                    categoryTag = p.eventType,
                    milestoneType = UnifiedMilestone.Type.EVOLUTION
                )
            )
        }

        syncLogs.forEach { s ->
            list.add(
                UnifiedMilestone(
                    id = "slog_${s.id}",
                    timestamp = s.timestamp,
                    title = "GOOGLE DRIVE SYNC",
                    detail = s.syncDetail,
                    categoryTag = "Cloud Backup",
                    milestoneType = UnifiedMilestone.Type.DRIVE_SYNC,
                    contextSnippet = s.fileName
                )
            )
        }

        list.sortedByDescending { it.timestamp }
    }

    val filteredTimeline = unifiedTimeline.filter { item ->
        val matchesCategory = selectedCategory == "ALL" || item.categoryTag.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                item.detail.contains(searchQuery, ignoreCase = true) ||
                item.categoryTag.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val filteredMemoriesOnly = memories.filter { item ->
        val matchesCategory = selectedCategory == "ALL" || item.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                item.keyFact.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07140B))
            .padding(16.dp)
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MEMORIES & VISUAL TIMELINE",
                    color = Color(0xFFFFD166),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${memories.size} Learned Facts • Auto-Synced to Google Drive .txt",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onTriggerSync,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = "Sync",
                    modifier = Modifier.size(15.dp),
                    tint = Color(0xFF00F5D4)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Sync Drive", fontSize = 11.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // View Mode Toggle Row (Timeline, Summary, List)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F2B1A))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val modes = listOf(
                Pair(0, "Visual Timeline"),
                Pair(1, "Milestone Summary"),
                Pair(2, "Memory List")
            )

            modes.forEach { (index, label) ->
                val isSelected = activeViewMode == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF2D6A4F) else Color.Transparent)
                        .clickable { activeViewMode = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFF00F5D4) else Color.Gray,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (activeViewMode) {
            // VIEW MODE 0: VISUAL TIMELINE
            0 -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item {
                        // Summary Badge Card at top of timeline
                        TimelineHeaderSummaryCard(
                            memoriesCount = memories.size,
                            personality = personality,
                            firstMemoryDate = memories.lastOrNull()?.timestamp?.let { dateFormat.format(Date(it)) } ?: "Today"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (filteredTimeline.isEmpty()) {
                        item {
                            EmptyStateNotice("No timeline milestones recorded yet. Chat with your pet to create memories!")
                        }
                    } else {
                        items(filteredTimeline, key = { it.id }) { item ->
                            TimelineNodeItem(
                                item = item,
                                onDeleteMemory = onDeleteMemory
                            )
                        }
                    }
                }
            }

            // VIEW MODE 1: MILESTONE SUMMARY HIGHLIGHTS
            1 -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        MilestoneHighlightCard(
                            title = "🌟 First Memory Recorded",
                            subtitle = memories.lastOrNull()?.keyFact ?: "No memories stored yet",
                            category = memories.lastOrNull()?.category ?: "Initial Awakening",
                            dateStr = memories.lastOrNull()?.timestamp?.let { dateFormat.format(Date(it)) } ?: "N/A",
                            accentColor = Color(0xFFFFD166)
                        )
                    }

                    item {
                        MilestoneHighlightCard(
                            title = "🧠 Dominant Mastered Topic",
                            subtitle = "Pet focuses on: '${personality?.dominantTopic ?: "General Wisdom"}'",
                            category = "Learned Interests: ${personality?.topInterests ?: "Cosmology"}",
                            dateStr = "Continuously Evolving",
                            accentColor = Color(0xFF00F5D4)
                        )
                    }

                    item {
                        MilestoneHighlightCard(
                            title = "🔮 Evolution Stage & Demeanor",
                            subtitle = "Stage: ${personality?.evolutionStage ?: "Wise Shaman"} • Demeanor: '${personality?.demeanor ?: "Comforting Sanctuary"}'",
                            category = "Conversational Tone: ${personality?.conversationalStyle ?: "Empathetic"}",
                            dateStr = "Level ${personality?.level ?: 1}",
                            accentColor = Color(0xFFFF70A6)
                        )
                    }

                    item {
                        MilestoneHighlightCard(
                            title = "☁️ Google Drive Backup Milestone",
                            subtitle = "Synced ${memories.size} total memories & trait metrics to Drive .txt file",
                            category = "Synced to: GoogleDrive/LifelongPet_${personality?.petName ?: "Aura"}_Memories.txt",
                            dateStr = syncLogs.firstOrNull()?.timestamp?.let { dateFormat.format(Date(it)) } ?: "Auto Sync Active",
                            accentColor = Color(0xFFC77DFF)
                        )
                    }
                }
            }

            // VIEW MODE 2: SEARCHABLE LIST
            2 -> {
                Column(modifier = Modifier.weight(1f)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search learned memories...", color = Color.Gray, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF00F5D4)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E0A38),
                            unfocusedContainerColor = Color(0xFF19082E),
                            focusedBorderColor = Color(0xFF00F5D4),
                            unfocusedBorderColor = Color(0xFF3B1566),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Filters
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 10.5.sp, color = if (isSelected) Color.Black else Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00F5D4),
                                    containerColor = Color(0xFF2B114D)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredMemoriesOnly.isEmpty()) {
                        EmptyStateNotice("No memories match search filter.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredMemoriesOnly, key = { it.id }) { memory ->
                                MemoryCard(memory = memory, onDelete = { onDeleteMemory(memory.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineHeaderSummaryCard(
    memoriesCount: Int,
    personality: PersonalityEntity?,
    firstMemoryDate: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFFD166), Color(0xFF00F5D4), Color(0xFF7209B7))
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        color = Color(0xFF1A0933)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Timeline",
                        tint = Color(0xFFFFD166),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "LIFELONG MEMORY TIMELINE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF35155D)
                ) {
                    Text(
                        text = "Synced Drive Text Active",
                        color = Color(0xFF00F5D4),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Total Memory Facts", color = Color.Gray, fontSize = 9.5.sp)
                    Text(text = "$memoriesCount Facts", color = Color(0xFFFFD166), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(text = "Primary Focus", color = Color.Gray, fontSize = 9.5.sp)
                    Text(text = personality?.dominantTopic ?: "General Wisdom", color = Color(0xFF00F5D4), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(text = "First Memory Date", color = Color.Gray, fontSize = 9.5.sp)
                    Text(text = firstMemoryDate, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TimelineNodeItem(
    item: UnifiedMilestone,
    onDeleteMemory: (Long) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val timeStr = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }

    val nodeColor = when (item.milestoneType) {
        UnifiedMilestone.Type.MEMORY -> Color(0xFF00F5D4)
        UnifiedMilestone.Type.EVOLUTION -> Color(0xFFFF70A6)
        UnifiedMilestone.Type.DRIVE_SYNC -> Color(0xFFFFD166)
    }

    val icon = when (item.milestoneType) {
        UnifiedMilestone.Type.MEMORY -> Icons.Default.Memory
        UnifiedMilestone.Type.EVOLUTION -> Icons.Default.AutoAwesome
        UnifiedMilestone.Type.DRIVE_SYNC -> Icons.Default.CloudDone
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Left Column: Node Icon and Vertical Connecting Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = nodeColor.copy(alpha = 0.2f),
                border = BorderStroke(1.5.dp, nodeColor),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = nodeColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(nodeColor.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Content Card
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .border(0.8.dp, nodeColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
            color = Color(0xFF1E0A38)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = nodeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.title,
                            color = nodeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timeStr,
                            color = Color.Gray,
                            fontSize = 9.5.sp
                        )
                        if (item.memoryId != null) {
                            IconButton(
                                onClick = { onDeleteMemory(item.memoryId) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Memory",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.detail,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 17.sp
                )

                if (item.contextSnippet.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "\"${item.contextSnippet}\"",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Drive Sync Badge
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Synced",
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "Synced to Google Drive .txt",
                        color = Color(0xFF00F5D4),
                        fontSize = 8.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MilestoneHighlightCard(
    title: String,
    subtitle: String,
    category: String,
    dateStr: String,
    accentColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        color = Color(0xFF1E0A38)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateStr,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF33145A)
            ) {
                Text(
                    text = category,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.5.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun MemoryCard(memory: MemoryEntity, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    val dateStr = remember(memory.timestamp) { dateFormat.format(Date(memory.timestamp)) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF381A66), RoundedCornerShape(16.dp)),
        color = Color(0xFF1E0A38)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF3D166E)
                ) {
                    Text(
                        text = memory.category.uppercase(),
                        color = Color(0xFF00F5D4),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = dateStr,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Memory",
                            tint = Color(0xFFFF70A6),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = memory.keyFact,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp
            )

            if (memory.contextSnippet.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${memory.contextSnippet}\"",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun EmptyStateNotice(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
