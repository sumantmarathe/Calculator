package com.calculator.engine.core

import com.calculator.engine.AngleUnit
import com.calculator.engine.CalculatorException
import com.calculator.engine.scientific.ExactTrigTables
import com.calculator.engine.scientific.ScientificCalculatorEngine
import java.math.BigDecimal

class Evaluator {

    fun evaluate(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): BigDecimal {
        val tokens = Lexer.tokenize(expression)
        val rpn = ShuntingYard.parseToRpn(tokens)
        return evaluateRpn(rpn, angleUnit)
    }

    companion object {
        fun evaluate(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): BigDecimal {
            val tokens = Lexer.tokenize(expression)
            val rpn = ShuntingYard.parseToRpn(tokens)
            return evaluateRpn(rpn, angleUnit)
        }

        fun evaluateRpn(rpn: List<Token>, angleUnit: AngleUnit = AngleUnit.DEGREES): BigDecimal {
            if (rpn.isEmpty()) return BigDecimal.ZERO
            val stack = ArrayDeque<BigDecimal>()

            for (token in rpn) {
                when (token) {
                    is Token.Number -> stack.addLast(token.value)
                    is Token.Constant -> stack.addLast(token.type.value)
                    is Token.Operator -> {
                        if (token.isUnary) {
                            if (stack.isEmpty()) throw CalculatorException.SyntaxError("Missing operand for unary operator ${token.symbol}")
                            val operand = stack.removeLast()
                            val result = evaluateUnary(token, operand)
                            stack.addLast(result)
                        } else {
                            if (stack.size < 2) throw CalculatorException.SyntaxError("Missing operand for operator ${token.symbol}")
                            val b = stack.removeLast()
                            val a = stack.removeLast()
                            val result = evaluateBinary(token, a, b)
                            stack.addLast(result)
                        }
                    }
                    is Token.Function -> {
                        if (stack.isEmpty()) throw CalculatorException.SyntaxError("Missing argument for function ${token.type.identifier}")
                        val arg = stack.removeLast()
                        val result = evaluateFunction(token.type, arg, angleUnit)
                        stack.addLast(result)
                    }
                    else -> throw CalculatorException.SyntaxError("Unexpected token in RPN evaluation: $token")
                }
            }

            if (stack.size != 1) {
                throw CalculatorException.SyntaxError("Malformed expression: leftover operands on stack")
            }
            return stack.removeLast()
        }

        private fun evaluateUnary(op: Token.Operator, a: BigDecimal): BigDecimal {
            return when (op) {
                is Token.Operator.UnaryMinus -> a.negate(Precision.MATH_CONTEXT)
                is Token.Operator.UnaryPlus -> a
                is Token.Operator.Percentage -> a.divide(BigDecimal(100, Precision.MATH_CONTEXT), Precision.MATH_CONTEXT)
                is Token.Operator.Factorial -> ScientificCalculatorEngine.factorial(a)
                else -> throw CalculatorException.SyntaxError("Unsupported unary operator ${op.symbol}")
            }
        }

        private fun evaluateBinary(op: Token.Operator, a: BigDecimal, b: BigDecimal): BigDecimal {
            return when (op) {
                is Token.Operator.Add -> a.add(b, Precision.MATH_CONTEXT)
                is Token.Operator.Subtract -> a.subtract(b, Precision.MATH_CONTEXT)
                is Token.Operator.Multiply -> a.multiply(b, Precision.MATH_CONTEXT)
                is Token.Operator.Divide -> {
                    if (b.compareTo(BigDecimal.ZERO) == 0) throw CalculatorException.DivisionByZero()
                    a.divide(b, Precision.MATH_CONTEXT)
                }
                is Token.Operator.Modulo -> {
                    if (b.compareTo(BigDecimal.ZERO) == 0) throw CalculatorException.DivisionByZero()
                    a.remainder(b, Precision.MATH_CONTEXT)
                }
                is Token.Operator.Power -> ScientificCalculatorEngine.power(a, b)
                else -> throw CalculatorException.SyntaxError("Unsupported binary operator ${op.symbol}")
            }
        }

        private fun evaluateFunction(type: Token.FunctionType, x: BigDecimal, unit: AngleUnit): BigDecimal {
            return when (type) {
                Token.FunctionType.SIN -> ExactTrigTables.sin(x, unit)
                Token.FunctionType.COS -> ExactTrigTables.cos(x, unit)
                Token.FunctionType.TAN -> ExactTrigTables.tan(x, unit)
                Token.FunctionType.ASIN -> ExactTrigTables.asin(x, unit)
                Token.FunctionType.ACOS -> ExactTrigTables.acos(x, unit)
                Token.FunctionType.ATAN -> ExactTrigTables.atan(x, unit)
                Token.FunctionType.SINH -> ScientificCalculatorEngine.sinh(x)
                Token.FunctionType.COSH -> ScientificCalculatorEngine.cosh(x)
                Token.FunctionType.TANH -> ScientificCalculatorEngine.tanh(x)
                Token.FunctionType.ASINH -> ScientificCalculatorEngine.asinh(x)
                Token.FunctionType.ACOSH -> ScientificCalculatorEngine.acosh(x)
                Token.FunctionType.ATANH -> ScientificCalculatorEngine.atanh(x)
                Token.FunctionType.LN -> ScientificCalculatorEngine.ln(x)
                Token.FunctionType.LOG10 -> ScientificCalculatorEngine.log10(x)
                Token.FunctionType.LOG2 -> ScientificCalculatorEngine.log2(x)
                Token.FunctionType.SQRT -> ScientificCalculatorEngine.sqrt(x)
                Token.FunctionType.CBRT -> ScientificCalculatorEngine.cbrt(x)
                Token.FunctionType.EXP -> ScientificCalculatorEngine.exp(x)
                Token.FunctionType.POW10 -> ScientificCalculatorEngine.exp10(x)
                Token.FunctionType.ABS -> x.abs(Precision.MATH_CONTEXT)
            }
        }
    }
}
