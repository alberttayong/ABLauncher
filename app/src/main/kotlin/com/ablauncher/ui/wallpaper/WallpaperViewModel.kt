package com.ablauncher.ui.wallpaper

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ablauncher.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository
) : ViewModel() {

    private val _isApplying = MutableStateFlow(false)
    val isApplying: StateFlow<Boolean> = _isApplying.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

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

    fun clearMessage() {
        _successMessage.value = null
    }
}
