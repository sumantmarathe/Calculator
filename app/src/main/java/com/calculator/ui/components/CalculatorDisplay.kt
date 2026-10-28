package com.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.engine.AngleUnit
import com.calculator.engine.programmer.BaseRepresentations
import com.calculator.engine.programmer.NumberBase
import com.calculator.engine.programmer.WordSize
import com.calculator.ui.CalculatorMode
import com.calculator.ui.theme.SkinTheme

@Composable
fun CalculatorDisplay(
    mode: CalculatorMode,
    expression: String,
    displayResult: String,
    livePreview: String?,
    isError: Boolean,
    hasMemory: Boolean,
    memoryFormatted: String,
    angleUnit: AngleUnit,
    programmerBase: NumberBase,
    programmerWordSize: WordSize,
    programmerSigned: Boolean,
    programmerReps: BaseRepresentations,
    onAngleUnitClick: () -> Unit,
    onBaseSelect: (NumberBase) -> Unit,
    onWordSizeClick: () -> Unit,
    onSignedToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SkinTheme.colors
    val typography = SkinTheme.typography
    val shapes = SkinTheme.shapes

    val displayShape: Shape = if (shapes.isCutCorner) {
        CutCornerShape(topStart = shapes.displayCornerRadius, bottomEnd = shapes.displayCornerRadius)
    } else {
        RoundedCornerShape(shapes.displayCornerRadius)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(displayShape)
            .background(colors.displayBackground)
            .border(shapes.displayBorderWidth, colors.border, displayShape)
            .drawBehind {
                if (shapes.hasScanlineOverlay) {
                    val step = 6.dp.toPx()
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = colors.glow.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += step
                    }
                }
            }
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            // Top Status Bar inside Display (Memory badge, Angle unit, Word Size)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasMemory) {
                        Text(
                            text = "M ($memoryFormatted)",
                            style = typography.programmerBaseLabel,
                            color = colors.buttonAccent,
                            modifier = Modifier.testTag("display_memory_indicator")
                        )
                    } else {
                        Text(
                            text = mode.displayName.uppercase(),
                            style = typography.programmerBaseLabel,
                            color = colors.textSecondary
                        )
                    }
                }

                if (mode == CalculatorMode.SCIENTIFIC) {
                    Text(
                        text = angleUnit.name,
                        style = typography.programmerBaseLabel,
                        color = colors.buttonAccent,
                        modifier = Modifier
                            .testTag("btn_angle_unit")
                            .clickable { onAngleUnitClick() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                } else if (mode == CalculatorMode.PROGRAMMER) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = programmerWordSize.label,
                            style = typography.programmerBaseLabel,
                            color = colors.buttonAccent,
                            modifier = Modifier
                                .testTag("btn_word_size")
                                .clickable { onWordSizeClick() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Text(
                            text = if (programmerSigned) "SIGNED" else "UNSIGNED",
                            style = typography.programmerBaseLabel,
                            color = colors.buttonOperatorText,
                            modifier = Modifier
                                .testTag("btn_signed_toggle")
                                .clickable { onSignedToggle() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Expression Line
            Text(
                text = expression.ifEmpty { " " },
                style = typography.displayExpression,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("display_expression")
            )

            // Primary Result Line
            Text(
                text = displayResult,
                style = typography.displayResult,
                color = if (isError) colors.error else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("display_result")
            )

            // Live Preview or Programmer Multi-Base Readout
            if (mode == CalculatorMode.PROGRAMMER) {
                Spacer(modifier = Modifier.height(6.dp))
                ProgrammerMultiBaseReadout(
                    activeBase = programmerBase,
                    isSigned = programmerSigned,
                    reps = programmerReps,
                    onBaseSelect = onBaseSelect
                )
            } else if (!livePreview.isNullOrBlank() && livePreview != displayResult) {
                Text(
                    text = "= $livePreview",
                    style = typography.displayPreview,
                    color = colors.textSecondary,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("display_preview")
                )
            }
        }
    }
}

@Composable
private fun ProgrammerMultiBaseReadout(
    activeBase: NumberBase,
    isSigned: Boolean,
    reps: BaseRepresentations,
    onBaseSelect: (NumberBase) -> Unit
) {
    val colors = SkinTheme.colors
    val typography = SkinTheme.typography

    val rows = listOf(
        NumberBase.HEX to reps.hex,
        NumberBase.DEC to (if (isSigned) reps.decSigned else reps.decUnsigned),
        NumberBase.OCT to reps.oct,
        NumberBase.BIN to reps.bin
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        rows.forEach { (base, valueStr) ->
            val selected = base == activeBase
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prog_base_row_${base.name}")
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (selected) colors.surface else colors.displayBackground)
                    .clickable { onBaseSelect(base) }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = base.label,
                    style = typography.programmerBaseLabel,
                    color = if (selected) colors.buttonAccent else colors.textSecondary
                )
                Text(
                    text = valueStr,
                    style = typography.programmerBaseValue.copy(fontSize = 12.sp),
                    color = if (selected) colors.textPrimary else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
