/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.foldable

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

class PhotoDirectorStateProvider : PreviewParameterProvider<PhotoDirectorState> {
    override val values: Sequence<PhotoDirectorState>
        get() = sequenceOf(
            // No photos captured yet
            aPhotoDirectorState(
                capturedPhotos = persistentListOf(),
                caption = "",
                isCoverScreenMirroring = true,
            ),
            // One photo captured with caption
            aPhotoDirectorState(
                capturedPhotos = persistentListOf(
                    Uri.parse("content://media/external/images/media/1"),
                ),
                caption = "Look at this!",
                isCoverScreenMirroring = true,
            ),
            // Three photos captured
            aPhotoDirectorState(
                capturedPhotos = listOf(
                    Uri.parse("content://media/external/images/media/1"),
                    Uri.parse("content://media/external/images/media/2"),
                    Uri.parse("content://media/external/images/media/3"),
                ).toPersistentList(),
                caption = "Check out these shots",
                isCoverScreenMirroring = false,
            ),
        )
}
