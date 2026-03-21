package com.ablauncher.data.model

import android.graphics.Bitmap

data class MediaInfo(
    val title: String = "",
    val artist: String = "",
    val albumArt: Bitmap? = null,
    val isPlaying: Boolean = false,
    val packageName: String = ""
)
