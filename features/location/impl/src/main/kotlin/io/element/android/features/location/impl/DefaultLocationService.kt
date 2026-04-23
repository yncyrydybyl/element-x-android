/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.location.api.LocationService

@ContributesBinding(AppScope::class)
class DefaultLocationService : LocationService {
    override fun isServiceAvailable(): Boolean {
        // Forced to true to let the location menu item appear on forks that
        // have no MapTiler API key. Maps will not render but the share-location
        // flow (including live location) is still functional.
        return true
    }
}
