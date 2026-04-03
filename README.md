# KablanPro — Contractor Cash Flow Android

> A modern Android app for contractors to manage projects, workers, expenses, invoices, and cash flow — all in one place.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Free vs Pro](#free-vs-pro)
- [Getting Started](#getting-started)
- [Build & Run](#build--run)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Legal Assets Required](#legal-assets-required)

---

## Overview

**KablanPro** is a contractor-focused cash flow management app built for Android with Jetpack Compose. It gives contractors a clear view of their projects, workers, income, and expenses — with smart notifications, cloud backup, and receipt scanning baked in.

---

## Features

### 📁 Projects
- Create and manage unlimited projects (Pro) or 1 project (Free)
- View per-project financial summary: net balance, income, expenses, profit margin
- Export project data as JSON
- Attach expenses and invoices directly to a project

### 👷 Workers / Labor
- Add and manage workers across projects
- Track labor cost per worker and project
- Free plan supports up to 2 workers; Pro is unlimited

### 💸 Expenses
- Log expenses with category, date, amount, and description
- Scan receipts using the device camera (ML Kit OCR)
- Filter and search expenses by date, category, or project
- Day-grouped list view with daily totals

### 🧾 Invoices
- Create, edit, and track invoices per project
- Filter by status and date
- View invoice details and payment state

### 👤 Clients
- Maintain a client directory linked to projects
- View client details and associated projects

### 📊 Analytics
- Visual cash flow charts (income vs expenses over time)
- Per-project profitability overview

### 🔔 Notifications
- Invoice payment reminders
- Overdue invoice alerts
- Budget warning notifications

### ☁️ Cloud Sync
- Sign in with Google account
- Sync data to Firebase Firestore
- Network-aware sync with connectivity checks

### 📤 Export
- Export full app data snapshot as JSON (projects, expenses, invoices, clients, preferences)

### ⚙️ Settings
- Language selection (with RTL layout support)
- Currency selection
- Theme: Light / Dark / System
- Subscription management (KablanPro Monthly / Yearly)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Database | Room (local SQLite) |
| Preferences | DataStore |
| Cloud | Firebase Firestore + Firebase Auth |
| Billing | Google Play Billing Library |
| OCR | ML Kit Text Recognition |
| Camera | CameraX |
| Image Loading | Coil |
| Charts | Vico Charts |
| Background | WorkManager |
| DI | Manual (ViewModel factories) |
| Serialization | Gson |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

---

## Architecture

The app follows **MVVM** with a clean layered structure:

```
UI (Composables)
    ↓
ViewModels (state + business logic)
    ↓
Repositories (data access abstraction)
    ↓
Room DAOs / Firebase / DataStore
```

- **UI layer** — Compose screens, navigation graphs, reusable components
- **ViewModel layer** — Kotlin `StateFlow` for state, `coroutines` for async work
- **Data layer** — Room entities + DAOs, Firestore sync service, DataStore preferences
- **Billing layer** — `PurchaseManager` wraps Google Play Billing, exposes `isProUser` flow

---

## Project Structure

```
app/src/main/java/com/yetzira/ContractorCashFlowAndroid/
├── billing/            # Google Play Billing integration (PurchaseManager, PurchaseViewModel)
├── data/
│   ├── local/          # Room database, entities, DAOs
│   ├── preferences/    # DataStore (currency, language, theme, notifications)
│   └── repository/     # Data repositories (Project, Expense, Invoice, Labor, Client)
├── export/             # JSON export service and data snapshot models
├── locale/             # RTL / language helpers
├── network/            # Network connectivity checker
├── notification/       # Notification scheduling (invoice reminders, budget warnings)
├── services/           # OCR service (ML Kit)
├── sync/               # Firestore sync service
└── ui/
    ├── analytics/      # Analytics screen and ViewModel
    ├── clients/        # Clients list, detail, create, edit
    ├── components/     # Shared composables (StatPill, WorkerAvatar, SearchBar…)
    ├── expenses/       # Expenses list, detail, create, edit, receipt scanner
    ├── invoices/       # Invoices list, detail, create, edit
    ├── labor/          # Workers list, add, edit
    ├── navigation/     # NavGraphs, KablanProNavigationShell
    ├── paywall/        # PaywallScreen + PaywallSheet (modal bottom sheet)
    ├── projects/       # Projects list, detail, create, edit
    ├── scan/           # Receipt scan + review screens
    ├── settings/       # Settings screen and ViewModel
    └── theme/          # Color, Type, Shape tokens
```

---

## Free vs Pro

| Feature | Free | Pro |
|---|---|---|
| Projects | 1 | Unlimited |
| Workers | 2 | Unlimited |
| Expenses | ✓ | ✓ |
| Invoices | ✓ | ✓ |
| Analytics | ✓ | ✓ |
| Cloud Sync | ✓ | ✓ |
| Receipt Scan | ✓ | ✓ |

**Pro plans:**
- **KablanPro Monthly** — ₪69.90 / month
- **KablanPro Yearly** — ₪349.90 / year (save ~17%)

Subscriptions are managed via Google Play Billing. The paywall opens as a modal bottom sheet above any screen that hits a free-tier limit.

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11
- A Firebase project with:
  - Firestore enabled
  - Google Sign-In enabled in Firebase Auth
  - `google-services.json` placed at `app/google-services.json`
- A Google Play Console project with:
  - In-app products configured: `kablanpro_monthly`, `kablanpro_yearly`

### Clone

```bash
git clone https://github.com/your-org/ContractorCashFlowAndroid.git
cd ContractorCashFlowAndroid
```

### Firebase Setup

1. Create a project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package name `com.yetzira.ContractorCashFlowAndroid`
3. Download `google-services.json` and place it at `app/google-services.json`
4. Enable **Cloud Firestore** and **Google Sign-In** in Firebase Auth

### local.properties

Make sure `local.properties` contains your Android SDK path:

```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```

---

## Build & Run

```bash
# Debug build
./gradlew :app:assembleDebug

# Install on connected device
./gradlew :app:installDebug

# Run unit tests then install (default for assembleDebug)
./gradlew :app:assembleDebug
```

> **Note:** `assembleDebug` and `installDebug` automatically run unit tests first (configured in `build.gradle.kts`).

### Clean build (if you hit duplicate-class errors)

```bash
./gradlew --stop
rm -rf app/build
./gradlew :app:assembleDebug
```

---

## Testing

```bash
# Unit tests
./gradlew :app:testDebugUnitTest

# Check for duplicate class artifacts (runs automatically on preBuild)
./gradlew :app:verifyNoDuplicateBuildArtifacts
```

Test files live in `app/src/test/` and cover:
- Currency formatting
- Entity enum validation
- Project export service
- Settings ViewModel logic

---

## Troubleshooting

### `Type X is defined multiple times`
Caused by duplicated generated artifacts in `app/build` (often from iCloud or Finder duplicating folders). Fix:
```bash
./gradlew --stop
rm -rf app/build
./gradlew :app:assembleDebug
```

### Google Sign-In not working
- Make sure `google-services.json` is up to date
- Verify SHA-1 fingerprint is registered in Firebase Console
- Check `default_web_client_id` is present in generated `strings.xml`

### Billing / Paywall not loading products
- Google Play Billing requires a real device (not emulator) with a signed APK
- Products must be active in Google Play Console
- Test with a test account added to the Play Console license testers list

---

## Legal Assets Required

Before publishing to Google Play, the following legal documents and assets must be created and hosted at publicly accessible URLs.

### 📄 Terms of Use
A Terms of Use (Terms of Service) page must be created and hosted. It should cover:
- Acceptance of terms
- Description of the service and subscription plans (Free / KablanPro Monthly / Yearly)
- User responsibilities and prohibited activities
- Subscription billing, renewal, and cancellation policy
- Google Play auto-renewal disclosure (required by Google Play policy)
- Limitation of liability
- Governing law and jurisdiction
- Contact information

> The URL must be linked from:
> - Google Play Store listing → "App details" → Terms of Service URL
> - Inside the app → Paywall screen **Terms of Use** button
> - Settings screen (if applicable)

---

### 🔒 Privacy Policy
A Privacy Policy page is **mandatory** for Google Play apps that collect or process personal data. It should cover:
- What data is collected (Google account email, Firebase Auth UID, project / expense / invoice / client data)
- How data is stored (locally via Room, optionally synced to Firebase Firestore)
- Third-party services used and their own privacy policies:
  - [Firebase / Google](https://policies.google.com/privacy)
  - [Google Play Billing](https://payments.google.com/payments/apis-secure/get_legal_document?ldo=0&ldt=privacynotice)
  - [ML Kit](https://developers.google.com/ml-kit/terms)
- Data retention and deletion policy
- User rights (access, correction, deletion of their data)
- How to contact the developer for data requests

> The URL must be linked from:
> - Google Play Store listing → "App details" → Privacy Policy URL (**required**, app will be rejected without it)
> - Inside the app → Paywall screen **Privacy Policy** button
> - Settings screen (if applicable)

---

### 🏪 Google Play Store Listing Assets

The following assets must be prepared for the Play Store listing:

| Asset | Spec |
|---|---|
| App Icon | 512 × 512 px PNG, no alpha |
| Feature Graphic | 1024 × 500 px JPG or PNG |
| Phone Screenshots | Min 2, max 8 · 16:9 or 9:16 · min 320 px on shortest side |
| Short Description | Up to 80 characters |
| Full Description | Up to 4000 characters |
| App Category | Finance or Productivity |
| Content Rating | Complete questionnaire in Play Console |
| Contact Email | Developer support email (publicly shown) |
| Privacy Policy URL | Publicly accessible HTTPS link |
| Terms of Service URL | Publicly accessible HTTPS link (recommended) |

---

### 🔑 In-App Subscription Products (Google Play Console)

The following subscription products must be created in the Google Play Console before billing works in production:

| Product ID | Type | Price |
|---|---|---|
| `kablanpro_monthly` | Subscription | ₪69.90 / month |
| `kablanpro_yearly` | Subscription | ₪349.90 / year |

Each product needs:
- A **base plan** with billing period configured
- At least one **active offer**
- Status set to **Active** before the app can fetch products via the Billing Library

---

### 🔥 Firebase Configuration Checklist

- [ ] `google-services.json` added to `app/`
- [ ] SHA-1 and SHA-256 fingerprints registered in Firebase Console (for both debug **and** release keystores)
- [ ] Firebase Auth → Google Sign-In provider enabled
- [ ] Firestore database created (start in test mode, tighten rules before release)
- [ ] Firestore security rules reviewed — ensure users can only read/write their own data
- [ ] Firebase project linked to Google Play app (for Play Integrity / billing validation if needed)

---

### 🔐 Release Keystore

Before publishing to Google Play:
- [ ] Generate a release keystore (`keytool -genkey ...`) and store it securely
- [ ] Configure signing in `app/build.gradle.kts` under `signingConfigs`
- [ ] Upload the release key SHA-1 / SHA-256 to Firebase Console
- [ ] Enroll in **Google Play App Signing** (recommended — Google holds the upload key)
- [ ] Never commit the `.jks` keystore or its credentials to version control

---

## License

Private — All rights reserved © 2026 Yetzira

