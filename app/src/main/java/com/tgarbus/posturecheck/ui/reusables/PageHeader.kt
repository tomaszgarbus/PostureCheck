package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Composable
fun PageHeader(text: String) {
    Text(
        text,
        style = TextStyle(fontSize = 36.sp)
    )
}