package com.example.expensetrackerv1.feature.home

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerv1.base.BaseViewModel
import com.example.expensetrackerv1.base.HomeNavigationEvent
import com.example.expensetrackerv1.base.UiEvent
import com.example.expensetrackerv1.data.ExpenseDatabase
import com.example.expensetrackerv1.data.FirebaseDatabase
import com.example.expensetrackerv1.utils.Utils
import com.example.expensetrackerv1.data.dao.ExpenseDao
import com.example.expensetrackerv1.data.model.ExpenseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch

class HomeViewModel(database: ExpenseDatabase,context: Context) : BaseViewModel() {
    private val dao: ExpenseDao = database.expenseDao()
    val expenses = dao.getAllExpense()
    private val firestore = FirebaseDatabase()
    val context = context

    suspend fun firebaseSync() {
        firestore.syncFirebaseData(dao, context = context)
    }

    override fun onEvent(event: UiEvent) {
        when (event) {
            is HomeUiEvent.OnAddExpenseClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToAddExpense)
                }
            }

            is HomeUiEvent.OnAddIncomeClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToAddIncome)
                }
            }

            is HomeUiEvent.OnSeeAllClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToSeeAll)
                }
            }
        }
    }



    fun getBalance(list: List<ExpenseEntity>): String {
        var balance = 0.0
        for (expense in list) {
            if (expense.type == "Income") {
                balance += expense.amount
            } else {
                balance -= expense.amount
            }
        }
        return Utils.formatCurrency(balance)
    }

    fun getTotalExpense(list: List<ExpenseEntity>): String {
        var total = 0.0
        for (expense in list) {
            if (expense.type != "Income") {
                total += expense.amount
            }
        }

        return Utils.formatCurrency(total)
    }

    fun getTotalIncome(list: List<ExpenseEntity>): String {
        var totalIncome = 0.0
        for (expense in list) {
            if (expense.type == "Income") {
                totalIncome += expense.amount
            }
        }
        return Utils.formatCurrency(totalIncome)
    }

}

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    private val firestore = FirebaseDatabase()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val database = ExpenseDatabase.getInstance(context)

            //here code the first time thing

            // Perform first-time logic (Firebase sync)
            if (isFirstRun()) {
                // Run your first-time setup logic (like Firebase sync)
                CoroutineScope(Dispatchers.IO).launch {
                    runFirebaseSync(database, context)
                }

                // Mark sync as completed
                markSyncAsCompleted()
            }



            return HomeViewModel(database,context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    // Check if it's the first run
    private fun isFirstRun(): Boolean {
        return sharedPreferences.getBoolean("first_run", true)
    }

    // Mark sync as completed in SharedPreferences
    private fun markSyncAsCompleted() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("first_run", false)
        editor.apply()
    }

    // Function to run Firebase sync
    private suspend fun runFirebaseSync(database: ExpenseDatabase, context: Context) {
        // Run Firebase sync logic here

        firestore.syncFirebaseData(database.expenseDao(),context)

        // You can call your Firebase sync logic here, for example:
        // firestore.syncFirebaseData(context)  (this is just a placeholder, implement actual sync logic)
    }
}

sealed class HomeUiEvent : UiEvent() {
    data object OnAddExpenseClicked : HomeUiEvent()
    data object OnAddIncomeClicked : HomeUiEvent()
    data object OnSeeAllClicked : HomeUiEvent()
}
