package com.example.expensetrackerv1.feature.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetrackerv1.base.BaseViewModel
import com.example.expensetrackerv1.base.UiEvent
import com.example.expensetrackerv1.utils.Utils
import com.example.expensetrackerv1.data.dao.ExpenseDao
import com.example.expensetrackerv1.data.model.ExpenseSummary
import com.github.mikephil.charting.data.Entry
import com.example.expensetrackerv1.data.ExpenseDatabase

class StatsViewModel constructor(dao: ExpenseDao) : BaseViewModel() {
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

