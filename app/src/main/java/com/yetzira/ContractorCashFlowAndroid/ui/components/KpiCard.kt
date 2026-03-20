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
 * KpiCard - Key Performance Indicator card with metric and trend
 * Usage: KpiCard(metric = "Budget Used", value = "65%", trend = "+5%", color = Color.Blue)
 */
@Composable
fun KpiCard(
    metric: String,
    value: String,
    trend: String? = null,
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
            text = metric,
            color = textColor,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = textColor,
            fontSize = 24.sp
        )
        if (trend != null) {
            Text(
                text = trend,
                color = textColor,
                fontSize = 11.sp
            )
        }
    }
}

