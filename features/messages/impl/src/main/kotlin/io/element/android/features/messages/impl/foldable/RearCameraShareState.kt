/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.foldable

data class RearCameraShareState(
    val isRearDisplayActive: Boolean,
    val isCaptureReady: Boolean,
    val isFlashEnabled: Boolean,
    val eventSink: (RearCameraShareEvent) -> Unit,
)
