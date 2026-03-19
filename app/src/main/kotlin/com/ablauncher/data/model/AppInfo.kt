package com.ablauncher.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val lastUsedTimestamp: Long = 0L,
    val isInTaskbar: Boolean = false
)
