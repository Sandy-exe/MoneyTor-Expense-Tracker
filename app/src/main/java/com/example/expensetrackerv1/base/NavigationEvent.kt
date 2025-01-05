package com.example.expensetrackerv1.base

sealed class NavigationEvent {
    data object NavigateBack : NavigationEvent()
}

sealed class AddExpenseNavigationEvent : NavigationEvent() {
    data object MenuOpenedClicked : AddExpenseNavigationEvent()
}

sealed class HomeNavigationEvent : NavigationEvent() {
    data object NavigateToAddExpense : HomeNavigationEvent()
    data object NavigateToAddIncome : HomeNavigationEvent()
    data object NavigateToSeeAll : HomeNavigationEvent()
}