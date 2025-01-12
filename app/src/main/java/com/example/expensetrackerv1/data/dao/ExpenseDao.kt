package com.example.expensetrackerv1.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expensetrackerv1.data.model.ExpenseEntity
import com.example.expensetrackerv1.data.model.ExpenseSummary
import com.example.expensetrackerv1.data.model.ExpensePendingSync
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    // Expense Table Operations

    @Query("SELECT * FROM expense_table")
    fun getAllExpense(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense_table WHERE type = 'Expense' ORDER BY amount DESC LIMIT 5")
    fun getTopExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT type, date, SUM(amount) AS total_amount FROM expense_table where type = :type GROUP BY type, date ORDER BY date")
    fun getAllExpenseByDate(type: String = "Expense"): Flow<List<ExpenseSummary>>

    @Query("SELECT * FROM expense_table ORDER BY id DESC LIMIT 1")
    fun getLastExpense(): ExpenseEntity

    @Query("SELECT * FROM expense_table WHERE id = :id")
    fun getExpenseById(id: Int): Flow<ExpenseEntity>

    @Insert
    suspend fun insertExpense(expenseEntity: ExpenseEntity)

    @Insert
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Delete
    suspend fun deleteExpense(expenseEntity: ExpenseEntity)

    @Update
    suspend fun updateExpense(expenseEntity: ExpenseEntity)

    @Query("DELETE FROM expense_table")
    suspend fun deleteAllExpenses()

    @Transaction
    suspend fun replaceAllExpenses(expenses: List<ExpenseEntity>) {
        deleteAllExpenses()
        insertExpenses(expenses)
    }
}