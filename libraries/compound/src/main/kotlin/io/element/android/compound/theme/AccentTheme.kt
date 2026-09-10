/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The brand shades an [AccentTheme] is built from.
 *
 * A palette only describes the accent hue; everything else (canvas, text, borders) keeps coming
 * from Compound. The individual accent tokens are derived from these shades in
 * `SemanticColors.withAccent`, so a new theme never has to touch the generated token files.
 */
data class AccentPalette(
    /** The brand colour itself: filled buttons, borders, badges, selected states. */
    val rest: Color,
    /** One step darker than [rest], for hovered states and the middle of the action gradient. */
    val hovered: Color,
    /** Two steps darker than [rest], for pressed states and the dark end of the action gradient. */
    val pressed: Color,
    /** One step lighter than [rest], for the bright end of the action gradient. */
    val bright: Color,
    /** A shade dark enough to read as text or an icon on a light canvas. */
    val onLight: Color,
    /** A softer [onLight], for tertiary icons. */
    val onLightSubtle: Color,
)

/**
 * A wordmark a theme can show above the onboarding screen.
 *
 * These are institution names rather than prose, so they are not translated.
 */
data class BrandHeadline(
    val title: String,
    val subtitle: String? = null,
)

/**
 * The accent colour the user picked for the app.
 *
 * [Default] keeps the colours exactly as Compound ships them, which is why its palette is `null`:
 * the accent tokens are then left untouched rather than re-stated here, so Element's own brand
 * changes still arrive through the generated token files.
 */
enum class AccentTheme(
    val palette: AccentPalette?,
    val brandHeadline: BrandHeadline? = null,
) {
    Default(palette = null),

    Yellow(
        palette = AccentPalette(
            rest = Color(0xFFFFED00),
            hovered = Color(0xFFE6D500),
            pressed = Color(0xFFCCBD00),
            bright = Color(0xFFFFFF4D),
            onLight = Color(0xFF8A7800),
            onLightSubtle = Color(0xFFA39000),
        )
    ),

    /** The palette of the Håck ma's Castle event (hack-mas.at). */
    HackmasCastle(
        palette = AccentPalette(
            rest = Color(0xFFA77AFF),
            hovered = Color(0xFF9260F5),
            pressed = Color(0xFF7E46EB),
            bright = Color(0xFFC4A3FF),
            onLight = Color(0xFF6B3FD6),
            onLightSubtle = Color(0xFF8659E6),
        )
    ),

    /**
     * The corporate design of the state of Baden-Württemberg: its lemon yellow paired with a warm
     * near-black, as published for its authorities on corporate-design-bw.de.
     */
    KultusministeriumBW(
        palette = AccentPalette(
            rest = Color(0xFFFFFC00),
            hovered = Color(0xFFE6E300),
            pressed = Color(0xFFCCCA00),
            bright = Color(0xFFFFFF66),
            onLight = Color(0xFF8A8700),
            onLightSubtle = Color(0xFFA3A000),
        ),
        brandHeadline = BrandHeadline(
            title = "Kultusministerium",
            subtitle = "Baden-Württemberg",
        ),
    ),
}

/** The accent theme in force, so that themed chrome outside of the colours can react to it. */
val LocalAccentTheme = staticCompositionLocalOf { AccentTheme.Default }

/**
 * Maps the persisted accent theme name to an [AccentTheme].
 *
 * Unknown names fall back to [AccentTheme.Default] so that a build which no longer offers a
 * previously selected theme still starts.
 */
fun Flow<String?>.mapToAccentTheme(): Flow<AccentTheme> = map { storedName ->
    AccentTheme.entries.firstOrNull { it.name == storedName } ?: AccentTheme.Default
}
