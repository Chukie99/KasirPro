package com.kasirpro.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color

// ── KasirPro PREMIUM — Ink Navy + Warm Ivory + Brass Gold ───────────────────
// Vibe: private banking / boutique hotel POS — calm, expensive, confident.
// Bukan biru Google norak, bukan hijau murahan.
private val LightColors = lightColorScheme(
    primary = Color(0xFF0F2440),           // Ink Navy — deep, quiet luxury
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8EDF3),  // mist — soft navy tint
    onPrimaryContainer = Color(0xFF0F2440),
    secondary = Color(0xFFC5A059),         // Brass Gold — warm, expensive accent
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFFF5EEDD),// warm ivory gold
    onSecondaryContainer = Color(0xFF3D2E14),
    tertiary = Color(0xFF8B7355),          // warm taupe
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF0E6D8),
    background = Color(0xFFFAF7F2),        // Warm Ivory — not cold grey
    onBackground = Color(0xFF101828),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101828),
    onSurfaceVariant = Color(0xFF667085),  // muted, not #5F6368 murahan
    surfaceVariant = Color(0xFFF2EFE9),    // warm stone
    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFF3F4F6),
    scrim = Color(0x660F2440),
    error = Color(0xFF8B2635),             // muted burgundy, not menyala
    onError = Color(0xFFFFFFFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC5A059),           // Gold leads in dark — luxury
    onPrimary = Color(0xFF0F2440),
    primaryContainer = Color(0xFF122E57),
    onPrimaryContainer = Color(0xFFE8EDF3),
    secondary = Color(0xFFD4B678),
    onSecondary = Color(0xFF0F2440),
    secondaryContainer = Color(0xFF2A1F0E),
    tertiary = Color(0xFFD9C5A5),
    onTertiary = Color(0xFF2A1F0E),
    background = Color(0xFF0A1628),        // deep navy night
    onBackground = Color(0xFFE8EDF3),
    surface = Color(0xFF12233F),           // navy surface, not #1E293B murahan
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceVariant = Color(0xFF1A2F4D),
    outline = Color(0xFF2D3F5E),
    outlineVariant = Color(0xFF1E314D),
    scrim = Color(0x990A1628),
    error = Color(0xFFE8A0A8),
    onError = Color(0xFF4A1018),
)

@Composable
fun Theme(
    themeMode: String = "system",
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val colors = if (isDark) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content,
    )
}
