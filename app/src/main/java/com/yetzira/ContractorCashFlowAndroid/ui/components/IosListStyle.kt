package com.yetzira.ContractorCashFlowAndroid.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val IosGroupedBackground = Color(0xFFF2F2F7)
val IosGroupedCard = Color(0xFFFFFFFF)
val IosSeparator = Color(0x1F3C3C43)

fun groupedRowShape(index: Int, lastIndex: Int): RoundedCornerShape {
    val radius = 14.dp
    return when {
        index == 0 && index == lastIndex -> RoundedCornerShape(radius)
        index == 0 -> RoundedCornerShape(topStart = radius, topEnd = radius, bottomStart = 0.dp, bottomEnd = 0.dp)
        index == lastIndex -> RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = radius, bottomEnd = radius)
        else -> RoundedCornerShape(0.dp)
    }
}

