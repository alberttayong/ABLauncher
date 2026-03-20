package com.ablauncher.ui.home.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ablauncher.data.model.HomeItem
import com.ablauncher.data.model.IconConfig
import com.ablauncher.data.model.IconShape
import com.ablauncher.data.model.IconSizeTier
import com.ablauncher.data.model.IconStyle

private val TINT_PRESETS = listOf(
    0xFFFFFFFF, 0xFFEF5350, 0xFFFF9800, 0xFFFFEB3B,
    0xFF4CAF50, 0xFF00D4FF, 0xFF2196F3, 0xFF9C27B0,
    0xFFE91E63, 0xFF607D8B
).map { it.toLong() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconCustomizerSheet(
    item: HomeItem.AppShortcut,
    onSave: (IconConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var config by remember { mutableStateOf(item.iconConfig) }

    ModalBottomSheet(
        onDismissRequest = { onSave(config); onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                HomeAppIcon(
                    item = item.copy(iconConfig = config),
                    onClick = {}
                )
            }

            Spacer(Modifier.height(4.dp))

            SectionTitle("Shape")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconShape.entries.forEach { shape ->
                    FilterChip(
                        selected = config.shape == shape,
                        onClick = { config = config.copy(shape = shape) },
                        label = { Text(shape.displayName, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SectionTitle("Style")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconStyle.entries.forEach { style ->
                    FilterChip(
                        selected = config.style == style,
                        onClick = { config = config.copy(style = style) },
                        label = { Text(style.displayName, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SectionTitle("Size")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconSizeTier.entries.forEach { tier ->
                    FilterChip(
                        selected = config.sizeTier == tier,
                        onClick = { config = config.copy(sizeTier = tier) },
                        label = { Text(tier.displayName, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (config.style == IconStyle.TINTED || config.style == IconStyle.OUTLINED) {
                SectionTitle("Color")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(TINT_PRESETS) { colorLong ->
                        val color = Color(colorLong)
                        val isSelected = config.tintColor == colorLong
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected)
                                        Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { config = config.copy(tintColor = colorLong) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Label", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Switch(
                    checked = config.showLabel,
                    onCheckedChange = { config = config.copy(showLabel = it) }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        modifier = Modifier.padding(top = 4.dp)
    )
}
