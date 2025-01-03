package com.tgarbus.posturecheck.ui.reusables

import android.content.Context
import androidx.compose.runtime.Composable
import com.tgarbus.posturecheck.sendTestNotification

@Composable
fun SendTestNotificationButton(context: Context) {
    SecondaryButton("Send test notification") {
        sendTestNotification(context)
    }
}