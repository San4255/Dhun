package com.dhun.app.ui.screens.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhun.app.DhunApp
import com.dhun.app.components.HairlineDivider
import com.dhun.app.components.SectionHeader
import com.dhun.app.components.SettingsRow
import com.dhun.app.components.TIcon
import com.dhun.app.components.TermButton
import com.dhun.app.ui.theme.LocalDhunColors
import com.dhun.app.ui.theme.TermTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val c = LocalDhunColors.current
    var screen by remember { mutableStateOf(S.Main) }
    Column(Modifier.fillMaxSize().background(c.bg)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TIcon(Icons.Default.ArrowBack, onClick = {
                if (screen != S.Main) screen = S.Main else onBack()
            })
            androidx.compose.material.Text(
                when (screen) {
                    S.Main -> "settings"
                    S.EQ -> "equalizer"
                    S.Appearance -> "appearance"
                    S.Folders -> "scan folders"
                    S.Sleep -> "sleep timer"
                    S.About -> "about"
                },
                fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = c.textMuted,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        HairlineDivider()
        when (screen) {
            S.Main -> SettingsMain(
                onOpenEq = { screen = S.EQ },
                onOpenAppearance = { screen = S.Appearance },
                onOpenFolders = { screen = S.Folders },
                onOpenSleep = { screen = S.Sleep },
                onOpenAbout = { screen = S.About },
            )
            S.EQ -> EqSub()
            S.Appearance -> AppearanceSub()
            S.Folders -> FoldersSub()
            S.Sleep -> SleepSub()
            S.About -> AboutSub()
        }
    }
}

private enum class S { Main, EQ, Appearance, Folders, Sleep, About }

@Composable
private fun SettingsMain(
    onOpenEq: () -> Unit, onOpenAppearance: () -> Unit, onOpenFolders: () -> Unit,
    onOpenSleep: () -> Unit, onOpenAbout: () -> Unit,
) {
    val c = LocalDhunColors.current
    val app = DhunApp.instance
    val scope = rememberCoroutineScope()
    val prefs = app.prefs
    var gapless by remember { mutableStateOf(true) }
    var crossfade by remember { mutableIntStateOf(0) }
    var speed by remember { mutableFloatStateOf(1.0f) }
    var resumeHp by remember { mutableStateOf(false) }
    var pauseDisc by remember { mutableStateOf(true) }
    var focusMode by remember { mutableStateOf("duck") }
    var eqEnabled by remember { mutableStateOf(false) }
    var normalize by remember { mutableStateOf(false) }
    var minDur by remember { mutableIntStateOf(5) }
    var scanning by remember { mutableStateOf(false) }
    val scan by app.library.scanProgress.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
        SectionHeader("library")
        SettingsRow("scan folders", value = "all folders", onClick = onOpenFolders)
        Box(Modifier.fillMaxWidth().border(1.dp, c.border).padding(horizontal = 12.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    androidx.compose.material.Text("rescan library", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textPrimary)
                    androidx.compose.material.Text(
                        if (scanning && !scan.done) "indexing ${scan.scanned}/${scan.totalFound}"
                        else "last scan: ${if (app.library.lastScanTime == 0L) "never" else "just now"}",
                        fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted,
                    )
                }
                TermButton(label = if (scanning) "scanning..." else "rescan", small = true,
                    onClick = {
                        scanning = true
                        scope.launch { app.library.scan(minDur); scanning = false }
                    }
                )
            }
        }
        SettingsRow("file formats", value = "mp3/flac/wav/m4a/ogg/opus")
        StepperRow("minimum file duration", minDur, "sec", 0..60 step 5) { minDur = it }
        SettingsRow("ignore folders", value = "whatsapp/voice notes")

        SectionHeader("playback")
        ToggleRow("gapless playback", gapless) { gapless = it }
        StepperRow("crossfade", crossfade, "s", 0..12 step 1) { crossfade = it }
        SettingsRow("default playback speed", value = "${"%.1f".format(speed)}x", onClick = {
            val next = when {
                speed < 0.75f -> 0.75f
                speed < 1.0f -> 1.0f
                speed < 1.25f -> 1.25f
                speed < 1.5f -> 1.5f
                speed < 2.0f -> 2.0f
                else -> 0.5f
            }
            speed = next; app.player.setSpeed(next)
        })
        ToggleRow("resume on headphone connect", resumeHp) { resumeHp = it }
        ToggleRow("pause on disconnect", pauseDisc) { pauseDisc = it }
        SettingsRow("audio focus behavior", value = focusMode, onClick = {
            focusMode = if (focusMode == "duck") "pause" else "duck"
        })

        SectionHeader("equalizer")
        ToggleRow("equalizer", eqEnabled) { eqEnabled = it; onOpenEq() }
        SettingsRow("custom bands / presets", value = "flat", onClick = onOpenEq)
        ToggleRow("replay gain / normalize volume", normalize) { normalize = it }

        SectionHeader("appearance")
        SettingsRow("theme", value = "dark", onClick = onOpenAppearance)
        SettingsRow("accent color", value = "green", onClick = onOpenAppearance)
        SettingsRow("now playing art", value = "embedded / ascii", onClick = onOpenAppearance)
        SettingsRow("list density", value = "comfortable", onClick = onOpenAppearance)

        SectionHeader("sleep & automation")
        SettingsRow("sleep timer", value = "off", onClick = onOpenSleep)

        SectionHeader("storage")
        SettingsRow("cache size", value = "0 mb")
        SettingsRow("clear cache", value = "clear", onClick = {})
        SettingsRow("storage used by library", value = "—")

        SectionHeader("about")
        SettingsRow("version", value = "0.1.0-offline", onClick = onOpenAbout)
        SettingsRow("licenses")
        SettingsRow("online sources", value = "coming soon")
        Spacer(Modifier.height(110.dp))
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    val c = LocalDhunColors.current
    Box(
        Modifier.fillMaxWidth().border(1.dp, c.border).clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onChange(!value) }
        ).padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material.Text(label, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textPrimary)
            Box(Modifier.size(16.dp).border(1.dp, if (value) c.accent else c.textMuted)) {
                if (value) Box(Modifier.size(8.dp).background(c.accent).align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun StepperRow(label: String, value: Int, unit: String, range: IntProgression, step: Int, onChange: (Int) -> Unit) {
    val c = LocalDhunColors.current
    Box(Modifier.fillMaxWidth().border(1.dp, c.border).padding(horizontal = 12.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material.Text(label, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textPrimary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TermButton(label = "-", small = true, onClick = {
                    val next = value - step
                    if (next >= range.first) onChange(next)
                })
                androidx.compose.material.Text(
                    "$value $unit", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = c.textSecondary,
                )
                TermButton(label = "+", small = true, accent = true, onClick = {
                    val next = value + step
                    if (next <= range.last) onChange(next)
                })
            }
        }
    }
}
@Composable
private fun StepperRow(label: String, value: Int, unit: String, range: IntProgression, onChange: (Int) -> Unit) {
    StepperRow(label = label, value = value, unit = unit, range = range, step = range.step, onChange = onChange)
}

// --- Sub screens ---

@Composable
private fun EqSub() {
    val c = LocalDhunColors.current
    var enabled by remember { mutableStateOf(false) }
    var preset by remember { mutableStateOf("flat") }
    val presets = listOf("off", "flat", "bass boost", "vocal", "custom")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
        ToggleRow("equalizer", enabled) { enabled = it }
        Spacer(Modifier.height(8.dp))
        presets.forEach { p ->
            Box(
                Modifier.fillMaxWidth().border(1.dp, c.border).clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null,
                ) { preset = p }.padding(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material.Text(p, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                        color = if (p == preset) c.accent else c.textPrimary,
                        fontWeight = if (p == preset) FontWeight.Bold else FontWeight.Normal)
                    if (p == preset) TIcon(Icons.Default.Check, active = true, size = 14.dp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        androidx.compose.material.Text("custom bands (5)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted)
        Spacer(Modifier.height(8.dp))
        listOf("60", "230", "910", "3k6", "14k").forEach { freq ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material.Text(freq.padStart(4), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted, modifier = Modifier.width(40.dp))
                var v by remember { mutableFloatStateOf(0.5f) }
                Box(Modifier.weight(1f).height(16.dp).border(1.dp, c.border), contentAlignment = Alignment.CenterStart) {
                    Box(Modifier.fillMaxWidth(v).height(2.dp).background(c.accent))
                }
                androidx.compose.material.Text("+0db", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = c.textMuted, modifier = Modifier.width(40.dp).padding(start = 6.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        StepperRow("bass boost", 0, "%", 0..100 step 10) {}
        ToggleRow("virtualizer", false) {}
    }
}

@Composable
private fun AppearanceSub() {
    val c = LocalDhunColors.current
    var theme by remember { mutableStateOf("dark") }
    var accent by remember { mutableStateOf("green") }
    var artStyle by remember { mutableStateOf("embedded") }
    var density by remember { mutableStateOf("comfortable") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
        SectionHeader("theme")
        listOf("dark", "true black", "light").forEach { t ->
            Box(
                Modifier.fillMaxWidth().border(1.dp, c.border).clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null,
                ) { theme = t }.padding(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    androidx.compose.material.Text(t, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                        color = if (t == theme) c.accent else c.textPrimary)
                    if (t == theme) TIcon(Icons.Default.Check, active = true, size = 14.dp)
                }
            }
        }
        SectionHeader("accent color")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TermTheme.accentOptions.forEach { (name, hex) ->
                Box(
                    Modifier.size(36.dp).border(if (name == accent) 2.dp else 1.dp, if (name == accent) c.accent else c.border)
                        .background(Color(hex)).clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null,
                        ) { accent = name },
                    contentAlignment = Alignment.Center,
                ) {
                    if (name == accent) TIcon(Icons.Default.Check, active = name == "green", size = 14.dp)
                }
            }
        }
        SectionHeader("now playing art style")
        listOf("embedded art", "ascii pattern", "minimal waveform").forEach { a ->
            Box(Modifier.fillMaxWidth().border(1.dp, c.border).clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null,
            ) { artStyle = a }.padding(12.dp)) {
                androidx.compose.material.Text(a, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                    color = if (a == artStyle) c.accent else c.textPrimary)
            }
        }
        SectionHeader("list density")
        listOf("compact", "comfortable").forEach { d ->
            Box(Modifier.fillMaxWidth().border(1.dp, c.border).clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null,
            ) { density = d }.padding(12.dp)) {
                androidx.compose.material.Text(d, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                    color = if (d == density) c.accent else c.textPrimary)
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun FoldersSub() {
    val c = LocalDhunColors.current
    val folders by DhunApp.instance.library.folders.collectAsState()
    val included = remember(folders) { folders.map { it.path }.toMutableSet() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
        androidx.compose.material.Text(
            "toggle folders to include in scan", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted,
        )
        Spacer(Modifier.height(12.dp))
        folders.forEach { f ->
            val isIn = included.contains(f.path)
            Box(
                Modifier.fillMaxWidth().border(1.dp, c.border).clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null,
                ) {
                    if (isIn) included.remove(f.path) else included.add(f.path)
                }.padding(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        androidx.compose.material.Text(f.name, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                            color = if (isIn) c.textPrimary else c.textMuted)
                        androidx.compose.material.Text(f.path, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = c.textMuted,
                            maxLines = 1)
                    }
                    Box(Modifier.size(14.dp).border(1.dp, if (isIn) c.accent else c.textMuted)) {
                        if (isIn) Box(Modifier.size(7.dp).background(c.accent).align(Alignment.Center))
                    }
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SleepSub() {
    val c = LocalDhunColors.current
    var mins by remember { mutableIntStateOf(0) }
    var endOfTrack by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
        androidx.compose.material.Text(
            "stop playback after", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted,
        )
        Spacer(Modifier.height(12.dp))
        listOf(0, 5, 10, 15, 30, 45, 60, 90, 120).forEach { m ->
            Box(Modifier.fillMaxWidth().border(1.dp, c.border).clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null,
            ) { mins = m }.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    androidx.compose.material.Text(if (m == 0) "off" else "$m minutes",
                        fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = if (m == mins) c.accent else c.textPrimary)
                    if (m == mins) TIcon(Icons.Default.Check, active = true, size = 14.dp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        ToggleRow("stop at end of track", endOfTrack) { endOfTrack = it }
    }
}

@Composable
private fun AboutSub() {
    val c = LocalDhunColors.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
        androidx.compose.material.Text("dhun", fontFamily = FontFamily.Monospace, fontSize = 22.sp, color = c.accent, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        androidx.compose.material.Text("offline music player · v0.1.0-offline",
            fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted)
        Spacer(Modifier.height(16.dp))
        val ascii = """
        ____  _
       |  _ \| |__  _   _ _ __
       | | | | '_ \| | | | '_ \
       | |_| | | | | |_| | | | |
       |____/|_| |_|\__,_|_| |_|
        """.trimIndent()
        androidx.compose.material.Text(ascii, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = c.textMuted, lineHeight = 14.sp)
        Spacer(Modifier.height(16.dp))
        SectionHeader("license")
        androidx.compose.material.Text("mit", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = c.textSecondary)
        SectionHeader("credits")
        androidx.compose.material.Text("media3 · compose · coil · accompanist",
            fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = c.textMuted)
        Spacer(Modifier.height(80.dp))
    }
}
