package com.calculator.engine.normal

import java.math.BigDecimal
import java.math.MathContext

/**
 * Thread-safe calculation memory register providing M+, M-, MR, MC, and state inspection.
 */
class MemoryRegister(initialValue: BigDecimal = BigDecimal.ZERO) {
    @Volatile
    private var _value: BigDecimal = initialValue

    val value: BigDecimal
        @Synchronized get() = _value

    val hasMemory: Boolean
        @Synchronized get() = _value.compareTo(BigDecimal.ZERO) != 0

    @Synchronized
    fun hasMemory(): Boolean = hasMemory

    @Synchronized
    fun add(operand: BigDecimal): BigDecimal {
        _value = _value.add(operand, MathContext.DECIMAL128)
        return _value
    }

    @Synchronized
    fun subtract(operand: BigDecimal): BigDecimal {
        _value = _value.subtract(operand, MathContext.DECIMAL128)
        return _value
    }

    @Synchronized
    fun recall(): BigDecimal = _value

    @Synchronized
    fun clear(): BigDecimal {
        _value = BigDecimal.ZERO
        return _value
    }

    @Synchronized
    fun set(newValue: BigDecimal) {
        _value = newValue
    }
}
