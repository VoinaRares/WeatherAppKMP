package org.example.project.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.example.project.data.repository.WeatherRepository
import org.example.project.domain.model.WeatherInfo

/**
 * In-memory test double for [WeatherRepository].
 *
 * Configure [result] before each test:
 *  - provide a [WeatherInfo] for the happy path
 *  - set [shouldThrow] to test error handling
 */
class FakeWeatherRepository : WeatherRepository {

    var result: WeatherInfo = defaultWeather()
    var shouldThrow: Boolean = false
    var throwable: Throwable = RuntimeException("Network error")

    // Records the last arguments received — use in assertions
    var lastLatitude: Double? = null
    var lastLongitude: Double? = null
    var lastCityName: String? = null

    override fun getWeather(
        latitude: Double,
        longitude: Double,
        cityName: String,
    ): Flow<WeatherInfo> = flow {
        lastLatitude  = latitude
        lastLongitude = longitude
        lastCityName  = cityName

        if (shouldThrow) throw throwable
        emit(result)
    }

    companion object {
        fun defaultWeather() = WeatherInfo(
            temperature = 15.0,
            windSpeed   = 10.0,
            humidity    = 65,
            weatherCode = 1,
            time        = "2024-01-01T12:00",
            cityName    = "London",
        )
    }
}
