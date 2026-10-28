package com.calculator.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Futuristic Cyber HUD Skin:
 * Sci-fi HUD aesthetic with neon cyan/magenta/amber accents, glowing borders,
 * angular chamfered/cut-corner geometry, and scanline HUD treatment.
 */
object FuturisticSkin {
    val VoidBlack = Color(0xFF05070E)
    val CyberPanel = Color(0xFF0A1022)
    val HudDisplayBg = Color(0xFF070D1C)
    val NeonCyan = Color(0xFF00F5D4)
    val NeonMagenta = Color(0xFFFF2A85)
    val NeonAmber = Color(0xFFFFBE0B)
    val CyberKeyBg = Color(0xFF0D162E)
    val CyberOpBg = Color(0xFF141036)
    val CyberActionBg = Color(0xFF1F1135)
    val HoloWhite = Color(0xFFE0FBFC)
    val HoloMuted = Color(0xFF5CE1E6)

    val spec = SkinSpec(
        type = SkinType.FUTURISTIC,
        colors = SkinColors(
            background = VoidBlack,
            surface = CyberPanel,
            displayBackground = HudDisplayBg,
            textPrimary = NeonCyan,
            textSecondary = HoloMuted,
            buttonNormal = CyberKeyBg,
            buttonNormalText = HoloWhite,
            buttonOperator = CyberOpBg,
            buttonOperatorText = NeonAmber,
            buttonAction = CyberActionBg,
            buttonActionText = NeonMagenta,
            buttonAccent = NeonCyan,
            buttonAccentText = VoidBlack,
            border = NeonCyan,
            glow = Color(0x6600F5D4),
            error = NeonMagenta
        ),
        typography = SkinTypography(
            displayExpression = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                letterSpacing = 1.8.sp
            ),
            displayResult = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                letterSpacing = 2.2.sp
            ),
            displayPreview = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                letterSpacing = 1.4.sp
            ),
            buttonText = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.2.sp
            ),
            programmerBaseLabel = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp
            ),
            programmerBaseValue = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.2.sp
            ),
            headerTitle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 2.sp
            )
        ),
        shapes = SkinShapes(
            buttonCornerRadius = 10.dp,
            buttonBorderWidth = 1.5.dp,
            displayCornerRadius = 12.dp,
            displayBorderWidth = 2.dp,
            buttonSpacing = 7.dp,
            buttonElevation = 8.dp,
            isCutCorner = true,
            hasScanlineOverlay = true,
            hasPixelShadow = false
        )
    )
}
