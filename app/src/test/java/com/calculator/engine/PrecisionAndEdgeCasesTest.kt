package com.calculator.engine

import com.calculator.engine.scientific.ScientificCalculatorEngine
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class PrecisionAndEdgeCasesTest {

    private lateinit var engine: ScientificCalculatorEngine

    @Before
    fun setUp() {
        engine = ScientificCalculatorEngine()
    }

    // =========================================================================
    // 1. High-Precision Decimal Arithmetic & Formatting (Edge Cases 1, 2)
    // =========================================================================

    @Test
    fun testPrecision_addition_pointOnePlusPointTwoEqualsPointThree_edgeCase1() {
        // Edge Case 1: 0.1 + 0.2 strictly equals 0.3
        val result = engine.evaluate("0.1 + 0.2")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        assertThat(success.formatted).isEqualTo("0.3")
        assertThat(success.value).isEqualTo(BigDecimal("0.3"))
    }

    @Test
    fun testPrecision_subtraction_onePointZeroMinusPointNineEqualsPointOne_edgeCase2() {
        // Edge Case 2: 1.0 - 0.9 strictly equals 0.1
        val result = engine.evaluate("1.0 - 0.9")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        assertThat(success.formatted).isEqualTo("0.1")
        assertThat(success.value).isEqualTo(BigDecimal("0.1"))
    }

    @Test
    fun testPrecision_decimalMultiplicationExact() {
        val result = engine.evaluate("0.05 * 0.2")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("0.01")
    }

    @Test
    fun testPrecision_trailingZeroStripping() {
        // Formatted result strips redundant trailing decimal zeros
        assertThat((engine.evaluate("1.50000 + 0") as EvaluationResult.Success).formatted).isEqualTo("1.5")
        assertThat((engine.evaluate("2.000 + 0") as EvaluationResult.Success).formatted).isEqualTo("2")
        assertThat((engine.evaluate("10.00 / 2") as EvaluationResult.Success).formatted).isEqualTo("5")
    }

    @Test
    fun testPrecision_decimal128Precision34Digits() {
        // Evaluates 1 / 3 to 34 decimal places without IEEE-754 53-bit mantissa truncation
        val result = engine.evaluate("1 / 3")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val formatted = (result as EvaluationResult.Success).formatted
        assertThat(formatted).startsWith("0.333333333333333333333333333333333")
    }

    // =========================================================================
    // 2. Division By Zero & Indeterminate Forms (Edge Cases 3, 4)
    // =========================================================================

    @Test
    fun testDivisionByZero_nonZeroDividedByZero_edgeCase3() {
        // Edge Case 3: 5 / 0 -> Cannot divide by zero
        val result = engine.evaluate("5 / 0")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Cannot divide by zero")
    }

    @Test
    fun testDivisionByZero_zeroDividedByZero_edgeCase4() {
        // Edge Case 4: 0 / 0 -> Cannot divide by zero
        val result = engine.evaluate("0 / 0")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Cannot divide by zero")
    }

    @Test
    fun testDivisionByZero_nestedExpressionDenominatorZero() {
        val result = engine.evaluate("10 / (5 - 5)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Cannot divide by zero")
    }

    // =========================================================================
    // 3. Domain Errors Consolidated (Edge Cases 21, 24, 25, 27, 30, 31, 34, 35)
    // =========================================================================

    @Test
    fun testDomainError_squareRootNegative_edgeCase27() {
        val result = engine.evaluate("sqrt(-4)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testDomainError_lnZero_edgeCase30() {
        val result = engine.evaluate("ln(0)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testDomainError_log10Negative_edgeCase31() {
        val result = engine.evaluate("log10(-10)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testDomainError_tan90Degrees_edgeCase21() {
        val result = engine.evaluate("tan(90)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testDomainError_asinOutOfBounds_edgeCase24() {
        val result = engine.evaluate("asin(1.0001)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testDomainError_acosOutOfBounds_edgeCase25() {
        val result = engine.evaluate("acos(-1.5)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testDomainError_factorialNegative_edgeCase34() {
        val result = engine.evaluate("(-5)!")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testDomainError_factorialNonInteger_edgeCase35() {
        val result = engine.evaluate("3.5!")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    // =========================================================================
    // 4. Live Expression Preview Evaluator
    // =========================================================================

    @Test
    fun testLivePreview_completeValidExpression() {
        val preview = engine.evaluatePreview("2 + 3 * 4")
        assertThat(preview).isEqualTo("14")
    }

    @Test
    fun testLivePreview_danglingBinaryOperatorTolerance() {
        // When typing "12 + ", preview returns "12" or null without throwing syntax errors
        val preview = engine.evaluatePreview("12 + ")
        assertThat(preview).isAnyOf("12", null)
    }

    @Test
    fun testLivePreview_unclosedParenthesesAutoCompleted_edgeCase40() {
        // While typing "2 * (3 + 4", preview automatically balances parens to show "14"
        val preview = engine.evaluatePreview("2 * (3 + 4")
        assertThat(preview).isEqualTo("14")
    }

    @Test
    fun testLivePreview_zeroDivisionNonCrashing() {
        // While typing "5 / 0", preview does not crash and returns null or non-fatal warning
        val preview = engine.evaluatePreview("5 / 0")
        assertThat(preview).isAnyOf(null, "Cannot divide by zero")
    }

    // =========================================================================
    // 5. Syntax Errors & Recovery
    // =========================================================================

    @Test
    fun testSyntaxError_unbalancedExtraClosingParenthesis() {
        val result = engine.evaluate("(2 + 3))")
        assertThat(result).isInstanceOf(EvaluationResult.SyntaxError::class.java)
    }

    @Test
    fun testSyntaxError_consecutiveBinaryOperators() {
        val result = engine.evaluate("2 + * 3")
        assertThat(result).isInstanceOf(EvaluationResult.SyntaxError::class.java)
    }

    @Test
    fun testSyntaxError_emptyOrWhitespaceExpression() {
        val result = engine.evaluate("   ")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("0")
    }
}
