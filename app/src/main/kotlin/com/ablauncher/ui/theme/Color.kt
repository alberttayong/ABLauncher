package com.ablauncher.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Glass Theme (default) ────────────────────────────────────────────────────
object GlassColors {
    val background = Color(0x00000000)
    val surface = Color(0x59FFFFFF)           // White 35%
    val surfaceVariant = Color(0x40FFFFFF)    // White 25%
    val surfaceBorder = Color(0x33FFFFFF)     // White 20%
    val onSurface = Color.White
    val onSurfaceVariant = Color(0xCCFFFFFF)  // White 80%
    val primary = Color(0xFF00D4FF)           // Cyan
    val onPrimary = Color.Black
    val taskbarBackground = Color(0x80000000) // Black 50%
    val scrim = Color(0x66000000)
    val error = Color(0xFFFF6B6B)
}

// ─── Dark Theme ───────────────────────────────────────────────────────────────
object DarkColors {
    val background = Color(0xFF0D0D0D)
    val surface = Color(0xD91A1A1A)           // Dark grey 85%
    val surfaceVariant = Color(0xCC252525)
    val surfaceBorder = Color(0x40FFFFFF)
    val onSurface = Color(0xFFF0F0F0)
    val onSurfaceVariant = Color(0xCCB0B0B0)
    val primary = Color(0xFF4FC3F7)           // Light blue
    val onPrimary = Color.Black
    val taskbarBackground = Color(0xE6111111)
    val scrim = Color(0x99000000)
    val error = Color(0xFFCF6679)
}

// ─── Neon Theme ───────────────────────────────────────────────────────────────
object NeonColors {
    val background = Color(0xFF000000)
    val surface = Color(0xB3000000)           // Black 70%
    val surfaceVariant = Color(0x99001A00)    // Dark green tint
    val surfaceBorder = Color(0x8000FF88)     // Neon green border
    val onSurface = Color(0xFF00FF88)         // Neon green
    val onSurfaceVariant = Color(0xCC00FF88)
    val primary = Color(0xFF00FF88)
    val onPrimary = Color.Black
    val taskbarBackground = Color(0xCC000000)
    val scrim = Color(0x99000000)
    val error = Color(0xFFFF0055)
}

// ─── Light Theme ──────────────────────────────────────────────────────────────
object LightColors {
    val background = Color(0xFFF5F5F5)
    val surface = Color(0xCCFFFFFF)           // White 80%
    val surfaceVariant = Color(0xB3FFFFFF)
    val surfaceBorder = Color(0x33000000)
    val onSurface = Color(0xFF1A1A1A)
    val onSurfaceVariant = Color(0xFF555555)
    val primary = Color(0xFF7C4DFF)           // Purple
    val onPrimary = Color.White
    val taskbarBackground = Color(0xE6FFFFFF)
    val scrim = Color(0x44000000)
    val error = Color(0xFFB00020)
}

// ─── AMOLED Theme ─────────────────────────────────────────────────────────────
object AmoledColors {
    val background = Color(0xFF000000)
    val surface = Color(0xF2000000)           // Near-black 95%
    val surfaceVariant = Color(0xFF0A0A0A)
    val surfaceBorder = Color(0x26FFFFFF)
    val onSurface = Color.White
    val onSurfaceVariant = Color(0xAAFFFFFF)
    val primary = Color.White
    val onPrimary = Color.Black
    val taskbarBackground = Color(0xFF000000)
    val scrim = Color(0xBB000000)
    val error = Color(0xFFCF6679)
}
