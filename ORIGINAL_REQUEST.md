# Original User Request

## Initial Request — 2026-10-28T03:06:49Z

Build a production-grade, Google Play-ready native Android calculator app in Kotlin and Jetpack Compose featuring Normal, Scientific, and Programmer modes, multiple customizable visual skins (including Retro Pixel B/W, Modern, and Futuristic) with skin-aware haptics and optional sound effects, local calculation history and memory operations, responsive portrait/landscape/tablet layouts, a strictly offline zero-data-collection privacy architecture with no sign-in required, and a complete Google Play Store publishing and compliance bundle.

Working directory: c:\work\Repositories\Calculator
Integrity mode: demo

## Requirements

### R1. Multi-Mode Custom Calculator Engine & Original Implementation
Provide three full-featured calculation modes—**Normal** (standard arithmetic, percentages, basic operations, memory keys `M+`, `M-`, `MR`, `MC`), **Scientific** (trigonometric functions with DEG/RAD/GRAD toggle, inverse/hyperbolic trig, logarithmic, exponential, roots, factorials, modulo, constants like $\pi$ and $e$, nested parentheses, and full operator precedence), and **Programmer** (`HEX`, `DEC`, `OCT`, `BIN` live base conversions, selectable word sizes `QWORD` 64-bit / `DWORD` 32-bit / `WORD` 16-bit / `BYTE` 8-bit, signed/unsigned two's complement handling, bitwise operations `AND`, `OR`, `XOR`, `NOT`, `NAND`, `NOR`, bit shifts `LSH`, `RSH`, rotate, and interactive bit-toggling keypad).
All core calculation, expression parsing, arbitrary/high-precision decimal handling, and bitwise evaluation logic must be written from scratch in Kotlin without relying on third-party math-evaluation libraries or directly copying external open-source calculator code; handle edge cases (division by zero, domain errors, overflow, floating-point representation precision) gracefully with clear, user-friendly error states and live expression preview.

### R2. Customizable Visual Skins, Smooth UX, and Responsive Layouts
Build the UI using native Android Kotlin and Jetpack Compose with smooth transitions and animations between modes and skins. Support multiple distinct, switchable visual skins that comprehensively transform typography, button geometry, surface treatments, color palettes, and display aesthetics—including at minimum:
1. **Retro Pixel B/W Skin** (crisp 1-bit/monochrome pixel-art aesthetic, segmented/dot-matrix display styling, tactile retro button borders).
2. **Modern Skin** (clean, sleek contemporary Material/minimalist aesthetic with subtle elevation and refined typography).
3. **Futuristic Skin** (cyber/sci-fi HUD aesthetic with neon accents, glowing borders, and angular/tech surface styling).
Include a persistent local calculation history tape (with ability to recall previous expressions/results into the active display and clear history), tactile haptic feedback and optional synthesized/procedural button sound effects (with user toggles in settings), and responsive layouts that adapt seamlessly across portrait phones, landscape mode, and tablets/foldables. All user preferences (selected skin, active mode, angle unit, word size, haptics/sound toggles, and history) must persist locally on-device across app restarts.

### R3. Strict Privacy, Offline-Only Architecture & Zero Telemetry
The application must strictly limit its usage to calculator functionality on the device. It must require **zero network, contacts, location, storage, or identity permissions** in `AndroidManifest.xml` (only optional `VIBRATE` if needed for haptics), include zero analytics/telemetry/advertising SDKs, require no sign-in or login flows, and never transmit or read any external user data from the device.

### R4. Google Play Store Readiness & Publishing Bundle
Configure the Android project so it is immediately ready for Google Play production release:
- Proper Gradle configuration (`applicationId`, semantic `versionCode`/`versionName`, target/compile SDK configured for current Google Play requirements, R8/ProGuard minification and resource shrinking rules configured for release builds, and signing config scaffolding).
- Adaptive launcher icons (foreground/background vector drawables) and splash screen support.
- A dedicated `play_store/` directory containing ready-to-publish store listing assets and compliance documentation: short description (<= 80 chars), full description (<= 4000 chars), Privacy Policy document (Markdown and standalone HTML), Data Safety declaration summary confirming zero data collection/sharing, release checklist, and generated high-resolution SVG/PNG icon and feature graphic assets.

## Acceptance Criteria

### Build & Manifest Privacy Verification
- [ ] The project compiles cleanly via Gradle (`assembleDebug`, `assembleRelease` or unsigned release bundle/APK, and `lint`) with zero compilation errors.
- [ ] Automated inspection of the merged `AndroidManifest.xml` confirms that `android.permission.INTERNET`, `ACCESS_NETWORK_STATE`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, and all location/contacts/account permissions are completely absent.
- [ ] Automated inspection of `build.gradle.kts` dependencies confirms no third-party math parser libraries, network clients (Retrofit/OkHttp), or analytics/crash-reporting SDKs (Firebase, Play Services Ads/Analytics) are included.

### Engine Accuracy & Edge-Case Unit Tests
- [ ] A comprehensive JUnit test suite (`testDebugUnitTest`) passes 100% of tests with at least 100 test assertions covering:
  - Normal mode arithmetic, operator precedence (PEMDAS/BODMAS), chained operations, percentages, and `M+`/`M-`/`MR`/`MC` memory state.
  - Decimal precision checks (e.g., `0.1 + 0.2 == 0.3` exact representation without `0.30000000000000004` floating-point artifacts).
  - Scientific mode functions (`sin`, `cos`, `tan`, `asin`, `acos`, `atan`, `sinh`, `cosh`, `tanh`, `ln`, `log10`, `sqrt`, `cbrt`, `x^y`, `n!`, $\pi$, $e$) across both `DEG` and `RAD` modes, nested parentheses, and domain error recovery (e.g., `1/0`, `sqrt(-1)`, `log(0)`).
  - Programmer mode conversions across `HEX`, `DEC`, `OCT`, and `BIN`, bitwise operations (`AND`, `OR`, `XOR`, `NOT`, `NAND`, `NOR`, `LSH`, `RSH`), individual bit toggling, and two's complement overflow/wraparound behavior across all 4 word sizes (`BYTE` 8-bit, `WORD` 16-bit, `DWORD` 32-bit, `QWORD` 64-bit).

### UI, Skin & State Persistence Verification
- [ ] Automated Compose UI / Robolectric / instrumentation tests verify mode switching (`Normal`, `Scientific`, `Programmer`), skin switching (`Retro Pixel B/W`, `Modern`, `Futuristic`, plus any additional skins), calculation history recording/recall/clearing, and local state persistence.
- [ ] Independent visual/code audit confirms that each skin (`Retro Pixel B/W`, `Modern`, `Futuristic`) applies distinct typography, color tokens, button shapes/borders, and display styling rather than a superficial single-color swap, and that both portrait and landscape/tablet layouts render without clipping or overlapping controls.

### Google Play Publishing Bundle Verification
- [ ] The `play_store/` directory exists and contains complete, valid Privacy Policy files (`.md` and `.html`), store listing copy (`short_description.txt` <= 80 characters, `full_description.txt` <= 4000 characters), Data Safety questionnaire guide, release signing instructions, and store graphic assets.
