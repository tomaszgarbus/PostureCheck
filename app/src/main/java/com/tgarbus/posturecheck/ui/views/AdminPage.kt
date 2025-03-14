package com.tgarbus.posturecheck.ui.views

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tgarbus.posturecheck.data.AdminViewModel
import com.tgarbus.posturecheck.data.PastChecksRepository
import com.tgarbus.posturecheck.data.PlannedChecksRepository
import com.tgarbus.posturecheck.data.generateFakePastChecks
import com.tgarbus.posturecheck.scheduleRealCheckNSecondsFromNow
import com.tgarbus.posturecheck.ui.reusables.PageHeader
import kotlinx.coroutines.launch

@Composable
fun GenerateFakeDateSection(
    callback: (checksPerDay: Int, daysBack: Int) -> Unit
) {
    val checksPerDay = remember { mutableIntStateOf(3) }
    val daysBack = remember { mutableIntStateOf(30) }

    Text("Checks per day: ${checksPerDay.intValue}")
    Slider(
        value = checksPerDay.intValue.toFloat(),
        valueRange = 1f..10f,
        onValueChange = {
            checksPerDay.intValue = it.toInt()
        })

    Text("Days back: ${daysBack.intValue}")
    Slider(
        value = daysBack.intValue.toFloat(),
        valueRange = 1f..365f,
        onValueChange = {
            daysBack.intValue = it.toInt()
        })

    Button(onClick = {
        callback(checksPerDay.intValue, daysBack.intValue)
    }) {
        Text("Generate fake data")
    }
}

@Composable
fun CheckEntry(checkStr: String, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(checkStr)
        Button(onDelete) { Text("Delete") }
    }
}

@Composable
fun AdminPage(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Column (
        modifier = Modifier
            .safeDrawingPadding()
            .padding(horizontal = 20.dp)
    ) {
        PageHeader("Admin Page")
        val pastChecksFlowState =
            PastChecksRepository(LocalContext.current).getPastChecksHistoryAsFlow().collectAsState(
                initial = HashSet()
            )
        val plannedChecksFlowState =
            PlannedChecksRepository(LocalContext.current).getPlannedChecksAsFlow().collectAsState(
                initial = HashSet()
            )
        val showPlannedChecks = remember { mutableStateOf(false) }
        val showPastChecks = remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                PageHeader("Generate fake data")
                GenerateFakeDateSection { checksPerDay, daysBack ->
                    viewModel.addPastChecks(
                        context, generateFakePastChecks(daysBack, checksPerDay)) {
                        Toast.makeText(
                            context, "Fake checks added to history!",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                PageHeader("Planned Checks")
                if (showPlannedChecks.value) {
                    Text(
                        text = plannedChecksFlowState.value.toList().sortedBy { it.millis }
                            .toString()
                    )
                } else {
                    Button(onClick = {showPlannedChecks.value = true}) { Text("Show planned checks (may be slow)") }
                }
                PageHeader("Historical Checks")
                if (showPastChecks.value) {
                    for (check in pastChecksFlowState.value.toList().sortedBy { it.planned.millis }) {
                        CheckEntry("${check.planned.getDay().toShortString()}: ${check.reply}") { coroutineScope.launch {
                            PastChecksRepository(context).deleteCheck(
                                check.planned.id,
                                check.planned.getDay()
                            )
                        } }
                    }
                    Text(
                        text = pastChecksFlowState.value.toList().sortedBy { it.planned.millis }
                            .toString()
                    )
                } else {
                    Button(onClick = {showPastChecks.value = true}) { Text("Show past checks (may be slow)") }
                }
                PageHeader("Onboarding")
                Button(onClick = { navController.navigate("onboarding") }) { Text("Open onboarding") }
                PageHeader("Schedule real notification in N seconds")
                Text("The difference between real and test notification is that the real one is stored in history and counts to statistics.")
                val nSeconds = remember { mutableIntStateOf(10) }
                Slider(
                    value = nSeconds.intValue.toFloat(),
                    valueRange = 10f..120f,
                    onValueChange = {
                        nSeconds.intValue = it.toInt()
                    })
                Button(onClick = {
                    coroutineScope.launch {
                        scheduleRealCheckNSecondsFromNow(context, nSeconds.intValue)
                    }
                }) {
                    Text("Schedule a check in ${nSeconds.intValue} seconds")
                }
            }
        }
    }
}