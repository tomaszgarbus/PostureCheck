package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h1
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h4

@Composable
fun PageHeader(text: String, subtitleText: String? = null, modifier: Modifier = Modifier.fillMaxWidth()) {
    Column (
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(
            text,
            style = h1.copy(color = colorResource(R.color.dark_green))
        )
        if (subtitleText != null) {
            Text(
                subtitleText,
                style = h4.copy(color = colorResource(R.color.subtitle_gray))
            )
        }
    }
}
