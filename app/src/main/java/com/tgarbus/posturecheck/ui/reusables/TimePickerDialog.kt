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
import androidx.compose.ui.res.colorResource
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
                Text("Dismiss")
            }
        },
        textContentColor = colorResource(R.color.dark_green),
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(TimeOfDay(timePickerState.hour, timePickerState.minute));
                    onDismiss()
            }) {
                Text("OK")
            }
        },
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    timeSelectorSelectedContainerColor = colorResource(R.color.light_mint),
                    selectorColor = colorResource(R.color.dark_green)
                )
            )
        }
    )
}