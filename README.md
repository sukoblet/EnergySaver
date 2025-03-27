EnergyApp
EnergyApp is an Android application built using Kotlin, Jetpack Compose, and Firebase. This sample project demonstrates key functionalities such as user authentication, device management, energy consumption simulation, and notifications—all integrated in a modern MVVM architecture.

Table of Contents
Features

Architecture

Project Structure

Setup Instructions

Usage

Dependencies

License

Features
User Authentication:
Firebase Authentication is used for user registration, login, and password reset.

Device Management:
Users can add, edit, and delete devices. Each device has properties such as name, power rating, and on/off status.

Simulated Energy Consumption:
The app calculates energy consumption in real time for each device based on its power rating and time of operation.

Usage Limits & Notifications:
Users can set daily, weekly, and monthly energy thresholds. The app monitors overall energy usage and notifies users when limits are exceeded.

Additional Settings:
Options for enabling/disabling local and push notifications, cost coefficient, and auto-optimization settings are provided.

Architecture
EnergyApp uses a modern MVVM (Model-View-ViewModel) architecture:

UI Layer (Jetpack Compose):
Built using Compose, the UI consists of declarative Composable functions (e.g., LoginScreen, DeviceListScreen, SettingsScreen) that observe state changes from ViewModels.

ViewModel Layer:
ViewModels such as AuthViewModel, DeviceViewModel, and UsageLimitViewModel manage UI state using Kotlin Flow and Coroutines.

Repository Layer:
The DeviceRepository abstracts access to data (Firebase Firestore and Firebase Realtime Database), handling CRUD operations for device data and settings.

Backend (Firebase):
Firebase provides Authentication, Firestore for data storage, and Cloud Messaging (FCM) for push notifications.

Project Structure
swift
Копіювати
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
Setup Instructions
Clone the Repository:

bash
Копіювати
git clone https://github.com/yourusername/EnergyApp.git
cd EnergyApp
Firebase Setup:

Create a project in the Firebase Console.

Add an Android app to your Firebase project using the package name com.example.energyapp.

Download the google-services.json file and place it in the app/ directory.

Enable Firebase Authentication (Email/Password), Firestore, and Firebase Cloud Messaging (FCM) in the Firebase Console.

Configure Gradle:

This project uses Gradle Version Catalog. Ensure that your settings.gradle.kts contains the proper dependencyResolutionManagement and pluginManagement blocks.

Sync the project with Gradle files in Android Studio.

JDK Setup:

Ensure that you have JDK 11 or higher installed and configured as your JAVA_HOME.

Verify JDK settings in Android Studio under File > Project Structure > SDK Location.

Usage
Build the App:
Open the project in Android Studio and click on "Build" → "Make Project" or run ./gradlew assembleDebug.

Run the App:
Use Android Studio’s run button or execute ./gradlew installDebug from the command line.

Explore the Code:

UI Layer: Check the Composable functions in the ui package.

ViewModel Layer: Business logic is managed in the viewmodel package.

Repository Layer: Data operations (CRUD) with Firebase are handled in the repository package.

Backend Integration: Firebase is used for authentication, real-time database, and notifications.

Dependencies
The project uses the following key dependencies:

Kotlin: 2.0.0

Android Gradle Plugin: 8.8.0

Jetpack Compose & Material3: Using Compose BOM (2024.04.01) and Material3 components.

Firebase: Authentication, Firestore, and Messaging.

Kotlin Coroutines & Flow: For asynchronous programming.

AndroidX Components: Core KTX, Lifecycle, Navigation, etc.

Dependencies and plugin versions are managed centrally via the gradle/libs.versions.toml file.

License
This project is licensed under the MIT License. See the LICENSE file for details.
