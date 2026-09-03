package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BottleAmber
import com.example.ui.theme.BottleClear
import com.example.ui.theme.BottleGreen

enum class BottleColor(val displayColor: Color, val label: String) {
    Amber(BottleAmber, "Amber Ale"),
    Emerald(BottleGreen, "Vintage Green"),
    Clear(BottleClear, "Clear Spirits")
}

data class Weapon(
    val id: String,
    val displayName: String,
    val category: String,
    val magazineCapacity: Int,
    val maxReserve: Int,
    val damage: Float,
    val recoilPitch: Float,
    val recoilYaw: Float,
    val fireRateMs: Long,
    val isAutomatic: Boolean,
    val adsZoom: Float,
    val description: String
) {
    companion object {
        val ALL = listOf(
            Weapon(
                id = "custom_pistol",
                displayName = "CUSTOM PISTOL",
                category = "Handgun",
                magazineCapacity = 15,
                maxReserve = 45,
                damage = 45f,
                recoilPitch = 2.4f,
                recoilYaw = 0.8f,
                fireRateMs = 200L,
                isAutomatic = false,
                adsZoom = 1.35f,
                description = "9mm suppressed pistol fitted with micro RMR red dot."
            ),
            Weapon(
                id = "revolver",
                displayName = "REVOLVER",
                category = "Sidearm",
                magazineCapacity = 6,
                maxReserve = 30,
                damage = 110f,
                recoilPitch = 4.8f,
                recoilYaw = 1.6f,
                fireRateMs = 450L,
                isAutomatic = false,
                adsZoom = 1.25f,
                description = ".357 Magnum steel revolver with heavy stopping force."
            ),
            Weapon(
                id = "smg",
                displayName = "SUBMACHINE GUN",
                category = "Close Quarters",
                magazineCapacity = 30,
                maxReserve = 120,
                damage = 32f,
                recoilPitch = 1.5f,
                recoilYaw = 0.9f,
                fireRateMs = 95L,
                isAutomatic = true,
                adsZoom = 1.3f,
                description = "9x19mm tactical submachine gun with high cyclic rate."
            ),
            Weapon(
                id = "shotgun",
                displayName = "SHOTGUN",
                category = "Breaching",
                magazineCapacity = 8,
                maxReserve = 24,
                damage = 160f,
                recoilPitch = 5.2f,
                recoilYaw = 2.0f,
                fireRateMs = 600L,
                isAutomatic = false,
                adsZoom = 1.2f,
                description = "12-gauge tactical pump-action firing dense buckshot."
            ),
            Weapon(
                id = "assault_rifle",
                displayName = "ASSAULT RIFLE",
                category = "Primary Rifle",
                magazineCapacity = 30,
                maxReserve = 90,
                damage = 65f,
                recoilPitch = 2.6f,
                recoilYaw = 1.1f,
                fireRateMs = 120L,
                isAutomatic = true,
                adsZoom = 1.6f,
                description = "5.56 NATO modular carbine with holographic optic."
            ),
            Weapon(
                id = "sniper_rifle",
                displayName = "SNIPER RIFLE",
                category = "Precision",
                magazineCapacity = 5,
                maxReserve = 15,
                damage = 250f,
                recoilPitch = 6.5f,
                recoilYaw = 0.5f,
                fireRateMs = 950L,
                isAutomatic = false,
                adsZoom = 3.2f,
                description = "7.62x51mm precision bolt-action rifle with 8x scope."
            )
        )
    }
}

enum class LocationLevel(
    val levelNumber: Int,
    val title: String,
    val subtitle: String,
    val skyColors: List<Color>,
    val ambientDescription: String,
    val foliageColor: Color,
    val locationBadge: String
) {
    DeepPineForest(
        levelNumber = 1,
        title = "DEEP PINE FOREST",
        subtitle = "Level 1 • Gnarled Ancient Oak Canopy",
        skyColors = listOf(Color(0xFF1B2E24), Color(0xFF2E4633), Color(0xFF566B4E)),
        ambientDescription = "Sunbeams filtering through evergreen needles with morning songbirds.",
        foliageColor = Color(0xFF2E4A28),
        locationBadge = "LVL 1: PINE FOREST"
    ),
    MistyMountainEdge(
        levelNumber = 2,
        title = "MISTY MOUNTAIN EDGE",
        subtitle = "Level 2 • High Altitude Alpine Ridge",
        skyColors = listOf(Color(0xFF1B2430), Color(0xFF334658), Color(0xFF677E95)),
        ambientDescription = "Alpine winds echoing across jagged crags with distant mountain hawks.",
        foliageColor = Color(0xFF3A4E46),
        locationBadge = "LVL 2: MOUNTAIN EDGE"
    ),
    AutumnWoods(
        levelNumber = 3,
        title = "AUTUMN WOODS",
        subtitle = "Level 3 • Amber Sunset Clearing",
        skyColors = listOf(Color(0xFF341C16), Color(0xFF63331C), Color(0xFFB8662F)),
        ambientDescription = "Crisp autumn breeze rustling golden maple boughs and twilight crickets.",
        foliageColor = Color(0xFFBF5722),
        locationBadge = "LVL 3: AUTUMN WOODS"
    ),
    OvergrownJungleRuins(
        levelNumber = 4,
        title = "OVERGROWN JUNGLE RUINS",
        subtitle = "Level 4 • Ancient Stone Monoliths",
        skyColors = listOf(Color(0xFF0D2418), Color(0xFF163B26), Color(0xFF255435)),
        ambientDescription = "Humid tropical canopy buzzing with cicadas and exotic wildlife calls.",
        foliageColor = Color(0xFF1B5E20),
        locationBadge = "LVL 4: JUNGLE RUINS"
    )
}

enum class WeatherMode(val title: String, val subtitle: String, val windForce: Float) {
    ClearDay("CLEAR DAY (Default)", "Golden hour sun dapples through the oak canopy.", 0.25f),
    OvercastRain("OVERCAST & RAIN", "Persistent drizzle with mist and steady wind.", 0.75f),
    ThunderstormWind("THUNDERSTORM & WIND", "Violent wind gusts with blinding lightning flashes.", 1.85f),
    SnowflurriesFog("SNOWFLURRIES & FOG", "Cold fog with swirling snowflake drafts.", 0.55f)
}

data class BottleTarget(
    val id: Int,
    val color: BottleColor,
    val branchAnchorX: Float, // percentage of canvas width (0.1f - 0.9f)
    val branchAnchorY: Float, // percentage of canvas height (0.15f - 0.35f)
    val ropeLength: Float,    // rope length in pixels
    val currentAngle: Float = 0f, // in degrees
    val angularVelocity: Float = 0f,
    val naturalFrequency: Float = 1.8f, // pendulum speed
    val isRopeCut: Boolean = false,
    val isShattered: Boolean = false,
    val fallYOffset: Float = 0f,
    val fallVelocityY: Float = 0f
)

data class GlassShard(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var vRot: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    var alpha: Float = 1f,
    var life: Float = 1.5f
)

data class HitMarker(
    val id: Long,
    val text: String,
    val x: Float,
    val y: Float,
    val color: Color,
    val createdAt: Long = System.currentTimeMillis()
)
