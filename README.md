# DriveMind

DriveMind is a native Android portfolio project for capturing short voice notes, tasks, reminders, and ideas. It categorizes transcripts using English keywords and stores notes in an on-device Room database. The Notes tab supports search, filters, task completion, editing, read-aloud, and deletion. Reminder is a note type. It does not schedule an alert.

[Try the interactive browser demo](https://drivemind-demo.vercel.app). The demo uses sample data and simulates speech and driving status. It runs without requesting microphone or location access. Reloading restores the sample notes.

## Build and test

Open this repository in Android Studio and select JDK 17 as the Gradle JDK. Install Android SDK Platform 34 and Android SDK Build-Tools 34. Use the checked-in Gradle 8.5 wrapper with Android Gradle Plugin 8.2.2. The minimum device API is 26 and the target API is 34.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

On Linux or macOS, use `./gradlew`. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Connected tests require a running device or emulator.

## Device dependencies

- Microphone access and an Android speech recognition service are needed for voice capture. The app requests English recognition. Network use and offline model availability depend on the installed service.
- Read-aloud requires an installed Android text-to-speech engine with English support.
- Speed-based status requires precise location and Google Play services. Notes remain usable without location permission.
- No DriveMind account or backend is needed. The browser demo and native app use separate data.

Use a parked setup or a stationary emulator to test the interface. See the [native screen index](docs/verification/SCREEN-INDEX.md) for verification steps and coverage.
