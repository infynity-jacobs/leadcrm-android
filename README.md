# LeadCRM Android

Android mobile companion application for LeadCRM.

## Current Status

Initial Android project foundation is complete.

- Kotlin
- Jetpack Compose
- AndroidX
- Android SDK 36
- Minimum SDK 26
- Java 17
- Gradle 8.13
- Android Gradle Plugin 8.13.0
- Kotlin 2.2.20
- Compose Compiler plugin
- Retrofit/API integration will be added in a later phase

## Build

From the project root:

    ./gradlew assembleDebug

Debug APK:

    app/build/outputs/apk/debug/app-debug.apk

## Architecture Direction

The application will use the existing LeadCRM FastAPI backend as the single source of truth.

Planned areas:

- Authentication
- Home dashboard
- Leads
- Tasks
- Calendar
- VOIP integration
- More / Settings

The Android application will not maintain a separate CRM backend or database.

## Security

Sensitive backend, database, PBX, Yeastar, SMTP, and infrastructure credentials must never be stored in the Android application.

Authentication tokens and sensitive local data will use Android secure storage mechanisms.

## Development Environment

Recommended development environment for the current project:

- Ubuntu 22.04 LTS
- 4 CPU cores or more
- 8 GB RAM or more
- Java 17
- Android SDK 36
