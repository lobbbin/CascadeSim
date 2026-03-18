# CascadeSim

Android Management Simulator with Jetpack Compose UI, Room database, Hilt DI, and WorkManager background simulation.

## Download

### Latest Debug Build
1. Go to [Actions](https://github.com/lobbbin/CascadeSim/actions)
2. Click the latest passing build
3. Download `CascadeSim-debug` artifact
4. Extract and install `app-debug.apk`

**Direct link:** [Latest Workflow](https://github.com/lobbbin/CascadeSim/actions/workflows/android-debug-apk.yml)

## Installation

### Requirements
- Android 8.0 (API 26) or higher
- Enable "Install from Unknown Sources" in Settings

### Install via ADB (Recommended)
```bash
# Download the APK first, then:
adb install app-debug.apk

# Or if you have an existing install and want to replace:
adb install -r app-debug.apk
```

### Install via File Manager
1. Download the APK from GitHub Actions
2. Open your file manager
3. Tap the APK file
4. Grant permission if prompted
5. Tap "Install"

### Troubleshooting

**"Package Installer" error:**
- Try installing via ADB: `adb install app-debug.apk`
- Uninstall any existing version first: `adb uninstall com.cascadesim.debug`
- Check Android version (requires 8.0+)

**"App not installed" error:**
- Storage full - free up space
- Conflicting signature - uninstall old version first
- Corrupted download - re-download the APK

**Debug vs Release:**
- Debug APK: `com.cascadesim.debug` (can coexist with release)
- Release APK: `com.cascadesim` (production version)

## Architecture

```
:common  ← Shared types, entities, models (no dependencies)
   ↑
:core    ← Room database, repositories
   ↑
:game    ← Game engine, WorkManager worker
   ↑
:app     ← UI, ViewModel, DI wiring
```

## Tech Stack
- **UI:** Jetpack Compose + Material 3
- **DI:** Hilt 2.48.1
- **Database:** Room 2.6.1
- **Background:** WorkManager 2.9.0
- **Architecture:** MVVM + Repository pattern

## Build

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

## License
MIT
