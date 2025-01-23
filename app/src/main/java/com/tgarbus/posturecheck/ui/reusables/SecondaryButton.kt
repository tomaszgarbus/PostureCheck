package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h3

@Composable
fun SecondaryButton(text: String, invisibleTexts: List<String> = listOf(), onClick: () -> Unit) {
    Button(
        onClick = onClick,
        border = BorderStroke(1.dp, color = colorResource(R.color.dark_green)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colorResource(R.color.dark_green)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = h3.copy(colorResource(R.color.dark_green)))
            for (invisibleText in invisibleTexts) {
                Text(invisibleText, modifier = Modifier.alpha(0f), style = h3)
            }
        }
    }
}