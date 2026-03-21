package com.ablauncher.ui.wallpaper

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ablauncher.R
import com.ablauncher.ui.components.FrostedGlassPanel

/** Gradient wallpaper descriptors for built-in options */
private data class GradientWallpaper(
    val id: String,
    val label: String,
    val colors: List<Color>
)

private val builtInWallpapers = listOf(
    GradientWallpaper("aurora", "Aurora", listOf(Color(0xFF0D0D2B), Color(0xFF1A3A4A), Color(0xFF0D2B1A))),
    GradientWallpaper("twilight", "Twilight", listOf(Color(0xFF1A0533), Color(0xFF3D0A5E), Color(0xFF7B1EA2))),
    GradientWallpaper("ocean", "Ocean", listOf(Color(0xFF001A2E), Color(0xFF003A5C), Color(0xFF006994))),
    GradientWallpaper("ember", "Ember", listOf(Color(0xFF1A0800), Color(0xFF5C1900), Color(0xFFBF360C))),
    GradientWallpaper("forest", "Forest", listOf(Color(0xFF0A1A0A), Color(0xFF1B5E20), Color(0xFF2E7D32))),
    GradientWallpaper("midnight", "Midnight", listOf(Color(0xFF000000), Color(0xFF0A0A0A), Color(0xFF1A1A2E))),
    GradientWallpaper("rose", "Rose Gold", listOf(Color(0xFF1A0A0F), Color(0xFF5C1A2E), Color(0xFFB06080))),
    GradientWallpaper("cyber", "Cyber", listOf(Color(0xFF000D1A), Color(0xFF001433), Color(0xFF002B66)))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperPickerScreen(
    navController: NavController,
    viewModel: WallpaperViewModel = hiltViewModel()
) {
    val isApplying by viewModel.isApplying.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val wallpaperDim by viewModel.wallpaperDim.collectAsStateWithLifecycle()
    val wallpaperBlur by viewModel.wallpaperBlur.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.applyWallpaperFromUri(it) }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

    FrostedGlassPanel(
        modifier = Modifier.fillMaxSize(),
        cornerRadius = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = stringResource(R.string.wallpaper_label),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ── Tabs ──────────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.built_in_wallpapers)) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.gallery_wallpapers)) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                    text = { Text("Adjust") })
            }

            // ── Content ───────────────────────────────────────────────────────
            when (selectedTab) {
                0 -> BuiltInWallpapers(
                    onWallpaperSelected = { gradient ->
                        viewModel.applyBuiltInGradient(gradient.id, gradient.colors)
                    }
                )
                1 -> GalleryTab(
                    onPickFromGallery = { galleryLauncher.launch("image/*") },
                    onRemoveWallpaper = { viewModel.removeWallpaper() }
                )
                2 -> AdjustmentsTab(
                    wallpaperDim = wallpaperDim,
                    wallpaperBlur = wallpaperBlur,
                    onDimChange = { viewModel.setWallpaperDim(it) },
                    onBlurChange = { viewModel.setWallpaperBlur(it) }
                )
            }

            // ── Status indicators ─────────────────────────────────────────────
            if (isApplying) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            successMessage?.let { msg ->
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
            }
        }
    }
}

@Composable
private fun BuiltInWallpapers(onWallpaperSelected: (GradientWallpaper) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(builtInWallpapers, key = { it.id }) { wallpaper ->
            GradientWallpaperTile(
                gradient = wallpaper,
                onClick = { onWallpaperSelected(wallpaper) }
            )
        }
    }
}

@Composable
private fun GradientWallpaperTile(
    gradient: GradientWallpaper,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.6f)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(gradient.colors))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = gradient.label, style = MaterialTheme.typography.labelMedium, color = Color.White)
        }
    }
}

@Composable
private fun GalleryTab(onPickFromGallery: () -> Unit, onRemoveWallpaper: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Pick a photo from your gallery to use as your wallpaper",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onPickFromGallery,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Choose from Gallery")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onRemoveWallpaper,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Remove Wallpaper")
        }
    }
}

@Composable
private fun AdjustmentsTab(
    wallpaperDim: Float,
    wallpaperBlur: Float,
    onDimChange: (Float) -> Unit,
    onBlurChange: (Float) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dim", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text("${(wallpaperDim * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = wallpaperDim,
                        onValueChange = onDimChange,
                        valueRange = 0f..0.8f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Darkens the wallpaper behind the launcher",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Blur", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium)
                        Text("${(wallpaperBlur * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = wallpaperBlur,
                        onValueChange = onBlurChange,
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Blurs the gallery wallpaper (requires Android 12+)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}
