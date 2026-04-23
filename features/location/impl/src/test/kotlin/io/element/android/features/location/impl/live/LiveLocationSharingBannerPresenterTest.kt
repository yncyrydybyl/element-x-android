/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.location.impl.live

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class LiveLocationSharingBannerPresenterTest {
    @Test
    fun `present - hidden when no active session`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            val state = awaitItem()
            assertThat(state.isVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - visible when active session for current room`() = runTest {
        val manager = FakeLiveLocationShareManager()
        manager.start(sessionId = A_SESSION_ID, roomId = A_ROOM_ID, duration = 15.minutes)
        val presenter = createPresenter(manager = manager)
        presenter.test {
            val state = awaitItem()
            assertThat(state.isVisible).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - hidden when active session is for a different room`() = runTest {
        val manager = FakeLiveLocationShareManager()
        manager.start(sessionId = A_SESSION_ID, roomId = A_ROOM_ID_2, duration = 15.minutes)
        val presenter = createPresenter(manager = manager)
        presenter.test {
            val state = awaitItem()
            assertThat(state.isVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Stop event clears the active session for the room`() = runTest {
        val manager = FakeLiveLocationShareManager()
        manager.start(sessionId = A_SESSION_ID, roomId = A_ROOM_ID, duration = 15.minutes)
        val presenter = createPresenter(manager = manager)
        presenter.test {
            val visibleState = awaitItem()
            assertThat(visibleState.isVisible).isTrue()
            visibleState.eventSink(LiveLocationSharingBannerEvents.Stop)
            advanceUntilIdle()
            val hiddenState = awaitItem()
            assertThat(hiddenState.isVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `LiveLocationSharingBannerState equality and copy`() {
        val sink: (LiveLocationSharingBannerEvents) -> Unit = {}
        val a = LiveLocationSharingBannerState(isVisible = true, eventSink = sink)
        val b = LiveLocationSharingBannerState(isVisible = true, eventSink = sink)
        val c = a.copy(isVisible = false)

        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(c.isVisible).isFalse()
        assertThat(a.toString()).contains("isVisible")
        assertThat(LiveLocationSharingBannerEvents.Stop).isEqualTo(LiveLocationSharingBannerEvents.Stop)
    }

    private fun TestScope.createPresenter(
        manager: LiveLocationShareManager = FakeLiveLocationShareManager(),
    ) = LiveLocationSharingBannerPresenter(
        room = FakeJoinedRoom(),
        liveLocationShareManager = manager,
        appCoroutineScope = backgroundScope,
    )
}
