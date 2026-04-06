/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.composer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerState
import io.element.android.features.messages.api.timeline.voicemessages.composer.aVoiceMessageComposerState
import io.element.android.libraries.designsystem.components.media.WaveFormSamples
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import kotlin.time.Duration.Companion.seconds

class TabletopVoiceRecordingStateProvider : PreviewParameterProvider<VoiceMessageComposerState> {
    override val values: Sequence<VoiceMessageComposerState>
        get() = sequenceOf(
            // Short recording
            aVoiceMessageComposerState(
                voiceMessageState = VoiceMessageState.Recording(
                    duration = 5.seconds,
                    levels = WaveFormSamples.realisticWaveForm,
                ),
            ),
            // Long recording with full waveform
            aVoiceMessageComposerState(
                voiceMessageState = VoiceMessageState.Recording(
                    duration = 125.seconds,
                    levels = WaveFormSamples.allRangeWaveForm,
                ),
            ),
        )
}
