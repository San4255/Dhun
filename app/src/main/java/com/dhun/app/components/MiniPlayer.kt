package com.dhun.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhun.app.DhunApp
import com.dhun.app.ui.theme.LocalDhunColors

@Composable
fun MiniPlayer(
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onSwipePrev: () -> Unit,
    onSwipeNext: () -> Unit,
) {
    val c = LocalDhunColors.current
    val player = DhunApp.instance.player
    val state by player.state.collectAsState()
    val song = player.currentSong()

    AnimatedVisibility(visible = song != null) {
        if (song == null) return@AnimatedVisibility
        val fraction = if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs.toFloat() else 0f
        Column(
            Modifier
                .fillMaxWidth()
                .background(c.surface)
                .border(1.dp, c.border)
        ) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            Row(
                Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            if (dragAmount > 40) { onSwipePrev(); change.consume() }
                            else if (dragAmount < -40) { onSwipeNext(); change.consume() }
                        }
                    }
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onExpand() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SquareArt(artUri = song.albumArtUri, sizeDp = 40.dp, fallbackGlyph = "♪", fallbackGlyphSizeSp = 16)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    androidx.compose.material.Text(
                        song.title,
                        color = c.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    androidx.compose.material.Text(
                        song.artist,
                        color = c.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(c.border)) {
                        Box(Modifier.fillMaxWidth(fraction).height(1.dp).background(c.accent))
                    }
                }
                Spacer(Modifier.width(8.dp))
                TIcon(
                    icon = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    active = state.isPlaying,
                    size = 20.dp,
                    onClick = { player.togglePlayPause() },
                )
                TIcon(
                    icon = Icons.Default.SkipNext,
                    size = 18.dp,
                    onClick = { player.next() },
                )
            }
        }
    }
}
