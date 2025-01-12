package com.example.expensetrackerv1.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.constraintlayout.compose.Dimension
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.expensetrackerv1.R
import com.example.expensetrackerv1.base.HomeNavigationEvent
import com.example.expensetrackerv1.base.NavigationEvent
import com.example.expensetrackerv1.data.model.ExpenseEntity
import com.example.expensetrackerv1.ui.theme.DarkGreen
import com.example.expensetrackerv1.ui.theme.Zinc
import com.example.expensetrackerv1.widget.ExpenseTextView
import com.example.expensetrackerv1.ui.theme.LightGrey
import com.example.expensetrackerv1.ui.theme.Typography
import com.example.expensetrackerv1.utils.Utils

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.unit.Dp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Composable
fun HomeScreen(navController: NavController){
    val viewModel : HomeViewModel = HomeViewModelFactory(LocalContext.current).create(HomeViewModel::class.java)
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                NavigationEvent.NavigateBack -> navController.popBackStack()
                HomeNavigationEvent.NavigateToSeeAll -> {
                    navController.navigate("/all_transactions")
                }

                HomeNavigationEvent.NavigateToAddIncome -> {
                    navController.navigate("/add_income")
                }

                HomeNavigationEvent.NavigateToAddExpense -> {
                    navController.navigate("/add_exp")
                }

                else -> {}
            }
        }
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (nameRow, list, card, topBar, add) = createRefs()
            Image(painter = painterResource(id = R.drawable.ic_topbar), contentDescription = null,
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
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    ExpenseTextView(
                        text = "Good Afternoon",
                        style = Typography.bodyMedium,
                        color = Color.White
                    )
                    ExpenseTextView(
                        text = "Santhosh Kumar S",
                        style = Typography.titleLarge,
                        color = Color.White
                    )
                }

            }
            val state = viewModel.expenses.collectAsState(initial = emptyList())
            val expense = viewModel.getTotalExpense(state.value)
            val income = viewModel.getTotalIncome(state.value)
            val balance = viewModel.getBalance(state.value)
            CardItem(
                modifier = Modifier.constrainAs(card) {
                    top.linkTo(nameRow.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                balance = balance, income = income, expense = expense
            )

            if (state.value.isEmpty()) {
                // Show image if state.value is empty
                Image(
                    painter = painterResource(id = R.drawable.no_data_png), // Replace with your empty list image
                    contentDescription = "No Data",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp)
                        .padding(bottom = 50.dp)
                        .constrainAs(list) {
                            top.linkTo(card.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }
                )
            } else {

            TransactionList(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(list) {
                        top.linkTo(card.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    }, list = state.value, onSeeAllClicked = {
                    viewModel.onEvent(HomeUiEvent.OnSeeAllClicked)
                },
                navController = navController
            )

            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(add) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }, contentAlignment = Alignment.BottomEnd
            ) {
                MultiFloatingActionButton(modifier = Modifier, {
                    viewModel.onEvent(HomeUiEvent.OnAddExpenseClicked)
                }, {
                    viewModel.onEvent(HomeUiEvent.OnAddIncomeClicked)
                })
            }
        }
    }
}


@Composable
fun CardItem(
    modifier: Modifier,
    balance: String, income: String, expense: String
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Zinc)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column {
                ExpenseTextView(
                    text = "Total Balance",
                    style = Typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.size(8.dp))
                ExpenseTextView(
                    text = balance, style = Typography.headlineLarge, color = Color.White,
                )
            }

        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CardRowItem(
                modifier = Modifier
                    .align(Alignment.CenterStart),
                title = "Income",
                amount = income,
                image = R.drawable.ic_income
            )
            Spacer(modifier = Modifier.size(8.dp))
            CardRowItem(
                modifier = Modifier
                    .align(Alignment.CenterEnd),
                title = "Expense",
                amount = expense,
                image = R.drawable.ic_expense
            )
        }

    }
}


@Composable
fun ActionIcon(
    onClick: () -> Unit,
    backgroundColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.White,
    padding: Dp = 16.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10)) // Rounded button
            .background(backgroundColor)
            .clickable(
                onClick = onClick,
            )
            .padding(padding)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

@Composable
fun SwippableItemWithActions(
    isRevealedLeft: Boolean,
    isRevealedRight: Boolean,
    beforeContentActions: @Composable RowScope.() -> Unit = {}, // Actions for left swipe
    afterContentActions: @Composable RowScope.() -> Unit = {},  // Actions for right swipe
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var beforeActionsWidth by remember { mutableFloatStateOf(0f) }
    var afterActionsWidth by remember { mutableFloatStateOf(0f) }
    val offset = remember { Animatable(0f) } // 0: content, +ve: right swipe, -ve: left swipe
    val scope = rememberCoroutineScope()

    // Sync offset with the revealed state
    LaunchedEffect(key1 = isRevealedLeft, key2 = isRevealedRight) {
        if (isRevealedLeft) {
            offset.animateTo(-beforeActionsWidth)
        } else if (isRevealedRight) {
            offset.animateTo(afterActionsWidth)
        } else {
            offset.animateTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Actions behind the content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    beforeActionsWidth = it.width.toFloat() / 5 // Left swipe area width
                    afterActionsWidth = it.width.toFloat() / 5 // Right swipe area width
                },
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Left swipe actions (before content)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .height(IntrinsicSize.Min),

                contentAlignment = Alignment.Center
            ) {

                Row(){beforeContentActions()}
            }

            Spacer(modifier = Modifier.weight(1f))

            // Right swipe actions (after content)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .height(IntrinsicSize.Min),
                contentAlignment = Alignment.Center
            ) {

                Row(){afterContentActions()}

            }
        }

        // Foreground content
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newOffset = (offset.value + dragAmount)
                                    .coerceIn(-beforeActionsWidth, afterActionsWidth)
                                offset.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offset.value <= -beforeActionsWidth / 4 -> {
                                        offset.animateTo(-beforeActionsWidth)
                                    }
                                    offset.value >= afterActionsWidth / 4 -> {
                                        offset.animateTo(afterActionsWidth)
                                    }
                                    else -> {
                                        offset.animateTo(0f)
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}




@Composable
fun TransactionList(
    modifier: Modifier,
    list: List<ExpenseEntity>,
    title: String = "Recent Transactions",
    onSeeAllClicked: () -> Unit,
    navController: NavController
) {
    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
        item {
            Column {
                Box(modifier = modifier.fillMaxWidth()) {
                    ExpenseTextView(
                        text = title,
                        style = Typography.titleLarge,
                    )
                    if (title == "Recent Transactions") {
                        ExpenseTextView(
                            text = "See all",
                            style = Typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable {
                                    onSeeAllClicked.invoke()
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.size(12.dp))
            }
        }
        items(items = list,
            key = { item -> item.id ?: 0 }) { item ->
            val icon = Utils.getItemIcon(item)
            val amount = if (item.type == "Income") item.amount else item.amount * -1
            LocalContext.current

            SwippableItemWithActions(
                isRevealedLeft = false,
                isRevealedRight = false,

                {
                    ActionIcon(
                        onClick = {
                            val json = Json.encodeToString(item)
                            navController.navigate("/update_income_expense?expenseEntity=$json")
                        },
                        backgroundColor = Zinc,
                        icon = Icons.Default.Edit,
                        modifier = Modifier.fillMaxHeight()
                    )

                },
               {
                   ActionIcon(
                       onClick = {
                           val json = Json.encodeToString(item)
                           navController.navigate("/delete_income_expense?expenseEntity=$json")
                       },
                       backgroundColor = Zinc,
                       icon = Icons.Default.Delete,
                       modifier = Modifier.fillMaxHeight()
                   )

                },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                TransactionItem(
                    title = item.title,
                    amount = Utils.formatCurrency(amount),
                    icon = icon,
                    date = Utils.formatStringDateToMonthDayYear(item.date),
                    color = DarkGreen,
                    Modifier
                )
            }


        }
    }
}

@Composable
fun TransactionItem(
    title: String,
    amount: String,
    icon: Int,
    date: String,
    color: Color,
    modifier: Modifier
) {

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(51.dp).padding(6.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                ExpenseTextView(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.size(6.dp))
                ExpenseTextView(text = date, fontSize = 13.sp, color = LightGrey)
            }
        }
        ExpenseTextView(
            text = amount,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterEnd),
            color = color
        )
    }
}


@Composable
fun MultiFloatingActionButton(
    modifier: Modifier,
    onAddExpenseClicked: () -> Unit,
    onAddIncomeClicked: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Secondary FABs
            AnimatedVisibility(visible = expanded) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(width = 150.dp, height = 48.dp)
                            .background(color = Zinc, shape = RoundedCornerShape(12.dp))
                            .clickable {
                                onAddIncomeClicked.invoke()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_income),
                                contentDescription = "Add Income",
                                tint = Color.White
                            )
                            Text(
                                text = "Add Income",
                                color = Color.White,
                                style = Typography.titleMedium // Adjust style as needed
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 150.dp, height = 48.dp)
                            .background(color = Zinc, shape = RoundedCornerShape(12.dp))
                            .clickable {
                                onAddExpenseClicked.invoke()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Icon(
                                painter = painterResource(R.drawable.ic_expense),
                                contentDescription = "Add Expense",
                                tint = Color.White
                            )
                            Text(
                                text = "Add Expense",
                                color = Color.White,
                                style = Typography.titleMedium // Adjust style as needed
                            )
                        }
                    }
                }
            }
            // Main FAB
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = Zinc)
                    .clickable {
                        expanded = !expanded
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_addbutton),
                    contentDescription = "small floating action button",
                    modifier = Modifier.size(40.dp)
                )
            }


        }
    }
}

@Composable
fun CardRowItem(modifier: Modifier, title: String, amount: String, image: Int) {
    Column(modifier = modifier) {
        Row {
            Image(
                painter = painterResource(id = image),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.size(8.dp))
            ExpenseTextView(text = title, style = Typography.bodyLarge, color = Color.White)
        }
        Spacer(modifier = Modifier.size(4.dp))
        ExpenseTextView(text = amount, style = Typography.titleLarge, color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}


