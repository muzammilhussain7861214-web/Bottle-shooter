package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Weapon
import com.example.ui.theme.TacticalOrange
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeaponWheelOverlay(
    isOpen: Boolean,
    currentWeapon: Weapon,
    onSelectWeapon: (Weapon) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f),
        modifier = modifier.fillMaxSize()
    ) {
        val weapons = Weapon.ALL // 6 weapons in exact clockwise wheel order
        var hoveredIndex by remember(currentWeapon) {
            mutableStateOf(weapons.indexOfFirst { it.id == currentWeapon.id }.coerceAtLeast(0))
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC0B0E0C))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                        if (dist in 60f..380f) {
                            var angle = atan2(dy, dx) * (180f / Math.PI.toFloat())
                            // Normalize 0 to 360 where -90 (top) is segment 0
                            angle = (angle + 90f + 30f + 360f) % 360f
                            val index = (angle / 60f).toInt().coerceIn(0, 5)
                            hoveredIndex = index
                            onSelectWeapon(weapons[index])
                        } else if (dist > 400f) {
                            onClose()
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = change.position.x - cx
                        val dy = change.position.y - cy
                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                        if (dist in 60f..380f) {
                            var angle = atan2(dy, dx) * (180f / Math.PI.toFloat())
                            angle = (angle + 90f + 30f + 360f) % 360f
                            val index = (angle / 60f).toInt().coerceIn(0, 5)
                            hoveredIndex = index
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val wheelRadiusPx = 340f
            val innerRadiusPx = 100f

            // 1. Draw Circular Radial Wheel Geometry
            Canvas(modifier = Modifier.size(360.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.width / 2f * 0.95f
                val innerR = radius * 0.36f

                // Outer and Inner Rings
                drawCircle(
                    color = Color(0x66FFFFFF),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.5f)
                )
                drawCircle(
                    color = Color(0x88FFFFFF),
                    radius = innerR,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2f)
                )

                // Draw 6 Sectors (60 degrees each)
                for (i in 0 until 6) {
                    val startAngle = -90f - 30f + (i * 60f)
                    val isSelected = i == hoveredIndex
                    val sectorColor = if (isSelected) {
                        TacticalOrange.copy(alpha = 0.35f)
                    } else {
                        Color(0x551E2822)
                    }

                    // Wedge Arc
                    drawArc(
                        color = sectorColor,
                        startAngle = startAngle,
                        sweepAngle = 60f,
                        useCenter = true,
                        topLeft = Offset(cx - radius, cy - radius),
                        size = Size(radius * 2, radius * 2)
                    )

                    // Sector Separator Lines
                    val sepAngleRad = Math.toRadians(startAngle.toDouble())
                    val x1 = cx + cos(sepAngleRad).toFloat() * innerR
                    val y1 = cy + sin(sepAngleRad).toFloat() * innerR
                    val x2 = cx + cos(sepAngleRad).toFloat() * radius
                    val y2 = cy + sin(sepAngleRad).toFloat() * radius

                    drawLine(
                        color = Color(0x66FFFFFF),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 1.5f
                    )

                    // Highlight border on active segment
                    if (isSelected) {
                        drawArc(
                            color = TacticalOrange,
                            startAngle = startAngle,
                            sweepAngle = 60f,
                            useCenter = false,
                            topLeft = Offset(cx - radius, cy - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = 4f, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center cutout hole
                drawCircle(
                    color = Color(0xFF0F1512),
                    radius = innerR - 2f,
                    center = Offset(cx, cy)
                )
            }

            // 2. Wheel Weapon Labels & Buttons placed at each sector
            val labelRadiusDp = 118.dp
            for (i in 0 until 6) {
                val weapon = weapons[i]
                val angleDeg = -90f + (i * 60f)
                val rad = Math.toRadians(angleDeg.toDouble())
                val offsetX = (cos(rad) * labelRadiusDp.value).dp
                val offsetY = (sin(rad) * labelRadiusDp.value).dp
                val isSelected = i == hoveredIndex

                Box(
                    modifier = Modifier
                        .offset(x = offsetX, y = offsetY)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) TacticalOrange.copy(alpha = 0.25f) else Color(0x66101713))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) TacticalOrange else Color(0x444A594E),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            hoveredIndex = i
                            onSelectWeapon(weapon)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Visual Preview Icon
                        WeaponSilhouetteIcon(
                            weaponId = weapon.id,
                            isSelected = isSelected,
                            modifier = Modifier.size(width = 38.dp, height = 18.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = weapon.displayName,
                            color = if (isSelected) TacticalOrange else Color(0xFFE5EAE6),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${weapon.magazineCapacity} RDS",
                            color = if (isSelected) Color(0xFFFFCC80) else Color(0xFF88998C),
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 3. Center Target Indicator & Selected Weapon Overview
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .size(105.dp)
                    .clickable { onSelectWeapon(weapons[hoveredIndex]) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                WeaponSilhouetteIcon(
                    weaponId = weapons[hoveredIndex].id,
                    isSelected = true,
                    modifier = Modifier.size(width = 46.dp, height = 22.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "EQUIP",
                    color = TacticalOrange,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
            }

            // 4. Bottom Info Banner & Stats Panel
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val sel = weapons[hoveredIndex]
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xEE101613))
                        .border(1.dp, TacticalOrange.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = sel.displayName,
                            color = TacticalOrange,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${sel.category.uppercase()} • ${sel.description}",
                            color = Color(0xFFC5D1C8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Stats Meters
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatMeter(label = "DMG", value = (sel.damage / 250f).coerceIn(0.1f, 1f))
                            StatMeter(label = "RATE", value = (1000f / sel.fireRateMs / 10f).coerceIn(0.1f, 1f))
                            StatMeter(label = "RECOIL", value = (sel.recoilPitch / 7f).coerceIn(0.1f, 1f))
                            StatMeter(label = "ZOOM", value = ((sel.adsZoom - 1f) / 2.2f).coerceIn(0.1f, 1f))
                        }
                    }
                }
            }

            // Top Close Button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp)
                    .background(Color(0x66000000), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Weapon Wheel",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun WeaponSilhouetteIcon(
    weaponId: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val tint = if (isSelected) TacticalOrange else Color(0xFFCCD6CF)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (weaponId) {
            "custom_pistol" -> {
                // Suppressed Tactical Pistol
                drawRect(tint, Offset(w * 0.35f, h * 0.30f), Size(w * 0.35f, h * 0.22f))
                drawRoundRect(tint, Offset(w * 0.70f, h * 0.26f), Size(w * 0.28f, h * 0.30f), CornerRadius(2f, 2f))
                drawRect(tint, Offset(w * 0.40f, h * 0.14f), Size(w * 0.16f, h * 0.16f))
                drawRect(tint, Offset(w * 0.22f, h * 0.45f), Size(w * 0.20f, h * 0.45f))
                drawLine(tint, Offset(w * 0.42f, h * 0.55f), Offset(w * 0.50f, h * 0.65f), 1.8f)
            }
            "revolver" -> {
                // Heavy Magnum Revolver
                drawRect(tint, Offset(w * 0.45f, h * 0.32f), Size(w * 0.48f, h * 0.18f))
                drawRoundRect(tint, Offset(w * 0.30f, h * 0.24f), Size(w * 0.20f, h * 0.36f), CornerRadius(3f, 3f))
                drawRoundRect(tint, Offset(w * 0.12f, h * 0.42f), Size(w * 0.22f, h * 0.50f), CornerRadius(4f, 4f))
                drawRect(tint, Offset(w * 0.24f, h * 0.18f), Size(w * 0.08f, h * 0.14f))
            }
            "shotgun" -> {
                // Tactical Pump Shotgun
                drawRect(tint, Offset(w * 0.35f, h * 0.32f), Size(w * 0.60f, h * 0.14f))
                drawRect(tint.copy(alpha = 0.8f), Offset(w * 0.45f, h * 0.48f), Size(w * 0.24f, h * 0.16f))
                drawRect(tint, Offset(w * 0.22f, h * 0.28f), Size(w * 0.20f, h * 0.26f))
                drawRoundRect(tint, Offset(w * 0.02f, h * 0.36f), Size(w * 0.22f, h * 0.32f), CornerRadius(2f, 2f))
            }
            "smg" -> {
                // Submachine Gun
                drawRect(tint, Offset(w * 0.25f, h * 0.28f), Size(w * 0.55f, h * 0.24f))
                drawRect(tint, Offset(w * 0.80f, h * 0.34f), Size(w * 0.12f, h * 0.12f))
                drawRect(tint, Offset(w * 0.48f, h * 0.52f), Size(w * 0.12f, h * 0.44f))
                drawRect(tint, Offset(w * 0.26f, h * 0.52f), Size(w * 0.14f, h * 0.38f))
                drawLine(tint, Offset(w * 0.25f, h * 0.34f), Offset(w * 0.05f, h * 0.42f), 2f)
            }
            "assault_rifle" -> {
                // Carbine / AR Rifle
                drawRect(tint, Offset(w * 0.38f, h * 0.32f), Size(w * 0.50f, h * 0.16f))
                drawRect(tint, Offset(w * 0.88f, h * 0.30f), Size(w * 0.08f, h * 0.20f))
                drawRect(tint, Offset(w * 0.34f, h * 0.16f), Size(w * 0.18f, h * 0.16f))
                drawRect(tint, Offset(w * 0.24f, h * 0.28f), Size(w * 0.24f, h * 0.24f))
                drawRoundRect(tint, Offset(w * 0.42f, h * 0.52f), Size(w * 0.14f, h * 0.40f), CornerRadius(2f, 2f))
                drawRect(tint, Offset(w * 0.26f, h * 0.52f), Size(w * 0.12f, h * 0.36f))
                drawRect(tint, Offset(w * 0.04f, h * 0.28f), Size(w * 0.20f, h * 0.30f))
            }
            "sniper_rifle" -> {
                // Long-range Precision Sniper Rifle
                drawRect(tint, Offset(w * 0.35f, h * 0.38f), Size(w * 0.58f, h * 0.10f))
                drawRect(tint, Offset(w * 0.92f, h * 0.32f), Size(w * 0.07f, h * 0.22f))
                drawRoundRect(tint, Offset(w * 0.32f, h * 0.18f), Size(w * 0.30f, h * 0.14f), CornerRadius(2f, 2f))
                drawRect(tint, Offset(w * 0.36f, h * 0.32f), Size(w * 0.06f, h * 0.06f))
                drawRect(tint, Offset(w * 0.52f, h * 0.32f), Size(w * 0.06f, h * 0.06f))
                drawRect(tint, Offset(w * 0.25f, h * 0.36f), Size(w * 0.22f, h * 0.22f))
                drawRoundRect(tint, Offset(w * 0.02f, h * 0.34f), Size(w * 0.24f, h * 0.32f), CornerRadius(2f, 2f))
                drawRect(tint, Offset(w * 0.26f, h * 0.56f), Size(w * 0.12f, h * 0.34f))
            }
        }
    }
}

@Composable
private fun StatMeter(label: String, value: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color(0xFF88998C),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF222C26))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value)
                    .fillMaxSize()
                    .background(TacticalOrange)
            )
        }
    }
}
