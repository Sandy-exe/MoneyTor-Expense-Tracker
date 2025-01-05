package com.santhosh.expenseTracker.feature.add_expense

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.santhosh.expenseTracker.base.AddExpenseNavigationEvent
import com.santhosh.expenseTracker.base.BaseViewModel
import com.santhosh.expenseTracker.base.NavigationEvent
import com.santhosh.expenseTracker.base.UiEvent
import com.santhosh.expenseTracker.data.ExpenseDatabase
import com.santhosh.expenseTracker.data.dao.ExpenseDao
import com.santhosh.expenseTracker.data.model.ExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddExpenseViewModel @Inject constructor(private val dao: ExpenseDao) : BaseViewModel() {
    private suspend fun addExpense(expenseEntity: ExpenseEntity): Boolean {
        return try {
            dao.insertExpense(expenseEntity)
            true
        } catch (ex: Throwable) {
            false
        }
    }

    override fun onEvent(event: UiEvent) {
        when (event) {
            is AddExpenseUiEvent.OnAddExpenseClicked -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        val result = addExpense(event.expenseEntity)
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

class AddExpenseModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddExpenseViewModel::class.java)) {
            val expenseDao = ExpenseDatabase.getInstance(context).expenseDao()
            return AddExpenseViewModel(expenseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class AddExpenseUiEvent : UiEvent() {
    data class OnAddExpenseClicked(val expenseEntity: ExpenseEntity) : AddExpenseUiEvent()
    data object OnBackPressed : AddExpenseUiEvent()
    data object OnMenuClicked : AddExpenseUiEvent()
}


