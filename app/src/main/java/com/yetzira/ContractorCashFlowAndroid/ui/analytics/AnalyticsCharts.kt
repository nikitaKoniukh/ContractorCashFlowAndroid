package com.yetzira.ContractorCashFlowAndroid.ui.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProColors

@Composable
fun VicoMonthlyTrendChart(
    points: List<MonthlyTrendPointUi>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val incomeLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(KablanProColors.IncomeGreen))
    )
    val expenseLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(KablanProColors.ExpenseRed))
    )

    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineSeries {
                series(points.map { it.income })
                series(points.map { it.expenses })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(incomeLine, expenseLine)
            )
        ),
        modelProducer = modelProducer,
        modifier = modifier
    )
}
