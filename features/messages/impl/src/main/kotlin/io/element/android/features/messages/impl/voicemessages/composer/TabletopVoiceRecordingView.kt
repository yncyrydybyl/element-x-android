/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.composer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * State of the device fold that is relevant to voice recording.
 */
internal enum class VoiceRecordingFoldState {
    /** Device is in tabletop / half-opened horizontal fold — show split layout. */
    TABLETOP,

    /** Device is flat (fully open) or not a foldable — show standard layout. */
    FLAT,
}

/**
 * Maps a [FoldingFeature] to a [VoiceRecordingFoldState].
 */
private fun FoldingFeature.toVoiceRecordingFoldState(): VoiceRecordingFoldState = when {
    state == FoldingFeature.State.HALF_OPENED &&
        orientation == FoldingFeature.Orientation.HORIZONTAL -> VoiceRecordingFoldState.TABLETOP
    else -> VoiceRecordingFoldState.FLAT
}

/**
 * A split-screen voice recording layout optimised for foldable devices in tabletop mode
 * (device half-opened with a horizontal fold).
 *
 * - **Top half** – read-only context showing the last few messages from the timeline.
 * - **Bottom half** – a large animated waveform visualizer, elapsed duration, and
 *   record / stop / cancel controls.
 *
 * When the device transitions from [VoiceRecordingFoldState.TABLETOP] to
 * [VoiceRecordingFoldState.FLAT] while a recording is in progress the [onSendVoiceMessage]
 * callback is invoked to implement the "fold-to-send" gesture.
 *
 * This composable is gated by [io.element.android.libraries.featureflag.api.FeatureFlags.FoldableFeatures]
 * in the host screen — it does not check the flag itself.
 *
 * @param voiceMessageState The current state of the voice recorder.
 * @param contextMessages Recent timeline items to show as conversation context. Up to 5 are shown.
 * @param onRecorderEvent Callback for recorder user actions (start, stop, cancel).
 * @param onSendVoiceMessage Callback to send the finished voice message.
 * @param modifier Optional [Modifier].
 */
@Composable
internal fun TabletopVoiceRecordingView(
    voiceMessageState: VoiceMessageState,
    contextMessages: ImmutableList<TimelineItem>,
    onRecorderEvent: (VoiceMessageRecorderEvent) -> Unit,
    onSendVoiceMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var foldState by remember { mutableStateOf(VoiceRecordingFoldState.FLAT) }
    var wasTabletop by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        foldState = VoiceRecordingFoldState.FLAT
        wasTabletop = false
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
                val newFoldState = foldingFeature?.toVoiceRecordingFoldState()
                    ?: VoiceRecordingFoldState.FLAT

                // Fold-to-send: device went from tabletop → flat while recording
                if (wasTabletop &&
                    newFoldState == VoiceRecordingFoldState.FLAT &&
                    voiceMessageState is VoiceMessageState.Recording
                ) {
                    onSendVoiceMessage()
                }

                wasTabletop = newFoldState == VoiceRecordingFoldState.TABLETOP
                foldState = newFoldState
            }
    }

    when (foldState) {
        VoiceRecordingFoldState.TABLETOP -> {
            TabletopSplitLayout(
                voiceMessageState = voiceMessageState,
                contextMessages = contextMessages,
                onRecorderEvent = onRecorderEvent,
                onSendVoiceMessage = onSendVoiceMessage,
                modifier = modifier,
            )
        }
        VoiceRecordingFoldState.FLAT -> {
            // In flat mode this composable is a no-op: the standard composer handles rendering.
            // The host should only show TabletopVoiceRecordingView when tabletop is detected;
            // this branch acts as a transparent fallback.
        }
    }
}

// ---------------------------------------------------------------------------
// Internal layout
// ---------------------------------------------------------------------------

@Composable
private fun TabletopSplitLayout(
    voiceMessageState: VoiceMessageState,
    contextMessages: ImmutableList<TimelineItem>,
    onRecorderEvent: (VoiceMessageRecorderEvent) -> Unit,
    onSendVoiceMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElementTheme.colors.bgCanvasDefault),
    ) {
        // Top half — conversation context
        ContextPanel(
            messages = contextMessages,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )

        // Fold divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ElementTheme.colors.borderInteractiveSecondary),
        )

        // Bottom half — waveform + controls
        RecordingPanel(
            voiceMessageState = voiceMessageState,
            onRecorderEvent = onRecorderEvent,
            onSendVoiceMessage = onSendVoiceMessage,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
    }
}

// ---------------------------------------------------------------------------
// Context panel (top half)
// ---------------------------------------------------------------------------

@Composable
private fun ContextPanel(
    messages: ImmutableList<TimelineItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(ElementTheme.colors.bgSubtlePrimary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        val recentEvents = messages
            .filterIsInstance<TimelineItem.Event>()
            .takeLast(5)

        if (recentEvents.isEmpty()) {
            Text(
                text = "No recent messages",
                style = ElementTheme.typography.fontBodySmRegular,
                color = ElementTheme.colors.textSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        } else {
            recentEvents.forEach { event ->
                ContextMessageRow(event = event)
            }
        }
    }
}

@Composable
private fun ContextMessageRow(
    event: TimelineItem.Event,
    modifier: Modifier = Modifier,
) {
    val textContent = (event.content as? TimelineItemTextBasedContent)?.body
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = event.safeSenderName,
            style = ElementTheme.typography.fontBodySmMedium,
            color = ElementTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(72.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = textContent ?: "[media]",
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

// ---------------------------------------------------------------------------
// Recording panel (bottom half)
// ---------------------------------------------------------------------------

@Composable
private fun RecordingPanel(
    voiceMessageState: VoiceMessageState,
    onRecorderEvent: (VoiceMessageRecorderEvent) -> Unit,
    onSendVoiceMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRecording = voiceMessageState is VoiceMessageState.Recording
    val levels = (voiceMessageState as? VoiceMessageState.Recording)?.levels
        ?: (voiceMessageState as? VoiceMessageState.Preview)?.waveform
        ?: persistentListOf()
    val duration = (voiceMessageState as? VoiceMessageState.Recording)?.duration
        ?: (voiceMessageState as? VoiceMessageState.Preview)?.time
        ?: 0.seconds

    Column(
        modifier = modifier
            .background(ElementTheme.colors.bgCanvasDefault)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        // Duration label
        Text(
            text = duration.toFormattedString(),
            style = ElementTheme.typography.fontHeadingMdBold,
            color = ElementTheme.colors.textPrimary,
        )

        // Waveform
        LiveWaveformView(
            levels = levels,
            isAnimating = isRecording,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
        )

        // Controls row
        RecordingControlsRow(
            isRecording = isRecording,
            onRecorderEvent = onRecorderEvent,
            onSendVoiceMessage = onSendVoiceMessage,
        )
    }
}

@Composable
private fun RecordingControlsRow(
    isRecording: Boolean,
    onRecorderEvent: (VoiceMessageRecorderEvent) -> Unit,
    onSendVoiceMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Cancel button
        IconButton(
            onClick = { onRecorderEvent(VoiceMessageRecorderEvent.Cancel) },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ElementTheme.colors.bgSubtlePrimary),
        ) {
            Icon(
                imageVector = CompoundIcons.Close(),
                contentDescription = "Cancel recording",
                tint = ElementTheme.colors.iconSecondary,
                modifier = Modifier.size(24.dp),
            )
        }

        // Main record / stop button (64 dp)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ElementTheme.colors.bgAccentRest),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = {
                    if (isRecording) {
                        onRecorderEvent(VoiceMessageRecorderEvent.Stop)
                    } else {
                        onRecorderEvent(VoiceMessageRecorderEvent.Start)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = if (isRecording) CompoundIcons.EndCall() else CompoundIcons.MicOn(),
                    contentDescription = if (isRecording) "Stop recording" else "Start recording",
                    tint = ElementTheme.colors.iconOnSolidPrimary,
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        // Send button (only meaningful when finished)
        IconButton(
            onClick = onSendVoiceMessage,
            enabled = !isRecording,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isRecording) {
                        ElementTheme.colors.bgSubtlePrimary
                    } else {
                        ElementTheme.colors.bgAccentRest
                    }
                ),
        ) {
            Icon(
                imageVector = CompoundIcons.Send(),
                contentDescription = "Send voice message",
                tint = if (isRecording) {
                    ElementTheme.colors.iconDisabled
                } else {
                    ElementTheme.colors.iconOnSolidPrimary
                },
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Live waveform Canvas
// ---------------------------------------------------------------------------

/**
 * Draws animated vertical bars representing current audio recording levels.
 *
 * Uses [ElementTheme.colors.iconAccentPrimary] for the bar colour, which is yellow
 * when the yellow-accent customization is applied.
 */
@Composable
private fun LiveWaveformView(
    levels: ImmutableList<Float>,
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
) {
    // A gentle shimmer phase offset that cycles when actively recording
    val phase = remember { Animatable(0f) }
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            phase.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            )
        } else {
            phase.stop()
        }
    }

    val accentColor = ElementTheme.colors.iconAccentPrimary

    Canvas(modifier = modifier) {
        val barCount = if (levels.isEmpty()) 40 else levels.size.coerceAtMost(64)
        val totalWidth = size.width
        val totalHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.5f)).coerceAtLeast(4f)
        val barSpacing = barWidth * 0.5f

        val sampledLevels: List<Float> = if (levels.isEmpty()) {
            // Idle — draw a flat midline
            List(barCount) { 0.08f }
        } else {
            val step = levels.size.toFloat() / barCount
            (0 until barCount).map { i ->
                levels[(i * step).toInt().coerceIn(0, levels.size - 1)]
            }
        }

        sampledLevels.forEachIndexed { index, level ->
            val animatedLevel = if (isAnimating) {
                // Subtle shimmer: shift level by phase
                val shifted = (level + phase.value * 0.15f).coerceIn(0.05f, 1f)
                shifted
            } else {
                level.coerceAtLeast(0.05f)
            }
            val barHeight = (totalHeight * animatedLevel).coerceAtLeast(8f)
            val left = index * (barWidth + barSpacing)
            val top = (totalHeight - barHeight) / 2f

            drawRoundRect(
                brush = SolidColor(accentColor),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun Duration.toFormattedString(): String {
    val totalSeconds = inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@PreviewsDayNight
@Composable
internal fun TabletopVoiceRecordingViewRecordingPreview(
    @PreviewParameter(TabletopVoiceRecordingStateProvider::class)
    state: TabletopVoiceRecordingPreviewState,
) = ElementPreview {
    TabletopSplitLayout(
        voiceMessageState = state.voiceMessageState,
        contextMessages = state.contextMessages,
        onRecorderEvent = {},
        onSendVoiceMessage = {},
    )
}
