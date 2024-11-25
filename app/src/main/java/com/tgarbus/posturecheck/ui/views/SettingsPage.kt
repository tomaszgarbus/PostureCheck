package com.tgarbus.posturecheck.ui.views

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tgarbus.posturecheck.data.SettingsViewModel
import com.tgarbus.posturecheck.data.TimeOfDay
import com.tgarbus.posturecheck.data.kDefaultEarliestNotificationTime
import com.tgarbus.posturecheck.data.kDefaultLatestNotificationTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun TimeOfDaySettingsEntry(
    timeOfDay: TimeOfDay,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Text(timeOfDay.toString())
    }
}

@Composable
fun SettingsPage(
    viewModel: SettingsViewModel = viewModel(),
    triggerRecompute: () -> Unit
) {
    val context = LocalContext.current
    var notificationsPerDay: Int
    runBlocking {
        notificationsPerDay =
            viewModel.getNotificationsPerDay(context).first()
    }
    val notificationsPerDayLocal = remember { mutableIntStateOf(notificationsPerDay) }
    val notificationsPerDayBounds = Pair(1, 10)

    val earliestNotificationTime = viewModel.getEarliestNotificationTime(context).collectAsState(
        TimeOfDay.fromPreferencesStorageFormat(kDefaultEarliestNotificationTime))
    val latestNotificationTime = viewModel.getLatestNotificationTime(context).collectAsState(
        TimeOfDay.fromPreferencesStorageFormat(kDefaultLatestNotificationTime))

    val showEarliestTimePicker = remember { mutableStateOf<Boolean>(false) }
    val showLatestTimePicker = remember { mutableStateOf<Boolean>(false) }

    Column {
        PageHeader("Settings")

        SettingsItem("Notifications per day: ${notificationsPerDayLocal.intValue}") {
            Slider(
                value = notificationsPerDayLocal.intValue.toFloat(),
                onValueChange = {
                    notificationsPerDayLocal.intValue = it.toInt()
                },
                onValueChangeFinished = {
                    Log.i("tomek", notificationsPerDayLocal.intValue.toString())
                    viewModel.setNotificationsPerDay(context, notificationsPerDayLocal.intValue)
                    triggerRecompute()
                },
                steps = notificationsPerDayBounds.second - notificationsPerDayBounds.first + 1,
                valueRange = notificationsPerDayBounds.first.toFloat()..notificationsPerDayBounds.second.toFloat(),
            )
        }

        SettingsItem("Earliest notification time") {
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