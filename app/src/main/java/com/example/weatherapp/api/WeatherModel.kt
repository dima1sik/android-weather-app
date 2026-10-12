package com.example.weatherapp.api

import com.google.gson.annotations.SerializedName

data class WeatherModel(
    val current: Current? = null,
    val location: Location? = null,
    val forecast: Forecast? = null
)

data class Current(
    val condition: Condition? = null,
    val humidity: Int? = null,
    @SerializedName("temp_c") val temperatureC: Double? = null,
    @SerializedName("wind_kph") val windKph: Double? = null
)

data class Condition(
    val icon: String? = null,
    val text: String? = null
)

data class Location(
    val country: String? = null,
    val localtime: String? = null,
    val name: String? = null,
    val region: String? = null
)

data class Forecast(
    @SerializedName("forecastday") val days: List<ForecastDay?>? = null
)

data class ForecastDay(
    val day: Day? = null,
    @SerializedName("hour") val hours: List<Hour?>? = null
)

data class Day(
    @SerializedName("daily_chance_of_rain") val rainChance: Int? = null
)

data class Hour(
    val time: String? = null,
    @SerializedName("temp_c") val temperatureC: Double? = null,
    @SerializedName("feelslike_c") val feelsLikeC: Double? = null,
    val condition: Condition? = null
)
