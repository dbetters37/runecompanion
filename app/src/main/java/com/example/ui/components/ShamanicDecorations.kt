package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom Weathered Stone Graphical Overlay Modifier.
 * Renders an authentic ancient, nature-y, and spooky weathered stone border with:
 * - Chiseled granite bevels & masonry joints
 * - Corner stone fissures and cracks
 * - Creeping mossy lichen spots & vine tendrils
 * - Subtle pulsing shamanic rune magic
 */
fun Modifier.weatheredStoneBorder(
    cornerRadius: Dp = 10.dp,
    borderColor: Color = Color(0xFF38473C),
    mossColor: Color = Color(0xFF1E6B42),
    runeGlowAlpha: Float = 0.75f,
    showRunes: Boolean = true
): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        val w = size.width
        val h = size.height
        val radiusPx = cornerRadius.toPx()

        // 1. Heavy Outer Granite Mortar Shadow
        drawRoundRect(
            color = Color(0xFF0C0907),
            size = size,
            cornerRadius = CornerRadius(radiusPx, radiusPx),
            style = Stroke(width = 4f)
        )

        // 2. Main Weathered Stone Chiseled Frame
        drawRoundRect(
            color = borderColor,
            size = size,
            cornerRadius = CornerRadius(radiusPx, radiusPx),
            style = Stroke(width = 2.5f)
        )

        // Inner Bevel Highlight for 3D Carved Stone Look
        drawRoundRect(
            color = Color(0xFF536959).copy(alpha = 0.4f),
            topLeft = Offset(2f, 2f),
            size = Size(w - 4f, h - 4f),
            cornerRadius = CornerRadius(radiusPx - 1f, radiusPx - 1f),
            style = Stroke(width = 1.2f)
        )

        // 3. Masonry Stone Joints along perimeter
        val segmentCountX = (w / 40f).toInt().coerceAtLeast(2)
        val stepX = w / segmentCountX
        for (i in 1 until segmentCountX) {
            val x = i * stepX
            drawLine(
                color = Color(0xFF0F0B08),
                start = Offset(x, 0f),
                end = Offset(x, 5f),
                strokeWidth = 2f
            )
            drawLine(
                color = Color(0xFF0F0B08),
                start = Offset(x, h - 5f),
                end = Offset(x, h),
                strokeWidth = 2f
            )
        }

        // 4. Creeping Moss & Spooky Lichen Overgrowth Patches
        val mossPatches = listOf(
            Offset(w * 0.15f, 2f) to 6f,
            Offset(w * 0.45f, 1f) to 8f,
            Offset(w * 0.8f, 3f) to 7f,
            Offset(2f, h * 0.3f) to 6f,
            Offset(w - 2f, h * 0.6f) to 7f,
            Offset(w * 0.25f, h - 2f) to 8f,
            Offset(w * 0.7f, h - 2f) to 6f
        )
        mossPatches.forEach { (offset, rad) ->
            drawCircle(
                color = mossColor,
                radius = rad,
                center = offset
            )
            drawCircle(
                color = Color(0xFF00FF9D).copy(alpha = 0.6f * runeGlowAlpha),
                radius = rad * 0.5f,
                center = offset
            )
        }

        // 5. Deep Angular Stone Fissures & Cracks at Corners
        val crack1 = Path().apply {
            moveTo(0f, 14f)
            lineTo(12f, 8f)
            lineTo(8f, 0f)
        }
        val crack2 = Path().apply {
            moveTo(w - 14f, 0f)
            lineTo(w - 8f, 10f)
            lineTo(w, 12f)
        }
        val crack3 = Path().apply {
            moveTo(w, h - 14f)
            lineTo(w - 10f, h - 8f)
            lineTo(w - 14f, h)
        }
        listOf(crack1, crack2, crack3).forEach { crack ->
            drawPath(
                path = crack,
                color = Color(0xFF090705),
                style = Stroke(width = 2.2f)
            )
            drawPath(
                path = crack,
                color = Color(0xFF00FF9D).copy(alpha = 0.4f * runeGlowAlpha),
                style = Stroke(width = 1.0f)
            )
        }

        // 6. Vine Tendrils hugging the top edge
        val vinePath = Path().apply {
            moveTo(20f, 4f)
            cubicTo(w * 0.2f, 8f, w * 0.35f, 1f, w * 0.5f, 5f)
            cubicTo(w * 0.65f, 1f, w * 0.8f, 7f, w - 20f, 3f)
        }
        drawPath(
            path = vinePath,
            color = Color(0xFF00FF9D).copy(alpha = 0.35f * runeGlowAlpha),
            style = Stroke(width = 1.5f)
        )
    }
)

/**
 * Custom Rustic Carved Jungle / Teak Wood Border Modifier.
 * Features:
 * - Carved timber plank slats & grain grooves
 * - Forged iron corner rivet studs
 * - Weathered oak bevels & mossy bark crevices
 */
fun Modifier.rusticWoodBorder(
    cornerRadius: Dp = 10.dp,
    plankColor: Color = Color(0xFF4A3423),
    rivetColor: Color = Color(0xFFC0A080),
    mossColor: Color = Color(0xFF2E6B44)
): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        val w = size.width
        val h = size.height
        val radiusPx = cornerRadius.toPx()

        // 1. Dark Shadow Timber Rim
        drawRoundRect(
            color = Color(0xFF140D08),
            size = size,
            cornerRadius = CornerRadius(radiusPx, radiusPx),
            style = Stroke(width = 3.5f)
        )

        // 2. Warm Carved Plank Frame
        drawRoundRect(
            color = plankColor,
            size = size,
            cornerRadius = CornerRadius(radiusPx, radiusPx),
            style = Stroke(width = 2.2f)
        )

        // Inner Timber Bevel
        drawRoundRect(
            color = Color(0xFF7A593E).copy(alpha = 0.5f),
            topLeft = Offset(2f, 2f),
            size = Size(w - 4f, h - 4f),
            cornerRadius = CornerRadius(radiusPx - 1f, radiusPx - 1f),
            style = Stroke(width = 1f)
        )

        // 3. Horizontal Wood Grain Slats
        val slatStep = 28f
        val slatCount = (h / slatStep).toInt()
        for (i in 1 until slatCount) {
            val y = i * slatStep
            drawLine(
                color = Color(0xFF26190E).copy(alpha = 0.65f),
                start = Offset(6f, y),
                end = Offset(w - 6f, y),
                strokeWidth = 1f
            )
        }

        // 4. Forged Iron Corner Rivets
        val rivetRadius = 3f
        val cornerOffsets = listOf(
            Offset(8f, 8f),
            Offset(w - 8f, 8f),
            Offset(8f, h - 8f),
            Offset(w - 8f, h - 8f)
        )
        cornerOffsets.forEach { pos ->
            drawCircle(
                color = Color(0xFF1B130D),
                radius = rivetRadius + 1f,
                center = pos
            )
            drawCircle(
                color = rivetColor,
                radius = rivetRadius,
                center = pos
            )
            drawCircle(
                color = Color(0xFFFFF0D0).copy(alpha = 0.7f),
                radius = rivetRadius * 0.4f,
                center = Offset(pos.x - 0.8f, pos.y - 0.8f)
            )
        }

        // 5. Mossy bark lichen patches
        listOf(
            Offset(w * 0.3f, 2f) to 5f,
            Offset(w * 0.75f, h - 2f) to 6f,
            Offset(2f, h * 0.5f) to 5f
        ).forEach { (pos, rad) ->
            drawCircle(
                color = mossColor,
                radius = rad,
                center = pos
            )
        }
    }
)

/**
 * Weathered Stone Panel Container Composable.
 * Wraps any layout with an authentic ancient, nature-y, and spooky weathered stone UI border.
 */
@Composable
fun WeatheredStonePanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    showFloraCorners: Boolean = true,
    showRunes: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weathered_stone_rune_glow")
    val runeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rune_glow"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B1612),
                        Color(0xFF14100D),
                        Color(0xFF1B1612)
                    )
                )
            )
            .weatheredStoneBorder(
                cornerRadius = cornerRadius,
                runeGlowAlpha = runeGlowAlpha,
                showRunes = showRunes
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            content()
        }

        if (showFloraCorners) {
            Text(
                text = "🌿",
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 2.dp, y = 2.dp)
            )
            Text(
                text = "🍃",
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
            )
        }

        if (showRunes) {
            Text(
                text = "ᛟ",
                color = Color(0xFF00FF9D).copy(alpha = runeGlowAlpha),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-10).dp, y = (-2).dp)
            )
        }
    }
}

/**
 * Ancient Overgrown Stone & Vine Container Card
 * Gives UI elements carved stone aesthetics, creeping vine tendrils, and glowing shamanic runes.
 */
@Composable
fun OvergrownStoneCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    showRunes: Boolean = true,
    showVineCorners: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    WeatheredStonePanel(
        modifier = modifier,
        cornerRadius = cornerRadius,
        showFloraCorners = showVineCorners,
        showRunes = showRunes,
        content = content
    )
}

/**
 * Rustic Carved Wood Container Card
 */
@Composable
fun RusticWoodCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E1F14),
                        Color(0xFF1E140C),
                        Color(0xFF271B11)
                    )
                )
            )
            .rusticWoodBorder(cornerRadius = cornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            content()
        }
    }
}

/**
 * Ancient Wooden Plank & Moss Banner Header
 */
@Composable
fun AncientWoodStoneBanner(
    title: String,
    subtitle: String? = null,
    icon: String = "🗿",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_rune_glow")
    val runeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF122118),
                        Color(0xFF1B3828),
                        Color(0xFF122118)
                    )
                )
            )
            .weatheredStoneBorder(
                cornerRadius = 8.dp,
                borderColor = Color(0xFF2E6B44),
                runeGlowAlpha = runeGlowAlpha
            )
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("🌿", fontSize = 14.sp)
                Text(icon, fontSize = 16.sp)
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = title,
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = OsrsParchment,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ᚱ", color = Color(0xFF00FF9D).copy(alpha = runeGlowAlpha), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("🍃", fontSize = 14.sp)
            }
        }
    }
}
