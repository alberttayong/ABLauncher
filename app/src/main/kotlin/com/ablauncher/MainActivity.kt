package com.ablauncher

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ablauncher.ui.apptray.AppTrayScreen
import com.ablauncher.ui.home.HomeScreen
import com.ablauncher.ui.settings.SettingsScreen
import com.ablauncher.ui.settings.SettingsViewModel
import com.ablauncher.ui.theme.ABLauncherTheme
import com.ablauncher.ui.wallpaper.WallpaperPickerScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Full-screen immersive mode — no status/nav bar chrome
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeConfig by settingsViewModel.themeConfig.collectAsStateWithLifecycle()
            val appTrayAnim by settingsViewModel.appTrayAnim.collectAsStateWithLifecycle()

            ABLauncherTheme(themeConfig = themeConfig) {
                val navController = rememberNavController()

                // Derive animation durations from the persisted speed multiplier.
                val slideDuration by remember(themeConfig.animSpeed) {
                    derivedStateOf { (300f / themeConfig.animSpeed).toInt().coerceIn(60, 1200) }
                }
                val fadeDuration by remember(themeConfig.animSpeed) {
                    derivedStateOf { (200f / themeConfig.animSpeed).toInt().coerceIn(40, 800) }
                }

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ── Home ──────────────────────────────────────────────────
                    composable("home") {
                        HomeScreen(navController = navController)
                    }

                    // ── App Tray (animation style from settings) ───────────────
                    composable(
                        route = "apptray",
                        enterTransition = {
                            when (appTrayAnim) {
                                "FADE" -> fadeIn(tween(fadeDuration))
                                "SCALE" -> scaleIn(
                                    animationSpec = tween(slideDuration),
                                    initialScale = 0.85f
                                ) + fadeIn(tween(fadeDuration))
                                else -> slideInVertically(
                                    animationSpec = tween(slideDuration),
                                    initialOffsetY = { it }
                                ) + fadeIn(tween(fadeDuration))
                            }
                        },
                        exitTransition = {
                            when (appTrayAnim) {
                                "FADE" -> fadeOut(tween(fadeDuration))
                                "SCALE" -> scaleOut(
                                    animationSpec = tween(slideDuration),
                                    targetScale = 0.85f
                                ) + fadeOut(tween(fadeDuration))
                                else -> slideOutVertically(
                                    animationSpec = tween(slideDuration),
                                    targetOffsetY = { it }
                                ) + fadeOut(tween(fadeDuration))
                            }
                        }
                    ) {
                        AppTrayScreen(navController = navController)
                    }

                    // ── Settings (slides in from right) ───────────────────────
                    composable(
                        route = "settings",
                        enterTransition = {
                            slideInHorizontally(
                                animationSpec = tween(slideDuration),
                                initialOffsetX = { it }
                            ) + fadeIn(tween(fadeDuration))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                animationSpec = tween(slideDuration),
                                targetOffsetX = { it }
                            ) + fadeOut(tween(fadeDuration))
                        }
                    ) {
                        SettingsScreen(navController = navController)
                    }

                    // ── Wallpaper Picker (slides up) ──────────────────────────
                    composable(
                        route = "wallpaper",
                        enterTransition = {
                            slideInVertically(
                                animationSpec = tween(slideDuration),
                                initialOffsetY = { it }
                            ) + fadeIn(tween(fadeDuration))
                        },
                        exitTransition = {
                            slideOutVertically(
                                animationSpec = tween(slideDuration),
                                targetOffsetY = { it }
                            ) + fadeOut(tween(fadeDuration))
                        }
                    ) {
                        WallpaperPickerScreen(navController = navController)
                    }
                }
            }
        }
    }
}
