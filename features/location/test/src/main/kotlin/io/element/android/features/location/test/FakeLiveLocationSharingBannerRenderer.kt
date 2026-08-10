/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.test

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.features.location.api.live.LiveLocationSharingBannerRenderer

class FakeLiveLocationSharingBannerRenderer : LiveLocationSharingBannerRenderer {
    @Composable
    override fun View(modifier: Modifier) = Unit
}
