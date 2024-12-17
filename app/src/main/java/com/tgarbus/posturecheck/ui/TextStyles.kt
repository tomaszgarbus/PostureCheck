package com.tgarbus.posturecheck.ui

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tgarbus.posturecheck.ui.Fonts.Companion.mulishFontFamily

class TextStyles {
    companion object {
        val h4 = TextStyle(
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontFamily = mulishFontFamily,
            fontWeight = FontWeight.Bold,
        )

    }
}