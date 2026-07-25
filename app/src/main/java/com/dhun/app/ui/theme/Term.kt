package com.dhun.app.ui.theme

/**
 * Terminal / monospace design tokens. Mirror values in res/values/colors.xml.
 */
object TermTheme {
    const val bg        = 0xFF0A0A0AL
    const val surface   = 0xFF111111L
    const val border    = 0xFF262626L
    const val textPrimary  = 0xFFF5F5F5L
    const val textSecondary = 0xFF888888L
    const val textMuted    = 0xFF555555L
    const val accent    = 0xFF7EE787L
    const val error     = 0xFFE5534BL

    // accent palette options (used in appearance settings)
    val accentOptions = mapOf(
        "green" to 0xFF7EE787L,
        "amber" to 0xFFF0B429L,
        "blue"  to 0xFF58A6FFL,
        "red"   to 0xFFE5534BL,
    )

    // monospace font setup
    const val mono = "monospace"
}
