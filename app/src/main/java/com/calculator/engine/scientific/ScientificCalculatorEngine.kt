package com.calculator.engine.scientific

import com.calculator.engine.AngleUnit
import com.calculator.engine.CalculatorEngine
import com.calculator.engine.CalculatorException
import com.calculator.engine.EvaluationResult
import com.calculator.engine.core.Evaluator
import com.calculator.engine.core.Precision
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext

class ScientificCalculatorEngine(
    private val evaluator: Evaluator = Evaluator()
) : CalculatorEngine {

    override fun evaluate(expression: String, angleUnit: AngleUnit): EvaluationResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) {
            return EvaluationResult.Success(BigDecimal.ZERO, "0")
        }

        return try {
            val processed = preprocessScientificExpression(trimmed)
            val balanced = autoCloseParentheses(processed)
            val result = evaluator.evaluate(balanced, angleUnit)
            EvaluationResult.Success(result, Precision.format(result))
        } catch (e: CalculatorException.DivisionByZero) {
            EvaluationResult.DomainError(e.message ?: "Cannot divide by zero")
        } catch (e: CalculatorException.DomainError) {
            EvaluationResult.DomainError(e.message ?: "Domain Error")
        } catch (e: CalculatorException.Overflow) {
            EvaluationResult.DomainError("Overflow")
        } catch (e: CalculatorException.SyntaxError) {
            EvaluationResult.SyntaxError(e.message ?: "Syntax Error")
        } catch (e: Exception) {
            EvaluationResult.SyntaxError(e.message ?: "Syntax Error")
        }
    }

    override fun evaluatePreview(expression: String, angleUnit: AngleUnit): String? {
        val sanitized = sanitizeForPreview(expression) ?: return null
        return when (val result = evaluate(sanitized, angleUnit)) {
            is EvaluationResult.Success -> result.formatted
            is EvaluationResult.DomainError -> result.message
            is EvaluationResult.SyntaxError -> null
        }
    }

    override fun calculatePercentage(currentExpression: String, percentValue: String): String {
        if (percentValue.isBlank()) return "0"
        val percentNum = try {
            BigDecimal(percentValue.trim())
        } catch (e: Exception) {
            return "0"
        }
        val res = percentNum.divide(BigDecimal(100), MathContext.DECIMAL128)
        return Precision.format(res)
    }

    companion object {
        fun factorial(n: BigDecimal): BigDecimal {
            if (n < BigDecimal.ZERO) throw CalculatorException.DomainError("Domain Error: Factorial undefined for negative numbers")
            if (n.stripTrailingZeros().scale() > 0) throw CalculatorException.DomainError("Domain Error: Factorial requires an integer")
            if (n > BigDecimal(1000)) throw CalculatorException.Overflow("Factorial exceeds maximum limit (n <= 1000)")

            val nInt = n.toInt()
            if (nInt == 0 || nInt == 1) return BigDecimal.ONE

            var acc = BigInteger.ONE
            for (i in 2..nInt) {
                acc = acc.multiply(BigInteger.valueOf(i.toLong()))
            }
            return BigDecimal(acc)
        }

        fun sinh(x: BigDecimal): BigDecimal {
            if (kotlin.math.abs(x.toDouble()) > 710.0) throw CalculatorException.Overflow("Hyperbolic sine overflow")
            if (x.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
            return BigDecimal.valueOf(kotlin.math.sinh(x.toDouble()))
        }

        fun cosh(x: BigDecimal): BigDecimal {
            if (kotlin.math.abs(x.toDouble()) > 710.0) throw CalculatorException.Overflow("Hyperbolic cosine overflow")
            if (x.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ONE
            return BigDecimal.valueOf(kotlin.math.cosh(x.toDouble()))
        }

        fun tanh(x: BigDecimal): BigDecimal {
            val d = x.toDouble()
            if (d > 20.0) return BigDecimal.ONE
            if (d < -20.0) return BigDecimal("-1")
            if (x.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
            return BigDecimal.valueOf(kotlin.math.tanh(d))
        }

        fun asinh(x: BigDecimal): BigDecimal {
            val d = x.toDouble()
            if (d == 0.0) return BigDecimal.ZERO
            return BigDecimal.valueOf(kotlin.math.ln(d + kotlin.math.sqrt(d * d + 1.0)))
        }

        fun acosh(x: BigDecimal): BigDecimal {
            if (x < BigDecimal.ONE) throw CalculatorException.DomainError("Domain Error: acosh argument must be >= 1")
            val d = x.toDouble()
            if (d == 1.0) return BigDecimal.ZERO
            return BigDecimal.valueOf(kotlin.math.ln(d + kotlin.math.sqrt(d * d - 1.0)))
        }

        fun atanh(x: BigDecimal): BigDecimal {
            val d = x.toDouble()
            if (kotlin.math.abs(d) >= 1.0) throw CalculatorException.DomainError("Domain Error: atanh argument must be in (-1, 1)")
            if (d == 0.0) return BigDecimal.ZERO
            return BigDecimal.valueOf(0.5 * kotlin.math.ln((1.0 + d) / (1.0 - d)))
        }

        fun ln(x: BigDecimal): BigDecimal {
            if (x <= BigDecimal.ZERO) throw CalculatorException.DomainError("Domain Error: ln requires positive argument")
            if (x == BigDecimal.ONE) return BigDecimal.ZERO
            if (x.subtract(ExactTrigTables.E_DECIMAL).abs() < BigDecimal("1e-15")) {
                return BigDecimal.ONE
            }
            return BigDecimal.valueOf(kotlin.math.ln(x.toDouble()))
        }

        fun log10(x: BigDecimal): BigDecimal {
            if (x <= BigDecimal.ZERO) throw CalculatorException.DomainError("Domain Error: log10 requires positive argument")
            if (x == BigDecimal.ONE) return BigDecimal.ZERO
            val d = x.toDouble()
            val r = Math.round(kotlin.math.log10(d))
            if (r in -308..308 && Math.pow(10.0, r.toDouble()) == d) {
                return BigDecimal.valueOf(r)
            }
            return BigDecimal.valueOf(kotlin.math.log10(d))
        }

        fun log2(x: BigDecimal): BigDecimal {
            if (x <= BigDecimal.ZERO) throw CalculatorException.DomainError("Domain Error: log2 requires positive argument")
            if (x == BigDecimal.ONE) return BigDecimal.ZERO
            val d = x.toDouble()
            val r = Math.round(kotlin.math.ln(d) / kotlin.math.ln(2.0))
            if (r in -1000..1000 && Math.pow(2.0, r.toDouble()) == d) {
                return BigDecimal.valueOf(r)
            }
            return BigDecimal.valueOf(kotlin.math.ln(d) / kotlin.math.ln(2.0))
        }

        fun exp(x: BigDecimal): BigDecimal {
            val d = x.toDouble()
            if (d > 709.0) throw CalculatorException.Overflow("Exponential overflow")
            if (d < -745.0) return BigDecimal.ZERO
            if (x.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ONE
            return BigDecimal.valueOf(kotlin.math.exp(d))
        }

        fun exp10(x: BigDecimal): BigDecimal {
            val d = x.toDouble()
            if (d > 308.0) throw CalculatorException.Overflow("Base-10 exponential overflow")
            if (d < -324.0) return BigDecimal.ZERO
            if (x.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ONE
            if (x.stripTrailingZeros().scale() <= 0 && x >= BigDecimal.ZERO && x <= BigDecimal(308)) {
                return BigDecimal.TEN.pow(x.toInt())
            }
            return BigDecimal.valueOf(Math.pow(10.0, d))
        }

        fun power(x: BigDecimal, y: BigDecimal): BigDecimal {
            if (x.compareTo(BigDecimal.ZERO) == 0) {
                if (y < BigDecimal.ZERO) throw CalculatorException.DivisionByZero()
                if (y.compareTo(BigDecimal.ZERO) == 0) throw CalculatorException.DomainError("Domain Error: 0^0 is undefined")
                return BigDecimal.ZERO
            }
            if (y.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ONE

            val isIntExponent = y.stripTrailingZeros().scale() <= 0
            if (x < BigDecimal.ZERO) {
                if (!isIntExponent) throw CalculatorException.DomainError("Domain Error: Negative base cannot have non-integer exponent")
                val yBigInt = y.toBigInteger()
                val absX = x.abs()
                val magnitude = if (yBigInt >= BigInteger.ZERO && yBigInt <= BigInteger.valueOf(9999)) {
                    absX.pow(yBigInt.toInt(), MathContext.DECIMAL128)
                } else if (yBigInt < BigInteger.ZERO && yBigInt >= BigInteger.valueOf(-9999)) {
                    BigDecimal.ONE.divide(absX.pow((-yBigInt).toInt(), MathContext.DECIMAL128), MathContext.DECIMAL128)
                } else {
                    BigDecimal.valueOf(Math.pow(absX.toDouble(), y.toDouble()))
                }
                return if (yBigInt.testBit(0)) -magnitude else magnitude
            }

            if (isIntExponent) {
                val yBigInt = y.toBigInteger()
                if (yBigInt >= BigInteger.ZERO && yBigInt <= BigInteger.valueOf(9999)) {
                    return x.pow(yBigInt.toInt(), MathContext.DECIMAL128)
                } else if (yBigInt < BigInteger.ZERO && yBigInt >= BigInteger.valueOf(-9999)) {
                    return BigDecimal.ONE.divide(x.pow((-yBigInt).toInt(), MathContext.DECIMAL128), MathContext.DECIMAL128)
                }
            }

            val d = Math.pow(x.toDouble(), y.toDouble())
            if (d.isInfinite() || d.isNaN()) throw CalculatorException.Overflow("Power overflow")
            val r = Math.round(d)
            if (Math.abs(d - r) < 1e-12) {
                return BigDecimal.valueOf(r)
            }
            return BigDecimal.valueOf(d)
        }

        fun sqrt(x: BigDecimal): BigDecimal {
            if (x < BigDecimal.ZERO) throw CalculatorException.DomainError("Domain Error: Square root of negative number")
            if (x.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
            if (x.stripTrailingZeros().scale() <= 0 && x <= BigDecimal(Long.MAX_VALUE)) {
                val r = Math.round(Math.sqrt(x.toDouble()))
                if (r * r == x.toLong()) return BigDecimal.valueOf(r)
            }
            return x.sqrt(MathContext.DECIMAL128)
        }

        fun cbrt(x: BigDecimal): BigDecimal {
            if (x.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
            if (x < BigDecimal.ZERO) return -cbrt(-x)
            if (x.stripTrailingZeros().scale() <= 0 && x <= BigDecimal(Long.MAX_VALUE)) {
                val r = Math.round(Math.cbrt(x.toDouble()))
                if (r * r * r == x.toLong()) return BigDecimal.valueOf(r)
            }
            return BigDecimal.valueOf(Math.cbrt(x.toDouble()))
        }

        fun reciprocal(x: BigDecimal): BigDecimal {
            if (x.compareTo(BigDecimal.ZERO) == 0) throw CalculatorException.DivisionByZero()
            return BigDecimal.ONE.divide(x, MathContext.DECIMAL128)
        }
    }

    private fun preprocessScientificExpression(expr: String): String {
        var text = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("—", "-")
            .replace("π", "pi")
            .replace("√", "sqrt")

        text = transformPercentages(text)
        return text
    }

    private fun transformPercentages(expression: String): String {
        val percentRegex = Regex("""(\d+(?:\.\d+)?)\s*%""")
        var text = expression

        var match = percentRegex.find(text)
        while (match != null) {
            val bStr = match.groupValues[1]
            val beforePercent = text.substring(0, match.range.first).trimEnd()

            val opMatch = Regex("""([+\-*/])\s*$""").find(beforePercent)
            if (opMatch != null) {
                val op = opMatch.groupValues[1]
                val aExpr = beforePercent.substring(0, opMatch.range.first).trim()
                if (aExpr.isNotEmpty() && (op == "+" || op == "-")) {
                    val replacement = "(($aExpr) * ($bStr / 100))"
                    text = text.substring(0, opMatch.range.last + 1) + " " + replacement + text.substring(match.range.last + 1)
                } else {
                    val replacement = "($bStr / 100)"
                    text = text.substring(0, match.range.first) + replacement + text.substring(match.range.last + 1)
                }
            } else {
                val replacement = "($bStr / 100)"
                text = text.substring(0, match.range.first) + replacement + text.substring(match.range.last + 1)
            }
            match = percentRegex.find(text)
        }
        return text
    }

    private fun autoCloseParentheses(expr: String): String {
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }
        return if (openCount > closeCount) {
            expr + ")".repeat(openCount - closeCount)
        } else {
            expr
        }
    }

    private fun sanitizeForPreview(expression: String): String? {
        var expr = expression.trim()
        if (expr.isEmpty()) return null

        while (expr.isNotEmpty()) {
            val lastChar = expr.last()
            if (lastChar in "+-*/^,%×÷" || lastChar == '.') {
                expr = expr.dropLast(1).trimEnd()
            } else if (expr.endsWith("mod", ignoreCase = true)) {
                expr = expr.dropLast(3).trimEnd()
            } else {
                break
            }
        }
        if (expr.isEmpty()) return null

        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }
        if (openCount > closeCount) {
            if (expr.endsWith("(")) {
                expr = expr.dropLast(1).trimEnd()
                while (expr.isNotEmpty() && (expr.last() in "+-*/^,%×÷" || expr.last() == '.')) {
                    expr = expr.dropLast(1).trimEnd()
                }
                if (expr.isEmpty()) return null
                val newOpen = expr.count { it == '(' }
                val newClose = expr.count { it == ')' }
                if (newOpen > newClose) {
                    expr += ")".repeat(newOpen - newClose)
                }
            } else {
                expr += ")".repeat(openCount - closeCount)
            }
        }
        return expr
    }
}
