package com.ablauncher

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ablauncher.data.model.ThemeConfig
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

            ABLauncherTheme(themeConfig = themeConfig) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ── Home ──────────────────────────────────────────────────
                    composable("home") {
                        HomeScreen(navController = navController)
                    }

                    // ── App Tray (slides up from bottom) ──────────────────────
                    composable(
                        route = "apptray",
                        enterTransition = {
                            slideInVertically(
                                animationSpec = tween(300),
                                initialOffsetY = { it }
                            ) + fadeIn(tween(200))
                        },
                        exitTransition = {
                            slideOutVertically(
                                animationSpec = tween(300),
                                targetOffsetY = { it }
                            ) + fadeOut(tween(200))
                        }
                    ) {
                        AppTrayScreen(navController = navController)
                    }

                    // ── Settings (slides in from right) ───────────────────────
                    composable(
                        route = "settings",
                        enterTransition = {
                            slideInHorizontally(
                                animationSpec = tween(300),
                                initialOffsetX = { it }
                            ) + fadeIn(tween(200))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                animationSpec = tween(300),
                                targetOffsetX = { it }
                            ) + fadeOut(tween(200))
                        }
                    ) {
                        SettingsScreen(navController = navController)
                    }

                    // ── Wallpaper Picker (slides up) ──────────────────────────
                    composable(
                        route = "wallpaper",
                        enterTransition = {
                            slideInVertically(
                                animationSpec = tween(300),
                                initialOffsetY = { it }
                            ) + fadeIn(tween(200))
                        },
                        exitTransition = {
                            slideOutVertically(
                                animationSpec = tween(300),
                                targetOffsetY = { it }
                            ) + fadeOut(tween(200))
                        }
                    ) {
                        WallpaperPickerScreen(navController = navController)
                    }
                }
            }
        }
    }
}
