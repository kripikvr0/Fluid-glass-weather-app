package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedWeatherIcon(
    weatherCode: Int,
    isDay: Boolean = true,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    // Group weather code into categories
    when (weatherCode) {
        0 -> {
            if (isDay) SunIcon(modifier.size(size))
            else MoonIcon(modifier.size(size))
        }
        1, 2, 3 -> CloudyIcon(modifier.size(size), isModerate = weatherCode > 1)
        45, 48 -> FogIcon(modifier.size(size))
        51, 53, 55, 56, 57, 61, 63, 65, 80, 81, 82 -> RainyIcon(modifier.size(size), isHeavy = weatherCode % 10 >= 5)
        71, 73, 75, 77, 85, 86 -> SnowyIcon(modifier.size(size))
        95, 96, 99 -> ThunderIcon(modifier.size(size))
        else -> SunIcon(modifier.size(size)) // Fallback
    }
}

@Composable
fun SunIcon(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "SunRotation")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val scale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        val sunRadius = width * 0.22f * scale

        // Draw sun core with soft glow
        drawCircle(
            color = Color(0xFFFFD54F).copy(alpha = 0.3f),
            radius = sunRadius * 1.3f,
            center = center
        )
        drawCircle(
            color = Color(0xFFFFB300),
            radius = sunRadius,
            center = center
        )

        // Draw rotating rays
        withTransform({
            rotate(rotation, center)
        }) {
            val rayCount = 8
            val innerRayRadius = sunRadius * 1.3f
            val outerRayRadius = sunRadius * 1.7f
            val rayWidth = width * 0.06f

            for (i in 0 until rayCount) {
                val angle = (i * (2 * PI / rayCount)).toFloat()
                val startX = center.x + innerRayRadius * cos(angle)
                val startY = center.y + innerRayRadius * sin(angle)
                val endX = center.x + outerRayRadius * cos(angle)
                val endY = center.y + outerRayRadius * sin(angle)

                drawLine(
                    color = Color(0xFFFFCA28),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = rayWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun MoonIcon(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "MoonPulse")
    val glowPulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width * 0.45f, height * 0.45f)
        val outerRadius = width * 0.32f

        // Soft moon glow
        drawCircle(
            color = Color(0xFFE0F7FA).copy(alpha = glowPulse),
            radius = outerRadius * 1.25f,
            center = center
        )

        // Moon base surface
        drawCircle(
            color = Color(0xFFECEFF1),
            radius = outerRadius,
            center = center
        )

        // Moon crescent cutout matching ambient glass/fog depths
        drawCircle(
            color = Color(0xFF0F172A).copy(alpha = 0.35f),
            radius = outerRadius * 1.05f,
            center = Offset(center.x - outerRadius * 0.55f, center.y - outerRadius * 0.25f)
        )

        // Stars orbiting
        val starPos = Offset(width * 0.75f, height * 0.3f)
        drawCircle(
            color = Color.White.copy(alpha = glowPulse * 2f),
            radius = width * 0.04f,
            center = starPos
        )

        drawCircle(
            color = Color.White.copy(alpha = glowPulse * 1.5f),
            radius = width * 0.025f,
            center = Offset(width * 0.2f, height * 0.75f)
        )
    }
}

@Composable
fun CloudyIcon(modifier: Modifier = Modifier, isModerate: Boolean = false) {
    val transition = rememberInfiniteTransition(label = "CloudDrift")
    val drift1 by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift1"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Sun behind cloud if it is just partly cloudy
        if (!isModerate) {
            val sunCenter = Offset(width * 0.65f, height * 0.35f)
            val sunRadius = width * 0.18f
            drawCircle(
                color = Color(0xFFFFB300),
                radius = sunRadius,
                center = sunCenter
            )
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = 0.4f),
                radius = sunRadius * 1.2f,
                center = sunCenter
            )
        }

        // Draw Cloud outline by joining circles and rounded rects
        val cloudColor = if (isModerate) Color(0xFFCFD8DC).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.92f)
        val cloudGlowColor = if (isModerate) Color(0xFF90A4AE).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.4f)

        // Simulated cloud shadow / back highlights
        drawRoundRect(
            color = cloudGlowColor,
            topLeft = Offset(width * 0.18f + drift1, height * 0.48f),
            size = Size(width * 0.6f, height * 0.28f),
            cornerRadius = CornerRadius(height * 0.14f)
        )
        drawCircle(
            color = cloudGlowColor,
            radius = width * 0.18f,
            center = Offset(width * 0.4f + drift1, height * 0.42f)
        )

        // Main Cloud surface (Clean White/Alabaster)
        drawRoundRect(
            color = cloudColor,
            topLeft = Offset(width * 0.2f + drift1, height * 0.5f),
            size = Size(width * 0.58f, height * 0.26f),
            cornerRadius = CornerRadius(height * 0.13f)
        )
        drawCircle(
            color = cloudColor,
            radius = width * 0.18f,
            center = Offset(width * 0.42f + drift1, height * 0.45f)
        )
        drawCircle(
            color = cloudColor,
            radius = width * 0.13f,
            center = Offset(width * 0.62f + drift1, height * 0.52f)
        )
    }
}

@Composable
fun RainyIcon(modifier: Modifier = Modifier, isHeavy: Boolean = false) {
    val transition = rememberInfiniteTransition(label = "RainFlow")
    val rainOffset1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainOffset1"
    )
    val cloudPulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudPulse"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Dark stormy cloud base
        val cloudColor = Color(0xFF607D8B).copy(alpha = 0.95f)
        val cloudGlow = Color(0xFF37474F).copy(alpha = 0.25f)

        drawRoundRect(
            color = cloudGlow,
            topLeft = Offset(width * 0.15f, height * 0.36f),
            size = Size(width * 0.62f, height * 0.28f),
            cornerRadius = CornerRadius(height * 0.14f)
        )

        drawCircle(
            color = cloudColor,
            radius = width * 0.18f * cloudPulse,
            center = Offset(width * 0.4f, height * 0.38f)
        )
        drawCircle(
            color = cloudColor,
            radius = width * 0.13f,
            center = Offset(width * 0.62f, height * 0.45f)
        )
        drawRoundRect(
            color = cloudColor,
            topLeft = Offset(width * 0.18f, height * 0.42f),
            size = Size(width * 0.58f, height * 0.24f),
            cornerRadius = CornerRadius(height * 0.12f)
        )

        // Rainy Droplets
        val lineCount = if (isHeavy) 5 else 3
        val rainColor = Color(0xFF0288D1).copy(alpha = 0.8f)

        for (i in 0 until lineCount) {
            val spaceRatio = 0.15f
            val baseLeft = width * (0.28f + i * spaceRatio)
            val dropY = height * 0.64f + (rainOffset1 + i * 4) % 18f

            drawLine(
                color = rainColor,
                start = Offset(baseLeft, dropY),
                end = Offset(baseLeft - width * 0.03f, dropY + height * 0.08f),
                strokeWidth = width * 0.015f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun SnowyIcon(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "SnowFall")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Clean white cool cloud background
        val cloudColor = Color(0xFFECEFF1).copy(alpha = 0.95f)
        drawRoundRect(
            color = cloudColor,
            topLeft = Offset(width * 0.18f, height * 0.42f),
            size = Size(width * 0.58f, height * 0.24f),
            cornerRadius = CornerRadius(height * 0.12f)
        )
        drawCircle(
            color = cloudColor,
            radius = width * 0.18f,
            center = Offset(width * 0.4f, height * 0.38f)
        )

        // Soft falling snowflakes
        val snowRadius = width * 0.03f
        val snowColor = Color.White.copy(alpha = 0.9f)

        withTransform({
            rotate(rotation, Offset(width * 0.5f, height * 0.72f))
        }) {
            // Draw 4 circular points behaving like rotating ice crystals
            drawCircle(snowColor, snowRadius, Offset(width * 0.5f, height * 0.64f))
            drawCircle(snowColor, snowRadius, Offset(width * 0.5f, height * 0.8f))
            drawCircle(snowColor, snowRadius, Offset(width * 0.42f, height * 0.72f))
            drawCircle(snowColor, snowRadius, Offset(width * 0.58f, height * 0.72f))
        }
    }
}

@Composable
fun ThunderIcon(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "LightningFlashing")
    val alphaFlash by transition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1800
                0.0f at 0
                0.1f at 500
                1.0f at 550
                0.2f at 600
                0.8f at 650
                0.0f at 750
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "flash"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Severe dark grey clouds
        val cloudColor = Color(0xFF455A64)
        drawCircle(
            color = cloudColor,
            radius = width * 0.19f,
            center = Offset(width * 0.4f, height * 0.36f)
        )
        drawCircle(
            color = cloudColor,
            radius = width * 0.14f,
            center = Offset(width * 0.63f, height * 0.42f)
        )
        drawRoundRect(
            color = cloudColor,
            topLeft = Offset(width * 0.18f, height * 0.4f),
            size = Size(width * 0.58f, height * 0.24f),
            cornerRadius = CornerRadius(height * 0.12f)
        )

        // Lighting Bolt Path
        val lightningPath = Path().apply {
            moveTo(width * 0.55f, height * 0.55f)
            lineTo(width * 0.42f, height * 0.72f)
            lineTo(width * 0.52f, height * 0.72f)
            lineTo(width * 0.38f, height * 0.92f)
            lineTo(width * 0.44f, height * 0.92f)
            lineTo(width * 0.60f, height * 0.68f)
            lineTo(width * 0.50f, height * 0.68f)
            close()
        }

        // Animated neon yellow flash glow
        drawPath(
            path = lightningPath,
            color = Color(0xFFFFEB3B).copy(alpha = alphaFlash)
        )
    }
}

@Composable
fun FogIcon(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "FogWaves")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Simulated soft fog lines flowing left-right
        val strokeWidth = height * 0.05f
        val fogLineColor = Color(0xFFB0BEC5).copy(alpha = 0.7f)

        // Fog lines that wave gently
        for (i in 0 until 3) {
            val waveHeight = height * (0.35f + i * 0.15f)
            val path = Path().apply {
                moveTo(0f, waveHeight)
                for (x in 0..width.toInt() step 5) {
                    val angle = (x / width) * 2 * PI + phase + i * PI / 4
                    val y = waveHeight + sin(angle).toFloat() * 6f
                    lineTo(x.toFloat(), y)
                }
            }
            drawPath(
                path = path,
                color = fogLineColor,
                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}
