/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.login.impl.screens.howitworks

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.impl.R
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

// The yellow accent is shared across light & dark themes, so it (and the dark
// content drawn on top of it) are defined as fixed colours here.
private val Accent = Color(0xFFA77AFF)
private val OnAccent = Color(0xFF1B1D22)

private data class Beat(
    @StringRes val title: Int,
    @StringRes val message: Int,
)

private val beats = listOf(
    Beat(R.string.screen_explainer_step1_title, R.string.screen_explainer_step1_message),
    Beat(R.string.screen_explainer_step2_title, R.string.screen_explainer_step2_message),
    Beat(R.string.screen_explainer_step3_title, R.string.screen_explainer_step3_message),
    Beat(R.string.screen_explainer_step4_title, R.string.screen_explainer_step4_message),
    Beat(R.string.screen_explainer_step5_title, R.string.screen_explainer_step5_message),
    Beat(R.string.screen_explainer_step6_title, R.string.screen_explainer_step6_message),
)

// Fractional positions (x, y in [0, 1]) of each node within the diagram.
private val pYouPhone = Offset(0.40f, 0.88f)
private val pYourProv = Offset(0.40f, 0.60f)
private val pBobProv = Offset(0.70f, 0.16f)
private val pCarolProv = Offset(0.22f, 0.16f)
private val pBob = Offset(0.82f, 0.40f)
private val pCarol = Offset(0.16f, 0.40f)

@Composable
fun HowItWorksView(
    state: HowItWorksState,
    onBackClick: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.screen_explainer_title),
                        style = ElementTheme.typography.fontBodyLgMedium,
                    )
                },
                navigationIcon = { BackButton(onClick = onBackClick) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            HowItWorksDiagram(
                currentStep = state.currentStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            HowItWorksCaption(currentStep = state.currentStep)
            HowItWorksDots(
                currentStep = state.currentStep,
                stepCount = state.stepCount,
                onStepClick = { state.eventSink(HowItWorksEvents.GoToStep(it)) },
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
            ) {
                Button(
                    text = stringResource(id = CommonStrings.action_continue),
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    text = stringResource(id = R.string.screen_explainer_replay),
                    onClick = { state.eventSink(HowItWorksEvents.Replay) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun HowItWorksDiagram(
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    val ink = ElementTheme.colors.textPrimary
    val neutralLine = ElementTheme.colors.borderInteractiveSecondary

    val showYourProv = currentStep >= 1
    val showOthers = currentStep >= 2
    val showFed = currentStep >= 3
    val showRoom = currentStep >= 5

    val alphaYourProv by animateFloatAsState(if (showYourProv) 1f else 0f, tween(450), label = "yourProv")
    val alphaOthers by animateFloatAsState(if (showOthers) 1f else 0f, tween(450), label = "others")
    val alphaYouLink by animateFloatAsState(if (showYourProv) 1f else 0f, tween(550), label = "youLink")
    val alphaOtherLinks by animateFloatAsState(if (showOthers) 1f else 0f, tween(550), label = "otherLinks")
    val alphaFed by animateFloatAsState(if (showFed) 1f else 0f, tween(550), label = "fed")
    val alphaRoom by animateFloatAsState(if (showRoom) 1f else 0f, tween(450), label = "room")

    // The direct-message packet travels You -> your provider -> Bob's provider -> Bob during beat 5 (index 4).
    val dmProgress = remember { Animatable(0f) }
    LaunchedEffect(currentStep) {
        dmProgress.snapTo(0f)
        if (currentStep == 4) {
            dmProgress.animateTo(1f, tween(durationMillis = 2400, easing = FastOutSlowInEasing))
        }
    }

    BoxWithConstraints(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            fun px(p: Offset) = Offset(p.x * size.width, p.y * size.height)

            // Room enclosure (drawn first, behind everything).
            if (alphaRoom > 0f) {
                val people = listOf(px(pYouPhone), px(pBob), px(pCarol))
                val pad = 26.dp.toPx()
                val minX = people.minOf { it.x } - pad
                val maxX = people.maxOf { it.x } + pad
                val minY = people.minOf { it.y } - pad
                val maxY = people.maxOf { it.y } + pad
                val topLeft = Offset(minX, minY)
                val rectSize = Size(maxX - minX, maxY - minY)
                val corner = CornerRadius(26.dp.toPx())
                drawRoundRect(
                    color = Accent.copy(alpha = 0.10f * alphaRoom),
                    topLeft = topLeft,
                    size = rectSize,
                    cornerRadius = corner,
                )
                drawRoundRect(
                    color = Accent.copy(alpha = alphaRoom),
                    topLeft = topLeft,
                    size = rectSize,
                    cornerRadius = corner,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx())),
                    ),
                )
            }

            // Federation links — dashed triangle between the three providers.
            if (alphaFed > 0f) {
                val fedEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 7.dp.toPx()))
                listOf(
                    pYourProv to pBobProv,
                    pYourProv to pCarolProv,
                    pBobProv to pCarolProv,
                ).forEach { (a, b) ->
                    drawLine(
                        color = ink.copy(alpha = 0.5f * alphaFed),
                        start = px(a),
                        end = px(b),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = fedEffect,
                    )
                }
            }

            // You -> your provider (accent, solid).
            if (alphaYouLink > 0f) {
                drawLine(
                    color = Accent,
                    start = px(pYouPhone),
                    end = px(pYourProv),
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    alpha = alphaYouLink,
                )
            }

            // Bob/Carol -> their providers (neutral).
            if (alphaOtherLinks > 0f) {
                drawLine(neutralLine, px(pBob), px(pBobProv), 3.dp.toPx(), StrokeCap.Round, alpha = alphaOtherLinks)
                drawLine(neutralLine, px(pCarol), px(pCarolProv), 3.dp.toPx(), StrokeCap.Round, alpha = alphaOtherLinks)
            }

            // Direct-message packet.
            if (currentStep == 4) {
                val route = listOf(px(pYouPhone), px(pYourProv), px(pBobProv), px(pBob))
                val pos = pointAlong(route, dmProgress.value)
                drawCircle(Accent.copy(alpha = 0.25f), radius = 13.dp.toPx(), center = pos)
                drawCircle(Accent, radius = 7.dp.toPx(), center = pos)
                drawCircle(OnAccent, radius = 2.5.dp.toPx(), center = pos)
            }
        }

        // Provider nodes.
        DiagramNode(fraction = pCarolProv, width = 104.dp, height = 50.dp, alpha = alphaOthers) {
            ProviderChip(label = "etke · SH", isYours = false)
        }
        DiagramNode(fraction = pBobProv, width = 104.dp, height = 50.dp, alpha = alphaOthers) {
            ProviderChip(label = "matrix.org", isYours = false)
        }
        DiagramNode(fraction = pYourProv, width = 112.dp, height = 50.dp, alpha = alphaYourProv) {
            ProviderChip(label = "Datanauten", isYours = true)
        }

        // People (clients).
        DiagramNode(fraction = pCarol, width = 64.dp, height = 74.dp, alpha = alphaOthers) {
            PersonNode(label = "Carol", initial = "C", isYours = false)
        }
        DiagramNode(fraction = pBob, width = 64.dp, height = 74.dp, alpha = alphaOthers) {
            PersonNode(label = "Bob", initial = "B", isYours = false)
        }
        DiagramNode(fraction = pYouPhone, width = 64.dp, height = 74.dp, alpha = 1f) {
            PersonNode(label = "You", initial = "Y", isYours = true)
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.DiagramNode(
    fraction: Offset,
    width: Dp,
    height: Dp,
    alpha: Float,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(
                x = maxWidth * fraction.x - width / 2f,
                y = maxHeight * fraction.y - height / 2f,
            )
            .size(width = width, height = height)
            .graphicsLayer {
                this.alpha = alpha
                val scale = 0.85f + 0.15f * alpha
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun ProviderChip(
    label: String,
    isYours: Boolean,
) {
    val borderColor = if (isYours) Accent else ElementTheme.colors.borderInteractiveSecondary
    val headerColor = if (isYours) Accent else ElementTheme.colors.textPrimary
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(ElementTheme.colors.bgCanvasDefault)
            .border(width = if (isYours) 2.dp else 1.5.dp, color = borderColor, shape = RoundedCornerShape(12.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp)
                .background(headerColor),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = ElementTheme.typography.fontBodyXsMedium,
                color = ElementTheme.colors.textPrimary,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PersonNode(
    label: String,
    initial: String,
    isYours: Boolean,
) {
    val borderColor = if (isYours) Accent else ElementTheme.colors.borderInteractiveSecondary
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(ElementTheme.colors.bgSubtleSecondary)
                .border(width = if (isYours) 2.dp else 1.5.dp, color = borderColor, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                style = ElementTheme.typography.fontBodyMdMedium,
                color = ElementTheme.colors.textPrimary,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = ElementTheme.typography.fontBodyXsMedium,
            color = ElementTheme.colors.textSecondary,
            maxLines = 1,
        )
    }
}

@Composable
private fun HowItWorksCaption(currentStep: Int) {
    val beat = beats[currentStep.coerceIn(0, beats.size - 1)]
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .heightIn(min = 128.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(Accent)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = "Step ${currentStep + 1} / ${beats.size}",
                style = ElementTheme.typography.fontBodyXsMedium,
                color = OnAccent,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Crossfade(targetState = currentStep, label = "caption") { step ->
            val b = beats[step.coerceIn(0, beats.size - 1)]
            Column {
                Text(
                    text = stringResource(id = b.title),
                    style = ElementTheme.typography.fontHeadingSmMedium,
                    color = ElementTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id = b.message),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun HowItWorksDots(
    currentStep: Int,
    stepCount: Int,
    onStepClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(stepCount) { index ->
            val active = index == currentStep
            val dotWidth by animateDpAsState(if (active) 26.dp else 8.dp, label = "dot")
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(dotWidth)
                    .height(8.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (active) Accent else ElementTheme.colors.borderInteractiveSecondary)
                    .clickable { onStepClick(index) },
            )
        }
    }
}

private fun pointAlong(points: List<Offset>, t: Float): Offset {
    if (points.size < 2) return points.firstOrNull() ?: Offset.Zero
    val segments = points.size - 1
    val scaled = t.coerceIn(0f, 1f) * segments
    val index = scaled.toInt().coerceIn(0, segments - 1)
    val local = scaled - index
    val a = points[index]
    val b = points[index + 1]
    return Offset(a.x + (b.x - a.x) * local, a.y + (b.y - a.y) * local)
}

@PreviewsDayNight
@Composable
internal fun HowItWorksViewPreview(
    @PreviewParameter(HowItWorksStateProvider::class) state: HowItWorksState,
) = ElementPreview {
    HowItWorksView(
        state = state,
        onBackClick = {},
        onContinue = {},
    )
}
