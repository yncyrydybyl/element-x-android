/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.howitworks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.delay

/** Number of beats in the "How Matrix works" explainer. */
const val HOW_IT_WORKS_STEP_COUNT = 6

/** How long each beat is shown before auto-advancing, in milliseconds. */
private val STEP_DURATIONS_MS = longArrayOf(2800L, 3000L, 3200L, 3000L, 4000L, 4200L)

@Inject
class HowItWorksPresenter : Presenter<HowItWorksState> {
    @Composable
    override fun present(): HowItWorksState {
        var currentStep by rememberSaveable { mutableIntStateOf(0) }
        var isPlaying by rememberSaveable { mutableStateOf(true) }

        LaunchedEffect(isPlaying, currentStep) {
            if (isPlaying) {
                delay(STEP_DURATIONS_MS[currentStep.coerceIn(0, HOW_IT_WORKS_STEP_COUNT - 1)])
                currentStep = (currentStep + 1) % HOW_IT_WORKS_STEP_COUNT
            }
        }

        fun handleEvents(event: HowItWorksEvents) {
            when (event) {
                HowItWorksEvents.TogglePlayPause -> isPlaying = !isPlaying
                HowItWorksEvents.Replay -> {
                    currentStep = 0
                    isPlaying = true
                }
                is HowItWorksEvents.GoToStep -> {
                    currentStep = event.step.coerceIn(0, HOW_IT_WORKS_STEP_COUNT - 1)
                    isPlaying = false
                }
            }
        }

        return HowItWorksState(
            currentStep = currentStep,
            stepCount = HOW_IT_WORKS_STEP_COUNT,
            isPlaying = isPlaying,
            eventSink = ::handleEvents,
        )
    }
}
