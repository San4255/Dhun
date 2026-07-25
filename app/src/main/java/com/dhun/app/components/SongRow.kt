package com.dhun.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhun.app.DhunApp
import com.dhun.app.data.Song
import com.dhun.app.data.formatBitrateKbps
import com.dhun.app.ui.theme.LocalDhunColors

@Composable
fun SongRow(
    index: String,
    song: Song,
    compact: Boolean = false,
    showFormatTag: Boolean = true,
    isCurrent: Boolean = false,
    onClick: () -> Unit,
    onMore: (() -> Unit)? = null,
) {
    val c = LocalDhunColors.current
    val bg = if (isCurrent) c.surface else Color.Transparent
    val fgTitle = if (isCurrent) c.accent else c.textPrimary
    val fgIdx = if (isCurrent) c.accent else c.textMuted

    Row(
        Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = if (compact) 6.dp else 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(38.dp), contentAlignment = Alignment.CenterStart) {
            if (isCurrent) {
                // terminal-style playing marker
                androidx.compose.material.Text(
                    "▶",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = c.accent,
                )
            } else {
                androidx.compose.material.Text(
                    index,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = fgIdx,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            androidx.compose.material.Text(
                song.title,
                fontFamily = FontFamily.Monospace,
                fontSize = if (compact) 13.sp else 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = fgTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val parts = listOf(song.artist, song.displayDuration, formatBitrateKbps(song.bitrate).ifEmpty { song.format })
            androidx.compose.material.Text(
                parts.filter { it.isNotBlank() }.joinToString(" · "),
                fontFamily = FontFamily.Monospace,
                fontSize = if (compact) 11.sp else 12.sp,
                color = c.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showFormatTag && !isCurrent) {
            Box(
                Modifier.border(1.dp, c.textMuted).padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                androidx.compose.material.Text(
                    "[${song.format}]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = c.textMuted,
                )
            }
        }
        if (onMore != null) {
            TIcon(Icons.Default.MoreVert, muted = true, size = 16.dp, onClick = onMore)
        }
    }
    // hairline separator (only non-current rows for subtlety)
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(1.dp).background(if (isCurrent) c.accent.copy(alpha = 0.15f) else c.border))
}
