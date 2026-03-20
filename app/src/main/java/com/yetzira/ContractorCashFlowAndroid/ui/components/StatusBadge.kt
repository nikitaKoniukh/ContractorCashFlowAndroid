package com.yetzira.ContractorCashFlowAndroid.ui.components

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * StatusBadge - Displays status with background color and text
 * Usage: StatusBadge(status = "Paid", backgroundColor = Color.Green)
 */
@Composable
fun StatusBadge(
    status: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        color = backgroundColor,
        modifier = modifier
    ) {
        Text(
            text = status,
            color = textColor
        )
    }
}

