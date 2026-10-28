package com.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.ui.CalculatorMode
import com.calculator.ui.theme.SkinTheme
import com.calculator.ui.theme.SkinType

@Composable
fun ModeAndSkinHeader(
    activeMode: CalculatorMode,
    activeSkin: SkinType,
    hasHistory: Boolean,
    onModeSelect: (CalculatorMode) -> Unit,
    onSkinCycle: () -> Unit,
    onHistoryToggle: () -> Unit,
    onSettingsToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SkinTheme.colors
    val typography = SkinTheme.typography
    val shapes = SkinTheme.shapes
    val chipShape: Shape = if (shapes.isCutCorner) {
        CutCornerShape(6.dp)
    } else {
        RoundedCornerShape(shapes.buttonCornerRadius.coerceAtMost(12.dp))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Top row: Mode Tabs + Skin / History / Settings Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Switcher Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CalculatorMode.entries.forEach { mode ->
                    val selected = mode == activeMode
                    val bg = if (selected) colors.buttonAccent else colors.surface
                    val fg = if (selected) colors.buttonAccentText else colors.textSecondary
                    Box(
                        modifier = Modifier
                            .testTag("mode_tab_${mode.name}")
                            .clip(chipShape)
                            .background(bg)
                            .border(shapes.buttonBorderWidth, colors.border, chipShape)
                            .clickable { onModeSelect(mode) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.displayName,
                            style = typography.headerTitle.copy(fontSize = 13.sp),
                            color = fg
                        )
                    }
                }
            }

            // Utility buttons: Skin Switcher, History Tape, Settings
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .testTag("btn_cycle_skin")
                        .clip(chipShape)
                        .background(colors.buttonOperator)
                        .border(shapes.buttonBorderWidth, colors.border, chipShape)
                        .clickable { onSkinCycle() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (activeSkin) {
                            SkinType.RETRO_PIXEL -> "PIXEL"
                            SkinType.MODERN -> "MODERN"
                            SkinType.FUTURISTIC -> "CYBER"
                        },
                        style = typography.programmerBaseLabel,
                        color = colors.buttonOperatorText
                    )
                }

                Box(
                    modifier = Modifier
                        .testTag("btn_history_toggle")
                        .semantics { contentDescription = "Calculation History" }
                        .clip(chipShape)
                        .background(if (hasHistory) colors.buttonAction else colors.surface)
                        .border(shapes.buttonBorderWidth, colors.border, chipShape)
                        .clickable { onHistoryToggle() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HIST",
                        style = typography.programmerBaseLabel,
                        color = colors.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .testTag("btn_settings_toggle")
                        .semantics { contentDescription = "Settings and Skins" }
                        .clip(chipShape)
                        .background(colors.surface)
                        .border(shapes.buttonBorderWidth, colors.border, chipShape)
                        .clickable { onSettingsToggle() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SKIN",
                        style = typography.programmerBaseLabel,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun SkinAndSettingsDialog(
    activeSkin: SkinType,
    hapticsEnabled: Boolean,
    soundEnabled: Boolean,
    onSelectSkin: (SkinType) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SkinTheme.colors
    val typography = SkinTheme.typography

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textPrimary,
        title = {
            Text(
                text = "Visual Skins & Feedback",
                style = typography.headerTitle.copy(fontSize = 18.sp),
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select Visual Skin",
                    style = typography.programmerBaseLabel,
                    color = colors.textSecondary
                )
                SkinType.entries.forEach { skin ->
                    val isSelected = skin == activeSkin
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("skin_option_${skin.name}")
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.buttonAccent else colors.displayBackground)
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            .clickable { onSelectSkin(skin) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = skin.displayName,
                                style = typography.buttonText.copy(fontSize = 15.sp),
                                color = if (isSelected) colors.buttonAccentText else colors.textPrimary
                            )
                            Text(
                                text = skin.subtitle,
                                style = typography.programmerBaseValue.copy(fontSize = 11.sp),
                                color = if (isSelected) colors.buttonAccentText else colors.textSecondary
                            )
                        }
                        if (isSelected) {
                            Text(
                                text = "ACTIVE",
                                style = typography.programmerBaseLabel,
                                color = colors.buttonAccentText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tactile Haptics",
                        style = typography.buttonText.copy(fontSize = 15.sp),
                        color = colors.textPrimary
                    )
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = onToggleHaptics,
                        modifier = Modifier.testTag("switch_haptics")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Synthesized Button Audio",
                        style = typography.buttonText.copy(fontSize = 15.sp),
                        color = colors.textPrimary
                    )
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onToggleSound,
                        modifier = Modifier.testTag("switch_sound")
                    )
                }

                Text(
                    text = "100% Offline • Zero Permissions • Zero Data Collection",
                    style = typography.programmerBaseValue.copy(fontSize = 11.sp),
                    color = colors.textSecondary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_close_settings")
            ) {
                Text("Done", color = colors.buttonAccent)
            }
        }
    )
}
