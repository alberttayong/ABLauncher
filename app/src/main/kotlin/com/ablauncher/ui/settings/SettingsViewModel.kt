package com.ablauncher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.model.AppTheme
import com.ablauncher.data.model.ThemeConfig
import com.ablauncher.data.repository.RecentAppsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsDataStore: PreferencesDataStore,
    private val recentAppsRepository: RecentAppsRepository
) : ViewModel() {

    val themeConfig: StateFlow<ThemeConfig> = prefsDataStore.themeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeConfig())

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    init {
        checkUsagePermission()
    }

    fun checkUsagePermission() {
        _hasUsagePermission.value = recentAppsRepository.hasUsagePermission()
    }

    fun requestUsagePermission() {
        recentAppsRepository.requestUsagePermission()
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { prefsDataStore.setTheme(theme) }
    }

    fun setBlurRadius(radius: Float) {
        viewModelScope.launch { prefsDataStore.setBlurRadius(radius) }
    }

    fun setPanelAlpha(alpha: Float) {
        viewModelScope.launch { prefsDataStore.setPanelAlpha(alpha) }
    }
}
