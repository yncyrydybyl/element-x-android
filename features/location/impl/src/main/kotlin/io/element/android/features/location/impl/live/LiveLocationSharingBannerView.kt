/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.location.impl.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LiveLocationSharingBannerView(
    state: LiveLocationSharingBannerState,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = state.isVisible,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = ElementTheme.colors.bgCanvasDefaultLevel1,
            shadowElevation = 8.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = CompoundIcons.LocationPinSolid(),
                    contentDescription = null,
                    tint = ElementTheme.colors.iconAccentPrimary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.screen_live_location_banner_title),
                    style = ElementTheme.typography.fontBodyMdMedium,
                    color = ElementTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    text = stringResource(CommonStrings.action_stop),
                    onClick = { state.eventSink(LiveLocationSharingBannerEvents.Stop) },
                    size = ButtonSize.Small,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun LiveLocationSharingBannerViewPreview() = ElementPreview {
    LiveLocationSharingBannerView(
        state = LiveLocationSharingBannerState(
            isVisible = true,
            eventSink = {},
        ),
    )
}
