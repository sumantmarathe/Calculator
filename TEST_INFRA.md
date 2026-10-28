# E2E Test Infra: Native Android Multi-Mode Calculator

## Test Philosophy
- Opaque-box, requirement-driven derived strictly from `ORIGINAL_REQUEST.md`.
- Methodology: Category-Partition + Boundary Value Analysis (BVA) + Pairwise Combinatorial Testing + Real-World Workload Testing.
- Zero reliance on internal private methods or implementation quirks; testing strictly through public domain interfaces, public ViewModel APIs, and Compose semantics.

---

## Feature Inventory & Test Coverage Matrix
| # | Feature | Requirement Source | Tier 1 (Happy) | Tier 2 (Boundaries) | Tier 3 (Cross-Feature) | Tier 4 (Real-World) |
|---|---------|--------------------|:--------------:|:-------------------:|:----------------------:|:-------------------:|
| 1 | Basic Arithmetic (+, -, *, /) | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 2 | Operator Precedence (PEMDAS) | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 3 | Exact Decimal Precision (0.1+0.2=0.3) | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 4 | Percentage Logic (4 models) | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 5 | Memory Operations (M+, M-, MR, MC) | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 6 | Scientific Trig (DEG/RAD/GRAD) | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 7 | Hyperbolic & Inverse Functions | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 8 | Powers, Roots & Logarithms | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 9 | Factorials & Constants (pi, e) | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 10 | Domain Error Recovery (1/0, log 0) | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 11 | Programmer Multi-Base Live Sync | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 12 | Programmer Word Sizes & Masking | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 13 | Bitwise Logic (AND, OR, XOR, etc.) | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 14 | Bit Shifts & Rotations | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 15 | Interactive Bit Keypad Toggling | R1 / Acceptance | 5 | 5 | ✓ | ✓ |
| 16 | Retro Pixel B/W Visual Skin | R2 / Acceptance | 5 | 5 | ✓ | ✓ |
| 17 | Modern Minimalist Visual Skin | R2 / Acceptance | 5 | 5 | ✓ | ✓ |
| 18 | Futuristic Cyber HUD Visual Skin | R2 / Acceptance | 5 | 5 | ✓ | ✓ |
| 19 | Responsive Layouts (Portrait/Landscape/Tablet) | R2 / Acceptance | 5 | 5 | ✓ | ✓ |
| 20 | Local History Tape & Recall | R2 / Acceptance | 5 | 5 | ✓ | ✓ |
| 21 | Skin-Aware Haptics & Sound Toggles | R2 / Acceptance | 5 | 5 | ✓ | ✓ |
| 22 | Offline Zero-Network Architecture | R3 / Acceptance | 5 | 5 | ✓ | ✓ |
| 23 | Google Play Readiness & Asset Validation | R4 / Acceptance | 5 | 5 | ✓ | ✓ |

---

## Test Architecture
- **Test Runners**:
  - `testDebugUnitTest`: Gradle command executing all JVM unit tests and headless Robolectric Compose UI tests.
  - Test assertions: JUnit 4 + Google Truth assertions.
- **Directory Layout**:
  - `app/src/test/java/com/calculator/engine/`: Arithmetic, Scientific, Programmer, Precision & Domain Error tests.
  - `app/src/test/java/com/calculator/ui/`: Compose UI semantics, Skin switching, Mode transitions, History tape.
  - `app/src/test/java/com/calculator/privacy/`: Manifest permission absence, build script dependency audit.

---

## Real-World Application Scenarios (Tier 4)
| # | Scenario | Features Exercised | Complexity |
|---|----------|--------------------|------------|
| 1 | Financial Mortgage & Compound Interest | Multi-parentheses, power, division, high decimal precision | High |
| 2 | Physics Orbital Velocity & Kinetic Energy | Constants pi, square roots, scientific notation, powers | High |
| 3 | Embedded Systems Register Mask & Bit Shift | Programmer mode, HEX/BIN conversion, bitwise AND/OR, shift | High |
| 4 | Civil Engineering Triangle Incline Calculation | DEG angle mode, inverse trigonometry, square root, memory keys | Medium |
| 5 | Cross-Device Theme & History Workflow | Skin switching, landscape rotation, calculation recall from tape | Medium |

---

## Coverage Thresholds
- **Tier 1 (Feature Coverage)**: ≥ 115 test cases (≥5 per feature)
- **Tier 2 (Boundary & Corner Cases)**: ≥ 115 test cases (covering 0-division, IEEE-754 noise, wraparounds, asymptotes)
- **Tier 3 (Cross-Feature Combinations)**: ≥ 30 pairwise interaction tests
- **Tier 4 (Real-World Application Scenarios)**: ≥ 5 comprehensive end-to-end workflows
- **Total Minimum Test Count**: ≥ 265 test cases / assertions with 100% pass rate.
