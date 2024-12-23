package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h3

@Composable
fun SettingsItem(text: String, inline: Boolean = true, body: @Composable () -> Unit) {
    if (inline) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text,
                style = h3.copy(colorResource(R.color.dark_green)),
                modifier = Modifier.fillMaxWidth(0.6f)
            )
            body()
        }
    } else {
        Column (
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text,
                style = h3.copy(colorResource(R.color.dark_green))
            )
            body()
        }
    }
}