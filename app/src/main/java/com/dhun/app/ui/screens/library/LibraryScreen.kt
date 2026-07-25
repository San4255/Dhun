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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhun.app.DhunApp
import com.dhun.app.components.HairlineDivider
import com.dhun.app.components.TIcon
import com.dhun.app.components.TabMode
import com.dhun.app.components.TermTabs
import com.dhun.app.ui.theme.LocalDhunColors

@Composable
fun LibraryScreen(
    onOpenNowPlaying: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenFolder: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val c = LocalDhunColors.current
    var tab by remember { mutableStateOf(0) }
    val tabLabels = listOf("songs", "albums", "artists", "folders")

    Column(Modifier.fillMaxSize().background(c.bg)) {
        // header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material.Text(
                "library",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = c.textPrimary,
                letterSpacing = 1.sp,
            )
            Row {
                TIcon(Icons.Default.FolderOpen, onClick = { /* TODO: src folders */ }, contentDescription = "source")
                TIcon(Icons.Default.Tune, onClick = onOpenSettings, contentDescription = "settings")
            }
        }
        HairlineDivider()

        // sub-tabs
        TermTabs(tabs = tabLabels, selected = tab, onSelect = { tab = it }, mode = TabMode.FILLED_ACTIVE)

        // online placeholder slot (disabled)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Box(
                Modifier
                    .border(1.dp, c.border)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                androidx.compose.material.Text(
                    "[src: offline · online coming soon]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = c.textMuted,
                )
            }
        }

        Box(Modifier.fillMaxSize()) {
            when (tab) {
                0 -> SongsList(
                    onGoToNowPlaying = onOpenNowPlaying,
                    onOpenSearch = onOpenSearch,
                )
                1 -> AlbumsGrid(onClickAlbum = onOpenAlbum)
                2 -> ArtistsList(onClickArtist = onOpenArtist)
                3 -> FoldersList(onClickFolder = onOpenFolder)
            }
        }
    }
}
