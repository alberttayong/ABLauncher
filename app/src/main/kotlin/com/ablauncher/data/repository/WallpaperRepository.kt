package com.ablauncher.data.repository

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.ablauncher.data.datastore.PreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsDataStore: PreferencesDataStore
) {
    private val wallpaperManager: WallpaperManager by lazy {
        WallpaperManager.getInstance(context)
    }

    /** Set wallpaper from a content URI (from gallery picker) */
    suspend fun setWallpaperFromUri(uri: Uri) = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            wallpaperManager.setStream(stream)
        }
        prefsDataStore.setWallpaperUri(uri.toString())
    }

    /** Set wallpaper from a Bitmap (for built-in gradient wallpapers).
     *  Saves to internal storage so HomeScreen can display it via wallpaperUri. */
    suspend fun setWallpaperFromBitmap(bitmap: Bitmap) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "gradient_wallpaper.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        prefsDataStore.setWallpaperUri(Uri.fromFile(file).toString())
        runCatching { wallpaperManager.setBitmap(bitmap) }
    }

    /** Clear custom wallpaper selection (revert to DataStore null) */
    suspend fun clearCustomWallpaper() {
        prefsDataStore.setWallpaperUri(null)
    }
}
