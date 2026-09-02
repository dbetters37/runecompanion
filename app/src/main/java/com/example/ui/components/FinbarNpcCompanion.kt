package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel
import kotlin.math.roundToInt

/**
 * Screen corners for NPC positioning
 */
enum class NpcScreenCorner(val label: String, val arrowSymbol: String) {
    TOP_LEFT("Top Left", "↖"),
    TOP_RIGHT("Top Right", "↗"),
    BOTTOM_LEFT("Bottom Left", "↙"),
    BOTTOM_RIGHT("Bottom Right", "↘")
}

/**
 * Finbar the NPC Spirit Angler:
 * A friendly, circle-shaped translucent water spirit wearing a cozy fishing bucket hat.
 * He appears in the Shaman Pool tab with freeform dragging anywhere on screen,
 * saved position memory, quick corner snap buttons, session minimization, and interactive lore.
 */
@Composable
fun FinbarNpcBadge(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier,
    currentTabContext: String = "Shaman Pool"
) {
    val isNpcEnabled by viewModel.isNpcCompanionsEnabled.collectAsState()
    val isSessionMinimized by viewModel.isFinbarSessionMinimized.collectAsState()

    if (!isNpcEnabled || isSessionMinimized) return

    val haptic = LocalHapticFeedback.current
    var showDialogueDialog by remember { mutableStateOf(false) }
    var currentDialogueIndex by remember { mutableIntStateOf(0) }
    var showMiniBubble by remember { mutableStateOf(true) }
    var isDragging by remember { mutableStateOf(false) }

    val fishingDialogues = remember {
        listOf(
            "Ahoy, spirit friend! The currents in the Shaman Pool are singing today. Cast your net where the ripples dance! 🎣",
            "Old Finbar here! Back in my mortal days, I once caught a manta ray bigger than a karambwan boat! 🐟",
            "Patience is the angler's greatest incantation. Still your breath and the spirit fish will come right to your hook. 🌊",
            "Did you know? Raw fish aren't just for frying—communing with sacred water spirits heals your inner essence! ✨",
            "Keep your hook sharp and your spirit calm. The legendary catches only bite when the lake is glass! 🪝",
            "The ancient shaman pool is deep, traveler. Strange relics from forgotten ages lie at the bottom. Keep casting!"
        )
    }

    val currentDialogue = fishingDialogues[currentDialogueIndex % fishingDialogues.size]

    // Floating bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "finbar_bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current
        val maxContainerWidthPx = with(density) { maxWidth.toPx() }
        val maxContainerHeightPx = with(density) { maxHeight.toPx() }

        // Scaled dimensions (10% smaller than previous 210dp / 150dp)
        val badgeWidthPx = with(density) { 189.dp.toPx() }
        val badgeHeightPx = with(density) { 135.dp.toPx() }

        val maxDragX = (maxContainerWidthPx - badgeWidthPx).coerceAtLeast(0f)
        val maxDragY = (maxContainerHeightPx - badgeHeightPx).coerceAtLeast(0f)

        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        var hasInitializedPosition by remember { mutableStateOf(false) }

        LaunchedEffect(maxDragX, maxDragY) {
            if (!hasInitializedPosition && maxDragX > 0f && maxDragY > 0f) {
                val (savedNormX, savedNormY) = viewModel.getNpcPosition("finbar", defaultNormalizedX = 0.82f, defaultNormalizedY = 0.70f)
                offsetX = (savedNormX * maxDragX).coerceIn(0f, maxDragX)
                offsetY = (savedNormY * maxDragY).coerceIn(0f, maxDragY)
                hasInitializedPosition = true
            }
        }

        val isNearLeft = if (maxDragX > 0f) (offsetX / maxDragX) < 0.5f else false
        val horizontalAlign = if (isNearLeft) Alignment.Start else Alignment.End

        Column(
            horizontalAlignment = horizontalAlign,
            verticalArrangement = Arrangement.spacedBy(3.5.dp),
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(maxDragX, maxDragY) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxDragX)
                            offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxDragY)
                        },
                        onDragEnd = {
                            isDragging = false
                            if (maxDragX > 0f && maxDragY > 0f) {
                                val normX = (offsetX / maxDragX).coerceIn(0f, 1f)
                                val normY = (offsetY / maxDragY).coerceIn(0f, 1f)
                                viewModel.saveNpcPosition("finbar", normX, normY)
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                        }
                    )
                }
                .padding(4.dp)
                .testTag("finbar_npc_badge")
        ) {
            // --- TOP CONTROL BAR: DRAGGABLE HANDLE + 4 CORNER SNAP ARROWS + MINIMIZE BUTTON ---
            Surface(
                shape = RoundedCornerShape(7.dp),
                color = Color(0xF00A1A24),
                border = BorderStroke(if (isDragging) 1.5.dp else 1.dp, if (isDragging) Color(0xFF90E0EF) else Color(0xFF00B4D8)),
                shadowElevation = if (isDragging) 8.dp else 3.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isDragging) "✋ Dragging" else "✋ Drag",
                        color = Color(0xFF90E0EF),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // 4 Arrow Buttons for quick snap to all 4 corners
                    NpcScreenCorner.values().forEach { corner ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(3.5.dp))
                                .background(Color(0xFF03045E))
                                .border(0.5.dp, Color(0xFF0096C7), RoundedCornerShape(3.5.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val targetX = when (corner) {
                                        NpcScreenCorner.TOP_LEFT, NpcScreenCorner.BOTTOM_LEFT -> 0f
                                        NpcScreenCorner.TOP_RIGHT, NpcScreenCorner.BOTTOM_RIGHT -> maxDragX
                                    }
                                    val targetY = when (corner) {
                                        NpcScreenCorner.TOP_LEFT, NpcScreenCorner.TOP_RIGHT -> 0f
                                        NpcScreenCorner.BOTTOM_LEFT, NpcScreenCorner.BOTTOM_RIGHT -> maxDragY
                                    }
                                    offsetX = targetX
                                    offsetY = targetY
                                    if (maxDragX > 0f && maxDragY > 0f) {
                                        viewModel.saveNpcPosition("finbar", (targetX / maxDragX).coerceIn(0f, 1f), (targetY / maxDragY).coerceIn(0f, 1f))
                                    }
                                }
                                .testTag("finbar_move_${corner.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = corner.arrowSymbol,
                                color = Color(0xFFCAF0F8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(1.5.dp))

                    // Minimize Button (Hides NPC until next app restart)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.5.dp))
                            .background(Color(0xFF5A1827))
                            .border(0.8.dp, Color(0xFFFF4D6D), RoundedCornerShape(3.5.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.minimizeFinbarForSession()
                            }
                            .testTag("finbar_minimize_button")
                            .padding(horizontal = 4.dp, vertical = 1.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                        ) {
                            Text("━", color = Color(0xFFFFB3C1), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("Hide", color = Color(0xFFFFCCD5), fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Interactive Speech Bubble (10% smaller)
            AnimatedVisibility(
                visible = showMiniBubble,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = Color(0xEE142428),
                    border = BorderStroke(1.dp, Color(0xFF48CAE4)),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .widthIn(max = 189.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentDialogueIndex = (currentDialogueIndex + 1) % fishingDialogues.size
                        }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🎣 Finbar the Spirit",
                                color = Color(0xFF90E0EF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "tap to cycle",
                                color = Color(0xFF709090),
                                fontSize = 7.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(1.5.dp))
                        Text(
                            text = currentDialogue,
                            color = Color(0xFFCAF0F8),
                            fontSize = 9.sp,
                            lineHeight = 12.sp
                        )
                    }
                }
            }

            // Finbar Circle Spirit Avatar (47dp - 10% smaller than 52dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .offset(y = bounceOffset.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDialogueDialog = true
                    }
            ) {
                if (horizontalAlign == Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xCC03045E),
                        border = BorderStroke(0.8.dp, Color(0xFF00B4D8)),
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        Text(
                            text = "Talk",
                            color = Color(0xFF90E0EF),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }
                }

                // Circle Spirit Body with Fishing Hat Canvas (47dp)
                Box(
                    modifier = Modifier
                        .size(47.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF90E0EF).copy(alpha = glowAlpha),
                                    Color(0xFF0077B6).copy(alpha = 0.85f),
                                    Color(0xFF03045E)
                                )
                            )
                        )
                        .border(1.3.dp, Color(0xFF48CAE4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Water swirl aura
                        drawCircle(
                            color = Color(0x44CAF0F8),
                            radius = w * 0.42f,
                            style = Stroke(width = 1.8f)
                        )

                        // Friendly spirit eyes
                        drawCircle(
                            color = Color(0xFF03045E),
                            radius = 3.1f,
                            center = Offset(w * 0.38f, h * 0.48f)
                        )
                        drawCircle(
                            color = Color(0xFF03045E),
                            radius = 3.1f,
                            center = Offset(w * 0.62f, h * 0.48f)
                        )
                        // Eye glints
                        drawCircle(
                            color = Color.White,
                            radius = 1.1f,
                            center = Offset(w * 0.36f, h * 0.46f)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 1.1f,
                            center = Offset(w * 0.60f, h * 0.46f)
                        )

                        // Happy smile
                        val smilePath = Path().apply {
                            moveTo(w * 0.38f, h * 0.62f)
                            quadraticBezierTo(w * 0.5f, h * 0.72f, w * 0.62f, h * 0.62f)
                        }
                        drawPath(smilePath, color = Color(0xFF03045E), style = Stroke(width = 2.2f))
                    }

                    // Fishing bucket hat overlay emoji
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "👒",
                            fontSize = 16.sp,
                            modifier = Modifier.offset(y = (-3.5).dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "🎣",
                            fontSize = 11.sp,
                            modifier = Modifier.offset(x = 12.dp, y = (-2).dp)
                        )
                    }
                }

                if (horizontalAlign == Alignment.Start) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xCC03045E),
                        border = BorderStroke(0.8.dp, Color(0xFF00B4D8)),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Text(
                            text = "Talk",
                            color = Color(0xFF90E0EF),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }
                }
            }
        }
    }

    // Finbar Full Dialogue Dialog
    if (showDialogueDialog) {
        Dialog(onDismissRequest = { showDialogueDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E24)),
                border = BorderStroke(2.dp, Color(0xFF48CAE4)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header with Finbar Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🎣", fontSize = 22.sp)
                            Column {
                                Text(
                                    text = "Finbar the Angler Spirit",
                                    color = Color(0xFF90E0EF),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Keeper of the Shaman Waters",
                                    color = Color(0xFF00B4D8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = { showDialogueDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    // Large Animated Finbar Spirit Portrait
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF90E0EF),
                                        Color(0xFF0077B6),
                                        Color(0xFF03045E)
                                    )
                                )
                            )
                            .border(2.dp, Color(0xFF48CAE4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👒", fontSize = 32.sp, modifier = Modifier.offset(y = (-14).dp))
                        Text("👻", fontSize = 36.sp, modifier = Modifier.offset(y = 6.dp))
                    }

                    // Favor Level Card (Clickable to view all 50 rewards)
                    var showFinbarRewardsDialog by remember { mutableStateOf(false) }
                    val favorMap by viewModel.npcFavorMap.collectAsState()
                    val favorPair = favorMap["finbar"] ?: Pair(1, 0L)
                    val favorLevel = favorPair.first.coerceIn(1, 50)
                    val favorXp = favorPair.second
                    val reqXp = viewModel.getRequiredXpForFavorLevel(favorLevel)

                    // Compact Formatted Favor Level Panel (Single View Rewards Button)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF032230)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF48CAE4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🤝 Favor Lv.$favorLevel/50", color = Color(0xFF90E0EF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    if (favorLevel >= 50) {
                                        Text("🏆", fontSize = 11.sp)
                                    }
                                }
                                FilledTonalButton(
                                    onClick = { showFinbarRewardsDialog = true },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF0077B6),
                                        contentColor = Color(0xFFFFD54F)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("🎁 View Rewards", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = {
                                    if (favorLevel >= 50) 1f
                                    else (favorXp.toFloat() / reqXp.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFF48CAE4),
                                trackColor = Color(0xFF02131D)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (favorLevel >= 50) "Max Level 50 Spirit Master!" else "$favorXp / $reqXp XP",
                                    color = Color(0xFFE0F7FA),
                                    fontSize = 9.5.sp
                                )
                                if (favorLevel < 50) {
                                    Text(
                                        text = "${((favorXp.toFloat() / reqXp.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f) * 100).toInt()}%",
                                        color = Color(0xFF90E0EF),
                                        fontSize = 9.5.sp
                                    )
                                }
                            }
                        }
                    }

                    if (showFinbarRewardsDialog) {
                        NpcFavorRewardsDialog(
                            npcId = "finbar",
                            npcName = "Finbar the Angler Spirit",
                            npcEmoji = "🎣",
                            currentLevel = favorLevel,
                            currentXp = favorXp,
                            reqXp = reqXp,
                            onDismiss = { showFinbarRewardsDialog = false },
                            onOfferTribute = {
                                viewModel.addNpcFavorXp("finbar", 50L, "Finbar", "Spirit Offering")
                            }
                        )
                    }

                    // Compact Active Favors Section
                    NpcActiveFavorsCompactSection(
                        npcId = "finbar",
                        npcName = "Finbar",
                        viewModel = viewModel
                    )

                    // Main Dialogue Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF05131A),
                        border = BorderStroke(1.dp, Color(0xFF0077B6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${fishingDialogues[currentDialogueIndex % fishingDialogues.size]}\"",
                            color = Color(0xFFCAF0F8),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    // Dialogue Choice Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Captain Barnaby Parcel Dispatch Deliveries
                        NpcParcelActionButtons(
                            npcId = "finbar",
                            npcName = "Finbar",
                            viewModel = viewModel
                        )

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentDialogueIndex = (currentDialogueIndex + 1) % fishingDialogues.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💭 Ask for another fishing & spirit tip", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentDialogueIndex = 1
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF023E8A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🐟 Hear Finbar's giant leviathan story", color = Color(0xFFCAF0F8), fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showDialogueDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                            border = BorderStroke(1.dp, Color(0xFF48CAE4).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("👋 Farewell for now, Finbar!", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
