package com.ablauncher.data.model

enum class AppTheme {
    GLASS, DARK, NEON, LIGHT, AMOLED
}

data class ThemeConfig(
    val appTheme: AppTheme = AppTheme.GLASS,
    val accentColorValue: Long = 0xFF00D4FF,
    val blurRadius: Float = 20f,
    val panelAlpha: Float = 0.35f,
    val animSpeed: Float = 1.0f   // multiplier: 0.25 = very slow, 1.0 = normal, 3.0 = fast
)
