package com.example.expensetrackerv1

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.expensetrackerv1.background.SyncWorker
import com.example.expensetrackerv1.notification.SyncingNotificationService
import com.example.expensetrackerv1.ui.theme.ExpenseTrackerAndroidTheme
import com.example.expensetrackerv1.ui.theme.Zinc
import java.time.Duration
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    //Setting UP Database
    private val syncViewModel: SyncViewModel by viewModels {
        SyncViewModelFactory(application)  // Pass Application, not Context
    }

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

        //Initialize Notification
        SyncingNotificationService(applicationContext)

        //Initial Sync
        syncViewModel.performInitialSync()







        setContent {
            ExpenseTrackerAndroidTheme {

                //Background Sync
                LaunchedEffect(key1 = Unit) {


                    val workManager = WorkManager.getInstance(applicationContext)


                    val workTag = "SyncJOB"

                    // Check if there's already a work request with the same tag
                    val existingWork = workManager.getWorkInfosByTag(workTag).get()
                    if (existingWork.isEmpty() || existingWork.none {
                            it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
                        }) {
                        val constraints = Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()

                        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                            repeatInterval = 15,
                            repeatIntervalTimeUnit = TimeUnit.MINUTES // Use a reasonable interval
                        ).addTag(workTag) // Add a unique tag to this work request
                            .setConstraints(constraints)
                            .setBackoffCriteria(
                                backoffPolicy = BackoffPolicy.LINEAR,
                                duration = Duration.ofSeconds(15)
                            )
                            .build()

                        workManager.enqueue(workRequest)
                    }

                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SyncContent(syncViewModel)
                }

            }
        }

    }

    private fun notificationRequest() {
        // Check for permission on Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermission = android.Manifest.permission.POST_NOTIFICATIONS

            // Check if the permission is granted
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // Permission already granted, proceed with showing notifications
                println("Notification permission already granted")
            } else {
                // Request notification permission
                notificationPermissionRequest.launch(notificationPermission)
            }
        } else {
            // For devices below Android 13, no need to request notification permission
            println("Notification permission not required for this device")
        }
    }


}

@Composable
fun SyncContent(syncViewModel: SyncViewModel) {
    val syncState by syncViewModel.syncState.collectAsState()

    when (syncState) {
        SyncState.InProgress -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = Zinc
                )
            }
        }
        SyncState.Success -> {
            NavHostScreen()
        }
        else -> {}
    }
}