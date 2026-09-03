package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.GameViewModel

@Composable
fun BottleRangeGameScreen(
    viewModel: GameViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val remainingTargetsCount = state.targets.count { !it.isShattered }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C100E))
    ) {
        // Layer 1: Forest Environment, Oak Tree, Swinging Bottles, Glass Shards & Weather
        RangeEnvironmentCanvas(
            targets = state.targets,
            shards = state.shards,
            hitMarkers = state.hitMarkers,
            weather = state.weather,
            currentLevel = state.currentLevel,
            aimPointNormalized = state.aimPointNormalized,
            lightningIntensity = state.lightningIntensity,
            onAimChange = { x, y ->
                viewModel.setAimPoint(x, y)
            },
            onShoot = { x, y, w, h ->
                viewModel.shoot(x, y, w, h)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2: True First-Person Tactical Gloves & Weapon Viewmodel
        TacticalViewmodel(
            weapon = state.currentWeapon,
            recoilKick = state.recoilKick,
            isAdsActive = state.isAdsActive,
            aimPointNormalized = state.aimPointNormalized,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 3: Tactical HUD (Ammo, Pips, Crosshair, Controls)
        TacticalHUD(
            currentWeapon = state.currentWeapon,
            currentAmmo = state.currentAmmo,
            reserveAmmo = state.reserveAmmo,
            isReloading = state.isReloading,
            isAdsActive = state.isAdsActive,
            aimPointNormalized = state.aimPointNormalized,
            weather = state.weather,
            currentLevel = state.currentLevel,
            levelCompletedBanner = state.levelCompletedBanner,
            score = state.score,
            sessionDay = state.sessionDay,
            remainingTargets = remainingTargetsCount,
            accuracy = state.accuracyPercentage,
            isAmbientAudioEnabled = state.isAmbientAudioEnabled,
            onFire = {
                // Shoot dynamically at currently aimed crosshair coordinate
                viewModel.shoot(
                    targetX = state.aimPointNormalized.x * 1080f,
                    targetY = state.aimPointNormalized.y * 1920f,
                    canvasWidth = 1080f,
                    canvasHeight = 1920f
                )
            },
            onReload = { viewModel.reload() },
            onToggleAds = { viewModel.toggleAds() },
            onOpenWeaponWheel = { viewModel.toggleWeaponWheel() },
            onToggleMenu = { viewModel.toggleTacticalMenu() },
            onRespawnTargets = { viewModel.respawnTargets() },
            onNextLevel = { viewModel.advanceLevel() },
            onToggleAmbient = { viewModel.toggleAmbientAudio() },
            modifier = Modifier.fillMaxSize()
        )

        // Layer 4: Tactical Left-Side Menu Drawer (image_2.png)
        TacticalMenuDrawer(
            isOpen = state.isTacticalMenuOpen,
            isWeatherSubmenuOpen = state.isWeatherSubmenuOpen,
            currentWeather = state.weather,
            currentLevel = state.currentLevel,
            sessionDay = state.sessionDay,
            onPlay = { viewModel.closeMenus() },
            onToggleWeather = { viewModel.toggleWeatherSubmenu() },
            onSelectWeather = { mode -> viewModel.setWeather(mode) },
            onSelectLevel = { level -> viewModel.selectLocation(level) },
            onOpenArmory = { viewModel.openArmory() },
            onOpenSettings = { viewModel.openSettings() },
            onOpenLeaderboards = { viewModel.openLeaderboards() },
            onOpenWeaponWheel = {
                viewModel.closeMenus()
                viewModel.toggleWeaponWheel()
            },
            onExit = { viewModel.closeMenus() }
        )

        // Layer 5: Circular Radial Weapon Wheel Overlay (image_4.png)
        WeaponWheelOverlay(
            isOpen = state.isWeaponWheelOpen,
            currentWeapon = state.currentWeapon,
            onSelectWeapon = { weapon -> viewModel.equipWeapon(weapon) },
            onClose = { viewModel.closeWeaponWheel() }
        )

        // Layer 6: Tactical Modals (Armory, Settings, Leaderboards)
        if (state.isArmoryOpen) {
            ArmoryDialog(
                currentWeapon = state.currentWeapon,
                onEquip = { weapon -> viewModel.equipWeapon(weapon) },
                onDismiss = { viewModel.closeArmory() }
            )
        }

        if (state.isSettingsOpen) {
            SettingsDialog(
                currentWeather = state.weather,
                onSelectWeather = { mode -> viewModel.setWeather(mode) },
                onResetRange = { viewModel.respawnTargets() },
                onDismiss = { viewModel.closeSettings() }
            )
        }

        if (state.isLeaderboardsOpen) {
            LeaderboardsDialog(
                score = state.score,
                shotsFired = state.shotsFired,
                shotsHit = state.shotsHit,
                accuracy = state.accuracyPercentage,
                bottlesSmashed = state.bottlesSmashed,
                sessionDay = state.sessionDay,
                onDismiss = { viewModel.closeLeaderboards() }
            )
        }
    }
}
