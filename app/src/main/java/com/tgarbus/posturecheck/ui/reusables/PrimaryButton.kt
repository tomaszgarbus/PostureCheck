package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h2
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h3

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.dark_green),
            contentColor = Color.White
        )
    ) {
        Text(text, style = h3.copy(Color.White))
    }
}