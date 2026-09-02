package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.CardType
import com.example.data.models.CombatCard
import com.example.data.models.DefaultCombatCards
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

val OsrsGold = Color(0xFFFFD700)
val OsrsDarkPanel = Color(0xFF1B2430)
val OsrsTextYellow = Color(0xFFFFE866)

data class CombatHitParticle(
    val id: Long = Random.nextLong(),
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val radius: Float,
    val symbol: String? = null,
    val maxLifeMs: Float = 500f,
    var currentLifeMs: Float = 0f
)

@Composable
fun CombatHitParticleOverlay(
    triggerKey: Any?,
    modifier: Modifier = Modifier,
    particleColor: Color = Color(0xFFFFD700),
    symbolsList: List<String> = listOf("⚔️", "💥", "✨", "🔥", "⚡", "💫", "🌟")
) {
    val particles = remember { mutableStateListOf<CombatHitParticle>() }

    // Spawn particle burst whenever triggerKey changes to a non-null / non-zero value
    LaunchedEffect(triggerKey) {
        if (triggerKey != null && triggerKey != 0) {
            val rand = Random(System.currentTimeMillis())
            val count = rand.nextInt(16, 24)
            val colors = listOf(
                particleColor,
                Color(0xFFFFD700), // Gold
                Color(0xFFFF3D00), // Crimson
                Color(0xFF00E5FF), // Cyan spark
                Color(0xFFE040FB), // Purple magic
                Color(0xFFFFEA00)  // Star yellow
            )

            for (i in 0 until count) {
                val angle = rand.nextDouble(0.0, Math.PI * 2)
                val speed = rand.nextFloat() * 14f + 5f
                val symbol = if (i % 3 == 0 && symbolsList.isNotEmpty()) symbolsList[rand.nextInt(symbolsList.size)] else null
                particles.add(
                    CombatHitParticle(
                        x = 0f,
                        y = 0f,
                        vx = (cos(angle) * speed).toFloat(),
                        vy = (sin(angle) * speed).toFloat() - 2f,
                        color = colors[rand.nextInt(colors.size)],
                        radius = rand.nextFloat() * 10f + 4f,
                        symbol = symbol,
                        maxLifeMs = rand.nextFloat() * 250f + 350f
                    )
                )
            }
        }
    }

    // 60fps physics & decay update loop
    LaunchedEffect(particles.size) {
        if (particles.isNotEmpty()) {
            var lastTime = withFrameNanos { it }
            while (particles.isNotEmpty()) {
                withFrameNanos { frameTime ->
                    val dtMs = ((frameTime - lastTime) / 1_000_000f).coerceIn(1f, 32f)
                    lastTime = frameTime

                    val iter = particles.iterator()
                    while (iter.hasNext()) {
                        val p = iter.next()
                        p.currentLifeMs += dtMs
                        p.x += p.vx * (dtMs / 16f)
                        p.y += p.vy * (dtMs / 16f)
                        p.vy += 0.35f * (dtMs / 16f) // Gravity pull
                        p.vx *= 0.94f // Air friction
                        if (p.currentLifeMs >= p.maxLifeMs) {
                            iter.remove()
                        }
                    }
                }
            }
        }
    }

    val textPaint = remember {
        android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    val density = LocalDensity.current

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        particles.forEach { p ->
            val progress = (p.currentLifeMs / p.maxLifeMs).coerceIn(0f, 1f)
            val alpha = (1f - progress).coerceIn(0f, 1f)
            val px = centerX + p.x
            val py = centerY + p.y

            if (p.symbol != null) {
                drawContext.canvas.nativeCanvas.apply {
                    val spSize = with(density) { (p.radius * 2.5f).sp.toPx() }
                    textPaint.textSize = spSize
                    textPaint.alpha = (alpha * 255).toInt()
                    drawText(p.symbol, px, py, textPaint)
                }
            } else {
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.radius * (1f - progress * 0.4f),
                    center = Offset(px, py)
                )
            }
        }
    }
}

@Composable
fun EnlargedCardDetailDialog(
    card: CombatCard,
    isPlayable: Boolean = true,
    currentEnergy: Int = 3,
    onPlayCard: ((CombatCard) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val skillColor = card.skill.accentColor
    val typeBorderColor = when (card.cardType) {
        CardType.ATTACK -> Color(0xFFFF5252)
        CardType.DEFENSE -> Color(0xFF448AFF)
        CardType.MAGIC -> Color(0xFFE040FB)
        CardType.CONSUMABLE -> Color(0xFF69F0AE)
        CardType.ULTIMATE -> OsrsGold
        CardType.BUFF -> Color(0xFF60A5FA)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = OsrsDarkPanel,
            border = BorderStroke(2.5.dp, typeBorderColor),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                skillColor.copy(alpha = 0.40f),
                                Color(0xFF131B24),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Energy Cost Badge
                        Surface(
                            color = if (isPlayable) Color(0xFFFFD54F) else Color.DarkGray,
                            shape = CircleShape,
                            border = BorderStroke(1.5.dp, Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("⚡", fontSize = 14.sp)
                                Text(
                                    "${card.energyCost} Energy",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Skill & Level Badge
                        Surface(
                            color = skillColor.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, skillColor)
                        ) {
                            Text(
                                "${card.skill.iconSymbol} ${card.skill.displayName} Lv.${card.reqLevel}",
                                color = OsrsTextYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Card Big Emoji & Title
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(skillColor.copy(alpha = 0.25f))
                            .border(2.dp, skillColor, CircleShape)
                    ) {
                        Text(card.iconEmoji, fontSize = 42.sp)
                    }

                    Text(
                        text = card.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Stance & Card Type Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = typeBorderColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, typeBorderColor)
                        ) {
                            Text(
                                card.cardType.name,
                                color = typeBorderColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = Color(0xFF334155),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            val stanceEmoji = when (card.stance) {
                                "MELEE" -> "⚔️ MELEE"
                                "RANGED" -> "🏹 RANGED"
                                "MAGIC" -> "🔮 MAGIC"
                                else -> "🌟 ALL STANCES"
                            }
                            Text(
                                stanceEmoji,
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Stats Grid Row (if card has stats)
                    if (card.baseDamage > 0 || card.baseShield > 0 || card.baseHeal > 0 || card.nextAttackBuff > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (card.baseDamage > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⚔️ Damage", color = Color.Gray, fontSize = 10.sp)
                                    Text("+${card.baseDamage}", color = Color(0xFFFF5252), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (card.baseShield > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🛡️ Shield", color = Color.Gray, fontSize = 10.sp)
                                    Text("+${card.baseShield}", color = Color(0xFF448AFF), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (card.baseHeal > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💖 Heal", color = Color.Gray, fontSize = 10.sp)
                                    Text("+${card.baseHeal}", color = Color(0xFF69F0AE), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (card.nextAttackBuff > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("✨ Buff", color = Color.Gray, fontSize = 10.sp)
                                    Text("+${card.nextAttackBuff}", color = Color(0xFF60A5FA), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Full Description Container
                    Surface(
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Card Description & Effects:",
                                color = OsrsGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = card.description,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Text("Close", color = Color.LightGray)
                        }

                        if (onPlayCard != null) {
                            Button(
                                onClick = { onPlayCard(card) },
                                enabled = isPlayable,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OsrsGold,
                                    disabledContainerColor = Color.DarkGray
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (isPlayable) "⚔️ Play Card" else "⚡ Low Energy",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CombatCardView(
    card: CombatCard,
    currentEnergy: Int,
    onPlayCard: (CombatCard) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlayable = currentEnergy >= card.energyCost
    val coroutineScope = rememberCoroutineScope()
    val cardScaleAnim = remember { Animatable(1.0f) }
    val dragOffsetY = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var showEnlargedDetail by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val skillColor = card.skill.accentColor
    val cardBgGradient = listOf(
        skillColor.copy(alpha = 0.85f),
        skillColor.copy(alpha = 0.45f),
        Color(0xFF0F172A)
    )

    val typeBorderColor = when (card.cardType) {
        CardType.ATTACK -> Color(0xFFFF5252)
        CardType.DEFENSE -> Color(0xFF448AFF)
        CardType.MAGIC -> Color(0xFFE040FB)
        CardType.CONSUMABLE -> Color(0xFF69F0AE)
        CardType.ULTIMATE -> OsrsGold
        CardType.BUFF -> Color(0xFF60A5FA)
    }

    val borderColor = if (isPlayable) typeBorderColor.copy(alpha = pulseGlow) else Color.DarkGray
    val isPastThreshold = dragOffsetY.value < -100f

    if (showEnlargedDetail) {
        EnlargedCardDetailDialog(
            card = card,
            isPlayable = isPlayable,
            currentEnergy = currentEnergy,
            onPlayCard = { playedCard ->
                onPlayCard(playedCard)
                showEnlargedDetail = false
            },
            onDismiss = { showEnlargedDetail = false }
        )
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(if (isPlayable) 2.dp else 1.dp, if (isPastThreshold) Color(0xFF00FF9D) else borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlayable) 8.dp else 1.dp),
        modifier = modifier
            .width(115.dp)
            .height(150.dp)
            .graphicsLayer {
                translationY = dragOffsetY.value
                scaleX = if (isPastThreshold) 1.08f else cardScaleAnim.value
                scaleY = if (isPastThreshold) 1.08f else cardScaleAnim.value
                rotationZ = (dragOffsetY.value / 25f).coerceIn(-12f, 12f)
            }
            .shadow(if (isPlayable) 8.dp else 0.dp, RoundedCornerShape(10.dp))
            .pointerInput(isPlayable) {
                if (isPlayable) {
                    detectVerticalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            if (dragOffsetY.value < -90f) {
                                coroutineScope.launch {
                                    dragOffsetY.animateTo(-350f, tween(120))
                                    onPlayCard(card)
                                    dragOffsetY.snapTo(0f)
                                }
                            } else {
                                coroutineScope.launch {
                                    dragOffsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                }
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            coroutineScope.launch { dragOffsetY.animateTo(0f, spring()) }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val nextY = (dragOffsetY.value + dragAmount).coerceAtMost(0f)
                                dragOffsetY.snapTo(nextY)
                            }
                        }
                    )
                }
            }
            .combinedClickable(
                enabled = true,
                onClick = {
                    if (isPlayable) {
                        coroutineScope.launch {
                            cardScaleAnim.animateTo(0.88f, tween(40))
                            cardScaleAnim.animateTo(1.12f, tween(70))
                            cardScaleAnim.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        onPlayCard(card)
                    } else {
                        showEnlargedDetail = true
                    }
                },
                onLongClick = {
                    showEnlargedDetail = true
                }
            )
            .testTag("combat_card_${card.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(cardBgGradient))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Energy Cost & Card Type Emoji
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Energy Cost Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isPlayable) Color(0xFFFFD54F) else Color.Gray)
                            .border(1.dp, Color.White, CircleShape)
                    ) {
                        Text(
                            "⚡${card.energyCost}",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Skill Picture Badge in Top Right Corner
                    Surface(
                        color = skillColor.copy(alpha = 0.95f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color.White),
                        shadowElevation = 3.dp,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(card.skill.iconSymbol, fontSize = 10.sp)
                        }
                    }
                }

                // Card Title
                Text(
                    card.title,
                    color = Color.White,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Illustration Box
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x55000000))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                ) {
                    Text(card.iconEmoji, fontSize = 26.sp)
                }

                // Card Description
                Surface(
                    color = Color(0xCC000000),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        card.description,
                        color = OsrsTextYellow,
                        fontSize = 8.5.sp,
                        lineHeight = 10.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                    )
                }
            }

            // Drag Upward Prompt Overlay when user drags card
            if (isDragging && dragOffsetY.value < -30f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isPastThreshold) Color(0xAA004D40)
                            else Color(0x88000000)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = if (isPastThreshold) Color(0xFF00C853) else Color(0xDD333333),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (isPastThreshold) Color.White else Color.Yellow)
                    ) {
                        Text(
                            if (isPastThreshold) "⚡ RELEASE TO PLAY!" else "⬆️ SLIDE UP",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (!isPlayable) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = Color(0xDD333333),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Need ⚡${card.energyCost}",
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CombatDeckHandSection(
    currentEnergy: Int,
    maxEnergy: Int,
    playerShield: Int,
    hand: List<CombatCard>,
    drawDeckCount: Int,
    discardPileCount: Int,
    stance: String,
    nextAttackBuff: Int = 0,
    activeIncantationIds: Set<String> = emptySet(),
    activeCookingBuffs: List<com.example.data.models.ActiveCookingBuff> = emptyList(),
    onPlayCard: (CombatCard) -> Unit,
    onEndTurn: () -> Unit,
    onOpenDeckManager: () -> Unit
) {
    val hasShield = playerShield > 0
    val hasNextAtkBuff = nextAttackBuff > 0
    val activeIncants = remember(activeIncantationIds) {
        if (activeIncantationIds.isEmpty()) emptyList()
        else com.example.data.models.IncantationsData.ALL_INCANTATIONS.filter { activeIncantationIds.contains(it.id) }
    }
    val now = System.currentTimeMillis()
    val activeMealBuff = activeCookingBuffs.firstOrNull { it.expiryTimeMs > now }
    val hasMealBuff = activeMealBuff != null
    val hasAnyBuffs = hasShield || hasNextAtkBuff || activeIncants.isNotEmpty() || hasMealBuff

    Card(
        colors = CardDefaults.cardColors(containerColor = OsrsDarkPanel),
        border = BorderStroke(1.5.dp, OsrsGold),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Status Bar: Energy Line (Energy Counter & Deck Manager Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Energy Counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "⚡ ENERGY:",
                        color = OsrsGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (i in 1..maxEnergy) {
                            val isActive = i <= currentEnergy
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) Color(0xFFFFD54F) else Color.DarkGray)
                                    .border(1.dp, if (isActive) Color.White else Color.Gray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isActive) {
                                    Text("⚡", fontSize = 8.sp)
                                }
                            }
                        }
                    }
                    Text(
                        "$currentEnergy/$maxEnergy",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Deck Manager Button
                Surface(
                    color = Color(0xFF263238),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, OsrsGold),
                    modifier = Modifier.clickable { onOpenDeckManager() }
                ) {
                    Text(
                        "🃏 Deck (${drawDeckCount + hand.size + discardPileCount})",
                        color = OsrsTextYellow,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Scrollable Buffs Row (Under the Energy Line)
            if (hasAnyBuffs) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Shield Indicator
                    if (hasShield) {
                        item {
                            Surface(
                                color = Color(0xFF0D47A1),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF448AFF))
                            ) {
                                Text(
                                    "🛡️ Shield: +$playerShield",
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Next Attack Buff Badge
                    if (hasNextAtkBuff) {
                        item {
                            Surface(
                                color = Color(0xFFB91C1C),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                            ) {
                                Text(
                                    "🔥 Next Atk: +$nextAttackBuff Dmg",
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Active Incantations
                    items(activeIncants) { incant ->
                        Surface(
                            color = Color(0xFF1B3B22),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF00FF9D))
                        ) {
                            Text(
                                "${incant.iconEmoji} ${incant.name}",
                                color = Color(0xFF00FF9D),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Active Meal / Cauldron Recipe Buff
                    if (hasMealBuff && activeMealBuff != null) {
                        item {
                            Surface(
                                color = Color(0xFF3E2723),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFB74D))
                            ) {
                                Text(
                                    "${activeMealBuff.emoji} ${activeMealBuff.recipeName}: ${activeMealBuff.buffEffect}",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Cards Hand Row
            if (hand.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0x33000000), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🖐️ Your hand is empty!", color = Color.Gray, fontSize = 12.sp)
                        Text("Tap END TURN / DRAW below to draw new cards!", color = OsrsGold, fontSize = 11.sp)
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(
                        items = hand,
                        key = { index, card -> "${card.id}_$index" }
                    ) { _, card ->
                        CombatCardView(
                            card = card,
                            currentEnergy = currentEnergy,
                            onPlayCard = onPlayCard
                        )
                    }
                }
            }

            // Controls Row: Draw Pile - END TURN Button - Discard Pile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Draw Pile Badge
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🎴 Draw:", color = Color.LightGray, fontSize = 10.sp)
                        Text("$drawDeckCount", color = OsrsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Main END TURN / REDRAW Button
                Button(
                    onClick = onEndTurn,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, OsrsGold),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .padding(horizontal = 8.dp)
                        .testTag("end_turn_button")
                ) {
                    Text(
                        "🌙 END TURN / DRAW 4 🃏",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Discard Pile Badge
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🗑️ Discard:", color = Color.LightGray, fontSize = 10.sp)
                        Text("$discardPileCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DeckManagerDialog(
    stance: String,
    customCardIds: Set<String>,
    onToggleCard: (String) -> Unit,
    onDismiss: () -> Unit,
    skillXpMap: Map<OsrsSkill, Long> = emptyMap(),
    onOpenFullDeckBuilder: (() -> Unit)? = null
) {
    val allCards = remember { DefaultCombatCards.ALL_CARDS }

    var selectedSkillFilter by remember { mutableStateOf<OsrsSkill?>(null) }
    var showOnlySelected by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var enlargedCard by remember { mutableStateOf<CombatCard?>(null) }

    val selectedCardsCount = remember(customCardIds, stance, allCards) {
        if (customCardIds.isEmpty()) {
            allCards.count { it.stance == stance || it.stance == "ALL" }
        } else {
            customCardIds.size
        }
    }

    val filteredCards = remember(
        allCards, selectedSkillFilter, showOnlySelected, searchQuery, customCardIds, stance
    ) {
        allCards.filter { card ->
            val matchesSelected = if (showOnlySelected) {
                if (customCardIds.isEmpty()) {
                    card.stance == stance || card.stance == "ALL"
                } else {
                    customCardIds.contains(card.id)
                }
            } else true

            val matchesSkill = if (selectedSkillFilter != null) {
                card.skill == selectedSkillFilter
            } else true

            val matchesSearch = if (searchQuery.isNotBlank()) {
                card.title.contains(searchQuery, ignoreCase = true) ||
                card.description.contains(searchQuery, ignoreCase = true) ||
                card.skill.displayName.contains(searchQuery, ignoreCase = true) ||
                card.skill.name.contains(searchQuery, ignoreCase = true)
            } else true

            matchesSelected && matchesSkill && matchesSearch
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OsrsDarkPanel,
            border = BorderStroke(2.dp, OsrsGold),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "🃏 Deck Manager",
                            color = OsrsGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2E7D32)
                        ) {
                            Text(
                                text = "$selectedCardsCount Cards",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    TextButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("✕ Close", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Text(
                    "Select cards to customize your active deck. Filter by skill tabs below to build your deck strategy!",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("🔍 Search cards or skill effects...", fontSize = 11.sp, color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OsrsGold,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF141C24),
                        unfocusedContainerColor = Color(0xFF141C24)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                // SKILL FILTER TABS (Scrollable Row for each skill)
                Text(
                    "Sort by Skill:",
                    color = OsrsTextYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isSelected = !showOnlySelected && selectedSkillFilter == null
                        FilterChipPill(
                            label = "🌐 All Cards",
                            count = allCards.size,
                            isSelected = isSelected,
                            onClick = {
                                showOnlySelected = false
                                selectedSkillFilter = null
                            }
                        )
                    }

                    item {
                        FilterChipPill(
                            label = "✓ Deck Cards",
                            count = selectedCardsCount,
                            isSelected = showOnlySelected,
                            onClick = {
                                showOnlySelected = !showOnlySelected
                                selectedSkillFilter = null
                            }
                        )
                    }

                    val availableSkills = OsrsSkill.entries.filter { skill ->
                        allCards.any { it.skill == skill }
                    }

                    items(availableSkills, key = { it.name }) { skill ->
                        val isSelected = !showOnlySelected && selectedSkillFilter == skill
                        val cardCount = allCards.count { it.skill == skill }
                        val playerLvl = OsrsXpCalculator.getLevelForXp(skillXpMap[skill] ?: 0L)

                        FilterChipPill(
                            label = "${skill.iconSymbol} ${skill.displayName} (Lv.$playerLvl)",
                            count = cardCount,
                            isSelected = isSelected,
                            accentColor = skill.accentColor,
                            onClick = {
                                showOnlySelected = false
                                selectedSkillFilter = if (selectedSkillFilter == skill) null else skill
                            }
                        )
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.4f), thickness = 0.5.dp)

                // Subheader & Quick Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${filteredCards.size} cards",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    TextButton(
                        onClick = {
                            filteredCards.forEach { card ->
                                val isSelected = if (customCardIds.isEmpty()) {
                                    card.stance == stance || card.stance == "ALL"
                                } else {
                                    customCardIds.contains(card.id)
                                }
                                if (!isSelected) {
                                    onToggleCard(card.id)
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("+ Select Filtered", color = OsrsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // SCROLLABLE CARDS LIST (LazyColumn for 60fps performance)
                if (filteredCards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No cards found matching current skill filter or search query.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredCards, key = { it.id }) { card ->
                            val isSelected = if (customCardIds.isEmpty()) {
                                card.stance == stance || card.stance == "ALL"
                            } else {
                                customCardIds.contains(card.id)
                            }
                            val playerLvl = OsrsXpCalculator.getLevelForXp(skillXpMap[card.skill] ?: 0L)
                            val isUnlocked = playerLvl >= card.reqLevel

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isSelected -> Color(0xFF1E3A20)
                                        !isUnlocked -> Color(0xFF1A1A1A)
                                        else -> Color(0xFF263238)
                                    }
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isSelected -> OsrsGold
                                        !isUnlocked -> Color.DarkGray
                                        else -> Color.Gray.copy(alpha = 0.5f)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onToggleCard(card.id) },
                                        onLongClick = { enlargedCard = card }
                                    )
                                    .testTag("deck_card_item_${card.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(card.iconEmoji, fontSize = 22.sp)

                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    card.title,
                                                    color = if (isUnlocked) Color.White else Color.Gray,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (card.energyCost == 0) Color(0xFF2E7D32) else Color(0xFFD84315)
                                                ) {
                                                    Text(
                                                        "⚡${card.energyCost}",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }

                                                if (card.stance != "ALL") {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFF37474F)
                                                    ) {
                                                        Text(
                                                            card.stance,
                                                            color = Color.LightGray,
                                                            fontSize = 8.sp,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    "${card.skill.iconSymbol} ${card.skill.displayName}",
                                                    color = card.skill.accentColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                if (isUnlocked) {
                                                    Text(
                                                        "✓ Lv.${card.reqLevel}",
                                                        color = Color(0xFF81C784),
                                                        fontSize = 9.sp
                                                    )
                                                } else {
                                                    Text(
                                                        "🔒 Req Lv.${card.reqLevel}",
                                                        color = Color(0xFFE57373),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (card.baseDamage > 0) {
                                                    Text("💥 ${card.baseDamage}", color = Color(0xFFFFB74D), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                                if (card.baseShield > 0) {
                                                    Text("🛡️ ${card.baseShield}", color = Color(0xFF64B5F6), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                                if (card.baseHeal > 0) {
                                                    Text("💚 ${card.baseHeal}", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                                if (card.nextAttackBuff > 0) {
                                                    Text("⚡ +${card.nextAttackBuff}", color = Color(0xFFBA68C8), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }

                                            Text(
                                                card.description,
                                                color = Color.LightGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { onToggleCard(card.id) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = OsrsGold,
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Save / Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onOpenFullDeckBuilder != null) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenFullDeckBuilder()
                            },
                            border = BorderStroke(1.dp, OsrsGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OsrsGold),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text("🎴 Deck Builder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("deck_manager_save_button")
                    ) {
                        Text(
                            "✓ Done ($selectedCardsCount)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    enlargedCard?.let { card ->
        val playerLvl = OsrsXpCalculator.getLevelForXp(skillXpMap[card.skill] ?: 0L)
        val isUnlocked = playerLvl >= card.reqLevel
        EnlargedCardDetailDialog(
            card = card,
            isPlayable = isUnlocked,
            onDismiss = { enlargedCard = null }
        )
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    accentColor: Color = OsrsGold,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0xFF1E262C),
        border = BorderStroke(
            1.dp,
            if (isSelected) accentColor else Color.Gray.copy(alpha = 0.4f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(
                label,
                color = if (isSelected) Color.White else Color.LightGray,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Surface(
                shape = CircleShape,
                color = if (isSelected) accentColor else Color.DarkGray
            ) {
                Text(
                    "$count",
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
