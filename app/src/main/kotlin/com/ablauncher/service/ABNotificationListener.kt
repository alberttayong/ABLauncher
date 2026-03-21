package com.ablauncher.service

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.ablauncher.data.model.MediaInfo
import kotlinx.coroutines.flow.MutableStateFlow

class ABNotificationListener : NotificationListenerService() {

    companion object {
        val mediaInfo = MutableStateFlow<MediaInfo?>(null)
        var activeController: MediaController? = null
    }

    private var sessionManager: MediaSessionManager? = null

    override fun onListenerConnected() {
        super.onListenerConnected()
        sessionManager = getSystemService(MEDIA_SESSION_SERVICE) as? MediaSessionManager
        refreshMediaState()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        mediaInfo.value = null
        activeController = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        refreshMediaState()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        refreshMediaState()
    }

    private fun refreshMediaState() {
        try {
            val controllers = sessionManager?.getActiveSessions(
                ComponentName(this, ABNotificationListener::class.java)
            ) ?: run {
                mediaInfo.value = null
                activeController = null
                return
            }
            val controller = controllers.firstOrNull { ctrl ->
                val s = ctrl.playbackState?.state
                s == PlaybackState.STATE_PLAYING || s == PlaybackState.STATE_PAUSED
            }
            activeController = controller
            mediaInfo.value = controller?.let { ctrl ->
                val meta = ctrl.metadata
                val state = ctrl.playbackState
                MediaInfo(
                    title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown",
                    artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "",
                    albumArt = meta?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                        ?: meta?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART),
                    isPlaying = state?.state == PlaybackState.STATE_PLAYING,
                    packageName = ctrl.packageName
                )
            }
        } catch (_: Exception) {
            mediaInfo.value = null
        }
    }
}
