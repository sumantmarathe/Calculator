package com.calculator.ui.layouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun PhonePortraitLayout(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("layout_phone_portrait")
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

        AnimatedVisibility(visible = state.mode == CalculatorMode.PROGRAMMER && state.showBitMatrix) {
            BitMatrixKeypad(
                bits = state.programmerRepresentations.bits,
                wordSize = state.programmerWordSize,
                onToggleBit = { viewModel.onProgrammerBitToggle(it) }
            )
        }

        if (state.showHistorySheet) {
            HistoryPanel(
                history = state.history,
                onRecallResult = { viewModel.recallHistoryResult(it) },
                onRecallExpression = { viewModel.recallHistoryExpression(it) },
                onDeleteItem = { viewModel.deleteHistoryItem(it) },
                onClearHistory = { viewModel.clearHistory() },
                onClose = { viewModel.toggleHistorySheet(false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
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
