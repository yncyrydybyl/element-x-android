/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.foldable

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.flow.map

/**
 * Detects whether the device is currently in tent/half-open mode (Campfire Mode).
 *
 * Tent mode heuristic: a [FoldingFeature] with [FoldingFeature.State.HALF_OPENED] is treated
 * as the Campfire trigger. True tent detection (determining which face is towards the user)
 * requires the dual-screen API which is stubbed at this stage.
 *
 * @return A [State] that is `true` when the device is detected to be in half-open (tent) mode,
 *   or `false` if the context is not an [Activity] or no qualifying fold is detected.
 */
@Composable
internal fun rememberIsCampfireModeAvailable(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = false) {
        val activity = context as? Activity ?: return@produceState
        WindowInfoTracker
            .getOrCreate(activity)
            .windowLayoutInfo(activity)
            .map { layoutInfo ->
                layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .any { it.state == FoldingFeature.State.HALF_OPENED }
            }
            .collect { isTentMode ->
                value = isTentMode
            }
    }
}
