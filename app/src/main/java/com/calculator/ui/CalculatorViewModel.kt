package com.calculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calculator.data.history.HistoryItem
import com.calculator.data.history.HistoryRepository
import com.calculator.data.history.LocalHistoryRepository
import com.calculator.data.preferences.DataStorePreferencesRepository
import com.calculator.data.preferences.PreferencesRepository
import com.calculator.engine.AngleUnit
import com.calculator.engine.EvaluationResult
import com.calculator.engine.core.Precision
import com.calculator.engine.normal.NormalCalculatorEngine
import com.calculator.engine.programmer.BitwiseOp
import com.calculator.engine.programmer.NumberBase
import com.calculator.engine.programmer.ProgrammerEngine
import com.calculator.engine.programmer.ProgrammerEngineImpl
import com.calculator.engine.programmer.ShiftType
import com.calculator.engine.programmer.WordSize
import com.calculator.engine.scientific.ScientificCalculatorEngine
import com.calculator.platform.audio.AudioService
import com.calculator.platform.audio.ProceduralAudioService
import com.calculator.platform.haptics.HapticService
import com.calculator.platform.haptics.SkinHapticService
import com.calculator.ui.theme.SkinType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val preferencesRepository: PreferencesRepository = DataStorePreferencesRepository(null),
    private val historyRepository: HistoryRepository = LocalHistoryRepository(null),
    private val hapticService: HapticService = SkinHapticService(null),
    private val audioService: AudioService = ProceduralAudioService(false),
    private val normalEngine: NormalCalculatorEngine = NormalCalculatorEngine(),
    private val scientificEngine: ScientificCalculatorEngine = ScientificCalculatorEngine(),
    private val programmerEngine: ProgrammerEngine = ProgrammerEngineImpl()
) : ViewModel() {

    private var programmerPendingLeftOperand: ULong? = null
    private var programmerPendingBitwiseOp: BitwiseOp? = null
    private var programmerPendingShiftOp: ShiftType? = null
    private var programmerResetNextDigit: Boolean = false
    private var justEvaluated: Boolean = false

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.userPreferences.collect { prefs ->
                programmerEngine.setWordSize(prefs.wordSize)
                programmerEngine.setBase(prefs.numberBase)
                _uiState.update { current ->
                    current.copy(
                        skin = prefs.skin,
                        mode = prefs.mode,
                        angleUnit = prefs.angleUnit,
                        programmerWordSize = prefs.wordSize,
                        programmerBase = prefs.numberBase,
                        programmerRepresentations = programmerEngine.formatAllBases(),
                        hapticsEnabled = prefs.hapticsEnabled,
                        soundEnabled = prefs.soundEnabled
                    )
                }
            }
        }
        viewModelScope.launch {
            historyRepository.historyTape.collect { items ->
                _uiState.update { it.copy(history = items) }
            }
        }
    }

    private fun createInitialState(): CalculatorUiState {
        val prefs = preferencesRepository.currentPreferences
        programmerEngine.setWordSize(prefs.wordSize)
        programmerEngine.setBase(prefs.numberBase)
        return CalculatorUiState(
            mode = prefs.mode,
            skin = prefs.skin,
            angleUnit = prefs.angleUnit,
            programmerWordSize = prefs.wordSize,
            programmerBase = prefs.numberBase,
            programmerRepresentations = programmerEngine.formatAllBases(),
            hapticsEnabled = prefs.hapticsEnabled,
            soundEnabled = prefs.soundEnabled,
            history = historyRepository.currentHistory
        )
    }

    private fun triggerFeedback(isError: Boolean = false) {
        val state = _uiState.value
        if (state.hapticsEnabled) {
            if (isError) hapticService.performError(state.skin)
            else hapticService.performClick(state.skin)
        }
        if (state.soundEnabled) {
            audioService.playButtonClick(state.skin)
        }
    }

    fun setMode(mode: CalculatorMode) {
        triggerFeedback()
        _uiState.update { it.copy(mode = mode, isError = false, errorMessage = null) }
        viewModelScope.launch {
            preferencesRepository.setCalculatorMode(mode)
        }
    }

    fun setSkin(skin: SkinType) {
        _uiState.update { it.copy(skin = skin) }
        if (_uiState.value.hapticsEnabled) hapticService.performClick(skin)
        if (_uiState.value.soundEnabled) audioService.playButtonClick(skin)
        viewModelScope.launch {
            preferencesRepository.setSkin(skin)
        }
    }

    fun cycleSkin() {
        val skins = SkinType.entries
        val nextIndex = (skins.indexOf(_uiState.value.skin) + 1) % skins.size
        setSkin(skins[nextIndex])
    }

    fun cycleAngleUnit() {
        triggerFeedback()
        val units = AngleUnit.entries
        val nextUnit = units[(units.indexOf(_uiState.value.angleUnit) + 1) % units.size]
        _uiState.update { state ->
            val preview = computePreview(state.expression, state.mode, nextUnit)
            state.copy(angleUnit = nextUnit, livePreview = preview)
        }
        viewModelScope.launch {
            preferencesRepository.setAngleUnit(nextUnit)
        }
    }

    fun setAngleUnit(unit: AngleUnit) {
        triggerFeedback()
        _uiState.update { state ->
            val preview = computePreview(state.expression, state.mode, unit)
            state.copy(angleUnit = unit, livePreview = preview)
        }
        viewModelScope.launch {
            preferencesRepository.setAngleUnit(unit)
        }
    }

    fun toggleSecondFunction() {
        triggerFeedback()
        _uiState.update { it.copy(isSecondFunctionActive = !it.isSecondFunctionActive) }
    }

    fun toggleBitMatrix() {
        triggerFeedback()
        _uiState.update { it.copy(showBitMatrix = !it.showBitMatrix) }
    }

    fun toggleHistorySheet(show: Boolean = !_uiState.value.showHistorySheet) {
        triggerFeedback()
        _uiState.update { it.copy(showHistorySheet = show) }
    }

    fun toggleSettingsDialog(show: Boolean = !_uiState.value.showSettingsDialog) {
        triggerFeedback()
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(hapticsEnabled = enabled) }
        viewModelScope.launch {
            preferencesRepository.setHapticsEnabled(enabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _uiState.update { it.copy(soundEnabled = enabled) }
        viewModelScope.launch {
            preferencesRepository.setSoundEnabled(enabled)
        }
    }

    // =========================================================================
    // Normal & Scientific Input Actions
    // =========================================================================

    fun onDigit(digit: String) {
        triggerFeedback()
        val state = _uiState.value
        if (state.mode == CalculatorMode.PROGRAMMER) {
            if (digit.isNotEmpty()) onProgrammerDigit(digit[0])
            return
        }

        val currentExpr = if (justEvaluated || state.isError) "" else state.expression
        justEvaluated = false

        val nextExpr = if (currentExpr == "0" && digit != ".") digit else currentExpr + digit
        updateExpressionState(nextExpr)
    }

    fun onDecimalPoint() {
        triggerFeedback()
        val state = _uiState.value
        if (state.mode == CalculatorMode.PROGRAMMER) return

        val currentExpr = if (justEvaluated || state.isError) "0" else state.expression
        justEvaluated = false

        // Extract last number token to avoid duplicate decimal points
        val lastNumberToken = currentExpr.takeLastWhile { it.isDigit() || it == '.' }
        if (lastNumberToken.contains('.')) return

        val nextExpr = if (currentExpr.isEmpty() || !currentExpr.last().isDigit()) {
            "${currentExpr}0."
        } else {
            "$currentExpr."
        }
        updateExpressionState(nextExpr)
    }

    fun onOperator(op: String) {
        triggerFeedback()
        val state = _uiState.value
        if (state.mode == CalculatorMode.PROGRAMMER) {
            onProgrammerOperator(op)
            return
        }

        val baseExpr = when {
            state.isError -> ""
            justEvaluated -> state.displayResult
            state.expression.isEmpty() && state.displayResult != "0" -> state.displayResult
            else -> state.expression
        }
        justEvaluated = false

        if (baseExpr.isEmpty()) {
            if (op == "-" || op == "−") {
                updateExpressionState("-")
            } else {
                updateExpressionState("0$op")
            }
            return
        }

        val trimmed = baseExpr.trimEnd()
        val lastChar = trimmed.lastOrNull()
        val nextExpr = if (lastChar != null && lastChar in "+-*/×÷−^") {
            if ((op == "-" || op == "−") && lastChar in "*/×÷^") {
                "$trimmed-"
            } else {
                trimmed.dropLast(1) + op
            }
        } else {
            "$trimmed$op"
        }
        updateExpressionState(nextExpr)
    }

    fun onScientificFunction(func: String) {
        triggerFeedback()
        val state = _uiState.value
        val currentExpr = if (justEvaluated || state.isError) {
            if (justEvaluated && !state.isError && state.displayResult != "0") {
                justEvaluated = false
                when (func) {
                    "x²" -> "(${state.displayResult})^2"
                    "x³" -> "(${state.displayResult})^3"
                    "xʸ" -> "(${state.displayResult})^"
                    "1/x" -> "1/(${state.displayResult})"
                    "n!" -> "(${state.displayResult})!"
                    else -> "$func(${state.displayResult})"
                }.also {
                    updateExpressionState(it)
                    return
                }
            }
            ""
        } else {
            state.expression
        }
        justEvaluated = false

        val nextExpr = when (func) {
            "π" -> "${currentExpr}π"
            "e" -> "${currentExpr}e"
            "x²" -> if (currentExpr.isEmpty()) "0^2" else "$currentExpr^2"
            "x³" -> if (currentExpr.isEmpty()) "0^3" else "$currentExpr^3"
            "xʸ" -> if (currentExpr.isEmpty()) "0^" else "$currentExpr^"
            "1/x" -> if (currentExpr.isEmpty()) "1/(" else "${currentExpr}1/("
            "n!" -> if (currentExpr.isEmpty()) "0!" else "$currentExpr!"
            "eˣ" -> "${currentExpr}exp("
            "10ˣ" -> "${currentExpr}pow10("
            "√" -> "${currentExpr}sqrt("
            "∛" -> "${currentExpr}cbrt("
            "|x|" -> "${currentExpr}abs("
            "mod" -> if (currentExpr.isEmpty()) "0 mod " else "$currentExpr mod "
            else -> "$currentExpr$func("
        }
        updateExpressionState(nextExpr)
    }

    fun onParenthesis(paren: String) {
        triggerFeedback()
        val state = _uiState.value
        val currentExpr = if (justEvaluated || state.isError) "" else state.expression
        justEvaluated = false
        updateExpressionState(currentExpr + paren)
    }

    fun onSmartParenthesis() {
        triggerFeedback()
        val state = _uiState.value
        val currentExpr = if (justEvaluated || state.isError) "" else state.expression
        justEvaluated = false
        val openCount = currentExpr.count { it == '(' }
        val closeCount = currentExpr.count { it == ')' }
        val lastChar = currentExpr.trimEnd().lastOrNull()
        val nextParen = if (openCount > closeCount && lastChar != null && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '!')) {
            ")"
        } else {
            "("
        }
        updateExpressionState(currentExpr + nextParen)
    }

    fun onPercentage() {
        triggerFeedback()
        val state = _uiState.value
        if (state.isError) return
        val currentExpr = if (justEvaluated) state.displayResult else state.expression
        justEvaluated = false
        if (currentExpr.isEmpty()) return
        if (!currentExpr.endsWith("%")) {
            updateExpressionState("$currentExpr%")
        }
    }

    fun onNegate() {
        triggerFeedback()
        val state = _uiState.value
        if (state.mode == CalculatorMode.PROGRAMMER) {
            programmerEngine.negateTwosComplement()
            syncProgrammerState()
            return
        }
        val currentExpr = if (justEvaluated) state.displayResult else state.expression
        justEvaluated = false
        val nextExpr = when {
            currentExpr.isEmpty() || currentExpr == "0" -> "-"
            currentExpr.startsWith("-") -> currentExpr.removePrefix("-")
            else -> "-($currentExpr)"
        }
        updateExpressionState(nextExpr)
    }

    fun onClear() {
        triggerFeedback()
        justEvaluated = false
        if (_uiState.value.mode == CalculatorMode.PROGRAMMER) {
            programmerEngine.clear()
            programmerPendingLeftOperand = null
            programmerPendingBitwiseOp = null
            programmerPendingShiftOp = null
            programmerResetNextDigit = false
            syncProgrammerState(pendingOpLabel = null)
        } else {
            _uiState.update {
                it.copy(
                    expression = "",
                    displayResult = "0",
                    livePreview = null,
                    isError = false,
                    errorMessage = null
                )
            }
        }
    }

    fun onBackspace() {
        triggerFeedback()
        val state = _uiState.value
        if (state.mode == CalculatorMode.PROGRAMMER) {
            programmerEngine.backspace()
            syncProgrammerState()
            return
        }
        if (state.isError || justEvaluated) {
            onClear()
            return
        }
        if (state.expression.isNotEmpty()) {
            val dropped = when {
                state.expression.endsWith(" mod ") -> state.expression.dropLast(5)
                else -> state.expression.dropLast(1)
            }
            updateExpressionState(dropped)
        }
    }

    fun onEquals() {
        val state = _uiState.value
        if (state.mode == CalculatorMode.PROGRAMMER) {
            onProgrammerEquals()
            return
        }

        val exprToEval = state.expression.ifBlank { state.displayResult }
        if (exprToEval.isBlank()) return

        val engine = if (state.mode == CalculatorMode.SCIENTIFIC) scientificEngine else normalEngine
        when (val result = engine.evaluate(exprToEval, state.angleUnit)) {
            is EvaluationResult.Success -> {
                triggerFeedback(isError = false)
                justEvaluated = true
                _uiState.update {
                    it.copy(
                        displayResult = result.formatted,
                        livePreview = null,
                        isError = false,
                        errorMessage = null
                    )
                }
                viewModelScope.launch {
                    historyRepository.addRecord(
                        expression = exprToEval,
                        result = result.formatted,
                        mode = state.mode
                    )
                }
            }
            is EvaluationResult.DomainError -> {
                triggerFeedback(isError = true)
                _uiState.update {
                    it.copy(
                        displayResult = result.message,
                        livePreview = null,
                        isError = true,
                        errorMessage = result.message
                    )
                }
            }
            is EvaluationResult.SyntaxError -> {
                triggerFeedback(isError = true)
                _uiState.update {
                    it.copy(
                        displayResult = result.message,
                        livePreview = null,
                        isError = true,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    // =========================================================================
    // Memory Register Actions (M+, M-, MR, MC)
    // =========================================================================

    fun onMemoryAdd() {
        triggerFeedback()
        val state = _uiState.value
        val target = state.expression.ifBlank { state.displayResult }
        if (normalEngine.addToMemory(target, state.angleUnit)) {
            syncMemoryState()
        }
    }

    fun onMemorySubtract() {
        triggerFeedback()
        val state = _uiState.value
        val target = state.expression.ifBlank { state.displayResult }
        if (normalEngine.subtractFromMemory(target, state.angleUnit)) {
            syncMemoryState()
        }
    }

    fun onMemoryRecall() {
        triggerFeedback()
        val recalled = Precision.format(normalEngine.memory.recall())
        val currentExpr = if (justEvaluated || _uiState.value.isError) "" else _uiState.value.expression
        justEvaluated = false
        val nextExpr = if (currentExpr.isEmpty() || currentExpr.last() in "+-*/×÷−^(") {
            currentExpr + recalled
        } else {
            recalled
        }
        updateExpressionState(nextExpr)
    }

    fun onMemoryClear() {
        triggerFeedback()
        normalEngine.memory.clear()
        syncMemoryState()
    }

    private fun syncMemoryState() {
        val memValue = normalEngine.memory.recall()
        _uiState.update {
            it.copy(
                hasMemory = normalEngine.memory.hasMemory,
                memoryFormatted = Precision.format(memValue)
            )
        }
    }

    // =========================================================================
    // Programmer Mode Actions
    // =========================================================================

    fun setProgrammerWordSize(wordSize: WordSize) {
        triggerFeedback()
        programmerEngine.setWordSize(wordSize)
        syncProgrammerState()
        viewModelScope.launch {
            preferencesRepository.setWordSize(wordSize)
        }
    }

    fun cycleProgrammerWordSize() {
        val sizes = WordSize.entries
        val next = sizes[(sizes.indexOf(_uiState.value.programmerWordSize) + 1) % sizes.size]
        setProgrammerWordSize(next)
    }

    fun setProgrammerBase(base: NumberBase) {
        triggerFeedback()
        programmerEngine.setBase(base)
        syncProgrammerState()
        viewModelScope.launch {
            preferencesRepository.setNumberBase(base)
        }
    }

    fun toggleProgrammerSigned() {
        triggerFeedback()
        val nextSigned = !_uiState.value.programmerSigned
        programmerEngine.setSignedDisplay(nextSigned)
        syncProgrammerState()
    }

    fun onProgrammerDigit(digitChar: Char) {
        if (programmerResetNextDigit) {
            programmerEngine.clear()
            programmerResetNextDigit = false
        }
        val accepted = programmerEngine.appendDigit(digitChar)
        if (!accepted) {
            triggerFeedback(isError = true)
            return
        }
        syncProgrammerState()
    }

    fun onProgrammerBitToggle(bitIndex: Int) {
        triggerFeedback()
        programmerEngine.toggleBit(bitIndex)
        syncProgrammerState()
    }

    fun onProgrammerBitwiseOp(op: BitwiseOp) {
        triggerFeedback()
        if (op == BitwiseOp.NOT) {
            programmerEngine.executeBitwise(BitwiseOp.NOT)
            syncProgrammerState()
            return
        }
        commitPendingProgrammerOpIfPresent()
        programmerPendingLeftOperand = programmerEngine.activeValue
        programmerPendingBitwiseOp = op
        programmerPendingShiftOp = null
        programmerResetNextDigit = true
        syncProgrammerState(pendingOpLabel = "${programmerEngine.formatActiveDisplay()} ${op.symbol}")
    }

    fun onProgrammerShiftOp(shiftType: ShiftType) {
        triggerFeedback()
        if (shiftType == ShiftType.ROL || shiftType == ShiftType.ROR) {
            programmerEngine.shift(shiftType, 1)
            syncProgrammerState()
            return
        }
        commitPendingProgrammerOpIfPresent()
        programmerPendingLeftOperand = programmerEngine.activeValue
        programmerPendingShiftOp = shiftType
        programmerPendingBitwiseOp = null
        programmerResetNextDigit = true
        syncProgrammerState(pendingOpLabel = "${programmerEngine.formatActiveDisplay()} ${shiftType.symbol}")
    }

    private fun onProgrammerOperator(opSymbol: String) {
        val mapped = when (opSymbol) {
            "+" -> BitwiseOp.ADD
            "-", "−" -> BitwiseOp.SUB
            "*", "×" -> BitwiseOp.MUL
            "/", "÷" -> BitwiseOp.DIV
            "%" -> BitwiseOp.MOD
            else -> return
        }
        onProgrammerBitwiseOp(mapped)
    }

    private fun onProgrammerEquals() {
        val left = programmerPendingLeftOperand ?: return
        val right = programmerEngine.activeValue
        val baseLabel = programmerEngine.activeBase.label
        val exprSummary: String

        try {
            if (programmerPendingBitwiseOp != null) {
                val op = programmerPendingBitwiseOp!!
                exprSummary = "${left.toString(programmerEngine.activeBase.radix).uppercase()} ${op.symbol} ${right.toString(programmerEngine.activeBase.radix).uppercase()} ($baseLabel)"
                programmerEngine.setValue(left)
                programmerEngine.executeBitwise(op, right)
            } else if (programmerPendingShiftOp != null) {
                val shiftOp = programmerPendingShiftOp!!
                exprSummary = "${left.toString(programmerEngine.activeBase.radix).uppercase()} ${shiftOp.symbol} ${right.toInt()}"
                programmerEngine.setValue(left)
                programmerEngine.shift(shiftOp, right.toInt())
            } else {
                return
            }

            triggerFeedback(isError = false)
            programmerPendingLeftOperand = null
            programmerPendingBitwiseOp = null
            programmerPendingShiftOp = null
            programmerResetNextDigit = true
            val formattedOut = programmerEngine.formatActiveDisplay()
            syncProgrammerState(pendingOpLabel = null)

            viewModelScope.launch {
                historyRepository.addRecord(
                    expression = exprSummary,
                    result = formattedOut,
                    mode = CalculatorMode.PROGRAMMER
                )
            }
        } catch (e: Exception) {
            triggerFeedback(isError = true)
            _uiState.update {
                it.copy(
                    isError = true,
                    errorMessage = e.message ?: "Cannot divide by zero",
                    displayResult = e.message ?: "Cannot divide by zero"
                )
            }
        }
    }

    private fun commitPendingProgrammerOpIfPresent() {
        val left = programmerPendingLeftOperand ?: return
        val right = programmerEngine.activeValue
        if (programmerResetNextDigit) return
        try {
            if (programmerPendingBitwiseOp != null) {
                programmerEngine.setValue(left)
                programmerEngine.executeBitwise(programmerPendingBitwiseOp!!, right)
            } else if (programmerPendingShiftOp != null) {
                programmerEngine.setValue(left)
                programmerEngine.shift(programmerPendingShiftOp!!, right.toInt())
            }
        } catch (ignored: Exception) {
        }
    }

    private fun syncProgrammerState(pendingOpLabel: String? = _uiState.value.programmerPendingOp) {
        val reps = programmerEngine.formatAllBases()
        val activeDisplay = programmerEngine.formatActiveDisplay()
        _uiState.update {
            it.copy(
                programmerWordSize = programmerEngine.activeWordSize,
                programmerBase = programmerEngine.activeBase,
                programmerSigned = programmerEngine.isSignedDisplay,
                programmerRepresentations = reps,
                programmerPendingOp = pendingOpLabel,
                expression = pendingOpLabel ?: "",
                displayResult = activeDisplay,
                isError = false,
                errorMessage = null
            )
        }
    }

    // =========================================================================
    // History Recall & Clearing
    // =========================================================================

    fun recallHistoryResult(item: HistoryItem) {
        triggerFeedback()
        justEvaluated = false
        if (_uiState.value.mode == CalculatorMode.PROGRAMMER) {
            val parsed = item.result.toULongOrNull(programmerEngine.activeBase.radix) ?: 0uL
            programmerEngine.setValue(parsed)
            syncProgrammerState()
            _uiState.update { it.copy(showHistorySheet = false) }
        } else {
            updateExpressionState(item.result)
            _uiState.update { it.copy(showHistorySheet = false) }
        }
    }

    fun recallHistoryExpression(item: HistoryItem) {
        triggerFeedback()
        justEvaluated = false
        if (item.mode != CalculatorMode.PROGRAMMER) {
            updateExpressionState(item.expression)
        }
        _uiState.update { it.copy(showHistorySheet = false) }
    }

    fun deleteHistoryItem(id: Long) {
        triggerFeedback()
        viewModelScope.launch {
            historyRepository.deleteRecord(id)
        }
    }

    fun clearHistory() {
        triggerFeedback()
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    private fun updateExpressionState(newExpression: String) {
        val state = _uiState.value
        val preview = computePreview(newExpression, state.mode, state.angleUnit)
        _uiState.update {
            it.copy(
                expression = newExpression,
                displayResult = if (newExpression.isEmpty()) "0" else (preview ?: it.displayResult),
                livePreview = preview,
                isError = false,
                errorMessage = null
            )
        }
    }

    private fun computePreview(expression: String, mode: CalculatorMode, angleUnit: AngleUnit): String? {
        if (expression.isBlank()) return null
        val engine = if (mode == CalculatorMode.SCIENTIFIC) scientificEngine else normalEngine
        return engine.evaluatePreview(expression, angleUnit)
    }

    override fun onCleared() {
        super.onCleared()
        audioService.release()
    }

    companion object {
        fun provideFactory(
            preferencesRepository: PreferencesRepository,
            historyRepository: HistoryRepository,
            hapticService: HapticService,
            audioService: AudioService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CalculatorViewModel(
                    preferencesRepository = preferencesRepository,
                    historyRepository = historyRepository,
                    hapticService = hapticService,
                    audioService = audioService
                ) as T
            }
        }
    }
}
