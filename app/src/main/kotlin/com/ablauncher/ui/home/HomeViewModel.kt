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
import kotlinx.coroutines.flow.combine
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

    val wallpaperDim: StateFlow<Float> = prefsDataStore.wallpaperDim
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    val wallpaperBlur: StateFlow<Float> = prefsDataStore.wallpaperBlur
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    // ── Multi-page support ────────────────────────────────────────────────────
    val homePages: StateFlow<List<List<HomeItem>>> = homeItemRepository.homePages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentPageIndex = MutableStateFlow(0)

    // Current page items (for HomeCanvas display)
    val homeItems: StateFlow<List<HomeItem>> = combine(homePages, currentPageIndex) { pages, idx ->
        pages.getOrNull(idx) ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // All items across all pages (for widget-enabled state checks)
    private val allHomeItems: StateFlow<List<HomeItem>> = homeItemRepository.homeItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Widget enabled state (derived from ALL pages) ─────────────────────────
    val widgetClockEnabled: StateFlow<Boolean> = allHomeItems
        .map { it.any { item -> item is HomeItem.Widget && item.widgetType == WidgetType.CLOCK } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val widgetWeatherEnabled: StateFlow<Boolean> = allHomeItems
        .map { it.any { item -> item is HomeItem.Widget && item.widgetType == WidgetType.WEATHER } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val widgetCalendarEnabled: StateFlow<Boolean> = allHomeItems
        .map { it.any { item -> item is HomeItem.Widget && item.widgetType == WidgetType.CALENDAR } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val widgetNewsEnabled: StateFlow<Boolean> = allHomeItems
        .map { it.any { item -> item is HomeItem.Widget && item.widgetType == WidgetType.NEWS } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val widgetSearchEnabled: StateFlow<Boolean> = allHomeItems
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

    // ── Page management ───────────────────────────────────────────────────────
    fun setCurrentPage(idx: Int) {
        currentPageIndex.value = idx.coerceIn(0, (homePages.value.size - 1).coerceAtLeast(0))
    }

    fun addPage() {
        viewModelScope.launch {
            val pages = homePages.value.toMutableList()
            pages.add(emptyList())
            homeItemRepository.savePages(pages)
            currentPageIndex.value = pages.size - 1
        }
    }

    fun removePage(idx: Int) {
        viewModelScope.launch {
            val pages = homePages.value.toMutableList()
            if (pages.size <= 1) return@launch
            pages.removeAt(idx)
            homeItemRepository.savePages(pages)
            if (currentPageIndex.value >= pages.size) {
                currentPageIndex.value = pages.size - 1
            }
        }
    }

    // ── Canvas item mutations ─────────────────────────────────────────────────
    /** Atomically transform the current page and persist. */
    private suspend fun modifyCurrentPage(transform: (List<HomeItem>) -> List<HomeItem>) {
        val pages = homePages.value.toMutableList()
        if (pages.isEmpty()) return
        val idx = currentPageIndex.value.coerceIn(0, pages.size - 1)
        pages[idx] = transform(pages[idx])
        homeItemRepository.savePages(pages)
    }

    fun moveItem(id: String, xFrac: Float, yFrac: Float) {
        viewModelScope.launch {
            modifyCurrentPage { page ->
                page.toMutableList().also { list ->
                    val idx = list.indexOfFirst { it.id == id }
                    if (idx != -1) {
                        list[idx] = when (val item = list[idx]) {
                            is HomeItem.Widget -> item.copy(
                                xFrac = xFrac.coerceIn(0f, 0.98f),
                                yFrac = yFrac.coerceIn(0f, 0.98f)
                            )
                            is HomeItem.AppShortcut -> item.copy(
                                xFrac = xFrac.coerceIn(0f, 0.98f),
                                yFrac = yFrac.coerceIn(0f, 0.98f)
                            )
                        }
                    }
                }
            }
        }
    }

    fun resizeItem(id: String, widthFrac: Float, heightFrac: Float) {
        viewModelScope.launch {
            modifyCurrentPage { page ->
                page.toMutableList().also { list ->
                    val idx = list.indexOfFirst { it.id == id }
                    if (idx != -1) {
                        val item = list[idx]
                        if (item is HomeItem.Widget) {
                            list[idx] = item.copy(
                                widthFrac = widthFrac.coerceIn(0.10f, 1f),
                                heightFrac = heightFrac.coerceIn(0.08f, 1f)
                            )
                        }
                    }
                }
            }
        }
    }

    fun removeItem(id: String) {
        viewModelScope.launch {
            modifyCurrentPage { page -> page.filter { it.id != id } }
        }
    }

    // ── Widget toggle (add/remove from canvas) ────────────────────────────────
    fun toggleWidget(type: WidgetType, enabled: Boolean) {
        if (enabled) addWidget(type) else removeWidget(type)
    }

    private fun addWidget(type: WidgetType) {
        viewModelScope.launch {
            if (allHomeItems.value.any { it is HomeItem.Widget && it.widgetType == type }) return@launch
            val newItem = HomeItem.Widget(
                widgetType = type,
                xFrac = type.defaultXFrac,
                yFrac = type.defaultYFrac,
                widthFrac = type.defaultWidthFrac,
                heightFrac = type.defaultHeightFrac
            )
            modifyCurrentPage { it + newItem }
        }
    }

    private fun removeWidget(type: WidgetType) {
        viewModelScope.launch {
            // Remove from ALL pages
            val pages = homePages.value.map { page ->
                page.filter { !(it is HomeItem.Widget && it.widgetType == type) }
            }
            homeItemRepository.savePages(pages)
        }
    }

    // ── App shortcut ──────────────────────────────────────────────────────────
    fun addAppShortcut(packageName: String) {
        viewModelScope.launch {
            if (allHomeItems.value.any { it is HomeItem.AppShortcut && it.packageName == packageName }) return@launch
            val label = appRepository.apps.value
                .find { it.packageName == packageName }?.appName ?: packageName
            val newItem = HomeItem.AppShortcut(
                packageName = packageName,
                appLabel = label,
                xFrac = 0.40f + (Math.random() * 0.15f - 0.07f).toFloat(),
                yFrac = 0.35f + (Math.random() * 0.15f - 0.07f).toFloat()
            )
            modifyCurrentPage { it + newItem }
        }
    }

    // ── Icon customization ────────────────────────────────────────────────────
    fun openCustomizer(item: HomeItem.AppShortcut) { _customizerItem.value = item }
    fun closeCustomizer() { _customizerItem.value = null }

    fun saveIconConfig(id: String, config: IconConfig) {
        viewModelScope.launch {
            modifyCurrentPage { page ->
                page.toMutableList().also { list ->
                    val idx = list.indexOfFirst { it.id == id }
                    if (idx != -1) {
                        val item = list[idx]
                        if (item is HomeItem.AppShortcut) {
                            list[idx] = item.copy(iconConfig = config)
                        }
                    }
                }
            }
        }
    }

    // ── Uninstall ─────────────────────────────────────────────────────────────
    fun uninstallApp(packageName: String) {
        viewModelScope.launch {
            // Remove from ALL pages
            val pages = homePages.value.map { page ->
                page.filter { !(it is HomeItem.AppShortcut && it.packageName == packageName) }
            }
            homeItemRepository.savePages(pages)
        }
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // ── Legacy compat (used by WidgetPanelSheet) ──────────────────────────────
    fun setWidgetEnabled(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, enabled: Boolean) {
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
