package com.example.weatherapp

import com.example.weatherapp.api.Current
import com.example.weatherapp.api.Location
import com.example.weatherapp.api.NetworkResponse
import com.example.weatherapp.api.WeatherApi
import com.example.weatherapp.api.WeatherModel
import com.example.weatherapp.api.WeatherRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {
    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun startupUsesApproximateIpLocation() = runTest {
        val requests = mutableListOf<String>()
        val viewModel = viewModel { location ->
            requests += location
            Response.success(weather(location))
        }
        runCurrent()
        assertEquals(listOf("auto:ip"), requests)
        assertTrue(viewModel.weatherResult.value is NetworkResponse.Success)
    }

    @Test
    fun searchTrimsInputAndRetryUsesLastLocation() = runTest {
        val requests = mutableListOf<String>()
        val viewModel = viewModel { location ->
            requests += location
            Response.success(weather(location))
        }
        runCurrent()
        viewModel.getData("  Vilnius  ")
        runCurrent()
        viewModel.retry()
        runCurrent()
        assertEquals(listOf("auto:ip", "Vilnius", "Vilnius"), requests)
    }

    @Test
    fun blankSearchReturnsValidationErrorWithoutAnApiRequest() = runTest {
        val requests = mutableListOf<String>()
        val viewModel = viewModel { location ->
            requests += location
            Response.success(weather(location))
        }
        runCurrent()
        viewModel.getData(" \t ")
        runCurrent()
        assertEquals(listOf("auto:ip"), requests)
        assertTrue(viewModel.weatherResult.value is NetworkResponse.Error)
    }

    @Test
    fun latestSearchWinsEvenIfCancelledRequestReturnsLate() = runTest {
        val oldResponse = CompletableDeferred<Response<WeatherModel>>()
        val newResponse = CompletableDeferred<Response<WeatherModel>>()
        val viewModel = viewModel { location ->
            when (location) {
                "Old" -> withContext(NonCancellable) { oldResponse.await() }
                "New" -> newResponse.await()
                else -> Response.success(weather(location))
            }
        }
        runCurrent()
        viewModel.getData("Old")
        runCurrent()
        viewModel.getData("New")
        runCurrent()
        assertEquals(NetworkResponse.Loading, viewModel.weatherResult.value)
        newResponse.complete(Response.success(weather("New")))
        runCurrent()
        oldResponse.complete(Response.success(weather("Old")))
        runCurrent()
        val result = viewModel.weatherResult.value as NetworkResponse.Success
        assertEquals("New", result.data.location?.name)
    }

    @Test
    fun missingKeySettlesOnConfigurationErrorInsteadOfLoading() = runTest {
        val viewModel = WeatherViewModel(WeatherRepository(api { Response.success(weather(it)) }, ""))
        runCurrent()
        assertTrue(viewModel.weatherResult.value is NetworkResponse.Error)
    }

    private fun viewModel(handler: suspend (String) -> Response<WeatherModel>) =
        WeatherViewModel(WeatherRepository(api(handler), "test-key"))

    private fun api(handler: suspend (String) -> Response<WeatherModel>) = object : WeatherApi {
        override suspend fun getWeather(apiKey: String, city: String, days: Int): Response<WeatherModel> =
            handler(city)
    }

    private fun weather(location: String) = WeatherModel(
        current = Current(temperatureC = 18.5), location = Location(name = location)
    )
}
