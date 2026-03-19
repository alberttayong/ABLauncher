package com.ablauncher.ui.widgets

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.model.ClockFace
import com.ablauncher.data.model.ClockFormat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WidgetPanelSheet(
    clockEnabled: Boolean,
    weatherEnabled: Boolean,
    calendarEnabled: Boolean,
    newsEnabled: Boolean,
    searchEnabled: Boolean,
    onClockToggle: (Boolean) -> Unit,
    onWeatherToggle: (Boolean) -> Unit,
    onCalendarToggle: (Boolean) -> Unit,
    onNewsToggle: (Boolean) -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    clockViewModel: ClockViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val clockFace by clockViewModel.clockFace.collectAsState()
    val clockFormat by clockViewModel.clockFormat.collectAsState()
    val weatherUnit by weatherViewModel.weatherUnit.collectAsState()
    val manualCity by weatherViewModel.manualCity.collectAsState()

    var cityInput by remember(manualCity) { mutableStateOf(manualCity) }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
    val calendarPermission = rememberPermissionState(Manifest.permission.READ_CALENDAR)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "Widgets",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ── Search Bar ────────────────────────────────────────────
            WidgetToggleRow(label = "Search Bar", enabled = searchEnabled, onToggle = onSearchToggle)
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // ── Clock ─────────────────────────────────────────────────
            WidgetToggleRow(label = "Clock", enabled = clockEnabled, onToggle = onClockToggle)

            if (clockEnabled) {
                SectionLabel("Clock Face")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClockFace.entries.forEach { face ->
                        FilterChip(
                            selected = clockFace == face,
                            onClick = { clockViewModel.setClockFace(face) },
                            label = { Text(face.displayName, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                SectionLabel("Time Format")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClockFormat.entries.forEach { fmt ->
                        FilterChip(
                            selected = clockFormat == fmt,
                            onClick = { clockViewModel.setClockFormat(fmt) },
                            label = { Text(fmt.displayName, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // ── Weather ───────────────────────────────────────────────
            WidgetToggleRow(label = "Weather", enabled = weatherEnabled, onToggle = onWeatherToggle)

            if (weatherEnabled) {
                if (!locationPermission.status.isGranted) {
                    PermissionRow(
                        text = "Grant location permission for auto-detect weather",
                        onRequest = { locationPermission.launchPermissionRequest() }
                    )
                }

                SectionLabel("Temperature Unit")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("CELSIUS" to "Celsius", "FAHRENHEIT" to "Fahrenheit").forEach { (key, label) ->
                        FilterChip(
                            selected = weatherUnit == key,
                            onClick = { weatherViewModel.setWeatherUnit(key) },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                SectionLabel("Manual City (overrides GPS)")
                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    placeholder = { Text("e.g. London, Tokyo...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (cityInput != manualCity) {
                            androidx.compose.material3.TextButton(
                                onClick = { weatherViewModel.setManualCity(cityInput.trim()) }
                            ) { Text("Save") }
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // ── Calendar ──────────────────────────────────────────────
            WidgetToggleRow(label = "Calendar", enabled = calendarEnabled, onToggle = onCalendarToggle)

            if (calendarEnabled && !calendarPermission.status.isGranted) {
                PermissionRow(
                    text = "Grant calendar permission to show events",
                    onRequest = { calendarPermission.launchPermissionRequest() }
                )
                Spacer(Modifier.height(8.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // ── News ──────────────────────────────────────────────────
            WidgetToggleRow(label = "Google News", enabled = newsEnabled, onToggle = onNewsToggle)
        }
    }
}

@Composable
private fun WidgetToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun PermissionRow(text: String, onRequest: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f)
        )
        androidx.compose.material3.TextButton(onClick = onRequest) {
            Text("Allow", fontSize = 12.sp)
        }
    }
}
