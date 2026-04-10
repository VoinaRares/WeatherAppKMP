package org.example.project.data.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.WeatherInfo

// Interface in commonMain — enables easy mocking in tests
interface WeatherRepository {
    fun getWeather(latitude: Double, longitude: Double, cityName: String): Flow<WeatherInfo>
}
