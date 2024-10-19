package com.tgarbus.posturecheck

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.ScrollView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tgarbus.posturecheck.data.PastChecksRepository
import com.tgarbus.posturecheck.data.PlannedChecksRepository
import com.tgarbus.posturecheck.ui.theme.PostureCheckTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      PostureCheckTheme {
        val pastChecksFlowState = PastChecksRepository(this).getPastChecksHistoryAsFlow().collectAsState(
          initial = HashSet()
        )
        val plannedChecksFlowState = PlannedChecksRepository(this).getPlannedChecksAsFlow().collectAsState(
          initial = HashSet()
        )
        val scrollState = rememberScrollState()
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Column(modifier = Modifier.verticalScroll(scrollState)) {
            Text("Planned checks:", style = TextStyle(fontSize = 20.sp))
            Text(
              text = plannedChecksFlowState.value.toList().sortedBy { it.millis }. toString()
            )
            Text("Historical checks:", style = TextStyle(fontSize = 20.sp))
            Text(
              text = pastChecksFlowState.value.toList().sortedBy { it.planned.millis }. toString()
            )
          }
        }
      }
    }
    val intent = Intent(baseContext, RecomputeNextNotificationsBroadcastReceiver::class.java)
    sendBroadcast(intent)
  }
}
