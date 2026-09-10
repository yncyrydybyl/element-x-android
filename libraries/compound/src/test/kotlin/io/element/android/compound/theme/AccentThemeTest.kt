/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.compound.colors.contentColorOnAccent
import io.element.android.compound.colors.withAccent
import io.element.android.compound.tokens.generated.compoundColorsDark
import io.element.android.compound.tokens.generated.compoundColorsLight
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AccentThemeTest {
    @Test
    fun `mapToAccentTheme - a stored name resolves to its theme`() = runTest {
        assertThat(flowOf(AccentTheme.Yellow.name).mapToAccentTheme().first())
            .isEqualTo(AccentTheme.Yellow)
        assertThat(flowOf(AccentTheme.HackmasCastle.name).mapToAccentTheme().first())
            .isEqualTo(AccentTheme.HackmasCastle)
    }

    @Test
    fun `mapToAccentTheme - no stored value falls back to the default theme`() = runTest {
        assertThat(flowOf(null).mapToAccentTheme().first()).isEqualTo(AccentTheme.Default)
    }

    @Test
    fun `mapToAccentTheme - an unknown name falls back to the default theme`() = runTest {
        assertThat(flowOf("ATrappedThemeName").mapToAccentTheme().first()).isEqualTo(AccentTheme.Default)
    }

    @Test
    fun `withAccent - the default theme leaves the palettes untouched`() {
        val colors = SemanticColorsLightDark.default
        assertThat(colors.withAccent(AccentTheme.Default)).isSameInstanceAs(colors)
    }

    @Test
    fun `withAccent - accent tokens follow the palette and other tokens do not`() {
        val palette = AccentTheme.HackmasCastle.palette!!
        val light = compoundColorsLight.withAccent(palette)

        assertThat(light.bgAccentRest).isEqualTo(palette.rest)
        assertThat(light.bgAccentHovered).isEqualTo(palette.hovered)
        assertThat(light.bgAccentPressed).isEqualTo(palette.pressed)
        assertThat(light.borderAccentPrimary).isEqualTo(palette.rest)
        assertThat(light.iconAccentPrimary).isEqualTo(palette.onLight)
        // Colours outside the accent family must survive, including the success green.
        assertThat(light.bgCanvasDefault).isEqualTo(compoundColorsLight.bgCanvasDefault)
        assertThat(light.textSuccessPrimary).isEqualTo(compoundColorsLight.textSuccessPrimary)
        assertThat(light.isLight).isTrue()
    }

    @Test
    fun `withAccent - text and icons on the canvas differ between light and dark`() {
        val palette = AccentTheme.Yellow.palette!!
        val light = compoundColorsLight.withAccent(palette)
        val dark = compoundColorsDark.withAccent(palette)

        // On a light canvas the darkened shade is used, on a dark one the brand colour itself.
        assertThat(light.textActionAccent).isEqualTo(palette.onLight)
        assertThat(dark.textActionAccent).isEqualTo(palette.rest)
    }

    @Test
    fun `withAccent - the action gradient is reversed in dark theme`() {
        val palette = AccentTheme.Yellow.palette!!
        val light = compoundColorsLight.withAccent(palette)
        val dark = compoundColorsDark.withAccent(palette)

        assertThat(light.gradientActionStop1).isEqualTo(palette.bright)
        assertThat(light.gradientActionStop4).isEqualTo(palette.pressed)
        assertThat(dark.gradientActionStop1).isEqualTo(palette.pressed)
        assertThat(dark.gradientActionStop4).isEqualTo(palette.bright)
    }

    @Test
    fun `brandHeadline - only the Kultusministerium theme carries a wordmark`() {
        assertThat(AccentTheme.KultusministeriumBW.brandHeadline?.title).isEqualTo("Kultusministerium")
        assertThat(AccentTheme.KultusministeriumBW.brandHeadline?.subtitle).isEqualTo("Baden-Württemberg")
        assertThat(AccentTheme.Default.brandHeadline).isNull()
        assertThat(AccentTheme.Yellow.brandHeadline).isNull()
        assertThat(AccentTheme.HackmasCastle.brandHeadline).isNull()
    }

    @Test
    fun `every theme other than the default carries a palette`() {
        AccentTheme.entries.filter { it != AccentTheme.Default }.forEach {
            assertThat(it.palette).isNotNull()
        }
    }

    @Test
    fun `contentColorOnAccent - dark content on bright accents, white on dark ones`() {
        // Yellow is bright, so content on top of it has to be dark.
        assertThat(contentColorOnAccent(AccentTheme.Yellow.palette!!.rest)).isNotEqualTo(Color.White)
        // The Håck ma's Castle purple is dark enough to carry white content.
        assertThat(contentColorOnAccent(AccentTheme.HackmasCastle.palette!!.rest)).isEqualTo(Color.White)
        // The Baden-Württemberg lemon yellow is brighter still.
        assertThat(contentColorOnAccent(AccentTheme.KultusministeriumBW.palette!!.rest)).isNotEqualTo(Color.White)
    }
}
