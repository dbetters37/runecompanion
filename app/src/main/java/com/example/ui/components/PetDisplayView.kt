package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PetEmote
import com.example.data.models.PetMoodState
import com.example.data.models.PetState
import com.example.data.models.PohHouseState
import com.example.ui.theme.*
import kotlin.math.roundToInt

data class AfkActivityMetaData(
    val id: String,
    val name: String,
    val emoji: String,
    val skillName: String
)

val AFK_ACTIVITIES_MAP = mapOf(
    "woodcutting" to AfkActivityMetaData("woodcutting", "Woodcutting Grove", "🌳", "Harvesting"),
    "fishing" to AfkActivityMetaData("fishing", "POH Fishing Pond", "🎣", "Fishing"),
    "mining" to AfkActivityMetaData("mining", "POH Quarry", "⛏️", "Gemology"),
    "campfire" to AfkActivityMetaData("campfire", "Campfire Range", "🔥", "Summoning"),
    "cooking" to AfkActivityMetaData("cooking", "Kitchen Range", "🍳", "Cooking"),
    "smelting" to AfkActivityMetaData("smelting", "Smelting Furnace", "⚒️", "Forging"),
    "sawmill" to AfkActivityMetaData("sawmill", "Sawmill Planks", "🪚", "Hut-Keeping"),
    "fletching" to AfkActivityMetaData("fletching", "Arrow Fletching", "🏹", "Whittling"),
    "thieving" to AfkActivityMetaData("thieving", "Pickpocketing", "🕵️", "Trickery"),
    "slayer" to AfkActivityMetaData("slayer", "Slayer Task", "⚔️", "Bounty Hunter"),
    "hunter" to AfkActivityMetaData("hunter", "Hunter Tracking", "🐾", "Beast Tracking"),
    "boss" to AfkActivityMetaData("boss", "Boss Fight", "☠️", "Bossing"),
    "druid_altar" to AfkActivityMetaData("druid_altar", "Druid Altar", "🌿", "Summoning"),
    "catacombs" to AfkActivityMetaData("catacombs", "Catacombs", "🗿", "Trickery"),
    "sailing" to AfkActivityMetaData("sailing", "Ocean Rowing", "⛵", "Navigation"),
    "bone_burying" to AfkActivityMetaData("bone_burying", "Bone Burying", "🦴", "Magic"),
    "runecrafting" to AfkActivityMetaData("runecrafting", "Runemaking Altar", "🔮", "Runemaking"),
    "farming" to AfkActivityMetaData("farming", "Agriculture Patch", "🌾", "Agriculture")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PetDisplayView(
    petState: PetState,
    totalLevel: Int = 0,
    maxPetHp: Int = 100,
    pohState: PohHouseState? = null,
    petMoodState: PetMoodState = PetMoodState(),
    currentActivityText: String? = null,
    recentAfkHistory: List<String> = emptyList(),
    activeAfkName: String? = null,
    onQuickFeed: () -> Unit,
    onQuickPlay: () -> Unit = {},
    onPetTouch: () -> Unit,
    onOpenPetSelector: () -> Unit,
    onOpenPoh: () -> Unit = {},
    onSendChatMessage: (String) -> Unit = {},
    onToggleMute: () -> Unit = {},
    onResetPetXp: () -> Unit = {},
    onOpenMasterControlPanel: () -> Unit = {},
    onBoostMood: () -> Unit = {},
    onEvolvePet: () -> Unit = {},
    onOpenDailySpiritQuests: () -> Unit = {},
    onStartAfkActivity: (String) -> Unit = {},
    onStopAllAfk: () -> Unit = {},
    queuedFoodItem: com.example.data.models.InventoryItem? = null,
    allCookedFoods: List<com.example.data.models.InventoryItem> = emptyList(),
    lastUsedTotem: com.example.data.models.SummonableAnimal = com.example.data.models.SummoningData.ALL_ANIMALS.first(),
    totemStockCount: Int = 0,
    activeSummon: com.example.data.models.ActiveSummoningCompanion? = null,
    allTotems: List<com.example.data.models.SummonableAnimal> = com.example.data.models.SummoningData.ALL_ANIMALS,
    getTotemCount: (String) -> Int = { 0 },
    onFeedQueuedFood: (com.example.data.models.InventoryItem?) -> Unit = { onQuickFeed() },
    onSelectQueuedFood: (com.example.data.models.InventoryItem?) -> Unit = {},
    onUseTotem: (String) -> Unit = {},
    onSelectTotem: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current

    // 60fps continuous infinite animation for Shaman ritual aura, bounce & pacing
    val infiniteTransition = rememberInfiniteTransition(label = "pet_shaman_60fps_anim")

    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val pacingOffset by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pacing"
    )

    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_rotation"
    )

    val auraPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    var topPromptText by remember { mutableStateOf("") }
    var tapFlipCount by remember { mutableIntStateOf(0) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // User drag offsets for grabbing and moving pet avatar
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val petFlipRotation by animateFloatAsState(
        targetValue = tapFlipCount * 360f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "pet_flip"
    )

    OvergrownStoneCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        showRunes = true,
        showVineCorners = false
    ) {
        // Reformatted Unified Top Companion Box
        OvergrownStoneCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 10.dp,
            showRunes = true,
            showVineCorners = false
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Top Title Row: Pet Name, Class & Settings Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenPetSelector() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2A1C12))
                                .border(1.dp, OsrsGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = petState.petType.iconSymbol, fontSize = 20.sp)
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = petState.customName,
                                    color = OsrsTextYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("🐾", fontSize = 11.sp)
                            }
                            Text(
                                text = "${petState.petType.displayName} • ${petState.petType.primarySkill.displayName} Pet",
                                color = OsrsParchment,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Settings Icon Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF381C10))
                            .border(1.dp, OsrsGold, CircleShape)
                            .clickable { onOpenMasterControlPanel() }
                            .testTag("header_settings_reset_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚙️", fontSize = 13.sp)
                    }
                }

                // 2. Action Buttons Row: Switch Pet & Floating Bubble
                val isOverlayActive by com.example.services.FloatingPetOverlayService.isRunning.collectAsState(initial = false)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Switch Pet Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenPetSelector()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("header_switch_pet_button")
                    ) {
                        Text("Switch Pet 🐾", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Overlay Bubble Toggle Button
                    Button(
                        onClick = {
                            if (isOverlayActive) {
                                val serviceIntent = android.content.Intent(context, com.example.services.FloatingPetOverlayService::class.java)
                                context.stopService(serviceIntent)
                            } else {
                                if (!android.provider.Settings.canDrawOverlays(context)) {
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } else {
                                    val serviceIntent = android.content.Intent(context, com.example.services.FloatingPetOverlayService::class.java)
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        context.startForegroundService(serviceIntent)
                                    } else {
                                        context.startService(serviceIntent)
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOverlayActive) Color(0xFF0284C7) else Color(0xFF1E5628)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("show_overlay_button")
                    ) {
                        Text(if (isOverlayActive) "🔮 Bubble ON" else "🔮 Bubble", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 3. Status Badge: Current Activity
                val actText = currentActivityText ?: "💤 Idle"
                val isActive = !actText.startsWith("💤")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActive) Color(0xFF1B5E20) else Color(0xFF3E2D1D))
                        .border(
                            1.dp,
                            if (isActive) Color(0xFF81C784) else OsrsGold.copy(alpha = 0.6f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⚡ Current Activity: $actText",
                        color = if (isActive) Color(0xFF81C784) else OsrsGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // POH Visual House Backdrop Enclosure
        val builtRooms = pohState?.builtRooms ?: emptyList()
        val totalFurnitureCount = builtRooms.sumOf { it.builtFurnitureIds.size }
        val builtRoomIcons = builtRooms.map { it.roomType.iconEmoji }.distinct()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2E2218),
                            Color(0xFF1B140D)
                        )
                    )
                )
                .border(1.5.dp, OsrsGold, RoundedCornerShape(10.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background 60fps Shaman Animation Canvas to ensure continuous shaman ritual animation on home screen
            HomeScreenShamanAnimationCanvas(
                auraRotation = auraRotation,
                auraPulseScale = auraPulseScale,
                modifier = Modifier.matchParentSize()
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Top row displaying built house room icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (builtRoomIcons.isEmpty()) {
                        Text("🏡 Standard Wooden Parlour", color = OsrsParchment, fontSize = 10.sp)
                    } else {
                        builtRoomIcons.take(6).forEach { symbol ->
                            Text(text = symbol, fontSize = 15.sp)
                        }
                    }
                }

                // Compact Pet Sprite Box
                Box(
                    modifier = Modifier
                        .offset { IntOffset(dragOffsetX.roundToInt(), dragOffsetY.roundToInt()) }
                        .size(100.dp)
                        .scale(auraPulseScale)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                dragOffsetX += dragAmount.x
                                dragOffsetY += dragAmount.y
                            }
                        }
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            tapFlipCount++
                            onPetTouch()
                        }
                        .testTag("pet_sprite_touch"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .offset(x = pacingOffset.dp, y = bounceOffset.dp)
                            .rotate(petFlipRotation)
                    ) {
                        PetSpriteRenderer(
                            petType = petState.petType,
                            sizeDp = 70.dp,
                            petState = petState,
                            petMoodState = petMoodState
                        )
                    }
                }

                // Bottom House Upgrades Status Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF38291B))
                        .border(1.dp, OsrsGold, RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenPoh()
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🏰 POH: ${builtRooms.size} Rooms • $totalFurnitureCount Furniture",
                            color = OsrsTextYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "➕ Add Rooms ➔",
                            color = OsrsGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pet Care Status Bars Grid
        val maxHunger = 100 + totalLevel
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBar(
                    label = "HP",
                    value = petState.health,
                    maxValue = maxPetHp,
                    color = Color(0xFFE63946),
                    modifier = Modifier.weight(1f)
                )
                StatusBar(
                    label = "Motivation / Happiness",
                    value = petState.happiness,
                    color = Color(0xFF2A9D8F),
                    iconResId = com.example.R.drawable.ic_sableye_head,
                    modifier = Modifier.weight(1f)
                )
            }
            StatusBar(
                label = "Hunger (Total Level Scaled)",
                value = petState.hunger,
                maxValue = maxHunger,
                color = Color(0xFFF4A261),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Two Square Quick Panels: Food Queue on Left, Last Used Totem on Right
        CompanionQuickPanels(
            queuedFood = queuedFoodItem,
            allCookedFoods = allCookedFoods,
            lastUsedTotem = lastUsedTotem,
            totemStockCount = totemStockCount,
            activeSummon = activeSummon,
            allTotems = allTotems,
            getTotemCount = getTotemCount,
            onFeedQueuedFood = onFeedQueuedFood,
            onSelectQueuedFood = onSelectQueuedFood,
            onUseTotem = onUseTotem,
            onSelectTotem = onSelectTotem,
            modifier = Modifier.fillMaxWidth()
        )

        // Additional Actions & Evolution
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Evolution Action Banner if pet has next form
            val nextForm = petState.petType.evolvesTo
            if (nextForm != null) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEvolvePet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, OsrsGoldBright, RoundedCornerShape(8.dp))
                        .testTag("evolve_pet_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🌟", fontSize = 16.sp)
                        Text(
                            text = "EVOLVE TO ${nextForm.displayName.uppercase()} (Lvl ${petState.petType.evolutionLevelReq} ${petState.petType.primarySkill.displayName})",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- QUICK AFK ACTIVITIES & REPEAT LAST CARD ---
        val lastAfkId = recentAfkHistory.firstOrNull() ?: "woodcutting"
        val lastAfkMeta = AFK_ACTIVITIES_MAP[lastAfkId] ?: AfkActivityMetaData("woodcutting", "Woodcutting Grove", "🌳", "Woodcutting")
        val isAnyAfkActive = activeAfkName != null

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1712)),
            border = BorderStroke(1.5.dp, if (isAnyAfkActive) Color(0xFF81C784) else OsrsGold),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("⚡", fontSize = 16.sp)
                        Text(
                            "QUICK AFK ACTIVITIES",
                            color = OsrsTextYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isAnyAfkActive) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1B5E20),
                            border = BorderStroke(1.dp, Color(0xFF81C784))
                        ) {
                            Text(
                                "🟢 ACTIVE",
                                color = Color(0xFF81C784),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Primary "Repeat Last AFK" or "Stop AFK" Button
                Button(
                    onClick = {
                        if (isAnyAfkActive) {
                            onStopAllAfk()
                        } else {
                            onStartAfkActivity(lastAfkMeta.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAnyAfkActive) Color(0xFFB91C1C) else Color(0xFF2E6B38)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isAnyAfkActive) Color(0xFFEF4444) else OsrsGoldBright,
                            RoundedCornerShape(8.dp)
                        )
                        .testTag("repeat_last_afk_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isAnyAfkActive) {
                            Text(
                                "⏹️ STOP CURRENT AFK (${activeAfkName ?: "Active"})",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                "🔁 DO LAST AFK AGAIN: ${lastAfkMeta.emoji} ${lastAfkMeta.name}",
                                color = OsrsTextYellow,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Recent AFK Activities Selector Row
                if (recentAfkHistory.isNotEmpty()) {
                    Text(
                        "MOST RECENT ACTIVITIES:",
                        color = OsrsParchment,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        recentAfkHistory.distinct().take(6).forEach { activityId ->
                            val meta = AFK_ACTIVITIES_MAP[activityId] ?: return@forEach
                            val isActiveThis = isAnyAfkActive && activeAfkName?.lowercase()?.contains(meta.skillName.lowercase()) == true

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isActiveThis) Color(0xFF1B5E20) else Color(0xFF2B1F17),
                                border = BorderStroke(
                                    1.dp,
                                    if (isActiveThis) Color(0xFF81C784) else Color(0xFF5A4432)
                                ),
                                modifier = Modifier.clickable {
                                    onStartAfkActivity(meta.id)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(meta.emoji, fontSize = 12.sp)
                                    Text(
                                        meta.name,
                                        color = if (isActiveThis) Color(0xFF81C784) else OsrsTextWhite,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (isActiveThis) {
                                        Text("⚡", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                title = {
                    Text(
                        text = "Reset ${petState.customName}'s XP?",
                        color = OsrsGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to reset ${petState.customName}'s (${petState.petType.primarySkill.displayName}) XP back to Level 1 (0 XP) and restore care stats? Other pets will keep their own stats when switching.",
                        color = OsrsParchment,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetConfirmDialog = false
                            onResetPetXp()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C))
                    ) {
                        Text("Yes, Reset XP", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showResetConfirmDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF221A14)
            )
        }
    }
}

@Composable
fun StatusBar(
    label: String,
    value: Int,
    color: Color,
    maxValue: Int = 100,
    iconResId: Int? = null,
    modifier: Modifier = Modifier
) {
    val fraction = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (iconResId != null) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = label,
                        tint = OsrsGold,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(end = 3.dp)
                    )
                }
                Text(text = label, color = OsrsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            val pct = if (maxValue > 0) ((value.toFloat() / maxValue.toFloat()) * 100).toInt() else value
            Text(text = if (maxValue == 100) "$value%" else "$value/$maxValue ($pct%)", color = OsrsTextWhite, fontSize = 10.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(OsrsLeatherDark)
                .border(1.dp, Color(0xFF423528), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun ShamanicTreantSprite(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 90.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shaman_tangleroot_anim")
    val eyeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eye_glow"
    )
    val bodySwayY by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "body_sway"
    )
    val leafRotate by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf_rotate"
    )

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = bodySwayY }
        ) {
            val w = size.width
            val h = size.height

            // 1. Soft Shamanic Emerald Ambient Magic Aura (No harsh box/border)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x5552B788),
                        Color(0x112E6B38),
                        Color.Transparent
                    )
                ),
                radius = w * 0.45f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f)
            )

            // Color Palette
            val barkDark = Color(0xFF26180E)
            val barkMid = Color(0xFF4A301C)
            val barkHighlight = Color(0xFF6B482B)
            val mossDark = Color(0xFF2A5220)
            val mossLight = Color(0xFF52B788)
            val leafGreen = Color(0xFF70E000)
            val leafGlow = Color(0xFFCCFF00)
            val eyeColor = Color(0xFF00FFCC)

            // 2. Root Feet
            val leftRoot = Path().apply {
                moveTo(w * 0.35f, h * 0.75f)
                quadraticTo(w * 0.25f, h * 0.88f, w * 0.20f, h * 0.92f)
                lineTo(w * 0.38f, h * 0.90f)
                close()
            }
            val rightRoot = Path().apply {
                moveTo(w * 0.65f, h * 0.75f)
                quadraticTo(w * 0.75f, h * 0.88f, w * 0.80f, h * 0.92f)
                lineTo(w * 0.62f, h * 0.90f)
                close()
            }
            drawPath(leftRoot, barkDark)
            drawPath(rightRoot, barkDark)

            // 3. Legs & Lower Trunk
            drawRect(
                color = barkDark,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.65f),
                size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.20f)
            )
            drawRect(
                color = barkDark,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.53f, h * 0.65f),
                size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.20f)
            )

            // 4. Main Treant Gnarled Torso
            val torsoPath = Path().apply {
                moveTo(w * 0.30f, h * 0.40f)
                lineTo(w * 0.70f, h * 0.40f)
                lineTo(w * 0.65f, h * 0.72f)
                lineTo(w * 0.35f, h * 0.72f)
                close()
            }
            drawPath(torsoPath, barkMid)

            // Torso Bark Texture Lines
            drawLine(barkHighlight, androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.45f), androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.68f), strokeWidth = w * 0.03f)
            drawLine(barkDark, androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.42f), androidx.compose.ui.geometry.Offset(w * 0.52f, h * 0.70f), strokeWidth = w * 0.03f)
            drawLine(barkHighlight, androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.45f), androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.68f), strokeWidth = w * 0.03f)

            // Moss on Shoulders and Chest
            drawCircle(mossLight, radius = w * 0.08f, center = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.42f))
            drawCircle(mossLight, radius = w * 0.08f, center = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.42f))
            drawCircle(mossDark, radius = w * 0.06f, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.50f))

            // 5. Gnarled Wooden Arms & Claws
            val leftArm = Path().apply {
                moveTo(w * 0.30f, h * 0.42f)
                quadraticTo(w * 0.15f, h * 0.52f, w * 0.12f, h * 0.68f)
                lineTo(w * 0.22f, h * 0.68f)
                quadraticTo(w * 0.25f, h * 0.55f, w * 0.35f, h * 0.48f)
                close()
            }
            val rightArm = Path().apply {
                moveTo(w * 0.70f, h * 0.42f)
                quadraticTo(w * 0.85f, h * 0.52f, w * 0.88f, h * 0.68f)
                lineTo(w * 0.78f, h * 0.68f)
                quadraticTo(w * 0.75f, h * 0.55f, w * 0.65f, h * 0.48f)
                close()
            }
            drawPath(leftArm, barkMid)
            drawPath(rightArm, barkMid)

            // Claw Fingers
            drawCircle(barkDark, radius = w * 0.03f, center = androidx.compose.ui.geometry.Offset(w * 0.10f, h * 0.70f))
            drawCircle(barkDark, radius = w * 0.03f, center = androidx.compose.ui.geometry.Offset(w * 0.14f, h * 0.72f))
            drawCircle(barkDark, radius = w * 0.03f, center = androidx.compose.ui.geometry.Offset(w * 0.90f, h * 0.70f))
            drawCircle(barkDark, radius = w * 0.03f, center = androidx.compose.ui.geometry.Offset(w * 0.86f, h * 0.72f))

            // 6. Head & Mossy Beard
            val headPath = Path().apply {
                moveTo(w * 0.38f, h * 0.28f)
                lineTo(w * 0.62f, h * 0.28f)
                lineTo(w * 0.58f, h * 0.42f)
                lineTo(w * 0.42f, h * 0.42f)
                close()
            }
            drawPath(headPath, barkMid)

            // Mossy Beard
            val beardPath = Path().apply {
                moveTo(w * 0.42f, h * 0.40f)
                quadraticTo(w * 0.50f, h * 0.58f, w * 0.58f, h * 0.40f)
                close()
            }
            drawPath(beardPath, mossDark)
            drawCircle(mossLight, radius = w * 0.04f, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.46f))

            // 7. Shamanic Leafy Crown & Antler Canopy
            val crownCenter = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.18f)
            drawCircle(leafGreen, radius = w * 0.22f, center = crownCenter)
            drawCircle(leafGlow, radius = w * 0.15f, center = crownCenter)

            // Canopy Leaf Clusters
            drawCircle(mossLight, radius = w * 0.10f, center = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.22f))
            drawCircle(mossLight, radius = w * 0.10f, center = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.22f))
            drawCircle(leafGreen, radius = w * 0.08f, center = androidx.compose.ui.geometry.Offset(w * 0.24f, h * 0.16f))
            drawCircle(leafGreen, radius = w * 0.08f, center = androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.16f))

            // Antler Branches
            drawLine(barkDark, androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.25f), androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.10f), strokeWidth = w * 0.04f)
            drawLine(barkDark, androidx.compose.ui.geometry.Offset(w * 0.55f, h * 0.25f), androidx.compose.ui.geometry.Offset(w * 0.70f, h * 0.10f), strokeWidth = w * 0.04f)
            drawCircle(leafGlow, radius = w * 0.035f, center = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.08f))
            drawCircle(leafGlow, radius = w * 0.035f, center = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.08f))

            // 8. Glowing Shaman Eyes
            val eyeLeft = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.34f)
            val eyeRight = androidx.compose.ui.geometry.Offset(w * 0.56f, h * 0.34f)

            // Eye aura glow
            drawCircle(eyeColor.copy(alpha = 0.5f * eyeGlowAlpha), radius = w * 0.05f, center = eyeLeft)
            drawCircle(eyeColor.copy(alpha = 0.5f * eyeGlowAlpha), radius = w * 0.05f, center = eyeRight)

            // Eye cores
            drawCircle(eyeColor.copy(alpha = eyeGlowAlpha), radius = w * 0.03f, center = eyeLeft)
            drawCircle(eyeColor.copy(alpha = eyeGlowAlpha), radius = w * 0.03f, center = eyeRight)
            drawCircle(Color.White, radius = w * 0.012f, center = eyeLeft)
            drawCircle(Color.White, radius = w * 0.012f, center = eyeRight)
        }
    }
}

@Composable
private fun HomeScreenShamanAnimationCanvas(
    auraRotation: Float,
    auraPulseScale: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerOffset = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f)
        val radius = minOf(w, h) * 0.42f * auraPulseScale

        // 1. Radiant Shamanic Spirit Core Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x6652B788), // Shaman Emerald
                    Color(0x33D4AF37), // Shaman Gold
                    Color(0x111B4332),
                    Color.Transparent
                ),
                center = centerOffset,
                radius = radius * 1.3f
            ),
            radius = radius * 1.3f,
            center = centerOffset
        )

        // 2. Rotating Outer Shamanic Rune Circle
        drawCircle(
            color = Color(0x7752B788),
            radius = radius,
            center = centerOffset,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )

        drawCircle(
            color = Color(0x55D4AF37),
            radius = radius * 0.82f,
            center = centerOffset,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )

        // 3. 8 Shamanic Spirit Nodes rotating on the ring at 60fps
        val numNodes = 8
        for (i in 0 until numNodes) {
            val angleRad = Math.toRadians((auraRotation + (i * (360f / numNodes))).toDouble())
            val nx = centerOffset.x + (radius * kotlin.math.cos(angleRad)).toFloat()
            val ny = centerOffset.y + (radius * kotlin.math.sin(angleRad)).toFloat()

            // Outer node glow
            drawCircle(
                color = if (i % 2 == 0) Color(0xFF70E000) else Color(0xFFFFD700),
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(nx, ny)
            )

            // Inner node core
            drawCircle(
                color = Color.White,
                radius = 1.8.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(nx, ny)
            )

            // Connection line to center
            drawLine(
                color = Color(0x2252B788),
                start = centerOffset,
                end = androidx.compose.ui.geometry.Offset(nx, ny),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
