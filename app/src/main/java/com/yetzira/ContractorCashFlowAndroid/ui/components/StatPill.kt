package com.yetzira.ContractorCashFlowAndroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProShapes
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProSpacing

/**
 * StatPill - Small metric display with value and label
 * Usage: StatPill(value = "₪5,000", label = "Revenue", backgroundColor = Color.Green)
 */
@Composable
fun StatPill(
    value: String,
    label: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(backgroundColor, shape = KablanProShapes.medium)
            .padding(KablanProSpacing.md)
    ) {
        Text(
            text = "$value\n$label",
            color = textColor,
            fontSize = 12.sp
        )
    }
}

