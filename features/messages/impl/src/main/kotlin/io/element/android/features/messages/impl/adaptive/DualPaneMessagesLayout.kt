/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * Dual-pane layout for the unfolded inner display of foldable devices.
 *
 * Left pane (55%) = room timeline. Right pane (45%) = room info panel
 * showing avatar, name, topic, member count. Activates on screens >= 600dp wide.
 */
@Composable
fun DualPaneMessagesLayout(
    enabled: Boolean,
    roomName: String?,
    roomAvatar: AvatarData,
    roomTopic: String?,
    roomMemberCount: Long,
    modifier: Modifier = Modifier,
    mainContent: @Composable () -> Unit,
) {
    val isWideEnough = LocalConfiguration.current.screenWidthDp >= 600

    if (!enabled || !isWideEnough) {
        Box(modifier = modifier) { mainContent() }
        return
    }

    Row(modifier = modifier.fillMaxSize()) {
        Box(Modifier.weight(0.55f)) { mainContent() }
        VerticalDivider(Modifier.fillMaxHeight(), color = ElementTheme.colors.borderDisabled)
        RoomInfoPane(roomName, roomAvatar, roomTopic, roomMemberCount, Modifier.weight(0.45f))
    }
}

@Composable
private fun RoomInfoPane(
    roomName: String?,
    roomAvatar: AvatarData,
    roomTopic: String?,
    roomMemberCount: Long,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        Avatar(
            avatarData = AvatarData(roomAvatar.id, roomAvatar.name, roomAvatar.url, AvatarSize.RoomDetailsHeader),
            avatarType = AvatarType.Room(),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = roomName ?: "Unknown room",
            style = ElementTheme.typography.fontHeadingMdBold,
            color = ElementTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        if (roomMemberCount > 0) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(CompoundIcons.UserProfileSolid(), null, tint = ElementTheme.colors.iconSecondary, modifier = Modifier.size(16.dp))
                Text("$roomMemberCount members", style = ElementTheme.typography.fontBodySmRegular, color = ElementTheme.colors.textSecondary)
            }
        }
        if (!roomTopic.isNullOrBlank()) {
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = ElementTheme.colors.borderDisabled)
            Spacer(Modifier.height(16.dp))
            Text("Topic", style = ElementTheme.typography.fontBodyXsRegular, color = ElementTheme.colors.textSecondary, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Text(roomTopic, style = ElementTheme.typography.fontBodyMdRegular, color = ElementTheme.colors.textPrimary, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = ElementTheme.colors.borderDisabled)
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(CompoundIcons.Threads(), null, tint = ElementTheme.colors.iconTertiary, modifier = Modifier.size(20.dp))
            Text("Thread view coming soon", style = ElementTheme.typography.fontBodySmRegular, color = ElementTheme.colors.textSecondary)
        }
    }
}
