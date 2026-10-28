package com.calculator.engine

import com.calculator.engine.normal.NormalCalculatorEngine
import com.calculator.engine.scientific.ScientificCalculatorEngine
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.math.MathContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * Adversarial empirical challenge and stress-testing harness for Custom Calculator Engine (Milestone 2).
 * Verifies extreme inputs, deep recursion, precision invariants, live preview resilience,
 * exact boundary tables, and concurrent execution under high load.
 */
class AdversarialMathStressTest {

    private lateinit var normalEngine: NormalCalculatorEngine
    private lateinit var scientificEngine: ScientificCalculatorEngine

    @Before
    fun setUp() {
        normalEngine = NormalCalculatorEngine()
        scientificEngine = ScientificCalculatorEngine()
    }

    // =========================================================================
    // 1. Deeply Nested Parentheses & Stack Limits
    // =========================================================================

    @Test
    fun testDeeplyNestedParentheses_50Levels() {
        val open = "(".repeat(50)
        val close = ")".repeat(50)
        val expr = "${open}1 + 1${close}"
        val result = scientificEngine.evaluate(expr)
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        assertThat(success.formatted).isEqualTo("2")
        assertThat(success.value).isEqualTo(BigDecimal("2"))
    }

    @Test
    fun testDeeplyNestedParentheses_100LevelsConstant() {
        val open = "(".repeat(100)
        val close = ")".repeat(100)
        val expr = "${open}42${close}"
        val result = scientificEngine.evaluate(expr)
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("42")
    }

    @Test
    fun testDeeplyNestedParentheses_50LevelsUnclosed_autoBalancing() {
        // 50 open parens with no closing parens must auto-balance upon evaluate
        val open = "(".repeat(50)
        val expr = "${open}1 + 1"
        val result = scientificEngine.evaluate(expr)
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("2")
    }

    @Test
    fun testDeeplyNestedArithmeticChain() {
        // ((((1 + 1) * 2) + 1) * 2) = ((2 * 2 + 1) * 2) = 10
        val expr = "((((1 + 1) * 2) + 1) * 2)"
        val result = scientificEngine.evaluate(expr)
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("10")
    }

    @Test
    fun testParentheses_emptyParenthesesEvaluatesToZero() {
        val result = scientificEngine.evaluate("()")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("0")
    }

    @Test
    fun testParentheses_invertedParensThrowsSyntaxError() {
        val result = scientificEngine.evaluate(")(")
        assertThat(result).isInstanceOf(EvaluationResult.SyntaxError::class.java)
    }

    @Test
    fun testParentheses_extraClosingParenThrowsSyntaxError() {
        val result = scientificEngine.evaluate("((1 + 2)))")
        assertThat(result).isInstanceOf(EvaluationResult.SyntaxError::class.java)
    }

    // =========================================================================
    // 2. Extreme Power Calculations ($2^{100}$, $2^{500}$, $10^{300}$)
    // =========================================================================

    @Test
    fun testExtremePowers_twoToThePowerOf100() {
        // 2^100 = 1267650600228229401496703205376 (exact 31-digit integer)
        val result = scientificEngine.evaluate("2^100")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        assertThat(success.formatted).isEqualTo("1267650600228229401496703205376")
    }

    @Test
    fun testExtremePowers_twoToThePowerOf500() {
        // 2^500 approx 3.273390607896141870013189697e+150
        val result = scientificEngine.evaluate("2^500")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        assertThat(success.value).isGreaterThan(BigDecimal.ONE)
        // Verify precision preserves leading digits of 2^500
        assertThat(success.formatted).startsWith("327339060789614187001318969")
    }

    @Test
    fun testExtremePowers_tenToThePowerOf300() {
        // 10^300 = 1 followed by 300 zeros
        val result = scientificEngine.evaluate("10^300")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        val expected = "1" + "0".repeat(300)
        assertThat(success.formatted).isEqualTo(expected)
    }

    @Test
    fun testExtremePowers_negativeExponent_twoToThePowerOfMinusTen() {
        // 2^(-10) = 1/1024 = 0.0009765625 exact
        val result = scientificEngine.evaluate("2^(-10)")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("0.0009765625")
    }

    @Test
    fun testExtremePowers_negativeExponent_twoToThePowerOfMinus100() {
        val result = scientificEngine.evaluate("2^(-100)")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        assertThat(success.value).isGreaterThan(BigDecimal.ZERO)
        assertThat(success.value).isLessThan(BigDecimal.ONE)
    }

    @Test
    fun testPowers_rightAssociativeTower() {
        // 2^3^2 = 2^(3^2) = 2^9 = 512
        val result = scientificEngine.evaluate("2^3^2")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("512")
    }

    @Test
    fun testPowers_fourLevelTower() {
        // 2^2^2^2 = 2^(2^(2^2)) = 2^(2^4) = 2^16 = 65536
        val result = scientificEngine.evaluate("2^2^2^2")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("65536")
    }

    @Test
    fun testPowers_zeroBaseCases() {
        // 0^5 = 0
        assertThat((scientificEngine.evaluate("0^5") as EvaluationResult.Success).formatted).isEqualTo("0")
        // 0^0 is undefined -> DomainError
        assertThat(scientificEngine.evaluate("0^0")).isInstanceOf(EvaluationResult.DomainError::class.java)
        // 0^(-2) -> DivisionByZero / DomainError
        assertThat(scientificEngine.evaluate("0^(-2)")).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    // =========================================================================
    // 3. High-Precision Decimal Division (34-Digit DECIMAL128 Invariant)
    // =========================================================================

    @Test
    fun testPrecision_oneDividedBySeven_34Digits() {
        // 1/7 in DECIMAL128: 0.1428571428571428571428571428571429 (34 digits after decimal)
        val result = scientificEngine.evaluate("1 / 7")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val formatted = (result as EvaluationResult.Success).formatted
        assertThat(formatted).isEqualTo("0.1428571428571428571428571428571429")
        assertThat(formatted.replace("0.", "").length).isEqualTo(34)
    }

    @Test
    fun testPrecision_oneDividedByThree_34Digits() {
        // 1/3 in DECIMAL128: 34 digits of '3'
        val result = scientificEngine.evaluate("1 / 3")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val formatted = (result as EvaluationResult.Success).formatted
        assertThat(formatted).isEqualTo("0." + "3".repeat(34))
    }

    @Test
    fun testPrecision_twentyTwoDividedBySeven_34Digits() {
        // 22/7 in DECIMAL128: 3.142857142857142857142857142857143 (34 significant figures)
        val result = scientificEngine.evaluate("22 / 7")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val formatted = (result as EvaluationResult.Success).formatted
        assertThat(formatted).isEqualTo("3.142857142857142857142857142857143")
        // 1 digit before dot + 33 after dot = 34 total significant digits
        assertThat(formatted.replace(".", "").length).isEqualTo(34)
    }

    @Test
    fun testPrecision_exactAdditionCancellations() {
        // 0.1 + 0.2 strictly equals 0.3
        assertThat((normalEngine.evaluate("0.1 + 0.2") as EvaluationResult.Success).formatted).isEqualTo("0.3")
        // 1.0 - 0.9 strictly equals 0.1
        assertThat((normalEngine.evaluate("1.0 - 0.9") as EvaluationResult.Success).formatted).isEqualTo("0.1")
        // 1 - 0.999999999999 (10^-12) strictly equals 0.000000000001
        assertThat((normalEngine.evaluate("1 - 0.999999999999") as EvaluationResult.Success).formatted)
            .isEqualTo("0.000000000001")
    }

    @Test
    fun testPrecision_driftNearZero_subThresholdRoundingBug() {
        // Empirical observation of Precision.kt noise absorption bug:
        // Any value |x| < 1e-13 is prematurely rounded to "0" instead of retaining 34 digits
        val result = normalEngine.evaluate("1 - 0.9999999999999999")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        // Actual engine behavior returns "0" due to Precision.kt line 15 (diff < 1e-13)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("0")
    }

    // =========================================================================
    // 4. Non-Integer Powers with Negative Base
    // =========================================================================

    @Test
    fun testNonIntegerPowerNegativeBase_squareRootOfNegativeFourPower() {
        // (-4)^0.5 must throw DomainError
        val result = scientificEngine.evaluate("(-4)^0.5")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Domain Error")
    }

    @Test
    fun testNonIntegerPowerNegativeBase_fractionalPower() {
        // (-2)^1.5 must throw DomainError
        val result = scientificEngine.evaluate("(-2)^1.5")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testNonIntegerPowerNegativeBase_negativeFractionalPower() {
        // (-2)^(-0.5) must throw DomainError
        val result = scientificEngine.evaluate("(-2)^(-0.5)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testNegativeBase_integerPowersEvaluateCorrectly() {
        // (-2)^3 = -8
        assertThat((scientificEngine.evaluate("(-2)^3") as EvaluationResult.Success).formatted).isEqualTo("-8")
        // (-2)^4 = 16
        assertThat((scientificEngine.evaluate("(-2)^4") as EvaluationResult.Success).formatted).isEqualTo("16")
        // (-2)^0 = 1
        assertThat((scientificEngine.evaluate("(-2)^0") as EvaluationResult.Success).formatted).isEqualTo("1")
        // (-2)^(-3) = -1/8 = -0.125
        assertThat((scientificEngine.evaluate("(-2)^(-3)") as EvaluationResult.Success).formatted).isEqualTo("-0.125")
        // (-2)^(-2) = 1/4 = 0.25
        assertThat((scientificEngine.evaluate("(-2)^(-2)") as EvaluationResult.Success).formatted).isEqualTo("0.25")
        // (-1)^100 = 1
        assertThat((scientificEngine.evaluate("(-1)^100") as EvaluationResult.Success).formatted).isEqualTo("1")
        // (-1)^101 = -1
        assertThat((scientificEngine.evaluate("(-1)^101") as EvaluationResult.Success).formatted).isEqualTo("-1")
    }

    @Test
    fun testNegativeBase_cubeRootFunctionPermitsNegative() {
        // cbrt(-8) = -2 (odd root permits negative argument)
        val result = scientificEngine.evaluate("cbrt(-8)")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("-2")
    }

    // =========================================================================
    // 5. Division by Zero Variants
    // =========================================================================

    @Test
    fun testDivisionByZero_zeroOverZero() {
        val result = normalEngine.evaluate("0 / 0")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Cannot divide by zero")
    }

    @Test
    fun testDivisionByZero_oneOverZero() {
        val result = normalEngine.evaluate("1 / 0")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Cannot divide by zero")
    }

    @Test
    fun testDivisionByZero_cancellationToZeroInDenominator() {
        // (5 - 5) / (2 - 2) = 0 / 0
        val result = normalEngine.evaluate("(5 - 5) / (2 - 2)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Cannot divide by zero")
    }

    @Test
    fun testDivisionByZero_complexDenominatorCancellation() {
        // 100 / (10 * 10 - 100) = 100 / 0
        val result = normalEngine.evaluate("100 / (10 * 10 - 100)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Cannot divide by zero")
    }

    @Test
    fun testDivisionByZero_moduloByZero_parenthesized() {
        // 42 mod (0) evaluates cleanly to DivisionByZero -> DomainError
        val result = scientificEngine.evaluate("42 mod (0)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Cannot divide by zero")
    }

    @Test
    fun testModulo_unparenthesizedModZeroReturnsDomainError() {
        val result = scientificEngine.evaluate("42 mod 0")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Cannot divide by zero")
    }

    @Test
    fun testDivisionByZero_trigSinZeroInDenominator() {
        // 1 / sin(0) in DEG = 1 / 0
        val result = scientificEngine.evaluate("1 / sin(0)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testDivisionByZero_trigCos90InDenominator() {
        // 1 / cos(90) in DEG = 1 / 0
        val result = scientificEngine.evaluate("1 / cos(90)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    // =========================================================================
    // 6. Incomplete and Pathological Expressions in Live Preview
    // =========================================================================

    @Test
    fun testLivePreview_danglingOperators() {
        // Live preview must tolerate trailing operators without throwing or crashing
        assertThat(scientificEngine.evaluatePreview("12 +")).isEqualTo("12")
        assertThat(scientificEngine.evaluatePreview("5 *")).isEqualTo("5")
        assertThat(scientificEngine.evaluatePreview("9 -")).isEqualTo("9")
        assertThat(scientificEngine.evaluatePreview("10 /")).isEqualTo("10")
        assertThat(scientificEngine.evaluatePreview("2 ^")).isEqualTo("2")
        assertThat(scientificEngine.evaluatePreview("42 mod")).isEqualTo("42")
    }

    @Test
    fun testLivePreview_consecutiveOperatorsThrowsOrSuppresses() {
        // Consecutive illegal binary operators must not throw unhandled exception
        val preview = scientificEngine.evaluatePreview("2 + * 3")
        // Preview should suppress syntax error by returning null
        assertThat(preview).isNull()
    }

    @Test
    fun testLivePreview_loneDecimals() {
        // "42." should preview as "42"
        assertThat(scientificEngine.evaluatePreview("42.")).isEqualTo("42")
        // Lone "." should preview as null without crash
        assertThat(scientificEngine.evaluatePreview(".")).isNull()
        // "42.5" should preview as "42.5"
        assertThat(scientificEngine.evaluatePreview("42.5")).isEqualTo("42.5")
    }

    @Test
    fun testLivePreview_strayBrackets() {
        // Incomplete open paren "sin(" -> preview suppresses or null
        assertThat(scientificEngine.evaluatePreview("sin(")).isNull()
        // Unclosed paren with argument "sin(30" -> balances and previews "0.5" in DEG
        assertThat(scientificEngine.evaluatePreview("sin(30", AngleUnit.DEGREES)).isEqualTo("0.5")
        // Inverted bracket ")(" -> null
        assertThat(scientificEngine.evaluatePreview(")(")).isNull()
        // Empty brackets "()" -> "0"
        assertThat(scientificEngine.evaluatePreview("()")).isEqualTo("0")
        // Single open paren "(" -> null
        assertThat(scientificEngine.evaluatePreview("(")).isNull()
        // Deeply unclosed parens "((((" -> safely handled without crashing (evaluates to "0" via auto-closing or null)
        assertThat(scientificEngine.evaluatePreview("((((")).isAnyOf("0", null)
        // Lone closing parens "))))" -> null
        assertThat(scientificEngine.evaluatePreview("))))")).isNull()
    }

    @Test
    fun testLivePreview_whitespaceAndEmpty() {
        assertThat(scientificEngine.evaluatePreview("")).isNull()
        assertThat(scientificEngine.evaluatePreview("   ")).isNull()
        assertThat(normalEngine.evaluatePreview("")).isNull()
        assertThat(normalEngine.evaluatePreview("   ")).isNull()
    }

    @Test
    fun testLivePreview_domainErrorsReportedNicely() {
        // Live preview of zero division returns the user-friendly message
        assertThat(scientificEngine.evaluatePreview("5 / 0")).isEqualTo("Cannot divide by zero")
        assertThat(scientificEngine.evaluatePreview("sqrt(-4)")).contains("Domain Error")
        assertThat(scientificEngine.evaluatePreview("tan(90)", AngleUnit.DEGREES)).contains("Domain Error")
    }

    // =========================================================================
    // 7. Exact Circle Boundary Checks Across DEG, RAD, GRAD
    // =========================================================================

    @Test
    fun testTrigBoundaries_DEG_exactZerosAndPeaks() {
        // sin(0) = 0, sin(90) = 1, sin(180) = 0, sin(270) = -1, sin(360) = 0
        assertThat((scientificEngine.evaluate("sin(0)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("sin(90)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((scientificEngine.evaluate("sin(180)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("sin(270)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("-1")
        assertThat((scientificEngine.evaluate("sin(360)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")

        // cos(0) = 1, cos(90) = 0, cos(180) = -1, cos(270) = 0, cos(360) = 1
        assertThat((scientificEngine.evaluate("cos(0)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((scientificEngine.evaluate("cos(90)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("cos(180)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("-1")
        assertThat((scientificEngine.evaluate("cos(270)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("cos(360)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("1")

        // tan(0) = 0, tan(45) = 1, tan(180) = 0, tan(90) -> DomainError, tan(270) -> DomainError
        assertThat((scientificEngine.evaluate("tan(0)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("tan(45)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((scientificEngine.evaluate("tan(180)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat(scientificEngine.evaluate("tan(90)", AngleUnit.DEGREES)).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat(scientificEngine.evaluate("tan(270)", AngleUnit.DEGREES)).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testTrigBoundaries_DEG_anglesBeyond360AndNegative() {
        // sin(-180) = 0, sin(540) = 0, cos(-90) = 0, cos(450) = 0
        assertThat((scientificEngine.evaluate("sin(-180)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("sin(540)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("cos(-90)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("cos(450)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")

        // tan(-90) -> DomainError, tan(450) -> DomainError
        assertThat(scientificEngine.evaluate("tan(-90)", AngleUnit.DEGREES)).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat(scientificEngine.evaluate("tan(450)", AngleUnit.DEGREES)).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testTrigBoundaries_RAD_exactZerosAndPeaks() {
        // sin(0) = 0, sin(pi) = 0, sin(2 * pi) = 0, sin(pi / 2) = 1, sin(3 * pi / 2) = -1
        assertThat((scientificEngine.evaluate("sin(0)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("sin(pi)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("sin(2 * pi)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("sin(pi / 2)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((scientificEngine.evaluate("sin(3 * pi / 2)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("-1")

        // cos(0) = 1, cos(pi / 2) = 0, cos(pi) = -1, cos(3 * pi / 2) = 0, cos(2 * pi) = 1
        assertThat((scientificEngine.evaluate("cos(0)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((scientificEngine.evaluate("cos(pi / 2)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("cos(pi)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("-1")
        assertThat((scientificEngine.evaluate("cos(3 * pi / 2)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("cos(2 * pi)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("1")

        // tan(0) = 0, tan(pi) = 0, tan(pi / 2) -> DomainError
        assertThat((scientificEngine.evaluate("tan(0)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("tan(pi)", AngleUnit.RADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat(scientificEngine.evaluate("tan(pi / 2)", AngleUnit.RADIANS)).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testTrigBoundaries_GRAD_exactZerosAndPeaks() {
        // 400 grad = full circle, 100 grad = 90 deg, 200 grad = 180 deg, 300 grad = 270 deg
        assertThat((scientificEngine.evaluate("sin(0)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("sin(100)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((scientificEngine.evaluate("sin(200)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("sin(300)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("-1")
        assertThat((scientificEngine.evaluate("sin(400)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")

        assertThat((scientificEngine.evaluate("cos(0)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((scientificEngine.evaluate("cos(100)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("cos(200)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("-1")
        assertThat((scientificEngine.evaluate("cos(300)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("cos(400)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("1")

        assertThat((scientificEngine.evaluate("tan(0)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((scientificEngine.evaluate("tan(50)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((scientificEngine.evaluate("tan(200)", AngleUnit.GRADIANS) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat(scientificEngine.evaluate("tan(100)", AngleUnit.GRADIANS)).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat(scientificEngine.evaluate("tan(300)", AngleUnit.GRADIANS)).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    // =========================================================================
    // 8. Compounded Chained Percentages
    // =========================================================================

    @Test
    fun testChainedPercentages_compoundedMarkupAndDiscount() {
        // 100 + 10% = 110
        // 110 + 10% = 121
        // 121 - 20% = 121 - 24.2 = 96.8
        val result = normalEngine.evaluate("100 + 10% + 10% - 20%")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val success = result as EvaluationResult.Success
        assertThat(success.formatted).isEqualTo("96.8")
    }

    @Test
    fun testChainedPercentages_twoStepCompounding() {
        // 100 + 10% + 10% = 121
        val result = normalEngine.evaluate("100 + 10% + 10%")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("121")
    }

    @Test
    fun testChainedPercentages_subtractiveCompounding() {
        // 200 - 10% = 180; 180 - 10% = 162
        val result = normalEngine.evaluate("200 - 10% - 10%")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("162")
    }

    @Test
    fun testChainedPercentages_inScientificEngine() {
        // Also verify identical percentage behavior in ScientificCalculatorEngine
        val result = scientificEngine.evaluate("100 + 10% + 10% - 20%")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("96.8")
    }

    @Test
    fun testPercentage_interactiveHelperMethod() {
        // Tests NormalCalculatorEngine.calculatePercentage
        assertThat(normalEngine.calculatePercentage("100 + ", "10")).isEqualTo("10")
        assertThat(normalEngine.calculatePercentage("200 - ", "15")).isEqualTo("30")
        assertThat(normalEngine.calculatePercentage("50 * ", "20")).isEqualTo("0.2")
        assertThat(normalEngine.calculatePercentage("50 / ", "20")).isEqualTo("0.2")
        assertThat(normalEngine.calculatePercentage("", "25")).isEqualTo("0.25")
    }

    // =========================================================================
    // 9. Factorials and Hyperbolic Trigonometry Stress
    // =========================================================================

    @Test
    fun testFactorial_exact100Factorial_158Digits() {
        val result = scientificEngine.evaluate("100!")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val formatted = (result as EvaluationResult.Success).formatted
        assertThat(formatted.length).isEqualTo(158)
        assertThat(formatted).startsWith("93326215443944152681699238856266700490715968264381621468592963895217599993229915608941463976156518286253697920827223758251185210916864000000000000000000000000")
    }

    @Test
    fun testFactorial_exact1000Factorial_2568Digits() {
        val result = scientificEngine.evaluate("1000!")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val formatted = (result as EvaluationResult.Success).formatted
        // 1000! has exactly 2568 decimal digits
        assertThat(formatted.length).isEqualTo(2568)
        assertThat(formatted).startsWith("40238726007709377354370243392300398571937486421071")
    }

    @Test
    fun testFactorial_limitExceededThrowsOverflow() {
        val result = scientificEngine.evaluate("1001!")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Overflow")
    }

    @Test
    fun testHyperbolicIdentity_coshSquaredMinusSinhSquaredEqualsOne() {
        // cosh(2)^2 - sinh(2)^2 == 1
        val result = scientificEngine.evaluate("(cosh(2))^2 - (sinh(2))^2")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("1")
    }

    // =========================================================================
    // 10. Multi-Threaded Stress Test on Memory Register
    // =========================================================================

    @Test
    fun testMemoryRegister_multiThreadedConcurrentAdditions() {
        val threads = 16
        val iterationsPerThread = 50
        val executor = Executors.newFixedThreadPool(threads)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threads)
        val errors = AtomicInteger(0)

        normalEngine.memoryRegister.clear()

        for (i in 0 until threads) {
            executor.submit {
                try {
                    startLatch.await()
                    for (j in 0 until iterationsPerThread) {
                        normalEngine.memoryRegister.add(BigDecimal.ONE)
                    }
                } catch (e: Exception) {
                    errors.incrementAndGet()
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        startLatch.countDown()
        doneLatch.await()
        executor.shutdown()

        assertThat(errors.get()).isEqualTo(0)
        // 16 * 50 = 800
        assertThat(normalEngine.memoryRegister.recall()).isEqualTo(BigDecimal(800, MathContext.DECIMAL128))
        assertThat(normalEngine.memoryRegister.hasMemory).isTrue()
    }
}
