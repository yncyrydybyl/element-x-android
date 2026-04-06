/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.foldable

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class AmbientPresenceStateProvider : PreviewParameterProvider<List<AmbientPresenceContact>> {
    override val values: Sequence<List<AmbientPresenceContact>> = sequenceOf(
        // Active state: mixed online/offline, typing, messages
        listOf(
            AmbientPresenceContact(
                id = "@alice:server.org",
                displayName = "Alice",
                avatarUrl = null,
                isOnline = true,
                isTyping = true,
                lastMessage = null,
                lastMessageTimestamp = null,
            ),
            AmbientPresenceContact(
                id = "@bob:server.org",
                displayName = "Bob",
                avatarUrl = null,
                isOnline = true,
                isTyping = false,
                lastMessage = "Hey, are you free later?",
                lastMessageTimestamp = System.currentTimeMillis() - 5_000,
            ),
            AmbientPresenceContact(
                id = "@carol:server.org",
                displayName = "Carol",
                avatarUrl = null,
                isOnline = false,
                isTyping = false,
                lastMessage = null,
                lastMessageTimestamp = null,
            ),
            AmbientPresenceContact(
                id = "@david:server.org",
                displayName = "David",
                avatarUrl = null,
                isOnline = true,
                isTyping = true,
                lastMessage = null,
                lastMessageTimestamp = null,
            ),
            AmbientPresenceContact(
                id = "@eve:server.org",
                displayName = "Eve",
                avatarUrl = null,
                isOnline = false,
                isTyping = false,
                lastMessage = "See you tomorrow!",
                lastMessageTimestamp = System.currentTimeMillis() - 2_000,
            ),
            AmbientPresenceContact(
                id = "@frank:server.org",
                displayName = "Frank",
                avatarUrl = null,
                isOnline = true,
                isTyping = false,
                lastMessage = null,
                lastMessageTimestamp = null,
            ),
        ),
    )
}
