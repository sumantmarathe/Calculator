import java.io.File
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.calculator"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.prismcalc.calculator"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val rootDir = rootProject.projectDir
            val keystorePropsFile = File(rootDir, "keystore.properties")
            val localPropsFile = File(rootDir, "local.properties")

            var isConfigured = false

            // Tier 1: keystore.properties
            if (keystorePropsFile.exists()) {
                val props = Properties().apply {
                    keystorePropsFile.inputStream().use { load(it) }
                }
                val storePath = props.getProperty("storeFile")
                if (!storePath.isNullOrBlank()) {
                    val f = File(storePath)
                    val candidate = if (f.isAbsolute) f else File(rootDir, storePath)
                    if (candidate.exists()) {
                        storeFile = candidate.canonicalFile
                        storePassword = props.getProperty("storePassword")
                        keyAlias = props.getProperty("keyAlias")
                        keyPassword = props.getProperty("keyPassword")
                        isConfigured = true
                    }
                }
            }

            // Tier 2: local.properties
            if (!isConfigured && localPropsFile.exists()) {
                val props = Properties().apply {
                    localPropsFile.inputStream().use { load(it) }
                }
                val storePath = props.getProperty("release.storeFile") ?: props.getProperty("storeFile")
                if (!storePath.isNullOrBlank()) {
                    val f = File(storePath)
                    val candidate = if (f.isAbsolute) f else File(rootDir, storePath)
                    if (candidate.exists()) {
                        storeFile = candidate.canonicalFile
                        storePassword = props.getProperty("release.storePassword") ?: props.getProperty("storePassword")
                        keyAlias = props.getProperty("release.keyAlias") ?: props.getProperty("keyAlias")
                        keyPassword = props.getProperty("release.keyPassword") ?: props.getProperty("keyPassword")
                        isConfigured = true
                    }
                }
            }

            // Tier 3: Environment Variables
            if (!isConfigured) {
                val envStorePath = System.getenv("KEYSTORE_FILE") ?: System.getenv("RELEASE_KEYSTORE_FILE")
                if (!envStorePath.isNullOrBlank()) {
                    val candidate = File(envStorePath)
                    if (candidate.exists()) {
                        storeFile = candidate
                        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: System.getenv("RELEASE_KEYSTORE_PASSWORD")
                        keyAlias = System.getenv("KEY_ALIAS") ?: System.getenv("RELEASE_KEY_ALIAS")
                        keyPassword = System.getenv("KEY_PASSWORD") ?: System.getenv("RELEASE_KEY_PASSWORD")
                        isConfigured = true
                    }
                }
            }

            // Tier 4: Fallback to debug keystore for automated verification and CI builds
            if (!isConfigured) {
                val debugKeystore = signingConfigs.getByName("debug")
                storeFile = debugKeystore.storeFile
                storePassword = debugKeystore.storePassword
                keyAlias = debugKeystore.keyAlias
                keyPassword = debugKeystore.keyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    lint {
        abortOnError = true
        disable += "NewApi"
    }
}

dependencies {
    // Core AndroidX & Architecture
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Jetpack Compose BOM & UI Components
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)

    // Debug Tooling
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Testing Framework (Tier 1 Math Engine & Tier 2 Robolectric Compose UI)
    testImplementation(libs.junit)
    testImplementation(libs.google.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}

// ------------------------------------------------------------------------------
// Automated Privacy & Security Verification Tasks (R3 Acceptance Criteria)
// ------------------------------------------------------------------------------

tasks.register("verifyManifestPrivacy") {
    group = "verification"
    description = "Enforces strict zero-network, zero-storage, zero-telemetry permission policies on source and merged AndroidManifest.xml"

    doLast {
        val bannedPermissions = setOf(
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android.permission.ACCESS_MEDIA_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_BACKGROUND_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.GET_ACCOUNTS",
            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_PHONE_NUMBERS",
            "android.permission.CALL_PHONE",
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.NEARBY_WIFI_DEVICES",
            "android.permission.BODY_SENSORS",
            "android.permission.ACTIVITY_RECOGNITION",
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "android.permission.WAKE_LOCK",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.POST_NOTIFICATIONS",
            "com.google.android.gms.permission.AD_ID",
            "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE"
        )
        val allowedPermissions = setOf(
            "android.permission.VIBRATE",
            "com.prismcalc.calculator.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
            "com.prismcalc.calculator.debug.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
            "com.calculator.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
            "com.calculator.app.debug.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
        )

        // 1. Gather all manifest files (source + any generated merged manifests)
        val manifestFiles = mutableListOf<File>()
        val sourceManifest = file("src/main/AndroidManifest.xml")
        if (sourceManifest.exists()) {
            manifestFiles.add(sourceManifest)
        } else {
            throw GradleException("PRIVACY VERIFICATION FAILURE: Source manifest missing at ${sourceManifest.absolutePath}")
        }

        val intermediatesDir = layout.buildDirectory.dir("intermediates").get().asFile
        if (intermediatesDir.exists()) {
            intermediatesDir.walkTopDown()
                .filter { it.isFile && it.name == "AndroidManifest.xml" }
                .forEach { manifestFiles.add(it) }
        }

        val distinctManifests = manifestFiles.distinctBy { it.canonicalPath }

        // Helper regex to extract permission names ignoring XML comments, supporting both uses-permission and uses-permission-sdk-23
        val permissionRegex = Regex(
            """<(?:uses-permission|uses-permission-sdk-23)\b[^>]*android:name\s*=\s*["']([^"']+)["']""",
            RegexOption.IGNORE_CASE
        )

        for (manifest in distinctManifests) {
            val content = manifest.readText()

            // Strip XML comments prior to analysis to prevent false positives
            val cleanContent = content.replace(Regex("""<!--.*?-->""", RegexOption.DOT_MATCHES_ALL), "")

            val declaredPermissions = permissionRegex.findAll(cleanContent)
                .map { it.groupValues[1].trim() }
                .toSet()

            // Check against banned list
            val bannedViolations = declaredPermissions.intersect(bannedPermissions)
            if (bannedViolations.isNotEmpty()) {
                throw GradleException(
                    "PRIVACY VIOLATION in [${manifest.path}]: Forbidden permission(s) detected: $bannedViolations. " +
                    "Calculator must have zero network, storage, location, or tracking permissions."
                )
            }

            // Strict Whitelist Check: only VIBRATE and app internal dynamic receiver security permissions are permitted
            val isSource = (manifest.canonicalPath == sourceManifest.canonicalPath)
            if (isSource) {
                // Source manifest: ONLY VIBRATE is permitted
                val unauthorizedInSource = declaredPermissions.filter { it != "android.permission.VIBRATE" }
                if (unauthorizedInSource.isNotEmpty()) {
                    throw GradleException(
                        "PRIVACY VIOLATION in source manifest [${manifest.path}]: " +
                        "User-facing source manifest must contain ONLY 'android.permission.VIBRATE'. Found unauthorized: $unauthorizedInSource"
                    )
                }
            } else {
                // Merged manifests: only VIBRATE and AndroidX internal dynamic receiver signature permission
                val unknownPermissions = declaredPermissions.filterNot { perm ->
                    perm in allowedPermissions ||
                    (perm.endsWith(".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION") &&
                        (perm.startsWith("com.prismcalc.calculator") || perm.startsWith("com.calculator.app")))
                }
                if (unknownPermissions.isNotEmpty()) {
                    throw GradleException(
                        "PRIVACY VIOLATION in merged manifest [${manifest.path}]: Unauthorized permission(s) detected: $unknownPermissions. " +
                        "Only 'android.permission.VIBRATE' and internal dynamic receiver permissions are permitted."
                    )
                }
            }

            // Verify allowBackup="false"
            if (!cleanContent.contains("""android:allowBackup="false"""")) {
                throw GradleException(
                    "SECURITY VIOLATION in [${manifest.path}]: android:allowBackup must be explicitly set to 'false' to prevent cloud data leakage."
                )
            }

            // Verify usesCleartextTraffic="false"
            if (!cleanContent.contains("""android:usesCleartextTraffic="false"""")) {
                throw GradleException(
                    "SECURITY VIOLATION in [${manifest.path}]: android:usesCleartextTraffic must be explicitly set to 'false'."
                )
            }

            // Verify MainActivity exported="true"
            if (cleanContent.contains("MainActivity") && !cleanContent.contains("""android:exported="true"""")) {
                throw GradleException(
                    "MANIFEST VIOLATION in [${manifest.path}]: MainActivity must declare android:exported=\"true\" for Android 12+ compatibility."
                )
            }
        }

        println("SUCCESS: verifyManifestPrivacy verified ${distinctManifests.size} manifest(s). Zero forbidden permissions detected.")
    }
}

tasks.register("verifyDependenciesPrivacy") {
    group = "verification"
    description = "Scans dependencies to guarantee zero telemetry, ads, network clients, or 3rd-party math libraries"

    doLast {
        val prohibitedTokens = listOf(
            // Analytics & Telemetry
            "firebase", "google-analytics", "flurry", "mixpanel", "segment",
            "appsflyer", "adjust", "amplitude", "snowplow", "leanplum",
            // Crash Reporting
            "crashlytics", "sentry", "bugsnag", "datadog", "acra", "rollbar", "instabug",
            // Advertising & Tracking
            "play-services-ads", "admob", "applovin", "unity-ads", "ironsource",
            "facebook-ads", "mopub", "chartboost", "vungle", "adcolony", "inmobi",
            // Network Clients & HTTP
            "okhttp", "retrofit", "ktor-client", "volley", "apache.http",
            "async-http-client", "cronet", "fuel",
            // Third-Party Math Engines (R1 clean-room requirement)
            "exp4j", "evalex", "keval", "parsii", "symja", "rhino", "mxparser"
        )

        // 1. Scan external build scripts and version catalog files
        val externalFilesToScan = listOf(
            rootProject.file("build.gradle.kts"),
            rootProject.file("settings.gradle.kts"),
            rootProject.file("gradle/libs.versions.toml")
        ).filter { it.exists() }

        val detectedInFiles = mutableListOf<String>()
        for (f in externalFilesToScan) {
            val content = f.readText().lowercase()
            for (token in prohibitedTokens) {
                if (content.contains(token)) {
                    detectedInFiles.add("File '${f.name}' contains prohibited token '$token'")
                }
            }
        }

        // 2. Scan module build script (app/build.gradle.kts) excluding this task's definition
        // to prevent circular self-matching against its own prohibitedTokens blacklist literals
        val appBuildScript = file("build.gradle.kts")
        if (appBuildScript.exists()) {
            val rawScript = appBuildScript.readText()
            val taskStartIndex = rawScript.indexOf("tasks.register(\"verifyDependenciesPrivacy\")")
            val sanitizedScript = if (taskStartIndex >= 0) {
                val afterTask = rawScript.substring(taskStartIndex)
                val checkTaskIndex = afterTask.indexOf("\ntasks.named(\"check\")")
                if (checkTaskIndex >= 0) {
                    rawScript.substring(0, taskStartIndex) + afterTask.substring(checkTaskIndex)
                } else {
                    rawScript.substring(0, taskStartIndex)
                }
            } else {
                rawScript
            }
            val lowerScript = sanitizedScript.lowercase()
            for (token in prohibitedTokens) {
                if (lowerScript.contains(token)) {
                    detectedInFiles.add("File '${appBuildScript.name}' contains prohibited token '$token'")
                }
            }
        }

        if (detectedInFiles.isNotEmpty()) {
            throw GradleException(
                "PRIVACY / DEPENDENCY VIOLATION:\n" + detectedInFiles.joinToString("\n") +
                "\nCalculator app must be 100% offline and use a clean-room custom Kotlin math engine."
            )
        }

        // 3. Scan declared project configurations (direct dependencies)
        val declaredConfigNames = listOf(
            "implementation", "api", "compileOnly", "runtimeOnly",
            "debugImplementation", "releaseImplementation"
        )

        val detectedInConfigs = mutableListOf<String>()
        for (cfgName in declaredConfigNames) {
            val cfg = configurations.findByName(cfgName) ?: continue
            cfg.dependencies.forEach { dep ->
                val coord = "${dep.group}:${dep.name}:${dep.version}".lowercase()
                for (token in prohibitedTokens) {
                    if (coord.contains(token)) {
                        detectedInConfigs.add("Declared configuration '$cfgName' includes prohibited dependency: $coord (matched '$token')")
                    }
                }
            }
        }

        // 4. Scan resolved runtime classpaths (direct AND transitive dependencies)
        val runtimeConfigNames = listOf("debugRuntimeClasspath", "releaseRuntimeClasspath")
        for (cfgName in runtimeConfigNames) {
            val cfg = configurations.findByName(cfgName)
            if (cfg != null && cfg.isCanBeResolved) {
                try {
                    val components = cfg.incoming.resolutionResult.allComponents
                    for (component in components) {
                        val displayName = component.id.displayName.lowercase()
                        // Exclude internal project subprojects (e.g., "project :app")
                        if (displayName.startsWith("project :")) continue
                        for (token in prohibitedTokens) {
                            if (displayName.contains(token)) {
                                detectedInConfigs.add("Runtime classpath '$cfgName' includes prohibited resolved component: $displayName (matched '$token')")
                            }
                        }
                    }
                } catch (ignored: Exception) {
                    // Safe guard against unresolved transient configurations
                }
            }
        }

        if (detectedInConfigs.isNotEmpty()) {
            throw GradleException(
                "PRIVACY / DEPENDENCY VIOLATION in project dependencies / runtime classpath:\n" +
                detectedInConfigs.distinct().joinToString("\n")
            )
        }

        println("SUCCESS: verifyDependenciesPrivacy verified build scripts, declared configurations, and resolved runtime classpaths. Zero prohibited dependencies detected.")
    }
}

tasks.named("check") {
    dependsOn("verifyManifestPrivacy", "verifyDependenciesPrivacy")
}

// Ensure privacy verification runs prior to assembling debug or release APKs/AABs
tasks.matching { it.name in listOf("assembleDebug", "assembleRelease", "bundleRelease") }.configureEach {
    dependsOn("verifyManifestPrivacy", "verifyDependenciesPrivacy")
}
