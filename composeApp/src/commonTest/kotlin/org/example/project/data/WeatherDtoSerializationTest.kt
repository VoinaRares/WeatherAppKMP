package org.example.project.data

import kotlinx.serialization.json.Json
import org.example.project.data.remote.dto.CurrentWeatherDto
import org.example.project.data.remote.dto.WeatherResponseDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Verifies that [WeatherResponseDto] and [CurrentWeatherDto] deserialize
 * correctly from JSON using kotlinx.serialization.
 *
 * These tests run without any network or coroutines — pure kotlinx.serialization + kotlin.test.
 */
class WeatherDtoSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ── happy path ────────────────────────────────────────────────────────

    @Test
    fun weatherResponseDto_validJson_deserializesAllFields() {
        val dto = json.decodeFromString<WeatherResponseDto>(VALID_RESPONSE_JSON)

        assertEquals(51.5,     dto.latitude,  absoluteTolerance = 0.01)
        assertEquals(-0.12,    dto.longitude, absoluteTolerance = 0.01)
        assertEquals("Europe/London", dto.timezone)
    }

    @Test
    fun currentWeatherDto_validJson_deserializesTemperature() {
        val dto = json.decodeFromString<WeatherResponseDto>(VALID_RESPONSE_JSON)
        assertEquals(12.3, dto.current.temperature, absoluteTolerance = 0.001)
    }

    @Test
    fun currentWeatherDto_validJson_deserializesWindSpeed() {
        val dto = json.decodeFromString<WeatherResponseDto>(VALID_RESPONSE_JSON)
        assertEquals(18.5, dto.current.windSpeed, absoluteTolerance = 0.001)
    }

    @Test
    fun currentWeatherDto_validJson_deserializesHumidity() {
        val dto = json.decodeFromString<WeatherResponseDto>(VALID_RESPONSE_JSON)
        assertEquals(75, dto.current.humidity)
    }

    @Test
    fun currentWeatherDto_validJson_deserializesWeatherCode() {
        val dto = json.decodeFromString<WeatherResponseDto>(VALID_RESPONSE_JSON)
        assertEquals(3, dto.current.weatherCode)
    }

    @Test
    fun currentWeatherDto_validJson_deserializesTime() {
        val dto = json.decodeFromString<WeatherResponseDto>(VALID_RESPONSE_JSON)
        assertEquals("2024-06-01T14:00", dto.current.time)
    }

    // ── lenient / unknown keys ─────────────────────────────────────────────

    @Test
    fun weatherResponseDto_extraUnknownFields_ignoredWithoutError() {
        // Open-Meteo adds extra fields we don't model; this must not throw
        val dto = json.decodeFromString<WeatherResponseDto>(JSON_WITH_EXTRA_FIELDS)
        assertEquals(48.8566, dto.latitude, absoluteTolerance = 0.01)
    }

    // ── error cases ───────────────────────────────────────────────────────

    @Test
    fun weatherResponseDto_missingRequiredField_throwsException() {
        // 'current' block is missing entirely
        assertFailsWith<Exception> {
            json.decodeFromString<WeatherResponseDto>(JSON_MISSING_CURRENT)
        }
    }

    // ── serialization round-trip (DTO → JSON → DTO) ────────────────────

    @Test
    fun currentWeatherDto_roundTrip_preservesValues() {
        val original = CurrentWeatherDto(
            time        = "2024-01-01T10:00",
            temperature = 22.5,
            windSpeed   = 5.0,
            weatherCode = 0,
            humidity    = 50,
        )
        val serialized   = Json.encodeToString(CurrentWeatherDto.serializer(), original)
        val deserialized = Json.decodeFromString<CurrentWeatherDto>(serialized)
        assertEquals(original, deserialized)
    }

    // ── test fixtures ─────────────────────────────────────────────────────

    companion object {
        val VALID_RESPONSE_JSON = """
            {
              "latitude": 51.5,
              "longitude": -0.12,
              "timezone": "Europe/London",
              "current": {
                "time": "2024-06-01T14:00",
                "interval": 900,
                "temperature_2m": 12.3,
                "wind_speed_10m": 18.5,
                "weather_code": 3,
                "relative_humidity_2m": 75
              }
            }
        """.trimIndent()

        val JSON_WITH_EXTRA_FIELDS = """
            {
              "latitude": 48.8566,
              "longitude": 2.3522,
              "timezone": "Europe/Paris",
              "generationtime_ms": 0.543,
              "utc_offset_seconds": 3600,
              "elevation": 38.0,
              "current": {
                "time": "2024-06-01T12:00",
                "interval": 900,
                "temperature_2m": 20.1,
                "wind_speed_10m": 8.2,
                "weather_code": 1,
                "relative_humidity_2m": 60
              }
            }
        """.trimIndent()

        val JSON_MISSING_CURRENT = """
            {
              "latitude": 51.5,
              "longitude": -0.12,
              "timezone": "Europe/London"
            }
        """.trimIndent()
    }
}
