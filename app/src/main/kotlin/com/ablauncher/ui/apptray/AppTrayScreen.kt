package com.ablauncher.ui.apptray

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ablauncher.R
import com.ablauncher.ui.components.AppIcon
import com.ablauncher.ui.components.FrostedGlassPanel
import com.ablauncher.ui.components.LauncherSearchBar

@Composable
fun AppTrayScreen(
    navController: NavController,
    viewModel: AppTrayViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val apps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showAppOptions by viewModel.showAppOptions.collectAsStateWithLifecycle()
    val selectedApp by viewModel.selectedApp.collectAsStateWithLifecycle()

    FrostedGlassPanel(
        modifier = Modifier.fillMaxSize(),
        cornerRadius = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(R.string.app_tray_label),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }

            // ── Search bar ────────────────────────────────────────────────────
            LauncherSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // ── App grid ──────────────────────────────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 80.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppIcon(
                        packageName = app.packageName,
                        appName = app.appName,
                        icon = app.icon,
                        onClick = {
                            viewModel.launchApp(app.packageName)
                            navController.popBackStack()
                        },
                        onLongClick = { viewModel.onAppLongPress(app.packageName) },
                        showLabel = true,
                        iconSize = 56.dp
                    )
                }
            }
        }
    }

    // ── App options bottom sheet ──────────────────────────────────────────────
    if (showAppOptions && selectedApp != null) {
        val app = selectedApp!!
        AppOptionsSheet(
            appName = app.appName,
            icon = app.icon,
            onDismiss = { viewModel.dismissAppOptions() },
            onOpen = {
                viewModel.launchApp(app.packageName)
                viewModel.dismissAppOptions()
                navController.popBackStack()
            },
            onPinToTaskbar = { viewModel.pinToTaskbar(app.packageName) },
            onUnpinFromTaskbar = { viewModel.unpinFromTaskbar(app.packageName) },
            onAddToHome = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("add_to_home_pkg", app.packageName)
                viewModel.dismissAppOptions()
                navController.popBackStack()
            },
            onAppInfo = {
                viewModel.dismissAppOptions()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", app.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppOptionsSheet(
    appName: String,
    icon: android.graphics.drawable.Drawable,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onPinToTaskbar: () -> Unit,
    onUnpinFromTaskbar: () -> Unit,
    onAddToHome: () -> Unit,
    onAppInfo: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                AsyncImage(
                    model = icon,
                    contentDescription = appName,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))
            OptionsAction(Icons.Default.OpenInNew, stringResource(R.string.open_app), onOpen)
            OptionsAction(Icons.Default.AddHome, "Add to Home Screen", onAddToHome)
            OptionsAction(Icons.Default.PushPin, stringResource(R.string.pin_to_taskbar), onPinToTaskbar)
            OptionsAction(Icons.Default.Info, stringResource(R.string.app_info), onAppInfo)
        }
    }
}

@Composable
private fun OptionsAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
    }
}
