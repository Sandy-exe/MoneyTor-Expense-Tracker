package com.example.expensetrackerv1.utils

import com.example.expensetrackerv1.R
import com.example.expensetrackerv1.data.model.ExpenseEntity
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Utils {

    fun formatDateToHumanReadableForm(dateInMillis: Long): String {
        val dateFormatter = SimpleDateFormat("dd/MM/YYYY", Locale.getDefault())
        return dateFormatter.format(dateInMillis)
    }

    fun convertDateToMillis(dateString: String, format: String = "dd/MM/yyyy"): Long {
        val sdf = SimpleDateFormat(format)
        val date = sdf.parse(dateString)
        return date.time
    }



    fun formatDateForChart(dateInMillis: Long): String {
        val dateFormatter = SimpleDateFormat("dd-MMM", Locale.getDefault())
        return dateFormatter.format(dateInMillis)
    }

    fun formatCurrency(amount: Double, locale:  Locale = Locale("en", "IN")): String {
        val currencyFormatter = NumberFormat.getCurrencyInstance(locale)
        return currencyFormatter.format(amount)
    }

    fun formatDayMonthYear(dateInMillis: Long): String {
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormatter.format(dateInMillis)
    }

    fun formatDayMonth(dateInMillis: Long): String {
        val dateFormatter = SimpleDateFormat("dd/MMM", Locale.getDefault())
        return dateFormatter.format(dateInMillis)
    }

    fun formatToDecimalValue(d: Double): String {
        return String.format("%.2f", d)
    }

    fun formatStringDateToMonthDayYear(date: String): String {
        val millis = getMillisFromDate(date)
        return formatDayMonthYear(millis)
    }

    fun getMillisFromDate(date: String): Long {
        return getMilliFromDate(date)
    }

    fun getMilliFromDate(dateFormat: String?): Long {
        var date = Date()
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            date = dateFormat?.let { formatter.parse(it) }!!
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date.time
    }

    fun getItemIcon(item: ExpenseEntity): Int {
        return if (item.type == "Income") {
            R.drawable.ic_incomepng
        } else {
            R.drawable.ic_expensepng
        }
    }

}