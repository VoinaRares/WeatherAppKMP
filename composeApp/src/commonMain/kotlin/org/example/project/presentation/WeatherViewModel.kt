package org.example.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.example.project.data.repository.WeatherRepository
import org.example.project.domain.model.City

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // Kept so we can cancel an in-flight request when the user picks a different city
    private var fetchJob: Job? = null

    fun fetchWeather(city: City) {
        // Cancel any previous in-flight request before starting a new one
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            repository
                .getWeather(city.latitude, city.longitude, city.name)
                .onStart { _uiState.value = WeatherUiState.Loading }
                .catch { e ->
                    _uiState.value = WeatherUiState.Error(e.message ?: "Unknown error")
                }
                .collect { weather ->
                    _uiState.value = WeatherUiState.Success(weather)
                }
        }
    }

    fun reset() {
        fetchJob?.cancel()
        _uiState.value = WeatherUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        fetchJob?.cancel()
    }
}
