package com.yetzira.ContractorCashFlowAndroid.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Custom text styles (per ANDROID_UI_GUIDE §3) ──

/** Financial summary net balance — iOS .system(size:34, weight:.bold, design:.rounded) */
val BalanceTextStyle = TextStyle(
    fontSize = 34.sp,
    fontWeight = FontWeight.SemiBold,
    fontFamily = FontFamily.Default,
    letterSpacing = (-0.4).sp
)

/** KPI card values — iOS .title3 + .bold (20sp) */
val KpiValueBold = TextStyle(
    fontSize = 20.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = (-0.2).sp
)

/** Secondary KPI values — iOS .title3 + .semibold (20sp) */
val KpiValueSemibold = TextStyle(
    fontSize = 20.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = (-0.2).sp
)

/** Status badge / type badge text — iOS .caption2 + .medium (11sp) */
val BadgeTextStyle = TextStyle(
    fontSize = 11.sp,
    fontWeight = FontWeight.Normal
)

/** Save badge on paywall — iOS .caption2 + .bold (11sp) */
val BadgeBoldStyle = TextStyle(
    fontSize = 11.sp,
    fontWeight = FontWeight.SemiBold
)

/** Stat pill values — iOS .caption + .semibold (12sp) */
val LabelSmallSemibold = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium
)

/** Section headers — iOS .caption + .medium + uppercase (12sp) */
val SectionHeaderStyle = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.4.sp
)

/** .subheadline + .semibold (15sp) — amount values */
val BodyMediumSemibold = TextStyle(
    fontSize = 15.sp,
    fontWeight = FontWeight.Medium
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)