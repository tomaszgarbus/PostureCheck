package com.tgarbus.posturecheck.ui

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tgarbus.posturecheck.ui.Fonts.Companion.mulishFontFamily

class TextStyles {
    companion object {
        val h1 = TextStyle(
            fontSize = 24.sp,
            lineHeight = 28.8.sp,
            fontFamily = mulishFontFamily,
            fontWeight = FontWeight.Bold,
        )
        val h2 = TextStyle(
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontFamily = mulishFontFamily,
            fontWeight = FontWeight.Bold,
        )
        val h3 = TextStyle(
            fontSize = 16.sp,
            lineHeight = 19.2.sp,
            fontFamily = mulishFontFamily,
            fontWeight = FontWeight.Bold,
        )
        val h4 = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.8.sp,
            fontFamily = mulishFontFamily,
            fontWeight = FontWeight.Bold,
        )
    }
}