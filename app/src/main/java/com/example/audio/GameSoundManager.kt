package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural synthesized sound engine and haptic feedback manager.
 * Delivers zero-latency, joyful audio without external asset dependencies.
 */
class GameSoundManager(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Default + Job())
    private var musicJob: Job? = null

    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                stopMusic()
            } else {
                startMusic()
            }
        }

    var isSfxEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val sampleRate = 22050

    // Pre-synthesized PCM buffers for instant playback
    private val coinSound: ShortArray by lazy { synthesizeChime(listOf(659.25, 783.99, 1046.50), 0.18f) }
    private val starSound: ShortArray by lazy { synthesizeChime(listOf(523.25, 659.25, 783.99, 1046.50, 1318.51), 0.28f) }
    private val carrotSound: ShortArray by lazy { synthesizeFanfare(listOf(392.0, 523.25, 659.25, 783.99, 1046.50), 0.45f) }
    private val jumpSound: ShortArray by lazy { synthesizeSweep(320.0, 680.0, 0.16f, isUp = true) }
    private val slideSound: ShortArray by lazy { synthesizeSweep(480.0, 240.0, 0.14f, isUp = false) }
    private val bumpSound: ShortArray by lazy { synthesizeBump(180.0, 90.0, 0.15f) }
    private val clickSound: ShortArray by lazy { synthesizeClick(880.0, 0.04f) }
    private val unlockSound: ShortArray by lazy { synthesizeFanfare(listOf(523.25, 659.25, 783.99, 987.77, 1046.50), 0.5f) }
    private val gameOverSound: ShortArray by lazy { synthesizeChime(listOf(587.33, 523.25, 440.0, 392.0), 0.45f) }

    fun playJump() {
        if (isSfxEnabled) playPcm(jumpSound)
        triggerHaptic(20)
    }

    fun playSlide() {
        if (isSfxEnabled) playPcm(slideSound)
        triggerHaptic(15)
    }

    fun playCoin() {
        if (isSfxEnabled) playPcm(coinSound)
        triggerHaptic(12)
    }

    fun playStar() {
        if (isSfxEnabled) playPcm(starSound)
        triggerHaptic(25)
    }

    fun playCarrot() {
        if (isSfxEnabled) playPcm(carrotSound)
        triggerHaptic(40)
    }

    fun playBump() {
        if (isSfxEnabled) playPcm(bumpSound)
        triggerHaptic(60)
    }

    fun playClick() {
        if (isSfxEnabled) playPcm(clickSound)
        triggerHaptic(10)
    }

    fun playUnlock() {
        if (isSfxEnabled) playPcm(unlockSound)
        triggerHaptic(45)
    }

    fun playGameOver() {
        if (isSfxEnabled) playPcm(gameOverSound)
        triggerHaptic(50)
    }

    fun startMusic() {
        if (!isMusicEnabled || musicJob?.isActive == true) return
        musicJob = audioScope.launch {
            // Cheerful, gentle pentatonic melody loop
            val melody = listOf(
                Pair(523.25, 250L), // C5
                Pair(587.33, 250L), // D5
                Pair(659.25, 250L), // E5
                Pair(783.99, 250L), // G5
                Pair(880.00, 350L), // A5
                Pair(783.99, 250L), // G5
                Pair(659.25, 250L), // E5
                Pair(523.25, 400L), // C5
                Pair(587.33, 250L), // D5
                Pair(659.25, 250L), // E5
                Pair(783.99, 250L), // G5
                Pair(1046.50, 450L), // C6
                Pair(880.00, 300L), // A5
                Pair(783.99, 300L), // G5
                Pair(659.25, 400L), // E5
                Pair(523.25, 500L)  // C5
            )

            while (isActive && isMusicEnabled) {
                for (note in melody) {
                    if (!isActive || !isMusicEnabled) break
                    val pcm = synthesizeSoftNote(note.first, (note.second * 0.85f / 1000f), 0.18f)
                    playPcm(pcm)
                    delay(note.second)
                }
                delay(300L)
            }
        }
    }

    fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun playPcm(pcm: ShortArray) {
        audioScope.launch {
            try {
                val bufferSize = pcm.size * 2
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(pcm, 0, pcm.size)
                audioTrack.play()
                val durationMs = (pcm.size * 1000L) / sampleRate
                delay(durationMs + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Audio device busy or interrupted
            }
        }
    }

    private fun triggerHaptic(milliseconds: Long) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        milliseconds.coerceIn(10, 100),
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(milliseconds)
            }
        } catch (_: Exception) {
            // Vibration permission or hardware missing
        }
    }

    // --- Procedural synthesis helpers ---

    private fun synthesizeSweep(startFreq: Double, endFreq: Double, durationSec: Float, isUp: Boolean): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / totalSamples
            val freq = if (isUp) {
                startFreq + (endFreq - startFreq) * t * t
            } else {
                startFreq + (endFreq - startFreq) * t
            }
            phase += 2.0 * PI * freq / sampleRate
            val env = 1.0 - (i.toDouble() / totalSamples)
            val sample = (sin(phase) * env * 0.45 * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeChime(frequencies: List<Double>, durationSec: Float): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        val noteLength = totalSamples / frequencies.size

        for (n in frequencies.indices) {
            val freq = frequencies[n]
            val offset = n * noteLength
            var phase = 0.0
            for (i in 0 until noteLength) {
                val idx = offset + i
                if (idx >= totalSamples) break
                phase += 2.0 * PI * freq / sampleRate
                val t = i.toDouble() / noteLength
                val envelope = exp(-3.0 * t)
                // Fundamental + pleasant 2nd harmonic
                val wave = (sin(phase) + 0.3 * sin(2.0 * phase)) / 1.3
                val sample = (wave * envelope * 0.45 * Short.MAX_VALUE).toInt()
                buffer[idx] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return buffer
    }

    private fun synthesizeFanfare(frequencies: List<Double>, durationSec: Float): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        val noteLength = totalSamples / (frequencies.size + 1)

        for (n in frequencies.indices) {
            val freq = frequencies[n]
            val offset = n * noteLength
            var phase = 0.0
            val len = if (n == frequencies.lastIndex) noteLength * 2 else noteLength
            for (i in 0 until len) {
                val idx = offset + i
                if (idx >= totalSamples) break
                phase += 2.0 * PI * freq / sampleRate
                val t = i.toDouble() / len
                val envelope = exp(-2.2 * t)
                val wave = (sin(phase) + 0.4 * sin(2.0 * phase) + 0.2 * sin(3.0 * phase)) / 1.6
                val sample = (wave * envelope * 0.42 * Short.MAX_VALUE).toInt()
                val current = buffer[idx].toInt()
                val combined = (current + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[idx] = combined.toShort()
            }
        }
        return buffer
    }

    private fun synthesizeBump(startFreq: Double, endFreq: Double, durationSec: Float): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / totalSamples
            val freq = startFreq + (endFreq - startFreq) * t
            phase += 2.0 * PI * freq / sampleRate
            val env = exp(-6.0 * t)
            val sample = (sin(phase) * env * 0.5 * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeClick(freq: Double, durationSec: Float): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / totalSamples
            phase += 2.0 * PI * freq / sampleRate
            val env = exp(-12.0 * t)
            val sample = (sin(phase) * env * 0.3 * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeSoftNote(freq: Double, durationSec: Float, volume: Float): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / totalSamples
            phase += 2.0 * PI * freq / sampleRate
            val env = (1.0 - t) * (1.0 - exp(-15.0 * t))
            val wave = (sin(phase) + 0.25 * sin(2.0 * phase)) / 1.25
            val sample = (wave * env * volume * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    fun release() {
        stopMusic()
    }
}
