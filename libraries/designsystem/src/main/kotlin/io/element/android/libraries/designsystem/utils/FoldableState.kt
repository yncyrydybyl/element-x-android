/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker

/**
 * Returns the current [FoldingFeature] if present, or null on non-foldable devices.
 */
@Composable
fun rememberFoldingFeature(): State<FoldingFeature?> {
    val activity = LocalContext.current as? Activity
        ?: return remember { mutableStateOf(null) }
    return produceState<FoldingFeature?>(initialValue = null) {
        WindowInfoTracker.getOrCreate(activity)
            .windowLayoutInfo(activity)
            .collect { layoutInfo ->
                value = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()
            }
    }
}

/** True when half-folded with horizontal hinge (tabletop / laptop posture). */
fun FoldingFeature?.isTabletopMode(): Boolean =
    this != null &&
        state == FoldingFeature.State.HALF_OPENED &&
        orientation == FoldingFeature.Orientation.HORIZONTAL

/** True when half-folded with vertical hinge (book posture). */
fun FoldingFeature?.isBookMode(): Boolean =
    this != null &&
        state == FoldingFeature.State.HALF_OPENED &&
        orientation == FoldingFeature.Orientation.VERTICAL
