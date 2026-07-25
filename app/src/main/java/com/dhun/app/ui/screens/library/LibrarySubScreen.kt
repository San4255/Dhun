package com.dhun.app.ui.screens.library

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import com.dhun.app.components.SquareArt
import com.dhun.app.components.TIcon
import com.dhun.app.components.TermButton
import com.dhun.app.data.Album
import com.dhun.app.data.Artist
import com.dhun.app.data.Folder
import com.dhun.app.data.formatDuration
import com.dhun.app.player.DhunPlayer
import com.dhun.app.ui.theme.LocalDhunColors

@Composable
fun SongsList(
    onGoToNowPlaying: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    val c = LocalDhunColors.current
    val app = DhunApp.instance
    val songs by app.library.songs.collectAsState()
    val playState by app.player.state.collectAsState()
    val currentId = app.player.currentSongId()
    val scope = rememberCoroutineScope()

    var sortMode by remember { mutableStateOf(SortMode.TITLE) }
    var sortAsc by remember { mutableStateOf(true) }
    var sortOpen by remember { mutableStateOf(false) }

    val sorted = remember(songs, sortMode, sortAsc) {
        val list = when (sortMode) {
            SortMode.TITLE -> songs.sortedBy { it.title.lowercase() }
            SortMode.ARTIST -> songs.sortedBy { it.artist.lowercase() }
            SortMode.ALBUM -> songs.sortedBy { it.album.lowercase() }
            SortMode.DATE_ADDED -> songs.sortedBy { it.dateAdded }
            SortMode.DURATION -> songs.sortedBy { it.durationMs }
        }
        if (sortAsc) list else list.reversed()
    }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        // search bar
        Box(
            Modifier
                .fillMaxWidth()
                .border(1.dp, c.border)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenSearch,
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material.Icon(Icons.Default.MusicNote, null, tint = c.textMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                androidx.compose.material.Text(
                    "search ${songs.size} tracks",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = c.textMuted,
                )
            }
        }

        // sort control row
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material.Text(
                "${sorted.size} tracks",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = c.textMuted,
            )
            Row(
                Modifier
                    .border(1.dp, c.border)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { sortOpen = !sortOpen }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material.Text(
                    "${sortMode.label} ${if (sortAsc) "↑" else "↓"}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = c.textSecondary,
                )
                TIcon(Icons.Default.ArrowDropDown, muted = true, size = 14.dp)
            }
        }

        if (sortOpen) {
            SortSheet(
                current = sortMode, asc = sortAsc,
                onSelect = { m, a -> sortMode = m; sortAsc = a; sortOpen = false }
            )
        }

        if (songs.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material.Text(
                        "[ no tracks found ]",
                        fontFamily = FontFamily.Monospace,
                        color = c.textSecondary,
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    TermButton(label = "rescan storage", onClick = {
                        scope.launch { app.library.scan() }
                    })
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(sorted, key = { _, s -> s.id }) { i, s ->
                    val idx = "%03d".format(i + 1)
                    SongRow(
                        index = idx,
                        song = s,
                        isCurrent = s.id == currentId,
                        onClick = {
                            app.player.play(sorted, i)
                            onGoToNowPlaying()
                        },
                        onMore = {
                            // TODO: per-track action sheet
                        },
                    )
                }
                item { Spacer(Modifier.height(110.dp)) }
            }
        }
    }
}

enum class SortMode(val label: String) { TITLE("title"), ARTIST("artist"), ALBUM("album"), DATE_ADDED("added"), DURATION("duration") }

@Composable
fun SortSheet(current: SortMode, asc: Boolean, onSelect: (SortMode, Boolean) -> Unit) {
    val c = LocalDhunColors.current
    Column(Modifier.fillMaxWidth().background(c.surface).border(1.dp, c.border).padding(8.dp)) {
        SortMode.values().forEach { m ->
            val active = m == current
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onSelect(m, if (m == current) !asc else asc)
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material.Text(
                    m.label, fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                    color = if (active) c.accent else c.textPrimary,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                )
                if (active) androidx.compose.material.Text(if (asc) "↑" else "↓", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = c.accent)
            }
        }
    }
}

@Composable
fun AlbumsGrid(onClickAlbum: (Long) -> Unit) {
    val c = LocalDhunColors.current
    val albums by DhunApp.instance.library.albums.collectAsState()
    LazyVerticalGrid(columns = GridCells.Fixed(2), Modifier.fillMaxSize().padding(6.dp)) {
        items(albums) { a -> AlbumTile(a, onClick = { onClickAlbum(a.id) }) }
    }
}

@Composable
fun AlbumTile(a: Album, onClick: () -> Unit, compact: Boolean = false) {
    val c = LocalDhunColors.current
    Column(
        Modifier.padding(6.dp).clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick
        )
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val ctx = androidx.compose.ui.platform.LocalContext.current
            var imgFailed by remember { mutableStateOf(false) }
            coil.compose.AsyncImage(
                model = coil.request.ImageRequest.Builder(ctx).data(a.artUri).crossfade(false).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().border(1.dp, c.border).background(c.surface),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                onError = { imgFailed = true },
                onSuccess = { imgFailed = false },
            )
            if (a.artUri == null || imgFailed) {
                androidx.compose.material.Text(
                    "♫", fontFamily = FontFamily.Monospace, color = c.accent,
                    fontSize = if (compact) 20.sp else 32.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        androidx.compose.material.Text(
            a.title, fontFamily = FontFamily.Monospace, fontSize = 12.sp,
            color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium,
        )
        androidx.compose.material.Text(
            "${a.artist} · ${a.trackCount} tr",
            fontFamily = FontFamily.Monospace, fontSize = 11.sp,
            color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun ArtistsList(onClickArtist: (String) -> Unit) {
    val c = LocalDhunColors.current
    val artists by DhunApp.instance.library.artists.collectAsState()
    LazyColumn {
        items(artists) { a ->
            Row(
                Modifier.fillMaxWidth()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClickArtist(a.name) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(44.dp).background(c.surface).border(1.dp, c.border),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material.Text(
                        a.name.take(1).uppercase(),
                        fontFamily = FontFamily.Monospace, color = c.accent,
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    androidx.compose.material.Text(
                        a.name, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textPrimary,
                    )
                    androidx.compose.material.Text(
                        "${a.trackCount} tracks · ${a.albumCount} albums",
                        fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textSecondary,
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
        }
        item { Spacer(Modifier.height(110.dp)) }
    }
}

@Composable
fun FoldersList(onClickFolder: (String) -> Unit) {
    val c = LocalDhunColors.current
    val folders by DhunApp.instance.library.folders.collectAsState()
    LazyColumn {
        items(folders) { f ->
            Row(
                Modifier.fillMaxWidth()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClickFolder(f.path) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(44.dp).background(c.surface).border(1.dp, c.border), contentAlignment = Alignment.Center) {
                    androidx.compose.material.Icon(Icons.Default.Folder, null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    androidx.compose.material.Text(
                        f.name, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textPrimary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    androidx.compose.material.Text(
                        f.path, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = c.textMuted,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    androidx.compose.material.Text(
                        "${f.trackCount} tracks",
                        fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textSecondary,
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
        }
        item { Spacer(Modifier.height(110.dp)) }
    }
}
