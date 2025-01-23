package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.LatestNotificationTimestampRepository
import com.tgarbus.posturecheck.data.PlannedChecksRepository
import com.tgarbus.posturecheck.data.PlannedPostureCheck
import com.tgarbus.posturecheck.data.PostureCheckReply
import com.tgarbus.posturecheck.dismissTestNotification
import com.tgarbus.posturecheck.getCurrentPostureCheckId
import com.tgarbus.posturecheck.storeReplyAndCancelNotification
import com.tgarbus.posturecheck.ui.TextStyles
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h1
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h2
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h4
import com.tgarbus.posturecheck.ui.reusables.SecondaryButton
import kotlinx.coroutines.launch

data class InAppNotificationButtonDescriptor(
    val text: String,
    val onClick: () -> Unit
)

@Composable
fun InAppNotification(
    titleText: String,
    subtitleText: String?,
    leftButtonDescriptor: InAppNotificationButtonDescriptor?,
    rightButtonDescriptor: InAppNotificationButtonDescriptor?,
    tinyButtonDescriptor: InAppNotificationButtonDescriptor?,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val invisibleTexts = ArrayList<String>()
    leftButtonDescriptor?.let { invisibleTexts.add(it.text) }
    rightButtonDescriptor?.let { invisibleTexts.add(it.text) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
            .background(Color(0f, 0f, 0f, 0.5f))
            .clickable(indication = null, interactionSource = interactionSource) {  },
        contentAlignment = Alignment.Center) {
        Box(modifier = Modifier
            .padding(horizontal = 10.dp),
            contentAlignment = Alignment.TopCenter) {
            Image(
                painterResource(R.drawable.in_app_notification_icon),
                "Hey, how's your posture?",
                modifier = Modifier.size(84.dp).zIndex(5f))
            Column(
                modifier = Modifier
                    .padding(top = 42.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colorResource(R.color.mint))
                    .fillMaxWidth()
                    .padding(top = 42.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
            ) {
                Text(
                    titleText,
                    style = TextStyles.header.copy(Color.White),
                    textAlign = TextAlign.Center
                )
                if (subtitleText != null) {
                    Text(
                        subtitleText,
                        style = h4.copy(Color.White),
                        textAlign = TextAlign.Center)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    if (leftButtonDescriptor != null) {
                        SecondaryButton(leftButtonDescriptor.text, invisibleTexts = invisibleTexts) { leftButtonDescriptor.onClick() }
                    }
                    if (rightButtonDescriptor != null) {
                        SecondaryButton(rightButtonDescriptor.text, invisibleTexts = invisibleTexts) { rightButtonDescriptor.onClick() }
                    }
                }
                if (tinyButtonDescriptor != null) {
                    Box(
                        modifier = Modifier.clickable { tinyButtonDescriptor.onClick() }
                            .padding(10.dp)
                    ) {
                        Text(
                            tinyButtonDescriptor.text,
                            style = TextStyles.h4,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InAppPostureCheck(plannedCheck: PlannedPostureCheck?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val onReply: suspend (PostureCheckReply) -> Unit = { reply ->
        if (plannedCheck != null) {
            storeReplyAndCancelNotification(context, plannedCheck.withReply(reply), true)
        } else {
            dismissTestNotification(context)
        }
    }
    InAppNotification(
        titleText = "Hey! How's your posture now?",
        subtitleText = null,
        leftButtonDescriptor = InAppNotificationButtonDescriptor(
            text = "Good",
            onClick = {
                coroutineScope.launch {
                    onReply(PostureCheckReply.GOOD)
                }
            }
        ),
        rightButtonDescriptor = InAppNotificationButtonDescriptor(
            text = "Bad",
            onClick = {
                coroutineScope.launch {
                    onReply(PostureCheckReply.BAD)
                }
            }
        ),
        tinyButtonDescriptor = InAppNotificationButtonDescriptor(
            text = "Skip (N/A)",
            onClick = {
                coroutineScope.launch {
                    onReply(PostureCheckReply.NOT_APPLICABLE)
                }
            }
        )
    )
}

@Composable
fun InAppPostureCheckContainer(content: @Composable () -> Unit) {
    val latestNotificationTimestamp = LatestNotificationTimestampRepository(
        LocalContext.current).getLastNotificationTimestampAsFlow().collectAsState(null)
    val plannedPostureChecks = PlannedChecksRepository(LocalContext.current).getPlannedChecksAsFlow().collectAsState(HashSet())
    Box(modifier = Modifier.fillMaxSize()) {
        val currentCheckId = getCurrentPostureCheckId(LocalContext.current, latestNotificationTimestamp.value)
        if (currentCheckId != null) {
            val plannedCheck = plannedPostureChecks.value.find { it.id == currentCheckId }
            InAppPostureCheck(plannedCheck)
        }
        content()
    }
}