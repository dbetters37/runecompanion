package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PersonalityEntity
import com.example.data.db.PetDailyJournalEntity
import com.example.data.db.PetDreamJournalEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetJournalScreen(
    personality: PersonalityEntity?,
    dailyJournals: List<PetDailyJournalEntity>,
    dreamJournals: List<PetDreamJournalEntity>,
    isGenerating: Boolean,
    onGenerateDailyJournal: () -> Unit,
    onGenerateDreamJournal: () -> Unit,
    onAddCustomDailyJournal: (title: String, content: String, mood: String, vibe: String, takeaway: String, gratitude: String) -> Unit,
    onAddCustomDreamJournal: (title: String, content: String, symbol: String, lucidity: String, tone: String, reflection: String) -> Unit,
    onDeleteDailyJournal: (id: Long) -> Unit,
    onDeleteDreamJournal: (id: Long) -> Unit,
    onEnsureTodayJournals: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Daily Journal, 1: Dream Journal
    var searchQuery by remember { mutableStateOf("") }

    var showAddDailyDialog by remember { mutableStateOf(false) }
    var showAddDreamDialog by remember { mutableStateOf(false) }

    val petName = personality?.petName ?: "Aura"

    LaunchedEffect(Unit) {
        onEnsureTodayJournals()
    }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val hasTodayDailyEntry = remember(dailyJournals) {
        dailyJournals.any { it.dateStr == todayStr }
    }
    val hasTodayDreamEntry = remember(dreamJournals) {
        dreamJournals.any { it.dateStr == todayStr }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07140B))
            .padding(16.dp)
    ) {
        // Header Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2417)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D6A4F))
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
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF52B788), Color(0xFF163B25))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.MenuBook else Icons.Default.NightsStay,
                                contentDescription = "Journal Header Icon",
                                tint = Color(0xFF00F5D4),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$petName's Journals & Dreams",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Daily Reflections & Subconscious Visions",
                                fontSize = 12.sp,
                                color = Color(0xFF52B788)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF163B25),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D6A4F))
                    ) {
                        Text(
                            text = "${personality?.evolutionStage ?: "Wise Shaman"}",
                            color = Color(0xFF00F5D4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    JournalStatChip(
                        icon = Icons.Default.Book,
                        label = "Daily Entries",
                        value = "${dailyJournals.size}",
                        accentColor = Color(0xFF52B788)
                    )
                    JournalStatChip(
                        icon = Icons.Default.Bedtime,
                        label = "Dream Logs",
                        value = "${dreamJournals.size}",
                        accentColor = Color(0xFF00F5D4)
                    )
                    JournalStatChip(
                        icon = Icons.Default.AutoAwesome,
                        label = "AI Synced",
                        value = "${dailyJournals.count { it.aiGenerated } + dreamJournals.count { it.aiGenerated }}",
                        accentColor = Color(0xFFFFD166)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dual View Segmented Toggle Bar
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0E2417),
            contentColor = Color(0xFF00F5D4),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF00F5D4),
                    height = 3.dp
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFF2D6A4F), RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Daily Journal (${dailyJournals.size})", fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = Color(0xFF00F5D4),
                unselectedContentColor = Color.Gray
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.NightsStay,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dream Journal (${dreamJournals.size})", fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = Color(0xFF00F5D4),
                unselectedContentColor = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (selectedTab == 0) onGenerateDailyJournal() else onGenerateDreamJournal()
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFF00F5D4),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating...", color = Color.White, fontSize = 13.sp)
                } else {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedTab == 0) "Generate Journal" else "Record Dream",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    if (selectedTab == 0) showAddDailyDialog = true else showAddDreamDialog = true
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF52B788)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF52B788)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.EditNote,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Write Entry", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = if (selectedTab == 0) "Search daily entries..." else "Search dream logs...",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF163B25),
                unfocusedContainerColor = Color(0xFF163B25),
                focusedBorderColor = Color(0xFF00F5D4),
                unfocusedBorderColor = Color(0xFF2D6A4F),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // List Content
        if (selectedTab == 0) {
            val filteredDaily = dailyJournals.filter {
                searchQuery.isBlank() ||
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true) ||
                        it.mood.contains(searchQuery, ignoreCase = true) ||
                        it.vibe.contains(searchQuery, ignoreCase = true)
            }

            if (filteredDaily.isEmpty()) {
                EmptyJournalPlaceholder(
                    icon = Icons.Default.Book,
                    message = if (searchQuery.isNotBlank()) "No daily entries matching '$searchQuery'" else "No daily journal entries yet!\nTap 'Generate Journal' or 'Write Entry' to create $petName's first daily log."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredDaily, key = { it.id }) { journal ->
                        DailyJournalCard(
                            journal = journal,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString("${journal.title}\n\n${journal.content}"))
                                Toast.makeText(context, "Journal entry copied!", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { onDeleteDailyJournal(journal.id) }
                        )
                    }
                }
            }
        } else {
            val filteredDreams = dreamJournals.filter {
                searchQuery.isBlank() ||
                        it.dreamTitle.contains(searchQuery, ignoreCase = true) ||
                        it.dreamContent.contains(searchQuery, ignoreCase = true) ||
                        it.dreamSymbol.contains(searchQuery, ignoreCase = true) ||
                        it.emotionalTone.contains(searchQuery, ignoreCase = true)
            }

            if (filteredDreams.isEmpty()) {
                EmptyJournalPlaceholder(
                    icon = Icons.Default.NightsStay,
                    message = if (searchQuery.isNotBlank()) "No dream entries matching '$searchQuery'" else "No dream logs yet!\nTap 'Record Dream' or 'Write Entry' to capture $petName's subconscious nocturnal visions."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredDreams, key = { it.id }) { dream ->
                        DreamJournalCard(
                            dream = dream,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString("${dream.dreamTitle}\n\n${dream.dreamContent}"))
                                Toast.makeText(context, "Dream log copied!", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { onDeleteDreamJournal(dream.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Custom Daily Journal Dialog
    if (showAddDailyDialog) {
        AddDailyJournalDialog(
            petName = petName,
            onDismiss = { showAddDailyDialog = false },
            onSave = { title, content, mood, vibe, takeaway, gratitude ->
                onAddCustomDailyJournal(title, content, mood, vibe, takeaway, gratitude)
                showAddDailyDialog = false
            }
        )
    }

    // Add Custom Dream Journal Dialog
    if (showAddDreamDialog) {
        AddDreamJournalDialog(
            petName = petName,
            onDismiss = { showAddDreamDialog = false },
            onSave = { title, content, symbol, lucidity, tone, reflection ->
                onAddCustomDreamJournal(title, content, symbol, lucidity, tone, reflection)
                showAddDreamDialog = false
            }
        )
    }
}

@Composable
fun JournalStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFF163B25), RoundedCornerShape(10.dp))
            .border(0.5.dp, Color(0xFF2D6A4F), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = label, fontSize = 9.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DailyJournalCard(
    journal: PetDailyJournalEntity,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dateFormatted = remember(journal.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()).format(Date(journal.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF163B25)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D6A4F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Date & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = dateFormatted, fontSize = 11.sp, color = Color.Gray)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = Color(0xFF2D6A4F),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = journal.mood,
                            fontSize = 10.sp,
                            color = Color(0xFF00F5D4),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (journal.aiGenerated) {
                        Surface(
                            color = Color(0xFF0E2417),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF52B788))
                        ) {
                            Text(
                                text = "✨ AI Log",
                                fontSize = 10.sp,
                                color = Color(0xFF52B788),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = journal.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (journal.vibe.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FilterVintage,
                        contentDescription = null,
                        tint = Color(0xFF52B788),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Vibe: ${journal.vibe}",
                        fontSize = 11.sp,
                        color = Color(0xFF52B788)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Content Body
            Text(
                text = journal.content,
                fontSize = 13.sp,
                color = Color(0xFFE0E0E0),
                lineHeight = 18.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            // Key Takeaway & Gratitude if present & expanded
            if (expanded) {
                if (journal.keyTakeaway.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2417)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 Key Takeaway",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD166)
                            )
                            Text(
                                text = journal.keyTakeaway,
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                if (journal.gratitudeNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2417)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🌸 Gratitude Note",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F5D4)
                            )
                            Text(
                                text = journal.gratitudeNote,
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (expanded) "Show Less" else "Read Full Log",
                        fontSize = 12.sp,
                        color = Color(0xFF00F5D4),
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry?", color = Color.White) },
            text = { Text("Are you sure you want to delete this journal entry?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF163B25)
        )
    }
}

@Composable
fun DreamJournalCard(
    dream: PetDreamJournalEntity,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dateFormatted = remember(dream.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()).format(Date(dream.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2417)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D6A4F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = dateFormatted, fontSize = 11.sp, color = Color.Gray)
                }

                Surface(
                    color = Color(0xFF163B25),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF00F5D4))
                ) {
                    Text(
                        text = "🔮 ${dream.dreamSymbol}",
                        fontSize = 10.sp,
                        color = Color(0xFF00F5D4),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = dream.dreamTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Badges row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = Color(0xFF2D6A4F),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "✨ ${dream.lucidityLevel}",
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = Color(0xFF163B25),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "💫 ${dream.emotionalTone}",
                        fontSize = 10.sp,
                        color = Color(0xFFFFD166),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Content
            Text(
                text = dream.dreamContent,
                fontSize = 13.sp,
                color = Color(0xFFE0E0E0),
                lineHeight = 18.sp,
                fontFamily = FontFamily.Serif,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            if (expanded && dream.wakingReflection.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF163B25)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🌅 Waking Reflection",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF52B788)
                        )
                        Text(
                            text = dream.wakingReflection,
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (expanded) "Show Less" else "Read Full Dream Log",
                        fontSize = 12.sp,
                        color = Color(0xFF00F5D4),
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Dream Log?", color = Color.White) },
            text = { Text("Are you sure you want to delete this dream journal entry?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0E2417)
        )
    }
}

@Composable
fun EmptyJournalPlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2417)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D6A4F))
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF52B788),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = Color.LightGray,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun AddDailyJournalDialog(
    petName: String,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, mood: String, vibe: String, takeaway: String, gratitude: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("Serene") }
    var vibe by remember { mutableStateOf("Ambient Starlight") }
    var takeaway by remember { mutableStateOf("") }
    var gratitude by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Write Daily Journal Entry for $petName", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00F5D4),
                        unfocusedBorderColor = Color(0xFF2D6A4F)
                    )
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content / Reflections", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00F5D4),
                        unfocusedBorderColor = Color(0xFF2D6A4F)
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mood,
                        onValueChange = { mood = it },
                        label = { Text("Mood", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00F5D4),
                            unfocusedBorderColor = Color(0xFF2D6A4F)
                        )
                    )

                    OutlinedTextField(
                        value = vibe,
                        onValueChange = { vibe = it },
                        label = { Text("Vibe", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00F5D4),
                            unfocusedBorderColor = Color(0xFF2D6A4F)
                        )
                    )
                }

                OutlinedTextField(
                    value = takeaway,
                    onValueChange = { takeaway = it },
                    label = { Text("Key Takeaway (Optional)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00F5D4),
                        unfocusedBorderColor = Color(0xFF2D6A4F)
                    )
                )

                OutlinedTextField(
                    value = gratitude,
                    onValueChange = { gratitude = it },
                    label = { Text("Gratitude Note (Optional)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00F5D4),
                        unfocusedBorderColor = Color(0xFF2D6A4F)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        onSave(title, content, mood, vibe, takeaway, gratitude)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F))
            ) {
                Text("Save Entry", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF0E2417)
    )
}

@Composable
fun AddDreamJournalDialog(
    petName: String,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, symbol: String, lucidity: String, tone: String, reflection: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf("Starlight Crystal") }
    var lucidity by remember { mutableStateOf("Deep Astral Dream") }
    var tone by remember { mutableStateOf("Wonder & Awe") }
    var reflection by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Dream Log for $petName", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Dream Title", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00F5D4),
                        unfocusedBorderColor = Color(0xFF2D6A4F)
                    )
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Subconscious Dream Vision", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00F5D4),
                        unfocusedBorderColor = Color(0xFF2D6A4F)
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = symbol,
                        onValueChange = { symbol = it },
                        label = { Text("Dream Symbol", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00F5D4),
                            unfocusedBorderColor = Color(0xFF2D6A4F)
                        )
                    )

                    OutlinedTextField(
                        value = tone,
                        onValueChange = { tone = it },
                        label = { Text("Emotional Tone", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00F5D4),
                            unfocusedBorderColor = Color(0xFF2D6A4F)
                        )
                    )
                }

                OutlinedTextField(
                    value = reflection,
                    onValueChange = { reflection = it },
                    label = { Text("Waking Reflection (Optional)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00F5D4),
                        unfocusedBorderColor = Color(0xFF2D6A4F)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        onSave(title, content, symbol, lucidity, tone, reflection)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F))
            ) {
                Text("Save Dream Log", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF0E2417)
    )
}
