package com.example.expensetrackerv1.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import com.example.expensetrackerv1.data.model.ExpenseEntity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class FirebaseDatabase {

    private val firestore = Firebase.firestore

    suspend fun syncFirebaseData(database: ExpenseDatabase) {
        val expenseDao = database.expenseDao()

        try {
            // Fetch all data from Firebase
            val snapshot = firestore.collection("expenses").get().await()
            val firebaseExpenses = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ExpenseEntity::class.java)
            }

            // Fetch all local expenses from Room database
            val localExpenses = expenseDao.getAllExpense().first()

            // Combine local and Firebase expenses
            val mergedExpenses = unionExpenses(localExpenses, firebaseExpenses)

            // If there is any new data, update both Firebase and Room database
            if (mergedExpenses.isNotEmpty()) {
                // Insert the merged data into Firebase
                updateFirebaseExpenses(mergedExpenses)

                // Replace local database with the merged data
                expenseDao.replaceAllExpenses(mergedExpenses)
            }

        } catch (e: Exception) {
            println(e)
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

    // Function to update Firebase with the merged data
    private fun updateFirebaseExpenses(mergedExpenses: List<ExpenseEntity>) {
        val batch = firestore.batch()

        mergedExpenses.forEach { expense ->
            val expenseRef = firestore.collection("expenses").document(expense.id?.toString() ?: UUID.randomUUID().toString())
            batch.set(expenseRef, expense)
        }

        batch.commit().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("Firebase sync successful")
            } else {
                task.exception?.printStackTrace()
            }
        }
    }

    // Create function
    suspend fun addExpense(expense: ExpenseEntity): Boolean {
        return try {
            firestore.collection("expenses")
                .add(expense)
                .await()
            true
        } catch (e: Exception) {
            println(e)
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
            println(e)
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
            println(e)
            null
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
            println(e)
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
            println(e)
            false
        }
    }
}