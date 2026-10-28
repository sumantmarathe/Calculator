package com.calculator.engine

import com.calculator.engine.programmer.BitwiseOp
import com.calculator.engine.programmer.NumberBase
import com.calculator.engine.programmer.ProgrammerEngineImpl
import com.calculator.engine.programmer.ShiftType
import com.calculator.engine.programmer.WordSize
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class ProgrammerEngineTest {

    private lateinit var engine: ProgrammerEngineImpl

    @Before
    fun setUp() {
        engine = ProgrammerEngineImpl()
    }

    @Test
    fun testMultiBaseConversion_hexDecOctBinSynchronized() {
        engine.setValue(255uL)
        val reps = engine.formatAllBases()
        assertThat(reps.hex).isEqualTo("FF")
        assertThat(reps.decUnsigned).isEqualTo("255")
        assertThat(reps.decSigned).isEqualTo("255")
        assertThat(reps.oct).isEqualTo("377")
        assertThat(reps.bin).isEqualTo("11111111")
    }

    @Test
    fun testWordSizeMasking_byteWordDwordQword() {
        engine.setValue(0x123456789ABCDEF0uL)
        assertThat(engine.activeWordSize).isEqualTo(WordSize.QWORD)
        assertThat(engine.formatAllBases().hex).isEqualTo("123456789ABCDEF0")

        engine.setWordSize(WordSize.DWORD)
        assertThat(engine.activeValue).isEqualTo(0x9ABCDEF0uL)
        assertThat(engine.formatAllBases().hex).isEqualTo("9ABCDEF0")

        engine.setWordSize(WordSize.WORD)
        assertThat(engine.activeValue).isEqualTo(0xDEF0uL)
        assertThat(engine.formatAllBases().hex).isEqualTo("DEF0")

        engine.setWordSize(WordSize.BYTE)
        assertThat(engine.activeValue).isEqualTo(0xF0uL)
        assertThat(engine.formatAllBases().hex).isEqualTo("F0")
    }

    @Test
    fun testTwosComplementSignedRepresentation_allWordSizes() {
        // BYTE: 0xFF = -1 signed, 255 unsigned; 0x80 = -128 signed; 0x7F = 127 signed
        engine.setWordSize(WordSize.BYTE)
        engine.setValue(0xFFuL)
        assertThat(engine.formatAllBases().decSigned).isEqualTo("-1")
        assertThat(engine.formatAllBases().decUnsigned).isEqualTo("255")

        engine.setValue(0x80uL)
        assertThat(engine.formatAllBases().decSigned).isEqualTo("-128")
        assertThat(engine.formatAllBases().decUnsigned).isEqualTo("128")

        engine.setValue(0x7FuL)
        assertThat(engine.formatAllBases().decSigned).isEqualTo("127")

        // WORD: 0xFFFF = -1 signed, 0x8000 = -32768 signed
        engine.setWordSize(WordSize.WORD)
        engine.setValue(0xFFFFuL)
        assertThat(engine.formatAllBases().decSigned).isEqualTo("-1")
        assertThat(engine.formatAllBases().decUnsigned).isEqualTo("65535")
        engine.setValue(0x8000uL)
        assertThat(engine.formatAllBases().decSigned).isEqualTo("-32768")

        // DWORD: 0xFFFFFFFF = -1 signed, 0x80000000 = -2147483648 signed
        engine.setWordSize(WordSize.DWORD)
        engine.setValue(0xFFFFFFFFuL)
        assertThat(engine.formatAllBases().decSigned).isEqualTo("-1")
        assertThat(engine.formatAllBases().decUnsigned).isEqualTo("4294967295")
        engine.setValue(0x80000000uL)
        assertThat(engine.formatAllBases().decSigned).isEqualTo("-2147483648")

        // QWORD: 0xFFFFFFFFFFFFFFFF = -1 signed
        engine.setWordSize(WordSize.QWORD)
        engine.setValue(0xFFFFFFFFFFFFFFFFuL)
        assertThat(engine.formatAllBases().decSigned).isEqualTo("-1")
        assertThat(engine.formatAllBases().decUnsigned).isEqualTo("18446744073709551615")
    }

    @Test
    fun testBitwiseOperations_andOrXorNotNandNor() {
        engine.setWordSize(WordSize.BYTE)

        engine.setValue(0b11001010uL)
        assertThat(engine.executeBitwise(BitwiseOp.AND, 0b10101100uL)).isEqualTo(0b10001000uL)

        engine.setValue(0b11001010uL)
        assertThat(engine.executeBitwise(BitwiseOp.OR, 0b10101100uL)).isEqualTo(0b11101110uL)

        engine.setValue(0b11001010uL)
        assertThat(engine.executeBitwise(BitwiseOp.XOR, 0b10101100uL)).isEqualTo(0b01100110uL)

        engine.setValue(0b11001010uL)
        assertThat(engine.executeBitwise(BitwiseOp.NOT)).isEqualTo(0b00110101uL)

        engine.setValue(0b11001010uL)
        assertThat(engine.executeBitwise(BitwiseOp.NAND, 0b10101100uL)).isEqualTo(0b01110111uL)

        engine.setValue(0b11001010uL)
        assertThat(engine.executeBitwise(BitwiseOp.NOR, 0b10101100uL)).isEqualTo(0b00010001uL)
    }

    @Test
    fun testBitShiftsAndRotations_lshRshArshRolRor() {
        engine.setWordSize(WordSize.BYTE)
        engine.setValue(0b10010110uL)

        // LSH by 1 -> 0b00101100 (0x2C)
        assertThat(engine.shift(ShiftType.LSH, 1)).isEqualTo(0b00101100uL)

        // RSH by 2 -> 0b00001011 (0x0B)
        assertThat(engine.shift(ShiftType.RSH, 2)).isEqualTo(0b00001011uL)

        // ROL on 0b10000001 by 1 in BYTE -> 0b00000011 (3)
        engine.setValue(0b10000001uL)
        assertThat(engine.shift(ShiftType.ROL, 1)).isEqualTo(0b00000011uL)

        // ROR on 0b00000011 by 1 in BYTE -> 0b10000001 (129)
        assertThat(engine.shift(ShiftType.ROR, 1)).isEqualTo(0b10000001uL)

        // ARSH on negative signed BYTE 0b10000000 (-128) by 2 -> 0b11100000 (-32)
        engine.setValue(0b10000000uL)
        assertThat(engine.shift(ShiftType.ARSH, 2)).isEqualTo(0b11100000uL)
        assertThat(engine.formatAllBases().decSigned).isEqualTo("-32")
    }

    @Test
    fun testInteractiveBitToggling_withinAndOutsideWordSize() {
        engine.setWordSize(WordSize.BYTE)
        engine.setValue(0uL)

        // Toggle bit 0 -> 1
        assertThat(engine.toggleBit(0)).isEqualTo(1uL)
        assertThat(engine.getBit(0)).isTrue()

        // Toggle bit 7 -> 129 (0x81)
        assertThat(engine.toggleBit(7)).isEqualTo(129uL)
        assertThat(engine.getBit(7)).isTrue()

        // Attempt to toggle bit 8 (outside BYTE) -> ignored, remains 129
        assertThat(engine.toggleBit(8)).isEqualTo(129uL)
        assertThat(engine.getBit(8)).isFalse()

        // Toggle bit 0 again -> 128 (0x80)
        assertThat(engine.toggleBit(0)).isEqualTo(128uL)
        assertThat(engine.getBit(0)).isFalse()
    }

    @Test
    fun testOverflowWraparound_acrossAllWordSizes() {
        // BYTE: 127 + 1 = -128 (signed), 255 + 1 = 0
        engine.setWordSize(WordSize.BYTE)
        engine.setValue(127uL)
        engine.executeBitwise(BitwiseOp.ADD, 1uL)
        assertThat(engine.activeValue).isEqualTo(128uL)
        assertThat(engine.formatAllBases().decSigned).isEqualTo("-128")

        engine.setValue(255uL)
        engine.executeBitwise(BitwiseOp.ADD, 1uL)
        assertThat(engine.activeValue).isEqualTo(0uL)

        // Underflow 0 - 1 = 255 (-1 signed)
        engine.executeBitwise(BitwiseOp.SUB, 1uL)
        assertThat(engine.activeValue).isEqualTo(255uL)
        assertThat(engine.formatAllBases().decSigned).isEqualTo("-1")
    }

    @Test
    fun testBaseInputValidation_rejectsInvalidDigitsForActiveBase() {
        engine.setWordSize(WordSize.DWORD)
        engine.clear()

        // BIN mode only accepts 0 and 1
        engine.setBase(NumberBase.BIN)
        assertThat(engine.appendDigit('1')).isTrue()
        assertThat(engine.appendDigit('0')).isTrue()
        assertThat(engine.appendDigit('2')).isFalse()
        assertThat(engine.appendDigit('A')).isFalse()
        assertThat(engine.activeValue).isEqualTo(2uL)

        // OCT mode accepts 0..7
        engine.clear()
        engine.setBase(NumberBase.OCT)
        assertThat(engine.appendDigit('7')).isTrue()
        assertThat(engine.appendDigit('8')).isFalse()
        assertThat(engine.activeValue).isEqualTo(7uL)

        // HEX mode accepts 0..9 and A..F
        engine.clear()
        engine.setBase(NumberBase.HEX)
        assertThat(engine.appendDigit('F')).isTrue()
        assertThat(engine.appendDigit('A')).isTrue()
        assertThat(engine.appendDigit('G')).isFalse()
        assertThat(engine.activeValue).isEqualTo(0xFAuL)
    }

    @Test
    fun testExpressionEvaluationInProgrammerMode() {
        engine.setWordSize(WordSize.BYTE)
        engine.setBase(NumberBase.HEX)
        val res = engine.evaluateExpression("0F OR F0")
        assertThat(res).isEqualTo(0xFFuL)

        val res2 = engine.evaluateExpression("(FF AND 0F) LSH 4")
        assertThat(res2).isEqualTo(0xF0uL)
    }
}
