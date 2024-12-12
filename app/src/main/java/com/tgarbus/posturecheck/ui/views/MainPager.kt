package com.tgarbus.posturecheck.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.tgarbus.posturecheck.R
import kotlin.math.absoluteValue
import kotlin.math.max

@Composable
fun MainPager(pages: List<@Composable () -> Unit>) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(16.dp),
        // beyondBoundsPageCount = 3,6
        modifier = Modifier.fillMaxSize().background(brush = Brush.linearGradient(
            listOf(
                colorResource(R.color.mint),
                colorResource(R.color.light_mint),
                colorResource(R.color.mint)
            ),
            tileMode = TileMode.Mirror
        ))
    ) { page ->
        val distanceFromCurrentPage = (
                (pagerState.currentPage - page) + pagerState
                    .currentPageOffsetFraction
                ).absoluteValue
        Box(
            modifier = Modifier
                .scale(max(0.75, 1.0 - distanceFromCurrentPage).toFloat())
                .alpha(max(0.5, 1.0 - distanceFromCurrentPage).toFloat())
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(28.dp)
        ) {
            pages[page]()
        }
    }
}