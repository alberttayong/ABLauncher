package com.ablauncher.ui.home

import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.ui.components.ContextMenuOverlay
import com.ablauncher.ui.home.canvas.HomeCanvas
import com.ablauncher.ui.home.canvas.IconCustomizerSheet
import com.ablauncher.ui.taskbar.TaskbarSection
import com.ablauncher.ui.widgets.WidgetPanelSheet

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val taskbarVisible by viewModel.taskbarVisible.collectAsStateWithLifecycle()
    val showContextMenu by viewModel.showContextMenu.collectAsStateWithLifecycle()
    val showWidgetPanel by viewModel.showWidgetPanel.collectAsStateWithLifecycle()
    val wallpaperUri by viewModel.wallpaperUri.collectAsStateWithLifecycle()
    val homeItems by viewModel.homeItems.collectAsStateWithLifecycle()
    val selectedItemId by viewModel.selectedItemId.collectAsStateWithLifecycle()
    val customizerItem by viewModel.customizerItem.collectAsStateWithLifecycle()

    // Widget enabled states derived from canvas items
    val clockEnabled by viewModel.widgetClockEnabled.collectAsStateWithLifecycle()
    val weatherEnabled by viewModel.widgetWeatherEnabled.collectAsStateWithLifecycle()
    val calendarEnabled by viewModel.widgetCalendarEnabled.collectAsStateWithLifecycle()
    val newsEnabled by viewModel.widgetNewsEnabled.collectAsStateWithLifecycle()
    val searchEnabled by viewModel.widgetSearchEnabled.collectAsStateWithLifecycle()

    // Handle "add to home" passed back from AppTray via savedStateHandle
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val pendingPkg by (savedStateHandle?.getStateFlow<String?>("add_to_home_pkg", null)
        ?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) })
    LaunchedEffect(pendingPkg) {
        val pkg = pendingPkg ?: return@LaunchedEffect
        viewModel.addAppShortcut(pkg)
        savedStateHandle?.remove<String>("add_to_home_pkg")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        if (selectedItemId == null) viewModel.onHomeScreenLongPress()
                    }
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

        // ── Free-form home canvas ─────────────────────────────────────────────
        HomeCanvas(
            items = homeItems,
            selectedItemId = selectedItemId,
            onSelect = { viewModel.selectItem(it) },
            onMoved = { id, x, y -> viewModel.moveItem(id, x, y) },
            onResized = { id, w, h -> viewModel.resizeItem(id, w, h) },
            onRemove = { viewModel.removeItem(it) },
            onCustomize = { viewModel.openCustomizer(it) },
            onUninstall = { viewModel.uninstallApp(it) },
            modifier = Modifier.fillMaxSize()
        )

        // ── Taskbar at the bottom ─────────────────────────────────────────────
        TaskbarSection(
            isVisible = taskbarVisible,
            onToggleVisibility = { viewModel.toggleTaskbarVisibility() },
            onAppTrayOpen = { navController.navigate("apptray") },
            onSettingsOpen = { navController.navigate("settings") },
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
                onMoreWindows = { viewModel.dismissContextMenu() },
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

        // ── Icon customizer sheet ─────────────────────────────────────────────
        val ci = customizerItem
        if (ci != null) {
            IconCustomizerSheet(
                item = ci,
                onSave = { config -> viewModel.saveIconConfig(ci.id, config) },
                onDismiss = { viewModel.closeCustomizer() }
            )
        }
    }
}
