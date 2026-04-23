/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface LiveLocationShareManager {
    val activeSessions: StateFlow<List<LiveLocationSession>>

    suspend fun start(sessionId: SessionId, roomId: RoomId, duration: Duration): Result<Unit>

    suspend fun stop(roomId: RoomId): Result<Unit>

    suspend fun stopAll()
}
