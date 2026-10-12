package com.example.weatherapp.api

import com.example.weatherapp.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class WeatherRepository(
    private val api: WeatherApi = RetrofitInstance.weatherApi,
    private val apiKey: String = BuildConfig.WEATHER_API_KEY
) {
    suspend fun getWeather(location: String): NetworkResponse<WeatherModel> {
        if (apiKey.isBlank()) {
            return NetworkResponse.Error("Add WEATHER_API_KEY to local.properties and rebuild the app.")
        }

        return try {
            val response = api.getWeather(apiKey, location)
            if (!response.isSuccessful) {
                NetworkResponse.Error(errorMessage(response))
            } else {
                val weather = response.body()
                if (weather?.current?.temperatureC?.isFinite() != true ||
                    weather.location?.name.isNullOrBlank()
                ) {
                    NetworkResponse.Error("The weather service returned incomplete data. Try again.")
                } else {
                    NetworkResponse.Success(weather)
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: MalformedJsonException) {
            NetworkResponse.Error("The weather service returned invalid data. Try again.")
        } catch (error: IOException) {
            NetworkResponse.Error("Unable to connect. Check your internet connection and try again.")
        } catch (error: JsonParseException) {
            NetworkResponse.Error("The weather service returned invalid data. Try again.")
        } catch (error: Exception) {
            // Exception messages may contain the request URL, including its API key.
            NetworkResponse.Error("Unable to load weather. Try again.")
        }
    }

    private fun errorMessage(response: Response<WeatherModel>): String {
        val apiErrorCode = response.errorBody()?.use { body ->
            try {
                Gson().fromJson(body.charStream(), ApiError::class.java)?.error?.code
            } catch (_: JsonParseException) {
                null
            } catch (_: IOException) {
                null
            }
        }
        return when (apiErrorCode) {
            1006 -> "Location not found. Check the name and try again."
            2006, 2008 -> "The weather API key is invalid or disabled."
            2007 -> "The weather service request limit has been reached. Check your account quota."
            2009 -> "The weather API account does not permit this request."
            else -> when (response.code()) {
                400 -> "Location not found. Check the name and try again."
                401 -> "The weather API key is invalid."
                403 -> "Weather service access was denied. Check your API key and account limits."
                429 -> "The weather service request limit has been reached. Try again later."
                in 500..599 -> "The weather service is unavailable. Try again later."
                else -> "Unable to load weather. Try again."
            }
        }
    }

    private data class ApiError(val error: ErrorDetails? = null)
    private data class ErrorDetails(val code: Int? = null)
}
