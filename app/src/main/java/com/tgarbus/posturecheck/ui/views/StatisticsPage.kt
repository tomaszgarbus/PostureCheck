package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RichTooltipBox
import androidx.compose.material3.RichTooltipState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.Day
import com.tgarbus.posturecheck.data.PastPostureCheck
import com.tgarbus.posturecheck.data.PostureCheckReply
import com.tgarbus.posturecheck.data.StatisticsViewModel
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h4
import com.tgarbus.posturecheck.ui.reusables.CircularGraph
import com.tgarbus.posturecheck.ui.reusables.CircularGraphComponent
import com.tgarbus.posturecheck.ui.reusables.DropdownMenu
import com.tgarbus.posturecheck.ui.reusables.DropdownOption
import com.tgarbus.posturecheck.ui.reusables.LineChart
import com.tgarbus.posturecheck.ui.reusables.LineChartEntry
import com.tgarbus.posturecheck.ui.reusables.ScrollableFullScreenColumn
import kotlinx.coroutines.launch
import kotlin.math.max

// https://stackoverflow.com/questions/70057396/how-to-show-vertical-text-with-proper-size-layout-in-jetpack-compose
fun Modifier.rotateVertically(clockwise: Boolean = true): Modifier {
    val rotate = rotate(if (clockwise) 90f else -90f)

    val adjustBounds = layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }
    return rotate then adjustBounds
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityGraph(checks: Set<PastPostureCheck>) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier.horizontalScroll(scrollState, reverseScrolling = true),
        verticalAlignment = Alignment.Top
    ) {
        // Group checks by day.
        val groupedByDay = HashMap<Day, ArrayList<PastPostureCheck>>()
        for (check in checks) {
            val day = check.planned.getDay()
            if (!groupedByDay.contains(day)) {
                groupedByDay[day] = ArrayList()
            }
            groupedByDay[day]!!.add(check)
        }
        var highestCountPerDay = 0
        for (day in groupedByDay.keys) {
            groupedByDay[day]!!.sortBy { it.planned.getTimeOfDay() }
            highestCountPerDay = max(highestCountPerDay, groupedByDay[day]!!.size)
        }


        val colorMapping: HashMap<PostureCheckReply, Int> = hashMapOf(
            PostureCheckReply.GOOD to R.color.good_answer,
            PostureCheckReply.BAD to R.color.bad_answer,
            PostureCheckReply.NO_ANSWER to R.color.no_answer,
            PostureCheckReply.NOT_APPLICABLE to R.color.no_answer
        )

        for (day in groupedByDay.keys.sorted()) {
            Column {
                // Add padding
                for (i in 1..highestCountPerDay - groupedByDay[day]!!.size) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .padding(3.dp)
                            .background(
                                Color.Transparent,
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
                for (check in groupedByDay[day]!!) {
                    val tooltipState = remember { RichTooltipState() }
                    RichTooltipBox(
                        text = { Text("${day}, ${check.planned.getTimeOfDay()}") },
                        tooltipState = tooltipState
                    ) {
                        val highlighted = tooltipState.isVisible.compareTo(false)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .padding(3.dp * (1 - highlighted))
                                .background(
                                    colorResource(colorMapping[check.reply]!!),
                                    RoundedCornerShape(3.dp)
                                )
                                .clickable {
                                    scope.launch {
                                        tooltipState.show()
                                    }
                                }
                        )
                    }
                }
                Column(modifier = Modifier.rotateVertically(false)) {
                    Text(
                        text = "$day",
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryEntry(
    percent: Int, name: String, color: Color,
    textColor: Color = colorResource(R.color.dark_green)) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier =
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Text("${percent}%", style = h4.copy(color = textColor))
        }
        Text(name, style = h4.copy(color = Color.White))
    }
}

@Composable
fun Summary() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colorResource(R.color.mint))
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            SummaryEntry(60, "Straight posture", colorResource(R.color.accent_yellow))
            SummaryEntry(23, "Slouching", colorResource(R.color.dark_green), textColor = Color.White)
            SummaryEntry(17, "No answer", colorResource(R.color.light_mint))
        }
        Box(
            modifier = Modifier.weight(1f).aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularGraph(
                canvasModifier = Modifier.fillMaxSize(),
                entries = listOf(
                    CircularGraphComponent(60, colorResource(R.color.accent_yellow)),
                    CircularGraphComponent(23, colorResource(R.color.dark_green)),
                    CircularGraphComponent(17, colorResource(R.color.light_mint)),
                ))
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Row(modifier = Modifier.wrapContentSize()) {
                    Text(
                        "60",
                        style = h4.copy(color = Color.White, fontSize = 32.sp),
                        modifier = Modifier.alignByBaseline())
                    Text(
                        "%",
                        style = h4.copy(color = Color.White, fontSize = 16.sp),
                        modifier = Modifier.alignByBaseline())
                }
                Text("straight posture", style = h4.copy(color = Color.White))
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun LineChartDisplay() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LineChart(
            ArrayList(listOf(
                LineChartEntry(value = 1f, label = "Mon"),
                LineChartEntry(value = 0.8f, label = "Tue"),
                LineChartEntry(value = 0.5f, label = "Wed"),
                LineChartEntry(value = 1f, label = "Thu"),
                LineChartEntry(value = 0f, label = "Fri"),
                LineChartEntry(value = 0.3f, label = "Sat"),
                LineChartEntry(value = 1f, label = "Sun"),
            )),
            canvasModifier = Modifier.fillMaxSize())
    }
}

@Composable
fun StatisticsPage(
    viewModel: StatisticsViewModel = viewModel(),
) {
    val pastChecks = viewModel.getPastPostureChecks(LocalContext.current).collectAsState(HashSet())
    val weekDropdownOption = DropdownOption(text = "Week", onSelect = {})
    val monthDropdownOption = DropdownOption(text = "Month", onSelect = {})
    val allTimeDropdownOption = DropdownOption(text = "All time", onSelect = {})
    ScrollableFullScreenColumn(headerHeight = 86.dp) {
        Summary()
        Spacer(modifier = Modifier.height(20.dp))
        LineChartDisplay()
        // PageHeader("Statistics")
        // ActivityGraph(pastChecks.value)
    }
    Box(modifier = Modifier.fillMaxSize().padding(20.dp, 32.dp)) {
        DropdownMenu(
            modifier = Modifier.align(Alignment.TopEnd),
            options = listOf(weekDropdownOption, monthDropdownOption, allTimeDropdownOption),
            default = weekDropdownOption
        )
    }
}