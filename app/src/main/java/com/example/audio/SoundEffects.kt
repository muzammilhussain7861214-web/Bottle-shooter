package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.LocationLevel
import com.example.model.WeatherMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object SoundEffects {
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 22050

    var isMuted = false
    var isAmbientEnabled = true

    private var ambientJob: Job? = null
    private var currentLocation: LocationLevel = LocationLevel.DeepPineForest
    private var currentWeather: WeatherMode = WeatherMode.ClearDay

    fun startAmbientLoop(location: LocationLevel, weather: WeatherMode) {
        currentLocation = location
        currentWeather = weather
        if (ambientJob?.isActive == true) return

        ambientJob = scope.launch {
            while (isActive) {
                if (!isMuted && isAmbientEnabled) {
                    playAmbientAtmo(currentLocation, currentWeather)
                }
                // Ambient interval
                delay(Random.nextLong(3200L, 5500L))
            }
        }
    }

    fun updateAtmosphere(location: LocationLevel, weather: WeatherMode) {
        currentLocation = location
        currentWeather = weather
    }

    fun stopAmbientLoop() {
        ambientJob?.cancel()
        ambientJob = null
    }

    private fun playPcm(samples: ShortArray) {
        if (isMuted) return
        scope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                // Let track play out and release
                kotlinx.coroutines.delay((samples.size * 1000L) / SAMPLE_RATE + 50L)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Ignore audio init issues gracefully
            }
        }
    }

    fun playGunshot(weaponId: String) {
        when (weaponId) {
            "custom_pistol" -> {
                // Suppressed pop: quick noise pulse damped heavily
                val duration = (SAMPLE_RATE * 0.12f).toInt()
                val buffer = ShortArray(duration)
                for (i in 0 until duration) {
                    val progress = i.toFloat() / duration
                    val decay = (1f - progress) * (1f - progress)
                    val noise = Random.nextFloat() * 2f - 1f
                    val sub = sin(2.0 * Math.PI * 140.0 * (i.toDouble() / SAMPLE_RATE)).toFloat()
                    val sample = ((noise * 0.6f + sub * 0.4f) * decay * 22000).toInt()
                    buffer[i] = sample.coerceIn(-32767, 32767).toShort()
                }
                playPcm(buffer)
            }
            "revolver" -> {
                // Heavy explosive crack
                val duration = (SAMPLE_RATE * 0.28f).toInt()
                val buffer = ShortArray(duration)
                for (i in 0 until duration) {
                    val progress = i.toFloat() / duration
                    val decay = Math.exp(-progress.toDouble() * 12.0).toFloat()
                    val noise = Random.nextFloat() * 2f - 1f
                    val bass = sin(2.0 * Math.PI * (90.0 - 40.0 * progress) * (i.toDouble() / SAMPLE_RATE)).toFloat()
                    val sample = ((noise * 0.7f + bass * 0.6f) * decay * 30000).toInt()
                    buffer[i] = sample.coerceIn(-32767, 32767).toShort()
                }
                playPcm(buffer)
            }
            "shotgun" -> {
                // Wide blast
                val duration = (SAMPLE_RATE * 0.35f).toInt()
                val buffer = ShortArray(duration)
                for (i in 0 until duration) {
                    val progress = i.toFloat() / duration
                    val decay = Math.exp(-progress.toDouble() * 9.0).toFloat()
                    val noise = Random.nextFloat() * 2f - 1f
                    val bass = sin(2.0 * Math.PI * 65.0 * (i.toDouble() / SAMPLE_RATE)).toFloat()
                    val sample = ((noise * 0.8f + bass * 0.5f) * decay * 31000).toInt()
                    buffer[i] = sample.coerceIn(-32767, 32767).toShort()
                }
                playPcm(buffer)
            }
            "sniper_rifle" -> {
                // Supersonic high crack with long rumble
                val duration = (SAMPLE_RATE * 0.45f).toInt()
                val buffer = ShortArray(duration)
                for (i in 0 until duration) {
                    val progress = i.toFloat() / duration
                    val decay = Math.exp(-progress.toDouble() * 8.0).toFloat()
                    val noise = Random.nextFloat() * 2f - 1f
                    val sharpCrack = sin(2.0 * Math.PI * 880.0 * (i.toDouble() / SAMPLE_RATE)).toFloat() * (if (progress < 0.05f) 1f else 0.1f)
                    val rumble = sin(2.0 * Math.PI * 70.0 * (i.toDouble() / SAMPLE_RATE)).toFloat()
                    val sample = ((noise * 0.7f + sharpCrack * 0.3f + rumble * 0.4f) * decay * 32000).toInt()
                    buffer[i] = sample.coerceIn(-32767, 32767).toShort()
                }
                playPcm(buffer)
            }
            else -> { // SMG & Assault Rifle
                val duration = (SAMPLE_RATE * 0.15f).toInt()
                val buffer = ShortArray(duration)
                for (i in 0 until duration) {
                    val progress = i.toFloat() / duration
                    val decay = Math.exp(-progress.toDouble() * 18.0).toFloat()
                    val noise = Random.nextFloat() * 2f - 1f
                    val sample = (noise * decay * 28000).toInt()
                    buffer[i] = sample.coerceIn(-32767, 32767).toShort()
                }
                playPcm(buffer)
            }
        }
    }

    fun playGlassShatter() {
        val duration = (SAMPLE_RATE * 0.3f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val progress = i.toFloat() / duration
            val decay = Math.exp(-progress.toDouble() * 10.0).toFloat()
            // High pitch tinkles
            val chime1 = sin(2.0 * Math.PI * 2800.0 * (i.toDouble() / SAMPLE_RATE)).toFloat()
            val chime2 = sin(2.0 * Math.PI * 4100.0 * (i.toDouble() / SAMPLE_RATE)).toFloat()
            val noise = Random.nextFloat() * 2f - 1f
            val sample = ((noise * 0.6f + chime1 * 0.25f + chime2 * 0.25f) * decay * 26000).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        playPcm(buffer)
    }

    fun playRopeCut() {
        val duration = (SAMPLE_RATE * 0.1f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val progress = i.toFloat() / duration
            val decay = (1f - progress)
            val freq = 1200.0 - 600.0 * progress
            val snap = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)).toFloat()
            val noise = Random.nextFloat() * 2f - 1f
            val sample = ((snap * 0.7f + noise * 0.3f) * decay * 24000).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        playPcm(buffer)
    }

    fun playReload() {
        val duration = (SAMPLE_RATE * 0.4f).toInt()
        val buffer = ShortArray(duration)
        // Click 1 (eject) at 10%, Click 2 (insert) at 55%, Click 3 (rack) at 85%
        for (i in 0 until duration) {
            val p = i.toFloat() / duration
            val isClick1 = p in 0.08f..0.12f
            val isClick2 = p in 0.52f..0.56f
            val isClick3 = p in 0.82f..0.88f
            val env = if (isClick1 || isClick2 || isClick3) 1f else 0.05f
            val noise = Random.nextFloat() * 2f - 1f
            val clickTone = sin(2.0 * Math.PI * 1800.0 * (i.toDouble() / SAMPLE_RATE)).toFloat()
            val sample = ((noise * 0.5f + clickTone * 0.5f) * env * 22000).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        playPcm(buffer)
    }

    fun playDryFire() {
        val duration = (SAMPLE_RATE * 0.05f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val p = i.toFloat() / duration
            val decay = (1f - p) * (1f - p)
            val click = sin(2.0 * Math.PI * 2200.0 * (i.toDouble() / SAMPLE_RATE)).toFloat()
            buffer[i] = (click * decay * 18000).toInt().toShort()
        }
        playPcm(buffer)
    }

    fun playThunder() {
        val duration = (SAMPLE_RATE * 0.6f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val p = i.toFloat() / duration
            val decay = Math.sin(p.toDouble() * Math.PI).toFloat()
            val rumble = sin(2.0 * Math.PI * (55.0 + 15.0 * sin(p * 20.0)) * (i.toDouble() / SAMPLE_RATE)).toFloat()
            val noise = (Random.nextFloat() * 2f - 1f) * 0.4f
            buffer[i] = ((rumble + noise) * decay * 22000).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(buffer)
    }

    fun playWheelTick() {
        val duration = (SAMPLE_RATE * 0.03f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val p = i.toFloat() / duration
            val decay = 1f - p
            val tick = sin(2.0 * Math.PI * 1400.0 * (i.toDouble() / SAMPLE_RATE)).toFloat()
            buffer[i] = (tick * decay * 15000).toInt().toShort()
        }
        playPcm(buffer)
    }

    private fun playAmbientAtmo(location: LocationLevel, weather: WeatherMode) {
        when (location) {
            LocationLevel.DeepPineForest -> {
                // Forest birds chirping (musical chirps & warbles)
                val duration = (SAMPLE_RATE * 0.45f).toInt()
                val buffer = ShortArray(duration)
                val baseFreq = 2200.0 + Random.nextInt(400)
                for (i in 0 until duration) {
                    val p = i.toFloat() / duration
                    // Chirp frequency trajectory
                    val freq = baseFreq + 800.0 * sin(p.toDouble() * Math.PI * 4.0)
                    val envelope = (sin(p.toDouble() * Math.PI) * 0.85).toFloat()
                    val tone = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)).toFloat()
                    val windRustle = (Random.nextFloat() * 2f - 1f) * 0.08f
                    val sample = ((tone * 0.7f + windRustle) * envelope * 12000).toInt()
                    buffer[i] = sample.coerceIn(-32767, 32767).toShort()
                }
                playPcm(buffer)
            }
            LocationLevel.MistyMountainEdge -> {
                // High altitude alpine gust & hawk echo
                val duration = (SAMPLE_RATE * 0.6f).toInt()
                val buffer = ShortArray(duration)
                for (i in 0 until duration) {
                    val p = i.toFloat() / duration
                    val envelope = sin(p.toDouble() * Math.PI).toFloat()
                    val noise = (Random.nextFloat() * 2f - 1f)
                    // Low whistling wind formant
                    val whistle = sin(2.0 * Math.PI * (480.0 + 120.0 * sin(p.toDouble() * Math.PI * 2.0)) * (i.toDouble() / SAMPLE_RATE)).toFloat()
                    val sample = ((noise * 0.4f + whistle * 0.4f) * envelope * 11000).toInt()
                    buffer[i] = sample.coerceIn(-32767, 32767).toShort()
                }
                playPcm(buffer)
            }
            LocationLevel.AutumnWoods -> {
                // Autumn leaves rustling & evening crickets
                val duration = (SAMPLE_RATE * 0.5f).toInt()
                val buffer = ShortArray(duration)
                for (i in 0 until duration) {
                    val p = i.toFloat() / duration
                    val cricketStrum = sin(p.toDouble() * Math.PI * 24.0).toFloat()
                    val cricketFreq = 4200.0
                    val tone = sin(2.0 * Math.PI * cricketFreq * (i.toDouble() / SAMPLE_RATE)).toFloat()
                    val env = (sin(p.toDouble() * Math.PI) * (if (cricketStrum > 0f) cricketStrum else 0f)).toFloat()
                    val rustle = (Random.nextFloat() * 2f - 1f) * 0.12f
                    val sample = ((tone * 0.5f * env + rustle) * 10000).toInt()
                    buffer[i] = sample.coerceIn(-32767, 32767).toShort()
                }
                playPcm(buffer)
            }
            LocationLevel.OvergrownJungleRuins -> {
                // Jungle cicadas & tropical bird trill
                val duration = (SAMPLE_RATE * 0.55f).toInt()
                val buffer = ShortArray(duration)
                for (i in 0 until duration) {
                    val p = i.toFloat() / duration
                    val cicadaMod = sin(p.toDouble() * Math.PI * 18.0).toFloat()
                    val cicadaTone = sin(2.0 * Math.PI * 3400.0 * (i.toDouble() / SAMPLE_RATE)).toFloat()
                    val birdTrill = sin(2.0 * Math.PI * (1600.0 + 600.0 * sin(p.toDouble() * Math.PI * 6.0)) * (i.toDouble() / SAMPLE_RATE)).toFloat()
                    val env = sin(p.toDouble() * Math.PI).toFloat()
                    val sample = ((cicadaTone * 0.35f * cicadaMod + birdTrill * 0.35f) * env * 11500).toInt()
                    buffer[i] = sample.coerceIn(-32767, 32767).toShort()
                }
                playPcm(buffer)
            }
        }
    }

    fun playLevelComplete() {
        // High crystal fanfare chime
        val duration = (SAMPLE_RATE * 0.65f).toInt()
        val buffer = ShortArray(duration)
        val chordNotes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        for (i in 0 until duration) {
            val p = i.toFloat() / duration
            val decay = (1f - p) * (1f - p)
            var sumTone = 0f
            for (freq in chordNotes) {
                sumTone += sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)).toFloat()
            }
            val sample = ((sumTone / chordNotes.size) * decay * 26000).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        playPcm(buffer)
    }
}
