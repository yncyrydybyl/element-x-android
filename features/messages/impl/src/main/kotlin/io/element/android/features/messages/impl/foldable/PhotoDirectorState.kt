/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.foldable

import android.net.Uri
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class PhotoDirectorState(
    val capturedPhotos: ImmutableList<Uri>,
    val caption: String,
    val isCoverScreenMirroring: Boolean,
    val eventSink: (PhotoDirectorEvent) -> Unit,
)

fun aPhotoDirectorState(
    capturedPhotos: ImmutableList<Uri> = persistentListOf(),
    caption: String = "",
    isCoverScreenMirroring: Boolean = true,
    eventSink: (PhotoDirectorEvent) -> Unit = {},
) = PhotoDirectorState(
    capturedPhotos = capturedPhotos,
    caption = caption,
    isCoverScreenMirroring = isCoverScreenMirroring,
    eventSink = eventSink,
)
