package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationLevel
import com.example.model.Weapon
import com.example.model.WeatherMode
import com.example.ui.theme.ReticleRed
import com.example.ui.theme.ReticleRedGlow
import com.example.ui.theme.TacticalOrange

@Composable
fun TacticalHUD(
    currentWeapon: Weapon,
    currentAmmo: Int,
    reserveAmmo: Int,
    isReloading: Boolean,
    isAdsActive: Boolean,
    aimPointNormalized: Offset,
    weather: WeatherMode,
    currentLevel: LocationLevel,
    levelCompletedBanner: String?,
    score: Int,
    sessionDay: Int,
    remainingTargets: Int,
    accuracy: Int,
    isAmbientAudioEnabled: Boolean,
    onFire: () -> Unit,
    onReload: () -> Unit,
    onToggleAds: () -> Unit,
    onOpenWeaponWheel: () -> Unit,
    onToggleMenu: () -> Unit,
    onRespawnTargets: () -> Unit,
    onNextLevel: () -> Unit,
    onToggleAmbient: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Top Bar: Subtle "BOTTLE RANGE" Title and Quick Menu / Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left: Tactical Menu button + Ambient Sound Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleMenu,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x99101612))
                        .border(1.dp, Color(0x33445544), RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Tactical Menu",
                        tint = TacticalOrange
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onToggleAmbient,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x99101612))
                        .border(1.dp, Color(0x33445544), RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = if (isAmbientAudioEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                        contentDescription = "Toggle Forest Ambient Sound",
                        tint = if (isAmbientAudioEnabled) TacticalOrange else Color(0xFF888888)
                    )
                }
            }

            // Center: BOTTLE RANGE Title (as in image_2.png)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BOTTLE RANGE",
                    color = Color(0xEEFFFFFF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "TACTICAL BALLISTICS SIMULATION",
                    color = TacticalOrange.copy(alpha = 0.8f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // Right: Target Counter & Reset
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x99101612))
                        .border(1.dp, Color(0x33445544), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "TARGETS: $remainingTargets/7",
                        color = if (remainingTargets == 0) TacticalOrange else Color(0xFFC0D0C5),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onRespawnTargets,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x88101612))
                        .border(1.dp, Color(0x33445544), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Respawn Targets",
                        tint = Color(0xFFD4E0D7),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Level Clear Banner
        AnimatedVisibility(
            visible = levelCompletedBanner != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 84.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xF216241C))
                    .border(2.dp, TacticalOrange, RoundedCornerShape(8.dp))
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = levelCompletedBanner ?: "",
                    color = TacticalOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 3. Dynamic Smooth Aim Crosshair (Moves with look/sway!)
        DynamicAimCrosshair(
            aimPointNormalized = aimPointNormalized,
            isAdsActive = isAdsActive,
            weaponId = currentWeapon.id
        )

        // 3. Bottom-Left: Ammo & Session Counter (matching image_0.png & image_2.png)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 44.dp)
        ) {
            // Ammo text e.g. "11/45 Ammo"
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (isReloading) "RELOADING..." else "$currentAmmo/$reserveAmmo",
                    color = if (currentAmmo <= 3 && !isReloading) ReticleRed else Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ammo",
                    color = Color(0xFFB0BEB5),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Magazine 4 status bars / pips (as in image_0.png)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mag icon
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = Color(0xFF90A496),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))

                val maxCap = currentWeapon.magazineCapacity.toFloat()
                val ratio = (currentAmmo.toFloat() / maxCap).coerceIn(0f, 1f)
                val activePips = kotlin.math.ceil(ratio * 4).toInt()

                for (i in 0 until 4) {
                    val isActive = i < activePips
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(5.dp)
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (isActive) {
                                    if (currentAmmo <= 3) ReticleRed else Color(0xFFE0EAE2)
                                } else {
                                    Color(0x444A594E)
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SESSION: DAY 3 (as in image_2.png)
            Text(
                text = "SESSION: DAY $sessionDay",
                color = Color(0xFF8A9A8F),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            // Dynamic Location Indicator (Click to advance level)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .clickable { onNextLevel() }
            ) {
                Text(
                    text = "LOCATION: [LVL ${currentLevel.levelNumber} - ${currentLevel.title.uppercase()}]",
                    color = TacticalOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Current Weather indicator
            Text(
                text = "WEATHER: ${weather.title.take(15)}",
                color = Color(0xFFCCD6CF),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // 4. Bottom-Right & Side Controls: Quick Action Hub
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Weapon Wheel Quick Toggle (circular button)
            IconButton(
                onClick = onOpenWeaponWheel,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC1A231D))
                    .border(2.dp, TacticalOrange, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = "Weapon Wheel",
                    tint = TacticalOrange,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ADS Toggle (Aim-Down-Sights zoom)
            IconButton(
                onClick = onToggleAds,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isAdsActive) TacticalOrange.copy(alpha = 0.4f) else Color(0xAA121814))
                    .border(1.5.dp, if (isAdsActive) TacticalOrange else Color(0x55556655), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Toggle ADS",
                    tint = if (isAdsActive) TacticalOrange else Color(0xFFCCD6CF),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reload Button
            IconButton(
                onClick = onReload,
                enabled = !isReloading && currentAmmo < currentWeapon.magazineCapacity,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xAA121814))
                    .border(1.5.dp, Color(0x55556655), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload Weapon",
                    tint = if (isReloading) Color(0xFF666666) else Color(0xFFCCD6CF),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Tactical Trigger / Fire Button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xEE2A130B))
                    .border(2.5.dp, TacticalOrange, CircleShape)
                    .clickable(onClick = onFire),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FIRE",
                        color = TacticalOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicAimCrosshair(
    aimPointNormalized: Offset,
    isAdsActive: Boolean,
    weaponId: String
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width * aimPointNormalized.x
        val cy = size.height * aimPointNormalized.y

            if (isAdsActive) {
                // In ADS: Glowing red dot reticle matching the RMR sight
                drawCircle(
                    color = ReticleRedGlow,
                    radius = 9f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = ReticleRed,
                    radius = 3.5f,
                    center = Offset(cx, cy)
                )
            } else {
                // Hipfire: Tactical 4-line crosshairs with center dot
                drawCircle(
                    color = Color(0xDDFFFFFF),
                    radius = 2.5f,
                    center = Offset(cx, cy)
                )
                val gap = 12f
                val length = 18f
                val strokeW = 1.8f
                val crossColor = Color(0xBBFFFFFF)

                // Top line
                drawLine(crossColor, Offset(cx, cy - gap), Offset(cx, cy - gap - length), strokeW)
                // Bottom line
                drawLine(crossColor, Offset(cx, cy + gap), Offset(cx, cy + gap + length), strokeW)
                // Left line
                drawLine(crossColor, Offset(cx - gap, cy), Offset(cx - gap - length, cy), strokeW)
                // Right line
                drawLine(crossColor, Offset(cx + gap, cy), Offset(cx + gap + length, cy), strokeW)
            }
        }
    }
