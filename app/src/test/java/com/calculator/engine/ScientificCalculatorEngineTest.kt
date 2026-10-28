package com.calculator.engine

import com.calculator.engine.scientific.ScientificCalculatorEngine
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class ScientificCalculatorEngineTest {

    private lateinit var engine: ScientificCalculatorEngine

    @Before
    fun setUp() {
        engine = ScientificCalculatorEngine()
    }

    // =========================================================================
    // 1. Trigonometry & Exact Circle Boundaries (Edge Cases 18, 19, 20, 21, 22, 23)
    // =========================================================================

    @Test
    fun testTrigonometry_exactSin180DegreesIsZero_edgeCase18() {
        // Edge Case 18: sin(180) in DEG == 0 (no 1.22e-16 floating-point noise)
        val result = engine.evaluate("sin(180)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("0")
    }

    @Test
    fun testTrigonometry_exactCos90DegreesIsZero_edgeCase19() {
        // Edge Case 19: cos(90) in DEG == 0 (no 6.12e-17 floating-point noise)
        val result = engine.evaluate("cos(90)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("0")
    }

    @Test
    fun testTrigonometry_exactSin90DegreesIsOne_edgeCase20() {
        // Edge Case 20: sin(90) in DEG == 1
        val result = engine.evaluate("sin(90)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("1")
    }

    @Test
    fun testTrigonometry_exactValuesDegrees() {
        assertThat((engine.evaluate("sin(0)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((engine.evaluate("cos(0)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((engine.evaluate("sin(30)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0.5")
        assertThat((engine.evaluate("cos(60)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0.5")
        assertThat((engine.evaluate("sin(270)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("-1")
        assertThat((engine.evaluate("cos(180)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("-1")
        assertThat((engine.evaluate("tan(45)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((engine.evaluate("tan(0)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
    }

    @Test
    fun testTrigonometry_tan90DegreesAsymptoteThrowsDomainError_edgeCase21() {
        // Edge Case 21: tan(90) in DEG -> Domain Error (singularity at pi/2)
        val result = engine.evaluate("tan(90)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        val error = result as EvaluationResult.DomainError
        assertThat(error.message).contains("Domain Error")
    }

    @Test
    fun testTrigonometry_tan270DegreesAsymptoteThrowsDomainError_edgeCase22() {
        // Edge Case 22: tan(270) in DEG -> Domain Error (singularity at 3pi/2)
        val result = engine.evaluate("tan(270)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Domain Error")
    }

    @Test
    fun testTrigonometry_tan100GradiansAsymptoteThrowsDomainError_edgeCase23() {
        // Edge Case 23: tan(100) in GRAD -> Domain Error (singularity at 100 grad)
        val result = engine.evaluate("tan(100)", AngleUnit.GRADIANS)
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat((result as EvaluationResult.DomainError).message).contains("Domain Error")
    }

    @Test
    fun testTrigonometry_radiansMode() {
        // sin(pi) == 0, cos(pi) == -1, sin(pi / 2) == 1
        val resSinPi = engine.evaluate("sin(pi)", AngleUnit.RADIANS)
        assertThat((resSinPi as EvaluationResult.Success).formatted).isEqualTo("0")

        val resCosPi = engine.evaluate("cos(pi)", AngleUnit.RADIANS)
        assertThat((resCosPi as EvaluationResult.Success).formatted).isEqualTo("-1")

        val resSinHalfPi = engine.evaluate("sin(pi / 2)", AngleUnit.RADIANS)
        assertThat((resSinHalfPi as EvaluationResult.Success).formatted).isEqualTo("1")
    }

    @Test
    fun testTrigonometry_gradiansMode() {
        // Full circle = 400 grad. sin(100 grad) == 1, cos(200 grad) == -1
        val resSin100 = engine.evaluate("sin(100)", AngleUnit.GRADIANS)
        assertThat((resSin100 as EvaluationResult.Success).formatted).isEqualTo("1")

        val resCos200 = engine.evaluate("cos(200)", AngleUnit.GRADIANS)
        assertThat((resCos200 as EvaluationResult.Success).formatted).isEqualTo("-1")
    }

    // =========================================================================
    // 2. Inverse Trigonometric Functions (Edge Cases 24, 25, 26)
    // =========================================================================

    @Test
    fun testInverseTrig_asinOutOfBoundsThrowsDomainError_edgeCase24() {
        // Edge Case 24: asin(1.0001) -> Domain Error (|x| > 1)
        val result = engine.evaluate("asin(1.0001)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testInverseTrig_acosOutOfBoundsThrowsDomainError_edgeCase25() {
        // Edge Case 25: acos(-1.5) -> Domain Error (|x| > 1)
        val result = engine.evaluate("acos(-1.5)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testInverseTrig_asinHalfInDegreesIs30_edgeCase26() {
        // Edge Case 26: asin(0.5) in DEG == 30
        val result = engine.evaluate("asin(0.5)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("30")
    }

    @Test
    fun testInverseTrig_exactValues() {
        assertThat((engine.evaluate("acos(0.5)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("60")
        assertThat((engine.evaluate("atan(1)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("45")
        assertThat((engine.evaluate("acos(1)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((engine.evaluate("asin(1)", AngleUnit.DEGREES) as EvaluationResult.Success).formatted).isEqualTo("90")
    }

    // =========================================================================
    // 3. Hyperbolic Functions
    // =========================================================================

    @Test
    fun testHyperbolic_sinhCoshTanhZero() {
        assertThat((engine.evaluate("sinh(0)") as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((engine.evaluate("cosh(0)") as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((engine.evaluate("tanh(0)") as EvaluationResult.Success).formatted).isEqualTo("0")
    }

    @Test
    fun testHyperbolic_identity() {
        // cosh(x)^2 - sinh(x)^2 == 1
        val result = engine.evaluate("cosh(2)^2 - sinh(2)^2")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("1")
    }

    @Test
    fun testHyperbolic_inverseFunctions() {
        assertThat((engine.evaluate("asinh(0)") as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((engine.evaluate("acosh(1)") as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((engine.evaluate("atanh(0)") as EvaluationResult.Success).formatted).isEqualTo("0")
    }

    @Test
    fun testHyperbolic_domainErrors() {
        // acosh(x) defined for x >= 1; atanh(x) defined for |x| < 1
        assertThat(engine.evaluate("acosh(0.5)")).isInstanceOf(EvaluationResult.DomainError::class.java)
        assertThat(engine.evaluate("atanh(1)")).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    // =========================================================================
    // 4. Roots & Powers (Edge Cases 7, 27, 28, 29)
    // =========================================================================

    @Test
    fun testRoots_sqrtNegativeThrowsDomainError_edgeCase27() {
        // Edge Case 27: sqrt(-4) -> Domain Error
        val result = engine.evaluate("sqrt(-4)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testRoots_sqrtZeroIsZero_edgeCase28() {
        // Edge Case 28: sqrt(0) == 0
        val result = engine.evaluate("sqrt(0)")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("0")
    }

    @Test
    fun testRoots_sqrtPositive() {
        assertThat((engine.evaluate("sqrt(4)") as EvaluationResult.Success).formatted).isEqualTo("2")
        assertThat((engine.evaluate("sqrt(144)") as EvaluationResult.Success).formatted).isEqualTo("12")
    }

    @Test
    fun testRoots_cbrtNegativeSupported_edgeCase29() {
        // Edge Case 29: cbrt(-8) == -2 (Real cube root supports negatives)
        val result = engine.evaluate("cbrt(-8)")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("-2")
    }

    @Test
    fun testRoots_cbrtPositiveAndZero() {
        assertThat((engine.evaluate("cbrt(27)") as EvaluationResult.Success).formatted).isEqualTo("3")
        assertThat((engine.evaluate("cbrt(0)") as EvaluationResult.Success).formatted).isEqualTo("0")
    }

    @Test
    fun testPrecedence_rightAssociativeExponentiation_edgeCase7() {
        // Edge Case 7: 2^3^2 == 512 (2^(3^2) = 2^9 = 512, NOT (2^3)^2 = 64)
        val result = engine.evaluate("2^3^2")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("512")
    }

    @Test
    fun testPowers_generalAndSquareCube() {
        assertThat((engine.evaluate("2^10") as EvaluationResult.Success).formatted).isEqualTo("1024")
        assertThat((engine.evaluate("10^3") as EvaluationResult.Success).formatted).isEqualTo("1000")
        assertThat((engine.evaluate("5^0") as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((engine.evaluate("4^0.5") as EvaluationResult.Success).formatted).isEqualTo("2")
        assertThat((engine.evaluate("4^2") as EvaluationResult.Success).formatted).isEqualTo("16")
        assertThat((engine.evaluate("3^3") as EvaluationResult.Success).formatted).isEqualTo("27")
        assertThat((engine.evaluate("1/4") as EvaluationResult.Success).formatted).isEqualTo("0.25")
    }

    // =========================================================================
    // 5. Logarithms (Edge Cases 30, 31)
    // =========================================================================

    @Test
    fun testLogarithms_lnZeroThrowsDomainError_edgeCase30() {
        // Edge Case 30: ln(0) -> Domain Error
        val result = engine.evaluate("ln(0)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testLogarithms_log10NegativeThrowsDomainError_edgeCase31() {
        // Edge Case 31: log10(-10) -> Domain Error
        val result = engine.evaluate("log10(-10)")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testLogarithms_exactValues() {
        assertThat((engine.evaluate("ln(1)") as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((engine.evaluate("log10(1)") as EvaluationResult.Success).formatted).isEqualTo("0")
        assertThat((engine.evaluate("log10(100)") as EvaluationResult.Success).formatted).isEqualTo("2")
        assertThat((engine.evaluate("log10(1000)") as EvaluationResult.Success).formatted).isEqualTo("3")
        assertThat((engine.evaluate("log2(8)") as EvaluationResult.Success).formatted).isEqualTo("3")
        assertThat((engine.evaluate("ln(e)") as EvaluationResult.Success).formatted).isEqualTo("1")
    }

    // =========================================================================
    // 6. Factorials (Edge Cases 32, 33, 34, 35)
    // =========================================================================

    @Test
    fun testFactorial_zeroFactorialEqualsOne_edgeCase32() {
        // Edge Case 32: 0! == 1
        val result = engine.evaluate("0!")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("1")
    }

    @Test
    fun testFactorial_standardIntegers() {
        assertThat((engine.evaluate("1!") as EvaluationResult.Success).formatted).isEqualTo("1")
        assertThat((engine.evaluate("5!") as EvaluationResult.Success).formatted).isEqualTo("120")
        assertThat((engine.evaluate("10!") as EvaluationResult.Success).formatted).isEqualTo("3628800")
    }

    @Test
    fun testFactorial_large100FactorialExactDigits_edgeCase33() {
        // Edge Case 33: 100! exact 158-digit integer without precision loss
        val result = engine.evaluate("100!")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        val formatted = (result as EvaluationResult.Success).formatted
        assertThat(formatted.length).isEqualTo(158)
        assertThat(formatted).startsWith("93326215443944152681699238856266700490715968264381621468592963895217599993229915608941463976156518286253697920827223758251185210916864000000000000000000000000")
    }

    @Test
    fun testFactorial_negativeThrowsDomainError_edgeCase34() {
        // Edge Case 34: (-5)! -> Domain Error
        val result = engine.evaluate("(-5)!")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testFactorial_nonIntegerThrowsDomainError_edgeCase35() {
        // Edge Case 35: 3.5! -> Domain Error
        val result = engine.evaluate("3.5!")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    @Test
    fun testFactorial_exceedsLimitThrowsDomainErrorOrOverflow() {
        val result = engine.evaluate("1001!")
        assertThat(result).isInstanceOf(EvaluationResult.DomainError::class.java)
    }

    // =========================================================================
    // 7. Constants & Implicit Multiplication (Edge Cases 36, 37, 38, 39, 40)
    // =========================================================================

    @Test
    fun testConstants_piAndEuler() {
        val piRes = engine.evaluate("pi")
        assertThat((piRes as EvaluationResult.Success).formatted).startsWith("3.141592653589793")

        val eRes = engine.evaluate("e")
        assertThat((eRes as EvaluationResult.Success).formatted).startsWith("2.718281828459045")
    }

    @Test
    fun testImplicitMultiplication_numberAdjacentToParentheses_edgeCase36() {
        // Edge Case 36: 2(3) == 6
        val result = engine.evaluate("2(3)")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("6")
    }

    @Test
    fun testImplicitMultiplication_parenthesesAdjacentToParentheses_edgeCase37() {
        // Edge Case 37: (2)(3) == 6
        val result = engine.evaluate("(2)(3)")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("6")
    }

    @Test
    fun testImplicitMultiplication_numberAdjacentToConstantPi_edgeCase38() {
        // Edge Case 38: 2pi == 2 * pi
        val result = engine.evaluate("2pi")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).startsWith("6.283185307179586")
    }

    @Test
    fun testImplicitMultiplication_numberAdjacentToTrigFunction_edgeCase39() {
        // Edge Case 39: 2sin(30) in DEG == 1 (2 * 0.5)
        val result = engine.evaluate("2sin(30)", AngleUnit.DEGREES)
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("1")
    }

    @Test
    fun testParentheses_unclosedAutoBalancedOnEvaluate_edgeCase40() {
        // Edge Case 40: 2 * (3 + 4 == 14 (auto-closes trailing parentheses on eval)
        val result = engine.evaluate("2 * (3 + 4")
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("14")
    }

    @Test
    fun testNestedParentheses_complexAlgebraic() {
        val result = engine.evaluate("((2 + 3) * (4 + 1)) / 5")
        // (5 * 5) / 5 = 5
        assertThat(result).isInstanceOf(EvaluationResult.Success::class.java)
        assertThat((result as EvaluationResult.Success).formatted).isEqualTo("5")
    }
}
