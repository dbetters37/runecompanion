package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.models.AiQuest
import com.example.ui.theme.*

@Composable
fun AiCompanionTab(
    quests: List<AiQuest>,
    isLoading: Boolean,
    onEvaluateAiAction: (String) -> Unit,
    onRefreshQuests: () -> Unit,
    onCompleteQuest: (AiQuest) -> Unit,
    onSendChatMessage: (String, (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var userActionText by remember { mutableStateOf("") }
    var chatInputText by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) } // Pair(text, isUser)

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(OsrsLeatherMedium)
            .border(2.dp, OsrsGold, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🤖 AI BOT: UNLIMITED XP HUB",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Powered by Gemini AI to evaluate custom actions & quests!",
                        color = OsrsParchment,
                        fontSize = 11.sp
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(
                        color = OsrsGold,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // Section 1: AI Custom Real-Life Action Evaluator
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(OsrsLeatherDark)
                    .border(1.dp, OsrsGold, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "✨ Log Custom Real-Life Activity",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Type ANY action you did in real life! The AI will analyze it and grant relevant OSRS XP and GP!",
                        color = OsrsParchment,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = userActionText,
                        onValueChange = { userActionText = it },
                        placeholder = { Text("e.g., 'I walked my dog, studied math, and made soup'", color = Color.Gray, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OsrsGold,
                            unfocusedBorderColor = OsrsParchment,
                            focusedTextColor = OsrsTextYellow,
                            unfocusedTextColor = OsrsTextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_custom_action_input")
                    )

                    Button(
                        onClick = {
                            if (userActionText.isNotBlank()) {
                                onEvaluateAiAction(userActionText)
                                userActionText = ""
                            }
                        },
                        enabled = !isLoading && userActionText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_ai_action_button")
                    ) {
                        Text(
                            text = if (isLoading) "Analyzing with Gemini..." else "⚡ Evaluate Action for XP & GP",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 2: AI Generated OSRS Quests
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📜 AI OSRS Quests",
                    color = OsrsTextYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                TextButton(onClick = onRefreshQuests, enabled = !isLoading) {
                    Text("🔄 Generate Quests", color = OsrsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(quests) { quest ->
            QuestCard(quest = quest, onComplete = { onCompleteQuest(quest) })
        }

        // Section 3: Interactive Chat with Pet / Wise Old Man
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(OsrsLeatherDark)
                    .border(1.dp, OsrsGold, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "💬 Chat with Pet",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    // Chat history window
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF19130D))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (chatHistory.isEmpty()) {
                            Text(
                                text = "💬 Talk to your pet! Your pet responds in creature noises!",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        } else {
                            chatHistory.forEach { (msg, isUser) ->
                                Text(
                                    text = if (isUser) "You: $msg" else "Pet: $msg",
                                    color = if (isUser) OsrsTextYellow else OsrsParchmentLight,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Talk to pet...", color = Color.Gray, fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OsrsGold,
                                unfocusedBorderColor = OsrsParchment,
                                focusedTextColor = OsrsTextYellow,
                                unfocusedTextColor = OsrsTextWhite
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_pet_chat_input")
                        )

                        Button(
                            onClick = {
                                val text = chatInputText.trim()
                                if (text.isNotBlank()) {
                                    chatHistory = chatHistory + Pair(text, true)
                                    chatInputText = ""
                                    onSendChatMessage(text) { reply ->
                                        chatHistory = chatHistory + Pair(reply, false)
                                    }
                                }
                            },
                            enabled = !isLoading && chatInputText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Send", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestCard(
    quest: AiQuest,
    onComplete: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (quest.isCompleted) Color(0xFF233B26) else OsrsLeatherDark)
            .border(1.dp, if (quest.isCompleted) Color(0xFF52B788) else OsrsGold, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (quest.isCompleted) "✅ " else "⚔️ "}${quest.title}",
                    color = if (quest.isCompleted) OsrsTextGreen else OsrsTextYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "+${quest.rewardXp} ${quest.targetSkill.displayName} XP | +${quest.rewardGp} GP",
                    color = OsrsGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = quest.description,
                color = OsrsParchment,
                fontSize = 11.sp
            )

            if (quest.isAutoPhoneTriggered) {
                Text(
                    text = "📱 Auto-Phone Tracking Active: '${quest.targetPackageKeyword.ifBlank { "Duolingo" }}' (${quest.triggerCount}/${quest.targetTriggerCount})",
                    color = Color(0xFF70A1FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "📌 Task: ${quest.realLifeTaskInstructions}",
                    color = OsrsTextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!quest.isCompleted) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .testTag("complete_quest_${quest.id}")
                    ) {
                        Text("Complete Task", color = OsrsTextYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    if (quest.isAutoPhoneTriggered) {
                        OutlinedButton(
                            onClick = {
                                val kw = quest.targetPackageKeyword.ifBlank { "duolingo" }
                                com.example.services.NotificationDismissListenerService.emitSimulatedAppEvent(kw, context)
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .testTag("test_auto_app_trigger_${quest.id}")
                        ) {
                            Text("📲 Test Open: ${quest.targetPackageKeyword.ifBlank { "Duolingo" }}", color = OsrsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
