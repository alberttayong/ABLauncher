package com.ablauncher.ui.widgets

import android.media.session.PlaybackState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ablauncher.data.model.MediaInfo
import com.ablauncher.service.ABNotificationListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MediaPlayerViewModel @Inject constructor() : ViewModel() {

    val mediaInfo: StateFlow<MediaInfo?> = ABNotificationListener.mediaInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun playPause() {
        val ctrl = ABNotificationListener.activeController ?: return
        if (ctrl.playbackState?.state == PlaybackState.STATE_PLAYING) {
            ctrl.transportControls.pause()
        } else {
            ctrl.transportControls.play()
        }
    }

    fun skipNext() {
        ABNotificationListener.activeController?.transportControls?.skipToNext()
    }

    fun skipPrevious() {
        ABNotificationListener.activeController?.transportControls?.skipToPrevious()
    }
}
