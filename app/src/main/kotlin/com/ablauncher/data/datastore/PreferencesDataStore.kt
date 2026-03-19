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
        val KEY_THEME = stringPreferencesKey("app_theme")
        val KEY_BLUR_RADIUS = floatPreferencesKey("blur_radius")
        val KEY_PANEL_ALPHA = floatPreferencesKey("panel_alpha")
        val KEY_TASKBAR_VISIBLE = booleanPreferencesKey("taskbar_visible")
        val KEY_TASKBAR_PINNED = stringSetPreferencesKey("taskbar_pinned")
        val KEY_WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
        val KEY_TASKBAR_PINNED_ORDER = stringPreferencesKey("taskbar_pinned_order")
    }

    val themeConfig: Flow<ThemeConfig> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            ThemeConfig(
                appTheme = prefs[KEY_THEME]?.let {
                    runCatching { AppTheme.valueOf(it) }.getOrDefault(AppTheme.GLASS)
                } ?: AppTheme.GLASS,
                blurRadius = prefs[KEY_BLUR_RADIUS] ?: 20f,
                panelAlpha = prefs[KEY_PANEL_ALPHA] ?: 0.35f
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
}
