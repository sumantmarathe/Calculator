package com.calculator.ui

import com.calculator.engine.AngleUnit
import com.calculator.engine.programmer.BitwiseOp
import com.calculator.engine.programmer.NumberBase
import com.calculator.engine.programmer.WordSize
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class ModeSwitchingTest {

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = CalculatorViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testModeSwitching_normalScientificProgrammer() {
        assertThat(viewModel.uiState.value.mode).isEqualTo(CalculatorMode.NORMAL)

        viewModel.setMode(CalculatorMode.SCIENTIFIC)
        assertThat(viewModel.uiState.value.mode).isEqualTo(CalculatorMode.SCIENTIFIC)

        viewModel.setMode(CalculatorMode.PROGRAMMER)
        assertThat(viewModel.uiState.value.mode).isEqualTo(CalculatorMode.PROGRAMMER)

        viewModel.setMode(CalculatorMode.NORMAL)
        assertThat(viewModel.uiState.value.mode).isEqualTo(CalculatorMode.NORMAL)
    }

    @Test
    fun testNormalModeFlow_exactDecimalAndMemoryAndHistory() {
        viewModel.setMode(CalculatorMode.NORMAL)
        viewModel.onDigit("0")
        viewModel.onDecimalPoint()
        viewModel.onDigit("1")
        viewModel.onOperator("+")
        viewModel.onDigit("0")
        viewModel.onDecimalPoint()
        viewModel.onDigit("2")
        viewModel.onEquals()

        assertThat(viewModel.uiState.value.displayResult).isEqualTo("0.3")
        assertThat(viewModel.uiState.value.isError).isFalse()
        assertThat(viewModel.uiState.value.history).hasSize(1)
        assertThat(viewModel.uiState.value.history.first().result).isEqualTo("0.3")

        // Memory operations
        viewModel.onMemoryAdd()
        assertThat(viewModel.uiState.value.hasMemory).isTrue()
        assertThat(viewModel.uiState.value.memoryFormatted).isEqualTo("0.3")

        viewModel.onClear()
        assertThat(viewModel.uiState.value.displayResult).isEqualTo("0")
        viewModel.onMemoryRecall()
        assertThat(viewModel.uiState.value.displayResult).isEqualTo("0.3")

        viewModel.onMemoryClear()
        assertThat(viewModel.uiState.value.hasMemory).isFalse()
    }

    @Test
    fun testScientificModeFlow_trigAndAngleUnitCycle() {
        viewModel.setMode(CalculatorMode.SCIENTIFIC)
        assertThat(viewModel.uiState.value.angleUnit).isEqualTo(AngleUnit.DEGREES)

        viewModel.onScientificFunction("sin")
        viewModel.onDigit("30")
        viewModel.onParenthesis(")")
        viewModel.onEquals()
        assertThat(viewModel.uiState.value.displayResult).isEqualTo("0.5")

        viewModel.cycleAngleUnit()
        assertThat(viewModel.uiState.value.angleUnit).isEqualTo(AngleUnit.RADIANS)
        viewModel.cycleAngleUnit()
        assertThat(viewModel.uiState.value.angleUnit).isEqualTo(AngleUnit.GRADIANS)
    }

    @Test
    fun testProgrammerModeFlow_baseSyncAndBitToggling() {
        viewModel.setMode(CalculatorMode.PROGRAMMER)
        viewModel.setProgrammerWordSize(WordSize.BYTE)
        viewModel.setProgrammerBase(NumberBase.HEX)

        viewModel.onDigit("F")
        assertThat(viewModel.uiState.value.displayResult).isEqualTo("F")
        assertThat(viewModel.uiState.value.programmerRepresentations.decUnsigned).isEqualTo("15")
        assertThat(viewModel.uiState.value.programmerRepresentations.bin).isEqualTo("1111")

        viewModel.onProgrammerBitwiseOp(BitwiseOp.XOR)
        viewModel.onDigit("5")
        viewModel.onEquals()
        // 0xF XOR 0x5 = 0xA (10)
        assertThat(viewModel.uiState.value.displayResult).isEqualTo("A")
        assertThat(viewModel.uiState.value.programmerRepresentations.decUnsigned).isEqualTo("10")

        // Toggle bit 0 (10 -> 11 = 0xB)
        viewModel.onProgrammerBitToggle(0)
        assertThat(viewModel.uiState.value.displayResult).isEqualTo("B")
    }
}
