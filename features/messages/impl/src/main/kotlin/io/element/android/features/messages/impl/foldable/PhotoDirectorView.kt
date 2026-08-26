/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.foldable

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField

// Accent colors matching the yellow customization (#FFED00)
private val AccentStart = Color(0xFFFFED00)
private val AccentEnd = Color(0xFFFFC107)

@Composable
fun PhotoDirectorView(
    state: PhotoDirectorState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElementTheme.colors.bgCanvasDefault),
    ) {
        // Top 60%: Camera viewfinder area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(Color(0xFF1A1A1A)),
        ) {
            // Rule-of-thirds grid lines overlay
            RuleOfThirdsGrid(modifier = Modifier.fillMaxSize())

            // Center camera icon + tap-to-capture prompt
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = CompoundIcons.TakePhoto(),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp),
                )
                Text(
                    text = "Tap to capture",
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { state.eventSink(PhotoDirectorEvent.Capture) },
                )
            }

            // Cover screen mirroring badge — top-right corner
            if (state.isCoverScreenMirroring) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = CompoundIcons.ShareScreen(),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Cover screen mirroring",
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = Color.White,
                    )
                }
            }
        }

        // Bottom 40%: Chat compose area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .background(ElementTheme.colors.bgSubtlePrimary)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Photo thumbnail strip
            if (state.capturedPhotos.isNotEmpty()) {
                PhotoThumbnailStrip(
                    photos = state.capturedPhotos,
                    onRemove = { index -> state.eventSink(PhotoDirectorEvent.RemovePhoto(index)) },
                )
            }

            // Caption text field
            TextField(
                value = state.caption,
                onValueChange = { state.eventSink(PhotoDirectorEvent.UpdateCaption(it)) },
                placeholder = "Add a caption…",
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
            )

            // Action row: Cancel + Send
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Cancel",
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                    modifier = Modifier.clickable { state.eventSink(PhotoDirectorEvent.Cancel) },
                )

                // Send button with accent gradient
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AccentStart, AccentEnd),
                            )
                        )
                        .clickable(enabled = state.capturedPhotos.isNotEmpty()) {
                            state.eventSink(PhotoDirectorEvent.Send)
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = CompoundIcons.Send(),
                            contentDescription = "Send",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Send",
                            style = ElementTheme.typography.fontBodyMdMedium,
                            color = Color.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleOfThirdsGrid(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val lineColor = Color.White.copy(alpha = 0.3f)
        val strokeWidth = 1.dp.toPx()

        // 2 horizontal lines at 1/3 and 2/3
        drawLine(
            color = lineColor,
            start = Offset(0f, size.height / 3f),
            end = Offset(size.width, size.height / 3f),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = lineColor,
            start = Offset(0f, size.height * 2f / 3f),
            end = Offset(size.width, size.height * 2f / 3f),
            strokeWidth = strokeWidth,
        )

        // 2 vertical lines at 1/3 and 2/3
        drawLine(
            color = lineColor,
            start = Offset(size.width / 3f, 0f),
            end = Offset(size.width / 3f, size.height),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = lineColor,
            start = Offset(size.width * 2f / 3f, 0f),
            end = Offset(size.width * 2f / 3f, size.height),
            strokeWidth = strokeWidth,
        )
    }
}

@Composable
private fun PhotoThumbnailStrip(
    photos: List<Uri>,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        itemsIndexed(photos) { index, _ ->
            Box(
                modifier = Modifier.size(64.dp),
            ) {
                // Photo placeholder (would be AsyncImage with real CameraX output)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3A3A3A))
                        .border(
                            width = 1.dp,
                            color = ElementTheme.colors.borderInteractivePrimary,
                            shape = RoundedCornerShape(8.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = CompoundIcons.Image(),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp),
                    )
                }

                // Remove (X) button overlay
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { onRemove(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = CompoundIcons.Close(),
                        contentDescription = "Remove photo",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun PhotoDirectorViewPreview(
    @PreviewParameter(PhotoDirectorStateProvider::class) state: PhotoDirectorState,
) = ElementPreview {
    PhotoDirectorView(state = state)
}
