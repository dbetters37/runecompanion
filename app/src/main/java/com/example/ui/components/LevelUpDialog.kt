package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.LevelUpEvent
import com.example.ui.theme.*

@Composable
fun LevelUpNotificationBanner(
    event: LevelUpEvent,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(event) {
        isVisible = true
        kotlinx.coroutines.delay(4200L)
        isVisible = false
        kotlinx.coroutines.delay(350L)
        onDismiss()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "levelup_banner_anim")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300)),
        modifier = modifier.fillMaxWidth()
    ) {
        val skillColor = event.skill.accentColor
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp), ambientColor = OsrsGoldBright, spotColor = OsrsGoldBright)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A1C12),
                            Color(0xFF18100A),
                            Color(0xFF100A06)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            OsrsGoldBright,
                            Color(0xFFFFF3B0),
                            skillColor,
                            OsrsGoldBright
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable {
                    isVisible = false
                    onDismiss()
                }
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("level_up_notification_banner"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Glowing Skill Emblem with Level Crown
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    skillColor.copy(alpha = glowAlpha),
                                    Color(0xFF2A1C12)
                                )
                            )
                        )
                        .border(1.5.dp, OsrsGoldBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = event.skill.iconSymbol, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Middle: Text & Level Information
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "✨ LEVEL UP! ✨",
                            color = OsrsGoldBright,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFD4AF37), Color(0xFFFFE066))
                                    )
                                )
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Lv. ${event.newLevel}",
                                color = Color(0xFF1A1208),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Text(
                        text = "${event.skill.displayName.uppercase()} advanced to Level ${event.newLevel}!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        lineHeight = 15.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF38291B))
                                .border(0.5.dp, OsrsGold.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("🪙 +30 GP Claimed", color = OsrsTextYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1E3A20))
                                .border(0.5.dp, Color(0xFF81C784), RoundedCornerShape(3.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("⚡ Milestone Active", color = Color(0xFF81C784), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Stylish Dismiss Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF381C10))
                        .border(1.dp, OsrsGold.copy(alpha = 0.8f), CircleShape)
                        .clickable {
                            isVisible = false
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LevelUpDialog(
    event: LevelUpEvent,
    onDismiss: () -> Unit
) {
    LevelUpNotificationBanner(event = event, onDismiss = onDismiss)
}

