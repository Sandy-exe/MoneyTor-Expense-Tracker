package com.example.expensetrackerv1.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expensetrackerv1.data.model.ExpensePendingSync
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpensePendingSyncDao {

    @Query("SELECT * FROM expense_pending_sync_table")
    fun getAllPendingSync(): Flow<List<ExpensePendingSync>>

    @Query("SELECT * FROM expense_pending_sync_table ORDER BY amount DESC LIMIT 5")
    fun getTopPendingSync(): Flow<List<ExpensePendingSync>>

    @Insert
    suspend fun insertPendingSync(expensePendingSync: ExpensePendingSync)

    @Insert
    suspend fun insertPendingSyncs(expensesPendingSync: List<ExpensePendingSync>)

    @Delete
    suspend fun deletePendingSync(expensePendingSync: ExpensePendingSync)

    @Update
    suspend fun updatePendingSync(expensePendingSync: ExpensePendingSync)

    @Query("DELETE FROM expense_pending_sync_table")
    suspend fun deleteAllPendingSync()

    @Transaction
    suspend fun replaceAllPendingSync(expenses: List<ExpensePendingSync>) {
        deleteAllPendingSync()
        insertPendingSyncs(expenses)
    }
}
