package com.stikr.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// stikr palette: violet primary, mint secondary, warm peach accent.
private val Violet = Color(0xFF6C5CE7)
private val VioletDeep = Color(0xFF3C2FA8)
private val VioletSoft = Color(0xFFE4DFFF)
private val Mint = Color(0xFF00C2A8)
private val MintSoft = Color(0xFFC6F5EB)
private val Peach = Color(0xFFFFB03A)
private val PeachSoft = Color(0xFFFFE3B3)
private val Ink = Color(0xFF1B1633)
private val InkSoft = Color(0xFF5B5476)
private val Cream = Color(0xFFFDFBFF)
private val NightSurface = Color(0xFF141125)
private val NightRaised = Color(0xFF211D3A)
private val NightLine = Color(0xFF3A3459)
private val Moon = Color(0xFFEAE6F8)
private val MoonSoft = Color(0xFFB5AECF)

private val LightColors = lightColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = VioletSoft,
    onPrimaryContainer = Color(0xFF1B1352),
    secondary = Mint,
    onSecondary = Color(0xFF00382F),
    secondaryContainer = MintSoft,
    onSecondaryContainer = Color(0xFF00382F),
    tertiary = Peach,
    onTertiary = Color(0xFF3A2500),
    tertiaryContainer = PeachSoft,
    onTertiaryContainer = Color(0xFF3A2500),
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEFEBFA),
    onSurfaceVariant = InkSoft,
    surfaceContainer = Color(0xFFF4F0FF),
    surfaceContainerHigh = Color(0xFFEDE8FB),
    outline = Color(0xFF8F88AB),
    outlineVariant = Color(0xFFDCD6EE),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9AEFF),
    onPrimary = Color(0xFF23156B),
    primaryContainer = VioletDeep,
    onPrimaryContainer = VioletSoft,
    secondary = Color(0xFF64E0CB),
    onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF005045),
    onSecondaryContainer = MintSoft,
    tertiary = Color(0xFFFFCF7E),
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5B4300),
    onTertiaryContainer = PeachSoft,
    background = NightSurface,
    onBackground = Moon,
    surface = NightSurface,
    onSurface = Moon,
    surfaceVariant = NightRaised,
    onSurfaceVariant = MoonSoft,
    surfaceContainer = NightRaised,
    surfaceContainerHigh = Color(0xFF2A2443),
    outline = Color(0xFF7A7396),
    outlineVariant = NightLine,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun StikrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
