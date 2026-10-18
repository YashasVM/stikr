package com.stikr.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stikr.app.R

/**
 * stikr's look: cream paper, ink outlines, hard offset shadows and pastel
 * cards — a sticker-book, neobrutalist theme. Colours are sampled from the
 * design reference.
 */
@Immutable
data class NeoPalette(
    /** Screen background: warm cream. */
    val paper: Color,
    /** Raised surfaces: cards, fields. */
    val card: Color,
    /** Text and every border. */
    val ink: Color,
    /** The hard offset shadow under cards and buttons. */
    val shadow: Color,
    /** Secondary text. */
    val muted: Color,
    /** Primary action fill. */
    val butter: Color,
    val cyan: Color,
    val green: Color,
    val pink: Color,
    /** The one loud accent: red. */
    val cherry: Color,
    /** Subtle fills: checkerboard, tracks, empty slots. */
    val creamDim: Color,
    /**
     * Text/icon colour for pastel fills (butter, cyan, green, pink). Those
     * fills stay light in both themes, so their content must stay dark ink.
     */
    val onAccent: Color,
)

private val LightPalette = NeoPalette(
    paper = Color(0xFFF1EDE7),
    card = Color(0xFFFFFFFF),
    ink = Color(0xFF191715),
    shadow = Color(0xFF191715),
    muted = Color(0xFF6B675E),
    butter = Color(0xFFF8DFA0),
    cyan = Color(0xFF9EDDE6),
    green = Color(0xFF9CE4A4),
    pink = Color(0xFFF3CDDA),
    cherry = Color(0xFFAE403E),
    creamDim = Color(0xFFE7E1D8),
    onAccent = Color(0xFF191715),
)

private val DarkPalette = NeoPalette(
    paper = Color(0xFF16140F),
    card = Color(0xFF232019),
    ink = Color(0xFFF3EFE6),
    shadow = Color(0xFF000000),
    muted = Color(0xFFA69E90),
    butter = Color(0xFFE9D08C),
    cyan = Color(0xFF7ECBD6),
    green = Color(0xFF82CF8B),
    pink = Color(0xFFE0AFC0),
    cherry = Color(0xFFD06A63),
    creamDim = Color(0xFF2E2A21),
    onAccent = Color(0xFF191715),
)

val LocalNeoPalette = staticCompositionLocalOf { LightPalette }

/** Anton: the heavy condensed display face used for every headline. */
val Anton = FontFamily(Font(R.font.anton_regular))

private fun lightScheme(p: NeoPalette) = lightColorScheme(
    primary = p.cherry,
    onPrimary = Color.White,
    primaryContainer = p.butter,
    onPrimaryContainer = p.ink,
    secondary = p.green,
    onSecondary = p.ink,
    secondaryContainer = p.creamDim,
    onSecondaryContainer = p.ink,
    tertiary = p.cyan,
    onTertiary = p.ink,
    background = p.paper,
    onBackground = p.ink,
    surface = p.card,
    onSurface = p.ink,
    surfaceVariant = p.creamDim,
    onSurfaceVariant = p.muted,
    surfaceContainer = p.creamDim,
    surfaceContainerHigh = p.creamDim,
    surfaceContainerHighest = p.creamDim,
    outline = p.ink,
    outlineVariant = p.ink.copy(alpha = 0.25f),
    error = p.cherry,
    onError = Color.White,
    errorContainer = p.pink,
    onErrorContainer = p.ink,
    inverseSurface = p.ink,
    inverseOnSurface = p.paper,
)

private fun darkScheme(p: NeoPalette) = darkColorScheme(
    primary = p.cherry,
    onPrimary = Color.White,
    primaryContainer = p.butter,
    onPrimaryContainer = Color(0xFF191715),
    secondary = p.green,
    onSecondary = Color(0xFF191715),
    secondaryContainer = p.creamDim,
    onSecondaryContainer = p.ink,
    tertiary = p.cyan,
    onTertiary = Color(0xFF191715),
    background = p.paper,
    onBackground = p.ink,
    surface = p.card,
    onSurface = p.ink,
    surfaceVariant = p.creamDim,
    onSurfaceVariant = p.muted,
    surfaceContainer = p.creamDim,
    surfaceContainerHigh = p.creamDim,
    surfaceContainerHighest = p.creamDim,
    outline = p.ink,
    outlineVariant = p.ink.copy(alpha = 0.3f),
    error = p.cherry,
    onError = Color.White,
    errorContainer = p.pink,
    onErrorContainer = Color(0xFF191715),
    inverseSurface = p.ink,
    inverseOnSurface = p.paper,
)

@Composable
fun StikrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    CompositionLocalProvider(LocalNeoPalette provides palette) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkScheme(palette) else lightScheme(palette),
            shapes = Shapes(
                extraSmall = RoundedCornerShape(8.dp),
                small = RoundedCornerShape(10.dp),
                medium = RoundedCornerShape(14.dp),
                large = RoundedCornerShape(18.dp),
                extraLarge = RoundedCornerShape(24.dp),
            ),
            typography = Typography(
                displayLarge = TextStyle(fontFamily = Anton, fontSize = 40.sp, lineHeight = 43.sp, letterSpacing = 0.4.sp),
                displayMedium = TextStyle(fontFamily = Anton, fontSize = 32.sp, lineHeight = 35.sp, letterSpacing = 0.4.sp),
                headlineMedium = TextStyle(fontFamily = Anton, fontSize = 26.sp, lineHeight = 30.sp, letterSpacing = 0.3.sp),
                headlineSmall = TextStyle(fontFamily = Anton, fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = 0.3.sp),
                titleLarge = TextStyle(fontFamily = Anton, fontSize = 20.sp, lineHeight = 24.sp, letterSpacing = 0.3.sp),
                titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold),
                titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold),
                bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
                bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
                bodySmall = TextStyle(fontSize = 12.5.sp, lineHeight = 17.sp),
                labelLarge = TextStyle(fontSize = 14.5.sp, fontWeight = FontWeight.Bold),
                labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
                labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
            ),
            content = content,
        )
    }
}
