/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * A layout used in tabletop mode (foldable device half-opened with horizontal hinge).
 * Splits the available space into two halves:
 * - Top: camera/video call placeholder (shown above the hinge).
 * - Bottom: the chat content slot (shown below the hinge).
 *
 * @param chatContent Composable content for the lower half (timeline + composer).
 * @param modifier Modifier applied to the root container.
 */
@Composable
fun TabletopMessagesLayout(
    chatContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Top half — video call placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(ElementTheme.colors.bgCanvasDefault),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = CompoundIcons.VideoCall(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = ElementTheme.colors.iconSecondary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.screen_room_tabletop_video_placeholder_title),
                    style = ElementTheme.typography.fontBodyLgMedium,
                    color = ElementTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.screen_room_tabletop_video_placeholder_subtitle),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Bottom half — chat timeline and composer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            chatContent()
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TabletopMessagesLayoutPreview() = ElementPreview {
    TabletopMessagesLayout(
        chatContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ElementTheme.colors.bgSubtleSecondary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Chat timeline and composer",
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
    )
}
