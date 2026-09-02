package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

private data class AmbientParticle(
    val initialXRatio: Float,
    val initialYRatio: Float,
    val speedY: Float,
    val swayAmplitude: Float,
    val swaySpeed: Float,
    val radiusDp: Float,
    val alpha: Float,
    val phase: Float,
    val color: Color
)

private data class DriftingLeaf(
    val initialXRatio: Float,
    val initialYRatio: Float,
    val speedY: Float,
    val swayAmplitude: Float,
    val swaySpeed: Float,
    val rotationSpeed: Float,
    val sizeDp: Float,
    val color: Color,
    val phase: Float
)

@Composable
fun NatureBackgroundParticles(
    selectedNavAnimations: List<String> = emptyList(),
    navColor1: Int = 0xFF0096C7.toInt(),
    navColor2: Int = 0xFF00E5FF.toInt(),
    activeNavIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nature_bg_particles_60fps")

    // Continuous time loop for 60fps smooth physics
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_time"
    )

    // Pulsing atmosphere light ray opacity
    val rayPulse by infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue = 0.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ray_pulse"
    )

    // Ambient glow pulse
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    // Tab Environment Classification
    val envType = remember(activeNavIndex) {
        when (activeNavIndex) {
            21, 13, 15, 9, 10 -> "JUNGLE_SWAMP"
            1, 14, 4, 16, 7, 6 -> "ANCIENT_STONE"
            12, 11, 20, 22 -> "MYSTIC_ASTRAL"
            19, 8, 18, 24, 23 -> "SEAFARING_EXPEDITION"
            else -> "COZY_HOMESTEAD"
        }
    }

    // Pre-allocated particles
    val particles = remember(envType) {
        val count = 28
        val rand = java.util.Random(1337L + envType.hashCode())
        List(count) {
            val isColorAlt = rand.nextBoolean()
            val particleColor = when (envType) {
                "JUNGLE_SWAMP" -> if (isColorAlt) Color(0xFF00FF9D) else Color(0xFF81C784)
                "ANCIENT_STONE" -> if (isColorAlt) Color(0xFFFFB703) else Color(0xFFFF7043)
                "MYSTIC_ASTRAL" -> if (isColorAlt) Color(0xFFE0AAFF) else Color(0xFF80FFE8)
                "SEAFARING_EXPEDITION" -> if (isColorAlt) Color(0xFFFFD166) else Color(0xFF48CAE4)
                else -> if (isColorAlt) Color(0xFFFFD54F) else Color(0xFFA5D6A7)
            }
            AmbientParticle(
                initialXRatio = rand.nextFloat(),
                initialYRatio = rand.nextFloat(),
                speedY = 0.008f + rand.nextFloat() * 0.016f,
                swayAmplitude = 12f + rand.nextFloat() * 22f,
                swaySpeed = 0.08f + rand.nextFloat() * 0.14f,
                radiusDp = 1.8f + rand.nextFloat() * 2.8f,
                alpha = 0.35f + rand.nextFloat() * 0.5f,
                phase = rand.nextFloat() * 6.28f,
                color = particleColor
            )
        }
    }

    // Pre-allocated falling leaves / motes
    val leafList = remember(envType) {
        val count = 8
        val rand = java.util.Random(4242L + envType.hashCode())
        List(count) {
            val leafColor = when (envType) {
                "JUNGLE_SWAMP" -> if (rand.nextBoolean()) Color(0xFF2D6A4F) else Color(0xFF1B4332)
                "ANCIENT_STONE" -> if (rand.nextBoolean()) Color(0xFF5A4532) else Color(0xFF382A1B)
                "MYSTIC_ASTRAL" -> if (rand.nextBoolean()) Color(0xFF5A189A) else Color(0xFF3C096C)
                "SEAFARING_EXPEDITION" -> if (rand.nextBoolean()) Color(0xFF0077B6) else Color(0xFF023E8A)
                else -> if (rand.nextBoolean()) Color(0xFF556B2F) else Color(0xFF6B8E23)
            }
            DriftingLeaf(
                initialXRatio = rand.nextFloat(),
                initialYRatio = rand.nextFloat(),
                speedY = 0.012f + rand.nextFloat() * 0.018f,
                swayAmplitude = 20f + rand.nextFloat() * 30f,
                swaySpeed = 0.06f + rand.nextFloat() * 0.12f,
                rotationSpeed = 0.4f + rand.nextFloat() * 0.8f,
                sizeDp = 5f + rand.nextFloat() * 4f,
                color = leafColor,
                phase = rand.nextFloat() * 6.28f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val density = this.density

        // 1. Base Environment Atmosphere Gradient
        val bgGradient = when (envType) {
            "JUNGLE_SWAMP" -> Brush.verticalGradient(
                listOf(Color(0xFF07140B), Color(0xFF0B1F13), Color(0xFF040A06))
            )
            "ANCIENT_STONE" -> Brush.verticalGradient(
                listOf(Color(0xFF140F0B), Color(0xFF1E1712), Color(0xFF0B0806))
            )
            "MYSTIC_ASTRAL" -> Brush.verticalGradient(
                listOf(Color(0xFF0C091A), Color(0xFF140E2A), Color(0xFF07050F))
            )
            "SEAFARING_EXPEDITION" -> Brush.verticalGradient(
                listOf(Color(0xFF07121C), Color(0xFF0C1F2E), Color(0xFF040A10))
            )
            else -> Brush.verticalGradient(
                listOf(Color(0xFF0D120B), Color(0xFF141C10), Color(0xFF070A06))
            )
        }
        drawRect(brush = bgGradient)

        // 2. Light Rays / Atmosphere Shimmer
        val rayColor1 = when (envType) {
            "JUNGLE_SWAMP" -> Color(0xFF00FF9D)
            "ANCIENT_STONE" -> Color(0xFFFFB703)
            "MYSTIC_ASTRAL" -> Color(0xFFC77DFF)
            "SEAFARING_EXPEDITION" -> Color(0xFF48CAE4)
            else -> Color(0xFFFFF176)
        }

        val rayBrush1 = Brush.linearGradient(
            colors = listOf(
                rayColor1.copy(alpha = rayPulse * 1.1f),
                rayColor1.copy(alpha = rayPulse * 0.3f),
                Color.Transparent
            ),
            start = Offset(0f, 0f),
            end = Offset(width * 0.65f, height * 0.5f)
        )
        val rayPath1 = Path().apply {
            moveTo(0f, 0f)
            lineTo(width * 0.45f, 0f)
            lineTo(width * 0.7f, height * 0.55f)
            lineTo(0f, height * 0.35f)
            close()
        }
        drawPath(path = rayPath1, brush = rayBrush1)

        val rayBrush2 = Brush.linearGradient(
            colors = listOf(
                rayColor1.copy(alpha = rayPulse * 0.8f),
                Color.Transparent
            ),
            start = Offset(width, 0f),
            end = Offset(width * 0.35f, height * 0.55f)
        )
        val rayPath2 = Path().apply {
            moveTo(width, 0f)
            lineTo(width * 0.55f, 0f)
            lineTo(width * 0.25f, height * 0.45f)
            lineTo(width, height * 0.35f)
            close()
        }
        drawPath(path = rayPath2, brush = rayBrush2)

        // Bottom ambient swamp mist / aura
        val mistBrush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                rayColor1.copy(alpha = 0.04f * auraPulse)
            ),
            startY = height * 0.6f,
            endY = height
        )
        drawRect(brush = mistBrush, topLeft = Offset(0f, height * 0.6f), size = Size(width, height * 0.4f))

        // 3. Render Floating Ambient Particles (Rising Upward)
        particles.forEach { p ->
            val totalYOffset = (animTime * p.speedY + p.initialYRatio) % 1.2f
            val currentY = height * (1.1f - totalYOffset)
            val swayX = sin(animTime * p.swaySpeed + p.phase) * p.swayAmplitude * density
            val currentX = (p.initialXRatio * width + swayX) % width
            val radiusPx = p.radiusDp * density

            // Glow halo for atmospheric feel
            drawCircle(
                color = p.color.copy(alpha = p.alpha * 0.35f * auraPulse),
                radius = radiusPx * 2.2f,
                center = Offset(currentX, currentY)
            )
            // Core spark
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = radiusPx,
                center = Offset(currentX, currentY)
            )
        }

        // 4. Render Drifting Leaves / Motes
        leafList.forEach { leaf ->
            val totalYOffset = (animTime * leaf.speedY + leaf.initialYRatio) % 1.2f
            val currentY = height * (totalYOffset - 0.1f)
            val swayX = cos(animTime * leaf.swaySpeed + leaf.phase) * leaf.swayAmplitude * density
            val currentX = ((leaf.initialXRatio * width + swayX) % width + width) % width
            val leafSizePx = leaf.sizeDp * density
            val rotationAngle = (animTime * leaf.rotationSpeed + leaf.phase * 50f) % 360f

            rotate(degrees = rotationAngle, pivot = Offset(currentX, currentY)) {
                val leafPath = Path().apply {
                    moveTo(currentX, currentY - leafSizePx)
                    quadraticTo(
                        currentX + leafSizePx * 0.8f, currentY,
                        currentX, currentY + leafSizePx
                    )
                    quadraticTo(
                        currentX - leafSizePx * 0.8f, currentY,
                        currentX, currentY - leafSizePx
                    )
                    close()
                }
                drawPath(
                    path = leafPath,
                    color = leaf.color.copy(alpha = 0.45f)
                )
            }
        }

        // 5. Render Active Seafaring Sea Trail & Aura Animations
        if (selectedNavAnimations.isNotEmpty()) {
            val primaryColor = Color(navColor1)
            val auraColor = Color(navColor2)
            selectedNavAnimations.forEach { animStyle ->
                when {
                    animStyle.contains("Bioluminescent") || animStyle.contains("Surge") -> {
                        for (i in 0 until 16) {
                            val pX = ((i * 67 + animTime * 15f) % width)
                            val pY = height * 0.2f + ((i * 41 + sin(animTime * 0.1f + i) * 30f) % (height * 0.7f))
                            val pRadius = (2f + (i % 4) * 1.5f) * density
                            drawCircle(
                                color = auraColor.copy(alpha = 0.35f + (i % 3) * 0.15f),
                                radius = pRadius * 1.8f,
                                center = Offset(pX, pY)
                            )
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.7f),
                                radius = pRadius,
                                center = Offset(pX, pY)
                            )
                        }
                    }
                    animStyle.contains("Waves") || animStyle.contains("Tide") || animStyle.contains("Tsunami") -> {
                        for (i in 0 until 4) {
                            val waveY = height * (0.3f + i * 0.18f)
                            val waveOffset = (animTime * (20f + i * 10f)) % width
                            val wavePath = Path().apply {
                                moveTo(-width + waveOffset, waveY)
                                quadraticTo(
                                    -width / 2 + waveOffset, waveY - 25f * density,
                                    waveOffset, waveY
                                )
                                quadraticTo(
                                    width / 2 + waveOffset, waveY + 25f * density,
                                    width + waveOffset, waveY
                                )
                            }
                            drawPath(
                                path = wavePath,
                                color = primaryColor.copy(alpha = 0.18f),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f * density)
                            )
                        }
                    }
                    animStyle.contains("Star") || animStyle.contains("Astral") || animStyle.contains("Comet") -> {
                        for (i in 0 until 10) {
                            val starX = ((i * 89 + animTime * 8f) % width)
                            val starY = height * 0.1f + ((i * 37) % (height * 0.5f))
                            val alpha = 0.3f + 0.4f * sin(animTime * 0.2f + i).coerceIn(0f, 1f)
                            drawCircle(
                                color = Color.White.copy(alpha = alpha),
                                radius = 2.5f * density,
                                center = Offset(starX, starY)
                            )
                            drawCircle(
                                color = auraColor.copy(alpha = alpha * 0.5f),
                                radius = 6f * density,
                                center = Offset(starX, starY)
                            )
                        }
                    }
                    animStyle.contains("Kraken") || animStyle.contains("Tidal") || animStyle.contains("Vortex") -> {
                        for (i in 0 until 12) {
                            val bY = height - ((animTime * 25f + i * 60f) % height)
                            val bX = width * 0.15f + (i * 73) % (width * 0.7f) + sin(animTime * 0.08f + i) * 20f
                            val bRadius = (3f + (i % 3) * 2f) * density
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.3f),
                                radius = bRadius,
                                center = Offset(bX, bY),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * density)
                            )
                        }
                    }
                    animStyle.contains("Master") || animStyle.contains("Crown") || animStyle.contains("Lightning") -> {
                        for (i in 0 until 14) {
                            val auraX = ((i * 53 + animTime * 12f) % width)
                            val auraY = ((i * 79 + animTime * 10f) % height)
                            val goldColor = Color(0xFFFFD700)
                            drawCircle(
                                color = goldColor.copy(alpha = 0.4f),
                                radius = 3.5f * density,
                                center = Offset(auraX, auraY)
                            )
                        }
                    }
                }
            }
        }
    }
}
