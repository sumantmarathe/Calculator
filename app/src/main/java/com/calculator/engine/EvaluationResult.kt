package com.calculator.engine

import java.math.BigDecimal

sealed class EvaluationResult {
    data class Success(val value: BigDecimal, val formatted: String) : EvaluationResult()
    data class DomainError(val message: String) : EvaluationResult()
    data class SyntaxError(val message: String) : EvaluationResult()
}
