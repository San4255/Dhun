package com.dhun.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhun.app.DhunApp
import com.dhun.app.components.HairlineDivider
import com.dhun.app.components.SectionHeader
import com.dhun.app.components.SongRow
import com.dhun.app.components.TIcon
import com.dhun.app.components.TermButton
import com.dhun.app.ui.theme.LocalDhunColors

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onNowPlaying: () -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (String) -> Unit,
) {
    val c = LocalDhunColors.current
    val app = DhunApp.instance
    val songs by app.library.songs.collectAsState()
    val albums by app.library.albums.collectAsState()
    val artists by app.library.artists.collectAsState()

    var q by remember { mutableStateOf("") }
    val recent = remember { mutableListOf<String>() }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        // top bar with back + field
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            TIcon(Icons.Default.ArrowBack, onClick = onBack, size = 20.dp)
            Spacer(Modifier.width(4.dp))
            Box(Modifier.weight(1f).border(1.dp, c.border).padding(horizontal = 10.dp, vertical = 6.dp)) {
                androidx.compose.foundation.text.BasicTextField(
                    value = q,
                    onValueChange = { q = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textPrimary),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(c.accent),
                    decorationBox = { inner ->
                        if (q.isEmpty()) androidx.compose.material.Text(
                            "search ${songs.size} tracks...",
                            fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textMuted,
                        )
                        inner()
                    }
                )
            }
            if (q.isNotEmpty()) TIcon(Icons.Default.Close, muted = true, size = 18.dp, onClick = { q = "" })
        }
        HairlineDivider()

        val query = q.trim().lowercase()
        if (query.isBlank()) {
            // recent searches
            Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                SectionHeader("recent searches") {
                    if (recent.isNotEmpty()) TermButton(label = "clear", small = true, onClick = { recent.clear() })
                }
                if (recent.isEmpty()) {
                    androidx.compose.material.Text(
                        "[ no recent searches ]", fontFamily = FontFamily.Monospace, color = c.textMuted, fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                } else {
                    recent.forEach { r ->
                        Row(
                            Modifier.fillMaxWidth().clickable(
                                interactionSource = remember { MutableInteractionSource() }, indication = null,
                            ) { q = r }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            androidx.compose.material.Icon(Icons.Default.History, null, tint = c.textMuted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(10.dp))
                            androidx.compose.material.Text(r, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = c.textSecondary)
                        }
                    }
                }
            }
        } else {
            val songMatches = songs.filter {
                it.title.lowercase().contains(query) || it.artist.lowercase().contains(query) || it.album.lowercase().contains(query)
            }.take(5)
            val albumMatches = albums.filter { it.title.lowercase().contains(query) || it.artist.lowercase().contains(query) }.take(3)
            val artistMatches = artists.filter { it.name.lowercase().contains(query) }.take(3)

            if (songMatches.isEmpty() && albumMatches.isEmpty() && artistMatches.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.material.Text(
                        "[ no matches for \"$q\" ]", fontFamily = FontFamily.Monospace, color = c.textSecondary, fontSize = 12.sp,
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                    if (songMatches.isNotEmpty()) {
                        item {
                            SectionHeader("songs") {}
                        }
                        items(songMatches) { s ->
                            SongRow(
                                index = "·", song = s, showFormatTag = true, isCurrent = false,
                                onClick = {
                                    if (!recent.contains(q) && q.isNotBlank()) recent.add(0, q)
                                    val full = songs
                                    val idx = full.indexOf(s)
                                    app.player.play(full, idx)
                                    onNowPlaying()
                                },
                                onMore = null,
                            )
                        }
                    }
                    if (albumMatches.isNotEmpty()) {
                        item { SectionHeader("albums") {} }
                        items(albumMatches) { a ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() }, indication = null,
                                    ) { if (!recent.contains(q)) recent.add(0, q); onOpenAlbum(a.id) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.size(40.dp).background(c.surface).border(1.dp, c.border), contentAlignment = Alignment.Center) {
                                    androidx.compose.material.Text("♫", fontFamily = FontFamily.Monospace, color = c.accent, fontSize = 16.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    androidx.compose.material.Text(a.title, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textPrimary)
                                    androidx.compose.material.Text(a.artist, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textSecondary)
                                }
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border).padding(horizontal = 4.dp))
                        }
                    }
                    if (artistMatches.isNotEmpty()) {
                        item { SectionHeader("artists") {} }
                        items(artistMatches) { ar ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() }, indication = null,
                                    ) { if (!recent.contains(q)) recent.add(0, q); onOpenArtist(ar.name) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.size(40.dp).background(c.surface).border(1.dp, c.border), contentAlignment = Alignment.Center) {
                                    androidx.compose.material.Text(
                                        ar.name.take(1).uppercase(),
                                        fontFamily = FontFamily.Monospace, color = c.accent, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                androidx.compose.material.Text(ar.name, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textPrimary)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border).padding(horizontal = 4.dp))
                        }
                    }
                    item { Spacer(Modifier.height(120.dp)) }
                }
            }
        }
    }
}
