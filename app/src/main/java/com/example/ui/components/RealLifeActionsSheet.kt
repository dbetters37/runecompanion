package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.data.models.AiQuest
import com.example.data.models.OsrsSkill
import androidx.compose.ui.draw.scale
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
import com.example.ui.theme.*

@Composable
fun RealLifeActionsSheet(
    stepCount: Long,
    quests: List<AiQuest> = emptyList(),
    skillAppListeners: Map<OsrsSkill, Boolean> = emptyMap(),
    onToggleSkillAppListener: (OsrsSkill) -> Unit = {},
    onAddSimulatedSteps: (Long) -> Unit,
    onSwipeNotification: () -> Unit,
    onPickpocketThieving: () -> Unit = {},
    onDrinkWater: () -> Unit,
    onWorkout: (String, Int) -> Unit,
    onCleanRoom: () -> Unit,
    onReadingBlockComplete: (Int) -> Unit,
    onCompleteQuest: (AiQuest) -> Unit = {},
    onLogSleep: (Float) -> Unit = {},
    onLogTextMessage: () -> Unit = {},
    onLogDuolingo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var workoutRepsText by remember { mutableStateOf("15") }
    var readingMinsText by remember { mutableStateOf("10") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val isNotificationAccessEnabled = remember {
        com.example.services.NotificationDismissListenerService.isNotificationServiceEnabled(context)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(OsrsLeatherMedium)
            .border(2.dp, OsrsGold, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                text = "🏃 REAL-LIFE SKILL TRAINING HUB",
                color = OsrsTextYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Gain OSRS experience by performing physical and mental real-world actions!",
                color = OsrsParchment,
                fontSize = 12.sp
            )
        }

        // 📱 REAL-TIME SKILL APP LISTENERS CONFIGURATION HUB
        Column {
            var activeAppStatus by remember {
                mutableStateOf(com.example.utils.PhoneContextHelper.getPhoneContext(context).activeAppOrScreen)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2018)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, OsrsGold)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📱 AUTOMATIC SKILL APP LISTENERS",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        if (!isNotificationAccessEnabled) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("⚙️ Enable System Listener", color = OsrsTextYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = "Enable background tracking for specific phone apps to gain OSRS XP automatically in real time!",
                        color = OsrsParchment,
                        fontSize = 11.sp
                    )

                    // Listener Toggles
                    listOf<Triple<OsrsSkill, String, String>>(
                        Triple(OsrsSkill.WOODCUTTING, "🪓 Harvesting", "Swiped system notifications & notification dismissals"),
                        Triple(OsrsSkill.MAGIC, "📺 Incantations", "YouTube, Netflix, Hulu, Peacock, Twitch video streaming"),
                        Triple(OsrsSkill.RUNECRAFT, "📚 Runemaking", "Audible, Google Play Books, Kindle audiobooks & reading"),
                        Triple(OsrsSkill.THIEVING, "🔍 Trickery", "Google Smart Lens, Google Search Widget, Camera scans"),
                        Triple(OsrsSkill.DIVINATION, "🔮 Divination", "Sending or receiving text messages, SMS, WhatsApp, Messenger"),
                        Triple(OsrsSkill.HITPOINTS, "❤️ Life Energy & Sleep", "7+ hours of phone screen-off inactivity & sleep"),
                        Triple(OsrsSkill.COOKING, "🍳 Cooking / Health", "MyFitnessPal, Health Connect, Samsung Health apps"),
                        Triple(OsrsSkill.AGILITY, "🏃 Dexterity / Blowdarts", "Strava, Step Counter, Nike Run Club fitness apps")
                    ).forEach { (skill, label, appsDesc) ->
                        val isEnabled = skillAppListeners[skill] ?: true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E1610))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(label, color = OsrsTextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    if (isEnabled) {
                                        Text("⚡ ACTIVE", color = Color(0xFF70E000), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(appsDesc, color = OsrsParchment, fontSize = 10.sp)
                            }

                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { onToggleSkillAppListener(skill) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OsrsGold,
                                    checkedTrackColor = OsrsRedFrame,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.scale(0.8f).testTag("listener_toggle_${skill.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }
    }
}

