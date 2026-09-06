package com.kasirpro.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── KasirPro WARM EMBER — hangus terracotta + sage + peach cream ─────────────
// Nada KosKeeper Terracotta tapi lebih dalam/pekat, bukan navy dingin.
// Light = peach pucat #FFFBF3, Dark = coklat panggang.

private val LightColors = lightColorScheme(
    primary = Color(0xFFC2703E),            // Burnt Terracotta
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFDE8D0),    // peach container
    onPrimaryContainer = Color(0xFF3E2F24),
    secondary = Color(0xFF6B8F7A),           // Sage kalem
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD8E8DC),
    onSecondaryContainer = Color(0xFF25352B),
    tertiary = Color(0xFF8C5A3C),            // Warm Cacao
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF5E0CC),
    background = Color(0xFFFFFBF3),          // Warm Peach pucat
    onBackground = Color(0xFF3E2F24),
    surface = Color(0xFFFFFEFB),
    onSurface = Color(0xFF3E2F24),
    onSurfaceVariant = Color(0xFF8A6B5A),
    surfaceVariant = Color(0xFFF5E6D3),
    outline = Color(0xFFEADCCB),
    outlineVariant = Color(0xFFF3E8D9),
    scrim = Color(0x663E2F24),
    error = Color(0xFFC25A4A),
    onError = Color(0xFFFFFFFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE8A06B),             // terracotta terang di dark
    onPrimary = Color(0xFF3E2F24),
    primaryContainer = Color(0xFF6B3D1E),
    onPrimaryContainer = Color(0xFFFDE8D0),
    secondary = Color(0xFF8AB4A0),
    onSecondary = Color(0xFF1A3324),
    secondaryContainer = Color(0xFF2D4A3A),
    tertiary = Color(0xFFD9A88A),
    onTertiary = Color(0xFF3E2F24),
    background = Color(0xFF1F1610),           // coklat panggang gelap
    onBackground = Color(0xFFFDE8D0),
    surface = Color(0xFF2A1D14),
    onSurface = Color(0xFFF5E6D3),
    onSurfaceVariant = Color(0xFFB8A090),
    surfaceVariant = Color(0xFF3E2F24),
    outline = Color(0xFF5A4435),
    outlineVariant = Color(0xFF3E2F24),
    scrim = Color(0x991F1610),
    error = Color(0xFFE8A0A0),
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
        content = content,
    )
}
