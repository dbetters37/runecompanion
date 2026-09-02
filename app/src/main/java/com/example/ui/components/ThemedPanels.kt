package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================================
// 1. WOOD / TIMBER THEMED PANELS (For The Grove, Whittling, and Farming)
// =============================================================================

val WoodDarkBrown = Color(0xFF1E130B)
val WoodMidBrown = Color(0xFF2C1C12)
val WoodLightBrown = Color(0xFF3E281A)
val WoodGrainLine = Color(0xFF4A3221).copy(alpha = 0.35f)
val WoodBorderBronze = Color(0xFF795548)
val WoodHighlightGold = Color(0xFFD7CCC8)
val WoodAccentAmber = Color(0xFFFFB74D)

/**
 * Rich Wood Plank Panel with subtle horizontal grain and timber corner brackets.
 */
@Composable
fun WoodPlankPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = WoodBorderBronze,
    showCornerAccents: Boolean = true,
    accentIcon: String = "🪵",
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius)),
        color = WoodDarkBrown,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Wood Grain Background Texture Canvas
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height

                // Base subtle gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(WoodMidBrown, WoodDarkBrown, WoodMidBrown)
                    )
                )

                // Horizontal Wood Planks and Grain Lines
                val plankHeight = 36f
                var y = 0f
                while (y < height) {
                    // Plank separator line
                    drawLine(
                        color = Color(0xFF140D07),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = WoodGrainLine,
                        start = Offset(0f, y + 2f),
                        end = Offset(width, y + 2f),
                        strokeWidth = 1f
                    )

                    // Secondary fine grain streaks
                    drawLine(
                        color = WoodGrainLine.copy(alpha = 0.18f),
                        start = Offset(20f, y + 14f),
                        end = Offset(width - 20f, y + 14f),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = WoodGrainLine.copy(alpha = 0.12f),
                        start = Offset(40f, y + 24f),
                        end = Offset(width - 50f, y + 24f),
                        strokeWidth = 0.8f
                    )

                    y += plankHeight
                }
            }

            // Optional rustic corner indicators
            if (showCornerAccents) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(accentIcon, fontSize = 9.sp, color = Color.White.copy(alpha = 0.25f))
                    Text(accentIcon, fontSize = 9.sp, color = Color.White.copy(alpha = 0.25f))
                }
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
            ) {
                content()
            }
        }
    }
}

/**
 * Wooden Header Banner for Groves, Workshops, and Farmsteads
 */
@Composable
fun WoodHeaderBanner(
    title: String,
    subtitle: String? = null,
    icon: String = "🪵",
    trailingText: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = WoodMidBrown,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF8D6E63).copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(icon, fontSize = 16.sp)
                Column {
                    Text(
                        text = title,
                        color = WoodAccentAmber,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = WoodHighlightGold.copy(alpha = 0.85f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    color = Color(0xFFA5D6A7),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


// =============================================================================
// 2. STONE / MASONRY THEMED PANELS (For Forging / Smithing and PohHouse)
// =============================================================================

val StoneDarkSlate = Color(0xFF14171A)
val StoneMidSlate = Color(0xFF1E2328)
val StoneLightSlate = Color(0xFF2A3138)
val StoneMortarLine = Color(0xFF0F1214)
val StoneBorderSlate = Color(0xFF607D8B)
val StoneHighlightSilver = Color(0xFFB0BEC5)
val StoneAccentGold = Color(0xFFFFD54F)

/**
 * Chiseled Stone Masonry Panel with brick mortar grid and forged iron rivets.
 */
@Composable
fun StoneMasonryPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = StoneBorderSlate,
    showCornerRivets: Boolean = true,
    accentIcon: String = "⚒️",
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius)),
        color = StoneDarkSlate,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Stone Masonry Canvas Texture
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height

                // Deep granite gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(StoneMidSlate, StoneDarkSlate, StoneMidSlate)
                    )
                )

                // Masonry Bricks Pattern
                val blockHeight = 28f
                val blockWidth = 56f
                var row = 0
                var y = 0f
                while (y < height) {
                    val offsetX = if (row % 2 == 1) blockWidth / 2f else 0f
                    // Horizontal mortar line
                    drawLine(
                        color = StoneMortarLine,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.5f
                    )
                    // Highlight on top of mortar
                    drawLine(
                        color = StoneLightSlate.copy(alpha = 0.25f),
                        start = Offset(0f, y + 1.5f),
                        end = Offset(width, y + 1.5f),
                        strokeWidth = 0.8f
                    )

                    // Vertical brick cuts
                    var x = -offsetX
                    while (x < width) {
                        if (x >= 0f) {
                            drawLine(
                                color = StoneMortarLine,
                                start = Offset(x, y),
                                end = Offset(x, y + blockHeight),
                                strokeWidth = 1.2f
                            )
                            drawLine(
                                color = StoneLightSlate.copy(alpha = 0.2f),
                                start = Offset(x + 1f, y),
                                end = Offset(x + 1f, y + blockHeight),
                                strokeWidth = 0.6f
                            )
                        }
                        x += blockWidth
                    }

                    y += blockHeight
                    row++
                }
            }

            // Forged Iron Corner Rivets
            if (showCornerRivets) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("⚙️", fontSize = 8.sp, color = Color.White.copy(alpha = 0.25f))
                    Text("⚙️", fontSize = 8.sp, color = Color.White.copy(alpha = 0.25f))
                }
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
            ) {
                content()
            }
        }
    }
}

/**
 * Stone Masonry Header Banner for Forges, Quarries, and Estates
 */
@Composable
fun StoneHeaderBanner(
    title: String,
    subtitle: String? = null,
    icon: String = "⚒️",
    trailingText: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StoneMidSlate,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, StoneBorderSlate.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(icon, fontSize = 16.sp)
                Column {
                    Text(
                        text = title,
                        color = StoneAccentGold,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = StoneHighlightSilver,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    color = Color(0xFF80DEEA),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


// =============================================================================
// 3. NAUTICAL / WATERY THEMED PANELS (For Shaman Pool and Navigation / Sailing)
// =============================================================================

val OceanicDeepAbyss = Color(0xFF04121F)
val OceanicMidNavy = Color(0xFF082035)
val OceanicCyanGlow = Color(0xFF00E5FF)
val OceanicSeafoam = Color(0xFF00ACC1)
val OceanicWaterWave = Color(0xFF0077B6).copy(alpha = 0.28f)
val OceanicBrassGold = Color(0xFFFFD54F)

/**
 * Nautical Oceanic Panel with rippling sea waves and crystalline azure border.
 */
@Composable
fun NauticalOceanicPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = OceanicSeafoam,
    showNauticalAccents: Boolean = true,
    accentIcon: String = "⛵",
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nautical_waves")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_anim"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius)),
        color = OceanicDeepAbyss,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Water Waves Canvas
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height

                // Deep water abyssal gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(OceanicMidNavy, OceanicDeepAbyss, OceanicMidNavy)
                    )
                )

                // Sine wave water ripples
                val waveHeight = 5f
                val wavelength = 120f

                for (w in 0..4) {
                    val waveY = height * (0.2f + w * 0.18f)
                    val path = Path()
                    path.moveTo(0f, waveY)
                    var x = 0f
                    while (x <= width) {
                        val phase = waveOffset + (w * 1.2f) + (x / wavelength)
                        val y = waveY + kotlin.math.sin(phase).toFloat() * waveHeight
                        path.lineTo(x, y)
                        x += 10f
                    }
                    drawPath(
                        path = path,
                        color = if (w % 2 == 0) OceanicWaterWave else OceanicCyanGlow.copy(alpha = 0.08f),
                        style = Stroke(width = 1.5f)
                    )
                }

                // Ambient light bubbles
                drawCircle(
                    color = OceanicCyanGlow.copy(alpha = 0.12f),
                    radius = 4f,
                    center = Offset(width * 0.15f, height * 0.35f)
                )
                drawCircle(
                    color = OceanicCyanGlow.copy(alpha = 0.09f),
                    radius = 6f,
                    center = Offset(width * 0.82f, height * 0.65f)
                )
                drawCircle(
                    color = OceanicCyanGlow.copy(alpha = 0.15f),
                    radius = 3f,
                    center = Offset(width * 0.55f, height * 0.85f)
                )
            }

            // Nautical Accents
            if (showNauticalAccents) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🧭", fontSize = 8.sp, color = Color.White.copy(alpha = 0.25f))
                    Text(accentIcon, fontSize = 8.sp, color = Color.White.copy(alpha = 0.25f))
                }
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
            ) {
                content()
            }
        }
    }
}

/**
 * Nautical Header Banner for Harbors, Sea Expeditions, and Spirit Springs
 */
@Composable
fun NauticalHeaderBanner(
    title: String,
    subtitle: String? = null,
    icon: String = "⛵",
    trailingText: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = OceanicMidNavy,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, OceanicSeafoam.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(icon, fontSize = 16.sp)
                Column {
                    Text(
                        text = title,
                        color = OceanicBrassGold,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = Color(0xFFB2EBF2),
                            fontSize = 10.sp
                        )
                    }
                }
            }
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    color = Color(0xFF80E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
