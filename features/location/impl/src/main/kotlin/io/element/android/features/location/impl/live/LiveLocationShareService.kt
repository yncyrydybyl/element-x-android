/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import dev.zacsweers.metro.Inject
import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.R
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import android.location.Location as AndroidLocation

private const val NOTIFICATION_ID = 4242
private const val NOTIFICATION_CHANNEL_ID = "live_location_sharing"
private const val ACTION_STOP = "io.element.android.features.location.impl.live.ACTION_STOP"
private const val MIN_UPDATE_INTERVAL_MS = 3_000L
private const val MIN_UPDATE_DISTANCE_M = 10f

class LiveLocationShareService : Service() {
    @Inject lateinit var manager: LiveLocationShareManager
    @Inject lateinit var matrixClientProvider: MatrixClientProvider
    @Inject @AppCoroutineScope lateinit var appCoroutineScope: CoroutineScope

    private var locationManager: LocationManager? = null
    private var listenerRegistered = false

    private val locationListener = LocationListener { location ->
        onLocationChanged(location)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("LiveLocationShareService onCreate")
        bindings<LiveLocationShareBindings>().inject(this)
        locationManager = getSystemService(LOCATION_SERVICE) as? LocationManager
        ensureNotificationChannel()
        startForegroundCompat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Timber.d("LiveLocationShareService received stop intent")
            appCoroutineScope.launch { manager.stopAll() }
            return START_NOT_STICKY
        }
        if (manager.activeSessions.value.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }
        registerLocationUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        Timber.d("LiveLocationShareService onDestroy")
        unregisterLocationUpdates()
        super.onDestroy()
    }

    private fun startForegroundCompat() {
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent(this),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(CommonDrawables.ic_notification)
            .setContentTitle(getString(R.string.screen_live_location_notification_title))
            .setContentText(getString(R.string.screen_live_location_notification_text))
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(0, getString(CommonStrings.action_stop), stopPendingIntent)
            .build()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        } else {
            0
        }
        runCatchingExceptions {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
        }.onFailure { Timber.e(it, "Failed to start live-location foreground service") }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.screen_live_location_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        nm.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    private fun registerLocationUpdates() {
        if (listenerRegistered) return
        val lm = locationManager ?: return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.w("Missing location permission; cannot stream live location")
            return
        }
        val providers = buildList {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) add(LocationManager.GPS_PROVIDER)
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) add(LocationManager.NETWORK_PROVIDER)
        }
        if (providers.isEmpty()) {
            Timber.w("No available location providers")
            return
        }
        try {
            providers.forEach { provider ->
                lm.requestLocationUpdates(
                    provider,
                    MIN_UPDATE_INTERVAL_MS,
                    MIN_UPDATE_DISTANCE_M,
                    locationListener,
                )
            }
            listenerRegistered = true
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied when requesting location updates")
        }
    }

    private fun unregisterLocationUpdates() {
        if (!listenerRegistered) return
        locationManager?.removeUpdates(locationListener)
        listenerRegistered = false
    }

    private fun onLocationChanged(androidLocation: AndroidLocation) {
        val sessions = manager.activeSessions.value
        if (sessions.isEmpty()) {
            stopSelf()
            return
        }
        val location = Location(
            lat = androidLocation.latitude,
            lon = androidLocation.longitude,
            accuracy = if (androidLocation.hasAccuracy()) androidLocation.accuracy else null,
        )
        val geoUri = location.toGeoUri()
        appCoroutineScope.launch {
            sessions.forEach { session ->
                val client = matrixClientProvider.getOrNull(session.sessionId) ?: return@forEach
                val room = client.getJoinedRoom(session.roomId) ?: return@forEach
                room.sendLiveLocation(geoUri).onFailure {
                    Timber.w(it, "sendLiveLocation failed for ${session.roomId}")
                }
            }
        }
    }

    companion object {
        fun startIntent(context: Context): Intent = Intent(context, LiveLocationShareService::class.java)

        fun stopIntent(context: Context): Intent = Intent(context, LiveLocationShareService::class.java).setAction(ACTION_STOP)

        fun stop(context: Context) {
            context.stopService(Intent(context, LiveLocationShareService::class.java))
        }
    }
}
