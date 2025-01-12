package com.example.expensetrackerv1.feature.transactionlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensetrackerv1.R
import com.example.expensetrackerv1.feature.add_expense.ExpenseDropDown
import com.example.expensetrackerv1.feature.home.TransactionItem
import com.example.expensetrackerv1.utils.Utils
import com.example.expensetrackerv1.feature.home.HomeViewModel
import com.example.expensetrackerv1.feature.home.HomeViewModelFactory
import com.example.expensetrackerv1.ui.theme.DarkGreen
import com.example.expensetrackerv1.widget.ExpenseTextView
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TransactionListScreen(navController: NavController) {
    val viewModel : HomeViewModel = HomeViewModelFactory(LocalContext.current).create(HomeViewModel::class.java)
    val state = viewModel.expenses.collectAsState(initial = emptyList())
    var filterType by remember { mutableStateOf("All") }
    var dateRange by remember { mutableStateOf("All Time") }
    var menuExpanded by remember { mutableStateOf(false) }

    val filteredTransactions = when (filterType) {

        "Expense" -> state.value.filter { it.type == "Expense" }
        "Income" -> state.value.filter { it.type == "Income" }
        else -> state.value
    }

    val filteredByDateRange = filteredTransactions.filter { transaction ->
        // Apply date range filter logic here
        val transactionDateInMillis = try {
            Utils.getMilliFromDate(transaction.date) // Convert the transaction date to milliseconds
        } catch (e: Exception) {
            null // Handle invalid date formats gracefully
        }

        // Get the current date in milliseconds

        val currentDateInMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Apply date range filter logic
        val dateRangeStartInMillis = when (dateRange) {
            "Yesterday" -> currentDateInMillis - 24 * 60 * 60 * 1000 // Subtract 1 day in milliseconds
            "Today" -> currentDateInMillis
            "Last 30 Days" -> currentDateInMillis - 30L * 24 * 60 * 60 * 1000 // Subtract 30 days
            "Last 90 Days" -> currentDateInMillis - 90L * 24 * 60 * 60 * 1000 // Subtract 90 days
            "Last Year" -> currentDateInMillis - 365L * 24 * 60 * 60 * 1000 // Subtract 365 days
            else -> null // No date range filter
        }

        // If the date range is valid, apply the filter
        if (dateRangeStartInMillis != null && transactionDateInMillis != null) {

            transactionDateInMillis >= dateRangeStartInMillis && transactionDateInMillis <= currentDateInMillis
        } else {
            true // If no date range filter is selected or invalid, include the transaction
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                // Back Button
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { navController.popBackStack() },
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Black)
                )

                // Title
                ExpenseTextView(
                    text = "Transactions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { menuExpanded = !menuExpanded },
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Black)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Content area for the transaction list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    // Dropdowns
                    AnimatedVisibility(
                        visible = menuExpanded,
                        enter = slideInVertically(initialOffsetY = { -it / 2 }),
                        exit = slideOutVertically(targetOffsetY = { -it  }),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Column {
                            // Type Filter Dropdown
                            ExpenseDropDown(
                                listOfItems = listOf("All", "Expense", "Income"),
                                onItemSelected = { selected ->
                                    filterType = selected
                                    menuExpanded = true // Close menu after selection
                                },
                                onSelected = "Select Filter"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Date Range Filter Dropdown
                            ExpenseDropDown(
                                listOfItems = listOf( "Yesterday", "Today", "Last 30 Days", "Last 90 Days", "Last Year"),
                                onItemSelected = { selected ->
                                    dateRange = selected
                                    menuExpanded = true// Close menu after selection
                                },
                                onSelected = "Select Time"
                            )
                        }
                    }
                }
                items(filteredByDateRange) { item ->
                    val icon = Utils.getItemIcon(item)
                    val amount = if (item.type == "Income") item.amount else item.amount * -1
                    TransactionItem(
                        title = item.title,
                        amount = amount.toString(),
                        icon = icon,
                        date = item.date,
                        color = DarkGreen,
                        Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                            placementSpec = tween(100)
                        )
                    )
                }
            }
        }
    }
}
