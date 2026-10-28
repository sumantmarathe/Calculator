package com.calculator.privacy

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class PrivacyManifestTest {

    private val prohibitedPermissions = listOf(
        "android.permission.INTERNET",
        "android.permission.ACCESS_NETWORK_STATE",
        "android.permission.ACCESS_WIFI_STATE",
        "android.permission.CHANGE_WIFI_STATE",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.MANAGE_EXTERNAL_STORAGE",
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
        "android.permission.POST_NOTIFICATIONS",
        "com.google.android.gms.permission.AD_ID"
    )

    private fun findProjectRoot(): File {
        var dir = File(System.getProperty("user.dir") ?: ".")
        while (dir.parentFile != null) {
            if (File(dir, "settings.gradle.kts").exists() || File(dir, "ORIGINAL_REQUEST.md").exists()) {
                return dir
            }
            dir = dir.parentFile!!
        }
        return File(".")
    }

    @Test
    fun testManifestContainsZeroProhibitedPermissions() {
        val root = findProjectRoot()
        val manifestFile = File(root, "app/src/main/AndroidManifest.xml")
        assertThat(manifestFile.exists()).isTrue()

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(manifestFile)

        // Inspect both standard <uses-permission> and SDK-23+ conditional <uses-permission-sdk-23> tags
        val permissionTags = listOf("uses-permission", "uses-permission-sdk-23")
        val declaredPermissions = mutableListOf<String>()
        for (tag in permissionTags) {
            val permissionNodes = doc.getElementsByTagName(tag)
            for (i in 0 until permissionNodes.length) {
                val element = permissionNodes.item(i) as Element
                val permName = element.getAttribute("android:name")
                if (permName.isNotEmpty()) {
                    declaredPermissions.add(permName)
                }
            }
        }

        // Verify zero prohibited permissions
        for (prohibited in prohibitedPermissions) {
            assertThat(declaredPermissions).doesNotContain(prohibited)
        }

        // Whitelist: ONLY VIBRATE is allowed in source manifest
        for (perm in declaredPermissions) {
            assertThat(perm).isEqualTo("android.permission.VIBRATE")
        }

        // Verify zero custom <permission> definitions exist in the application manifest
        val customPermissionNodes = doc.getElementsByTagName("permission")
        assertThat(customPermissionNodes.length).isEqualTo(0)
    }

    @Test
    fun testManifestApplicationAttributesEnforcePrivacy() {
        val root = findProjectRoot()
        val manifestFile = File(root, "app/src/main/AndroidManifest.xml")
        assertThat(manifestFile.exists()).isTrue()

        val factory = DocumentBuilderFactory.newInstance()
        val doc = factory.newDocumentBuilder().parse(manifestFile)

        val appNodes = doc.getElementsByTagName("application")
        assertThat(appNodes.length).isEqualTo(1)
        val app = appNodes.item(0) as Element

        assertThat(app.getAttribute("android:allowBackup")).isEqualTo("false")
        assertThat(app.getAttribute("android:usesCleartextTraffic")).isEqualTo("false")
        assertThat(app.getAttribute("android:supportsRtl")).isEqualTo("true")

        val activityNodes = doc.getElementsByTagName("activity")
        assertThat(activityNodes.length).isAtLeast(1)
        val mainActivity = activityNodes.item(0) as Element
        assertThat(mainActivity.getAttribute("android:exported")).isEqualTo("true")
    }

    @Test
    fun testDependenciesContainZeroTelemetryOrNetworkLibraries() {
        val root = findProjectRoot()
        val tomlFile = File(root, "gradle/libs.versions.toml")
        // Strict existence guard: fail immediately if version catalog is missing or path unresolvable
        assertThat(tomlFile.exists()).isTrue()

        val bannedTokens = listOf(
            // Analytics & Telemetry
            "firebase", "google-analytics", "analytics", "flurry", "mixpanel", "segment",
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

        // 1. Verify version catalog
        val tomlContent = tomlFile.readText().lowercase()
        for (token in bannedTokens) {
            assertThat(tomlContent).doesNotContain(token)
        }

        // 2. Verify module build script (sanitized to exclude the verification task definition itself)
        val appBuildFile = File(root, "app/build.gradle.kts")
        assertThat(appBuildFile.exists()).isTrue()
        val buildContent = appBuildFile.readText()
        val taskStartIndex = buildContent.indexOf("tasks.register(\"verifyDependenciesPrivacy\")")
        val sanitizedContent = if (taskStartIndex >= 0) {
            val afterTask = buildContent.substring(taskStartIndex)
            val checkIndex = afterTask.indexOf("\ntasks.named(\"check\")")
            if (checkIndex >= 0) {
                buildContent.substring(0, taskStartIndex) + afterTask.substring(checkIndex)
            } else {
                buildContent.substring(0, taskStartIndex)
            }
        } else {
            buildContent
        }.lowercase()

        for (token in bannedTokens) {
            assertThat(sanitizedContent).doesNotContain(token)
        }
    }
}
