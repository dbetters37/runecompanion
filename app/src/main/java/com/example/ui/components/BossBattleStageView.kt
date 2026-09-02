package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.viewmodel.PetViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BossGold = Color(0xFFFFD700)
private val BossParchment = Color(0xFFE8D8B0)
private val BossCardBg = Color(0xFF231A12)

/**
 * Interactive Boss Battle Scene with scripted turn-by-turn combat.
 * Cohesive with the Adventuring tab battle scene, featuring custom deck integration,
 * animated enemy attacks with visual impact, and turn-based user freezing.
 */
@Composable
fun BossBattleStageView(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val boss by viewModel.activeBossBattle.collectAsStateWithLifecycle()
    val bossHp by viewModel.bossBattleHp.collectAsStateWithLifecycle()
    val bossMaxHp by viewModel.bossBattleMaxHp.collectAsStateWithLifecycle()
    val turn by viewModel.bossBattleTurn.collectAsStateWithLifecycle()
    val status by viewModel.bossBattleStatus.collectAsStateWithLifecycle()
    val script by viewModel.bossBattleScript.collectAsStateWithLifecycle()
    val lastMove by viewModel.bossBattleLastPlayedMove.collectAsStateWithLifecycle()
    val nextMove by viewModel.bossBattleNextMove.collectAsStateWithLifecycle()
    val isBossAttacking by viewModel.isBossAttacking.collectAsStateWithLifecycle()
    val lastDamageTaken by viewModel.bossBattleLastDamageTaken.collectAsStateWithLifecycle()

    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val petMaxHp = viewModel.getPetMaxHealth()
    val petHp = petState.health.coerceAtMost(petMaxHp)

    val playerShield by viewModel.bossBattlePlayerShield.collectAsStateWithLifecycle()
    val playerEnergy by viewModel.bossBattlePlayerEnergy.collectAsStateWithLifecycle()
    val maxEnergy by viewModel.bossBattleMaxEnergy.collectAsStateWithLifecycle()
    val playerHand by viewModel.bossBattlePlayerHand.collectAsStateWithLifecycle()
    val playerDeck by viewModel.bossBattlePlayerDeck.collectAsStateWithLifecycle()
    val playerDiscard by viewModel.bossBattlePlayerDiscard.collectAsStateWithLifecycle()
    val battleLog by viewModel.bossBattleLog.collectAsStateWithLifecycle()
    val bossAttackTrigger by viewModel.bossAttackTrigger.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()

    // Deck integration states from Adventuring
    val adventuringCombatStance by viewModel.adventuringCombatStance.collectAsStateWithLifecycle()
    val customDeckCardIds by viewModel.customDeckCardIds.collectAsStateWithLifecycle()
    val savedDeckLoadouts by viewModel.savedDeckLoadouts.collectAsStateWithLifecycle()
    val activeDeckLoadoutId by viewModel.activeDeckLoadoutId.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()

    var showScriptTimelineDialog by remember { mutableStateOf(false) }
    var showQuickFoodDialog by remember { mutableStateOf(false) }
    var showDeckManagerDialog by remember { mutableStateOf(false) }
    var showStanceSwitchDialog by remember { mutableStateOf(false) }

    // Boss & Player attack animations
    val bossScale = remember { Animatable(1.0f) }
    val bossTranslationX = remember { Animatable(0f) }
    val bossShakeX = remember { Animatable(0f) }
    val bossHitFlashAlpha = remember { Animatable(0f) }
    val bossAuraAlpha = remember { Animatable(0f) }

    val playerScale = remember { Animatable(1.0f) }
    val playerAttackLungeX = remember { Animatable(0f) }
    val playerFlinchX = remember { Animatable(0f) }

    val screenShake = remember { Animatable(0f) }
    val flashOverlayAlpha = remember { Animatable(0f) }
    val damagePopupScale = remember { Animatable(0f) }
    val damagePopupAlpha = remember { Animatable(0f) }
    var displayedDamageText by remember { mutableStateOf<String?>(null) }
    var bossAttackBannerText by remember { mutableStateOf<String?>(null) }

    // Dynamic player attack card trigger animation (Lunging, scaling pulse, and violent enemy damage shake)
    val triggerPlayerCardAnim = remember(coroutineScope) {
        { card: CombatCard ->
            coroutineScope.launch {
                if (card.baseDamage > 0) {
                    // 1. Player attack anticipation & lunge forward
                    launch {
                        playerScale.animateTo(1.24f, tween(90, easing = FastOutSlowInEasing))
                        playerScale.animateTo(1.0f, tween(140, easing = FastOutSlowInEasing))
                    }
                    launch {
                        playerAttackLungeX.animateTo(34f, tween(100, easing = LinearOutSlowInEasing))
                        playerAttackLungeX.animateTo(0f, tween(150, easing = FastOutSlowInEasing))
                    }

                    delay(80)

                    // 2. Enemy violent shake & damage pulse feedback
                    launch {
                        bossHitFlashAlpha.snapTo(0.9f)
                        bossHitFlashAlpha.animateTo(0f, tween(260))
                    }
                    launch {
                        bossScale.animateTo(1.24f, tween(70, easing = FastOutSlowInEasing))
                        bossScale.animateTo(0.88f, tween(70))
                        bossScale.animateTo(1.0f, tween(120))
                    }
                    launch {
                        bossShakeX.animateTo(-18f, tween(40))
                        bossShakeX.animateTo(16f, tween(40))
                        bossShakeX.animateTo(-12f, tween(40))
                        bossShakeX.animateTo(8f, tween(40))
                        bossShakeX.animateTo(-4f, tween(40))
                        bossShakeX.animateTo(0f, tween(50))
                    }
                    launch {
                        screenShake.animateTo(5f, tween(40))
                        screenShake.animateTo(-5f, tween(40))
                        screenShake.animateTo(3f, tween(40))
                        screenShake.animateTo(0f, tween(50))
                    }
                } else if (card.baseShield > 0) {
                    // Shield / Defend pulse
                    launch {
                        playerScale.animateTo(1.22f, tween(120, easing = FastOutSlowInEasing))
                        playerScale.animateTo(1.0f, tween(160))
                    }
                } else if (card.baseHeal > 0) {
                    // Heal card pulse
                    launch {
                        playerScale.animateTo(1.20f, tween(140, easing = FastOutSlowInEasing))
                        playerScale.animateTo(1.0f, tween(180))
                    }
                } else {
                    // Other ability pulse
                    launch {
                        playerScale.animateTo(1.15f, tween(100))
                        playerScale.animateTo(1.0f, tween(140))
                    }
                }
            }
        }
    }

    // Run dynamic multi-phase boss attack animation sequence when triggered
    LaunchedEffect(bossAttackTrigger) {
        if (bossAttackTrigger > 0 && lastMove != null) {
            val move = lastMove!!
            bossAttackBannerText = "⚡ ${move.moveName}: \"${move.bossRoarQuote}\""
            val dmgTaken = lastDamageTaken ?: move.baseDamage
            displayedDamageText = if (move.shieldPierce) "💥 -$dmgTaken DMG (PIERCED!)" else "💥 -$dmgTaken DMG"

            // Phase 1: Wind-up / Telegraph Focus (300ms)
            launch { bossScale.animateTo(1.32f, tween(260, easing = FastOutSlowInEasing)) }
            launch { bossAuraAlpha.animateTo(0.9f, tween(260)) }
            delay(280)

            // Phase 2: Boss Lunge / Strike (240ms)
            launch { bossTranslationX.animateTo(-60f, tween(200, easing = LinearOutSlowInEasing)) }
            launch { flashOverlayAlpha.animateTo(0.65f, tween(100)) }
            delay(180)

            // Phase 3: Impact Shake, Screen Flash, Player Recoil & Floating Damage (400ms)
            launch { flashOverlayAlpha.animateTo(0f, tween(250)) }
            launch { playerFlinchX.animateTo(22f, tween(90)) }
            launch { screenShake.animateTo(14f, tween(80)) }
            damagePopupScale.snapTo(0.6f)
            damagePopupAlpha.snapTo(1f)
            launch { damagePopupScale.animateTo(1.25f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
            delay(120)
            launch { screenShake.animateTo(0f, tween(180)) }
            launch { playerFlinchX.animateTo(0f, tween(200)) }
            delay(320)

            // Phase 4: Boss Return & Recovery (300ms)
            launch { bossTranslationX.animateTo(0f, tween(260, easing = FastOutSlowInEasing)) }
            launch { bossScale.animateTo(1.0f, tween(260)) }
            launch { bossAuraAlpha.animateTo(0f, tween(220)) }
            launch { damagePopupAlpha.animateTo(0f, tween(250)) }
            delay(260)

            bossAttackBannerText = null
            displayedDamageText = null
        }
    }

    if (boss == null) return

    val currentBoss = boss!!
    val currentScript = script ?: BossBattleScripts.getScriptForBoss(currentBoss)

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(currentScript.bgStartColor),
            Color(currentScript.bgEndColor)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .graphicsLayer {
                translationX = screenShake.value
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // --- TOP HEADER BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Boss Name & Turn Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF3E1C14),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, BossGold)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("👑 Turn $turn", color = BossGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showScriptTimelineDialog = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Text("📜", fontSize = 16.sp)
                    }
                }

                // Stance & Deck Info Pill + Flee Button (Auto & AFK removed per user directive)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stance Switch Button
                    Surface(
                        color = when (adventuringCombatStance.uppercase()) {
                            "MELEE" -> Color(0xFF8B0000)
                            "RANGED" -> Color(0xFF1B5E20)
                            "MAGIC" -> Color(0xFF0D47A1)
                            else -> Color(0xFF3E2723)
                        },
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, BossGold),
                        modifier = Modifier.clickable(enabled = !isBossAttacking) {
                            showStanceSwitchDialog = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                when (adventuringCombatStance.uppercase()) {
                                    "MELEE" -> "⚔️ Melee"
                                    "RANGED" -> "🏹 Ranged"
                                    "MAGIC" -> "🪄 Magic"
                                    else -> "🛡️ Defensive"
                                },
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Flee Button
                    Surface(
                        color = Color(0xFF5C1D1D),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF5252)),
                        modifier = Modifier.clickable(enabled = !isBossAttacking) {
                            viewModel.retreatFromBossBattle()
                        }
                    ) {
                        Text(
                            "🏃 Retreat",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- BOSS STAGE & COMBAT ARENA ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF19120C)),
                border = BorderStroke(1.5.dp, BossGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 1. BOSS INFO & HP BAR
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(currentBoss.iconSymbol, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentBoss.name,
                                        color = BossGold,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(Lvl ${currentBoss.reqCombatLevel})",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "$bossHp / $bossMaxHp HP",
                                    color = Color(0xFFFF8A80),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            val bossHpRatio = (bossHp.toFloat() / bossMaxHp.toFloat()).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFF3E1F1F))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(bossHpRatio)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFFF3D00), Color(0xFFFFD600))
                                            )
                                        )
                                )
                            }
                        }

                        // 2. BOSS TELEGRAPH / INTENT BANNER
                        if (nextMove != null) {
                            Surface(
                                color = Color(0xFF2C160F),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFF9800)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(nextMove!!.emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Next Intent: ${nextMove!!.moveName} (${nextMove!!.baseDamage} DMG)",
                                            color = Color(0xFFFFB74D),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = nextMove!!.telegraphWarning,
                                            color = Color(0xFFE0E0E0),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (nextMove!!.shieldPierce) {
                                        Surface(
                                            color = Color(0xFF7F0000),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "PIERCE",
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. CENTER DUEL ARENA (Boss vs Companion Sprites)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // COMPANION SIDE
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = playerScale.value
                                    scaleY = playerScale.value
                                    translationX = playerFlinchX.value + playerAttackLungeX.value
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .background(Color(0xFF241C16), CircleShape)
                                        .border(2.dp, if (playerShield > 0) Color(0xFF42A5F5) else BossGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = petState.petType.iconSymbol,
                                        fontSize = 34.sp
                                    )
                                    // Tactical stance badge
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = CircleShape,
                                        border = BorderStroke(1.dp, BossGold),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = when (adventuringCombatStance.uppercase()) {
                                                    "RANGED" -> "🏹"
                                                    "MAGIC" -> "🧙"
                                                    else -> "⚔️"
                                                },
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    if (playerShield > 0) {
                                        Surface(
                                            color = Color(0xFF0D47A1),
                                            shape = CircleShape,
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(22.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("$playerShield", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    petState.customName.ifBlank { "Companion" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                // HEALTH BAR FOR PLAYER (Generous width and dedicated clear HP text so it never cuts off)
                                val petHpRatio = (petHp.toFloat() / petMaxHp.toFloat()).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .width(96.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1E150F))
                                        .border(1.dp, Color(0xFF4A3420), RoundedCornerShape(4.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = petHpRatio)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (petHpRatio < 0.35f) Color(0xFFFF5252)
                                                else if (petHpRatio < 0.7f) Color(0xFFFFB74D)
                                                else Color(0xFF4CAF50)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$petHp / $petMaxHp HP",
                                    color = if (petHpRatio < 0.35f) Color(0xFFFF5252) else if (petHpRatio < 0.7f) Color(0xFFFFB74D) else Color(0xFF81C784),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // VS BADGE
                            Surface(
                                color = Color(0xFF332014),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, BossGold),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("VS", color = BossGold, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }

                            // BOSS SIDE (With dramatic scale, aura, hit flash, and shake animations)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = bossScale.value
                                    scaleY = bossScale.value
                                    translationX = bossTranslationX.value + bossShakeX.value
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .background(
                                            if (bossHitFlashAlpha.value > 0f) Color(0xFFFF1744).copy(alpha = bossHitFlashAlpha.value)
                                            else if (bossAuraAlpha.value > 0f) Color(0xFFFF1744).copy(alpha = bossAuraAlpha.value)
                                            else Color(0xFF3B1F16),
                                            CircleShape
                                        )
                                        .border(
                                            2.5.dp,
                                            if (bossHitFlashAlpha.value > 0f) Color(0xFFFF1744) else Color(0xFFFF5722),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = currentBoss.iconSymbol, fontSize = 40.sp)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    currentBoss.name,
                                    color = Color(0xFFFFB74D),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    "Phase: ${currentScript.phaseName}",
                                    color = Color.LightGray,
                                    fontSize = 9.5.sp
                                )
                            }
                        }

                        // 4. COMBAT LOG TICKER
                        Surface(
                            color = Color(0xFF140D09),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF3E2D1F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            val logState = rememberLazyListState()
                            LaunchedEffect(battleLog.size) {
                                if (battleLog.isNotEmpty()) {
                                    logState.animateScrollToItem(battleLog.size - 1)
                                }
                            }
                            LazyColumn(
                                state = logState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                items(battleLog) { entry ->
                                    Text(
                                        text = entry,
                                        color = if (entry.contains("Turn") || entry.contains("VICTORY")) BossGold else Color.LightGray,
                                        fontSize = 9.5.sp,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // IMPACT FLASH OVERLAY DURING BOSS ATTACK
                    if (flashOverlayAlpha.value > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFF1744).copy(alpha = flashOverlayAlpha.value))
                        )
                    }

                    // FLOATING DAMAGE POPUP OVER COMPANION
                    if (displayedDamageText != null && damagePopupAlpha.value > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 24.dp, top = 60.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = displayedDamageText!!,
                                color = Color(0xFFFF5252),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = damagePopupScale.value
                                        scaleY = damagePopupScale.value
                                        alpha = damagePopupAlpha.value
                                    }
                                    .background(Color(0xCC000000), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // ACTIVE BOSS ATTACK BANNER / FROZEN NOTIFICATION
                    if (isBossAttacking || bossAttackBannerText != null) {
                        Surface(
                            color = Color(0xEE1E0B0B),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFF1744)),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "⚡ BOSS ATTACKING (USER FROZEN) ⚡",
                                    color = Color(0xFFFF5252),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    bossAttackBannerText ?: "Boss is executing turn action...",
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // --- BOTTOM PLAYER ACTION & DECK SECTION ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1610)),
                border = BorderStroke(1.5.dp, BossGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = if (isBossAttacking) 0.55f else 1.0f
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    // Energy & Quick Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Energy Orbs
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⚡ ENERGY:", color = BossGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            for (i in 1..maxEnergy) {
                                val isFilled = i <= playerEnergy
                                Surface(
                                    color = if (isFilled) Color(0xFFFFD54F) else Color.DarkGray,
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, if (isFilled) Color.White else Color.Gray),
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isFilled) {
                                            Text("⚡", fontSize = 8.sp)
                                        }
                                    }
                                }
                            }
                            Text("$playerEnergy/$maxEnergy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        // Deck Viewer Button & Food Bag Button
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Deck Manager Button
                            Surface(
                                color = Color(0xFF2E2419),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, BossGold),
                                modifier = Modifier.clickable(enabled = !isBossAttacking) {
                                    showDeckManagerDialog = true
                                }
                            ) {
                                Text(
                                    "🃏 Deck (${playerDeck.size + playerHand.size + playerDiscard.size})",
                                    color = BossGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            // Quick Food Button (Consumes 1 Energy)
                            Surface(
                                color = if (playerEnergy >= 1) Color(0xFF2E2419) else Color(0xFF1C1713),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (playerEnergy >= 1) Color(0xFF4CAF50) else Color.DarkGray),
                                modifier = Modifier.clickable(enabled = !isBossAttacking) {
                                    showQuickFoodDialog = true
                                }
                            ) {
                                Text(
                                    "🍖 Eat (⚡1)",
                                    color = if (playerEnergy >= 1) Color(0xFF81C784) else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            // End Turn Button
                            Button(
                                onClick = { viewModel.endBossBattleTurn() },
                                enabled = !isBossAttacking && status == BossCombatStatus.IN_PROGRESS,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    if (isBossAttacking) "⏳ Freezing..." else "⏳ End Turn",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Player Card Hand (Scrollable Row with CombatCardView matching Adventuring tab design)
                    if (playerHand.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(155.dp)
                                .background(Color(0x33000000), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No cards in hand. Tap 'End Turn' to draw next turn!", color = Color.Gray, fontSize = 11.sp)
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(155.dp)
                        ) {
                            items(playerHand) { card ->
                                val effectiveEnergy = if (isBossAttacking || status != BossCombatStatus.IN_PROGRESS) 0 else playerEnergy
                                CombatCardView(
                                    card = card,
                                    currentEnergy = effectiveEnergy,
                                    onPlayCard = { playedCard ->
                                        if (status == BossCombatStatus.IN_PROGRESS && !isBossAttacking && playerEnergy >= playedCard.energyCost) {
                                            viewModel.playBossCombatCard(playedCard)
                                            triggerPlayerCardAnim(playedCard)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SCRIPT ROTATION TIMELINE DIALOG
        // ==========================================
        if (showScriptTimelineDialog) {
            Dialog(onDismissRequest = { showScriptTimelineDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E150F)),
                    border = BorderStroke(1.5.dp, BossGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📜 ${currentBoss.name} Script", color = BossGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { showScriptTimelineDialog = false }, modifier = Modifier.size(24.dp)) {
                                Text("✕", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Every boss follows a scripted move sequence. Study the rotation to time your defenses and attacks!",
                            color = Color.LightGray,
                            fontSize = 10.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(currentScript.standardRotation) { move ->
                                val isCurrent = move.turnNumber == ((turn - 1) % currentScript.standardRotation.size + 1)
                                Surface(
                                    color = if (isCurrent) Color(0xFF3E2718) else Color(0xFF16100B),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isCurrent) BossGold else Color(0xFF3E2D1F)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(move.emoji, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    "Turn ${move.turnNumber}: ${move.moveName}",
                                                    color = if (isCurrent) BossGold else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp
                                                )
                                            }
                                            Text(
                                                "${move.baseDamage} DMG",
                                                color = Color(0xFFFF7043),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(move.description, color = Color.LightGray, fontSize = 10.sp)
                                        Text("\"${move.bossRoarQuote}\"", color = Color(0xFFFFB74D), fontSize = 9.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // STANCE SWITCH DIALOG
        // ==========================================
        if (showStanceSwitchDialog) {
            AlertDialog(
                onDismissRequest = { showStanceSwitchDialog = false },
                containerColor = Color(0xFF1E150F),
                title = {
                    Text("⚔️ Switch Combat Style", color = BossGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Select a combat style configured in your Adventuring tab. Switching styles will equip that stance's deck immediately.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        val stances = listOf(
                            Triple("MELEE", "⚔️ Melee (Attack / Defence)", Color(0xFF8B0000)),
                            Triple("RANGED", "🏹 Blowdarts (Ranged / Consumables)", Color(0xFF1B5E20)),
                            Triple("MAGIC", "🪄 Incantations (Magic / Warding)", Color(0xFF0D47A1)),
                            Triple("DEFENSIVE", "🛡️ Defensive (Full Block / Armor)", Color(0xFF3E2723))
                        )
                        stances.forEach { (stanceKey, label, color) ->
                            val isCurrent = adventuringCombatStance.equals(stanceKey, ignoreCase = true)
                            Button(
                                onClick = {
                                    viewModel.setAdventuringCombatStance(stanceKey, endTurn = false)
                                    showStanceSwitchDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCurrent) color else Color(0xFF2C1F16)
                                ),
                                border = BorderStroke(1.dp, if (isCurrent) BossGold else Color(0xFF4A3B2C)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showStanceSwitchDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3B2C))
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            )
        }

        // ==========================================
        // DECK MANAGER DIALOG INTEGRATION
        // ==========================================
        if (showDeckManagerDialog) {
            DeckManagerDialog(
                stance = adventuringCombatStance,
                customCardIds = customDeckCardIds,
                onToggleCard = { cardId -> viewModel.toggleCustomDeckCard(cardId) },
                onDismiss = { showDeckManagerDialog = false },
                onOpenFullDeckBuilder = { showDeckManagerDialog = false }
            )
        }

        // ==========================================
        // QUICK FOOD DIALOG (Costs 1 Energy)
        // ==========================================
        if (showQuickFoodDialog) {
            val foodItems = inventoryItems.filter { (it.category == ItemCategory.FOOD || it.category == ItemCategory.POTION || it.healHp > 0) && it.quantity > 0 }
            AlertDialog(
                onDismissRequest = { showQuickFoodDialog = false },
                containerColor = Color(0xFF1E150F),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🍖 Quick Food & Healing", color = BossGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(
                            color = if (playerEnergy >= 1) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "⚡ Cost: 1 Energy ($playerEnergy/$maxEnergy)",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                text = {
                    Column {
                        if (playerEnergy < 1) {
                            Text(
                                "⚠️ 0 Energy available! Eating food or drinking potions consumes 1 Energy during battle. End turn to restore energy.",
                                color = Color(0xFFFF8A80),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        if (foodItems.isEmpty()) {
                            Text("No food in inventory! Cook food or buy provisions first.", color = Color.LightGray, fontSize = 11.sp)
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 240.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(foodItems) { food ->
                                    val canEat = playerEnergy >= 1 && !isBossAttacking && status == BossCombatStatus.IN_PROGRESS
                                    Surface(
                                        color = if (canEat) Color(0xFF2C1E14) else Color(0xFF1C1510),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, if (canEat) Color(0xFF4CAF50) else Color.DarkGray),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = canEat) {
                                                viewModel.useBossConsumableItem(food.id)
                                                showQuickFoodDialog = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(food.name, color = if (canEat) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                                Text("${food.quantity}x in bag", color = Color.LightGray, fontSize = 9.5.sp)
                                            }
                                            Surface(
                                                color = if (canEat) Color(0xFF2E7D32) else Color.DarkGray,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    if (canEat) "Eat (-1 ⚡)" else "Need 1 ⚡",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showQuickFoodDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            )
        }

        // ==========================================
        // VICTORY DIALOG OVERLAY
        // ==========================================
        if (status == BossCombatStatus.VICTORY) {
            val loot by viewModel.bossBattleVictoryLoot.collectAsStateWithLifecycle()
            val xp by viewModel.bossBattleVictoryXp.collectAsStateWithLifecycle()
            AlertDialog(
                onDismissRequest = {},
                containerColor = Color(0xFF1F160C),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("🏆 BOSS SLAIN! 🏆", color = BossGold, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(currentBoss.name, color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚔️ Experience Gained:", color = Color.LightGray, fontSize = 11.sp)
                        Text("+$xp Slayer XP & +${(xp * 1.5).toLong()} Combat XP", color = Color(0xFF70E000), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🎁 Loot Dropped:", color = BossGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        loot.forEach { item ->
                            Text("• ${item.maxQty}x ${item.itemName}", color = Color.White, fontSize = 11.5.sp)
                        }
                        Text("• 2x Dragon Bones", color = Color.White, fontSize = 11.5.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.restartInteractiveBossBattle() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("⚔️ Fight Again", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { viewModel.retreatFromBossBattle() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3B2C))
                    ) {
                        Text("🚪 Return to Lair", color = Color.White)
                    }
                }
            )
        }

        // ==========================================
        // DEFEAT DIALOG OVERLAY
        // ==========================================
        if (status == BossCombatStatus.DEFEAT) {
            AlertDialog(
                onDismissRequest = {},
                containerColor = Color(0xFF240E0E),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("💀 DEFEATED! 💀", color = Color(0xFFFF5252), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(currentBoss.name, color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                text = {
                    Text(
                        text = "Your companion was overwhelmed by the boss's ferocious assault! You retreated safely.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.restartInteractiveBossBattle() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                    ) {
                        Text("🔄 Try Again", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { viewModel.retreatFromBossBattle() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3B2C))
                    ) {
                        Text("🏃 Flee Lair", color = Color.White)
                    }
                }
            )
        }
    }
}
