package org.example.project.di

import org.example.project.data.remote.WeatherApi
import org.example.project.data.repository.WeatherRepository
import org.example.project.data.repository.WeatherRepositoryImpl
import org.example.project.network.createHttpClient

// Lightweight manual DI — no framework needed for this scope.
// Each property is lazy so it is only initialised on first access.
object AppDependencies {
    private val httpClient by lazy { createHttpClient() }
    private val weatherApi by lazy { WeatherApi(httpClient) }
    val weatherRepository: WeatherRepository by lazy { WeatherRepositoryImpl(weatherApi) }
}
