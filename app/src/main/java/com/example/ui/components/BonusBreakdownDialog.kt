package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

data class BonusSourceDetail(
    val title: String,
    val description: String,
    val bonusPercent: Int,
    val emoji: String,
    val isUnlocked: Boolean = true,
    val unlockRequirement: String? = null
)

@Composable
fun BonusBreakdownDialog(
    title: String,
    categoryName: String,
    iconEmoji: String,
    sources: List<BonusSourceDetail>,
    onDismiss: () -> Unit,
    accentColor: Color = OsrsGold,
    note: String? = null
) {
    val totalActivePercent = sources.filter { it.isUnlocked }.sumOf { it.bonusPercent }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("bonus_breakdown_dialog"),
            colors = CardDefaults.cardColors(containerColor = OsrsLeatherDark),
            border = BorderStroke(2.dp, accentColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, accentColor),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(iconEmoji, fontSize = 20.sp)
                            }
                        }
                        Column {
                            Text(
                                text = title,
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = categoryName,
                                color = Color(0xFFA5D6A7),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_bonus_dialog_btn")
                    ) {
                        Text("✖", color = Color.LightGray, fontSize = 14.sp)
                    }
                }

                HorizontalDivider(color = OsrsGold.copy(alpha = 0.5f), thickness = 1.dp)

                // Total Active Bonus Banner
                Surface(
                    color = Color(0xFF1B3A1B),
                    border = BorderStroke(1.2.dp, Color(0xFF81C784)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL BONUS CHANCE",
                                color = Color(0xFFA5D6A7),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Active Double Proc Rate",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.5.sp
                            )
                        }

                        Surface(
                            color = Color(0xFF2E7D32),
                            border = BorderStroke(1.dp, Color(0xFFFFD700)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "+$totalActivePercent%",
                                color = Color(0xFFFFD700),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Sources list title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BONUS CONTRIBUTORS & SOURCES",
                        color = OsrsGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sources Cards
                sources.forEach { source ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (source.isUnlocked) OsrsLeatherMedium else OsrsLeatherMedium.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (source.isUnlocked) Color(0xFF4CAF50).copy(alpha = 0.7f) else Color.DarkGray
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(source.emoji, fontSize = 22.sp)

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = source.title,
                                        color = if (source.isUnlocked) OsrsTextYellow else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )

                                    if (source.isUnlocked) {
                                        Surface(
                                            color = Color(0xFF1B5E20),
                                            border = BorderStroke(0.8.dp, Color(0xFF81C784)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "+${source.bonusPercent}%",
                                                color = Color(0xFFA5D6A7),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            color = Color(0xFF37474F),
                                            border = BorderStroke(0.8.dp, Color.Gray),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "🔒 LOCKED",
                                                color = Color.LightGray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = source.description,
                                    color = if (source.isUnlocked) OsrsParchment else Color.Gray,
                                    fontSize = 10.5.sp,
                                    lineHeight = 13.sp
                                )

                                if (!source.isUnlocked && source.unlockRequirement != null) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Req: ${source.unlockRequirement}",
                                        color = Color(0xFFFF8A80),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                if (note != null) {
                    Text(
                        text = "💡 $note",
                        color = Color(0xFFB0BEC5),
                        fontSize = 10.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, OsrsGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("dismiss_bonus_dialog_btn")
                ) {
                    Text(
                        text = "Understood",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
