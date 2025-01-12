package com.example.expensetrackerv1.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SyncingNotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val text = intent?.getStringExtra("Text") ?: ""
        val titleText = intent?.getStringExtra("title") ?: ""
        val service = SyncingNotificationService(context)
        service.showNotification(text, titleText)
    }
}