package com.tgarbus.posturecheck.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.tgarbus.posturecheck.R

class Fonts {
    companion object {
        val mulishFontFamily = FontFamily(
            Font(R.font.mulish_regular, FontWeight.Normal),
            Font(R.font.mulish_bold, FontWeight.Bold),
            Font(R.font.mulish_light, FontWeight.Thin),
            Font(R.font.mulish_italic, FontWeight.Normal, FontStyle.Italic),
            Font(R.font.mulish_bolditalic, FontWeight.Bold, FontStyle.Italic)
        )
        val aboretoFontFamily = FontFamily(
            Font(R.font.aboreto_regular, FontWeight.Normal)
        )
    }
}