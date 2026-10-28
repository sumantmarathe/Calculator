package com.calculator.engine.core

import com.calculator.engine.CalculatorException

object ShuntingYard {

    fun parseToRpn(tokens: List<Token>, autoBalance: Boolean = true): List<Token> {
        val balancedTokens = if (autoBalance) autoBalanceParens(tokens) else tokens
        val outputQueue = mutableListOf<Token>()
        val operatorStack = ArrayDeque<Token>()

        for (token in balancedTokens) {
            when (token) {
                is Token.Number, is Token.Constant -> {
                    outputQueue.add(token)
                }
                is Token.Function -> {
                    operatorStack.addLast(token)
                }
                is Token.Comma -> {
                    while (operatorStack.isNotEmpty() && operatorStack.last() !is Token.LeftParen) {
                        outputQueue.add(operatorStack.removeLast())
                    }
                    if (operatorStack.isEmpty() || operatorStack.last() !is Token.LeftParen) {
                        throw CalculatorException.SyntaxError("Misplaced comma or missing '('")
                    }
                }
                is Token.Operator -> {
                    if (token.isPostfix) {
                        outputQueue.add(token)
                    } else if (token.isUnary) {
                        operatorStack.addLast(token)
                    } else {
                        while (operatorStack.isNotEmpty()) {
                            val top = operatorStack.last()
                            if (top is Token.LeftParen) break

                            val topPrec = when (top) {
                                is Token.Operator -> top.precedence
                                is Token.Function -> 6
                                else -> 0
                            }
                            val incomingPrec = token.precedence

                            if (topPrec > incomingPrec || (topPrec == incomingPrec && token.associativity == Token.Associativity.LEFT)) {
                                outputQueue.add(operatorStack.removeLast())
                            } else {
                                break
                            }
                        }
                        operatorStack.addLast(token)
                    }
                }
                is Token.LeftParen -> {
                    operatorStack.addLast(token)
                }
                is Token.RightParen -> {
                    var foundLeft = false
                    while (operatorStack.isNotEmpty()) {
                        val top = operatorStack.removeLast()
                        if (top is Token.LeftParen) {
                            foundLeft = true
                            break
                        }
                        outputQueue.add(top)
                    }
                    if (!foundLeft) {
                        throw CalculatorException.SyntaxError("Mismatched parentheses: unexpected ')'")
                    }
                    if (operatorStack.isNotEmpty() && operatorStack.last() is Token.Function) {
                        outputQueue.add(operatorStack.removeLast())
                    }
                }
            }
        }

        while (operatorStack.isNotEmpty()) {
            val top = operatorStack.removeLast()
            if (top is Token.LeftParen) {
                throw CalculatorException.SyntaxError("Mismatched parentheses: unclosed '('")
            }
            outputQueue.add(top)
        }

        return outputQueue
    }

    private fun autoBalanceParens(tokens: List<Token>): List<Token> {
        var openCount = 0
        for (t in tokens) {
            if (t is Token.LeftParen) openCount++
            if (t is Token.RightParen) openCount--
        }
        if (openCount <= 0) return tokens

        val result = tokens.toMutableList()
        repeat(openCount) {
            result.add(Token.RightParen)
        }
        return result
    }
}
