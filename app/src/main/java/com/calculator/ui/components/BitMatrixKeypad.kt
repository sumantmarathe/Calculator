package com.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.engine.programmer.WordSize
import com.calculator.ui.theme.SkinTheme

@Composable
fun BitMatrixKeypad(
    bits: List<Boolean>,
    wordSize: WordSize,
    onToggleBit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SkinTheme.colors
    val shapes = SkinTheme.shapes

    // Show active bits grouped into 16-bit rows from MSB to LSB
    val maxBits = wordSize.bits
    val rowCount = (maxBits / 16).coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bit_matrix_panel")
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(shapes.displayCornerRadius.coerceAtMost(12.dp)))
            .background(colors.surface)
            .border(shapes.buttonBorderWidth, colors.border, RoundedCornerShape(shapes.displayCornerRadius.coerceAtMost(12.dp)))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (maxBits == 8) {
            BitRow(
                startBit = 7,
                endBit = 0,
                bits = bits,
                onToggleBit = onToggleBit
            )
        } else {
            for (r in 0 until rowCount) {
                val highBit = maxBits - 1 - (r * 16)
                val lowBit = highBit - 15
                BitRow(
                    startBit = highBit,
                    endBit = lowBit,
                    bits = bits,
                    onToggleBit = onToggleBit
                )
            }
        }
    }
}

@Composable
private fun BitRow(
    startBit: Int,
    endBit: Int,
    bits: List<Boolean>,
    onToggleBit: (Int) -> Unit
) {
    val colors = SkinTheme.colors
    val typography = SkinTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$startBit",
            style = typography.programmerBaseLabel.copy(fontSize = 9.sp),
            color = colors.textSecondary
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (bitIdx in startBit downTo endBit) {
                val isSet = bits.getOrElse(bitIdx) { false }
                Box(
                    modifier = Modifier
                        .testTag("bit_cell_$bitIdx")
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSet) colors.buttonAccent else colors.displayBackground)
                        .border(1.dp, colors.border, RoundedCornerShape(3.dp))
                        .clickable { onToggleBit(bitIdx) }
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSet) "1" else "0",
                        style = typography.programmerBaseValue.copy(fontSize = 11.sp),
                        color = if (isSet) colors.buttonAccentText else colors.textPrimary
                    )
                }
            }
        }
        Text(
            text = "$endBit",
            style = typography.programmerBaseLabel.copy(fontSize = 9.sp),
            color = colors.textSecondary
        )
    }
}
