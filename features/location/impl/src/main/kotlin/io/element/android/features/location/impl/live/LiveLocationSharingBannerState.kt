/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

data class LiveLocationSharingBannerState(
    val isVisible: Boolean,
    val eventSink: (LiveLocationSharingBannerEvents) -> Unit,
)

sealed interface LiveLocationSharingBannerEvents {
    data object Stop : LiveLocationSharingBannerEvents
}
