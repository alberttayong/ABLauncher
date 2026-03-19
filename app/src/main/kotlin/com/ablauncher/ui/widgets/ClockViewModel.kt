package com.ablauncher.ui.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.model.ClockFace
import com.ablauncher.data.model.ClockFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(
    private val prefs: PreferencesDataStore
) : ViewModel() {

    val clockFace = prefs.clockFace
        .map { runCatching { ClockFace.valueOf(it) }.getOrDefault(ClockFace.DIGITAL_FULL) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClockFace.DIGITAL_FULL)

    val clockFormat = prefs.clockFormat
        .map { runCatching { ClockFormat.valueOf(it) }.getOrDefault(ClockFormat.HOUR_12) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClockFormat.HOUR_12)

    fun setClockFace(face: ClockFace) {
        viewModelScope.launch { prefs.setClockFace(face.name) }
    }

    fun setClockFormat(format: ClockFormat) {
        viewModelScope.launch { prefs.setClockFormat(format.name) }
    }
}
