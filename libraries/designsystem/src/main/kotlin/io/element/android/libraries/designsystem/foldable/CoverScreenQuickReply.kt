/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.foldable

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * A room shown in the cover-screen quick-reply panel.
 */
data class CoverScreenRoom(
    val roomId: String,
    val name: String,
    val avatarData: AvatarData,
    val lastMessagePreview: String?,
    val timestamp: String?,
    val hasUnread: Boolean = false,
)

/**
 * Compact UI showing up to 3 rooms as large, touch-friendly cards.
 *
 * Designed for the 6.3" cover display of foldable devices and as a quick-reply
 * header on the home screen. Tap a card to open the room; long-press to start
 * a voice message (caller-handled).
 */
@Composable
fun CoverScreenQuickReply(
    rooms: List<CoverScreenRoom>,
    onRoomClick: (roomId: String) -> Unit,
    onRoomLongClick: (roomId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rooms.take(3).forEach { room ->
            CoverScreenRoomCard(room, onRoomClick, onRoomLongClick)
        }
    }
}

@Composable
private fun CoverScreenRoomCard(
    room: CoverScreenRoom,
    onRoomClick: (roomId: String) -> Unit,
    onRoomLongClick: (roomId: String) -> Unit,
) {
    val cardShape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(ElementTheme.colors.bgSubtlePrimary, cardShape)
            .combinedClickable(
                onClick = { onRoomClick(room.roomId) },
                onLongClick = { onRoomLongClick(room.roomId) },
                indication = ripple(),
                interactionSource = interactionSource,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarData = room.avatarData,
            avatarType = AvatarType.Room(),
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = room.name,
                    style = ElementTheme.typography.fontBodyLgMedium,
                    color = ElementTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (!room.timestamp.isNullOrEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = room.timestamp,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
            }
            if (!room.lastMessagePreview.isNullOrEmpty()) {
                Text(
                    text = room.lastMessagePreview,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (room.hasUnread) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(ElementTheme.colors.iconAccentPrimary)
            )
        }
    }
}

