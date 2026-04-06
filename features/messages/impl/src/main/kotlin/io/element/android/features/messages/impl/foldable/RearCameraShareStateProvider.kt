/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.foldable

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class RearCameraShareStateProvider : PreviewParameterProvider<RearCameraShareState> {
    override val values: Sequence<RearCameraShareState>
        get() = sequenceOf(
            aRearCameraShareState(),
            aRearCameraShareState(isCaptureReady = false),
            aRearCameraShareState(isFlashEnabled = true),
            aRearCameraShareState(isRearDisplayActive = true),
        )
}

fun aRearCameraShareState(
    isRearDisplayActive: Boolean = false,
    isCaptureReady: Boolean = true,
    isFlashEnabled: Boolean = false,
    eventSink: (RearCameraShareEvent) -> Unit = {},
) = RearCameraShareState(
    isRearDisplayActive = isRearDisplayActive,
    isCaptureReady = isCaptureReady,
    isFlashEnabled = isFlashEnabled,
    eventSink = eventSink,
)
