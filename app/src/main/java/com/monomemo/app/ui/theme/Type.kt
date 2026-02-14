package com.monomemo.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.monomemo.app.R

val D2CodingFamily = FontFamily(
    Font(R.font.d2coding, FontWeight.Normal),
    Font(R.font.d2coding_bold, FontWeight.Bold),
)

private val defaultText = TextStyle(fontFamily = D2CodingFamily)

val Typography = Typography(
    displayLarge = defaultText.copy(fontSize = 57.sp, fontWeight = FontWeight.Normal),
    displayMedium = defaultText.copy(fontSize = 45.sp, fontWeight = FontWeight.Normal),
    displaySmall = defaultText.copy(fontSize = 36.sp, fontWeight = FontWeight.Normal),
    headlineLarge = defaultText.copy(fontSize = 32.sp, fontWeight = FontWeight.Normal),
    headlineMedium = defaultText.copy(fontSize = 28.sp, fontWeight = FontWeight.Normal),
    headlineSmall = defaultText.copy(fontSize = 24.sp, fontWeight = FontWeight.Normal),
    titleLarge = defaultText.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = defaultText.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
    titleSmall = defaultText.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
    bodyLarge = defaultText.copy(fontSize = 16.sp),
    bodyMedium = defaultText.copy(fontSize = 14.sp),
    bodySmall = defaultText.copy(fontSize = 12.sp),
    labelLarge = defaultText.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
    labelMedium = defaultText.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
    labelSmall = defaultText.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
)
