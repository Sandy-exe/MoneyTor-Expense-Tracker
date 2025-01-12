package com.example.expensetrackerv1

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.expensetrackerv1.notification.SyncingNotificationService

class MyApp: Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SyncingNotificationService.SYNCING_CHANNEL_ID,
                "Syncing",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "An Indicator for Firestore Syncing"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}