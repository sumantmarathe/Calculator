package com.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.calculator.ui.components.SkinAndSettingsDialog
import com.calculator.ui.layouts.PhoneLandscapeLayout
import com.calculator.ui.layouts.PhonePortraitLayout
import com.calculator.ui.layouts.TabletWorkstationLayout
import com.calculator.ui.theme.CalculatorSkinTheme
import com.calculator.ui.theme.SkinTheme

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    CalculatorSkinTheme(skinType = state.skin) {
        val colors = SkinTheme.colors
        Surface(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
                .safeDrawingPadding()
                .testTag("calculator_root_surface_${state.skin.name}"),
            color = colors.background
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val width = maxWidth
                val height = maxHeight

                when {
                    width >= 720.dp -> {
                        TabletWorkstationLayout(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                    width > height && width >= 520.dp -> {
                        PhoneLandscapeLayout(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                    else -> {
                        PhonePortraitLayout(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                }

                if (state.showSettingsDialog) {
                    SkinAndSettingsDialog(
                        activeSkin = state.skin,
                        hapticsEnabled = state.hapticsEnabled,
                        soundEnabled = state.soundEnabled,
                        onSelectSkin = { viewModel.setSkin(it) },
                        onToggleHaptics = { viewModel.setHapticsEnabled(it) },
                        onToggleSound = { viewModel.setSoundEnabled(it) },
                        onDismiss = { viewModel.toggleSettingsDialog(false) }
                    )
                }
            }
        }
    }
}
