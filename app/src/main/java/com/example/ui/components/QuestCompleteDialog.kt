package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.OsrsQuest
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun QuestCompleteDialog(
    quest: OsrsQuest,
    onDismiss: () -> Unit
) {
    // Entrance scale animation
    var animationTriggered by remember { mutableStateOf(false) }
    var selectedRewardItemForDetail by remember { mutableStateOf<com.example.data.models.InventoryItem?>(null) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (animationTriggered) 1.0f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "quest_complete_dialog_scale"
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (animationTriggered) 1.0f else 0f,
        animationSpec = tween(250),
        label = "quest_complete_dialog_alpha"
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
            // Full screen overlay confetti particles floating over dialog
            QuestCelebrationConfetti(modifier = Modifier.fillMaxSize())

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF231911))
                    .border(2.5.dp, OsrsGold, RoundedCornerShape(12.dp))
                    .testTag("quest_complete_dialog"),
                color = Color(0xFF231911)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isShaman = com.example.data.models.TrainerLeagueData.isShamanPathQuest(quest)
                    val region = com.example.data.models.TrainerLeagueData.getRegionForQuest(quest.id)
                    val chapter = com.example.data.models.TrainerLeagueData.getChapterForQuest(quest)
                    val totem = com.example.data.models.TrainerLeagueData.getTotemForQuest(quest.id)
                    val petUnlock = com.example.data.models.TrainerLeagueData.getGymPetUnlock(quest.id)
                    val isChampion = quest.id.contains("champion") || quest.id.contains("lunar_diplomacy")

                    // Outer Header Banner with glowing shimmer animation
                    val headerShimmerTransition = rememberInfiniteTransition(label = "header_shimmer")
                    val headerBorderAlpha by headerShimmerTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "header_border_alpha"
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isShaman) Color(0xFF1E2D1F) else Color(0xFF382312)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isShaman) Color(0xFF81C784).copy(alpha = headerBorderAlpha) else OsrsGold.copy(alpha = headerBorderAlpha)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isShaman) "🌿 SHAMAN PATH CONQUERED! 🌿" else "🏆 QUEST COMPLETE! 🏆",
                                color = if (isShaman) Color(0xFF70E000) else OsrsTextYellow,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isShaman) "You have conquered ${quest.name}!" else "You have completed ${quest.name}!",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            if (isShaman && region != null) {
                                Surface(
                                    color = Color(0xFF2E7D32).copy(alpha = 0.35f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${region.emoji} ${region.displayName}${if (chapter != null) " • ${chapter.title}" else ""}",
                                        color = Color(0xFFA5D6A7),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    color = Color(quest.difficulty.colorHex).copy(alpha = 0.25f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(quest.difficulty.colorHex)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${quest.difficulty.badgeIcon} ${quest.difficulty.rarityLabel} (${quest.difficulty.displayName})",
                                        color = Color(quest.difficulty.colorHex),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quest Icon Seal with Golden Sunburst Rays behind it
                    Box(
                        modifier = Modifier.size(86.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SunburstRays(modifier = Modifier.fillMaxSize())

                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(29.dp))
                                .background(if (isShaman) Color(0xFF1E3A20) else Color(0xFF3E2D1D))
                                .border(2.dp, if (isShaman) Color(0xFF70E000) else OsrsGold, RoundedCornerShape(29.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(quest.iconEmoji, fontSize = 30.sp)
                        }
                    }

                    // Awarded Banner
                    Text(
                        text = if (isShaman) "Path Rewards & Spoils Awarded:" else "You are awarded:",
                        color = if (isShaman) Color(0xFF81C784) else OsrsGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Parchment scroll list of rewards with vertical scroll component
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2016)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5C4535)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 270.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Totem / Badge Earned Card
                            if (totem != null) {
                                Surface(
                                    color = Color(0xFF3B2E17),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(totem.emoji, fontSize = 18.sp)
                                        Column {
                                            Text(
                                                text = "🏆 Totem Conquered: ${totem.name}",
                                                color = Color(0xFFFFD700),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = "Permanently recorded in your Shaman Totem Obelisk!",
                                                color = Color(0xFFFFE082),
                                                fontSize = 9.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Pet Companion Unlocked Card
                            if (petUnlock != null) {
                                Surface(
                                    color = Color(0xFF163828),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E676)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(petUnlock.iconSymbol, fontSize = 18.sp)
                                        Column {
                                            Text(
                                                text = "🐾 New Pet Companion Unlocked: ${petUnlock.displayName}!",
                                                color = Color(0xFF69F0AE),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = "Available in your Pet Switcher & Companion Sanctuary!",
                                                color = Color(0xFFB9F6CA),
                                                fontSize = 9.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Incantation Slot Unlock
                            if (isChampion) {
                                Surface(
                                    color = Color(0xFF331C3D),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCE93D8)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("🪄", fontSize = 18.sp)
                                        Column {
                                            Text(
                                                text = "✨ Shaman Blessing: +1 Active Incantation Slot!",
                                                color = Color(0xFFE1BEE7),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = "You can now channel an additional active incantation simultaneously.",
                                                color = Color(0xFFF3E5F5),
                                                fontSize = 9.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Quest Points & Gold
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFF3E301F),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("⭐", fontSize = 14.sp)
                                        Text(
                                            text = "+${quest.questPoints} Quest Point${if (quest.questPoints > 1) "s" else ""}",
                                            color = OsrsTextYellow,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (quest.rewardGp > 0) {
                                    Surface(
                                        color = Color(0xFF3E301F),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("🪙", fontSize = 14.sp)
                                            Text(
                                                text = "+${quest.rewardGp} GP",
                                                color = Color(0xFFFFD700),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Divider(color = Color(0xFF5C4535), thickness = 1.dp)

                            // XP Rewards
                            if (quest.rewardXpMap.isNotEmpty()) {
                                Text("🧠 Skill XP Awarded:", color = Color(0xFF90CAF9), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    quest.rewardXpMap.forEach { (skill, xp) ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(skill.iconSymbol, fontSize = 13.sp)
                                            Text(
                                                text = "+$xp ${skill.displayName} XP",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            // Item Awarded
                            if (quest.rewardItemName != null || quest.rewardItemId != null) {
                                Divider(color = Color(0xFF5C4535), thickness = 1.dp)
                                Text("🎁 Items Earned:", color = Color(0xFF81C784), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.clickable {
                                        val itemId = quest.rewardItemId ?: "item_${quest.rewardItemName?.lowercase()?.replace(" ", "_")}"
                                        val foundItem = com.example.data.models.DefaultItems.getItemById(itemId) ?: com.example.data.models.InventoryItem(
                                            id = itemId,
                                            name = quest.rewardItemName ?: quest.rewardItemId ?: "Unique Reward",
                                            category = com.example.data.models.ItemCategory.EQUIPMENT,
                                            iconEmoji = quest.rewardItemEmoji ?: "🎁",
                                            description = quest.description ?: "A unique reward artifact earned by completing the ${quest.name} quest!",
                                            costGp = quest.rewardGp ?: 1000L
                                        )
                                        selectedRewardItemForDetail = foundItem
                                    }
                                ) {
                                    Text(quest.rewardItemEmoji ?: "🎁", fontSize = 14.sp)
                                    Text(
                                        text = quest.rewardItemName ?: quest.rewardItemId ?: "Quest Reward Item",
                                        color = Color(0xFF70E000),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Unlocks and Access
                            val unlocks = quest.unlockedFeatures.ifEmpty {
                                listOfNotNull(
                                    quest.rewardItemName?.let { "Access to $it" }
                                )
                            }

                            if (unlocks.isNotEmpty()) {
                                Divider(color = Color(0xFF5C4535), thickness = 1.dp)
                                Text("🔓 Content & Features Unlocked:", color = OsrsGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    unlocks.forEach { unlock ->
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("✨", fontSize = 11.sp)
                                            Text(
                                                text = unlock,
                                                color = OsrsParchment,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Close / Claim Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isShaman) Color(0xFF2E7D32) else OsrsRedFrame
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isShaman) Color(0xFF70E000) else OsrsGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("claim_quest_rewards_button")
                    ) {
                        Text(
                            text = if (isShaman) "🌿 Claim Path Rewards & Continue" else "🎉 Claim Rewards & Complete",
                            color = if (isShaman) Color.White else OsrsTextYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    selectedRewardItemForDetail?.let { item ->
        UniqueItemDetailDialog(
            item = item,
            onDismissRequest = { selectedRewardItemForDetail = null }
        )
    }
}

@Composable
private fun SunburstRays(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sunburst_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier) {
        val rayCount = 12
        val rayAngle = 360f / rayCount
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        rotate(rotation, center) {
            for (i in 0 until rayCount) {
                if (i % 2 == 0) {
                    val angle1 = Math.toRadians((i * rayAngle).toDouble())
                    val angle2 = Math.toRadians(((i + 0.6f) * rayAngle).toDouble())
                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(
                            center.x + (radius * cos(angle1)).toFloat(),
                            center.y + (radius * sin(angle1)).toFloat()
                        )
                        lineTo(
                            center.x + (radius * cos(angle2)).toFloat(),
                            center.y + (radius * sin(angle2)).toFloat()
                        )
                        close()
                    }
                    drawPath(path, color = Color(0xFFFFD700).copy(alpha = 0.22f))
                }
            }
        }


    }
}

private data class ConfettiParticleData(
    val xRatio: Float,
    val speedY: Float,
    val flutterSpeed: Float,
    val initialOffset: Float,
    val sizePx: Float,
    val color: Color,
    val isCircle: Boolean,
    val rotationSpeed: Float
)

@Composable
private fun QuestCelebrationConfetti(modifier: Modifier = Modifier) {
    val particles = remember {
        val colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFE53935), // Crimson
            Color(0xFF43A047), // Emerald
            Color(0xFF8E24AA), // Royal Purple
            Color(0xFF00ACC1), // Cyan
            Color(0xFFFB8C00), // Orange
            Color(0xFFFFFFFF)  // Silver
        )
        List(50) {
            ConfettiParticleData(
                xRatio = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.4f + 0.6f,
                flutterSpeed = Random.nextFloat() * 10f + 5f,
                initialOffset = Random.nextFloat(),
                sizePx = Random.nextFloat() * 12f + 8f,
                color = colors.random(),
                isCircle = Random.nextBoolean(),
                rotationSpeed = Random.nextFloat() * 360f - 180f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti_anim")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animProgress"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val totalY = (p.initialOffset + animProgress * p.speedY) % 1.0f
            val yPos = totalY * h
            val flutter = sin((animProgress * p.flutterSpeed).toDouble()).toFloat() * 18f
            val xPos = (p.xRatio * w) + flutter
            val rotation = animProgress * p.rotationSpeed

            rotate(rotation, Offset(xPos, yPos)) {
                if (p.isCircle) {
                    drawCircle(
                        color = p.color,
                        radius = p.sizePx / 2f,
                        center = Offset(xPos, yPos)
                    )
                } else {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(xPos - p.sizePx / 2f, yPos - p.sizePx / 2f),
                        size = Size(p.sizePx, p.sizePx * 0.7f)
                    )
                }
            }
        }
    }
}
