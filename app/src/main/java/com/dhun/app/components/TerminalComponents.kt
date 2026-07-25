package com.dhun.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhun.app.ui.theme.DhunColors
import com.dhun.app.ui.theme.LocalDhunColors
import com.dhun.app.ui.theme.TermTheme

// --- primitives ---

@Composable fun Square(color: Color, size: Dp = 6.dp) {
    Box(Modifier.size(size).background(color))
}

@Composable
fun TIcon(
    icon: ImageVector,
    active: Boolean = false,
    muted: Boolean = false,
    size: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    val c = LocalDhunColors.current
    val tint = when {
        active -> c.accent
        muted -> c.textMuted
        else -> c.textSecondary
    }
    val mod = if (onClick != null) Modifier
        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
        .padding(6.dp) else Modifier.padding(6.dp)
    Box(mod.size(size + 12.dp), contentAlignment = Alignment.Center) {
        androidx.compose.material.Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(size))
    }
}

@Composable
fun HairlineDivider(dashed: Boolean = false, modifier: Modifier = Modifier) {
    val c = LocalDhunColors.current
    if (!dashed) {
        Box(modifier.fillMaxWidth().height(1.dp).background(c.border))
    } else {
        Row(modifier.fillMaxWidth().height(1.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(60) { Box(Modifier.weight(1f).height(1.dp).background(c.textMuted)) }
        }
    }
}

@Composable
fun TermButton(
    label: String,
    onClick: () -> Unit,
    accent: Boolean = false,
    danger: Boolean = false,
    modifier: Modifier = Modifier,
    small: Boolean = false,
) {
    val c = LocalDhunColors.current
    val fg = when {
        accent -> c.bg
        danger -> c.error
        else -> c.textPrimary
    }
    val bg = when {
        accent -> c.accent
        else -> Color.Transparent
    }
    val borderColor = when {
        danger -> c.error
        accent -> c.accent
        else -> c.border
    }
    val padV = if (small) 4.dp else 8.dp
    val padH = if (small) 10.dp else 14.dp
    val fontSize = if (small) 12.sp else 13.sp
    Box(
        modifier
            .background(bg)
            .border(1.dp, borderColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = padV, horizontal = padH),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material.Text(
            label.lowercase(),
            color = fg,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
fun TermField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
) {
    val c = LocalDhunColors.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().border(1.dp, c.border).padding(horizontal = 10.dp, vertical = 8.dp),
        singleLine = true,
        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = c.textPrimary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
        cursorBrush = SolidColor(c.accent),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    androidx.compose.material.Text(
                        placeholder,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = c.textMuted,
                    )
                }
                inner()
            }
        }
    )
}

@Composable
fun SectionHeader(label: String, action: (@Composable () -> Unit)? = null) {
    val c = LocalDhunColors.current
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        androidx.compose.material.Text(
            label.lowercase(),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = c.textMuted,
            letterSpacing = 1.sp,
        )
        if (action != null) action()
    }
}

@Composable
fun SettingsRow(
    label: String,
    value: String? = null,
    toggle: Boolean? = null,
    onClick: (() -> Unit)? = null,
    danger: Boolean = false,
) {
    val c = LocalDhunColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .border(1.dp, c.border)
            .let { if (onClick != null) it.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ) else it }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        androidx.compose.material.Text(
            label,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = if (danger) c.error else c.textPrimary,
        )
        when {
            toggle != null -> {
                Square(if (toggle) c.accent else c.textMuted, size = 12.dp)
            }
            value != null -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material.Text(
                        value,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = c.textSecondary,
                    )
                    Spacer(Modifier.width(6.dp))
                    androidx.compose.material.Icon(
                        Icons.Default.ChevronRight, null,
                        tint = c.textMuted,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            else -> {
                androidx.compose.material.Icon(
                    Icons.Default.ChevronRight, null,
                    tint = c.textMuted,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

// Tabs (square, underlined/filled)
enum class TabMode { FILLED_ACTIVE, UNDERLINE }

@Composable
fun TermTabs(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    mode: TabMode = TabMode.FILLED_ACTIVE,
) {
    val c = LocalDhunColors.current
    Row(Modifier.fillMaxWidth().border(1.dp, c.border)) {
        tabs.forEachIndexed { i, label ->
            val active = i == selected
            val bg = if (active && mode == TabMode.FILLED_ACTIVE) c.surface else Color.Transparent
            val fg = if (active) c.textPrimary else c.textSecondary
            val bottomBorder = if (active && mode == TabMode.UNDERLINE) c.accent else Color.Transparent
            Box(
                Modifier
                    .weight(1f)
                    .background(bg)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material.Text(
                        label.lowercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = fg,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.width(24.dp).height(1.dp).background(bottomBorder))
                }
            }
        }
    }
}

// Bottom tab indicator dot square
@Composable
fun BottomTab(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val c = LocalDhunColors.current
    Column(
        Modifier
            .weight(1f)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (active) Square(c.accent, size = 5.dp) else Spacer(Modifier.height(5.dp))
        Spacer(Modifier.height(3.dp))
        androidx.compose.material.Icon(
            icon, label,
            tint = if (active) c.accent else c.textMuted,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.height(2.dp))
        androidx.compose.material.Text(
            label,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = if (active) c.accent else c.textMuted,
        )
    }
}
