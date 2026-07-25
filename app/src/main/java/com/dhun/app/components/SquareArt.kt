package com.dhun.app.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dhun.app.ui.theme.LocalDhunColors

@Composable
fun SquareArt(
    artUri: Uri?,
    sizeDp: androidx.compose.ui.unit.Dp,
    fallbackGlyph: String = "♫",
    fallbackGlyphSizeSp: Int = 28,
    borderColor: Color? = null,
) {
    val c = LocalDhunColors.current
    val bc = borderColor ?: c.border
    var failed by remember { mutableStateOf(false) }
    Box(
        Modifier.size(sizeDp).background(c.surface).border(1.dp, bc),
        contentAlignment = Alignment.Center,
    ) {
        if (artUri != null && !failed) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artUri)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { failed = true },
            )
        }
        val showGlyph = artUri == null || failed
        androidx.compose.material.Text(
            fallbackGlyph,
            fontFamily = FontFamily.Monospace,
            color = if (showGlyph) c.accent else Color.Transparent,
            fontSize = fallbackGlyphSizeSp.sp,
        )
    }
}
