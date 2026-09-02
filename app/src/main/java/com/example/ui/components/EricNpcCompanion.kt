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
 * Eric the NPC Dungeon Delver & Bounty Master:
 * A rugged, fearless adventurer wearing a brimmed explorer hat, carrying torches and ancient maps.
 * He guides the player through Bounty Slayer tasks, Boss Hunting raids, and Beast Tracking expeditions.
 * Freely draggable with persistent position memory and quick-snap corner controls.
 */
@Composable
fun EricNpcBadge(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier,
    currentSubTab: Int = 0 // 0 = Slayer, 1 = Bosses, 2 = Hunter
) {
    val isNpcEnabled by viewModel.isNpcCompanionsEnabled.collectAsState()
    val isSessionMinimized by viewModel.isEricSessionMinimized.collectAsState()

    if (!isNpcEnabled || isSessionMinimized) return

    val haptic = LocalHapticFeedback.current
    var showDialogueDialog by remember { mutableStateOf(false) }
    var currentDialogueIndex by remember { mutableIntStateOf(0) }
    var showMiniBubble by remember { mutableStateOf(true) }
    var isDragging by remember { mutableStateOf(false) }

    val slayerDialogues = remember {
        listOf(
            "Greetings, warrior! Eric here. Got your slayer helmet strapped tight? Dark beasts in the catacombs don't give second chances! 💀",
            "Always check your monster weaknesses before entering a dungeon chamber! Crushing maces for gargoyles, slashing scythes for abyssal fiends! ⚔️",
            "Bounties grant Slayer Points and trophy tokens. Save them up for master slayer gear and damage bonuses! 🛡️",
            "In the deep subterranean ruins, always keep a lit torch. Some shadows strike before you even hear footsteps! 🕯️",
            "Finished a bounty task? Return to the hall to claim your combat XP and pick your next contract! 📜"
        )
    }

    val bossDialogues = remember {
        listOf(
            "Heading into a Boss Lair? Watch their overhead prayer icons and telegraphed special attacks! 👑",
            "The ancient Dragon King guards legendary relic crystals. Pack high-tier cooked food and prayer potions! 🐉",
            "Boss fights require precision: time your ultimate ability activations when the boss enters enrage phase! ⚡",
            "Don't get greedy with damage! Stepping out of area-of-effect hazards keeps you alive to claim the loot chest! 💎",
            "I once soloed a subterranean Hydra in the lower catacombs. Key to victory: constant movement and clean prayer flicks! 🛡️"
        )
    }

    val hunterDialogues = remember {
        listOf(
            "Tracking wild beasts requires patience, partner! Check the wind direction so chinchompas don't catch your scent! 🐾",
            "Lay down your box traps and pitfall snares near animal watering holes. Bait them with fresh seeds and raw meat! 🪤",
            "Spotted kebbits and razor-winged falcons move fast. Stay crouched in the tall grass until the trigger snaps! 🦅",
            "Hunter XP adds up fast once you establish a multi-trap grid across the savannah! Keep your trap repair kits handy. 🌿",
            "Rare mutant beasts carry vibrant fur and exotic tusks. Keep an eye out for golden tracks in the dirt! ✨"
        )
    }

    val activeDialogues = when (currentSubTab) {
        1 -> bossDialogues
        2 -> hunterDialogues
        else -> slayerDialogues
    }

    val subTabTitle = when (currentSubTab) {
        1 -> "👑 Boss Hunting"
        2 -> "🐾 Beast Tracking"
        else -> "💀 Bounty Slayer"
    }

    val currentDialogue = activeDialogues[currentDialogueIndex % activeDialogues.size]

    // Floating pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "eric_bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -3.1f,
        targetValue = 3.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val torchGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
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
                val (savedNormX, savedNormY) = viewModel.getNpcPosition("eric", defaultNormalizedX = 0.82f, defaultNormalizedY = 0.70f)
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
                                viewModel.saveNpcPosition("eric", normX, normY)
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                        }
                    )
                }
                .padding(4.dp)
                .testTag("eric_npc_badge")
        ) {
            // --- TOP CONTROL BAR: DRAGGABLE HANDLE + 4 CORNER SNAP ARROWS + MINIMIZE BUTTON ---
            Surface(
                shape = RoundedCornerShape(7.dp),
                color = Color(0xF024160A),
                border = BorderStroke(if (isDragging) 1.5.dp else 1.dp, if (isDragging) Color(0xFFFFD166) else Color(0xFFFB8500)),
                shadowElevation = if (isDragging) 8.dp else 3.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isDragging) "✋ Dragging" else "✋ Drag",
                        color = Color(0xFFFFB703),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // 4 Arrow Buttons for quick snapping to all 4 corners
                    NpcScreenCorner.values().forEach { corner ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(3.5.dp))
                                .background(Color(0xFF381C10))
                                .border(0.5.dp, Color(0xFFD4A373), RoundedCornerShape(3.5.dp))
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
                                        viewModel.saveNpcPosition("eric", (targetX / maxDragX).coerceIn(0f, 1f), (targetY / maxDragY).coerceIn(0f, 1f))
                                    }
                                }
                                .testTag("eric_move_${corner.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = corner.arrowSymbol,
                                color = Color(0xFFFFE3A8),
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
                                viewModel.minimizeEricForSession()
                            }
                            .testTag("eric_minimize_button")
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
                    color = Color(0xEE2A180E),
                    border = BorderStroke(1.dp, Color(0xFFFFB703)),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .widthIn(max = 189.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentDialogueIndex = (currentDialogueIndex + 1) % activeDialogues.size
                        }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🗺️ Eric Delver",
                                color = Color(0xFFFFB703),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "tap to cycle",
                                color = Color(0xFFB08968),
                                fontSize = 7.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(1.5.dp))
                        Text(
                            text = currentDialogue,
                            color = Color(0xFFFFE8D6),
                            fontSize = 9.sp,
                            lineHeight = 12.sp
                        )
                    }
                }
            }

            // Eric Dungeon Delver Avatar (47dp - 10% smaller than 52dp)
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
                        color = Color(0xDD3A1C0A),
                        border = BorderStroke(0.8.dp, Color(0xFFFB8500)),
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        Text(
                            text = "Talk",
                            color = Color(0xFFFFB703),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }
                }

                // Circle Dungeon Delver Body with Explorer Hat & Torch Canvas (47dp)
                Box(
                    modifier = Modifier
                        .size(47.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFB703).copy(alpha = torchGlow),
                                    Color(0xFFFB8500).copy(alpha = 0.85f),
                                    Color(0xFF381C10)
                                )
                            )
                        )
                        .border(1.3.dp, Color(0xFFFFB703), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Golden torch glow circle
                        drawCircle(
                            color = Color(0x33FFB703),
                            radius = w * 0.44f,
                            style = Stroke(width = 1.8f)
                        )

                        // Confident warrior eyes
                        drawCircle(
                            color = Color(0xFF241408),
                            radius = 2.9f,
                            center = Offset(w * 0.38f, h * 0.48f)
                        )
                        drawCircle(
                            color = Color(0xFF241408),
                            radius = 2.9f,
                            center = Offset(w * 0.62f, h * 0.48f)
                        )
                        // Determined glint
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

                        // Determined smirk/smile
                        val smirkPath = Path().apply {
                            moveTo(w * 0.38f, h * 0.62f)
                            quadraticBezierTo(w * 0.52f, h * 0.68f, w * 0.64f, h * 0.60f)
                        }
                        drawPath(smirkPath, color = Color(0xFF241408), style = Stroke(width = 2.2f))
                    }

                    // Explorer hat & sword / torch overlay
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "🤠",
                            fontSize = 16.sp,
                            modifier = Modifier.offset(y = (-3.5).dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "⚔️",
                            fontSize = 11.sp,
                            modifier = Modifier.offset(x = 12.dp, y = (-2).dp)
                        )
                    }
                }

                if (horizontalAlign == Alignment.Start) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xDD3A1C0A),
                        border = BorderStroke(0.8.dp, Color(0xFFFB8500)),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Text(
                            text = "Talk",
                            color = Color(0xFFFFB703),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }
                }
            }
        }
    }

    // Eric Full Dialogue Dialog
    if (showDialogueDialog) {
        Dialog(onDismissRequest = { showDialogueDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1208)),
                border = BorderStroke(2.dp, Color(0xFFFFB703)),
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
                    // Header with Eric Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🗺️", fontSize = 22.sp)
                            Column {
                                Text(
                                    text = "Eric the Dungeon Delver",
                                    color = Color(0xFFFFB703),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Master of Slayer Bounties & Labyrinth Relics",
                                    color = Color(0xFFD4A373),
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

                    // Large Animated Eric Portrait
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFB703),
                                        Color(0xFFFB8500),
                                        Color(0xFF381C10)
                                    )
                                )
                            )
                            .border(2.dp, Color(0xFFFFB703), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤠", fontSize = 34.sp, modifier = Modifier.offset(y = (-10).dp))
                        Text("🗡️", fontSize = 22.sp, modifier = Modifier.offset(x = 18.dp, y = 14.dp))
                    }

                    // Sub-tab context badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF381C10),
                        border = BorderStroke(1.dp, Color(0xFFFB8500))
                    ) {
                        Text(
                            text = "Current Focus: $subTabTitle",
                            color = Color(0xFFFFD166),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Favor Level Card (Clickable to view all 50 rewards)
                    var showEricRewardsDialog by remember { mutableStateOf(false) }
                    val favorMap by viewModel.npcFavorMap.collectAsState()
                    val favorPair = favorMap["eric"] ?: Pair(1, 0L)
                    val favorLevel = favorPair.first.coerceIn(1, 50)
                    val favorXp = favorPair.second
                    val reqXp = viewModel.getRequiredXpForFavorLevel(favorLevel)

                    // Compact Formatted Favor Level Panel (Single View Rewards Button)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1C12)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF8B6508)),
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
                                    Text("🤝 Favor Lv.$favorLevel/50", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    if (favorLevel >= 50) {
                                        Text("🏆", fontSize = 11.sp)
                                    }
                                }
                                FilledTonalButton(
                                    onClick = { showEricRewardsDialog = true },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF5D3A1A),
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
                                color = Color(0xFFFFD700),
                                trackColor = Color(0xFF1E140C)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (favorLevel >= 50) "Max Level 50 Mastered!" else "$favorXp / $reqXp XP",
                                    color = Color(0xFFFFF9C4),
                                    fontSize = 9.5.sp
                                )
                                if (favorLevel < 50) {
                                    Text(
                                        text = "${((favorXp.toFloat() / reqXp.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f) * 100).toInt()}%",
                                        color = Color(0xFFFFCC80),
                                        fontSize = 9.5.sp
                                    )
                                }
                            }
                        }
                    }

                    if (showEricRewardsDialog) {
                        NpcFavorRewardsDialog(
                            npcId = "eric",
                            npcName = "Eric the Dungeon Delver",
                            npcEmoji = "🤠",
                            currentLevel = favorLevel,
                            currentXp = favorXp,
                            reqXp = reqXp,
                            onDismiss = { showEricRewardsDialog = false },
                            onOfferTribute = {
                                viewModel.addNpcFavorXp("eric", 50L, "Eric", "Bounty Tribute")
                            }
                        )
                    }

                    // Compact Active Favors Section
                    NpcActiveFavorsCompactSection(
                        npcId = "eric",
                        npcName = "Eric",
                        viewModel = viewModel
                    )

                    // Main Dialogue Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF120A04),
                        border = BorderStroke(1.dp, Color(0xFF8D5B28)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${activeDialogues[currentDialogueIndex % activeDialogues.size]}\"",
                            color = Color(0xFFFFE8D6),
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
                            npcId = "eric",
                            npcName = "Eric",
                            viewModel = viewModel
                        )

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentDialogueIndex = (currentDialogueIndex + 1) % activeDialogues.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB8500)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💭 Ask for another bounty & dungeon tip", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentDialogueIndex = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A2810)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🐉 Hear Eric's Catacomb Hydra battle story", color = Color(0xFFFFE3A8), fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showDialogueDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                            border = BorderStroke(1.dp, Color(0xFFFFB703).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("👋 Farewell for now, Eric!", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
