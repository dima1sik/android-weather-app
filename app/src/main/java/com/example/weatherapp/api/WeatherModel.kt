package com.example.weatherapp.api

data class WeatherModel(
    val current: Current,
    val location: Location,
    val forecast: Forecast?
)

data class Current(
    val condition: Condition,
    val feelslike_c: String,
    val humidity: String,
    val last_updated: String,
    val last_updated_epoch: String,
    val temp_c: String,
    val uv: String,
    val wind_dir: String,
    val wind_kph: String,
    val precip_mm: String
)

data class Condition(
    val icon: String,
    val text: String
)

data class Location(
    val country: String,
    val localtime: String,
    val name: String,
    val region: String
)

data class Forecast(
    val forecastday: List<Forecastday>
)

data class Forecastday(
    val date: String,
    val day: Day,
    val hour: List<Hour>
)

data class Day(
    val daily_chance_of_rain: String,
    val maxtemp_c: String,
    val mintemp_c: String,
    val condition: Condition
)

data class Hour(
    val time: String,
    val temp_c: String,
    val feelslike_c: String,
    val condition: Condition
)
