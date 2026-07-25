package com.dhun.app.ui.screens.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.dhun.app.components.TIcon
import com.dhun.app.components.TermButton
import com.dhun.app.data.formatDuration
import com.dhun.app.ui.theme.LocalDhunColors

@Composable
fun QueueSheet(onClose: () -> Unit, onAddTracks: () -> Unit) {
    val c = LocalDhunColors.current
    val player = DhunApp.instance.player
    val state by player.state.collectAsState()
    val queueSongs = state.queue.mapNotNull { DhunApp.instance.library.findSong(it) }
    val curIdx = state.currentIndex

    Column(Modifier.fillMaxSize().background(c.bg)) {
        // drag handle strip
        Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.width(40.dp).height(3.dp).background(c.border))
        }
        // header
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TIcon(Icons.Default.KeyboardArrowDown, onClick = onClose, size = 20.dp)
                Spacer(Modifier.width(8.dp))
                val shown = if (curIdx >= 0) (curIdx + 1) else 0
                androidx.compose.material.Text(
                    "queue · ${shown.toString().padStart(2,'0')}/${queueSongs.size}",
                    fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = c.textPrimary,
                )
            }
            Row {
                TermButton(label = "add tracks", small = true, onClick = onAddTracks)
                Spacer(Modifier.width(6.dp))
                TermButton(label = "clear", small = true, danger = true, onClick = { player.clearQueue(); onClose() })
            }
        }
        HairlineDivider()
        if (queueSongs.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) {
                androidx.compose.material.Text(
                    "[ queue empty ]", fontFamily = FontFamily.Monospace, color = c.textSecondary, fontSize = 12.sp,
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(queueSongs) { i, s ->
                    val isCur = i == curIdx
                    val bg = if (isCur) c.surface else Color.Transparent
                    val fg = if (isCur) c.accent else c.textPrimary
                    Row(
                        Modifier.fillMaxWidth().background(bg).padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TIcon(Icons.Default.DragHandle, muted = true, size = 14.dp, onClick = { /* TODO: drag reorder */ })
                        Spacer(Modifier.width(4.dp))
                        androidx.compose.material.Text(
                            "%03d".format(i + 1),
                            fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = if (isCur) c.accent else c.textMuted,
                            modifier = Modifier.width(32.dp),
                        )
                        Column(Modifier.weight(1f).clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null,
                        ) { player.play(queueSongs, i) }) {
                            androidx.compose.material.Text(
                                s.title, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = fg,
                                fontWeight = if (isCur) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            androidx.compose.material.Text(
                                "${s.artist} · ${formatDuration(s.durationMs)}",
                                fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                                color = if (isCur) c.accent.copy(alpha = 0.7f) else c.textSecondary,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                        TIcon(Icons.Default.Close, muted = true, size = 14.dp, onClick = { player.removeFromQueue(i) })
                    }
                    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(1.dp).background(c.border))
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}
