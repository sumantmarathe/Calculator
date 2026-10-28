package com.calculator.engine.scientific

import com.calculator.engine.AngleUnit
import com.calculator.engine.CalculatorException
import java.math.BigDecimal
import java.math.MathContext

object ExactTrigTables {
    val PI_DECIMAL = BigDecimal("3.1415926535897932384626433832795028841971693993751")
    val E_DECIMAL = BigDecimal("2.7182818284590452353602874713526624977572470936999")

    fun sin(angle: BigDecimal, unit: AngleUnit): BigDecimal {
        return when (unit) {
            AngleUnit.DEGREES -> {
                val norm = normalizeAngle(angle, BigDecimal(360))
                when (norm.stripTrailingZeros().toPlainString()) {
                    "0", "180", "360" -> BigDecimal.ZERO
                    "90" -> BigDecimal.ONE
                    "270" -> BigDecimal("-1")
                    "30", "150" -> BigDecimal("0.5")
                    "210", "330" -> BigDecimal("-0.5")
                    else -> BigDecimal.valueOf(kotlin.math.sin(norm.toDouble() * Math.PI / 180.0))
                }
            }
            AngleUnit.GRADIANS -> {
                val norm = normalizeAngle(angle, BigDecimal(400))
                when (norm.stripTrailingZeros().toPlainString()) {
                    "0", "200", "400" -> BigDecimal.ZERO
                    "100" -> BigDecimal.ONE
                    "300" -> BigDecimal("-1")
                    else -> BigDecimal.valueOf(kotlin.math.sin(norm.toDouble() * Math.PI / 200.0))
                }
            }
            AngleUnit.RADIANS -> {
                val d = angle.toDouble()
                if (d == 0.0) return BigDecimal.ZERO
                val k = Math.round(d / Math.PI)
                if (Math.abs(d - k * Math.PI) < 1e-12) return BigDecimal.ZERO
                val m = Math.round((d / Math.PI) - 0.5)
                if (Math.abs(d - (m + 0.5) * Math.PI) < 1e-12) {
                    return if (m % 2L == 0L) BigDecimal.ONE else BigDecimal("-1")
                }
                BigDecimal.valueOf(kotlin.math.sin(d))
            }
        }
    }

    fun cos(angle: BigDecimal, unit: AngleUnit): BigDecimal {
        return when (unit) {
            AngleUnit.DEGREES -> {
                val norm = normalizeAngle(angle, BigDecimal(360))
                when (norm.stripTrailingZeros().toPlainString()) {
                    "0", "360" -> BigDecimal.ONE
                    "180" -> BigDecimal("-1")
                    "90", "270" -> BigDecimal.ZERO
                    "60", "300" -> BigDecimal("0.5")
                    "120", "240" -> BigDecimal("-0.5")
                    else -> BigDecimal.valueOf(kotlin.math.cos(norm.toDouble() * Math.PI / 180.0))
                }
            }
            AngleUnit.GRADIANS -> {
                val norm = normalizeAngle(angle, BigDecimal(400))
                when (norm.stripTrailingZeros().toPlainString()) {
                    "0", "400" -> BigDecimal.ONE
                    "200" -> BigDecimal("-1")
                    "100", "300" -> BigDecimal.ZERO
                    else -> BigDecimal.valueOf(kotlin.math.cos(norm.toDouble() * Math.PI / 200.0))
                }
            }
            AngleUnit.RADIANS -> {
                val d = angle.toDouble()
                if (d == 0.0) return BigDecimal.ONE
                val m = Math.round((d / Math.PI) - 0.5)
                if (Math.abs(d - (m + 0.5) * Math.PI) < 1e-12) return BigDecimal.ZERO
                val k = Math.round(d / Math.PI)
                if (Math.abs(d - k * Math.PI) < 1e-12) {
                    return if (k % 2L == 0L) BigDecimal.ONE else BigDecimal("-1")
                }
                BigDecimal.valueOf(kotlin.math.cos(d))
            }
        }
    }

    fun tan(angle: BigDecimal, unit: AngleUnit): BigDecimal {
        return when (unit) {
            AngleUnit.DEGREES -> {
                val norm = normalizeAngle(angle, BigDecimal(360))
                val str = norm.stripTrailingZeros().toPlainString()
                if (str == "90" || str == "270") {
                    throw CalculatorException.DomainError("Domain Error: Tangent undefined at asymptote")
                }
                when (str) {
                    "0", "180", "360" -> BigDecimal.ZERO
                    "45", "225" -> BigDecimal.ONE
                    "135", "315" -> BigDecimal("-1")
                    else -> BigDecimal.valueOf(kotlin.math.tan(norm.toDouble() * Math.PI / 180.0))
                }
            }
            AngleUnit.GRADIANS -> {
                val norm = normalizeAngle(angle, BigDecimal(400))
                val str = norm.stripTrailingZeros().toPlainString()
                if (str == "100" || str == "300") {
                    throw CalculatorException.DomainError("Domain Error: Tangent undefined at asymptote")
                }
                when (str) {
                    "0", "200", "400" -> BigDecimal.ZERO
                    "50", "250" -> BigDecimal.ONE
                    "150", "350" -> BigDecimal("-1")
                    else -> BigDecimal.valueOf(kotlin.math.tan(norm.toDouble() * Math.PI / 200.0))
                }
            }
            AngleUnit.RADIANS -> {
                val d = angle.toDouble()
                if (d == 0.0) return BigDecimal.ZERO
                val m = Math.round((d / Math.PI) - 0.5)
                if (Math.abs(d - (m + 0.5) * Math.PI) < 1e-12) {
                    throw CalculatorException.DomainError("Domain Error: Tangent undefined at asymptote")
                }
                val k = Math.round(d / Math.PI)
                if (Math.abs(d - k * Math.PI) < 1e-12) return BigDecimal.ZERO
                BigDecimal.valueOf(kotlin.math.tan(d))
            }
        }
    }

    fun asin(value: BigDecimal, unit: AngleUnit): BigDecimal {
        if (value < BigDecimal("-1") || value > BigDecimal.ONE) {
            throw CalculatorException.DomainError("Domain Error: Inverse sine argument must be in [-1, 1]")
        }
        val str = value.stripTrailingZeros().toPlainString()
        return when (str) {
            "0" -> BigDecimal.ZERO
            "1" -> when (unit) {
                AngleUnit.DEGREES -> BigDecimal("90")
                AngleUnit.GRADIANS -> BigDecimal("100")
                AngleUnit.RADIANS -> PI_DECIMAL.divide(BigDecimal(2), MathContext.DECIMAL128)
            }
            "-1" -> when (unit) {
                AngleUnit.DEGREES -> BigDecimal("-90")
                AngleUnit.GRADIANS -> BigDecimal("-100")
                AngleUnit.RADIANS -> (-PI_DECIMAL).divide(BigDecimal(2), MathContext.DECIMAL128)
            }
            "0.5" -> when (unit) {
                AngleUnit.DEGREES -> BigDecimal("30")
                AngleUnit.GRADIANS -> BigDecimal("100").divide(BigDecimal(3), MathContext.DECIMAL128)
                AngleUnit.RADIANS -> PI_DECIMAL.divide(BigDecimal(6), MathContext.DECIMAL128)
            }
            "-0.5" -> when (unit) {
                AngleUnit.DEGREES -> BigDecimal("-30")
                AngleUnit.GRADIANS -> BigDecimal("-100").divide(BigDecimal(3), MathContext.DECIMAL128)
                AngleUnit.RADIANS -> (-PI_DECIMAL).divide(BigDecimal(6), MathContext.DECIMAL128)
            }
            else -> {
                val rad = kotlin.math.asin(value.toDouble())
                radToUnit(rad, unit)
            }
        }
    }

    fun acos(value: BigDecimal, unit: AngleUnit): BigDecimal {
        if (value < BigDecimal("-1") || value > BigDecimal.ONE) {
            throw CalculatorException.DomainError("Domain Error: Inverse cosine argument must be in [-1, 1]")
        }
        val str = value.stripTrailingZeros().toPlainString()
        return when (str) {
            "1" -> BigDecimal.ZERO
            "0" -> when (unit) {
                AngleUnit.DEGREES -> BigDecimal("90")
                AngleUnit.GRADIANS -> BigDecimal("100")
                AngleUnit.RADIANS -> PI_DECIMAL.divide(BigDecimal(2), MathContext.DECIMAL128)
            }
            "-1" -> when (unit) {
                AngleUnit.DEGREES -> BigDecimal("180")
                AngleUnit.GRADIANS -> BigDecimal("200")
                AngleUnit.RADIANS -> PI_DECIMAL
            }
            "0.5" -> when (unit) {
                AngleUnit.DEGREES -> BigDecimal("60")
                AngleUnit.GRADIANS -> BigDecimal("200").divide(BigDecimal(3), MathContext.DECIMAL128)
                AngleUnit.RADIANS -> PI_DECIMAL.divide(BigDecimal(3), MathContext.DECIMAL128)
            }
            "-0.5" -> when (unit) {
                AngleUnit.DEGREES -> BigDecimal("120")
                AngleUnit.GRADIANS -> BigDecimal("400").divide(BigDecimal(3), MathContext.DECIMAL128)
                AngleUnit.RADIANS -> PI_DECIMAL.multiply(BigDecimal(2)).divide(BigDecimal(3), MathContext.DECIMAL128)
            }
            else -> {
                val rad = kotlin.math.acos(value.toDouble())
                radToUnit(rad, unit)
            }
        }
    }

    fun atan(value: BigDecimal, unit: AngleUnit): BigDecimal {
        val str = value.stripTrailingZeros().toPlainString()
        return when (str) {
            "0" -> BigDecimal.ZERO
            "1" -> when (unit) {
                AngleUnit.DEGREES -> BigDecimal("45")
                AngleUnit.GRADIANS -> BigDecimal("50")
                AngleUnit.RADIANS -> PI_DECIMAL.divide(BigDecimal(4), MathContext.DECIMAL128)
            }
            "-1" -> when (unit) {
                AngleUnit.DEGREES -> BigDecimal("-45")
                AngleUnit.GRADIANS -> BigDecimal("-50")
                AngleUnit.RADIANS -> (-PI_DECIMAL).divide(BigDecimal(4), MathContext.DECIMAL128)
            }
            else -> {
                val rad = kotlin.math.atan(value.toDouble())
                radToUnit(rad, unit)
            }
        }
    }

    private fun normalizeAngle(angle: BigDecimal, period: BigDecimal): BigDecimal {
        val rem = angle.remainder(period)
        return if (rem < BigDecimal.ZERO) rem + period else rem
    }

    private fun radToUnit(rad: Double, unit: AngleUnit): BigDecimal {
        val converted = when (unit) {
            AngleUnit.DEGREES -> rad * 180.0 / Math.PI
            AngleUnit.GRADIANS -> rad * 200.0 / Math.PI
            AngleUnit.RADIANS -> rad
        }
        val r = Math.round(converted)
        if (Math.abs(converted - r) < 1e-12) {
            return BigDecimal.valueOf(r)
        }
        return BigDecimal.valueOf(converted)
    }
}
