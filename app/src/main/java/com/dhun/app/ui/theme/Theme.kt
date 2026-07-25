package com.dhun.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class DhunColors(
    val bg: Color,
    val surface: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val error: Color,
)

val LocalDhunColors = compositionLocalOf {
    DhunColors(
        bg = Color(TermTheme.bg),
        surface = Color(TermTheme.surface),
        border = Color(TermTheme.border),
        textPrimary = Color(TermTheme.textPrimary),
        textSecondary = Color(TermTheme.textSecondary),
        textMuted = Color(TermTheme.textMuted),
        accent = Color(TermTheme.accent),
        error = Color(TermTheme.error),
    )
}

object DhunShapes {
    val Square: Shape = RoundedCornerShape(0)
}

object DhunTypo {
    val MonoFamily = FontFamily.Monospace
    val body = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = Color(TermTheme.textPrimary),
    )
    val small = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = Color(TermTheme.textSecondary),
    )
    val tiny = TextStyle(
        fontFamily = MonoFamily,
        fontSize = 11.sp,
        color = Color(TermTheme.textMuted),
    )
    val title = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color(TermTheme.textPrimary),
    )
    val header = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = Color(TermTheme.textPrimary),
        letterSpacing = 0.5.sp,
    )
}

private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color(TermTheme.accent).copy(alpha = 0.15f)
    @Composable
    override fun rippleAlpha() = RippleAlpha(0.0f, 0.0f, 0.0f, 0.15f)
}

@Composable
fun DhunTheme(content: @Composable () -> Unit) {
    // always dark, terminal aesthetic; keep true black/dark toggle handled via surface/bg swap if wanted
    val colors = DhunColors(
        bg = Color(TermTheme.bg),
        surface = Color(TermTheme.surface),
        border = Color(TermTheme.border),
        textPrimary = Color(TermTheme.textPrimary),
        textSecondary = Color(TermTheme.textSecondary),
        textMuted = Color(TermTheme.textMuted),
        accent = Color(TermTheme.accent),
        error = Color(TermTheme.error),
    )
    CompositionLocalProvider(
        LocalDhunColors provides colors,
        LocalRippleTheme provides NoRippleTheme,
        content = content,
    )
}
