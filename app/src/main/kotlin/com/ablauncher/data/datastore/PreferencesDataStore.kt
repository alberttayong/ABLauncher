package com.ablauncher.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.ablauncher.data.model.AppTheme
import com.ablauncher.data.model.ThemeConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ablauncher_prefs")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Appearance
        val KEY_THEME = stringPreferencesKey("app_theme")
        val KEY_BLUR_RADIUS = floatPreferencesKey("blur_radius")
        val KEY_PANEL_ALPHA = floatPreferencesKey("panel_alpha")
        // Taskbar
        val KEY_TASKBAR_VISIBLE = booleanPreferencesKey("taskbar_visible")
        val KEY_TASKBAR_PINNED = stringSetPreferencesKey("taskbar_pinned")
        val KEY_WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
        val KEY_TASKBAR_PINNED_ORDER = stringPreferencesKey("taskbar_pinned_order")
        // Widget visibility toggles
        val KEY_WIDGET_CLOCK_ENABLED = booleanPreferencesKey("widget_clock_enabled")
        val KEY_WIDGET_WEATHER_ENABLED = booleanPreferencesKey("widget_weather_enabled")
        val KEY_WIDGET_CALENDAR_ENABLED = booleanPreferencesKey("widget_calendar_enabled")
        val KEY_WIDGET_NEWS_ENABLED = booleanPreferencesKey("widget_news_enabled")
        val KEY_WIDGET_SEARCH_ENABLED = booleanPreferencesKey("widget_search_enabled")
        // Clock settings
        val KEY_CLOCK_FACE = stringPreferencesKey("clock_face")
        val KEY_CLOCK_FORMAT = stringPreferencesKey("clock_format")
        // Weather settings
        val KEY_WEATHER_UNIT = stringPreferencesKey("weather_unit")
        val KEY_WEATHER_MANUAL_CITY = stringPreferencesKey("weather_manual_city")
        // Home canvas layout (legacy single-page)
        val KEY_HOME_ITEMS = stringPreferencesKey("home_items")
        // Home canvas multi-page layout
        val KEY_HOME_PAGES = stringPreferencesKey("home_pages")
        // Animation speed
        val KEY_ANIM_SPEED = floatPreferencesKey("anim_speed")
        // Wallpaper adjustments
        val KEY_WALLPAPER_DIM = floatPreferencesKey("wallpaper_dim")      // 0..0.8 darkness overlay
        val KEY_WALLPAPER_BLUR = floatPreferencesKey("wallpaper_blur")    // 0..1 blur intensity
        // App tray customization
        val KEY_APP_TRAY_COLUMNS = intPreferencesKey("app_tray_columns")  // 3..6
        val KEY_APP_TRAY_ICON_DP = intPreferencesKey("app_tray_icon_dp")  // 40/56/72
        val KEY_APP_TRAY_STYLE = stringPreferencesKey("app_tray_style")   // FROSTED/DARK/LIGHT/TRANSPARENT
        val KEY_APP_TRAY_ANIM = stringPreferencesKey("app_tray_anim")     // SLIDE_UP/FADE/SCALE
        // Screen saver
        val KEY_SCREENSAVER_STYLE = stringPreferencesKey("screensaver_style") // CLOCK/GRADIENT/COLORS
    }

    val themeConfig: Flow<ThemeConfig> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            ThemeConfig(
                appTheme = prefs[KEY_THEME]?.let {
                    runCatching { AppTheme.valueOf(it) }.getOrDefault(AppTheme.GLASS)
                } ?: AppTheme.GLASS,
                blurRadius = prefs[KEY_BLUR_RADIUS] ?: 20f,
                panelAlpha = prefs[KEY_PANEL_ALPHA] ?: 0.35f,
                animSpeed = prefs[KEY_ANIM_SPEED] ?: 1.0f
            )
        }

    val taskbarVisible: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_TASKBAR_VISIBLE] ?: true }

    /** Ordered comma-separated list of pinned package names */
    val pinnedTaskbarPackages: Flow<List<String>> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            prefs[KEY_TASKBAR_PINNED_ORDER]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        }

    val wallpaperUri: Flow<String?> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_WALLPAPER_URI] }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME] = theme.name }
    }

    suspend fun setBlurRadius(radius: Float) {
        context.dataStore.edit { prefs -> prefs[KEY_BLUR_RADIUS] = radius }
    }

    suspend fun setPanelAlpha(alpha: Float) {
        context.dataStore.edit { prefs -> prefs[KEY_PANEL_ALPHA] = alpha }
    }

    suspend fun setTaskbarVisible(visible: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_TASKBAR_VISIBLE] = visible }
    }

    suspend fun setPinnedApps(orderedPackages: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TASKBAR_PINNED_ORDER] = orderedPackages.joinToString(",")
        }
    }

    suspend fun setWallpaperUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri != null) prefs[KEY_WALLPAPER_URI] = uri
            else prefs.remove(KEY_WALLPAPER_URI)
        }
    }

    // ── Widget visibility ────────────────────────────────────────────────────
    val widgetClockEnabled: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_WIDGET_CLOCK_ENABLED] ?: true }

    val widgetWeatherEnabled: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_WIDGET_WEATHER_ENABLED] ?: true }

    val widgetCalendarEnabled: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_WIDGET_CALENDAR_ENABLED] ?: true }

    val widgetNewsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_WIDGET_NEWS_ENABLED] ?: false }

    val widgetSearchEnabled: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_WIDGET_SEARCH_ENABLED] ?: true }

    // ── Clock settings ───────────────────────────────────────────────────────
    val clockFace: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_CLOCK_FACE] ?: "DIGITAL_FULL" }

    val clockFormat: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_CLOCK_FORMAT] ?: "HOUR_12" }

    // ── Weather settings ─────────────────────────────────────────────────────
    val weatherUnit: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_WEATHER_UNIT] ?: "CELSIUS" }

    val weatherManualCity: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_WEATHER_MANUAL_CITY] ?: "" }

    suspend fun setWidgetEnabled(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[key] = enabled }
    }

    suspend fun setClockFace(face: String) {
        context.dataStore.edit { prefs -> prefs[KEY_CLOCK_FACE] = face }
    }

    suspend fun setClockFormat(format: String) {
        context.dataStore.edit { prefs -> prefs[KEY_CLOCK_FORMAT] = format }
    }

    suspend fun setWeatherUnit(unit: String) {
        context.dataStore.edit { prefs -> prefs[KEY_WEATHER_UNIT] = unit }
    }

    suspend fun setWeatherManualCity(city: String) {
        context.dataStore.edit { prefs -> prefs[KEY_WEATHER_MANUAL_CITY] = city }
    }

    suspend fun setAnimSpeed(speed: Float) {
        context.dataStore.edit { prefs -> prefs[KEY_ANIM_SPEED] = speed }
    }

    // ── Home canvas layout (legacy single page) ───────────────────────────────
    val homeItemsJson: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_HOME_ITEMS] ?: "" }

    suspend fun setHomeItemsJson(json: String) {
        context.dataStore.edit { prefs -> prefs[KEY_HOME_ITEMS] = json }
    }

    // ── Home canvas multi-page layout ─────────────────────────────────────────
    val homePagesJson: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_HOME_PAGES] ?: "" }

    suspend fun setHomePagesJson(json: String) {
        context.dataStore.edit { prefs -> prefs[KEY_HOME_PAGES] = json }
    }

    // ── Wallpaper adjustments ─────────────────────────────────────────────────
    val wallpaperDim: Flow<Float> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_WALLPAPER_DIM] ?: 0f }

    val wallpaperBlur: Flow<Float> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_WALLPAPER_BLUR] ?: 0f }

    suspend fun setWallpaperDim(dim: Float) {
        context.dataStore.edit { prefs -> prefs[KEY_WALLPAPER_DIM] = dim }
    }

    suspend fun setWallpaperBlur(blur: Float) {
        context.dataStore.edit { prefs -> prefs[KEY_WALLPAPER_BLUR] = blur }
    }

    // ── App tray customization ────────────────────────────────────────────────
    val appTrayColumns: Flow<Int> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_APP_TRAY_COLUMNS] ?: 4 }

    val appTrayIconDp: Flow<Int> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_APP_TRAY_ICON_DP] ?: 56 }

    val appTrayStyle: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_APP_TRAY_STYLE] ?: "FROSTED" }

    val appTrayAnim: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_APP_TRAY_ANIM] ?: "SLIDE_UP" }

    suspend fun setAppTrayColumns(columns: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_APP_TRAY_COLUMNS] = columns }
    }

    suspend fun setAppTrayIconDp(dp: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_APP_TRAY_ICON_DP] = dp }
    }

    suspend fun setAppTrayStyle(style: String) {
        context.dataStore.edit { prefs -> prefs[KEY_APP_TRAY_STYLE] = style }
    }

    suspend fun setAppTrayAnim(anim: String) {
        context.dataStore.edit { prefs -> prefs[KEY_APP_TRAY_ANIM] = anim }
    }

    // ── Screen saver ──────────────────────────────────────────────────────────
    val screensaverStyle: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_SCREENSAVER_STYLE] ?: "CLOCK" }

    suspend fun setScreensaverStyle(style: String) {
        context.dataStore.edit { prefs -> prefs[KEY_SCREENSAVER_STYLE] = style }
    }
}
