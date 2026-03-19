package com.ablauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.ablauncher.data.model.ThemeConfig

@Composable
fun ABLauncherTheme(
    themeConfig: ThemeConfig = ThemeConfig(),
    content: @Composable () -> Unit
) {
    val colorScheme = themeConfig.appTheme.toColorScheme()
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ABLauncherTypography,
        content = content
    )
}
