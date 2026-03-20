package com.ablauncher.ui.home.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ablauncher.data.model.HomeItem
import com.ablauncher.data.model.IconShape
import com.ablauncher.data.model.IconStyle

/** Squircle shape — cubic-bezier approximation of a superellipse */
val SquircleShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val kw = w * 0.38f
    val kh = h * 0.38f
    val hw = w / 2f
    val hh = h / 2f
    moveTo(hw, 0f)
    cubicTo(hw + kw, 0f, w, hh - kh, w, hh)
    cubicTo(w, hh + kh, hw + kw, h, hw, h)
    cubicTo(hw - kw, h, 0f, hh + kh, 0f, hh)
    cubicTo(0f, hh - kh, hw - kw, 0f, hw, 0f)
    close()
}

fun IconShape.toShape(): Shape = when (this) {
    IconShape.CIRCLE -> CircleShape
    IconShape.ROUNDED_SQUARE -> RoundedCornerShape(20.dp)
    IconShape.SQUARE -> RectangleShape
    IconShape.SQUIRCLE -> SquircleShape
}

@Composable
fun HomeAppIcon(
    item: HomeItem.AppShortcut,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val icon = remember(item.packageName) {
        runCatching { context.packageManager.getApplicationIcon(item.packageName) }.getOrNull()
    }
    val cfg = item.iconConfig
    val iconSize = cfg.sizeTier.sizeDp.dp
    val shape = cfg.shape.toShape()
    val tint = cfg.tintColor?.let { Color(it) } ?: MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(iconSize)
                .clip(shape)
                .then(
                    if (cfg.style == IconStyle.TINTED)
                        Modifier.background(tint.copy(alpha = 0.85f))
                    else Modifier
                )
                .then(
                    if (cfg.style == IconStyle.OUTLINED)
                        Modifier.border(2.dp, tint, shape)
                    else Modifier
                )
        ) {
            AsyncImage(
                model = icon,
                contentDescription = item.appLabel,
                modifier = Modifier
                    .size(if (cfg.style == IconStyle.TINTED) iconSize * 0.72f else iconSize)
                    .clip(shape),
                colorFilter = when (cfg.style) {
                    IconStyle.MONO -> ColorFilter.colorMatrix(
                        ColorMatrix().apply { setToSaturation(0f) }
                    )
                    else -> null
                }
            )
        }

        if (cfg.showLabel && item.appLabel.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.appLabel,
                fontSize = 11.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.9f),
                        blurRadius = 6f
                    )
                ),
                modifier = Modifier.widthIn(max = iconSize + 16.dp)
            )
        }
    }
}
