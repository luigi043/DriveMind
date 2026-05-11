package com.drivemind

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DriveMindApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val drivingChannel = NotificationChannel(
                CHANNEL_DRIVING,
                "Driving Mode",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active while driving mode is on"
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Voice reminder notifications"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(listOf(drivingChannel, reminderChannel))
        }
    }

    companion object {
        const val CHANNEL_DRIVING = "channel_driving"
        const val CHANNEL_REMINDERS = "channel_reminders"
    }
}
