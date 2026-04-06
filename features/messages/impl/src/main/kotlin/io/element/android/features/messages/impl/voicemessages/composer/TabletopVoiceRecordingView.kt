/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.composer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerEvent
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentWithAttachment
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration

/**
 * Split-screen voice recording layout for foldable devices in tabletop mode.
 *
 * The top half shows recent timeline messages as context; the bottom half
 * shows recording controls. A fold-to-send gesture (closing the hinge while
 * recording is active) triggers [VoiceMessageComposerEvent.SendVoiceMessage].
 */
@Composable
fun TabletopVoiceRecordingView(
    voiceMessageComposerState: VoiceMessageComposerState,
    contextMessages: ImmutableList<TimelineItem>,
    modifier: Modifier = Modifier,
) {
    val recordingState = voiceMessageComposerState.voiceMessageState as? VoiceMessageState.Recording

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElementTheme.colors.bgCanvasDefault),
    ) {
        // Top panel — recent timeline items for context
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(ElementTheme.colors.bgSubtleSecondary),
        ) {
            if (contextMessages.isEmpty()) {
                Text(
                    text = stringResource(CommonStrings.common_no_results),
                    modifier = Modifier.align(Alignment.Center),
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    textAlign = TextAlign.Center,
                )
            } else {
                val listState = rememberLazyListState()
                LaunchedEffect(contextMessages.size) {
                    if (contextMessages.isNotEmpty()) {
                        listState.scrollToItem(contextMessages.size - 1)
                    }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(contextMessages, key = { it.identifier }) { item ->
                        TabletopContextMessageRow(item)
                    }
                }
            }
        }

        // Hinge / divider line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(ElementTheme.colors.borderDisabled),
        )

        // Bottom panel — recording controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Duration indicator
            val duration: Duration = recordingState?.duration ?: Duration.ZERO
            Text(
                text = formatDuration(duration),
                style = ElementTheme.typography.fontHeadingMdBold,
                color = ElementTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Waveform visualiser (live audio levels)
            WaveformVisualiser(
                levels = recordingState?.levels ?: persistentListOf(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action row: Cancel | Record indicator | Send
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Cancel button
                IconButton(
                    onClick = {
                        voiceMessageComposerState.eventSink(
                            VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Cancel)
                        )
                    },
                ) {
                    Icon(
                        imageVector = CompoundIcons.Delete(),
                        contentDescription = stringResource(CommonStrings.action_cancel),
                        tint = ElementTheme.colors.iconSecondary,
                    )
                }

                // Red pulsing record dot
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(ElementTheme.colors.iconCriticalPrimary),
                )

                // Send button
                Button(
                    text = stringResource(CommonStrings.action_send),
                    onClick = {
                        voiceMessageComposerState.eventSink(
                            VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop)
                        )
                        voiceMessageComposerState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
                    },
                    leadingIcon = IconSource.Vector(CompoundIcons.Send()),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.screen_room_voice_tabletop_hint_close_to_send),
                style = ElementTheme.typography.fontBodySmRegular,
                color = ElementTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TabletopContextMessageRow(item: TimelineItem) {
    when (item) {
        is TimelineItem.Event -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.safeSenderName,
                    style = ElementTheme.typography.fontBodySmMedium,
                    color = ElementTheme.colors.textPrimary,
                    modifier = Modifier.width(80.dp),
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.contentSummary(),
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 2,
                )
            }
        }
        else -> {
            // Skip separators and other non-message items
        }
    }
}

@Composable
private fun WaveformVisualiser(
    levels: ImmutableList<Float>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val barCount = 40
        val displayLevels = if (levels.size >= barCount) {
            levels.takeLast(barCount)
        } else {
            List(barCount - levels.size) { 0.05f } + levels
        }
        displayLevels.forEach { level ->
            val clampedLevel = level.coerceIn(0.05f, 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(clampedLevel)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(ElementTheme.colors.iconAccentPrimary),
            )
        }
    }
}

private fun TimelineItem.Event.contentSummary(): String {
    return when (val c = content) {
        is TimelineItemTextBasedContent -> c.plainText
        is TimelineItemEventContentWithAttachment -> c.bestDescription
        else -> ""
    }
}

private fun formatDuration(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@PreviewsDayNight
@Composable
internal fun TabletopVoiceRecordingViewPreview(
    @PreviewParameter(TabletopVoiceRecordingStateProvider::class) state: VoiceMessageComposerState,
) = ElementPreview {
    TabletopVoiceRecordingView(
        voiceMessageComposerState = state,
        contextMessages = persistentListOf(),
    )
}
