# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**FocusLock** is an Android accessibility app for users with low vision. It overlays a large-text clock and battery indicator on top of the system lock screen — it is **not a launcher and does not replace the Android lock screen**. The overlay appears on `ACTION_SCREEN_ON` and disappears on `ACTION_USER_PRESENT` (device unlocked).

Package: `com.developermind.focuslock`  
Min SDK: 26 | Target SDK: 36  
Stack: Kotlin + Jetpack Compose + Material 3  
Architecture: **MVVM with Unidirectional Data Flow (UDF)** per the [official Android architecture guide](https://developer.android.com/topic/architecture)

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single unit test class
./gradlew test --tests "com.developermind.focuslock.ExampleUnitTest"

# Lint
./gradlew lint
```

## Official Folder Structure

```
app/src/main/java/com/developermind/focuslock/
│
├── MainActivity.kt                    # Entry point — starts ScreenMonitorService
│
├── service/
│   └── ScreenMonitorService.kt        # Foreground Service; registers ScreenReceiver at runtime
│
├── receiver/
│   └── ScreenReceiver.kt              # BroadcastReceiver — MUST be registered at runtime, NOT in manifest
│
├── ui/
│   ├── overlay/
│   │   ├── OverlayActivity.kt         # Fullscreen lock-screen Activity (setShowWhenLocked/setTurnScreenOn)
│   │   ├── OverlayViewModel.kt        # Holds OverlayUiState; observes BatteryRepository
│   │   ├── OverlayUiState.kt          # Immutable data class — single source of truth for UI
│   │   └── OverlayScreen.kt           # Root @Composable for the overlay
│   │
│   ├── components/
│   │   └── BatteryRing.kt             # Canvas-based circular battery indicator (drawArc + animation)
│   │
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── data/
│   ├── model/
│   │   └── BatteryState.kt            # Plain data model (percentage, isCharging, isFull, isLow, isPlugged)
│   └── repository/
│       └── BatteryRepository.kt       # Exposes Flow<BatteryState> via ACTION_BATTERY_CHANGED
│
└── util/
    └── BatteryMonitor.kt              # Reads current battery snapshot from sticky broadcast
```

## Architecture Rules (MVVM + UDF)

Based on [developer.android.com/topic/architecture/ui-layer](https://developer.android.com/topic/architecture/ui-layer):

- **State flows DOWN**: ViewModel exposes a single immutable `data class XxxUiState` via `StateFlow`. UI never owns state.
- **Events flow UP**: Composables call ViewModel methods. ViewModels never reference Compose elements.
- **Collect with lifecycle**: Use `collectAsStateWithLifecycle()` in Composables (not `collectAsState()`).
- **Repository is SSOT**: `BatteryRepository` is the single source of truth for battery data. ViewModel reads from it, never from `BatteryMonitor` directly.

## Critical Android Constraints

### BroadcastReceiver (ACTION_SCREEN_ON / OFF / USER_PRESENT)
These three actions are **NOT on the implicit-broadcast exceptions list**. They **cannot be declared in `AndroidManifest.xml`** — doing so is silently ignored on API 26+.  
**→ Must be registered programmatically inside `ScreenMonitorService.onCreate()` and unregistered in `onDestroy()`.**

### Foreground Service — API 34+ `foregroundServiceType`
Apps targeting API 34+ must declare `android:foregroundServiceType` or `startForeground()` throws `MissingForegroundServiceTypeException`.  
**→ Use `specialUse` type. Requires `FOREGROUND_SERVICE_SPECIAL_USE` permission and a `<property>` child element with a description (reviewed by Google Play).**

### OverlayActivity — Lock Screen Display
Set both at runtime (API 27+):
```kotlin
setShowWhenLocked(true)
setTurnScreenOn(true)
```
Also set in manifest: `android:showWhenLocked="true"` and `android:turnScreenOn="true"`.

## UI Design Constraints

Hard requirements for low-vision accessibility:

| Element           | Spec                                      |
|-------------------|-------------------------------------------|
| Background        | `#000000` pure AMOLED black               |
| Time text         | 88–110sp, bold, white                     |
| Battery % text    | 56–72sp, bold, white                      |
| Battery ring stroke | 22–30dp, rounded caps                   |
| Ring color >50%   | Green                                     |
| Ring color 20–50% | Yellow/Orange                             |
| Ring color <20%   | Red                                       |

`BatteryRing` uses `Canvas.drawArc()` with `animateFloatAsState` for smooth progress animation.

## Required Permissions (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

## Hard Restrictions

- No root, no private APIs, no SystemUI modification
- The overlay must close when the user unlocks — never block the real lock screen
- Do not replace Android lock screen security
