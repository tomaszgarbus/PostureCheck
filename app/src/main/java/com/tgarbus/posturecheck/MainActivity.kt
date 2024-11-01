package com.tgarbus.posturecheck

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tgarbus.posturecheck.ui.theme.PostureCheckTheme
import com.tgarbus.posturecheck.ui.views.AdminPage
import com.tgarbus.posturecheck.ui.views.SettingsPage
import com.tgarbus.posturecheck.ui.views.StatisticsPage
import kotlin.math.absoluteValue
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // How about doing HorizontalPager instead for different screens?

        setContent {
            val pagerState = rememberPagerState(pageCount = { 3 })
            PostureCheckTheme {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(32.dp),
                    // beyondBoundsPageCount = 3,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    var distanceFromCurrentPage = (
                            (pagerState.currentPage - page) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue
                    Box(
                        modifier = Modifier
                            .scale(max(0.75, 1.0 - distanceFromCurrentPage).toFloat())
                            .alpha(max(0.5, 1.0 - distanceFromCurrentPage).toFloat())
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color.Black)
                            .padding(28.dp)
                    ) {
                        when (page) {
                            0 -> AdminPage()
                            1 -> StatisticsPage()
                            2 -> SettingsPage()
                        }
                    }
                }
            }
        }
        val intent = Intent(baseContext, RecomputeNextNotificationsBroadcastReceiver::class.java)
        sendBroadcast(intent)
    }
}
