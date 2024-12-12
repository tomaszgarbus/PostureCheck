package com.tgarbus.posturecheck

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.tgarbus.posturecheck.ui.views.AdminPage
import com.tgarbus.posturecheck.ui.views.NavigationFloat
import com.tgarbus.posturecheck.ui.views.NavigationPage
import com.tgarbus.posturecheck.ui.views.SettingsPage
import com.tgarbus.posturecheck.ui.views.StatisticsPage

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val currentPage = remember { mutableStateOf(NavigationPage.STATISTICS) }
            when (currentPage.value) {
                NavigationPage.ADMIN -> AdminPage()
                NavigationPage.STATISTICS -> StatisticsPage()
                NavigationPage.ABOUT -> AdminPage()
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
