package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class FireParticle(
    val id: String = UUID.randomUUID().toString(),
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var alpha: Float = 1.0f,
    var lifetimeMs: Long = 600L,
    var ageMs: Long = 0L,
    val color: Color,
    val animType: String = "Flame Blast"
)

data class FloatingXpText(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val text: String,
    var alpha: Float = 1.0f,
    var offsetY: Float = 0f
)

@Composable
fun TapFireOverlay(
    firemakingLevel: Int,
    fmColor1: Int,
    fmColor2: Int,
    navColor1: Int = 0xFF0096C7.toInt(),
    navColor2: Int = 0xFF00E5FF.toInt(),
    selectedFmAnimations: List<String>,
    onTapFire: ((x: Float, y: Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val particles = remember { mutableStateListOf<FireParticle>() }
    val floatingTexts = remember { mutableStateListOf<FloatingXpText>() }

    // Fire scale based on Firemaking level (0.5x at lvl 1 -> 2.0x at lvl 99)
    val fireScale = remember(firemakingLevel) {
        0.5f + (firemakingLevel.coerceIn(1, 99) / 99f) * 1.5f
    }

    // Determine primary/secondary colors based on navigation selection
    val primaryColor = remember(navColor1) { Color(navColor1) }
    val secondaryColor = remember(navColor2) { Color(navColor2) }

    // High-performance 60fps frame loop for particle physics and decay
    LaunchedEffect(Unit) {
        var lastTime = System.currentTimeMillis()
        while (isActive) {
            val now = System.currentTimeMillis()
            val dt = (now - lastTime).coerceIn(1L, 33L)
            lastTime = now

            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.ageMs += dt
                if (p.ageMs >= p.lifetimeMs) {
                    iterator.remove()
                } else {
                    val progress = p.ageMs.toFloat() / p.lifetimeMs.toFloat()
                    p.alpha = (1.0f - progress).coerceIn(0f, 1f)
                    p.x += p.vx * (dt / 16f)
                    p.y += p.vy * (dt / 16f)
                    p.vy -= 0.15f * (dt / 16f) // Upward buoyancy
                    p.radius *= 0.98f
                }
            }

            val textIterator = floatingTexts.iterator()
            while (textIterator.hasNext()) {
                val txt = textIterator.next()
                txt.offsetY -= 1.5f
                txt.alpha -= 0.02f
                if (txt.alpha <= 0f) {
                    textIterator.remove()
                }
            }

            kotlinx.coroutines.delay(16L) // ~60 FPS
        }
    }

    fun spawnFireBurst(tapX: Float, tapY: Float) {
        val particleCount = (18 * fireScale).toInt().coerceIn(10, 45)
        val activeAnims = if (selectedFmAnimations.isNotEmpty()) {
            selectedFmAnimations.take(2)
        } else {
            listOf("Flame Blast")
        }

        activeAnims.forEach { anim ->
            for (i in 0 until particleCount) {
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                val speed = (Random.nextFloat() * 8f + 2f) * fireScale
                val chosenColor = if (Random.nextBoolean()) primaryColor else secondaryColor
                val baseRadius = (Random.nextFloat() * 12f + 6f) * fireScale

                val vx = cos(angle) * speed
                val vy = sin(angle) * speed - (Random.nextFloat() * 4f)

                particles.add(
                    FireParticle(
                        x = tapX,
                        y = tapY,
                        vx = vx,
                        vy = vy,
                        radius = baseRadius,
                        color = chosenColor,
                        lifetimeMs = (400L + Random.nextInt(400) * fireScale).toLong(),
                        animType = anim
                    )
                )
            }
        }

        onTapFire?.invoke(tapX, tapY)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(firemakingLevel, navColor1, navColor2, selectedFmAnimations) {
                detectTapGestures { offset ->
                    spawnFireBurst(offset.x, offset.y)
                }
            }
            .testTag("tap_fire_overlay")
    ) {
        content()

        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val drawColor = p.color.copy(alpha = p.alpha)
                when (p.animType) {
                    "Phoenix Rise" -> {
                        drawCircle(color = drawColor, radius = p.radius, center = Offset(p.x, p.y))
                        drawLine(
                            color = drawColor,
                            start = Offset(p.x - p.radius * 2, p.y + p.radius),
                            end = Offset(p.x + p.radius * 2, p.y - p.radius),
                            strokeWidth = 3f * p.alpha
                        )
                    }
                    "Cosmic Ember Ring" -> {
                        drawCircle(
                            color = drawColor,
                            radius = p.radius * 1.5f,
                            center = Offset(p.x, p.y),
                            style = Stroke(width = 3f)
                        )
                    }
                    "Inferno Vortex" -> {
                        val vortexOffset = sin(p.ageMs / 50f) * 15f
                        drawCircle(
                            color = drawColor,
                            radius = p.radius,
                            center = Offset(p.x + vortexOffset, p.y)
                        )
                    }
                    "Dragon Breath Ring" -> {
                        val path = Path().apply {
                            moveTo(p.x, p.y - p.radius)
                            lineTo(p.x + p.radius, p.y)
                            lineTo(p.x, p.y + p.radius)
                            lineTo(p.x - p.radius, p.y)
                            close()
                        }
                        drawPath(path, color = drawColor)
                    }
                    "Supernova Nova" -> {
                        // Expanding star points
                        drawCircle(color = drawColor, radius = p.radius * 0.8f, center = Offset(p.x, p.y))
                        drawLine(color = drawColor, start = Offset(p.x - p.radius * 2, p.y), end = Offset(p.x + p.radius * 2, p.y), strokeWidth = 2f)
                        drawLine(color = drawColor, start = Offset(p.x, p.y - p.radius * 2), end = Offset(p.x, p.y + p.radius * 2), strokeWidth = 2f)
                    }
                    "Meteor Shower" -> {
                        // Trailing meteor streak
                        drawCircle(color = drawColor, radius = p.radius, center = Offset(p.x, p.y))
                        drawLine(color = drawColor.copy(alpha = drawColor.alpha * 0.5f), start = Offset(p.x, p.y), end = Offset(p.x - p.vx * 3, p.y - p.vy * 3), strokeWidth = p.radius * 0.8f)
                    }
                    "Lightning Spark" -> {
                        // Zig zag bolt fragment
                        val path = Path().apply {
                            moveTo(p.x, p.y)
                            lineTo(p.x + 5f, p.y - 8f)
                            lineTo(p.x - 3f, p.y - 14f)
                        }
                        drawPath(path, color = drawColor, style = Stroke(width = 3f))
                    }
                    "Tidal Wave Fire" -> {
                        // Curved wave crest
                        val waveOffset = sin((p.ageMs + p.x) / 30f) * 10f
                        drawCircle(color = drawColor, radius = p.radius, center = Offset(p.x, p.y + waveOffset))
                    }
                    "Astral Comet" -> {
                        // Glowing core + outer ring
                        drawCircle(color = drawColor, radius = p.radius, center = Offset(p.x, p.y))
                        drawCircle(color = drawColor, radius = p.radius * 2f, center = Offset(p.x, p.y), style = Stroke(width = 1.5f))
                    }
                    "Master Divine Flare" -> {
                        // Golden crown flare
                        drawCircle(color = drawColor, radius = p.radius * 1.2f, center = Offset(p.x, p.y))
                        drawCircle(color = Color(0xFFFFD700).copy(alpha = p.alpha), radius = p.radius * 2.2f, center = Offset(p.x, p.y), style = Stroke(width = 2.5f))
                    }
                    else -> { // Flame Blast
                        drawCircle(
                            color = drawColor,
                            radius = p.radius,
                            center = Offset(p.x, p.y)
                        )
                    }
                }
            }
        }

        // Floating XP texts
        floatingTexts.forEach { txt ->
            Text(
                text = txt.text,
                color = Color(0xFFFFD700).copy(alpha = txt.alpha.coerceIn(0f, 1f)),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .offset(
                        x = (txt.x / 2.7f).dp, // Approximate px to dp conversion
                        y = ((txt.y + txt.offsetY) / 2.7f).dp
                    )
            )
        }
    }
}
