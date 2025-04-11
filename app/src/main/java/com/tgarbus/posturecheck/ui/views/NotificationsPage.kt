package com.tgarbus.posturecheck.ui.views

import android.app.Dialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.Day
import com.tgarbus.posturecheck.data.NotificationsViewModel
import com.tgarbus.posturecheck.data.PastPostureCheck
import com.tgarbus.posturecheck.data.PostureCheckReply
import com.tgarbus.posturecheck.data.toDisplayableString
import com.tgarbus.posturecheck.kNotificationText
import com.tgarbus.posturecheck.ui.TextStyles
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h2
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h3
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h4
import com.tgarbus.posturecheck.ui.reusables.PageHeader
import com.tgarbus.posturecheck.ui.reusables.SecondaryButton

@Composable
fun NotificationEntry(
    pastPostureCheck: PastPostureCheck,
    updateResponse: (PostureCheckReply) -> Unit) {
    val editCheck = remember { mutableStateOf(false) }
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
        Column(modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                kNotificationText,
                style = h3.copy(color = colorResource(R.color.dark_green))
            )
            Text(
                dateText,
                style = h4.copy(color = colorResource(R.color.subtitle_gray))
            )
        }
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = pastPostureCheck.reply.toDisplayableString(),
                style = h4.copy(color = colorResource(R.color.subtitle_gray))
            )
            Image(
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        editCheck.value = true
                    },
                painter = painterResource(R.drawable.edit),
                contentDescription = "edit response")
            if (editCheck.value) {
                val dismiss = { editCheck.value = false }
                Dialog(onDismissRequest = dismiss) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(colorResource(R.color.light_mint))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Editing response for check: $dateText", style = h3,
                            textAlign = TextAlign.Center)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            SecondaryButton("Good posture") {
                                updateResponse(PostureCheckReply.GOOD)
                                dismiss()
                            }
                            SecondaryButton("Bad posture") {
                                updateResponse(PostureCheckReply.BAD)
                                dismiss()
                            }
                            SecondaryButton("Skipped check") {
                                updateResponse(PostureCheckReply.NOT_APPLICABLE)
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsPage(navController: NavController, viewModel: NotificationsViewModel = viewModel()) {
    val checks = viewModel.getPastChecks(LocalContext.current).collectAsState(HashSet())
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.light_mint))
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
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
                    NotificationEntry(
                        check, { viewModel.updatePastCheck(context, check, it)})
                }
            }
        }
    }
}