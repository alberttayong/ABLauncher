package com.ablauncher.ui.taskbar

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ablauncher.R
import com.ablauncher.ui.components.AppIcon
import com.ablauncher.ui.components.AppTrayButton
import com.ablauncher.ui.components.FrostedGlassPanel

/**
 * The collapsible taskbar at the bottom of the home screen.
 * Contains: [AppTrayButton] + recent/pinned app icons + collapse toggle.
 */
@Composable
fun TaskbarSection(
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onAppTrayOpen: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskbarViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val taskbarItems by viewModel.taskbarItems.collectAsStateWithLifecycle()
    val showEditSheet by viewModel.showEditSheet.collectAsStateWithLifecycle()
    val selectedApp by viewModel.selectedApp.collectAsStateWithLifecycle()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // ── Collapse/Expand pull tab ──────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(48.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .clickable(onClick = onToggleVisibility)
        ) {
            Icon(
                imageVector = if (isVisible) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                contentDescription = stringResource(
                    if (isVisible) R.string.collapse_taskbar else R.string.expand_taskbar
                ),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }

        // ── Collapsible taskbar body ──────────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
        ) {
            FrostedGlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                cornerRadius = 20.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Round app tray button (left side)
                    AppTrayButton(onClick = onAppTrayOpen)

                    Spacer(Modifier.width(12.dp))

                    // Recent/pinned app icons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        taskbarItems.forEach { item ->
                            val appInfo = viewModel.getAppInfo(item.packageName)
                            if (appInfo != null) {
                                AppIcon(
                                    packageName = appInfo.packageName,
                                    appName = appInfo.appName,
                                    icon = appInfo.icon,
                                    onClick = { viewModel.launchApp(item.packageName) },
                                    onLongClick = { viewModel.onAppLongPress(item.packageName) },
                                    iconSize = 44.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Taskbar app edit bottom sheet ────────────────────────────────────────
    if (showEditSheet && selectedApp != null) {
        val app = selectedApp!!
        val isPinned = taskbarItems.find { it.packageName == app.packageName }?.isPinned == true
        TaskbarEditSheet(
            app = app,
            isPinned = isPinned,
            onDismiss = { viewModel.dismissEditSheet() },
            onPin = { viewModel.pinApp(app.packageName) },
            onUnpin = { viewModel.unpinApp(app.packageName) },
            onOpenAppInfo = {
                viewModel.dismissEditSheet()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", app.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        )
    }
}
