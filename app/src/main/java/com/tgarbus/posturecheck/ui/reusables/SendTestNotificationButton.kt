package com.tgarbus.posturecheck.ui.reusables

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.tgarbus.posturecheck.sendTestNotification
import kotlinx.coroutines.launch

@Composable
fun SendTestNotificationButton(context: Context) {
    val coroutineScope = rememberCoroutineScope()
    SecondaryButton("Send test notification") {
        coroutineScope.launch {
            sendTestNotification(context)
        }
    }
}