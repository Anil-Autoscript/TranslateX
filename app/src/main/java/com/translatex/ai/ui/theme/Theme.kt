package com.translatex.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand colours ─────────────────────────────────────────────────────────

val Primary        = Color(0xFF2563EB)
val PrimaryVariant = Color(0xFF3B82F6)
val Accent         = Color(0xFF10B981)
val AccentDark     = Color(0xFF059669)

val SurfaceLight   = Color(0xFFF8FAFC)
val SurfaceDark    = Color(0xFF1E1E2E)
val BackgroundDark = Color(0xFF121212)
val CardDark       = Color(0xFF1E1E2E)
val CardLight      = Color(0xFFFFFFFF)

val ErrorRed       = Color(0xFFEF4444)

// ── Light colour scheme ───────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary            = Primary,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFDCEEFF),
    onPrimaryContainer = Color(0xFF001C3D),
    secondary          = PrimaryVariant,
    onSecondary        = Color.White,
    tertiary           = Accent,
    onTertiary         = Color.White,
    background         = SurfaceLight,
    onBackground       = Color(0xFF1A1A2E),
    surface            = CardLight,
    onSurface          = Color(0xFF1A1A2E),
    surfaceVariant     = Color(0xFFEEF2FF),
    error              = ErrorRed,
    onError            = Color.White
)

// ── Dark colour scheme ────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary            = PrimaryVariant,
    onPrimary          = Color(0xFF001C3D),
    primaryContainer   = Color(0xFF004494),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary          = Color(0xFF7DB3FF),
    onSecondary        = Color(0xFF003060),
    tertiary           = Color(0xFF34D399),
    onTertiary         = Color(0xFF003825),
    background         = BackgroundDark,
    onBackground       = Color(0xFFE2E8F0),
    surface            = CardDark,
    onSurface          = Color(0xFFE2E8F0),
    surfaceVariant     = Color(0xFF2D2D44),
    error              = Color(0xFFF87171),
    onError            = Color(0xFF690005)
)

// ── Typography ─────────────────────────────────────────────────────────────

val TranslateXTypography = Typography()   // uses Material defaults; customise as needed

// ── Theme composable ───────────────────────────────────────────────────────

@Composable
fun TranslateXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = TranslateXTypography,
        content     = content
    )
}
