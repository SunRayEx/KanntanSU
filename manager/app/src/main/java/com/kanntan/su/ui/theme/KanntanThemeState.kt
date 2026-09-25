package com.kanntan.su.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.material.SwitchColors
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * KanntanSU customizable color palette.
 *
 * The signature look is pure black & white. Every color below can be customized
 * from the "自定义主题" screen; [DefaultKanntanColors] is the stock black/white pair.
 *
 * - [topColor]      : home top (header) button section
 * - [middleColor]   : home middle content section background
 * - [bottomColor]   : home bottom (footer) button section
 * - [primaryColor]  : theme color #1, replaces "black" across the app
 * - [secondaryColor]: theme color #2, replaces "white" across the app
 */
data class KanntanColors(
    val topColor: Color,
    val middleColor: Color,
    val bottomColor: Color,
    val primaryColor: Color,
    val secondaryColor: Color,
) {
    /** Text/foreground color that stays readable on top of [topColor]. */
    val onTopColor: Color get() = contrastingOn(topColor)

    /** Text/foreground color that stays readable on top of [middleColor]. */
    val onMiddleColor: Color get() = contrastingOn(middleColor)

    /** Text/foreground color that stays readable on top of [bottomColor]. */
    val onBottomColor: Color get() = contrastingOn(bottomColor)

    /** Text/foreground color that stays readable on top of [primaryColor]. */
    val onPrimaryColor: Color get() = contrastingOn(primaryColor)

    /** Text/foreground color that stays readable on top of [secondaryColor]. */
    val onSecondaryColor: Color get() = contrastingOn(secondaryColor)
}

/** Stock black & white palette. */
val DefaultKanntanColors = KanntanColors(
    topColor = PureBlack,
    middleColor = PureWhite,
    bottomColor = PureBlack,
    primaryColor = PureBlack,
    secondaryColor = PureWhite,
)

/**
 * User-picked theme images for deeper customization.
 *
 * Both hold an absolute path into app-private storage, or `null` to fall back
 * to the matching [KanntanColors] slot:
 * - [backgroundImagePath] replaces the home content background ([KanntanColors.middleColor])
 * - [topImagePath] replaces the whole top section of the home page ([KanntanColors.topColor])
 */
data class KanntanImages(
    val backgroundImagePath: String? = null,
    val topImagePath: String? = null,
)

/** Pick a dark-ish or white-ish foreground for a background, keeping the high-contrast feel. */
fun contrastingOn(background: Color): Color =
    if (background.luminance() > 0.5f) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)

/**
 * Holds and persists the user-customizable theme colors.
 *
 * Colors are exposed as a [StateFlow] so any Compose screen that collects
 * [colors] re-renders immediately when the user picks a new color.
 */
class KanntanThemeState(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _colors = MutableStateFlow(load())
    val colors: StateFlow<KanntanColors> = _colors.asStateFlow()

    private val _images = MutableStateFlow(loadImages())
    val images: StateFlow<KanntanImages> = _images.asStateFlow()

    private fun load(): KanntanColors = KanntanColors(
        topColor = prefs.color(KEY_TOP, DefaultKanntanColors.topColor),
        middleColor = prefs.color(KEY_MIDDLE, DefaultKanntanColors.middleColor),
        bottomColor = prefs.color(KEY_BOTTOM, DefaultKanntanColors.bottomColor),
        primaryColor = prefs.color(KEY_PRIMARY, DefaultKanntanColors.primaryColor),
        secondaryColor = prefs.color(KEY_SECONDARY, DefaultKanntanColors.secondaryColor),
    )

    private fun loadImages(): KanntanImages = KanntanImages(
        backgroundImagePath = prefs.getString(KEY_BG_IMAGE, null),
        topImagePath = prefs.getString(KEY_TOP_IMAGE, null),
    )

    fun setTopColor(color: Color) = update(KEY_TOP, color) { it.copy(topColor = color) }
    fun setMiddleColor(color: Color) = update(KEY_MIDDLE, color) { it.copy(middleColor = color) }
    fun setBottomColor(color: Color) = update(KEY_BOTTOM, color) { it.copy(bottomColor = color) }
    fun setPrimaryColor(color: Color) = update(KEY_PRIMARY, color) { it.copy(primaryColor = color) }
    fun setSecondaryColor(color: Color) = update(KEY_SECONDARY, color) { it.copy(secondaryColor = color) }

    fun setBackgroundImage(path: String?) = updateImage(KEY_BG_IMAGE, path) {
        it.copy(backgroundImagePath = path)
    }

    fun setTopImage(path: String?) = updateImage(KEY_TOP_IMAGE, path) {
        it.copy(topImagePath = path)
    }

    private fun update(key: String, color: Color, block: (KanntanColors) -> KanntanColors) {
        prefs.edit().putInt(key, color.value.toInt()).apply()
        _colors.update(block)
    }

    private fun updateImage(key: String, path: String?, block: (KanntanImages) -> KanntanImages) {
        val edit = prefs.edit()
        if (path == null) edit.remove(key) else edit.putString(key, path)
        edit.apply()
        _images.update(block)
    }

    /** Restore the stock black & white palette and drop any custom images. */
    fun reset() {
        prefs.edit().clear().apply()
        _colors.value = DefaultKanntanColors
        _images.value = KanntanImages()
    }

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_TOP = "top_color"
        private const val KEY_MIDDLE = "middle_color"
        private const val KEY_BOTTOM = "bottom_color"
        private const val KEY_PRIMARY = "primary_color"
        private const val KEY_SECONDARY = "secondary_color"
        private const val KEY_BG_IMAGE = "background_image"
        private const val KEY_TOP_IMAGE = "top_image"

        private fun SharedPreferences.color(key: String, default: Color): Color {
            val value = getInt(key, DEFAULT_SENTINEL)
            return if (value == DEFAULT_SENTINEL) default else Color(value)
        }

        private const val DEFAULT_SENTINEL = Int.MIN_VALUE
    }
}

/** Provides the app-wide [KanntanThemeState] to the composition tree. */
val LocalKanntanTheme = compositionLocalOf<KanntanThemeState> {
    error("KanntanThemeState is not provided. Wrap the tree in CompositionLocalProvider.")
}

/** Current customized colors; re-composes when the user changes them. */
@Composable
fun kanntanColors(): KanntanColors = LocalKanntanTheme.current.colors.collectAsState().value

/** Current customized images; re-composes when the user picks or clears one. */
@Composable
fun kanntanImages(): KanntanImages = LocalKanntanTheme.current.images.collectAsState().value

/**
 * Decode a user-picked theme image off the main thread.
 *
 * Returns `null` while loading and permanently if [path] is null/unreadable, so
 * callers can fall back to the matching theme color.
 */
@Composable
fun rememberThemeImage(path: String?): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(path) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        bitmap = if (path == null) {
            null
        } else {
            withContext(Dispatchers.IO) { decodeSampledImage(context, path) }
        }
    }
    return bitmap
}

/**
 * Foreground color that stays readable over an optional theme image.
 *
 * A fixed theme color would vanish against an arbitrary photo, so when [path] is set the
 * text follows the image: [imageLuminance] already folds in how much of the picture is
 * white/bright, so a light photo yields near-black text and a dark one yields white.
 * Without an image this is just [fallback].
 */
@Composable
fun rememberImageAwareForeground(path: String?, fallback: Color): Color {
    val context = LocalContext.current
    var color by remember(path) { mutableStateOf(fallback) }
    LaunchedEffect(path) {
        color = if (path == null) {
            fallback
        } else {
            val luminance = withContext(Dispatchers.IO) { imageLuminance(context, path) }
            if (luminance > LUMINANCE_TEXT_THRESHOLD) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
        }
    }
    return color
}

/**
 * Average luminance (0..1) of a stored theme image, sampled from a tiny 32x32 thumbnail
 * so it is cheap enough for a background thread. White pixels push the average up, so
 * this doubles as a "how much of the image is light" measure.
 */
fun imageLuminance(context: Context, path: String): Float {
    return runCatching {
        val source = decodeSampledBitmap(context, path) ?: return 0f
        val small = Bitmap.createScaledBitmap(source, 32, 32, true)
        if (small !== source) source.recycle()

        var sum = 0.0
        for (y in 0 until small.height) {
            for (x in 0 until small.width) {
                val px = small.getPixel(x, y)
                val r = ((px shr 16) and 0xFF) / 255.0
                val g = ((px shr 8) and 0xFF) / 255.0
                val b = (px and 0xFF) / 255.0
                sum += 0.299 * r + 0.587 * g + 0.114 * b
            }
        }
        val pixels = small.width * small.height
        small.recycle()
        (sum / pixels).toFloat()
    }.getOrDefault(0f)
}

/**
 * Decode [path] (app-private file or a persisted content uri) as an [ImageBitmap],
 * sub-sampled so a multi-megapixel photo can't OOM the app.
 */
private fun decodeSampledImage(context: Context, path: String): ImageBitmap? =
    decodeSampledBitmap(context, path)?.asImageBitmap()

/**
 * Same decoding as [decodeSampledImage] but returns a platform [Bitmap], e.g. for
 * running it through the Android Palette API.
 */
fun decodeSampledBitmap(context: Context, path: String): Bitmap? {
    return runCatching {
        // Prefer our own private copy; fall back to a content uri if that's what was stored.
        val isFile = File(path).isFile
        fun openStream(): InputStream? =
            if (isFile) File(path).inputStream()
            else context.contentResolver.openInputStream(Uri.parse(path))

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openStream()?.use { BitmapFactory.decodeStream(it, null, bounds) }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(
                bounds.outWidth.takeIf { it > 0 } ?: 1,
                bounds.outHeight.takeIf { it > 0 } ?: 1,
            )
        }
        // Re-open: decodeStream consumed the stream during the bounds pass.
        openStream()?.use { BitmapFactory.decodeStream(it, null, options) }
    }.getOrNull()
}

/** Sub-sample so the long edge is at most ~2x the target, to bound memory use. */
private fun calculateSampleSize(width: Int, height: Int): Int {
    val target = 1440
    var sample = 1
    while ((width / (sample * 2)) >= target && (height / (sample * 2)) >= target) {
        sample *= 2
    }
    return sample
}

/**
 * Material Design 2 [SwitchColors] tuned for the Kanntan palette.
 *
 * The off-state track uses a fixed neutral gray instead of a translucent
 * [KanntanColors.secondaryColor]: with the stock black/white theme a
 * translucent white track blends into the white card background and the
 * toggle looks like a bare dot floating on the card.
 */
@Composable
fun kanntanSwitchColors(colors: KanntanColors = kanntanColors()): SwitchColors =
    SwitchDefaults.colors(
        checkedThumbColor = colors.onPrimaryColor,
        checkedTrackColor = colors.primaryColor,
        uncheckedThumbColor = colors.onPrimaryColor,
        uncheckedTrackColor = NeutralSwitchTrack,
        disabledCheckedThumbColor = colors.onPrimaryColor.copy(alpha = 0.55f),
        disabledCheckedTrackColor = colors.primaryColor.copy(alpha = 0.38f),
        disabledUncheckedThumbColor = colors.onPrimaryColor.copy(alpha = 0.55f),
        disabledUncheckedTrackColor = NeutralSwitchTrack.copy(alpha = 0.38f),
    )

/** Neutral gray that stays visible on both the light and the dark theme colors. */
private val NeutralSwitchTrack = Color(0xFF9E9E9E)

/**
 * Images whose average luminance is above this are treated as "bright" (mostly light
 * pixels), so text drawn over them should be dark rather than white.
 */
private const val LUMINANCE_TEXT_THRESHOLD = 0.55f
