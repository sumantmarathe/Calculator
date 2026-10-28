package com.calculator.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.calculator.ui.theme.SkinType
import com.calculator.ui.theme.getSkinSpec
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class SkinSwitchingTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAllThreeRequiredSkinsHaveDistinctPalettesTypographyAndShapes() {
        val retro = getSkinSpec(SkinType.RETRO_PIXEL)
        val modern = getSkinSpec(SkinType.MODERN)
        val futuristic = getSkinSpec(SkinType.FUTURISTIC)

        // 1. Distinct background and primary text colors across all 3 skins
        assertThat(retro.colors.background).isNotEqualTo(modern.colors.background)
        assertThat(modern.colors.background).isNotEqualTo(futuristic.colors.background)
        assertThat(retro.colors.background).isNotEqualTo(futuristic.colors.background)

        assertThat(retro.colors.textPrimary).isNotEqualTo(modern.colors.textPrimary)
        assertThat(modern.colors.textPrimary).isNotEqualTo(futuristic.colors.textPrimary)

        // 2. Retro Pixel B/W has sharp 0dp corners, pixel shadow, and Monospace font
        assertThat(retro.shapes.buttonCornerRadius).isEqualTo(0.dp)
        assertThat(retro.shapes.displayCornerRadius).isEqualTo(0.dp)
        assertThat(retro.shapes.hasPixelShadow).isTrue()
        assertThat(retro.typography.displayResult.fontFamily).isEqualTo(FontFamily.Monospace)

        // 3. Modern has rounded squircle corners (18dp) and SansSerif typography
        assertThat(modern.shapes.buttonCornerRadius).isEqualTo(18.dp)
        assertThat(modern.shapes.isCutCorner).isFalse()
        assertThat(modern.typography.displayResult.fontFamily).isEqualTo(FontFamily.SansSerif)

        // 4. Futuristic has chamfered cut corners and scanline HUD overlay
        assertThat(futuristic.shapes.isCutCorner).isTrue()
        assertThat(futuristic.shapes.hasScanlineOverlay).isTrue()
        assertThat(futuristic.colors.glow.alpha).isGreaterThan(0f)
    }

    @Test
    fun testViewModelSkinCyclingAndSelectionUpdatesUiState() {
        val vm = CalculatorViewModel()
        assertThat(vm.uiState.value.skin).isEqualTo(SkinType.MODERN)

        vm.setSkin(SkinType.RETRO_PIXEL)
        assertThat(vm.uiState.value.skin).isEqualTo(SkinType.RETRO_PIXEL)

        vm.cycleSkin()
        assertThat(vm.uiState.value.skin).isEqualTo(SkinType.MODERN)

        vm.cycleSkin()
        assertThat(vm.uiState.value.skin).isEqualTo(SkinType.FUTURISTIC)

        vm.cycleSkin()
        assertThat(vm.uiState.value.skin).isEqualTo(SkinType.RETRO_PIXEL)
    }
}
