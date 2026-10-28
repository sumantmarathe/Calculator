package com.calculator.engine.programmer

enum class NumberBase(val radix: Int, val label: String) {
    HEX(16, "HEX"),
    DEC(10, "DEC"),
    OCT(8, "OCT"),
    BIN(2, "BIN")
}

enum class WordSize(val bits: Int, val mask: ULong, val label: String) {
    QWORD(64, 0xFFFFFFFFFFFFFFFFuL, "QWORD"),
    DWORD(32, 0xFFFFFFFFuL, "DWORD"),
    WORD(16, 0xFFFFuL, "WORD"),
    BYTE(8, 0xFFuL, "BYTE");

    fun maskValue(value: ULong): ULong = value and mask

    fun signBitMask(): ULong = 1uL shl (bits - 1)

    fun isNegativeSigned(value: ULong): Boolean {
        val masked = maskValue(value)
        return (masked and signBitMask()) != 0uL
    }

    fun toSignedLong(value: ULong): Long {
        val masked = maskValue(value)
        return when (this) {
            QWORD -> masked.toLong()
            DWORD -> masked.toUInt().toInt().toLong()
            WORD -> masked.toUShort().toShort().toLong()
            BYTE -> masked.toUByte().toByte().toLong()
        }
    }

    fun fromSignedLong(value: Long): ULong {
        return maskValue(value.toULong())
    }
}

enum class BitwiseOp(val symbol: String) {
    AND("AND"),
    OR("OR"),
    XOR("XOR"),
    NAND("NAND"),
    NOR("NOR"),
    NOT("NOT"),
    ADD("+"),
    SUB("-"),
    MUL("×"),
    DIV("÷"),
    MOD("MOD")
}

enum class ShiftType(val symbol: String) {
    LSH("LSH"),
    RSH("RSH"),
    ARSH("ARSH"),
    ROL("ROL"),
    ROR("ROR")
}

data class BaseRepresentations(
    val hex: String,
    val decSigned: String,
    val decUnsigned: String,
    val oct: String,
    val bin: String,
    val groupedBin: String,
    val bits: List<Boolean>
)

interface ProgrammerEngine {
    val activeValue: ULong
    val activeWordSize: WordSize
    val activeBase: NumberBase
    val isSignedDisplay: Boolean

    fun setValue(value: ULong)
    fun setWordSize(wordSize: WordSize)
    fun setBase(base: NumberBase)
    fun setSignedDisplay(signed: Boolean)
    fun clear()
    fun appendDigit(digitChar: Char): Boolean
    fun backspace()
    fun negateTwosComplement()
    fun formatAllBases(): BaseRepresentations
    fun formatActiveDisplay(): String
    fun executeBitwise(op: BitwiseOp, operand: ULong? = null): ULong
    fun shift(type: ShiftType, bits: Int = 1): ULong
    fun toggleBit(bitIndex: Int): ULong
    fun getBit(bitIndex: Int): Boolean
    fun evaluateExpression(expression: String): ULong
}
