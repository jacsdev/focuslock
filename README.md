# FocusLock

**FocusLock** is a free and open-source Android accessibility app designed for people with low vision who need larger, clearer information on their device screen.

When the device screen turns on, FocusLock displays a simplified, high-contrast interface with large text showing the current time, date, battery level, and optional weather information — making essential information easier to read at a glance.

FocusLock is built specifically for accessibility, simplicity, readability, and minimal visual clutter.

---

# Why FocusLock Exists

Modern Android interfaces are designed for average vision and dense information layouts. For many people — especially older adults and users with visual impairments — checking the time or battery level on a locked device can be frustrating due to:

* Small text
* Low contrast
* Busy wallpapers
* Notification clutter
* Complex lock screen layouts

FocusLock solves this by providing a clean, enlarged accessibility display focused only on essential information.

The goal is simple:

> Make Android devices easier to read for people who struggle with small interfaces.

---

# Features

## Large Text Display

FocusLock shows an oversized digital clock optimized for readability and visibility from a distance.

* Large bold typography
* High contrast colors
* AMOLED-friendly pure black background
* Minimal distractions

---

## Date & Time

Displays:

* Current time
* Full localized date
* Automatic 12h / 24h formatting based on device settings

---

## Battery Indicator

FocusLock includes a circular battery indicator with:

* Percentage display
* Charging state indicator
* Color-coded battery levels

Battery colors:

* Green → healthy battery
* Yellow → medium battery
* Red → low battery

---

## Optional Weather

Users can optionally enable weather information using a manually entered city name.

Weather features:

* Current temperature
* Automatic refresh
* No API key required
* No location permission required

Weather data is provided by [Open-Meteo](https://open-meteo.com).

---

## Accessibility-Focused Design

FocusLock is designed specifically for:

* Low-vision users
* Older adults
* Users sensitive to visual clutter
* Users who need larger readability

The app uses Android Accessibility APIs exclusively to provide enlarged visual information.

FocusLock does **not**:

* read screen content
* capture typed text
* monitor other apps
* track user activity
* perform automated actions
* collect accessibility data
* modify system settings

All processing happens locally on the device.

---

# Privacy First

FocusLock was intentionally designed to be privacy-friendly.

The app:

* contains no ads
* does not sell user data
* does not create user profiles
* stores preferences locally on the device

Optional crash reports and anonymous analytics may be processed through Firebase to improve stability and usability.

See the full Privacy Policy for details.

---

# Technical Overview

| Component               | Technology           |
| ----------------------- | -------------------- |
| Language                | Kotlin               |
| UI                      | Jetpack Compose      |
| Architecture            | MVVM + UDF           |
| Persistence             | Android DataStore    |
| Background Tasks        | WorkManager          |
| Accessibility Layer     | AccessibilityService |
| Weather API             | Open-Meteo           |
| Minimum Android Version | Android 8.0 (API 26) |
| Target SDK              | Android 16 / API 36  |

---

# Permissions

| Permission                             | Purpose                                                 |
| -------------------------------------- | ------------------------------------------------------- |
| `BIND_ACCESSIBILITY_SERVICE`           | Required for accessibility functionality                |
| `INTERNET`                             | Fetch optional weather information                      |
| `ACCESS_NETWORK_STATE`                 | Check connectivity before weather updates               |
| `FOREGROUND_SERVICE`                   | Maintain accessibility-related background functionality |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Improve service reliability on some devices             |

FocusLock does not request:

* location permission
* contacts access
* storage access
* SMS access
* microphone access

---

# How It Works

When the device screen becomes active, FocusLock displays an enlarged accessibility interface with essential information.

When the device is unlocked, the interface disappears automatically.

FocusLock is designed to be passive and non-intrusive:

* it never blocks device access
* it never replaces system security
* it never intercepts user input

---

# Installation

## Clone Repository

```bash
git clone https://github.com/yourusername/FocusLock.git
cd FocusLock
```

---

## Build Debug APK

```bash
./gradlew assembleDebug
```

---

## Install on Device

```bash
./gradlew installDebug
```

---

# Setup

After installation:

1. Open FocusLock
2. Follow the accessibility setup instructions
3. Enable the FocusLock accessibility service
4. Configure optional weather and theme preferences

Once enabled, FocusLock will automatically display the accessibility interface when the device screen turns on.

---

# Localization

Currently supported languages:

* English
* Español
* Português (Brasil)
* हिन्दी

---

# Themes

Available themes:

* Dynamic
* Ocean
* Aurora
* Arctic

All themes are optimized for:

* high contrast
* readability
* OLED battery efficiency

---

# Open Source

FocusLock is fully open source and intended to support the Android accessibility community.

The project demonstrates:

* accessibility-focused UI patterns
* Jetpack Compose inside accessibility components
* large-text accessibility design
* lightweight background updates
* privacy-first Android architecture

Contributions, improvements, and accessibility feedback are welcome.

---

# License

MIT License

---

# Contact

Developer: **jacsdev**
Email: **[jacsdev@gmail.com](mailto:jacsdev@gmail.com)**

If you have accessibility suggestions, bug reports, or questions, feel free to reach out.