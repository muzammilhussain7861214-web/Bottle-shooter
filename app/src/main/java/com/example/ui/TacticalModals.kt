package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.SoundEffects
import com.example.model.Weapon
import com.example.model.WeatherMode
import com.example.ui.theme.TacticalOrange

@Composable
fun ArmoryDialog(
    currentWeapon: Weapon,
    onEquip: (Weapon) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(1.5.dp, TacticalOrange, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xF0121714)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = TacticalOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ARMORY LOADOUT",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFAAB8AF))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.height(380.dp)) {
                    items(Weapon.ALL) { weapon ->
                        val isEquipped = weapon.id == currentWeapon.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isEquipped) Color(0x33FF6D1F) else Color(0xAA18211B))
                                .border(
                                    width = 1.dp,
                                    color = if (isEquipped) TacticalOrange else Color(0x33455A4A),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = weapon.displayName,
                                            color = if (isEquipped) TacticalOrange else Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "${weapon.category} • ${weapon.magazineCapacity} RDS",
                                            color = Color(0xFF90A395),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            onEquip(weapon)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isEquipped) Color(0x66FF6D1F) else TacticalOrange
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isEquipped) "EQUIPPED" else "EQUIP",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Weapon Stats Bar (Damage & Recoil)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "DMG",
                                        color = Color(0xFF7E8F82),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.width(36.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = { (weapon.damage / 250f).coerceIn(0f, 1f) },
                                        modifier = Modifier.weight(1f).height(4.dp),
                                        color = TacticalOrange,
                                        trackColor = Color(0x33455A4A)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "RECOIL",
                                        color = Color(0xFF7E8F82),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.width(36.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = { (weapon.recoilPitch / 7f).coerceIn(0f, 1f) },
                                        modifier = Modifier.weight(1f).height(4.dp),
                                        color = Color(0xFFFF9800),
                                        trackColor = Color(0x33455A4A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    currentWeather: WeatherMode,
    onSelectWeather: (WeatherMode) -> Unit,
    onResetRange: () -> Unit,
    onDismiss: () -> Unit
) {
    var isAudioMuted by remember { mutableStateOf(SoundEffects.isMuted) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(1.5.dp, TacticalOrange, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xF0121714)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RANGE SETTINGS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFAAB8AF))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Audio Mute Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xAA18211B))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isAudioMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = TacticalOrange
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tactical Audio FX",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = !isAudioMuted,
                        onCheckedChange = {
                            isAudioMuted = !it
                            SoundEffects.isMuted = !it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TacticalOrange
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Respawn Targets Action
                Button(
                    onClick = {
                        onResetRange()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28362B)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "RESET ALL BOTTLE TARGETS",
                        color = TacticalOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardsDialog(
    score: Int,
    shotsFired: Int,
    shotsHit: Int,
    accuracy: Int,
    bottlesSmashed: Int,
    sessionDay: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(1.5.dp, TacticalOrange, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xF0121714)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = TacticalOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LEADERBOARDS & STATS",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFAAB8AF))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                StatRow("TOTAL SCORE", "$score PTS", TacticalOrange)
                StatRow("BOTTLES SMASHED", "$bottlesSmashed", Color.White)
                StatRow("ACCURACY RATING", "$accuracy%", if (accuracy > 70) Color(0xFF81C784) else TacticalOrange)
                StatRow("SHOTS FIRED / HIT", "$shotsFired / $shotsHit", Color(0xFFC0D0C5))
                StatRow("CURRENT SESSION", "DAY $sessionDay", Color(0xFFFFD54F))

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xAA18211B))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "TOP MARKS RECORD",
                            color = TacticalOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1. OPERATOR VIPER — 2,450 PTS (94% ACC)", color = Color(0xFFE2ECE4), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("2. GHOST_9 — 1,980 PTS (88% ACC)", color = Color(0xFFA8B8AC), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("3. YOU (DAY $sessionDay) — $score PTS ($accuracy% ACC)", color = TacticalOrange, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF8D9F92),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
