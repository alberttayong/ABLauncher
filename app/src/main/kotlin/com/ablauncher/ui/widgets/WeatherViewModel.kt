package com.ablauncher.ui.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.model.WeatherData
import com.ablauncher.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val prefs: PreferencesDataStore
) : ViewModel() {

    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData = _weatherData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val weatherUnit = prefs.weatherUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "CELSIUS")

    val manualCity = prefs.weatherManualCity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        startRefreshLoop()
    }

    private fun startRefreshLoop() {
        viewModelScope.launch {
            while (true) {
                refresh()
                delay(30 * 60 * 1000L) // refresh every 30 min
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val unit = prefs.weatherUnit.first()
            val city = prefs.weatherManualCity.first()
            _weatherData.value = weatherRepository.fetchWeather(unit, city)
            _isLoading.value = false
        }
    }

    fun setWeatherUnit(unit: String) {
        viewModelScope.launch {
            prefs.setWeatherUnit(unit)
            refresh()
        }
    }

    fun setManualCity(city: String) {
        viewModelScope.launch {
            prefs.setWeatherManualCity(city)
            refresh()
        }
    }
}
