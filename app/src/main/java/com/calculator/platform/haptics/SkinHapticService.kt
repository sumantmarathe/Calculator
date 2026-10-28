package com.calculator.platform.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.calculator.ui.theme.SkinType

interface HapticService {
    fun performClick(skin: SkinType)
    fun performError(skin: SkinType)
}

class SkinHapticService(private val context: Context?) : HapticService {

    private val vibrator: Vibrator? by lazy {
        try {
            if (context == null) return@lazy null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun performClick(skin: SkinType) {
        val vib = vibrator ?: return
        try {
            if (!vib.hasVibrator()) return
            when (skin) {
                SkinType.RETRO_PIXEL -> {
                    // Crisp mechanical tactile tick
                    vib.vibrate(VibrationEffect.createOneShot(18L, 220))
                }
                SkinType.MODERN -> {
                    // Subtle modern micro-pulse
                    vib.vibrate(VibrationEffect.createOneShot(10L, 110))
                }
                SkinType.FUTURISTIC -> {
                    // Sci-fi double-pulse HUD feedback
                    val timings = longArrayOf(0L, 8L, 22L, 12L)
                    val amplitudes = intArrayOf(0, 160, 0, 210)
                    vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                }
            }
        } catch (ignored: Exception) {
            // Safe no-op in headless/test or permission-restricted environments
        }
    }

    override fun performError(skin: SkinType) {
        val vib = vibrator ?: return
        try {
            if (!vib.hasVibrator()) return
            val timings = longArrayOf(0L, 30L, 40L, 45L)
            val amplitudes = intArrayOf(0, 240, 0, 255)
            vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } catch (ignored: Exception) {
        }
    }
}
