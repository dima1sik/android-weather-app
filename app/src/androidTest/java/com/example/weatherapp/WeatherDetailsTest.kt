package com.example.weatherapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.weatherapp.api.Current
import com.example.weatherapp.api.Forecast
import com.example.weatherapp.api.ForecastDay
import com.example.weatherapp.api.Location
import com.example.weatherapp.api.WeatherModel
import com.example.weatherapp.ui.theme.WeatherAppTheme
import org.junit.Rule
import org.junit.Test

class WeatherDetailsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyForecastShowsCurrentWeatherAndUnavailableMessage() {
        showWeather(Forecast(days = emptyList()))
        composeRule.onNodeWithText("Vilnius").assertIsDisplayed()
        composeRule.onNodeWithText("Hourly forecast is unavailable for this location.").assertIsDisplayed()
    }

    @Test
    fun nullForecastEntriesDoNotCrashRendering() {
        showWeather(Forecast(days = listOf(null, ForecastDay(hours = listOf(null)))))
        composeRule.onNodeWithText("Hourly forecast is unavailable for this location.").assertIsDisplayed()
    }

    private fun showWeather(forecast: Forecast) {
        composeRule.setContent {
            WeatherAppTheme {
                WeatherDetails(WeatherModel(
                    current = Current(temperatureC = 18.5),
                    location = Location(name = "Vilnius"),
                    forecast = forecast
                ))
            }
        }
    }
}
