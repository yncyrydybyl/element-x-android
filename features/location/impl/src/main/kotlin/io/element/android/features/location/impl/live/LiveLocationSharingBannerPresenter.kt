/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class LiveLocationSharingBannerPresenter(
    private val room: JoinedRoom,
    private val liveLocationShareManager: LiveLocationShareManager,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) : Presenter<LiveLocationSharingBannerState> {
    @Composable
    override fun present(): LiveLocationSharingBannerState {
        val activeSessions by liveLocationShareManager.activeSessions.collectAsState()
        val isVisible = activeSessions.any { it.roomId == room.roomId }

        fun handleEvent(event: LiveLocationSharingBannerEvents) {
            when (event) {
                LiveLocationSharingBannerEvents.Stop -> appCoroutineScope.launch {
                    liveLocationShareManager.stop(room.roomId)
                }
            }
        }

        return LiveLocationSharingBannerState(
            isVisible = isVisible,
            eventSink = ::handleEvent,
        )
    }
}
