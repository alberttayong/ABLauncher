package com.ablauncher.ui.home.canvas

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ablauncher.data.model.HomeItem
import com.ablauncher.data.model.IconSizeTier
import com.ablauncher.data.model.WidgetType
import com.ablauncher.ui.widgets.CalendarWidget
import com.ablauncher.ui.widgets.ClockViewModel
import com.ablauncher.ui.widgets.ClockWidget
import com.ablauncher.ui.widgets.NewsWidget
import com.ablauncher.ui.widgets.SearchBarWidget
import com.ablauncher.ui.widgets.WeatherWidget
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

// ── Size helpers ──────────────────────────────────────────────────────────────

private fun HomeItem.widthPx(canvasW: Float, density: Density): Float = when (this) {
    is HomeItem.Widget -> widthFrac * canvasW
    is HomeItem.AppShortcut -> with(density) { (iconConfig.sizeTier.sizeDp.dp + 16.dp).toPx() }
}

private fun HomeItem.heightPx(canvasH: Float, density: Density): Float = when (this) {
    is HomeItem.Widget -> heightFrac * canvasH
    is HomeItem.AppShortcut -> with(density) {
        (iconConfig.sizeTier.sizeDp.dp + (if (iconConfig.showLabel) 22.dp else 0.dp) + 12.dp).toPx()
    }
}

// ── Main canvas ───────────────────────────────────────────────────────────────

@Composable
fun HomeCanvas(
    items: List<HomeItem>,
    selectedItemId: String?,
    onSelect: (String?) -> Unit,
    onMoved: (String, Float, Float) -> Unit,
    onResized: (String, Float, Float) -> Unit,
    onRemove: (String) -> Unit,
    onCustomize: (HomeItem.AppShortcut) -> Unit,
    onUninstall: (String) -> Unit,
    modifier: Modifier = Modifier,
    clockViewModel: ClockViewModel = hiltViewModel()
) {
    val clockFace by clockViewModel.clockFace.collectAsState()
    val clockFormat by clockViewModel.clockFormat.collectAsState()
    val density = LocalDensity.current
    val context = LocalContext.current

    var draggingAppPackage by remember { mutableStateOf<String?>(null) }
    var isTrashHot by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            // Tap on blank canvas deselects
            .pointerInput(Unit) {
                androidx.compose.foundation.gestures.detectTapGestures { onSelect(null) }
            }
    ) {
        val canvasW = constraints.maxWidth.toFloat()
        val canvasH = constraints.maxHeight.toFloat()

        items.forEach { item ->
            key(item.id) {
                HomeCanvasItem(
                    item = item,
                    canvasW = canvasW,
                    canvasH = canvasH,
                    density = density,
                    isSelected = item.id == selectedItemId,
                    onSelect = { onSelect(item.id) },
                    onMoved = { xFrac, yFrac -> onMoved(item.id, xFrac, yFrac) },
                    onResized = { wFrac, hFrac -> onResized(item.id, wFrac, hFrac) },
                    onDragStarted = {
                        onSelect(item.id)
                        if (item is HomeItem.AppShortcut) {
                            draggingAppPackage = item.packageName
                        }
                    },
                    onDragUpdate = { xPx, yPx, wPx, hPx ->
                        if (item is HomeItem.AppShortcut) {
                            val cx = xPx + wPx / 2
                            val cy = yPx + hPx / 2
                            val trashCy = canvasH - with(density) { 96.dp.toPx() }
                            val dist = sqrt(
                                (cx - canvasW / 2).pow(2) + (cy - trashCy).pow(2)
                            )
                            isTrashHot = dist < with(density) { 90.dp.toPx() }
                        }
                    },
                    onDragEnded = { xPx, yPx, wPx, hPx ->
                        val wasDraggingApp = draggingAppPackage
                        draggingAppPackage = null
                        isTrashHot = false

                        if (wasDraggingApp != null && item is HomeItem.AppShortcut) {
                            val cx = xPx + wPx / 2
                            val cy = yPx + hPx / 2
                            val trashCy = canvasH - with(density) { 96.dp.toPx() }
                            val dist = sqrt(
                                (cx - canvasW / 2).pow(2) + (cy - trashCy).pow(2)
                            )
                            if (dist < with(density) { 90.dp.toPx() }) {
                                onUninstall(item.packageName)
                                return@HomeCanvasItem
                            }
                        }
                    }
                ) {
                    when (item) {
                        is HomeItem.Widget -> WidgetItemContent(item, clockFace, clockFormat)
                        is HomeItem.AppShortcut -> HomeAppIcon(
                            item = item,
                            onClick = {
                                val intent = context.packageManager
                                    .getLaunchIntentForPackage(item.packageName)
                                    ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                intent?.let { context.startActivity(it) }
                            }
                        )
                    }
                }

                // Item action bar (shown when selected)
                if (item.id == selectedItemId) {
                    val xPx = item.xFrac * canvasW
                    val yPx = item.yFrac * canvasH
                    val barYPx = if (yPx > canvasH * 0.5f) {
                        yPx - with(density) { 48.dp.toPx() }
                    } else {
                        yPx + item.heightPx(canvasH, density) + with(density) { 4.dp.toPx() }
                    }
                    Row(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    xPx.roundToInt(),
                                    barYPx.coerceIn(0f, canvasH - 100f).roundToInt()
                                )
                            }
                    ) {
                        if (item is HomeItem.AppShortcut) {
                            FilledTonalIconButton(
                                onClick = { onCustomize(item) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Edit, "Customize", modifier = Modifier.size(18.dp))
                            }
                        }
                        FilledTonalIconButton(
                            onClick = { onRemove(item.id); onSelect(null) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Trash zone — animated, appears when dragging an app
        AnimatedVisibility(
            visible = draggingAppPackage != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = with(density) { 96.dp })
        ) {
            TrashZone(isHot = isTrashHot)
        }
    }
}

// ── Single canvas item (draggable + resizable) ────────────────────────────────

@Composable
private fun HomeCanvasItem(
    item: HomeItem,
    canvasW: Float,
    canvasH: Float,
    density: Density,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onMoved: (xFrac: Float, yFrac: Float) -> Unit,
    onResized: (widthFrac: Float, heightFrac: Float) -> Unit,
    onDragStarted: () -> Unit,
    onDragUpdate: (xPx: Float, yPx: Float, wPx: Float, hPx: Float) -> Unit,
    onDragEnded: (xPx: Float, yPx: Float, wPx: Float, hPx: Float) -> Unit,
    content: @Composable () -> Unit
) {
    var xPx by remember(item.id) { mutableFloatStateOf(item.xFrac * canvasW) }
    var yPx by remember(item.id) { mutableFloatStateOf(item.yFrac * canvasH) }
    var wPx by remember(item.id) { mutableFloatStateOf(item.widthPx(canvasW, density)) }
    var hPx by remember(item.id) { mutableFloatStateOf(item.heightPx(canvasH, density)) }
    var isDragging by remember { mutableStateOf(false) }

    val minW = with(density) { 80.dp.toPx() }
    val minH = with(density) { 60.dp.toPx() }

    Box(
        modifier = Modifier
            .offset { IntOffset(xPx.roundToInt(), yPx.roundToInt()) }
            .size(
                width = with(density) { wPx.toDp() },
                height = with(density) { hPx.toDp() }
            )
            .then(
                if (isSelected || isDragging)
                    Modifier
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                else Modifier
            )
            .pointerInput(item.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isDragging = true
                        onSelect()
                        onDragStarted()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        xPx = (xPx + dragAmount.x).coerceIn(0f, canvasW - wPx)
                        yPx = (yPx + dragAmount.y).coerceIn(0f, canvasH - hPx)
                        onDragUpdate(xPx, yPx, wPx, hPx)
                    },
                    onDragEnd = {
                        isDragging = false
                        onDragEnded(xPx, yPx, wPx, hPx)
                        onMoved(xPx / canvasW, yPx / canvasH)
                    },
                    onDragCancel = {
                        isDragging = false
                        onDragEnded(xPx, yPx, wPx, hPx)
                    }
                )
            }
    ) {
        content()

        // Resize handle — shown only for widget items when selected
        if (item is HomeItem.Widget && isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .then(
                        Modifier.pointerInput(item.id + "_resize") {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                wPx = (wPx + dragAmount.x).coerceIn(minW, canvasW - xPx)
                                hPx = (hPx + dragAmount.y).coerceIn(minH, canvasH - yPx)
                                onResized(wPx / canvasW, hPx / canvasH)
                            }
                        }
                    )
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = androidx.compose.ui.graphics.Color(0xFF4FC3F7))
                    // Draw resize arrows
                    val stroke = 2.dp.toPx()
                    val m = size.minDimension * 0.25f
                    drawLine(
                        androidx.compose.ui.graphics.Color.White,
                        androidx.compose.ui.geometry.Offset(m, size.height - m),
                        androidx.compose.ui.geometry.Offset(size.width - m, m),
                        strokeWidth = stroke
                    )
                }
            }
        }
    }
}

// ── Widget content router ─────────────────────────────────────────────────────

@Composable
private fun WidgetItemContent(
    item: HomeItem.Widget,
    clockFace: com.ablauncher.data.model.ClockFace,
    clockFormat: com.ablauncher.data.model.ClockFormat
) {
    when (item.widgetType) {
        WidgetType.CLOCK -> ClockWidget(
            face = clockFace,
            format = clockFormat,
            modifier = Modifier.fillMaxSize()
        )
        WidgetType.WEATHER -> WeatherWidget(modifier = Modifier.fillMaxSize())
        WidgetType.CALENDAR -> CalendarWidget(modifier = Modifier.fillMaxSize())
        WidgetType.NEWS -> NewsWidget(modifier = Modifier.fillMaxSize())
        WidgetType.SEARCH -> SearchBarWidget(modifier = Modifier.fillMaxSize())
    }
}
