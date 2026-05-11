package com.drivemind.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.drivemind.DriveMindApplication
import com.drivemind.R
import com.drivemind.ui.MainActivity
import com.drivemind.utils.DrivingDetector
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground service that keeps the GPS-based driving detector alive
 * even when the app is in the background.
 *
 * Required for continuous driving detection without being killed by the OS.
 */
@AndroidEntryPoint
class DrivingDetectionService : Service() {

    @Inject
    lateinit var drivingDetector: DrivingDetector

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        drivingDetector.startDetection()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        drivingDetector.stopDetection()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, DrivingDetectionService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DriveMindApplication.CHANNEL_DRIVING)
            .setContentTitle(getString(R.string.notification_driving_title))
            .setContentText(getString(R.string.notification_driving_text))
            .setSmallIcon(R.drawable.ic_car)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_close, getString(R.string.notification_stop), stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_STOP = "ACTION_STOP_DRIVING_SERVICE"

        fun start(context: Context) {
            val intent = Intent(context, DrivingDetectionService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, DrivingDetectionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
