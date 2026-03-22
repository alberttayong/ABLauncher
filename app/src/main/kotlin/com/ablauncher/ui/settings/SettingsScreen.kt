package com.ablauncher.ui.settings

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsStateWithLifecycle()
    val hasNotificationPermission by viewModel.hasNotificationListenerPermission.collectAsStateWithLifecycle()

    // Widget states
    val clockEnabled by viewModel.widgetClockEnabled.collectAsStateWithLifecycle()
    val weatherEnabled by viewModel.widgetWeatherEnabled.collectAsStateWithLifecycle()
    val calendarEnabled by viewModel.widgetCalendarEnabled.collectAsStateWithLifecycle()
    val newsEnabled by viewModel.widgetNewsEnabled.collectAsStateWithLifecycle()
    val searchEnabled by viewModel.widgetSearchEnabled.collectAsStateWithLifecycle()
    val mediaPlayerEnabled by viewModel.widgetMediaPlayerEnabled.collectAsStateWithLifecycle()
    val widgetPageIndices by viewModel.widgetPageIndices.collectAsStateWithLifecycle()

    // Clock / weather settings
    val clockFace by viewModel.clockFace.collectAsStateWithLifecycle()
    val clockFormat by viewModel.clockFormat.collectAsStateWithLifecycle()
    val weatherUnit by viewModel.weatherUnit.collectAsStateWithLifecycle()
    val manualCity by viewModel.manualCity.collectAsStateWithLifecycle()
    var cityInput by remember(manualCity) { mutableStateOf(manualCity) }

    // App tray settings
    val appTrayColumns by viewModel.appTrayColumns.collectAsStateWithLifecycle()
    val appTrayIconDp by viewModel.appTrayIconDp.collectAsStateWithLifecycle()
    val appTrayStyle by viewModel.appTrayStyle.collectAsStateWithLifecycle()
    val appTrayAnim by viewModel.appTrayAnim.collectAsStateWithLifecycle()

    // Home pages
    val homePageCount by viewModel.homePageCount.collectAsStateWithLifecycle()

    // Display
    val screensaverStyle by viewModel.screensaverStyle.collectAsStateWithLifecycle()

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
    val calendarPermission = rememberPermissionState(Manifest.permission.READ_CALENDAR)

    LaunchedEffect(Unit) {
        viewModel.checkUsagePermission()
        viewModel.checkNotificationListenerPermission()
    }

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

                // ── Display ───────────────────────────────────────────────────
                item {
                    ExpandableSection("Display") {
                        // Wallpaper
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("wallpaper") }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Wallpaper", fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Text("Change →", fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Screen saver style
                        SettingSubLabel("Screen Saver Style")
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "CLOCK"    to "Clock — drifting time & date",
                                "GRADIENT" to "Gradient — slow colour cycle",
                                "COLORS"   to "Colors — hue sweep"
                            ).forEach { (id, label) ->
                                val selected = screensaverStyle == id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setScreensaverStyle(id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selected,
                                            onClick = { viewModel.setScreensaverStyle(id) }
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(label, fontSize = 14.sp,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Appearance ────────────────────────────────────────────────
                item {
                    ExpandableSection("Appearance") {
                        // Theme
                        val themeLabels = mapOf(
                            AppTheme.GLASS to stringResource(R.string.theme_glass),
                            AppTheme.DARK to stringResource(R.string.theme_dark),
                            AppTheme.NEON to stringResource(R.string.theme_neon),
                            AppTheme.LIGHT to stringResource(R.string.theme_light),
                            AppTheme.AMOLED to stringResource(R.string.theme_amoled)
                        )
                        SettingSubLabel("Theme")
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

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        SliderRow(
                            label = stringResource(R.string.blur_radius_label),
                            valueText = "${themeConfig.blurRadius.toInt()}",
                            value = themeConfig.blurRadius,
                            onValueChange = { viewModel.setBlurRadius(it) },
                            valueRange = 0f..40f
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        SliderRow(
                            label = stringResource(R.string.panel_opacity_label),
                            valueText = "${(themeConfig.panelAlpha * 100).toInt()}%",
                            value = themeConfig.panelAlpha,
                            onValueChange = { viewModel.setPanelAlpha(it) },
                            valueRange = 0.1f..0.9f
                        )
                    }
                }

                // ── Animations ────────────────────────────────────────────────
                item {
                    ExpandableSection("Animations") {
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

                // ── Widgets ───────────────────────────────────────────────────
                item {
                    ExpandableSection("Widgets") {
                        // Search Bar
                        WidgetToggleRow("Search Bar", searchEnabled) {
                            viewModel.toggleWidget(WidgetType.SEARCH, it)
                        }
                        if (searchEnabled && homePageCount > 1) {
                            WidgetPagePicker(
                                currentPage = widgetPageIndices[WidgetType.SEARCH] ?: 0,
                                pageCount = homePageCount,
                                onPageSelected = { viewModel.moveWidgetToPage(WidgetType.SEARCH, it) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Clock
                        WidgetToggleRow("Clock", clockEnabled) {
                            viewModel.toggleWidget(WidgetType.CLOCK, it)
                        }
                        if (clockEnabled) {
                            if (homePageCount > 1) {
                                WidgetPagePicker(
                                    currentPage = widgetPageIndices[WidgetType.CLOCK] ?: 0,
                                    pageCount = homePageCount,
                                    onPageSelected = { viewModel.moveWidgetToPage(WidgetType.CLOCK, it) }
                                )
                            }
                            SettingSubLabel("Clock Face")
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ClockFace.entries.forEach { face ->
                                    FilterChip(selected = clockFace == face,
                                        onClick = { viewModel.setClockFace(face) },
                                        label = { Text(face.displayName, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f))
                                }
                            }
                            SettingSubLabel("Time Format")
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ClockFormat.entries.forEach { fmt ->
                                    FilterChip(selected = clockFormat == fmt,
                                        onClick = { viewModel.setClockFormat(fmt) },
                                        label = { Text(fmt.displayName, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Weather
                        WidgetToggleRow("Weather", weatherEnabled) {
                            viewModel.toggleWidget(WidgetType.WEATHER, it)
                        }
                        if (weatherEnabled) {
                            if (homePageCount > 1) {
                                WidgetPagePicker(
                                    currentPage = widgetPageIndices[WidgetType.WEATHER] ?: 0,
                                    pageCount = homePageCount,
                                    onPageSelected = { viewModel.moveWidgetToPage(WidgetType.WEATHER, it) }
                                )
                            }
                            if (!locationPermission.status.isGranted) {
                                PermissionRow("Grant location for auto-detect weather") {
                                    locationPermission.launchPermissionRequest()
                                }
                            }
                            SettingSubLabel("Temperature Unit")
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("CELSIUS" to "Celsius", "FAHRENHEIT" to "Fahrenheit")
                                    .forEach { (key, label) ->
                                        FilterChip(selected = weatherUnit == key,
                                            onClick = { viewModel.setWeatherUnit(key) },
                                            label = { Text(label, fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f))
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
                                        TextButton(onClick = { viewModel.setManualCity(cityInput.trim()) }) {
                                            Text("Save")
                                        }
                                    }
                                }
                            )
                            Spacer(Modifier.height(4.dp))
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Calendar
                        WidgetToggleRow("Calendar", calendarEnabled) {
                            viewModel.toggleWidget(WidgetType.CALENDAR, it)
                        }
                        if (calendarEnabled) {
                            if (homePageCount > 1) {
                                WidgetPagePicker(
                                    currentPage = widgetPageIndices[WidgetType.CALENDAR] ?: 0,
                                    pageCount = homePageCount,
                                    onPageSelected = { viewModel.moveWidgetToPage(WidgetType.CALENDAR, it) }
                                )
                            }
                            if (!calendarPermission.status.isGranted) {
                                PermissionRow("Grant calendar permission to show events") {
                                    calendarPermission.launchPermissionRequest()
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Google News
                        WidgetToggleRow("Google News", newsEnabled) {
                            viewModel.toggleWidget(WidgetType.NEWS, it)
                        }
                        if (newsEnabled && homePageCount > 1) {
                            WidgetPagePicker(
                                currentPage = widgetPageIndices[WidgetType.NEWS] ?: 0,
                                pageCount = homePageCount,
                                onPageSelected = { viewModel.moveWidgetToPage(WidgetType.NEWS, it) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Media Player
                        WidgetToggleRow("Media Player", mediaPlayerEnabled) {
                            viewModel.toggleWidget(WidgetType.MEDIA_PLAYER, it)
                        }
                        if (mediaPlayerEnabled) {
                            if (homePageCount > 1) {
                                WidgetPagePicker(
                                    currentPage = widgetPageIndices[WidgetType.MEDIA_PLAYER] ?: 0,
                                    pageCount = homePageCount,
                                    onPageSelected = { viewModel.moveWidgetToPage(WidgetType.MEDIA_PLAYER, it) }
                                )
                            }
                            if (!hasNotificationPermission) {
                                PermissionRow("Grant Notification Access for media controls") {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        }
                    }
                }

                // ── App Tray ──────────────────────────────────────────────────
                item {
                    ExpandableSection("App Tray") {
                        SettingSubLabel("Background Style")
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("FROSTED" to "Frosted", "DARK" to "Dark",
                                   "LIGHT" to "Light", "TRANSPARENT" to "Clear")
                                .forEach { (key, label) ->
                                    FilterChip(selected = appTrayStyle == key,
                                        onClick = { viewModel.setAppTrayStyle(key) },
                                        label = { Text(label, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f))
                                }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        SettingSubLabel("Grid Columns")
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(3, 4, 5, 6).forEach { col ->
                                FilterChip(selected = appTrayColumns == col,
                                    onClick = { viewModel.setAppTrayColumns(col) },
                                    label = { Text("$col") },
                                    modifier = Modifier.weight(1f))
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        SettingSubLabel("Icon Size")
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(40 to "Compact", 56 to "Normal", 72 to "Large").forEach { (dp, label) ->
                                FilterChip(selected = appTrayIconDp == dp,
                                    onClick = { viewModel.setAppTrayIconDp(dp) },
                                    label = { Text(label, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f))
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        SettingSubLabel("Enter Animation")
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("SLIDE_UP" to "Slide", "FADE" to "Fade", "SCALE" to "Scale")
                                .forEach { (key, label) ->
                                    FilterChip(selected = appTrayAnim == key,
                                        onClick = { viewModel.setAppTrayAnim(key) },
                                        label = { Text(label, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f))
                                }
                        }
                    }
                }

                // ── Home Pages ────────────────────────────────────────────────
                item { SectionHeader("Home Pages") }

                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Pages", style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Text("$homePageCount",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(8.dp))
                            repeat(homePageCount) { idx ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text("Page ${idx + 1}",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp)
                                    if (homePageCount > 1) {
                                        TextButton(onClick = { viewModel.removePage(idx) }) {
                                            Text("Remove",
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.addPage() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Add Page")
                            }
                        }
                    }
                }

                // ── Permissions ───────────────────────────────────────────────
                item { SectionHeader(stringResource(R.string.usage_access_title)) }

                item {
                    SettingsCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
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
                                Text(stringResource(R.string.usage_access_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Text(stringResource(R.string.usage_access_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Text(stringResource(R.string.version_label),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text(BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun ExpandableSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                                  else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun WidgetPagePicker(
    currentPage: Int,
    pageCount: Int,
    onPageSelected: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Text(
            text = "Page:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(end = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(pageCount) { idx ->
                FilterChip(
                    selected = currentPage == idx,
                    onClick = { onPageSelected(idx) },
                    label = { Text("${idx + 1}", fontSize = 11.sp) }
                )
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
private fun WidgetToggleRow(label: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f))
        TextButton(onClick = onRequest) { Text("Allow", fontSize = 12.sp) }
    }
}
