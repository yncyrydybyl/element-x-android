/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Represents the display mode for the book-mode translator layout.
 */
enum class TranslatorDisplayMode {
    /** Device is half-folded with a vertical hinge (book mode) — split pane. */
    BOOK_MODE,

    /** Device is fully unfolded flat — single timeline with translation subtitles. */
    FLAT,

    /** Device is compact/phone form — no translation UI. */
    COMPACT,
}

/**
 * Data class representing a timeline event augmented with a stub translation.
 */
data class TranslatedTimelineItem(
    val original: TimelineItem.Event,
    val translatedText: String,
)

/**
 * Detects the current [TranslatorDisplayMode] from the device fold state using [WindowInfoTracker].
 *
 * - Book mode: `HALF_OPENED` + vertical hinge.
 * - Flat: `FLAT` (fully unfolded).
 * - Compact: non-foldable or unknown fold state.
 */
@Composable
fun rememberTranslatorDisplayMode(): TranslatorDisplayMode {
    val context = LocalContext.current
    var displayMode by remember { mutableStateOf(TranslatorDisplayMode.COMPACT) }

    DisposableEffect(context) {
        displayMode = TranslatorDisplayMode.COMPACT
        onDispose { }
    }

    LaunchedEffect(context) {
        WindowInfoTracker
            .getOrCreate(context)
            .windowLayoutInfo(context)
            .distinctUntilChanged()
            .collect { layoutInfo ->
                val foldingFeature = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()
                displayMode = foldingFeature?.toTranslatorDisplayMode() ?: TranslatorDisplayMode.COMPACT
            }
    }

    return displayMode
}

private fun FoldingFeature.toTranslatorDisplayMode(): TranslatorDisplayMode = when {
    state == FoldingFeature.State.HALF_OPENED &&
        orientation == FoldingFeature.Orientation.VERTICAL -> TranslatorDisplayMode.BOOK_MODE
    state == FoldingFeature.State.FLAT -> TranslatorDisplayMode.FLAT
    else -> TranslatorDisplayMode.COMPACT
}

/**
 * Stub translator: reverses the words of the message body as a visual placeholder.
 * Replace with real on-device ML translation in a future iteration.
 */
private fun stubTranslate(text: String): String {
    if (text.isBlank()) return text
    val words = text.split(" ")
    return if (words.size > 1) words.reversed().joinToString(" ") else "$text [translated]"
}

/** Extracts translatable text from a timeline event. */
private fun TimelineItem.Event.extractBodyText(): String =
    (content as? TimelineItemTextBasedContent)?.body ?: ""

/**
 * Builds a list of [TranslatedTimelineItem] from the timeline items, filtering to text events only.
 */
private fun buildTranslatedItems(items: ImmutableList<TimelineItem>): List<TranslatedTimelineItem> =
    items
        .filterIsInstance<TimelineItem.Event>()
        .filter { it.extractBodyText().isNotBlank() }
        .map { event ->
            TranslatedTimelineItem(
                original = event,
                translatedText = stubTranslate(event.extractBodyText()),
            )
        }

/**
 * Dual-screen translator layout for foldable devices.
 *
 * - [TranslatorDisplayMode.BOOK_MODE]: splits into left (original) + right (translated) panes divided at the hinge.
 * - [TranslatorDisplayMode.FLAT]: single timeline with translation shown as a subtitle under each message.
 * - [TranslatorDisplayMode.COMPACT]: passes through to [normalContent] unchanged.
 */
@Composable
fun BookModeTranslatorLayout(
    displayMode: TranslatorDisplayMode,
    timelineItems: ImmutableList<TimelineItem>,
    modifier: Modifier = Modifier,
    normalContent: @Composable () -> Unit,
) {
    when (displayMode) {
        TranslatorDisplayMode.BOOK_MODE -> BookModeDualPane(
            timelineItems = timelineItems,
            modifier = modifier,
        )
        TranslatorDisplayMode.FLAT -> FlatModeInterleavedTimeline(
            timelineItems = timelineItems,
            modifier = modifier,
        )
        TranslatorDisplayMode.COMPACT -> normalContent()
    }
}

// ---------------------------------------------------------------------------
// Book mode — dual pane
// ---------------------------------------------------------------------------

@Composable
private fun BookModeDualPane(
    timelineItems: ImmutableList<TimelineItem>,
    modifier: Modifier = Modifier,
) {
    val translatedItems = buildTranslatedItems(timelineItems)

    Row(modifier = modifier.fillMaxSize()) {
        // Left pane — original messages
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(ElementTheme.colors.bgCanvasDefault),
        ) {
            PaneHeader(
                title = "Original",
                languageCode = "EN",
                modifier = Modifier.fillMaxWidth(),
            )
            OriginalMessageList(
                items = translatedItems,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
            )
        }

        // Hinge divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(ElementTheme.colors.borderInteractivePrimary),
        )

        // Right pane — translated messages
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(ElementTheme.colors.bgSubtlePrimary),
        ) {
            PaneHeader(
                title = "Translated",
                languageCode = "ES",
                modifier = Modifier.fillMaxWidth(),
            )
            TranslatedMessageList(
                items = translatedItems,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
            )
        }
    }
}

@Composable
private fun PaneHeader(
    title: String,
    languageCode: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(ElementTheme.colors.bgCanvasDefault)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = ElementTheme.typography.fontBodySmMedium,
            color = ElementTheme.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        LanguageBadge(code = languageCode)
    }
}

@Composable
private fun LanguageBadge(
    code: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = ElementTheme.colors.bgActionPrimaryRest,
    ) {
        Text(
            text = code,
            style = ElementTheme.typography.fontBodyXsMedium,
            color = ElementTheme.colors.textOnSolidPrimary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun OriginalMessageList(
    items: List<TranslatedTimelineItem>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
    ) {
        items(items, key = { it.original.id.value }) { item ->
            TranslatorChatBubble(
                senderName = item.original.safeSenderName,
                avatarData = item.original.senderAvatar,
                text = item.original.extractBodyText(),
                isMine = item.original.isMine,
                sentTime = item.original.sentTime,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun TranslatedMessageList(
    items: List<TranslatedTimelineItem>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
    ) {
        items(items, key = { it.original.id.value }) { item ->
            TranslatorChatBubble(
                senderName = item.original.safeSenderName,
                avatarData = item.original.senderAvatar,
                text = item.translatedText,
                isMine = item.original.isMine,
                sentTime = item.original.sentTime,
                isTranslated = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun TranslatorChatBubble(
    senderName: String,
    avatarData: io.element.android.libraries.designsystem.components.avatar.AvatarData,
    text: String,
    isMine: Boolean,
    sentTime: String,
    modifier: Modifier = Modifier,
    isTranslated: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
    ) {
        if (!isMine) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(avatarData = avatarData.copy(size = AvatarSize.TimelineSender))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = senderName,
                    style = ElementTheme.typography.fontBodyXsRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
        Surface(
            shape = RoundedCornerShape(
                topStart = if (isMine) 12.dp else 2.dp,
                topEnd = if (isMine) 2.dp else 12.dp,
                bottomStart = 12.dp,
                bottomEnd = 12.dp,
            ),
            color = if (isMine) ElementTheme.colors.bgActionPrimaryRest else ElementTheme.colors.bgSubtleSecondary,
        ) {
            Text(
                text = text,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = if (isMine) ElementTheme.colors.textOnSolidPrimary else ElementTheme.colors.textPrimary,
                fontStyle = if (isTranslated) FontStyle.Italic else FontStyle.Normal,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
        Text(
            text = sentTime,
            style = ElementTheme.typography.fontBodyXsRegular,
            color = ElementTheme.colors.textSecondary,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Flat mode — interleaved single timeline with translation subtitle
// ---------------------------------------------------------------------------

@Composable
private fun FlatModeInterleavedTimeline(
    timelineItems: ImmutableList<TimelineItem>,
    modifier: Modifier = Modifier,
) {
    val translatedItems = buildTranslatedItems(timelineItems)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        reverseLayout = true,
    ) {
        items(translatedItems, key = { it.original.id.value }) { item ->
            FlatModeMessageItem(
                item = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun FlatModeMessageItem(
    item: TranslatedTimelineItem,
    modifier: Modifier = Modifier,
) {
    val isMine = item.original.isMine
    Column(
        modifier = modifier,
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
    ) {
        if (!isMine) {
            Text(
                text = item.original.safeSenderName,
                style = ElementTheme.typography.fontBodyXsRegular,
                color = ElementTheme.colors.textSecondary,
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        Surface(
            shape = RoundedCornerShape(
                topStart = if (isMine) 12.dp else 2.dp,
                topEnd = if (isMine) 2.dp else 12.dp,
                bottomStart = 12.dp,
                bottomEnd = 12.dp,
            ),
            color = if (isMine) ElementTheme.colors.bgActionPrimaryRest else ElementTheme.colors.bgSubtleSecondary,
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                // Original text
                Text(
                    text = item.original.extractBodyText(),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = if (isMine) ElementTheme.colors.textOnSolidPrimary else ElementTheme.colors.textPrimary,
                )
                // Translation subtitle
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.translatedText,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = if (isMine) ElementTheme.colors.textOnSolidPrimary else ElementTheme.colors.textSecondary,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
        Text(
            text = item.original.sentTime,
            style = ElementTheme.typography.fontBodyXsRegular,
            color = ElementTheme.colors.textSecondary,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@PreviewsDayNight
@Composable
internal fun BookModeTranslatorLayoutBookModePreview(
    @PreviewParameter(BookModeTranslatorStateProvider::class) state: BookModeTranslatorPreviewState,
) = ElementPreview {
    if (state.displayMode == TranslatorDisplayMode.BOOK_MODE) {
        BookModeTranslatorLayout(
            displayMode = state.displayMode,
            timelineItems = state.timelineItems,
        ) {}
    }
}

@PreviewsDayNight
@Composable
internal fun BookModeTranslatorLayoutFlatModePreview(
    @PreviewParameter(BookModeTranslatorStateProvider::class) state: BookModeTranslatorPreviewState,
) = ElementPreview {
    if (state.displayMode == TranslatorDisplayMode.FLAT) {
        BookModeTranslatorLayout(
            displayMode = state.displayMode,
            timelineItems = state.timelineItems,
        ) {}
    }
}
