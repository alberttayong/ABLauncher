package com.ablauncher.ui.home

import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.ui.components.ContextMenuOverlay
import com.ablauncher.ui.taskbar.TaskbarSection
import com.ablauncher.ui.widgets.WidgetBoard
import com.ablauncher.ui.widgets.WidgetPanelSheet

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val taskbarVisible by viewModel.taskbarVisible.collectAsStateWithLifecycle()
    val showContextMenu by viewModel.showContextMenu.collectAsStateWithLifecycle()
    val showWidgetPanel by viewModel.showWidgetPanel.collectAsStateWithLifecycle()
    val wallpaperUri by viewModel.wallpaperUri.collectAsStateWithLifecycle()

    val clockEnabled by viewModel.widgetClockEnabled.collectAsStateWithLifecycle()
    val weatherEnabled by viewModel.widgetWeatherEnabled.collectAsStateWithLifecycle()
    val calendarEnabled by viewModel.widgetCalendarEnabled.collectAsStateWithLifecycle()
    val newsEnabled by viewModel.widgetNewsEnabled.collectAsStateWithLifecycle()
    val searchEnabled by viewModel.widgetSearchEnabled.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { viewModel.onHomeScreenLongPress() }
                )
            }
    ) {
        // ── Wallpaper layer ──────────────────────────────────────────────────
        if (wallpaperUri != null) {
            AsyncImage(
                model = Uri.parse(wallpaperUri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Widget board ─────────────────────────────────────────────────────
        WidgetBoard(
            clockEnabled = clockEnabled,
            weatherEnabled = weatherEnabled,
            calendarEnabled = calendarEnabled,
            newsEnabled = newsEnabled,
            searchEnabled = searchEnabled,
            modifier = Modifier.fillMaxSize()
        )

        // ── Taskbar at the bottom ─────────────────────────────────────────────
        TaskbarSection(
            isVisible = taskbarVisible,
            onToggleVisibility = { viewModel.toggleTaskbarVisibility() },
            onAppTrayOpen = { navController.navigate("apptray") },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // ── Long-press context menu overlay ──────────────────────────────────
        if (showContextMenu) {
            ContextMenuOverlay(
                onWidgets = {
                    viewModel.dismissContextMenu()
                    viewModel.openWidgetPanel()
                },
                onWallpapers = {
                    viewModel.dismissContextMenu()
                    navController.navigate("wallpaper")
                },
                onMoreWindows = {
                    viewModel.dismissContextMenu()
                },
                onSettings = {
                    viewModel.dismissContextMenu()
                    navController.navigate("settings")
                },
                onDismiss = { viewModel.dismissContextMenu() }
            )
        }

        // ── Widget panel sheet ────────────────────────────────────────────────
        if (showWidgetPanel) {
            WidgetPanelSheet(
                clockEnabled = clockEnabled,
                weatherEnabled = weatherEnabled,
                calendarEnabled = calendarEnabled,
                newsEnabled = newsEnabled,
                searchEnabled = searchEnabled,
                onClockToggle = { viewModel.setWidgetEnabled(PreferencesDataStore.KEY_WIDGET_CLOCK_ENABLED, it) },
                onWeatherToggle = { viewModel.setWidgetEnabled(PreferencesDataStore.KEY_WIDGET_WEATHER_ENABLED, it) },
                onCalendarToggle = { viewModel.setWidgetEnabled(PreferencesDataStore.KEY_WIDGET_CALENDAR_ENABLED, it) },
                onNewsToggle = { viewModel.setWidgetEnabled(PreferencesDataStore.KEY_WIDGET_NEWS_ENABLED, it) },
                onSearchToggle = { viewModel.setWidgetEnabled(PreferencesDataStore.KEY_WIDGET_SEARCH_ENABLED, it) },
                onDismiss = { viewModel.closeWidgetPanel() }
            )
        }
    }
}
