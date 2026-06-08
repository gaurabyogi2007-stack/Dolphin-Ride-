package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun DolphinJumpAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dolphin_twin_jump_main")

    // Animate dolphin progress for jump trajectory (0.0 to 1.0)
    val jumpProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "jumpProgress"
    )

    // Bubble rising phase animation (0.0 to 1.0)
    val bubblesProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubblesProgress"
    )

    // Ray of light pulsate animation
    val rayAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rayAlpha"
    )

    // Animate wave offset horizontally for "yeta uta" water wave movement
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    // Animate wave vertical bobbing slightly
    val waveBob by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveBob"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(175.dp)
            .testTag("dolphin_custom_jump_animation_box"),
        contentAlignment = Alignment.BottomCenter
    ) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }

        // Water baseline level
        val waterY = heightPx * 0.72f

        // Draw background magic: Sunbeams, sparkling water depths, bubbles
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("water_environment_canvas")
        ) {
            // Draw background soft gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9).copy(alpha = 0.2f), // soft pastel glow representative of colorful flower shores
                        Color(0xFFE0F7FA).copy(alpha = 0.7f), // vibrant cyan sky
                        Color(0xFF81D4FA).copy(alpha = 0.85f), // clear sunny water top
                        Color(0xFF0288D1) // rich ocean depths
                    )
                )
            )

            // Draw Sunbeams/Rays looking like light from Heaven (matching dream image)
            val rayPath1 = Path().apply {
                moveTo(widthPx * 0.5f, 0f)
                lineTo(widthPx * 0.1f, heightPx)
                lineTo(widthPx * 0.35f, heightPx)
                close()
            }
            drawPath(
                path = rayPath1,
                color = Color.White.copy(alpha = rayAlpha * 0.5f)
            )

            val rayPath2 = Path().apply {
                moveTo(widthPx * 0.5f, 0f)
                lineTo(widthPx * 0.40f, heightPx)
                lineTo(widthPx * 0.65f, heightPx)
                close()
            }
            drawPath(
                path = rayPath2,
                color = Color.White.copy(alpha = rayAlpha * 0.61f)
            )

            val rayPath3 = Path().apply {
                moveTo(widthPx * 0.5f, 0f)
                lineTo(widthPx * 0.70f, heightPx)
                lineTo(widthPx * 0.95f, heightPx)
                close()
            }
            drawPath(
                path = rayPath3,
                color = Color.White.copy(alpha = rayAlpha * 0.45f)
            )

            // Draw dynamic underwater bubbles rising
            for (i in 0..7) {
                // Different offset & speed for each bubble
                val individualPhase = (bubblesProgress + i * 0.15f) % 1.0f
                val bubbleY = heightPx * (1.1f - individualPhase * 1.2f)
                val driftX = sin(individualPhase * 2 * PI.toFloat() + i) * 12f
                val bubbleBaseX = widthPx * (0.12f + i * 0.11f) + driftX
                val bubbleRadius = 4f + (i % 3) * 3f

                // Draw translucent outline of bubble with highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f),
                    radius = bubbleRadius,
                    center = Offset(bubbleBaseX, bubbleY),
                    style = Stroke(width = 1.5f)
                )
                // Outer glow of bubble
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                    radius = bubbleRadius + 1f,
                    center = Offset(bubbleBaseX, bubbleY)
                )
                // Highlight spot inside bubble
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = bubbleRadius * 0.3f,
                    center = Offset(bubbleBaseX - bubbleRadius * 0.35f, bubbleY - bubbleRadius * 0.35f)
                )
            }
        }

        // Draw multiple layers of matching waves (from dark blue to aqua clear gradient)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("water_waves_canvas")
        ) {
            // Layer 1: Back Wave (Deep Teal Ocean Navy)
            val pathBack = Path()
            pathBack.moveTo(0f, heightPx)
            for (x in 0..widthPx.toInt() step 5) {
                val angle = (x.toFloat() / widthPx) * 3f * PI.toFloat() - waveOffset
                val y = waterY - 8f + waveBob + sin(angle) * 11f
                pathBack.lineTo(x.toFloat(), y)
            }
            pathBack.lineTo(widthPx, heightPx)
            pathBack.close()
            drawPath(
                path = pathBack,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0277BD).copy(alpha = 0.45f), Color(0xFF003764).copy(alpha = 0.75f))
                )
            )

            // Layer 2: Main wave (Shiny Indigo Aura)
            val pathMain = Path()
            pathMain.moveTo(0f, heightPx)
            for (x in 0..widthPx.toInt() step 5) {
                val angle = (x.toFloat() / widthPx) * 2.5f * PI.toFloat() + waveOffset
                val y = waterY + waveBob + sin(angle) * 13f
                pathMain.lineTo(x.toFloat(), y)
            }
            pathMain.lineTo(widthPx, heightPx)
            pathMain.close()
            drawPath(
                path = pathMain,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00B0FF).copy(alpha = 0.8f), Color(0xFF0D47A1))
                )
            )

            // Layer 3: Foreground Wave (Bright Magical Turquoise) with highest speed & yeta uta bobbing
            val pathFore = Path()
            pathFore.moveTo(0f, heightPx)
            for (x in 0..widthPx.toInt() step 5) {
                val angle = (x.toFloat() / widthPx) * 4f * PI.toFloat() - (waveOffset * 1.4f)
                val y = waterY + 6f - waveBob + sin(angle) * 8f
                pathFore.lineTo(x.toFloat(), y)
            }
            pathFore.lineTo(widthPx, heightPx)
            pathFore.close()
            drawPath(
                path = pathFore,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.9f), Color(0xFF005b64).copy(alpha = 0.95f))
                )
            )
        }

        // TWO cute dolphins playing!
        // We will make them jump in alternating schedules or simultaneously meeting in the center to build a heart shape!
        // Time triggers: loop represents 0 to 1
        
        // --- DOLPHIN 1 (Leaps Left to Right, snout flipped forward facing right) ---
        val d1Start = 0.05f
        val d1End = 0.85f
        if (jumpProgress in d1Start..d1End) {
            val p = (jumpProgress - d1Start) / (d1End - d1Start)

            // Leap path Left-to-Right
            val d1X = this@BoxWithConstraints.maxWidth * (0.05f + 0.65f * p)

            // Higher parabolic jump
            val archHeight = 90.dp
            val surfaceY = 120.dp
            val d1Y = surfaceY - (archHeight * sin(p * PI.toFloat()).coerceAtLeast(0f).toFloat())

            // Curve rotation: points up then angles down
            val d1Rotation = -50f + 100f * p

            // Splashes
            val showLeftSplash = p > 0.01f && p < 0.15f
            val showRightSplash = p > 0.85f && p < 0.99f

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                Text(
                    text = "🐬",
                    fontSize = 46.sp,
                    modifier = Modifier
                        .offset(x = d1X - 23.dp, y = d1Y - 23.dp)
                        .scale(scaleX = -1f, scaleY = 1f) // Flip horizontally so cute dolphin snout points RIGHT forward
                        .rotate(d1Rotation)
                        .testTag("jumping_dolphin_1")
                )

                if (showLeftSplash) {
                    WaterSplashEffect(
                        modifier = Modifier.offset(x = this@BoxWithConstraints.maxWidth * 0.06f, y = 100.dp),
                        scale = p / 0.15f
                    )
                }
                if (showRightSplash) {
                    WaterSplashEffect(
                        modifier = Modifier.offset(x = this@BoxWithConstraints.maxWidth * 0.69f, y = 100.dp),
                        scale = (1.0f - p) / 0.15f
                    )
                }
            }
        }

        // --- DOLPHIN 2 (Leaps Right to Left, naturally points LEFT snout) ---
        // Offset this slightly in time or place so they look like they are playing hide and seek in unison
        val d2Start = 0.15f
        val d2End = 0.95f
        if (jumpProgress in d2Start..d2End) {
            val p = (jumpProgress - d2Start) / (d2End - d2Start)

            // Leap path Right-to-Left (starts from 95% and dives to 35%)
            val d2X = this@BoxWithConstraints.maxWidth * (0.95f - 0.65f * p)

            // Dynamic arch height
            val archHeight = 80.dp
            val surfaceY = 120.dp
            val d2Y = surfaceY - (archHeight * sin(p * PI.toFloat()).coerceAtLeast(0f).toFloat())

            // Curve rotation for moving right to left
            val d2Rotation = 50f - 100f * p

            // Splashes
            val showRightSplash = p > 0.01f && p < 0.15f
            val showLeftSplash = p > 0.85f && p < 0.99f

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                Text(
                    text = "🐬",
                    fontSize = 42.sp,
                    modifier = Modifier
                        .offset(x = d2X - 21.dp, y = d2Y - 21.dp)
                        // No horizontal flip because 🐬 naturally points LEFT snout
                        .rotate(d2Rotation)
                        .testTag("jumping_dolphin_2")
                )

                if (showRightSplash) {
                    WaterSplashEffect(
                        modifier = Modifier.offset(x = this@BoxWithConstraints.maxWidth * 0.94f - 15.dp, y = 100.dp),
                        scale = p / 0.15f
                    )
                }
                if (showLeftSplash) {
                    WaterSplashEffect(
                        modifier = Modifier.offset(x = this@BoxWithConstraints.maxWidth * 0.31f - 15.dp, y = 100.dp),
                        scale = (1.0f - p) / 0.15f
                    )
                }
            }
        }
    }
}

@Composable
fun WaterSplashEffect(
    modifier: Modifier = Modifier,
    scale: Float
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val bounceOffset = (15.dp * scale)
        Text(
            text = "💧",
            fontSize = (12 * scale + 6).sp,
            modifier = Modifier.offset(y = -bounceOffset)
        )
        Text(
            text = "💦",
            fontSize = (16 * scale + 8).sp,
            modifier = Modifier.offset(y = -bounceOffset - 5.dp)
        )
        Text(
            text = "💧",
            fontSize = (12 * scale + 6).sp,
            modifier = Modifier.offset(y = -bounceOffset)
        )
    }
}
