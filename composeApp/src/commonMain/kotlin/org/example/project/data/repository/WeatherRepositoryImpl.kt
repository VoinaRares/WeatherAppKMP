package org.example.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.example.project.data.remote.WeatherApi
import org.example.project.domain.model.WeatherInfo

class WeatherRepositoryImpl(private val api: WeatherApi) : WeatherRepository {

    // Wrapping the suspend call in a Flow lets the ViewModel apply
    // onStart / catch / collect operators without extra boilerplate.
    override fun getWeather(
        latitude: Double,
        longitude: Double,
        cityName: String,
    ): Flow<WeatherInfo> = flow {
        val dto = api.fetchWeather(latitude, longitude)
        emit(
            WeatherInfo(
                temperature = dto.current.temperature,
                windSpeed   = dto.current.windSpeed,
                humidity    = dto.current.humidity,
                weatherCode = dto.current.weatherCode,
                time        = dto.current.time,
                cityName    = cityName,
            )
        )
    }
}
