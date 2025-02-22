@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetrackerv1.screens.add_expense

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.expensetrackerv1.R
import com.example.expensetrackerv1.base.NavigationEvent
import com.example.expensetrackerv1.utils.Utils
import com.example.expensetrackerv1.data.model.ExpenseEntity
import com.example.expensetrackerv1.ui.theme.InterFontFamily
import com.example.expensetrackerv1.ui.theme.LightGrey
import com.example.expensetrackerv1.ui.theme.Typography
import com.example.expensetrackerv1.ui.theme.Zinc
import com.example.expensetrackerv1.utils.connectivity.NetworkUtils
import com.example.expensetrackerv1.widget.ExpenseTextView

@Composable
fun EditExpense(
    navController: NavController,
    operation: String = "Add",
    isIncome: Boolean? = null,
    expenseEntity: ExpenseEntity? = null
) {
    val viewModel = EditExpenseModelFactory(LocalContext.current).create(EditExpenseViewModel::class.java)
    val menuExpanded = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                NavigationEvent.NavigateBack -> navController.popBackStack()
                else->{}
            }
        }
    }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (nameRow, card, topBar) = createRefs()
            Image(painter = painterResource(id = R.drawable.ic_topbar),
                contentDescription = null,
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                .constrainAs(nameRow) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
                Image(painter = painterResource(id = R.drawable.ic_back), contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable {
                            viewModel.onEvent(AddExpenseUiEvent.OnBackPressed)
                        })
                ExpenseTextView(
                    text = "$operation ${if (isIncome == true) "Income" else "Expense"}",
                    style = Typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                )


            }
            if (isIncome != null) {
                DataForm(modifier = Modifier.constrainAs(card) {
                    top.linkTo(nameRow.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, onAddExpenseClick = {
                        model, isOnline ->
                    viewModel.onEvent(AddExpenseUiEvent.OnAddExpenseClicked(model, isOnline))
                },
                    onUpdateExpenseClick = {
                        model, isOnline ->
                        viewModel.onEvent(AddExpenseUiEvent.OnUpdateExpenseClicked(model, isOnline))
                    },
                    onDeletedExpenseClick = {
                        model, isOnline ->
                        viewModel.onEvent(AddExpenseUiEvent.OnDeleteExpenseClicked(model,isOnline))
                    }
                    ,isIncome,operation,expenseEntity)
            }
        }
    }
}

@Composable
fun DataForm(
    modifier: Modifier,
    onAddExpenseClick: (model: ExpenseEntity, isOnline: Boolean) -> Unit,
    onUpdateExpenseClick: (model: ExpenseEntity, isOnline: Boolean) -> Unit,
    onDeletedExpenseClick: (model: ExpenseEntity, isOnline: Boolean) -> Unit,
    isIncome: Boolean,
    operation: String = "Add",
    expenseEntity: ExpenseEntity?
) {
    val name = remember { mutableStateOf(expenseEntity?.title ?: "") }
    val amount = remember { mutableStateOf(expenseEntity?.amount?.toString() ?: "") }
    val date = remember { mutableLongStateOf(expenseEntity?.let { Utils.convertDateToMillis(it.date) } ?: 0L) }
    val dateDialogVisibility = remember { mutableStateOf(false) }
    val type = remember { mutableStateOf(expenseEntity?.type ?: if (isIncome) "Income" else "Expense") }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .shadow(16.dp)
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TitleComponent(title = "name")

        if (operation == "Delete") {
            OutlinedTextField(
                value = name.value,
                enabled = false,
                onValueChange = {}, textStyle = TextStyle(color = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { ExpenseTextView(text = "Enter amount") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    disabledBorderColor = Color.Black, disabledTextColor = Color.Black,
                    disabledPlaceholderColor = Color.Black,
                    focusedTextColor = Color.Black,
                )
            )
        }
        else {
            ExpenseDropDown(
                if (isIncome) listOf(
                    "Paypal",
                    "Salary",
                    "Freelance",
                    "Investments",
                    "Bonus",
                    "Rental Income",
                    "Other Income"
                ) else listOf(
                    "Grocery",
                    "Netflix",
                    "Rent",
                    "Paypal",
                    "Starbucks",
                    "Shopping",
                    "Transport",
                    "Utilities",
                    "Dining Out",
                    "Entertainment",
                    "Healthcare",
                    "Insurance",
                    "Subscriptions",
                    "Education",
                    "Debt Payments",
                    "Gifts & Donations",
                    "Travel",
                    "Other Expenses"
                ),
                onItemSelected = {
                    name.value = it
                },
                onSelected = if (name.value == "") "Select Item" else name.value
            )
        }
        Spacer(modifier = Modifier.size(24.dp))
        TitleComponent("amount")
        val rupeeSymbol = stringResource(R.string.Rs)
        OutlinedTextField(
            value = amount.value,
            enabled = operation != "Delete",
            onValueChange = { newValue ->
                amount.value = newValue.filter { it.isDigit() || it == '.' }
            }, textStyle = TextStyle(color = Color.Black),
            visualTransformation = { text ->
                val out = rupeeSymbol + text.text
                val currencyOffsetTranslator = object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int {
                        return offset + 1
                    }

                    override fun transformedToOriginal(offset: Int): Int {
                        return if (offset > 0) offset - 1 else 0
                    }
                }

                TransformedText(AnnotatedString(out), currencyOffsetTranslator)
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { ExpenseTextView(text = "Enter amount") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                disabledBorderColor = Color.Black, disabledTextColor = Color.Black,
                disabledPlaceholderColor = Color.Black,
                focusedTextColor = Color.Black,
            )
        )

        Spacer(modifier = Modifier.size(24.dp))
        TitleComponent("date")
        OutlinedTextField(
            value = if (date.longValue == 0L) "" else Utils.formatDateToHumanReadableForm(
            date.longValue,
        ),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { dateDialogVisibility.value = operation != "Delete" },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Black, disabledTextColor = Color.Black,
                disabledPlaceholderColor = Color.Black,
            ),
            placeholder = { ExpenseTextView(text = (if (date.value == 0L) "Select date" else date.value).toString()) })
        Spacer(modifier = Modifier.size(24.dp))
        Button(
            onClick = {
                val model = ExpenseEntity(
                    expenseEntity?.id ?:null,
                    name.value,
                    amount.value.toDoubleOrNull() ?: 0.0,
                    Utils.formatDateToHumanReadableForm(date.longValue),
                    type.value,    
                )

                val isInternetAvailable = NetworkUtils().checkForInternet(context)
                println("Is internet available: $isInternetAvailable")

                when (operation) {
                    "Add" -> onAddExpenseClick(model,isInternetAvailable)
                    "Update" -> onUpdateExpenseClick(model,isInternetAvailable)
                    "Delete" -> onDeletedExpenseClick(model,isInternetAvailable)
                }

            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp) , colors = ButtonDefaults.buttonColors(
                containerColor = Zinc, // Background color
                contentColor = Color.White // Text and icon color
            )
        ) {
            ExpenseTextView(
                text ="$operation ${if (isIncome) "Income" else "Expense"}",
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
    if (dateDialogVisibility.value) {
        ExpenseDatePickerDialog(onDateSelected = {
            date.longValue = it
            dateDialogVisibility.value = false
        }, onDismiss = {
            dateDialogVisibility.value = false
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDatePickerDialog(
    onDateSelected: (date: Long) -> Unit, onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis ?: 0L
    DatePickerDialog(onDismissRequest = { onDismiss() }, confirmButton = {
        TextButton(onClick = { onDateSelected(selectedDate) }) {
            ExpenseTextView(text = "Confirm")
        }
    }, dismissButton = {
        TextButton(onClick = { onDateSelected(selectedDate) }) {
            ExpenseTextView(text = "Cancel")
        }
    }) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun TitleComponent(title: String) {
    ExpenseTextView(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = LightGrey
    )
    Spacer(modifier = Modifier.size(10.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDropDown(listOfItems: List<String>, onItemSelected: (item: String) -> Unit,onSelected: String) {
    val expanded = remember {
        mutableStateOf(false)
    }
    val selectedItem = remember {
        mutableStateOf(onSelected)
    }

    ExposedDropdownMenuBox(expanded =  expanded.value, onExpandedChange = { expanded.value = it }) {
        OutlinedTextField(
            value = selectedItem.value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
            textStyle = TextStyle(fontFamily = InterFontFamily, color = Color.Black),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                disabledBorderColor = Color.Black, disabledTextColor = Color.Black,
                disabledPlaceholderColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,

            )
        )
        ExposedDropdownMenu(expanded = expanded.value, onDismissRequest = { }) {
            listOfItems.forEach {
                DropdownMenuItem(text = { ExpenseTextView(text = it) }, onClick = {
                    selectedItem.value = it
                    onItemSelected(selectedItem.value)
                    expanded.value = false
                })
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    EditExpense(rememberNavController(),operation = "Add",
        isIncome = false,
        expenseEntity = null)
}