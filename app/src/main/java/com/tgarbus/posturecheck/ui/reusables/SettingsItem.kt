package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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

@Composable
fun InlineClickableSettingsItem(text: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable {
        onClick()
    },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            style = h3.copy(colorResource(R.color.dark_green)),
            modifier = Modifier.fillMaxWidth(0.6f)
        )
        Image(painterResource(R.drawable.forward), text)
    }
}