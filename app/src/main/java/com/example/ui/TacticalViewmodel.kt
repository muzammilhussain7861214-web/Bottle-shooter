package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.Weapon
import com.example.ui.theme.ReticleRed
import com.example.ui.theme.ReticleRedGlow
import com.example.ui.theme.TacticalOlive
import com.example.ui.theme.TacticalOliveDark
import com.example.ui.theme.TacticalOrange
import kotlin.random.Random

@Composable
fun TacticalViewmodel(
    weapon: Weapon,
    recoilKick: Float,
    isAdsActive: Boolean,
    aimPointNormalized: Offset = Offset(0.5f, 0.45f),
    modifier: Modifier = Modifier
) {
    val adsTransition by animateFloatAsState(
        targetValue = if (isAdsActive) 1f else 0f,
        animationSpec = spring(stiffness = 350f, dampingRatio = 0.85f),
        label = "adsTransition"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Calculate aiming sway & perspective offsets
            val aimOffsetX = aimPointNormalized.x - 0.5f
            val aimOffsetY = aimPointNormalized.y - 0.45f

            // Base position of hands and gun: bottom right in Hipfire, dynamically sway towards aim point
            val hipX = w * 0.68f + aimOffsetX * w * 0.26f
            val hipY = h * 0.80f + aimOffsetY * h * 0.16f

            // In ADS, align the optic center directly with the dynamic aim point
            val adsX = w * aimPointNormalized.x
            val adsY = (h * aimPointNormalized.y) + (h * 0.18f)

            val currentBaseX = hipX + (adsX - hipX) * adsTransition
            val currentBaseY = hipY + (adsY - hipY) * adsTransition

            // Realistic wrist tilt & viewmodel sway angle
            val swayRot = (aimOffsetX * 16f) * (1f - adsTransition * 0.7f)

            // Recoil offset & rotation
            val recoilY = -recoilKick * 18f
            val recoilRot = -recoilKick * 4.5f + swayRot

            rotate(recoilRot, pivot = Offset(currentBaseX, currentBaseY)) {
                when (weapon.id) {
                    "custom_pistol" -> drawCustomPistol(
                        baseX = currentBaseX,
                        baseY = currentBaseY + recoilY,
                        recoil = recoilKick,
                        ads = adsTransition
                    )
                    "revolver" -> drawRevolver(
                        baseX = currentBaseX,
                        baseY = currentBaseY + recoilY,
                        recoil = recoilKick,
                        ads = adsTransition
                    )
                    "smg" -> drawSubmachineGun(
                        baseX = currentBaseX,
                        baseY = currentBaseY + recoilY,
                        recoil = recoilKick,
                        ads = adsTransition
                    )
                    "shotgun" -> drawShotgun(
                        baseX = currentBaseX,
                        baseY = currentBaseY + recoilY,
                        recoil = recoilKick,
                        ads = adsTransition
                    )
                    "assault_rifle" -> drawAssaultRifle(
                        baseX = currentBaseX,
                        baseY = currentBaseY + recoilY,
                        recoil = recoilKick,
                        ads = adsTransition
                    )
                    "sniper_rifle" -> drawSniperRifle(
                        baseX = currentBaseX,
                        baseY = currentBaseY + recoilY,
                        recoil = recoilKick,
                        ads = adsTransition
                    )
                    else -> drawCustomPistol(
                        baseX = currentBaseX,
                        baseY = currentBaseY + recoilY,
                        recoil = recoilKick,
                        ads = adsTransition
                    )
                }
            }

            // Draw Muzzle Flash if recoil is high
            if (recoilKick > 0.8f) {
                drawMuzzleFlash(
                    x = currentBaseX - (if (weapon.id == "custom_pistol") 18f else 12f),
                    y = currentBaseY + recoilY - 260f,
                    intensity = recoilKick
                )
            }
        }
    }
}

private fun DrawScope.drawCustomPistol(baseX: Float, baseY: Float, recoil: Float, ads: Float) {
    val gloveBrush = Brush.linearGradient(
        colors = listOf(TacticalOlive, TacticalOliveDark, Color(0xFF1B2318)),
        start = Offset(baseX - 120f, baseY),
        end = Offset(baseX + 120f, baseY + 200f)
    )

    // 1. Hands & Tactical Gloves
    // Left hand supporting bottom grip
    val leftGlovePath = Path().apply {
        moveTo(baseX - 110f, baseY + 180f)
        cubicTo(baseX - 130f, baseY + 100f, baseX - 60f, baseY + 40f, baseX - 15f, baseY + 60f)
        lineTo(baseX - 10f, baseY + 120f)
        cubicTo(baseX - 40f, baseY + 160f, baseX - 80f, baseY + 210f, baseX - 110f, baseY + 240f)
        close()
    }
    drawPath(leftGlovePath, gloveBrush)

    // Right hand main grip
    val rightGlovePath = Path().apply {
        moveTo(baseX + 130f, baseY + 220f)
        cubicTo(baseX + 110f, baseY + 120f, baseX + 50f, baseY + 50f, baseX + 10f, baseY + 65f)
        lineTo(baseX + 15f, baseY + 135f)
        cubicTo(baseX + 50f, baseY + 180f, baseX + 90f, baseY + 230f, baseX + 120f, baseY + 260f)
        close()
    }
    drawPath(rightGlovePath, gloveBrush)

    // Glove straps and knuckle armor pads
    drawRoundRect(
        color = Color(0xFF1E281C),
        topLeft = Offset(baseX - 45f, baseY + 70f),
        size = Size(90f, 28f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // 2. Pistol Lower Frame & Trigger Guard
    drawRoundRect(
        color = Color(0xFF1C1F1D),
        topLeft = Offset(baseX - 22f, baseY - 20f),
        size = Size(44f, 130f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Trigger guard
    val guardPath = Path().apply {
        moveTo(baseX - 12f, baseY + 20f)
        cubicTo(baseX - 12f, baseY + 65f, baseX - 30f, baseY + 65f, baseX - 30f, baseY + 20f)
        close()
    }
    drawPath(guardPath, Color(0xFF151816), style = Stroke(width = 6f))

    // Trigger
    drawRoundRect(
        color = Color(0xFF323A35),
        topLeft = Offset(baseX - 22f, baseY + 30f),
        size = Size(6f, 22f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // 3. Pistol Slide (Glock style) - slide moves back on recoil
    val slideBlowback = recoil * 24f
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF333A36), Color(0xFF1C201E))),
        topLeft = Offset(baseX - 20f, baseY - 120f + slideBlowback),
        size = Size(40f, 125f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Slide serrations
    for (i in 0..4) {
        drawLine(
            color = Color(0xFF141715),
            start = Offset(baseX - 18f, baseY - 40f + (i * 8f) + slideBlowback),
            end = Offset(baseX - 8f, baseY - 40f + (i * 8f) + slideBlowback),
            strokeWidth = 3f
        )
    }

    // 4. Threaded Barrel & Tactical Suppressor
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(Color(0xFF202522), Color(0xFF404844), Color(0xFF191D1A))),
        topLeft = Offset(baseX - 18f, baseY - 260f),
        size = Size(36f, 145f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Suppressor ribs / knurling bands
    for (i in 0..3) {
        drawLine(
            color = Color(0xFF151816),
            start = Offset(baseX - 18f, baseY - 240f + (i * 32f)),
            end = Offset(baseX + 18f, baseY - 240f + (i * 32f)),
            strokeWidth = 3f
        )
    }

    // 5. RMR Red Dot Sight
    // Sight body
    drawRoundRect(
        color = Color(0xFF181C1A),
        topLeft = Offset(baseX - 17f, baseY - 112f + slideBlowback),
        size = Size(34f, 48f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Lens aperture
    drawRoundRect(
        color = Color(0x3300E5FF), // Anti-reflective glass coating
        topLeft = Offset(baseX - 12f, baseY - 105f + slideBlowback),
        size = Size(24f, 26f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // RMR Illuminated Red Dot
    drawCircle(
        color = ReticleRedGlow,
        radius = 8f,
        center = Offset(baseX, baseY - 92f + slideBlowback)
    )
    drawCircle(
        color = ReticleRed,
        radius = 3.5f,
        center = Offset(baseX, baseY - 92f + slideBlowback)
    )
}

private fun DrawScope.drawRevolver(baseX: Float, baseY: Float, recoil: Float, ads: Float) {
    // Steel silver finish Revolver
    val steelBrush = Brush.horizontalGradient(listOf(Color(0xFF687278), Color(0xFFC0CCD4), Color(0xFF4A5258)))

    // Revolver Cylinder
    drawRoundRect(
        brush = steelBrush,
        topLeft = Offset(baseX - 25f, baseY - 65f),
        size = Size(50f, 65f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    // Flutes on cylinder
    for (offset in listOf(-14f, 0f, 14f)) {
        drawRoundRect(
            color = Color(0xFF31363A),
            topLeft = Offset(baseX + offset - 3f, baseY - 60f),
            size = Size(6f, 55f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }

    // Heavy long barrel
    drawRoundRect(
        brush = steelBrush,
        topLeft = Offset(baseX - 16f, baseY - 230f),
        size = Size(32f, 170f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Top rib
    drawLine(
        color = Color(0xFF353C40),
        start = Offset(baseX, baseY - 230f),
        end = Offset(baseX, baseY - 65f),
        strokeWidth = 6f
    )
    // Hammer
    drawRoundRect(
        color = Color(0xFF2B3033),
        topLeft = Offset(baseX - 8f, baseY - 10f),
        size = Size(16f, 28f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Wood grip
    val woodBrush = Brush.linearGradient(listOf(Color(0xFF5D381E), Color(0xFF8B5A2B), Color(0xFF3E2412)))
    drawRoundRect(
        brush = woodBrush,
        topLeft = Offset(baseX - 24f, baseY + 15f),
        size = Size(48f, 130f),
        cornerRadius = CornerRadius(8f, 8f)
    )
}

private fun DrawScope.drawSubmachineGun(baseX: Float, baseY: Float, recoil: Float, ads: Float) {
    // Tactical SMG (MP5/MPX style)
    drawRoundRect(
        color = Color(0xFF1E2220),
        topLeft = Offset(baseX - 26f, baseY - 130f),
        size = Size(52f, 150f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Curved magazine
    val magPath = Path().apply {
        moveTo(baseX - 12f, baseY + 20f)
        cubicTo(baseX - 16f, baseY + 80f, baseX - 40f, baseY + 150f, baseX - 55f, baseY + 180f)
        lineTo(baseX - 25f, baseY + 185f)
        cubicTo(baseX - 10f, baseY + 140f, baseX + 10f, baseY + 80f, baseX + 12f, baseY + 20f)
        close()
    }
    drawPath(magPath, Color(0xFF151816))

    // Barrel & Flash Hider
    drawRoundRect(
        color = Color(0xFF282F2B),
        topLeft = Offset(baseX - 16f, baseY - 240f),
        size = Size(32f, 115f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Optic
    drawRoundRect(
        color = Color(0xFF151816),
        topLeft = Offset(baseX - 16f, baseY - 120f),
        size = Size(32f, 50f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawCircle(
        color = ReticleRed,
        radius = 3.5f,
        center = Offset(baseX, baseY - 95f)
    )
}

private fun DrawScope.drawShotgun(baseX: Float, baseY: Float, recoil: Float, ads: Float) {
    // Shotgun Barrel (thick dual tubes)
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(Color(0xFF2C322F), Color(0xFF4C5550), Color(0xFF222724))),
        topLeft = Offset(baseX - 20f, baseY - 270f),
        size = Size(40f, 210f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Magazine tube under barrel
    drawRoundRect(
        color = Color(0xFF222724),
        topLeft = Offset(baseX - 16f, baseY - 240f),
        size = Size(32f, 170f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Pump grip (ribbed)
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(Color(0xFF5D4037), Color(0xFF8D6E63), Color(0xFF4E342E))),
        topLeft = Offset(baseX - 24f, baseY - 160f + recoil * 30f),
        size = Size(48f, 75f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Brass bead sight
    drawCircle(color = Color(0xFFFFD54F), radius = 5f, center = Offset(baseX, baseY - 265f))
}

private fun DrawScope.drawAssaultRifle(baseX: Float, baseY: Float, recoil: Float, ads: Float) {
    // M4 Carbine / AR style
    // Receiver
    drawRoundRect(
        color = Color(0xFF1E2220),
        topLeft = Offset(baseX - 24f, baseY - 90f),
        size = Size(48f, 140f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Quad Rail Handguard
    drawRoundRect(
        color = Color(0xFF2B332E),
        topLeft = Offset(baseX - 20f, baseY - 230f),
        size = Size(40f, 145f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Extended Barrel & Birdcage Flash Hider
    drawRoundRect(
        color = Color(0xFF1B201D),
        topLeft = Offset(baseX - 10f, baseY - 290f),
        size = Size(20f, 65f),
        cornerRadius = CornerRadius(2f, 2f)
    )
    // Holographic Sight (EOTech style box)
    drawRoundRect(
        color = Color(0xFF141715),
        topLeft = Offset(baseX - 22f, baseY - 135f),
        size = Size(44f, 55f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Red Holo Reticle
    drawCircle(
        color = ReticleRedGlow,
        radius = 12f,
        center = Offset(baseX, baseY - 110f),
        style = Stroke(width = 2f)
    )
    drawCircle(
        color = ReticleRed,
        radius = 3f,
        center = Offset(baseX, baseY - 110f)
    )
}

private fun DrawScope.drawSniperRifle(baseX: Float, baseY: Float, recoil: Float, ads: Float) {
    // Long high-caliber Precision Rifle
    drawRoundRect(
        color = Color(0xFF202622),
        topLeft = Offset(baseX - 18f, baseY - 110f),
        size = Size(36f, 160f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Extra long fluted heavy barrel
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(Color(0xFF1C221F), Color(0xFF454E48), Color(0xFF191D1A))),
        topLeft = Offset(baseX - 12f, baseY - 320f),
        size = Size(24f, 215f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Muzzle Brake with side gas ports
    drawRoundRect(
        color = Color(0xFF141715),
        topLeft = Offset(baseX - 18f, baseY - 345f),
        size = Size(36f, 30f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Large 8x Optical Scope Tube
    drawRoundRect(
        color = Color(0xFF181D1A),
        topLeft = Offset(baseX - 22f, baseY - 170f),
        size = Size(44f, 110f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    // Scope Lens
    drawCircle(
        color = Color(0x6640C4FF),
        radius = 18f,
        center = Offset(baseX, baseY - 145f)
    )
    // Sniper crosshairs in scope
    drawLine(
        color = Color(0xFF111111),
        start = Offset(baseX - 18f, baseY - 145f),
        end = Offset(baseX + 18f, baseY - 145f),
        strokeWidth = 1.5f
    )
    drawLine(
        color = Color(0xFF111111),
        start = Offset(baseX, baseY - 163f),
        end = Offset(baseX, baseY - 127f),
        strokeWidth = 1.5f
    )
}

private fun DrawScope.drawMuzzleFlash(x: Float, y: Float, intensity: Float) {
    val flashRadius = (35f + intensity * 25f).coerceIn(30f, 85f)
    // Core yellow/white starburst
    drawCircle(
        color = Color(0xFFFFF9C4),
        radius = flashRadius * 0.45f,
        center = Offset(x, y)
    )
    drawCircle(
        color = Color(0xAAFF9800),
        radius = flashRadius * 0.85f,
        center = Offset(x, y)
    )
    drawCircle(
        color = Color(0x44FF5722),
        radius = flashRadius * 1.3f,
        center = Offset(x, y)
    )
    // Spike flares
    for (i in 0..5) {
        val angle = (i * 60f + Random.nextFloat() * 20f) * (Math.PI.toFloat() / 180f)
        val spikeLen = flashRadius * (1.2f + Random.nextFloat() * 0.6f)
        drawLine(
            color = Color(0xFFFFCC80),
            start = Offset(x, y),
            end = Offset(x + kotlin.math.cos(angle) * spikeLen, y + kotlin.math.sin(angle) * spikeLen),
            strokeWidth = 4f
        )
    }
}
