package com.example.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom modifier that draws a dashed / dotted border around a composable using any shape.
 */
fun Modifier.dashedBorder(
    width: Dp = 1.dp,
    color: Color = Color.White,
    shape: Shape = RoundedCornerShape(4.dp),
    dashLength: Dp = 4.dp,
    gapLength: Dp = 4.dp
): Modifier = this.drawWithCache {
    val outline: Outline = shape.createOutline(size, layoutDirection, this)
    val stroke = Stroke(
        width = width.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            0f
        )
    )
    onDrawWithContent {
        drawContent()
        drawOutline(
            outline = outline,
            color = color,
            style = stroke
        )
    }
}
