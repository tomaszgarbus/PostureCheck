package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.ui.TextStyles.Companion.aboutPageNumber
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h2
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h3
import com.tgarbus.posturecheck.ui.reusables.PageHeader
import kotlin.math.absoluteValue
import kotlin.math.max

fun headerText(page: Int): String {
    return when (page) {
        0 -> "Shoulder Alignment"
        1 -> "Head Position"
        2 -> "Back Check"
        3 -> "Distribute Weight Evenly"
        4 -> "Rope Test"
        5 -> "Breathing Test"
        else -> "dupsko"
    }
}

fun descriptionText(page: Int): String {
    return when (page) {
        0 -> "Shoulders should be relaxed, not raised or slouched forward."
        1 -> "Ears should be in line with your shoulders."
        2 -> "Your head, shoulders, and hips should touch the wall, with a small space between the wall and your lower back."
        3 -> "For sitting, keep both feet flat on the ground and balanced on both hips. For standing, keep both feet planted firmly, avoiding leaning."
        4 -> "Imagine a rope pulling you up from the crown of your head to help lengthen your spine and maintain balance."
        5 -> "Take a deep breath. If your chest and stomach expand naturally without strain, you’re likely in a good posture."
        else -> "dupsko"
    }
}

@Composable
fun AboutCard(number: Int, header: String, description: String, painter: Painter, active: Boolean = false) {
    val modifier = if (active) Modifier.shadow(10.dp, RoundedCornerShape(24.dp)) else Modifier
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(colorResource(R.color.mint))
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painter, header)
        Text(
            "${number + 1}",
            style = aboutPageNumber
        )
        Text(
            header,
            style = h2.copy(color = colorResource(R.color.dark_green)),
            textAlign = TextAlign.Center,
        )
        Text(
            description,
            style = h3.copy(color = Color.White),
            textAlign = TextAlign.Center,
            minLines = 5
        )
    }
}

@Composable
fun AboutPage() {
    val pagerState = rememberPagerState { 6 }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.light_mint))
            .safeDrawingPadding()
            .padding(horizontal = 20.dp)
    ) {
        PageHeader(
            "How to check posture?",
            subtitleText = "Simple tips to check and improve your posture. Start by learning these habits, but once you know what to look for, a quick check can take just seconds. For any concerns or pain, please consult a healthcare professional.")
        HorizontalPager (
            state = pagerState,
            contentPadding = PaddingValues(32.dp),
            beyondViewportPageCount = 3,
            pageSpacing = 10.dp
        ) { page ->
            val distanceFromCurrentPage = (
                    (pagerState.currentPage - page) + pagerState
                        .currentPageOffsetFraction
                    ).absoluteValue
            val painter = when (page) {
                0 -> painterResource(R.drawable.about_card_0)
                1 -> painterResource(R.drawable.about_card_1)
                2 -> painterResource(R.drawable.about_card_2)
                3 -> painterResource(R.drawable.about_card_3)
                4 -> painterResource(R.drawable.about_card_4)
                else -> painterResource(R.drawable.about_card_5)
            }
            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    //.scale(max(0.9, 1.0 - distanceFromCurrentPage).toFloat())
                    .alpha(max(0.5, 1.0 - distanceFromCurrentPage).toFloat())
            ) {
                AboutCard(page, headerText(page), descriptionText(page), painter, active = distanceFromCurrentPage == 0f)
            }
        }
    }
}