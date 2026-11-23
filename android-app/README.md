# Starter App (Android)

A minimal Android app written in Kotlin with Jetpack Compose and Material 3.
It shows a greeting and a button that counts how many times it was pressed.

## Open and run

1. Install [Android Studio](https://developer.android.com/studio).
2. **File → Open** and choose this `android-app` folder.
3. Wait for Gradle sync, then press **Run ▶** with an emulator or a phone connected over USB.

From the command line (needs the Android SDK and JDK 17+):

```sh
./gradlew assembleDebug        # APK in app/build/outputs/apk/debug/
./gradlew installDebug         # install on a connected device
```

## Get a ready-made APK

Every push that touches `android-app/` runs the **Android build** workflow on GitHub.
Open the run under the repository's **Actions** tab and download the `app-debug` artifact.

## Where to start editing

| File | What it holds |
|------|---------------|
| `app/src/main/java/com/zatanzen/starter/MainActivity.kt` | The screen (`HomeScreen`) |
| `app/src/main/java/com/zatanzen/starter/ui/theme/Theme.kt` | Colors and theme |
| `app/src/main/res/values/strings.xml` | App name |
| `gradle/libs.versions.toml` | Library versions |
