# Google Play Console — Data Safety Questionnaire Guide

Use this exact declaration when completing the **App Content > Data Safety** form in Google Play Console for `com.prismcalc.calculator`.

## 1. Data Collection and Security
| Google Play Question | Required Response | Rationale |
|---|---|---|
| **Does your app collect or share any of the required user data types?** | **No** | The app has zero network permissions (`android.permission.INTERNET` is absent), zero analytics/advertising SDKs, and all calculation history/preferences remain exclusively in local device storage with `android:allowBackup="false"`. |
| **Is all of the user data collected by your app encrypted in transit?** | **N/A** (Not asked when "No" is selected above) | No data is ever transmitted over any network. |
| **Do you provide a way for users to request that their data is deleted?** | **N/A** (No account or server-side data exists) | Users can clear their local calculation history directly via the in-app **CLEAR** button or via Android system settings. |

## 2. Automated Verification Guarantees
Our Gradle build pipeline enforces this declaration on every build via two custom tasks in `app/build.gradle.kts`:
1. `:app:verifyManifestPrivacy` — Inspects both source and merged `AndroidManifest.xml` files to ensure only `android.permission.VIBRATE` is requested and `android:allowBackup="false"` + `android:usesCleartextTraffic="false"` are present.
2. `:app:verifyDependenciesPrivacy` — Inspects all declared and resolved transitive runtime classpaths to block any analytics, crash reporting, advertising, or network client libraries.
