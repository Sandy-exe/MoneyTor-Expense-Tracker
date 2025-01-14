package com.example.expensetrackerv1.data

import com.example.expensetrackerv1.data.dao.ExpenseDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import com.example.expensetrackerv1.data.model.ExpenseEntity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Context
import android.content.Intent
import com.example.expensetrackerv1.notification.SyncingNotificationReceiver

class FirebaseDatabase {

    private val firestore = Firebase.firestore

    fun sendSyncBroadcast(context: Context, message: String, title: String = "Syncing with Firebase") {
        val intent = Intent(context, SyncingNotificationReceiver::class.java).apply {
            putExtra("Text", message,)  // Add the message to the Intent
            putExtra("title", title,)
        }
        context.sendBroadcast(intent)  // Send the broadcast
    }

    suspend fun syncFirebaseData(expenseDao: ExpenseDao,context: Context,title: String="Syncing with Firebase") {
        try {
            sendSyncBroadcast(context, "Started Syncing",title)
            // Fetch all data from Firebase
            val snapshot = firestore.collection("expenses").get().await()
            val firebaseExpenses = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ExpenseEntity::class.java)
            }

  
            // Fetch all local expenses from Room database
            val localExpenses = expenseDao.getAllExpense().first()

            sendSyncBroadcast(context, "All Data Fetched...")
            println("All Data Fetched...")

            // Combine local and Firebase expenses
            val mergedExpenses = unionExpenses(localExpenses, firebaseExpenses)

            sendSyncBroadcast(context, "Union Performed...")
            println("Union Performed...")

            // If there is any new data, update both Firebase and Room database
            if (mergedExpenses.isNotEmpty()) {
                // Insert the merged data into Firebase
                updateFirebaseExpenses(mergedExpenses)

                // Replace local database with the merged data
                expenseDao.replaceAllExpenses(mergedExpenses)
            }

            sendSyncBroadcast(context, "Sync Complete")
            println("Sync Complete")

        } catch (e: Exception) {
            sendSyncBroadcast(context, "Un Expected Error")
            println("e")
            e.printStackTrace() // Log errors, e.g., no internet connection
        }
    }


    // Union function to combine Firebase and local data, avoiding duplicates
    private fun unionExpenses(localExpenses: List<ExpenseEntity>, firebaseExpenses: List<ExpenseEntity>): List<ExpenseEntity> {
        val localExpenseMap = localExpenses.associateBy { it.id ?: -1 }
        val firebaseExpenseMap = firebaseExpenses.associateBy { it.id ?: -1 }

        val mergedMap = mutableMapOf<Int, ExpenseEntity>()

        // Add all local expenses
        mergedMap.putAll(localExpenseMap)

        // Add all Firebase expenses, replacing local if Firebase one is newer
        firebaseExpenseMap.forEach { (id, firebaseExpense) ->
            mergedMap[id] = firebaseExpense
        }

        return mergedMap.values.toList()
    }

    // Create function
    suspend fun addExpense(expense: ExpenseEntity): Boolean {
        return try {
            firestore.collection("expenses")
                .add(expense)
                .await()
            true
        } catch (e: Exception) {
            println("e")
            false
        }
    }

    // Read all expenses
    suspend fun getAllExpenses(): List<ExpenseEntity> {
        return try {
            val snapshot = firestore.collection("expenses").get().await()
            val expenses = snapshot.documents.map { document ->
                document.toObject(ExpenseEntity::class.java)!!
            }
            expenses
        } catch (e: Exception) {
            println("e")
            emptyList()  // Return empty list in case of error
        }
    }

    // Read a single expense by ID (assuming "id" is used in Firestore as well)
    suspend fun getExpenseById(expenseId: Int): ExpenseEntity? {
        return try {
            val snapshot = firestore.collection("expenses")
                .whereEqualTo("id", expenseId)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(ExpenseEntity::class.java)
        } catch (e: Exception) {
            println("e")
            null
        }
    }

    // Function to update Firebase with the merged data
    private suspend fun updateFirebaseExpenses(mergedExpenses: List<ExpenseEntity>) {
        mergedExpenses.forEach { expense ->
            try {
                // Fetch the document with the matching id
                val snapshot = firestore.collection("expenses")
                    .whereEqualTo("id", expense.id)
                    .get()
                    .await()

                // Check if the document exists and update it
                if (snapshot.documents.isNotEmpty()) {
                    val documentId = snapshot.documents[0].id
                    firestore.collection("expenses").document(documentId)
                        .set(expense)
                        .await()
                } else {
                    // If document doesn't exist, create a new one with a new ID
                    val newExpenseRef = firestore.collection("expenses").document()
                    newExpenseRef.set(expense).await()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Update an existing expense
    suspend fun updateExpense(expense: ExpenseEntity): Boolean {
        return try {
            val snapshot = firestore.collection("expenses")
                .whereEqualTo("id", expense.id)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val documentId = snapshot.documents[0].id
                firestore.collection("expenses").document(documentId)
                    .set(expense)
                    .await()
                true
            } else {

                false
            }
        } catch (e: Exception) {
            println("e")
            false
        }
    }

    // Delete an expense by ID
    suspend fun deleteExpense(expenseId: Int): Boolean {
        return try {
            val snapshot = firestore.collection("expenses")
                .whereEqualTo("id", expenseId)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val documentId = snapshot.documents[0].id
                firestore.collection("expenses").document(documentId)
                    .delete()
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            println("e")
            false
        }
    }
}