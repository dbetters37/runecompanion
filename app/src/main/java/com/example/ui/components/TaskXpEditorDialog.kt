package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AiQuest
import com.example.data.models.OsrsSkill
import com.example.ui.theme.*

data class StandardTaskXp(
    val id: String,
    val title: String,
    val skill: OsrsSkill,
    var currentXp: Long,
    val description: String
)

@Composable
fun TaskXpEditorDialog(
    taskXpList: List<StandardTaskXp>,
    customQuests: List<AiQuest>,
    onUpdateTaskXp: (taskId: String, newXp: Long) -> Unit,
    onUpdateQuestXp: (questId: String, newXp: Long) -> Unit,
    onFitnessSyncSteps: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var fitnessStepInput by remember { mutableStateOf("5000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame)
            ) {
                Text("Done", color = OsrsTextYellow)
            }
        },
        title = {
            Text("⚙️ TASK XP EDITOR & FITNESS SYNC", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fitness App Sync Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E281B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF70E000))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🏃 Fitness App Step Tracker Sync", color = Color(0xFF70E000), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Sync real steps recorded from Android Step Sensor / Fitness App:", color = OsrsParchment, fontSize = 10.sp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = fitnessStepInput,
                                onValueChange = { fitnessStepInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("Steps to Sync", color = Color.Gray, fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("fitness_step_sync_input")
                            )

                            Button(
                                onClick = {
                                    val steps = fitnessStepInput.toLongOrNull() ?: 0L
                                    if (steps > 0) {
                                        onFitnessSyncSteps(steps)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Sync Steps", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Divider(color = OsrsGold)

                Text("📝 EDIT TASK XP GRANTED:", color = OsrsGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Standard Real Life Actions
                    items(taskXpList) { task ->
                        var editXpText by remember(task.currentXp) { mutableStateOf(task.currentXp.toString()) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2018)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsParchment)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.title, color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Skill: ${task.skill.displayName}", color = OsrsParchment, fontSize = 10.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OutlinedTextField(
                                        value = editXpText,
                                        onValueChange = { editXpText = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .width(85.dp)
                                            .testTag("edit_xp_input_${task.id}")
                                    )

                                    Button(
                                        onClick = {
                                            val newXp = editXpText.toLongOrNull() ?: task.currentXp
                                            onUpdateTaskXp(task.id, newXp)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Save", color = OsrsTextYellow, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Custom AI Quests
                    items(customQuests) { quest ->
                        var editQuestXpText by remember(quest.rewardXp) { mutableStateOf(quest.rewardXp.toString()) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF251E18)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE76F51))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("🤖 ${quest.title}", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Skill: ${quest.targetSkill.displayName}", color = OsrsParchment, fontSize = 10.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OutlinedTextField(
                                        value = editQuestXpText,
                                        onValueChange = { editQuestXpText = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .width(85.dp)
                                            .testTag("edit_quest_xp_input_${quest.id}")
                                    )

                                    Button(
                                        onClick = {
                                            val newXp = editQuestXpText.toLongOrNull() ?: quest.rewardXp
                                            onUpdateQuestXp(quest.id, newXp)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Save", color = OsrsTextYellow, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = OsrsLeatherDark
    )
}
