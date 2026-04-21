# StockDemo - Warehouse Management System

**StockDemo** is a modern Android application designed for efficient warehouse management, integrated with Artificial Intelligence (AI) to assist users. The app is built following **Clean Architecture** principles and utilizes the latest technologies in the Android ecosystem.

## 🚀 Key Features

*   **Stock In/Out Management:** Record goods receipt and dispatch transactions quickly.
*   **Inventory Control:** Manage real-time stock lists and perform audits.
*   **Transaction History:** Look up detailed history of all stock movements.
*   **AI Chat Integration:** Get answers and data analysis through an AI assistant (powered by a Python backend).
*   **Offline-First Support:** Uses Room Database for local storage and background synchronization when a connection is available.
*   **RFID Integration:** Supports scanning via dedicated RFID libraries.

## 🛠 Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Modern Declarative UI)
*   **Dependency Injection:** Hilt (Dagger)
*   **Architecture:** Clean Architecture (Domain, Data, UI Layers)
*   **Networking:** Retrofit 2 & OkHttp 4 (Dual-Backend connectivity)
*   **Local Database:** Room Persistence Library
*   **Concurrency:** Kotlin Coroutines & Flow
*   **Local Preferences:** Jetpack DataStore
*   **Background Tasks:** WorkManager & HiltWorker
*   **Navigation:** Navigation Compose

## 📂 Project Structure

The project is organized using a feature-based approach within the Clean Architecture layers:

```text
com.example.stockdemo/
├── core/               # Shared components (DI, Network, Theme, Utils)
├── feature/
│   ├── auth/           # Login and User Session management
│   ├── stock/          # Core Warehouse logic (In/Out, Sync, Local/Remote Data)
│   ├── chat/           # AI Chat functionality
│   └── home/           # Menu, Settings, and Dashboard
└── ui/
    └── navigation/     # NavGraph configuration and Screen definitions
```

## ⚙️ Setup & Configuration

### Prerequisites
*   Android Studio Ladybug (or newer)
*   JDK 11+
*   Android SDK 24 (Minimum SDK)

### API Configuration
The application connects to two different backend systems. You can modify the server IP addresses in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "STOCK_BASE_URL", "\"http://10.84.30.46:8686/api/\"")
buildConfigField("String", "PYTHON_BASE_URL", "\"http://10.84.30.46:8000/\"")
```

## 🏗 Build & Run

1.  Clone this repository.
2.  Open the project in Android Studio.
3.  Wait for Gradle to sync.
4.  Press **Run** to install the app on your physical device or emulator.

## 📝 License
This project is developed for internal warehouse management purposes.
