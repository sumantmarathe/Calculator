package com.calculator.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Retro Pixel B/W Skin:
 * Crisp 1-bit/monochrome pixel-art & classic LCD pocket calculator aesthetic,
 * segmented/dot-matrix monospace typography, sharp 0dp corners, and heavy tactile pixel borders.
 */
object RetroPixelSkin {
    val PaperWhite = Color(0xFFE8ECE0)       // Classic dot-matrix LCD reflective backing
    val LcdScreenBg = Color(0xFFD0D9C4)      // High-contrast monochrome LCD screen
    val PixelInk = Color(0xFF111410)         // Pure dark pixel ink
    val PixelSecondary = Color(0xFF3E453A)   // Dimmed pixel matrix ink
    val KeyCapLight = Color(0xFFF4F6F0)      // Crisp monochrome white keycap
    val KeyCapShade = Color(0xFFC4CCB8)      // Shaded operator keycap
    val KeyCapDark = Color(0xFF111410)       // Inverted black accent keycap

    val spec = SkinSpec(
        type = SkinType.RETRO_PIXEL,
        colors = SkinColors(
            background = PaperWhite,
            surface = LcdScreenBg,
            displayBackground = LcdScreenBg,
            textPrimary = PixelInk,
            textSecondary = PixelSecondary,
            buttonNormal = KeyCapLight,
            buttonNormalText = PixelInk,
            buttonOperator = KeyCapShade,
            buttonOperatorText = PixelInk,
            buttonAction = KeyCapShade,
            buttonActionText = PixelInk,
            buttonAccent = KeyCapDark,
            buttonAccentText = KeyCapLight,
            border = PixelInk,
            glow = Color.Transparent,
            error = PixelInk
        ),
        typography = SkinTypography(
            displayExpression = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 1.5.sp
            ),
            displayResult = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp,
                letterSpacing = 2.sp
            ),
            displayPreview = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                letterSpacing = 1.sp
            ),
            buttonText = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                letterSpacing = 0.8.sp
            ),
            programmerBaseLabel = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            ),
            programmerBaseValue = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            ),
            headerTitle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                letterSpacing = 1.5.sp
            )
        ),
        shapes = SkinShapes(
            buttonCornerRadius = 0.dp,
            buttonBorderWidth = 2.5.dp,
            displayCornerRadius = 0.dp,
            displayBorderWidth = 3.dp,
            buttonSpacing = 6.dp,
            buttonElevation = 0.dp,
            isCutCorner = false,
            hasScanlineOverlay = false,
            hasPixelShadow = true
        )
    )
}
