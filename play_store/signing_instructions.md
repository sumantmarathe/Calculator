# Release Signing & Google Play Upload Instructions

This project (`com.prismcalc.calculator`) is pre-configured in `app/build.gradle.kts` with a 4-tier release signing configuration and R8 full-mode minification (`isMinifyEnabled = true`, `isShrinkResources = true`).

## 1. Generate Your Production Upload Keystore
Run the following `keytool` command (bundled with your JDK / Android Studio) outside of the Git repository or in a secure directory:

```powershell
keytool -genkey -v -keystore release-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

## 2. Configure `keystore.properties` (Recommended)
Create a file named `keystore.properties` in the root directory `c:\work\Repositories\Calculator\keystore.properties` (never commit this file to public version control):

```properties
storeFile=release-upload-key.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=upload
keyPassword=YOUR_KEY_PASSWORD
```

*(Alternatively, you can pass environment variables `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` in CI/CD).*

## 3. Build the Signed Release App Bundle (AAB) or APK
To build the production Android App Bundle (`.aab`) for Google Play Console upload:

```powershell
.\gradlew.bat :app:bundleRelease
```
Output location:
`app\build\outputs\bundle\release\app-release.aab`

To build the minified, shrinked Release APK:

```powershell
.\gradlew.bat :app:assembleRelease
```
Output location:
`app\build\outputs\apk\release\app-release.apk`

## 4. Upload R8 Deobfuscation Mapping File
When uploading `app-release.aab` to Google Play Console, attach or verify the generated R8 mapping file so crash stack traces in Play Console are deobfuscated:
`app\build\outputs\mapping\release\mapping.txt`
