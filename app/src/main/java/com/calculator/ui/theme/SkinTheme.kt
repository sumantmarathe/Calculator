package com.calculator.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class SkinType(val displayName: String, val subtitle: String) {
    RETRO_PIXEL("Retro Pixel B/W", "1-Bit Monochrome Dot-Matrix"),
    MODERN("Modern", "Sleek Contemporary Minimalist"),
    FUTURISTIC("Futuristic", "Cyber Sci-Fi Neon HUD")
}

@Immutable
data class SkinColors(
    val background: Color,
    val surface: Color,
    val displayBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val buttonNormal: Color,
    val buttonNormalText: Color,
    val buttonOperator: Color,
    val buttonOperatorText: Color,
    val buttonAction: Color,
    val buttonActionText: Color,
    val buttonAccent: Color,
    val buttonAccentText: Color,
    val border: Color,
    val glow: Color,
    val error: Color
)

@Immutable
data class SkinTypography(
    val displayExpression: TextStyle,
    val displayResult: TextStyle,
    val displayPreview: TextStyle,
    val buttonText: TextStyle,
    val programmerBaseLabel: TextStyle,
    val programmerBaseValue: TextStyle,
    val headerTitle: TextStyle
)

@Immutable
data class SkinShapes(
    val buttonCornerRadius: Dp,
    val buttonBorderWidth: Dp,
    val displayCornerRadius: Dp,
    val displayBorderWidth: Dp,
    val buttonSpacing: Dp,
    val buttonElevation: Dp,
    val isCutCorner: Boolean = false,
    val hasScanlineOverlay: Boolean = false,
    val hasPixelShadow: Boolean = false
)

@Immutable
data class SkinSpec(
    val type: SkinType,
    val colors: SkinColors,
    val typography: SkinTypography,
    val shapes: SkinShapes
)

val LocalSkinSpec = staticCompositionLocalOf { ModernSkin.spec }

object SkinTheme {
    val current: SkinSpec
        @Composable
        get() = LocalSkinSpec.current

    val colors: SkinColors
        @Composable
        get() = LocalSkinSpec.current.colors

    val typography: SkinTypography
        @Composable
        get() = LocalSkinSpec.current.typography

    val shapes: SkinShapes
        @Composable
        get() = LocalSkinSpec.current.shapes
}

fun getSkinSpec(skinType: SkinType): SkinSpec = when (skinType) {
    SkinType.RETRO_PIXEL -> RetroPixelSkin.spec
    SkinType.MODERN -> ModernSkin.spec
    SkinType.FUTURISTIC -> FuturisticSkin.spec
}

@Composable
fun CalculatorSkinTheme(
    skinType: SkinType = SkinType.MODERN,
    content: @Composable () -> Unit
) {
    val targetSpec = getSkinSpec(skinType)
    val animSpec = tween<Color>(durationMillis = 280)

    val animatedColors = SkinColors(
        background = animateColorAsState(targetSpec.colors.background, animSpec, label = "bg").value,
        surface = animateColorAsState(targetSpec.colors.surface, animSpec, label = "surface").value,
        displayBackground = animateColorAsState(targetSpec.colors.displayBackground, animSpec, label = "dispBg").value,
        textPrimary = animateColorAsState(targetSpec.colors.textPrimary, animSpec, label = "txtPri").value,
        textSecondary = animateColorAsState(targetSpec.colors.textSecondary, animSpec, label = "txtSec").value,
        buttonNormal = animateColorAsState(targetSpec.colors.buttonNormal, animSpec, label = "btnNorm").value,
        buttonNormalText = animateColorAsState(targetSpec.colors.buttonNormalText, animSpec, label = "btnNormTxt").value,
        buttonOperator = animateColorAsState(targetSpec.colors.buttonOperator, animSpec, label = "btnOp").value,
        buttonOperatorText = animateColorAsState(targetSpec.colors.buttonOperatorText, animSpec, label = "btnOpTxt").value,
        buttonAction = animateColorAsState(targetSpec.colors.buttonAction, animSpec, label = "btnAct").value,
        buttonActionText = animateColorAsState(targetSpec.colors.buttonActionText, animSpec, label = "btnActTxt").value,
        buttonAccent = animateColorAsState(targetSpec.colors.buttonAccent, animSpec, label = "btnAcc").value,
        buttonAccentText = animateColorAsState(targetSpec.colors.buttonAccentText, animSpec, label = "btnAccTxt").value,
        border = animateColorAsState(targetSpec.colors.border, animSpec, label = "border").value,
        glow = animateColorAsState(targetSpec.colors.glow, animSpec, label = "glow").value,
        error = animateColorAsState(targetSpec.colors.error, animSpec, label = "error").value
    )

    val activeSpec = targetSpec.copy(colors = animatedColors)

    val materialScheme = if (skinType == SkinType.RETRO_PIXEL) {
        lightColorScheme(
            primary = activeSpec.colors.buttonAccent,
            background = activeSpec.colors.background,
            surface = activeSpec.colors.surface,
            onPrimary = activeSpec.colors.buttonAccentText,
            onBackground = activeSpec.colors.textPrimary,
            onSurface = activeSpec.colors.textPrimary
        )
    } else {
        darkColorScheme(
            primary = activeSpec.colors.buttonAccent,
            background = activeSpec.colors.background,
            surface = activeSpec.colors.surface,
            onPrimary = activeSpec.colors.buttonAccentText,
            onBackground = activeSpec.colors.textPrimary,
            onSurface = activeSpec.colors.textPrimary
        )
    }

    CompositionLocalProvider(LocalSkinSpec provides activeSpec) {
        MaterialTheme(
            colorScheme = materialScheme,
            content = content
        )
    }
}
