package com.example.expensetrackerv1

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetrackerv1.feature.add_expense.EditExpense
import com.example.expensetrackerv1.feature.home.HomeScreen
import com.example.expensetrackerv1.feature.stats.StatsScreen
import com.example.expensetrackerv1.feature.transactionlist.TransactionListScreen
import com.example.expensetrackerv1.ui.theme.Zinc
import androidx.compose.ui.tooling.preview.Preview
import com.example.expensetrackerv1.data.model.ExpenseEntity
import kotlinx.serialization.json.Json

@Composable
fun NavHostScreen() {
    val navController = rememberNavController()
    var bottomBarVisibility by remember {
        mutableStateOf(true)

    }
    Scaffold(bottomBar = {
        AnimatedVisibility(visible = bottomBarVisibility) {
            NavigationBottomBar(
                navController = navController,
                items = listOf(
                    NavItem(route = "/home", icon = R.drawable.ic_home),
                    NavItem(route = "/stats", icon = R.drawable.ic_stats)
                )
            )
        }
    }) {
        NavHost(
            navController = navController,
            startDestination = "/home",
            modifier = Modifier.padding(it)
        ) {
            composable(route = "/home") {
                bottomBarVisibility = true
                HomeScreen(navController)
            }

            composable(route = "/add_income") {
                bottomBarVisibility = false
                EditExpense(navController = navController, operation = "Add",
                    isIncome = true)
            }

            composable(route = "/add_exp") {
                bottomBarVisibility = false
                EditExpense(navController = navController, operation = "Add",
                    isIncome = false)
            }

            composable(route = "/update_income_expense?expenseEntity={expenseEntity}") { backStackEntry ->
                val expenseEntityJson = backStackEntry.arguments?.getString("expenseEntity") ?: ""
                val expenseEntity = if (expenseEntityJson.isNotEmpty()) {
                    Json.decodeFromString<ExpenseEntity>(expenseEntityJson)
                } else {
                    null
                }
                if (expenseEntity != null) {
                    EditExpense(navController = navController, operation = "Update",isIncome = expenseEntity.type == "Income", expenseEntity = expenseEntity)
                }
            }

            composable(route = "/delete_income_expense?expenseEntity={expenseEntity}") { backStackEntry ->
                val expenseEntityJson = backStackEntry.arguments?.getString("expenseEntity") ?: ""
                val expenseEntity = if (expenseEntityJson.isNotEmpty()) {
                    Json.decodeFromString<ExpenseEntity>(expenseEntityJson)
                } else {
                    null
                }

                if (expenseEntity != null) {
                    EditExpense(navController = navController, operation = "Delete", isIncome = expenseEntity.type == "Income" , expenseEntity = expenseEntity)
                }
            }

            composable(route = "/stats") {
                bottomBarVisibility = true
                StatsScreen(navController)
            }
            composable(route = "/all_transactions") {
                bottomBarVisibility = true // Show the bottom bar if you want it visible
                TransactionListScreen(navController)
            }
        }
    }
}


data class NavItem(
    val route: String,
    val icon: Int
)

@Composable
fun NavigationBottomBar(
    navController: NavController,
    items: List<NavItem>
) {
    // Bottom Navigation Bar
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    BottomAppBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(painter = painterResource(id = item.icon), contentDescription = null)
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.White,
                    selectedTextColor = Zinc,
                    selectedIconColor = Zinc,
                    unselectedTextColor = Color.Gray,
                    unselectedIconColor = Color.Gray
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavHostScreenPreview() {
    NavHostScreen()
}