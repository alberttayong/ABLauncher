package com.ablauncher.ui.wallpaper

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ablauncher.R
import com.ablauncher.ui.components.FrostedGlassPanel

// ── Built-in gradient wallpapers ──────────────────────────────────────────────

private data class GradientWallpaper(
    val id: String,
    val label: String,
    val colors: List<Color>
)

private val gradientWallpapers = listOf(
    GradientWallpaper("aurora",   "Aurora",    listOf(Color(0xFF0D0D2B), Color(0xFF1A3A4A), Color(0xFF0D2B1A))),
    GradientWallpaper("twilight", "Twilight",  listOf(Color(0xFF1A0533), Color(0xFF3D0A5E), Color(0xFF7B1EA2))),
    GradientWallpaper("ocean",    "Ocean",     listOf(Color(0xFF001A2E), Color(0xFF003A5C), Color(0xFF006994))),
    GradientWallpaper("ember",    "Ember",     listOf(Color(0xFF1A0800), Color(0xFF5C1900), Color(0xFFBF360C))),
    GradientWallpaper("forest",   "Forest",   listOf(Color(0xFF0A1A0A), Color(0xFF1B5E20), Color(0xFF2E7D32))),
    GradientWallpaper("midnight", "Midnight", listOf(Color(0xFF000000), Color(0xFF0A0A0A), Color(0xFF1A1A2E))),
    GradientWallpaper("rose",     "Rose Gold", listOf(Color(0xFF1A0A0F), Color(0xFF5C1A2E), Color(0xFFB06080))),
    GradientWallpaper("cyber",    "Cyber",    listOf(Color(0xFF000D1A), Color(0xFF001433), Color(0xFF002B66))),
    GradientWallpaper("sunset",   "Sunset",   listOf(Color(0xFF1A0000), Color(0xFF8B2500), Color(0xFFE65100))),
    GradientWallpaper("arctic",   "Arctic",   listOf(Color(0xFF0A1628), Color(0xFF163060), Color(0xFF1565C0))),
    GradientWallpaper("lava",     "Lava",     listOf(Color(0xFF1A0000), Color(0xFF7B1111), Color(0xFFD32F2F))),
    GradientWallpaper("cosmos",   "Cosmos",   listOf(Color(0xFF050014), Color(0xFF12003B), Color(0xFF1A0066))),
)

// ── Built-in solid-colour wallpapers ─────────────────────────────────────────

private data class SolidWallpaper(val id: String, val label: String, val color: Color)

private val solidWallpapers = listOf(
    SolidWallpaper("black",      "Black",       Color(0xFF000000)),
    SolidWallpaper("charcoal",   "Charcoal",    Color(0xFF1C1C1E)),
    SolidWallpaper("navy",       "Navy",        Color(0xFF001F3F)),
    SolidWallpaper("dark_green", "Dark Green",  Color(0xFF0A2E0A)),
    SolidWallpaper("dark_red",   "Dark Red",    Color(0xFF2E0A0A)),
    SolidWallpaper("dark_blue",  "Dark Blue",   Color(0xFF0A0A2E)),
    SolidWallpaper("slate",      "Slate",       Color(0xFF263238)),
    SolidWallpaper("espresso",   "Espresso",    Color(0xFF1B1007)),
    SolidWallpaper("deep_plum",  "Deep Plum",   Color(0xFF1A0030)),
    SolidWallpaper("graphite",   "Graphite",    Color(0xFF2D2D2D)),
    SolidWallpaper("dark_teal",  "Dark Teal",   Color(0xFF002B2B)),
    SolidWallpaper("wine",       "Wine",        Color(0xFF2B0014)),
)

// ── Screen saver style descriptors ───────────────────────────────────────────

private data class ScreensaverOption(val id: String, val label: String, val description: String)

private val screensaverOptions = listOf(
    ScreensaverOption("CLOCK",    "Clock",    "Floating time & date that drifts to prevent burn-in"),
    ScreensaverOption("GRADIENT", "Gradient", "Slowly cycling deep colour gradient"),
    ScreensaverOption("COLORS",   "Colors",   "Full hue-wheel colour sweep"),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperPickerScreen(
    navController: NavController,
    viewModel: WallpaperViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isApplying by viewModel.isApplying.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val wallpaperDim by viewModel.wallpaperDim.collectAsStateWithLifecycle()
    val wallpaperBlur by viewModel.wallpaperBlur.collectAsStateWithLifecycle()
    val screensaverStyle by viewModel.screensaverStyle.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.applyWallpaperFromUri(it) } }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(2_000)
            viewModel.clearMessage()
        }
    }

    FrostedGlassPanel(modifier = Modifier.fillMaxSize(), cornerRadius = 0.dp) {
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
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("Gradients") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("Colors") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                    text = { Text("Gallery") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 },
                    text = { Text("Adjust") })
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 },
                    text = { Text("Screen Saver") })
            }

            // ── Content ───────────────────────────────────────────────────────
            when (selectedTab) {
                0 -> GradientWallpapersTab(
                    onWallpaperSelected = { viewModel.applyBuiltInGradient(it.id, it.colors) }
                )
                1 -> SolidColorsTab(
                    onColorSelected = { viewModel.applyBuiltInGradient(it.id, listOf(it.color, it.color)) }
                )
                2 -> GalleryTab(
                    onPickFromGallery = { galleryLauncher.launch("image/*") },
                    onRemoveWallpaper = { viewModel.removeWallpaper() }
                )
                3 -> AdjustmentsTab(
                    wallpaperDim = wallpaperDim,
                    wallpaperBlur = wallpaperBlur,
                    onDimChange = { viewModel.setWallpaperDim(it) },
                    onBlurChange = { viewModel.setWallpaperBlur(it) }
                )
                4 -> ScreenSaverTab(
                    currentStyle = screensaverStyle,
                    onStyleSelected = { viewModel.setScreensaverStyle(it) },
                    onOpenSystemSettings = {
                        val intent = Intent(Settings.ACTION_DREAM_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching { context.startActivity(intent) }
                    }
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

// ── Tab composables ───────────────────────────────────────────────────────────

@Composable
private fun GradientWallpapersTab(onWallpaperSelected: (GradientWallpaper) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(gradientWallpapers, key = { it.id }) { w ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.6f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(w.colors))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { onWallpaperSelected(w) },
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(w.label, style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SolidColorsTab(onColorSelected: (SolidWallpaper) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(solidWallpapers, key = { it.id }) { w ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(w.color)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { onColorSelected(w) },
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(w.label, style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun GalleryTab(onPickFromGallery: () -> Unit, onRemoveWallpaper: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(32.dp)
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
        Button(onClick = onPickFromGallery, modifier = Modifier.fillMaxWidth()) {
            Text("Choose from Gallery")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onRemoveWallpaper,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
            AdjustCard(
                title = "Dim",
                valueText = "${(wallpaperDim * 100).toInt()}%",
                subtitle = "Darkens the wallpaper behind the launcher",
                value = wallpaperDim,
                onValueChange = onDimChange,
                valueRange = 0f..0.8f
            )
        }
        item {
            AdjustCard(
                title = "Blur",
                valueText = "${(wallpaperBlur * 100).toInt()}%",
                subtitle = "Blurs the gallery wallpaper (requires Android 12+)",
                value = wallpaperBlur,
                onValueChange = onBlurChange,
                valueRange = 0f..1f
            )
        }
    }
}

@Composable
private fun AdjustCard(
    title: String,
    valueText: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
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
                Text(title, style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                Text(valueText, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.fillMaxWidth()
            )
            Text(subtitle, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun ScreenSaverTab(
    currentStyle: String,
    onStyleSelected: (String) -> Unit,
    onOpenSystemSettings: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Screen Saver Style",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Activates when your device is charging and idle. Enable it in Android Settings → Display → Screen saver → select ABLauncher.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        items(screensaverOptions.size) { i ->
            val option = screensaverOptions[i]
            val selected = currentStyle == option.id
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStyleSelected(option.id) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected, onClick = { onStyleSelected(option.id) })
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = option.description,
                            fontSize = 12.sp,
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            OutlinedButton(onClick = onOpenSystemSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Open Android Screen Saver Settings")
            }
        }
    }
}
