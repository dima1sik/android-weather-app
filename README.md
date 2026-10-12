# WeatherNow

An Android weather app built with Kotlin, Jetpack Compose and Retrofit. It loads approximate weather for the public IP address at startup and lets users search by city or another location supported by WeatherAPI.

## Features

- Current temperature, conditions, wind and humidity
- Today’s rain probability and hourly forecast, including feels-like temperature
- Loading, error and retry states; a new search supersedes the previous request
- Theme-aware, scrollable interface

## Stack and structure

Kotlin · Jetpack Compose / Material 3 · ViewModel / StateFlow · Coroutines · Retrofit / Gson · Coil

- `WeatherPage.kt`: Compose interface
- `WeatherViewModel.kt`: search state and request lifecycle
- `api/`: HTTP client, response models and error handling
- `app/src/test/`: HTTP, state and presentation regression tests

## Run

Requires JDK 21, Android SDK 36 and a device or emulator running Android 7.0 (API 24) or later. Gradle 9.3.1 is supplied by the wrapper. Use an Android Studio version that supports Android Gradle Plugin 9.1.

1. Open the project in Android Studio and install the required SDK components.
2. Get a key from [WeatherAPI](https://www.weatherapi.com/).
3. Add the following line to the project’s `local.properties`, keeping any existing `sdk.dir` line:

   ```properties
   WEATHER_API_KEY=your_weatherapi_key
   ```

4. Sync Gradle, then run the `app` configuration on your device or emulator. Rebuild after changing the key.

The key and local SDK path stay in the ignored `local.properties`. An unconfigured build displays a configuration error instead of sending requests. A key embedded in an Android APK can be extracted; this setup is for a portfolio app. A production service should apply API restrictions and use a backend where appropriate.

## Build and checks

From the repository root, with the Android SDK configured:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

On Linux or macOS:

```sh
bash gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Local tests use a mock HTTP server and fake responses, so they need no API key or live weather connection. Compose regression tests cover empty forecast rendering; run `:app:connectedDebugAndroidTest` with a device or emulator connected.

## Limits

Weather data and icons require an internet connection. There is no offline cache or GPS permission: IP-based location is approximate and may reflect a VPN or mobile network. Forecast availability depends on WeatherAPI’s response and account limits; the app requests one day and handles an absent hourly forecast.
