package com.calculator.ui

import com.calculator.data.history.HistoryItem
import com.calculator.engine.AngleUnit
import com.calculator.engine.programmer.BaseRepresentations
import com.calculator.engine.programmer.NumberBase
import com.calculator.engine.programmer.WordSize
import com.calculator.ui.theme.SkinType

enum class CalculatorMode(val displayName: String) {
    NORMAL("Normal"),
    SCIENTIFIC("Scientific"),
    PROGRAMMER("Programmer")
}

data class CalculatorUiState(
    val mode: CalculatorMode = CalculatorMode.NORMAL,
    val skin: SkinType = SkinType.MODERN,
    val expression: String = "",
    val displayResult: String = "0",
    val livePreview: String? = null,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val angleUnit: AngleUnit = AngleUnit.DEGREES,
    val isSecondFunctionActive: Boolean = false,
    val hasMemory: Boolean = false,
    val memoryFormatted: String = "0",
    val programmerWordSize: WordSize = WordSize.QWORD,
    val programmerBase: NumberBase = NumberBase.DEC,
    val programmerSigned: Boolean = true,
    val programmerPendingOp: String? = null,
    val programmerRepresentations: BaseRepresentations = BaseRepresentations(
        hex = "0",
        decSigned = "0",
        decUnsigned = "0",
        oct = "0",
        bin = "0",
        groupedBin = "0000",
        bits = List(64) { false }
    ),
    val showBitMatrix: Boolean = false,
    val showHistorySheet: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val history: List<HistoryItem> = emptyList()
)
