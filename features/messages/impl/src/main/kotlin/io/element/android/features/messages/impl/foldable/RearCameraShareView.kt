/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.foldable

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun RearCameraShareView(
    state: RearCameraShareState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElementTheme.colors.bgCanvasDefault)
            .statusBarsPadding(),
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.screen_rear_camera_share_title),
                style = ElementTheme.typography.fontHeadingSmMedium,
                color = ElementTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.screen_rear_camera_share_subtitle),
                style = ElementTheme.typography.fontBodySmRegular,
                color = ElementTheme.colors.textSecondary,
            )
        }

        // Camera viewfinder placeholder — top 2/3
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = CompoundIcons.TakePhotoSolid(),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White.copy(alpha = 0.4f),
            )
            if (state.isRearDisplayActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(ElementTheme.colors.bgAccentRest)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.screen_rear_camera_share_live_badge),
                        style = ElementTheme.typography.fontBodyXsMedium,
                        color = Color.White,
                    )
                }
            }
        }

        // Controls — bottom 1/3
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(ElementTheme.colors.bgSubtlePrimary),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Cancel button
                IconButton(
                    onClick = { state.eventSink(RearCameraShareEvent.Cancel) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ElementTheme.colors.bgSubtleSecondary),
                ) {
                    Icon(
                        imageVector = CompoundIcons.Close(),
                        contentDescription = stringResource(R.string.a11y_rear_camera_share_cancel),
                        tint = ElementTheme.colors.textPrimary,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Capture button with pulse animation
                CaptureButton(
                    isCaptureReady = state.isCaptureReady,
                    onClick = { state.eventSink(RearCameraShareEvent.Capture) },
                )

                Spacer(modifier = Modifier.weight(1f))

                // Flash toggle button
                IconButton(
                    onClick = { state.eventSink(RearCameraShareEvent.ToggleFlash) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.isFlashEnabled) {
                                ElementTheme.colors.bgAccentRest
                            } else {
                                ElementTheme.colors.bgSubtleSecondary
                            }
                        ),
                ) {
                    Icon(
                        imageVector = CompoundIcons.Spotlight(),
                        contentDescription = stringResource(R.string.a11y_rear_camera_share_toggle_flash),
                        tint = if (state.isFlashEnabled) Color.White else ElementTheme.colors.textPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptureButton(
    isCaptureReady: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "capture_pulse")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "capture_scale",
    )
    val scale = if (isCaptureReady) animatedScale else 1.0f

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(72.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(ElementTheme.colors.bgAccentRest),
    ) {
        Icon(
            imageVector = CompoundIcons.TakePhotoSolid(),
            contentDescription = stringResource(R.string.a11y_rear_camera_share_capture),
            modifier = Modifier.size(32.dp),
            tint = Color.White,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RearCameraShareViewPreview(
    @PreviewParameter(RearCameraShareStateProvider::class) state: RearCameraShareState,
) = ElementPreview {
    RearCameraShareView(state = state)
}
