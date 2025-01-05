package com.santhosh.expenseTracker.feature.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.santhosh.expenseTracker.base.BaseViewModel
import com.santhosh.expenseTracker.base.UiEvent
import com.santhosh.expenseTracker.utils.Utils
import com.santhosh.expenseTracker.data.dao.ExpenseDao
import com.santhosh.expenseTracker.data.model.ExpenseSummary
import com.github.mikephil.charting.data.Entry
import com.santhosh.expenseTracker.data.ExpenseDatabase
import javax.inject.Inject

class StatsViewModel @Inject constructor(dao: ExpenseDao) : BaseViewModel() {
    val entries = dao.getAllExpenseByDate()
    val topEntries = dao.getTopExpenses()
    fun getEntriesForChart(entries: List<ExpenseSummary>): List<Entry> {
        val list = mutableListOf<Entry>()
        for (entry in entries) {
            val formattedDate = Utils.getMillisFromDate(entry.date)
            list.add(Entry(formattedDate.toFloat(), entry.total_amount.toFloat()))
        }
        return list
    }

    override fun onEvent(event: UiEvent) {
    }
}

class StatsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            val expenseDao = ExpenseDatabase.getInstance(context).expenseDao()
            return StatsViewModel(expenseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

