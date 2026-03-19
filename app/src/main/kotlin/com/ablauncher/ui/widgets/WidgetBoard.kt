package com.ablauncher.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ablauncher.data.model.ClockFace
import com.ablauncher.data.model.ClockFormat

@Composable
fun WidgetBoard(
    clockEnabled: Boolean,
    weatherEnabled: Boolean,
    calendarEnabled: Boolean,
    newsEnabled: Boolean,
    searchEnabled: Boolean,
    modifier: Modifier = Modifier,
    clockViewModel: ClockViewModel = hiltViewModel()
) {
    val clockFace by clockViewModel.clockFace.collectAsState()
    val clockFormat by clockViewModel.clockFormat.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 88.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp)
    ) {
        if (searchEnabled) {
            SearchBarWidget()
        }
        if (clockEnabled) {
            ClockWidget(face = clockFace, format = clockFormat)
        }
        if (weatherEnabled) {
            WeatherWidget()
        }
        if (calendarEnabled) {
            CalendarWidget()
        }
        if (newsEnabled) {
            NewsWidget()
        }
    }
}
