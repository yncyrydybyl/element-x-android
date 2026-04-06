/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.foldable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * The "second screen" view for Campfire Mode — conceptually shown on the cover display for the
 * person sitting across from the primary user.
 *
 * The entire content is rotated 180° so it reads naturally when the device is in tent mode and
 * a second person is sitting on the opposite side.
 *
 * This is a stub layout: the actual cover-display API integration is not yet implemented.
 * The [timeline] slot renders a read-only representation of the shared timeline.
 *
 * @param timeline Composable slot for the shared room timeline content.
 * @param onSendMessage Called when the second user sends a message from this view.
 * @param modifier Modifier applied to the outermost container.
 */
@Composable
internal fun CampfireModeView(
    timeline: @Composable () -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Rotate 180° so the content is readable for the person on the opposite side.
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(rotationZ = 180f),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Stub header indicating second-screen context.
            CampfireModeSecondScreenHeader()

            // Timeline content slot — fills remaining space.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                timeline()
            }

            // Simplified compose bar for the second user.
            CampfireModeComposeBar(onSendMessage = onSendMessage)
        }
    }
}

@Composable
private fun CampfireModeSecondScreenHeader(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ElementTheme.colors.bgSubtleSecondary)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = CompoundIcons.UserProfileSolid(),
            contentDescription = null,
            tint = ElementTheme.colors.iconSecondary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Second screen — Campfire Mode",
            style = ElementTheme.typography.fontBodySmMedium,
            color = ElementTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun CampfireModeComposeBar(
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    val textColor = ElementTheme.colors.textPrimary
    val hintColor = ElementTheme.colors.textDisabled
    val borderColor = ElementTheme.colors.borderInteractiveSecondary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ElementTheme.colors.bgCanvasDefault)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = ElementTheme.colors.bgSubtleSecondary,
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            if (text.isEmpty()) {
                Text(
                    text = "Message…",
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = hintColor,
                )
            }
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = ElementTheme.typography.fontBodyMdRegular.copy(color = textColor),
                cursorBrush = SolidColor(ElementTheme.colors.iconAccentTertiary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendMessage(text)
                    text = ""
                }
            },
        ) {
            Icon(
                imageVector = CompoundIcons.SendSolid(),
                contentDescription = "Send",
                tint = if (text.isNotBlank()) {
                    ElementTheme.colors.iconAccentTertiary
                } else {
                    ElementTheme.colors.iconDisabled
                },
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun CampfireModeViewPreview() = ElementPreview {
    CampfireModeView(
        timeline = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ElementTheme.colors.bgCanvasDefault),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Timeline placeholder",
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        },
        onSendMessage = {},
    )
}

@PreviewsDayNight
@Composable
internal fun CampfireModeComposeBarPreview() = ElementPreview {
    Column {
        CampfireModeComposeBar(onSendMessage = {})
        Spacer(modifier = Modifier.height(8.dp))
        CampfireModeSecondScreenHeader()
    }
}
