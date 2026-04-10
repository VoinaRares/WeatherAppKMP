package org.example.project.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.example.project.data.remote.WeatherApi
import org.example.project.data.repository.WeatherRepositoryImpl
import org.example.project.network.createHttpClient
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests [WeatherRepositoryImpl] end-to-end through a [MockEngine].
 *
 * No real network. The engine intercepts Ktor requests and returns
 * pre-canned JSON, exercising serialization + mapping in one shot.
 */
class WeatherRepositoryTest {

    // ── happy path ────────────────────────────────────────────────────────

    @Test
    fun getWeather_successResponse_emitsWeatherInfo() = runTest {
        val repository = buildRepository(responseJson = MOCK_WEATHER_JSON)

        val result = repository.getWeather(51.5074, -0.1278, "London").single()

        assertEquals("London",        result.cityName)
        assertEquals(12.3,            result.temperature, absoluteTolerance = 0.001)
        assertEquals(18.5,            result.windSpeed,   absoluteTolerance = 0.001)
        assertEquals(75,              result.humidity)
        assertEquals(3,               result.weatherCode)
        assertEquals("2024-06-01T14:00", result.time)
    }

    @Test
    fun getWeather_cityNamePassedThrough_storedOnResult() = runTest {
        val repository = buildRepository(responseJson = MOCK_WEATHER_JSON)

        val result = repository.getWeather(35.6762, 139.6503, "Tokyo").first()

        assertEquals("Tokyo", result.cityName)
    }

    @Test
    fun getWeather_weatherCode0_descriptionIsClearSky() = runTest {
        val repository = buildRepository(responseJson = MOCK_CLEAR_SKY_JSON)

        val result = repository.getWeather(40.7128, -74.006, "New York").single()

        assertEquals("Clear Sky", result.weatherDescription)
    }

    // ── error path ────────────────────────────────────────────────────────

    @Test
    fun getWeather_serverError_flowThrowsException() = runTest {
        val repository = buildRepository(
            responseJson   = """{"error": true, "reason": "Parameter not found"}""",
            statusCode     = HttpStatusCode.BadRequest,
        )

        assertFailsWith<Exception> {
            repository.getWeather(0.0, 0.0, "Nowhere").single()
        }
    }

    // ── request correctness ───────────────────────────────────────────────

    @Test
    fun getWeather_requestContainsLatLon_inQueryParams() = runTest {
        var capturedUrl = ""
        val mockEngine = MockEngine { request ->
            capturedUrl = request.url.toString()
            respond(
                content = ByteReadChannel(MOCK_WEATHER_JSON),
                status  = HttpStatusCode.OK,
                headers = jsonHeaders(),
            )
        }
        val repository = buildRepositoryWithEngine(mockEngine)

        repository.getWeather(48.8566, 2.3522, "Paris").single()

        assertContains(capturedUrl, "latitude=48.8566")
        assertContains(capturedUrl,"longitude=2.3522")
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private fun buildRepository(
        responseJson: String,
        statusCode: HttpStatusCode = HttpStatusCode.OK,
    ) = buildRepositoryWithEngine(
        MockEngine { _ ->
            respond(
                content = ByteReadChannel(responseJson),
                status  = statusCode,
                headers = jsonHeaders(),
            )
        }
    )

    private fun buildRepositoryWithEngine(engine: MockEngine): WeatherRepositoryImpl {
        val client = createHttpClient(engine)
        val api    = WeatherApi(client)
        return WeatherRepositoryImpl(api)
    }

    private fun jsonHeaders() =
        headersOf(HttpHeaders.ContentType, "application/json")

    // ── fixtures ──────────────────────────────────────────────────────────

    companion object {
        val MOCK_WEATHER_JSON = """
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

        val MOCK_CLEAR_SKY_JSON = """
            {
              "latitude": 40.71,
              "longitude": -74.01,
              "timezone": "America/New_York",
              "current": {
                "time": "2024-06-01T10:00",
                "interval": 900,
                "temperature_2m": 22.0,
                "wind_speed_10m": 5.0,
                "weather_code": 0,
                "relative_humidity_2m": 40
              }
            }
        """.trimIndent()
    }
}
