package org.example.project.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.example.project.data.remote.dto.WeatherResponseDto

class WeatherApi(private val client: HttpClient) {

    suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherResponseDto {
        return client.get(BASE_URL) {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            // Request specific current-weather fields
            parameter("current", "temperature_2m,wind_speed_10m,weather_code,relative_humidity_2m")
            parameter("timezone", "auto")
        }.body()
    }

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/v1/forecast"
    }
}
