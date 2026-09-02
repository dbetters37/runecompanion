package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.GoogleTaskItem
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

@Composable
fun GoogleTasksTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val googleTasks by viewModel.googleTasks.collectAsStateWithLifecycle()
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val forXp = skillXpMap[OsrsSkill.SMITHING] ?: 0L
    val forgingLvl = OsrsXpCalculator.getLevelForXp(forXp)

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskNotes by remember { mutableStateOf("") }
    var showOAuthDialog by remember { mutableStateOf(false) }
    var oauthTokenInput by remember { mutableStateOf("") }

    val pendingTasks = googleTasks.filter { !it.isCompleted }
    val completedTasks = googleTasks.filter { it.isCompleted }

    val nowMs = System.currentTimeMillis()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OsrsLeatherDark)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium),
            border = androidx.compose.foundation.BorderStroke(2.dp, OsrsGold),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📋", fontSize = 22.sp)
                        Column {
                            Text(
                                "Google Tasks Mining Quarry",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                "Complete tasks to mine XP & rare Ores!",
                                color = OsrsParchment,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = { showOAuthDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("🔑 OAuth API", color = OsrsTextYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = Color(0xFF4A3828), modifier = Modifier.padding(vertical = 2.dp))

                // Stats Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pending Tasks", color = OsrsParchment, fontSize = 10.sp)
                        Text("${pendingTasks.size}", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Forging Level", color = OsrsParchment, fontSize = 10.sp)
                        Text("⚒️ Lvl $forgingLvl", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sync Mode", color = OsrsParchment, fontSize = 10.sp)
                        Text("Google Tasks Ready 🟢", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Create Task Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2218)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5A4532)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "➕ Add New Google Task:",
                    color = OsrsTextYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    placeholder = { Text("e.g., Complete project presentation, Clean workspace...", color = Color.Gray, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OsrsTextYellow,
                        unfocusedBorderColor = Color(0xFF5A4532),
                        focusedTextColor = OsrsTextWhite,
                        unfocusedTextColor = OsrsTextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_task_title_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskNotes,
                        onValueChange = { newTaskNotes = it },
                        placeholder = { Text("Notes (optional)", color = Color.Gray, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OsrsTextYellow,
                            unfocusedBorderColor = Color(0xFF5A4532),
                            focusedTextColor = OsrsTextWhite,
                            unfocusedTextColor = OsrsTextWhite
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("google_task_notes_input")
                    )

                    Button(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                viewModel.createGoogleTask(newTaskTitle, newTaskNotes.ifBlank { null })
                                newTaskTitle = ""
                                newTaskNotes = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("add_google_task_button")
                    ) {
                        Text("Add Task ➕", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Task List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⌛ Pending Tasks (${pendingTasks.size})",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "💡 Older tasks = Rarer Ores (Runite 💙 / Addy 🟢)",
                        color = OsrsParchment,
                        fontSize = 10.sp
                    )
                }
            }

            if (pendingTasks.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF221A12)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "🎉 All Google Tasks complete! Add a new task above or sync with Google Tasks API.",
                                color = OsrsParchment,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                items(pendingTasks, key = { it.id }) { task ->
                    GoogleTaskItemCard(
                        task = task,
                        nowMs = nowMs,
                        onComplete = { viewModel.completeGoogleTask(task.id) }
                    )
                }
            }

            if (completedTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "✅ Recently Completed (${completedTasks.size})",
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                items(completedTasks.take(10), key = { "comp_${it.id}" }) { task ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E281F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E4D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    task.title,
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    style = androidx.compose.ui.text.TextStyle(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                )
                                Text(
                                    "Completed & Claimed Mining Rewards ✅",
                                    color = Color(0xFF81C784),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // OAuth Credentials Dialog
    if (showOAuthDialog) {
        AlertDialog(
            onDismissRequest = { showOAuthDialog = false },
            title = {
                Text("🔑 Google Tasks OAuth Settings", color = OsrsTextYellow, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "OAuth for Google Tasks is enabled! You can sync directly with your Google Account or enter a Google OAuth Access Token below:",
                        color = OsrsParchment,
                        fontSize = 11.sp
                    )
                    OutlinedTextField(
                        value = oauthTokenInput,
                        onValueChange = { oauthTokenInput = it },
                        placeholder = { Text("Paste OAuth Access Token...", color = Color.Gray) },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (oauthTokenInput.isNotBlank()) {
                            viewModel.setGoogleOAuthToken(oauthTokenInput.trim())
                        }
                        showOAuthDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame)
                ) {
                    Text("Save & Sync", color = OsrsTextYellow)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOAuthDialog = false }) {
                    Text("Close", color = OsrsParchment)
                }
            },
            containerColor = OsrsLeatherMedium,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun GoogleTaskItemCard(
    task: GoogleTaskItem,
    nowMs: Long,
    onComplete: () -> Unit
) {
    val ageDays = task.getAgeInDays(nowMs)
    val xpReward = task.getMiningXpReward(nowMs)
    val gpReward = task.getGpReward(nowMs)
    val oreReward = task.getOreReward(nowMs)

    val ageLabel = when {
        ageDays >= 7.0 -> "⏳ Pending: ${"%.1f".format(ageDays)} days (ANCIENT 💙)"
        ageDays >= 3.0 -> "⏳ Pending: ${"%.1f".format(ageDays)} days (AGED 🟢)"
        ageDays >= 1.0 -> "⏳ Pending: ${"%.1f".format(ageDays)} days (MATURE 🔷)"
        else -> "⏳ Pending: <1 day (FRESH 🪨)"
    }

    val cardBorderColor = when {
        ageDays >= 7.0 -> Color(0xFF4FC3F7) // Bright Runite Blue
        ageDays >= 3.0 -> Color(0xFF81C784) // Adamant Green
        ageDays >= 1.0 -> Color(0xFF64B5F6) // Mithril Blue
        else -> Color(0xFF5A4532)
    }

    val headerBgColor = when {
        ageDays >= 7.0 -> Color(0xFF0F2B3C)
        ageDays >= 3.0 -> Color(0xFF132D18)
        else -> Color(0xFF2A1E14)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = headerBgColor),
        border = androidx.compose.foundation.BorderStroke(2.dp, cardBorderColor),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("google_task_card_${task.id}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.title,
                        color = OsrsTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    task.notes?.let {
                        Text(it, color = OsrsParchment, fontSize = 11.sp)
                    }
                }

                Surface(
                    color = Color(0xFF18120B),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
                ) {
                    Text(
                        text = ageLabel,
                        color = if (ageDays >= 7.0) Color(0xFF80DEEA) else OsrsTextYellow,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Divider(color = Color(0xFF3E2F20))

            // Rewards Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⛏️ +${xpReward} Mining XP", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("💰 +${gpReward} GP", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Text(
                        "📦 Drop: ${oreReward.quantity}x ${oreReward.oreName}",
                        color = if (ageDays >= 7.0) Color(0xFF80DEEA) else Color(0xFFFFB74D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }

                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B38)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("complete_google_task_${task.id}")
                ) {
                    Text("Complete & Claim ⛏️", color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}
