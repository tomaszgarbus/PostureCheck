package com.tgarbus.posturecheck

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tgarbus.posturecheck.ui.theme.PostureCheckTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      PostureCheckTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Greeting("Android")
        }
      }
    }

    val a: AlarmManager = baseContext!!.getSystemService(ALARM_SERVICE) as AlarmManager
    Log.i("tomek", "building intent")
    // var alarmIntent = Intent(baseContext, NotificationService::class.java).let { intent ->
    //   PendingIntent.getBroadcast(baseContext, 0, intent, 0)
    // }
    startService(Intent(baseContext, NotificationService::class.java))
    //   // val intent = Intent("$packageName.NOTIFY")
    //   // Or other component setting APIs e.g. setComponent, setClass
    //   intent.setClassName(packageName, NotificationService::class.simpleName!!)
    //   a.set(
    //     AlarmManager.RTC_WAKEUP,
    //     System.currentTimeMillis() + 10000,
    //     alarmIntent)
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