package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.BuildConfig

@Composable
fun NavigationButton(
    iconResource: Int, description: String, isActive: Boolean, action: () -> Unit) {
    Box(modifier = Modifier
        .size(56.dp, 56.dp)
        .clip(RoundedCornerShape(32.dp))
        .background(if (isActive) colorResource(R.color.mint) else Color.Transparent)
        .clickable { action() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painterResource(iconResource),
            description,
            alignment = Alignment.Center
        )
    }
}

enum class NavigationPage {
    STATISTICS,
    ABOUT,
    SETTINGS,
    ADMIN
}

@Composable
fun NavigationFloat(
    currentPage: NavigationPage,
    onPageChanged: (page: NavigationPage) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .shadow(3.dp, RoundedCornerShape(39.dp))
                .clip(RoundedCornerShape(39.dp))
                .background(Color.White)
                .padding(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NavigationButton(
                    R.drawable.statistic, "Statistic",
                    currentPage == NavigationPage.STATISTICS, {
                    onPageChanged(NavigationPage.STATISTICS)
                })
                NavigationButton(
                    R.drawable.about, "About",
                    currentPage == NavigationPage.ABOUT, {
                    onPageChanged(NavigationPage.ABOUT)
                })
                NavigationButton(
                    R.drawable.settings, "Settings",
                    currentPage == NavigationPage.SETTINGS, {
                    onPageChanged(NavigationPage.SETTINGS)
                })
                if (BuildConfig.isAdmin) {
                    NavigationButton(
                        R.drawable.about, "Admin",
                        currentPage == NavigationPage.ADMIN, {
                            onPageChanged(NavigationPage.ADMIN)
                        })
                }
            }
        }
    }
}