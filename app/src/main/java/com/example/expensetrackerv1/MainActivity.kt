package com.example.expensetrackerv1

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import com.example.expensetrackerv1.notification.SyncingNotificationService
import com.example.expensetrackerv1.ui.theme.ExpenseTrackerAndroidTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {


    // Register for permission result
    private val notificationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, proceed with notifications
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //Request for Notification
        notificationRequest()

        val service = SyncingNotificationService(applicationContext)
        setContent {
            ExpenseTrackerAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHostScreen()
                }

            }
        }

    }

    fun notificationRequest() {
        // Check for permission on Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermission = android.Manifest.permission.POST_NOTIFICATIONS

            // Check if the permission is granted
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // Permission already granted, proceed with showing notifications
                Toast.makeText(this, "Notifications are enabled", Toast.LENGTH_SHORT).show()
            } else {
                // Request notification permission
                notificationPermissionRequest.launch(notificationPermission)
            }
        } else {
            // For devices below Android 13, no need to request notification permission
            Toast.makeText(this, "No need for notification permission below Android 13", Toast.LENGTH_SHORT).show()
        }
    }




}