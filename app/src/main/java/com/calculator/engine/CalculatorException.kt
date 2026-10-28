package com.calculator.engine

sealed class CalculatorException(message: String) : Exception(message) {
    class DivisionByZero(message: String = "Cannot divide by zero") : CalculatorException(message)
    class DomainError(message: String = "Domain Error") : CalculatorException(message)
    class Overflow(message: String = "Overflow") : CalculatorException(message)
    class SyntaxError(message: String = "Syntax Error") : CalculatorException(message)
}
