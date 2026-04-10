package org.example.project.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeatherDto,
)

@Serializable
data class CurrentWeatherDto(
    val time: String,
    @SerialName("temperature_2m")       val temperature: Double,
    @SerialName("wind_speed_10m")       val windSpeed: Double,
    @SerialName("weather_code")         val weatherCode: Int,
    @SerialName("relative_humidity_2m") val humidity: Int,
)
