/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.adaptive

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class BookModeTranslatorPreviewState(
    val displayMode: TranslatorDisplayMode,
    val timelineItems: ImmutableList<TimelineItem>,
)

private fun aTranslatorTimelineItems(): ImmutableList<TimelineItem> = persistentListOf(
    aTimelineItemEvent(
        isMine = false,
        senderDisplayName = "Alice",
        content = aTimelineItemTextContent(body = "Hello, how are you?"),
        groupPosition = TimelineItemGroupPosition.First,
    ),
    aTimelineItemEvent(
        isMine = false,
        senderDisplayName = "Alice",
        content = aTimelineItemTextContent(body = "I hope this message finds you well."),
        groupPosition = TimelineItemGroupPosition.Last,
    ),
    aTimelineItemEvent(
        isMine = true,
        senderDisplayName = "Me",
        content = aTimelineItemTextContent(body = "I am doing great, thanks for asking!"),
        groupPosition = TimelineItemGroupPosition.None,
    ),
    aTimelineItemEvent(
        isMine = false,
        senderDisplayName = "Bob",
        content = aTimelineItemTextContent(body = "Can you share the document with me?"),
        groupPosition = TimelineItemGroupPosition.None,
    ),
    aTimelineItemEvent(
        isMine = true,
        senderDisplayName = "Me",
        content = aTimelineItemTextContent(body = "Sure, I will send it shortly."),
        groupPosition = TimelineItemGroupPosition.None,
    ),
)

class BookModeTranslatorStateProvider : PreviewParameterProvider<BookModeTranslatorPreviewState> {
    override val values: Sequence<BookModeTranslatorPreviewState> = sequenceOf(
        BookModeTranslatorPreviewState(
            displayMode = TranslatorDisplayMode.BOOK_MODE,
            timelineItems = aTranslatorTimelineItems(),
        ),
        BookModeTranslatorPreviewState(
            displayMode = TranslatorDisplayMode.FLAT,
            timelineItems = aTranslatorTimelineItems(),
        ),
    )
}
