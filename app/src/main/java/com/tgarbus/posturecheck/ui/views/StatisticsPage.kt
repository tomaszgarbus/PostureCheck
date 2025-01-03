package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.AnswersDistribution
import com.tgarbus.posturecheck.data.DefaultSettings
import com.tgarbus.posturecheck.data.PastPostureCheck
import com.tgarbus.posturecheck.data.PeriodType
import com.tgarbus.posturecheck.data.StatisticsViewModel
import com.tgarbus.posturecheck.data.TimeOfDay
import com.tgarbus.posturecheck.data.buildAnswersDistribution
import com.tgarbus.posturecheck.data.buildGridChartColumnsForPeriod
import com.tgarbus.posturecheck.data.buildLineChartEntriesForPeriod
import com.tgarbus.posturecheck.data.percentBad
import com.tgarbus.posturecheck.data.percentGood
import com.tgarbus.posturecheck.data.percentNoAnswer
import com.tgarbus.posturecheck.data.shouldOnlyShowBePatientBanner
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h3
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h4
import com.tgarbus.posturecheck.ui.reusables.CircularGraph
import com.tgarbus.posturecheck.ui.reusables.CircularGraphComponent
import com.tgarbus.posturecheck.ui.reusables.DropdownMenu
import com.tgarbus.posturecheck.ui.reusables.DropdownOption
import com.tgarbus.posturecheck.ui.reusables.GridChartColumnInput
import com.tgarbus.posturecheck.ui.reusables.GridChartOnCanvas
import com.tgarbus.posturecheck.ui.reusables.LineChart
import com.tgarbus.posturecheck.ui.reusables.LineChartEntry
import com.tgarbus.posturecheck.ui.reusables.ScrollableFullScreenColumn
import com.tgarbus.posturecheck.ui.reusables.buildHorizontalGridChartSpec
import kotlin.math.min

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
fun Summary(answersDistribution: AnswersDistribution) {
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
            SummaryEntry(
                answersDistribution.percentGood(),
                "Straight posture", colorResource(R.color.accent_yellow))
            SummaryEntry(
                answersDistribution.percentBad(),
                "Slouching", colorResource(R.color.dark_green), textColor = Color.White)
            SummaryEntry(answersDistribution.percentNoAnswer(),
                "No answer", colorResource(R.color.light_mint))
        }
        Box(
            modifier = Modifier.weight(1f).aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularGraph(
                canvasModifier = Modifier.fillMaxSize(),
                entries = listOf(
                    CircularGraphComponent(
                        answersDistribution.percentGood(), colorResource(R.color.accent_yellow)),
                    CircularGraphComponent(
                        answersDistribution.percentBad(), colorResource(R.color.dark_green)),
                    CircularGraphComponent(
                        answersDistribution.percentNoAnswer(), colorResource(R.color.light_mint)),
                ))
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Row(modifier = Modifier.wrapContentSize()) {
                    Text(
                        "${answersDistribution.percentGood()}",
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
fun GridChartLegendEntry(
    color: Color, text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.clip(CircleShape).size(28.dp).background(color)) {}
        Text(text, style = h4.copy(color = colorResource(R.color.subtitle_gray)))
    }
}

@Composable
fun LineChartDisplay(entries: ArrayList<LineChartEntry>) {
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
            entries, canvasModifier = Modifier.fillMaxSize())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GridChartDisplay(
    columns: ArrayList<GridChartColumnInput>, minTimeOfDay: TimeOfDay, maxTimeOfDay: TimeOfDay) {
    val spec = buildHorizontalGridChartSpec(columns, minTimeOfDay, maxTimeOfDay, LocalContext.current)
    Column (modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(Color.White)
        .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(min(5f, spec.numColumns.toFloat() / spec.numRows)),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            GridChartOnCanvas(spec, canvasModifier = Modifier.fillMaxSize())
        }
        FlowRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GridChartLegendEntry(colorResource(R.color.accent_yellow), "Straight posture")
            GridChartLegendEntry(colorResource(R.color.dark_green), "Slouching")
            GridChartLegendEntry(colorResource(R.color.light_mint), "No answer / skipped")
        }
    }
}

@Composable
fun ChartTypeSwitcherOption(
    painter: Painter,
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(if (active) colorResource(R.color.mint) else Color.Transparent)
            .clickable { onClick() }
            .padding(13.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(painter, text, modifier = Modifier.size(14.dp))
        Text(text, style = h4.copy(colorResource(R.color.dark_green)))
    }
}

enum class ChartType {
    LINE,
    GRID
}

@Composable
fun ChartTypeSwitcher(
    activeChartType: ChartType,
    onLineChartSelected: () -> Unit,
    onGridChartSelected: () -> Unit,
) {
    Row(modifier = Modifier
        .clip(RoundedCornerShape(38.dp))
        .background(Color.White)
        .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ChartTypeSwitcherOption(
            painterResource(R.drawable.line_chart), "Line chart",
            activeChartType == ChartType.LINE,
            onLineChartSelected)
        ChartTypeSwitcherOption(
            painterResource(R.drawable.grid_chart), "Grid chart",
            activeChartType == ChartType.GRID,
            onGridChartSelected)
    }
}

@Composable
fun NoDataForChartText(selectedPeriod: PeriodType) {
    Row(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        val text = when (selectedPeriod) {
            PeriodType.WEEK -> "Oops! We need consistent data from last week to show you this chart!"
            PeriodType.MONTH -> "Sorry! Can't show the month chart until we have posture check results for each day!"
            PeriodType.ALL_TIME -> "Be patient! We need more data to build this chart."
        }
        Text(
            text,
            style = h3.copy(color = colorResource(R.color.subtitle_gray))
        )
    }
}

@Composable
fun ChartBlock(
    activeChartType: ChartType,
    pastChecks: Set<PastPostureCheck>,
    selectedPeriod: PeriodType,
    minTimeOfDay: TimeOfDay,
    maxTimeOfDay: TimeOfDay
) {
    if (activeChartType == ChartType.LINE) {
        val chartEntries = buildLineChartEntriesForPeriod(selectedPeriod, pastChecks, false)
        if (chartEntries != null) {
            LineChartDisplay(chartEntries)
        } else {
            NoDataForChartText(selectedPeriod)
        }
    }
    if (activeChartType == ChartType.GRID) {
        val chartColumns = buildGridChartColumnsForPeriod(pastChecks, selectedPeriod, false)
        if (chartColumns != null) {
            GridChartDisplay(chartColumns, minTimeOfDay, maxTimeOfDay)
        } else {
            NoDataForChartText(selectedPeriod)
        }
    }
}

@Composable
fun BePatientBlock() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colorResource(R.color.mint))
            .padding(25.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            "Be patient!",
            style = h4.copy(Color.White),
            textAlign = TextAlign.Start,
        )
        Text(
            "We know you want to see your progress, but let us collect some stats for a few more days.",
            style = h3.copy(colorResource(R.color.dark_green)),
            textAlign = TextAlign.Start,
        )
        Text(
            "Good habits aren't built overnight.",
            style = h4.copy(Color.White),
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
fun StatisticsPage(
    navController: NavController,
    viewModel: StatisticsViewModel = viewModel(),
) {
    val selectedPeriod = remember { mutableStateOf(PeriodType.WEEK) }
    val weekDropdownOption = DropdownOption(text = "Week", onSelect = {
        selectedPeriod.value = PeriodType.WEEK
    })
    val monthDropdownOption = DropdownOption(text = "Month", onSelect = {
        selectedPeriod.value = PeriodType.MONTH
    })
    val allTimeDropdownOption = DropdownOption(text = "All time", onSelect = {
        selectedPeriod.value = PeriodType.ALL_TIME
    })
    // TODO: If there is any posture check today, set includeToday to true.
    val pastChecks = viewModel.getPastPostureChecks(
        LocalContext.current, selectedPeriod.value, includeToday = false).collectAsState(HashSet())
    val answersDistribution = buildAnswersDistribution(
        pastChecks.value
    )
    val showOnlyBePatientBanner = shouldOnlyShowBePatientBanner(answersDistribution)
    val activeChartType = remember { mutableStateOf(ChartType.LINE) }
    val minTimeOfDay = viewModel.getMinTimeOfDay(LocalContext.current).collectAsState(
        DefaultSettings.defaultEarliestNotificationTime)
    val maxTimeOfDay = viewModel.getMaxTimeOfDay(LocalContext.current).collectAsState(
        DefaultSettings.defaultLatestNotificationTime)
    ScrollableFullScreenColumn(
        headerHeight = 86.dp,
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        if (showOnlyBePatientBanner) {
            BePatientBlock()
        } else {
            if (answersDistribution != null) {
                Summary(answersDistribution)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                ChartTypeSwitcher(
                    activeChartType = activeChartType.value,
                    onLineChartSelected = {
                        activeChartType.value = ChartType.LINE
                    },
                    onGridChartSelected = {
                        activeChartType.value = ChartType.GRID
                    }
                )
            }
            ChartBlock(
                activeChartType.value, pastChecks.value, selectedPeriod.value, minTimeOfDay.value,
                maxTimeOfDay.value
            )
        }
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()
        .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier
                .size(56.dp).clip(CircleShape).background(Color.White)
                .clickable { navController.navigate("notifications") },
                contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.notifications), "Notifications",
                    alignment = Alignment.Center)
            }
            DropdownMenu(
                options = listOf(weekDropdownOption, monthDropdownOption, allTimeDropdownOption),
                default = weekDropdownOption
            )
        }
    }
}