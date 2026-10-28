package com.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.calculator.data.history.LocalHistoryRepository
import com.calculator.data.preferences.DataStorePreferencesRepository
import com.calculator.platform.audio.ProceduralAudioService
import com.calculator.platform.haptics.SkinHapticService
import com.calculator.ui.CalculatorScreen
import com.calculator.ui.CalculatorViewModel

/**
 * MainActivity
 *
 * Single-Activity host for the Jetpack Compose Calculator application.
 * Configured with Android 12+ Splash Screen API, AndroidX Activity 1.9.0
 * edge-to-edge support, and responsive multi-skin Compose UI.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels {
        val appContext = applicationContext
        CalculatorViewModel.provideFactory(
            preferencesRepository = DataStorePreferencesRepository(appContext),
            historyRepository = LocalHistoryRepository(appContext),
            hapticService = SkinHapticService(appContext),
            audioService = ProceduralAudioService(enabledInRuntime = true)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CalculatorScreen(viewModel = viewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorAppPreview() {
    CalculatorScreen(viewModel = CalculatorViewModel())
}
