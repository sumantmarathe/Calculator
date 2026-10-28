package com.calculator.engine.core

import java.math.BigDecimal

sealed interface Token {
    data class Number(val value: BigDecimal, val raw: String = value.toPlainString()) : Token

    enum class ConstantType(val symbol: String, val value: BigDecimal) {
        PI("pi", BigDecimal("3.1415926535897932384626433832795028841971693993751")),
        E("e", BigDecimal("2.7182818284590452353602874713526624977572470936999"))
    }
    data class Constant(val type: ConstantType) : Token

    enum class Associativity {
        LEFT, RIGHT
    }

    sealed class Operator(
        val symbol: String,
        val precedence: Int,
        val associativity: Associativity,
        val isUnary: Boolean = false,
        val isPostfix: Boolean = false
    ) : Token {
        // Precedence Rank 1: Additive (+, -)
        data object Add : Operator("+", precedence = 1, associativity = Associativity.LEFT)
        data object Subtract : Operator("-", precedence = 1, associativity = Associativity.LEFT)

        // Precedence Rank 2: Multiplicative (*, /, mod)
        data object Multiply : Operator("*", precedence = 2, associativity = Associativity.LEFT)
        data object Divide : Operator("/", precedence = 2, associativity = Associativity.LEFT)
        data object Modulo : Operator("mod", precedence = 2, associativity = Associativity.LEFT)

        // Precedence Rank 3: Unary Prefix (+, -)
        data object UnaryMinus : Operator("-", precedence = 3, associativity = Associativity.RIGHT, isUnary = true)
        data object UnaryPlus : Operator("+", precedence = 3, associativity = Associativity.RIGHT, isUnary = true)

        // Precedence Rank 4: Power (^) - Right Associative
        data object Power : Operator("^", precedence = 4, associativity = Associativity.RIGHT)

        // Precedence Rank 5: Postfix Unary (%, !) - Left Associative
        data object Percentage : Operator("%", precedence = 5, associativity = Associativity.LEFT, isUnary = true, isPostfix = true)
        data object Factorial : Operator("!", precedence = 5, associativity = Associativity.LEFT, isUnary = true, isPostfix = true)
    }

    enum class FunctionType(val identifier: String) {
        SIN("sin"),
        COS("cos"),
        TAN("tan"),
        ASIN("asin"),
        ACOS("acos"),
        ATAN("atan"),
        SINH("sinh"),
        COSH("cosh"),
        TANH("tanh"),
        ASINH("asinh"),
        ACOSH("acosh"),
        ATANH("atanh"),
        LN("ln"),
        LOG10("log10"),
        LOG2("log2"),
        SQRT("sqrt"),
        CBRT("cbrt"),
        EXP("exp"),
        POW10("pow10"),
        ABS("abs")
    }
    data class Function(val type: FunctionType) : Token

    data object LeftParen : Token
    data object RightParen : Token
    data object Comma : Token
}
