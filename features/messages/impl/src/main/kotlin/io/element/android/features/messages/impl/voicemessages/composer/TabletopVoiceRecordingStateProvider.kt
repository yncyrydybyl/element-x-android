/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.composer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.designsystem.components.media.WaveFormSamples
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Duration.Companion.seconds

/**
 * Preview state for [TabletopVoiceRecordingView].
 *
 * @param voiceMessageState The state of the voice recorder to render.
 * @param contextMessages Recent timeline events shown in the top-half context panel.
 */
data class TabletopVoiceRecordingPreviewState(
    val voiceMessageState: VoiceMessageState,
    val contextMessages: ImmutableList<TimelineItem>,
)

internal class TabletopVoiceRecordingStateProvider : PreviewParameterProvider<TabletopVoiceRecordingPreviewState> {
    override val values: Sequence<TabletopVoiceRecordingPreviewState>
        get() = sequenceOf(
            // Active recording with context
            TabletopVoiceRecordingPreviewState(
                voiceMessageState = VoiceMessageState.Recording(
                    duration = 23.seconds,
                    levels = WaveFormSamples.allRangeWaveForm,
                ),
                contextMessages = sampleContextMessages(),
            ),
            // Finished (preview) state ready to send
            TabletopVoiceRecordingPreviewState(
                voiceMessageState = VoiceMessageState.Preview(
                    isSending = false,
                    isPlaying = false,
                    showCursor = false,
                    playbackProgress = 0f,
                    time = 42.seconds,
                    waveform = WaveFormSamples.realisticWaveForm,
                ),
                contextMessages = sampleContextMessages(),
            ),
            // Idle / no messages yet
            TabletopVoiceRecordingPreviewState(
                voiceMessageState = VoiceMessageState.Idle,
                contextMessages = persistentListOf(),
            ),
        )
}

private fun sampleContextMessages(): ImmutableList<TimelineItem> = listOf(
    aTimelineItemEvent(
        senderDisplayName = "Alice",
        content = aTimelineItemTextContent(body = "Hey, are you joining the call later?"),
    ),
    aTimelineItemEvent(
        senderDisplayName = "Bob",
        isMine = false,
        content = aTimelineItemTextContent(body = "PR review is ready for the new feature branch."),
    ),
    aTimelineItemEvent(
        senderDisplayName = "You",
        isMine = true,
        content = aTimelineItemTextContent(body = "On my way, give me 5 minutes."),
    ),
    aTimelineItemEvent(
        senderDisplayName = "Alice",
        content = aTimelineItemTextContent(body = "No rush! We'll start in 10."),
    ),
).toImmutableList()
