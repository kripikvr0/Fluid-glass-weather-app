package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FluidBackground(
    weatherCode: Int,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    // Determine the color scheme based on weather code and isDay
    val themeColors = getWeatherColors(weatherCode, isDay)

    // Smoothly animate colors when the weather or time changes
    val color1 by animateColorAsState(themeColors.color1, tween(1500, easing = EaseInOutQuad), label = "c1")
    val color2 by animateColorAsState(themeColors.color2, tween(1500, easing = EaseInOutQuad), label = "c2")
    val color3 by animateColorAsState(themeColors.color3, tween(1500, easing = EaseInOutQuad), label = "c3")
    val colorBg by animateColorAsState(themeColors.colorBg, tween(1500, easing = EaseInOutQuad), label = "cBg")

    // Smooth floating animation loops for 4 orbs
    val transition = rememberInfiniteTransition(label = "FluidOrbs")

    val drift1X by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * java.lang.Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "d1x"
    )
    val drift2X by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * java.lang.Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
        label = "d2x"
    )
    val drift3Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * java.lang.Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing)),
        label = "d3y"
    )

    val drift4Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * java.lang.Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing)),
        label = "d4y"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Fill background baseline
        drawRect(color = colorBg)

        // Orb 1: Warm ambient glowing hub (top/leftish)
        val orb1X = w * 0.25f + sin(drift1X) * (w * 0.12f)
        val orb1Y = h * 0.2f + cos(drift1X) * (h * 0.08f)
        val orb1Radius = w * 0.55f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1, Color.Transparent),
                center = Offset(orb1X, orb1Y),
                radius = orb1Radius
            ),
            radius = orb1Radius,
            center = Offset(orb1X, orb1Y)
        )

        // Orb 2: Dynamic bright secondary tone (middle/rightish)
        val orb2X = w * 0.75f + cos(drift2X) * (w * 0.15f)
        val orb2Y = h * 0.5f + sin(drift2X) * (h * 0.1f)
        val orb2Radius = w * 0.65f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2, Color.Transparent),
                center = Offset(orb2X, orb2Y),
                radius = orb2Radius
            ),
            radius = orb2Radius,
            center = Offset(orb2X, orb2Y)
        )

        // Orb 3: Accented supporting glow (bottom/leftish)
        val orb3X = w * 0.35f + cos(drift3Y) * (w * 0.1f)
        val orb3Y = h * 0.8f + sin(drift3Y) * (h * 0.08f)
        val orb3Radius = w * 0.5f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color3, Color.Transparent),
                center = Offset(orb3X, orb3Y),
                radius = orb3Radius
            ),
            radius = orb3Radius,
            center = Offset(orb3X, orb3Y)
        )

        // Orb 4: Subdued depth layer (bottom/rightish)
        val orb4X = w * 0.8f + sin(drift4Y) * (w * 0.15f)
        val orb4Y = h * 0.9f + cos(drift4Y) * (h * 0.12f)
        val orb4Radius = w * 0.7f
        drawCircle(
            brush = Brush.radialGradient(
                // Use a mix of color1 and color3
                colors = listOf(color1.copy(alpha = color1.alpha * 0.6f), Color.Transparent),
                center = Offset(orb4X, orb4Y),
                radius = orb4Radius
            ),
            radius = orb4Radius,
            center = Offset(orb4X, orb4Y)
        )
    }
}

private data class ThemeColors(
    val color1: Color,
    val color2: Color,
    val color3: Color,
    val colorBg: Color
)

private fun getWeatherColors(weatherCode: Int, isDay: Boolean): ThemeColors {
    return if (isDay) {
        when (weatherCode) {
            0 -> ThemeColors(
                color1 = Color(0xFFFFE082).copy(alpha = 0.55f), // Golden Amber
                color2 = Color(0xFF80DEEA).copy(alpha = 0.55f), // Cyan sky
                color3 = Color(0xFFFFF59D).copy(alpha = 0.45f), // Soft sunlight
                colorBg = Color(0xFFE0F7FA)                     // Light icy cyan
            )
            1, 2, 3 -> ThemeColors(
                color1 = Color(0xFFB0BEC5).copy(alpha = 0.6f),  // Soft cloud metal
                color2 = Color(0xFF90CAF9).copy(alpha = 0.5f),  // Muted steel blue
                color3 = Color(0xFFE1BEE7).copy(alpha = 0.45f), // Lavender haze
                colorBg = Color(0xFFECEFF1)                     // Clean alabaster
            )
            45, 48 -> ThemeColors(
                color1 = Color(0xFFCFD8DC).copy(alpha = 0.55f), // Deep foggy grey
                color2 = Color(0xFFB0BEC5).copy(alpha = 0.5f),
                color3 = Color(0xFFECEFF1).copy(alpha = 0.4f),
                colorBg = Color(0xFFECEFF1)
            )
            95, 96, 99 -> ThemeColors( // Stormy
                color1 = Color(0xFF5C6BC0).copy(alpha = 0.55f), // Indigo storm core
                color2 = Color(0xFF263238).copy(alpha = 0.6f),  // Heavy charcoal slate
                color3 = Color(0xFFFFF59D).copy(alpha = 0.45f), // Yellow electric storm highlights
                colorBg = Color(0xFF37474F)
            )
            else -> ThemeColors( // Rain/Drizzle
                color1 = Color(0xFF4FC3F7).copy(alpha = 0.60f), // Soft rain teal
                color2 = Color(0xFF90A4AE).copy(alpha = 0.55f), // Wet slate grey
                color3 = Color(0xFF81C784).copy(alpha = 0.45f), // Fresh wet grass green
                colorBg = Color(0xFFECEFF1)
            )
        }
    } else {
        // Night Theme
        when (weatherCode) {
            0 -> ThemeColors(
                color1 = Color(0xFF311B92).copy(alpha = 0.65f), // Midnight royal indigo
                color2 = Color(0xFF1565C0).copy(alpha = 0.60f), // Star-lit sapphire blue
                color3 = Color(0xFF006064).copy(alpha = 0.50f), // Cosmic dark teal
                colorBg = Color(0xFF0F172A)                     // Dark Slate Gray
            )
            1, 2, 3 -> ThemeColors(
                color1 = Color(0xFF37474F).copy(alpha = 0.65f), // Overcast midnight
                color2 = Color(0xFF4A148C).copy(alpha = 0.55f), // Purple dark cloud margins
                color3 = Color(0xFF1A237E).copy(alpha = 0.50f), // Deep navy sky
                colorBg = Color(0xFF0E121E)
            )
            95, 96, 99 -> ThemeColors( // Stormy Night
                color1 = Color(0xFF4A148C).copy(alpha = 0.65f), // Severe dark thunder violet
                color2 = Color(0xFF212121).copy(alpha = 0.70f), // Absolute black charcoal
                color3 = Color(0xFFFFEB3B).copy(alpha = 0.4f),  // Lightning bolt yellow flares
                colorBg = Color(0xFF05050A)
            )
            else -> ThemeColors( // Rain / Snowy night
                color1 = Color(0xFF01579B).copy(alpha = 0.65f), // Wet dark blue
                color2 = Color(0xFF263238).copy(alpha = 0.60f), // Heavy rain slate
                color3 = Color(0xFF4A148C).copy(alpha = 0.45f), // Stormy dark amethyst
                colorBg = Color(0xFF0A101D)
            )
        }
    }
}
