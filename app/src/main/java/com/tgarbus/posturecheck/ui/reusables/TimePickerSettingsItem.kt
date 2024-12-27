package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.chargemap.compose.numberpicker.FullHours
import com.chargemap.compose.numberpicker.HoursNumberPicker
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.TimeOfDay
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h2
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h3

@Composable
fun TimeOfDayDisplay(
    timeOfDay: TimeOfDay,
) {
    Row {
        Text(timeOfDay.toString(), style = h2.copy(colorResource(R.color.dark_green)))
    }
}

@Composable
fun ExpandableTimePickerSettingsItem(
    text: String, timeOfDay: TimeOfDay,
    onChange: (TimeOfDay) -> Unit,
    ) {
    val expanded = remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().clickable { expanded.value = !expanded.value },
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text,
                style = h3.copy(colorResource(R.color.dark_green)),
                modifier = Modifier.fillMaxWidth(0.6f)
            )
            TimeOfDayDisplay(timeOfDay)
        }
        if (expanded.value) {
            HoursNumberPicker(
                value = FullHours(timeOfDay.hour, timeOfDay.minute),
                onValueChange = { onChange(TimeOfDay(it.hours, it.minutes)) }
            )
        }
    }
}

@Composable
fun TimePickerSettingsItemWithDialog(
    text: String, timeOfDay: TimeOfDay,
    onNewTimeConfirmed: (TimeOfDay) -> Unit
) {
    val showTimePickerDialog = remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().clickable { showTimePickerDialog.value = true }) {
        SettingsItem(text, inline = true) { TimeOfDayDisplay(timeOfDay) }
        if (showTimePickerDialog.value) {
            TimePickerDialog(
                timeOfDay,
                onDismiss = {
                    showTimePickerDialog.value = false
                },
                onConfirm = {
                    onNewTimeConfirmed(it)
                }
            )
        }
    }
}