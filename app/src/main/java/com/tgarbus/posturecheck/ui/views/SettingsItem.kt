package com.tgarbus.posturecheck.ui.views

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Composable
fun SettingsItem(text: String, body: @Composable () -> Unit) {
    Text(
        text,
        style = TextStyle(
            fontSize = 20.sp
        )
    )
    body()
}