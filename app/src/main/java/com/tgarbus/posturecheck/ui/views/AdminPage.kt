package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.tgarbus.posturecheck.data.PastChecksRepository
import com.tgarbus.posturecheck.data.PlannedChecksRepository

@Composable
fun AdminPage() {
    Column {
        PageHeader("Admin Page")
        val pastChecksFlowState =
            PastChecksRepository(LocalContext.current).getPastChecksHistoryAsFlow().collectAsState(
                initial = HashSet()
            )
        val plannedChecksFlowState =
            PlannedChecksRepository(LocalContext.current).getPlannedChecksAsFlow().collectAsState(
                initial = HashSet()
            )
        val scrollState = rememberScrollState()
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Text("Planned checks:", style = TextStyle(fontSize = 20.sp))
                Text(
                    text = plannedChecksFlowState.value.toList().sortedBy { it.millis }
                        .toString()
                )
                Text("Historical checks:", style = TextStyle(fontSize = 20.sp))
                Text(
                    text = pastChecksFlowState.value.toList().sortedBy { it.planned.millis }
                        .toString()
                )
            }
        }
    }
}