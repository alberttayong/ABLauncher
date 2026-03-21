package com.ablauncher.ui.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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
    val wallpaperDim by viewModel.wallpaperDim.collectAsStateWithLifecycle()
    val wallpaperBlur by viewModel.wallpaperBlur.collectAsStateWithLifecycle()
    val homePagesList by viewModel.homePages.collectAsStateWithLifecycle()
    val selectedItemId by viewModel.selectedItemId.collectAsStateWithLifecycle()
    val customizerItem by viewModel.customizerItem.collectAsStateWithLifecycle()

    // Widget enabled states
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

    val pageCount = homePagesList.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // Sync pager → viewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { viewModel.setCurrentPage(it) }
    }

    // Sync viewModel → pager (e.g. when a page is added)
    val currentPageIdx by viewModel.currentPageIndex.collectAsStateWithLifecycle()
    LaunchedEffect(currentPageIdx) {
        if (pagerState.currentPage != currentPageIdx && currentPageIdx < pageCount) {
            pagerState.animateScrollToPage(currentPageIdx)
        }
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
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (wallpaperBlur > 0f) Modifier.blur(wallpaperBlur * 25.dp)
                        else Modifier
                    )
            )
        }

        // ── Darkness dim overlay ─────────────────────────────────────────────
        if (wallpaperDim > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = wallpaperDim))
            )
        }

        // ── Multi-page canvas (HorizontalPager) ───────────────────────────────
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = selectedItemId == null,
            modifier = Modifier.fillMaxSize()
        ) { pageIdx ->
            HomeCanvas(
                items = homePagesList.getOrElse(pageIdx) { emptyList() },
                selectedItemId = selectedItemId,
                onSelect = { viewModel.selectItem(it) },
                onMoved = { id, x, y -> viewModel.moveItem(id, x, y) },
                onResized = { id, w, h -> viewModel.resizeItem(id, w, h) },
                onRemove = { viewModel.removeItem(it) },
                onCustomize = { viewModel.openCustomizer(it) },
                onUninstall = { viewModel.uninstallApp(it) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Page indicator dots ───────────────────────────────────────────────
        if (pageCount > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
            ) {
                repeat(pageCount) { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (i == pagerState.currentPage) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == pagerState.currentPage) Color.White
                                else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }

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
