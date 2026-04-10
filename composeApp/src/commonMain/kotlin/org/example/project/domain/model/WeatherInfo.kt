package org.example.project.domain.model

data class WeatherInfo(
    val temperature: Double,
    val windSpeed: Double,
    val humidity: Int,
    val weatherCode: Int,
    val time: String,
    val cityName: String,
) {
    // WMO weather interpretation codes → human-readable description
    val weatherDescription: String
        get() = when (weatherCode) {
            0          -> "Clear Sky"
            1, 2, 3    -> "Partly Cloudy"
            45, 48     -> "Fog"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snow"
            80, 81, 82 -> "Rain Showers"
            95         -> "Thunderstorm"
            96, 99     -> "Thunderstorm with Hail"
            else       -> "Unknown"
        }

    val weatherEmoji: String
        get() = when (weatherCode) {
            0          -> "☀️"
            1, 2, 3    -> "⛅"
            45, 48     -> "🌫️"
            51, 53, 55 -> "🌦️"
            61, 63, 65 -> "🌧️"
            71, 73, 75 -> "❄️"
            80, 81, 82 -> "🌨️"
            95, 96, 99 -> "⛈️"
            else       -> "🌡️"
        }
}
