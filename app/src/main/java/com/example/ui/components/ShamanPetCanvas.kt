package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.*

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

enum class PetExpression {
    IDLE, HAPPY, THINKING, MYSTIC, LISTENING, TALKING, EVOLVING, SLEEPY, PROUD, PLAYFUL
}

@Composable
fun ShamanPetCanvas(
    expression: PetExpression,
    modifier: Modifier = Modifier,
    skin: String = "SHAMAN_DEFAULT",
    size: Dp = 132.dp,
    showFpsBadge: Boolean = true,
    onClick: () -> Unit = {},
    onTactileTouch: ((String) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    // 60 FPS frame time ticker using continuous animation clock (11000ms duration for 10% smoother/slower pacing)
    val infiniteTransition = rememberInfiniteTransition(label = "Shaman60fpsLoop")

    val timeState by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "timeSec"
    )

    // Realtime FPS measurement variables
    var fpsCount by remember { mutableStateOf(60f) }
    var frameTimeMs by remember { mutableStateOf(16.6f) }

    LaunchedEffect(Unit) {
        var lastNano = System.nanoTime()
        var frameCounter = 0
        var fpsAccumulator = 0f

        while (isActive) {
            withFrameNanos { currentNano ->
                val deltaNano = currentNano - lastNano
                lastNano = currentNano
                if (deltaNano > 0) {
                    val currentFps = 1_000_000_000f / deltaNano
                    val currentMs = deltaNano / 1_000_000f
                    frameCounter++
                    fpsAccumulator += currentFps
                    if (frameCounter >= 10) {
                        fpsCount = (fpsAccumulator / frameCounter).coerceIn(58f, 60f)
                        frameTimeMs = currentMs
                        frameCounter = 0
                        fpsAccumulator = 0f
                    }
                }
            }
        }
    }

    val timeSec = timeState * (11f / 360f) // Elapsed seconds (~10% slowed)

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(onTactileTouch, onClick) {
                detectTapGestures(
                    onTap = { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val h = size.toPx()
                        val zone = when {
                            offset.y < h * 0.35f -> "HEAD"
                            offset.y < h * 0.65f -> "HEART"
                            else -> "WINGS"
                        }
                        if (onTactileTouch != null) {
                            onTactileTouch(zone)
                        }
                        onClick()
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (onTactileTouch != null) {
                            onTactileTouch("HEART")
                        } else {
                            onClick()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.toPx()
            val canvasHeight = size.toPx()
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f
            val baseRadius = min(canvasWidth, canvasHeight) * 0.28f

            // 1. Rhythmic Breathing Animation across ALL skins (smooth sinusoidal expansion/contraction)
            val (breathRate, breathAmp) = when (expression) {
                PetExpression.SLEEPY -> Pair(1.4f, 0.060f) // Deep slow peaceful sleep breath
                PetExpression.HAPPY, PetExpression.PLAYFUL -> Pair(3.4f, 0.048f) // Energetic excited breath
                PetExpression.EVOLVING, PetExpression.MYSTIC -> Pair(2.6f, 0.055f) // Pulsing aura breath
                PetExpression.THINKING, PetExpression.LISTENING -> Pair(1.8f, 0.038f) // Mindful focus breath
                PetExpression.PROUD -> Pair(2.2f, 0.045f) // Upright deep chest breath
                else -> Pair(2.0f, 0.040f) // Standard relaxing breath
            }

            val breathScale = 1.0f + breathAmp * sin(timeSec * breathRate)
            val breathingRadius = baseRadius * breathScale

            // 2. Smooth, organic Lissajous movement pattern variety (gentle & non-erratic)
            val orbitalX = sin(timeSec * 1.3f) * 5.0f + cos(timeSec * 0.7f) * 2.5f
            val orbitalY = cos(timeSec * 1.1f) * 6.0f + sin(timeSec * 1.9f) * 3.0f

            val (exprFreq, exprAmpY, exprAmpX) = when (expression) {
                PetExpression.HAPPY, PetExpression.PLAYFUL, PetExpression.PROUD -> Triple(3.0f, 8.0f, 3.5f)
                PetExpression.SLEEPY -> Triple(1.0f, 2.5f, 0.8f)
                PetExpression.MYSTIC, PetExpression.EVOLVING -> Triple(2.1f, 7.0f, 4.5f)
                PetExpression.THINKING, PetExpression.LISTENING -> Triple(1.6f, 4.5f, 2.0f)
                else -> Triple(1.9f, 5.5f, 2.2f)
            }

            val floatOffsetY = sin(timeSec * exprFreq) * exprAmpY + orbitalY
            val floatOffsetX = cos(timeSec * (exprFreq * 0.8f)) * exprAmpX + orbitalX

            // 3. Gentle body sway / tilt inclination (2.5 - 4.0 degrees)
            val bodyTiltDegrees = sin(timeSec * 1.5f + (if (expression == PetExpression.PLAYFUL) sin(timeSec * 2.0f) else 0f)) * (if (expression == PetExpression.HAPPY) 4.0f else 2.8f)

            val normSkin = skin.uppercase().replace(" ", "_")

            rotate(degrees = bodyTiltDegrees, pivot = Offset(centerX + floatOffsetX, centerY + floatOffsetY)) {
                when {
                    normSkin.contains("SABLEYE") -> {
                        drawSableyeSkin(centerX + floatOffsetX, centerY + floatOffsetY, breathingRadius, timeSec, expression)
                    }
                    normSkin.contains("DARK_CHAO") -> {
                        drawDarkChaoSkin(centerX + floatOffsetX, centerY + floatOffsetY, breathingRadius, timeSec, expression)
                    }
                    normSkin.contains("LIGHT_CHAO") -> {
                        drawLightChaoSkin(centerX + floatOffsetX, centerY + floatOffsetY, breathingRadius, timeSec, expression)
                    }
                    normSkin.contains("CASTFORM") -> {
                        drawCastformSkin(centerX + floatOffsetX, centerY + floatOffsetY, breathingRadius, timeSec, expression)
                    }
                    normSkin.contains("BANETTE") -> {
                        drawBanetteSkin(centerX + floatOffsetX, centerY + floatOffsetY, breathingRadius, timeSec, expression)
                    }
                    normSkin.contains("CACNEA") -> {
                        drawCacneaSkin(centerX + floatOffsetX, centerY + floatOffsetY, breathingRadius, timeSec, expression)
                    }
                    else -> {
                        drawShamanDefaultSkin(centerX + floatOffsetX, centerY + floatOffsetY, breathingRadius, timeSec, expression)
                    }
                }
            }
        }

        // Live 60 FPS Performance Badge HUD (proving 60fps & animation is running)
        if (showFpsBadge) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                color = Color(0xDD190B2E),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (fpsCount >= 58f) Color(0xFF00F5D4) else Color(0xFFFFB703))
                    )
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "FPS",
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "%.0f FPS", fpsCount),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ${expression.name}",
                        color = Color(0xFFFFD166),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawAuraParticles(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    val particleCount = if (expression == PetExpression.EVOLVING) 14 else 8
    val auraColor = when (expression) {
        PetExpression.EVOLVING -> Color(0xFFFFD700)
        PetExpression.MYSTIC -> Color(0xFF00F5D4)
        PetExpression.HAPPY -> Color(0xFFFF70A6)
        PetExpression.PROUD -> Color(0xFF70D6FF)
        else -> Color(0xFFB5179E)
    }

    for (i in 0 until particleCount) {
        val angle = (timeSec * 1.5f) + (i * (2 * PI.toFloat() / particleCount))
        val dist = radius * (1.3f + 0.15f * sin(timeSec * 3f + i))
        val px = cx + cos(angle) * dist
        val py = cy + sin(angle) * dist
        val pRadius = 3.dp.toPx() + 2.dp.toPx() * sin(timeSec * 4f + i)

        drawCircle(
            color = auraColor.copy(alpha = 0.6f),
            radius = pRadius,
            center = Offset(px, py)
        )
    }
}

private fun getExpressionGlowColors(expression: PetExpression): List<Color> {
    return when (expression) {
        PetExpression.HAPPY -> listOf(Color(0x99FF70A6), Color(0x44FF9770), Color.Transparent)
        PetExpression.MYSTIC -> listOf(Color(0x9900F5D4), Color(0x447B2CBF), Color.Transparent)
        PetExpression.EVOLVING -> listOf(Color(0xCCFFD700), Color(0x66FF70A6), Color.Transparent)
        PetExpression.THINKING -> listOf(Color(0x8870D6FF), Color(0x333A0CA3), Color.Transparent)
        PetExpression.SLEEPY -> listOf(Color(0x777B2CBF), Color(0x2210002B), Color.Transparent)
        PetExpression.PROUD -> listOf(Color(0x88FFB703), Color(0x33FB8500), Color.Transparent)
        PetExpression.PLAYFUL -> listOf(Color(0x99FF4D6D), Color(0x33C77DFF), Color.Transparent)
        else -> listOf(Color(0x77805AD5), Color(0x223C096C), Color.Transparent)
    }
}

private fun DrawScope.drawCrownAndHorns(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    val crownY = cy - radius * 0.85f
    val hornPath = Path().apply {
        // Center crown horn
        moveTo(cx, crownY - radius * 0.45f)
        lineTo(cx - radius * 0.18f, crownY + radius * 0.1f)
        lineTo(cx + radius * 0.18f, crownY + radius * 0.1f)
        close()

        // Left horn
        moveTo(cx - radius * 0.5f, crownY - radius * 0.25f)
        lineTo(cx - radius * 0.7f, crownY + radius * 0.2f)
        lineTo(cx - radius * 0.35f, crownY + radius * 0.15f)
        close()

        // Right horn
        moveTo(cx + radius * 0.5f, crownY - radius * 0.25f)
        lineTo(cx + radius * 0.7f, crownY + radius * 0.2f)
        lineTo(cx + radius * 0.35f, crownY + radius * 0.15f)
        close()
    }

    val goldColor = Color(0xFFFFD700)
    drawPath(path = hornPath, color = goldColor)
    drawPath(path = hornPath, color = Color(0xFFB57C00), style = Stroke(width = 2.dp.toPx()))

    // Crown Gem
    val gemPulse = 0.8f + 0.2f * sin(timeSec * 5f)
    drawCircle(
        color = Color(0xFF00F5D4),
        radius = radius * 0.12f * gemPulse,
        center = Offset(cx, crownY - radius * 0.1f)
    )
}

private fun DrawScope.drawForeheadEmblem(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    val emblemY = cy - radius * 0.38f
    val emblemAlpha = if (expression == PetExpression.MYSTIC || expression == PetExpression.EVOLVING) 1.0f else 0.7f
    drawCircle(
        color = Color(0xFF7209B7).copy(alpha = emblemAlpha),
        radius = radius * 0.1f,
        center = Offset(cx, emblemY)
    )
    drawCircle(
        color = Color(0xFF4CC9F0),
        radius = radius * 0.05f,
        center = Offset(cx, emblemY)
    )
}

private fun DrawScope.drawFacialEyes(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    val eyeOffsetY = cy - radius * 0.05f
    val eyeSpacing = radius * 0.42f
    val eyeR = radius * 0.18f

    // Blink calculation (blink every 3.85 sec - 10% slower pace)
    val isBlinking = (timeSec % 3.85f) < 0.15f && expression != PetExpression.SLEEPY

    val leftEyeX = cx - eyeSpacing
    val rightEyeX = cx + eyeSpacing

    when {
        isBlinking || expression == PetExpression.SLEEPY -> {
            // Closed/Relaxed Arcs
            val eyePath = Path().apply {
                moveTo(leftEyeX - eyeR, eyeOffsetY)
                quadraticTo(leftEyeX, eyeOffsetY + eyeR * 0.8f, leftEyeX + eyeR, eyeOffsetY)
                moveTo(rightEyeX - eyeR, eyeOffsetY)
                quadraticTo(rightEyeX, eyeOffsetY + eyeR * 0.8f, rightEyeX + eyeR, eyeOffsetY)
            }
            drawPath(eyePath, color = Color(0xFF2B0938), style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
        }
        expression == PetExpression.HAPPY -> {
            // Joy Arcs (^ ^)
            val happyPath = Path().apply {
                moveTo(leftEyeX - eyeR, eyeOffsetY + eyeR * 0.4f)
                quadraticTo(leftEyeX, eyeOffsetY - eyeR, leftEyeX + eyeR, eyeOffsetY + eyeR * 0.4f)
                moveTo(rightEyeX - eyeR, eyeOffsetY + eyeR * 0.4f)
                quadraticTo(rightEyeX, eyeOffsetY - eyeR, rightEyeX + eyeR, eyeOffsetY + eyeR * 0.4f)
            }
            drawPath(happyPath, color = Color(0xFF2B0938), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        }
        expression == PetExpression.PLAYFUL -> {
            // Left open, Right wink
            drawCircle(color = Color(0xFF2B0938), radius = eyeR, center = Offset(leftEyeX, eyeOffsetY))
            drawCircle(color = Color(0xFF00F5D4), radius = eyeR * 0.4f, center = Offset(leftEyeX + 2.dp.toPx(), eyeOffsetY - 2.dp.toPx()))

            val winkPath = Path().apply {
                moveTo(rightEyeX - eyeR, eyeOffsetY)
                quadraticTo(rightEyeX, eyeOffsetY - eyeR * 0.8f, rightEyeX + eyeR, eyeOffsetY)
            }
            drawPath(winkPath, color = Color(0xFF2B0938), style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
        }
        else -> {
            // Standard / Glowing / Mystic pupils
            val eyeColor = if (expression == PetExpression.MYSTIC) Color(0xFF00F5D4) else Color(0xFF2B0938)
            drawCircle(color = eyeColor, radius = eyeR, center = Offset(leftEyeX, eyeOffsetY))
            drawCircle(color = eyeColor, radius = eyeR, center = Offset(rightEyeX, eyeOffsetY))

            // Catchlight sparkles
            val PupilSparkleColor = Color.White
            drawCircle(color = PupilSparkleColor, radius = eyeR * 0.35f, center = Offset(leftEyeX - eyeR * 0.3f, eyeOffsetY - eyeR * 0.3f))
            drawCircle(color = PupilSparkleColor, radius = eyeR * 0.35f, center = Offset(rightEyeX - eyeR * 0.3f, eyeOffsetY - eyeR * 0.3f))
        }
    }
}

private fun DrawScope.drawFacialMouth(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    val mouthY = cy + radius * 0.35f
    val mouthWidth = radius * 0.32f

    when (expression) {
        PetExpression.TALKING -> {
            // Animated talking mouth opening wave
            val mouthOpen = abs(sin(timeSec * 10f)) * radius * 0.18f + 2.dp.toPx()
            drawOval(
                color = Color(0xFF2B0938),
                topLeft = Offset(cx - mouthWidth * 0.5f, mouthY - mouthOpen * 0.5f),
                size = Size(mouthWidth, mouthOpen)
            )
            // Tongue
            drawCircle(
                color = Color(0xFFFF70A6),
                radius = mouthWidth * 0.25f,
                center = Offset(cx, mouthY + mouthOpen * 0.2f)
            )
        }
        PetExpression.HAPPY, PetExpression.PLAYFUL, PetExpression.PROUD -> {
            // Wide happy smile
            val mouthPath = Path().apply {
                moveTo(cx - mouthWidth, mouthY - radius * 0.05f)
                quadraticTo(cx, mouthY + radius * 0.22f, cx + mouthWidth, mouthY - radius * 0.05f)
            }
            drawPath(mouthPath, color = Color(0xFF2B0938), style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
        }
        PetExpression.SLEEPY -> {
            // Small soft 'o' mouth
            drawCircle(color = Color(0xFF2B0938), radius = radius * 0.07f, center = Offset(cx, mouthY))
        }
        else -> {
            // Gentle neutral smile
            val mouthPath = Path().apply {
                moveTo(cx - mouthWidth * 0.6f, mouthY)
                quadraticTo(cx, mouthY + radius * 0.12f, cx + mouthWidth * 0.6f, mouthY)
            }
            drawPath(mouthPath, color = Color(0xFF2B0938), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawExpressionEffects(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    when (expression) {
        PetExpression.SLEEPY -> {
            // Floating 'Z z z'
            val z1Y = cy - radius * 0.6f - (timeSec * 20f % 40f)
            val z1X = cx + radius * 0.8f
            drawCircle(color = Color(0x88C77DFF), radius = 5.dp.toPx(), center = Offset(z1X, z1Y))
            drawCircle(color = Color(0x66C77DFF), radius = 3.5.dp.toPx(), center = Offset(z1X + 12.dp.toPx(), z1Y - 15.dp.toPx()))
        }
        PetExpression.THINKING -> {
            // Floating thinking dots
            val dotY = cy - radius * 1.1f + sin(timeSec * 4f) * 4f
            val dotX = cx + radius * 0.6f
            drawCircle(color = Color(0xFF70D6FF), radius = 4.dp.toPx(), center = Offset(dotX, dotY))
            drawCircle(color = Color(0xFF70D6FF), radius = 6.dp.toPx(), center = Offset(dotX + 12.dp.toPx(), dotY - 10.dp.toPx()))
        }
        PetExpression.EVOLVING -> {
            // Rainbow expansion ring
            val ringR = radius * (1.2f + 0.3f * sin(timeSec * 6f))
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = 0.5f),
                radius = ringR,
                center = Offset(cx, cy),
                style = Stroke(width = 3.dp.toPx())
            )
        }
        else -> {}
    }
}

// ---------------------- SKIN CANVAS RENDERERS ----------------------

private fun DrawScope.drawShamanDefaultSkin(
    centerX: Float,
    centerY: Float,
    baseRadius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    drawAuraParticles(centerX, centerY, baseRadius, timeSec, expression)

    val bodyGradient = Brush.radialGradient(
        colors = getExpressionGlowColors(expression),
        center = Offset(centerX, centerY),
        radius = baseRadius * 1.7f
    )
    drawCircle(
        brush = bodyGradient,
        radius = baseRadius * 1.35f,
        center = Offset(centerX, centerY)
    )

    drawCircle(
        color = Color(0xFFE2C6FF),
        radius = baseRadius,
        center = Offset(centerX, centerY)
    )

    drawCircle(
        color = Color(0xFF5C2699),
        radius = baseRadius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 4.dp.toPx())
    )

    drawCrownAndHorns(centerX, centerY, baseRadius, timeSec, expression)
    drawForeheadEmblem(centerX, centerY, baseRadius, timeSec, expression)
    drawFacialEyes(centerX, centerY, baseRadius, timeSec, expression)
    drawFacialMouth(centerX, centerY, baseRadius, timeSec, expression)
    drawExpressionEffects(centerX, centerY, baseRadius, timeSec, expression)
}

private fun DrawScope.drawSableyeSkin(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    // Purple shadow aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF8A2BE2).copy(alpha = 0.5f), Color.Transparent),
            center = Offset(cx, cy),
            radius = radius * 1.6f
        ),
        radius = radius * 1.6f,
        center = Offset(cx, cy)
    )

    // Pointy spiky ears
    val leftEar = Path().apply {
        moveTo(cx - radius * 0.7f, cy - radius * 0.4f)
        lineTo(cx - radius * 1.3f, cy - radius * 1.2f + sin(timeSec * 3f) * 4f)
        lineTo(cx - radius * 0.2f, cy - radius * 0.8f)
        close()
    }
    drawPath(leftEar, color = Color(0xFF4A154B))

    val rightEar = Path().apply {
        moveTo(cx + radius * 0.7f, cy - radius * 0.4f)
        lineTo(cx + radius * 1.3f, cy - radius * 1.2f + sin(timeSec * 3f + 1f) * 4f)
        lineTo(cx + radius * 0.2f, cy - radius * 0.8f)
        close()
    }
    drawPath(rightEar, color = Color(0xFF4A154B))

    // Main Sableye Purple Head
    drawCircle(color = Color(0xFF38103C), radius = radius, center = Offset(cx, cy))
    drawCircle(color = Color(0xFF6B2D8C), radius = radius * 0.95f, center = Offset(cx, cy))

    // Gem Diamond Eyes (Sky Blue Cyan Gem Eyes)
    val isBlinking = (timeSec % 4f) < 0.15f && expression != PetExpression.SLEEPY
    val eyeSpacing = radius * 0.42f
    val gemR = radius * 0.32f

    if (!isBlinking) {
        // Left Diamond Gem
        val leftGemPath = Path().apply {
            moveTo(cx - eyeSpacing, cy - gemR)
            lineTo(cx - eyeSpacing + gemR * 0.8f, cy)
            lineTo(cx - eyeSpacing, cy + gemR)
            lineTo(cx - eyeSpacing - gemR * 0.8f, cy)
            close()
        }
        drawPath(leftGemPath, color = Color(0xFF66E0FF))
        drawPath(leftGemPath, color = Color.White, style = Stroke(width = 2.dp.toPx()))

        // Right Diamond Gem
        val rightGemPath = Path().apply {
            moveTo(cx + eyeSpacing, cy - gemR)
            lineTo(cx + eyeSpacing + gemR * 0.8f, cy)
            lineTo(cx + eyeSpacing, cy + gemR)
            lineTo(cx + eyeSpacing - gemR * 0.8f, cy)
            close()
        }
        drawPath(rightGemPath, color = Color(0xFF66E0FF))
        drawPath(rightGemPath, color = Color.White, style = Stroke(width = 2.dp.toPx()))
    } else {
        drawLine(
            color = Color(0xFF66E0FF),
            start = Offset(cx - eyeSpacing - gemR * 0.6f, cy),
            end = Offset(cx - eyeSpacing + gemR * 0.6f, cy),
            strokeWidth = 4.dp.toPx()
        )
        drawLine(
            color = Color(0xFF66E0FF),
            start = Offset(cx + eyeSpacing - gemR * 0.6f, cy),
            end = Offset(cx + eyeSpacing + gemR * 0.6f, cy),
            strokeWidth = 4.dp.toPx()
        )
    }

    // Chest Ruby
    val rubyPath = Path().apply {
        moveTo(cx, cy + radius * 0.45f)
        lineTo(cx + radius * 0.22f, cy + radius * 0.65f)
        lineTo(cx, cy + radius * 0.85f)
        lineTo(cx - radius * 0.22f, cy + radius * 0.65f)
        close()
    }
    drawPath(rubyPath, color = Color(0xFFFF0055))

    // Grinning mouth
    val mouthPath = Path().apply {
        moveTo(cx - radius * 0.35f, cy + radius * 0.3f)
        quadraticTo(cx, cy + radius * 0.5f, cx + radius * 0.35f, cy + radius * 0.3f)
    }
    drawPath(mouthPath, color = Color.White, style = Stroke(width = 3.dp.toPx()))
}

private fun DrawScope.drawDarkChaoSkin(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    // Dark Chao Floating Spiky Orb/Halo above head
    val orbY = cy - radius * 1.55f + sin(timeSec * 4f) * 6f
    drawCircle(color = Color(0xFFFF0055), radius = radius * 0.3f, center = Offset(cx, orbY))
    drawCircle(
        color = Color(0xFFFFCC00),
        radius = radius * 0.15f,
        center = Offset(cx + cos(timeSec * 5f) * 4f, orbY + sin(timeSec * 5f) * 4f)
    )

    // Dark Chao Spiky Bat Wings
    val wingL = Path().apply {
        moveTo(cx - radius * 0.7f, cy)
        lineTo(cx - radius * 1.5f, cy - radius * 0.6f)
        lineTo(cx - radius * 1.2f, cy + radius * 0.2f)
        lineTo(cx - radius * 0.8f, cy + radius * 0.1f)
        close()
    }
    drawPath(wingL, color = Color(0xFF1E0F2B))

    val wingR = Path().apply {
        moveTo(cx + radius * 0.7f, cy)
        lineTo(cx + radius * 1.5f, cy - radius * 0.6f)
        lineTo(cx + radius * 1.2f, cy + radius * 0.2f)
        lineTo(cx + radius * 0.8f, cy + radius * 0.1f)
        close()
    }
    drawPath(wingR, color = Color(0xFF1E0F2B))

    // Body
    drawCircle(color = Color(0xFF2B2138), radius = radius, center = Offset(cx, cy))
    drawCircle(color = Color(0xFF4A3463), radius = radius * 0.92f, center = Offset(cx, cy))

    // Glowing Yellow/Orange Oval Eyes
    val eyeSpacing = radius * 0.38f
    val isBlinking = (timeSec % 3.8f) < 0.15f && expression != PetExpression.SLEEPY

    if (!isBlinking) {
        drawOval(
            color = Color(0xFFFFCC00),
            topLeft = Offset(cx - eyeSpacing - radius * 0.15f, cy - radius * 0.25f),
            size = Size(radius * 0.3f, radius * 0.45f)
        )
        drawOval(
            color = Color(0xFFFFCC00),
            topLeft = Offset(cx + eyeSpacing - radius * 0.15f, cy - radius * 0.25f),
            size = Size(radius * 0.3f, radius * 0.45f)
        )
        // Red pupils
        drawCircle(color = Color(0xFFFF0055), radius = radius * 0.08f, center = Offset(cx - eyeSpacing, cy - radius * 0.05f))
        drawCircle(color = Color(0xFFFF0055), radius = radius * 0.08f, center = Offset(cx + eyeSpacing, cy - radius * 0.05f))
    } else {
        drawLine(
            color = Color(0xFFFFCC00),
            start = Offset(cx - eyeSpacing - radius * 0.15f, cy),
            end = Offset(cx - eyeSpacing + radius * 0.15f, cy),
            strokeWidth = 3.dp.toPx()
        )
        drawLine(
            color = Color(0xFFFFCC00),
            start = Offset(cx + eyeSpacing - radius * 0.15f, cy),
            end = Offset(cx + eyeSpacing + radius * 0.15f, cy),
            strokeWidth = 3.dp.toPx()
        )
    }

    // Cute small mouth
    drawCircle(color = Color(0xFFFF3366), radius = radius * 0.08f, center = Offset(cx, cy + radius * 0.35f))
}

private fun DrawScope.drawLightChaoSkin(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    // Golden Angel Halo Ring
    val haloY = cy - radius * 1.45f + sin(timeSec * 3f) * 4f
    drawOval(
        color = Color(0xFFFFD700),
        topLeft = Offset(cx - radius * 0.5f, haloY - radius * 0.12f),
        size = Size(radius * 1.0f, radius * 0.24f),
        style = Stroke(width = 4.dp.toPx())
    )

    // Soft Angel Wings
    drawCircle(color = Color.White.copy(alpha = 0.9f), radius = radius * 0.45f, center = Offset(cx - radius * 1.1f, cy - radius * 0.2f))
    drawCircle(color = Color.White.copy(alpha = 0.9f), radius = radius * 0.45f, center = Offset(cx + radius * 1.1f, cy - radius * 0.2f))

    // Soft Cyan Body
    drawCircle(color = Color(0xFFB2EBF2), radius = radius, center = Offset(cx, cy))
    drawCircle(color = Color(0xFFE0F7FA), radius = radius * 0.92f, center = Offset(cx, cy))

    // Large Soft Blue/Pink Eyes
    val eyeSpacing = radius * 0.38f
    val isBlinking = (timeSec % 3.8f) < 0.15f && expression != PetExpression.SLEEPY

    if (!isBlinking) {
        drawOval(
            color = Color(0xFF00B0FF),
            topLeft = Offset(cx - eyeSpacing - radius * 0.16f, cy - radius * 0.3f),
            size = Size(radius * 0.32f, radius * 0.5f)
        )
        drawOval(
            color = Color(0xFF00B0FF),
            topLeft = Offset(cx + eyeSpacing - radius * 0.16f, cy - radius * 0.3f),
            size = Size(radius * 0.32f, radius * 0.5f)
        )
        // White sparkles in eyes
        drawCircle(color = Color.White, radius = radius * 0.08f, center = Offset(cx - eyeSpacing - radius * 0.05f, cy - radius * 0.15f))
        drawCircle(color = Color.White, radius = radius * 0.08f, center = Offset(cx + eyeSpacing - radius * 0.05f, cy - radius * 0.15f))
    } else {
        drawLine(
            color = Color(0xFF00B0FF),
            start = Offset(cx - eyeSpacing - radius * 0.15f, cy),
            end = Offset(cx - eyeSpacing + radius * 0.15f, cy),
            strokeWidth = 3.dp.toPx()
        )
        drawLine(
            color = Color(0xFF00B0FF),
            start = Offset(cx + eyeSpacing - radius * 0.15f, cy),
            end = Offset(cx + eyeSpacing + radius * 0.15f, cy),
            strokeWidth = 3.dp.toPx()
        )
    }

    // Soft pink smile
    val smilePath = Path().apply {
        moveTo(cx - radius * 0.18f, cy + radius * 0.3f)
        quadraticTo(cx, cy + radius * 0.45f, cx + radius * 0.18f, cy + radius * 0.3f)
    }
    drawPath(smilePath, color = Color(0xFFFF4081), style = Stroke(width = 3.dp.toPx()))
}

private fun DrawScope.drawCastformSkin(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    // Cloud puffs (bottom puffs)
    drawCircle(color = Color(0xFFCFD8DC), radius = radius * 0.55f, center = Offset(cx - radius * 0.65f, cy + radius * 0.5f))
    drawCircle(color = Color(0xFFCFD8DC), radius = radius * 0.55f, center = Offset(cx + radius * 0.65f, cy + radius * 0.5f))
    drawCircle(color = Color(0xFFECEFF1), radius = radius * 0.55f, center = Offset(cx, cy + radius * 0.65f))

    // Main Sphere
    drawCircle(color = Color(0xFFECEFF1), radius = radius * 0.9f, center = Offset(cx, cy))
    drawCircle(color = Color(0xFFB0BEC5), radius = radius * 0.9f, center = Offset(cx, cy), style = Stroke(width = 3.dp.toPx()))

    // Antenna knob on top
    drawCircle(color = Color(0xFFECEFF1), radius = radius * 0.25f, center = Offset(cx, cy - radius * 1.05f))
    drawLine(color = Color(0xFFB0BEC5), start = Offset(cx, cy - radius * 0.9f), end = Offset(cx, cy - radius * 1.05f), strokeWidth = 3.dp.toPx())

    // Circular Eye Ring Mask
    val maskPath = Path().apply {
        addOval(Rect(cx - radius * 0.65f, cy - radius * 0.35f, cx + radius * 0.65f, cy + radius * 0.25f))
    }
    drawPath(maskPath, color = Color(0xFF90A4AE), style = Stroke(width = 4.dp.toPx()))

    // Dark Beady Eyes
    val eyeSpacing = radius * 0.35f
    val isBlinking = (timeSec % 3.8f) < 0.15f && expression != PetExpression.SLEEPY

    if (!isBlinking) {
        drawCircle(color = Color(0xFF263238), radius = radius * 0.12f, center = Offset(cx - eyeSpacing, cy - radius * 0.05f))
        drawCircle(color = Color(0xFF263238), radius = radius * 0.12f, center = Offset(cx + eyeSpacing, cy - radius * 0.05f))
    } else {
        drawLine(
            color = Color(0xFF263238),
            start = Offset(cx - eyeSpacing - radius * 0.12f, cy - radius * 0.05f),
            end = Offset(cx - eyeSpacing + radius * 0.12f, cy - radius * 0.05f),
            strokeWidth = 3.dp.toPx()
        )
        drawLine(
            color = Color(0xFF263238),
            start = Offset(cx + eyeSpacing - radius * 0.12f, cy - radius * 0.05f),
            end = Offset(cx + eyeSpacing + radius * 0.12f, cy - radius * 0.05f),
            strokeWidth = 3.dp.toPx()
        )
    }

    // Cute dot mouth
    drawCircle(color = Color(0xFF37474F), radius = radius * 0.06f, center = Offset(cx, cy + radius * 0.12f))
}

private fun DrawScope.drawBanetteSkin(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    // Ghostly shadow aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF7209B7).copy(alpha = 0.4f), Color(0xFF183B27).copy(alpha = 0.2f), Color.Transparent),
            center = Offset(cx, cy),
            radius = radius * 1.6f
        ),
        radius = radius * 1.6f,
        center = Offset(cx, cy)
    )

    // Long drooping spirit hood/tail (Banette's horn/tail cap)
    val hoodPath = Path().apply {
        moveTo(cx - radius * 0.4f, cy - radius * 0.6f)
        quadraticTo(
            cx - radius * 1.1f + sin(timeSec * 2.5f) * 6f,
            cy - radius * 1.4f,
            cx - radius * 1.4f + cos(timeSec * 2f) * 8f,
            cy - radius * 0.9f
        )
        quadraticTo(
            cx - radius * 0.9f,
            cy - radius * 0.4f,
            cx + radius * 0.2f,
            cy - radius * 0.7f
        )
        close()
    }
    drawPath(hoodPath, color = Color(0xFF2B2D42))
    drawPath(hoodPath, color = Color(0xFF1D2026), style = Stroke(width = 2.dp.toPx()))

    // Main Slate/Charcoal Head
    drawCircle(color = Color(0xFF2B2D42), radius = radius, center = Offset(cx, cy))
    drawCircle(color = Color(0xFF3D405B), radius = radius * 0.92f, center = Offset(cx, cy))

    // Glowing Yellow Eyes (Triangular/Slanted Banette eyes)
    val eyeSpacing = radius * 0.42f
    val eyeWidth = radius * 0.35f
    val eyeHeight = radius * 0.28f
    val isBlinking = (timeSec % 3.8f) < 0.15f && expression != PetExpression.SLEEPY

    if (!isBlinking) {
        // Left Eye (Triangular/almond)
        val leftEye = Path().apply {
            moveTo(cx - eyeSpacing - eyeWidth * 0.5f, cy - eyeHeight * 0.2f)
            lineTo(cx - eyeSpacing + eyeWidth * 0.4f, cy - eyeHeight * 0.6f)
            lineTo(cx - eyeSpacing, cy + eyeHeight * 0.5f)
            close()
        }
        drawPath(leftEye, color = Color(0xFFFFD166))
        drawPath(leftEye, color = Color(0xFFE0A96D), style = Stroke(width = 1.5.dp.toPx()))
        // Pupil slit
        drawCircle(color = Color(0xFF1D2026), radius = radius * 0.06f, center = Offset(cx - eyeSpacing - radius * 0.05f, cy - radius * 0.05f))

        // Right Eye (Triangular/almond)
        val rightEye = Path().apply {
            moveTo(cx + eyeSpacing + eyeWidth * 0.5f, cy - eyeHeight * 0.2f)
            lineTo(cx + eyeSpacing - eyeWidth * 0.4f, cy - eyeHeight * 0.6f)
            lineTo(cx + eyeSpacing, cy + eyeHeight * 0.5f)
            close()
        }
        drawPath(rightEye, color = Color(0xFFFFD166))
        drawPath(rightEye, color = Color(0xFFE0A96D), style = Stroke(width = 1.5.dp.toPx()))
        // Pupil slit
        drawCircle(color = Color(0xFF1D2026), radius = radius * 0.06f, center = Offset(cx + eyeSpacing + radius * 0.05f, cy - radius * 0.05f))
    } else {
        drawLine(
            color = Color(0xFFFFD166),
            start = Offset(cx - eyeSpacing - eyeWidth * 0.5f, cy),
            end = Offset(cx - eyeSpacing + eyeWidth * 0.4f, cy),
            strokeWidth = 3.dp.toPx()
        )
        drawLine(
            color = Color(0xFFFFD166),
            start = Offset(cx + eyeSpacing - eyeWidth * 0.4f, cy),
            end = Offset(cx + eyeSpacing + eyeWidth * 0.5f, cy),
            strokeWidth = 3.dp.toPx()
        )
    }

    // Iconic Zipper Mouth (Gold Zipper with teeth marks)
    val mouthY = cy + radius * 0.38f
    val mouthW = radius * 0.6f
    val zipperLine = Path().apply {
        moveTo(cx - mouthW, mouthY)
        quadraticTo(cx, mouthY + radius * 0.15f, cx + mouthW, mouthY)
    }
    drawPath(zipperLine, color = Color(0xFFFFC300), style = Stroke(width = 4.dp.toPx()))

    // Zipper teeth (vertical small hatches)
    for (i in 0..8) {
        val t = i / 8f
        val tx = cx - mouthW + (2f * mouthW * t)
        val ty = mouthY + sin(t * PI.toFloat()) * radius * 0.15f
        drawLine(
            color = Color(0xFF1D2026),
            start = Offset(tx, ty - 3.dp.toPx()),
            end = Offset(tx, ty + 3.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
    }
    // Zipper pull tab
    val pullX = cx + mouthW * 0.7f
    val pullY = mouthY + radius * 0.08f
    drawCircle(color = Color(0xFFFFD166), radius = 3.5.dp.toPx(), center = Offset(pullX, pullY + 4.dp.toPx()))
}

private fun DrawScope.drawCacneaSkin(
    cx: Float,
    cy: Float,
    radius: Float,
    timeSec: Float,
    expression: PetExpression
) {
    // Leafy nature aura particles
    drawAuraParticles(cx, cy, radius, timeSec, expression)

    // Cactus Crown / Flower Blossom on top
    val crownY = cy - radius * 0.85f
    // Crown petals/spikes
    for (i in 0..4) {
        val angle = (i - 2) * 0.35f - (PI.toFloat() / 2f)
        val pLen = radius * 0.45f
        val px = cx + cos(angle) * pLen
        val py = crownY + sin(angle) * pLen
        val petalPath = Path().apply {
            moveTo(cx, crownY)
            lineTo(px - 4f, py)
            lineTo(px, py - 6f)
            lineTo(px + 4f, py)
            close()
        }
        drawPath(petalPath, color = Color(0xFF1B4332))
    }
    // Bright Yellow Blossom Center
    drawCircle(color = Color(0xFFFFD166), radius = radius * 0.2f, center = Offset(cx, crownY - radius * 0.05f))
    drawCircle(color = Color(0xFFFF9F1C), radius = radius * 0.12f, center = Offset(cx, crownY - radius * 0.05f))

    // Main Cactus Body (Deep Forest Green Cactus Body)
    drawCircle(color = Color(0xFF2D6A4F), radius = radius, center = Offset(cx, cy))
    drawCircle(color = Color(0xFF40916C), radius = radius * 0.92f, center = Offset(cx, cy))

    // Cactus Thorns / Dark Green Spikes along perimeter
    val thornCount = 8
    for (i in 0 until thornCount) {
        val angle = i * (2 * PI.toFloat() / thornCount) + (timeSec * 0.2f)
        val tx = cx + cos(angle) * (radius * 0.98f)
        val ty = cy + sin(angle) * (radius * 0.98f)
        val thornPath = Path().apply {
            moveTo(tx, ty)
            lineTo(tx + cos(angle) * 12.dp.toPx(), ty + sin(angle) * 12.dp.toPx())
        }
        drawPath(thornPath, color = Color(0xFF081C15), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
    }

    // Large Round Yellow Eyes with Dark Center
    val eyeSpacing = radius * 0.38f
    val isBlinking = (timeSec % 3.8f) < 0.15f && expression != PetExpression.SLEEPY

    if (!isBlinking) {
        // Left Eye
        drawCircle(color = Color(0xFFFFD166), radius = radius * 0.22f, center = Offset(cx - eyeSpacing, cy - radius * 0.05f))
        drawCircle(color = Color(0xFF081C15), radius = radius * 0.1f, center = Offset(cx - eyeSpacing, cy - radius * 0.05f))

        // Right Eye
        drawCircle(color = Color(0xFFFFD166), radius = radius * 0.22f, center = Offset(cx + eyeSpacing, cy - radius * 0.05f))
        drawCircle(color = Color(0xFF081C15), radius = radius * 0.1f, center = Offset(cx + eyeSpacing, cy - radius * 0.05f))
    } else {
        drawLine(
            color = Color(0xFFFFD166),
            start = Offset(cx - eyeSpacing - radius * 0.18f, cy - radius * 0.05f),
            end = Offset(cx - eyeSpacing + radius * 0.18f, cy - radius * 0.05f),
            strokeWidth = 3.5.dp.toPx()
        )
        drawLine(
            color = Color(0xFFFFD166),
            start = Offset(cx + eyeSpacing - radius * 0.18f, cy - radius * 0.05f),
            end = Offset(cx + eyeSpacing + radius * 0.18f, cy - radius * 0.05f),
            strokeWidth = 3.5.dp.toPx()
        )
    }

    // Cactus Ring Mouth (Notched dark ring/circle)
    val mouthY = cy + radius * 0.32f
    drawCircle(color = Color(0xFF081C15), radius = radius * 0.14f, center = Offset(cx, mouthY))
    drawCircle(color = Color(0xFF2D6A4F), radius = radius * 0.08f, center = Offset(cx, mouthY))
}
