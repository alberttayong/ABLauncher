package com.ablauncher.util

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asComposeRenderEffect

/**
 * Creates a Compose-compatible blur RenderEffect.
 * Uses Android 12+ (API 31) RenderEffect API — safe here since minSdk is 34.
 */
@Composable
fun rememberBlurEffect(radiusX: Float, radiusY: Float = radiusX): androidx.compose.ui.graphics.RenderEffect {
    return remember(radiusX, radiusY) {
        RenderEffect.createBlurEffect(
            radiusX.coerceAtLeast(0.01f),
            radiusY.coerceAtLeast(0.01f),
            Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    }
}
