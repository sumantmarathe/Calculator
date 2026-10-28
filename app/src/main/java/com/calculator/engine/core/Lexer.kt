package com.calculator.engine.core

import com.calculator.engine.CalculatorException
import java.math.BigDecimal

object Lexer {

    fun tokenize(input: String): List<Token> {
        val sanitized = sanitize(input)
        if (sanitized.isEmpty()) return emptyList()
        val rawTokens = scan(sanitized)
        val withDisambiguatedSigns = disambiguateSigns(rawTokens)
        val withImplicitMultiply = insertImplicitMultiplication(withDisambiguatedSigns)
        return expandPercentages(withImplicitMultiply)
    }

    private fun sanitize(input: String): String {
        return input
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("—", "-")
            .replace("π", "pi")
            .replace("√", "sqrt")
            .replace("\\s+".toRegex(), "")
    }

    private fun scan(text: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val length = text.length

        while (i < length) {
            val c = text[i]

            when {
                c.isDigit() || (c == '.' && i + 1 < length && text[i + 1].isDigit()) -> {
                    val start = i
                    var hasDot = false
                    while (i < length && (text[i].isDigit() || text[i] == '.')) {
                        if (text[i] == '.') {
                            if (hasDot) throw CalculatorException.SyntaxError("Invalid number format at index $i")
                            hasDot = true
                        }
                        i++
                    }
                    if (i < length && (text[i] == 'e' || text[i] == 'E')) {
                        var temp = i + 1
                        if (temp < length && (text[temp] == '+' || text[temp] == '-')) {
                            temp++
                        }
                        if (temp < length && text[temp].isDigit()) {
                            i = temp
                            while (i < length && text[i].isDigit()) {
                                i++
                            }
                        }
                    }
                    val numStr = text.substring(start, i)
                    tokens.add(Token.Number(BigDecimal(numStr, Precision.MATH_CONTEXT), numStr))
                }
                c == '+' -> {
                    tokens.add(Token.Operator.Add)
                    i++
                }
                c == '-' -> {
                    tokens.add(Token.Operator.Subtract)
                    i++
                }
                c == '*' -> {
                    tokens.add(Token.Operator.Multiply)
                    i++
                }
                c == '/' -> {
                    tokens.add(Token.Operator.Divide)
                    i++
                }
                c == '^' -> {
                    tokens.add(Token.Operator.Power)
                    i++
                }
                c == '%' -> {
                    tokens.add(Token.Operator.Percentage)
                    i++
                }
                c == '!' -> {
                    tokens.add(Token.Operator.Factorial)
                    i++
                }
                c == '(' -> {
                    tokens.add(Token.LeftParen)
                    i++
                }
                c == ')' -> {
                    tokens.add(Token.RightParen)
                    i++
                }
                c == ',' -> {
                    tokens.add(Token.Comma)
                    i++
                }
                c.isLetter() -> {
                    val start = i
                    while (i < length && text[i].isLetter()) {
                        i++
                    }
                    var ident = text.substring(start, i).lowercase()
                    if (ident == "log" && i + 2 <= length && text.substring(i, i + 2) == "10") {
                        ident = "log10"
                        i += 2
                    } else if (ident == "log" && i + 1 <= length && text[i] == '2') {
                        ident = "log2"
                        i += 1
                    } else if (ident == "pow" && i + 2 <= length && text.substring(i, i + 2) == "10") {
                        ident = "pow10"
                        i += 2
                    }
                    when (ident) {
                        "pi" -> tokens.add(Token.Constant(Token.ConstantType.PI))
                        "e" -> tokens.add(Token.Constant(Token.ConstantType.E))
                        "mod" -> tokens.add(Token.Operator.Modulo)
                        else -> {
                            val funcType = Token.FunctionType.entries.find { it.identifier.equals(ident, ignoreCase = true) }
                                ?: throw CalculatorException.SyntaxError("Unknown identifier: '$ident'")
                            tokens.add(Token.Function(funcType))
                        }
                    }
                }
                else -> throw CalculatorException.SyntaxError("Unexpected character: '$c'")
            }
        }
        return tokens
    }

    private fun disambiguateSigns(tokens: List<Token>): List<Token> {
        val result = mutableListOf<Token>()
        for (i in tokens.indices) {
            val token = tokens[i]
            if (token is Token.Operator.Subtract || token is Token.Operator.Add) {
                val isUnary = if (i == 0) {
                    true
                } else {
                    val prev = result.last()
                    when (prev) {
                        is Token.Operator -> !prev.isPostfix
                        is Token.LeftParen, is Token.Comma, is Token.Function -> true
                        else -> false
                    }
                }
                if (isUnary) {
                    if (token is Token.Operator.Subtract) {
                        result.add(Token.Operator.UnaryMinus)
                    } else {
                        result.add(Token.Operator.UnaryPlus)
                    }
                } else {
                    result.add(token)
                }
            } else {
                result.add(token)
            }
        }
        return result
    }

    private fun insertImplicitMultiplication(tokens: List<Token>): List<Token> {
        if (tokens.size < 2) return tokens
        val result = mutableListOf<Token>()

        for (i in 0 until tokens.size - 1) {
            val current = tokens[i]
            val next = tokens[i + 1]
            result.add(current)

            val currentCanPrecedeImplicit = current is Token.Number ||
                    current is Token.Constant ||
                    current is Token.RightParen ||
                    (current is Token.Operator && current.isPostfix)

            val nextCanFollowImplicit = next is Token.Constant ||
                    next is Token.LeftParen ||
                    next is Token.Function ||
                    (next is Token.Number && (current is Token.Constant || current is Token.RightParen || (current is Token.Operator && current.isPostfix)))

            if (currentCanPrecedeImplicit && nextCanFollowImplicit) {
                result.add(Token.Operator.Multiply)
            }
        }
        result.add(tokens.last())
        return result
    }

    private fun expandPercentages(tokens: List<Token>): List<Token> {
        val result = mutableListOf<Token>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token is Token.Operator.Percentage && result.size >= 2) {
                val prev = result[result.size - 1]
                val prevOp = result[result.size - 2]
                if ((prevOp is Token.Operator.Add || prevOp is Token.Operator.Subtract) &&
                    (prev is Token.Number || prev is Token.Constant) &&
                    result.size >= 3
                ) {
                    val baseOperand = result[result.size - 3]
                    result.removeAt(result.size - 1)
                    result.add(Token.LeftParen)
                    result.add(baseOperand)
                    result.add(Token.Operator.Multiply)
                    result.add(Token.LeftParen)
                    result.add(prev)
                    result.add(Token.Operator.Divide)
                    result.add(Token.Number(BigDecimal(100, Precision.MATH_CONTEXT)))
                    result.add(Token.RightParen)
                    result.add(Token.RightParen)
                    i++
                    continue
                }
            }
            result.add(token)
            i++
        }
        return result
    }
}
