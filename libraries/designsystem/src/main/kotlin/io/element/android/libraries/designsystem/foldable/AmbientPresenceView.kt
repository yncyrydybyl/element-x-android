/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.foldable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.coroutines.delay

private val AvatarSizeCoverDisplay = AvatarSize.UserListItem // 36dp, close to 48dp intent

/**
 * Ambient presence display for foldable cover screens.
 *
 * Renders a living mosaic of the user's closest contacts with animated
 * typing indicators, presence dots, and fading message previews.
 * Optimised for the 6.3" cover display of foldable devices.
 *
 * Gated behind [io.element.android.libraries.featureflag.api.FeatureFlags.FoldableFeatures].
 *
 * @param contacts List of contacts to display (up to 6 shown, 3 columns x 2 rows).
 * @param onContactClick Called with the contact's ID when an avatar is tapped.
 * @param modifier Modifier applied to the outermost container.
 */
@Composable
fun AmbientPresenceView(
    contacts: List<AmbientPresenceContact>,
    onContactClick: (contactId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ElementTheme.colors.bgCanvasDefault),
    ) {
        // Subtle gradient overlay from top
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ElementTheme.colors.bgSubtleSecondary.copy(alpha = 0.4f),
                            Color.Transparent,
                        ),
                    )
                )
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(contacts.take(6)) { contact ->
                AmbientContactBubble(
                    contact = contact,
                    onClick = { onContactClick(contact.id) },
                )
            }
        }
    }
}

@Composable
private fun AmbientContactBubble(
    contact: AmbientPresenceContact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Visibility state for the message preview — auto-fades after 3 seconds
    var showMessage by remember(contact.lastMessageTimestamp) {
        mutableStateOf(contact.lastMessage != null)
    }
    LaunchedEffect(contact.lastMessageTimestamp) {
        if (contact.lastMessage != null) {
            showMessage = true
            delay(3_000)
            showMessage = false
        }
    }

    Column(
        modifier = modifier
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            // Avatar
            Avatar(
                avatarData = AvatarData(
                    id = contact.id,
                    name = contact.displayName,
                    url = contact.avatarUrl,
                    size = AvatarSizeCoverDisplay,
                ),
                avatarType = AvatarType.User,
                modifier = Modifier.size(48.dp),
            )

            // Presence dot — bottom-right of avatar
            PresenceDot(isOnline = contact.isOnline)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Display name
        Text(
            text = contact.displayName,
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Typing indicator shown when isTyping, otherwise message preview
        if (contact.isTyping) {
            TypingDotsIndicator()
        } else {
            AnimatedVisibility(
                visible = showMessage,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Text(
                    text = contact.lastMessage.orEmpty(),
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PresenceDot(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
) {
    val dotColor = if (isOnline) {
        ElementTheme.colors.iconSuccessPrimary
    } else {
        ElementTheme.colors.iconSecondary
    }
    val borderColor = ElementTheme.colors.bgCanvasDefault

    Box(
        modifier = modifier
            .offset(x = 2.dp, y = 2.dp)
            .size(10.dp)
            .border(width = 1.5.dp, color = borderColor, shape = CircleShape)
            .clip(CircleShape)
            .background(dotColor),
    )
}

@Composable
private fun TypingDotsIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")

    val dot1Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0),
        ),
        label = "dot1",
    )
    val dot2Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(150),
        ),
        label = "dot2",
    )
    val dot3Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(300),
        ),
        label = "dot3",
    )

    val dotColor = ElementTheme.colors.textSecondary

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = dot1Alpha))
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = dot2Alpha))
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = dot3Alpha))
        )
    }
}

// -- Previews --

@PreviewsDayNight
@Composable
internal fun AmbientPresenceViewPreview(
    @PreviewParameter(AmbientPresenceStateProvider::class) contacts: List<AmbientPresenceContact>,
) = ElementPreview {
    AmbientPresenceView(
        contacts = contacts,
        onContactClick = {},
    )
}
