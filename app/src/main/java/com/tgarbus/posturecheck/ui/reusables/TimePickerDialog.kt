package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.TimeOfDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: TimeOfDay,
    onDismiss: () -> Unit,
    onConfirm: (TimeOfDay) -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss", color = colorResource(R.color.dark_green))
            }
        },
        containerColor = colorResource(R.color.light_mint),
        textContentColor = colorResource(R.color.dark_green),
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(TimeOfDay(timePickerState.hour, timePickerState.minute));
                    onDismiss()
            }) {
                Text("OK", color = colorResource(R.color.dark_green))
            }
        },
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    timeSelectorSelectedContentColor = Color.White,
                    timeSelectorSelectedContainerColor = colorResource(R.color.dark_green),
                    timeSelectorUnselectedContainerColor = colorResource(R.color.light_mint),
                    timeSelectorUnselectedContentColor = Color.Black,
                    selectorColor = colorResource(R.color.dark_green),
                    clockDialColor = Color.White,
                )
            )
        }
    )
}