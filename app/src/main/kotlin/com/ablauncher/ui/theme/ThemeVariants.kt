package com.ablauncher.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.ablauncher.data.model.AppTheme

fun AppTheme.toColorScheme(): ColorScheme = when (this) {
    AppTheme.GLASS -> darkColorScheme(
        background = GlassColors.background,
        surface = GlassColors.surface,
        surfaceVariant = GlassColors.surfaceVariant,
        onSurface = GlassColors.onSurface,
        onSurfaceVariant = GlassColors.onSurfaceVariant,
        primary = GlassColors.primary,
        onPrimary = GlassColors.onPrimary,
        error = GlassColors.error,
        outline = GlassColors.surfaceBorder,
        scrim = GlassColors.scrim
    )
    AppTheme.DARK -> darkColorScheme(
        background = DarkColors.background,
        surface = DarkColors.surface,
        surfaceVariant = DarkColors.surfaceVariant,
        onSurface = DarkColors.onSurface,
        onSurfaceVariant = DarkColors.onSurfaceVariant,
        primary = DarkColors.primary,
        onPrimary = DarkColors.onPrimary,
        error = DarkColors.error,
        outline = DarkColors.surfaceBorder,
        scrim = DarkColors.scrim
    )
    AppTheme.NEON -> darkColorScheme(
        background = NeonColors.background,
        surface = NeonColors.surface,
        surfaceVariant = NeonColors.surfaceVariant,
        onSurface = NeonColors.onSurface,
        onSurfaceVariant = NeonColors.onSurfaceVariant,
        primary = NeonColors.primary,
        onPrimary = NeonColors.onPrimary,
        error = NeonColors.error,
        outline = NeonColors.surfaceBorder,
        scrim = NeonColors.scrim
    )
    AppTheme.LIGHT -> lightColorScheme(
        background = LightColors.background,
        surface = LightColors.surface,
        surfaceVariant = LightColors.surfaceVariant,
        onSurface = LightColors.onSurface,
        onSurfaceVariant = LightColors.onSurfaceVariant,
        primary = LightColors.primary,
        onPrimary = LightColors.onPrimary,
        error = LightColors.error,
        outline = LightColors.surfaceBorder,
        scrim = LightColors.scrim
    )
    AppTheme.AMOLED -> darkColorScheme(
        background = AmoledColors.background,
        surface = AmoledColors.surface,
        surfaceVariant = AmoledColors.surfaceVariant,
        onSurface = AmoledColors.onSurface,
        onSurfaceVariant = AmoledColors.onSurfaceVariant,
        primary = AmoledColors.primary,
        onPrimary = AmoledColors.onPrimary,
        error = AmoledColors.error,
        outline = AmoledColors.surfaceBorder,
        scrim = AmoledColors.scrim
    )
}
