package com.ablauncher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.model.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefsDataStore: PreferencesDataStore
) : ViewModel() {

    val themeConfig: StateFlow<ThemeConfig> = prefsDataStore.themeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeConfig())

    val taskbarVisible: StateFlow<Boolean> = prefsDataStore.taskbarVisible
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val wallpaperUri: StateFlow<String?> = prefsDataStore.wallpaperUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _showContextMenu = MutableStateFlow(false)
    val showContextMenu: StateFlow<Boolean> = _showContextMenu.asStateFlow()

    private val _showWidgetPanel = MutableStateFlow(false)
    val showWidgetPanel: StateFlow<Boolean> = _showWidgetPanel.asStateFlow()

    // ── Widget visibility ────────────────────────────────────────────────────
    val widgetClockEnabled: StateFlow<Boolean> = prefsDataStore.widgetClockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val widgetWeatherEnabled: StateFlow<Boolean> = prefsDataStore.widgetWeatherEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val widgetCalendarEnabled: StateFlow<Boolean> = prefsDataStore.widgetCalendarEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val widgetNewsEnabled: StateFlow<Boolean> = prefsDataStore.widgetNewsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val widgetSearchEnabled: StateFlow<Boolean> = prefsDataStore.widgetSearchEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun onHomeScreenLongPress() {
        _showContextMenu.value = true
    }

    fun dismissContextMenu() {
        _showContextMenu.value = false
    }

    fun openWidgetPanel() {
        _showWidgetPanel.value = true
    }

    fun closeWidgetPanel() {
        _showWidgetPanel.value = false
    }

    fun toggleTaskbarVisibility() {
        viewModelScope.launch {
            prefsDataStore.setTaskbarVisible(!taskbarVisible.value)
        }
    }

    fun setWidgetEnabled(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, enabled: Boolean) {
        viewModelScope.launch { prefsDataStore.setWidgetEnabled(key, enabled) }
    }
}
