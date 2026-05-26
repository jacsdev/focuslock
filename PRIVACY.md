# Privacy Policy — FocusLock

**Effective date:** May 25, 2025
**Contact:** jacsdev@gmail.com

---

## Our Mission

FocusLock exists to make Android phones easier to use for people who struggle with small text, cluttered screens, and complex visual interfaces — especially older adults and people with low vision.

Setting up a full accessibility theme on Android can be overwhelming. FocusLock takes a different approach: it replaces the default lock screen with a large-text, high-contrast overlay that shows exactly what matters — time, date, and weather — without requiring any technical knowledge to set up.

This app is free, contains no ads, and does not sell your data. Optional one-time donations help keep it maintained. Our privacy practices are as simple as our interface.

---

## What Data We Collect and Why

### 1. Data We Do NOT Collect

FocusLock does **not** collect:
- Your name, email address, or any personal identifiers
- Your location (GPS or IP-based)
- Your contacts, messages, photos, or files
- Any data entered on your lock screen
- Any data from other apps on your device

### 2. Crash Reports (Firebase Crashlytics)

**What:** If the app crashes, an automated crash report is sent to Google's Firebase Crashlytics service.

**Contains:** The type of error, a stack trace (technical log of what the app was doing), your device model, Android version, and app version. No personally identifiable information.

**Why:** So we can identify and fix bugs. Without crash data, silent failures go unnoticed.

**Your control:** You can opt out of crash reporting via Android's device settings under Privacy → Usage & Diagnostics.

### 3. Anonymous Usage Statistics (Firebase Analytics)

**What:** Basic, aggregated events such as "app opened" or "donation screen viewed."

**Contains:** No names, no identifiers, no content you type or see. Events are anonymous counts.

**Why:** To understand how many people use the app and whether the donation feature works correctly. We never build user profiles or track individuals.

**Your control:** You can opt out via Android's device settings under Privacy → Usage & Diagnostics.

### 4. Weather Data (Open-Meteo)

**What:** To display current weather on your lock screen, FocusLock uses the [Open-Meteo](https://open-meteo.com) free weather API.

**How it works:** You manually enter a city name in the app settings. FocusLock sends that city name to Open-Meteo's geocoding service to retrieve a latitude and longitude. After that, only the coordinates are used for weather requests — your city name is not stored on any server.

**Open-Meteo's policy:** Open-Meteo is a privacy-focused, open-source weather service with no user accounts and no tracking. Their data is sourced from public meteorological services (NOAA, DWD, ECMWF). See [open-meteo.com](https://open-meteo.com) for their privacy information.

**Stored locally:** Your entered city and retrieved coordinates are saved only on your device using Android DataStore. They are never transmitted to us.

### 5. Donations (Google Play Billing)

**What:** FocusLock offers optional one-time donations processed entirely by Google Play.

**We never see:** Your payment method, card number, billing address, or any financial data. All payment processing is handled by Google.

**What we receive:** Confirmation that a purchase was completed, so we can show a thank-you message. This confirmation contains only the product ID (e.g., "focuslock_donation_small") — no personal details.

**Purpose:** Donations are voluntary and unlock no features. They simply help cover the cost of maintaining a free app.

---

## Accessibility Service

FocusLock uses Android's Accessibility Service to display a fullscreen overlay on the lock screen. This is the only mechanism Android provides for drawing over the lock screen without requiring a full system theme replacement — which is precisely what makes it suitable for users who cannot navigate complex setup processes.

**Explicit declaration required by Google Play:**

> The Accessibility Service in FocusLock is used **solely** to render a large-text, high-contrast lock screen overlay.
>
> - Data accessed through the Accessibility Service is **NOT sold** to third parties.
> - It is **NOT used** for advertising, tracking, or user profiling.
> - It is **NOT shared** with any party for purposes unrelated to the app's core function.
> - All processing happens **on-device**. No accessibility data is transmitted to our servers.

---

## Permissions Explained

| Permission | Why it's needed |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Draw the lock screen overlay on top of the system UI |
| `ACCESSIBILITY_SERVICE` | Access the overlay window type required for lock screen display |
| `BIND_ACCESSIBILITY_SERVICE` | Required by Android to register as an Accessibility Service |
| `FOREGROUND_SERVICE` | Keep the overlay running reliably; Android requires a notification for long-running services |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Android 14+ category for foreground services with specialized functionality |
| `RECEIVE_BOOT_COMPLETED` | Restart the lock screen service automatically after the phone is rebooted |
| `INTERNET` | Fetch weather data from Open-Meteo and send anonymous crash/analytics reports |
| `ACCESS_NETWORK_STATE` | Check if the device is online before making weather requests |
| `BILLING` | Process optional one-time donations through Google Play |

---

## Data Storage Summary

| Data | Where stored | Transmitted to |
|---|---|---|
| Your city name setting | On your device only | Open-Meteo (once, for geocoding) |
| Coordinates for weather | On your device only | Open-Meteo (weather requests) |
| App preferences | On your device only | Nowhere |
| Crash reports | Firebase Crashlytics | If a crash occurs |
| Anonymous analytics events | Firebase Analytics | On key app events |
| Donation purchase token | Not stored | Google Play (for verification) |

---

## Third-Party Services

FocusLock uses the following third-party services. Each has its own privacy policy:

- **Google Firebase Analytics** — [firebase.google.com/support/privacy](https://firebase.google.com/support/privacy)
- **Google Firebase Crashlytics** — [firebase.google.com/support/privacy](https://firebase.google.com/support/privacy)
- **Google Play Billing** — [payments.google.com/payments/apis-secure/get_legal_document?ldo=0&ldt=privacynotice](https://payments.google.com/payments/apis-secure/get_legal_document?ldo=0&ldt=privacynotice)
- **Open-Meteo Weather API** — [open-meteo.com](https://open-meteo.com)

---

## Children's Privacy

FocusLock is not directed at children under 13. We do not knowingly collect personal information from anyone under 13. The app's target audience is older adults and people with low vision. If you believe a child has submitted information through this app, please contact us and we will promptly delete it.

---

## Your Rights

Since FocusLock stores almost all data locally on your device, you are already in direct control of it. Specifically:

- **Delete app data:** Go to Android Settings → Apps → FocusLock → Storage → Clear Data. This removes all locally stored preferences.
- **Uninstall the app:** Removes all local data permanently.
- **Opt out of analytics and crash reports:** Android Settings → Privacy → Usage & Diagnostics → turn off.
- **Contact us:** If you have questions about your data, email us at jacsdev@gmail.com.

We will respond to any privacy inquiry within 30 days.

---

## Changes to This Policy

If we make significant changes to this policy, we will update the date at the top and, where possible, notify users through an in-app message or Play Store update notes. Continued use of FocusLock after changes are posted means you accept the updated policy.

---

## Contact

**Developer:** jacsdev
**Email:** jacsdev@gmail.com

We take privacy seriously because our users do. If something in this policy is unclear, email us — we'll explain it in plain language.
