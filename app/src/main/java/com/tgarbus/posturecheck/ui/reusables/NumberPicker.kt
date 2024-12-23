package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h3

@Composable
fun NumberPicker(minNumber: Int, maxNumber: Int, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth().clip(RoundedCornerShape(40.dp)).background(Color.White)
            .padding(horizontal = 19.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (number in minNumber..maxNumber) {
            val selectedModifier = Modifier.clip(CircleShape).background(
                colorResource(R.color.accent_yellow))
                .padding(
                    vertical = 8.dp,
                    horizontal = 9.dp
                )
            val defaultModifier = Modifier.clickable {
                onSelect(number)
            }
            Row(
                modifier = if (number == selected) selectedModifier else defaultModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "$number",
                    style = h3.copy(color = colorResource(R.color.dark_green)),
                )
            }
        }
    }
}