package com.ablauncher.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.model.HomeItem
import com.ablauncher.data.model.IconConfig
import com.ablauncher.data.model.ThemeConfig
import com.ablauncher.data.model.WidgetType
import com.ablauncher.data.repository.AppRepository
import com.ablauncher.data.repository.HomeItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefsDataStore: PreferencesDataStore,
    private val homeItemRepository: HomeItemRepository,
    private val appRepository: AppRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val themeConfig: StateFlow<ThemeConfig> = prefsDataStore.themeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeConfig())

    val taskbarVisible: StateFlow<Boolean> = prefsDataStore.taskbarVisible
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val wallpaperUri: StateFlow<String?> = prefsDataStore.wallpaperUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Home canvas items ─────────────────────────────────────────────────────
    val homeItems: StateFlow<List<HomeItem>> = homeItemRepository.homeItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Widget enabled state (derived from canvas items) ──────────────────────
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

    // ── UI state ──────────────────────────────────────────────────────────────
    private val _showContextMenu = MutableStateFlow(false)
    val showContextMenu: StateFlow<Boolean> = _showContextMenu.asStateFlow()

    private val _showWidgetPanel = MutableStateFlow(false)
    val showWidgetPanel: StateFlow<Boolean> = _showWidgetPanel.asStateFlow()

    private val _selectedItemId = MutableStateFlow<String?>(null)
    val selectedItemId: StateFlow<String?> = _selectedItemId.asStateFlow()

    private val _customizerItem = MutableStateFlow<HomeItem.AppShortcut?>(null)
    val customizerItem: StateFlow<HomeItem.AppShortcut?> = _customizerItem.asStateFlow()

    init {
        viewModelScope.launch { homeItemRepository.initIfEmpty() }
    }

    // ── Context menu ──────────────────────────────────────────────────────────
    fun onHomeScreenLongPress() { _showContextMenu.value = true }
    fun dismissContextMenu() { _showContextMenu.value = false }
    fun openWidgetPanel() { _showWidgetPanel.value = true }
    fun closeWidgetPanel() { _showWidgetPanel.value = false }

    // ── Item selection ────────────────────────────────────────────────────────
    fun selectItem(id: String?) { _selectedItemId.value = id }

    // ── Taskbar ───────────────────────────────────────────────────────────────
    fun toggleTaskbarVisibility() {
        viewModelScope.launch {
            prefsDataStore.setTaskbarVisible(!taskbarVisible.value)
        }
    }

    // ── Canvas item mutations ─────────────────────────────────────────────────
    fun moveItem(id: String, xFrac: Float, yFrac: Float) {
        viewModelScope.launch {
            val current = homeItems.value.toMutableList()
            val idx = current.indexOfFirst { it.id == id }
            if (idx == -1) return@launch
            current[idx] = when (val item = current[idx]) {
                is HomeItem.Widget -> item.copy(
                    xFrac = xFrac.coerceIn(0f, 0.98f),
                    yFrac = yFrac.coerceIn(0f, 0.98f)
                )
                is HomeItem.AppShortcut -> item.copy(
                    xFrac = xFrac.coerceIn(0f, 0.98f),
                    yFrac = yFrac.coerceIn(0f, 0.98f)
                )
            }
            homeItemRepository.save(current)
        }
    }

    fun resizeItem(id: String, widthFrac: Float, heightFrac: Float) {
        viewModelScope.launch {
            val current = homeItems.value.toMutableList()
            val idx = current.indexOfFirst { it.id == id }
            if (idx == -1) return@launch
            val item = current[idx]
            if (item is HomeItem.Widget) {
                current[idx] = item.copy(
                    widthFrac = widthFrac.coerceIn(0.10f, 1f),
                    heightFrac = heightFrac.coerceIn(0.08f, 1f)
                )
                homeItemRepository.save(current)
            }
        }
    }

    fun removeItem(id: String) {
        viewModelScope.launch {
            homeItemRepository.save(homeItems.value.filter { it.id != id })
        }
    }

    // ── Widget toggle (add/remove from canvas) ────────────────────────────────
    fun toggleWidget(type: WidgetType, enabled: Boolean) {
        if (enabled) addWidget(type) else removeWidget(type)
    }

    private fun addWidget(type: WidgetType) {
        viewModelScope.launch {
            val existing = homeItems.value.any {
                it is HomeItem.Widget && it.widgetType == type
            }
            if (existing) return@launch
            val newItem = HomeItem.Widget(
                widgetType = type,
                xFrac = type.defaultXFrac,
                yFrac = type.defaultYFrac,
                widthFrac = type.defaultWidthFrac,
                heightFrac = type.defaultHeightFrac
            )
            homeItemRepository.save(homeItems.value + newItem)
        }
    }

    private fun removeWidget(type: WidgetType) {
        viewModelScope.launch {
            homeItemRepository.save(
                homeItems.value.filter { !(it is HomeItem.Widget && it.widgetType == type) }
            )
        }
    }

    // ── App shortcut ──────────────────────────────────────────────────────────
    fun addAppShortcut(packageName: String) {
        viewModelScope.launch {
            val alreadyAdded = homeItems.value.any {
                it is HomeItem.AppShortcut && it.packageName == packageName
            }
            if (alreadyAdded) return@launch
            val label = appRepository.apps.value
                .find { it.packageName == packageName }?.appName ?: packageName
            val newItem = HomeItem.AppShortcut(
                packageName = packageName,
                appLabel = label,
                xFrac = 0.40f + (Math.random() * 0.15f - 0.07f).toFloat(),
                yFrac = 0.35f + (Math.random() * 0.15f - 0.07f).toFloat()
            )
            homeItemRepository.save(homeItems.value + newItem)
        }
    }

    // ── Icon customization ────────────────────────────────────────────────────
    fun openCustomizer(item: HomeItem.AppShortcut) { _customizerItem.value = item }
    fun closeCustomizer() { _customizerItem.value = null }

    fun saveIconConfig(id: String, config: IconConfig) {
        viewModelScope.launch {
            val current = homeItems.value.toMutableList()
            val idx = current.indexOfFirst { it.id == id }
            if (idx == -1) return@launch
            val item = current[idx]
            if (item is HomeItem.AppShortcut) {
                current[idx] = item.copy(iconConfig = config)
                homeItemRepository.save(current)
            }
        }
    }

    // ── Uninstall ─────────────────────────────────────────────────────────────
    fun uninstallApp(packageName: String) {
        viewModelScope.launch {
            // Remove from canvas immediately
            homeItemRepository.save(
                homeItems.value.filter { !(it is HomeItem.AppShortcut && it.packageName == packageName) }
            )
        }
        // Launch system uninstall dialog
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // ── Legacy compat (used by WidgetPanelSheet) ──────────────────────────────
    fun setWidgetEnabled(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, enabled: Boolean) {
        // Map old DataStore keys to new WidgetType toggles
        val type = when (key) {
            PreferencesDataStore.KEY_WIDGET_CLOCK_ENABLED -> WidgetType.CLOCK
            PreferencesDataStore.KEY_WIDGET_WEATHER_ENABLED -> WidgetType.WEATHER
            PreferencesDataStore.KEY_WIDGET_CALENDAR_ENABLED -> WidgetType.CALENDAR
            PreferencesDataStore.KEY_WIDGET_NEWS_ENABLED -> WidgetType.NEWS
            PreferencesDataStore.KEY_WIDGET_SEARCH_ENABLED -> WidgetType.SEARCH
            else -> return
        }
        toggleWidget(type, enabled)
    }
}
