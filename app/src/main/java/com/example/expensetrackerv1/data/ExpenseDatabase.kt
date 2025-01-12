package com.example.expensetrackerv1.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetrackerv1.data.dao.ExpenseDao
import com.example.expensetrackerv1.data.dao.ExpensePendingSyncDao
import com.example.expensetrackerv1.data.model.ExpenseEntity
import com.example.expensetrackerv1.data.model.ExpensePendingSync

@Database(entities = [ExpenseEntity::class, ExpensePendingSync::class], version = 2, exportSchema = false)
abstract class ExpenseDatabase : RoomDatabase() {

    // DAO for expense operations
    abstract fun expenseDao(): ExpenseDao

    // DAO for expense pending sync operations
    abstract fun expensePendingSyncDao(): ExpensePendingSyncDao

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


                instance
            }
        }



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

        // Migrate expense_pending_sync_table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS expense_pending_sync_table_new (
                id INTEGER PRIMARY KEY,
                title TEXT NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                type TEXT NOT NULL,
                syncOperation TEXT NOT NULL,
                syncStatus INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Step 2: Copy the data from the old expense_pending_sync_table to the new expense_pending_sync_table_new
        database.execSQL(
            """
            INSERT INTO expense_pending_sync_table_new (id, title, amount, date, type,  syncOperation, syncStatus)
            SELECT id, title, amount, date, type, syncOperation, syncStatus FROM expense_pending_sync_table
            """.trimIndent()
        )

        // Step 3: Drop the old expense_pending_sync_table
        database.execSQL("DROP TABLE expense_pending_sync_table")

        // Step 4: Rename the new expense_pending_sync_table_new to expense_pending_sync_table
        database.execSQL("ALTER TABLE expense_pending_sync_table_new RENAME TO expense_pending_sync_table")
    }
}