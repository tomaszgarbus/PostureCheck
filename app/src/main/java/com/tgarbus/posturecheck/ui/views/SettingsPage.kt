package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.SettingsViewModel
import com.tgarbus.posturecheck.data.TimeOfDay
import com.tgarbus.posturecheck.data.kDefaultEarliestNotificationTime
import com.tgarbus.posturecheck.data.kDefaultLatestNotificationTime
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h2
import com.tgarbus.posturecheck.ui.reusables.NumberPicker
import com.tgarbus.posturecheck.ui.reusables.PageHeader
import com.tgarbus.posturecheck.ui.reusables.ScrollableFullScreenColumn
import com.tgarbus.posturecheck.ui.reusables.TimePickerDialog

@Composable
fun TimeOfDaySettingsEntry(
    timeOfDay: TimeOfDay,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable { onClick() }) {
        Text(timeOfDay.toString(), style = h2)
    }
}

@Composable
fun SettingsPage(
    viewModel: SettingsViewModel = viewModel(),
    triggerRecompute: () -> Unit
) {
    val context = LocalContext.current
    val notificationsPerDay = viewModel.getNotificationsPerDay(context).collectAsState(3)

    val earliestNotificationTime = viewModel.getEarliestNotificationTime(context).collectAsState(
        TimeOfDay.fromPreferencesStorageFormat(kDefaultEarliestNotificationTime))
    val latestNotificationTime = viewModel.getLatestNotificationTime(context).collectAsState(
        TimeOfDay.fromPreferencesStorageFormat(kDefaultLatestNotificationTime))

    val showEarliestTimePicker = remember { mutableStateOf(false) }
    val showLatestTimePicker = remember { mutableStateOf(false) }

    ScrollableFullScreenColumn (
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PageHeader("Settings", "Your notifications preferences")

        SettingsItem("Number of notifications per day", inline = false) {
            NumberPicker(1, 10, notificationsPerDay.value) {
                viewModel.setNotificationsPerDay(context, it)
            }
        }

        SettingsItem("Earliest acceptable time to send notifications") {
            TimeOfDaySettingsEntry(earliestNotificationTime.value) {
                showEarliestTimePicker.value = true
            }
            if (showEarliestTimePicker.value) {
                TimePickerDialog(
                    earliestNotificationTime.value,
                    onDismiss = {
                        showEarliestTimePicker.value = false
                    }, onConfirm = {
                        timeOfDay -> viewModel.setEarliestNotificationTime(context, timeOfDay)
                        triggerRecompute()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(0.5.dp)
            .background(colorResource(R.color.spacer_grey)))

        SettingsItem("Latest notification time") {
            TimeOfDaySettingsEntry(latestNotificationTime.value) {
                showLatestTimePicker.value = true
            }
            if (showLatestTimePicker.value) {
                TimePickerDialog(
                    latestNotificationTime.value,
                    onDismiss = {
                        showLatestTimePicker.value = false
                    }, onConfirm = {
                        timeOfDay -> viewModel.setLatestNotificationTime(context, timeOfDay)
                        triggerRecompute()
                    }
                )
            }
        }
    }
}