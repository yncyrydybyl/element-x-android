/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import io.element.android.compound.theme.AccentTheme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.preferences.DropdownOption
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import kotlinx.collections.immutable.ImmutableList

data class AdvancedSettingsState(
    val isDeveloperModeEnabled: Boolean,
    val isSharePresenceEnabled: Boolean,
    val mediaOptimizationState: MediaOptimizationState?,
    val theme: ThemeOption,
    val availableThemeOptions: ImmutableList<ThemeOption>,
    val accentTheme: AccentTheme,
    val mediaPreviewConfigState: MediaPreviewConfigState,
    val liveLocationMinimumDistanceUpdate: Int?,
    val eventSink: (AdvancedSettingsEvent) -> Unit
)

sealed interface MediaOptimizationState {
    data class AllMedia(val isEnabled: Boolean) : MediaOptimizationState
    data class Split(
        val compressImages: Boolean,
        val videoPreset: VideoCompressionPreset,
    ) : MediaOptimizationState

    val shouldCompressImages: Boolean get() = when (this) {
        is AllMedia -> isEnabled
        is Split -> compressImages
    }
}

/** The label shown for an accent colour theme in the settings. */
internal val AccentTheme.titleRes: Int
    @StringRes
    get() = when (this) {
        AccentTheme.Default -> R.string.screen_advanced_settings_colour_theme_default
        AccentTheme.Yellow -> R.string.screen_advanced_settings_colour_theme_yellow
        AccentTheme.HackmasCastle -> R.string.screen_advanced_settings_colour_theme_hackmas
        AccentTheme.KultusministeriumBW -> R.string.screen_advanced_settings_colour_theme_kultusministerium_bw
    }

enum class ThemeOption : DropdownOption {
    System {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_system)
    },

    Light {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_light)
    },

    Dark {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_dark)
    },

    Black {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_black)
    }
}
