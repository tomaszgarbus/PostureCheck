package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.navigation.NavController
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h2
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h3
import com.tgarbus.posturecheck.ui.TextStyles.Companion.welcomeHeader
import kotlin.math.abs

val kOnboardingTexts = arrayOf(
    "Focus on progress, not perfection. Be kind to yourself — habits aren't built overnight.",
    "Three times a day, we'll ask about your posture. Just respond honestly, and you're all set!",
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
            style = welcomeHeader.copy(color = colorResource(R.color.dark_green)),
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
fun PagerController(numPages: Int, pagerState: PagerState, onScrollBeyondLastPage: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp),
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
                        if (pagerState.currentPage + 1 < numPages) {
                            pagerState.requestScrollToPage(pagerState.currentPage + 1)
                        } else {
                            onScrollBeyondLastPage()
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

@Composable
fun LetsGetStartedScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(
            "LET'S GET STARTED!",
            style = welcomeHeader.copy(color = colorResource(R.color.dark_green)),
            textAlign = TextAlign.Center,
        )
        Image(painterResource(R.drawable.onboarding_lets_get_started), "Let's get started ")
        Button(
            onClick = {
                navController.navigate("main")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.dark_green),
                contentColor = Color.White
            )
        ) {
            Text("Take me to the app!")
        }
        OutlinedButton(
            onClick = {
                navController.navigate("main")
            },
            border = BorderStroke(1.dp, color = colorResource(R.color.dark_green)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorResource(R.color.dark_green)
            )
        ) {
            Text("Send test notification")
        }
    }
}

@Composable
fun OnboardingPage(navController: NavController) {
    val numPages = 4
    val pagerState = rememberPagerState { numPages }
    val showLetsGetStartedScreen = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.mint))
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(50.dp)
    ) {
        if (showLetsGetStartedScreen.value) {
            LetsGetStartedScreen(navController)
        }
        else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.size(1.dp))
                Box(modifier = Modifier.wrapContentSize().clickable {
                    navController.navigate("main")
                }.padding(10.dp)) {
                    Text("Skip", style = h3.copy(color = Color.White))
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f),
                // .background(Color.Yellow),
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> OnboardingSlide(1)
                    2 -> OnboardingSlide(2)
                    3 -> OnboardingSlide(3)
                }
            }
            PagerController(numPages, pagerState) {
                showLetsGetStartedScreen.value = true
            }
        }
    }
}