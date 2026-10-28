# ==============================================================================
# R8 / ProGuard Configuration — Native Android Multi-Mode Calculator
# Package namespace: com.calculator
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. R8 Full-Mode Optimization & General Directives
# ------------------------------------------------------------------------------
# Allow aggressive optimizations while preserving standard type safety
-repackageclasses ''
-allowaccessmodification

# Keep source file name and line number table for readable crash traces
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,InnerClasses,EnclosingMethod,Signature,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# ------------------------------------------------------------------------------
# 2. Kotlin Language, Coroutines & Flow
# ------------------------------------------------------------------------------
# Preserve Kotlin @Metadata for reflection and coroutine stack-frame construction
-keepclassmembers class * {
    @kotlin.jvm.JvmField *;
}

# Preserve standard Enum methods used across calculations and UI state
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Ignore benign compile-time warnings in Kotlin Coroutines
-dontwarn kotlinx.coroutines.**
-dontwarn kotlin.jvm.**

# ------------------------------------------------------------------------------
# 3. Jetpack Compose UI & Runtime
# ------------------------------------------------------------------------------
# Preserve Compose View and internal platform hooks
-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView {
    *;
}

# Suppress benign warnings from Compose tooling and compiler-generated bridges
-dontwarn androidx.compose.**

# ------------------------------------------------------------------------------
# 4. AndroidX DataStore Preferences
# ------------------------------------------------------------------------------
# Preserve DataStore preferences keys and serialization
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences {
    *;
}
-dontwarn androidx.datastore.**

# ------------------------------------------------------------------------------
# 5. Domain Calculator Engine & State Models
# ------------------------------------------------------------------------------
# Ensure domain models, tokens, and evaluation results are never stripped
-keep class com.calculator.engine.** { *; }

# Preserve UI State, ViewModels, and Skin Theme Tokens
-keep class com.calculator.ui.** { *; }

# Preserve Data Persistence Entities and History records
-keep class com.calculator.data.** { *; }

# Preserve Hardware & Platform Feedback Services (Audio & Haptics)
-keep class com.calculator.platform.** { *; }
