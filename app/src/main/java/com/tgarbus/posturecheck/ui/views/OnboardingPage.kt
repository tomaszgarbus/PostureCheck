package com.tgarbus.posturecheck.ui.views

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.BuildConfig
import com.tgarbus.posturecheck.data.DefaultSettings
import com.tgarbus.posturecheck.data.OnboardingViewModel
import com.tgarbus.posturecheck.scheduleChecksFirstDay
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h2
import com.tgarbus.posturecheck.ui.TextStyles.Companion.header
import com.tgarbus.posturecheck.ui.reusables.PrimaryButton
import com.tgarbus.posturecheck.ui.reusables.SecondaryButton
import kotlinx.coroutines.launch
import kotlin.math.abs

val kOnboardingTexts = arrayOf(
    "Focus on progress, not perfection. Be kind to yourself — habits aren't built overnight.",
    "Three times a day, we'll ask about your posture. Just respond honestly, and you're all set!",
    "Of course, we will track your progress for you.",
    "Over time, you'll naturally become more mindful of maintaining good posture.",
)

@Composable
fun WelcomePage() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.Top)
    ) {
        Text(
            "WELCOME TO POSTURE CHECK!",
            style = header.copy(color = colorResource(R.color.dark_green)),
            textAlign = TextAlign.Center,
        )
        Text(
            "We're here to help you build good posture habits!",
            style = h2.copy(color = Color.White),
            textAlign = TextAlign.Center,
        )
        Image(painterResource(R.drawable.onboarding_0), "App icon")
    }
}

@Composable
fun OnboardingSlide(pageNumber: Int) {
    val text = kOnboardingTexts[pageNumber - 1]
    val resource = when (pageNumber) {
        1 -> R.drawable.onboarding_1
        2 -> R.drawable.onboarding_2
        3 -> R.drawable.onboarding_3
        4 -> R.drawable.onboarding_4
        else -> -1
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.Top)
    ) {
        Text(
            text,
            style = h2.copy(color = Color.White),
            textAlign = TextAlign.Center,
        )
        Image(painterResource(resource), "Promo graphic material")
    }
}

@Composable
fun PagerController(numPages: Int, pagerState: PagerState) {
    val animationScope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth().height(75.dp).padding(horizontal = 25.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row (
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            for (page in 0..<numPages) {
                if (page == pagerState.currentPage) {
                    val minWidth = 7
                    val maxWidth = 31
                    // Between 0 and 1.
                    val currentChangePresence = 1 - 2 * abs(pagerState.currentPageOffsetFraction)
                    val width = minWidth + (maxWidth - minWidth) * currentChangePresence
                    val color = Color(ColorUtils.blendARGB(Color.White.toArgb(), colorResource(R.color.dark_green).toArgb(), currentChangePresence))
                    Box(modifier = Modifier.size(
                        width = width.dp,
                        height = 7.dp
                    ).clip(RoundedCornerShape(7.dp)).background(color))
                } else {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color.White))
                }
            }
        }
        AnimatedVisibility(pagerState.currentPage + 1 < numPages,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.size(75.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress = (pagerState.currentPage + pagerState.currentPageOffsetFraction) / (numPages - 1)
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    progress = progress,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.dark_green))
                        .align(Alignment.Center)
                        .clickable {
                            animationScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painterResource(R.drawable.onboarding_forward), "Next page",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
fun LetsGetStartedScreen(
    navController: NavController,
    isOnboardingCompleted: Boolean,
    markOnboardingScreenCompleted: () -> Unit,
    triggerRecompute: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(
            "LET'S GET STARTED!",
            style = header.copy(color = colorResource(R.color.dark_green)),
            textAlign = TextAlign.Center,
        )
        Image(painterResource(R.drawable.onboarding_lets_get_started), "Let's get started ")
        PrimaryButton("Take me to the app!") {
            if (!isOnboardingCompleted) {
                coroutineScope.launch {
                    scheduleChecksFirstDay(
                        context, DefaultSettings.defaulNotificationsPerDay,
                        DefaultSettings.defaultEarliestNotificationTime,
                        DefaultSettings.defaultLatestNotificationTime,
                        triggerRecompute,
                    )
                }
            }
            markOnboardingScreenCompleted()
            navController.navigate("main/SETTINGS")
        }
        // SendTestNotificationButton(context)
        // SecondaryButton("Go back") { onGoBack() }
    }
}

@Composable
fun OnboardingPage(
    navController: NavController,
    requestExactAlarmPermissions: () -> Unit,
    triggerRecompute: () -> Unit,
    showNotificationAboutExactAlarm: Boolean,
    viewModel: OnboardingViewModel = viewModel()) {
    val numPages = 6
    val pagerState = rememberPagerState { numPages }
    val animationScope = rememberCoroutineScope()
    val isOnboardingCompleted = viewModel.isOnboardingCompleted(LocalContext.current).collectAsState(false)
    val context = LocalContext.current
    if (pagerState.currentPage == 5 && BuildConfig.askForAlarmPermissions) {
        val dismissedNotification = remember { mutableStateOf(false) }
        if (showNotificationAboutExactAlarm && !dismissedNotification.value) {
            Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                InAppNotification(
                    titleText = "One last thing!",
                    subtitleText = "You can optionally grant Posture Check the so-called alarm permissions. This will allow us to deliver your posture checks more reliably and on-time. Do you want to grant this permission?",
                    leftButtonDescriptor = InAppNotificationButtonDescriptor(
                        text = "Yes",
                        onClick = {
                            requestExactAlarmPermissions()
                            dismissedNotification.value = true
                        }
                    ),
                    rightButtonDescriptor = InAppNotificationButtonDescriptor(
                        text = "No",
                        onClick = {
                            dismissedNotification.value = true
                        }
                    ),
                    tinyButtonDescriptor = null
                )
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.mint))
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(50.dp, alignment = Alignment.CenterVertically)
    ) {
        /* Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.size(1.dp))
            Box(modifier = Modifier.wrapContentSize().clickable {
                navController.navigate("main")
            }.padding(10.dp)) {
                Text("Skip", style = h3.copy(color = Color.White))
            }
        } */
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f),
            // .background(Color.Yellow),
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> OnboardingSlide(1)
                2 -> OnboardingSlide(2)
                3 -> OnboardingSlide(3)
                4 -> OnboardingSlide(4)
                5 -> LetsGetStartedScreen(navController, isOnboardingCompleted = isOnboardingCompleted.value,
                    markOnboardingScreenCompleted = {
                        viewModel.markOnboardingCompleted(context)
                    },
                    triggerRecompute = triggerRecompute,
                )
            }
        }
        PagerController(numPages, pagerState)
    }
}