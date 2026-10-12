package com.example.weatherapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherPresentationTest {
    @Test
    fun iconAcceptsAbsoluteAndProtocolRelativeCdnUrls() {
        val url = "https://cdn.weatherapi.com/weather/64x64/day/113.png"
        assertEquals(url, weatherIconUrl(url))
        assertEquals(url, weatherIconUrl(url.removePrefix("https:")))
    }

    @Test
    fun iconRejectsUntrustedOrMalformedUrls() {
        listOf(null, "", "not a url", "http://cdn.weatherapi.com/icon.png",
            "https://example.com/icon.png", "https://cdn.weatherapi.com@example.com/icon.png"
        ).forEach { assertNull(weatherIconUrl(it)) }
    }

    @Test
    fun missingOrMalformedTimeDoesNotThrow() {
        assertEquals("12:00", hourlyTime("2026-10-09 12:00"))
        assertEquals("12:00", hourlyTime("12:00"))
        assertEquals("—", hourlyTime(null))
        assertEquals("—", hourlyTime(""))
    }

    @Test
    fun missingValuesAreNotDisplayedAsZeroWeather() {
        assertEquals("—", (null as Double?).weatherValue("°C"))
        assertEquals("—", Double.NaN.weatherValue("°C"))
        assertEquals("—", Double.POSITIVE_INFINITY.weatherValue(" km/h"))
        assertEquals("—", (null as Int?).percentage())
        assertEquals("—", 101.percentage())
        assertEquals("0%", 0.percentage())
    }
}
