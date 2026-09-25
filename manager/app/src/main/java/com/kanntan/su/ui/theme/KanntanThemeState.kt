package com.kanntan.su.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.SwitchColors
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    private fun load(): KanntanColors = KanntanColors(
        topColor = prefs.color(KEY_TOP, DefaultKanntanColors.topColor),
        middleColor = prefs.color(KEY_MIDDLE, DefaultKanntanColors.middleColor),
        bottomColor = prefs.color(KEY_BOTTOM, DefaultKanntanColors.bottomColor),
        primaryColor = prefs.color(KEY_PRIMARY, DefaultKanntanColors.primaryColor),
        secondaryColor = prefs.color(KEY_SECONDARY, DefaultKanntanColors.secondaryColor),
    )

    fun setTopColor(color: Color) = update(KEY_TOP, color) { it.copy(topColor = color) }
    fun setMiddleColor(color: Color) = update(KEY_MIDDLE, color) { it.copy(middleColor = color) }
    fun setBottomColor(color: Color) = update(KEY_BOTTOM, color) { it.copy(bottomColor = color) }
    fun setPrimaryColor(color: Color) = update(KEY_PRIMARY, color) { it.copy(primaryColor = color) }
    fun setSecondaryColor(color: Color) = update(KEY_SECONDARY, color) { it.copy(secondaryColor = color) }

    private fun update(key: String, color: Color, block: (KanntanColors) -> KanntanColors) {
        prefs.edit().putInt(key, color.value.toInt()).apply()
        _colors.update(block)
    }

    /** Restore the stock black & white palette. */
    fun reset() {
        prefs.edit().clear().apply()
        _colors.value = DefaultKanntanColors
    }

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_TOP = "top_color"
        private const val KEY_MIDDLE = "middle_color"
        private const val KEY_BOTTOM = "bottom_color"
        private const val KEY_PRIMARY = "primary_color"
        private const val KEY_SECONDARY = "secondary_color"

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
