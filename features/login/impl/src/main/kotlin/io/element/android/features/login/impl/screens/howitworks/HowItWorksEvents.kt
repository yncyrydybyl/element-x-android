/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.howitworks

sealed interface HowItWorksEvents {
    /** Pause or resume the auto-playing animation. */
    data object TogglePlayPause : HowItWorksEvents

    /** Restart the animation from the first beat. */
    data object Replay : HowItWorksEvents

    /** Jump to a specific beat (also pauses auto-play). */
    data class GoToStep(val step: Int) : HowItWorksEvents
}
