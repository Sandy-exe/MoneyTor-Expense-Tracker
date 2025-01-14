package com.example.expensetrackerv1.screens.stats

import android.graphics.Color
import android.view.LayoutInflater
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.expensetrackerv1.R
import com.example.expensetrackerv1.utils.Utils
import com.example.expensetrackerv1.screens.home.TransactionList
import com.example.expensetrackerv1.widget.ExpenseTextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

@Composable
fun StatsScreen(navController: NavController) {
    val viewModel : StatsViewModel = StatsViewModelFactory(LocalContext.current).create(StatsViewModel::class.java)
    Scaffold(topBar = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                modifier = Modifier.align(
                    Alignment.CenterStart
                ).clickable {
                    navController.navigateUp()
                },
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
            )
            ExpenseTextView(
                text = "Statistics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center)
            )

        }
    }) {
        val dataState = viewModel.entries.collectAsState(emptyList())
        val topExpense = viewModel.topEntries.collectAsState(initial = emptyList())
        Column(modifier = Modifier.padding(it)) {
            val entries = viewModel.getEntriesForChart(dataState.value)
            if (entries.isEmpty()) {
                // Show image if entries is empty
                Box(
                    modifier = Modifier
                        .fillMaxSize() // This makes the Box take up the full screen size
                        .padding(16.dp) // Add padding around the image
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.no_data_png), // Replace with your empty chart image
                        contentDescription = "No Data",
                        modifier = Modifier
                            .align(Alignment.Center) // This will center the image both horizontally and vertically
                    )
                }


            } else {
                // Show LineChart if entries is not empty
                LineChart(entries = entries)
                Spacer(modifier = Modifier.height(16.dp))
                TransactionList(
                    Modifier, list = topExpense.value, "Top Spending", onSeeAllClicked = {},
                    navController = navController
                )
            }


        }
    }
}

@Composable
fun LineChart(entries: List<Entry>) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            val view = LayoutInflater.from(context).inflate(R.layout.stats_line_chart, null)
            view
        }, modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp)
    ) { view ->
        val lineChart = view.findViewById<LineChart>(R.id.lineChart)

        val dataSet = LineDataSet(entries, "Expenses").apply {
            color = Color.parseColor("#FF2F7E79")
            valueTextColor = Color.BLACK
            lineWidth = 3f
            axisDependency = YAxis.AxisDependency.RIGHT
            setDrawFilled(true)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextSize = 12f
            valueTextColor = Color.parseColor("#FF2F7E79")
            val drawable = ContextCompat.getDrawable(context, R.drawable.char_gradient)
            drawable?.let {
                fillDrawable = it
            }

        }

        lineChart.xAxis.valueFormatter =
            object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return Utils.formatDateForChart(value.toLong())
                }
            }
        lineChart.data = LineData(dataSet)
        lineChart.axisLeft.isEnabled = false
        lineChart.axisRight.isEnabled = false
        lineChart.axisRight.setDrawGridLines(false)
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.xAxis.setDrawAxisLine(false)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.invalidate()
    }
}

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    StatsScreen(rememberNavController())
}