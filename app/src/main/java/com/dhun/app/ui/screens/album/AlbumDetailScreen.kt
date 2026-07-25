package com.dhun.app.ui.screens.album

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhun.app.DhunApp
import com.dhun.app.components.HairlineDivider
import com.dhun.app.components.SongRow
import com.dhun.app.components.TIcon
import com.dhun.app.components.TermButton
import com.dhun.app.data.Song
import com.dhun.app.data.formatDuration
import com.dhun.app.ui.theme.LocalDhunColors

@Composable
fun AlbumDetailScreen(
    albumId: Long?,
    onBack: () -> Unit,
    onGoToArtist: (String) -> Unit,
    onNowPlaying: () -> Unit,
) {
    val c = LocalDhunColors.current
    val app = DhunApp.instance
    val allAlbums by app.library.albums.collectAsState()
    val album = allAlbums.firstOrNull { it.id == albumId } ?: return
    val songs = remember(albumId) { app.library.songsForAlbum(albumId ?: -1L) }
    val currentId = app.player.currentSongId()
    val totalFmt = songs.map { it.format }.distinct().joinToString("/")

    Column(Modifier.fillMaxSize().background(c.bg)) {
        // top bar
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TIcon(Icons.Default.ArrowBack, onClick = onBack)
            androidx.compose.material.Text(
                "album",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = c.textMuted,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        HairlineDivider()

        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
                    // square art
                    Box(
                        Modifier
                            .size(130.dp)
                            .aspectRatio(1f)
                            .background(c.surface)
                            .border(1.dp, c.border),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material.Text("♫", fontFamily = FontFamily.Monospace, color = c.accent, fontSize = 42.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        androidx.compose.material.Text(
                            album.title,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.textPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        androidx.compose.material.Text(
                            album.artist,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = c.textSecondary,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onGoToArtist(album.artist) },
                        )
                        Spacer(Modifier.height(8.dp))
                        val metaParts = buildList {
                            if (album.year != null) add("${album.year}")
                            add("${album.trackCount} tracks")
                            add(formatDuration(album.durationMs))
                            add(totalFmt)
                        }
                        androidx.compose.material.Text(
                            metaParts.joinToString(" · "),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = c.textMuted,
                        )
                    }
                }
                // actions
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TermButton(label = "play all", accent = true, onClick = {
                        app.player.play(songs, 0); onNowPlaying()
                    })
                    TermButton(label = "shuffle", onClick = {
                        app.player.play(songs.shuffled(), 0); onNowPlaying()
                    })
                    TermButton(label = "add to queue", onClick = {
                        songs.forEach { app.player.enqueue(it.id) }
                    })
                }
                Spacer(Modifier.height(10.dp))
                HairlineDivider(dashed = true)
                Spacer(Modifier.height(6.dp))
            }
            itemsIndexed(songs) { i, s: Song ->
                SongRow(
                    index = "%02d".format(s.trackNumber.takeIf { it > 0 } ?: (i + 1)),
                    song = s,
                    isCurrent = s.id == currentId,
                    onClick = { app.player.play(songs, i); onNowPlaying() },
                )
            }
            item { Spacer(Modifier.height(130.dp)) }
        }
    }
}

@Composable
fun ArtistDetailScreen(
    artistName: String?,
    onBack: () -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onNowPlaying: () -> Unit,
) {
    val c = LocalDhunColors.current
    val app = DhunApp.instance
    val songs = remember(artistName) { app.library.songsForArtist(artistName ?: "") }
    val currentId = app.player.currentSongId()
    Column(Modifier.fillMaxSize().background(c.bg)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TIcon(Icons.Default.ArrowBack, onClick = onBack)
            androidx.compose.material.Text(
                "artist", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = c.textMuted,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        HairlineDivider()
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Box(
                Modifier.size(100.dp).background(c.surface).border(1.dp, c.border),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material.Text(
                    (artistName ?: "?").take(1).uppercase(),
                    fontFamily = FontFamily.Monospace, color = c.accent, fontSize = 36.sp, fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(10.dp))
            androidx.compose.material.Text(
                artistName ?: "unknown",
                fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            val albums = songs.map { it.album }.distinct().size
            androidx.compose.material.Text(
                "${songs.size} tracks · $albums albums",
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TermButton(label = "play all", accent = true, onClick = { app.player.play(songs, 0); onNowPlaying() })
                TermButton(label = "shuffle", onClick = { app.player.play(songs.shuffled(), 0); onNowPlaying() })
            }
        }
        HairlineDivider(dashed = true)
        Spacer(Modifier.height(6.dp))
        LazyColumnish(songs, currentId, onOpenAlbum, onNowPlaying)
    }
}

@Composable
private fun LazyColumnish(songs: List<Song>, currentId: Long?, onOpenAlbum: (Long) -> Unit, onNowPlaying: () -> Unit) {
    val app = DhunApp.instance
    androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize()) {
        itemsIndexed(songs) { i, s ->
            SongRow(
                index = "%03d".format(i + 1),
                song = s,
                isCurrent = s.id == currentId,
                onClick = { app.player.play(songs, i); onNowPlaying() },
            )
        }
        item { Spacer(Modifier.height(130.dp)) }
    }
}

@Composable
fun FolderDetailScreen(
    path: String?,
    onBack: () -> Unit,
    onNowPlaying: () -> Unit,
) {
    val c = LocalDhunColors.current
    val app = DhunApp.instance
    val songs = remember(path) { app.library.songsForFolder(path ?: "") }
    val currentId = app.player.currentSongId()
    Column(Modifier.fillMaxSize().background(c.bg)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TIcon(Icons.Default.ArrowBack, onClick = onBack)
            androidx.compose.material.Text(
                "folder", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = c.textMuted,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        HairlineDivider()
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            androidx.compose.material.Text(
                path ?: "/",
                fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = c.textSecondary,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.material.Text(
                "${songs.size} tracks",
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TermButton(label = "play all", accent = true, onClick = { app.player.play(songs, 0); onNowPlaying() })
                TermButton(label = "shuffle", onClick = { app.player.play(songs.shuffled(), 0); onNowPlaying() })
            }
        }
        HairlineDivider(dashed = true)
        androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize()) {
            itemsIndexed(songs) { i, s ->
                SongRow(
                    index = "%03d".format(i + 1),
                    song = s,
                    isCurrent = s.id == currentId,
                    onClick = { app.player.play(songs, i); onNowPlaying() },
                )
            }
            item { Spacer(Modifier.height(130.dp)) }
        }
    }
}
