/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package ui

import androidx.compose.runtime.CompositionLocalProvider
import app.cash.paparazzi.Paparazzi
import base.BaseDeviceConfig
import com.android.resources.NightMode
import io.element.android.compound.colors.withAccent
import io.element.android.compound.theme.AccentTheme
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.LocalAccentTheme
import io.element.android.compound.tokens.generated.compoundColorsDark
import io.element.android.compound.tokens.generated.compoundColorsLight
import io.element.android.features.login.impl.screens.onboarding.OnBoardingView
import io.element.android.features.login.impl.screens.onboarding.anOnBoardingState
import org.junit.Rule
import org.junit.Test

/**
 * Renders the Kultusministerium Baden-Württemberg theme, which cannot be seen in the normal preview
 * screenshots: those wrap plain [ElementTheme], while the accent is applied a layer higher up.
 */
class KultusministeriumThemeTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = BaseDeviceConfig.NEXUS_5.deviceConfig.copy(nightMode = NightMode.NOTNIGHT),
        maxPercentDifference = 0.0,
    )

    private val accent = AccentTheme.KultusministeriumBW

    @Test
    fun onboarding() {
        paparazzi.snapshot {
            CompositionLocalProvider(LocalAccentTheme provides accent) {
                ElementTheme(
                    compoundLight = compoundColorsLight.withAccent(accent.palette!!),
                    compoundDark = compoundColorsDark.withAccent(accent.palette!!),
                    applySystemBarsUpdate = false,
                ) {
                    OnBoardingView(
                        state = anOnBoardingState(),
                        onBackClick = {},
                        onDeveloperSettingsClick = {},
                        onSignInWithQrCode = {},
                        onSignIn = {},
                        onCreateAccount = {},
                        onReportProblem = {},
                        onOAuthDetails = {},
                        onNeedLoginPassword = {},
                        onLearnMoreClick = {},
                    )
                }
            }
        }
    }
}
