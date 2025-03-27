# Energy App

**Energy App** is a sample Android application built using Kotlin, Jetpack Compose, and Firebase. It demonstrates user authentication, device management, energy consumption simulation, and push notifications, all while following modern Android architecture principles.

## Features

- **User Authentication**  
  Provides email and password-based registration, login, and password reset flows using Firebase Authentication.

- **Simulated Energy Consumption**  
  The app calculates energy usage in real-time based on each device’s power rating and how long it has been turned on.

- **Device Management**  
  Users can add, edit, and remove devices. Each device has a name, power rating (in watts), and on/off status.

- **Usage Limits & Notifications**  
  Users can set daily, weekly, or monthly energy usage thresholds. The app monitors total usage and notifies users (via Firebase Cloud Messaging) when limits are exceeded.

- **Additional Settings**  
  Options for enabling/disabling local and push notifications, auto-optimization, cost coefficients, and custom update intervals.

## Architecture

The project follows the **MVVM** pattern with **Kotlin Coroutines** and **Flow** for asynchronous data processing and state management.

- **UI Layer (Jetpack Compose)**  
  - Composable screens (e.g., `LoginScreen`, `DeviceListScreen`, `SettingsScreen`) declared with `@Composable` functions.  
  - Observes state from ViewModels to update UI reactively.

- **ViewModel Layer**  
  - Business logic in `AuthViewModel`, `DeviceViewModel`, `UsageLimitViewModel`.  
  - Uses Coroutines & Flow to handle background tasks and push updates to the UI.

- **Repository Layer**  
  - Data operations (CRUD) with Firebase Firestore and Realtime Database are abstracted in `DeviceRepository`.  
  - This isolates Firebase logic from the rest of the application, making it easier to test and maintain.

- **Backend (Firebase)**  
  - **Authentication**: User login, registration, password resets.  
  - **Firestore / Realtime Database**: Storing device info, usage thresholds, and other settings.  
  - **Cloud Messaging (FCM)**: Sending push notifications when usage limits are exceeded.

## Project Structure


```bash
EnergyApp/
├── app/
│   ├── google-services.json           // Download from Firebase Console
│   ├── build.gradle.kts               // Module-level Gradle configuration (Kotlin DSL)
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           └── java/com/example/energyapp/
│               ├── MyApplication.kt   // Application class initializing Firebase
│               ├── MainActivity.kt    // Entry point using Jetpack Compose
│               ├── model/
│               │   └── Device.kt      // Data model for devices
│               ├── repository/
│               │   └── DeviceRepository.kt  // Data access abstraction layer
│               ├── ui/
│               │   ├── auth/
│               │   │   └── LoginScreen.kt  // User authentication UI
│               │   ├── devices/
│               │   │   └── DeviceListScreen.kt  // Device list UI
│               │   └── settings/
│               │       └── SettingsScreen.kt   // Settings UI (energy thresholds, notifications, etc.)
│               └── viewmodel/
│                   ├── AuthViewModel.kt        // Manages authentication state
│                   ├── DeviceViewModel.kt      // Manages device data and simulation
│                   └── UsageLimitViewModel.kt  // Manages energy usage limit settings
├── build.gradle.kts                   // Root-level (often empty or minimal)
├── settings.gradle.kts                // Settings, including dependencyResolutionManagement
└── gradle/
    └── libs.versions.toml             // Version Catalog for managing dependencies and plugins
```


## Getting Started

**Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/EnergyApp.git
   cd EnergyApp
```

**Configure Gradle:**

This project uses Gradle Version Catalog. Ensure that your settings.gradle.kts contains the proper dependencyResolutionManagement and pluginManagement blocks.

Sync the project with Gradle files in Android Studio.

**JDK Setup:**

Ensure that you have JDK 11 or higher installed and configured as your JAVA_HOME.

Verify JDK settings in Android Studio under File > Project Structure > SDK Location.

**Firebase Setup**

Create a Firebase project in the Firebase Console.

Register your Android app with the matching package name.

Download the google-services.json file and place it in the app/ directory.

Enable Firebase Authentication (Email/Password), Firestore, and Cloud Messaging.

**Build and Run**

Open the project in Android Studio.

Sync the Gradle files and run the app on an emulator or physical device.

## Dependencies
Key libraries and tools include:

Kotlin (2.0.0)

Android Gradle Plugin (8.8.0)

Jetpack Compose (with Material3)

Firebase (Auth, Firestore, Messaging)

Coroutines & Flow

Navigation Compose (optional)

All dependencies are managed via gradle/libs.versions.toml (Version Catalog).

## License
This project is licensed under the MIT License. See the LICENSE file for details.
