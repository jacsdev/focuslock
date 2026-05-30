# Privacy Policy — FocusLock

**Effective date:** May 29, 2026
**Developer:** jacsdev
**Contact:** [jacsdev@gmail.com](mailto:jacsdev@gmail.com)

---

# Overview

FocusLock is an Android application that displays a customizable lock screen overlay showing time, date, battery level, and optional weather information. It is designed for users who want a cleaner, more readable lock screen — including people with low vision, older adults, and users who prefer a simplified interface.

The app uses Android's Accessibility Service exclusively as the technical mechanism to draw content above the system lock screen. This is the only official Android API that allows overlay content above the keyguard without requiring elevated system permissions.

FocusLock is designed with privacy, transparency, and minimal data collection in mind.

This app:

* contains no ads
* does not sell user data
* does not profile users
* does not collect personal identifiers
* performs nearly all processing locally on the device

Optional one-time donations help support maintenance and future accessibility improvements.

---

# Core Accessibility Functionality

The Android Accessibility Service is the core technical mechanism FocusLock uses to draw a full-screen overlay above the system lock screen.

Without the Accessibility Service, the app cannot render its overlay using `TYPE_ACCESSIBILITY_OVERLAY`, which is the only official Android API that allows content to appear above the keyguard without requiring elevated system permissions.

FocusLock uses Android Accessibility APIs exclusively to:

* render a full-screen overlay on the lock screen displaying time, date, battery level, and optional weather information
* respond to screen-on and screen-off system events to attach and detach the overlay

The app does not use accessibility features for monitoring, automation, surveillance, advertising, or data collection.

---

# Accessibility Data Handling

FocusLock uses Android Accessibility APIs only to draw a lock screen overlay. No accessibility events from other apps are read or processed.

FocusLock does NOT:

* read text displayed in other apps
* capture passwords or typed content
* inspect notifications
* monitor user behavior
* record screen activity
* collect accessibility events for analytics
* perform gestures or automated interactions
* access content from other applications

The accessibility service is limited exclusively to rendering the accessibility interface.

No accessibility-related data is transmitted, stored remotely, or shared with third parties.

All accessibility-related processing happens locally on the device.

---

# Information We Do NOT Collect

FocusLock does not collect:

* names
* email addresses
* phone numbers
* contacts
* photos
* videos
* files
* messages
* passwords
* payment card information
* precise location data
* browsing history
* app usage from other apps
* accessibility content from other applications

The app does not require user registration or account creation.

---

# No User Account Required

FocusLock can be fully used without:

* creating an account
* signing in
* registering
* providing personal information

All preferences are stored locally on the device.

---

# Information We Collect

FocusLock collects only limited technical information necessary for app functionality, diagnostics, and optional features.

---

## 1. Crash Reports (Firebase Crashlytics)

### What is collected

If the app crashes, Firebase Crashlytics may automatically collect:

* crash stack traces
* device model
* Android version
* app version
* technical diagnostic information

### What is NOT collected

Crash reports do not include:

* personal identifiers
* passwords
* messages
* accessibility content
* typed text
* screenshots

### Why this data is collected

Crash diagnostics help identify bugs and improve app stability.

### User control

Users may disable Android diagnostic sharing features in device privacy settings.

---

## 2. Anonymous Usage Analytics (Firebase Analytics)

### What is collected

FocusLock may collect anonymous aggregated events such as:

* app opened
* settings viewed
* weather enabled
* donation screen viewed

### Analytics limitations

Firebase Analytics is configured without:

* advertising features
* personalized advertising
* cross-app tracking
* behavioral profiling

**Advertising ID:** FocusLock explicitly disables Google Advertising ID (GAID) collection via Firebase Analytics configuration. The `AD_ID` permission is removed from the app manifest. No advertising identifier is collected or used.

FocusLock does not use analytics data for:

* advertising
* marketing
* user profiling
* personalized recommendations

### User control

Users may disable analytics sharing through Android privacy settings.

---

## 3. Weather Information (Open-Meteo)

FocusLock optionally displays weather information using the Open-Meteo API.

### How it works

Users manually enter a city name inside the app.

The city name is sent to Open-Meteo's geocoding service to retrieve approximate coordinates required for weather requests.

After geocoding:

* weather requests use coordinates only
* the city name is stored locally on the device
* no weather data is sent to the developer

### Data stored locally

Stored locally on the device:

* city name
* latitude and longitude
* weather preferences

### Permissions

FocusLock does not request location permission.

---

## 4. Donations (Google Play Billing)

FocusLock offers optional one-time donations processed entirely through Google Play Billing.

### Payment information

FocusLock never receives:

* card numbers
* billing addresses
* banking information
* payment credentials

All payment processing is handled by Google Play.

### What the app receives

The app only receives:

* purchase confirmation
* donation product identifier

This information is used solely to confirm successful donations.

Donations do not unlock features or affect app functionality.

---

# Foreground Service

FocusLock runs a persistent foreground service (visible to the user as a low-priority notification labeled "FocusLock active" or "FocusLock paused").

### Purpose

The foreground service exists solely to prevent aggressive OEM battery management systems — present on Xiaomi, Samsung, Huawei, and other Android devices — from terminating the process that hosts the Accessibility Service.

### What the foreground service does NOT do

* Perform any network requests
* Collect or transmit any user data
* Access sensors, location, camera, or microphone
* Monitor app usage or device activity

The foreground service has no functionality beyond keeping the app process alive. Users can verify this by observing that the notification shows only a status label and contains no actionable content.

---

# Data Storage

| Data                  | Storage Location     | Shared With          |
| --------------------- | -------------------- | -------------------- |
| App settings          | User device only     | Nobody               |
| Theme preferences     | User device only     | Nobody               |
| Weather city          | User device only     | Open-Meteo geocoding |
| Weather coordinates   | User device only     | Open-Meteo           |
| Crash diagnostics     | Firebase Crashlytics | Google Firebase      |
| Anonymous analytics   | Firebase Analytics   | Google Firebase      |
| Donation confirmation | Google Play Billing  | Google               |

---

# Third-Party Services

FocusLock uses limited third-party services for diagnostics, analytics, weather information, and optional donations.

These services operate under their own privacy policies.

## Included Services

### Google Firebase Analytics

[https://firebase.google.com/support/privacy](https://firebase.google.com/support/privacy)

### Google Firebase Crashlytics

[https://firebase.google.com/support/privacy](https://firebase.google.com/support/privacy)

### Google Play Billing

[https://payments.google.com/payments/apis-secure/get_legal_document?ldo=0&ldt=privacynotice](https://payments.google.com/payments/apis-secure/get_legal_document?ldo=0&ldt=privacynotice)

### Open-Meteo

[https://open-meteo.com](https://open-meteo.com)

---

# Third-Party SDK Disclosure

FocusLock includes limited third-party SDKs strictly for:

* crash diagnostics
* anonymous usage statistics
* optional donation processing

Included SDKs:

* Firebase Analytics
* Firebase Crashlytics
* Google Play Billing

No SDKs are used for:

* advertising
* behavioral tracking
* personalized marketing

---

# Permissions Explained

| Permission                             | Purpose                                                                                                   |
| -------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| `BIND_ACCESSIBILITY_SERVICE`           | Required to draw the lock screen overlay via `TYPE_ACCESSIBILITY_OVERLAY`                                 |
| `FOREGROUND_SERVICE`                   | Required to run the companion keepalive service                                                           |
| `FOREGROUND_SERVICE_SPECIAL_USE`       | Required on Android 14+ for the companion service that prevents OEM battery managers from killing the app |
| `POST_NOTIFICATIONS`                   | Required to show the mandatory persistent notification for the foreground service                         |
| `INTERNET`                             | Retrieve optional weather information and send crash diagnostics                                          |
| `ACCESS_NETWORK_STATE`                 | Check connectivity before weather requests                                                                |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Prevent Doze mode from terminating the accessibility service during extended screen-off periods           |

FocusLock does not request:

* location access
* microphone access
* camera access
* contacts access
* SMS access
* storage access

---

# Children's Privacy

FocusLock is not directed toward children under 13.

The app is designed for accessibility assistance and general usability for adults and low-vision users.

We do not knowingly collect personal information from children.

If you believe a child has provided information through the app, contact us and we will promptly address the issue.

---

# Legal Basis for Processing

Where applicable under privacy laws such as the GDPR, FocusLock processes limited technical data based on:

* legitimate interest (application stability and diagnostics)
* user consent (optional analytics and donations)

FocusLock does not process personal data for:

* advertising
* profiling
* behavioral analysis
* marketing purposes

---

# Your Rights

Users remain in direct control of nearly all data because most information is stored locally on the device.

Users may:

* clear app data through Android Settings
* uninstall the app at any time
* disable analytics through Android privacy settings
* disable weather functionality
* contact the developer regarding privacy questions

---

# Accessibility Commitment

FocusLock was created to improve readability and accessibility for users who struggle with standard Android interfaces.

We are committed to:

* accessibility-first design
* privacy-first development
* minimal data collection
* transparent behavior
* non-intrusive functionality

---

# Changes to This Policy

If this Privacy Policy changes significantly:

* the effective date will be updated
* users may be notified through app updates or release notes

Continued use of FocusLock after changes are published constitutes acceptance of the updated policy.

---

# Contact

**Developer:** jacsdev
**Email:** [jacsdev@gmail.com](mailto:jacsdev@gmail.com)

If you have questions about accessibility, privacy, or data handling, feel free to contact us.