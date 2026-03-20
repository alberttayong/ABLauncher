package com.ablauncher.ui.home.canvas

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrashZone(
    isHot: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isHot) 1.25f else 1f, label = "trashScale")
    val bgColor by animateColorAsState(
        if (isHot) Color(0xCCEF5350) else Color(0x80000000),
        label = "trashBg"
    )
    val iconColor by animateColorAsState(
        if (isHot) Color.White else Color.White.copy(alpha = 0.7f),
        label = "trashIcon"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = "Uninstall",
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isHot) "Release to Uninstall" else "Drag here to Uninstall",
                fontSize = 11.sp,
                color = iconColor
            )
        }
    }
}
