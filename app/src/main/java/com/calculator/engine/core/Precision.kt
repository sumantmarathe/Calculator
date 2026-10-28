package com.calculator.engine.core

import java.math.BigDecimal
import java.math.MathContext

object Precision {
    val MATH_CONTEXT: MathContext = MathContext.DECIMAL128

    fun format(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return "0"
        }
        val roundedInt = value.setScale(0, java.math.RoundingMode.HALF_UP)
        val diff = (value - roundedInt).abs()
        val normalized = if (diff > BigDecimal.ZERO && diff < BigDecimal("1e-13")) {
            roundedInt
        } else {
            value
        }
        val stripped = normalized.stripTrailingZeros()
        if (stripped.compareTo(BigDecimal.ZERO) == 0) {
            return "0"
        }
        return stripped.toPlainString()
    }
}
