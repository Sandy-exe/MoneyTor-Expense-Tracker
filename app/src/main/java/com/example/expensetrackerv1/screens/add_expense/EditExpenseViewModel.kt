package com.example.expensetrackerv1.screens.add_expense

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerv1.base.AddExpenseNavigationEvent
import com.example.expensetrackerv1.base.BaseViewModel
import com.example.expensetrackerv1.base.NavigationEvent
import com.example.expensetrackerv1.base.UiEvent
import com.example.expensetrackerv1.data.ExpenseDatabase
import com.example.expensetrackerv1.data.FirebaseDatabase
import com.example.expensetrackerv1.data.dao.ExpenseDao
import com.example.expensetrackerv1.data.dao.ExpensePendingSyncDao
import com.example.expensetrackerv1.data.model.ExpenseEntity
import com.example.expensetrackerv1.data.model.ExpensePendingSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditExpenseViewModel(private val dao: ExpenseDao, private val expensePendingSyncDao: ExpensePendingSyncDao) : BaseViewModel() {

    private val firestore = FirebaseDatabase()
    private suspend fun addToPendingSync(expenseEntity: ExpenseEntity, operation: String) {
        println("ExpenseEntity : $expenseEntity")
        val expensePending = ExpensePendingSync(
            tableId = null,
            id = requireNotNull(expenseEntity.id),
            title = expenseEntity.title,
            amount = expenseEntity.amount,
            date = expenseEntity.date,
            type = expenseEntity.type,
            syncOperation = operation,
            syncStatus = false
        )

        expensePendingSyncDao.insertPendingSync(expensePending)
    }

    private suspend fun addExpense(expenseEntity: ExpenseEntity,isOnline: Boolean): Boolean {
        return try {
            dao.insertExpense(expenseEntity)
            println("Expense Added successfully")

            val lastExpense = dao.getLastExpense()

            // Check for internet connectivity
            if (isOnline) {
                try {
                    // Attempt to add expense to Firestore
                    val firebaseStatus = firestore.addExpense(lastExpense)

                    if (!firebaseStatus) {
                        // Add to pending sync table if Firestore operation fails
                        addToPendingSync(lastExpense, "INSERT")
                        println("Expense added to Pending Table due to Firestore failure.")
                    } else {
                        println("Expense successfully added to Firestore.")
                    }
                } catch (e: Exception) {
                    // Handle exceptions and add to pending sync table
                    addToPendingSync(lastExpense, "INSERT")
                    println("Error adding to Firestore: ${e.message}. Added to Pending Table.")
                }
            } else {
                // No internet connection, add to pending sync table
                addToPendingSync(lastExpense, "INSERT")
                println("No internet connection. Expense added to Pending Table.")
            }

            true
        } catch (ex: Throwable) {
            false
        }
    }



    private suspend fun updateExpense(expenseEntity: ExpenseEntity, isOnline: Boolean): Boolean {
        return try {
            dao.updateExpense(expenseEntity)
            println("Expense updated successfully")

            if (isOnline) {
                try {
                    // Attempt to update the expense in Firestore
                    val firebaseStatus = firestore.updateExpense(expenseEntity)

                    if (!firebaseStatus) {
                        // Add to pending sync table if Firestore operation fails
                        addToPendingSync(expenseEntity, "UPDATE")
                        println("Expense added to Pending Table due to Firestore failure.")
                    } else {
                        println("Expense successfully updated in Firestore.")
                    }
                } catch (e: Exception) {
                    // Handle exceptions and add to pending sync table
                    addToPendingSync(expenseEntity, "UPDATE")
                    println("Error updating in Firestore: ${e.message}. Added to Pending Table.")
                }
            } else {
                // No internet connection, add to pending sync table
                addToPendingSync(expenseEntity, "UPDATE")
                println("No internet connection. Expense updated to Pending Table.")
            }
            true
        } catch (ex: Throwable) {
            println("Error updating expense: ${ex.message}")
            false
        }
    }

    private suspend fun deleteExpense(expenseEntity: ExpenseEntity, isOnline: Boolean): Boolean {
        return try {
            dao.deleteExpense(expenseEntity)
            println("Expense deleted successfully")

            if (isOnline) {
                try {
                    // Attempt to delete the expense in Firestore
                    val firebaseStatus = expenseEntity.id?.let { firestore.deleteExpense(expenseEntity.id.toInt()) }

                    if (!firebaseStatus!!) {
                        // Add to pending sync table if Firestore operation fails
                        addToPendingSync(expenseEntity, "DELETE")
                        println("Expense added to Pending Table due to Firestore failure.")
                    } else {
                        println("Expense successfully deleted from Firestore.")
                    }
                } catch (e: Exception) {
                    // Handle exceptions and add to pending sync table
                    addToPendingSync(expenseEntity, "DELETE")
                    println("Error deleting in Firestore: ${e.message}. Added to Pending Table.")
                }
            } else {
                // No internet connection, add to pending sync table
                addToPendingSync(expenseEntity, "DELETE")
                println("No internet connection. Expense added to Pending Table.")
            }

            true
        } catch (ex: Throwable) {
            println("Error deleting expense: ${ex.message}")
            false
        }
    }


    override fun onEvent(event: UiEvent) {
        when (event) {
            is AddExpenseUiEvent.OnAddExpenseClicked -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        val result = addExpense(event.expenseEntity,event.isOnline)
                        if (result) {
                            _navigationEvent.emit(NavigationEvent.NavigateBack)
                        }
                    }
                }
            }

            is AddExpenseUiEvent.OnUpdateExpenseClicked -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        val result = updateExpense(event.expenseEntity,event.isOnline)
                        if (result) {
                            _navigationEvent.emit(NavigationEvent.NavigateBack)
                        }
                    }
                }
            }

            is AddExpenseUiEvent.OnDeleteExpenseClicked -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        val result = deleteExpense(event.expenseEntity,event.isOnline)
                        if (result) {
                            _navigationEvent.emit(NavigationEvent.NavigateBack)
                        }
                    }
                }
            }

            is AddExpenseUiEvent.OnBackPressed -> {
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                }
            }

            is AddExpenseUiEvent.OnMenuClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(AddExpenseNavigationEvent.MenuOpenedClicked)
                }
            }
        }
    }


}

class EditExpenseModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditExpenseViewModel::class.java)) {
            val expenseDao = ExpenseDatabase.getInstance(context).expenseDao()
            val expensePendingSyncDao = ExpenseDatabase.getInstance(context).expensePendingSyncDao()
            return EditExpenseViewModel(expenseDao,expensePendingSyncDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class AddExpenseUiEvent : UiEvent() {
    data class OnAddExpenseClicked(val expenseEntity: ExpenseEntity,val isOnline: Boolean) : AddExpenseUiEvent()
    data class OnUpdateExpenseClicked(val expenseEntity: ExpenseEntity,val isOnline: Boolean) : AddExpenseUiEvent()
    data class OnDeleteExpenseClicked(val expenseEntity: ExpenseEntity,val isOnline: Boolean) : AddExpenseUiEvent()
    data object OnBackPressed : AddExpenseUiEvent()
    data object OnMenuClicked : AddExpenseUiEvent()
}


