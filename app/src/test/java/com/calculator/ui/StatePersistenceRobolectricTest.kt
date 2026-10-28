package com.calculator.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.calculator.data.history.LocalHistoryRepository
import com.calculator.data.preferences.DataStorePreferencesRepository
import com.calculator.engine.AngleUnit
import com.calculator.engine.programmer.NumberBase
import com.calculator.engine.programmer.WordSize
import com.calculator.platform.audio.ProceduralAudioService
import com.calculator.ui.theme.SkinType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StatePersistenceRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testComposeUi_modeAndSkinSwitchingAndCalculation_rendersAndUpdatesDisplay() {
        val viewModel = CalculatorViewModel()
        composeTestRule.setContent {
            CalculatorScreen(viewModel = viewModel)
        }

        // Verify default Modern skin root surface and Normal mode display
        composeTestRule.onNodeWithTag("calculator_root_surface_MODERN").assertIsDisplayed()
        composeTestRule.onNodeWithTag("display_result").assertTextEquals("0")

        // Perform 7 * 8 = 56 via Compose UI clicks
        composeTestRule.onNodeWithTag("btn_7").performClick()
        composeTestRule.onNodeWithTag("btn_mul").performClick()
        composeTestRule.onNodeWithTag("btn_8").performClick()
        composeTestRule.onNodeWithTag("btn_equals").performClick()

        composeTestRule.onNodeWithTag("display_result").assertTextEquals("56")

        // Cycle skin to Futuristic via header button
        composeTestRule.onNodeWithTag("btn_cycle_skin").performClick()
        composeTestRule.onNodeWithTag("calculator_root_surface_FUTURISTIC").assertIsDisplayed()

        // Cycle skin to Retro Pixel B/W
        composeTestRule.onNodeWithTag("btn_cycle_skin").performClick()
        composeTestRule.onNodeWithTag("calculator_root_surface_RETRO_PIXEL").assertIsDisplayed()

        // Switch to Scientific mode via tab
        composeTestRule.onNodeWithTag("mode_tab_SCIENTIFIC").performClick()
        composeTestRule.onNodeWithTag("btn_sin").assertIsDisplayed()

        // Switch to Programmer mode via tab
        composeTestRule.onNodeWithTag("mode_tab_PROGRAMMER").performClick()
        composeTestRule.onNodeWithTag("prog_base_row_HEX").assertIsDisplayed()
    }

    @Test
    fun testLocalPreferencesAndHistoryPersistence_survivesAppRestart() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Session 1: User sets Retro Pixel skin, Programmer mode, RAD angle, BYTE word size, and adds history
        val prefsRepo1 = DataStorePreferencesRepository(context)
        val historyRepo1 = LocalHistoryRepository(context)
        historyRepo1.clearHistory()

        prefsRepo1.setSkin(SkinType.RETRO_PIXEL)
        prefsRepo1.setCalculatorMode(CalculatorMode.PROGRAMMER)
        prefsRepo1.setAngleUnit(AngleUnit.RADIANS)
        prefsRepo1.setWordSize(WordSize.BYTE)
        prefsRepo1.setNumberBase(NumberBase.HEX)
        prefsRepo1.setHapticsEnabled(false)
        prefsRepo1.setSoundEnabled(true)

        historyRepo1.addRecord("0.1 + 0.2", "0.3", CalculatorMode.NORMAL)
        historyRepo1.addRecord("sin(30)", "0.5", CalculatorMode.SCIENTIFIC)

        // Session 2: Simulate cold app restart by instantiating fresh repositories & ViewModel from disk
        val prefsRepo2 = DataStorePreferencesRepository(context)
        val historyRepo2 = LocalHistoryRepository(context)
        val viewModel2 = CalculatorViewModel(
            preferencesRepository = prefsRepo2,
            historyRepository = historyRepo2
        )

        val restoredState = viewModel2.uiState.value
        assertThat(restoredState.skin).isEqualTo(SkinType.RETRO_PIXEL)
        assertThat(restoredState.mode).isEqualTo(CalculatorMode.PROGRAMMER)
        assertThat(restoredState.angleUnit).isEqualTo(AngleUnit.RADIANS)
        assertThat(restoredState.programmerWordSize).isEqualTo(WordSize.BYTE)
        assertThat(restoredState.programmerBase).isEqualTo(NumberBase.HEX)
        assertThat(restoredState.hapticsEnabled).isFalse()
        assertThat(restoredState.soundEnabled).isTrue()
        assertThat(restoredState.history).hasSize(2)
        assertThat(restoredState.history[0].expression).isEqualTo("sin(30)")
        assertThat(restoredState.history[1].result).isEqualTo("0.3")
    }

    @Test
    fun testProceduralAudioSynthesizer_generatesDistinctNonZeroWaveformsForEachSkin() {
        val audio = ProceduralAudioService(enabledInRuntime = false)
        val retroSamples = audio.getWaveformSamples(SkinType.RETRO_PIXEL)
        val modernSamples = audio.getWaveformSamples(SkinType.MODERN)
        val futuristicSamples = audio.getWaveformSamples(SkinType.FUTURISTIC)

        assertThat(retroSamples.isNotEmpty()).isTrue()
        assertThat(modernSamples.isNotEmpty()).isTrue()
        assertThat(futuristicSamples.isNotEmpty()).isTrue()
        assertThat(retroSamples.any { it != 0.toShort() }).isTrue()
        assertThat(retroSamples.contentEquals(modernSamples)).isFalse()
        assertThat(modernSamples.contentEquals(futuristicSamples)).isFalse()
    }

    @Test
    fun testPlayStorePublishingBundle_allRequiredFilesExistAndMeetCharacterLimits() {
        // Locate root play_store directory
        val candidates = listOf(
            File("play_store"),
            File("../play_store")
        )
        val playStoreDir = candidates.firstOrNull { it.exists() && it.isDirectory }
        assertThat(playStoreDir).isNotNull()
        val dir = playStoreDir!!

        val shortDesc = File(dir, "short_description.txt")
        val fullDesc = File(dir, "full_description.txt")
        val privacyMd = File(dir, "privacy_policy.md")
        val privacyHtml = File(dir, "privacy_policy.html")
        val dataSafety = File(dir, "data_safety.md")
        val signingGuide = File(dir, "signing_instructions.md")
        val checklist = File(dir, "release_checklist.md")
        val iconPng = File(dir, "graphics/app_icon_512.png")
        val featurePng = File(dir, "graphics/feature_graphic_1024x500.png")

        assertThat(shortDesc.exists()).isTrue()
        assertThat(shortDesc.readText().trim().length).isAtMost(80)
        assertThat(shortDesc.readText().trim().length).isGreaterThan(10)

        assertThat(fullDesc.exists()).isTrue()
        assertThat(fullDesc.readText().trim().length).isAtMost(4000)
        assertThat(fullDesc.readText().trim().length).isGreaterThan(200)

        assertThat(privacyMd.exists()).isTrue()
        assertThat(privacyHtml.exists()).isTrue()
        assertThat(dataSafety.exists()).isTrue()
        assertThat(signingGuide.exists()).isTrue()
        assertThat(checklist.exists()).isTrue()
        assertThat(iconPng.exists()).isTrue()
        assertThat(featurePng.exists()).isTrue()
    }
}
