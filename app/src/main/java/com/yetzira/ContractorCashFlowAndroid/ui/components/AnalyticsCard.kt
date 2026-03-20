package com.yetzira.ContractorCashFlowAndroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProShapes
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProSpacing

/**
 * AnalyticsCard - Card for displaying analytics data
 * Usage: AnalyticsCard(title = "Total Expenses", content = "₪15,000", backgroundColor = Color.Blue)
 */
@Composable
fun AnalyticsCard(
    title: String,
    content: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = KablanProShapes.medium)
            .padding(KablanProSpacing.lg)
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 12.sp
        )
        Text(
            text = content,
            color = textColor,
            fontSize = 20.sp
        )
    }
}

