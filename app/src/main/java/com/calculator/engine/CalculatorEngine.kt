package com.calculator.engine

interface CalculatorEngine {
    fun evaluate(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): EvaluationResult
    fun evaluatePreview(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): String?
    fun calculatePercentage(currentExpression: String, percentValue: String): String
}

enum class AngleUnit {
    DEGREES, RADIANS, GRADIANS
}
