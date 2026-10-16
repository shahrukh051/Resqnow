# ResQNow – Android (Kotlin + Compose)

## 1. Create the project so names match
New Project -> **Empty Activity** (Compose).  Name: `ResQNow`  Package name: **`com.resqnow.app`**  Min SDK 24.
Copy the 3 .kt files from `app/src/main/java/com/resqnow/app/` into your project's `com/resqnow/app/` folder (overwrite MainActivity.kt).
If your package is different, change the first line (`package ...`) of all 3 files to match it.

## 2. app/build.gradle.kts -> dependencies { }
```
implementation(platform("androidx.compose:compose-bom:2024.10.01"))
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose:2.8.3")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
implementation("com.google.maps.android:maps-compose:6.5.0")
```
(maps-compose already pulls in the Maps SDK and Maps Utils, do not add them separately.
If `PolyUtil` is still red, also add `implementation("com.google.maps.android:android-maps-utils:3.10.0")`.)
Then File -> Sync Project with Gradle Files.

## 3. AndroidManifest.xml
Above `<application>`:  `<uses-permission android:name="android.permission.INTERNET"/>`
Inside `<application>`:  `<meta-data android:name="com.google.android.geo.API_KEY" android:value="YOUR_KEY"/>`
Set `android:label="ResQNow"` on `<application>` for the app name.

## 4. Google Cloud
Enable **Maps SDK for Android** and **Routes API**, billing on. Same key goes in `RoutesApi.KEY` (TrackViewModel.kt).
