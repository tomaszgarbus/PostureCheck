package com.tgarbus.posturecheck.ui.views

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.DefaultSettings
import com.tgarbus.posturecheck.data.SettingsViewModel
import com.tgarbus.posturecheck.ui.reusables.NumberPicker
import com.tgarbus.posturecheck.ui.reusables.PageHeader
import com.tgarbus.posturecheck.ui.reusables.ScrollableFullScreenColumn
import com.tgarbus.posturecheck.ui.reusables.SettingsItem
import com.tgarbus.posturecheck.ui.reusables.TimePickerSettingsItemWithDialog

@Composable
fun SettingsPage(
    viewModel: SettingsViewModel = viewModel(),
    triggerRecompute: () -> Unit
) {
    val context = LocalContext.current
    val notificationsPerDay = viewModel.getNotificationsPerDay(context).collectAsState(3)

    val earliestNotificationTime = viewModel.getEarliestNotificationTime(context).collectAsState(
        DefaultSettings.defaultEarliestNotificationTime)
    val latestNotificationTime = viewModel.getLatestNotificationTime(context).collectAsState(
        DefaultSettings.defaultLatestNotificationTime)

    ScrollableFullScreenColumn (
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PageHeader("Settings", "Your notifications preferences")

        SettingsItem("Number of notifications per day", inline = false) {
            NumberPicker(1, 10, notificationsPerDay.value) {
                viewModel.setNotificationsPerDay(context, it)
            }
        }

        SettingsItem("Temporarily disable notifications", inline = true) {
            Switch(
                checked = true, onCheckedChange = {},
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colorResource(R.color.mint),
                ))
        }

        TimePickerSettingsItemWithDialog(
            "Earliest acceptable time to send notifications",
            earliestNotificationTime.value) { timeOfDay ->
            if (timeOfDay >= latestNotificationTime.value) {
                Toast.makeText(context, "Earliest allowed notification time must be earlier the latest allowed time.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.setEarliestNotificationTime(context, timeOfDay)
                triggerRecompute()
            }
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(0.5.dp)
            .background(colorResource(R.color.spacer_grey)))

        TimePickerSettingsItemWithDialog(
            "Latest notification time",
            latestNotificationTime.value) { timeOfDay ->
            if (timeOfDay <= earliestNotificationTime.value) {
                Toast.makeText(context, "Latest allowed notification time must be later than the earliest allowed time.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.setLatestNotificationTime(context, timeOfDay)
                triggerRecompute()
            }
        }

    }
}