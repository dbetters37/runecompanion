@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.ui.tabs

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.AdventuringStoryData
import com.example.data.models.IncantationCategory
import com.example.data.models.IncantationsData
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import com.example.ui.components.PetSpriteRenderer
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class AdventuringMode {
    BATTLE,
    DECK,
    MAP,
    LOADOUT,
    CODEX,
    LOG
}

@Composable
fun AdventuringTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val adventuringFloor by viewModel.adventuringFloor.collectAsStateWithLifecycle()
    val adventuringMaxFloor by viewModel.adventuringMaxFloor.collectAsStateWithLifecycle()
    val currentMonster by viewModel.adventuringCurrentMonster.collectAsStateWithLifecycle()
    val monsterHp by viewModel.adventuringMonsterHp.collectAsStateWithLifecycle()
    val petHp by viewModel.adventuringPetHp.collectAsStateWithLifecycle()
    val petMaxHp by viewModel.adventuringPetMaxHp.collectAsStateWithLifecycle()
    val adventuringLog by viewModel.adventuringLog.collectAsStateWithLifecycle()
    val adventuringCombatStance by viewModel.adventuringCombatStance.collectAsStateWithLifecycle()

    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val activeCauldronRecipe by viewModel.selectedCauldronRecipe.collectAsStateWithLifecycle()
    val activeIncantationIds by viewModel.activeIncantationIds.collectAsStateWithLifecycle()

    val adventuringXp = skillXpMap[OsrsSkill.ADVENTURING] ?: 0L
    val adventuringLvl = OsrsXpCalculator.getLevelForXp(adventuringXp)

    val enemyLastPlayedAttack by viewModel.enemyLastPlayedAttack.collectAsStateWithLifecycle()
    val enemyAttackTrigger by viewModel.enemyAttackTrigger.collectAsStateWithLifecycle()

    val playerCombatLevel = remember(skillXpMap) {
        viewModel.calculatePetCombatLevel()
    }

    var activeMode by remember { mutableStateOf(AdventuringMode.BATTLE) }
    var isAutoBattleActive by remember { mutableStateOf(false) }

    // Physical Attack Movement Animatables & Damage Overlays
    val playerAttackXAnim = remember { Animatable(0f) }
    val enemyAttackXAnim = remember { Animatable(0f) }
    val enemyShakeXAnim = remember { Animatable(0f) }
    val enemyShakeYAnim = remember { Animatable(0f) }
    val enemyScaleAnim = remember { Animatable(1.0f) }
    val enemyRotationAnim = remember { Animatable(0f) }
    val enemyAlphaAnim = remember { Animatable(1.0f) }
    val enemyOffsetYAnim = remember { Animatable(0f) }

    var showPlayerHitSplash by remember { mutableStateOf(false) }
    var showEnemyHitSlash by remember { mutableStateOf(false) }
    var showEnemyEliminatedBanner by remember { mutableStateOf(false) }
    var playerDamageText by remember { mutableStateOf("") }
    var enemyDamageText by remember { mutableStateOf("") }
    var enemyHitParticleTrigger by remember { mutableIntStateOf(0) }
    var lastCardPlayedStance by remember { mutableStateOf("MELEE") }

    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val triggerHaptic: (String) -> Unit = remember(haptic, view) {
        { eventType ->
            try {
                when (eventType) {
                    "pet", "bonding" -> {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                    "hit", "attack" -> {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                    "guard" -> {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                    "level_up", "victory", "floor_clear" -> {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                    "item", "fish" -> {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // Spawn Animation for New Monster
    LaunchedEffect(currentMonster?.id) {
        if (currentMonster != null && monsterHp > 0) {
            enemyAlphaAnim.snapTo(0f)
            enemyOffsetYAnim.snapTo(-110f)
            enemyScaleAnim.snapTo(0.2f)
            enemyRotationAnim.snapTo(18f)

            coroutineScope.launch {
                enemyAlphaAnim.animateTo(1f, tween(200))
                enemyOffsetYAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                enemyScaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                enemyRotationAnim.animateTo(0f, spring())
            }
        }
    }

    // Enemy Attack Animation Triggered via enemyAttackTrigger
    LaunchedEffect(enemyAttackTrigger) {
        if (enemyAttackTrigger > 0) {
            triggerHaptic("guard")
            coroutineScope.launch {
                // Enemy lunges towards player
                enemyAttackXAnim.animateTo(-110f, tween(110, easing = FastOutLinearInEasing))
                enemyRotationAnim.animateTo(-22f, tween(90))
                enemyAttackXAnim.animateTo(-125f, tween(50))

                // Hero hit recoil
                playerAttackXAnim.animateTo(-30f, tween(60))
                playerAttackXAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))

                // Enemy returns back
                enemyAttackXAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                enemyRotationAnim.animateTo(0f, spring())
            }
        }
    }

    // Auto-Battle Loop Logic
    LaunchedEffect(isAutoBattleActive, currentMonster, monsterHp, petHp) {
        if (isAutoBattleActive && currentMonster != null && monsterHp > 0 && petHp > 0) {
            while (isAutoBattleActive && monsterHp > 0 && petHp > 0) {
                delay(1400)
                if (isAutoBattleActive && currentMonster != null && monsterHp > 0 && petHp > 0) {
                    triggerHaptic("attack")
                    viewModel.attackAdventuringMonster()
                }
            }
        }
    }

    // Dynamic Haptic Feedback & Impact Animations Triggers on Combat Events
    var prevMonsterHp by remember { mutableIntStateOf(monsterHp) }
    LaunchedEffect(monsterHp) {
        if (monsterHp < prevMonsterHp && monsterHp > 0) {
            triggerHaptic("hit")
            val dmg = prevMonsterHp - monsterHp
            enemyDamageText = "-$dmg ⚔️"
            showEnemyHitSlash = true
            enemyHitParticleTrigger++

            // Player lunges forward
            coroutineScope.launch {
                playerAttackXAnim.animateTo(65f, tween(110, easing = FastOutLinearInEasing))
                playerAttackXAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            }

            // Enemy squish & recoil scale animation
            coroutineScope.launch {
                enemyScaleAnim.animateTo(0.82f, tween(50))
                enemyScaleAnim.animateTo(1.15f, tween(70))
                enemyScaleAnim.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }

            // Rapid directional shake effect
            coroutineScope.launch {
                val shakeOffsets = listOf(16f, -16f, 12f, -12f, 8f, -8f, 4f, -4f, 0f)
                for (offset in shakeOffsets) {
                    enemyShakeXAnim.snapTo(offset)
                    enemyShakeYAnim.snapTo((offset * 0.3f) * if (offset > 0) 1f else -1f)
                    delay(22)
                }
            }

            coroutineScope.launch {
                delay(600)
                showEnemyHitSlash = false
            }
        } else if (monsterHp <= 0 && prevMonsterHp > 0) {
            triggerHaptic("victory")
            enemyHitParticleTrigger++
            showEnemyEliminatedBanner = true
            coroutineScope.launch {
                // Dramatic elimination animation: Scale up, spin red, slide off screen down & right
                enemyScaleAnim.animateTo(1.4f, tween(100))
                enemyRotationAnim.animateTo(-35f, tween(120))
                launch { enemyScaleAnim.animateTo(0f, tween(350)) }
                launch { enemyOffsetYAnim.animateTo(160f, tween(350)) }
                launch { enemyAlphaAnim.animateTo(0f, tween(350)) }
                delay(850)
                showEnemyEliminatedBanner = false
            }
        }
        prevMonsterHp = monsterHp
    }

    var prevPetHp by remember { mutableIntStateOf(petHp) }
    LaunchedEffect(petHp) {
        if (petHp < prevPetHp) {
            triggerHaptic("guard")
            val dmg = prevPetHp - petHp
            playerDamageText = "-$dmg 💥"
            showPlayerHitSplash = true
            coroutineScope.launch {
                enemyAttackXAnim.animateTo(-65f, tween(110, easing = FastOutLinearInEasing))
                enemyAttackXAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            }
            coroutineScope.launch {
                delay(600)
                showPlayerHitSplash = false
            }
        }
        prevPetHp = petHp
    }

    val currentFloorData = remember(adventuringFloor) {
        AdventuringStoryData.getFloor(adventuringFloor)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1017))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // --- TOP RPG GAME TITLE & HERO PROFILE DASHBOARD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141D28)),
            border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header Title Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🗡️", fontSize = 18.sp)
                        Column {
                            Text(
                                "REALM OF THE SHAMAN",
                                color = OsrsGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Dungeon Crawler & RPG Arena",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Auto-Battle Toggle & Floor Restart Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = isAutoBattleActive,
                            onClick = { isAutoBattleActive = !isAutoBattleActive },
                            label = {
                                Text(
                                    if (isAutoBattleActive) "🤖 AUTO ON" else "🤖 AUTO OFF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAutoBattleActive) Color(0xFF00FF9D) else Color.Gray
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1B3B22),
                                containerColor = Color(0xFF1F2833)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isAutoBattleActive,
                                borderColor = Color(0xFF334252),
                                selectedBorderColor = Color(0xFF00FF9D)
                            )
                        )

                        IconButton(
                            onClick = { viewModel.resetAdventuringDungeon() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("↺", color = Color(0xFFFF8A80), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF263342), thickness = 1.dp)

                // Hero Badges & Floor Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF1F2B3A),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, OsrsGold)
                        ) {
                            Text(
                                "🗺️ Adventuring Lv. $adventuringLvl",
                                color = OsrsTextYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = Color(0xFF0D2818),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF00FF9D))
                        ) {
                            Text(
                                "🛡️ Combat Lv. $playerCombatLevel",
                                color = Color(0xFF00FF9D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF2A1F1D),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF9800))
                    ) {
                        Text(
                            "Floor $adventuringFloor / Max: $adventuringMaxFloor",
                            color = Color(0xFFFFB74D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // --- GAME MODE SUB-TAB NAVIGATION BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val modes = listOf(
                AdventuringMode.BATTLE to "⚔️ Battle Stage",
                AdventuringMode.DECK to "🎴 Deck Builder",
                AdventuringMode.MAP to "🗺️ Realm Map",
                AdventuringMode.LOADOUT to "🎒 Hero Loadout",
                AdventuringMode.CODEX to "🏆 Codex",
                AdventuringMode.LOG to "📜 Chronicle"
            )

            modes.forEach { (mode, label) ->
                val isSelected = activeMode == mode
                Surface(
                    color = if (isSelected) Color(0xFF2E3D52) else Color(0xFF141D28),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) OsrsGold else Color(0xFF263342)
                    ),
                    modifier = Modifier
                        .clickable { activeMode = mode }
                        .testTag("adventuring_mode_${mode.name.lowercase()}")
                ) {
                    Text(
                        label,
                        color = if (isSelected) OsrsGold else OsrsParchment,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // --- MAIN GAME VIEW BODY BASED ON SELECTED MODE ---
        Box(modifier = Modifier.weight(1f)) {
            when (activeMode) {
                AdventuringMode.BATTLE -> BattleStageView(
                    viewModel = viewModel,
                    currentFloorData = currentFloorData,
                    adventuringFloor = adventuringFloor,
                    adventuringMaxFloor = adventuringMaxFloor,
                    currentMonster = currentMonster,
                    monsterHp = monsterHp,
                    petHp = petHp,
                    petMaxHp = petMaxHp,
                    petState = petState,
                    adventuringCombatStance = adventuringCombatStance,
                    playerCombatLevel = playerCombatLevel,
                    playerAttackXAnim = playerAttackXAnim.value,
                    enemyAttackXAnim = enemyAttackXAnim.value,
                    enemyShakeXAnim = enemyShakeXAnim.value,
                    enemyShakeYAnim = enemyShakeYAnim.value,
                    enemyScaleAnim = enemyScaleAnim.value,
                    enemyRotationAnim = enemyRotationAnim.value,
                    enemyAlphaAnim = enemyAlphaAnim.value,
                    enemyOffsetYAnim = enemyOffsetYAnim.value,
                    showEnemyEliminatedBanner = showEnemyEliminatedBanner,
                    enemyHitParticleTrigger = enemyHitParticleTrigger,
                    lastCardPlayedStance = lastCardPlayedStance,
                    enemyLastPlayedAttack = enemyLastPlayedAttack,
                    showPlayerHitSplash = showPlayerHitSplash,
                    showEnemyHitSlash = showEnemyHitSlash,
                    playerDamageText = playerDamageText,
                    enemyDamageText = enemyDamageText,
                    isAutoBattleActive = isAutoBattleActive,
                    inventoryItems = inventoryItems,
                    activeCauldronRecipe = activeCauldronRecipe,
                    activeIncantationIds = activeIncantationIds,
                    triggerHaptic = triggerHaptic,
                    onCardPlayed = { stance -> lastCardPlayedStance = stance },
                    onNavigateToDeckBuilder = { activeMode = AdventuringMode.DECK }
                )

                AdventuringMode.DECK -> com.example.ui.components.DeckBuilderSection(
                    viewModel = viewModel
                )

                AdventuringMode.MAP -> RealmMapView(
                    viewModel = viewModel,
                    adventuringFloor = adventuringFloor,
                    adventuringMaxFloor = adventuringMaxFloor,
                    onSelectFloor = { floorNum ->
                        viewModel.selectAdventuringFloor(floorNum)
                        activeMode = AdventuringMode.BATTLE
                    }
                )

                AdventuringMode.LOADOUT -> HeroLoadoutView(
                    viewModel = viewModel,
                    adventuringCombatStance = adventuringCombatStance,
                    activeIncantationIds = activeIncantationIds,
                    activeCauldronRecipe = activeCauldronRecipe
                )

                AdventuringMode.CODEX -> BeastiaryCodexView(
                    currentFloorData = currentFloorData,
                    currentMonster = currentMonster
                )

                AdventuringMode.LOG -> DungeonChronicleView(
                    adventuringLog = adventuringLog
                )
            }
        }
    }
}

// --- 1. BATTLE STAGE VIEW COMPONENT ---
@Composable
private fun BattleStageView(
    viewModel: PetViewModel,
    currentFloorData: com.example.data.models.AdventuringFloor,
    adventuringFloor: Int,
    adventuringMaxFloor: Int,
    currentMonster: com.example.data.models.AdventuringMonster?,
    monsterHp: Int,
    petHp: Int,
    petMaxHp: Int,
    petState: com.example.data.models.PetState,
    adventuringCombatStance: String,
    playerCombatLevel: Int,
    playerAttackXAnim: Float,
    enemyAttackXAnim: Float,
    enemyShakeXAnim: Float = 0f,
    enemyShakeYAnim: Float = 0f,
    enemyScaleAnim: Float = 1f,
    enemyRotationAnim: Float = 0f,
    enemyAlphaAnim: Float = 1f,
    enemyOffsetYAnim: Float = 0f,
    showEnemyEliminatedBanner: Boolean = false,
    enemyHitParticleTrigger: Int = 0,
    lastCardPlayedStance: String = "MELEE",
    enemyLastPlayedAttack: com.example.data.models.EnemyAttack? = null,
    showPlayerHitSplash: Boolean,
    showEnemyHitSlash: Boolean,
    playerDamageText: String,
    enemyDamageText: String,
    isAutoBattleActive: Boolean,
    inventoryItems: List<com.example.data.models.InventoryItem>,
    activeCauldronRecipe: com.example.data.models.CauldronRecipe,
    activeIncantationIds: Set<String> = emptySet(),
    triggerHaptic: (String) -> Unit,
    onCardPlayed: (String) -> Unit = {},
    onNavigateToDeckBuilder: () -> Unit = {}
) {
    // Continuous 60fps frame tick animation state for floating & aura
    val infiniteTransition = rememberInfiniteTransition()
    val animStep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val petBounceY = (sin(Math.toRadians(animStep.toDouble() * 3)) * 8).toFloat()
    val monsterFloatY = (sin(Math.toRadians(animStep.toDouble() * 2)) * 10).toFloat()

    val combatEnergyMax by viewModel.combatEnergyMax.collectAsState()
    val combatEnergyCurrent by viewModel.combatEnergyCurrent.collectAsState()
    val combatNextAttackBonusDmg by viewModel.combatNextAttackBonusDmg.collectAsState()
    val combatHand by viewModel.combatHand.collectAsState()
    val combatDrawDeck by viewModel.combatDrawDeck.collectAsState()
    val combatDiscardPile by viewModel.combatDiscardPile.collectAsState()
    val combatPlayerShield by viewModel.combatPlayerShield.collectAsState()
    val customDeckCardIds by viewModel.customDeckCardIds.collectAsState()
    val bankItems by viewModel.bankItems.collectAsState()
    val skillXpMap by viewModel.skillXpMap.collectAsState()

    var showDeckManagerDialog by remember { mutableStateOf(false) }
    var showStanceSwitchConfirmDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(adventuringCombatStance) {
        viewModel.initCombatDeckForCurrentStance(forceReset = false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 2D Live Combat Arena Stage
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(currentFloorData.bgStartColor)),
            border = BorderStroke(2.dp, OsrsGold),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Animated Shaman Canvas Background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(currentFloorData.bgStartColor),
                                Color(currentFloorData.bgEndColor)
                            )
                        )
                    )

                    // Draw Shaman Aura Floating Particles
                    for (i in 0 until 8) {
                        val cx = (w * ((i * 0.13f + animStep * 0.002f) % 1.0f))
                        val cy = (h * 0.2f) + sin(Math.toRadians((animStep * 2 + i * 40).toDouble())).toFloat() * 30f
                        drawCircle(
                            color = Color(0x66FFD700),
                            radius = 6f + (i % 3) * 3f,
                            center = Offset(cx, cy)
                        )
                    }

                    // Ground Line
                    val groundY = h * 0.78f
                    drawLine(
                        color = Color(0x44FFFFFF),
                        start = Offset(0f, groundY),
                        end = Offset(w, groundY),
                        strokeWidth = 3f
                    )
                }

                // Floor Title Badge
                Surface(
                    color = Color(0xCC000000),
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        currentFloorData.title,
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Auto-Battle Active Glowing Badge
                if (isAutoBattleActive) {
                    Surface(
                        color = Color(0xDD003311),
                        shape = RoundedCornerShape(bottomStart = 10.dp),
                        border = BorderStroke(1.dp, Color(0xFF00FF9D)),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            "⚡ AUTO-COMBAT ACTIVE",
                            color = Color(0xFF00FF9D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Enemy Action Card Pop-up Banner
                enemyLastPlayedAttack?.let { atk ->
                    Surface(
                        color = Color(0xEE2A1212),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF4D4D)),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 22.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(atk.emoji, fontSize = 13.sp)
                            Column {
                                Text(
                                    "⚠️ ENEMY PLAYED: ${atk.name}",
                                    color = Color(0xFFFF8A80),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                )
                                Text(
                                    "${if (atk.shieldPierce) "⚡ Bypasses Shield • " else ""}${atk.damagePower} DMG • ${atk.description}",
                                    color = Color.LightGray,
                                    fontSize = 8.5.sp
                                )
                            }
                        }
                    }
                }

                // Left Side: Hero Unit
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 20.dp)
                        .offset(x = playerAttackXAnim.dp, y = petBounceY.dp)
                        .clickable {
                            triggerHaptic("pet")
                            viewModel.boostPetMood(15, "Spirit Bonding in Adventuring")
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (showPlayerHitSplash) {
                        Surface(
                            color = Color(0xDD8B0000),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color.Red),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                playerDamageText,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    val petHpRatio = (petHp.toFloat() / petMaxHp.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .width(75.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.DarkGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(petHpRatio)
                                .background(if (petHpRatio > 0.4f) Color(0xFF4CAF50) else Color.Red)
                        )
                    }
                    Text("❤️ $petHp / $petMaxHp", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)

                    Surface(
                        color = Color(0xAA003311),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            "🛡️ Lv. $playerCombatLevel",
                            color = Color(0xFF00FF9D),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    PetSpriteRenderer(
                        petType = petState.petType,
                        petState = petState,
                        sizeDp = 60.dp,
                        modifier = Modifier.size(60.dp)
                    )
                    Text(petState.customName, color = OsrsTextYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Right Side: Monster Unit
                currentMonster?.let { monster ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 15.dp, bottom = 15.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .offset(
                                    x = (enemyAttackXAnim + enemyShakeXAnim).dp,
                                    y = (monsterFloatY + enemyShakeYAnim + enemyOffsetYAnim).dp
                                )
                                .graphicsLayer {
                                    scaleX = enemyScaleAnim
                                    scaleY = enemyScaleAnim
                                    rotationZ = enemyRotationAnim
                                    alpha = enemyAlphaAnim
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (showEnemyEliminatedBanner) {
                                Surface(
                                    color = Color(0xEE8B0000),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        "☠️ ELIMINATED! 💥",
                                        color = Color.Yellow,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (showEnemyHitSlash) {
                                Surface(
                                    color = Color(0xDDB8860B),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                ) {
                                    Text(
                                        enemyDamageText,
                                        color = Color.Yellow,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            val monsterHpRatio = (monsterHp.toFloat() / monster.maxHp.toFloat()).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .width(75.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.DarkGray)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(monsterHpRatio)
                                        .background(Color(0xFFE53935))
                                )
                            }
                            Text("HP: $monsterHp / ${monster.maxHp}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)

                            Surface(
                                color = Color(0xAA4A1010),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    "⚔️ Lv. ${monster.combatLevel}",
                                    color = Color(0xFFFF8A80),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Surface(
                                color = Color(0x33000000),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color.Red),
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(monster.emoji, fontSize = 32.sp)
                                }
                            }
                            Text(monster.name, color = Color(0xFFFF8A80), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Combat Card Impact Particle Overlay centered on Enemy
                        com.example.ui.components.CombatHitParticleOverlay(
                            triggerKey = enemyHitParticleTrigger,
                            particleColor = when (lastCardPlayedStance) {
                                "MAGIC" -> Color(0xFFE040FB)
                                "RANGED" -> Color(0xFF00E5FF)
                                "ULTIMATE" -> Color(0xFFFFD700)
                                else -> Color(0xFFFF3D00)
                            },
                            symbolsList = when (lastCardPlayedStance) {
                                "MAGIC" -> listOf("🪄", "✨", "💫", "🔮", "⚡")
                                "RANGED" -> listOf("🏹", "🎯", "⚡", "✨", "💨")
                                "ULTIMATE" -> listOf("☄️", "💥", "🌟", "🔥", "✨")
                                else -> listOf("⚔️", "💥", "✨", "🔥", "⚡")
                            },
                            modifier = Modifier
                                .size(130.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }

        val activeCookingBuffs by viewModel.activeCookingBuffs.collectAsState()

        // --- CARD-BASED COMBAT DECK ---
        com.example.ui.components.CombatDeckHandSection(
            currentEnergy = combatEnergyCurrent,
            maxEnergy = combatEnergyMax,
            playerShield = combatPlayerShield,
            hand = combatHand,
            drawDeckCount = combatDrawDeck.size,
            discardPileCount = combatDiscardPile.size,
            stance = adventuringCombatStance,
            nextAttackBuff = combatNextAttackBonusDmg,
            activeIncantationIds = activeIncantationIds,
            activeCookingBuffs = activeCookingBuffs,
            onPlayCard = { card ->
                triggerHaptic("attack")
                onCardPlayed(card.stance)
                viewModel.playCombatCard(card)
            },
            onEndTurn = {
                triggerHaptic("guard")
                viewModel.endTurnAndDrawHand()
            },
            onOpenDeckManager = {
                showDeckManagerDialog = true
            }
        )

        // Stance Selectors & Quick Commands Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2430)),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stances = listOf(
                    Triple("MELEE", "⚔️ Melee", Color(0xFF9E2A2B)),
                    Triple("RANGED", "🏹 Blowdarts", Color(0xFF2E6B38)),
                    Triple("MAGIC", "🪄 Magic", Color(0xFF1D3557))
                )
                stances.forEach { (stanceKey, label, activeBg) ->
                    val isSelected = adventuringCombatStance == stanceKey
                    Button(
                        onClick = {
                            if (!isSelected) {
                                val isMonsterAlive = currentMonster != null && monsterHp > 0
                                if (isMonsterAlive) {
                                    showStanceSwitchConfirmDialog = stanceKey
                                } else {
                                    viewModel.setAdventuringCombatStance(stanceKey, endTurn = false)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) activeBg else Color(0xFF2B2118)
                        ),
                        border = BorderStroke(1.dp, if (isSelected) OsrsGold else Color(0xFF5A4433)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                    ) {
                        Text(
                            label,
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White else OsrsTextYellow,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Button(
                    onClick = {
                        triggerHaptic("floor_clear")
                        viewModel.advanceAdventuringFloor()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B6200)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("adventuring_advance_button")
                ) {
                    Text("⏩ NEXT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }

        if (showDeckManagerDialog) {
            com.example.ui.components.DeckManagerDialog(
                stance = adventuringCombatStance,
                customCardIds = customDeckCardIds,
                onToggleCard = { cardId -> viewModel.toggleCustomDeckCard(cardId) },
                onDismiss = { showDeckManagerDialog = false },
                skillXpMap = skillXpMap,
                onOpenFullDeckBuilder = onNavigateToDeckBuilder
            )
        }

        if (showStanceSwitchConfirmDialog != null) {
            val targetStance = showStanceSwitchConfirmDialog!!
            val targetLabel = when (targetStance) {
                "MELEE" -> "Melee ⚔️"
                "RANGED" -> "Blowdarts 🏹"
                "MAGIC" -> "Magic 🪄"
                else -> targetStance
            }
            AlertDialog(
                onDismissRequest = { showStanceSwitchConfirmDialog = null },
                title = {
                    Text(
                        "Switch Combat Style?",
                        color = OsrsGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        "Switching combat style to $targetLabel will end your turn and allow the enemy to attack! Your hand and deck will be reset for the new style.\n\nDo you wish to switch combat styles and end your turn?",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val stanceToSet = showStanceSwitchConfirmDialog
                            showStanceSwitchConfirmDialog = null
                            if (stanceToSet != null) {
                                triggerHaptic("click")
                                viewModel.setAdventuringCombatStance(stanceToSet, endTurn = true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C))
                    ) {
                        Text("Switch & End Turn", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showStanceSwitchConfirmDialog = null }
                    ) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = Color(0xFF141D28),
                shape = RoundedCornerShape(12.dp)
            )
        }

        val favoriteItemIds by viewModel.favoriteItemIds.collectAsState()

        // --- QUICK USABLE ITEMS / BACKPACK & FOOD BAG BAR ---
        val usableItems = remember(inventoryItems, bankItems, favoriteItemIds) {
            val combinedList = (inventoryItems + bankItems).distinctBy { it.id }.filter { item ->
                item.quantity > 0 &&
                !item.isRawUncookedFood &&
                !item.id.startsWith("item_raw_") &&
                !item.name.startsWith("Raw ", ignoreCase = true) &&
                !item.id.startsWith("item_burnt_") &&
                !item.name.startsWith("Burnt ", ignoreCase = true) &&
                !item.id.contains("bone") &&
                !item.name.contains("Bone", ignoreCase = true) &&
                item.category != com.example.data.models.ItemCategory.BONES && (
                    item.isCookedReadyToEatFood ||
                    item.healHp > 0 ||
                    item.restoreHunger > 0 ||
                    item.category == com.example.data.models.ItemCategory.FOOD ||
                    item.category == com.example.data.models.ItemCategory.POTION ||
                    item.id.contains("cooked") ||
                    item.id.startsWith("item_cooked_") ||
                    item.name.contains("Cooked", ignoreCase = true) ||
                    item.id.contains("stew") ||
                    item.id.contains("salad") ||
                    item.id.contains("pie") ||
                    item.id.contains("cake")
                )
            }

            // Merge duplicate items from inventory and bank
            val mergedMap = mutableMapOf<String, com.example.data.models.InventoryItem>()
            combinedList.forEach { item ->
                val normId = com.example.data.models.DefaultItems.normalizeItemId(item.id)
                val baseItem = com.example.data.models.DefaultItems.getItemById(normId)
                val existing = mergedMap[normId]
                if (existing != null) {
                    mergedMap[normId] = existing.copy(quantity = existing.quantity + item.quantity)
                } else {
                    mergedMap[normId] = item.copy(
                        name = if (item.name.isBlank() || item.name.startsWith("Item ")) baseItem.name else item.name,
                        healHp = if (item.healHp > 0) item.healHp else baseItem.healHp,
                        restoreHunger = if (item.restoreHunger > 0) item.restoreHunger else baseItem.restoreHunger,
                        iconEmoji = if (item.iconEmoji == "📦") baseItem.iconEmoji else item.iconEmoji,
                        category = if (item.category == com.example.data.models.ItemCategory.MISC) baseItem.category else item.category
                    )
                }
            }

            mergedMap.values.sortedWith { a, b ->
                val normA = com.example.data.models.DefaultItems.normalizeItemId(a.id)
                val normB = com.example.data.models.DefaultItems.normalizeItemId(b.id)
                val aFav = favoriteItemIds.contains(normA) || favoriteItemIds.contains(a.id)
                val bFav = favoriteItemIds.contains(normB) || favoriteItemIds.contains(b.id)
                when {
                    aFav && !bFav -> -1
                    !aFav && bFav -> 1
                    else -> 0
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161E27)),
            border = BorderStroke(1.dp, Color(0xFF2A394A)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    "🎒 Battle Consumables (Tap to Heal • Hold to ⭐ Favorite)",
                    color = OsrsTextYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                if (usableItems.isEmpty()) {
                    Text(
                        "No battle items or food in inventory. Catch fish, craft food, or buy food at GE Shop!",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        usableItems.forEach { item ->
                            val normId = com.example.data.models.DefaultItems.normalizeItemId(item.id)
                            val isFav = favoriteItemIds.contains(normId) || favoriteItemIds.contains(item.id)
                            val isBone = item.id.contains("bone")
                            val effectLabel = when {
                                isBone && item.id == "item_dragon_bones" -> "+600 HP"
                                isBone && item.id == "item_big_bones" -> "+300 HP"
                                isBone -> "+150 HP"
                                item.healHp > 0 -> "+${item.healHp} HP"
                                item.restoreHunger > 0 -> "+${(item.restoreHunger * 1.2).toInt().coerceAtLeast(15)} HP"
                                item.id.startsWith("item_cooked_") -> "+25 HP"
                                else -> "+20 HP"
                            }

                            Surface(
                                color = if (isFav) Color(0xFF332917) else if (isBone) Color(0xFF2E261A) else Color(0xFF232D3B),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isFav) Color(0xFFFFD700) else if (isBone) OsrsGold else Color(0xFF3D4F66)),
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        triggerHaptic("item")
                                        viewModel.useAdventuringItemInDungeon(item.id)
                                    },
                                    onLongClick = {
                                        triggerHaptic("favorite")
                                        viewModel.toggleFavoriteItem(item.id)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isFav) {
                                        Text("⭐", fontSize = 11.sp)
                                    }
                                    Text(if (isBone) "🦴" else item.iconEmoji, fontSize = 14.sp)
                                    Column {
                                        Text(
                                            "${item.name} x${item.quantity}",
                                            color = OsrsTextWhite,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            effectLabel,
                                            color = Color(0xFF81C784),
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.SemiBold
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
}

// --- 2. REALM MAP VIEW COMPONENT ---
@Composable
private fun RealmMapView(
    viewModel: PetViewModel,
    adventuringFloor: Int,
    adventuringMaxFloor: Int,
    onSelectFloor: (Int) -> Unit
) {
    var selectedChapterFilter by remember { mutableIntStateOf(((adventuringFloor - 1) / 10 + 1).coerceIn(1, 10)) }

    val displayFloors = remember(selectedChapterFilter, adventuringMaxFloor) {
        val startLvl = (selectedChapterFilter - 1) * 10 + 1
        val endLvl = (selectedChapterFilter * 10).coerceAtMost(99)
        (startLvl..endLvl).map { AdventuringStoryData.getFloor(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header Card with 99-Floor Tower Progress
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141D28)),
            border = BorderStroke(1.5.dp, OsrsGold),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🏯 FORTRESS OF THE FALLEN SHAMAN",
                        color = OsrsGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                    Surface(
                        color = Color(0xFF2C1F16),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D))
                    ) {
                        Text(
                            "Progression: $adventuringFloor / 99",
                            color = Color(0xFFFFB74D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                val towerProgress = (adventuringMaxFloor.toFloat() / 99f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF1B2430))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(towerProgress)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF4CAF50), Color(0xFFFFD700))
                                )
                            )
                    )
                }

                Text(
                    "Highest Cleared Floor: $adventuringMaxFloor / 99 • Select Chapter below to travel:",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )

                // Chapter Filter Chips (Chapters 1 to 10)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..10).forEach { ch ->
                        val isChUnlocked = adventuringMaxFloor >= (ch - 1) * 10 + 1
                        val isSel = selectedChapterFilter == ch

                        FilterChip(
                            selected = isSel,
                            onClick = { if (isChUnlocked) selectedChapterFilter = ch },
                            enabled = isChUnlocked,
                            label = {
                                Text(
                                    "Ch. $ch ${if (!isChUnlocked) "🔒" else ""}",
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color(0xFF00FF9D) else if (isChUnlocked) OsrsParchment else Color.Gray
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E3A2B),
                                containerColor = Color(0xFF141D28)
                            )
                        )
                    }
                }
            }
        }

        // Chapter Title Banner
        val currentChTitle = AdventuringStoryData.CHAPTER_TITLES[selectedChapterFilter] ?: "Chapter $selectedChapterFilter"
        Surface(
            color = Color(0xFF1C2736),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color(0xFF2C3C50)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "📜 $currentChTitle (Floors ${(selectedChapterFilter - 1) * 10 + 1} - ${(selectedChapterFilter * 10).coerceAtMost(99)})",
                color = OsrsTextYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        displayFloors.forEach { floor ->
            val isUnlocked = floor.floorLevel <= adventuringMaxFloor
            val isCurrent = floor.floorLevel == adventuringFloor

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(0xFF263321) else if (isUnlocked) Color(0xFF18222E) else Color(0xFF12161C)
                ),
                border = BorderStroke(
                    width = if (isCurrent) 1.5.dp else 1.dp,
                    color = if (isCurrent) Color(0xFF00FF9D) else if (isUnlocked) OsrsGold else Color(0xFF2A3644)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(floor.boss.emoji, fontSize = 22.sp)
                            Column {
                                Text(
                                    floor.title,
                                    color = if (isCurrent) Color(0xFF00FF9D) else if (isUnlocked) OsrsTextYellow else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    floor.description,
                                    color = Color.LightGray,
                                    fontSize = 9.5.sp
                                )
                            }
                        }

                        Surface(
                            color = when {
                                isCurrent -> Color(0xFF1B4D28)
                                isUnlocked -> Color(0xFF8B6200)
                                else -> Color(0xFF2C353F)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                when {
                                    isCurrent -> "CURRENT 📍"
                                    isUnlocked -> "UNLOCKED 🔓"
                                    else -> "LOCKED 🔒"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Boss Cards Summary
                    if (isUnlocked && floor.boss.attackCards.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Boss Cards:", color = Color.LightGray, fontSize = 9.sp)
                            floor.boss.attackCards.forEach { card ->
                                Surface(
                                    color = Color(0x442B1A1A),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFFFF5252))
                                ) {
                                    Text(
                                        "${card.emoji} ${card.name}",
                                        color = Color(0xFFFFAB91),
                                        fontSize = 8.5.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (isUnlocked) {
                        Button(
                            onClick = { onSelectFloor(floor.floorLevel) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrent) Color(0xFF2E6B38) else Color(0xFF1B3B5A)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                        ) {
                            Text(
                                if (isCurrent) "⚔️ IN DUNGEON" else "🗺️ TRAVEL TO FLOOR ${floor.floorLevel}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 3. HERO LOADOUT & INCANTATIONS VIEW COMPONENT ---
@Composable
private fun HeroLoadoutView(
    viewModel: PetViewModel,
    adventuringCombatStance: String,
    activeIncantationIds: Set<String>,
    activeCauldronRecipe: com.example.data.models.CauldronRecipe
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2430)),
            border = BorderStroke(1.5.dp, Color(0xFF00B4D8)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "🏹 Combat Incantations Deck",
                    color = OsrsTextYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Activate shamanic incantations to boost damage, defense, and lifesteal in dungeon battles.",
                    color = OsrsParchment,
                    fontSize = 10.5.sp
                )

                val relevantCategory = if (adventuringCombatStance == "RANGED") {
                    IncantationCategory.RANGED_INCANTATIONS
                } else {
                    IncantationCategory.MELEE_INCANTATIONS
                }

                val styleIncantations = remember(relevantCategory) {
                    IncantationsData.ALL_INCANTATIONS.filter { it.category == relevantCategory }
                }

                styleIncantations.forEach { incantation ->
                    val isTurnedOn = activeIncantationIds.contains(incantation.id)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTurnedOn) Color(0xFF1E3A2B) else Color(0xFF141C24),
                        border = BorderStroke(
                            width = if (isTurnedOn) 1.5.dp else 1.dp,
                            color = if (isTurnedOn) Color(0xFF00FF9D) else Color(0xFF2C3949)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleIncantation(incantation.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(incantation.iconEmoji, fontSize = 22.sp)

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    incantation.name,
                                    color = if (isTurnedOn) Color(0xFF00FF9D) else OsrsTextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    incantation.benefitSummary,
                                    color = OsrsTextYellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Switch(
                                checked = isTurnedOn,
                                onCheckedChange = { viewModel.toggleIncantation(incantation.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00FF9D)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 4. BEASTIARY CODEX VIEW COMPONENT ---
@Composable
private fun BeastiaryCodexView(
    currentFloorData: com.example.data.models.AdventuringFloor,
    currentMonster: com.example.data.models.AdventuringMonster?
) {
    val allMonsters = remember(currentFloorData) {
        currentFloorData.monsters + currentFloorData.boss
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141E28)),
            border = BorderStroke(1.dp, Color(0xFF00B4D8)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    "🏆 ${currentFloorData.title} Beastiary Codex",
                    color = OsrsGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    "Inspect enemy attributes, lore, and loot drops for this dungeon region.",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }
        }

        allMonsters.forEach { monsterItem ->
            val isCurrentTarget = currentMonster?.id == monsterItem.id
            val isBoss = monsterItem.id == currentFloorData.boss.id

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentTarget) Color(0xFF2C2216) else Color(0xFF17202B)
                ),
                border = BorderStroke(if (isCurrentTarget) 1.5.dp else 1.dp, if (isCurrentTarget) OsrsGold else Color(0xFF2A3848)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(monsterItem.emoji, fontSize = 24.sp)
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        monsterItem.name,
                                        color = if (isCurrentTarget) OsrsTextYellow else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    if (isBoss) {
                                        Text("👑 BOSS", color = Color(0xFFFF5252), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                                Text(
                                    "⚔️ Combat Lv. ${monsterItem.combatLevel} • HP: ${monsterItem.hp} • Atk: ${monsterItem.attackPower} • Def: ${monsterItem.defence}",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF1B2B3A),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "+${monsterItem.xpReward} XP | +${monsterItem.gpReward} GP",
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        monsterItem.storyLore,
                        color = OsrsParchment,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

// --- 5. DUNGEON CHRONICLE LOG COMPONENT ---
@Composable
private fun DungeonChronicleView(
    adventuringLog: List<String>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121820)),
        border = BorderStroke(1.dp, Color(0xFF243040)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                "📜 Shaman Dungeon Chronicle Log",
                color = OsrsTextYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(adventuringLog) { logLine ->
                    Surface(
                        color = Color(0xFF1A222C),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            logLine,
                            color = if (logLine.contains("Defeated") || logLine.contains("CLEARED")) OsrsGold else OsrsTextWhite,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
