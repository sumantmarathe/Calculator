package com.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.engine.programmer.BitwiseOp
import com.calculator.engine.programmer.NumberBase
import com.calculator.engine.programmer.ShiftType
import com.calculator.ui.CalculatorMode
import com.calculator.ui.CalculatorViewModel
import com.calculator.ui.theme.SkinTheme

enum class KeyRole {
    DIGIT,
    OPERATOR,
    ACTION,
    ACCENT,
    DISABLED
}

data class KeySpec(
    val label: String,
    val tag: String,
    val role: KeyRole = KeyRole.DIGIT,
    val weight: Float = 1f,
    val onClick: () -> Unit
)

@Composable
fun SkinKeyButton(
    spec: KeySpec,
    modifier: Modifier = Modifier
) {
    val colors = SkinTheme.colors
    val typography = SkinTheme.typography
    val shapes = SkinTheme.shapes

    val shape: Shape = if (shapes.isCutCorner) {
        CutCornerShape(topStart = shapes.buttonCornerRadius, bottomEnd = shapes.buttonCornerRadius)
    } else {
        RoundedCornerShape(shapes.buttonCornerRadius)
    }

    val bg = when (spec.role) {
        KeyRole.DIGIT -> colors.buttonNormal
        KeyRole.OPERATOR -> colors.buttonOperator
        KeyRole.ACTION -> colors.buttonAction
        KeyRole.ACCENT -> colors.buttonAccent
        KeyRole.DISABLED -> colors.surface.copy(alpha = 0.35f)
    }

    val fg = when (spec.role) {
        KeyRole.DIGIT -> colors.buttonNormalText
        KeyRole.OPERATOR -> colors.buttonOperatorText
        KeyRole.ACTION -> colors.buttonActionText
        KeyRole.ACCENT -> colors.buttonAccentText
        KeyRole.DISABLED -> colors.textSecondary.copy(alpha = 0.35f)
    }

    val enabled = spec.role != KeyRole.DISABLED

    Box(
        modifier = modifier
            .testTag(spec.tag)
            .clip(shape)
            .background(bg)
            .border(
                width = shapes.buttonBorderWidth,
                color = if (enabled) colors.border else colors.border.copy(alpha = 0.25f),
                shape = shape
            )
            .drawBehind {
                if (shapes.hasPixelShadow && enabled) {
                    val strokePx = 3.dp.toPx()
                    drawRect(
                        color = colors.border,
                        topLeft = Offset(0f, size.height - strokePx),
                        size = Size(size.width, strokePx)
                    )
                    drawRect(
                        color = colors.border,
                        topLeft = Offset(size.width - strokePx, 0f),
                        size = Size(strokePx, size.height)
                    )
                }
            }
            .then(if (enabled) Modifier.clickable { spec.onClick() } else Modifier)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = spec.label,
            style = typography.buttonText.copy(
                fontSize = if (spec.label.length > 3) 13.sp else typography.buttonText.fontSize
            ),
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
fun CalculatorKeypad(
    mode: CalculatorMode,
    viewModel: CalculatorViewModel,
    isSecondFunction: Boolean,
    programmerBase: NumberBase,
    showBitMatrix: Boolean,
    modifier: Modifier = Modifier
) {
    when (mode) {
        CalculatorMode.NORMAL -> NormalModeKeypad(viewModel = viewModel, modifier = modifier)
        CalculatorMode.SCIENTIFIC -> ScientificModeKeypad(
            viewModel = viewModel,
            isSecondFunction = isSecondFunction,
            modifier = modifier
        )
        CalculatorMode.PROGRAMMER -> ProgrammerModeKeypad(
            viewModel = viewModel,
            activeBase = programmerBase,
            showBitMatrix = showBitMatrix,
            modifier = modifier
        )
    }
}

@Composable
fun NormalModeKeypad(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf(
            KeySpec("MC", "btn_MC", KeyRole.ACTION) { viewModel.onMemoryClear() },
            KeySpec("MR", "btn_MR", KeyRole.ACTION) { viewModel.onMemoryRecall() },
            KeySpec("M-", "btn_M_minus", KeyRole.ACTION) { viewModel.onMemorySubtract() },
            KeySpec("M+", "btn_M_plus", KeyRole.ACTION) { viewModel.onMemoryAdd() }
        ),
        listOf(
            KeySpec("AC", "btn_AC", KeyRole.ACTION) { viewModel.onClear() },
            KeySpec("( )", "btn_parens", KeyRole.ACTION) { viewModel.onSmartParenthesis() },
            KeySpec("%", "btn_percent", KeyRole.OPERATOR) { viewModel.onPercentage() },
            KeySpec("÷", "btn_div", KeyRole.OPERATOR) { viewModel.onOperator("÷") }
        ),
        listOf(
            KeySpec("7", "btn_7") { viewModel.onDigit("7") },
            KeySpec("8", "btn_8") { viewModel.onDigit("8") },
            KeySpec("9", "btn_9") { viewModel.onDigit("9") },
            KeySpec("×", "btn_mul", KeyRole.OPERATOR) { viewModel.onOperator("×") }
        ),
        listOf(
            KeySpec("4", "btn_4") { viewModel.onDigit("4") },
            KeySpec("5", "btn_5") { viewModel.onDigit("5") },
            KeySpec("6", "btn_6") { viewModel.onDigit("6") },
            KeySpec("−", "btn_sub", KeyRole.OPERATOR) { viewModel.onOperator("−") }
        ),
        listOf(
            KeySpec("1", "btn_1") { viewModel.onDigit("1") },
            KeySpec("2", "btn_2") { viewModel.onDigit("2") },
            KeySpec("3", "btn_3") { viewModel.onDigit("3") },
            KeySpec("+", "btn_add", KeyRole.OPERATOR) { viewModel.onOperator("+") }
        ),
        listOf(
            KeySpec("±", "btn_negate", KeyRole.ACTION) { viewModel.onNegate() },
            KeySpec("0", "btn_0") { viewModel.onDigit("0") },
            KeySpec(".", "btn_dot") { viewModel.onDecimalPoint() },
            KeySpec("⌫", "btn_backspace", KeyRole.ACTION) { viewModel.onBackspace() },
            KeySpec("=", "btn_equals", KeyRole.ACCENT) { viewModel.onEquals() }
        )
    )

    KeypadGrid(rows = rows, modifier = modifier)
}

@Composable
fun ScientificModeKeypad(
    viewModel: CalculatorViewModel,
    isSecondFunction: Boolean,
    modifier: Modifier = Modifier
) {
    val sinKey = if (isSecondFunction) KeySpec("asin", "btn_asin", KeyRole.OPERATOR) { viewModel.onScientificFunction("asin") }
    else KeySpec("sin", "btn_sin", KeyRole.OPERATOR) { viewModel.onScientificFunction("sin") }

    val cosKey = if (isSecondFunction) KeySpec("acos", "btn_acos", KeyRole.OPERATOR) { viewModel.onScientificFunction("acos") }
    else KeySpec("cos", "btn_cos", KeyRole.OPERATOR) { viewModel.onScientificFunction("cos") }

    val tanKey = if (isSecondFunction) KeySpec("atan", "btn_atan", KeyRole.OPERATOR) { viewModel.onScientificFunction("atan") }
    else KeySpec("tan", "btn_tan", KeyRole.OPERATOR) { viewModel.onScientificFunction("tan") }

    val sinhKey = if (isSecondFunction) KeySpec("asinh", "btn_asinh", KeyRole.OPERATOR) { viewModel.onScientificFunction("asinh") }
    else KeySpec("sinh", "btn_sinh", KeyRole.OPERATOR) { viewModel.onScientificFunction("sinh") }

    val coshKey = if (isSecondFunction) KeySpec("acosh", "btn_acosh", KeyRole.OPERATOR) { viewModel.onScientificFunction("acosh") }
    else KeySpec("cosh", "btn_cosh", KeyRole.OPERATOR) { viewModel.onScientificFunction("cosh") }

    val tanhKey = if (isSecondFunction) KeySpec("atanh", "btn_atanh", KeyRole.OPERATOR) { viewModel.onScientificFunction("atanh") }
    else KeySpec("tanh", "btn_tanh", KeyRole.OPERATOR) { viewModel.onScientificFunction("tanh") }

    val rows = listOf(
        listOf(
            KeySpec("2nd", "btn_2nd", if (isSecondFunction) KeyRole.ACCENT else KeyRole.ACTION) { viewModel.toggleSecondFunction() },
            sinKey,
            cosKey,
            tanKey,
            KeySpec("π", "btn_pi", KeyRole.OPERATOR) { viewModel.onScientificFunction("π") }
        ),
        listOf(
            sinhKey,
            coshKey,
            tanhKey,
            KeySpec("ln", "btn_ln", KeyRole.OPERATOR) { viewModel.onScientificFunction("ln") },
            KeySpec("log", "btn_log10", KeyRole.OPERATOR) { viewModel.onScientificFunction("log10") }
        ),
        listOf(
            KeySpec("x²", "btn_sq", KeyRole.OPERATOR) { viewModel.onScientificFunction("x²") },
            KeySpec("xʸ", "btn_pow", KeyRole.OPERATOR) { viewModel.onScientificFunction("xʸ") },
            KeySpec("√", "btn_sqrt", KeyRole.OPERATOR) { viewModel.onScientificFunction("√") },
            KeySpec("∛", "btn_cbrt", KeyRole.OPERATOR) { viewModel.onScientificFunction("∛") },
            KeySpec("n!", "btn_fact", KeyRole.OPERATOR) { viewModel.onScientificFunction("n!") }
        ),
        listOf(
            KeySpec("e", "btn_e", KeyRole.OPERATOR) { viewModel.onScientificFunction("e") },
            KeySpec("(", "btn_lparen", KeyRole.ACTION) { viewModel.onParenthesis("(") },
            KeySpec(")", "btn_rparen", KeyRole.ACTION) { viewModel.onParenthesis(")") },
            KeySpec("mod", "btn_mod", KeyRole.OPERATOR) { viewModel.onScientificFunction("mod") },
            KeySpec("÷", "btn_div", KeyRole.OPERATOR) { viewModel.onOperator("÷") }
        ),
        listOf(
            KeySpec("1/x", "btn_recip", KeyRole.OPERATOR) { viewModel.onScientificFunction("1/x") },
            KeySpec("7", "btn_7") { viewModel.onDigit("7") },
            KeySpec("8", "btn_8") { viewModel.onDigit("8") },
            KeySpec("9", "btn_9") { viewModel.onDigit("9") },
            KeySpec("×", "btn_mul", KeyRole.OPERATOR) { viewModel.onOperator("×") }
        ),
        listOf(
            KeySpec("|x|", "btn_abs", KeyRole.OPERATOR) { viewModel.onScientificFunction("|x|") },
            KeySpec("4", "btn_4") { viewModel.onDigit("4") },
            KeySpec("5", "btn_5") { viewModel.onDigit("5") },
            KeySpec("6", "btn_6") { viewModel.onDigit("6") },
            KeySpec("−", "btn_sub", KeyRole.OPERATOR) { viewModel.onOperator("−") }
        ),
        listOf(
            KeySpec("AC", "btn_AC", KeyRole.ACTION) { viewModel.onClear() },
            KeySpec("1", "btn_1") { viewModel.onDigit("1") },
            KeySpec("2", "btn_2") { viewModel.onDigit("2") },
            KeySpec("3", "btn_3") { viewModel.onDigit("3") },
            KeySpec("+", "btn_add", KeyRole.OPERATOR) { viewModel.onOperator("+") }
        ),
        listOf(
            KeySpec("⌫", "btn_backspace", KeyRole.ACTION) { viewModel.onBackspace() },
            KeySpec("±", "btn_negate", KeyRole.ACTION) { viewModel.onNegate() },
            KeySpec("0", "btn_0") { viewModel.onDigit("0") },
            KeySpec(".", "btn_dot") { viewModel.onDecimalPoint() },
            KeySpec("=", "btn_equals", KeyRole.ACCENT) { viewModel.onEquals() }
        )
    )

    KeypadGrid(rows = rows, modifier = modifier)
}

@Composable
fun ProgrammerModeKeypad(
    viewModel: CalculatorViewModel,
    activeBase: NumberBase,
    showBitMatrix: Boolean,
    modifier: Modifier = Modifier
) {
    fun hexRole(char: Char): KeyRole {
        val digitVal = when (char) {
            in '0'..'9' -> char - '0'
            in 'A'..'F' -> char - 'A' + 10
            else -> 99
        }
        return if (digitVal < activeBase.radix) KeyRole.DIGIT else KeyRole.DISABLED
    }

    val rows = listOf(
        listOf(
            KeySpec("BITS", "btn_toggle_bits", if (showBitMatrix) KeyRole.ACCENT else KeyRole.ACTION) { viewModel.toggleBitMatrix() },
            KeySpec("AND", "btn_AND", KeyRole.OPERATOR) { viewModel.onProgrammerBitwiseOp(BitwiseOp.AND) },
            KeySpec("OR", "btn_OR", KeyRole.OPERATOR) { viewModel.onProgrammerBitwiseOp(BitwiseOp.OR) },
            KeySpec("XOR", "btn_XOR", KeyRole.OPERATOR) { viewModel.onProgrammerBitwiseOp(BitwiseOp.XOR) },
            KeySpec("NOT", "btn_NOT", KeyRole.OPERATOR) { viewModel.onProgrammerBitwiseOp(BitwiseOp.NOT) }
        ),
        listOf(
            KeySpec("NAND", "btn_NAND", KeyRole.OPERATOR) { viewModel.onProgrammerBitwiseOp(BitwiseOp.NAND) },
            KeySpec("NOR", "btn_NOR", KeyRole.OPERATOR) { viewModel.onProgrammerBitwiseOp(BitwiseOp.NOR) },
            KeySpec("LSH", "btn_LSH", KeyRole.OPERATOR) { viewModel.onProgrammerShiftOp(ShiftType.LSH) },
            KeySpec("RSH", "btn_RSH", KeyRole.OPERATOR) { viewModel.onProgrammerShiftOp(ShiftType.RSH) },
            KeySpec("ROL", "btn_ROL", KeyRole.OPERATOR) { viewModel.onProgrammerShiftOp(ShiftType.ROL) }
        ),
        listOf(
            KeySpec("A", "btn_A", hexRole('A')) { viewModel.onDigit("A") },
            KeySpec("B", "btn_B", hexRole('B')) { viewModel.onDigit("B") },
            KeySpec("7", "btn_7", hexRole('7')) { viewModel.onDigit("7") },
            KeySpec("8", "btn_8", hexRole('8')) { viewModel.onDigit("8") },
            KeySpec("9", "btn_9", hexRole('9')) { viewModel.onDigit("9") },
            KeySpec("÷", "btn_div", KeyRole.OPERATOR) { viewModel.onOperator("÷") }
        ),
        listOf(
            KeySpec("C", "btn_C", hexRole('C')) { viewModel.onDigit("C") },
            KeySpec("D", "btn_D", hexRole('D')) { viewModel.onDigit("D") },
            KeySpec("4", "btn_4", hexRole('4')) { viewModel.onDigit("4") },
            KeySpec("5", "btn_5", hexRole('5')) { viewModel.onDigit("5") },
            KeySpec("6", "btn_6", hexRole('6')) { viewModel.onDigit("6") },
            KeySpec("×", "btn_mul", KeyRole.OPERATOR) { viewModel.onOperator("×") }
        ),
        listOf(
            KeySpec("E", "btn_E", hexRole('E')) { viewModel.onDigit("E") },
            KeySpec("F", "btn_F", hexRole('F')) { viewModel.onDigit("F") },
            KeySpec("1", "btn_1", hexRole('1')) { viewModel.onDigit("1") },
            KeySpec("2", "btn_2", hexRole('2')) { viewModel.onDigit("2") },
            KeySpec("3", "btn_3", hexRole('3')) { viewModel.onDigit("3") },
            KeySpec("−", "btn_sub", KeyRole.OPERATOR) { viewModel.onOperator("−") }
        ),
        listOf(
            KeySpec("AC", "btn_AC", KeyRole.ACTION) { viewModel.onClear() },
            KeySpec("⌫", "btn_backspace", KeyRole.ACTION) { viewModel.onBackspace() },
            KeySpec("±", "btn_negate", KeyRole.ACTION) { viewModel.onNegate() },
            KeySpec("0", "btn_0", hexRole('0')) { viewModel.onDigit("0") },
            KeySpec("+", "btn_add", KeyRole.OPERATOR) { viewModel.onOperator("+") },
            KeySpec("=", "btn_equals", KeyRole.ACCENT) { viewModel.onEquals() }
        )
    )

    KeypadGrid(rows = rows, modifier = modifier)
}

@Composable
private fun KeypadGrid(
    rows: List<List<KeySpec>>,
    modifier: Modifier = Modifier
) {
    val shapes = SkinTheme.shapes
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(shapes.buttonSpacing)
    ) {
        rows.forEach { rowSpecs ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(shapes.buttonSpacing)
            ) {
                rowSpecs.forEach { keySpec ->
                    SkinKeyButton(
                        spec = keySpec,
                        modifier = Modifier
                            .weight(keySpec.weight)
                            .height(200.dp)
                    )
                }
            }
        }
    }
}
