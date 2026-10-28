package com.calculator.engine.normal

import com.calculator.engine.AngleUnit
import com.calculator.engine.CalculatorEngine
import com.calculator.engine.CalculatorException
import com.calculator.engine.EvaluationResult
import com.calculator.engine.core.Evaluator
import com.calculator.engine.core.Precision
import java.math.BigDecimal
import java.math.MathContext

class NormalCalculatorEngine(
    val memory: MemoryRegister = MemoryRegister(),
    private val evaluator: Evaluator = Evaluator()
) : CalculatorEngine {

    val memoryRegister: MemoryRegister get() = memory

    override fun evaluate(expression: String, angleUnit: AngleUnit): EvaluationResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) {
            return EvaluationResult.Success(BigDecimal.ZERO, "0")
        }

        return try {
            val processed = preprocessNormalExpression(trimmed)
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

        val trimmedExpr = currentExpression.trim()
        if (trimmedExpr.isEmpty()) {
            val res = percentNum.divide(BigDecimal(100), MathContext.DECIMAL128)
            return Precision.format(res)
        }

        val match = Regex("""^(.*?)\s*([+\-*/×÷−])\s*$""").find(trimmedExpr)
        if (match != null) {
            val baseExprStr = match.groupValues[1]
            val op = match.groupValues[2]
            val baseVal = try {
                val eval = evaluate(baseExprStr)
                if (eval is EvaluationResult.Success) eval.value else BigDecimal.ZERO
            } catch (e: Exception) {
                BigDecimal.ZERO
            }

            val result = when (op) {
                "+", "-", "−" -> baseVal.multiply(percentNum.divide(BigDecimal(100), MathContext.DECIMAL128), MathContext.DECIMAL128)
                "*", "×", "/", "÷" -> percentNum.divide(BigDecimal(100), MathContext.DECIMAL128)
                else -> percentNum.divide(BigDecimal(100), MathContext.DECIMAL128)
            }
            return Precision.format(result)
        }

        val res = percentNum.divide(BigDecimal(100), MathContext.DECIMAL128)
        return Precision.format(res)
    }

    fun addToMemory(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): Boolean {
        return when (val res = evaluate(expression, angleUnit)) {
            is EvaluationResult.Success -> {
                memory.add(res.value)
                true
            }
            else -> false
        }
    }

    fun subtractFromMemory(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): Boolean {
        return when (val res = evaluate(expression, angleUnit)) {
            is EvaluationResult.Success -> {
                memory.subtract(res.value)
                true
            }
            else -> false
        }
    }

    private fun preprocessNormalExpression(expr: String): String {
        var result = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("—", "-")

        result = transformPercentages(result)
        return result
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
