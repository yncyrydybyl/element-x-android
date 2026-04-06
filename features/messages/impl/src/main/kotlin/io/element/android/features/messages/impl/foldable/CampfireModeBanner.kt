/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.foldable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * A banner shown at the top of the messages view when the device is in tent/half-open mode,
 * indicating that both screens are active (Campfire Mode).
 *
 * @param onDismiss Called when the user taps the dismiss (X) button.
 * @param modifier Modifier for the banner.
 */
@Composable
internal fun CampfireModeBanner(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bannerBackground = ElementTheme.colors.bgAccentRest.copy(alpha = 0.15f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bannerBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = CompoundIcons.UserProfileSolid(),
            contentDescription = null,
            tint = ElementTheme.colors.textPrimary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Campfire Mode — both screens active",
            style = ElementTheme.typography.fontBodySmMedium,
            color = ElementTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = CompoundIcons.Close(),
                contentDescription = "Dismiss Campfire Mode banner",
                tint = ElementTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

internal class CampfireModeBannerPreviewProvider : PreviewParameterProvider<Unit> {
    override val values: Sequence<Unit> = sequenceOf(Unit)
}

@PreviewsDayNight
@Composable
internal fun CampfireModeBannerPreview(
    @PreviewParameter(CampfireModeBannerPreviewProvider::class) state: Unit,
) = ElementPreview {
    CampfireModeBanner(onDismiss = {})
}
