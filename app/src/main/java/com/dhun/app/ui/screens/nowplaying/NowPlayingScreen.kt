package com.dhun.app.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.dhun.app.DhunApp
import com.dhun.app.components.HairlineDivider
import com.dhun.app.components.SquareArt
import com.dhun.app.components.TIcon
import com.dhun.app.components.TermButton
import com.dhun.app.data.LyricsLine
import com.dhun.app.data.LyricsLoader
import com.dhun.app.data.formatBitrateKbps
import com.dhun.app.data.formatDuration
import com.dhun.app.ui.theme.LocalDhunColors

private const val BAR_COUNT = 56 // number of bars visible in the waveform strip

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
    var fav by remember { mutableStateOf(false) }
    var lyrics by remember { mutableStateOf<List<LyricsLine>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(song?.id) {
        lyrics = LyricsLoader.load(song)
    }

    // auto-scroll lyrics to current line
    LaunchedEffect(state.positionMs, lyrics) {
        if (lyrics.isEmpty()) return@LaunchedEffect
        val idx = lyrics.indexOfLast { (it.timestampMs ?: 0L) <= state.positionMs }
        if (idx >= 0) listState.animateScrollToItem(idx.coerceAtLeast(0))
    }

    val fraction = if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs.toFloat() else 0f
    val playedBars = (fraction * BAR_COUNT).toInt().coerceIn(0, BAR_COUNT)

    Column(Modifier.fillMaxSize().background(c.bg).padding(horizontal = 18.dp)) {
        // top bar
        Row(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TIcon(Icons.Default.KeyboardArrowDown, onClick = onCollapse, size = 24.dp)
            Spacer(Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(Modifier.size(7.dp).background(c.accent))
                Spacer(Modifier.width(7.dp))
                androidx.compose.material.Text(
                    if (state.isPlaying) "playing" else "paused",
                    fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = c.accent,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.width(10.dp))
                androidx.compose.material.Text(
                    "· offline",
                    fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = c.textMuted,
                )
            }
            TIcon(Icons.Default.Info, muted = true, onClick = { /* TODO: song info */ })
        }
        HairlineDivider()

        if (song != null) {
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.Top) {
                SquareArt(
                    artUri = song.albumArtUri,
                    sizeDp = 138.dp,
                    fallbackGlyph = "♫",
                    fallbackGlyphSizeSp = 44,
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    androidx.compose.material.Text(
                        song.title,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary,
                        lineHeight = 20.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    androidx.compose.material.Text(
                        song.artist,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = c.textSecondary,
                    )
                    Spacer(Modifier.height(3.dp))
                    androidx.compose.material.Text(
                        song.album,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = c.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material.Text(
                        "${song.format.uppercase()}${if (song.bitrate > 0) " · ${formatBitrateKbps(song.bitrate)}" else ""}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = c.textMuted,
                    )
                }
            }
        } else {
            // no song loaded
            Box(
                Modifier.fillMaxWidth().aspectRatio(1f).border(1.dp, c.border).background(c.surface),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material.Text("— no track —", fontFamily = FontFamily.Monospace, color = c.textMuted, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // waveform / progress bar (bar-style, tappable)
        WaveformBar(
            playedBars = playedBars,
            total = BAR_COUNT,
            enabled = song != null,
            onSeekFraction = { f ->
                if (song != null) player.seekTo((f * state.durationMs).toLong())
            },
        )
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            androidx.compose.material.Text(
                formatDuration(state.positionMs),
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textSecondary,
            )
            androidx.compose.material.Text(
                formatDuration(state.durationMs),
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted,
            )
        }

        Spacer(Modifier.height(18.dp))

        // transport controls
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            // prev
            Box(
                Modifier.size(56.dp).border(1.dp, c.border).clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null,
                ) { player.prev() },
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material.Icon(Icons.Default.SkipPrevious, null, tint = c.textSecondary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            // play-pause (accent outline)
            Box(
                Modifier.size(68.dp).border(2.dp, c.accent).clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null,
                ) { player.togglePlayPause() },
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material.Icon(
                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null, tint = c.accent, modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Box(
                Modifier.size(56.dp).border(1.dp, c.border).clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null,
                ) { player.next() },
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material.Icon(Icons.Default.SkipNext, null, tint = c.textSecondary, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        // secondary action row — labeled square buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(
                icon = Icons.Default.Shuffle, label = "shuffle",
                active = state.shuffle,
                onClick = { player.toggleShuffle() },
                modifier = Modifier.weight(1f),
            )
            ActionButton(
                icon = if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                label = "repeat",
                active = state.repeatMode != Player.REPEAT_MODE_OFF,
                onClick = { player.cycleRepeat() },
                modifier = Modifier.weight(1f),
            )
            ActionButton(
                icon = Icons.Default.QueueMusic, label = "queue",
                active = false,
                onClick = onOpenQueue,
                modifier = Modifier.weight(1f),
            )
            ActionButton(
                icon = if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = if (fav) "saved" else "save",
                active = fav,
                onClick = { fav = !fav },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(12.dp))
        HairlineDivider(dashed = true)

        // lyrics / queue footer panel
        if (song != null) {
            if (lyrics.isEmpty()) {
                Spacer(Modifier.height(10.dp))
                androidx.compose.material.Text(
                    "[ no lyrics found ]",
                    fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted,
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(lyrics) { line ->
                        val isActive = line.timestampMs != null &&
                                line.timestampMs <= state.positionMs &&
                                (lyrics.getOrNull(lyrics.indexOf(line) + 1)?.timestampMs
                                    ?: Long.MAX_VALUE) > state.positionMs
                        androidx.compose.material.Text(
                            line.text.ifBlank { "·" },
                            fontFamily = FontFamily.Monospace,
                            fontSize = if (isActive) 14.sp else 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) c.accent else c.textSecondary,
                            modifier = Modifier.fillMaxWidth().clickable(
                                interactionSource = remember { MutableInteractionSource() }, indication = null,
                            ) {
                                line.timestampMs?.let { player.seekTo(it) }
                            },
                        )
                    }
                }
            }
        } else { Spacer(Modifier.weight(1f)) }

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
            val nextId = state.queue.getOrNull(cur)
            val nextSong = DhunApp.instance.library.findSong(nextId)
            androidx.compose.material.Text(
                if (nextSong != null) "next → ${nextSong.title}" else "next → —",
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textSecondary,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = 10.dp),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalDhunColors.current
    val borderC = if (active) c.accent else c.border
    val fg = if (active) c.accent else c.textSecondary
    Row(
        modifier
            .border(1.dp, borderC)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        androidx.compose.material.Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        androidx.compose.material.Text(label, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = fg)
    }
}

@Composable
private fun WaveformBar(playedBars: Int, total: Int, enabled: Boolean, onSeekFraction: (Float) -> Unit) {
    val c = LocalDhunColors.current
    val seed = remember { List(total) { i ->
        // pseudo-random but deterministic per index heights (0.3 .. 1.0)
        val x = (i * 9301L + 49297L) % 233280
        0.3f + ((x.toFloat() / 233280f) * 0.7f)
    } }

    Box(
        Modifier
            .fillMaxWidth()
            .height(34.dp)
            .pointerInput(enabled) {
                detectTapGestures { offset ->
                    val f = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeekFraction(f)
                }
            }
            .pointerInput(enabled) {
                detectHorizontalDragGestures { change, _ ->
                    val f = (change.position.x / size.width).coerceIn(0f, 1f)
                    onSeekFraction(f)
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            seed.forEachIndexed { i, h ->
                val played = i < playedBars
                val color = if (!enabled) c.textMuted else if (played) c.accent else c.textMuted.copy(alpha = 0.55f)
                val barH: Dp = 2.dp + Dp(h * 22f)
                Box(
                    Modifier.weight(1f).height(barH).background(color)
                )
            }
        }
    }
}
