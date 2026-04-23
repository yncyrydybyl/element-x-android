/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import android.content.Context
import androidx.core.content.ContextCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import kotlin.time.Duration

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultLiveLocationShareManager(
    @ApplicationContext private val context: Context,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
    private val matrixClientProvider: MatrixClientProvider,
) : LiveLocationShareManager {
    private val sessionsState = MutableStateFlow<List<LiveLocationSession>>(emptyList())
    private val expiryJobs = mutableMapOf<RoomId, Job>()
    private val mutex = Mutex()

    override val activeSessions: StateFlow<List<LiveLocationSession>> = sessionsState.asStateFlow()

    override suspend fun start(
        sessionId: SessionId,
        roomId: RoomId,
        duration: Duration,
    ): Result<Unit> = runCatchingExceptions {
        mutex.withLock {
            val client = matrixClientProvider.getOrNull(sessionId)
                ?: error("No active MatrixClient for session $sessionId")
            val room = client.getJoinedRoom(roomId)
                ?: error("Not a joined room: $roomId")
            room.startLiveLocationShare(duration.inWholeMilliseconds).getOrThrow()

            val expiresAtEpochMillis = System.currentTimeMillis() + duration.inWholeMilliseconds
            val newSession = LiveLocationSession(sessionId, roomId, expiresAtEpochMillis)
            sessionsState.value = sessionsState.value.filterNot { it.roomId == roomId } + newSession

            expiryJobs.remove(roomId)?.cancel()
            expiryJobs[roomId] = appCoroutineScope.launch {
                delay(duration.inWholeMilliseconds)
                stop(roomId)
            }

            startService()
        }
    }

    override suspend fun stop(roomId: RoomId): Result<Unit> = runCatchingExceptions {
        val removed = mutex.withLock {
            expiryJobs.remove(roomId)?.cancel()
            val match = sessionsState.value.firstOrNull { it.roomId == roomId }
            if (match != null) {
                sessionsState.value = sessionsState.value - match
            }
            match
        }
        if (removed != null) {
            val client = matrixClientProvider.getOrNull(removed.sessionId)
            client?.getJoinedRoom(roomId)
                ?.stopLiveLocationShare()
                ?.onFailure { Timber.w(it, "stopLiveLocationShare failed for room $roomId") }
        }
        if (sessionsState.value.isEmpty()) {
            LiveLocationShareService.stop(context)
        }
    }

    override suspend fun stopAll() {
        sessionsState.value.toList().forEach { session ->
            stop(session.roomId)
        }
    }

    private fun startService() {
        val intent = LiveLocationShareService.startIntent(context)
        ContextCompat.startForegroundService(context, intent)
    }
}
