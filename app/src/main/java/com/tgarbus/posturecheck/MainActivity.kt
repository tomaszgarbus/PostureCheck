package com.tgarbus.posturecheck

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tgarbus.posturecheck.data.PastChecksRepository
import com.tgarbus.posturecheck.ui.theme.PostureCheckTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      PostureCheckTheme {
        val pastChecksFlowState = PastChecksRepository(this).getPastChecksHistoryAsFlow().collectAsState(
          initial = HashSet()
        )
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Greeting("Android")
          Text(
            text = pastChecksFlowState.value.toString()
          )
        }
      }
    }
    val intent = Intent(baseContext, RecomputeNextNotificationsService::class.java)
    startService(intent)
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  PostureCheckTheme {
    Greeting("Android")
  }
}