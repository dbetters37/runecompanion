package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PetMoodState
import com.example.data.models.PetState
import com.example.ui.theme.*

@Composable
fun OsrsHeader(
    petState: PetState,
    totalLevel: Int,
    petMoodState: PetMoodState = PetMoodState(),
    currentActivityText: String? = null,
    onOpenSettings: () -> Unit = {},
    onOpenDailySpiritQuests: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val maxHunger = 100 + totalLevel
    val hungerFraction = (petState.hunger.toFloat() / maxHunger.toFloat()).coerceIn(0f, 1f)
    val activityText = currentActivityText ?: "💤 Idle"
    val isActiveTask = !activityText.startsWith("💤")

    val infiniteTransition = rememberInfiniteTransition(label = "header_pulse")
    val activeDotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "active_dot_pulse"
    )

    OvergrownStoneCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 12.dp,
        showRunes = true,
        showVineCorners = false
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Top Row: Luminous Pet Icon + Custom Name + Settings Cog
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF4A3525),
                                        Color(0xFF1E140C)
                                    )
                                )
                            )
                            .border(1.5.dp, OsrsGoldBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = petState.petType.iconSymbol,
                            fontSize = 20.sp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = petState.customName,
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text("🐾", fontSize = 11.sp)
                        }
                        Text(
                            text = "${petState.petType.displayName} • ${petState.petType.primarySkill.displayName}",
                            color = OsrsParchment.copy(alpha = 0.85f),
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Top-Right Settings Cog
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF3E2D1D), Color(0xFF241910))
                            )
                        )
                        .border(1.dp, OsrsGold, CircleShape)
                        .clickable { onOpenSettings() }
                        .testTag("top_right_settings_cog"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚙️",
                        fontSize = 14.sp
                    )
                }
            }

            // Middle Row: Horizontally Scrollable Stats Badges Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hunger Badge Bar
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2C2016), Color(0xFF1B130D))
                            )
                        )
                        .border(1.dp, Color(0xFFF4A261).copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "🍗 ${petState.hunger}/$maxHunger",
                            color = OsrsTextYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF140E08))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(hungerFraction)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            if (hungerFraction > 0.5f) listOf(Color(0xFF4CAF50), Color(0xFF81C784))
                                            else if (hungerFraction > 0.25f) listOf(Color(0xFFE65100), Color(0xFFF4A261))
                                            else listOf(Color(0xFFB71C1C), Color(0xFFE63946))
                                        )
                                    )
                            )
                        }
                    }
                }

                // Total Level Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2C2016), Color(0xFF1B130D))
                            )
                        )
                        .border(1.dp, OsrsGoldBright, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.5.dp)
                ) {
                    Text(
                        text = "🛡️ Lvl $totalLevel",
                        color = OsrsTextYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Coins GP Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2C2016), Color(0xFF1B130D))
                            )
                        )
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.5.dp)
                ) {
                    Text(
                        text = "🪙 ${formatGp(petState.coinsGp)}",
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            // Bottom Row: Activity Display Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (isActiveTask) listOf(Color(0xFF133E18), Color(0xFF1B5E20))
                            else listOf(Color(0xFF2C1F15), Color(0xFF20160F))
                        )
                    )
                    .border(
                        1.dp,
                        if (isActiveTask) Color(0xFF81C784) else OsrsGold.copy(alpha = 0.6f),
                        RoundedCornerShape(7.dp)
                    )
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Pulsing status dot indicator
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActiveTask) Color(0xFF81C784).copy(alpha = activeDotAlpha)
                                    else Color(0xFFF4A261).copy(alpha = 0.7f)
                                )
                        )

                        Text(
                            text = if (isActiveTask) "Active Task:" else "Activity:",
                            color = if (isActiveTask) Color(0xFFC8E6C9) else OsrsGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activityText,
                            color = if (isActiveTask) Color(0xFFFFFFFF) else OsrsParchment,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun formatGp(gp: Long): String {
    return when {
        gp >= 10_000_000L -> "${gp / 1_000_000L}M"
        gp >= 100_000L -> "${gp / 1_000L}K"
        else -> String.format("%,d", gp)
    }
}

