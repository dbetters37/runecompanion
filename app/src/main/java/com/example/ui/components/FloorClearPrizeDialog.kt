package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.FloorClearReward
import com.example.ui.theme.*

@Composable
fun FloorClearPrizeDialog(
    reward: FloorClearReward,
    onClaim: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "prize_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD700),
            Color(0xFFFFA500),
            Color(0xFFFFEE55),
            Color(0xFFFFD700)
        )
    )

    Dialog(
        onDismissRequest = onClaim,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF161E28),
            border = BorderStroke(2.dp, Color(0xFFFFD700)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("floor_clear_prize_panel")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF22160C),
                                Color(0xFF151D28),
                                Color(0xFF0F151E)
                            )
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Celebration Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF3B2A10),
                    border = BorderStroke(1.dp, Color(0xFFFFD700))
                ) {
                    Text(
                        text = "🎉 FLOOR ${reward.floorNumber} CONQUERED! 🎉",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = reward.floorTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "You cleared the dungeon floor and earned Adventuring Level ${reward.floorNumber + 1}! Claim your 1 of 99 random Skilling Set prizes:",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                // Prize Showcase Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2836)),
                    border = BorderStroke(1.5.dp, if (reward.isNewPiece) Color(0xFF00FF9D) else Color(0xFFFFD700)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Badge: NEW or DUPLICATE
                        if (reward.isNewPiece) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1B4D2E),
                                border = BorderStroke(1.dp, Color(0xFF00FF9D))
                            ) {
                                Text(
                                    text = "✨ NEW SKILLING PIECE UNLOCKED! ✨",
                                    color = Color(0xFF00FF9D),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF332B1A),
                                border = BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Text(
                                    text = "⭐ SKILLING SET PIECE ⭐",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Animated Prize Icon Box
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF4A3818),
                                            Color(0xFF241B0E)
                                        )
                                    )
                                )
                                .border(2.dp, shimmerBrush, CircleShape)
                        ) {
                            Text(
                                text = reward.iconEmoji,
                                fontSize = 38.sp
                            )
                        }

                        // Prize Item Name & Slot
                        Text(
                            text = reward.pieceName,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        // Slot & Bonus
                        val slotName = reward.slotName
                        val skillBonus = reward.skill.displayName
                        Text(
                            text = "$slotName • +5% $skillBonus XP Permanent Passive Buff",
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )

                        Text(
                            text = reward.description,
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(color = Color(0xFF334252), thickness = 1.dp)

                        // Permanent Unlock Confirmation
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✨", fontSize = 14.sp)
                            Text(
                                text = "Permanent Passive Unlock (Takes 0 Storage)",
                                color = Color(0xFF00FF9D),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Collection Tracker Bar (e.g. 14 / 99 Pieces Collected)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🎁 99 Skilling Set Prizes Collection",
                            color = Color.LightGray,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${reward.totalOwnedCount} / ${reward.totalPrizePoolCount}",
                            color = Color(0xFFFFD700),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (reward.totalOwnedCount.toFloat() / reward.totalPrizePoolCount.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFFD700),
                        trackColor = Color(0xFF263238),
                    )
                }

                // Claim Button
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF00FF9D)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("claim_floor_prize_button")
                ) {
                    Text(
                        text = "Claim Prize & Continue Adventuring ⚔️",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
