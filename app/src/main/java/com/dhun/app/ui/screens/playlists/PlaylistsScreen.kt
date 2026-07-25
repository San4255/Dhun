package com.dhun.app.ui.screens.playlists

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlaylistPlay
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
import com.dhun.app.components.HairlineDivider
import com.dhun.app.components.SectionHeader
import com.dhun.app.components.SongRow
import com.dhun.app.components.TIcon
import com.dhun.app.components.TermButton
import com.dhun.app.components.TermField
import com.dhun.app.data.Playlist
import com.dhun.app.data.formatDuration
import com.dhun.app.ui.theme.LocalDhunColors

@Composable
fun PlaylistsListScreen(
    onOpenPlaylist: (Long) -> Unit,
) {
    val c = LocalDhunColors.current
    val app = DhunApp.instance
    val playlists by app.library.playlists.collectAsState()
    val userLists = playlists.filterNot { it.isSmart }
    val smartLists = playlists.filter { it.isSmart }
    var showNew by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material.Text(
                "playlists",
                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = c.textPrimary,
                letterSpacing = 1.sp,
            )
            TIcon(Icons.Default.Add, onClick = { showNew = true }, size = 20.dp)
        }
        HairlineDivider()

        if (showNew) {
            NewPlaylistRow(onCreate = { name ->
                app.library.createPlaylist(name)
                showNew = false
            }, onCancel = { showNew = false })
        }

        LazyColumn(Modifier.fillMaxSize().padding(6.dp)) {
            item {
                SectionHeader("your playlists") {}
            }
            if (userLists.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.material.Text(
                            "[ no playlists ]", fontFamily = FontFamily.Monospace, color = c.textSecondary, fontSize = 12.sp,
                        )
                    }
                }
            } else {
                items(userLists) { pl ->
                    PlaylistTile(pl, onClick = { onOpenPlaylist(pl.id) })
                }
            }
            item {
                SectionHeader("smart playlists") {}
            }
            items(smartLists) { pl ->
                SmartPlaylistRow(pl)
            }
            item { Spacer(Modifier.height(110.dp)) }
        }
    }
}

@Composable
private fun NewPlaylistRow(onCreate: (String) -> Unit, onCancel: () -> Unit) {
    val c = LocalDhunColors.current
    var name by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TermField(value = name, onValueChange = { name = it }, placeholder = "playlist name", modifier = Modifier.weight(1f))
        TermButton(label = "create", accent = true, small = true, onClick = { if (name.isNotBlank()) onCreate(name.trim()) })
        TermButton(label = "cancel", small = true, onClick = onCancel)
    }
}

@Composable
private fun PlaylistTile(pl: Playlist, onClick: () -> Unit) {
    val c = LocalDhunColors.current
    val songs = pl.songIds.mapNotNull { DhunApp.instance.library.findSong(it) }
    val total = songs.sumOf { it.durationMs }
    Row(
        Modifier.fillMaxWidth()
            .padding(6.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(60.dp).background(c.surface).border(1.dp, c.border), contentAlignment = Alignment.Center) {
            androidx.compose.material.Icon(Icons.Default.PlaylistPlay, null, tint = c.accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            androidx.compose.material.Text(
                pl.name, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textPrimary, fontWeight = FontWeight.Medium,
            )
            androidx.compose.material.Text(
                "${songs.size} tracks · ${formatDuration(total)}",
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textSecondary,
            )
        }
    }
}

@Composable
private fun SmartPlaylistRow(pl: Playlist) {
    val c = LocalDhunColors.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).background(c.surface).border(1.dp, c.border), contentAlignment = Alignment.Center) {
            androidx.compose.material.Icon(Icons.Default.AutoAwesome, null, tint = c.textSecondary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            androidx.compose.material.Text(pl.name, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textPrimary)
            androidx.compose.material.Text(
                "auto-generated", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted,
            )
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
}

@Composable
fun PlaylistDetailScreen(playlistId: Long?, onBack: () -> Unit, onNowPlaying: () -> Unit) {
    val c = LocalDhunColors.current
    val app = DhunApp.instance
    val playlists by app.library.playlists.collectAsState()
    val pl = playlists.firstOrNull { it.id == playlistId } ?: run {
        Box(Modifier.fillMaxSize().background(c.bg)) {
            TIcon(Icons.Default.ArrowBack, onClick = onBack)
        }
        return
    }
    val songs = pl.songIds.mapNotNull { app.library.findSong(it) }
    val currentId = app.player.currentSongId()
    val total = songs.sumOf { it.durationMs }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TIcon(Icons.Default.ArrowBack, onClick = onBack)
            androidx.compose.material.Text(
                "playlist", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = c.textMuted,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        HairlineDivider()
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            androidx.compose.material.Text(
                pl.name, fontFamily = FontFamily.Monospace, fontSize = 20.sp, color = c.textPrimary, fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            androidx.compose.material.Text(
                "${songs.size} tracks · ${formatDuration(total)}",
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TermButton(label = "play all", accent = true, onClick = {
                    if (songs.isNotEmpty()) { app.player.play(songs, 0); onNowPlaying() }
                })
                TermButton(label = "shuffle", onClick = {
                    if (songs.isNotEmpty()) { app.player.play(songs.shuffled(), 0); onNowPlaying() }
                })
                if (!pl.isSmart) TermButton(label = "delete", danger = true, onClick = { app.library.deletePlaylist(pl.id); onBack() })
            }
        }
        HairlineDivider(dashed = true)
        LazyColumn(Modifier.fillMaxSize()) {
            itemsIndexed(songs) { i, s ->
                SongRow(
                    index = "%02d".format(i + 1),
                    song = s,
                    isCurrent = s.id == currentId,
                    onClick = { app.player.play(songs, i); onNowPlaying() },
                    onMore = { app.library.addToPlaylist(pl.id, s.id) },
                )
            }
            item { Spacer(Modifier.height(130.dp)) }
        }
    }
}
