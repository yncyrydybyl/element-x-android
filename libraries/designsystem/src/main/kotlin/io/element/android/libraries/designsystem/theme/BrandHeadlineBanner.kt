/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.colors.contentColorOnAccent
import io.element.android.compound.theme.BrandHeadline
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.LocalAccentTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * The wordmark of the accent theme in force, as a band across the top of the screen.
 *
 * Renders nothing for themes without a headline, so it can be placed unconditionally. The band is
 * filled with the accent itself and left-aligned, which is how the authorities using these
 * palettes set their own headlines.
 */
@Composable
fun BrandHeadlineBanner(modifier: Modifier = Modifier) {
    val headline = LocalAccentTheme.current.brandHeadline ?: return
    BrandHeadlineBanner(headline = headline, modifier = modifier)
}

@Composable
private fun BrandHeadlineBanner(
    headline: BrandHeadline,
    modifier: Modifier = Modifier,
) {
    val background = ElementTheme.colors.bgAccentRest
    val content = contentColorOnAccent(background)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Text(
            text = headline.title,
            style = ElementTheme.typography.fontHeadingMdBold,
            color = content,
        )
        val subtitle = headline.subtitle
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = ElementTheme.typography.fontHeadingSmRegular,
                color = content,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun BrandHeadlineBannerPreview() = ElementPreview {
    BrandHeadlineBanner(
        headline = BrandHeadline(
            title = "Kultusministerium",
            subtitle = "Baden-Württemberg",
        ),
    )
}
