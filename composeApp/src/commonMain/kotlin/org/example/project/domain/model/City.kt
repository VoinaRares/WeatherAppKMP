package org.example.project.domain.model

data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

val availableCities = listOf(
    City("London",   51.5074,  -0.1278),
    City("New York", 40.7128, -74.0060),
    City("Tokyo",    35.6762, 139.6503),
    City("Paris",    48.8566,   2.3522),
    City("Sydney",  -33.8688, 151.2093),
)
