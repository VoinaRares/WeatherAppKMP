package org.example.project.domain

import org.example.project.domain.model.WeatherInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/**
 * Pure unit tests for [WeatherInfo] computed properties.
 * No coroutines, no framework integration — just kotlin.test assertions.
 */
class WeatherInfoTest {

    // ── weatherDescription ────────────────────────────────────────────────

    @Test
    fun weatherDescription_clearSky_returnsCorrectLabel() {
        assertEquals("Clear Sky", buildInfo(weatherCode = 0).weatherDescription)
    }

    @Test
    fun weatherDescription_partlyCloudyCodes_allReturnPartlyCloudy() {
        listOf(1, 2, 3).forEach { code ->
            assertEquals(
                "Partly Cloudy",
                buildInfo(weatherCode = code).weatherDescription,
                "Expected 'Partly Cloudy' for code $code",
            )
        }
    }

    @Test
    fun weatherDescription_fog_returnsCorrectLabel() {
        listOf(45, 48).forEach { code ->
            assertEquals("Fog", buildInfo(weatherCode = code).weatherDescription)
        }
    }

    @Test
    fun weatherDescription_drizzle_returnsCorrectLabel() {
        listOf(51, 53, 55).forEach { code ->
            assertEquals("Drizzle", buildInfo(weatherCode = code).weatherDescription)
        }
    }

    @Test
    fun weatherDescription_rain_returnsCorrectLabel() {
        listOf(61, 63, 65).forEach { code ->
            assertEquals("Rain", buildInfo(weatherCode = code).weatherDescription)
        }
    }

    @Test
    fun weatherDescription_snow_returnsCorrectLabel() {
        listOf(71, 73, 75).forEach { code ->
            assertEquals("Snow", buildInfo(weatherCode = code).weatherDescription)
        }
    }

    @Test
    fun weatherDescription_rainShowers_returnsCorrectLabel() {
        listOf(80, 81, 82).forEach { code ->
            assertEquals("Rain Showers", buildInfo(weatherCode = code).weatherDescription)
        }
    }

    @Test
    fun weatherDescription_thunderstorm_returnsCorrectLabel() {
        assertEquals("Thunderstorm", buildInfo(weatherCode = 95).weatherDescription)
    }

    @Test
    fun weatherDescription_thunderstormWithHail_returnsCorrectLabel() {
        listOf(96, 99).forEach { code ->
            assertEquals("Thunderstorm with Hail", buildInfo(weatherCode = code).weatherDescription)
        }
    }

    @Test
    fun weatherDescription_unknownCode_returnsUnknown() {
        assertEquals("Unknown", buildInfo(weatherCode = 999).weatherDescription)
    }

    // ── weatherEmoji ──────────────────────────────────────────────────────

    @Test
    fun weatherEmoji_clearSky_returnsSun() {
        assertEquals("☀️", buildInfo(weatherCode = 0).weatherEmoji)
    }

    @Test
    fun weatherEmoji_partlyCloudy_returnsCloudSun() {
        assertEquals("⛅", buildInfo(weatherCode = 2).weatherEmoji)
    }

    @Test
    fun weatherEmoji_snow_returnsSnowflake() {
        assertEquals("❄️", buildInfo(weatherCode = 73).weatherEmoji)
    }

    @Test
    fun weatherEmoji_thunderstorm_returnsThunderstorm() {
        assertEquals("⛈️", buildInfo(weatherCode = 95).weatherEmoji)
    }

    @Test
    fun weatherEmoji_unknownCode_returnsThermometer() {
        assertEquals("🌡️", buildInfo(weatherCode = 0).weatherEmoji.let {
            buildInfo(weatherCode = 999).weatherEmoji
        })
    }

    // ── data class equality ───────────────────────────────────────────────

    @Test
    fun weatherInfo_equalInstances_areEqual() {
        val a = buildInfo()
        val b = buildInfo()
        assertEquals(a, b)
    }

    @Test
    fun weatherInfo_diffTemperature_areNotEqual() {
        val a = buildInfo(temperature = 10.0)
        val b = buildInfo(temperature = 20.0)
        assertNotNull(a)
        assertNotNull(b)
        assertNotEquals(a, b)
    }

    // ── helper ────────────────────────────────────────────────────────────

    private fun buildInfo(
        temperature: Double = 15.0,
        windSpeed: Double   = 10.0,
        humidity: Int       = 60,
        weatherCode: Int    = 0,
        time: String        = "2024-01-01T12:00",
        cityName: String    = "Test City",
    ) = WeatherInfo(temperature, windSpeed, humidity, weatherCode, time, cityName)
}
