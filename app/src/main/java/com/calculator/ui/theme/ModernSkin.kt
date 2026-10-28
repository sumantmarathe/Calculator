package com.calculator.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern Minimalist Skin:
 * Clean, sleek contemporary Material/minimalist aesthetic with subtle elevation,
 * squircle button geometry, and refined sans-serif typography.
 */
object ModernSkin {
    val DeepObsidian = Color(0xFF0F172A)
    val ElevatedSlate = Color(0xFF1E293B)
    val DisplayGlass = Color(0xFF131D31)
    val KeySurface = Color(0xFF26334D)
    val OperatorSurface = Color(0xFF1D3557)
    val ActionSurface = Color(0xFF334155)
    val IndigoAccent = Color(0xFF38BDF8)
    val CrispWhite = Color(0xFFF8FAFC)
    val MutedSilver = Color(0xFF94A3B8)
    val SubtleBorder = Color(0xFF334155)
    val CoralError = Color(0xFFFB7185)

    val spec = SkinSpec(
        type = SkinType.MODERN,
        colors = SkinColors(
            background = DeepObsidian,
            surface = ElevatedSlate,
            displayBackground = DisplayGlass,
            textPrimary = CrispWhite,
            textSecondary = MutedSilver,
            buttonNormal = KeySurface,
            buttonNormalText = CrispWhite,
            buttonOperator = OperatorSurface,
            buttonOperatorText = IndigoAccent,
            buttonAction = ActionSurface,
            buttonActionText = Color(0xFFE2E8F0),
            buttonAccent = IndigoAccent,
            buttonAccentText = Color(0xFF082F49),
            border = SubtleBorder,
            glow = Color(0x2238BDF8),
            error = CoralError
        ),
        typography = SkinTypography(
            displayExpression = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                letterSpacing = 0.2.sp
            ),
            displayResult = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 40.sp,
                letterSpacing = (-0.5).sp
            ),
            displayPreview = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                letterSpacing = 0.sp
            ),
            buttonText = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                letterSpacing = 0.sp
            ),
            programmerBaseLabel = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            ),
            programmerBaseValue = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            ),
            headerTitle = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 0.2.sp
            )
        ),
        shapes = SkinShapes(
            buttonCornerRadius = 18.dp,
            buttonBorderWidth = 1.dp,
            displayCornerRadius = 22.dp,
            displayBorderWidth = 1.dp,
            buttonSpacing = 8.dp,
            buttonElevation = 4.dp,
            isCutCorner = false,
            hasScanlineOverlay = false,
            hasPixelShadow = false
        )
    )
}
