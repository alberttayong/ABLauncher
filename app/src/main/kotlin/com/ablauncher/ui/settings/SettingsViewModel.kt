package com.ablauncher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.model.AppTheme
import com.ablauncher.data.model.ClockFace
import com.ablauncher.data.model.ClockFormat
import com.ablauncher.data.model.HomeItem
import com.ablauncher.data.model.ThemeConfig
import com.ablauncher.data.model.WidgetType
import com.ablauncher.data.repository.HomeItemRepository
import com.ablauncher.data.repository.RecentAppsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsDataStore: PreferencesDataStore,
    private val recentAppsRepository: RecentAppsRepository,
    private val homeItemRepository: HomeItemRepository
) : ViewModel() {

    val themeConfig: StateFlow<ThemeConfig> = prefsDataStore.themeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeConfig())

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    // ── Widget enabled states (derived from canvas items) ─────────────────────
    private val homeItems: StateFlow<List<HomeItem>> = homeItemRepository.homeItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val widgetClockEnabled: StateFlow<Boolean> = homeItems
        .map { it.any { item -> item is HomeItem.Widget && item.widgetType == WidgetType.CLOCK } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val widgetWeatherEnabled: StateFlow<Boolean> = homeItems
        .map { it.any { item -> item is HomeItem.Widget && item.widgetType == WidgetType.WEATHER } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val widgetCalendarEnabled: StateFlow<Boolean> = homeItems
        .map { it.any { item -> item is HomeItem.Widget && item.widgetType == WidgetType.CALENDAR } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val widgetNewsEnabled: StateFlow<Boolean> = homeItems
        .map { it.any { item -> item is HomeItem.Widget && item.widgetType == WidgetType.NEWS } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val widgetSearchEnabled: StateFlow<Boolean> = homeItems
        .map { it.any { item -> item is HomeItem.Widget && item.widgetType == WidgetType.SEARCH } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ── Clock settings ────────────────────────────────────────────────────────
    val clockFace: StateFlow<ClockFace> = prefsDataStore.clockFace
        .map { runCatching { ClockFace.valueOf(it) }.getOrDefault(ClockFace.DIGITAL_FULL) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClockFace.DIGITAL_FULL)

    val clockFormat: StateFlow<ClockFormat> = prefsDataStore.clockFormat
        .map { runCatching { ClockFormat.valueOf(it) }.getOrDefault(ClockFormat.HOUR_12) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClockFormat.HOUR_12)

    // ── Weather settings ──────────────────────────────────────────────────────
    val weatherUnit: StateFlow<String> = prefsDataStore.weatherUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "CELSIUS")

    val manualCity: StateFlow<String> = prefsDataStore.weatherManualCity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    init {
        checkUsagePermission()
    }

    fun checkUsagePermission() {
        _hasUsagePermission.value = recentAppsRepository.hasUsagePermission()
    }

    fun requestUsagePermission() {
        recentAppsRepository.requestUsagePermission()
    }

    // ── Appearance ────────────────────────────────────────────────────────────
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { prefsDataStore.setTheme(theme) }
    }

    fun setBlurRadius(radius: Float) {
        viewModelScope.launch { prefsDataStore.setBlurRadius(radius) }
    }

    fun setPanelAlpha(alpha: Float) {
        viewModelScope.launch { prefsDataStore.setPanelAlpha(alpha) }
    }

    fun setAnimSpeed(speed: Float) {
        viewModelScope.launch { prefsDataStore.setAnimSpeed(speed) }
    }

    // ── Widget toggles ────────────────────────────────────────────────────────
    fun toggleWidget(type: WidgetType, enabled: Boolean) {
        if (enabled) addWidget(type) else removeWidget(type)
    }

    private fun addWidget(type: WidgetType) {
        viewModelScope.launch {
            val current = homeItems.value
            if (current.any { it is HomeItem.Widget && it.widgetType == type }) return@launch
            val newItem = HomeItem.Widget(
                widgetType = type,
                xFrac = type.defaultXFrac,
                yFrac = type.defaultYFrac,
                widthFrac = type.defaultWidthFrac,
                heightFrac = type.defaultHeightFrac
            )
            homeItemRepository.save(current + newItem)
        }
    }

    private fun removeWidget(type: WidgetType) {
        viewModelScope.launch {
            homeItemRepository.save(
                homeItems.value.filter { !(it is HomeItem.Widget && it.widgetType == type) }
            )
        }
    }

    // ── Clock / Weather settings ──────────────────────────────────────────────
    fun setClockFace(face: ClockFace) {
        viewModelScope.launch { prefsDataStore.setClockFace(face.name) }
    }

    fun setClockFormat(fmt: ClockFormat) {
        viewModelScope.launch { prefsDataStore.setClockFormat(fmt.name) }
    }

    fun setWeatherUnit(unit: String) {
        viewModelScope.launch { prefsDataStore.setWeatherUnit(unit) }
    }

    fun setManualCity(city: String) {
        viewModelScope.launch { prefsDataStore.setWeatherManualCity(city) }
    }
}
