package com.calculator.engine

import com.calculator.engine.normal.NormalCalculatorEngine
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class NormalCalculatorEngineTest {

    private lateinit var engine: NormalCalculatorEngine

    @Before
    fun setUp() {
        engine = NormalCalculatorEngine()
    }

    // =========================================================================
    // 1. Basic Arithmetic & Precision (Edge Cases 1, 2)
    // =========================================================================

    @Test
    fun testBasicArithmetic_addition() {
        val result = engine.evaluate("12 + 34")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        assertThat(success.formatted).isEqualTo("46")
        assertThat(success.value).isEqualTo(BigDecimal("46"))
    }

    @Test
    fun testBasicArithmetic_additionPrecision_edgeCase1() {
        // Edge Case 1: 0.1 + 0.2 == 0.3 without floating-point artifacts
        val result = engine.evaluate("0.1 + 0.2")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        assertThat(success.formatted).isEqualTo("0.3")
        assertThat(success.value).isEqualTo(BigDecimal("0.3"))
    }

    @Test
    fun testBasicArithmetic_subtraction() {
        val result = engine.evaluate("50 - 18")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        assertThat(success.formatted).isEqualTo("32")
    }

    @Test
    fun testBasicArithmetic_subtractionPrecision_edgeCase2() {
        // Edge Case 2: 1.0 - 0.9 == 0.1 without floating-point artifacts
        val result = engine.evaluate("1.0 - 0.9")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        assertThat(success.formatted).isEqualTo("0.1")
        assertThat(success.value).isEqualTo(BigDecimal("0.1"))
    }

    @Test
    fun testBasicArithmetic_multiplication() {
        val result1 = engine.evaluate("7 * 8")
        assertThat((result1 as EvaluationResult.Success).formatted).isEqualTo("56")

        val result2 = engine.evaluate("2.5 * 4")
        assertThat((result2 as EvaluationResult.Success).formatted).isEqualTo("10")
    }

    @Test
    fun testBasicArithmetic_division() {
        val result1 = engine.evaluate("100 / 4")
        assertThat((result1 as EvaluationResult.Success).formatted).isEqualTo("25")

        val result2 = engine.evaluate("1 / 8")
        assertThat((result2 as EvaluationResult.Success).formatted).isEqualTo("0.125")
    }

    @Test
    fun testDivision_divideByZeroReturnsDomainError_edgeCase3() {
        // Edge Case 3: 5 / 0 -> Cannot divide by zero
        val result = engine.evaluate("5 / 0")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        val error = result as EvaluationResult.DomainError
        assertThat(error.message).contains("Cannot divide by zero")
    }

    // =========================================================================
    // 2. Precedence & PEMDAS (Edge Cases 5, 6)
    // =========================================================================

    @Test
    fun testPrecedence_multiplicationBeforeAddition_edgeCase5() {
        // Edge Case 5: 2 + 3 * 4 == 14
        val result = engine.evaluate("2 + 3 * 4")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("14")
    }

    @Test
    fun testPrecedence_divisionBeforeSubtraction_edgeCase6() {
        // Edge Case 6: 10 - 6 / 2 == 7
        val result = engine.evaluate("10 - 6 / 2")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("7")
    }

    @Test
    fun testPrecedence_chainedOperationsLeftToRight() {
        val result1 = engine.evaluate("20 - 5 + 3")
        assertThat((result1 as EvaluationResult.Success).formatted).isEqualTo("18")

        val result2 = engine.evaluate("24 / 4 * 2")
        assertThat((result2 as EvaluationResult.Success).formatted).isEqualTo("12")
    }

    @Test
    fun testPrecedence_complexAlgebraicCombination() {
        val result = engine.evaluate("10 + 2 * 3 - 8 / 2")
        // 10 + 6 - 4 = 12
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("12")
    }

    // =========================================================================
    // 3. Percentage Models (Edge Cases 8, 9, 10, 11, 12, 13)
    // =========================================================================

    @Test
    fun testPercentage_additiveMarkup_edgeCase8() {
        // Edge Case 8: 100 + 10% == 110
        val result = engine.evaluate("100 + 10%")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("110")
    }

    @Test
    fun testPercentage_subtractiveDiscount_edgeCase9() {
        // Edge Case 9: 200 - 15% == 170
        val result = engine.evaluate("200 - 15%")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("170")
    }

    @Test
    fun testPercentage_multiplicativeRatio_edgeCase10() {
        // Edge Case 10: 50 * 20% == 10
        val result = engine.evaluate("50 * 20%")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("10")
    }

    @Test
    fun testPercentage_divisiveRatio_edgeCase11() {
        // Edge Case 11: 50 / 20% == 250
        val result = engine.evaluate("50 / 20%")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("250")
    }

    @Test
    fun testPercentage_standaloneUnary_edgeCase12() {
        // Edge Case 12: 45% == 0.45
        val result = engine.evaluate("45%")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("0.45")
    }

    @Test
    fun testPercentage_chainedAdditive_edgeCase13() {
        // Edge Case 13: 100 + 10% + 10% == 121
        val result = engine.evaluate("100 + 10% + 10%")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("121")
    }

    @Test
    fun testPercentage_calculatePercentageMethod() {
        val markup = engine.calculatePercentage("100 +", "10")
        assertThat(markup).isEqualTo("10")

        val discount = engine.calculatePercentage("200 -", "15")
        assertThat(discount).isEqualTo("30")
    }

    // =========================================================================
    // 4. Sign Negation & Unary Operators (Edge Cases 14, 15)
    // =========================================================================

    @Test
    fun testSignNegation_zeroSuppressesNegativeSignArtifact_edgeCase14() {
        // Edge Case 14: Negate 0 via +/- yields 0 (no -0)
        val result = engine.evaluate("-0")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("0")
    }

    @Test
    fun testSignNegation_operatorFollowedByNegation_edgeCase15() {
        // Edge Case 15: 5 + -3 == 2
        val result = engine.evaluate("5 + -3")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("2")
    }

    @Test
    fun testSignNegation_unaryMinusAtStart() {
        val result = engine.evaluate("-15 + 5")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("-10")
    }

    // =========================================================================
    // 5. Memory Operations (Edge Cases 16, 17)
    // =========================================================================

    @Test
    fun testMemory_initialStateEmpty() {
        val memory = engine.memoryRegister
        assertThat(memory.hasMemory()).isFalse()
        assertThat(memory.recall()).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun testMemory_additiveAccumulation_edgeCase16() {
        // Edge Case 16: 5 -> M+ -> 10 -> M+ -> MR == 15
        val memory = engine.memoryRegister
        memory.add(BigDecimal("5"))
        assertThat(memory.hasMemory()).isTrue()
        assertThat(memory.recall()).isEqualTo(BigDecimal("5"))

        memory.add(BigDecimal("10"))
        assertThat(memory.hasMemory()).isTrue()
        assertThat(memory.recall()).isEqualTo(BigDecimal("15"))
    }

    @Test
    fun testMemory_subtractiveAccumulation() {
        val memory = engine.memoryRegister
        memory.add(BigDecimal("20"))
        memory.subtract(BigDecimal("5"))
        assertThat(memory.hasMemory()).isTrue()
        assertThat(memory.recall()).isEqualTo(BigDecimal("15"))
    }

    @Test
    fun testMemory_clearResetsValueAndIndicator_edgeCase17() {
        // Edge Case 17: MR after MC == 0, hasMemory == false
        val memory = engine.memoryRegister
        memory.add(BigDecimal("42"))
        assertThat(memory.hasMemory()).isTrue()

        memory.clear()
        assertThat(memory.hasMemory()).isFalse()
        assertThat(memory.recall()).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun testMemory_survivesEngineClear() {
        val memory = engine.memoryRegister
        memory.add(BigDecimal("99"))

        // Evaluating a new expression does not clear memory
        engine.evaluate("2 + 2")
        assertThat(memory.hasMemory()).isTrue()
        assertThat(memory.recall()).isEqualTo(BigDecimal("99"))
    }

    @Test
    fun testMemory_engineHelpers() {
        assertThat(engine.addToMemory("25 + 25")).isTrue()
        assertThat(engine.memory.recall()).isEqualTo(BigDecimal("50"))

        assertThat(engine.subtractFromMemory("10")).isTrue()
        assertThat(engine.memory.recall()).isEqualTo(BigDecimal("40"))

        assertThat(engine.addToMemory("5 / 0")).isFalse()
    }
}
