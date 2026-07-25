package com.dhun.app.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.dhun.app.DhunApp
import com.dhun.app.components.HairlineDivider
import com.dhun.app.components.TIcon
import com.dhun.app.components.TermButton
import com.dhun.app.data.formatBitrateKbps
import com.dhun.app.data.formatDuration
import com.dhun.app.ui.theme.LocalDhunColors

@Composable
fun NowPlayingScreen(
    onCollapse: () -> Unit,
    onOpenQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
) {
    val c = LocalDhunColors.current
    val player = DhunApp.instance.player
    val state by player.state.collectAsState()
    val song = player.currentSong()

    Column(Modifier.fillMaxSize().background(c.bg).padding(horizontal = 18.dp)) {
        // top bar
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            TIcon(Icons.Default.KeyboardArrowDown, onClick = onCollapse, size = 22.dp)
            Spacer(Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(c.accent))
                Spacer(Modifier.width(6.dp))
                androidx.compose.material.Text(
                    if (state.isPlaying) "playing" else "paused",
                    fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.accent,
                )
            }
            Spacer(Modifier.weight(1f))
            TIcon(Icons.Default.Info, muted = true, onClick = { /* TODO: song info */ })
        }
        HairlineDivider(dashed = true)
        Spacer(Modifier.height(24.dp))

        // square album art
        if (song != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(c.surface)
                    .border(1.dp, c.border),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material.Text(
                    "♪",
                    fontFamily = FontFamily.Monospace, color = c.accent, fontSize = 64.sp,
                )
            }
            Spacer(Modifier.height(20.dp))

            androidx.compose.material.Text(
                song.title,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            androidx.compose.material.Text(
                song.artist,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = c.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(2.dp))
            androidx.compose.material.Text(
                "${song.album} · ${song.format.uppercase()}${if (song.bitrate > 0) " · ${formatBitrateKbps(song.bitrate)}" else ""}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = c.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(24.dp))

        // progress bar
        ProgressBar(
            position = state.positionMs,
            duration = state.durationMs,
            onSeek = { ms -> player.seekTo(ms) }
        )
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            androidx.compose.material.Text(
                formatDuration(state.positionMs),
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textSecondary,
            )
            androidx.compose.material.Text(
                "-${formatDuration((state.durationMs - state.positionMs).coerceAtLeast(0))}",
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted,
            )
        }

        Spacer(Modifier.height(22.dp))

        // transport controls — shuffle / prev / play / next / repeat
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            TIcon(
                Icons.Default.Shuffle,
                active = state.shuffle,
                size = 20.dp,
                onClick = { player.toggleShuffle() },
            )
            Spacer(Modifier.width(18.dp))
            TIcon(Icons.Default.SkipPrevious, size = 24.dp, onClick = { player.prev() })
            Spacer(Modifier.width(18.dp))
            // square outlined play/pause (accent)
            Box(
                Modifier
                    .size(64.dp)
                    .border(2.dp, c.accent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { player.togglePlayPause() },
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material.Icon(
                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null,
                    tint = c.accent,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.width(18.dp))
            TIcon(Icons.Default.SkipNext, size = 24.dp, onClick = { player.next() })
            Spacer(Modifier.width(18.dp))
            val repIcon = if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
            TIcon(
                repIcon,
                active = state.repeatMode != Player.REPEAT_MODE_OFF,
                size = 20.dp,
                onClick = { player.cycleRepeat() },
            )
        }

        Spacer(Modifier.height(18.dp))

        // secondary actions row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            var fav by remember { mutableStateOf(false) }
            TIcon(if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, active = fav, size = 18.dp, onClick = { fav = !fav })
            TIcon(Icons.Default.QueueMusic, size = 18.dp, onClick = onOpenQueue)
            TIcon(Icons.Default.Add, size = 18.dp, onClick = onAddToPlaylist)
        }

        Spacer(Modifier.weight(1f))
        // footer queue info
        HairlineDivider(dashed = true)
        Row(
            Modifier.fillMaxWidth().clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onOpenQueue,
            ).padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val cur = if (state.currentIndex >= 0) state.currentIndex + 1 else 0
            androidx.compose.material.Text(
                "queue ${cur.toString().padStart(2,'0')}/${state.queue.size}",
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted,
            )
            val nextId = state.queue.getOrNull(cur) // next in queue after current index
            val nextSong = DhunApp.instance.library.findSong(nextId)
            androidx.compose.material.Text(
                if (nextSong != null) "next → ${nextSong.title}" else "next → —",
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textSecondary,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ProgressBar(position: Long, duration: Long, onSeek: (Long) -> Unit) {
    val c = LocalDhunColors.current
    val fraction = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
    var dragging by remember { mutableStateOf<Float?>(null) }
    val f = dragging ?: fraction.coerceIn(0f, 1f)
    Box(
        Modifier
            .fillMaxWidth()
            .height(14.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val frac = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek((frac * duration).toLong())
                }
            }
            .padding(vertical = 6.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(2.dp).background(c.border).align(Alignment.CenterStart))
        Box(Modifier.fillMaxWidth(f).height(2.dp).background(c.accent).align(Alignment.CenterStart))
        Box(
            Modifier.fillMaxWidth(f).height(14.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(Modifier.size(8.dp).background(c.accent))
        }
    }
}
