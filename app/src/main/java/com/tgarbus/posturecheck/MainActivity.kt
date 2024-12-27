package com.tgarbus.posturecheck

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.tgarbus.posturecheck.ui.views.AboutPage
import com.tgarbus.posturecheck.ui.views.AdminPage
import com.tgarbus.posturecheck.ui.views.NavigationFloat
import com.tgarbus.posturecheck.ui.views.NavigationPage
import com.tgarbus.posturecheck.ui.views.SettingsPage
import com.tgarbus.posturecheck.ui.views.StatisticsPage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val currentPage = remember { mutableStateOf(NavigationPage.STATISTICS) }
            when (currentPage.value) {
                NavigationPage.ADMIN -> AdminPage()
                NavigationPage.STATISTICS -> StatisticsPage()
                NavigationPage.ABOUT -> AboutPage()
                NavigationPage.SETTINGS -> SettingsPage(triggerRecompute = {
                    val intent = Intent(
                        baseContext, RecomputeNextNotificationsBroadcastReceiver::class.java)
                    sendBroadcast(intent)
                })
            }
            NavigationFloat(
                currentPage = currentPage.value,
                onPageChanged = {
                    currentPage.value = it
                })
        }

        val intent = Intent(baseContext, RecomputeNextNotificationsBroadcastReceiver::class.java)
        sendBroadcast(intent)
    }
}
