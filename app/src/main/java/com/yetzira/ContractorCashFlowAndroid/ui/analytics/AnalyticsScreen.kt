package com.yetzira.ContractorCashFlowAndroid.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.ui.components.AnalyticsCard
import com.yetzira.ContractorCashFlowAndroid.ui.components.PeriodFilterBar
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProColors
import kotlin.math.max
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PeriodFilterBar(
            options = AnalyticsPeriod.entries.toList(),
            selectedOption = state.selectedPeriod,
            onOptionSelected = viewModel::setSelectedPeriod,
            optionLabel = { it.shortLabel },
            modifier = Modifier.fillMaxWidth()
        )

        KpiRow(state = state)
        IncomeExpenseDonutCard(state = state)

        if (state.showMonthlyTrend) {
            MonthlyTrendCard(state = state)
        }

        InvoiceStatusCard(state = state)
        ExpensesByCategoryCard(state = state)
        BudgetUtilizationCard(state = state)
        TopProjectsCard(state = state)
    }
}

@Composable
private fun KpiRow(state: AnalyticsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnalyticsMetricCard(
            modifier = Modifier.weight(1f),
            title = "Net Balance",
            value = formatCurrency(state.netBalance, state.currency),
            accentColor = if (state.netBalance >= 0.0) KablanProColors.IncomeGreen else KablanProColors.ExpenseRed
        )
        AnalyticsMetricCard(
            modifier = Modifier.weight(1f),
            title = "Overdue",
            value = formatCurrency(state.overdueAmount, state.currency),
            accentColor = if (state.overdueAmount > 0.0) KablanProColors.ExpenseRed else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AnalyticsMetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    AnalyticsCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

@Composable
private fun IncomeExpenseDonutCard(state: AnalyticsUiState) {
    AnalyticsCard {
        SectionTitle(title = "Income vs Expenses")
        if (!state.hasDonutData) {
            EmptySection(message = "No income or expense activity for the selected period.")
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(
                        income = state.totalIncome,
                        expenses = state.totalExpenses,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Net Balance",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(state.netBalance, state.currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (state.netBalance >= 0.0) KablanProColors.IncomeGreen else KablanProColors.ExpenseRed
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LegendRow(
                        label = "Income",
                        value = formatCurrency(state.totalIncome, state.currency),
                        color = KablanProColors.IncomeGreen
                    )
                    LegendRow(
                        label = "Expenses",
                        value = formatCurrency(state.totalExpenses, state.currency),
                        color = KablanProColors.ExpenseRed
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    income: Double,
    expenses: Double,
    modifier: Modifier = Modifier
) {
    val total = income + expenses
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier.padding(8.dp)) {
        val strokeWidth = size.minDimension * 0.16f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        if (total > 0.0) {
            var startAngle = -90f
            listOf(
                income to KablanProColors.IncomeGreen,
                expenses to KablanProColors.ExpenseRed
            ).forEach { (value, color) ->
                if (value > 0.0) {
                    val sweep = ((value / total) * 360.0).toFloat()
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
        }
    }
}

@Composable
private fun MonthlyTrendCard(state: AnalyticsUiState) {
    AnalyticsCard {
        SectionTitle(title = "Monthly Trend")
        if (state.monthlyTrend.isEmpty()) {
            EmptySection(message = "No monthly trend is available for this period yet.")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                VicoMonthlyTrendChart(
                    points = state.monthlyTrend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    state.monthlyTrend.forEach { point ->
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendRow(
                    label = "Income",
                    value = formatCurrency(state.totalIncome, state.currency),
                    color = KablanProColors.IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                LegendRow(
                    label = "Expenses",
                    value = formatCurrency(state.totalExpenses, state.currency),
                    color = KablanProColors.ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InvoiceStatusCard(state: AnalyticsUiState) {
    AnalyticsCard {
        SectionTitle(title = "Invoice Status")

        if (state.invoiceStatusTotal <= 0.0) {
            EmptySection(message = "No invoices match the selected period.")
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .padding(top = 6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            ) {
                state.invoiceStatus.forEachIndexed { index, segment ->
                    if (segment.amount > 0.0) {
                        Box(
                            modifier = Modifier
                                .weight(segment.amount.toFloat())
                                .fillMaxSize()
                                .background(
                                    color = segment.color,
                                    shape = when (index) {
                                        0 -> RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                                        state.invoiceStatus.lastIndex -> RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
                                        else -> RoundedCornerShape(0.dp)
                                    }
                                )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.invoiceStatus.forEach { segment ->
                    LegendRow(
                        label = segment.label,
                        value = "${formatCurrency(segment.amount, state.currency)} • ${formatPercent(segment.percentage)}",
                        color = segment.color
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpensesByCategoryCard(state: AnalyticsUiState) {
    AnalyticsCard {
        SectionTitle(title = "Expenses by Category")
        if (state.expensesByCategory.isEmpty()) {
            EmptySection(message = "No expense categories are available for this period.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val maxAmount = state.expensesByCategory.maxOfOrNull { it.amount } ?: 1.0
                state.expensesByCategory.forEach { category ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = formatCurrency(category.amount, state.currency),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalValueBar(
                            fillFraction = (category.amount / maxAmount).toFloat(),
                            color = category.color,
                            trailingLabel = formatPercent(category.percentage)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalValueBar(
    fillFraction: Float,
    color: Color,
    trailingLabel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(7.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillFraction.coerceIn(0f, 1f))
                    .height(14.dp)
                    .background(color, RoundedCornerShape(7.dp))
            )
        }
        Text(
            text = trailingLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BudgetUtilizationCard(state: AnalyticsUiState) {
    val averageColor = when {
        state.averageBudgetUtilization >= 85.0 -> KablanProColors.ExpenseRed
        state.averageBudgetUtilization >= 60.0 -> KablanProColors.PendingOrange
        else -> KablanProColors.IncomeGreen
    }

    AnalyticsCard {
        SectionTitle(title = "Budget Utilization")
        Text(
            text = "Average utilization: ${String.format(Locale.getDefault(), "%.0f%%", state.averageBudgetUtilization)}",
            style = MaterialTheme.typography.titleSmall,
            color = averageColor,
            fontWeight = FontWeight.SemiBold
        )

        if (state.budgetUtilization.isEmpty()) {
            EmptySection(message = "No projects with a budget are available yet.")
        } else {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.budgetUtilization.forEach { item ->
                    BudgetProjectRow(item = item, currency = state.currency)
                }
            }
        }
    }
}

@Composable
private fun BudgetProjectRow(item: ProjectBudgetUi, currency: CurrencyOption) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.projectName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format(Locale.getDefault(), "%.0f%%", item.utilization),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BudgetBar(
            spentFraction = if (item.budget > 0.0) (item.spent / item.budget).toFloat() else 0f,
            remainingFraction = if (item.budget > 0.0) (item.remaining / item.budget).toFloat() else 0f
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Spent ${formatCurrency(item.spent, currency)}",
                style = MaterialTheme.typography.labelMedium,
                color = KablanProColors.PendingOrange
            )
            Text(
                text = "Remaining ${formatCurrency(item.remaining, currency)}",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF8FD3FF)
            )
        }
    }
}

@Composable
private fun BudgetBar(
    spentFraction: Float,
    remainingFraction: Float,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(spentFraction.coerceIn(0f, 1f))
                    .height(12.dp)
                    .background(KablanProColors.PendingOrange, RoundedCornerShape(6.dp))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(remainingFraction.coerceIn(0f, 1f))
                    .height(12.dp)
                    .background(Color(0xFF8FD3FF), RoundedCornerShape(6.dp))
            )
        }
    }
}

@Composable
private fun TopProjectsCard(state: AnalyticsUiState) {
    AnalyticsCard {
        SectionTitle(title = "Top Projects")
        if (state.topProjects.isEmpty()) {
            EmptySection(message = "No project income has been recorded for this period.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.topProjects.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${item.rank}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(40.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.projectName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = item.clientName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(item.income, state.currency),
                                color = KablanProColors.IncomeGreen,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = formatSignedCurrency(item.balanceDelta, state.currency),
                                color = if (item.balanceDelta >= 0.0) KablanProColors.IncomeGreen else KablanProColors.ExpenseRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color = color, shape = RoundedCornerShape(5.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun EmptySection(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
}

private fun formatCurrency(amount: Double, currency: CurrencyOption): String {
    return "${String.format(Locale.getDefault(), "%,.2f", amount)} ${currency.symbol}"
}

private fun formatSignedCurrency(amount: Double, currency: CurrencyOption): String {
    val prefix = if (amount >= 0.0) "+" else "−"
    return prefix + formatCurrency(kotlin.math.abs(amount), currency)
}

private fun formatPercent(value: Float): String = String.format(Locale.getDefault(), "%.0f%%", value)

