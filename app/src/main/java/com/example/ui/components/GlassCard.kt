package com.example.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    glassColor: Color? = null,
    testTag: String = "glass_card",
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val defaultColor = if (isDark) {
        Color(0xFF0F172A).copy(alpha = 0.45f)
    } else {
        Color.White.copy(alpha = 0.40f)
    }

    val finalGlassColor = glassColor ?: defaultColor

    val borderBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.04f),
                Color.Black.copy(alpha = 0.25f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.45f),
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.05f)
            )
        }
    )

// Enhanced glass effect by making inner background more layered
    val gradientBackground = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                finalGlassColor.copy(alpha = finalGlassColor.alpha * 1.2f.coerceAtMost(1f)),
                finalGlassColor.copy(alpha = finalGlassColor.alpha * 0.7f),
                finalGlassColor
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.5f),
                Color.White.copy(alpha = 0.2f),
                Color.White.copy(alpha = 0.4f)
            )
        }
    )

    val shape = RoundedCornerShape(cornerRadius)

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .shadow(
                elevation = if (isDark) 12.dp else 4.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .border(BorderStroke(borderWidth, borderBrush), shape = shape)
            .clip(shape)
            .background(gradientBackground)
            .then(clickableModifier)
            .padding(16.dp)
    ) {
        content()
    }
}
