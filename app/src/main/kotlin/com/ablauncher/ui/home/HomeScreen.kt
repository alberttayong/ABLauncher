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
import com.ablauncher.ui.components.ContextMenuOverlay
import com.ablauncher.ui.taskbar.TaskbarSection

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val taskbarVisible by viewModel.taskbarVisible.collectAsStateWithLifecycle()
    val showContextMenu by viewModel.showContextMenu.collectAsStateWithLifecycle()
    val wallpaperUri by viewModel.wallpaperUri.collectAsStateWithLifecycle()

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
        // (When wallpaperUri is null the system wallpaper shows through the
        //  transparent window, thanks to android:windowShowWallpaper=true)

        // ── Widget area (future expansion) ───────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // leave space for taskbar
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
                    // Widgets screen — future expansion
                },
                onWallpapers = {
                    viewModel.dismissContextMenu()
                    navController.navigate("wallpaper")
                },
                onMoreWindows = {
                    viewModel.dismissContextMenu()
                    // More windows — future expansion
                },
                onSettings = {
                    viewModel.dismissContextMenu()
                    navController.navigate("settings")
                },
                onDismiss = { viewModel.dismissContextMenu() }
            )
        }
    }
}
