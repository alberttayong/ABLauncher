package com.ablauncher.ui.settings

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
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
import com.ablauncher.service.ABNotificationListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsDataStore: PreferencesDataStore,
    private val recentAppsRepository: RecentAppsRepository,
    private val homeItemRepository: HomeItemRepository
) : ViewModel() {

    val themeConfig: StateFlow<ThemeConfig> = prefsDataStore.themeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeConfig())

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    private val _hasNotificationListenerPermission = MutableStateFlow(false)
    val hasNotificationListenerPermission: StateFlow<Boolean> = _hasNotificationListenerPermission.asStateFlow()

    // ── Widget enabled states (derived from ALL canvas items) ─────────────────
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

    val widgetMediaPlayerEnabled: StateFlow<Boolean> = homeItems
        .map { it.any { item -> item is HomeItem.Widget && item.widgetType == WidgetType.MEDIA_PLAYER } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Maps each active WidgetType to the page index it lives on. */
    val widgetPageIndices: StateFlow<Map<WidgetType, Int>> = homeItemRepository.homePages
        .map { pages ->
            buildMap {
                pages.forEachIndexed { idx, page ->
                    page.filterIsInstance<HomeItem.Widget>().forEach { put(it.widgetType, idx) }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

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

    // ── App tray settings ─────────────────────────────────────────────────────
    val appTrayColumns: StateFlow<Int> = prefsDataStore.appTrayColumns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 4)

    val appTrayIconDp: StateFlow<Int> = prefsDataStore.appTrayIconDp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 56)

    val appTrayStyle: StateFlow<String> = prefsDataStore.appTrayStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "FROSTED")

    val appTrayAnim: StateFlow<String> = prefsDataStore.appTrayAnim
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "SLIDE_UP")

    // ── Screen saver ─────────────────────────────────────────────────────────
    val screensaverStyle: StateFlow<String> = prefsDataStore.screensaverStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "CLOCK")

    fun setScreensaverStyle(style: String) {
        viewModelScope.launch { prefsDataStore.setScreensaverStyle(style) }
    }

    // ── Home pages ────────────────────────────────────────────────────────────
    val homePageCount: StateFlow<Int> = homeItemRepository.homePages
        .map { it.size.coerceAtLeast(1) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)

    init {
        checkUsagePermission()
        checkNotificationListenerPermission()
    }

    fun checkUsagePermission() {
        _hasUsagePermission.value = recentAppsRepository.hasUsagePermission()
    }

    fun requestUsagePermission() {
        recentAppsRepository.requestUsagePermission()
    }

    fun checkNotificationListenerPermission() {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: ""
        val cn = ComponentName(context, ABNotificationListener::class.java).flattenToShortString()
        _hasNotificationListenerPermission.value = flat.contains(cn)
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
            val pages = homeItemRepository.homePages.first().toMutableList()
            if (pages.isEmpty()) return@launch
            // Check all pages for existing widget
            if (pages.flatten().any { it is HomeItem.Widget && it.widgetType == type }) return@launch
            val newItem = HomeItem.Widget(
                widgetType = type,
                xFrac = type.defaultXFrac,
                yFrac = type.defaultYFrac,
                widthFrac = type.defaultWidthFrac,
                heightFrac = type.defaultHeightFrac
            )
            // Add to page 0
            pages[0] = pages[0] + newItem
            homeItemRepository.savePages(pages)
        }
    }

    private fun removeWidget(type: WidgetType) {
        viewModelScope.launch {
            val pages = homeItemRepository.homePages.first().map { page ->
                page.filter { !(it is HomeItem.Widget && it.widgetType == type) }
            }
            homeItemRepository.savePages(pages)
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

    // ── App tray settings ─────────────────────────────────────────────────────
    fun setAppTrayColumns(columns: Int) {
        viewModelScope.launch { prefsDataStore.setAppTrayColumns(columns) }
    }

    fun setAppTrayIconDp(dp: Int) {
        viewModelScope.launch { prefsDataStore.setAppTrayIconDp(dp) }
    }

    fun setAppTrayStyle(style: String) {
        viewModelScope.launch { prefsDataStore.setAppTrayStyle(style) }
    }

    fun setAppTrayAnim(anim: String) {
        viewModelScope.launch { prefsDataStore.setAppTrayAnim(anim) }
    }

    fun moveWidgetToPage(type: WidgetType, targetPageIdx: Int) {
        viewModelScope.launch {
            val pages = homeItemRepository.homePages.first().toMutableList()
            var widget: HomeItem.Widget? = null
            val stripped = pages.map { page ->
                val found = page.filterIsInstance<HomeItem.Widget>().firstOrNull { it.widgetType == type }
                if (found != null) widget = found
                page.filter { !(it is HomeItem.Widget && it.widgetType == type) }
            }.toMutableList()
            val w = widget ?: return@launch
            if (targetPageIdx in stripped.indices) {
                stripped[targetPageIdx] = stripped[targetPageIdx] + w
                homeItemRepository.savePages(stripped)
            }
        }
    }

    // ── Home pages ────────────────────────────────────────────────────────────
    fun addPage() {
        viewModelScope.launch {
            val pages = homeItemRepository.homePages.first().toMutableList()
            pages.add(emptyList())
            homeItemRepository.savePages(pages)
        }
    }

    fun removePage(idx: Int) {
        viewModelScope.launch {
            val pages = homeItemRepository.homePages.first().toMutableList()
            if (pages.size <= 1) return@launch
            pages.removeAt(idx)
            homeItemRepository.savePages(pages)
        }
    }
}
