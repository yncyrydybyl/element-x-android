/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.impl.R
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Wraps media content with fold-aware behaviour for foldable devices.
 *
 * In tabletop mode (horizontal hinge, half-opened) the screen splits:
 * top half shows the media, bottom half shows file info and actions
 * (share, forward, save, remove). Landscape orientation is forced.
 *
 * On flat / non-foldable devices this is a transparent pass-through.
 */
@Composable
internal fun FoldAwareMediaWrapper(
    mediaInfo: MediaInfo,
    eventId: EventId?,
    eventSink: (MediaViewerEvents) -> Unit,
    data: MediaViewerPageData.MediaViewerData,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val activity = (LocalContext.current as? Activity)
    var isTabletop by remember { mutableStateOf(false) }

    if (activity != null) {
        LaunchedEffect(activity) {
            WindowInfoTracker.getOrCreate(activity)
                .windowLayoutInfo(activity)
                .distinctUntilChanged()
                .collect { info ->
                    val fold = info.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
                    isTabletop = fold != null &&
                        fold.state == FoldingFeature.State.HALF_OPENED &&
                        fold.orientation == FoldingFeature.Orientation.HORIZONTAL
                }
        }
    }

    // Force landscape in tabletop; reset immediately on state change.
    val orientationActivity = LocalActivity.current
    if (orientationActivity != null) {
        DisposableEffect(isTabletop) {
            orientationActivity.requestedOrientation = if (isTabletop) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            onDispose { orientationActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
        }
    }

    if (!isTabletop) {
        Box(modifier = modifier.fillMaxSize()) { content() }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { content() }
        TabletopInfoPanel(mediaInfo, eventId, eventSink, data, Modifier.weight(1f))
    }
}

// ── Bottom-half panel: file info + action buttons ────────────────────────

@Composable
private fun TabletopInfoPanel(
    mediaInfo: MediaInfo,
    eventId: EventId?,
    eventSink: (MediaViewerEvents) -> Unit,
    data: MediaViewerPageData.MediaViewerData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.92f))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Sender
        if (mediaInfo.senderName != null) {
            val id = mediaInfo.senderId?.value ?: "@unknown"
            Label(stringResource(R.string.screen_media_details_uploaded_by))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(AvatarData(id, mediaInfo.senderName, mediaInfo.senderAvatar, AvatarSize.MediaSender), AvatarType.User)
                Column(Modifier.padding(start = 8.dp).weight(1f)) {
                    Text(mediaInfo.senderName.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        color = AvatarColorsProvider.provide(id).foreground, style = ElementTheme.typography.fontBodyMdMedium,
                        modifier = Modifier.clipToBounds())
                    Text(id, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        color = Color.White.copy(alpha = 0.6f), style = ElementTheme.typography.fontBodySmRegular)
                }
            }
        }
        // Date
        if (!mediaInfo.dateSentFull.isNullOrEmpty()) {
            Label(stringResource(R.string.screen_media_details_uploaded_on))
            Text(mediaInfo.dateSentFull.orEmpty(), color = Color.White, style = ElementTheme.typography.fontBodyMdRegular)
        }
        // Filename + format
        Label(stringResource(R.string.screen_media_details_filename))
        Text(mediaInfo.filename, color = Color.White, style = ElementTheme.typography.fontBodyMdRegular, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Label(stringResource(R.string.screen_media_details_file_format))
        Text("${mediaInfo.mimeType} — ${mediaInfo.formattedFileSize}", color = Color.White, style = ElementTheme.typography.fontBodyMdRegular)

        // Actions
        if (eventId != null) {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ActionBtn(CompoundIcons.ShareAndroid(), stringResource(CommonStrings.action_share)) { eventSink(MediaViewerEvents.Share(data)) }
                ActionBtn(CompoundIcons.Forward(), stringResource(CommonStrings.action_forward)) { eventSink(MediaViewerEvents.Forward(eventId)) }
                ActionBtn(CompoundIcons.Download(), stringResource(CommonStrings.action_save)) { eventSink(MediaViewerEvents.SaveOnDisk(data)) }
                ActionBtn(CompoundIcons.Delete(), stringResource(CommonStrings.action_remove), Color(0xFFFF6B6B)) { eventSink(MediaViewerEvents.ConfirmDelete(eventId, data)) }
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text.uppercase(), style = ElementTheme.typography.fontBodyXsRegular, color = Color.White.copy(alpha = 0.5f))
}

@Composable
private fun ActionBtn(icon: ImageVector, label: String, tint: Color = Color.White, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(onClick = onClick) { Icon(icon, label, tint = tint) }
        Text(label, color = tint, style = ElementTheme.typography.fontBodyXsRegular)
    }
}
