package com.calculator.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.calculator.engine.AngleUnit
import com.calculator.engine.programmer.NumberBase
import com.calculator.engine.programmer.WordSize
import com.calculator.ui.CalculatorMode
import com.calculator.ui.theme.SkinType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserPreferences(
    val skin: SkinType = SkinType.MODERN,
    val mode: CalculatorMode = CalculatorMode.NORMAL,
    val angleUnit: AngleUnit = AngleUnit.DEGREES,
    val wordSize: WordSize = WordSize.QWORD,
    val numberBase: NumberBase = NumberBase.DEC,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = false
)

interface PreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    val currentPreferences: UserPreferences
    suspend fun setSkin(skin: SkinType)
    suspend fun setCalculatorMode(mode: CalculatorMode)
    suspend fun setAngleUnit(unit: AngleUnit)
    suspend fun setWordSize(wordSize: WordSize)
    suspend fun setNumberBase(base: NumberBase)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setSoundEnabled(enabled: Boolean)
}

/**
 * Local-only offline preferences repository backed by Android SharedPreferences
 * (or in-memory fallback when Context is null in pure JVM tests).
 * Strictly zero network or cloud sync (`allowBackup="false"` enforced in Manifest).
 */
class DataStorePreferencesRepository(context: Context? = null) : PreferencesRepository {

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadInitial())
    override val userPreferences: Flow<UserPreferences> = _state.asStateFlow()
    override val currentPreferences: UserPreferences get() = _state.value

    private fun loadInitial(): UserPreferences {
        val p = prefs ?: return UserPreferences()
        val skinName = p.getString(KEY_SKIN, SkinType.MODERN.name) ?: SkinType.MODERN.name
        val modeName = p.getString(KEY_MODE, CalculatorMode.NORMAL.name) ?: CalculatorMode.NORMAL.name
        val angleName = p.getString(KEY_ANGLE, AngleUnit.DEGREES.name) ?: AngleUnit.DEGREES.name
        val wordName = p.getString(KEY_WORD_SIZE, WordSize.QWORD.name) ?: WordSize.QWORD.name
        val baseName = p.getString(KEY_BASE, NumberBase.DEC.name) ?: NumberBase.DEC.name
        val haptics = p.getBoolean(KEY_HAPTICS, true)
        val sound = p.getBoolean(KEY_SOUND, false)

        return UserPreferences(
            skin = runCatching { SkinType.valueOf(skinName) }.getOrDefault(SkinType.MODERN),
            mode = runCatching { CalculatorMode.valueOf(modeName) }.getOrDefault(CalculatorMode.NORMAL),
            angleUnit = runCatching { AngleUnit.valueOf(angleName) }.getOrDefault(AngleUnit.DEGREES),
            wordSize = runCatching { WordSize.valueOf(wordName) }.getOrDefault(WordSize.QWORD),
            numberBase = runCatching { NumberBase.valueOf(baseName) }.getOrDefault(NumberBase.DEC),
            hapticsEnabled = haptics,
            soundEnabled = sound
        )
    }

    override suspend fun setSkin(skin: SkinType) {
        prefs?.edit()?.putString(KEY_SKIN, skin.name)?.apply()
        _state.value = _state.value.copy(skin = skin)
    }

    override suspend fun setCalculatorMode(mode: CalculatorMode) {
        prefs?.edit()?.putString(KEY_MODE, mode.name)?.apply()
        _state.value = _state.value.copy(mode = mode)
    }

    override suspend fun setAngleUnit(unit: AngleUnit) {
        prefs?.edit()?.putString(KEY_ANGLE, unit.name)?.apply()
        _state.value = _state.value.copy(angleUnit = unit)
    }

    override suspend fun setWordSize(wordSize: WordSize) {
        prefs?.edit()?.putString(KEY_WORD_SIZE, wordSize.name)?.apply()
        _state.value = _state.value.copy(wordSize = wordSize)
    }

    override suspend fun setNumberBase(base: NumberBase) {
        prefs?.edit()?.putString(KEY_BASE, base.name)?.apply()
        _state.value = _state.value.copy(numberBase = base)
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_HAPTICS, enabled)?.apply()
        _state.value = _state.value.copy(hapticsEnabled = enabled)
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_SOUND, enabled)?.apply()
        _state.value = _state.value.copy(soundEnabled = enabled)
    }

    companion object {
        const val PREFS_NAME = "calculator_local_preferences"
        private const val KEY_SKIN = "pref_skin"
        private const val KEY_MODE = "pref_mode"
        private const val KEY_ANGLE = "pref_angle"
        private const val KEY_WORD_SIZE = "pref_word_size"
        private const val KEY_BASE = "pref_base"
        private const val KEY_HAPTICS = "pref_haptics"
        private const val KEY_SOUND = "pref_sound"
    }
}
