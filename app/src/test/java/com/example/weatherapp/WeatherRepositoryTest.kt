package com.example.weatherapp

import com.example.weatherapp.api.NetworkResponse
import com.example.weatherapp.api.WeatherApi
import com.example.weatherapp.api.WeatherModel
import com.example.weatherapp.api.WeatherRepository
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: WeatherRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(WeatherApi::class.java)
        repository = WeatherRepository(api, "test-key")
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun requestUsesForecastEndpointOneDayAndEncodesLocation() = runTest {
        server.enqueue(MockResponse().setBody(VALID_WEATHER))

        val result = repository.getWeather("New York") as NetworkResponse.Success
        assertEquals(18.5, result.data.current?.temperatureC)
        assertEquals(60, result.data.current?.humidity)
        assertEquals(25, result.data.forecast?.days?.firstOrNull()?.day?.rainChance)
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/v1/forecast.json", request.requestUrl?.encodedPath)
        assertEquals("New York", request.requestUrl?.queryParameter("q"))
        assertEquals("1", request.requestUrl?.queryParameter("days"))
        assertEquals("test-key", request.requestUrl?.queryParameter("key"))
    }

    @Test
    fun missingKeyDoesNotSendRequest() = runTest {
        val api = object : WeatherApi {
            override suspend fun getWeather(apiKey: String, city: String, days: Int): Response<WeatherModel> {
                fail("No request should be sent without an API key")
                return Response.success(null)
            }
        }
        val result = WeatherRepository(api, " ").getWeather("Vilnius") as NetworkResponse.Error
        assertTrue(result.message.contains("WEATHER_API_KEY"))
        assertEquals(0, server.requestCount)
    }

    @Test
    fun httpErrorsReturnUsefulMessagesWithoutLeakingResponseBody() = runTest {
        val expected = mapOf(
            400 to "Location not found",
            401 to "API key",
            403 to "API key",
            429 to "request limit",
            503 to "unavailable"
        )
        expected.forEach { (code, message) ->
            server.enqueue(MockResponse().setResponseCode(code).setBody("secret-body-test-key"))
            val result = repository.getWeather("Vilnius") as NetworkResponse.Error
            assertTrue("HTTP $code: ${result.message}", result.message.contains(message))
            assertFalse(result.message.contains("test-key"))
        }
    }

    @Test
    fun emptySuccessfulBodyTerminatesWithAnError() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))
        val result = repository.getWeather("Vilnius") as NetworkResponse.Error
        assertTrue(result.message.contains("incomplete"))
    }

    @Test
    fun monthlyQuotaErrorIsNotMistakenForAnInvalidKey() = runTest {
        server.enqueue(MockResponse().setResponseCode(403)
            .setBody("""{"error":{"code":2007,"message":"API key quota exceeded"}}"""))
        val result = repository.getWeather("Vilnius") as NetworkResponse.Error
        assertTrue(result.message.contains("quota"))
        assertFalse(result.message.contains("invalid"))
    }

    @Test
    fun malformedErrorBodyFallsBackToHttpStatus() = runTest {
        server.enqueue(MockResponse().setResponseCode(503).setBody("<html>unavailable</html>"))
        val result = repository.getWeather("Vilnius") as NetworkResponse.Error
        assertTrue(result.message.contains("unavailable"))
    }

    @Test
    fun missingLocationOrCurrentTemperatureIsRejected() = runTest {
        listOf("{}", """{"location":{"name":"Vilnius"},"current":{}}""").forEach { body ->
            server.enqueue(MockResponse().setBody(body))
            assertTrue(repository.getWeather("Vilnius") is NetworkResponse.Error)
        }
    }

    @Test
    fun malformedJsonReturnsErrorInsteadOfThrowing() = runTest {
        server.enqueue(MockResponse().setBody("<html>service unavailable</html>"))
        val result = repository.getWeather("Vilnius") as NetworkResponse.Error
        assertTrue(result.message, result.message.contains("invalid data"))
    }

    @Test
    fun optionalForecastCanBeAbsentOrEmpty() = runTest {
        listOf("", ""","forecast":{"forecastday":[]}""").forEach { forecast ->
            server.enqueue(MockResponse().setBody(
                """{"location":{"name":"Vilnius"},"current":{"temp_c":18.5}$forecast}"""
            ))
            val result = repository.getWeather("Vilnius") as NetworkResponse.Success
            assertNull(result.data.forecast?.days?.firstOrNull())
        }
    }

    @Test
    fun networkErrorDoesNotExposeRequestUrl() = runTest {
        val api = ThrowingWeatherApi(IOException("https://api.weatherapi.com/?key=test-key"))
        val result = WeatherRepository(api, "test-key").getWeather("Vilnius") as NetworkResponse.Error
        assertTrue(result.message.contains("internet connection"))
        assertFalse(result.message.contains("test-key"))
    }

    @Test
    fun cancellationIsPropagated() = runTest {
        val api = ThrowingWeatherApi(CancellationException("search cancelled"))
        try {
            WeatherRepository(api, "test-key").getWeather("Vilnius")
            fail("Cancellation must not become an error state")
        } catch (_: CancellationException) {
            // Expected: a superseded search must stop without changing the new search state.
        }
    }

    private class ThrowingWeatherApi(private val error: Exception) : WeatherApi {
        override suspend fun getWeather(apiKey: String, city: String, days: Int): Response<WeatherModel> =
            throw error
    }

    companion object {
        private const val VALID_WEATHER = """{
            "location":{"name":"New York","country":"United States","localtime":"2026-10-09 12:00"},
            "current":{"temp_c":18.5,"humidity":60,"wind_kph":10.0,"condition":{"text":"Sunny","icon":"//cdn.weatherapi.com/weather/64x64/day/113.png"}},
            "forecast":{"forecastday":[{"day":{"daily_chance_of_rain":25},"hour":[{"time":"2026-10-09 12:00","temp_c":18.5,"feelslike_c":18.0}]}]}
        }"""
    }
}
