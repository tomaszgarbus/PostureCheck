package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.tgarbus.posturecheck.R

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        border = BorderStroke(1.dp, color = colorResource(R.color.dark_green)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colorResource(R.color.dark_green)
        )
    ) {
        Text(text)
    }
}