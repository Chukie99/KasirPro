package com.kasirpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color

// ── KasirPro Blue-Pastel Color Scheme (Google Blue, NOT Purple) ────────────────
private val LightColors = lightColorScheme(
    primary = Color(0xFF1A73E8),        // primary
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8F0FE),  // primary_light
    onPrimaryContainer = Color(0xFF0F3C8C),
    secondary = Color(0xFF34A853),     // secondary
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6F4EA),  // secondary_light
    onSecondaryContainer = Color(0xFF127D3F),
    tertiary = Color(0xFFFBBC04),      // tertiary
    onTertiary = Color(0xFF1C1B1E),
    tertiaryContainer = Color(0xFFFFECAD),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF5F6368),
    error = Color(0xFFD93025),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFFD1D5DB),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE8F0FE),
    onPrimary = Color(0xFF0F3C8C),
    primaryContainer = Color(0xFF1557B0),
    onPrimaryContainer = Color(0xFFE8F0FE),
    secondary = Color(0xFF66BB6A),
    onSecondary = Color(0xFF003319),
    secondaryContainer = Color(0xFF1B5E20),
    tertiary = Color(0xFFFFD54F),
    onTertiary = Color(0xFF4D3500),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF67001F),
    outline = Color(0xFF475569),
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
