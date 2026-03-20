package com.ablauncher.ui.settings

import android.Manifest
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ablauncher.BuildConfig
import com.ablauncher.R
import com.ablauncher.data.model.AppTheme
import com.ablauncher.data.model.ClockFace
import com.ablauncher.data.model.ClockFormat
import com.ablauncher.data.model.WidgetType
import com.ablauncher.ui.components.FrostedGlassPanel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsStateWithLifecycle()

    // Widget states
    val clockEnabled by viewModel.widgetClockEnabled.collectAsStateWithLifecycle()
    val weatherEnabled by viewModel.widgetWeatherEnabled.collectAsStateWithLifecycle()
    val calendarEnabled by viewModel.widgetCalendarEnabled.collectAsStateWithLifecycle()
    val newsEnabled by viewModel.widgetNewsEnabled.collectAsStateWithLifecycle()
    val searchEnabled by viewModel.widgetSearchEnabled.collectAsStateWithLifecycle()

    // Clock / weather settings
    val clockFace by viewModel.clockFace.collectAsStateWithLifecycle()
    val clockFormat by viewModel.clockFormat.collectAsStateWithLifecycle()
    val weatherUnit by viewModel.weatherUnit.collectAsStateWithLifecycle()
    val manualCity by viewModel.manualCity.collectAsStateWithLifecycle()
    var cityInput by remember(manualCity) { mutableStateOf(manualCity) }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
    val calendarPermission = rememberPermissionState(Manifest.permission.READ_CALENDAR)

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
                item { SectionHeader("Appearance") }

                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Theme", style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(12.dp))
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
                                AppTheme.entries.forEach { theme ->
                                    FilterChip(
                                        selected = themeConfig.appTheme == theme,
                                        onClick = { viewModel.setTheme(theme) },
                                        label = {
                                            Text(themeLabels[theme] ?: theme.name,
                                                style = MaterialTheme.typography.labelMedium)
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
                            SliderRow(
                                label = stringResource(R.string.blur_radius_label),
                                valueText = "${themeConfig.blurRadius.toInt()}",
                                value = themeConfig.blurRadius,
                                onValueChange = { viewModel.setBlurRadius(it) },
                                valueRange = 0f..40f
                            )
                        }
                    }
                }

                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SliderRow(
                                label = stringResource(R.string.panel_opacity_label),
                                valueText = "${(themeConfig.panelAlpha * 100).toInt()}%",
                                value = themeConfig.panelAlpha,
                                onValueChange = { viewModel.setPanelAlpha(it) },
                                valueRange = 0.1f..0.9f
                            )
                        }
                    }
                }

                // ── Animations ────────────────────────────────────────────────
                item { SectionHeader("Animations") }

                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val speedLabel = when {
                                themeConfig.animSpeed <= 0.4f -> "Very Slow"
                                themeConfig.animSpeed <= 0.75f -> "Slow"
                                themeConfig.animSpeed <= 1.25f -> "Normal"
                                themeConfig.animSpeed <= 1.75f -> "Fast"
                                else -> "Very Fast"
                            }
                            SliderRow(
                                label = "Transition Speed",
                                valueText = "${"%.1f".format(themeConfig.animSpeed)}× ($speedLabel)",
                                value = themeConfig.animSpeed,
                                onValueChange = { viewModel.setAnimSpeed(it) },
                                valueRange = 0.25f..3.0f
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Slower", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                                Text("Faster", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                            }
                        }
                    }
                }

                // ── Widgets ───────────────────────────────────────────────────
                item { SectionHeader("Widgets") }

                // Search Bar
                item {
                    SettingsCard {
                        WidgetToggleRow(
                            label = "Search Bar",
                            enabled = searchEnabled,
                            onToggle = { viewModel.toggleWidget(WidgetType.SEARCH, it) }
                        )
                    }
                }

                // Clock
                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(
                            bottom = if (clockEnabled) 16.dp else 0.dp)
                        ) {
                            WidgetToggleRow(
                                label = "Clock",
                                enabled = clockEnabled,
                                onToggle = { viewModel.toggleWidget(WidgetType.CLOCK, it) }
                            )
                            if (clockEnabled) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    SettingSubLabel("Clock Face")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ClockFace.entries.forEach { face ->
                                            FilterChip(
                                                selected = clockFace == face,
                                                onClick = { viewModel.setClockFace(face) },
                                                label = { Text(face.displayName, fontSize = 11.sp) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    SettingSubLabel("Time Format")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ClockFormat.entries.forEach { fmt ->
                                            FilterChip(
                                                selected = clockFormat == fmt,
                                                onClick = { viewModel.setClockFormat(fmt) },
                                                label = { Text(fmt.displayName, fontSize = 12.sp) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Weather
                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(
                            bottom = if (weatherEnabled) 16.dp else 0.dp)
                        ) {
                            WidgetToggleRow(
                                label = "Weather",
                                enabled = weatherEnabled,
                                onToggle = { viewModel.toggleWidget(WidgetType.WEATHER, it) }
                            )
                            if (weatherEnabled) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    if (!locationPermission.status.isGranted) {
                                        PermissionRow(
                                            text = "Grant location permission for auto-detect weather",
                                            onRequest = { locationPermission.launchPermissionRequest() }
                                        )
                                    }
                                    SettingSubLabel("Temperature Unit")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("CELSIUS" to "Celsius", "FAHRENHEIT" to "Fahrenheit")
                                            .forEach { (key, label) ->
                                                FilterChip(
                                                    selected = weatherUnit == key,
                                                    onClick = { viewModel.setWeatherUnit(key) },
                                                    label = { Text(label, fontSize = 12.sp) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                    }
                                    SettingSubLabel("Manual City (overrides GPS)")
                                    OutlinedTextField(
                                        value = cityInput,
                                        onValueChange = { cityInput = it },
                                        placeholder = { Text("e.g. London, Tokyo…") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            if (cityInput != manualCity) {
                                                TextButton(
                                                    onClick = { viewModel.setManualCity(cityInput.trim()) }
                                                ) { Text("Save") }
                                            }
                                        }
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }

                // Calendar
                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(
                            bottom = if (calendarEnabled && !calendarPermission.status.isGranted) 16.dp else 0.dp)
                        ) {
                            WidgetToggleRow(
                                label = "Calendar",
                                enabled = calendarEnabled,
                                onToggle = { viewModel.toggleWidget(WidgetType.CALENDAR, it) }
                            )
                            if (calendarEnabled && !calendarPermission.status.isGranted) {
                                Box(Modifier.padding(horizontal = 16.dp)) {
                                    PermissionRow(
                                        text = "Grant calendar permission to show events",
                                        onRequest = { calendarPermission.launchPermissionRequest() }
                                    )
                                }
                            }
                        }
                    }
                }

                // News
                item {
                    SettingsCard {
                        WidgetToggleRow(
                            label = "Google News",
                            enabled = newsEnabled,
                            onToggle = { viewModel.toggleWidget(WidgetType.NEWS, it) }
                        )
                    }
                }

                // ── Permissions ───────────────────────────────────────────────
                item { SectionHeader(stringResource(R.string.usage_access_title)) }

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

// ── Private helpers ────────────────────────────────────────────────────────────

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
private fun SettingSubLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
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

@Composable
private fun SliderRow(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface)
        Text(valueText, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary)
    }
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun WidgetToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun PermissionRow(text: String, onRequest: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onRequest) {
            Text("Allow", fontSize = 12.sp)
        }
    }
}
