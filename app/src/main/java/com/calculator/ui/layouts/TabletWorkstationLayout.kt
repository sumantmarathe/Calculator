package com.calculator.ui.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.calculator.ui.CalculatorMode
import com.calculator.ui.CalculatorUiState
import com.calculator.ui.CalculatorViewModel
import com.calculator.ui.components.BitMatrixKeypad
import com.calculator.ui.components.CalculatorDisplay
import com.calculator.ui.components.CalculatorKeypad
import com.calculator.ui.components.HistoryPanel
import com.calculator.ui.components.ModeAndSkinHeader

@Composable
fun TabletWorkstationLayout(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("layout_tablet_workstation")
    ) {
        ModeAndSkinHeader(
            activeMode = state.mode,
            activeSkin = state.skin,
            hasHistory = state.history.isNotEmpty(),
            onModeSelect = { viewModel.setMode(it) },
            onSkinCycle = { viewModel.cycleSkin() },
            onHistoryToggle = { viewModel.toggleHistorySheet() },
            onSettingsToggle = { viewModel.toggleSettingsDialog(true) }
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Left Pane: Persistent History Tape + Bit Matrix (when in Programmer Mode)
            Column(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
                    .padding(4.dp)
            ) {
                if (state.mode == CalculatorMode.PROGRAMMER) {
                    BitMatrixKeypad(
                        bits = state.programmerRepresentations.bits,
                        wordSize = state.programmerWordSize,
                        onToggleBit = { viewModel.onProgrammerBitToggle(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HistoryPanel(
                    history = state.history,
                    onRecallResult = { viewModel.recallHistoryResult(it) },
                    onRecallExpression = { viewModel.recallHistoryExpression(it) },
                    onDeleteItem = { viewModel.deleteHistoryItem(it) },
                    onClearHistory = { viewModel.clearHistory() },
                    onClose = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            // Right Pane: Main Display + Full Keypad
            Column(
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxHeight()
                    .padding(4.dp)
            ) {
                CalculatorDisplay(
                    mode = state.mode,
                    expression = state.expression,
                    displayResult = state.displayResult,
                    livePreview = state.livePreview,
                    isError = state.isError,
                    hasMemory = state.hasMemory,
                    memoryFormatted = state.memoryFormatted,
                    angleUnit = state.angleUnit,
                    programmerBase = state.programmerBase,
                    programmerWordSize = state.programmerWordSize,
                    programmerSigned = state.programmerSigned,
                    programmerReps = state.programmerRepresentations,
                    onAngleUnitClick = { viewModel.cycleAngleUnit() },
                    onBaseSelect = { viewModel.setProgrammerBase(it) },
                    onWordSizeClick = { viewModel.cycleProgrammerWordSize() },
                    onSignedToggle = { viewModel.toggleProgrammerSigned() },
                    modifier = Modifier.fillMaxWidth()
                )

                CalculatorKeypad(
                    mode = state.mode,
                    viewModel = viewModel,
                    isSecondFunction = state.isSecondFunctionActive,
                    programmerBase = state.programmerBase,
                    showBitMatrix = state.showBitMatrix,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}
