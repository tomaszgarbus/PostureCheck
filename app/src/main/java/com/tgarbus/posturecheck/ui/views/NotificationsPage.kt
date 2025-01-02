package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.Day
import com.tgarbus.posturecheck.data.NotificationsViewModel
import com.tgarbus.posturecheck.data.PastPostureCheck
import com.tgarbus.posturecheck.data.toDisplayableString
import com.tgarbus.posturecheck.kNotificationText
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h3
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h4
import com.tgarbus.posturecheck.ui.reusables.PageHeader

@Composable
fun NotificationEntry(pastPostureCheck: PastPostureCheck) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(Color.White)
        .padding(vertical = 20.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        val dayText = when (val day = pastPostureCheck.planned.getDay()) {
            Day.today() -> "Today"
            Day.today() - 1 -> "Yesterday"
            else -> day.toString()
        }
        val timeText = pastPostureCheck.planned.getTimeOfDay().toString()
        val dateText = "$dayText, $timeText"
        Column {
            Text(
                kNotificationText,
                style = h3.copy(color = colorResource(R.color.dark_green))
            )
            Text(
                dateText,
                style = h4.copy(color = colorResource(R.color.subtitle_gray))
            )
        }
        Column {
            Text(
                text = pastPostureCheck.reply.toDisplayableString(),
                style = h4.copy(color = colorResource(R.color.subtitle_gray))
            )
        }
    }
}

@Composable
fun NotificationsPage(navController: NavController, viewModel: NotificationsViewModel = viewModel()) {
    val checks = viewModel.getPastChecks(LocalContext.current).collectAsState(HashSet())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.light_mint))
            .safeDrawingPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(0.25f)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.size(56.dp)
                ) {
                    Image(painterResource(R.drawable.back), "back")
                }
            }
            PageHeader("Notifications", modifier = Modifier.weight(0.5f))
            Row(
                modifier = Modifier.weight(0.25f)
            ) {}
        }

        LazyColumn (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (check in checks.value.sortedBy { - it.planned.millis }) {
                item {
                    NotificationEntry(check)
                }
            }
        }
    }
}