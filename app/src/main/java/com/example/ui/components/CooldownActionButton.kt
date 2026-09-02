package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A custom action button that provides tactile haptic feedback on press,
 * darkens and becomes untargetable during a cooldown period, and shows a visual timer.
 */
@Composable
fun CooldownActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cooldownMs: Long = 1500L,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E6B42)),
    shape: Shape = RoundedCornerShape(8.dp),
    border: BorderStroke? = BorderStroke(1.dp, Color(0xFF00FF9D)),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isOnCooldown by remember { mutableStateOf(false) }
    var cooldownRemainingMs by remember { mutableLongStateOf(0L) }

    val progressFraction = if (cooldownMs > 0) {
        (cooldownRemainingMs.toFloat() / cooldownMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val isInteractable = enabled && !isOnCooldown

    Box(
        modifier = modifier.clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                if (isInteractable) {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (_: Exception) {
                    }
                    onClick()

                    if (cooldownMs > 0) {
                        isOnCooldown = true
                        cooldownRemainingMs = cooldownMs
                        coroutineScope.launch {
                            val step = 50L
                            while (cooldownRemainingMs > 0) {
                                delay(step)
                                cooldownRemainingMs -= step
                            }
                            cooldownRemainingMs = 0L
                            isOnCooldown = false
                        }
                    }
                }
            },
            enabled = isInteractable,
            colors = if (isOnCooldown) {
                ButtonDefaults.buttonColors(
                    containerColor = colors.containerColor.copy(alpha = 0.35f),
                    disabledContainerColor = colors.containerColor.copy(alpha = 0.3f),
                    contentColor = colors.contentColor.copy(alpha = 0.5f),
                    disabledContentColor = colors.contentColor.copy(alpha = 0.5f)
                )
            } else colors,
            shape = shape,
            border = if (isOnCooldown) BorderStroke(1.dp, Color.DarkGray) else border,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isOnCooldown) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sec = String.format("%.1f", cooldownRemainingMs / 1000.0f)
                    Text(
                        "⏳ ${sec}s",
                        color = Color(0xFFFFB703),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            } else {
                content()
            }
        }

        // Cooldown overlay & progress bar
        if (isOnCooldown) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = Color(0xFFFFB703),
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
