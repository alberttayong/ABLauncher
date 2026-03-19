package com.ablauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ablauncher.R

data class ContextMenuAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

/**
 * Full-screen overlay shown on long press of the home screen.
 * Contains a 2×2 grid: Widgets | Wallpapers & Styles | More Windows | Settings
 */
@Composable
fun ContextMenuOverlay(
    onWidgets: () -> Unit,
    onWallpapers: () -> Unit,
    onMoreWindows: () -> Unit,
    onSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        ContextMenuAction(Icons.Default.Widgets, stringResource(R.string.widgets_label), onWidgets),
        ContextMenuAction(Icons.Default.Palette, stringResource(R.string.wallpapers_styles_label), onWallpapers),
        ContextMenuAction(Icons.Default.GridView, stringResource(R.string.more_windows_label), onMoreWindows),
        ContextMenuAction(Icons.Default.Settings, stringResource(R.string.settings_label), onSettings)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        FrostedGlassPanel(
            modifier = Modifier
                .clickable { /* consume click to prevent dismiss */ }
                .padding(8.dp),
            cornerRadius = 24.dp
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(8.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(actions) { action ->
                    ContextMenuItem(
                        icon = action.icon,
                        label = action.label,
                        onClick = action.onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.size(120.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}
