# ==============================================================================
# verify_privacy.ps1 - Native Android Multi-Mode Calculator Privacy Auditor
# ==============================================================================
param(
    [string]$WorkspaceRoot = $PSScriptRoot
)

$ErrorActionPreference = "Stop"
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Running Strict Privacy & Zero-Permission Audit           " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$ManifestPath = Join-Path $WorkspaceRoot "app\src\main\AndroidManifest.xml"
$TomlPath = Join-Path $WorkspaceRoot "gradle\libs.versions.toml"
$AppBuildPath = Join-Path $WorkspaceRoot "app\build.gradle.kts"

$Violations = @()

# 1. Audit AndroidManifest.xml
if (-not (Test-Path $ManifestPath)) {
    Write-Error "AndroidManifest.xml not found at $ManifestPath"
}

$ManifestContent = Get-Content -Path $ManifestPath -Raw

# Remove XML comments
$CleanManifest = [regex]::Replace($ManifestContent, "<!--.*?-->", "", [System.Text.RegularExpressions.RegexOptions]::Singleline)

$BannedPermissions = @(
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.ACCESS_WIFI_STATE",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.WRITE_EXTERNAL_STORAGE",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.READ_CONTACTS",
    "android.permission.GET_ACCOUNTS",
    "android.permission.READ_PHONE_STATE",
    "android.permission.CAMERA",
    "android.permission.RECORD_AUDIO",
    "android.permission.POST_NOTIFICATIONS",
    "com.google.android.gms.permission.AD_ID"
)

foreach ($Perm in $BannedPermissions) {
    if ($CleanManifest -match [regex]::Escape($Perm)) {
        $Violations += "Manifest contains prohibited permission: $Perm"
    }
}

if ($CleanManifest -notmatch 'android:allowBackup="false"') {
    $Violations += "Manifest must explicitly set android:allowBackup=`"false`""
}

if ($CleanManifest -notmatch 'android:usesCleartextTraffic="false"') {
    $Violations += "Manifest must explicitly set android:usesCleartextTraffic=`"false`""
}

if ($CleanManifest -notmatch 'android:exported="true"') {
    $Violations += "MainActivity must declare android:exported=`"true`""
}

# 2. Audit Dependencies in libs.versions.toml
if (-not (Test-Path $TomlPath)) {
    $Violations += "Version catalog libs.versions.toml not found at $TomlPath"
} else {
    $TomlContent = (Get-Content -Path $TomlPath -Raw).ToLower()
    $BannedTokens = @(
        "firebase", "google-analytics", "analytics", "flurry", "mixpanel", "segment",
        "appsflyer", "adjust", "amplitude", "snowplow", "leanplum",
        "crashlytics", "sentry", "bugsnag", "datadog", "acra", "rollbar", "instabug",
        "play-services-ads", "admob", "applovin", "unity-ads", "ironsource",
        "facebook-ads", "mopub", "chartboost", "vungle", "adcolony", "inmobi",
        "okhttp", "retrofit", "ktor-client", "volley", "apache.http",
        "async-http-client", "cronet", "fuel",
        "exp4j", "evalex", "keval", "parsii", "symja", "rhino", "mxparser"
    )
    foreach ($Token in $BannedTokens) {
        if ($TomlContent.Contains($Token)) {
            $Violations += "libs.versions.toml contains prohibited token: $Token"
        }
    }
}

# 3. Report Results
if ($Violations.Count -gt 0) {
    Write-Host "`n[FAIL] Privacy Violations Detected:" -ForegroundColor Red
    foreach ($V in $Violations) {
        Write-Host "  - $V" -ForegroundColor Red
    }
    exit 1
} else {
    Write-Host "`n[PASS] All privacy policies verified successfully!" -ForegroundColor Green
    Write-Host "  - 0 Network / Storage / Tracking permissions." -ForegroundColor Green
    Write-Host "  - allowBackup=`"false`" enforced." -ForegroundColor Green
    Write-Host "  - 0 Telemetry / Ad / Network SDKs detected." -ForegroundColor Green
    exit 0
}
