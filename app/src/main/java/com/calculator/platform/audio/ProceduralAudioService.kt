package com.calculator.platform.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.calculator.ui.theme.SkinType
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

interface AudioService {
    fun playButtonClick(skin: SkinType)
    fun release()
}

/**
 * Zero-asset procedural PCM waveform synthesizer.
 * Generates skin-specific tactile audio signatures in memory:
 * - RETRO_PIXEL: Crisp square-wave mechanical pocket-calculator click
 * - MODERN: Warm sine-harmonic soft studio tap
 * - FUTURISTIC: Frequency-modulated sci-fi HUD chirp
 */
class ProceduralAudioService(private val enabledInRuntime: Boolean = true) : AudioService {

    private val sampleRate = 22050
    private val retroPcm: ShortArray = synthesizeWaveform(SkinType.RETRO_PIXEL)
    private val modernPcm: ShortArray = synthesizeWaveform(SkinType.MODERN)
    private val futuristicPcm: ShortArray = synthesizeWaveform(SkinType.FUTURISTIC)

    @Volatile
    private var released = false

    fun getWaveformSamples(skin: SkinType): ShortArray = when (skin) {
        SkinType.RETRO_PIXEL -> retroPcm
        SkinType.MODERN -> modernPcm
        SkinType.FUTURISTIC -> futuristicPcm
    }

    override fun playButtonClick(skin: SkinType) {
        if (released || !enabledInRuntime) return
        val samples = getWaveformSamples(skin)
        thread(start = true, isDaemon = true, name = "CalcAudioSynth") {
            try {
                val byteSize = samples.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
                    .setBufferSizeInBytes(byteSize.coerceAtLeast(1024))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                Thread.sleep(45)
                track.stop()
                track.release()
            } catch (ignored: Throwable) {
                // Safe fallback on headless JVM/Robolectric
            }
        }
    }

    override fun release() {
        released = true
    }

    private fun synthesizeWaveform(skin: SkinType): ShortArray {
        val durationMs = when (skin) {
            SkinType.RETRO_PIXEL -> 18
            SkinType.MODERN -> 22
            SkinType.FUTURISTIC -> 32
        }
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val envelope = exp(-progress * 6.5)

            val rawSample = when (skin) {
                SkinType.RETRO_PIXEL -> {
                    // 1-bit style square wave burst at 1200 Hz -> 600 Hz
                    val freq = 1200.0 - 500.0 * progress
                    val s = sin(2.0 * PI * freq * t)
                    if (s >= 0.0) 0.65 else -0.65
                }
                SkinType.MODERN -> {
                    // Soft low-passed sine tap at 480 Hz + octave harmonic
                    val f1 = sin(2.0 * PI * 480.0 * t)
                    val f2 = 0.3 * sin(2.0 * PI * 960.0 * t)
                    (f1 + f2) * 0.55
                }
                SkinType.FUTURISTIC -> {
                    // Sci-fi upward FM chirp 880 Hz -> 1960 Hz
                    val instFreq = 880.0 + 1080.0 * progress
                    sin(2.0 * PI * instFreq * t) * 0.6
                }
            }

            val value = (rawSample * envelope * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = value.toShort()
        }
        return buffer
    }
}
