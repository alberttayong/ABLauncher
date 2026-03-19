package com.ablauncher.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ablauncher.BuildConfig
import com.ablauncher.R
import com.ablauncher.data.model.AppTheme
import com.ablauncher.ui.components.FrostedGlassPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.checkUsagePermission() }

    FrostedGlassPanel(
        modifier = Modifier.fillMaxSize(),
        cornerRadius = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = stringResource(R.string.settings_label),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Appearance ────────────────────────────────────────────────
                item {
                    SectionHeader("Appearance")
                }

                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Theme",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(12.dp))
                            // Theme chip selector
                            val themes = AppTheme.entries
                            val themeLabels = mapOf(
                                AppTheme.GLASS to stringResource(R.string.theme_glass),
                                AppTheme.DARK to stringResource(R.string.theme_dark),
                                AppTheme.NEON to stringResource(R.string.theme_neon),
                                AppTheme.LIGHT to stringResource(R.string.theme_light),
                                AppTheme.AMOLED to stringResource(R.string.theme_amoled)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                themes.forEach { theme ->
                                    FilterChip(
                                        selected = themeConfig.appTheme == theme,
                                        onClick = { viewModel.setTheme(theme) },
                                        label = {
                                            Text(
                                                themeLabels[theme] ?: theme.name,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(R.string.blur_radius_label),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "${themeConfig.blurRadius.toInt()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = themeConfig.blurRadius,
                                onValueChange = { viewModel.setBlurRadius(it) },
                                valueRange = 0f..40f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(R.string.panel_opacity_label),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "${(themeConfig.panelAlpha * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = themeConfig.panelAlpha,
                                onValueChange = { viewModel.setPanelAlpha(it) },
                                valueRange = 0.1f..0.9f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ── Permissions ───────────────────────────────────────────────
                item {
                    SectionHeader(stringResource(R.string.usage_access_title))
                }

                item {
                    SettingsCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = if (hasUsagePermission) Icons.Default.CheckCircle
                                              else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (hasUsagePermission) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.usage_access_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    stringResource(R.string.usage_access_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!hasUsagePermission) {
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { viewModel.requestUsagePermission() }) {
                                    Text(stringResource(R.string.grant_permission))
                                }
                            }
                        }
                    }
                }

                // ── About ─────────────────────────────────────────────────────
                item { SectionHeader(stringResource(R.string.about_label)) }

                item {
                    SettingsCard {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                stringResource(R.string.version_label),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth(),
        content = content
    )
}
