/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.colors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import io.element.android.compound.theme.AccentPalette
import io.element.android.compound.theme.AccentTheme
import io.element.android.compound.tokens.generated.SemanticColors

/** Alpha values Compound uses for the translucent accent tokens. */
private const val ALPHA_SELECTED = 0.23f
private const val ALPHA_SUBTLE = 0.12f
private const val ALPHA_BORDER_SUBTLE = 0.50f
private val SUBTLE_GRADIENT_ALPHAS = listOf(0.41f, 0.23f, 0.11f, 0.06f, 0.02f)

/**
 * Above this relative luminance an accent counts as light, and content drawn on top of it has to
 * be dark rather than white.
 */
private const val LIGHT_ACCENT_LUMINANCE = 0.5f

/** Near-black, matching Compound's primary text colour on a light background. */
private val DARK_CONTENT = Color(0xFF1B1D22)

/**
 * The colour to draw text or icons in when they sit directly on a surface filled with [accent].
 *
 * Deliberately near-black or white rather than a darker shade of the accent itself: two shades of
 * the same bright hue (dark yellow on yellow, say) do not separate well enough to read.
 */
fun contentColorOnAccent(accent: Color): Color {
    return if (accent.luminance() > LIGHT_ACCENT_LUMINANCE) DARK_CONTENT else Color.White
}

/**
 * Re-colours the accent tokens of both palettes for [accentTheme].
 *
 * Any other colour override already applied to the receiver (for instance an enterprise brand
 * palette) is preserved, so this can be layered on top of it.
 */
fun SemanticColorsLightDark.withAccent(accentTheme: AccentTheme): SemanticColorsLightDark {
    val palette = accentTheme.palette ?: return this
    return SemanticColorsLightDark(
        light = light.withAccent(palette),
        dark = dark.withAccent(palette),
    )
}

/**
 * Derives every accent token from [palette], keeping the rest of the palette untouched.
 *
 * The mapping is luminance-aware in two places, so that a palette only has to declare its brand
 * shades and still stays legible: content on the canvas uses the darkened shade in light theme and
 * the brand colour in dark theme, while content drawn on a filled accent surface flips between
 * white and the darkest shade depending on how bright the accent itself is.
 */
fun SemanticColors.withAccent(palette: AccentPalette): SemanticColors {
    val onCanvas = if (isLight) palette.onLight else palette.rest
    val onCanvasSubtle = if (isLight) palette.onLightSubtle else palette.hovered
    val onAccent = contentColorOnAccent(palette.rest)
    // The action gradient runs bright to dark on light backgrounds and dark to bright on dark ones.
    val actionGradient = listOf(palette.bright, palette.rest, palette.hovered, palette.pressed)
        .let { if (isLight) it else it.reversed() }
    return copy(
        bgAccentRest = palette.rest,
        bgAccentHovered = palette.hovered,
        bgAccentPressed = palette.pressed,
        bgAccentSelected = palette.rest.copy(alpha = ALPHA_SELECTED),
        bgAccentSubtle = palette.rest.copy(alpha = ALPHA_SUBTLE),
        bgBadgeAccent = palette.rest,
        borderAccentPrimary = palette.rest,
        borderAccentSubtle = palette.rest.copy(alpha = ALPHA_BORDER_SUBTLE),
        iconAccentPrimary = onCanvas,
        iconAccentTertiary = onCanvasSubtle,
        textActionAccent = onCanvas,
        textBadgeAccent = onAccent,
        gradientActionStop1 = actionGradient[0],
        gradientActionStop2 = actionGradient[1],
        gradientActionStop3 = actionGradient[2],
        gradientActionStop4 = actionGradient[3],
        gradientSubtleStop1 = palette.rest.copy(alpha = SUBTLE_GRADIENT_ALPHAS[0]),
        gradientSubtleStop2 = palette.rest.copy(alpha = SUBTLE_GRADIENT_ALPHAS[1]),
        gradientSubtleStop3 = palette.rest.copy(alpha = SUBTLE_GRADIENT_ALPHAS[2]),
        gradientSubtleStop4 = palette.rest.copy(alpha = SUBTLE_GRADIENT_ALPHAS[3]),
        gradientSubtleStop5 = palette.rest.copy(alpha = SUBTLE_GRADIENT_ALPHAS[4]),
    )
}
