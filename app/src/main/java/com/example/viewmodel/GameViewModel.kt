package com.example.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffects
import com.example.model.BottleColor
import com.example.model.BottleTarget
import com.example.model.GlassShard
import com.example.model.HitMarker
import com.example.model.LocationLevel
import com.example.model.Weapon
import com.example.model.WeatherMode
import com.example.ui.theme.BottleAmber
import com.example.ui.theme.BottleClear
import com.example.ui.theme.BottleGreen
import com.example.ui.theme.TacticalOrange
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class GameState(
    val currentLevel: LocationLevel = LocationLevel.DeepPineForest,
    val aimPointNormalized: Offset = Offset(0.5f, 0.45f), // Free-look aim coordinate (0..1)
    val levelCompletedBanner: String? = null,
    val isAmbientAudioEnabled: Boolean = true,
    val currentWeapon: Weapon = Weapon.ALL.first(),
    val currentAmmo: Int = 15,
    val reserveAmmo: Int = 45,
    val isReloading: Boolean = false,
    val isAdsActive: Boolean = false,
    val weather: WeatherMode = WeatherMode.ClearDay,
    val targets: List<BottleTarget> = emptyList(),
    val shards: List<GlassShard> = emptyList(),
    val hitMarkers: List<HitMarker> = emptyList(),
    val isWeaponWheelOpen: Boolean = false,
    val isTacticalMenuOpen: Boolean = false,
    val isWeatherSubmenuOpen: Boolean = false,
    val isArmoryOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isLeaderboardsOpen: Boolean = false,
    val score: Int = 0,
    val shotsFired: Int = 0,
    val shotsHit: Int = 0,
    val bottlesSmashed: Int = 0,
    val ropesCut: Int = 0,
    val sessionDay: Int = 3,
    val recoilKick: Float = 0f,
    val lightningIntensity: Float = 0f,
    val lastShotTime: Long = 0L
) {
    val accuracyPercentage: Int
        get() = if (shotsFired > 0) ((shotsHit.toFloat() / shotsFired) * 100).toInt() else 100
}

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameState())
    val uiState = _uiState.asStateFlow()

    private var physicsJob: Job? = null
    private var lightningJob: Job? = null

    init {
        respawnTargets(LocationLevel.DeepPineForest)
        startPhysicsLoop()
        startWeatherCycle()
        SoundEffects.startAmbientLoop(LocationLevel.DeepPineForest, WeatherMode.ClearDay)
    }

    fun respawnTargets(level: LocationLevel = _uiState.value.currentLevel) {
        val newTargets = when (level) {
            LocationLevel.DeepPineForest -> listOf(
                BottleTarget(id = 1, color = BottleColor.Amber, branchAnchorX = 0.16f, branchAnchorY = 0.22f, ropeLength = 140f, naturalFrequency = 1.6f),
                BottleTarget(id = 2, color = BottleColor.Amber, branchAnchorX = 0.27f, branchAnchorY = 0.23f, ropeLength = 155f, naturalFrequency = 1.4f),
                BottleTarget(id = 3, color = BottleColor.Amber, branchAnchorX = 0.36f, branchAnchorY = 0.24f, ropeLength = 165f, naturalFrequency = 1.3f),
                BottleTarget(id = 4, color = BottleColor.Emerald, branchAnchorX = 0.44f, branchAnchorY = 0.27f, ropeLength = 180f, naturalFrequency = 1.2f),
                BottleTarget(id = 5, color = BottleColor.Amber, branchAnchorX = 0.52f, branchAnchorY = 0.16f, ropeLength = 230f, naturalFrequency = 0.95f),
                BottleTarget(id = 6, color = BottleColor.Emerald, branchAnchorX = 0.65f, branchAnchorY = 0.25f, ropeLength = 170f, naturalFrequency = 1.35f),
                BottleTarget(id = 7, color = BottleColor.Clear, branchAnchorX = 0.76f, branchAnchorY = 0.23f, ropeLength = 160f, naturalFrequency = 1.5f)
            )
            LocationLevel.MistyMountainEdge -> listOf(
                BottleTarget(id = 1, color = BottleColor.Clear, branchAnchorX = 0.14f, branchAnchorY = 0.19f, ropeLength = 190f, naturalFrequency = 1.8f),
                BottleTarget(id = 2, color = BottleColor.Amber, branchAnchorX = 0.25f, branchAnchorY = 0.24f, ropeLength = 145f, naturalFrequency = 1.5f),
                BottleTarget(id = 3, color = BottleColor.Clear, branchAnchorX = 0.38f, branchAnchorY = 0.22f, ropeLength = 175f, naturalFrequency = 1.3f),
                BottleTarget(id = 4, color = BottleColor.Emerald, branchAnchorX = 0.48f, branchAnchorY = 0.18f, ropeLength = 210f, naturalFrequency = 1.1f),
                BottleTarget(id = 5, color = BottleColor.Clear, branchAnchorX = 0.58f, branchAnchorY = 0.26f, ropeLength = 150f, naturalFrequency = 1.4f),
                BottleTarget(id = 6, color = BottleColor.Amber, branchAnchorX = 0.70f, branchAnchorY = 0.22f, ropeLength = 185f, naturalFrequency = 1.6f),
                BottleTarget(id = 7, color = BottleColor.Clear, branchAnchorX = 0.82f, branchAnchorY = 0.25f, ropeLength = 140f, naturalFrequency = 1.9f)
            )
            LocationLevel.AutumnWoods -> listOf(
                BottleTarget(id = 1, color = BottleColor.Amber, branchAnchorX = 0.18f, branchAnchorY = 0.23f, ropeLength = 150f, naturalFrequency = 1.4f),
                BottleTarget(id = 2, color = BottleColor.Emerald, branchAnchorX = 0.29f, branchAnchorY = 0.25f, ropeLength = 165f, naturalFrequency = 1.25f),
                BottleTarget(id = 3, color = BottleColor.Amber, branchAnchorX = 0.40f, branchAnchorY = 0.21f, ropeLength = 180f, naturalFrequency = 1.35f),
                BottleTarget(id = 4, color = BottleColor.Amber, branchAnchorX = 0.50f, branchAnchorY = 0.17f, ropeLength = 220f, naturalFrequency = 1.0f),
                BottleTarget(id = 5, color = BottleColor.Emerald, branchAnchorX = 0.60f, branchAnchorY = 0.24f, ropeLength = 160f, naturalFrequency = 1.45f),
                BottleTarget(id = 6, color = BottleColor.Amber, branchAnchorX = 0.71f, branchAnchorY = 0.22f, ropeLength = 175f, naturalFrequency = 1.3f),
                BottleTarget(id = 7, color = BottleColor.Amber, branchAnchorX = 0.80f, branchAnchorY = 0.26f, ropeLength = 145f, naturalFrequency = 1.6f)
            )
            LocationLevel.OvergrownJungleRuins -> listOf(
                BottleTarget(id = 1, color = BottleColor.Emerald, branchAnchorX = 0.15f, branchAnchorY = 0.20f, ropeLength = 200f, naturalFrequency = 1.5f),
                BottleTarget(id = 2, color = BottleColor.Amber, branchAnchorX = 0.26f, branchAnchorY = 0.27f, ropeLength = 135f, naturalFrequency = 1.7f),
                BottleTarget(id = 3, color = BottleColor.Emerald, branchAnchorX = 0.37f, branchAnchorY = 0.22f, ropeLength = 190f, naturalFrequency = 1.2f),
                BottleTarget(id = 4, color = BottleColor.Clear, branchAnchorX = 0.49f, branchAnchorY = 0.15f, ropeLength = 240f, naturalFrequency = 0.9f),
                BottleTarget(id = 5, color = BottleColor.Emerald, branchAnchorX = 0.62f, branchAnchorY = 0.26f, ropeLength = 155f, naturalFrequency = 1.4f),
                BottleTarget(id = 6, color = BottleColor.Emerald, branchAnchorX = 0.73f, branchAnchorY = 0.21f, ropeLength = 180f, naturalFrequency = 1.3f),
                BottleTarget(id = 7, color = BottleColor.Amber, branchAnchorX = 0.84f, branchAnchorY = 0.24f, ropeLength = 165f, naturalFrequency = 1.55f)
            )
        }
        _uiState.value = _uiState.value.copy(targets = newTargets)
    }

    private fun startPhysicsLoop() {
        physicsJob?.cancel()
        physicsJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            var timeAccumulator = 0f
            while (isActive) {
                val now = System.currentTimeMillis()
                val dt = ((now - lastTime) / 1000f).coerceIn(0.01f, 0.05f)
                lastTime = now
                timeAccumulator += dt

                val state = _uiState.value
                val wind = state.weather.windForce

                // 1. Update hanging targets physics
                val updatedTargets = state.targets.map { target ->
                    if (target.isShattered) {
                        target
                    } else if (target.isRopeCut) {
                        // Bottle falling under gravity
                        val newVy = target.fallVelocityY + 980f * dt
                        val newYOffset = target.fallYOffset + newVy * dt
                        // Check ground hit (e.g. 500px fall)
                        if (newYOffset > 520f) {
                            // Shatter upon ground impact!
                            SoundEffects.playGlassShatter()
                            spawnGlassParticles(
                                x = target.branchAnchorX * 1080f, // approximate, screen scaled in canvas
                                y = target.branchAnchorY * 1920f + target.ropeLength + newYOffset,
                                color = target.color.displayColor,
                                count = 18
                            )
                            target.copy(isShattered = true, fallYOffset = newYOffset)
                        } else {
                            target.copy(fallYOffset = newYOffset, fallVelocityY = newVy)
                        }
                    } else {
                        // Dynamic pendulum oscillation driven by natural frequency and wind turbulence
                        val windSway = sin(timeAccumulator * target.naturalFrequency * 1.5f) * (4.5f * wind)
                        val naturalSway = sin(timeAccumulator * target.naturalFrequency) * 3f
                        val angle = (naturalSway + windSway).coerceIn(-25f, 25f)
                        target.copy(currentAngle = angle)
                    }
                }

                // 2. Update shards
                val updatedShards = state.shards.mapNotNull { shard ->
                    shard.x += shard.vx * dt
                    shard.y += shard.vy * dt
                    shard.vy += 850f * dt // Gravity
                    shard.rotation += shard.vRot * dt
                    shard.life -= dt
                    shard.alpha = (shard.life / 1.5f).coerceIn(0f, 1f)
                    if (shard.life > 0f) shard else null
                }

                // 3. Update hit markers
                val updatedMarkers = state.hitMarkers.filter { now - it.createdAt < 900L }

                // 4. Decay recoil kickback smoothly
                val newRecoil = (state.recoilKick - dt * 14f).coerceAtLeast(0f)

                _uiState.value = state.copy(
                    targets = updatedTargets,
                    shards = updatedShards,
                    hitMarkers = updatedMarkers,
                    recoilKick = newRecoil
                )

                delay(16L) // ~60fps loop
            }
        }
    }

    private fun startWeatherCycle() {
        lightningJob?.cancel()
        lightningJob = viewModelScope.launch {
            while (isActive) {
                if (_uiState.value.weather == WeatherMode.ThunderstormWind) {
                    delay(Random.nextLong(4000L, 9000L))
                    // Lightning flash 1
                    _uiState.value = _uiState.value.copy(lightningIntensity = 0.95f)
                    delay(70L)
                    _uiState.value = _uiState.value.copy(lightningIntensity = 0.2f)
                    delay(40L)
                    _uiState.value = _uiState.value.copy(lightningIntensity = 0.8f)
                    delay(90L)
                    _uiState.value = _uiState.value.copy(lightningIntensity = 0f)
                    SoundEffects.playThunder()
                } else {
                    delay(1000L)
                }
            }
        }
    }

    fun shoot(targetX: Float, targetY: Float, canvasWidth: Float, canvasHeight: Float) {
        val state = _uiState.value
        val weapon = state.currentWeapon

        if (state.isReloading) return

        if (state.currentAmmo <= 0) {
            SoundEffects.playDryFire()
            return
        }

        val now = System.currentTimeMillis()
        if (now - state.lastShotTime < weapon.fireRateMs) return

        // Fire gunshot!
        SoundEffects.playGunshot(weapon.id)
        val newAmmo = state.currentAmmo - 1

        // Check hits on active targets
        var hitDetected = false
        var hitScore = 0
        var hitText = ""
        val newTargets = state.targets.map { target ->
            if (target.isShattered) return@map target

            // Compute bottle anchor & dynamic pendulum coordinates in pixels
            val anchorX = target.branchAnchorX * canvasWidth
            val anchorY = target.branchAnchorY * canvasHeight

            val angleRad = Math.toRadians(target.currentAngle.toDouble())
            val ropeLength = target.ropeLength
            val bottleTopX = (anchorX + sin(angleRad) * ropeLength).toFloat()
            val bottleTopY = (anchorY + cos(angleRad) * ropeLength).toFloat() + target.fallYOffset

            // 1. Check Twine Rope Cut (line segment from anchor to bottleTop)
            if (!target.isRopeCut) {
                val distToRope = distancePointToSegment(targetX, targetY, anchorX, anchorY, bottleTopX, bottleTopY)
                if (distToRope < 26f) {
                    // Rope severed!
                    hitDetected = true
                    hitScore += 250
                    hitText = "+250 TWINE CUT"
                    SoundEffects.playRopeCut()
                    return@map target.copy(isRopeCut = true, fallVelocityY = 40f)
                }
            }

            // 2. Check Bottle Body Hit (radius around bottle center)
            val bottleCenterX = bottleTopX
            val bottleCenterY = bottleTopY + 38f // bottle hangs down from rope tie
            val dx = targetX - bottleCenterX
            val dy = targetY - bottleCenterY
            val distToBottle = kotlin.math.sqrt(dx * dx + dy * dy)

            if (distToBottle < 42f) {
                // Direct glass shatter!
                hitDetected = true
                hitScore += 100
                hitText = "+100 SHATTERED"
                SoundEffects.playGlassShatter()
                spawnGlassParticles(bottleCenterX, bottleCenterY, target.color.displayColor, count = 22)
                return@map target.copy(isShattered = true)
            }

            target
        }

        // Hit marker
        val newMarkers = if (hitDetected) {
            state.hitMarkers + HitMarker(
                id = System.currentTimeMillis(),
                text = hitText,
                x = targetX,
                y = targetY - 20f,
                color = TacticalOrange
            )
        } else {
            state.hitMarkers
        }

        val newShotsFired = state.shotsFired + 1
        val newShotsHit = state.shotsHit + (if (hitDetected) 1 else 0)
        val newSmashed = state.bottlesSmashed + (if (hitDetected) 1 else 0)
        val newScore = state.score + hitScore

        val allTargetsCleared = newTargets.all { it.isShattered || (it.isRopeCut && it.fallYOffset > 800f) }

        _uiState.value = state.copy(
            currentAmmo = newAmmo,
            lastShotTime = now,
            recoilKick = weapon.recoilPitch,
            targets = newTargets,
            hitMarkers = newMarkers,
            shotsFired = newShotsFired,
            shotsHit = newShotsHit,
            bottlesSmashed = newSmashed,
            score = newScore
        )

        if (allTargetsCleared && state.levelCompletedBanner == null) {
            viewModelScope.launch {
                delay(800L)
                advanceLevel()
            }
        }
    }

    fun setAimPoint(normalizedX: Float, normalizedY: Float) {
        val clampedX = normalizedX.coerceIn(0.08f, 0.92f)
        val clampedY = normalizedY.coerceIn(0.12f, 0.88f)
        _uiState.value = _uiState.value.copy(aimPointNormalized = Offset(clampedX, clampedY))
    }

    fun advanceLevel() {
        val current = _uiState.value.currentLevel
        val allLevels = LocationLevel.entries
        val nextIndex = (allLevels.indexOf(current) + 1) % allLevels.size
        val nextLevel = allLevels[nextIndex]

        SoundEffects.playLevelComplete()
        _uiState.value = _uiState.value.copy(
            currentLevel = nextLevel,
            score = _uiState.value.score + 1000,
            levelCompletedBanner = "★ LEVEL CLEARED! +1000 PTS • ENTERING ${nextLevel.title} ★"
        )
        SoundEffects.updateAtmosphere(nextLevel, _uiState.value.weather)
        respawnTargets(nextLevel)

        viewModelScope.launch {
            delay(3200L)
            _uiState.value = _uiState.value.copy(levelCompletedBanner = null)
        }
    }

    fun selectLocation(level: LocationLevel) {
        _uiState.value = _uiState.value.copy(
            currentLevel = level,
            isTacticalMenuOpen = false
        )
        SoundEffects.updateAtmosphere(level, _uiState.value.weather)
        respawnTargets(level)
    }

    fun toggleAmbientAudio() {
        val enabled = !_uiState.value.isAmbientAudioEnabled
        SoundEffects.isAmbientEnabled = enabled
        _uiState.value = _uiState.value.copy(isAmbientAudioEnabled = enabled)
    }

    private fun spawnGlassParticles(x: Float, y: Float, color: Color, count: Int) {
        val newShards = (0 until count).map {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 260f + 70f
            GlassShard(
                x = x,
                y = y,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 90f,
                rotation = Random.nextFloat() * 360f,
                vRot = (Random.nextFloat() * 2f - 1f) * 400f,
                color = color,
                width = Random.nextFloat() * 8f + 5f,
                height = Random.nextFloat() * 14f + 6f,
                life = 1.4f
            )
        }
        _uiState.value = _uiState.value.copy(shards = _uiState.value.shards + newShards)
    }

    fun reload() {
        val state = _uiState.value
        val weapon = state.currentWeapon
        if (state.isReloading || state.currentAmmo >= weapon.magazineCapacity || state.reserveAmmo <= 0) return

        _uiState.value = state.copy(isReloading = true)
        SoundEffects.playReload()

        viewModelScope.launch {
            delay(1200L)
            val currentState = _uiState.value
            val needed = currentState.currentWeapon.magazineCapacity - currentState.currentAmmo
            val toLoad = needed.coerceAtMost(currentState.reserveAmmo)
            _uiState.value = currentState.copy(
                currentAmmo = currentState.currentAmmo + toLoad,
                reserveAmmo = currentState.reserveAmmo - toLoad,
                isReloading = false
            )
        }
    }

    fun equipWeapon(weapon: Weapon) {
        val state = _uiState.value
        if (state.currentWeapon.id == weapon.id) return

        SoundEffects.playReload()
        _uiState.value = state.copy(
            currentWeapon = weapon,
            currentAmmo = weapon.magazineCapacity,
            reserveAmmo = weapon.maxReserve,
            isReloading = false,
            isWeaponWheelOpen = false
        )
    }

    fun setWeather(mode: WeatherMode) {
        _uiState.value = _uiState.value.copy(
            weather = mode,
            isWeatherSubmenuOpen = false,
            isTacticalMenuOpen = false
        )
        SoundEffects.updateAtmosphere(_uiState.value.currentLevel, mode)
        if (mode == WeatherMode.ThunderstormWind) {
            SoundEffects.playThunder()
        }
    }

    fun toggleAds() {
        _uiState.value = _uiState.value.copy(isAdsActive = !_uiState.value.isAdsActive)
    }

    fun toggleWeaponWheel() {
        SoundEffects.playWheelTick()
        _uiState.value = _uiState.value.copy(
            isWeaponWheelOpen = !_uiState.value.isWeaponWheelOpen,
            isTacticalMenuOpen = false
        )
    }

    fun closeWeaponWheel() {
        _uiState.value = _uiState.value.copy(isWeaponWheelOpen = false)
    }

    fun toggleTacticalMenu() {
        SoundEffects.playWheelTick()
        _uiState.value = _uiState.value.copy(
            isTacticalMenuOpen = !_uiState.value.isTacticalMenuOpen,
            isWeatherSubmenuOpen = false,
            isWeaponWheelOpen = false
        )
    }

    fun toggleWeatherSubmenu() {
        _uiState.value = _uiState.value.copy(isWeatherSubmenuOpen = !_uiState.value.isWeatherSubmenuOpen)
    }

    fun closeMenus() {
        _uiState.value = _uiState.value.copy(
            isTacticalMenuOpen = false,
            isWeatherSubmenuOpen = false,
            isArmoryOpen = false,
            isSettingsOpen = false,
            isLeaderboardsOpen = false
        )
    }

    fun openArmory() {
        _uiState.value = _uiState.value.copy(isArmoryOpen = true, isTacticalMenuOpen = false)
    }

    fun closeArmory() {
        _uiState.value = _uiState.value.copy(isArmoryOpen = false)
    }

    fun openSettings() {
        _uiState.value = _uiState.value.copy(isSettingsOpen = true, isTacticalMenuOpen = false)
    }

    fun closeSettings() {
        _uiState.value = _uiState.value.copy(isSettingsOpen = false)
    }

    fun openLeaderboards() {
        _uiState.value = _uiState.value.copy(isLeaderboardsOpen = true, isTacticalMenuOpen = false)
    }

    fun closeLeaderboards() {
        _uiState.value = _uiState.value.copy(isLeaderboardsOpen = false)
    }

    private fun distancePointToSegment(
        px: Float, py: Float,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Float {
        val l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)
        if (l2 == 0f) return kotlin.math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1))
        var t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2
        t = t.coerceIn(0f, 1f)
        val projX = x1 + t * (x2 - x1)
        val projY = y1 + t * (y2 - y1)
        val dx = px - projX
        val dy = py - projY
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
