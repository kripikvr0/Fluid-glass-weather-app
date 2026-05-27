# CuacaGlass - Modern Android Weather Dashboard

CuacaGlass is a highly polished, interactive Android weather application built from the ground up using **Kotlin** and **Jetpack Compose**. It features a modern, real-time animated fluid background, smooth swipe-to-navigate gestures for managing multiple cities, multilingual support (English and Indonesian), and smart contextual insights powered by Google's **Gemini AI**.

## 🌟 Key Features

*   **Fluid Animated Background:** The app's backdrop is a dynamic, procedurally generated fluid canvas that visually reacts and adapts its color palettes based on real-time weather conditions and the time of day (Clear Sky, Clouds, Fog, Rain, Snow, Thunderstorm, Night time variants).
*   **Intuitive Gesture Navigation:** Seamlessly swipe left or right on the screen to page between your configured forecast locations, complete with smooth animations and interactive bottom navigation indicators. 
*   **Detailed Analytics Dashboard:** Track granular weather metrics including:
    *   Responsive temperature displays (Current, Apparent/Feels Like)
    *   12-Hour Hourly Forecast
    *   5-Day Extended Daily Forecast
    *   Granular localized attributes: Humidity, UV Index, and Wind Speed
*   **Gemini AI Smart Insights:** Employs the Gemini API to analyze current weather conditions and instantly generate brief, context-aware advice for the user (e.g. recommending sun protection or an umbrella).
*   **Bilingual Localization:** Fully localized in both English and Bahasa Indonesia.
*   **Customizable User Experience:** Choose between metric or imperial units, toggle manual or automatic dark mode matching the local sunset, and manage your location lists effortlessly.
*   **Offline Mode:** Caches the last known weather queries to ensure you have base metrics available even when offline or dealing with poor connectivity.

## 🛠 Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Declarative UI)
*   **Architecture:** MVVM Design Pattern (Model-View-ViewModel)
*   **Coroutines & Flows:** Used extensively for asynchronous data streams and seamless UI state observation.
*   **Gemini API:** Fully integrated system utilizing Gemini for dynamic on-device context analysis.
*   **Material Design 3:** Custom styling implementing M3 principles, responsive elements, edge-to-edge layout, typography pairings, and extensive use of Glassmorphism (GlassCards).
*   **Dependency Injection:** Clean separation of concerns with simple dependency injection methods configured.
*   **API Network/Serialization:** Handles network calls flawlessly capturing live weather responses payloading Geocoding mappings. 

## 🎨 Visual Identity

**"Realism meets Glassmorphism":** The application embraces a transparent, layered aesthetic where UI widgets sit beautifully stacked atop the rich animated canvas, simulating real-world layered glass depths.

*   `FluidBackground.kt`: A custom generative Compose canvas that renders multiple shifting gradients imitating cloud and sun depths.
*   `GlassCard.kt`: Defines the soft overlays with distinct blur implementations giving the panels a premium iOS-like tactile feel.

## 🚀 Getting Started

1.  **Clone the Repository** and open the project in **Android Studio MakerSuite/Giraffe** (or newer).
2.  **Wait for Gradle Sync.**
3.  **Run the Project:** Target a supported emulator (API level 24+) or a physical Android device. 
    `./gradlew assembleDebug`
    or natively via Android Studio `Run`.

## 📂 Project Structure Overview

```
app/src/main/java/com/example/
├── data/
│   └── model/           # Data classes mapping network responses (CurrentWeather, Hourly, etc.)
├── ui/
│   ├── components/      # Reusable visual widgets (FluidBackground, GlassCard, Animated Icons)
│   ├── screens/         # Main application navigation destinations (WeatherDashboard)
│   ├── WeatherViewModel.kt # Business Logic bridging model data with UI views
│   └── theme/           # Theming, Typography, Colors
└── MainActivity.kt      # Main execution point
```

## 📱 Interactive Usage 

1. **Swipe the Screen:** Drag left and right to iterate over your saved cities.
2. **Settings Icon (Top Right):** Tap to expand localization, unit preferences, and theme adjustments.
3. **Location Search:** Use the intuitive top search bar to query cities globally.

## ✨ Quality Assurances

*   **Responsive layouts:** Utilizes Compose spacing and alignments to scale correctly on devices of all form factors.
*   **State Preservations:** Handled through strictly typed UiStates encapsulating Loading, Success, Error states to ensure no unhandled exceptions are forwarded to the front end.
