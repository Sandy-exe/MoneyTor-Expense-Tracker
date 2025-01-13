package com.example.expensetrackerv1.background

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expensetrackerv1.data.ExpenseDatabase
import com.example.expensetrackerv1.data.FirebaseDatabase
import com.example.expensetrackerv1.data.model.ExpenseEntity
import com.example.expensetrackerv1.data.model.ExpensePendingSync
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class SyncWorker(
    private val appContext: Context,
    private val workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {



    override suspend fun doWork(): Result {
        val database = ExpenseDatabase.getInstance(appContext)
        val dao = database.expensePendingSyncDao()
        val firestore = FirebaseDatabase()

        delay(5000)
            // Get all pending sync entries
            val pendingSyncs = dao.getAllPendingSync().first()
            println(pendingSyncs)
            for (sync in pendingSyncs) {
                when (sync.syncOperation) {
                    "INSERT" -> handleInsert(sync,firestore)
                    "UPDATE" -> handleUpdate(sync,firestore)
                    "DELETE" -> handleDelete(sync,firestore)
                    else -> handleUnknownOperation(sync)
                }

                // Delete the processed entry from the table
                dao.deletePendingSync(sync)
            }

            Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, "Database Up-to-date!!", Toast.LENGTH_SHORT).show()
            }
            return Result.success()
    }

    private suspend fun handleInsert(sync: ExpensePendingSync,firestore: FirebaseDatabase) {
        firestore.addExpense(
            ExpenseEntity(
                id = sync.id,
                title = sync.title,
                type = sync.title,
                amount = sync.amount,
                date = sync.date
            )
        )
        // Perform the insert operation
        println("Inserted ${sync.title}")

    }

    private suspend fun handleUpdate(sync: ExpensePendingSync,firestore: FirebaseDatabase) {

        firestore.updateExpense(
            ExpenseEntity(
                id = sync.id,
                title = sync.title,
                type = sync.title,
                amount = sync.amount,
                date = sync.date
            )
        )

        println("Updated ${sync.title}")
        // Add your logic here
    }

    private suspend fun handleDelete(sync: ExpensePendingSync,firestore: FirebaseDatabase) {

        sync.id?.let {
            firestore.deleteExpense(
                it
            )
        }

        // Perform the delete operation
        println("Deleted ${sync.title}")
        // Add your logic here
    }

    private fun handleUnknownOperation(sync: ExpensePendingSync) {
        println("Unknown operation: ${sync.syncOperation}")
    }
}
