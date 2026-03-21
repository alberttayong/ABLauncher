package com.ablauncher.ui.wallpaper

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wallpaperRepository: WallpaperRepository,
    private val prefsDataStore: PreferencesDataStore
) : ViewModel() {

    private val _isApplying = MutableStateFlow(false)
    val isApplying: StateFlow<Boolean> = _isApplying.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    val wallpaperDim: StateFlow<Float> = prefsDataStore.wallpaperDim
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    val wallpaperBlur: StateFlow<Float> = prefsDataStore.wallpaperBlur
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    fun applyWallpaperFromUri(uri: Uri) {
        viewModelScope.launch {
            _isApplying.value = true
            try {
                wallpaperRepository.setWallpaperFromUri(uri)
                _successMessage.value = "Wallpaper applied!"
            } catch (e: Exception) {
                _successMessage.value = "Failed to apply wallpaper"
            } finally {
                _isApplying.value = false
            }
        }
    }

    fun applyBuiltInGradient(id: String, colors: List<Color>) {
        viewModelScope.launch {
            _isApplying.value = true
            try {
                withContext(Dispatchers.IO) {
                    val metrics = context.resources.displayMetrics
                    val w = metrics.widthPixels
                    val h = metrics.heightPixels
                    val bitmap = android.graphics.Bitmap.createBitmap(
                        w, h, android.graphics.Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    val intColors = colors.map { it.toArgb() }.toIntArray()
                    val positions = FloatArray(colors.size) { i ->
                        i.toFloat() / (colors.size - 1).coerceAtLeast(1)
                    }
                    val gradient = android.graphics.LinearGradient(
                        0f, 0f, 0f, h.toFloat(),
                        intColors, positions,
                        android.graphics.Shader.TileMode.CLAMP
                    )
                    val paint = android.graphics.Paint().apply { shader = gradient }
                    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
                    wallpaperRepository.setWallpaperFromBitmap(bitmap)
                }
                _successMessage.value = "Wallpaper applied!"
            } catch (e: Exception) {
                _successMessage.value = "Failed to apply wallpaper"
            } finally {
                _isApplying.value = false
            }
        }
    }

    fun removeWallpaper() {
        viewModelScope.launch {
            wallpaperRepository.clearCustomWallpaper()
            _successMessage.value = "Wallpaper removed"
        }
    }

    fun setWallpaperDim(dim: Float) {
        viewModelScope.launch { prefsDataStore.setWallpaperDim(dim) }
    }

    fun setWallpaperBlur(blur: Float) {
        viewModelScope.launch { prefsDataStore.setWallpaperBlur(blur) }
    }

    fun clearMessage() {
        _successMessage.value = null
    }
}
