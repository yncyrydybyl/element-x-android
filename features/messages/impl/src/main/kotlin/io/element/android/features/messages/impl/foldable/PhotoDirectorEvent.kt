/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.foldable

sealed interface PhotoDirectorEvent {
    data object Capture : PhotoDirectorEvent
    data class RemovePhoto(val index: Int) : PhotoDirectorEvent
    data class UpdateCaption(val text: String) : PhotoDirectorEvent
    data object Send : PhotoDirectorEvent
    data object Cancel : PhotoDirectorEvent
}
