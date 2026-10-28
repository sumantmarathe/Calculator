# Project: Native Android Multi-Mode Calculator

## Architecture
The application is a 100% native Android application written in Kotlin and Jetpack Compose with a clean, decoupled architecture:
1. **Engine Layer (`com.calculator.engine`)**: Pure Kotlin domain logic with zero external dependencies and zero Android framework imports.
   - `core/`: Tokenizer, Lexer, Shunting-Yard AST parser, precision evaluator using `BigDecimal` (`MathContext.DECIMAL128`) and `BigInteger`.
   - `normal/`: Arithmetic, percentages, memory registers (`M+`, `M-`, `MR`, `MC`).
   - `scientific/`: Trigonometric (DEG/RAD/GRAD with exact circle boundary tables), hyperbolic, logarithmic, exponential, powers, factorials, domain errors.
   - `programmer/`: 64-bit unsigned state (`ULong`), multi-base conversion (HEX, DEC, OCT, BIN), word-size masking (BYTE, WORD, DWORD, QWORD), bitwise operations, bit shifts, rotations, interactive bit toggling.
2. **UI & Presentation Layer (`com.calculator.ui`)**: Declarative UI using Jetpack Compose with dynamic skin tokens.
   - `theme/`: `SkinTheme` composition provider supplying `SkinColors`, `SkinTypography`, `SkinShapes`, and `SkinAesthetic` for `Retro Pixel B/W`, `Modern`, and `Futuristic` skins.
   - `components/`: Dynamic keypads, interactive 64-bit matrix, active expression display with syntax coloring and live preview, history drawer.
   - `layouts/`: Adaptive window-size layouts for Phone Portrait, Phone Landscape, and Tablet/Foldable Dual-Pane Workstation.
   - `viewmodel/`: MVI/MVVM `CalculatorViewModel` holding immutable UI state via `StateFlow`.
3. **Hardware & Feedback Services (`com.calculator.platform`)**:
   - `audio/`: Zero-asset procedural PCM waveform audio synthesizer (`SoundPool`) providing skin-tailored clicks and chimes.
   - `haptics/`: Skin-aware tactile vibration profiles using `VibratorManager` / `Vibrator`.
4. **Data & Persistence Layer (`com.calculator.data`)**:
   - `preferences/`: Jetpack DataStore persisting user preferences (skin, mode, angle unit, word size, sound/haptics toggles, memory).
   - `history/`: Local persistent history tape recording timestamped calculations with recall and deletion.
5. **Publishing & Compliance Bundle (`play_store/`)**:
   - Store listings, privacy policy, data safety questionnaire responses, release checklist, signing guide, and high-res store graphics.

---

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Gradle Build Setup & Scaffolding | AGP 8.4.2, Kotlin 1.9.24, Java 17, Compose Compiler 1.5.14, Compose BOM 2024.06.00 | M1 | env_spec |
| 2 | Strict Privacy & Zero-Permission Manifest | Zero network, storage, location, contacts permissions. Only optional VIBRATE. | M1 | env_spec |
| 3 | Automated Privacy & Dependency Verification | Gradle tasks to verify merged manifest and check for zero analytics/network SDKs | M1 | env_spec |
| 4 | R8 Full-Mode Minification & ProGuard Rules | Optimized release build configuration with code shrinking and obfuscation rules | M1 | env_spec |
| 5 | Baseline Activity & Compose App Scaffolding | Single Activity architecture with edge-to-edge support and Splash Screen API | M1 | env_spec |
| 6 | High-Precision Decimal Evaluator | `BigDecimal` math with `MathContext.DECIMAL128` (34 digits); `0.1 + 0.2 == 0.3` exact | M2 | engine_spec |
| 7 | Lexical Tokenizer & Shunting-Yard AST Parser | Unary minus disambiguation, 7 precedence ranks, right/left associativity | M2 | engine_spec |
| 8 | Normal Mode Arithmetic & Memory Operations | +, -, *, /, chained operations, memory keys (`M+`, `M-`, `MR`, `MC`, display indicator) | M2 | engine_spec |
| 9 | Multi-Model Percentage Calculations | Contextual percentage: markup (A+B%), discount (A-B%), ratio (A*B%), fraction (B%) | M2 | engine_spec |
| 10 | Scientific Trigonometry (DEG/RAD/GRAD) | sin, cos, tan with exact boundary lookup tables preventing floating-point noise | M2 | engine_spec |
| 11 | Inverse & Hyperbolic Trigonometric Functions | asin, acos, atan, sinh, cosh, tanh, asinh, acosh, atanh | M2 | engine_spec |
| 12 | Exponential, Logarithmic & Power Functions | ln, log10, log2, e^x, 10^x, x^y, sqrt, cbrt, x^2, x^3, 1/x, abs | M2 | engine_spec |
| 13 | Exact Factorials & Mathematical Constants | Exact n! for n <= 1000 using BigInteger; constants pi and e | M2 | engine_spec |
| 14 | Domain Error Recovery & Guardrails | Safe recovery for 1/0, sqrt(-1), log(0), asin(>1), tan(90 deg) with friendly messages | M2 | engine_spec |
| 15 | Live Expression Preview Evaluator | Speculative evaluation during typing with dangling operator tolerance | M2 | engine_spec |
| 16 | Programmer 64-bit Unsigned State Core | `ULong` state model with live simultaneous synchronization across HEX, DEC, OCT, BIN | M3 | engine_spec |
| 17 | Programmer Word Sizes & Masking | QWORD (64-bit), DWORD (32-bit), WORD (16-bit), BYTE (8-bit) with signed/unsigned views | M3 | engine_spec |
| 18 | Programmer Bitwise Logic Operations | AND, OR, XOR, NOT, NAND, NOR with word-size masking | M3 | engine_spec |
| 19 | Programmer Bit Shifts & Rotations | LSH, RSH (arithmetic/logical), ROL, ROR normalized modulo word size | M3 | engine_spec |
| 20 | Interactive 64-Bit Keypad Toggle Matrix | Direct 64-bit toggle interface updating register instantly with disabled high bits | M3 | engine_spec |
| 21 | Dynamic Skin Token Architecture | Strongly-typed Compose `SkinTheme` provider (Colors, Typography, Shapes, Aesthetic) | M4 | ui_spec |
| 22 | Retro Pixel B/W Visual Skin | 1-bit monochrome pixel aesthetic, tactile pixel borders, segmented/dot-matrix display | M4 | ui_spec |
| 23 | Modern Minimalist Visual Skin | Material 3 contemporary aesthetic, squircle geometry, refined typography, subtle elevation | M4 | ui_spec |
| 24 | Futuristic Cyber HUD Visual Skin | Sci-fi HUD aesthetic, neon accents (cyan/amber/magenta), glowing borders, scanlines | M4 | ui_spec |
| 25 | Active Expression Display & Live Preview | Multi-line expression display with syntax coloring and uncommitted live preview | M4 | ui_spec |
| 26 | Responsive Phone Portrait Layout | Stacked mode switching, keypad, and display optimized for one-hand phone use | M4 | ui_spec |
| 27 | Responsive Phone Landscape Layout | Side-by-side layout: display on left, expanded 7x5 unified keypad on right | M4 | ui_spec |
| 28 | Responsive Tablet / Foldable Dual-Pane Layout | Workstation layout with permanent History & Memory drawer on left, keypad on right | M4 | ui_spec |
| 29 | Skin-Aware Tactile Haptic System | Sharp tick (Retro), subtle click (Modern), double pulse (Futuristic) via Vibrator | M5 | ui_spec |
| 30 | Zero-Asset Procedural Audio Synthesizer | Real-time synthesized 16-bit PCM waveforms (mechanical click, soft blip, sci-fi chime) | M5 | ui_spec |
| 31 | Persistent Calculation History Tape | Timestamped history recording, tap to recall result, long-press to recall expression, clear | M5 | ui_spec |
| 32 | Local State Persistence (DataStore) | Offline preferences persistence for skin, mode, angle, word size, toggles, memory | M5 | ui_spec |
| 33 | Adaptive Launcher Icons & Vector Assets | XML vector drawable foreground and background for all screen densities | M6 | env_spec |
| 34 | Android 12+ Splash Screen Integration | Core splash screen support with theme integration | M6 | env_spec |
| 35 | Google Play Store Listing Copy | `short_description.txt` (<=80 chars), `full_description.txt` (<=4000 chars) | M6 | env_spec |
| 36 | Privacy Policy Documentation | Standalone responsive `privacy_policy.html` and Markdown version | M6 | env_spec |
| 37 | Google Play Data Safety Declaration | Complete summary documenting zero collection, sharing, or tracking | M6 | env_spec |
| 38 | Release Signing & Deployment Guide | `signing_instructions.md` and `release_checklist.md` | M6 | env_spec |
| 39 | High-Resolution Store Graphic Assets | 512x512 app icon, 1024x500 feature graphic, and screenshot specifications | M6 | env_spec |
| 40 | Comprehensive Engine Unit Test Suite | Pure JVM JUnit test suite with >100 assertions covering all 51 edge cases | M7 | engine_spec |
| 41 | Headless Robolectric Compose UI Test Suite | Verification of mode switching, skin transitions, history tape, and state persistence | M7 | ui_spec |
| 42 | Full Build & Release Compilation Verification | Clean `assembleDebug`, `assembleRelease`, and `lint` verification | M7 | env_spec |
| 43 | Adversarial Coverage Hardening | White-box stress testing and forensic audit for zero integrity violations | M7 | orchestrator |

---

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Build System, Privacy Manifest & App Scaffolding | Gradle setup, zero-permission manifest, R8 rules, privacy verification tasks, basic MainActivity | none | DONE |
| M2 | Custom Calculator Engine (Normal & Scientific) | Pure Kotlin math engine: tokenization, AST parser, BigDecimal precision, normal/scientific functions, edge cases | M1 | DONE |
| M3 | Programmer Mode Engine & Bit Matrix | 64-bit unsigned engine, 4-base live synchronization, word sizes, bitwise ops, shifts, rotations, bit toggle | M2 | DONE |
| M4 | Jetpack Compose Visual Skins & Responsive Layouts | SkinTheme tokens, Retro Pixel B/W, Modern, Futuristic skins, Portrait/Landscape/Tablet layouts | M1, M2, M3 | DONE |
| M5 | Haptics, Procedural Audio FX, State Persistence & History | VibratorManager haptics, SoundPool procedural PCM audio, DataStore preferences, History Tape | M4 | DONE |
| M6 | Google Play Store Publishing Bundle & Compliance Assets | Adaptive icons, Splash screen, `play_store/` copy, HTML/MD privacy policy, data safety, graphics | M1 | DONE |
| M7 | E2E Integration, Full Test Suite Verification & Adversarial Hardening | Comprehensive JUnit tests (>100 assertions), Robolectric UI tests, assembleDebug/Release, lint, Forensic Audit | M1..M6 | DONE |

---

## Interface Contracts

### 1. Engine ↔ ViewModel Contract (`com.calculator.engine.CalculatorEngine`)
```kotlin
interface CalculatorEngine {
    fun evaluate(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): EvaluationResult
    fun evaluatePreview(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): String?
    fun calculatePercentage(currentExpression: String, percentValue: String): String
}

sealed class EvaluationResult {
    data class Success(val value: BigDecimal, val formatted: String) : EvaluationResult()
    data class DomainError(val message: String) : EvaluationResult()
    data class SyntaxError(val message: String) : EvaluationResult()
}

enum class AngleUnit { DEGREES, RADIANS, GRADIANS }
```

### 2. Programmer Engine Contract (`com.calculator.engine.programmer.ProgrammerEngine`)
```kotlin
interface ProgrammerEngine {
    val activeValue: ULong
    val activeWordSize: WordSize

    fun setValue(value: ULong)
    fun setWordSize(wordSize: WordSize)
    fun setBase(base: NumberBase)
    fun formatAllBases(): BaseRepresentations
    fun executeBitwise(op: BitwiseOp, operand: ULong? = null)
    fun shift(type: ShiftType, bits: Int = 1)
    fun toggleBit(bitIndex: Int)
}

enum class NumberBase { HEX, DEC, OCT, BIN }
enum class WordSize(val bits: Int, val mask: ULong) {
    QWORD(64, 0xFFFFFFFFFFFFFFFFu),
    DWORD(32, 0xFFFFFFFFu),
    WORD(16, 0xFFFFu),
    BYTE(8, 0xFFu)
}
```

### 3. Skin Theme Contract (`com.calculator.ui.theme`)
```kotlin
data class SkinColors(
    val background: Color,
    val surface: Color,
    val displayBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val buttonNormal: Color,
    val buttonOperator: Color,
    val buttonAction: Color,
    val buttonAccent: Color,
    val border: Color,
    val glow: Color
)

data class SkinTypography(
    val displayExpression: TextStyle,
    val displayResult: TextStyle,
    val buttonText: TextStyle,
    val programmerBaseLabel: TextStyle,
    val programmerBaseValue: TextStyle
)

data class SkinShapes(
    val buttonCornerRadius: Dp,
    val buttonBorderWidth: Dp,
    val displayCornerRadius: Dp,
    val displayBorderWidth: Dp
)

enum class SkinType { RETRO_PIXEL, MODERN, FUTURISTIC }
```

### 4. Platform Feedback Contract (`com.calculator.platform`)
```kotlin
interface HapticService {
    fun performClick(skin: SkinType)
    fun performError(skin: SkinType)
}

interface AudioService {
    fun playButtonClick(skin: SkinType)
    fun release()
}
```

### 5. Persistence Contract (`com.calculator.data`)
```kotlin
interface PreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setSkin(skin: SkinType)
    suspend fun setCalculatorMode(mode: CalculatorMode)
    suspend fun setAngleUnit(unit: AngleUnit)
    suspend fun setWordSize(wordSize: WordSize)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setSoundEnabled(enabled: Boolean)
}

interface HistoryRepository {
    val historyTape: Flow<List<HistoryItem>>
    suspend fun addRecord(expression: String, result: String, mode: CalculatorMode)
    suspend fun deleteRecord(id: Long)
    suspend fun clearHistory()
}
```

---

## Code Layout
```
c:\work\Repositories\Calculator\
├── app\
│   ├── src\
│   │   ├── main\
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java\com\calculator\
│   │   │   │   ├── CalculatorApplication.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── engine\
│   │   │   │   │   ├── CalculatorEngine.kt
│   │   │   │   │   ├── CalculatorException.kt
│   │   │   │   │   ├── core\
│   │   │   │   │   │   ├── Token.kt
│   │   │   │   │   │   ├── Lexer.kt
│   │   │   │   │   │   ├── ShuntingYard.kt
│   │   │   │   │   │   ├── Evaluator.kt
│   │   │   │   │   │   └── Precision.kt
│   │   │   │   │   ├── normal\
│   │   │   │   │   │   ├── NormalCalculatorEngine.kt
│   │   │   │   │   │   └── MemoryRegister.kt
│   │   │   │   │   ├── scientific\
│   │   │   │   │   │   ├── ScientificCalculatorEngine.kt
│   │   │   │   │   │   └── ExactTrigTables.kt
│   │   │   │   │   └── programmer\
│   │   │   │   │       ├── ProgrammerEngine.kt
│   │   │   │   │       └── ProgrammerEngineImpl.kt
│   │   │   │   ├── ui\
│   │   │   │   │   ├── CalculatorViewModel.kt
│   │   │   │   │   ├── CalculatorUiState.kt
│   │   │   │   │   ├── theme\
│   │   │   │   │   │   ├── SkinTheme.kt
│   │   │   │   │   │   ├── RetroPixelSkin.kt
│   │   │   │   │   │   ├── ModernSkin.kt
│   │   │   │   │   │   └── FuturisticSkin.kt
│   │   │   │   │   ├── components\
│   │   │   │   │   │   ├── CalculatorDisplay.kt
│   │   │   │   │   │   ├── CalculatorKeypad.kt
│   │   │   │   │   │   ├── BitMatrixKeypad.kt
│   │   │   │   │   │   ├── HistoryDrawer.kt
│   │   │   │   │   │   └── ModeSelector.kt
│   │   │   │   │   └── layouts\
│   │   │   │   │       ├── PhonePortraitLayout.kt
│   │   │   │   │       ├── PhoneLandscapeLayout.kt
│   │   │   │   │       └── TabletWorkstationLayout.kt
│   │   │   │   ├── platform\
│   │   │   │   │   ├── audio\
│   │   │   │   │   │   └── ProceduralAudioService.kt
│   │   │   │   │   └── haptics\
│   │   │   │   │       └── SkinHapticService.kt
│   │   │   │   └── data\
│   │   │   │       ├── preferences\
│   │   │   │       │   └── DataStorePreferencesRepository.kt
│   │   │   │       └── history\
│   │   │   │           └── LocalHistoryRepository.kt
│   │   │   └── res\
│   │   │       ├── drawable\
│   │   │       ├── mipmap-anydpi-v26\
│   │   │       ├── values\
│   │   │       └── xml\
│   │   └── test\java\com\calculator\
│   │       ├── engine\
│   │       │   ├── NormalCalculatorEngineTest.kt
│   │       │   ├── ScientificCalculatorEngineTest.kt
│   │       │   ├── ProgrammerEngineTest.kt
│   │       │   └── PrecisionAndEdgeCasesTest.kt
│   │       └── ui\
│   │           ├── SkinSwitchingTest.kt
│   │           ├── ModeSwitchingTest.kt
│   │           └── StatePersistenceRobolectricTest.kt
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle\
│   ├── libs.versions.toml
│   └── wrapper\
├── play_store\
│   ├── short_description.txt
│   ├── full_description.txt
│   ├── privacy_policy.md
│   ├── privacy_policy.html
│   ├── data_safety.md
│   ├── release_checklist.md
│   ├── signing_instructions.md
│   └── graphics\
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── ORIGINAL_REQUEST.md
└── PROJECT.md
```
