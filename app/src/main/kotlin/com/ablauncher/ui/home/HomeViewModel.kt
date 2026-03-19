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

    fun onHomeScreenLongPress() {
        _showContextMenu.value = true
    }

    fun dismissContextMenu() {
        _showContextMenu.value = false
    }

    fun toggleTaskbarVisibility() {
        viewModelScope.launch {
            prefsDataStore.setTaskbarVisible(!taskbarVisible.value)
        }
    }
}
