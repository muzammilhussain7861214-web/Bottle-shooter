package com.example.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.R
import com.example.model.BottleColor
import com.example.model.BottleTarget
import com.example.model.GlassShard
import com.example.model.HitMarker
import com.example.model.LocationLevel
import com.example.model.WeatherMode
import com.example.ui.theme.BottleAmber
import com.example.ui.theme.BottleClear
import com.example.ui.theme.BottleGreen
import com.example.ui.theme.TwineRopeColor
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RangeEnvironmentCanvas(
    targets: List<BottleTarget>,
    shards: List<GlassShard>,
    hitMarkers: List<HitMarker>,
    weather: WeatherMode,
    currentLevel: LocationLevel,
    aimPointNormalized: Offset,
    lightningIntensity: Float,
    onAimChange: (Float, Float) -> Unit,
    onShoot: (x: Float, y: Float, canvasW: Float, canvasH: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backgroundBitmap = remember {
        try {
            BitmapFactory.decodeResource(context.resources, R.drawable.img_forest_background)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "weatherAnim")
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Restart),
        label = "rainAnim"
    )

    val leafDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "leafDrift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onAimChange(offset.x / size.width.toFloat(), offset.y / size.height.toFloat())
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val currentX = aimPointNormalized.x + (dragAmount.x / size.width.toFloat()) * 0.9f
                        val currentY = aimPointNormalized.y + (dragAmount.y / size.height.toFloat()) * 0.9f
                        onAimChange(currentX, currentY)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onAimChange(offset.x / size.width.toFloat(), offset.y / size.height.toFloat())
                    onShoot(offset.x, offset.y, size.width.toFloat(), size.height.toFloat())
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 3D Parallax perspective shift when aiming around
            val parallaxX = (aimPointNormalized.x - 0.5f) * -34f
            val parallaxY = (aimPointNormalized.y - 0.5f) * -22f

            // 1. Draw Location Backdrop (Level 1 can blend with resource photo; all levels have custom procedural panoramas)
            if (currentLevel == LocationLevel.DeepPineForest && backgroundBitmap != null) {
                drawImage(
                    image = backgroundBitmap,
                    dstOffset = IntOffset(parallaxX.toInt(), parallaxY.toInt()),
                    dstSize = IntSize((w + 35f).toInt(), (h + 25f).toInt())
                )
            } else {
                drawDynamicEnvironment(currentLevel, parallaxX, parallaxY, w, h)
            }

            // 2. Weather Lighting / Sky Atmosphere Layer
            drawWeatherAtmosphere(weather, lightningIntensity, w, h)

            // 3. Dynamic Location Canopy & Branches
            drawLevelBranches(currentLevel, parallaxX, parallaxY, w, h)

            // 4. Hanging Twine Cords and Bottles
            targets.forEach { target ->
                if (!target.isShattered) {
                    drawHangingTarget(target, w, h)
                }
            }

            // 5. Shattered Glass Shards
            shards.forEach { shard ->
                drawGlassShard(shard)
            }

            // 6. Autumn falling leaf particles for Autumn Woods
            if (currentLevel == LocationLevel.AutumnWoods) {
                drawFallingAutumnLeaves(leafDrift, w, h)
            }

            // 7. Precipitation (Rain streaks or Snow flurries)
            drawPrecipitation(weather, rainOffset, w, h)

            // 8. Hit Markers & Combat Text
            hitMarkers.forEach { marker ->
                drawCircle(
                    color = marker.color.copy(alpha = 0.85f),
                    radius = 16f,
                    center = Offset(marker.x, marker.y),
                    style = Stroke(width = 3f)
                )
                // Small crosshair hit tick
                drawLine(
                    color = marker.color,
                    start = Offset(marker.x - 10f, marker.y - 10f),
                    end = Offset(marker.x + 10f, marker.y + 10f),
                    strokeWidth = 2.5f
                )
                drawLine(
                    color = marker.color,
                    start = Offset(marker.x - 10f, marker.y + 10f),
                    end = Offset(marker.x + 10f, marker.y - 10f),
                    strokeWidth = 2.5f
                )
            }
        }
    }
}

private fun DrawScope.drawDynamicEnvironment(
    level: LocationLevel,
    px: Float,
    py: Float,
    w: Float,
    h: Float
) {
    // 1. Dynamic Skybox Gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = level.skyColors,
            startY = 0f,
            endY = h * 0.7f
        ),
        size = Size(w, h)
    )

    when (level) {
        LocationLevel.DeepPineForest -> {
            // Sunbeams streaming down
            val sunbeamBrush = Brush.linearGradient(
                listOf(Color(0x28FFE082), Color(0x00FFE082)),
                start = Offset(w * 0.45f + px, 0f),
                end = Offset(w * 0.8f + px, h * 0.9f)
            )
            val beamPath = Path().apply {
                moveTo(w * 0.42f + px, 0f)
                lineTo(w * 0.52f + px, 0f)
                lineTo(w * 0.95f + px, h)
                lineTo(w * 0.65f + px, h)
                close()
            }
            drawPath(beamPath, sunbeamBrush)

            // Distant mountain ridge & pines
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF283A2E), Color(0xFF3F5542), Color(0xFF4C6148)),
                    startY = h * 0.25f,
                    endY = h * 0.7f
                ),
                topLeft = Offset(px * 0.5f, h * 0.3f),
                size = Size(w, h * 0.4f)
            )

            // Dirt trail & rocky ground
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF564C3B), Color(0xFF3D3528), Color(0xFF2B251D)),
                    startY = h * 0.65f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.65f),
                size = Size(w, h * 0.35f)
            )
        }
        LocationLevel.MistyMountainEdge -> {
            // Jagged mountain peaks with snow
            val mountainPath = Path().apply {
                moveTo(0f, h * 0.55f)
                lineTo(w * 0.18f + px * 0.4f, h * 0.22f)
                lineTo(w * 0.32f + px * 0.4f, h * 0.42f)
                lineTo(w * 0.54f + px * 0.4f, h * 0.15f)
                lineTo(w * 0.72f + px * 0.4f, h * 0.38f)
                lineTo(w * 0.88f + px * 0.4f, h * 0.20f)
                lineTo(w, h * 0.48f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                mountainPath,
                Brush.verticalGradient(
                    listOf(Color(0xFF8DA3B5), Color(0xFF3B4E5D), Color(0xFF23323E)),
                    startY = h * 0.15f,
                    endY = h * 0.7f
                )
            )

            // Mountain snowcaps
            val snowcapPath = Path().apply {
                moveTo(w * 0.54f + px * 0.4f, h * 0.15f)
                lineTo(w * 0.50f + px * 0.4f, h * 0.23f)
                lineTo(w * 0.54f + px * 0.4f, h * 0.21f)
                lineTo(w * 0.58f + px * 0.4f, h * 0.24f)
                close()
            }
            drawPath(snowcapPath, Color(0xFFEDF4F9))

            // Swirling mountain cloud banks
            drawOval(
                color = Color(0x77CFDCE5),
                topLeft = Offset(px * 0.7f - 40f, h * 0.38f),
                size = Size(w * 0.75f, 90f)
            )
            drawOval(
                color = Color(0x66B0C4D3),
                topLeft = Offset(w * 0.35f + px * 0.7f, h * 0.44f),
                size = Size(w * 0.75f, 100f)
            )

            // Craggy precipice edge
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF3A4247), Color(0xFF262C30), Color(0xFF14181B)),
                    startY = h * 0.62f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.62f),
                size = Size(w, h * 0.38f)
            )
        }
        LocationLevel.AutumnWoods -> {
            // Distant warm hills
            drawOval(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF8A3B14), Color(0xFF5A220B)),
                    startY = h * 0.28f,
                    endY = h * 0.6f
                ),
                topLeft = Offset(px * 0.5f - 80f, h * 0.28f),
                size = Size(w * 1.3f, h * 0.45f)
            )

            // Layered blazing autumn tree crowns
            val autumnColors = listOf(Color(0xFFBF360C), Color(0xFFE65100), Color(0xFFFF8F00), Color(0xFFD84315))
            for (i in 0..7) {
                val cx = w * (0.08f + i * 0.13f) + px * 0.6f
                val cy = h * (0.42f + (i % 3) * 0.04f)
                drawCircle(
                    color = autumnColors[i % autumnColors.size],
                    radius = 48f,
                    center = Offset(cx, cy)
                )
            }

            // Warm leaf-strewn ground
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF6D361B), Color(0xFF4A1F0D), Color(0xFF291005)),
                    startY = h * 0.64f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.64f),
                size = Size(w, h * 0.36f)
            )
        }
        LocationLevel.OvergrownJungleRuins -> {
            // Distant ancient stepped Mayan temple silhouette
            val templePath = Path().apply {
                moveTo(w * 0.35f + px * 0.4f, h * 0.48f)
                lineTo(w * 0.40f + px * 0.4f, h * 0.48f)
                lineTo(w * 0.40f + px * 0.4f, h * 0.38f)
                lineTo(w * 0.44f + px * 0.4f, h * 0.38f)
                lineTo(w * 0.44f + px * 0.4f, h * 0.30f)
                lineTo(w * 0.56f + px * 0.4f, h * 0.30f)
                lineTo(w * 0.56f + px * 0.4f, h * 0.38f)
                lineTo(w * 0.60f + px * 0.4f, h * 0.38f)
                lineTo(w * 0.60f + px * 0.4f, h * 0.48f)
                lineTo(w * 0.68f + px * 0.4f, h * 0.48f)
                lineTo(w * 0.72f + px * 0.4f, h * 0.65f)
                lineTo(w * 0.30f + px * 0.4f, h * 0.65f)
                close()
            }
            drawPath(templePath, Color(0xFF1E3322))

            // Humid tropical mist band
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0x33A3C9A8), Color(0x11658D6B), Color.Transparent),
                    startY = h * 0.3f,
                    endY = h * 0.6f
                ),
                topLeft = Offset(0f, h * 0.3f),
                size = Size(w, h * 0.3f)
            )

            // Ancient carved monolith stelae
            drawRoundRect(
                color = Color(0xFF283B2C),
                topLeft = Offset(w * 0.12f + px * 0.8f, h * 0.46f),
                size = Size(36f, 130f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFF223526),
                topLeft = Offset(w * 0.82f + px * 0.8f, h * 0.48f),
                size = Size(40f, 120f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Deep jungle floor
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF1B2E1C), Color(0xFF132214), Color(0xFF0C160D)),
                    startY = h * 0.63f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.63f),
                size = Size(w, h * 0.37f)
            )
        }
    }
}

private fun DrawScope.drawLevelBranches(
    level: LocationLevel,
    px: Float,
    py: Float,
    w: Float,
    h: Float
) {
    val barkColor1 = when (level) {
        LocationLevel.DeepPineForest -> Color(0xFF261D16)
        LocationLevel.MistyMountainEdge -> Color(0xFF2A2E33)
        LocationLevel.AutumnWoods -> Color(0xFF381F12)
        LocationLevel.OvergrownJungleRuins -> Color(0xFF1A2616)
    }
    val barkColor2 = when (level) {
        LocationLevel.DeepPineForest -> Color(0xFF3E2F23)
        LocationLevel.MistyMountainEdge -> Color(0xFF454C54)
        LocationLevel.AutumnWoods -> Color(0xFF54311C)
        LocationLevel.OvergrownJungleRuins -> Color(0xFF293B22)
    }

    val barkBrush = Brush.linearGradient(
        listOf(barkColor1, barkColor2, barkColor1),
        start = Offset(w * 0.5f + px, 0f),
        end = Offset(w * 0.5f + px, h * 0.7f)
    )

    // Central ancient trunk
    val trunkPath = Path().apply {
        moveTo(w * 0.42f + px, h * 0.85f)
        cubicTo(w * 0.44f + px, h * 0.55f, w * 0.46f + px, h * 0.35f, w * 0.48f + px, h * 0.15f)
        lineTo(w * 0.54f + px, h * 0.15f)
        cubicTo(w * 0.56f + px, h * 0.35f, w * 0.58f + px, h * 0.55f, w * 0.60f + px, h * 0.85f)
        close()
    }
    drawPath(trunkPath, barkBrush)

    // Left main branch (holding targets 1, 2, 3, 4)
    val leftBranch = Path().apply {
        moveTo(w * 0.49f + px, h * 0.28f + py * 0.5f)
        cubicTo(w * 0.38f + px, h * 0.25f + py * 0.5f, w * 0.25f + px, h * 0.23f + py * 0.5f, w * 0.10f + px, h * 0.20f + py * 0.5f)
        lineTo(w * 0.10f + px, h * 0.23f + py * 0.5f)
        cubicTo(w * 0.25f + px, h * 0.26f + py * 0.5f, w * 0.38f + px, h * 0.28f + py * 0.5f, w * 0.49f + px, h * 0.32f + py * 0.5f)
        close()
    }
    drawPath(leftBranch, barkBrush)

    // Right main branch (holding targets 5, 6, 7)
    val rightBranch = Path().apply {
        moveTo(w * 0.52f + px, h * 0.24f + py * 0.5f)
        cubicTo(w * 0.62f + px, h * 0.22f + py * 0.5f, w * 0.75f + px, h * 0.23f + py * 0.5f, w * 0.90f + px, h * 0.22f + py * 0.5f)
        lineTo(w * 0.90f + px, h * 0.25f + py * 0.5f)
        cubicTo(w * 0.75f + px, h * 0.26f + py * 0.5f, w * 0.62f + px, h * 0.26f + py * 0.5f, w * 0.52f + px, h * 0.28f + py * 0.5f)
        close()
    }
    drawPath(rightBranch, barkBrush)

    // Location specific foliage clusters
    val foliageColor1 = when (level) {
        LocationLevel.DeepPineForest -> Color(0xEE2A3B24)
        LocationLevel.MistyMountainEdge -> Color(0xEE334852)
        LocationLevel.AutumnWoods -> Color(0xFFD84315)
        LocationLevel.OvergrownJungleRuins -> Color(0xEE1E401A)
    }
    val foliageColor2 = when (level) {
        LocationLevel.DeepPineForest -> Color(0xDD3A4E30)
        LocationLevel.MistyMountainEdge -> Color(0xDD4A606D)
        LocationLevel.AutumnWoods -> Color(0xFFFF8F00)
        LocationLevel.OvergrownJungleRuins -> Color(0xDD2D5E26)
    }

    for (i in 0..8) {
        val cx = w * (0.1f + i * 0.1f) + px
        val cy = h * (0.08f + (i % 3) * 0.05f) + py * 0.4f
        drawCircle(color = if (i % 2 == 0) foliageColor1 else foliageColor2, radius = 55f, center = Offset(cx, cy))
    }

    // Additional hanging vines for jungle
    if (level == LocationLevel.OvergrownJungleRuins) {
        for (i in 1..5) {
            val vx = w * (0.15f * i) + px
            val vyStart = h * 0.22f
            drawLine(
                color = Color(0xCC294320),
                start = Offset(vx, vyStart),
                end = Offset(vx + 8f, vyStart + 90f + (i % 3) * 35f),
                strokeWidth = 3.5f
            )
        }
    }
}

private fun DrawScope.drawFallingAutumnLeaves(drift: Float, w: Float, h: Float) {
    val leafColors = listOf(Color(0xFFE65100), Color(0xFFFF8F00), Color(0xFFBF360C), Color(0xFFFFB300))
    for (i in 0..11) {
        val speed = 0.6f + (i % 4) * 0.2f
        val startX = w * ((i * 0.09f + 0.05f) % 1f)
        val progress = (drift * speed + i * 0.08f) % 1f
        val ly = progress * h
        val lx = startX + sin(progress * 8f + i) * 30f

        drawOval(
            color = leafColors[i % leafColors.size].copy(alpha = 0.85f),
            topLeft = Offset(lx, ly),
            size = Size(10f, 6f)
        )
    }
}

private fun DrawScope.drawHangingTarget(target: BottleTarget, w: Float, h: Float) {
    val anchorX = target.branchAnchorX * w
    val anchorY = target.branchAnchorY * h

    val angle = target.currentAngle
    val rad = Math.toRadians(angle.toDouble())
    val ropeLen = target.ropeLength

    // Rope tie / knot at branch
    drawCircle(
        color = Color(0xFF8A7B54),
        radius = 5f,
        center = Offset(anchorX, anchorY)
    )

    // Calculate dynamic position of bottle top
    val bottleX = (anchorX + sin(rad) * ropeLen).toFloat()
    val bottleY = (anchorY + cos(rad) * ropeLen).toFloat() + target.fallYOffset

    // 1. Draw Twine Rope (if not cut)
    if (!target.isRopeCut) {
        drawLine(
            color = TwineRopeColor,
            start = Offset(anchorX, anchorY),
            end = Offset(bottleX, bottleY),
            strokeWidth = 2.8f
        )
        // Wrapped twine knot around bottle neck
        drawRoundRect(
            color = Color(0xFF9E8A5E),
            topLeft = Offset(bottleX - 6f, bottleY - 2f),
            size = Size(12f, 8f),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }

    // 2. Draw Realistic Glass Bottle
    rotate(angle, pivot = Offset(bottleX, bottleY)) {
        val bottleColor = when (target.color) {
            BottleColor.Amber -> BottleAmber
            BottleColor.Emerald -> BottleGreen
            BottleColor.Clear -> BottleClear
        }

        // Glass gradient (highlights & core tint)
        val glassBrush = Brush.horizontalGradient(
            colors = listOf(
                bottleColor.copy(alpha = 0.75f),
                bottleColor.copy(alpha = 0.95f),
                Color(0x88FFFFFF), // Specular highlight
                bottleColor.copy(alpha = 0.85f),
                bottleColor.copy(alpha = 0.6f)
            ),
            startX = bottleX - 14f,
            endX = bottleX + 14f
        )

        // Bottle Neck
        drawRoundRect(
            brush = glassBrush,
            topLeft = Offset(bottleX - 5f, bottleY + 5f),
            size = Size(10f, 22f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Bottle Lip / Crown
        drawRoundRect(
            color = bottleColor,
            topLeft = Offset(bottleX - 6.5f, bottleY + 4f),
            size = Size(13f, 5f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Bottle Shoulders & Body
        val bodyPath = Path().apply {
            moveTo(bottleX - 5f, bottleY + 27f)
            cubicTo(bottleX - 14f, bottleY + 34f, bottleX - 15f, bottleY + 40f, bottleX - 15f, bottleY + 45f)
            lineTo(bottleX - 15f, bottleY + 95f)
            cubicTo(bottleX - 15f, bottleY + 100f, bottleX + 15f, bottleY + 100f, bottleX + 15f, bottleY + 95f)
            lineTo(bottleX + 15f, bottleY + 45f)
            cubicTo(bottleX + 15f, bottleY + 40f, bottleX + 14f, bottleY + 34f, bottleX + 5f, bottleY + 27f)
            close()
        }
        drawPath(bodyPath, glassBrush)

        // Vintage Paper Label Band around belly
        drawRoundRect(
            color = Color(0xCCECE3D2),
            topLeft = Offset(bottleX - 14f, bottleY + 52f),
            size = Size(28f, 30f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Label border & vintage crest text simulation
        drawRoundRect(
            color = Color(0xFF6E5E4E),
            topLeft = Offset(bottleX - 12f, bottleY + 54f),
            size = Size(24f, 26f),
            style = Stroke(width = 1.2f),
            cornerRadius = CornerRadius(1f, 1f)
        )
        drawLine(
            color = Color(0xFF5A4D40),
            start = Offset(bottleX - 9f, bottleY + 67f),
            end = Offset(bottleX + 9f, bottleY + 67f),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawGlassShard(shard: GlassShard) {
    rotate(shard.rotation, pivot = Offset(shard.x, shard.y)) {
        val shardPath = Path().apply {
            moveTo(shard.x, shard.y - shard.height * 0.5f)
            lineTo(shard.x + shard.width * 0.5f, shard.y + shard.height * 0.3f)
            lineTo(shard.x - shard.width * 0.4f, shard.y + shard.height * 0.5f)
            close()
        }
        drawPath(shardPath, shard.color.copy(alpha = shard.alpha))
        // Specular glint
        drawPath(
            shardPath,
            Color.White.copy(alpha = shard.alpha * 0.7f),
            style = Stroke(width = 1.2f)
        )
    }
}

private fun DrawScope.drawPrecipitation(weather: WeatherMode, animOffset: Float, w: Float, h: Float) {
    when (weather) {
        WeatherMode.OvercastRain, WeatherMode.ThunderstormWind -> {
            val isStorm = weather == WeatherMode.ThunderstormWind
            val count = if (isStorm) 90 else 45
            val rainColor = if (isStorm) Color(0x88CCDDE8) else Color(0x55B0BEC5)
            val angleSlope = if (isStorm) 22f else 8f

            for (i in 0 until count) {
                val seedX = (i * 37f) % w
                val seedY = ((i * 53f) + (animOffset * h * 2f)) % h
                val len = if (isStorm) 38f else 24f

                drawLine(
                    color = rainColor,
                    start = Offset(seedX, seedY),
                    end = Offset(seedX - angleSlope, seedY + len),
                    strokeWidth = if (isStorm) 2.2f else 1.4f
                )
            }
        }
        WeatherMode.SnowflurriesFog -> {
            val count = 55
            for (i in 0 until count) {
                val seedX = ((i * 29f) + sin((animOffset * 2f + i) * Math.PI.toFloat()) * 30f) % w
                val seedY = ((i * 47f) + (animOffset * h * 0.8f)) % h
                val r = (i % 4 + 2).toFloat()
                drawCircle(
                    color = Color(0xCCFFFFFF),
                    radius = r,
                    center = Offset(seedX, seedY)
                )
            }
        }
        else -> { /* Clear Day has no precipitation */ }
    }
}

private fun DrawScope.drawWeatherAtmosphere(
    weather: WeatherMode,
    lightningIntensity: Float,
    w: Float,
    h: Float
) {
    when (weather) {
        WeatherMode.OvercastRain -> {
            drawRect(
                color = Color(0x35121E24),
                size = Size(w, h)
            )
        }
        WeatherMode.ThunderstormWind -> {
            drawRect(
                color = Color(0x450C161D),
                size = Size(w, h)
            )
            if (lightningIntensity > 0.05f) {
                drawRect(
                    color = Color.White.copy(alpha = (lightningIntensity * 0.75f).coerceIn(0f, 0.85f)),
                    size = Size(w, h)
                )
            }
        }
        WeatherMode.SnowflurriesFog -> {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0x30CFD8DC), Color(0x55ECEFF1)),
                    startY = 0f,
                    endY = h
                ),
                size = Size(w, h)
            )
        }
        WeatherMode.ClearDay -> {
            drawRect(
                brush = Brush.radialGradient(
                    listOf(Color(0x18FFE082), Color(0x00000000)),
                    center = Offset(w * 0.5f, h * 0.2f),
                    radius = w * 0.8f
                ),
                size = Size(w, h)
            )
        }
    }
}
