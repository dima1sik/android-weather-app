package com.example.weatherapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.api.NetworkResponse
import com.example.weatherapp.api.WeatherModel
import com.example.weatherapp.api.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _weatherResult = MutableStateFlow<NetworkResponse<WeatherModel>>(NetworkResponse.Loading)
    val weatherResult: StateFlow<NetworkResponse<WeatherModel>> = _weatherResult.asStateFlow()
    private var searchJob: Job? = null
    private var requestId = 0
    private var lastLocation = "auto:ip"

    init {
        getData(lastLocation)
    }

    fun getData(city: String) {
        searchJob?.cancel()
        val currentRequest = ++requestId
        val location = city.trim()
        if (location.isEmpty()) {
            _weatherResult.value = NetworkResponse.Error("Enter a location to search.")
            return
        }

        lastLocation = location
        _weatherResult.value = NetworkResponse.Loading
        searchJob = viewModelScope.launch {
            val result = repository.getWeather(location)
            if (currentRequest == requestId) {
                _weatherResult.value = result
            }
        }
    }

    fun retry() = getData(lastLocation)
}

