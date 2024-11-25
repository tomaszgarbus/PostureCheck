package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.Day
import com.tgarbus.posturecheck.data.PastPostureCheck
import com.tgarbus.posturecheck.data.PostureCheckReply
import com.tgarbus.posturecheck.data.StatisticsViewModel
import kotlinx.coroutines.launch

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
        verticalAlignment = Alignment.Bottom
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
        for (day in groupedByDay.keys) {
            groupedByDay[day]!!.sortBy { it.planned.getTimeOfDay() }
        }

        val colorMapping: HashMap<PostureCheckReply, Int> = hashMapOf(
            PostureCheckReply.GOOD to R.color.good_answer,
            PostureCheckReply.BAD to R.color.bad_answer,
            PostureCheckReply.NO_ANSWER to R.color.no_answer,
            PostureCheckReply.NOT_APPLICABLE to R.color.no_answer
        )

        for (day in groupedByDay.keys.sorted()) {
            Column {
                for (check in groupedByDay[day]!!) {
                    val tooltipState = remember { RichTooltipState() }
                    RichTooltipBox(
                        text = { Text("${day}, $check") },
                        tooltipState = tooltipState
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .padding(3.dp)
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
fun StatisticsPage(
    viewModel: StatisticsViewModel = viewModel(),
) {
    val pastChecks = viewModel.getPastPostureChecks(LocalContext.current).collectAsState(HashSet())
    Column {
        PageHeader("Statistics")
        ActivityGraph(pastChecks.value)
    }
}