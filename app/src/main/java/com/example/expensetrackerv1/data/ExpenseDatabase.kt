package com.example.expensetrackerv1.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetrackerv1.data.dao.ExpenseDao
import com.example.expensetrackerv1.data.model.ExpenseEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Database(entities = [ExpenseEntity::class], version = 2, exportSchema = false)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val DATABASE_NAME = "expense_database"

        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getInstance(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance

                // Sync Firebase data on first initialization
//                syncFirebaseData(context, instance)

                instance
            }
        }

//        private fun syncFirebaseData(context: Context, database: ExpenseDatabase) {
//            val expenseDao = database.expenseDao()
//            val firestore = FirebaseFirestore.getInstance()
//
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    // Fetch all data from Firebase
//                    val snapshot = firestore.collection("expenses").get().await()
//                    val firebaseExpenses = snapshot.toObjects(ExpenseEntity::class.java)
//
//                    // Fetch all local expenses from Room database
//                    val localExpenses = expenseDao.getAllExpense().first()
//
//                    // Combine local and Firebase expenses
//                    val mergedExpenses = unionExpenses(localExpenses, firebaseExpenses)
//
//                    // If there is any new data, update both Firebase and Room database
//                    if (mergedExpenses.isNotEmpty()) {
//                        // Insert the merged data into Firebase
//                        updateFirebaseExpenses(mergedExpenses, firestore)
//
//                        // Replace local database with the merged data
//                        expenseDao.replaceAll(mergedExpenses)
//                    }
//
//                } catch (e: Exception) {
//                    e.printStackTrace() // Log errors, e.g., no internet connection
//                }
//            }
//        }
//
//        // Union function to combine Firebase and local data, avoiding duplicates
//        private fun unionExpenses(localExpenses: List<ExpenseEntity>, firebaseExpenses: List<ExpenseEntity>): List<ExpenseEntity> {
//            // Use -1 as a placeholder for null ids (or any other value that's suitable)
//            val localExpenseMap = localExpenses.associateBy { it.id ?: -1 }
//            val firebaseExpenseMap = firebaseExpenses.associateBy { it.id ?: -1 }
//
//            // Union of both maps (combining local and Firebase expenses, and keeping the latest one)
//            val mergedMap = mutableMapOf<Int, ExpenseEntity>()
//
//            // Add all local expenses
//            mergedMap.putAll(localExpenseMap)
//
//            // Add all Firebase expenses, replacing local if the Firebase one is newer (optional logic)
//            firebaseExpenseMap.forEach { (id, expense) ->
//                mergedMap[id] = expense
//            }
//
//            return mergedMap.values.toList()
//        }
//
//
//        // Function to update Firebase with the merged data
//        private fun updateFirebaseExpenses(mergedExpenses: List<ExpenseEntity>, firestore: FirebaseFirestore) {
//            val batch = firestore.batch()
//
//            mergedExpenses.forEach { expense ->
//                val expenseRef = firestore.collection("expenses").document(expense.id.toString())
//                batch.set(expenseRef, expense)
//            }
//
//            batch.commit().addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    println("Firebase sync successful")
//                } else {
//                    task.exception?.printStackTrace()
//                }
//            }
//        }


    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS expense_table_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                type TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Step 2: Copy the data from the old table to the new table
        database.execSQL(
            """
            INSERT INTO expense_table_new (id, title, amount, date, type)
            SELECT id, title, amount, date, type FROM expense_table
            """.trimIndent()
        )

        // Step 3: Drop the old table
        database.execSQL("DROP TABLE expense_table")

        // Step 4: Rename the new table to the original table name
        database.execSQL("ALTER TABLE expense_table_new RENAME TO expense_table")
    }
}