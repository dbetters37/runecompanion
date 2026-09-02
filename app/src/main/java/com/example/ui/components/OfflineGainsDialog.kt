package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.OfflineGainsReport
import com.example.ui.theme.*

@Composable
fun OfflineGainsDialog(
    report: OfflineGainsReport,
    onDismiss: () -> Unit
) {
    var animationTriggered by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (animationTriggered) 1.0f else 0.75f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "offline_dialog_scale"
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (animationTriggered) 1.0f else 0f,
        animationSpec = tween(220),
        label = "offline_dialog_alpha"
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scaleAnim)
                .alpha(alphaAnim),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF231911))
                    .border(2.5.dp, OsrsGold, RoundedCornerShape(12.dp))
                    .testTag("offline_gains_dialog"),
                color = Color(0xFF231911)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B120B)),
                        border = BorderStroke(1.5.dp, OsrsGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B2514))
                                    .border(1.dp, OsrsGoldBright, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(report.activityEmoji, fontSize = 22.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Welcome Back, Hero!",
                                    color = OsrsGoldBright,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "AFK Task: ${report.activityName} (${report.formattedDuration})",
                                    color = OsrsParchment,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Main Scrollable Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 380.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stat Grid: Actions, XP, GP
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = Color(0xFF1B120B),
                                border = BorderStroke(1.dp, Color(0xFF4A3423)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("⚡ Actions", color = Color.Gray, fontSize = 10.sp)
                                    Text(
                                        "${report.actionsCompleted}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Surface(
                                color = Color(0xFF1B120B),
                                border = BorderStroke(1.dp, Color(0xFF4A3423)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🧠 XP Gained", color = Color(0xFFA5D6A7), fontSize = 10.sp)
                                    Text(
                                        "+${"%,d".format(report.xpGained)}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (report.gpGained > 0) {
                                Surface(
                                    color = Color(0xFF1B120B),
                                    border = BorderStroke(1.dp, Color(0xFF4A3423)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🪙 GP Earned", color = Color(0xFFFFE082), fontSize = 10.sp)
                                        Text(
                                            "+${"%,d".format(report.gpGained)}",
                                            color = OsrsGoldBright,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Stopped Reason Notice
                        if (report.stoppedReason != null) {
                            Surface(
                                color = Color(0xFF3B1E14),
                                border = BorderStroke(0.8.dp, Color(0xFFE57373)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🛑", fontSize = 12.sp)
                                    Text(
                                        text = report.stoppedReason,
                                        color = Color(0xFFFFCDD2),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // --- EXTRA BONUS MATERIALS FROM NPC FAVOR PERKS ---
                        if (report.extraBonusMaterialsGained.isNotEmpty()) {
                            Divider(color = Color(0xFFFFD700).copy(alpha = 0.6f), thickness = 1.dp)
                            Surface(
                                color = Color(0xFF2B1F0A),
                                border = BorderStroke(1.2.dp, Color(0xFFFFD700)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("✨", fontSize = 14.sp)
                                        Text(
                                            text = "NPC Favor Bonus Perks Proc'd (${report.extraBonusMaterialsGained.sumOf { it.quantity }} Extra Items)",
                                            color = Color(0xFFFFD700),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Text(
                                        text = "Your companion favor levels granted bonus double resource yields while AFK!",
                                        color = Color(0xFFFFE082),
                                        fontSize = 10.sp
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        report.extraBonusMaterialsGained.forEach { item ->
                                            Surface(
                                                color = Color(0xFF1B120B),
                                                border = BorderStroke(0.6.dp, Color(0xFF8D6E63)),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(item.iconEmoji, fontSize = 12.sp)
                                                        Text(
                                                            text = item.name,
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                    Text(
                                                        text = "+${item.quantity} EXTRA",
                                                        color = Color(0xFFFFD700),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Items / Loot Gained
                        if (report.itemsGained.isNotEmpty()) {
                            Divider(color = Color(0xFF5C4535), thickness = 1.dp)
                            Text(
                                "🎒 Total Resources Collected & Banked:",
                                color = Color(0xFF81C784),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                report.itemsGained.forEach { item ->
                                    Surface(
                                        color = Color(0xFF231911),
                                        border = BorderStroke(0.8.dp, Color(0xFF4E342E)),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(item.iconEmoji, fontSize = 13.sp)
                                                Text(
                                                    text = item.name,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Text(
                                                text = "+${item.quantity}",
                                                color = OsrsGoldBright,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Golem Offline Gains
                        val golem = report.golemGains
                        if (golem != null && golem.actionsCompleted > 0) {
                            Divider(color = Color(0xFF6A1B9A), thickness = 1.dp)
                            Surface(
                                color = Color(0xFF22132D),
                                border = BorderStroke(1.2.dp, Color(0xFFBA68C8)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(golem.golemEmoji, fontSize = 14.sp)
                                            Text(
                                                text = "${golem.golemName} Autonomous Work",
                                                color = Color(0xFFE1BEE7),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (golem.isCompleted) {
                                            Surface(
                                                color = Color(0xFF2E7D32),
                                                shape = RoundedCornerShape(3.dp)
                                            ) {
                                                Text(
                                                    text = "COMPLETED",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Task: ${golem.activityEmoji} ${golem.activityName} (${golem.actionsCompleted} ticks)",
                                        color = OsrsParchment,
                                        fontSize = 10.sp
                                    )
                                    // Golem XP & GP stats
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (golem.xpGained > 0) {
                                            Surface(
                                                color = Color(0xFF1B3820),
                                                border = BorderStroke(0.8.dp, Color(0xFF81C784)),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text("🧠 Golem XP", color = Color(0xFFA5D6A7), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text("+${"%,d".format(golem.xpGained)}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                        if (golem.gpGained > 0) {
                                            Surface(
                                                color = Color(0xFF3B2B16),
                                                border = BorderStroke(0.8.dp, Color(0xFFFFD700)),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text("🪙 Coins", color = Color(0xFFFFE082), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text("+${"%,d".format(golem.gpGained)} GP", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                    }
                                    // Golem Items Gathered
                                    if (golem.itemsGained.isNotEmpty()) {
                                        Text(
                                            text = "🪨 Materials Gathered:",
                                            color = Color(0xFFCE93D8),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            golem.itemsGained.forEach { item ->
                                                Surface(
                                                    color = Color(0xFF1B120B),
                                                    border = BorderStroke(0.6.dp, Color(0xFF4A3423)),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text(item.iconEmoji, fontSize = 12.sp)
                                                            Text(
                                                                text = item.name,
                                                                color = Color.White,
                                                                fontSize = 10.5.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                        Text(
                                                            text = "+${item.quantity}",
                                                            color = OsrsGoldBright,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
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

                    // Claim Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                        border = BorderStroke(1.5.dp, OsrsGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("claim_offline_gains_button")
                    ) {
                        Text(
                            text = "⚔️ Claim Gains & Continue",
                            color = OsrsTextYellow,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
