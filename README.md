# 🚗 DriveMind

> **Capture thoughts, ideas and tasks while driving — hands-free, eyes-free.**

A production-ready Android app built with Kotlin, MVVM, Clean Architecture and modern Android Jetpack libraries.

---

## 📱 Screenshots & Flow

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   DRIVING MODE  │     │   NOTES LIST    │     │  NOTE DETAIL    │
│                 │     │                 │     │                 │
│  🚗 Driving    │     │ 🔍 Search...    │     │  ✅ Task        │
│                 │     │                 │     │                 │
│                 │     │ 📝 Buy milk     │     │ Buy milk        │
│    ╔══════╗     │     │    2 min ago    │     │ tomorrow.       │
│    ║  🎙️  ║     │     │                 │     │                 │
│    ╚══════╝     │     │ 💡 App idea     │     │ 🛒 Shopping     │
│                 │     │    today        │     │ 12 Jan, 14:30   │
│  Tap to speak   │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

---

## ✨ Features

| Feature | Description |
|---|---|
| 🎙️ **Voice Capture** | One-tap recording using Android SpeechRecognizer — fully offline |
| 🔊 **TTS Feedback** | App reads confirmations aloud — no need to look at the screen |
| 🧠 **Smart Classification** | Keyword NLP classifies speech as Note / Task / Reminder / Idea |
| 🚗 **Driving Detection** | GPS speed-based detection auto-activates simplified UI |
| 📋 **Notes History** | Full list with search, type filters and swipe-to-delete |
| ✏️ **Edit & Read** | Edit notes, read them back with TTS from the detail screen |
| 🔔 **Foreground Service** | Keeps driving detection alive in background |
| 💾 **Room DB** | All notes stored locally — works 100% offline |

---

## 🏗️ Architecture

```
com.drivemind/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          ← Room database (v1)
│   │   ├── dao/VoiceNoteDao.kt     ← All SQL queries
│   │   └── entity/VoiceNoteEntity.kt
│   └── repository/
│       └── VoiceNoteRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   └── VoiceNote.kt            ← Pure Kotlin domain model
│   ├── repository/
│   │   └── VoiceNoteRepository.kt  ← Interface (no Android deps)
│   └── usecase/
│       └── UseCases.kt             ← SaveNote, GetAll, Delete, Search…
│
├── ui/
│   ├── MainActivity.kt             ← Bottom nav host
│   ├── driving/
│   │   ├── DrivingFragment.kt      ← Big mic button UI
│   │   └── DrivingViewModel.kt     ← Driving + speech state
│   ├── history/
│   │   ├── HistoryFragment.kt      ← Notes list + search
│   │   ├── HistoryViewModel.kt
│   │   └── VoiceNoteAdapter.kt     ← DiffUtil RecyclerView adapter
│   ├── notes/
│   │   ├── NoteDetailFragment.kt
│   │   └── NoteDetailViewModel.kt
│   └── splash/SplashActivity.kt
│
├── speech/
│   ├── SpeechManager.kt            ← SpeechRecognizer wrapper + StateFlow
│   └── TtsManager.kt               ← TextToSpeech wrapper + StateFlow
│
├── service/
│   └── DrivingDetectionService.kt  ← Foreground service (location type)
│
├── utils/
│   ├── DrivingDetector.kt          ← GPS speed → driving state
│   └── IntentProcessor.kt          ← Keyword NLP → NoteType + NoteCategory
│
└── di/
    └── AppModule.kt                ← Hilt modules (DB + Repository)
```

### Layer responsibilities

```
UI Layer  ──→  Domain Layer  ──→  Data Layer
(Fragments)    (Use Cases)        (Room + Repository)
(ViewModels)   (Models)
```

- **UI** knows only about ViewModels and domain models
- **Domain** has zero Android imports (pure Kotlin)
- **Data** implements domain interfaces

---

## ⚙️ Tech Stack

| Tech | Usage |
|---|---|
| Kotlin | Primary language |
| Hilt | Dependency injection |
| Room | Local SQLite database |
| Navigation Component | Fragment navigation + SafeArgs |
| ViewModel + StateFlow | Reactive UI state |
| Coroutines + Flow | Async/reactive streams |
| SpeechRecognizer API | Offline speech-to-text |
| TextToSpeech API | Audio confirmation |
| FusedLocationProvider | GPS driving detection |
| Material3 | UI components + dark theme |
| SplashScreen API | Modern splash |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- Kotlin 1.9.x
- A physical device recommended (SpeechRecognizer works better on real hardware)

### Setup

```bash
# 1. Clone / open in Android Studio
git clone https://github.com/yourname/drivemind.git

# 2. Add Google Services (if using Firebase later)
# For now, just sync Gradle

# 3. Run on device or emulator (API 26+)
```

### First run
1. Grant **Microphone** permission when prompted
2. Grant **Location** permission for driving detection (optional but recommended)
3. Tap the large 🎙️ button and speak naturally
4. View saved notes in the **Notes** tab

---

## 🎙️ Voice Recognition Details

### How it works (offline)

```kotlin
// SpeechManager.kt
val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
             RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)  // live preview
    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
}
```

- **Language**: English (en-US)
- **Partial results**: Show text live as you speak
- **Silence detection**: Stops after 1.5s of silence
- **No internet required**: Uses on-device model (Android 6+)

### Smart Classification (IntentProcessor)

```
Input:  "need to buy milk tomorrow"
         ↓ keyword scan
Type:   TASK  (keyword: "need to")
Category: SHOPPING  (keyword: "buy")
Output: "Need to buy milk tomorrow."

Input:  "idea for fitness app"
         ↓ keyword scan
Type:   IDEA  (keyword: "idea")
Category: GENERAL
Output: "Idea for fitness app."
```

---

## 🚗 Driving Detection

```kotlin
// DrivingDetector.kt
// DRIVING:  speed > 15 km/h for 3 consecutive readings
// PARKED:   speed < 5 km/h for 3 consecutive readings
// Hysteresis prevents rapid switching
```

The foreground service keeps GPS active even when the app is in the background, with a persistent notification the user can dismiss to stop tracking.

---

## 🧪 Tests

```bash
# Unit tests (no device needed)
./gradlew test

# Instrumented tests (Room DAO, needs emulator/device)
./gradlew connectedAndroidTest
```

### Test coverage
- `IntentProcessorTest` — 13 unit tests for NLP classification
- `DrivingViewModelTest` — 6 ViewModel tests with mocked dependencies
- `VoiceNoteDaoTest` — 8 Room DAO tests using in-memory database

---

## 🔒 Permissions

| Permission | Why |
|---|---|
| `RECORD_AUDIO` | Speech-to-text input |
| `ACCESS_FINE_LOCATION` | Driving speed detection |
| `FOREGROUND_SERVICE` | Keep detection running in background |
| `FOREGROUND_SERVICE_LOCATION` | Required for location foreground service (Android 14+) |
| `POST_NOTIFICATIONS` | Show driving mode notification (Android 13+) |
| `WAKE_LOCK` | Keep screen on during driving mode |

---

## 🗺️ Roadmap

- [ ] "Hey DriveMind" hotword trigger (always-listening)
- [ ] AI-powered note summarisation via ChatGPT API
- [ ] Scheduled reminder notifications (AlarmManager)
- [ ] Auto-reply SMS while driving
- [ ] Widget for quick capture from home screen
- [ ] Export notes to CSV / share

---

## 📄 License

MIT License — feel free to use for your portfolio.

---

*Built as a portfolio project demonstrating clean Android architecture, voice UX, and real-world safety-focused mobile design.*
