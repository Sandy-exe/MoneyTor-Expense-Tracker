package com.example.expensetrackerv1.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.expensetrackerv1.MainActivity
import com.example.expensetrackerv1.R

class SyncingNotificationService(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(progressText: String, contentText: String = "Syncing Changes with Firebase") {
        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val notification = NotificationCompat.Builder(context, SYNCING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_logo)
            .setContentTitle(contentText)
            .setContentText(progressText)
            .setContentIntent(activityPendingIntent)
            .build()

        notificationManager.notify(1, notification)
    }

    companion object {
        const val SYNCING_CHANNEL_ID = "syncing_channel"
    }
}