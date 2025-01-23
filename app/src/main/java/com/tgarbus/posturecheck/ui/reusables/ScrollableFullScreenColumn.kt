package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tgarbus.posturecheck.R

@Composable
fun ScrollableFullScreenColumn(
    topSpace: Dp = 30.dp,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.light_mint))
    ) {
        Column(modifier = Modifier.height(topSpace)) {  }
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .safeDrawingPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = verticalArrangement,
        ) {
            content()

            // Allow more scroll.
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}