package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationLevel
import com.example.model.WeatherMode
import com.example.ui.theme.TacticalDarkSurface
import com.example.ui.theme.TacticalOrange
import com.example.ui.theme.TacticalOrangeGlow

@Composable
fun TacticalMenuDrawer(
    isOpen: Boolean,
    isWeatherSubmenuOpen: Boolean,
    currentWeather: WeatherMode,
    currentLevel: LocationLevel,
    sessionDay: Int,
    onPlay: () -> Unit,
    onToggleWeather: () -> Unit,
    onSelectWeather: (WeatherMode) -> Unit,
    onSelectLevel: (LocationLevel) -> Unit,
    onOpenArmory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLeaderboards: () -> Unit,
    onOpenWeaponWheel: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLocationsSubmenuOpen by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInHorizontally { -it / 2 },
        exit = fadeOut() + slideOutHorizontally { -it / 2 },
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88060A08))
                .clickable { onPlay() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(enabled = false) {} // Prevent click-through
                    .padding(top = 40.dp, bottom = 40.dp, start = 24.dp)
            ) {
                // Primary Tactical Left Menu
                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    TacticalMenuItem(
                        title = "PLAY GAME",
                        isActive = false,
                        onClick = onPlay
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TacticalMenuItem(
                        title = "CAMPAIGN LOCATIONS",
                        isActive = isLocationsSubmenuOpen,
                        onClick = {
                            isLocationsSubmenuOpen = !isLocationsSubmenuOpen
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TacticalMenuItem(
                        title = "WEATHER SYSTEMS",
                        isActive = isWeatherSubmenuOpen,
                        onClick = {
                            isLocationsSubmenuOpen = false
                            onToggleWeather()
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TacticalMenuItem(
                        title = "SETTINGS",
                        isActive = false,
                        onClick = onOpenSettings
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TacticalMenuItem(
                        title = "ARMORY",
                        isActive = false,
                        onClick = onOpenArmory
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TacticalMenuItem(
                        title = "LEADERBOARDS",
                        isActive = false,
                        onClick = onOpenLeaderboards
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TacticalMenuItem(
                        title = "EXIT",
                        isActive = false,
                        onClick = onExit
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    TacticalMenuItem(
                        title = "WEAPON WHEEL",
                        isActive = false,
                        onClick = onOpenWeaponWheel
                    )
                }

                // Locations Submenu
                AnimatedVisibility(
                    visible = isLocationsSubmenuOpen,
                    enter = fadeIn() + slideInHorizontally { -it / 3 },
                    exit = fadeOut() + slideOutHorizontally { -it / 3 }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .width(260.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        LocationLevel.entries.forEach { level ->
                            WeatherSubmenuItem(
                                title = "LVL ${level.levelNumber}: ${level.title.uppercase()}",
                                icon = Icons.Default.Landscape,
                                isSelected = currentLevel == level,
                                onClick = {
                                    onSelectLevel(level)
                                    isLocationsSubmenuOpen = false
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Weather Submenu (as seen in image_2.png)
                AnimatedVisibility(
                    visible = isWeatherSubmenuOpen,
                    enter = fadeIn() + slideInHorizontally { -it / 3 },
                    exit = fadeOut() + slideOutHorizontally { -it / 3 }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .width(240.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        WeatherSubmenuItem(
                            title = "CLEAR DAY (Default)",
                            icon = Icons.Default.WbSunny,
                            isSelected = currentWeather == WeatherMode.ClearDay,
                            onClick = { onSelectWeather(WeatherMode.ClearDay) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        WeatherSubmenuItem(
                            title = "OVERCAST & RAIN",
                            icon = Icons.Default.Cloud,
                            isSelected = currentWeather == WeatherMode.OvercastRain,
                            onClick = { onSelectWeather(WeatherMode.OvercastRain) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        WeatherSubmenuItem(
                            title = "THUNDERSTORM & WIND",
                            icon = Icons.Default.Thunderstorm,
                            isSelected = currentWeather == WeatherMode.ThunderstormWind,
                            onClick = { onSelectWeather(WeatherMode.ThunderstormWind) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        WeatherSubmenuItem(
                            title = "SNOWFLURRIES & FOG",
                            icon = Icons.Default.AcUnit,
                            isSelected = currentWeather == WeatherMode.SnowflurriesFog,
                            onClick = { onSelectWeather(WeatherMode.SnowflurriesFog) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TacticalMenuItem(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderModifier = if (isActive) {
        Modifier.border(2.dp, TacticalOrange, RoundedCornerShape(4.dp))
    } else {
        Modifier.border(1.dp, Color(0x33445544), RoundedCornerShape(4.dp))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) Color(0xCC1A201C) else Color(0xAA101412))
            .then(borderModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = if (isActive) TacticalOrange else Color(0xFFD8E2DA),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
fun WeatherSubmenuItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderMod = if (isSelected) {
        Modifier.border(2.dp, TacticalOrange, RoundedCornerShape(4.dp))
    } else {
        Modifier.border(1.dp, Color(0x333F4F42), RoundedCornerShape(4.dp))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) Color(0xDD1C231E) else Color(0xAA0E1310))
            .then(borderMod)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) TacticalOrange else Color(0xFF8D9E92),
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = title,
                color = if (isSelected) TacticalOrange else Color(0xFFE0EAE2),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
