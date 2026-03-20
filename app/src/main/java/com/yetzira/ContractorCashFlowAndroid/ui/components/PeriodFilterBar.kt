package com.yetzira.ContractorCashFlowAndroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProShapes
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProSpacing

/**
 * PeriodFilterBar - Horizontal filter bar for selecting time period
 * Usage: PeriodFilterBar(selectedPeriod = "Month", onPeriodChange = { })
 */
@Composable
fun PeriodFilterBar(
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    backgroundColor: Color = Color.White,
    textColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    val periods = listOf("Week", "Month", "Quarter", "Year")

    Row(
        modifier = modifier
            .background(backgroundColor, shape = KablanProShapes.medium)
            .padding(KablanProSpacing.sm)
    ) {
        periods.forEach { period ->
            Button(
                onClick = { onPeriodChange(period) },
                modifier = Modifier.padding(KablanProSpacing.xs)
            ) {
                Text(period, color = textColor)
            }
        }
    }
}

