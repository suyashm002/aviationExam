package com.suyash.mockcivilaviationexam.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.suyash.mockcivilaviationexam.CivilAviationApp
import com.suyash.mockcivilaviationexam.MainActivity
import com.suyash.mockcivilaviationexam.R
import com.suyash.mockcivilaviationexam.domain.logbook.FlightPhase
import com.suyash.mockcivilaviationexam.domain.logbook.FlightRecorder
import com.suyash.mockcivilaviationexam.domain.logbook.RecorderState
import com.suyash.mockcivilaviationexam.domain.logbook.TrackPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Keeps GPS running while a flight is being recorded, even with the screen off
 * or the app in the background. Every fix goes to [FlightRecorder.onFix]; the
 * recorder decides what it means. The service holds no flight state of its own.
 */
class FlightRecorderService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var recorder: FlightRecorder
    private lateinit var locationManager: LocationManager
    private var listening = false

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            recorder.onFix(location.toTrackPoint())
        }

        override fun onProviderEnabled(provider: String) {
            recorder.onGpsAvailability(true)
        }

        override fun onProviderDisabled(provider: String) {
            recorder.onGpsAvailability(false)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    override fun onCreate() {
        super.onCreate()
        recorder = (application as CivilAviationApp).flightRecorder
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startInForeground(buildNotification(recorder.state.value))
        startListening()

        // Refresh the notification's clocks and phase as the flight progresses,
        // and stop ourselves once the pilot is on blocks or discards the flight.
        scope.launch {
            while (true) {
                val state = recorder.state.value
                if (!state.isActive) {
                    stopSelf()
                    return@launch
                }
                notificationManager().notify(NOTIFICATION_ID, buildNotification(state))
                delay(NOTIFICATION_REFRESH_MS)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopListening()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ---- Location ----------------------------------------------------------

    private fun startListening() {
        if (listening) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            recorder.onGpsAvailability(false)
            return
        }
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                FIX_INTERVAL_MS,
                0f,
                listener,
                Looper.getMainLooper()
            )
            listening = true
            recorder.onGpsAvailability(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        } catch (e: SecurityException) {
            recorder.onGpsAvailability(false)
        }
    }

    private fun stopListening() {
        if (!listening) return
        runCatching { locationManager.removeUpdates(listener) }
        listening = false
        recorder.onGpsAvailability(false)
    }

    private fun Location.toTrackPoint() = TrackPoint(
        timestamp = time,
        latitude = latitude,
        longitude = longitude,
        altitudeFt = if (hasAltitude()) altitude * METRES_TO_FEET else 0.0,
        groundSpeedKt = if (hasSpeed()) speed * MPS_TO_KNOTS else 0.0,
        bearingDeg = if (hasBearing()) bearing else null
    )

    // ---- Notification ------------------------------------------------------

    private fun startInForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(state: RecorderState): Notification {
        val now = System.currentTimeMillis()
        val phase = when (state.phase) {
            FlightPhase.PREFLIGHT -> "Waiting for off blocks"
            FlightPhase.TAXI_OUT -> "Taxiing out"
            FlightPhase.AIRBORNE -> "Airborne"
            FlightPhase.TAXI_IN -> "Taxiing in"
            FlightPhase.COMPLETE -> "On blocks"
        }
        val route = listOf(state.departure, state.arrival).filter { it.isNotBlank() }
            .joinToString(" → ").ifBlank { state.aircraft?.registration ?: "Flight" }
        val text = "$phase · Block ${clock(state.blockMillis(now))} · Air ${clock(state.airMillis(now))}"

        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_flight)
            .setContentTitle("Recording flight · $route")
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(openApp)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun clock(millis: Long): String {
        val totalMinutes = millis / 60_000
        return "%d:%02d".format(totalMinutes / 60, totalMinutes % 60)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID, "Flight recorder", NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows while a flight is being recorded with GPS"
            setShowBadge(false)
        }
        notificationManager().createNotificationChannel(channel)
    }

    private fun notificationManager() =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val ACTION_START = "com.suyash.mockcivilaviationexam.action.START_RECORDING"
        const val ACTION_STOP = "com.suyash.mockcivilaviationexam.action.STOP_RECORDING"

        private const val CHANNEL_ID = "flight_recorder"
        private const val NOTIFICATION_ID = 4101
        private const val FIX_INTERVAL_MS = 1_000L
        private const val NOTIFICATION_REFRESH_MS = 30_000L
        private const val METRES_TO_FEET = 3.28084
        private const val MPS_TO_KNOTS = 1.943844

        fun start(context: Context) {
            val intent = Intent(context, FlightRecorderService::class.java).setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, FlightRecorderService::class.java).setAction(ACTION_STOP)
            )
        }
    }
}
