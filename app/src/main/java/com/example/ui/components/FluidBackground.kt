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
    val colorBgTop by animateColorAsState(themeColors.colorBgTop, tween(1500, easing = EaseInOutQuad), label = "cBgTop")
    val colorBgBottom by animateColorAsState(themeColors.colorBgBottom, tween(1500, easing = EaseInOutQuad), label = "cBgBottom")

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

        // Fill background baseline with a linear gradient (sky-like)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(colorBgTop, colorBgBottom)
            )
        )

        // Orb 1: Sun / Moon or primary cloud
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

        // Orb 2: Dynamic secondary tone
        val orb2X = w * 0.75f + cos(drift2X) * (w * 0.15f)
        val orb2Y = h * 0.45f + sin(drift2X) * (h * 0.1f)
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

        // Orb 3: Accented supporting glow
        val orb3X = w * 0.35f + cos(drift3Y) * (w * 0.1f)
        val orb3Y = h * 0.75f + sin(drift3Y) * (h * 0.08f)
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

        // Orb 4: Subdued depth layer
        val orb4X = w * 0.8f + sin(drift4Y) * (w * 0.15f)
        val orb4Y = h * 0.9f + cos(drift4Y) * (h * 0.12f)
        val orb4Radius = w * 0.7f
        drawCircle(
            brush = Brush.radialGradient(
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
    val colorBgTop: Color,
    val colorBgBottom: Color
)

private fun getWeatherColors(weatherCode: Int, isDay: Boolean): ThemeColors {
    return if (isDay) {
        when (weatherCode) {
            0 -> ThemeColors( // Clear Sky
                colorBgTop = Color(0xFF135AB0),    // Deep sky blue
                colorBgBottom = Color(0xFF65A1EA), // Light sky blue
                color1 = Color(0xFFFFF176).copy(alpha = 0.60f), // Bright Sun
                color2 = Color(0xFF81D4FA).copy(alpha = 0.30f), // Light blue haze
                color3 = Color(0xFFFFD54F).copy(alpha = 0.40f)  // Sun glow
            )
            1, 2 -> ThemeColors( // Mostly clear / partly cloudy
                colorBgTop = Color(0xFF2673D2),
                colorBgBottom = Color(0xFF80BAF1),
                color1 = Color(0xFFFFFFFF).copy(alpha = 0.40f), // Soft white cloud
                color2 = Color(0xFFFFF59D).copy(alpha = 0.30f), // Very soft sun
                color3 = Color(0xFFE0F7FA).copy(alpha = 0.30f)
            )
            3 -> ThemeColors( // Cloudy
                colorBgTop = Color(0xFF5D7A92),
                colorBgBottom = Color(0xFF90A4AE),
                color1 = Color(0xFFCFD8DC).copy(alpha = 0.50f),
                color2 = Color(0xFFB0BEC5).copy(alpha = 0.40f),
                color3 = Color(0xFFECEFF1).copy(alpha = 0.30f)
            )
            45, 48 -> ThemeColors( // Foggy
                colorBgTop = Color(0xFF8B9DAA),
                colorBgBottom = Color(0xFFCFD8DC),
                color1 = Color(0xFFECEFF1).copy(alpha = 0.60f),
                color2 = Color(0xFFB0BEC5).copy(alpha = 0.50f),
                color3 = Color(0xFFCFD8DC).copy(alpha = 0.40f)
            )
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> ThemeColors( // Rain/Drizzle
                colorBgTop = Color(0xFF2D3C4C),
                colorBgBottom = Color(0xFF5B6E80),
                color1 = Color(0xFF90A4AE).copy(alpha = 0.45f), // Dark cloud
                color2 = Color(0xFF546E7A).copy(alpha = 0.40f),
                color3 = Color(0xFF78909C).copy(alpha = 0.35f)
            )
            71, 73, 75, 77, 85, 86 -> ThemeColors( // Snow
                colorBgTop = Color(0xFF7D9BB7),
                colorBgBottom = Color(0xFFB3C6DB),
                color1 = Color(0xFFFFFFFF).copy(alpha = 0.55f),
                color2 = Color(0xFFCFD8DC).copy(alpha = 0.50f),
                color3 = Color(0xFFECEFF1).copy(alpha = 0.40f)
            )
            95, 96, 99 -> ThemeColors( // Thunderstorm
                colorBgTop = Color(0xFF1E2632),
                colorBgBottom = Color(0xFF38475A),
                color1 = Color(0xFF455A64).copy(alpha = 0.60f), // Heavy dark cloud
                color2 = Color(0xFF263238).copy(alpha = 0.65f),
                color3 = Color(0xFFFFE082).copy(alpha = 0.25f)  // Faint lightning glow
            )
            else -> ThemeColors(
                colorBgTop = Color(0xFF4A83C4),
                colorBgBottom = Color(0xFF83B3E7),
                color1 = Color(0xFFFFFFFF).copy(alpha = 0.40f),
                color2 = Color(0xFFB0BEC5).copy(alpha = 0.40f),
                color3 = Color(0xFF90CAF9).copy(alpha = 0.30f)
            )
        }
    } else {
        // Night Theme
        when (weatherCode) {
            0 -> ThemeColors( // Clear night
                colorBgTop = Color(0xFF060D20),
                colorBgBottom = Color(0xFF14244B),
                color1 = Color(0xFF7A8AAA).copy(alpha = 0.30f), // Starlight / Moon glow
                color2 = Color(0xFF1C2D54).copy(alpha = 0.40f),
                color3 = Color(0xFF3A5080).copy(alpha = 0.20f)
            )
            1, 2 -> ThemeColors( // Partly cloudy night
                colorBgTop = Color(0xFF091227),
                colorBgBottom = Color(0xFF1A2A4D),
                color1 = Color(0xFF37474F).copy(alpha = 0.50f),
                color2 = Color(0xFF455A64).copy(alpha = 0.40f),
                color3 = Color(0xFF263238).copy(alpha = 0.60f)
            )
            3 -> ThemeColors( // Cloudy night
                colorBgTop = Color(0xFF141A24),
                colorBgBottom = Color(0xFF283445),
                color1 = Color(0xFF37474F).copy(alpha = 0.60f),
                color2 = Color(0xFF263238).copy(alpha = 0.50f),
                color3 = Color(0xFF1A232E).copy(alpha = 0.70f)
            )
            45, 48 -> ThemeColors( // Foggy night
                colorBgTop = Color(0xFF1A2229),
                colorBgBottom = Color(0xFF2C3946),
                color1 = Color(0xFF455A64).copy(alpha = 0.50f),
                color2 = Color(0xFF37474F).copy(alpha = 0.50f),
                color3 = Color(0xFF546E7A).copy(alpha = 0.40f)
            )
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> ThemeColors( // Rain night
                colorBgTop = Color(0xFF0F151C),
                colorBgBottom = Color(0xFF1B2633),
                color1 = Color(0xFF263238).copy(alpha = 0.60f),
                color2 = Color(0xFF1A232E).copy(alpha = 0.55f),
                color3 = Color(0xFF37474F).copy(alpha = 0.45f)
            )
            71, 73, 75, 77, 85, 86 -> ThemeColors( // Snow night
                colorBgTop = Color(0xFF192233),
                colorBgBottom = Color(0xFF2C3E5A),
                color1 = Color(0xFF546E7A).copy(alpha = 0.45f),
                color2 = Color(0xFF455A64).copy(alpha = 0.50f),
                color3 = Color(0xFF78909C).copy(alpha = 0.35f)
            )
            95, 96, 99 -> ThemeColors( // Thunderstorm night
                colorBgTop = Color(0xFF0A0C10),
                colorBgBottom = Color(0xFF161C26),
                color1 = Color(0xFF1A232E).copy(alpha = 0.80f),
                color2 = Color(0xFF111720).copy(alpha = 0.70f),
                color3 = Color(0xFFFFEE58).copy(alpha = 0.20f) // Lightning flash
            )
            else -> ThemeColors(
                colorBgTop = Color(0xFF0F172A),
                colorBgBottom = Color(0xFF1E293B),
                color1 = Color(0xFF334155).copy(alpha = 0.50f),
                color2 = Color(0xFF1E293B).copy(alpha = 0.60f),
                color3 = Color(0xFF475569).copy(alpha = 0.40f)
            )
        }
    }
}
