/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.howitworks

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class HowItWorksStateProvider : PreviewParameterProvider<HowItWorksState> {
    override val values: Sequence<HowItWorksState>
        get() = sequenceOf(
            aHowItWorksState(currentStep = 0),
            aHowItWorksState(currentStep = 3),
            aHowItWorksState(currentStep = 4),
            aHowItWorksState(currentStep = 5),
        )
}

fun aHowItWorksState(
    currentStep: Int = 0,
    stepCount: Int = HOW_IT_WORKS_STEP_COUNT,
    isPlaying: Boolean = true,
) = HowItWorksState(
    currentStep = currentStep,
    stepCount = stepCount,
    isPlaying = isPlaying,
    eventSink = {},
)
