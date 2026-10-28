package com.calculator.engine.programmer

import com.calculator.engine.CalculatorException

class ProgrammerEngineImpl(
    initialWordSize: WordSize = WordSize.QWORD,
    initialBase: NumberBase = NumberBase.DEC,
    initialSigned: Boolean = true
) : ProgrammerEngine {

    private var _wordSize: WordSize = initialWordSize
    private var _base: NumberBase = initialBase
    private var _value: ULong = 0uL
    private var _isSignedDisplay: Boolean = initialSigned

    override val activeValue: ULong
        get() = _wordSize.maskValue(_value)

    override val activeWordSize: WordSize
        get() = _wordSize

    override val activeBase: NumberBase
        get() = _base

    override val isSignedDisplay: Boolean
        get() = _isSignedDisplay

    override fun setValue(value: ULong) {
        _value = _wordSize.maskValue(value)
    }

    override fun setWordSize(wordSize: WordSize) {
        _wordSize = wordSize
        _value = _wordSize.maskValue(_value)
    }

    override fun setBase(base: NumberBase) {
        _base = base
    }

    override fun setSignedDisplay(signed: Boolean) {
        _isSignedDisplay = signed
    }

    override fun clear() {
        _value = 0uL
    }

    override fun appendDigit(digitChar: Char): Boolean {
        val upper = digitChar.uppercaseChar()
        val digitVal = when (upper) {
            in '0'..'9' -> (upper - '0').toULong()
            in 'A'..'F' -> (upper - 'A' + 10).toULong()
            else -> return false
        }
        if (digitVal >= _base.radix.toULong()) {
            return false
        }

        val radixU = _base.radix.toULong()
        if (_base == NumberBase.DEC && _isSignedDisplay) {
            val currentSigned = _wordSize.toSignedLong(activeValue)
            val nextSigned = if (currentSigned < 0) {
                currentSigned * 10L - digitVal.toLong()
            } else {
                currentSigned * 10L + digitVal.toLong()
            }
            val maxSigned = when (_wordSize) {
                WordSize.QWORD -> Long.MAX_VALUE
                WordSize.DWORD -> Int.MAX_VALUE.toLong()
                WordSize.WORD -> Short.MAX_VALUE.toLong()
                WordSize.BYTE -> Byte.MAX_VALUE.toLong()
            }
            val minSigned = when (_wordSize) {
                WordSize.QWORD -> Long.MIN_VALUE
                WordSize.DWORD -> Int.MIN_VALUE.toLong()
                WordSize.WORD -> Short.MIN_VALUE.toLong()
                WordSize.BYTE -> Byte.MIN_VALUE.toLong()
            }
            // Check overflow for QWORD multiplication
            if (_wordSize == WordSize.QWORD) {
                if (currentSigned > Long.MAX_VALUE / 10L || currentSigned < Long.MIN_VALUE / 10L) return false
            }
            if (nextSigned > maxSigned || nextSigned < minSigned) return false
            _value = _wordSize.fromSignedLong(nextSigned)
            return true
        } else {
            val current = activeValue
            // Prevent unsigned overflow beyond wordSize mask
            val maxUnsigned = _wordSize.mask
            if (current > maxUnsigned / radixU) return false
            val candidate = current * radixU + digitVal
            if (candidate < current || (candidate and _wordSize.mask) != candidate) return false
            _value = _wordSize.maskValue(candidate)
            return true
        }
    }

    override fun backspace() {
        val radixU = _base.radix.toULong()
        if (_base == NumberBase.DEC && _isSignedDisplay) {
            val currentSigned = _wordSize.toSignedLong(activeValue)
            val nextSigned = currentSigned / 10L
            _value = _wordSize.fromSignedLong(nextSigned)
        } else {
            _value = _wordSize.maskValue(activeValue / radixU)
        }
    }

    override fun negateTwosComplement() {
        val inverted = activeValue.inv()
        _value = _wordSize.maskValue(inverted + 1uL)
    }

    override fun formatAllBases(): BaseRepresentations {
        val v = activeValue
        val hexStr = v.toString(16).uppercase()
        val decUnsignedStr = v.toString(10)
        val decSignedStr = _wordSize.toSignedLong(v).toString()
        val octStr = v.toString(8)
        val binRaw = v.toString(2)
        val paddedBin = binRaw.padStart(_wordSize.bits, '0')
        val groupedBin = paddedBin.chunked(4).joinToString(" ")
        val bitsList = (0 until 64).map { bitIdx -> getBit(bitIdx) }

        return BaseRepresentations(
            hex = hexStr,
            decSigned = decSignedStr,
            decUnsigned = decUnsignedStr,
            oct = octStr,
            bin = binRaw,
            groupedBin = groupedBin,
            bits = bitsList
        )
    }

    override fun formatActiveDisplay(): String {
        val reps = formatAllBases()
        return when (_base) {
            NumberBase.HEX -> reps.hex
            NumberBase.DEC -> if (_isSignedDisplay) reps.decSigned else reps.decUnsigned
            NumberBase.OCT -> reps.oct
            NumberBase.BIN -> reps.bin
        }
    }

    override fun executeBitwise(op: BitwiseOp, operand: ULong?): ULong {
        val a = activeValue
        val b = operand?.let { _wordSize.maskValue(it) } ?: 0uL

        val rawResult: ULong = when (op) {
            BitwiseOp.NOT -> a.inv()
            BitwiseOp.AND -> a and b
            BitwiseOp.OR -> a or b
            BitwiseOp.XOR -> a xor b
            BitwiseOp.NAND -> (a and b).inv()
            BitwiseOp.NOR -> (a or b).inv()
            BitwiseOp.ADD -> a + b
            BitwiseOp.SUB -> a - b
            BitwiseOp.MUL -> a * b
            BitwiseOp.DIV -> {
                if (b == 0uL) throw CalculatorException.DivisionByZero("Cannot divide by zero")
                if (_isSignedDisplay) {
                    val sa = _wordSize.toSignedLong(a)
                    val sb = _wordSize.toSignedLong(b)
                    (sa / sb).toULong()
                } else {
                    a / b
                }
            }
            BitwiseOp.MOD -> {
                if (b == 0uL) throw CalculatorException.DivisionByZero("Cannot divide by zero")
                if (_isSignedDisplay) {
                    val sa = _wordSize.toSignedLong(a)
                    val sb = _wordSize.toSignedLong(b)
                    (sa % sb).toULong()
                } else {
                    a % b
                }
            }
        }

        _value = _wordSize.maskValue(rawResult)
        return _value
    }

    override fun shift(type: ShiftType, bits: Int): ULong {
        val w = _wordSize.bits
        val v = activeValue
        if (bits == 0) return v
        if (bits < 0) throw CalculatorException.DomainError("Shift count cannot be negative")

        val result: ULong = when (type) {
            ShiftType.LSH -> {
                if (bits >= w) 0uL else _wordSize.maskValue(v shl bits)
            }
            ShiftType.RSH -> {
                if (bits >= w) 0uL else _wordSize.maskValue(v shr bits)
            }
            ShiftType.ARSH -> {
                val signed = _wordSize.toSignedLong(v)
                val clamped = if (bits >= w) w - 1 else bits
                val shifted = signed shr clamped
                _wordSize.maskValue(shifted.toULong())
            }
            ShiftType.ROL -> {
                val norm = ((bits % w) + w) % w
                if (norm == 0) v
                else {
                    val left = (v shl norm) and _wordSize.mask
                    val right = v shr (w - norm)
                    _wordSize.maskValue(left or right)
                }
            }
            ShiftType.ROR -> {
                val norm = ((bits % w) + w) % w
                if (norm == 0) v
                else {
                    val right = v shr norm
                    val left = (v shl (w - norm)) and _wordSize.mask
                    _wordSize.maskValue(right or left)
                }
            }
        }

        _value = result
        return _value
    }

    override fun toggleBit(bitIndex: Int): ULong {
        if (bitIndex < 0 || bitIndex >= _wordSize.bits) {
            return activeValue
        }
        val bitMask = 1uL shl bitIndex
        _value = _wordSize.maskValue(activeValue xor bitMask)
        return _value
    }

    override fun getBit(bitIndex: Int): Boolean {
        if (bitIndex < 0 || bitIndex >= _wordSize.bits) return false
        return ((activeValue shr bitIndex) and 1uL) == 1uL
    }

    override fun evaluateExpression(expression: String): ULong {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return activeValue

        val tokens = tokenizeProgrammer(trimmed)
        val rpn = parseProgrammerRpn(tokens)
        val res = evalProgrammerRpn(rpn)
        _value = _wordSize.maskValue(res)
        return _value
    }

    private sealed interface ProgToken {
        data class Value(val v: ULong) : ProgToken
        data class Op(val name: String, val precedence: Int, val isUnary: Boolean = false) : ProgToken
        data object LParen : ProgToken
        data object RParen : ProgToken
    }

    private fun tokenizeProgrammer(expr: String): List<ProgToken> {
        val tokens = mutableListOf<ProgToken>()
        var i = 0
        val s = expr.trim()
        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> i++
                c == '(' -> { tokens.add(ProgToken.LParen); i++ }
                c == ')' -> { tokens.add(ProgToken.RParen); i++ }
                c == '~' -> { tokens.add(ProgToken.Op("NOT", 6, isUnary = true)); i++ }
                c == '+' -> { tokens.add(ProgToken.Op("+", 4)); i++ }
                c == '-' || c == '−' -> {
                    val isUnary = tokens.isEmpty() || tokens.last() is ProgToken.LParen || tokens.last() is ProgToken.Op
                    if (isUnary) {
                        tokens.add(ProgToken.Op("NEG", 6, isUnary = true))
                    } else {
                        tokens.add(ProgToken.Op("-", 4))
                    }
                    i++
                }
                c == '*' || c == '×' -> { tokens.add(ProgToken.Op("*", 5)); i++ }
                c == '/' || c == '÷' -> { tokens.add(ProgToken.Op("/", 5)); i++ }
                c == '%' -> { tokens.add(ProgToken.Op("MOD", 5)); i++ }
                c == '&' -> { tokens.add(ProgToken.Op("AND", 2)); i++ }
                c == '|' -> { tokens.add(ProgToken.Op("OR", 0)); i++ }
                c == '^' -> { tokens.add(ProgToken.Op("XOR", 1)); i++ }
                c == '<' && i + 1 < s.length && s[i + 1] == '<' -> {
                    tokens.add(ProgToken.Op("LSH", 3)); i += 2
                }
                c == '>' && i + 1 < s.length && s[i + 1] == '>' -> {
                    tokens.add(ProgToken.Op("RSH", 3)); i += 2
                }
                c.isLetterOrDigit() -> {
                    val start = i
                    while (i < s.length && s[i].isLetterOrDigit()) i++
                    val word = s.substring(start, i).uppercase()
                    when (word) {
                        "NOT" -> tokens.add(ProgToken.Op("NOT", 6, isUnary = true))
                        "AND" -> tokens.add(ProgToken.Op("AND", 2))
                        "NAND" -> tokens.add(ProgToken.Op("NAND", 2))
                        "XOR" -> tokens.add(ProgToken.Op("XOR", 1))
                        "OR" -> tokens.add(ProgToken.Op("OR", 0))
                        "NOR" -> tokens.add(ProgToken.Op("NOR", 0))
                        "LSH" -> tokens.add(ProgToken.Op("LSH", 3))
                        "RSH" -> tokens.add(ProgToken.Op("RSH", 3))
                        "ROL" -> tokens.add(ProgToken.Op("ROL", 3))
                        "ROR" -> tokens.add(ProgToken.Op("ROR", 3))
                        "MOD" -> tokens.add(ProgToken.Op("MOD", 5))
                        else -> {
                            val parsed = parseLiteral(word)
                            tokens.add(ProgToken.Value(parsed))
                        }
                    }
                }
                else -> throw CalculatorException.SyntaxError("Unexpected token '$c'")
            }
        }
        return tokens
    }

    private fun parseLiteral(word: String): ULong {
        return try {
            when {
                word.startsWith("0X") -> word.substring(2).toULong(16)
                word.startsWith("0B") -> word.substring(2).toULong(2)
                word.startsWith("0O") -> word.substring(2).toULong(8)
                else -> word.toULong(_base.radix)
            }
        } catch (e: NumberFormatException) {
            throw CalculatorException.SyntaxError("Invalid literal '$word' for base ${_base.label}")
        }
    }

    private fun parseProgrammerRpn(tokens: List<ProgToken>): List<ProgToken> {
        val out = mutableListOf<ProgToken>()
        val stack = ArrayDeque<ProgToken>()
        for (t in tokens) {
            when (t) {
                is ProgToken.Value -> out.add(t)
                is ProgToken.LParen -> stack.addLast(t)
                is ProgToken.RParen -> {
                    while (stack.isNotEmpty() && stack.last() !is ProgToken.LParen) {
                        out.add(stack.removeLast())
                    }
                    if (stack.isNotEmpty() && stack.last() is ProgToken.LParen) {
                        stack.removeLast()
                    }
                }
                is ProgToken.Op -> {
                    if (t.isUnary) {
                        stack.addLast(t)
                    } else {
                        while (stack.isNotEmpty()) {
                            val top = stack.last()
                            if (top is ProgToken.Op && top.precedence >= t.precedence) {
                                out.add(stack.removeLast())
                            } else break
                        }
                        stack.addLast(t)
                    }
                }
            }
        }
        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top !is ProgToken.LParen) out.add(top)
        }
        return out
    }

    private fun evalProgrammerRpn(rpn: List<ProgToken>): ULong {
        if (rpn.isEmpty()) return 0uL
        val stack = ArrayDeque<ULong>()
        for (t in rpn) {
            when (t) {
                is ProgToken.Value -> stack.addLast(_wordSize.maskValue(t.v))
                is ProgToken.Op -> {
                    if (t.isUnary) {
                        val a = if (stack.isNotEmpty()) stack.removeLast() else 0uL
                        val res = when (t.name) {
                            "NOT" -> _wordSize.maskValue(a.inv())
                            "NEG" -> _wordSize.maskValue(a.inv() + 1uL)
                            else -> a
                        }
                        stack.addLast(res)
                    } else {
                        if (stack.size < 2) throw CalculatorException.SyntaxError("Missing operand for ${t.name}")
                        val b = stack.removeLast()
                        val a = stack.removeLast()
                        val saved = _value
                        _value = a
                        val res = when (t.name) {
                            "+" -> executeBitwise(BitwiseOp.ADD, b)
                            "-" -> executeBitwise(BitwiseOp.SUB, b)
                            "*" -> executeBitwise(BitwiseOp.MUL, b)
                            "/" -> executeBitwise(BitwiseOp.DIV, b)
                            "MOD" -> executeBitwise(BitwiseOp.MOD, b)
                            "AND" -> executeBitwise(BitwiseOp.AND, b)
                            "OR" -> executeBitwise(BitwiseOp.OR, b)
                            "XOR" -> executeBitwise(BitwiseOp.XOR, b)
                            "NAND" -> executeBitwise(BitwiseOp.NAND, b)
                            "NOR" -> executeBitwise(BitwiseOp.NOR, b)
                            "LSH" -> shift(ShiftType.LSH, b.toInt())
                            "RSH" -> shift(ShiftType.RSH, b.toInt())
                            "ROL" -> shift(ShiftType.ROL, b.toInt())
                            "ROR" -> shift(ShiftType.ROR, b.toInt())
                            else -> a
                        }
                        _value = saved
                        stack.addLast(_wordSize.maskValue(res))
                    }
                }
                else -> {}
            }
        }
        return if (stack.isNotEmpty()) _wordSize.maskValue(stack.last()) else 0uL
    }
}
