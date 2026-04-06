/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.adaptive

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * Dual-pane layout for the unfolded inner display of foldable devices.
 *
 * Activates when [enabled] and the screen width is >= 600dp (the inner display
 * of Pixel Fold / Pixel 10 Pro Fold is ~904dp wide when unfolded).
 * Left pane = room timeline, right pane = thread placeholder.
 */
@Composable
fun DualPaneMessagesLayout(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    mainContent: @Composable () -> Unit,
) {
    val config = LocalConfiguration.current
    val isWideEnough = config.screenWidthDp >= 600

    if (!enabled || !isWideEnough) {
        Box(modifier = modifier) { mainContent() }
        return
    }

    Row(modifier = modifier.fillMaxSize()) {
        Box(Modifier.weight(0.55f)) { mainContent() }
        VerticalDivider(Modifier.fillMaxHeight(), color = ElementTheme.colors.borderDisabled)
        Box(Modifier.weight(0.45f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            ThreadPanePlaceholder()
        }
    }
}

@Composable
private fun ThreadPanePlaceholder() {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = CompoundIcons.Threads(),
            contentDescription = null,
            tint = ElementTheme.colors.iconTertiary,
            modifier = Modifier.width(48.dp).height(48.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Select a thread to view",
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
