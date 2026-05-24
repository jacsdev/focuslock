# FocusLock

**FocusLock** is a free, open-source Android accessibility app that overlays a large-text clock, date, battery indicator, and optional temperature on top of the system lock screen — designed for people with low vision who need maximum readability at a glance.

---

## The Problem

The default Android lock screen is built for average vision. Its clock and status elements are small, low-contrast, and often buried under wallpapers or notification clutter. For users with low vision, glancing at a locked phone to check the time or battery level can be a genuinely frustrating experience.

Commercial solutions either require replacing the entire launcher (overkill), demand system-level permissions that create security risks, or are cluttered with ads and unnecessary features.

FocusLock takes a different approach: it does one thing, does it well, and stays out of the way.

---

## What It Does

When your screen turns on and the device is locked, FocusLock draws a full-screen overlay showing:

- **Time** — displayed in 88–110sp bold white text, impossible to miss
- **Date** — full localized date below the clock
- **Battery ring** — a circular indicator that changes color based on level (green > 50%, yellow 20–50%, red < 20%), with percentage text in the center
- **Temperature** — optional real-time temperature from Open-Meteo for your city (no API key required)

When you unlock your device (swipe, PIN, fingerprint), the overlay disappears immediately. FocusLock never blocks access to your phone — it only adds information on top of the existing lock screen.

---

## How It Works

FocusLock uses Android's `AccessibilityService` to draw a `TYPE_ACCESSIBILITY_OVERLAY` window — a window type that sits above the keyguard without requiring `SYSTEM_ALERT_WINDOW` permission and without root access. This is the same mechanism used by screen readers and other accessibility tools that Android officially supports.

The overlay appears on `ACTION_SCREEN_ON` and is removed on `ACTION_USER_PRESENT` (device unlocked). Because Android preserves accessibility service settings across reboots, FocusLock resumes automatically after a device restart — no extra boot permission needed.

Temperature data is fetched via [Open-Meteo](https://open-meteo.com/), a free, no-key-required weather API, and refreshed in the background every 30 minutes using WorkManager.

---

## Features

- Pure AMOLED black background (`#000000`) for maximum contrast and battery efficiency on OLED screens
- 4 color themes: Dynamic, Ocean, Aurora, Arctic
- 4 interface languages: English, Spanish, Portuguese (Brazil), Hindi
- Optional temperature display with city configuration
- Optional battery percentage display
- Battery ring with color-coded levels and charging indicator
- Stale data detection — temperature shown in gray when last update was more than 2 hours ago
- No root required
- No `SYSTEM_ALERT_WINDOW` permission required
- No ads, no tracking, no internet permission beyond weather fetching

---

## Technical Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Unidirectional Data Flow (UDF) |
| Persistence | DataStore Preferences |
| Background work | WorkManager |
| Overlay mechanism | `AccessibilityService` + `TYPE_ACCESSIBILITY_OVERLAY` |
| Weather API | Open-Meteo (free, no API key) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 |

---

## Build & Install

```bash
# Clone the repository
git clone https://github.com/yourusername/FocusLock.git
cd FocusLock

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

After installing, open the app and follow the on-screen steps to enable the FocusLock accessibility service. That's all the setup required.

---

## Permissions

| Permission | Why |
|---|---|
| `BIND_ACCESSIBILITY_SERVICE` | Required to draw the lock screen overlay via the accessibility API |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Keeps the accessibility service alive so the overlay appears reliably |
| `INTERNET` | Fetches temperature data from Open-Meteo (only when weather is enabled) |

No location permission is required — city name is entered manually by the user.

---

## Open Source & Community

FocusLock is released as a free, open-source contribution to the Android accessibility community.

The patterns used here — drawing above the keyguard via `AccessibilityService`, real-time UI updates from a `CoroutineWorker`, and Jetpack Compose inside a Service — are not widely documented together. If you're building an accessibility tool, a low-vision aid, an always-on display, or any app that needs to interact with the lock screen without root or system permissions, this project is meant to be a reference you can learn from and build on.

Take it, adapt it, and use it to build things that help people.

---

## License

[MIT](LICENSE)
