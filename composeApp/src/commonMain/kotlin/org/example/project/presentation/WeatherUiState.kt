package org.example.project.presentation

import org.example.project.domain.model.WeatherInfo

sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object Loading : WeatherUiState
    data class Success(val weather: WeatherInfo) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
