package com.tgarbus.posturecheck.ui.reusables

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.sendTestNotification

@Composable
fun SendTestNotificationButton(context: Context) {
    OutlinedButton(
        onClick = {
            sendTestNotification(context)
        },
        border = BorderStroke(1.dp, color = colorResource(R.color.dark_green)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colorResource(R.color.dark_green)
        )
    ) {
        Text("Send test notification")
    }
}