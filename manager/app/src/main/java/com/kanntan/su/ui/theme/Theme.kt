package com.kanntan.su.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * KanntanSU Color Palette - Pure Black & White High-Contrast MD2 Theme
 * 
 * Theme: Material Design 2 flat design with pure black (#000000) and white (#FFFFFF)
 * High-contrast minimalist aesthetic. No shadows except explicitly specified.
 * 
 * WALLPAPER REPLACEMENT GUIDE:
 * ============================
 * To replace solid black/white areas with user-selected wallpaper:
 * 
 * 1. In HomeScreen.kt HeaderArea/FooterArea, replace:
 *    .background(PureBlack) 
 *    with:
 *    .background(wallpaperBitmap?.asImageBitmap() ?: PureBlack)
 * 
 * 2. Add import: androidx.compose.ui.graphics.asImageBitmap
 * 
 * 3. Load wallpaper Bitmap via ContentResolver or image picker
 *    and pass to HomeScreen via wallpaperBitmap parameter
 */

// region Color Definitions

/** Primary Black - Header, footer, and primary UI elements */
val PureBlack = Color(0xFF000000)

/** Primary White - Content areas and text on dark backgrounds */
val PureWhite = Color(0xFFFFFFFF)

/** Header bottom gradient start - subtle depth effect */
val DarkGrayGradientStart = Color(0xFF1C1C1C)

/** Header bottom gradient end - subtle depth effect */
val DarkGrayGradientEnd = Color(0xFF333333)

/** Separator gradient colors */
val SeparatorDarkGray = Color(0xFF4A4A4A)
val SeparatorLightGray = Color(0xFF8A8A8A)

/** Content area background - pure white */
val ContentBackground = Color(0xFFFFFFFF)

/** Text on black background */
val TextOnBlack = Color(0xFFFFFFFF)

/** Text on white background - primary */
val TextOnWhite = Color(0xFF1A1A1A)

/** Text on white background - secondary */
val TextSecondaryOnWhite = Color(0xFF666666)

// endregion

// region Material 2 Color Schemes

private val KanntanSUDarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = PureBlack,
    onPrimaryContainer = PureWhite,
    secondary = PureWhite,
    onSecondary = PureBlack,
    background = PureBlack,
    onBackground = PureWhite,
    surface = PureBlack,
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF1C1C1C),
    onSurfaceVariant = PureWhite,
    outline = Color(0xFF4A4A4A),
    error = Color(0xFFCF6679),
    onError = PureBlack
)

private val KanntanSULightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    primaryContainer = PureWhite,
    onPrimaryContainer = PureBlack,
    secondary = PureBlack,
    onSecondary = PureWhite,
    background = PureWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF1A1A1A),
    outline = Color(0xFF8A8A8A),
    error = Color(0xFFB00020),
    onError = PureWhite
)

// endregion

// region Composition Locals

/** Local color mode for theme state */
val LocalKanntanColorMode = staticCompositionLocalOf { 0 }

/** Local wallpaper enabled state */
val LocalWallpaperEnabled = staticCompositionLocalOf { false }

// endregion

// region Main Theme Composable

/**
 * KanntanSU Theme - Material Design 2 Black & White Theme
 * 
 * @param darkTheme If true, uses dark color scheme (for header/footer overlays)
 * @param wallpaperEnabled If true, header/footer accept wallpaper Bitmap
 * @param content Main content composable
 */
@Composable
fun KanntanSUTheme(
    darkTheme: Boolean = false,
    wallpaperEnabled: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KanntanSUDarkColorScheme else KanntanSULightColorScheme
    
    androidx.compose.runtime.CompositionLocalProvider(
        LocalKanntanColorMode provides if (darkTheme) 2 else 1,
        LocalWallpaperEnabled provides wallpaperEnabled
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

/**
 * Check if dark theme is active
 */
@Composable
@ReadOnlyComposable
fun isKanntanDarkTheme(): Boolean = LocalKanntanColorMode.current == 2

/**
 * Check if wallpaper mode is enabled
 */
@Composable
@ReadOnlyComposable
fun isWallpaperEnabled(): Boolean = LocalWallpaperEnabled.current

// endregion